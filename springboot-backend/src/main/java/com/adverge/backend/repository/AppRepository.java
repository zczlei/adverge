package com.adverge.backend.repository;

import com.adverge.backend.model.App;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 应用存储库接口
 */
@Repository
public interface AppRepository extends MongoRepository<App, String> {
    
    /**
     * 根据应用名称查询应用
     * 
     * @param name 应用名称
     * @return 应用（可能为空）
     */
    Optional<App> findByName(String name);
    
    /**
     * 根据包名查询应用
     * 
     * @param packageName 包名
     * @return 应用（可能为空）
     */
    Optional<App> findByPackageName(String packageName);
    
    /**
     * 根据API密钥查询应用
     * 
     * @param apiKey API密钥
     * @return 应用（可能为空）
     */
    Optional<App> findByApiKey(String apiKey);
    
    /**
     * 查询所有启用的应用
     * 
     * @return 启用的应用列表
     */
    List<App> findByEnabledTrue();
    
    /**
     * 查询包含特定广告单元ID的应用
     * 
     * @param adUnitId 广告单元ID
     * @return 应用列表
     */
    List<App> findByAdUnitIdsContaining(String adUnitId);
} 