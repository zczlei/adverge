package com.adverge.sdk.bidding;

import java.util.HashMap;
import java.util.Map;

public class BidResponse {
    private double bidPrice;
    private String platform;
    private String bidToken;
    private long timestamp;
    private Map<String, Object> additionalInfo;
    
    public BidResponse(double bidPrice, String platform) {
        this.bidPrice = bidPrice;
        this.platform = platform;
        this.timestamp = System.currentTimeMillis();
        this.additionalInfo = new HashMap<>();
    }
    
    // Getters and Setters
    public double getBidPrice() {
        return bidPrice;
    }
    
    public void setBidPrice(double bidPrice) {
        this.bidPrice = bidPrice;
    }
    
    public String getPlatform() {
        return platform;
    }
    
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    
    public String getBidToken() {
        return bidToken;
    }
    
    public void setBidToken(String bidToken) {
        this.bidToken = bidToken;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }
    
    public void setAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
    
    public void addInfo(String key, Object value) {
        additionalInfo.put(key, value);
    }
    
    public Object getInfo(String key) {
        return additionalInfo.get(key);
    }
    
    // 检查竞价是否过期（默认5分钟）
    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > 5 * 60 * 1000;
    }
} 