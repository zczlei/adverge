package com.adverge.sdk.adapter;

import android.content.Context;
import android.util.Log;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.BannerListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;
import java.util.Map;

/**
 * IronSource广告平台适配器
 */
public class IronSourceAdapter implements AdPlatformAdapter {
    private static final String TAG = "IronSourceAdapter";
    private static final String PLATFORM_NAME = "ironsource";
    private Context context;
    private Map<String, Object> config;
    private double historicalEcpm = 0.0;
    private AdListener currentListener;

    @Override
    public void init(Context context, Object config) {
        this.context = context;
        this.config = (Map<String, Object>) config;
        
        try {
            // 初始化IronSource SDK
            IronSource.init(context, (String) this.config.get("appKey"), IronSource.AD_UNIT.REWARDED_VIDEO, 
                IronSource.AD_UNIT.INTERSTITIAL, IronSource.AD_UNIT.BANNER);
            
            // 设置监听器
            IronSource.setRewardedVideoListener(new RewardedVideoListener() {
                @Override
                public void onRewardedVideoAdReady() {
                    if (currentListener != null) {
                        currentListener.onAdLoaded();
                    }
                }

                @Override
                public void onRewardedVideoAdUnavailable() {
                    if (currentListener != null) {
                        currentListener.onAdLoadFailed("广告不可用");
                    }
                }

                @Override
                public void onRewardedVideoAdShowFailed(IronSourceError error) {
                    if (currentListener != null) {
                        currentListener.onAdLoadFailed(error.getErrorMessage());
                    }
                }

                @Override
                public void onRewardedVideoAdOpened() {
                    if (currentListener != null) {
                        currentListener.onAdOpened();
                    }
                }

                @Override
                public void onRewardedVideoAdClosed() {
                    if (currentListener != null) {
                        currentListener.onAdClosed();
                    }
                }

                @Override
                public void onRewardedVideoAdStarted() {
                    if (currentListener != null) {
                        currentListener.onAdStarted();
                    }
                }

                @Override
                public void onRewardedVideoAdEnded() {
                    if (currentListener != null) {
                        currentListener.onAdCompleted();
                    }
                }

                @Override
                public void onRewardedVideoAdRewarded(Placement placement) {
                    if (currentListener != null) {
                        currentListener.onAdRewarded();
                    }
                }

                @Override
                public void onRewardedVideoAdClicked(Placement placement) {
                    if (currentListener != null) {
                        currentListener.onAdClicked();
                    }
                }
            });

            IronSource.setInterstitialListener(new InterstitialListener() {
                @Override
                public void onInterstitialAdReady() {
                    if (currentListener != null) {
                        currentListener.onAdLoaded();
                    }
                }

                @Override
                public void onInterstitialAdLoadFailed(IronSourceError error) {
                    if (currentListener != null) {
                        currentListener.onAdLoadFailed(error.getErrorMessage());
                    }
                }

                @Override
                public void onInterstitialAdOpened() {
                    if (currentListener != null) {
                        currentListener.onAdOpened();
                    }
                }

                @Override
                public void onInterstitialAdClosed() {
                    if (currentListener != null) {
                        currentListener.onAdClosed();
                    }
                }

                @Override
                public void onInterstitialAdShowSucceeded() {
                    if (currentListener != null) {
                        currentListener.onAdImpression();
                    }
                }

                @Override
                public void onInterstitialAdShowFailed(IronSourceError error) {
                    if (currentListener != null) {
                        currentListener.onAdLoadFailed(error.getErrorMessage());
                    }
                }

                @Override
                public void onInterstitialAdClicked() {
                    if (currentListener != null) {
                        currentListener.onAdClicked();
                    }
                }
            });

            IronSource.setBannerListener(new BannerListener() {
                @Override
                public void onBannerAdLoaded() {
                    if (currentListener != null) {
                        currentListener.onAdLoaded();
                    }
                }

                @Override
                public void onBannerAdLoadFailed(IronSourceError error) {
                    if (currentListener != null) {
                        currentListener.onAdLoadFailed(error.getErrorMessage());
                    }
                }

                @Override
                public void onBannerAdClicked() {
                    if (currentListener != null) {
                        currentListener.onAdClicked();
                    }
                }

                @Override
                public void onBannerAdScreenPresented() {
                    if (currentListener != null) {
                        currentListener.onAdOpened();
                    }
                }

                @Override
                public void onBannerAdScreenDismissed() {
                    if (currentListener != null) {
                        currentListener.onAdClosed();
                    }
                }

                @Override
                public void onBannerAdLeftApplication() {
                    if (currentListener != null) {
                        currentListener.onAdLeftApplication();
                    }
                }
            });

            Log.d(TAG, "IronSource SDK初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "IronSource SDK初始化失败: " + e.getMessage());
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
                    "IronSource广告",
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
                case "banner":
                    loadBannerAd(adUnitId);
                    break;
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
            if (IronSource.isInterstitialReady()) {
                IronSource.showInterstitial();
            } else if (IronSource.isRewardedVideoAvailable()) {
                IronSource.showRewardedVideo();
            }
        } catch (Exception e) {
            Log.e(TAG, "展示广告失败: " + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        try {
            IronSource.destroyBanner();
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
        if (adUnitId.contains("banner")) {
            return "banner";
        } else if (adUnitId.contains("interstitial")) {
            return "interstitial";
        } else if (adUnitId.contains("rewarded")) {
            return "rewarded";
        }
        return "";
    }

    private void loadBannerAd(String adUnitId) {
        IronSource.loadBanner(adUnitId);
    }

    private void loadInterstitialAd(String adUnitId) {
        IronSource.loadInterstitial();
    }

    private void loadRewardedAd(String adUnitId) {
        IronSource.loadRewardedVideo();
    }
} 