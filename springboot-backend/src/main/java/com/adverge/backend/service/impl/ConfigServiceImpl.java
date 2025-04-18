package com.adverge.backend.service.impl;

import com.adverge.backend.model.Config;
import com.adverge.backend.repository.ConfigRepository;
import com.adverge.backend.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 配置服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final ConfigRepository configRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String CONFIG_CACHE_KEY = "config";
    private static final int CONFIG_CACHE_TTL = 300; // 5分钟

    @Override
    public Config getConfig() {
        // 尝试从缓存获取
        Object cachedConfig = redisTemplate.opsForValue().get(CONFIG_CACHE_KEY);
        if (cachedConfig instanceof Config) {
            return (Config) cachedConfig;
        }
        
        // 从数据库获取
        Config config = configRepository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    // 如果不存在，创建默认配置
                    Config defaultConfig = new Config();
                    return configRepository.save(defaultConfig);
                });
        
        // 缓存配置
        redisTemplate.opsForValue().set(CONFIG_CACHE_KEY, config, CONFIG_CACHE_TTL, TimeUnit.SECONDS);
        
        return config;
    }

    @Override
    public Config saveConfig(Config config) {
        config.updateTimestamp();
        Config savedConfig = configRepository.save(config);
        
        // 更新缓存
        redisTemplate.opsForValue().set(CONFIG_CACHE_KEY, savedConfig, CONFIG_CACHE_TTL, TimeUnit.SECONDS);
        
        return savedConfig;
    }

    @Override
    public List<Config.Platform> getPlatforms() {
        Config config = getConfig();
        return config.getPlatforms() != null ? 
                new ArrayList<>(config.getPlatforms()) : 
                new ArrayList<>();
    }

    @Override
    public Config.Platform getPlatform(String name) {
        if (name == null) {
            return null;
        }
        
        Config config = getConfig();
        return config.getPlatforms()
                .stream()
                .filter(p -> name.equalsIgnoreCase(p.getName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Config.Platform savePlatform(Config.Platform platform) {
        if (platform == null || platform.getName() == null) {
            throw new IllegalArgumentException("平台名称不能为空");
        }
        
        Config config = getConfig();
        List<Config.Platform> platforms = config.getPlatforms();
        
        // 移除同名平台
        platforms = platforms.stream()
                .filter(p -> !platform.getName().equalsIgnoreCase(p.getName()))
                .collect(Collectors.toList());
        
        // 添加新平台
        platforms.add(platform);
        config.setPlatforms(platforms);
        config.updateTimestamp();
        
        // 保存配置
        configRepository.save(config);
        
        // 更新缓存
        redisTemplate.opsForValue().set(CONFIG_CACHE_KEY, config, CONFIG_CACHE_TTL, TimeUnit.SECONDS);
        
        return platform;
    }

    @Override
    public boolean deletePlatform(String name) {
        if (name == null) {
            return false;
        }
        
        Config config = getConfig();
        List<Config.Platform> platforms = config.getPlatforms();
        
        int sizeBefore = platforms.size();
        
        // 移除指定平台
        platforms = platforms.stream()
                .filter(p -> !name.equalsIgnoreCase(p.getName()))
                .collect(Collectors.toList());
        
        if (sizeBefore == platforms.size()) {
            // 没有找到要删除的平台
            return false;
        }
        
        config.setPlatforms(platforms);
        config.updateTimestamp();
        
        // 保存配置
        configRepository.save(config);
        
        // 更新缓存
        redisTemplate.opsForValue().set(CONFIG_CACHE_KEY, config, CONFIG_CACHE_TTL, TimeUnit.SECONDS);
        
        return true;
    }

    @Override
    public Config.Platform setPlatformEnabled(String name, boolean enabled) {
        if (name == null) {
            throw new IllegalArgumentException("平台名称不能为空");
        }
        
        Config config = getConfig();
        
        // 查找平台
        Config.Platform platform = config.getPlatforms()
                .stream()
                .filter(p -> name.equalsIgnoreCase(p.getName()))
                .findFirst()
                .orElse(null);
        
        if (platform == null) {
            throw new IllegalArgumentException("未找到平台: " + name);
        }
        
        // 设置状态
        platform.setEnabled(enabled);
        config.updateTimestamp();
        
        // 保存配置
        configRepository.save(config);
        
        // 更新缓存
        redisTemplate.opsForValue().set(CONFIG_CACHE_KEY, config, CONFIG_CACHE_TTL, TimeUnit.SECONDS);
        
        return platform;
    }

    @Override
    public Config updateBidTimeout(int timeout) {
        if (timeout < 1000 || timeout > 30000) {
            throw new IllegalArgumentException("超时时间必须在1000-30000毫秒之间");
        }
        
        Config config = getConfig();
        config.setBidTimeout(timeout);
        config.updateTimestamp();
        
        // 保存配置
        Config savedConfig = configRepository.save(config);
        
        // 更新缓存
        redisTemplate.opsForValue().set(CONFIG_CACHE_KEY, savedConfig, CONFIG_CACHE_TTL, TimeUnit.SECONDS);
        
        return savedConfig;
    }

    @Override
    public Config updateCacheExpiry(int expiry) {
        if (expiry < 60 || expiry > 3600) {
            throw new IllegalArgumentException("缓存过期时间必须在60-3600秒之间");
        }
        
        Config config = getConfig();
        config.setCacheExpiry(expiry);
        config.updateTimestamp();
        
        // 保存配置
        Config savedConfig = configRepository.save(config);
        
        // 更新缓存
        redisTemplate.opsForValue().set(CONFIG_CACHE_KEY, savedConfig, CONFIG_CACHE_TTL, TimeUnit.SECONDS);
        
        return savedConfig;
    }
} 