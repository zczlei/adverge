package com.adverge.sdk.model;

import com.adverge.sdk.AdSDK;
import com.adverge.sdk.utils.Logger;
import com.adverge.sdk.utils.SecurityUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 广告响应类
 */
public class AdResponse {
    private static final String TAG = "AdResponse";
    
    private String id;
    private String adUnitId;
    private AdRequest.AdType adType;
    private String platform;
    private String creativeType;
    private String content;
    private double revenue;
    private double ecpm;
    private long expiryTime;
    private JSONObject responseData;
    private Map<String, String> extras;
    
    // 支持不带参数的构造函数
    public AdResponse() {
        this.extras = new HashMap<>();
        this.responseData = new JSONObject();
    }

    public AdResponse(String encryptedData) {
        this();
        try {
            SecurityUtils securityUtils = AdSDK.getInstance().getSecurityUtils();
            String decryptedData = securityUtils.decrypt(encryptedData, AdSDK.getInstance().getConfig().getAppKey());
            this.responseData = new JSONObject(decryptedData);
            
            // 解析基本字段
            this.id = getString("id");
            this.adUnitId = getString("ad_unit_id");
            this.adType = AdRequest.AdType.valueOf(getString("ad_type"));
            this.platform = getString("platform");
            this.creativeType = getString("creative_type");
            this.content = getString("content");
            this.revenue = getDouble("revenue");
            this.ecpm = getDouble("ecpm");
            this.expiryTime = getLong("expiry_time");
        } catch (Exception e) {
            Logger.e(TAG, "Failed to parse response", e);
            this.responseData = new JSONObject();
        }
    }

    private String getString(String key) {
        try {
            return responseData.getString(key);
        } catch (JSONException e) {
            Logger.e(TAG, "Failed to get string: " + key, e);
            return "";
        }
    }

    private double getDouble(String key) {
        try {
            return responseData.getDouble(key);
        } catch (JSONException e) {
            Logger.e(TAG, "Failed to get double: " + key, e);
            return 0.0;
        }
    }

    private long getLong(String key) {
        try {
            return responseData.getLong(key);
        } catch (JSONException e) {
            Logger.e(TAG, "Failed to get long: " + key, e);
            return 0L;
        }
    }

    /**
     * 获取广告ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * 设置广告ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取广告单元ID
     */
    public String getAdUnitId() {
        return adUnitId;
    }
    
    /**
     * 设置广告单元ID
     */
    public void setAdUnitId(String adUnitId) {
        this.adUnitId = adUnitId;
    }
    
    /**
     * 获取广告ID (别名方法，兼容现有代码)
     */
    public String getAdId() {
        return adUnitId;
    }

    /**
     * 获取广告类型
     */
    public AdRequest.AdType getAdType() {
        return adType;
    }
    
    /**
     * 设置广告类型
     */
    public void setAdType(AdRequest.AdType adType) {
        this.adType = adType;
    }

    /**
     * 获取广告平台
     */
    public String getPlatform() {
        return platform;
    }
    
    /**
     * 设置广告平台
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    
    /**
     * 获取平台名称 (别名方法，兼容现有代码)
     */
    public String getPlatformName() {
        return platform;
    }

    /**
     * 获取创意类型
     */
    public String getCreativeType() {
        return creativeType;
    }
    
    /**
     * 设置创意类型
     */
    public void setCreativeType(String creativeType) {
        this.creativeType = creativeType;
    }

    /**
     * 获取广告内容
     */
    public String getContent() {
        return content;
    }
    
    /**
     * 设置广告内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取广告收益
     */
    public double getRevenue() {
        return revenue;
    }
    
    /**
     * 设置广告收益
     */
    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
    
    /**
     * 获取广告eCPM
     */
    public double getEcpm() {
        return ecpm;
    }
    
    /**
     * 设置广告eCPM
     */
    public void setEcpm(double ecpm) {
        this.ecpm = ecpm;
    }

    /**
     * 获取过期时间
     */
    public long getExpiryTime() {
        return expiryTime;
    }
    
    /**
     * 设置过期时间
     */
    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    /**
     * 获取响应数据
     */
    public JSONObject getResponseData() {
        return responseData;
    }
    
    /**
     * 设置响应数据
     */
    public void setResponseData(JSONObject responseData) {
        this.responseData = responseData;
    }

    /**
     * 是否已过期
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }

    /**
     * 获取自定义参数
     */
    public String getCustomParam(String key) {
        try {
            return responseData.getString(key);
        } catch (JSONException e) {
            Logger.e(TAG, "Failed to get custom param: " + key, e);
            return null;
        }
    }
    
    /**
     * 获取额外参数
     */
    public String getExtra(String key) {
        return extras.get(key);
    }
    
    /**
     * 设置额外参数
     */
    public void setExtra(String key, String value) {
        extras.put(key, value);
    }
    
    /**
     * 构建器类
     */
    public static class Builder {
        private AdResponse response;
        
        public Builder() {
            response = new AdResponse();
        }
        
        public Builder setId(String id) {
            response.setId(id);
            return this;
        }
        
        public Builder setAdUnitId(String adUnitId) {
            response.setAdUnitId(adUnitId);
            return this;
        }
        
        public Builder setAdType(AdRequest.AdType adType) {
            response.setAdType(adType);
            return this;
        }
        
        public Builder setPlatform(String platform) {
            response.setPlatform(platform);
            return this;
        }
        
        public Builder setCreativeType(String creativeType) {
            response.setCreativeType(creativeType);
            return this;
        }
        
        public Builder setContent(String content) {
            response.setContent(content);
            return this;
        }
        
        public Builder setRevenue(double revenue) {
            response.setRevenue(revenue);
            return this;
        }
        
        public Builder setEcpm(double ecpm) {
            response.setEcpm(ecpm);
            return this;
        }
        
        public Builder setExpiryTime(long expiryTime) {
            response.setExpiryTime(expiryTime);
            return this;
        }
        
        public Builder setExtra(String key, String value) {
            response.setExtra(key, value);
            return this;
        }
        
        public AdResponse build() {
            return response;
        }
    }
} 