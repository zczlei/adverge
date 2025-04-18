package com.adverge.backend.service.impl;

import com.adverge.backend.model.App;
import com.adverge.backend.repository.AppRepository;
import com.adverge.backend.service.AppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;

/**
 * 应用服务的实现类
 */
@Service
public class AppServiceImpl implements AppService {
    
    private static final Logger logger = LoggerFactory.getLogger(AppServiceImpl.class);
    
    private final AppRepository appRepository;
    
    @Autowired
    public AppServiceImpl(AppRepository appRepository) {
        this.appRepository = appRepository;
    }
    
    @Override
    public List<App> getAllApps() {
        logger.debug("获取所有应用列表");
        return appRepository.findAll();
    }
    
    @Override
    public Optional<App> getAppById(String id) {
        logger.debug("通过ID获取应用: {}", id);
        return appRepository.findById(id);
    }
    
    @Override
    public Optional<App> getAppByName(String name) {
        logger.debug("通过名称获取应用: {}", name);
        return appRepository.findByName(name);
    }
    
    @Override
    public Optional<App> getAppByPackageName(String packageName) {
        logger.debug("通过包名获取应用: {}", packageName);
        return appRepository.findByPackageName(packageName);
    }
    
    @Override
    public Optional<App> getAppByApiKey(String apiKey) {
        logger.debug("通过API密钥获取应用: {}", apiKey);
        return appRepository.findByApiKey(apiKey);
    }
    
    @Override
    public App saveApp(App app) {
        logger.debug("保存应用: {}", app.getName());
        
        // 设置时间戳
        LocalDateTime now = LocalDateTime.now();
        if (app.getId() == null) {
            // 新应用
            app.setCreatedAt(now);
            
            // 生成API密钥
            if (app.getApiKey() == null || app.getApiKey().isEmpty()) {
                app.generateApiKey();
            }
        } else {
            // 确保不改变创建时间
            Optional<App> existingApp = appRepository.findById(app.getId());
            existingApp.ifPresent(existing -> app.setCreatedAt(existing.getCreatedAt()));
        }
        
        app.setUpdatedAt(now);
        return appRepository.save(app);
    }
    
    @Override
    public void deleteApp(String id) {
        logger.debug("删除应用: {}", id);
        appRepository.deleteById(id);
    }
    
    @Override
    public Optional<App> setAppEnabled(String id, boolean enabled) {
        logger.debug("设置应用 {} 的启用状态为: {}", id, enabled);
        
        return getAppById(id).map(app -> {
            app.setEnabled(enabled);
            app.setUpdatedAt(LocalDateTime.now());
            return appRepository.save(app);
        });
    }
    
    @Override
    public Optional<App> addAdUnitId(String id, String adUnitId) {
        logger.debug("向应用 {} 添加广告单元: {}", id, adUnitId);
        
        return getAppById(id).map(app -> {
            app.addAdUnitId(adUnitId);
            app.setUpdatedAt(LocalDateTime.now());
            return appRepository.save(app);
        });
    }
    
    @Override
    public Optional<App> removeAdUnitId(String id, String adUnitId) {
        logger.debug("从应用 {} 移除广告单元: {}", id, adUnitId);
        
        return getAppById(id).map(app -> {
            app.removeAdUnitId(adUnitId);
            app.setUpdatedAt(LocalDateTime.now());
            return appRepository.save(app);
        });
    }
    
    @Override
    public List<App> getAllEnabledApps() {
        logger.debug("获取所有启用的应用");
        return appRepository.findByEnabledTrue();
    }
    
    @Override
    public Optional<App> regenerateApiKey(String id) {
        logger.debug("重新生成应用 {} 的API密钥", id);
        
        return getAppById(id).map(app -> {
            app.generateApiKey();
            app.setUpdatedAt(LocalDateTime.now());
            return appRepository.save(app);
        });
    }
    
    /**
     * 生成唯一的API密钥
     * 
     * @return 生成的API密钥
     */
    private String generateApiKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }
} 