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
 * IronSource广告平台服务实现
 */
@Slf4j
@Service
public class IronSourceServiceImpl extends AbstractAdNetworkService {

    private static final String PLATFORM_NAME = "IronSource";
    
    public IronSourceServiceImpl(RestTemplate restTemplate, Config config) {
        super(restTemplate);
        
        // 从配置中获取IronSource平台的配置信息
        Config.Platform ironSourceConfig = config.getPlatforms().stream()
                .filter(p -> PLATFORM_NAME.equalsIgnoreCase(p.getName()))
                .findFirst()
                .orElse(null);
        
        if (ironSourceConfig != null) {
            this.apiUrl = "https://prebid.ironsrc.net/v2";
            this.appId = ironSourceConfig.getAppId();
            this.appKey = ironSourceConfig.getAppKey();
            this.placementId = ironSourceConfig.getPlacementId();
            this.bidFloor = ironSourceConfig.getBidFloor();
        } else {
            log.warn("IronSource平台配置未找到");
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
                log.debug("向IronSource发送竞价请求");
                
                if (appId == null || appKey == null) {
                    log.warn("IronSource平台配置不完整，跳过竞价");
                    return null;
                }
                
                // 构建请求参数
                Object bidRequest = buildBidRequest(adRequest);
                
                // 构建请求头
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("X-IS-Auth", appKey);
                
                // 发送请求
                HttpEntity<Object> request = new HttpEntity<>(bidRequest, headers);
                ResponseEntity<Map> response = restTemplate.exchange(
                        apiUrl + "/auction", 
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
                log.error("IronSource竞价请求失败", e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> notifyWin(String bidToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("通知IronSource竞价胜出: {}", bidToken);
                
                if (appId == null || appKey == null) {
                    log.warn("IronSource平台配置不完整，跳过通知");
                    return false;
                }
                
                // 构建请求参数
                Map<String, Object> notifyRequest = new HashMap<>();
                notifyRequest.put("bid_id", bidToken);
                
                // 构建请求头
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("X-IS-Auth", appKey);
                
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
                log.error("通知IronSource竞价胜出失败", e);
                return false;
            }
        });
    }

    @Override
    protected Object buildBidRequest(AdRequest adRequest) {
        Map<String, Object> bidRequest = new HashMap<>();
        
        // 基本信息
        bidRequest.put("app_key", appKey);
        bidRequest.put("placement_id", placementId);
        bidRequest.put("auction_id", generateBidId());
        bidRequest.put("floor_price", bidFloor);
        
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
            device.put("make", adRequest.getDeviceInfo().getManufacturer());
            
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
            
            if (bid.containsKey("ad")) {
                Map<String, Object> ad = (Map<String, Object>) bid.get("ad");
                adData.setAdId(bidId);
                adData.setTitle(ad.containsKey("title") ? 
                        ad.get("title").toString() : "IronSource广告");
                adData.setDescription(ad.containsKey("description") ? 
                        ad.get("description").toString() : "");
                adData.setImageUrl(ad.containsKey("image_url") ? 
                        ad.get("image_url").toString() : "");
                adData.setIconUrl(ad.containsKey("icon_url") ? 
                        ad.get("icon_url").toString() : "");
                adData.setCtaText(ad.containsKey("cta_text") ? 
                        ad.get("cta_text").toString() : "点击查看");
                adData.setLandingUrl(ad.containsKey("click_url") ? 
                        ad.get("click_url").toString() : "");
            } else {
                adData.setAdId(bidId);
                adData.setTitle("IronSource广告");
                adData.setCtaText("点击查看");
            }
            
            return BidResponse.builder()
                    .source(PLATFORM_NAME)
                    .price(price)
                    .adData(adData)
                    .bidToken(bidToken)
                    .build();
        } catch (Exception e) {
            log.error("解析IronSource竞价响应失败", e);
            return null;
        }
    }
} 