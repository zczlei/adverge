package com.adverge.backend.controller;

import com.adverge.backend.dto.AppRequest;
import com.adverge.backend.model.App;
import com.adverge.backend.service.AppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/apps")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;

    /**
     * 获取所有应用
     */
    @GetMapping
    public ResponseEntity<List<App>> getAllApps() {
        List<App> apps = appService.getAllApps();
        return ResponseEntity.ok(apps);
    }

    /**
     * 根据ID获取应用
     */
    @GetMapping("/{id}")
    public ResponseEntity<App> getAppById(@PathVariable String id) {
        return appService.getAppById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建新应用
     */
    @PostMapping
    public ResponseEntity<App> createApp(@Valid @RequestBody AppRequest appRequest) {
        App createdApp = appService.createApp(appRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdApp);
    }

    /**
     * 更新应用信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<App> updateApp(@PathVariable String id, @Valid @RequestBody AppRequest appRequest) {
        App updatedApp = appService.updateApp(id, appRequest);
        return ResponseEntity.ok(updatedApp);
    }

    /**
     * 删除应用
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApp(@PathVariable String id) {
        appService.deleteApp(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据平台获取应用
     */
    @GetMapping("/platform/{platform}")
    public ResponseEntity<List<App>> getAppsByPlatform(@PathVariable String platform) {
        List<App> apps = appService.getAppsByPlatform(platform);
        return ResponseEntity.ok(apps);
    }

    /**
     * 获取活跃应用
     */
    @GetMapping("/active")
    public ResponseEntity<List<App>> getActiveApps() {
        List<App> apps = appService.getActiveApps();
        return ResponseEntity.ok(apps);
    }
} 