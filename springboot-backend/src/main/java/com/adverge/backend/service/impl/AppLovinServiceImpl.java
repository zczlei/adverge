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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * AppLovin广告平台服务实现
 */
@Slf4j
@Service
public class AppLovinServiceImpl extends AbstractAdNetworkService {

    private static final String PLATFORM_NAME = "AppLovin";
    
    public AppLovinServiceImpl(RestTemplate restTemplate, Config config) {
        super(restTemplate);
        
        // 从配置中获取AppLovin平台的配置信息
        Config.Platform appLovinConfig = config.getPlatforms().stream()
                .filter(p -> PLATFORM_NAME.equalsIgnoreCase(p.getName()))
                .findFirst()
                .orElse(null);
        
        if (appLovinConfig != null) {
            this.apiUrl = "https://a.applovin.com/bidding";
            this.appId = appLovinConfig.getAppId();
            this.appKey = appLovinConfig.getAppKey();
            this.placementId = appLovinConfig.getPlacementId();
            this.bidFloor = appLovinConfig.getBidFloor();
        } else {
            log.warn("AppLovin平台配置未找到");
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
                log.debug("向AppLovin发送竞价请求");
                
                if (appId == null || appKey == null) {
                    log.warn("AppLovin平台配置不完整，跳过竞价");
                    return null;
                }
                
                // 构建请求参数
                Object bidRequest = buildBidRequest(adRequest);
                
                // 构建请求头
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("X-AppLovin-SDK-Key", appKey);
                
                // 发送请求
                HttpEntity<Object> request = new HttpEntity<>(bidRequest, headers);
                ResponseEntity<Map> response = restTemplate.exchange(
                        apiUrl + "/bid", 
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
                log.error("AppLovin竞价请求失败", e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> notifyWin(String bidToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("通知AppLovin竞价胜出: {}", bidToken);
                
                if (appId == null || appKey == null) {
                    log.warn("AppLovin平台配置不完整，跳过通知");
                    return false;
                }
                
                // 构建请求参数
                Map<String, Object> notifyRequest = new HashMap<>();
                notifyRequest.put("bid_token", bidToken);
                notifyRequest.put("zone_id", placementId);
                
                // 构建请求头
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("X-AppLovin-SDK-Key", appKey);
                
                // 发送请求
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(notifyRequest, headers);
                ResponseEntity<Map> response = restTemplate.exchange(
                        apiUrl + "/win", 
                        HttpMethod.POST, 
                        request, 
                        Map.class
                );
                
                return response.getStatusCode().is2xxSuccessful();
            } catch (Exception e) {
                log.error("通知AppLovin竞价胜出失败", e);
                return false;
            }
        });
    }

    @Override
    protected Object buildBidRequest(AdRequest adRequest) {
        Map<String, Object> bidRequest = new HashMap<>();
        
        // 基本信息
        bidRequest.put("sdk_key", appKey);
        bidRequest.put("zone_id", placementId);
        bidRequest.put("bid_id", generateBidId());
        bidRequest.put("bid_floor", bidFloor);
        
        // App信息
        Map<String, Object> app = new HashMap<>();
        app.put("bundle", "com.adverge.app"); 
        app.put("version", "1.0.0");
        bidRequest.put("app", app);
        
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
        if (adRequest.getUserData() != null) {
            Map<String, Object> user = new HashMap<>();
            if (adRequest.getUserData().getGeo() != null) {
                Map<String, Object> geo = new HashMap<>();
                geo.put("country", adRequest.getUserData().getGeo().getCountry());
                geo.put("region", adRequest.getUserData().getGeo().getRegion());
                geo.put("city", adRequest.getUserData().getGeo().getCity());
                user.put("geo", geo);
            }
            bidRequest.put("user", user);
        }
        
        // 广告格式
        String adType = "banner";
        if (adRequest.getType() != null) {
            if (adRequest.getType().contains("interstitial")) {
                adType = "interstitial";
            } else if (adRequest.getType().contains("rewarded")) {
                adType = "rewarded";
            }
        }
        bidRequest.put("format", adType);
        
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
            String bidToken = bid.containsKey("token") ? 
                    bid.get("token").toString() : UUID.randomUUID().toString();
            
            BidResponse.AdData adData = new BidResponse.AdData();
            
            if (bid.containsKey("ad")) {
                Map<String, Object> ad = (Map<String, Object>) bid.get("ad");
                adData.setAdId(bidId);
                adData.setTitle(ad.containsKey("title") ? 
                        ad.get("title").toString() : "AppLovin广告");
                adData.setDescription(ad.containsKey("description") ? 
                        ad.get("description").toString() : "");
                adData.setImageUrl(ad.containsKey("main_image") ? 
                        ad.get("main_image").toString() : "");
                adData.setIconUrl(ad.containsKey("icon") ? 
                        ad.get("icon").toString() : "");
                adData.setCtaText(ad.containsKey("cta_text") ? 
                        ad.get("cta_text").toString() : "点击查看");
                adData.setLandingUrl(ad.containsKey("click_url") ? 
                        ad.get("click_url").toString() : "");
            } else {
                adData.setAdId(bidId);
                adData.setTitle("AppLovin广告");
                adData.setCtaText("点击查看");
            }
            
            return BidResponse.builder()
                    .source(PLATFORM_NAME)
                    .price(price)
                    .adData(adData)
                    .bidToken(bidToken)
                    .build();
        } catch (Exception e) {
            log.error("解析AppLovin竞价响应失败", e);
            return null;
        }
    }
} 