package com.adverge.sdk.ad;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.adverge.sdk.AdSDK;
import com.adverge.sdk.utils.Logger;
import com.adverge.sdk.utils.SecurityUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.TimeZone;

/**
 * 广告请求类
 */
public class AdRequest {
    private static final String TAG = "AdRequest";
    
    private final String adUnitId;
    private final AdType adType;
    private final JSONObject requestData;
    private final AdSDK sdk;
    private final SecurityUtils securityUtils;

    public AdRequest(String adUnitId, AdType adType) {
        this.adUnitId = adUnitId;
        this.adType = adType;
        this.requestData = new JSONObject();
        this.sdk = AdSDK.getInstance();
        this.securityUtils = sdk.getSecurityUtils();
        
        // 初始化请求数据
        initRequestData();
    }

    private void initRequestData() {
        try {
            Context context = sdk.getContext();
            
            // 基本信息
            requestData.put("ad_unit_id", adUnitId);
            requestData.put("ad_type", adType.name());
            requestData.put("app_id", sdk.getConfig().getAppId());
            
            // 设备信息
            requestData.put("device_id", getDeviceId(context));
            requestData.put("device_model", Build.MODEL);
            requestData.put("device_brand", Build.BRAND);
            requestData.put("device_manufacturer", Build.MANUFACTURER);
            requestData.put("os_version", Build.VERSION.RELEASE);
            requestData.put("sdk_version", Build.VERSION.SDK_INT);
            
            // 网络信息
            requestData.put("network_type", getNetworkType(context));
            requestData.put("carrier", getCarrier(context));
            
            // 位置信息
            requestData.put("language", Locale.getDefault().getLanguage());
            requestData.put("country", Locale.getDefault().getCountry());
            requestData.put("timezone", TimeZone.getDefault().getID());
            
            // 屏幕信息
            requestData.put("screen_width", context.getResources().getDisplayMetrics().widthPixels);
            requestData.put("screen_height", context.getResources().getDisplayMetrics().heightPixels);
            requestData.put("screen_density", context.getResources().getDisplayMetrics().density);
            
        } catch (JSONException e) {
            Logger.e(TAG, "Failed to init request data", e);
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
            requestData.put(key, value);
        } catch (JSONException e) {
            Logger.e(TAG, "Failed to add custom param", e);
        }
    }

    /**
     * 获取请求数据
     */
    public JSONObject getRequestData() {
        return requestData;
    }

    /**
     * 获取加密后的请求数据
     */
    public String getEncryptedRequestData() {
        String data = requestData.toString();
        return securityUtils.encrypt(data, sdk.getConfig().getAppKey());
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
    public AdType getAdType() {
        return adType;
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
} 