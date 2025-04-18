package com.adverge.backend.controller;

import com.adverge.backend.dto.AdEventDto;
import com.adverge.backend.service.EventService;
import com.adverge.backend.service.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 事件控制器
 */
@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final SecurityService securityService;

    /**
     * 处理事件
     */
    @PostMapping
    public ResponseEntity<Map<String, Boolean>> processEvent(
            @Valid @RequestBody AdEventDto event,
            HttpServletRequest request) {
        
        try {
            long timestamp = System.currentTimeMillis();
            String signature = securityService.generateRequestSignature(request, timestamp);
            
            eventService.processEvent(event);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", true);
            
            return ResponseEntity.ok()
                    .header("X-Timestamp", String.valueOf(timestamp))
                    .header("X-Signature", signature)
                    .body(response);
        } catch (Exception e) {
            log.error("处理事件失败", e);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", false);
            
            return ResponseEntity.badRequest()
                    .body(response);
        }
    }
    
    /**
     * 记录请求事件
     */
    @PostMapping("/request")
    public ResponseEntity<Map<String, Boolean>> logRequest(
            @RequestBody Map<String, Object> requestData,
            HttpServletRequest request) {
        
        try {
            long timestamp = System.currentTimeMillis();
            String signature = securityService.generateRequestSignature(request, timestamp);
            
            String appId = String.valueOf(requestData.getOrDefault("appId", "unknown"));
            String adUnitId = String.valueOf(requestData.getOrDefault("adUnitId", "unknown"));
            Object deviceInfo = requestData.get("deviceInfo");
            
            eventService.logRequestEvent(appId, adUnitId, deviceInfo);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", true);
            
            return ResponseEntity.ok()
                    .header("X-Timestamp", String.valueOf(timestamp))
                    .header("X-Signature", signature)
                    .body(response);
        } catch (Exception e) {
            log.error("记录请求事件失败", e);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", false);
            
            return ResponseEntity.badRequest()
                    .body(response);
        }
    }
    
    /**
     * 记录错误事件
     */
    @PostMapping("/error")
    public ResponseEntity<Map<String, Boolean>> logError(
            @RequestBody Map<String, Object> errorData,
            HttpServletRequest request) {
        
        try {
            long timestamp = System.currentTimeMillis();
            String signature = securityService.generateRequestSignature(request, timestamp);
            
            String appId = String.valueOf(errorData.getOrDefault("appId", "unknown"));
            String adUnitId = String.valueOf(errorData.getOrDefault("adUnitId", "unknown"));
            String platform = String.valueOf(errorData.getOrDefault("platform", "unknown"));
            String errorMsg = String.valueOf(errorData.getOrDefault("error", "未知错误"));
            
            eventService.logErrorEvent(appId, adUnitId, platform, errorMsg);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", true);
            
            return ResponseEntity.ok()
                    .header("X-Timestamp", String.valueOf(timestamp))
                    .header("X-Signature", signature)
                    .body(response);
        } catch (Exception e) {
            log.error("记录错误事件失败", e);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", false);
            
            return ResponseEntity.badRequest()
                    .body(response);
        }
    }
} 