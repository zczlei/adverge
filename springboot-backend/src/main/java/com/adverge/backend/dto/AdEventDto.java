package com.adverge.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 广告事件数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdEventDto {
    
    /**
     * 事件类型
     */
    private EventType eventType;
    
    /**
     * 应用ID
     */
    private String appId;
    
    /**
     * 广告单元ID
     */
    private String adUnitId;
    
    /**
     * 广告平台
     */
    private String platform;
    
    /**
     * 广告ID
     */
    private String adId;
    
    /**
     * 价格/收益
     */
    private double price;
    
    /**
     * 事件时间
     */
    private Date eventTime;
    
    /**
     * 设备信息
     */
    private AdRequest.DeviceInfo deviceInfo;
    
    /**
     * 事件相关数据
     */
    private Object data;
    
    /**
     * 事件类型枚举
     */
    public enum EventType {
        REQUEST, // 请求广告
        BID,     // 竞价结果
        IMPRESSION, // 广告展示
        CLICK,   // 广告点击
        CLOSE,   // 广告关闭
        ERROR    // 错误事件
    }
} 