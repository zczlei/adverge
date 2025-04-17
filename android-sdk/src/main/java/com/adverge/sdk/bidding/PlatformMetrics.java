package com.adverge.sdk.bidding;

public class PlatformMetrics {
    private String platform;
    private double fillRate;          // 填充率
    private double averageEcpm;       // 平均eCPM
    private double averageLoadTime;   // 平均加载时间(ms)
    private double clickThroughRate;  // 点击率
    private int totalRequests;        // 总请求数
    private int successfulRequests;   // 成功请求数
    private long totalRevenue;        // 总收入
    
    public PlatformMetrics(String platform) {
        this.platform = platform;
        this.fillRate = 0.0;
        this.averageEcpm = 0.0;
        this.averageLoadTime = 0.0;
        this.clickThroughRate = 0.0;
        this.totalRequests = 0;
        this.successfulRequests = 0;
        this.totalRevenue = 0;
    }
    
    // Getters and Setters
    public String getPlatform() {
        return platform;
    }
    
    public double getFillRate() {
        return fillRate;
    }
    
    public void setFillRate(double fillRate) {
        this.fillRate = fillRate;
    }
    
    public double getAverageEcpm() {
        return averageEcpm;
    }
    
    public void setAverageEcpm(double averageEcpm) {
        this.averageEcpm = averageEcpm;
    }
    
    public double getAverageLoadTime() {
        return averageLoadTime;
    }
    
    public void setAverageLoadTime(double averageLoadTime) {
        this.averageLoadTime = averageLoadTime;
    }
    
    public double getClickThroughRate() {
        return clickThroughRate;
    }
    
    public void setClickThroughRate(double clickThroughRate) {
        this.clickThroughRate = clickThroughRate;
    }
    
    public int getTotalRequests() {
        return totalRequests;
    }
    
    public void setTotalRequests(int totalRequests) {
        this.totalRequests = totalRequests;
    }
    
    public int getSuccessfulRequests() {
        return successfulRequests;
    }
    
    public void setSuccessfulRequests(int successfulRequests) {
        this.successfulRequests = successfulRequests;
    }
    
    public long getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(long totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    // 更新指标
    public void updateMetrics(boolean success, long loadTime, double ecpm) {
        totalRequests++;
        if (success) {
            successfulRequests++;
        }
        // 更新填充率
        fillRate = (double) successfulRequests / totalRequests;
        
        // 更新平均加载时间
        averageLoadTime = (averageLoadTime * (totalRequests - 1) + loadTime) / totalRequests;
        
        // 更新平均eCPM
        if (success) {
            averageEcpm = (averageEcpm * (successfulRequests - 1) + ecpm) / successfulRequests;
        }
    }
} 