package com.adverge.sdk.bidding;

import java.util.HashMap;
import java.util.Map;

public class AdRequest {
    private String adUnitId;
    private String adType;
    private String platform;
    private String deviceType;
    private String country;
    private Map<String, Object> parameters;
    
    public AdRequest() {
        parameters = new HashMap<>();
    }
    
    // Getters and Setters
    public String getAdUnitId() {
        return adUnitId;
    }
    
    public void setAdUnitId(String adUnitId) {
        this.adUnitId = adUnitId;
    }
    
    public String getAdType() {
        return adType;
    }
    
    public void setAdType(String adType) {
        this.adType = adType;
    }
    
    public String getPlatform() {
        return platform;
    }
    
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    
    public String getDeviceType() {
        return deviceType;
    }
    
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public void addParameter(String key, Object value) {
        parameters.put(key, value);
    }
    
    public Object getParameter(String key) {
        return parameters.get(key);
    }
} 