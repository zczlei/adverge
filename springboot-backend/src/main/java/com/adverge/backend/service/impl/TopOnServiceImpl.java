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
 * TopOn广告平台服务实现
 */
@Slf4j
@Service
public class TopOnServiceImpl extends AbstractAdNetworkService {

    private static final String PLATFORM_NAME = "TopOn";
    
    public TopOnServiceImpl(RestTemplate restTemplate, Config config) {
        super(restTemplate);
        
        // 从配置中获取TopOn平台的配置信息
        Config.Platform topOnConfig = config.getPlatforms().stream()
                .filter(p -> PLATFORM_NAME.equalsIgnoreCase(p.getName()))
                .findFirst()
                .orElse(null);
        
        if (topOnConfig != null) {
            this.apiUrl = "https://api.toponad.com/v1";
            this.appId = topOnConfig.getAppId();
            this.appKey = topOnConfig.getAppKey();
            this.placementId = topOnConfig.getPlacementId();
            this.bidFloor = topOnConfig.getBidFloor();
        } else {
            log.warn("TopOn平台配置未找到");
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
                log.debug("向TopOn发送竞价请求");
                
                if (appId == null || appKey == null) {
                    log.warn("TopOn平台配置不完整，跳过竞价");
                    return null;
                }
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("X-Auth-Token", appKey);
                
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
                    if (responseBody.containsKey("data") && responseBody.containsKey("code") 
                            && Integer.parseInt(responseBody.get("code").toString()) == 0) {
                        return parseBidResponse(responseBody.get("data"));
                    }
                }
                
                return null;
            } catch (Exception e) {
                log.error("TopOn竞价请求失败", e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> notifyWin(String bidToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("通知TopOn竞价胜出: {}", bidToken);
                
                if (appId == null || appKey == null) {
                    log.warn("TopOn平台配置不完整，跳过通知");
                    return false;
                }
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("X-Auth-Token", appKey);
                
                Map<String, Object> notifyRequest = new HashMap<>();
                notifyRequest.put("bid_id", bidToken);
                notifyRequest.put("win_price", 0.0);
                notifyRequest.put("app_id", appId);
                notifyRequest.put("placement_id", placementId);
                
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(notifyRequest, headers);
                
                ResponseEntity<Map> response = restTemplate.exchange(
                        apiUrl + "/win", 
                        HttpMethod.POST, 
                        request, 
                        Map.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    return responseBody.containsKey("code") && 
                           Integer.parseInt(responseBody.get("code").toString()) == 0;
                }
                return false;
            } catch (Exception e) {
                log.error("通知TopOn竞价胜出失败", e);
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
        
        // 设备信息
        Map<String, Object> deviceInfo = new HashMap<>();
        if (adRequest.getDeviceInfo() != null) {
            deviceInfo.put("os", adRequest.getDeviceInfo().getOs());
            deviceInfo.put("os_version", adRequest.getDeviceInfo().getOsVersion());
            deviceInfo.put("model", adRequest.getDeviceInfo().getModel());
            deviceInfo.put("manufacturer", adRequest.getDeviceInfo().getManufacturer());
            
            // 添加屏幕信息
            if (adRequest.getDeviceInfo().getScreenWidth() != null && 
                adRequest.getDeviceInfo().getScreenHeight() != null) {
                deviceInfo.put("screen_width", adRequest.getDeviceInfo().getScreenWidth());
                deviceInfo.put("screen_height", adRequest.getDeviceInfo().getScreenHeight());
            }
            
            // 添加语言信息
            if (adRequest.getDeviceInfo().getLanguage() != null) {
                deviceInfo.put("language", adRequest.getDeviceInfo().getLanguage());
            }
        }
        bidRequest.put("device", deviceInfo);
        
        // 用户信息
        Map<String, Object> userData = new HashMap<>();
        if (adRequest.getUserData() != null) {
            // 地理位置信息
            if (adRequest.getUserData().getGeo() != null) {
                Map<String, Object> geoInfo = new HashMap<>();
                geoInfo.put("country", adRequest.getUserData().getGeo().getCountry());
                geoInfo.put("region", adRequest.getUserData().getGeo().getRegion());
                geoInfo.put("city", adRequest.getUserData().getGeo().getCity());
                userData.put("geo", geoInfo);
            }
            
            userData.put("device_type", adRequest.getUserData().getDevice());
            userData.put("os_type", adRequest.getUserData().getOs());
        }
        bidRequest.put("user", userData);
        
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
            String bidId = response.containsKey("bid_id") ? 
                    response.get("bid_id").toString() : UUID.randomUUID().toString();
            
            BidResponse.AdData adData = new BidResponse.AdData();
            
            if (response.containsKey("ad_data")) {
                Map<String, Object> adDataMap = (Map<String, Object>) response.get("ad_data");
                adData.setAdId(bidId);
                adData.setTitle(adDataMap.containsKey("title") ? 
                        adDataMap.get("title").toString() : "TopOn广告");
                adData.setDescription(adDataMap.containsKey("description") ? 
                        adDataMap.get("description").toString() : "");
                adData.setImageUrl(adDataMap.containsKey("image_url") ? 
                        adDataMap.get("image_url").toString() : "");
                adData.setIconUrl(adDataMap.containsKey("icon_url") ? 
                        adDataMap.get("icon_url").toString() : "");
                adData.setCtaText(adDataMap.containsKey("cta_text") ? 
                        adDataMap.get("cta_text").toString() : "点击查看");
                adData.setLandingUrl(adDataMap.containsKey("landing_url") ? 
                        adDataMap.get("landing_url").toString() : "");
            } else {
                adData.setAdId(bidId);
                adData.setTitle("TopOn广告");
                adData.setCtaText("点击查看");
            }
            
            return BidResponse.builder()
                    .source(PLATFORM_NAME)
                    .price(price)
                    .adData(adData)
                    .build();
        } catch (Exception e) {
            log.error("解析TopOn竞价响应失败", e);
            return null;
        }
    }
} 