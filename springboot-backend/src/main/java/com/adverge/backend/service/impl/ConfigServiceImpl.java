package com.adverge.backend.service.impl;

import com.adverge.backend.model.App;
import com.adverge.backend.model.Config;
import com.adverge.backend.repository.ConfigRepository;
import com.adverge.backend.service.AppService;
import com.adverge.backend.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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
    private final AppService appService;
    
    private static final String CONFIG_CACHE_KEY = "config";
    private static final long CONFIG_CACHE_TTL = 3600; // 1小时缓存

    @Override
    public Config getConfigById(String id) {
        return configRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("配置不存在: " + id));
    }
    
    @Override
    public Config getConfigByAppId(String appId) {
        // 尝试从缓存获取
        String cacheKey = CONFIG_CACHE_KEY + ":" + appId;
        Object cachedConfig = redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedConfig != null) {
            return (Config) cachedConfig;
        }
        
        // 从数据库获取
        Config config = configRepository.findByAppId(appId)
                .orElseThrow(() -> new EntityNotFoundException("应用配置不存在: " + appId));
        
        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, config, CONFIG_CACHE_TTL, TimeUnit.SECONDS);
        
        return config;
    }
    
    @Override
    public List<Config> getAllConfigs() {
        return configRepository.findAll();
    }

    @Override
    public Config saveConfig(Config config) {
        // 设置时间戳
        if (config.getCreatedAt() == null) {
            config.setCreatedAt(new Date());
        }
        config.setUpdatedAt(new Date());
        
        Config savedConfig = configRepository.save(config);
        
        // 更新缓存
        String cacheKey = CONFIG_CACHE_KEY + ":" + savedConfig.getAppId();
        redisTemplate.opsForValue().set(cacheKey, savedConfig, CONFIG_CACHE_TTL, TimeUnit.SECONDS);
        
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
        
        try {
            // 使用安全地JPQL查询来获取平台数据，而不是访问懒加载集合
            // 使用EntityManager直接查询可以避免LazyInitializationException
            List<Config.Platform> platforms = configRepository.findPlatformByName(name);
            return platforms.isEmpty() ? null : platforms.get(0);
        } catch (Exception e) {
            log.error("获取平台配置失败: {}", name, e);
            return null;
        }
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

    /**
     * 获取默认配置
     * @return 配置对象
     */
    @Override
    public Config getConfig() {
        // 尝试从缓存获取
        Object cachedConfig = redisTemplate.opsForValue().get(CONFIG_CACHE_KEY);
        
        if (cachedConfig != null) {
            return (Config) cachedConfig;
        }
        
        // 从数据库获取所有配置
        List<Config> configs = configRepository.findAll();
        
        Config config;
        if (configs.isEmpty()) {
            // 如果没有配置，创建默认配置
            config = new Config();
            config.setId(UUID.randomUUID().toString());
            config.setBidTimeout(5000);
            config.setCacheExpiry(300);
            config.setPlatforms(new ArrayList<>());
            config.setCreatedAt(new Date());
            config.setUpdatedAt(new Date());
            
            // 保存到数据库
            config = configRepository.save(config);
        } else {
            // 获取第一个配置
            config = configs.get(0);
        }
        
        // 存入缓存
        redisTemplate.opsForValue().set(CONFIG_CACHE_KEY, config, CONFIG_CACHE_TTL, TimeUnit.SECONDS);
        
        return config;
    }
} 