package com.adverge.sdk.model;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.adverge.sdk.AdSDK;
import com.adverge.sdk.utils.Logger;
import com.adverge.sdk.utils.SecurityUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * 广告请求类
 */
public class AdRequest {
    private static final String TAG = "AdRequest";
    
    private String adUnitId;
    private AdType adType;
    private JSONObject requestData;
    private AdSDK sdk;
    private SecurityUtils securityUtils;
    private Map<String, String> extras;
    
    // 添加默认构造函数
    public AdRequest() {
        this.requestData = new JSONObject();
        this.extras = new HashMap<>();
        this.sdk = AdSDK.getInstance();
        
        try {
            this.securityUtils = sdk.getSecurityUtils();
        } catch (Exception e) {
            Logger.e(TAG, "Failed to get security utils", e);
        }
    }
    
    // 添加单参数构造函数
    public AdRequest(String adUnitId) {
        this();
        this.adUnitId = adUnitId;
        this.adType = AdType.BANNER; // 默认类型
        
        // 初始化请求数据
        try {
            this.requestData.put("ad_unit_id", adUnitId);
            this.requestData.put("ad_type", adType.name());
        } catch (JSONException e) {
            Logger.e(TAG, "Failed to init request data", e);
        }
    }

    public AdRequest(String adUnitId, AdType adType) {
        this(adUnitId);
        this.adType = adType;
        
        try {
            this.requestData.put("ad_type", adType.name());
        } catch (JSONException e) {
            Logger.e(TAG, "Failed to set ad type", e);
        }
        
        // 初始化请求数据
        initRequestData();
    }

    private void initRequestData() {
        try {
            Context context = sdk.getContext();
            
            // 基本信息
            this.requestData.put("ad_unit_id", adUnitId);
            this.requestData.put("ad_type", adType.name());
            this.requestData.put("app_id", sdk.getConfig().getAppId());
            
            // 设备信息
            this.requestData.put("device_id", getDeviceId(context));
            this.requestData.put("device_model", Build.MODEL);
            this.requestData.put("device_brand", Build.BRAND);
            this.requestData.put("device_manufacturer", Build.MANUFACTURER);
            this.requestData.put("os_version", Build.VERSION.RELEASE);
            this.requestData.put("sdk_version", Build.VERSION.SDK_INT);
            
            // 网络信息
            this.requestData.put("network_type", getNetworkType(context));
            this.requestData.put("carrier", getCarrier(context));
            
            // 位置信息
            this.requestData.put("language", Locale.getDefault().getLanguage());
            this.requestData.put("country", Locale.getDefault().getCountry());
            this.requestData.put("timezone", TimeZone.getDefault().getID());
            
            // 屏幕信息
            this.requestData.put("screen_width", context.getResources().getDisplayMetrics().widthPixels);
            this.requestData.put("screen_height", context.getResources().getDisplayMetrics().heightPixels);
            this.requestData.put("screen_density", context.getResources().getDisplayMetrics().density);
            
        } catch (JSONException e) {
            Logger.e(TAG, "Failed to init request data", e);
        } catch (Exception e) {
            Logger.e(TAG, "Failed to get context or config", e);
        }
    }

    private String getDeviceId(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String deviceId = tm.getDeviceId();
            if (TextUtils.isEmpty(deviceId)) {
                deviceId = android.provider.Settings.Secure.getString(
                    context.getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID
                );
            }
            return deviceId;
        } catch (Exception e) {
            Logger.e(TAG, "Failed to get device ID", e);
            return "";
        }
    }

    private String getNetworkType(Context context) {
        try {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) 
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
            android.net.NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                return info.getTypeName();
            }
        } catch (Exception e) {
            Logger.e(TAG, "Failed to get network type", e);
        }
        return "unknown";
    }

    private String getCarrier(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getNetworkOperatorName();
        } catch (Exception e) {
            Logger.e(TAG, "Failed to get carrier", e);
            return "";
        }
    }

    /**
     * 添加自定义参数
     */
    public void addCustomParam(String key, Object value) {
        try {
            this.requestData.put(key, value);
        } catch (JSONException e) {
            Logger.e(TAG, "Failed to add custom param", e);
        }
    }

    /**
     * 获取请求数据
     */
    public JSONObject getRequestData() {
        return this.requestData;
    }

    /**
     * 获取加密后的请求数据
     */
    public String getEncryptedRequestData() {
        String data = this.requestData.toString();
        try {
            return this.securityUtils.encrypt(data, this.sdk.getConfig().getAppKey());
        } catch (Exception e) {
            Logger.e(TAG, "Failed to encrypt request data", e);
            return data;
        }
    }

    /**
     * 获取广告单元ID
     */
    public String getAdUnitId() {
        return this.adUnitId;
    }
    
    /**
     * 设置广告单元ID
     */
    public void setAdUnitId(String adUnitId) {
        this.adUnitId = adUnitId;
        try {
            this.requestData.put("ad_unit_id", adUnitId);
        } catch (JSONException e) {
            Logger.e(TAG, "Failed to set ad unit id", e);
        }
    }
    
    /**
     * 获取广告ID (别名方法，兼容现有代码)
     */
    public String getAdId() {
        return this.adUnitId;
    }
    
    /**
     * 设置广告ID (别名方法，兼容现有代码)
     */
    public void setAdId(String adId) {
        setAdUnitId(adId);
    }

    /**
     * 获取广告类型
     */
    public AdType getAdType() {
        return this.adType;
    }
    
    /**
     * 设置广告类型
     */
    public void setAdType(AdType adType) {
        this.adType = adType;
        try {
            this.requestData.put("ad_type", adType.name());
        } catch (JSONException e) {
            Logger.e(TAG, "Failed to set ad type", e);
        }
    }
    
    /**
     * 设置额外参数
     * @param extras 额外参数
     */
    public void setExtras(Map<String, String> extras) {
        if (this.extras == null) {
            this.extras = new HashMap<>();
        }
        if (extras != null) {
            this.extras.putAll(extras);
        }
    }

    /**
     * 设置特定额外参数
     * @param key 键
     * @param value 值
     */
    public void setExtra(String key, String value) {
        if (this.extras == null) {
            this.extras = new HashMap<>();
        }
        this.extras.put(key, value);
    }

    /**
     * 获取额外参数
     * @return 额外参数
     */
    public Map<String, String> getExtras() {
        if (this.extras == null) {
            this.extras = new HashMap<>();
        }
        return this.extras;
    }

    /**
     * 获取特定额外参数
     * @param key 键
     * @return 值
     */
    public String getExtra(String key) {
        if (this.extras == null) {
            return null;
        }
        return this.extras.get(key);
    }

    /**
     * 广告类型枚举
     */
    public enum AdType {
        BANNER,
        INTERSTITIAL,
        REWARDED,
        NATIVE
    }
    
    /**
     * 构建器类
     */
    public static class Builder {
        private AdRequest request;
        
        public Builder() {
            request = new AdRequest();
        }
        
        public Builder setAdUnitId(String adUnitId) {
            request.setAdUnitId(adUnitId);
            return this;
        }
        
        public Builder setAdType(AdType adType) {
            request.setAdType(adType);
            return this;
        }
        
        public Builder setExtra(String key, String value) {
            request.setExtra(key, value);
            return this;
        }
        
        public Builder addCustomParam(String key, Object value) {
            request.addCustomParam(key, value);
            return this;
        }
        
        public AdRequest build() {
            return request;
        }
    }
} 