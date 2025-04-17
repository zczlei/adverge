package com.adverge.sdk.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.adverge.sdk.AdSDK;
import com.adverge.sdk.ad.AdView;
import com.adverge.sdk.listener.AdListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 广告生命周期监控器
 */
public class AdLifecycleMonitor {
    private static final String TAG = "AdLifecycleMonitor";
    private static final long MONITOR_INTERVAL = 30 * 1000; // 30秒
    private static final long AD_EXPIRY_TIME = 30 * 60 * 1000; // 30分钟
    
    private static AdLifecycleMonitor instance;
    private final Context context;
    private final Handler monitorHandler;
    private final Map<String, AdView> activeAds;
    private final Map<String, Long> impressionTimestamps;
    private final Map<String, Integer> impressionCounts;
    private final Map<String, Integer> clickCounts;
    private final Map<String, AdListener> adListeners;
    
    private AdLifecycleMonitor(Context context) {
        this.context = context.getApplicationContext();
        this.monitorHandler = new Handler(Looper.getMainLooper());
        this.activeAds = new HashMap<>();
        this.impressionTimestamps = new HashMap<>();
        this.impressionCounts = new HashMap<>();
        this.clickCounts = new HashMap<>();
        this.adListeners = new HashMap<>();
        
        startMonitoring();
    }
    
    public static AdLifecycleMonitor getInstance(Context context) {
        if (instance == null) {
            synchronized (AdLifecycleMonitor.class) {
                if (instance == null) {
                    instance = new AdLifecycleMonitor(context);
                }
            }
        }
        return instance;
    }
    
    /**
     * 注册广告视图
     * @param adView 广告视图
     * @param listener 广告监听器
     */
    public void registerAdView(AdView adView, AdListener listener) {
        String adUnitId = adView.getAdUnitId();
        activeAds.put(adUnitId, adView);
        adListeners.put(adUnitId, listener);
        
        // 设置广告监听器
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded(AdResponse response) {
                if (listener != null) {
                    listener.onAdLoaded(response);
                }
            }
            
            @Override
            public void onAdFailedToLoad(String error) {
                if (listener != null) {
                    listener.onAdFailedToLoad(error);
                }
            }
            
            @Override
            public void onAdShown() {
                impressionTimestamps.put(adUnitId, System.currentTimeMillis());
                impressionCounts.put(adUnitId, impressionCounts.getOrDefault(adUnitId, 0) + 1);
                if (listener != null) {
                    listener.onAdShown();
                }
            }
            
            @Override
            public void onAdClicked() {
                clickCounts.put(adUnitId, clickCounts.getOrDefault(adUnitId, 0) + 1);
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
            public void onAdRewarded() {
                if (listener != null) {
                    listener.onAdRewarded();
                }
            }
        });
    }
    
    /**
     * 注销广告视图
     * @param adUnitId 广告单元ID
     */
    public void unregisterAdView(String adUnitId) {
        activeAds.remove(adUnitId);
        impressionTimestamps.remove(adUnitId);
        impressionCounts.remove(adUnitId);
        clickCounts.remove(adUnitId);
        adListeners.remove(adUnitId);
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
        int clicks = getClickCount(adUnitId);
        return impressions > 0 ? (float) clicks / impressions * 100 : 0;
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
                unregisterAdView(adUnitId);
            }
        }
    }
    
    /**
     * 销毁监控器
     */
    public void destroy() {
        monitorHandler.removeCallbacksAndMessages(null);
        activeAds.clear();
        impressionTimestamps.clear();
        impressionCounts.clear();
        clickCounts.clear();
        adListeners.clear();
        instance = null;
    }
} 