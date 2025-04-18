package com.adverge.backend.controller;

import com.adverge.backend.dto.PlatformResponse;
import com.adverge.backend.model.Config;
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
import java.util.stream.Collectors;

/**
 * 广告平台管理控制器
 * 提供Web管理界面所需的REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/platforms")
@RequiredArgsConstructor
public class PlatformManagementController {

    private final ConfigService configService;
    private final SecurityService securityService;

    /**
     * 获取所有平台
     */
    @GetMapping
    public ResponseEntity<List<PlatformResponse>> getAllPlatforms() {
        List<Config.Platform> platforms = configService.getPlatforms();
        List<PlatformResponse> response = platforms.stream()
                .map(PlatformResponse::fromPlatform)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * 获取指定平台
     */
    @GetMapping("/{name}")
    public ResponseEntity<PlatformResponse> getPlatform(@PathVariable String name) {
        Config.Platform platform = configService.getPlatform(name);
        if (platform == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(PlatformResponse.fromPlatform(platform));
    }

    /**
     * 添加新平台
     */
    @PostMapping
    public ResponseEntity<PlatformResponse> addPlatform(
            @Valid @RequestBody PlatformResponse platformRequest) {
        
        // 转换为Config.Platform
        Config.Platform platform = new Config.Platform();
        platform.setName(platformRequest.getName());
        platform.setAppId(platformRequest.getAppId());
        platform.setAppKey(platformRequest.getAppKey());
        platform.setPlacementId(platformRequest.getPlacementId());
        platform.setEnabled(platformRequest.isEnabled());
        platform.setBidFloor(platformRequest.getBidFloor());
        
        Config.Platform savedPlatform = configService.savePlatform(platform);
        return ResponseEntity.ok(PlatformResponse.fromPlatform(savedPlatform));
    }

    /**
     * 更新平台
     */
    @PutMapping("/{name}")
    public ResponseEntity<PlatformResponse> updatePlatform(
            @PathVariable String name,
            @Valid @RequestBody PlatformResponse platformRequest) {
        
        // 检查平台是否存在
        Config.Platform existingPlatform = configService.getPlatform(name);
        if (existingPlatform == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 转换为Config.Platform
        Config.Platform platform = new Config.Platform();
        platform.setName(name);
        platform.setAppId(platformRequest.getAppId());
        platform.setAppKey(platformRequest.getAppKey());
        platform.setPlacementId(platformRequest.getPlacementId());
        platform.setEnabled(platformRequest.isEnabled());
        platform.setBidFloor(platformRequest.getBidFloor());
        
        Config.Platform updatedPlatform = configService.savePlatform(platform);
        return ResponseEntity.ok(PlatformResponse.fromPlatform(updatedPlatform));
    }

    /**
     * 更新平台AppID
     */
    @PutMapping("/{name}/app-id")
    public ResponseEntity<PlatformResponse> updatePlatformAppId(
            @PathVariable String name,
            @RequestBody Map<String, String> request) {
        
        if (!request.containsKey("appId")) {
            return ResponseEntity.badRequest().build();
        }
        
        // 检查平台是否存在
        Config.Platform existingPlatform = configService.getPlatform(name);
        if (existingPlatform == null) {
            return ResponseEntity.notFound().build();
        }
        
        existingPlatform.setAppId(request.get("appId"));
        Config.Platform updatedPlatform = configService.savePlatform(existingPlatform);
        return ResponseEntity.ok(PlatformResponse.fromPlatform(updatedPlatform));
    }

    /**
     * 更新平台AppKey
     */
    @PutMapping("/{name}/app-key")
    public ResponseEntity<PlatformResponse> updatePlatformAppKey(
            @PathVariable String name,
            @RequestBody Map<String, String> request) {
        
        if (!request.containsKey("appKey")) {
            return ResponseEntity.badRequest().build();
        }
        
        // 检查平台是否存在
        Config.Platform existingPlatform = configService.getPlatform(name);
        if (existingPlatform == null) {
            return ResponseEntity.notFound().build();
        }
        
        existingPlatform.setAppKey(request.get("appKey"));
        Config.Platform updatedPlatform = configService.savePlatform(existingPlatform);
        return ResponseEntity.ok(PlatformResponse.fromPlatform(updatedPlatform));
    }

    /**
     * 更新平台PlacementId
     */
    @PutMapping("/{name}/placement-id")
    public ResponseEntity<PlatformResponse> updatePlatformPlacementId(
            @PathVariable String name,
            @RequestBody Map<String, String> request) {
        
        if (!request.containsKey("placementId")) {
            return ResponseEntity.badRequest().build();
        }
        
        // 检查平台是否存在
        Config.Platform existingPlatform = configService.getPlatform(name);
        if (existingPlatform == null) {
            return ResponseEntity.notFound().build();
        }
        
        existingPlatform.setPlacementId(request.get("placementId"));
        Config.Platform updatedPlatform = configService.savePlatform(existingPlatform);
        return ResponseEntity.ok(PlatformResponse.fromPlatform(updatedPlatform));
    }

    /**
     * 更新平台底价
     */
    @PutMapping("/{name}/bid-floor")
    public ResponseEntity<PlatformResponse> updatePlatformBidFloor(
            @PathVariable String name,
            @RequestBody Map<String, Double> request) {
        
        if (!request.containsKey("bidFloor")) {
            return ResponseEntity.badRequest().build();
        }
        
        // 检查平台是否存在
        Config.Platform existingPlatform = configService.getPlatform(name);
        if (existingPlatform == null) {
            return ResponseEntity.notFound().build();
        }
        
        existingPlatform.setBidFloor(request.get("bidFloor"));
        Config.Platform updatedPlatform = configService.savePlatform(existingPlatform);
        return ResponseEntity.ok(PlatformResponse.fromPlatform(updatedPlatform));
    }

    /**
     * 启用平台
     */
    @PostMapping("/{name}/enable")
    public ResponseEntity<Map<String, Boolean>> enablePlatform(@PathVariable String name) {
        try {
            configService.setPlatformEnabled(name, true);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("启用平台失败: {}", name, e);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", false);
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 禁用平台
     */
    @PostMapping("/{name}/disable")
    public ResponseEntity<Map<String, Boolean>> disablePlatform(@PathVariable String name) {
        try {
            configService.setPlatformEnabled(name, false);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("禁用平台失败: {}", name, e);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", false);
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除平台
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<Map<String, Boolean>> deletePlatform(@PathVariable String name) {
        try {
            boolean deleted = configService.deletePlatform(name);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", deleted);
            
            if (deleted) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("删除平台失败: {}", name, e);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", false);
            
            return ResponseEntity.badRequest().body(response);
        }
    }
} 