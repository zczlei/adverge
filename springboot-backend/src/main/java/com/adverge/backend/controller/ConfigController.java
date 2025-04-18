package com.adverge.backend.controller;

import com.adverge.backend.model.Config;
import com.adverge.backend.repository.ConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigRepository configRepository;

    /**
     * 获取配置信息
     */
    @GetMapping
    public ResponseEntity<Config> getConfig() {
        List<Config> configs = configRepository.findAll();
        Config config = configs.isEmpty() ? new Config() : configs.get(0);
        return ResponseEntity.ok(config);
    }

    /**
     * 更新配置信息
     */
    @PutMapping
    public ResponseEntity<Config> updateConfig(@Valid @RequestBody Config config) {
        List<Config> existingConfigs = configRepository.findAll();
        
        if (existingConfigs.isEmpty()) {
            // 创建新配置
            return ResponseEntity.ok(configRepository.save(config));
        } else {
            // 更新现有配置
            Config existingConfig = existingConfigs.get(0);
            existingConfig.setBidTimeout(config.getBidTimeout());
            existingConfig.setCacheExpiry(config.getCacheExpiry());
            existingConfig.setPlatforms(config.getPlatforms());
            existingConfig.setUpdatedAt(new Date());
            
            return ResponseEntity.ok(configRepository.save(existingConfig));
        }
    }

    /**
     * 添加平台
     */
    @PostMapping("/platforms")
    public ResponseEntity<Config> addPlatform(@Valid @RequestBody Config.Platform platform) {
        List<Config> configs = configRepository.findAll();
        Config config = configs.isEmpty() ? new Config() : configs.get(0);
        
        config.getPlatforms().add(platform);
        config.setUpdatedAt(new Date());
        
        return ResponseEntity.ok(configRepository.save(config));
    }

    /**
     * 更新平台
     */
    @PutMapping("/platforms/{platformName}")
    public ResponseEntity<Config> updatePlatform(
            @PathVariable String platformName,
            @Valid @RequestBody Config.Platform platform) {
        
        List<Config> configs = configRepository.findAll();
        if (configs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Config config = configs.get(0);
        boolean found = false;
        
        for (int i = 0; i < config.getPlatforms().size(); i++) {
            if (config.getPlatforms().get(i).getName().equals(platformName)) {
                config.getPlatforms().set(i, platform);
                found = true;
                break;
            }
        }
        
        if (!found) {
            return ResponseEntity.notFound().build();
        }
        
        config.setUpdatedAt(new Date());
        return ResponseEntity.ok(configRepository.save(config));
    }

    /**
     * 删除平台
     */
    @DeleteMapping("/platforms/{platformName}")
    public ResponseEntity<Config> deletePlatform(@PathVariable String platformName) {
        List<Config> configs = configRepository.findAll();
        if (configs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Config config = configs.get(0);
        boolean removed = config.getPlatforms().removeIf(p -> p.getName().equals(platformName));
        
        if (!removed) {
            return ResponseEntity.notFound().build();
        }
        
        config.setUpdatedAt(new Date());
        return ResponseEntity.ok(configRepository.save(config));
    }
} 