package com.adverge.sdk.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.adverge.sdk.network.AdServerClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 广告预加载管理器
 */
public class AdPreloadManager {
    private static final String TAG = "AdPreloadManager";
    private static AdPreloadManager instance;
    
    private final Context context;
    private final AdServerClient adServerClient;
    private final Map<String, AdResponse> cachedAds;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    
    private AdPreloadManager(Context context) {
        this.context = context.getApplicationContext();
        this.adServerClient = AdServerClient.getInstance(context);
        this.cachedAds = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public static synchronized AdPreloadManager getInstance(Context context) {
        if (instance == null) {
            instance = new AdPreloadManager(context);
        }
        return instance;
    }
    
    /**
     * 预加载广告
     * @param adUnitId 广告位ID
     * @param adType 广告类型
     * @param adListener 回调
     */
    public void preloadAd(String adUnitId, String adType, AdListener adListener) {
        if (adUnitId == null || adUnitId.isEmpty()) {
            if (adListener != null) {
                adListener.onAdLoadFailed("Ad unit ID is empty");
            }
            return;
        }
        
        // 检查缓存中是否已有该广告
        if (cachedAds.containsKey(adUnitId)) {
            if (adListener != null) {
                adListener.onAdLoaded(cachedAds.get(adUnitId));
            }
            return;
        }
        
        // 创建请求参数
        AdRequest request = new AdRequest();
        request.setAdUnitId(adUnitId);
        Map<String, String> extras = new HashMap<>();
        extras.put("ad_type", adType);
        request.setExtras(extras);
        
        // 请求广告
        executorService.execute(() -> {
            try {
                adServerClient.requestAd(request, new AdServerClient.AdCallback() {
                    @Override
                    public void onSuccess(AdResponse response) {
                        // 缓存广告
                        cachedAds.put(adUnitId, response);
                        
                        // 回调通知
                        if (adListener != null) {
                            mainHandler.post(() -> adListener.onAdLoaded(response));
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        // 模拟广告响应
                        if (shouldMockResponse()) {
                            AdResponse mockResponse = createMockAdResponse(adUnitId, adType);
                            cachedAds.put(adUnitId, mockResponse);
                            
                            if (adListener != null) {
                                mainHandler.post(() -> adListener.onAdLoaded(mockResponse));
                            }
                            return;
                        }
                        
                        // 回调通知
                        if (adListener != null) {
                            mainHandler.post(() -> adListener.onAdLoadFailed(error));
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error while preloading ad", e);
                if (adListener != null) {
                    mainHandler.post(() -> adListener.onAdLoadFailed(e.getMessage()));
                }
            }
        });
    }
    
    /**
     * 获取预加载的广告
     * @param adUnitId 广告位ID
     * @return 广告响应，如果没有则返回null
     */
    public AdResponse getPreloadedAd(String adUnitId) {
        AdResponse response = cachedAds.get(adUnitId);
        if (response != null) {
            // 移除缓存，避免重用
            cachedAds.remove(adUnitId);
        }
        return response;
    }
    
    /**
     * 清除所有预加载的广告
     */
    public void clearAll() {
        cachedAds.clear();
    }
    
    /**
     * 是否应该模拟响应
     */
    private boolean shouldMockResponse() {
        // 判断是否在开发环境
        return BuildConfig.DEBUG;
    }
    
    /**
     * 创建模拟广告响应
     */
    private AdResponse createMockAdResponse(String adUnitId, String adType) {
        AdResponse.Builder builder = new AdResponse.Builder()
                .setAdUnitId(adUnitId)
                .setPlatform("mock")
                .setEcpm(0.1)
                .setExtra("ad_type", adType);
        
        return builder.build();
    }
    
    public void destroy() {
        executorService.shutdown();
        cachedAds.clear();
        instance = null;
    }
} 