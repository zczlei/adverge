package com.adverge.backend.service;

import com.adverge.backend.model.Config;

import java.util.List;

/**
 * 配置服务接口
 */
public interface ConfigService {
    
    /**
     * 根据ID获取配置
     * @param id 配置ID
     * @return 配置对象
     */
    Config getConfigById(String id);
    
    /**
     * 根据应用ID获取配置
     * @param appId 应用ID
     * @return 配置对象
     */
    Config getConfigByAppId(String appId);
    
    /**
     * 获取所有配置
     * @return 配置列表
     */
    List<Config> getAllConfigs();
    
    /**
     * 获取默认配置
     * @return 配置对象
     */
    Config getConfig();
    
    /**
     * 保存配置
     * @param config 配置对象
     * @return 保存后的配置对象
     */
    Config saveConfig(Config config);
    
    /**
     * 获取所有平台
     * @return 平台列表
     */
    List<Config.Platform> getPlatforms();
    
    /**
     * 获取指定名称的平台
     * @param name 平台名称
     * @return 平台对象，如果不存在则返回null
     */
    Config.Platform getPlatform(String name);
    
    /**
     * 保存平台
     * @param platform 平台对象
     * @return 保存后的平台对象
     */
    Config.Platform savePlatform(Config.Platform platform);
    
    /**
     * 删除平台
     * @param name 平台名称
     * @return 是否删除成功
     */
    boolean deletePlatform(String name);
    
    /**
     * 设置平台启用状态
     * @param name 平台名称
     * @param enabled 是否启用
     * @return 更新后的平台对象
     */
    Config.Platform setPlatformEnabled(String name, boolean enabled);
    
    /**
     * 更新竞价超时时间
     * @param timeout 超时时间(毫秒)
     * @return 更新后的配置对象
     */
    Config updateBidTimeout(int timeout);
    
    /**
     * 更新缓存过期时间
     * @param expiry 过期时间(秒)
     * @return 更新后的配置对象
     */
    Config updateCacheExpiry(int expiry);
} 