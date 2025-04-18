package com.adverge.backend.service;

import com.adverge.backend.dto.AdEventDto;

/**
 * 事件服务接口
 */
public interface EventService {
    
    /**
     * 处理广告事件
     * @param event 广告事件
     */
    void processEvent(AdEventDto event);
    
    /**
     * 记录广告请求事件
     * @param appId 应用ID
     * @param adUnitId 广告单元ID
     * @param deviceInfo 设备信息
     */
    void logRequestEvent(String appId, String adUnitId, Object deviceInfo);
    
    /**
     * 记录广告竞价事件
     * @param appId 应用ID
     * @param adUnitId 广告单元ID
     * @param platform 平台
     * @param price 价格
     */
    void logBidEvent(String appId, String adUnitId, String platform, double price);
    
    /**
     * 记录广告展示事件
     * @param appId 应用ID
     * @param adUnitId 广告单元ID
     * @param platform 平台
     * @param adId 广告ID
     */
    void logImpressionEvent(String appId, String adUnitId, String platform, String adId);
    
    /**
     * 记录广告点击事件
     * @param appId 应用ID
     * @param adUnitId 广告单元ID
     * @param platform 平台
     * @param adId 广告ID
     */
    void logClickEvent(String appId, String adUnitId, String platform, String adId);
    
    /**
     * 记录广告错误事件
     * @param appId 应用ID
     * @param adUnitId 广告单元ID
     * @param platform 平台
     * @param errorMsg 错误信息
     */
    void logErrorEvent(String appId, String adUnitId, String platform, String errorMsg);
} 