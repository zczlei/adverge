package com.adverge.sdk.adapter;

import android.content.Context;
import android.util.Log;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;
import java.util.Map;

/**
 * Unity Ads广告平台适配器
 */
public class UnityAdapter implements AdPlatformAdapter {
    private static final String TAG = "UnityAdapter";
    private static final String PLATFORM_NAME = "unity";
    private Context context;
    private Map<String, Object> config;
    private double historicalEcpm = 0.0;
    private AdListener currentListener;
    private String currentAdUnitId;

    @Override
    public void init(Context context, Object config) {
        this.context = context;
        this.config = (Map<String, Object>) config;
        
        try {
            // 初始化Unity Ads SDK
            UnityAds.initialize(context, 
                (String) this.config.get("gameId"),
                (Boolean) this.config.get("testMode"),
                new IUnityAdsInitializationListener() {
                    @Override
                    public void onInitializationComplete() {
                        Log.d(TAG, "Unity Ads SDK初始化成功");
                    }

                    @Override
                    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                        Log.e(TAG, "Unity Ads SDK初始化失败: " + message);
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Unity Ads SDK初始化失败: " + e.getMessage());
        }
    }

    @Override
    public AdResponse getBid(AdRequest request) {
        try {
            // 模拟竞价过程
            double ecpm = calculateEcpm(request);
            if (ecpm > 0) {
                return new AdResponse(
                    PLATFORM_NAME,
                    request.getAdUnitId(),
                    ecpm,
                    "Unity Ads广告",
                    System.currentTimeMillis() + 3600000 // 1小时后过期
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "获取竞价失败: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void loadAd(String adUnitId, AdListener listener) {
        this.currentListener = listener;
        this.currentAdUnitId = adUnitId;
        String adType = getAdTypeFromUnitId(adUnitId);
        
        try {
            switch (adType) {
                case "interstitial":
                    loadInterstitialAd(adUnitId);
                    break;
                case "rewarded":
                    loadRewardedAd(adUnitId);
                    break;
                default:
                    listener.onAdLoadFailed("不支持的广告类型");
            }
        } catch (Exception e) {
            listener.onAdLoadFailed("加载广告失败: " + e.getMessage());
        }
    }

    @Override
    public void showAd() {
        try {
            if (UnityAds.isReady(currentAdUnitId)) {
                UnityAds.show(context, currentAdUnitId, new UnityAdsShowOptions(), new IUnityAdsShowListener() {
                    @Override
                    public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                        if (currentListener != null) {
                            currentListener.onAdLoadFailed(message);
                        }
                    }

                    @Override
                    public void onUnityAdsShowStart(String placementId) {
                        if (currentListener != null) {
                            currentListener.onAdOpened();
                        }
                    }

                    @Override
                    public void onUnityAdsShowClick(String placementId) {
                        if (currentListener != null) {
                            currentListener.onAdClicked();
                        }
                    }

                    @Override
                    public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                        if (currentListener != null) {
                            if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                                currentListener.onAdRewarded();
                            }
                            currentListener.onAdClosed();
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "展示广告失败: " + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        try {
            // Unity Ads SDK不需要显式销毁
        } catch (Exception e) {
            Log.e(TAG, "销毁适配器失败: " + e.getMessage());
        }
    }

    @Override
    public String getPlatformName() {
        return PLATFORM_NAME;
    }

    @Override
    public double getHistoricalEcpm() {
        return historicalEcpm;
    }

    private double calculateEcpm(AdRequest request) {
        // 模拟计算eCPM
        return Math.random() * 10;
    }

    private String getAdTypeFromUnitId(String adUnitId) {
        // 从广告单元ID中解析广告类型
        if (adUnitId.contains("interstitial")) {
            return "interstitial";
        } else if (adUnitId.contains("rewarded")) {
            return "rewarded";
        }
        return "";
    }

    private void loadInterstitialAd(String adUnitId) {
        UnityAds.load(adUnitId, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(message);
                }
            }
        });
    }

    private void loadRewardedAd(String adUnitId) {
        UnityAds.load(adUnitId, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(message);
                }
            }
        });
    }
} 