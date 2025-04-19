package com.adverge.sdk.adapter;

import android.content.Context;
import android.util.Log;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.bigo.sdk.api.BigoAd;
import com.bigo.sdk.api.BigoAdError;
import com.bigo.sdk.api.BigoAdListener;
import com.bigo.sdk.api.BigoAdRequest;
import com.bigo.sdk.api.BigoAdSize;
import com.bigo.sdk.api.BigoAdView;
import com.bigo.sdk.api.BigoInterstitialAd;
import com.bigo.sdk.api.BigoRewardedVideoAd;
import java.util.Map;

/**
 * Bigo Ads广告平台适配器
 */
public class BigoAdsAdapter implements AdPlatformAdapter {
    private static final String TAG = "BigoAdsAdapter";
    private static final String PLATFORM_NAME = "bigoads";
    private Context context;
    private Map<String, Object> config;
    private double historicalEcpm = 0.0;
    private AdListener currentListener;
    private BigoAdView bannerAd;
    private BigoInterstitialAd interstitialAd;
    private BigoRewardedVideoAd rewardedAd;

    @Override
    public void init(Context context, Object config) {
        this.context = context;
        this.config = (Map<String, Object>) config;
        
        try {
            // 初始化Bigo Ads SDK
            BigoAd.initialize(context, (String) this.config.get("appId"));
            
            // 设置全局监听器
            BigoAd.setAdListener(new BigoAdListener() {
                @Override
                public void onAdLoaded(BigoAd ad) {
                    if (currentListener != null) {
                        currentListener.onAdLoaded();
                    }
                }

                @Override
                public void onAdFailedToLoad(BigoAd ad, BigoAdError error) {
                    if (currentListener != null) {
                        currentListener.onAdLoadFailed(error.getMessage());
                    }
                }

                @Override
                public void onAdShown(BigoAd ad) {
                    if (currentListener != null) {
                        currentListener.onAdOpened();
                    }
                }

                @Override
                public void onAdClicked(BigoAd ad) {
                    if (currentListener != null) {
                        currentListener.onAdClicked();
                    }
                }

                @Override
                public void onAdClosed(BigoAd ad) {
                    if (currentListener != null) {
                        currentListener.onAdClosed();
                    }
                }

                @Override
                public void onAdRewarded(BigoAd ad) {
                    if (currentListener != null) {
                        currentListener.onAdRewarded();
                    }
                }
            });

            Log.d(TAG, "Bigo Ads SDK初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "Bigo Ads SDK初始化失败: " + e.getMessage());
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
                    "Bigo Ads广告",
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
                interstitialAd.show();
            } else if (rewardedAd != null && rewardedAd.isReady()) {
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
        bannerAd = new BigoAdView(context);
        bannerAd.setAdUnitId(adUnitId);
        bannerAd.setAdSize(BigoAdSize.BANNER);
        bannerAd.loadAd(new BigoAdRequest.Builder().build());
    }

    private void loadInterstitialAd(String adUnitId) {
        interstitialAd = new BigoInterstitialAd(context, adUnitId);
        interstitialAd.loadAd(new BigoAdRequest.Builder().build());
    }

    private void loadRewardedAd(String adUnitId) {
        rewardedAd = new BigoRewardedVideoAd(context, adUnitId);
        rewardedAd.loadAd(new BigoAdRequest.Builder().build());
    }
} 