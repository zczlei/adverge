package com.adverge.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

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
     * 广告位ID
     */
    private String placementId;
    
    /**
     * 广告内容
     */
    private String adContent;
    
    /**
     * 竞价价格
     */
    private Double price;
    
    /**
     * 货币类型（默认USD）
     */
    private String currency;
    
    /**
     * 竞价标识（Token）
     */
    private String bidToken;
    
    /**
     * 平台特定参数（JSON字符串）
     */
    private Map<String, Object> platformParams;
    
    /**
     * 广告数据
     */
    private AdData adData;
    
    /**
     * 错误码
     */
    private String errorCode;
    
    /**
     * 错误消息
     */
    private String errorMessage;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 嵌套的广告数据类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdData {
        private String adId;
        private String title;
        private String description;
        private String imageUrl;
        private String iconUrl;
        private String ctaText;
        private String landingUrl;
        private String clickUrl;
        private String impressionUrl;
        private Map<String, String> trackingUrls;
        private Map<String, Object> metadata;
    }
} 