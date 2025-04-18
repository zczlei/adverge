package com.adverge.backend.service.impl;

import com.adverge.backend.dto.AdRequest;
import com.adverge.backend.dto.BidResponse;
import com.adverge.backend.model.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Chartboost广告平台服务实现
 */
@Slf4j
@Service
public class ChartboostServiceImpl extends AbstractAdNetworkService {

    private static final String PLATFORM_NAME = "Chartboost";
    
    public ChartboostServiceImpl(RestTemplate restTemplate, Config config) {
        super(restTemplate);
        
        // 从配置中获取Chartboost平台的配置信息
        Config.Platform chartboostConfig = config.getPlatforms().stream()
                .filter(p -> PLATFORM_NAME.equalsIgnoreCase(p.getName()))
                .findFirst()
                .orElse(null);
        
        if (chartboostConfig != null) {
            this.apiUrl = "https://bid.chartboost.com";
            this.appId = chartboostConfig.getAppId();
            this.appKey = chartboostConfig.getAppKey();
            this.placementId = chartboostConfig.getPlacementId();
            this.bidFloor = chartboostConfig.getBidFloor();
        } else {
            log.warn("Chartboost平台配置未找到");
            this.bidFloor = 0.0;
        }
    }

    @Override
    public String getPlatformName() {
        return PLATFORM_NAME;
    }

    @Override
    public CompletableFuture<BidResponse> bid(AdRequest adRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("向Chartboost发送竞价请求");
                
                if (appId == null || appKey == null) {
                    log.warn("Chartboost平台配置不完整，跳过竞价");
                    return null;
                }
                
                // 构建请求参数
                Object bidRequest = buildBidRequest(adRequest);
                
                // 构建请求头
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("X-Chartboost-App-ID", appId);
                headers.set("X-Chartboost-App-Signature", appKey);
                headers.set("X-Chartboost-API-Version", "1.0");
                
                // 发送请求
                HttpEntity<Object> request = new HttpEntity<>(bidRequest, headers);
                ResponseEntity<Map> response = restTemplate.exchange(
                        apiUrl + "/api/bid", 
                        HttpMethod.POST, 
                        request, 
                        Map.class
                );
                
                // 解析响应
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return parseBidResponse(response.getBody());
                }
                
                return null;
            } catch (Exception e) {
                log.error("Chartboost竞价请求失败", e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> notifyWin(String bidToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("通知Chartboost竞价胜出: {}", bidToken);
                
                if (appId == null || appKey == null) {
                    log.warn("Chartboost平台配置不完整，跳过通知");
                    return false;
                }
                
                // 构建请求参数
                Map<String, Object> notifyRequest = new HashMap<>();
                notifyRequest.put("token", bidToken);
                
                // 构建请求头
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("X-Chartboost-App-ID", appId);
                headers.set("X-Chartboost-App-Signature", appKey);
                
                // 发送请求
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(notifyRequest, headers);
                ResponseEntity<Map> response = restTemplate.exchange(
                        apiUrl + "/api/win", 
                        HttpMethod.POST, 
                        request, 
                        Map.class
                );
                
                return response.getStatusCode().is2xxSuccessful();
            } catch (Exception e) {
                log.error("通知Chartboost竞价胜出失败", e);
                return false;
            }
        });
    }

    @Override
    protected Object buildBidRequest(AdRequest adRequest) {
        Map<String, Object> bidRequest = new HashMap<>();
        
        // 请求ID和时间戳
        bidRequest.put("id", generateBidId());
        bidRequest.put("timestamp", Instant.now().getEpochSecond());
        
        // App信息
        Map<String, Object> app = new HashMap<>();
        app.put("bundle", "com.adverge.app");
        app.put("name", "AdVerge Sample App");
        app.put("id", appId);
        bidRequest.put("app", app);
        
        // 广告位信息
        Map<String, Object> placement = new HashMap<>();
        placement.put("id", placementId);
        placement.put("bidfloor", bidFloor);
        
        // 广告格式
        Map<String, Object> format = new HashMap<>();
        format.put("type", "banner");
        format.put("width", 320);
        format.put("height", 50);
        placement.put("format", format);
        
        bidRequest.put("placement", placement);
        
        // 设备信息
        Map<String, Object> device = new HashMap<>();
        if (adRequest.getDeviceInfo() != null) {
            device.put("os", adRequest.getDeviceInfo().getOs());
            device.put("os_version", adRequest.getDeviceInfo().getOsVersion());
            device.put("model", adRequest.getDeviceInfo().getModel());
            device.put("manufacturer", adRequest.getDeviceInfo().getManufacturer());
            
            // 屏幕信息
            if (adRequest.getDeviceInfo().getScreenWidth() != null && 
                adRequest.getDeviceInfo().getScreenHeight() != null) {
                device.put("screen_width", adRequest.getDeviceInfo().getScreenWidth());
                device.put("screen_height", adRequest.getDeviceInfo().getScreenHeight());
            }
            
            // 语言信息
            if (adRequest.getDeviceInfo().getLanguage() != null) {
                device.put("language", adRequest.getDeviceInfo().getLanguage());
            }
        }
        bidRequest.put("device", device);
        
        // 用户信息
        Map<String, Object> user = new HashMap<>();
        if (adRequest.getUserData() != null) {
            if (adRequest.getUserData().getGeo() != null) {
                Map<String, Object> geo = new HashMap<>();
                geo.put("country", adRequest.getUserData().getGeo().getCountry());
                geo.put("region", adRequest.getUserData().getGeo().getRegion());
                geo.put("city", adRequest.getUserData().getGeo().getCity());
                user.put("geo", geo);
            }
        }
        bidRequest.put("user", user);
        
        return bidRequest;
    }

    @Override
    protected BidResponse parseBidResponse(Object responseObj) {
        try {
            Map<String, Object> response = (Map<String, Object>) responseObj;
            
            if (response == null || !response.containsKey("bid")) {
                return null;
            }
            
            Map<String, Object> bid = (Map<String, Object>) response.get("bid");
            if (bid == null || !bid.containsKey("price")) {
                return null;
            }
            
            Double price = Double.parseDouble(bid.get("price").toString());
            String bidId = bid.containsKey("id") ? 
                    bid.get("id").toString() : UUID.randomUUID().toString();
            String bidToken = bid.containsKey("nurl") ? 
                    bid.get("nurl").toString() : UUID.randomUUID().toString();
            
            BidResponse.AdData adData = new BidResponse.AdData();
            
            if (bid.containsKey("adm")) {
                Map<String, Object> adm = (Map<String, Object>) bid.get("adm");
                adData.setAdId(bidId);
                adData.setTitle(adm.containsKey("title") ? 
                        adm.get("title").toString() : "Chartboost广告");
                adData.setDescription(adm.containsKey("desc") ? 
                        adm.get("desc").toString() : "");
                adData.setImageUrl(adm.containsKey("img_url") ? 
                        adm.get("img_url").toString() : "");
                adData.setIconUrl(adm.containsKey("icon_url") ? 
                        adm.get("icon_url").toString() : "");
                adData.setCtaText(adm.containsKey("cta") ? 
                        adm.get("cta").toString() : "点击查看");
                adData.setLandingUrl(adm.containsKey("click_url") ? 
                        adm.get("click_url").toString() : "");
            } else {
                adData.setAdId(bidId);
                adData.setTitle("Chartboost广告");
                adData.setCtaText("点击查看");
            }
            
            return BidResponse.builder()
                    .source(PLATFORM_NAME)
                    .price(price)
                    .adData(adData)
                    .bidToken(bidToken)
                    .build();
        } catch (Exception e) {
            log.error("解析Chartboost竞价响应失败", e);
            return null;
        }
    }
} 