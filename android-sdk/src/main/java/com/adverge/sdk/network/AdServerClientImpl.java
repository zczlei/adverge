package com.adverge.sdk.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.adverge.sdk.model.Platform;
import com.adverge.sdk.utils.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private static final String TAG = "AdServerClientImpl";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    private static AdServerClientImpl instance;
    private final Context context;
    private OkHttpClient client;
    private String baseUrl;
    private final Handler mainHandler;
    private final Gson gson;
    private boolean isInitialized = false;
    
    private AdServerClientImpl(Context context) {
        this.context = context.getApplicationContext();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.gson = new Gson();
    }
    
    public static synchronized AdServerClientImpl getInstance(Context context) {
        if (instance == null) {
            instance = new AdServerClientImpl(context);
        }
        return instance;
    }
    
    @Override
    public void init(Map<String, Object> configs) {
        if (isInitialized) {
            Log.w(TAG, "AdServerClient already initialized");
            return;
        }
        
        // 从配置中获取基础URL
        if (configs.containsKey("baseUrl")) {
            this.baseUrl = (String) configs.get("baseUrl");
        } else {
            this.baseUrl = "https://api.adverge.com"; // 默认服务器地址
        }
        
        // 获取超时设置
        int connectTimeout = 10;
        int readTimeout = 10;
        int writeTimeout = 10;
        
        if (configs.containsKey("connectTimeout")) {
            connectTimeout = ((Number) configs.get("connectTimeout")).intValue();
        }
        if (configs.containsKey("readTimeout")) {
            readTimeout = ((Number) configs.get("readTimeout")).intValue();
        }
        if (configs.containsKey("writeTimeout")) {
            writeTimeout = ((Number) configs.get("writeTimeout")).intValue();
        }
        
        // 创建OkHttpClient
        this.client = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .build();
        
        isInitialized = true;
        Log.i(TAG, "AdServerClient initialized with baseUrl: " + baseUrl);
    }
    
    @Override
    public void requestAd(AdRequest request, final AdCallback callback) {
        ensureInitialized();
        
        String url = baseUrl + "/api/v1/ads/request";
        String json = gson.toJson(request);
        RequestBody body = RequestBody.create(JSON, json);
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        
        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Request ad failed", e);
                notifyError(callback, e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String error = "Request ad failed with code: " + response.code();
                    Log.e(TAG, error);
                    notifyError(callback, error);
                    return;
                }
                
                String responseBody = response.body().string();
                try {
                    AdResponse adResponse = gson.fromJson(responseBody, AdResponse.class);
                    notifySuccess(callback, adResponse);
                } catch (Exception e) {
                    Log.e(TAG, "Parse response failed", e);
                    notifyError(callback, e.getMessage());
                }
            }
        });
    }
    
    @Override
    public void trackImpression(String adUnitId, String platform) {
        ensureInitialized();
        
        String url = baseUrl + "/api/v1/ads/impression";
        String json = gson.toJson(new ImpressionRequest(adUnitId, platform));
        RequestBody body = RequestBody.create(JSON, json);
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        
        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Track impression failed", e);
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Track impression failed with code: " + response.code());
                }
            }
        });
    }
    
    @Override
    public void trackClick(String adUnitId, String platform) {
        ensureInitialized();
        
        String url = baseUrl + "/api/v1/ads/click";
        String json = gson.toJson(new ClickRequest(adUnitId, platform));
        RequestBody body = RequestBody.create(JSON, json);
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        
        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Track click failed", e);
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Track click failed with code: " + response.code());
                }
            }
        });
    }
    
    @Override
    public void trackPerformance(String adId, String event, Map<String, Object> params) {
        ensureInitialized();
        
        String url = baseUrl + "/api/v1/ads/performance";
        String json = gson.toJson(new PerformanceRequest(adId, event, params));
        RequestBody body = RequestBody.create(JSON, json);
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        
        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Track performance failed", e);
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Track performance failed with code: " + response.code());
                }
            }
        });
    }
    
    @Override
    public void getPlatforms(final PlatformCallback callback) {
        ensureInitialized();
        
        String url = baseUrl + "/api/v1/platforms";
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .get()
                .build();
        
        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Get platforms failed", e);
                notifyPlatformError(callback, e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String error = "Get platforms failed with code: " + response.code();
                    Log.e(TAG, error);
                    notifyPlatformError(callback, error);
                    return;
                }
                
                String responseBody = response.body().string();
                try {
                    List<Platform> platforms = gson.fromJson(responseBody, new TypeToken<List<Platform>>(){}.getType());
                    notifyPlatformSuccess(callback, platforms);
                } catch (Exception e) {
                    Log.e(TAG, "Parse platforms failed", e);
                    notifyPlatformError(callback, e.getMessage());
                }
            }
        });
    }

    @Override
    public void savePlatform(Platform platform, final PlatformCallback callback) {
        ensureInitialized();
        
        String url = baseUrl + "/api/v1/platforms";
        String json = gson.toJson(platform);
        RequestBody body = RequestBody.create(JSON, json);
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        
        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Save platform failed", e);
                notifyPlatformError(callback, e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String error = "Save platform failed with code: " + response.code();
                    Log.e(TAG, error);
                    notifyPlatformError(callback, error);
                    return;
                }
                
                String responseBody = response.body().string();
                try {
                    List<Platform> platforms = gson.fromJson(responseBody, new TypeToken<List<Platform>>(){}.getType());
                    notifyPlatformSuccess(callback, platforms);
                } catch (Exception e) {
                    Log.e(TAG, "Parse platforms failed", e);
                    notifyPlatformError(callback, e.getMessage());
                }
            }
        });
    }

    @Override
    public void enablePlatform(String platformName, final PlatformCallback callback) {
        ensureInitialized();
        
        String url = baseUrl + "/api/v1/platforms/" + platformName + "/enable";
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .post(RequestBody.create(null, new byte[0]))
                .build();
        
        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Enable platform failed", e);
                notifyPlatformError(callback, e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String error = "Enable platform failed with code: " + response.code();
                    Log.e(TAG, error);
                    notifyPlatformError(callback, error);
                    return;
                }
                
                String responseBody = response.body().string();
                try {
                    List<Platform> platforms = gson.fromJson(responseBody, new TypeToken<List<Platform>>(){}.getType());
                    notifyPlatformSuccess(callback, platforms);
                } catch (Exception e) {
                    Log.e(TAG, "Parse platforms failed", e);
                    notifyPlatformError(callback, e.getMessage());
                }
            }
        });
    }

    @Override
    public void disablePlatform(String platformName, final PlatformCallback callback) {
        ensureInitialized();
        
        String url = baseUrl + "/api/v1/platforms/" + platformName + "/disable";
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .post(RequestBody.create(null, new byte[0]))
                .build();
        
        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Disable platform failed", e);
                notifyPlatformError(callback, e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String error = "Disable platform failed with code: " + response.code();
                    Log.e(TAG, error);
                    notifyPlatformError(callback, error);
                    return;
                }
                
                String responseBody = response.body().string();
                try {
                    List<Platform> platforms = gson.fromJson(responseBody, new TypeToken<List<Platform>>(){}.getType());
                    notifyPlatformSuccess(callback, platforms);
                } catch (Exception e) {
                    Log.e(TAG, "Parse platforms failed", e);
                    notifyPlatformError(callback, e.getMessage());
                }
            }
        });
    }
    
    @Override
    public void destroy() {
        Log.d(TAG, "Destroying AdServerClient");
        if (client != null) {
            client.dispatcher().cancelAll();
        }
        isInitialized = false;
        instance = null;
    }
    
    private void ensureInitialized() {
        if (!isInitialized) {
            throw new IllegalStateException("AdServerClient is not initialized. Call init() first.");
        }
    }
    
    private void notifySuccess(final AdCallback callback, final AdResponse response) {
        mainHandler.post(() -> callback.onSuccess(response));
    }
    
    private void notifyError(final AdCallback callback, final String error) {
        mainHandler.post(() -> callback.onError(error));
    }

    private void notifyPlatformSuccess(final PlatformCallback callback, final List<Platform> platforms) {
        mainHandler.post(() -> callback.onSuccess(platforms));
    }

    private void notifyPlatformError(final PlatformCallback callback, final String error) {
        mainHandler.post(() -> callback.onError(error));
    }

    private static class ImpressionRequest {
        private String adUnitId;
        private String platform;

        public ImpressionRequest(String adUnitId, String platform) {
            this.adUnitId = adUnitId;
            this.platform = platform;
        }
    }

    private static class ClickRequest {
        private String adUnitId;
        private String platform;

        public ClickRequest(String adUnitId, String platform) {
            this.adUnitId = adUnitId;
            this.platform = platform;
        }
    }
    
    private static class PerformanceRequest {
        private String adId;
        private String event;
        private Map<String, Object> params;
        
        public PerformanceRequest(String adId, String event, Map<String, Object> params) {
            this.adId = adId;
            this.event = event;
            this.params = params != null ? params : Collections.emptyMap();
        }
    }
} 