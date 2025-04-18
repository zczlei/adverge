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
 * Mintegral广告平台服务实现
 */
@Slf4j
@Service
public class MintegralServiceImpl extends AbstractAdNetworkService {

    private static final String PLATFORM_NAME = "Mintegral";
    
    public MintegralServiceImpl(RestTemplate restTemplate, Config config) {
        super(restTemplate);
        
        // 从配置中获取Mintegral平台的配置信息
        Config.Platform mintegralConfig = config.getPlatforms().stream()
                .filter(p -> PLATFORM_NAME.equalsIgnoreCase(p.getName()))
                .findFirst()
                .orElse(null);
        
        if (mintegralConfig != null) {
            this.apiUrl = "https://api.mintegral.com/v1";
            this.appId = mintegralConfig.getAppId();
            this.appKey = mintegralConfig.getAppKey();
            this.placementId = mintegralConfig.getPlacementId();
            this.bidFloor = mintegralConfig.getBidFloor();
        } else {
            log.warn("Mintegral平台配置未找到");
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
                log.debug("向Mintegral发送竞价请求");
                
                if (appId == null || appKey == null) {
                    log.warn("Mintegral平台配置不完整，跳过竞价");
                    return null;
                }
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("Authorization", appKey);
                
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
                    if (responseBody.containsKey("data")) {
                        return parseBidResponse(responseBody.get("data"));
                    }
                }
                
                return null;
            } catch (Exception e) {
                log.error("Mintegral竞价请求失败", e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> notifyWin(String bidToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("通知Mintegral竞价胜出: {}", bidToken);
                
                if (appId == null || appKey == null) {
                    log.warn("Mintegral平台配置不完整，跳过通知");
                    return false;
                }
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("Authorization", appKey);
                
                Map<String, Object> notifyRequest = new HashMap<>();
                notifyRequest.put("bid_id", bidToken);
                notifyRequest.put("price", 0.0);
                notifyRequest.put("app_id", appId);
                notifyRequest.put("unit_id", placementId);
                
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(notifyRequest, headers);
                
                ResponseEntity<Map> response = restTemplate.exchange(
                        apiUrl + "/bidwin", 
                        HttpMethod.POST, 
                        request, 
                        Map.class
                );
                
                return response.getStatusCode().is2xxSuccessful();
            } catch (Exception e) {
                log.error("通知Mintegral竞价胜出失败", e);
                return false;
            }
        });
    }

    @Override
    protected Object buildBidRequest(AdRequest adRequest) {
        Map<String, Object> bidRequest = new HashMap<>();
        
        // 基本信息
        bidRequest.put("app_id", appId);
        bidRequest.put("unit_id", placementId);
        bidRequest.put("token", generateBidId());
        bidRequest.put("floor_price", bidFloor);
        
        // 设备信息
        Map<String, Object> device = new HashMap<>();
        if (adRequest.getDeviceInfo() != null) {
            device.put("os", adRequest.getDeviceInfo().getOs());
            device.put("os_version", adRequest.getDeviceInfo().getOsVersion());
            device.put("model", adRequest.getDeviceInfo().getModel());
            device.put("brand", adRequest.getDeviceInfo().getManufacturer());
            
            // 屏幕信息
            if (adRequest.getDeviceInfo().getScreenWidth() != null && 
                adRequest.getDeviceInfo().getScreenHeight() != null) {
                device.put("width", adRequest.getDeviceInfo().getScreenWidth());
                device.put("height", adRequest.getDeviceInfo().getScreenHeight());
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
                user.put("country", adRequest.getUserData().getGeo().getCountry());
                user.put("region", adRequest.getUserData().getGeo().getRegion());
                user.put("city", adRequest.getUserData().getGeo().getCity());
            }
        }
        bidRequest.put("user", user);
        
        // 广告位信息
        Map<String, Object> unitInfo = new HashMap<>();
        unitInfo.put("unit_type", 1); // 1 = 横幅广告
        unitInfo.put("unit_size", "320x50");
        bidRequest.put("unit_setting", unitInfo);
        
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
            
            if (response.containsKey("ad")) {
                Map<String, Object> ad = (Map<String, Object>) response.get("ad");
                adData.setAdId(bidId);
                adData.setTitle(ad.containsKey("title") ? 
                        ad.get("title").toString() : "Mintegral广告");
                adData.setDescription(ad.containsKey("desc") ? 
                        ad.get("desc").toString() : "");
                adData.setImageUrl(ad.containsKey("image_url") ? 
                        ad.get("image_url").toString() : "");
                adData.setIconUrl(ad.containsKey("icon_url") ? 
                        ad.get("icon_url").toString() : "");
                adData.setCtaText(ad.containsKey("cta") ? 
                        ad.get("cta").toString() : "点击查看");
                adData.setLandingUrl(ad.containsKey("click_url") ? 
                        ad.get("click_url").toString() : "");
            } else {
                adData.setAdId(bidId);
                adData.setTitle("Mintegral广告");
                adData.setCtaText("点击查看");
            }
            
            return BidResponse.builder()
                    .source(PLATFORM_NAME)
                    .price(price)
                    .adData(adData)
                    .build();
        } catch (Exception e) {
            log.error("解析Mintegral竞价响应失败", e);
            return null;
        }
    }
} 