package com.adverge.backend.service.impl;

import com.adverge.backend.dto.AdEventDto;
import com.adverge.backend.dto.AdRequest;
import com.adverge.backend.model.Metrics;
import com.adverge.backend.repository.MetricsRepository;
import com.adverge.backend.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 事件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final MetricsRepository metricsRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void processEvent(AdEventDto event) {
        try {
            // 设置事件时间
            if (event.getEventTime() == null) {
                event.setEventTime(new Date());
            }
            
            // 发送事件到Kafka
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("ad-events", eventJson);
            
            // 创建指标记录
            Metrics metrics = new Metrics();
            metrics.setPlacementId(event.getAdUnitId());
            metrics.setPlatform(event.getPlatform());
            metrics.setPrice(event.getPrice());
            metrics.setTimestamp(event.getEventTime());
            
            // 保存到数据库
            metricsRepository.save(metrics);
            
            // 根据事件类型更新Redis统计数据
            updateRedisStats(event);
            
        } catch (Exception e) {
            log.error("处理广告事件失败", e);
        }
    }
    
    @Override
    public void logRequestEvent(String appId, String adUnitId, Object deviceInfo) {
        AdEventDto event = AdEventDto.builder()
                .eventType(AdEventDto.EventType.REQUEST)
                .appId(appId)
                .adUnitId(adUnitId)
                .eventTime(new Date())
                .deviceInfo(deviceInfo instanceof AdRequest.DeviceInfo ? 
                        (AdRequest.DeviceInfo) deviceInfo : null)
                .build();
        
        processEvent(event);
    }
    
    @Override
    public void logBidEvent(String appId, String adUnitId, String platform, double price) {
        AdEventDto event = AdEventDto.builder()
                .eventType(AdEventDto.EventType.BID)
                .appId(appId)
                .adUnitId(adUnitId)
                .platform(platform)
                .price(price)
                .eventTime(new Date())
                .build();
        
        processEvent(event);
    }
    
    @Override
    public void logWinEvent(String appId, String adUnitId, String platform, Double price) {
        AdEventDto event = AdEventDto.builder()
                .eventType(AdEventDto.EventType.WIN)
                .appId(appId)
                .adUnitId(adUnitId)
                .platform(platform)
                .price(price != null ? price : 0.0)
                .eventTime(new Date())
                .build();
        
        processEvent(event);
    }
    
    @Override
    public void logImpressionEvent(String appId, String adUnitId, String platform, String adId) {
        AdEventDto event = AdEventDto.builder()
                .eventType(AdEventDto.EventType.IMPRESSION)
                .appId(appId)
                .adUnitId(adUnitId)
                .platform(platform)
                .adId(adId)
                .eventTime(new Date())
                .build();
        
        processEvent(event);
    }
    
    @Override
    public void logClickEvent(String appId, String adUnitId, String platform, String adId) {
        AdEventDto event = AdEventDto.builder()
                .eventType(AdEventDto.EventType.CLICK)
                .appId(appId)
                .adUnitId(adUnitId)
                .platform(platform)
                .adId(adId)
                .eventTime(new Date())
                .build();
        
        processEvent(event);
    }
    
    @Override
    public void logErrorEvent(String appId, String adUnitId, String platform, String errorMsg) {
        AdEventDto event = AdEventDto.builder()
                .eventType(AdEventDto.EventType.ERROR)
                .appId(appId)
                .adUnitId(adUnitId)
                .platform(platform)
                .data(errorMsg)
                .eventTime(new Date())
                .build();
        
        processEvent(event);
    }
    
    /**
     * 更新Redis统计数据
     * @param event 广告事件
     */
    private void updateRedisStats(AdEventDto event) {
        String platform = event.getPlatform();
        if (platform == null) {
            return;
        }
        
        String date = String.format("%tF", new Date()); // yyyy-MM-dd格式
        
        switch (event.getEventType()) {
            case REQUEST:
                redisTemplate.opsForValue().increment("stats:" + date + ":request:" + platform);
                break;
            case BID:
                redisTemplate.opsForValue().increment("stats:" + date + ":bid:" + platform);
                if (event.getPrice() > 0) {
                    // 记录出价总额和次数，用于计算平均出价
                    redisTemplate.opsForValue().increment("stats:" + date + ":bid_price:" + platform, 
                            (long) (event.getPrice() * 1000)); // 乘以1000避免浮点数精度问题
                    redisTemplate.opsForValue().increment("stats:" + date + ":bid_count:" + platform);
                }
                break;
            case IMPRESSION:
                redisTemplate.opsForValue().increment("stats:" + date + ":impression:" + platform);
                break;
            case CLICK:
                redisTemplate.opsForValue().increment("stats:" + date + ":click:" + platform);
                break;
            case ERROR:
                redisTemplate.opsForValue().increment("stats:" + date + ":error:" + platform);
                break;
            default:
                break;
        }
    }
} 