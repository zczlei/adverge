package com.adverge.backend.service;

import com.adverge.backend.dto.AdUnitRequest;
import com.adverge.backend.model.AdUnit;

import java.util.List;

public interface AdUnitService {

    /**
     * 获取所有广告位
     */
    List<AdUnit> getAllAdUnits();
    
    /**
     * 根据ID获取广告位
     */
    AdUnit getAdUnitById(String id);
    
    /**
     * 创建新广告位
     */
    AdUnit createAdUnit(AdUnitRequest adUnitRequest);
    
    /**
     * 更新广告位信息
     */
    AdUnit updateAdUnit(String id, AdUnitRequest adUnitRequest);
    
    /**
     * 删除广告位
     */
    void deleteAdUnit(String id);
    
    /**
     * 根据应用ID获取广告位
     */
    List<AdUnit> getAdUnitsByAppId(String appId);
    
    /**
     * 根据类型获取广告位
     */
    List<AdUnit> getAdUnitsByType(String type);
    
    /**
     * 根据多个ID获取广告位
     */
    List<AdUnit> getAdUnitsByIds(List<String> ids);
    
    /**
     * 获取活跃广告位
     */
    List<AdUnit> getActiveAdUnits();
    
    /**
     * 获取应用的活跃广告位
     */
    List<AdUnit> getActiveAdUnitsByAppId(String appId);
} 