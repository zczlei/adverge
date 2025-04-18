package com.adverge.backend.controller;

import com.adverge.backend.dto.*;
import com.adverge.backend.model.Config;
import com.adverge.backend.service.AdService;
import com.adverge.backend.service.ConfigService;
import com.adverge.backend.service.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 兼容性控制器，处理Android SDK的API调用
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CompatibilityController {

    private final AdController adController;
    private final ConfigController configController;
    private final AdService adService;
    private final ConfigService configService;
    private final SecurityService securityService;

    /**
     * 竞价请求 (Android SDK兼容接口)
     */
    @PostMapping("/bid")
    public ResponseEntity<AdResponse> legacyBid(
            @Valid @RequestBody AdRequest adRequest,
            HttpServletRequest request) {
        
        log.debug("Android SDK发起竞价请求: {}", adRequest);
        
        try {
            // 使用adService直接竞价，获取胜出的广告
            BidResponse bidResponse = adService.bid(adRequest.getAdUnitId(), adRequest, request);
            
            if (bidResponse != null) {
                // 转换为SDK兼容的响应格式
                AdResponse adResponse = AdResponse.fromBidResponse(bidResponse);
                adResponse.setAdUnitId(adRequest.getAdUnitId());
                
                // 添加签名和时间戳
                long timestamp = System.currentTimeMillis();
                String signature = securityService.generateRequestSignature(request, timestamp);
                
                return ResponseEntity.ok()
                        .header("X-Timestamp", String.valueOf(timestamp))
                        .header("X-Signature", signature)
                        .body(adResponse);
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            log.error("处理竞价请求失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取广告 (Android SDK兼容接口)
     */
    @PostMapping("/ad")
    public ResponseEntity<AdResponse> legacyGetAd(
            @Valid @RequestBody AdRequest adRequest,
            HttpServletRequest request) {
        
        log.debug("Android SDK发起广告请求: {}", adRequest);
        
        try {
            // 构建选项参数
            Map<String, String> options = new HashMap<>();
            options.put("appId", adRequest.getAppId());
            
            if (adRequest.getDeviceInfo() != null) {
                options.put("deviceType", adRequest.getDeviceInfo().getType());
                options.put("os", adRequest.getDeviceInfo().getOs());
                options.put("model", adRequest.getDeviceInfo().getModel());
                options.put("osVersion", adRequest.getDeviceInfo().getOsVersion());
            }
            
            // 使用adService获取缓存的广告或进行新的竞价
            BidResponse bidResponse = adService.getAd(adRequest.getAdUnitId(), options, request);
            
            if (bidResponse != null) {
                // 转换为SDK兼容的响应格式
                AdResponse adResponse = AdResponse.fromBidResponse(bidResponse);
                adResponse.setAdUnitId(adRequest.getAdUnitId());
                
                // 添加签名和时间戳
                long timestamp = System.currentTimeMillis();
                String signature = securityService.generateRequestSignature(request, timestamp);
                
                return ResponseEntity.ok()
                        .header("X-Timestamp", String.valueOf(timestamp))
                        .header("X-Signature", signature)
                        .body(adResponse);
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            log.error("处理广告请求失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 记录广告展示 (Android SDK兼容接口)
     */
    @PostMapping("/track/impression")
    public ResponseEntity<Map<String, Boolean>> legacyTrackImpression(
            @RequestParam String adId,
            @RequestParam String platform,
            @RequestParam(required = false) String adUnitId,
            HttpServletRequest request) {
        
        log.debug("Android SDK记录广告展示: adId={}, platform={}, adUnitId={}", adId, platform, adUnitId);
        
        try {
            // 记录展示
            adService.trackImpression(adId, platform, request);
            
            // 构建成功响应
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", true);
            
            // 添加签名和时间戳
            long timestamp = System.currentTimeMillis();
            String signature = securityService.generateRequestSignature(request, timestamp);
            
            return ResponseEntity.ok()
                    .header("X-Timestamp", String.valueOf(timestamp))
                    .header("X-Signature", signature)
                    .body(response);
        } catch (Exception e) {
            log.error("记录广告展示失败", e);
            Map<String, Boolean> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * 记录广告点击 (Android SDK兼容接口)
     */
    @PostMapping("/track/click")
    public ResponseEntity<Map<String, Boolean>> legacyTrackClick(
            @RequestParam String adId,
            @RequestParam String platform,
            @RequestParam(required = false) double revenue,
            HttpServletRequest request) {
        
        log.debug("Android SDK记录广告点击: adId={}, platform={}, revenue={}", adId, platform, revenue);
        
        try {
            // 构建跟踪请求
            TrackRequest trackRequest = new TrackRequest();
            trackRequest.setPlatform(platform);
            trackRequest.setRevenue(revenue);
            
            // 记录点击
            adService.trackClick(adId, trackRequest, request);
            
            // 构建成功响应
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", true);
            
            // 添加签名和时间戳
            long timestamp = System.currentTimeMillis();
            String signature = securityService.generateRequestSignature(request, timestamp);
            
            return ResponseEntity.ok()
                    .header("X-Timestamp", String.valueOf(timestamp))
                    .header("X-Signature", signature)
                    .body(response);
        } catch (Exception e) {
            log.error("记录广告点击失败", e);
            Map<String, Boolean> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * 获取平台列表 (Android SDK兼容接口)
     */
    @GetMapping("/platforms")
    public ResponseEntity<List<PlatformResponse>> getPlatforms(HttpServletRequest request) {
        log.debug("Android SDK获取平台列表");
        
        try {
            List<Config.Platform> platforms = configService.getPlatforms();
            List<PlatformResponse> response = PlatformResponse.fromPlatforms(platforms);
            
            // 添加签名和时间戳
            long timestamp = System.currentTimeMillis();
            String signature = securityService.generateRequestSignature(request, timestamp);
            
            return ResponseEntity.ok()
                    .header("X-Timestamp", String.valueOf(timestamp))
                    .header("X-Signature", signature)
                    .body(response);
        } catch (Exception e) {
            log.error("获取平台列表失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 保存平台配置 (Android SDK兼容接口)
     */
    @PostMapping("/platform")
    public ResponseEntity<PlatformResponse> savePlatform(
            @Valid @RequestBody PlatformResponse platformRequest,
            HttpServletRequest request) {
        
        log.debug("Android SDK保存平台配置: {}", platformRequest.getName());
        
        try {
            long timestamp = System.currentTimeMillis();
            String signature = securityService.generateRequestSignature(request, timestamp);
            
            // 转换为Config.Platform
            Config.Platform platform = new Config.Platform();
            platform.setName(platformRequest.getName());
            platform.setAppId(platformRequest.getAppId());
            platform.setAppKey(platformRequest.getAppKey());
            platform.setEnabled(platformRequest.isEnabled());
            platform.setBidFloor(platformRequest.getBidFloor());
            
            Config.Platform savedPlatform = configService.savePlatform(platform);
            PlatformResponse response = PlatformResponse.fromPlatform(savedPlatform);
            
            return ResponseEntity.ok()
                    .header("X-Timestamp", String.valueOf(timestamp))
                    .header("X-Signature", signature)
                    .body(response);
        } catch (Exception e) {
            log.error("保存平台配置失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 启用平台 (Android SDK兼容接口)
     */
    @PostMapping("/platform/{name}/enable")
    public ResponseEntity<Map<String, Boolean>> enablePlatform(
            @PathVariable String name,
            HttpServletRequest request) {
        
        log.debug("Android SDK启用平台: {}", name);
        
        try {
            long timestamp = System.currentTimeMillis();
            String signature = securityService.generateRequestSignature(request, timestamp);
            
            configService.setPlatformEnabled(name, true);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", true);
            
            return ResponseEntity.ok()
                    .header("X-Timestamp", String.valueOf(timestamp))
                    .header("X-Signature", signature)
                    .body(response);
        } catch (Exception e) {
            log.error("启用平台失败", e);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", false);
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 禁用平台 (Android SDK兼容接口)
     */
    @PostMapping("/platform/{name}/disable")
    public ResponseEntity<Map<String, Boolean>> disablePlatform(
            @PathVariable String name,
            HttpServletRequest request) {
        
        log.debug("Android SDK禁用平台: {}", name);
        
        try {
            long timestamp = System.currentTimeMillis();
            String signature = securityService.generateRequestSignature(request, timestamp);
            
            configService.setPlatformEnabled(name, false);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", true);
            
            return ResponseEntity.ok()
                    .header("X-Timestamp", String.valueOf(timestamp))
                    .header("X-Signature", signature)
                    .body(response);
        } catch (Exception e) {
            log.error("禁用平台失败", e);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", false);
            
            return ResponseEntity.badRequest().body(response);
        }
    }
} 