package com.adverge.sdk.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.adverge.sdk.platform.AdPlatformAdapter;
import com.adverge.sdk.view.AdView;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsLoadOptions;
import com.unity3d.ads.UnityAdsShowOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Unity Ads广告平台适配器
 */
public class UnityAdapter implements AdPlatformAdapter {
    private static final String TAG = "UnityAdapter";
    private static final String PLATFORM_NAME = "unity";
    private Context context;
    private String gameId;
    private boolean testMode = false;
    private double historicalEcpm = 0.0;
    private AdPlatformAdapter.AdCallback currentCallback;

    public UnityAdapter(Context context) {
        this.context = context;
    }

    @Override
    public void init(Context context, Object config) {
        this.context = context;
        if (config instanceof Map) {
            Map<String, Object> configMap = (Map<String, Object>) config;
            if (configMap.containsKey("gameId")) {
                this.gameId = (String) configMap.get("gameId");
                if (configMap.containsKey("testMode")) {
                    this.testMode = (Boolean) configMap.get("testMode");
                }
                
                UnityAds.initialize(context, gameId, testMode, new IUnityAdsInitializationListener() {
                    @Override
                    public void onInitializationComplete() {
                        Log.d(TAG, "Unity Ads initialization complete");
                    }

                    @Override
                    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                        Log.e(TAG, "Unity Ads initialization failed: " + error + " - " + message);
                    }
                });
            } else {
                Log.e(TAG, "Missing gameId in Unity Ads config");
            }
        } else {
            Log.e(TAG, "Invalid config for Unity Ads, expected Map with gameId");
        }
    }

    @Override
    public AdResponse getBid(AdRequest request) {
        AdResponse response = new AdResponse();
        response.setPlatform(PLATFORM_NAME);
        response.setAdUnitId(request.getAdUnitId());
        
        // 计算预估eCPM
        double ecpm = calculateEcpm(request);
        response.setEcpm(ecpm);
        
        return response;
    }

    @Override
    public void loadAd(AdRequest request, AdPlatformAdapter.AdCallback callback) {
        this.currentCallback = callback;
        String adUnitId = request.getAdUnitId();
        String adType = request.getExtra("ad_type");
        
        if (gameId == null || adUnitId == null) {
            callback.onError("Game ID or Ad Unit ID is null");
            return;
        }
        
        UnityAds.load(adUnitId, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                AdResponse response = new AdResponse.Builder()
                    .setAdUnitId(placementId)
                    .setPlatform(PLATFORM_NAME)
                    .setExtra("ad_type", adType)
                    .build();
                
                callback.onSuccess(response);
            }
            
            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                callback.onError(message);
            }
        });
    }

    @Override
    public void showAd(AdView adView, AdResponse response) {
        if (!(context instanceof Activity)) {
            Log.e(TAG, "Context is not an Activity");
            return;
        }
        
        Activity activity = (Activity) context;
        String adUnitId = response.getAdUnitId();
        String adType = response.getExtra("ad_type");
        
        if (adUnitId == null) {
            Log.e(TAG, "Ad unit ID is null");
            return;
        }
        
        // Unity广告需要Activity才能展示
        UnityAds.show(activity, adUnitId, new UnityAdsShowOptions(), new IUnityAdsShowListener() {
            @Override
            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                Log.e(TAG, "Unity Ads show failure: " + message);
            }
            
            @Override
            public void onUnityAdsShowStart(String placementId) {
                Log.d(TAG, "Unity Ads show start: " + placementId);
            }
            
            @Override
            public void onUnityAdsShowClick(String placementId) {
                Log.d(TAG, "Unity Ads show click: " + placementId);
            }
            
            @Override
            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                Log.d(TAG, "Unity Ads show complete: " + placementId + ", state: " + state);
                
                // 如果是激励广告并且完成了观看
                if (adType != null && adType.equals("rewarded") && 
                    state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                    // 处理奖励逻辑，例如通知RewardedAd
                    if (adView instanceof com.adverge.sdk.view.RewardedAd) {
                        ((com.adverge.sdk.view.RewardedAd) adView).notifyAdRewarded();
                    }
                }
            }
        });
    }

    @Override
    public String getPlatformName() {
        return PLATFORM_NAME;
    }

    @Override
    public double getHistoricalEcpm() {
        return historicalEcpm;
    }

    /**
     * 根据请求计算预估eCPM
     * @param request 广告请求
     * @return 预估的eCPM值
     */
    private double calculateEcpm(AdRequest request) {
        // 简单实现，返回历史eCPM
        return historicalEcpm;
    }

    @Override
    public void destroy() {
        // Unity Ads没有显式的销毁方法
        // 清除回调引用
        currentCallback = null;
    }
} 