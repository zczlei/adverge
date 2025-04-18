package com.adverge.backend.service;

import com.adverge.backend.dto.AppRequest;
import com.adverge.backend.model.App;

import java.util.List;
import java.util.Optional;

/**
 * 应用服务接口
 */
public interface AppService {
    
    /**
     * 获取所有应用
     * 
     * @return 应用列表
     */
    List<App> getAllApps();
    
    /**
     * 根据ID获取应用
     * 
     * @param id 应用ID
     * @return 应用（可能为空）
     */
    Optional<App> getAppById(String id);
    
    /**
     * 根据名称获取应用
     * 
     * @param name 应用名称
     * @return 应用（可能为空）
     */
    Optional<App> getAppByName(String name);
    
    /**
     * 根据包名获取应用
     * 
     * @param packageName 包名
     * @return 应用（可能为空）
     */
    Optional<App> getAppByPackageName(String packageName);
    
    /**
     * 根据API密钥获取应用
     * 
     * @param apiKey API密钥
     * @return 应用（可能为空）
     */
    Optional<App> getAppByApiKey(String apiKey);
    
    /**
     * 创建或更新应用
     * 
     * @param app 应用对象
     * @return 保存后的应用
     */
    App saveApp(App app);
    
    /**
     * 删除应用
     * 
     * @param id 应用ID
     */
    void deleteApp(String id);
    
    /**
     * 设置应用启用状态
     * 
     * @param id 应用ID
     * @param enabled 启用状态
     * @return 更新后的应用（可能为空）
     */
    Optional<App> setAppEnabled(String id, boolean enabled);
    
    /**
     * 添加广告单元ID
     * 
     * @param id 应用ID
     * @param adUnitId 广告单元ID
     * @return 更新后的应用（可能为空）
     */
    Optional<App> addAdUnitId(String id, String adUnitId);
    
    /**
     * 移除广告单元ID
     * 
     * @param id 应用ID
     * @param adUnitId 广告单元ID
     * @return 更新后的应用（可能为空）
     */
    Optional<App> removeAdUnitId(String id, String adUnitId);
    
    /**
     * 获取所有启用的应用
     * 
     * @return 启用的应用列表
     */
    List<App> getAllEnabledApps();
    
    /**
     * 重新生成API密钥
     * 
     * @param id 应用ID
     * @return 更新后的应用（可能为空）
     */
    Optional<App> regenerateApiKey(String id);
    
    /**
     * 创建新应用
     * 
     * @param appRequest 应用请求
     * @return 创建的应用
     */
    App createApp(AppRequest appRequest);
    
    /**
     * 更新应用
     * 
     * @param id 应用ID
     * @param appRequest 应用请求
     * @return 更新后的应用
     */
    App updateApp(String id, AppRequest appRequest);
    
    /**
     * 获取平台上的应用
     * 
     * @param platform 平台
     * @return 应用列表
     */
    List<App> getAppsByPlatform(String platform);
    
    /**
     * 获取活跃应用
     * 
     * @return 活跃应用列表
     */
    List<App> getActiveApps();
} 