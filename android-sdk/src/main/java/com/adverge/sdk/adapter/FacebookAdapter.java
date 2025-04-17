package com.adverge.sdk.adapter;

import android.content.Context;
import android.util.Log;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;
import java.util.Map;

/**
 * Facebook Audience Network广告平台适配器
 */
public class FacebookAdapter implements AdPlatformAdapter {
    private static final String TAG = "FacebookAdapter";
    private static final String PLATFORM_NAME = "facebook";
    private Context context;
    private Map<String, Object> config;
    private double historicalEcpm = 0.0;
    private AdListener currentListener;
    private AdView bannerAd;
    private InterstitialAd interstitialAd;
    private RewardedVideoAd rewardedAd;

    @Override
    public void init(Context context, Object config) {
        this.context = context;
        this.config = (Map<String, Object>) config;
        
        try {
            // 初始化Facebook SDK
            com.facebook.ads.AudienceNetworkAds.initialize(context);
            Log.d(TAG, "Facebook SDK初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "Facebook SDK初始化失败: " + e.getMessage());
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
                    "Facebook广告",
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
            if (interstitialAd != null) {
                interstitialAd.show();
            } else if (rewardedAd != null) {
                rewardedAd.show();
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
                interstitialAd.destroy();
            }
            if (rewardedAd != null) {
                rewardedAd.destroy();
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
        bannerAd = new AdView(context, adUnitId, AdSize.BANNER_HEIGHT_50);
        
        bannerAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(adError.getErrorMessage());
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }
        });
        
        bannerAd.loadAd();
    }

    private void loadInterstitialAd(String adUnitId) {
        interstitialAd = new InterstitialAd(context, adUnitId);
        
        interstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(adError.getErrorMessage());
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onInterstitialDisplayed(Ad ad) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }
        });
        
        interstitialAd.loadAd();
    }

    private void loadRewardedAd(String adUnitId) {
        rewardedAd = new RewardedVideoAd(context, adUnitId);
        
        rewardedAd.setAdListener(new RewardedVideoAdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(adError.getErrorMessage());
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onRewardedVideoCompleted() {
                if (currentListener != null) {
                    currentListener.onAdRewarded();
                }
            }

            @Override
            public void onRewardedVideoClosed() {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }
        });
        
        rewardedAd.loadAd();
    }
} 