package com.adverge.sdk.adapter;

import android.content.Context;
import android.util.Log;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import java.util.Map;

/**
 * AppLovin广告平台适配器
 */
public class AppLovinAdapter implements AdPlatformAdapter {
    private static final String TAG = "AppLovinAdapter";
    private static final String PLATFORM_NAME = "applovin";
    private Context context;
    private Map<String, Object> config;
    private double historicalEcpm = 0.0;
    private MaxAdView bannerAd;
    private MaxInterstitialAd interstitialAd;
    private MaxRewardedAd rewardedAd;
    private AdListener currentListener;

    @Override
    public void init(Context context, Object config) {
        this.context = context;
        this.config = (Map<String, Object>) config;
        
        try {
            // 初始化AppLovin SDK
            AppLovinSdk.getInstance(context).setMediationProvider("max");
            AppLovinSdk.initializeSdk(context, new AppLovinSdk.SdkInitializationListener() {
                @Override
                public void onSdkInitialized(final AppLovinSdkConfiguration configuration) {
                    Log.d(TAG, "AppLovin SDK初始化成功");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "AppLovin SDK初始化失败: " + e.getMessage());
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
                    "AppLovin广告",
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
            if (interstitialAd != null && interstitialAd.isReady()) {
                interstitialAd.showAd();
            } else if (rewardedAd != null && rewardedAd.isReady()) {
                rewardedAd.showAd();
            }
        } catch (Exception e) {
            Log.e(TAG, "展示广告失败: " + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        try {
            if (bannerAd != null) {
                bannerAd.destroy();
            }
            if (interstitialAd != null) {
                interstitialAd = null;
            }
            if (rewardedAd != null) {
                rewardedAd = null;
            }
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
        bannerAd = new MaxAdView(adUnitId, context);
        bannerAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(error.getMessage());
                }
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }
        });
        bannerAd.loadAd();
    }

    private void loadInterstitialAd(String adUnitId) {
        interstitialAd = new MaxInterstitialAd(adUnitId, context);
        interstitialAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(error.getMessage());
                }
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }
        });
        interstitialAd.loadAd();
    }

    private void loadRewardedAd(String adUnitId) {
        rewardedAd = MaxRewardedAd.getInstance(adUnitId, context);
        rewardedAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(error.getMessage());
                }
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }

            @Override
            public void onRewardedVideoCompleted(MaxAd ad) {
                if (currentListener != null) {
                    currentListener.onAdCompleted();
                }
            }

            @Override
            public void onRewardedVideoStarted(MaxAd ad) {
                if (currentListener != null) {
                    currentListener.onAdStarted();
                }
            }

            @Override
            public void onUserRewarded(MaxAd ad, MaxReward reward) {
                if (currentListener != null) {
                    currentListener.onAdRewarded();
                }
            }
        });
        rewardedAd.loadAd();
    }
} 