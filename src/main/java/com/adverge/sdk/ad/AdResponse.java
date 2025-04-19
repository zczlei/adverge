package com.adverge.sdk.ad;

import com.adverge.sdk.AdSDK;
import com.adverge.sdk.utils.Logger;
import com.adverge.sdk.utils.SecurityUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 广告响应类
 */
public class AdResponse {
    private static final String TAG = "AdResponse";
    
    private final String id;
    private final String adUnitId;
    private final AdRequest.AdType adType;
    private final String platform;
    private final String creativeType;
    private final String content;
    private final double revenue;
    private final long expiryTime;
    private final JSONObject responseData;
    private final AdSDK sdk;
    private final SecurityUtils securityUtils;

    public AdResponse(String encryptedData) {
        this.sdk = AdSDK.getInstance();
        this.securityUtils = sdk.getSecurityUtils();
        this.responseData = parseResponse(encryptedData);
        
        // 解析基本字段
        this.id = getString("id");
        this.adUnitId = getString("ad_unit_id");
        this.adType = AdRequest.AdType.valueOf(getString("ad_type"));
        this.platform = getString("platform");
        this.creativeType = getString("creative_type");
        this.content = getString("content");
        this.revenue = getDouble("revenue");
        this.expiryTime = getLong("expiry_time");
    }

    private JSONObject parseResponse(String encryptedData) {
        try {
            String decryptedData = securityUtils.decrypt(encryptedData, sdk.getConfig().getAppKey());
            return new JSONObject(decryptedData);
        } catch (Exception e) {
            Logger.e(TAG, "Failed to parse response", e);
            return new JSONObject();
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
     * 获取广告单元ID
     */
    public String getAdUnitId() {
        return adUnitId;
    }

    /**
     * 获取广告类型
     */
    public AdRequest.AdType getAdType() {
        return adType;
    }

    /**
     * 获取广告平台
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * 获取创意类型
     */
    public String getCreativeType() {
        return creativeType;
    }

    /**
     * 获取广告内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 获取广告收益
     */
    public double getRevenue() {
        return revenue;
    }

    /**
     * 获取过期时间
     */
    public long getExpiryTime() {
        return expiryTime;
    }

    /**
     * 获取响应数据
     */
    public JSONObject getResponseData() {
        return responseData;
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
} 