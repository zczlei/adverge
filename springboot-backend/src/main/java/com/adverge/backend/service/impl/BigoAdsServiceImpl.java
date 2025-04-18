package com.adverge.backend.service.impl;

import com.adverge.backend.dto.AdRequest;
import com.adverge.backend.dto.BidResponse;
import com.adverge.backend.model.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * BigoAds广告平台服务实现
 */
@Slf4j
@Service
public class BigoAdsServiceImpl extends AbstractAdNetworkService {

    private static final String PLATFORM_NAME = "BigoAds";
    
    public BigoAdsServiceImpl(RestTemplate restTemplate, Config config) {
        super(restTemplate);
        
        // 从配置中获取BigoAds平台的配置信息
        Config.Platform bigoConfig = config.getPlatforms().stream()
                .filter(p -> PLATFORM_NAME.equalsIgnoreCase(p.getName()))
                .findFirst()
                .orElse(null);
        
        if (bigoConfig != null) {
            this.apiUrl = "https://api.bigoads.com/v1";
            this.appId = bigoConfig.getAppId();
            this.appKey = bigoConfig.getAppKey();
            this.placementId = bigoConfig.getPlacementId();
            this.bidFloor = bigoConfig.getBidFloor();
        } else {
            log.warn("BigoAds平台配置未找到");
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
                log.debug("向BigoAds发送竞价请求");
                
                if (appId == null || appKey == null) {
                    log.warn("BigoAds平台配置不完整，跳过竞价");
                    return null;
                }
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("Authorization", "Bearer " + appKey);
                
                Object bidRequest = buildBidRequest(adRequest);
                HttpEntity<Object> request = new HttpEntity<>(bidRequest, headers);
                
                ResponseEntity<Map> response = restTemplate.exchange(
                        apiUrl + "/bid", 
                        HttpMethod.POST, 
                        request, 
                        Map.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    if (responseBody.containsKey("bid")) {
                        Map<String, Object> bid = (Map<String, Object>) responseBody.get("bid");
                        return parseBidResponse(bid);
                    }
                }
                
                return null;
            } catch (Exception e) {
                log.error("BigoAds竞价请求失败", e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> notifyWin(String bidToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("通知BigoAds竞价胜出: {}", bidToken);
                
                if (appId == null || appKey == null) {
                    log.warn("BigoAds平台配置不完整，跳过通知");
                    return false;
                }
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("Authorization", "Bearer " + appKey);
                
                Map<String, Object> notifyRequest = new HashMap<>();
                notifyRequest.put("bidToken", bidToken);
                notifyRequest.put("timestamp", System.currentTimeMillis());
                
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(notifyRequest, headers);
                
                ResponseEntity<Map> response = restTemplate.exchange(
                        apiUrl + "/win", 
                        HttpMethod.POST, 
                        request, 
                        Map.class
                );
                
                return response.getStatusCode().is2xxSuccessful();
            } catch (Exception e) {
                log.error("通知BigoAds竞价胜出失败", e);
                return false;
            }
        });
    }

    @Override
    protected Object buildBidRequest(AdRequest adRequest) {
        Map<String, Object> bidRequest = new HashMap<>();
        
        // 应用信息
        Map<String, Object> app = new HashMap<>();
        app.put("id", appId);
        app.put("name", adRequest.getAppId());
        app.put("bundle", "");
        bidRequest.put("app", app);
        
        // 设备信息
        Map<String, Object> device = new HashMap<>();
        if (adRequest.getDeviceInfo() != null) {
            device.put("ua", adRequest.getDeviceInfo().getType());
            device.put("os", adRequest.getDeviceInfo().getOs());
            device.put("osv", adRequest.getDeviceInfo().getOsVersion());
            device.put("model", adRequest.getDeviceInfo().getModel());
            device.put("connectionType", "WIFI");
        }
        bidRequest.put("device", device);
        
        // 用户信息
        Map<String, Object> user = new HashMap<>();
        if (adRequest.getUserData() != null) {
            user.put("id", UUID.randomUUID().toString());
            
            // 地理信息
            if (adRequest.getUserData().getGeo() != null) {
                Map<String, Object> geo = new HashMap<>();
                geo.put("country", adRequest.getUserData().getGeo().getCountry());
                geo.put("region", adRequest.getUserData().getGeo().getRegion());
                geo.put("city", adRequest.getUserData().getGeo().getCity());
                device.put("geo", geo);
            }
        }
        bidRequest.put("user", user);
        
        // 广告位信息
        Map<String, Object> impression = new HashMap<>();
        impression.put("id", generateBidId());
        impression.put("placementId", placementId);
        
        // 尺寸信息
        Map<String, Object> banner = new HashMap<>();
        banner.put("w", 320);
        banner.put("h", 50);
        impression.put("banner", banner);
        
        bidRequest.put("imp", new Object[] { impression });
        
        // 其他参数
        bidRequest.put("at", 1); // 1 = 首次价格拍卖
        bidRequest.put("tmax", 100); // 100ms超时
        
        return bidRequest;
    }

    @Override
    protected BidResponse parseBidResponse(Object responseObj) {
        try {
            Map<String, Object> response = (Map<String, Object>) responseObj;
            
            if (response == null || !response.containsKey("price")) {
                return null;
            }
            
            Double price = Double.parseDouble(response.get("price").toString());
            
            BidResponse.AdData adData = new BidResponse.AdData();
            if (response.containsKey("creative")) {
                Map<String, Object> creative = (Map<String, Object>) response.get("creative");
                adData.setAdId(creative.containsKey("adId") ? creative.get("adId").toString() : UUID.randomUUID().toString());
                adData.setTitle(creative.containsKey("title") ? creative.get("title").toString() : "BigoAds广告");
                adData.setDescription(creative.containsKey("description") ? creative.get("description").toString() : "");
                adData.setImageUrl(creative.containsKey("imageUrl") ? creative.get("imageUrl").toString() : "");
                adData.setIconUrl(creative.containsKey("iconUrl") ? creative.get("iconUrl").toString() : "");
                adData.setCtaText(creative.containsKey("ctaText") ? creative.get("ctaText").toString() : "点击查看");
                adData.setLandingUrl(creative.containsKey("landingUrl") ? creative.get("landingUrl").toString() : "");
            } else {
                adData.setAdId(UUID.randomUUID().toString());
                adData.setTitle("BigoAds广告");
                adData.setCtaText("点击查看");
            }
            
            return BidResponse.builder()
                    .source(PLATFORM_NAME)
                    .price(price)
                    .adData(adData)
                    .build();
        } catch (Exception e) {
            log.error("解析BigoAds竞价响应失败", e);
            return null;
        }
    }
} 