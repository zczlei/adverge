package com.adverge.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 广告响应对象
 * 用于与Android SDK通信
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdResponse {
    /**
     * 广告单元ID
     */
    private String adUnitId;
    
    /**
     * 广告平台
     */
    private String platform;
    
    /**
     * 广告ID（平台提供的）
     */
    private String adId;
    
    /**
     * 广告内容
     */
    private String adContent;
    
    /**
     * 广告价格
     */
    private double price;
    
    /**
     * 货币类型
     */
    private String currency;
    
    /**
     * 过期时间（毫秒时间戳）
     */
    private long expiry;
    
    /**
     * 竞价标识
     */
    private String bidToken;
    
    /**
     * 平台特定参数，可用于SDK初始化
     */
    private String platformParams;

    /**
     * 从BidResponse创建AdResponse
     */
    public static AdResponse fromBidResponse(BidResponse bidResponse) {
        if (bidResponse == null) {
            return null;
        }
        
        long expiryTime = System.currentTimeMillis() + 5 * 60 * 1000; // 5分钟后过期
        
        return AdResponse.builder()
                .platform(bidResponse.getSource())
                .adId(bidResponse.getAdId())
                .adContent(bidResponse.getAdContent())
                .price(bidResponse.getPrice())
                .currency(bidResponse.getCurrency())
                .expiry(expiryTime)
                .bidToken(bidResponse.getBidToken())
                .platformParams(bidResponse.getPlatformParams())
                .build();
    }
} 