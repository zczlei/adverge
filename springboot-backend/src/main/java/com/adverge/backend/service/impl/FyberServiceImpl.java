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
 * Fyber广告平台服务实现
 */
@Slf4j
@Service
public class FyberServiceImpl extends AbstractAdNetworkService {

    private static final String PLATFORM_NAME = "Fyber";
    
    public FyberServiceImpl(RestTemplate restTemplate, Config config) {
        super(restTemplate);
        
        // 从配置中获取Fyber平台的配置信息
        Config.Platform fyberConfig = config.getPlatforms().stream()
                .filter(p -> PLATFORM_NAME.equalsIgnoreCase(p.getName()))
                .findFirst()
                .orElse(null);
        
        if (fyberConfig != null) {
            this.apiUrl = "https://engine.fyber.com/hb";
            this.appId = fyberConfig.getAppId();
            this.appKey = fyberConfig.getAppKey();
            this.placementId = fyberConfig.getPlacementId();
            this.bidFloor = fyberConfig.getBidFloor();
        } else {
            log.warn("Fyber平台配置未找到");
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
                log.debug("向Fyber发送竞价请求");
                
                if (appId == null || appKey == null) {
                    log.warn("Fyber平台配置不完整，跳过竞价");
                    return null;
                }
                
                // 构建请求参数
                Object bidRequest = buildBidRequest(adRequest);
                
                // 构建请求头
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("X-Fyber-App-Token", appKey);
                headers.set("X-Fyber-App-Id", appId);
                
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
                log.error("Fyber竞价请求失败", e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> notifyWin(String bidToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("通知Fyber竞价胜出: {}", bidToken);
                
                if (appId == null || appKey == null) {
                    log.warn("Fyber平台配置不完整，跳过通知");
                    return false;
                }
                
                // 构建请求参数
                Map<String, Object> notifyRequest = new HashMap<>();
                notifyRequest.put("bid_id", bidToken);
                
                // 构建请求头
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("X-Fyber-App-Token", appKey);
                headers.set("X-Fyber-App-Id", appId);
                
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
                log.error("通知Fyber竞价胜出失败", e);
                return false;
            }
        });
    }

    @Override
    protected Object buildBidRequest(AdRequest adRequest) {
        Map<String, Object> bidRequest = new HashMap<>();
        
        // 基本信息
        bidRequest.put("app_id", appId);
        bidRequest.put("placement_id", placementId);
        bidRequest.put("bid_id", generateBidId());
        bidRequest.put("bid_floor", bidFloor);
        
        // App信息
        Map<String, Object> app = new HashMap<>();
        app.put("id", appId);
        app.put("bundle", "com.adverge.app");
        app.put("name", "AdVerge Sample App");
        bidRequest.put("app", app);
        
        // 广告位信息
        Map<String, Object> placement = new HashMap<>();
        placement.put("id", placementId);
        placement.put("width", 320);
        placement.put("height", 50);
        placement.put("ad_format", "banner");
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
            String bidToken = bid.containsKey("bid_id") ? 
                    bid.get("bid_id").toString() : bidId;
            
            BidResponse.AdData adData = new BidResponse.AdData();
            
            if (bid.containsKey("creative")) {
                Map<String, Object> creative = (Map<String, Object>) bid.get("creative");
                adData.setAdId(bidId);
                adData.setTitle(creative.containsKey("title") ? 
                        creative.get("title").toString() : "Fyber广告");
                adData.setDescription(creative.containsKey("description") ? 
                        creative.get("description").toString() : "");
                adData.setImageUrl(creative.containsKey("main_image") ? 
                        creative.get("main_image").toString() : "");
                adData.setIconUrl(creative.containsKey("icon") ? 
                        creative.get("icon").toString() : "");
                adData.setCtaText(creative.containsKey("cta_text") ? 
                        creative.get("cta_text").toString() : "点击查看");
                adData.setLandingUrl(creative.containsKey("click_url") ? 
                        creative.get("click_url").toString() : "");
            } else {
                adData.setAdId(bidId);
                adData.setTitle("Fyber广告");
                adData.setCtaText("点击查看");
            }
            
            return BidResponse.builder()
                    .source(PLATFORM_NAME)
                    .price(price)
                    .adData(adData)
                    .bidToken(bidToken)
                    .build();
        } catch (Exception e) {
            log.error("解析Fyber竞价响应失败", e);
            return null;
        }
    }
} 