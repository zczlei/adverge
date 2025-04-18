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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * UnityAds广告平台服务实现
 */
@Slf4j
@Service
public class UnityAdsServiceImpl extends AbstractAdNetworkService {

    private static final String PLATFORM_NAME = "UnityAds";
    
    public UnityAdsServiceImpl(RestTemplate restTemplate, Config config) {
        super(restTemplate);
        
        // 从配置中获取UnityAds平台的配置信息
        Config.Platform unityConfig = config.getPlatforms().stream()
                .filter(p -> PLATFORM_NAME.equalsIgnoreCase(p.getName()))
                .findFirst()
                .orElse(null);
        
        if (unityConfig != null) {
            this.apiUrl = "https://auction.unityads.unity3d.com/v2";
            this.appId = unityConfig.getAppId();
            this.appKey = unityConfig.getAppKey();
            this.placementId = unityConfig.getPlacementId();
            this.bidFloor = unityConfig.getBidFloor();
        } else {
            log.warn("UnityAds平台配置未找到");
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
                log.debug("向UnityAds发送竞价请求");
                
                if (appId == null || appKey == null) {
                    log.warn("UnityAds平台配置不完整，跳过竞价");
                    return null;
                }
                
                // 构建请求参数
                Object bidRequest = buildBidRequest(adRequest);
                String requestBody = objectMapper.writeValueAsString(bidRequest);
                
                // 构建请求头
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("X-Unity-AppId", appId);
                
                // 添加签名
                String signature = generateSignature(requestBody, appKey);
                headers.set("X-Unity-Signature", signature);
                
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
                    Map<String, Object> responseBody = response.getBody();
                    if (responseBody.containsKey("bids") && responseBody.containsKey("status") 
                            && "ok".equals(responseBody.get("status"))) {
                        return parseBidResponse(responseBody);
                    }
                }
                
                return null;
            } catch (Exception e) {
                log.error("UnityAds竞价请求失败", e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> notifyWin(String bidToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("通知UnityAds竞价胜出: {}", bidToken);
                
                if (appId == null || appKey == null) {
                    log.warn("UnityAds平台配置不完整，跳过通知");
                    return false;
                }
                
                // 构建请求参数
                Map<String, Object> notifyRequest = new HashMap<>();
                notifyRequest.put("bidToken", bidToken);
                String requestBody = objectMapper.writeValueAsString(notifyRequest);
                
                // 构建请求头
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("X-Unity-AppId", appId);
                
                // 添加签名
                String signature = generateSignature(requestBody, appKey);
                headers.set("X-Unity-Signature", signature);
                
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
                log.error("通知UnityAds竞价胜出失败", e);
                return false;
            }
        });
    }

    @Override
    protected Object buildBidRequest(AdRequest adRequest) {
        Map<String, Object> bidRequest = new HashMap<>();
        
        // 基本信息
        bidRequest.put("appId", appId);
        bidRequest.put("placementId", placementId);
        bidRequest.put("bidId", generateBidId());
        bidRequest.put("bidFloor", bidFloor);
        
        // 设备信息
        Map<String, Object> device = new HashMap<>();
        if (adRequest.getDeviceInfo() != null) {
            device.put("os", adRequest.getDeviceInfo().getOs());
            device.put("osVersion", adRequest.getDeviceInfo().getOsVersion());
            device.put("model", adRequest.getDeviceInfo().getModel());
            device.put("manufacturer", adRequest.getDeviceInfo().getManufacturer());
            
            // 屏幕信息
            if (adRequest.getDeviceInfo().getScreenWidth() != null && 
                adRequest.getDeviceInfo().getScreenHeight() != null) {
                Map<String, Object> screen = new HashMap<>();
                screen.put("width", adRequest.getDeviceInfo().getScreenWidth());
                screen.put("height", adRequest.getDeviceInfo().getScreenHeight());
                device.put("screen", screen);
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
        
        // 广告配置
        Map<String, Object> adConfig = new HashMap<>();
        adConfig.put("format", "banner");
        adConfig.put("width", 320);
        adConfig.put("height", 50);
        bidRequest.put("adConfig", adConfig);
        
        return bidRequest;
    }

    @Override
    protected BidResponse parseBidResponse(Object responseObj) {
        try {
            Map<String, Object> response = (Map<String, Object>) responseObj;
            
            if (response == null || !response.containsKey("bids")) {
                return null;
            }
            
            Object[] bids = (Object[]) response.get("bids");
            if (bids == null || bids.length == 0) {
                return null;
            }
            
            Map<String, Object> bid = (Map<String, Object>) bids[0];
            if (bid == null || !bid.containsKey("price")) {
                return null;
            }
            
            Double price = Double.parseDouble(bid.get("price").toString());
            String bidId = bid.containsKey("bidId") ? 
                    bid.get("bidId").toString() : UUID.randomUUID().toString();
            String bidToken = bid.containsKey("token") ? 
                    bid.get("token").toString() : UUID.randomUUID().toString();
            
            BidResponse.AdData adData = new BidResponse.AdData();
            
            if (bid.containsKey("adData")) {
                Map<String, Object> adDataMap = (Map<String, Object>) bid.get("adData");
                adData.setAdId(bidId);
                adData.setTitle(adDataMap.containsKey("title") ? 
                        adDataMap.get("title").toString() : "UnityAds广告");
                adData.setDescription(adDataMap.containsKey("description") ? 
                        adDataMap.get("description").toString() : "");
                adData.setImageUrl(adDataMap.containsKey("imageUrl") ? 
                        adDataMap.get("imageUrl").toString() : "");
                adData.setIconUrl(adDataMap.containsKey("iconUrl") ? 
                        adDataMap.get("iconUrl").toString() : "");
                adData.setCtaText(adDataMap.containsKey("ctaText") ? 
                        adDataMap.get("ctaText").toString() : "点击查看");
                adData.setLandingUrl(adDataMap.containsKey("clickUrl") ? 
                        adDataMap.get("clickUrl").toString() : "");
            } else {
                adData.setAdId(bidId);
                adData.setTitle("UnityAds广告");
                adData.setCtaText("点击查看");
            }
            
            return BidResponse.builder()
                    .source(PLATFORM_NAME)
                    .price(price)
                    .adData(adData)
                    .build();
        } catch (Exception e) {
            log.error("解析UnityAds竞价响应失败", e);
            return null;
        }
    }
    
    /**
     * 生成UnityAds请求签名
     * 
     * @param data 请求数据
     * @param secret 密钥
     * @return 签名字符串
     */
    private String generateSignature(String data, String secret) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("生成UnityAds签名失败", e);
            return "";
        }
    }
} 