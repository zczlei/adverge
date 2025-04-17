package com.adverge.sdk.network;

import android.content.Context;
import android.util.Log;

import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.BidResponse;
import com.adverge.sdk.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AdServerClient {
    private static final String TAG = "AdServerClient";
    private static AdServerClient instance;
    
    private Context context;
    private String serverUrl;
    private Map<String, String> headers;
    
    private AdServerClient(Context context) {
        this.context = context;
        this.headers = new HashMap<>();
        // 设置默认请求头
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
    }
    
    public static AdServerClient getInstance(Context context) {
        if (instance == null) {
            synchronized (AdServerClient.class) {
                if (instance == null) {
                    instance = new AdServerClient(context);
                }
            }
        }
        return instance;
    }
    
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    public void addHeader(String key, String value) {
        headers.put(key, value);
    }
    
    public void requestBid(AdRequest request, AdServerCallback callback) {
        Logger.d(TAG, "发起竞价请求: " + request.getAdUnitId());
        
        // 构建竞价请求
        JSONObject bidRequest = new JSONObject();
        try {
            bidRequest.put("adUnitId", request.getAdUnitId());
            bidRequest.put("adType", request.getAdType());
            bidRequest.put("deviceInfo", getDeviceInfo());
            bidRequest.put("userInfo", getUserInfo());
            
            // 添加广告尺寸信息
            if (request.getWidth() > 0 && request.getHeight() > 0) {
                JSONObject size = new JSONObject();
                size.put("width", request.getWidth());
                size.put("height", request.getHeight());
                bidRequest.put("adSize", size);
            }
            
            // 添加测试模式标记
            if (request.isTestMode()) {
                bidRequest.put("testMode", true);
            }
        } catch (JSONException e) {
            Logger.e(TAG, "构建竞价请求失败", e);
            callback.onError("构建竞价请求失败");
            return;
        }

        // 发送竞价请求到后端
        sendRequest("/api/bid", bidRequest, new ResponseCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    BidResponse bidResponse = parseBidResponse(response);
                    Logger.d(TAG, "收到竞价响应: " + bidResponse.getPlatform());
                    callback.onBidResponse(bidResponse);
                } catch (JSONException e) {
                    Logger.e(TAG, "解析竞价响应失败", e);
                    callback.onError("解析竞价响应失败");
                }
            }

            @Override
            public void onError(String error) {
                Logger.e(TAG, "竞价请求失败: " + error);
                callback.onError(error);
            }
        });
    }
    
    private JSONObject getDeviceInfo() throws JSONException {
        JSONObject deviceInfo = new JSONObject();
        // 添加设备信息
        deviceInfo.put("deviceId", getDeviceId());
        deviceInfo.put("deviceModel", android.os.Build.MODEL);
        deviceInfo.put("deviceBrand", android.os.Build.BRAND);
        deviceInfo.put("osVersion", android.os.Build.VERSION.RELEASE);
        deviceInfo.put("sdkVersion", android.os.Build.VERSION.SDK_INT);
        return deviceInfo;
    }
    
    private JSONObject getUserInfo() throws JSONException {
        JSONObject userInfo = new JSONObject();
        // 添加用户信息
        userInfo.put("language", java.util.Locale.getDefault().getLanguage());
        userInfo.put("country", java.util.Locale.getDefault().getCountry());
        userInfo.put("timezone", java.util.TimeZone.getDefault().getID());
        return userInfo;
    }
    
    private String getDeviceId() {
        // 实现获取设备ID的逻辑
        return android.provider.Settings.Secure.getString(
            context.getContentResolver(),
            android.provider.Settings.Secure.ANDROID_ID
        );
    }
    
    private void sendRequest(String endpoint, JSONObject data, ResponseCallback callback) {
        // 实现实际的网络请求逻辑
        // 这里使用OkHttp或其他HTTP客户端发送请求
        // 实际实现中需要替换为真实的网络请求代码
        try {
            // 模拟网络请求
            Thread.sleep(100);
            
            // 模拟服务器响应
            JSONObject response = new JSONObject();
            response.put("platform", "admob");
            response.put("price", 5.0);
            response.put("currency", "USD");
            response.put("creative", "ad_content");
            
            callback.onSuccess(response);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
    
    private BidResponse parseBidResponse(JSONObject response) throws JSONException {
        return new BidResponse(
            response.getString("platform"),
            response.getDouble("price"),
            response.getString("currency"),
            response.getString("creative"),
            System.currentTimeMillis() + 300000 // 5分钟后过期
        );
    }
    
    public interface ResponseCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }
    
    public void requestAd(AdRequest request, AdServerCallback callback) {
        Logger.d(TAG, "向Adverge服务器发起广告请求: " + request.getAdUnitId());
        
        // 构建请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("adUnitId", request.getAdUnitId());
        params.put("adType", request.getAdType());
        params.put("width", request.getWidth());
        params.put("height", request.getHeight());
        params.put("testMode", request.isTestMode());
        
        // 发送请求到后端服务器
        // 这里使用OkHttp或其他HTTP客户端发送请求
        // 实际实现中需要替换为真实的网络请求代码
        try {
            // 模拟网络请求
            Thread.sleep(100);
            
            // 模拟服务器响应
            BidResponse response = new BidResponse(
                "bid_" + System.currentTimeMillis(),
                request.getAdUnitId(),
                request.getAdType()
            );
            response.setECPM(5.0);
            response.setCurrency("USD");
            response.setAdContent("ad_content");
            response.setExpiryTime(System.currentTimeMillis() + 300000); // 5分钟后过期
            
            // 通知回调
            callback.onBidResponse(response);
            
        } catch (Exception e) {
            Logger.e(TAG, "广告请求失败: " + e.getMessage());
            callback.onError(e.getMessage());
        }
    }
    
    public void trackImpression(String adUnitId, String platform) {
        Logger.d(TAG, "发送广告展示追踪: " + adUnitId);
        // 实现广告展示追踪
    }
    
    public void trackClick(String adUnitId, String platform) {
        Logger.d(TAG, "发送广告点击追踪: " + adUnitId);
        // 实现广告点击追踪
    }
} 