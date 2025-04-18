package com.adverge.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 竞价响应类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResponse {
    /**
     * 广告平台来源
     */
    private String source;
    
    /**
     * 广告ID（平台提供的标识）
     */
    private String adId;
    
    /**
     * 竞价价格
     */
    private double price;
    
    /**
     * 货币类型（默认USD）
     */
    private String currency = "USD";
    
    /**
     * 竞价标识（Token）
     */
    private String bidToken;
    
    /**
     * 广告内容
     */
    private String adContent;
    
    /**
     * 广告素材URL
     */
    private String creativeUrl;
    
    /**
     * 广告点击URL
     */
    private String clickUrl;
    
    /**
     * 广告曝光URL
     */
    private String impressionUrl;
    
    /**
     * 平台特定参数（JSON字符串）
     */
    private String platformParams;
    
    /**
     * 广告加载超时（毫秒）
     */
    private int loadTimeout = 30000;
    
    /**
     * 广告预热时间（毫秒）
     */
    private int warmupTime = 0;
} 