package com.adverge.backend.controller;

import com.adverge.backend.dto.AdRequest;
import com.adverge.backend.dto.BidResponse;
import com.adverge.backend.dto.TrackRequest;
import com.adverge.backend.service.AdService;
import com.adverge.backend.service.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AdController {

    private final AdService adService;
    private final SecurityService securityService;

    /**
     * 获取广告
     */
    @GetMapping("/ad/{adUnitId}")
    public ResponseEntity<BidResponse> getAd(
            @PathVariable String adUnitId,
            @RequestParam Map<String, String> options,
            HttpServletRequest request) {
        
        try {
            long timestamp = System.currentTimeMillis();
            String signature = securityService.generateRequestSignature(request, timestamp);
            
            BidResponse ad = adService.getAd(adUnitId, options, request);
            
            return ResponseEntity.ok()
                    .header("X-Timestamp", String.valueOf(timestamp))
                    .header("X-Signature", signature)
                    .body(ad);
        } catch (Exception e) {
            log.error("获取广告失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 竞价请求
     */
    @PostMapping("/bid/{adUnitId}")
    public ResponseEntity<BidResponse> bid(
            @PathVariable String adUnitId,
            @Valid @RequestBody AdRequest adRequest,
            HttpServletRequest request) {
        
        try {
            long timestamp = System.currentTimeMillis();
            String signature = securityService.generateRequestSignature(request, timestamp);
            
            BidResponse bidResult = adService.bid(adUnitId, adRequest, request);
            
            return ResponseEntity.ok()
                    .header("X-Timestamp", String.valueOf(timestamp))
                    .header("X-Signature", signature)
                    .body(bidResult);
        } catch (Exception e) {
            log.error("竞价请求失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 记录广告展示
     */
    @PostMapping("/track/impression/{adId}")
    public ResponseEntity<Map<String, Boolean>> trackImpression(
            @PathVariable String adId,
            @RequestParam String platform,
            HttpServletRequest request) {
        
        try {
            long timestamp = System.currentTimeMillis();
            String signature = securityService.generateRequestSignature(request, timestamp);
            
            adService.trackImpression(adId, platform, request);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", true);
            
            return ResponseEntity.ok()
                    .header("X-Timestamp", String.valueOf(timestamp))
                    .header("X-Signature", signature)
                    .body(response);
        } catch (Exception e) {
            log.error("记录广告展示失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 记录广告点击
     */
    @PostMapping("/track/click/{adId}")
    public ResponseEntity<Map<String, Boolean>> trackClick(
            @PathVariable String adId,
            @Valid @RequestBody TrackRequest trackRequest,
            HttpServletRequest request) {
        
        try {
            long timestamp = System.currentTimeMillis();
            String signature = securityService.generateRequestSignature(request, timestamp);
            
            adService.trackClick(adId, trackRequest, request);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", true);
            
            return ResponseEntity.ok()
                    .header("X-Timestamp", String.valueOf(timestamp))
                    .header("X-Signature", signature)
                    .body(response);
        } catch (Exception e) {
            log.error("记录广告点击失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
} 