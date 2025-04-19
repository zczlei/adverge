package com.adverge.sdk.model;

/**
 * 广告响应数据模型
 * 从服务器接收的广告信息
 */
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
     * 平台特定参数
     */
    private String platformParams;
    
    // Getters and Setters
    
    public String getAdUnitId() {
        return adUnitId;
    }
    
    public void setAdUnitId(String adUnitId) {
        this.adUnitId = adUnitId;
    }
    
    public String getPlatform() {
        return platform;
    }
    
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    
    public String getAdId() {
        return adId;
    }
    
    public void setAdId(String adId) {
        this.adId = adId;
    }
    
    public String getAdContent() {
        return adContent;
    }
    
    public void setAdContent(String adContent) {
        this.adContent = adContent;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public long getExpiry() {
        return expiry;
    }
    
    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }
    
    public String getBidToken() {
        return bidToken;
    }
    
    public void setBidToken(String bidToken) {
        this.bidToken = bidToken;
    }
    
    public String getPlatformParams() {
        return platformParams;
    }
    
    public void setPlatformParams(String platformParams) {
        this.platformParams = platformParams;
    }
    
    /**
     * 检查广告是否已过期
     * @return 是否已过期
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expiry;
    }
    
    @Override
    public String toString() {
        return "AdResponse{" +
                "adUnitId='" + adUnitId + '\'' +
                ", platform='" + platform + '\'' +
                ", adId='" + adId + '\'' +
                ", price=" + price +
                ", currency='" + currency + '\'' +
                '}';
    }
} 