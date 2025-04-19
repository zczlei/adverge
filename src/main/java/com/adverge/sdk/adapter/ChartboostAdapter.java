package com.adverge.sdk.adapter;

import android.content.Context;
import android.util.Log;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Libraries.CBLogging.Level;
import com.chartboost.sdk.Model.CBError;
import java.util.Map;

/**
 * Chartboost广告平台适配器
 */
public class ChartboostAdapter implements AdPlatformAdapter {
    private static final String TAG = "ChartboostAdapter";
    private static final String PLATFORM_NAME = "chartboost";
    private Context context;
    private Map<String, Object> config;
    private double historicalEcpm = 0.0;
    private AdListener currentListener;

    @Override
    public void init(Context context, Object config) {
        this.context = context;
        this.config = (Map<String, Object>) config;
        
        try {
            // 初始化Chartboost SDK
            Chartboost.startWithAppId(context, 
                (String) this.config.get("appId"), 
                (String) this.config.get("appSignature"));
            
            // 设置日志级别
            Chartboost.setLoggingLevel(Level.ALL);
            
            // 设置代理
            Chartboost.setDelegate(new ChartboostDelegate() {
                @Override
                public void didCacheInterstitial(String location) {
                    if (currentListener != null) {
                        currentListener.onAdLoaded();
                    }
                }

                @Override
                public void didFailToLoadInterstitial(String location, CBError.CBImpressionError error) {
                    if (currentListener != null) {
                        currentListener.onAdLoadFailed(error.toString());
                    }
                }

                @Override
                public void didDismissInterstitial(String location) {
                    if (currentListener != null) {
                        currentListener.onAdClosed();
                    }
                }

                @Override
                public void didClickInterstitial(String location) {
                    if (currentListener != null) {
                        currentListener.onAdClicked();
                    }
                }

                @Override
                public void didDisplayInterstitial(String location) {
                    if (currentListener != null) {
                        currentListener.onAdOpened();
                    }
                }

                @Override
                public void didCacheRewardedVideo(String location) {
                    if (currentListener != null) {
                        currentListener.onAdLoaded();
                    }
                }

                @Override
                public void didFailToLoadRewardedVideo(String location, CBError.CBImpressionError error) {
                    if (currentListener != null) {
                        currentListener.onAdLoadFailed(error.toString());
                    }
                }

                @Override
                public void didDismissRewardedVideo(String location) {
                    if (currentListener != null) {
                        currentListener.onAdClosed();
                    }
                }

                @Override
                public void didClickRewardedVideo(String location) {
                    if (currentListener != null) {
                        currentListener.onAdClicked();
                    }
                }

                @Override
                public void didDisplayRewardedVideo(String location) {
                    if (currentListener != null) {
                        currentListener.onAdOpened();
                    }
                }

                @Override
                public void didCompleteRewardedVideo(String location, int reward) {
                    if (currentListener != null) {
                        currentListener.onAdRewarded();
                    }
                }
            });
            
            Log.d(TAG, "Chartboost SDK初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "Chartboost SDK初始化失败: " + e.getMessage());
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
                    "Chartboost广告",
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
            if (Chartboost.hasInterstitial("default")) {
                Chartboost.showInterstitial("default");
            } else if (Chartboost.hasRewardedVideo("default")) {
                Chartboost.showRewardedVideo("default");
            }
        } catch (Exception e) {
            Log.e(TAG, "展示广告失败: " + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        try {
            Chartboost.onDestroy(context);
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
        Chartboost.cacheInterstitial(adUnitId);
    }

    private void loadRewardedAd(String adUnitId) {
        Chartboost.cacheRewardedVideo(adUnitId);
    }
} 