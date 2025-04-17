package com.adverge.sdk.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.adverge.sdk.AdSDK;
import com.adverge.sdk.ad.AdRequest;
import com.adverge.sdk.ad.AdResponse;
import com.adverge.sdk.ad.AdView;
import com.adverge.sdk.listener.AdListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 广告预加载管理器
 */
public class AdPreloadManager {
    private static final String TAG = "AdPreloadManager";
    private static final int MAX_PRELOAD_COUNT = 3;
    private static final long PRELOAD_INTERVAL = 30 * 1000; // 30秒
    
    private static AdPreloadManager instance;
    private final Context context;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private final Map<String, AdResponse> preloadedAds;
    private final Map<String, Long> preloadTimestamps;
    private final Map<String, AdListener> adListeners;
    
    private AdPreloadManager(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newFixedThreadPool(MAX_PRELOAD_COUNT);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.preloadedAds = new HashMap<>();
        this.preloadTimestamps = new HashMap<>();
        this.adListeners = new HashMap<>();
    }
    
    public static synchronized AdPreloadManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AdPreloadManager must be initialized first");
        }
        return instance;
    }
    
    public static synchronized void initialize(Context context) {
        if (instance == null) {
            instance = new AdPreloadManager(context);
        }
    }
    
    /**
     * 预加载广告
     * @param adUnitId 广告单元ID
     * @param adType 广告类型
     * @param listener 广告监听器
     */
    public void preloadAd(String adUnitId, String adType, AdListener listener) {
        if (adUnitId == null || adUnitId.isEmpty()) {
            Logger.e(TAG, "Invalid ad unit ID");
            return;
        }
        
        // 如果已经预加载，直接返回
        if (preloadedAds.containsKey(adUnitId)) {
            Logger.d(TAG, "Ad already preloaded: " + adUnitId);
            if (listener != null) {
                listener.onAdLoaded(preloadedAds.get(adUnitId));
            }
            return;
        }
        
        // 保存监听器
        adListeners.put(adUnitId, listener);
        
        // 异步预加载广告
        executorService.execute(() -> {
            try {
                AdRequest request = new AdRequest.Builder()
                        .setAdUnitId(adUnitId)
                        .setAdType(adType)
                        .build();
                
                // 模拟广告请求
                AdResponse response = simulateAdRequest(request);
                
                // 缓存广告响应
                preloadedAds.put(adUnitId, response);
                
                // 通知监听器
                mainHandler.post(() -> {
                    AdListener adListener = adListeners.get(adUnitId);
                    if (adListener != null) {
                        adListener.onAdLoaded(response);
                    }
                });
                
            } catch (Exception e) {
                Logger.e(TAG, "Failed to preload ad: " + adUnitId, e);
                mainHandler.post(() -> {
                    AdListener adListener = adListeners.get(adUnitId);
                    if (adListener != null) {
                        adListener.onAdFailedToLoad(e.getMessage());
                    }
                });
            }
        });
    }
    
    /**
     * 获取预加载的广告
     * @param adUnitId 广告单元ID
     * @return 广告响应，如果没有预加载则返回null
     */
    public AdResponse getPreloadedAd(String adUnitId) {
        return preloadedAds.get(adUnitId);
    }
    
    /**
     * 移除预加载的广告
     * @param adUnitId 广告单元ID
     */
    public void removePreloadedAd(String adUnitId) {
        preloadedAds.remove(adUnitId);
        adListeners.remove(adUnitId);
    }
    
    /**
     * 清理所有预加载的广告
     */
    public void clearPreloadedAds() {
        preloadedAds.clear();
        adListeners.clear();
    }
    
    /**
     * 模拟广告请求
     */
    private AdResponse simulateAdRequest(AdRequest request) {
        // TODO: 实现实际的广告请求逻辑
        // 这里使用模拟数据
        return new AdResponse.Builder()
                .setAdUnitId(request.getAdUnitId())
                .setAdType(request.getAdType())
                .setAdContent("Mock ad content")
                .build();
    }
    
    /**
     * 销毁资源
     */
    public void destroy() {
        executorService.shutdown();
        clearPreloadedAds();
        instance = null;
    }
} 