package com.adverge.sdk.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.adverge.sdk.AdSDK;
import com.adverge.sdk.config.Config;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.adverge.sdk.model.Platform;
import com.adverge.sdk.utils.JsonUtils;
import com.adverge.sdk.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 广告服务器客户端实现类
 */
public class AdServerClientImpl implements AdServerClient {
    private static final String TAG = "AdServerClient";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    private static AdServerClientImpl instance;
    private final Context context;
    private final OkHttpClient client;
    private final String baseUrl;
    private final Config config;
    private final Handler mainHandler;
    
    private AdServerClientImpl(Context context) {
        this.context = context.getApplicationContext();
        this.config = AdSDK.getInstance().getConfig();
        this.baseUrl = config.getServerUrl();
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        // 创建OkHttpClient
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    public static synchronized AdServerClientImpl getInstance(Context context) {
        if (instance == null) {
            instance = new AdServerClientImpl(context);
        }
        return instance;
    }
    
    @Override
    public void requestAd(AdRequest request, final AdServerCallback callback) {
        try {
            // 构建请求URL
            String url = baseUrl + "/v1/ad";
            
            // 构建请求体
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("adUnitId", request.getAdUnitId());
            jsonBody.put("appId", config.getAppId());
            
            // 添加设备信息
            if (request.getDeviceInfo() != null) {
                JSONObject deviceInfo = new JSONObject();
                deviceInfo.put("type", request.getDeviceInfo().getType());
                deviceInfo.put("os", request.getDeviceInfo().getOs());
                deviceInfo.put("model", request.getDeviceInfo().getModel());
                deviceInfo.put("osVersion", request.getDeviceInfo().getOsVersion());
                jsonBody.put("deviceInfo", deviceInfo);
            }
            
            // 添加用户信息
            if (request.getUserInfo() != null) {
                JSONObject userInfo = new JSONObject();
                userInfo.put("id", request.getUserInfo().getId());
                userInfo.put("age", request.getUserInfo().getAge());
                userInfo.put("gender", request.getUserInfo().getGender());
                jsonBody.put("userInfo", userInfo);
            }
            
            // 构建请求
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request httpRequest = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-API-Key", config.getApiKey())
                    .post(body)
                    .build();
            
            // 发送请求
            client.newCall(httpRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    notifyError(callback, "网络请求失败: " + e.getMessage());
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            notifyError(callback, "服务器响应错误: " + response.code());
                            return;
                        }
                        
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        
                        // 解析广告响应
                        AdResponse adResponse = new AdResponse();
                        adResponse.setAdUnitId(jsonResponse.optString("adUnitId"));
                        adResponse.setPlatform(jsonResponse.optString("platform"));
                        adResponse.setAdId(jsonResponse.optString("adId"));
                        adResponse.setAdContent(jsonResponse.optString("adContent"));
                        adResponse.setPrice(jsonResponse.optDouble("price"));
                        adResponse.setCurrency(jsonResponse.optString("currency", "USD"));
                        adResponse.setExpiry(jsonResponse.optLong("expiry"));
                        adResponse.setBidToken(jsonResponse.optString("bidToken"));
                        adResponse.setPlatformParams(jsonResponse.optString("platformParams"));
                        
                        // 通知回调
                        notifySuccess(callback, adResponse);
                    } catch (Exception e) {
                        notifyError(callback, "解析响应失败: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            notifyError(callback, "构建请求失败: " + e.getMessage());
        }
    }
    
    @Override
    public void trackImpression(String adUnitId, String platform) {
        try {
            // 构建请求URL
            String url = baseUrl + "/v1/track/impression?adUnitId=" + adUnitId + "&platform=" + platform;
            
            // 构建请求
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("X-API-Key", config.getApiKey())
                    .post(RequestBody.create("", null))
                    .build();
            
            // 发送请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Logger.e(TAG, "记录展示失败: " + e.getMessage());
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Logger.e(TAG, "记录展示服务器响应错误: " + response.code());
                    }
                }
            });
        } catch (Exception e) {
            Logger.e(TAG, "记录展示异常: " + e.getMessage());
        }
    }
    
    @Override
    public void trackClick(String adUnitId, String platform) {
        try {
            // 构建请求URL
            String url = baseUrl + "/v1/track/click?adUnitId=" + adUnitId + "&platform=" + platform;
            
            // 构建请求
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("X-API-Key", config.getApiKey())
                    .post(RequestBody.create("", null))
                    .build();
            
            // 发送请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Logger.e(TAG, "记录点击失败: " + e.getMessage());
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Logger.e(TAG, "记录点击服务器响应错误: " + response.code());
                    }
                }
            });
        } catch (Exception e) {
            Logger.e(TAG, "记录点击异常: " + e.getMessage());
        }
    }
    
    @Override
    public void getPlatforms(final PlatformCallback callback) {
        try {
            // 构建请求URL
            String url = baseUrl + "/v1/platforms";
            
            // 构建请求
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("X-API-Key", config.getApiKey())
                    .get()
                    .build();
            
            // 发送请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    notifyPlatformError(callback, "获取平台列表失败: " + e.getMessage());
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            notifyPlatformError(callback, "服务器响应错误: " + response.code());
                            return;
                        }
                        
                        String responseBody = response.body().string();
                        List<Platform> platforms = JsonUtils.fromJsonArray(responseBody, Platform.class);
                        
                        // 通知回调
                        notifyPlatformSuccess(callback, platforms);
                    } catch (Exception e) {
                        notifyPlatformError(callback, "解析响应失败: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            notifyPlatformError(callback, "构建请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 在主线程上通知成功回调
     */
    private void notifySuccess(final AdServerCallback callback, final AdResponse response) {
        if (callback != null) {
            mainHandler.post(() -> callback.onSuccess(response));
        }
    }
    
    /**
     * 在主线程上通知错误回调
     */
    private void notifyError(final AdServerCallback callback, final String error) {
        if (callback != null) {
            mainHandler.post(() -> callback.onError(error));
        }
    }
    
    /**
     * 在主线程上通知平台成功回调
     */
    private void notifyPlatformSuccess(final PlatformCallback callback, final List<Platform> platforms) {
        if (callback != null) {
            mainHandler.post(() -> callback.onSuccess(platforms));
        }
    }
    
    /**
     * 在主线程上通知平台错误回调
     */
    private void notifyPlatformError(final PlatformCallback callback, final String error) {
        if (callback != null) {
            mainHandler.post(() -> callback.onError(error));
        }
    }
} 