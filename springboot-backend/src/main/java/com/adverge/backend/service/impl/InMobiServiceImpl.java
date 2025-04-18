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
 * InMobi广告平台服务实现
 */
@Slf4j
@Service
public class InMobiServiceImpl extends AbstractAdNetworkService {

    private static final String PLATFORM_NAME = "InMobi";
    
    public InMobiServiceImpl(RestTemplate restTemplate, Config config) {
        super(restTemplate);
        
        // 从配置中获取InMobi平台的配置信息
        Config.Platform inMobiConfig = config.getPlatforms().stream()
                .filter(p -> PLATFORM_NAME.equalsIgnoreCase(p.getName()))
                .findFirst()
                .orElse(null);
        
        if (inMobiConfig != null) {
            this.apiUrl = "https://api.inmobi.com/v1";
            this.appId = inMobiConfig.getAppId();
            this.appKey = inMobiConfig.getAppKey();
            this.placementId = inMobiConfig.getPlacementId();
            this.bidFloor = inMobiConfig.getBidFloor();
        } else {
            log.warn("InMobi平台配置未找到");
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
                log.debug("向InMobi发送竞价请求");
                
                if (appId == null || appKey == null) {
                    log.warn("InMobi平台配置不完整，跳过竞价");
                    return null;
                }
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("X-App-Id", appId);
                headers.set("X-Api-Key", appKey);
                
                Object bidRequest = buildBidRequest(adRequest);
                HttpEntity<Object> request = new HttpEntity<>(bidRequest, headers);
                
                ResponseEntity<Map> response = restTemplate.exchange(
                        apiUrl + "/ads/bid", 
                        HttpMethod.POST, 
                        request, 
                        Map.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    if (responseBody.containsKey("seatbid")) {
                        return parseBidResponse(responseBody);
                    }
                }
                
                return null;
            } catch (Exception e) {
                log.error("InMobi竞价请求失败", e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> notifyWin(String bidToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("通知InMobi竞价胜出: {}", bidToken);
                
                if (appId == null || appKey == null) {
                    log.warn("InMobi平台配置不完整，跳过通知");
                    return false;
                }
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("X-App-Id", appId);
                headers.set("X-Api-Key", appKey);
                
                Map<String, Object> notifyRequest = new HashMap<>();
                notifyRequest.put("bidId", bidToken);
                notifyRequest.put("price", 0.0);
                notifyRequest.put("timestamp", System.currentTimeMillis());
                
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(notifyRequest, headers);
                
                ResponseEntity<Map> response = restTemplate.exchange(
                        apiUrl + "/ads/win", 
                        HttpMethod.POST, 
                        request, 
                        Map.class
                );
                
                return response.getStatusCode().is2xxSuccessful();
            } catch (Exception e) {
                log.error("通知InMobi竞价胜出失败", e);
                return false;
            }
        });
    }

    @Override
    protected Object buildBidRequest(AdRequest adRequest) {
        Map<String, Object> bidRequest = new HashMap<>();
        
        bidRequest.put("id", generateBidId());
        bidRequest.put("at", 1); // First-price auction
        bidRequest.put("tmax", 100); // 100ms timeout
        
        // App info
        Map<String, Object> app = new HashMap<>();
        app.put("id", appId);
        app.put("name", adRequest.getAppId());
        bidRequest.put("app", app);
        
        // Device info
        Map<String, Object> device = new HashMap<>();
        if (adRequest.getDeviceInfo() != null) {
            device.put("os", adRequest.getDeviceInfo().getOs());
            device.put("osv", adRequest.getDeviceInfo().getOsVersion());
            device.put("model", adRequest.getDeviceInfo().getModel());
        }
        bidRequest.put("device", device);
        
        // User info
        Map<String, Object> user = new HashMap<>();
        if (adRequest.getUserData() != null && adRequest.getUserData().getGeo() != null) {
            Map<String, Object> geo = new HashMap<>();
            geo.put("country", adRequest.getUserData().getGeo().getCountry());
            device.put("geo", geo);
        }
        bidRequest.put("user", user);
        
        // Impression info
        Map<String, Object> imp = new HashMap<>();
        imp.put("id", "1");
        imp.put("instl", 0); // 0 = not interstitial
        
        // Banner info
        Map<String, Object> banner = new HashMap<>();
        banner.put("w", 320);
        banner.put("h", 50);
        imp.put("banner", banner);
        
        // Floor price
        Map<String, Object> bidfloor = new HashMap<>();
        bidfloor.put("currency", "USD");
        bidfloor.put("bidfloor", this.bidFloor);
        imp.put("bidfloor", bidfloor);
        
        bidRequest.put("imp", new Object[] { imp });
        
        return bidRequest;
    }

    @Override
    protected BidResponse parseBidResponse(Object responseObj) {
        try {
            Map<String, Object> response = (Map<String, Object>) responseObj;
            
            if (response == null) {
                return null;
            }
            
            Object[] seatbids = (Object[]) response.get("seatbid");
            if (seatbids == null || seatbids.length == 0) {
                return null;
            }
            
            Map<String, Object> seatbid = (Map<String, Object>) seatbids[0];
            if (seatbid == null) {
                return null;
            }
            
            Object[] bids = (Object[]) seatbid.get("bid");
            if (bids == null || bids.length == 0) {
                return null;
            }
            
            Map<String, Object> bid = (Map<String, Object>) bids[0];
            if (bid == null) {
                return null;
            }
            
            Double price = bid.containsKey("price") ? 
                    Double.parseDouble(bid.get("price").toString()) : 0.0;
            
            BidResponse.AdData adData = new BidResponse.AdData();
            adData.setAdId(bid.containsKey("id") ? bid.get("id").toString() : UUID.randomUUID().toString());
            adData.setTitle(bid.containsKey("adomain") ? bid.get("adomain").toString() : "InMobi广告");
            
            if (bid.containsKey("adm")) {
                String adm = bid.get("adm").toString();
                
                // Parse creative from adm (ad markup)
                adData.setImageUrl(extractUrlFromAdm(adm, "img"));
                adData.setLandingUrl(extractUrlFromAdm(adm, "clickUrl"));
                adData.setCtaText("点击查看");
            }
            
            return BidResponse.builder()
                    .source(PLATFORM_NAME)
                    .price(price)
                    .adData(adData)
                    .build();
        } catch (Exception e) {
            log.error("解析InMobi竞价响应失败", e);
            return null;
        }
    }
    
    private String extractUrlFromAdm(String adm, String tag) {
        // 简单实现，实际项目中需要更复杂的解析逻辑
        int startIndex = adm.indexOf("<" + tag);
        if (startIndex < 0) return "";
        
        startIndex = adm.indexOf("src=\"", startIndex);
        if (startIndex < 0) return "";
        
        startIndex += 5; // skip src="
        int endIndex = adm.indexOf("\"", startIndex);
        if (endIndex < 0) return "";
        
        return adm.substring(startIndex, endIndex);
    }
} 