package com.adverge.backend.service.impl;

import com.adverge.backend.dto.AdRequest;
import com.adverge.backend.dto.BidResponse;
import com.adverge.backend.dto.TrackRequest;
import com.adverge.backend.model.AdUnit;
import com.adverge.backend.model.Config;
import com.adverge.backend.model.Metrics;
import com.adverge.backend.model.Platform;
import com.adverge.backend.repository.AdUnitRepository;
import com.adverge.backend.repository.ConfigRepository;
import com.adverge.backend.repository.MetricsRepository;
import com.adverge.backend.service.AdNetworkManager;
import com.adverge.backend.service.AdService;
import com.adverge.backend.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdServiceImpl implements AdService {

    private final ConfigRepository configRepository;
    private final MetricsRepository metricsRepository;
    private final AdUnitRepository adUnitRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final AdNetworkManager adNetworkManager;
    private final EventService eventService;
    
    @Value("${ad.bid.timeout:5000}")
    private int bidTimeout;
    
    @Value("${ad.bid.cache-expiry:300}")
    private int cacheExpiry;

    @Override
    public BidResponse getAd(String adUnitId, Map<String, String> options, HttpServletRequest request) {
        log.debug("获取广告请求: adUnitId={}, options={}", adUnitId, options);
        
        // 记录请求事件
        eventService.logRequestEvent(options.getOrDefault("appId", "unknown"), adUnitId, null);
        
        // 从Redis缓存获取广告数据
        String cachedBidKey = "bid:" + adUnitId;
        String cachedBid = redisTemplate.opsForValue().get(cachedBidKey);
        
        if (cachedBid != null) {
            try {
                BidResponse response = objectMapper.readValue(cachedBid, BidResponse.class);
                // 记录缓存命中事件
                if (response != null && response.getSource() != null) {
                    eventService.logBidEvent(
                            options.getOrDefault("appId", "unknown"),
                            adUnitId,
                            response.getSource(),
                            response.getPrice()
                    );
                }
                return response;
            } catch (Exception e) {
                log.error("解析缓存广告数据失败", e);
            }
        }
        
        // 如果没有缓存或缓存已过期，调用竞价方法获取新的广告
        try {
            // 查找AdUnit获取类型信息
            AdUnit adUnit = adUnitRepository.findById(adUnitId).orElse(null);
            if (adUnit == null) {
                log.warn("广告位不存在: {}", adUnitId);
                return null;
            }
            
            // 创建广告请求
            AdRequest adRequest = new AdRequest();
            adRequest.setAdUnitId(adUnitId);
            adRequest.setType(adUnit.getType());
            adRequest.setFloorPrice(adUnit.getFloorPrice());
            adRequest.setAppId(adUnit.getAppId());
            
            // 添加设备信息
            if (options.containsKey("deviceType") || options.containsKey("os")) {
                AdRequest.DeviceInfo deviceInfo = new AdRequest.DeviceInfo();
                deviceInfo.setType(options.getOrDefault("deviceType", "unknown"));
                deviceInfo.setOs(options.getOrDefault("os", "unknown"));
                adRequest.setDeviceInfo(deviceInfo);
            }
            
            // 调用竞价方法获取广告
            BidResponse bidResponse = bid(adUnitId, adRequest, request);
            
            if (bidResponse != null) {
                // 缓存竞价结果
                try {
                    String bidJson = objectMapper.writeValueAsString(bidResponse);
                    redisTemplate.opsForValue().set(cachedBidKey, bidJson, cacheExpiry, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.error("缓存广告数据失败", e);
                }
            }
            
            return bidResponse;
        } catch (Exception e) {
            log.error("获取广告失败", e);
            return null;
        }
    }

    @Override
    public BidResponse bid(String adUnitId, AdRequest adRequest, HttpServletRequest request) {
        log.debug("竞价请求: adUnitId={}, adRequest={}", adUnitId, adRequest);
        
        // 记录竞价请求事件
        eventService.logRequestEvent(adRequest.getAppId(), adUnitId, "bid");
        
        // 查找AdUnit获取类型信息（如果请求中没有提供）
        if (adRequest.getType() == null) {
            AdUnit adUnit = adUnitRepository.findById(adUnitId).orElse(null);
            if (adUnit == null) {
                log.warn("广告位不存在: {}", adUnitId);
                return null;
            }
            adRequest.setType(adUnit.getType());
            adRequest.setFloorPrice(adUnit.getFloorPrice());
            adRequest.setAppId(adUnit.getAppId());
        }
        
        try {
            // 向所有广告平台发送竞价请求
            CompletableFuture<List<BidResponse>> bidsFuture = adNetworkManager.bid(adRequest);
            
            // 等待广告平台响应，设置超时
            List<BidResponse> bids = bidsFuture.get(bidTimeout, TimeUnit.MILLISECONDS);
            
            // 过滤出有效响应
            List<BidResponse> validBids = bids.stream()
                    .filter(Objects::nonNull)
                    .filter(bid -> {
                        if (adRequest.getFloorPrice() == null) {
                            return true;
                        }
                        if (bid.getPrice() == null) {
                            return false;
                        }
                        return bid.getPrice() >= adRequest.getFloorPrice().doubleValue();
                    })
                    .collect(Collectors.toList());
            
            // 记录竞价事件
            validBids.forEach(bid -> {
                eventService.logBidEvent(adRequest.getAppId(), adUnitId, bid.getSource(), bid.getPrice());
            });
            
            // 选择价格最高的广告
            Optional<BidResponse> winner = validBids.stream()
                    .max(Comparator.comparingDouble(BidResponse::getPrice));
            
            if (winner.isPresent()) {
                BidResponse winnerBid = winner.get();
                
                // 通知胜出平台
                adNetworkManager.notifyWin(winnerBid.getSource(), winnerBid.getBidToken());
                
                // 记录胜出事件
                eventService.logWinEvent(adRequest.getAppId(), adUnitId, winnerBid.getSource(), winnerBid.getPrice());
                
                // 记录指标
                saveMetrics(adUnitId, winnerBid);
                
                return winnerBid;
            } else {
                log.info("无有效竞价: adUnitId={}", adUnitId);
                return null;
            }
        } catch (Exception e) {
            log.error("竞价请求失败", e);
            return null;
        }
    }

    @Override
    public void trackImpression(String adId, String platform, HttpServletRequest request) {
        log.debug("记录广告展示: adId={}, platform={}", adId, platform);
        
        try {
            // 更新指标
            Optional<Metrics> metricsOpt = metricsRepository.findByPlatformAndAdId(platform, adId);
            if (metricsOpt.isPresent()) {
                Metrics metrics = metricsOpt.get();
                metrics.setImpressions(metrics.getImpressions() + 1);
                metrics.setLastImpressionTime(new Date());
                metricsRepository.save(metrics);
            }
            
            // 发送Kafka事件
            Map<String, Object> event = new HashMap<>();
            event.put("type", "impression");
            event.put("adId", adId);
            event.put("platform", platform);
            event.put("timestamp", System.currentTimeMillis());
            
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("ad-events", eventJson);
        } catch (Exception e) {
            log.error("记录广告展示失败", e);
        }
    }

    @Override
    public void trackClick(String adId, TrackRequest trackRequest, HttpServletRequest request) {
        log.debug("记录广告点击: adId={}, platform={}, revenue={}", adId, trackRequest.getPlatform(), trackRequest.getRevenue());
        
        try {
            // 更新指标
            Optional<Metrics> metricsOpt = metricsRepository.findByPlatformAndAdId(trackRequest.getPlatform(), adId);
            if (metricsOpt.isPresent()) {
                Metrics metrics = metricsOpt.get();
                metrics.setClicks(metrics.getClicks() + 1);
                // 将 double 转换为 BigDecimal 后再相加
                if (metrics.getRevenue() == null) {
                    metrics.setRevenue(BigDecimal.valueOf(trackRequest.getRevenue()));
                } else {
                    metrics.setRevenue(metrics.getRevenue().add(BigDecimal.valueOf(trackRequest.getRevenue())));
                }
                metrics.setLastClickTime(new Date());
                metricsRepository.save(metrics);
            }
            
            // 发送Kafka事件
            Map<String, Object> event = new HashMap<>();
            event.put("type", "click");
            event.put("adId", adId);
            event.put("platform", trackRequest.getPlatform());
            event.put("revenue", trackRequest.getRevenue());
            event.put("timestamp", System.currentTimeMillis());
            
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("ad-events", eventJson);
        } catch (Exception e) {
            log.error("记录广告点击失败", e);
        }
    }

    /**
     * 保存指标数据
     */
    private void saveMetrics(String adUnitId, BidResponse bidResponse) {
        try {
            Metrics metrics = metricsRepository.findByPlatformAndAdId(bidResponse.getSource(), bidResponse.getAdId())
                    .orElse(new Metrics());
            
            // 如果是新创建的指标，设置ID和其他初始值
            if (metrics.getId() == null) {
                metrics.setId(UUID.randomUUID().toString());
                metrics.setPlacementId(bidResponse.getPlacementId());
                metrics.setBids(0);
                metrics.setWins(0);
                metrics.setImpressions(0);
                metrics.setClicks(0);
                metrics.setCreatedAt(new Date());
            }
            
            metrics.setPlatform(bidResponse.getSource());
            metrics.setAdId(bidResponse.getAdId());
            metrics.setAdUnitId(adUnitId);
            metrics.setBids(metrics.getBids() + 1);
            metrics.setWins(metrics.getWins() + 1);
            metrics.setPrice(bidResponse.getPrice());
            metrics.setLastBidTime(new Date());
            metrics.setLastWinTime(new Date());
            metrics.setUpdatedAt(new Date());
            
            metricsRepository.save(metrics);
        } catch (Exception e) {
            log.error("保存指标数据失败", e);
        }
    }
} 