package com.adverge.sdk.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.adverge.sdk.AdSDK;
import com.adverge.sdk.view.AdView;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 广告生命周期监控器
 * 用于监控和管理广告的整个生命周期
 */
public class AdLifecycleMonitor {
    private static final String TAG = "AdLifecycleMonitor";
    private static final long MONITOR_INTERVAL = 30 * 1000; // 30秒
    private static final long AD_EXPIRY_TIME = 30 * 60 * 1000; // 30分钟
    
    private static Map<Context, AdLifecycleMonitor> instances = new HashMap<>();
    
    private WeakHashMap<AdView, AdListener> registeredAdViews = new WeakHashMap<>();
    private Context context;
    private final Handler monitorHandler;
    private final Map<String, Long> impressionTimestamps;
    private final Map<String, Integer> impressionCounts;
    private final Map<String, Integer> clickCounts;
    
    private AdLifecycleMonitor(Context context) {
        this.context = context.getApplicationContext();
        this.monitorHandler = new Handler(Looper.getMainLooper());
        this.impressionTimestamps = new HashMap<>();
        this.impressionCounts = new HashMap<>();
        this.clickCounts = new HashMap<>();
        
        startMonitoring();
    }
    
    /**
     * 获取实例
     */
    public static synchronized AdLifecycleMonitor getInstance(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        
        Context appContext = context.getApplicationContext();
        AdLifecycleMonitor instance = instances.get(appContext);
        
        if (instance == null) {
            instance = new AdLifecycleMonitor(appContext);
            instances.put(appContext, instance);
        }
        
        return instance;
    }
    
    /**
     * 注册广告视图
     */
    public void registerAdView(AdView adView, AdListener listener) {
        if (adView == null) {
            Log.e(TAG, "Cannot register null AdView");
            return;
        }
        
        registeredAdViews.put(adView, listener);
        Log.d(TAG, "Registered AdView: " + adView);
        
        // 设置广告监听器
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (listener != null) {
                    listener.onAdLoaded();
                }
            }
            
            @Override
            public void onAdLoadFailed(String error) {
                if (listener != null) {
                    listener.onAdLoadFailed(error);
                }
            }
            
            @Override
            public void onAdShown() {
                impressionTimestamps.put(adView.getAdUnitId(), System.currentTimeMillis());
                impressionCounts.put(adView.getAdUnitId(), impressionCounts.getOrDefault(adView.getAdUnitId(), 0) + 1);
                if (listener != null) {
                    listener.onAdShown();
                }
            }
            
            @Override
            public void onAdClicked() {
                clickCounts.put(adView.getAdUnitId(), clickCounts.getOrDefault(adView.getAdUnitId(), 0) + 1);
                if (listener != null) {
                    listener.onAdClicked();
                }
            }
            
            @Override
            public void onAdClosed() {
                if (listener != null) {
                    listener.onAdClosed();
                }
            }
            
            @Override
            public void onRewarded(String type, int amount) {
                if (listener != null) {
                    listener.onRewarded(type, amount);
                }
            }
        });
    }
    
    /**
     * 注销广告视图
     */
    public void unregisterAdView(AdView adView) {
        if (adView == null) {
            Log.e(TAG, "Cannot unregister null AdView");
            return;
        }
        
        if (registeredAdViews.containsKey(adView)) {
            registeredAdViews.remove(adView);
            Log.d(TAG, "Unregistered AdView: " + adView);
        }
    }
    
    /**
     * 检查视图是否已注册
     */
    public boolean isRegistered(AdView adView) {
        return registeredAdViews.containsKey(adView);
    }
    
    /**
     * 获取广告视图对应的监听器
     */
    public AdListener getListenerForAdView(AdView adView) {
        return registeredAdViews.get(adView);
    }
    
    /**
     * 获取广告展示次数
     * @param adUnitId 广告单元ID
     * @return 展示次数
     */
    public int getImpressionCount(String adUnitId) {
        return impressionCounts.getOrDefault(adUnitId, 0);
    }
    
    /**
     * 获取广告点击次数
     * @param adUnitId 广告单元ID
     * @return 点击次数
     */
    public int getClickCount(String adUnitId) {
        return clickCounts.getOrDefault(adUnitId, 0);
    }
    
    /**
     * 获取广告点击率
     * @param adUnitId 广告单元ID
     * @return 点击率（百分比）
     */
    public float getClickThroughRate(String adUnitId) {
        int impressions = getImpressionCount(adUnitId);
        if (impressions == 0) {
            return 0;
        }
        return (float) getClickCount(adUnitId) / impressions;
    }
    
    /**
     * 获取广告平均展示时长
     * @param adUnitId 广告单元ID
     * @return 平均展示时长（毫秒）
     */
    public long getAverageImpressionDuration(String adUnitId) {
        Long timestamp = impressionTimestamps.get(adUnitId);
        if (timestamp == null) {
            return 0;
        }
        return System.currentTimeMillis() - timestamp;
    }
    
    /**
     * 启动监控
     */
    private void startMonitoring() {
        monitorHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAdExpiry();
                monitorHandler.postDelayed(this, MONITOR_INTERVAL);
            }
        }, MONITOR_INTERVAL);
    }
    
    /**
     * 检查广告过期
     */
    private void checkAdExpiry() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : impressionTimestamps.entrySet()) {
            String adUnitId = entry.getKey();
            long timestamp = entry.getValue();
            
            if (currentTime - timestamp > AD_EXPIRY_TIME) {
                Logger.d(TAG, "Ad expired: " + adUnitId);
                unregisterAdView(null);
            }
        }
    }
    
    /**
     * 恢复所有广告
     */
    public void resumeAll() {
        for (Map.Entry<AdView, AdListener> entry : registeredAdViews.entrySet()) {
            AdView adView = entry.getKey();
            // 根据需要执行恢复操作
            Log.d(TAG, "Resuming AdView: " + adView);
        }
    }
    
    /**
     * 暂停所有广告
     */
    public void pauseAll() {
        for (Map.Entry<AdView, AdListener> entry : registeredAdViews.entrySet()) {
            AdView adView = entry.getKey();
            // 根据需要执行暂停操作
            Log.d(TAG, "Pausing AdView: " + adView);
        }
    }
    
    /**
     * 销毁所有广告
     */
    public void destroyAll() {
        for (Map.Entry<AdView, AdListener> entry : registeredAdViews.entrySet()) {
            AdView adView = entry.getKey();
            // 根据需要执行销毁操作
            adView.destroy();
            Log.d(TAG, "Destroying AdView: " + adView);
        }
        registeredAdViews.clear();
    }

    /**
     * 通知广告状态改变
     */
    public void notifyOnAdStateChanged(AdView adView, String event) {
        AdListener adListener = getListenerForAdView(adView);
        if (adListener != null) {
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    adListener.onAdLoaded();
                }
                
                @Override
                public void onAdLoaded(AdResponse response) {
                    adListener.onAdLoaded(response);
                }
                
                @Override
                public void onAdLoadFailed(String error) {
                    adListener.onAdLoadFailed(error);
                }
                
                @Override
                public void onAdShown() {
                    adListener.onAdShown();
                }
                
                @Override
                public void onAdClicked() {
                    adListener.onAdClicked();
                }
                
                @Override
                public void onAdClosed() {
                    adListener.onAdClosed();
                }
                
                @Override
                public void onRewarded(String type, int amount) {
                    adListener.onRewarded(type, amount);
                }
            });
        }
    }
} 