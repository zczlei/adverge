package com.adverge.sdk.adapter;

import android.content.Context;
import android.util.Log;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.vungle.ads.AdConfig;
import com.vungle.ads.BannerAd;
import com.vungle.ads.BannerAdListener;
import com.vungle.ads.InterstitialAd;
import com.vungle.ads.InterstitialAdListener;
import com.vungle.ads.RewardedAd;
import com.vungle.ads.RewardedAdListener;
import com.vungle.ads.VungleAds;
import com.vungle.ads.VungleError;
import java.util.Map;

/**
 * Vungle广告平台适配器
 */
public class VungleAdapter implements AdPlatformAdapter {
    private static final String TAG = "VungleAdapter";
    private static final String PLATFORM_NAME = "vungle";
    private Context context;
    private Map<String, Object> config;
    private double historicalEcpm = 0.0;
    private AdListener currentListener;
    private BannerAd bannerAd;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    @Override
    public void init(Context context, Object config) {
        this.context = context;
        this.config = (Map<String, Object>) config;
        
        try {
            // 初始化Vungle SDK
            VungleAds.init(context, (String) this.config.get("appId"));
            
            // 设置全局监听器
            VungleAds.setLogLevel(VungleAds.LogLevel.DEBUG);
            
            Log.d(TAG, "Vungle SDK初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "Vungle SDK初始化失败: " + e.getMessage());
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
                    "Vungle广告",
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
            if (interstitialAd != null && interstitialAd.canPlayAd()) {
                interstitialAd.play();
            } else if (rewardedAd != null && rewardedAd.canPlayAd()) {
                rewardedAd.play();
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
        bannerAd = new BannerAd(context, adUnitId, new BannerAdListener() {
            @Override
            public void onAdLoaded(BannerAd ad) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onAdFailedToLoad(BannerAd ad, VungleError error) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(error.getMessage());
                }
            }

            @Override
            public void onAdClicked(BannerAd ad) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onAdImpression(BannerAd ad) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onAdClosed(BannerAd ad) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }
        });
        bannerAd.load();
    }

    private void loadInterstitialAd(String adUnitId) {
        interstitialAd = new InterstitialAd(context, adUnitId, new InterstitialAdListener() {
            @Override
            public void onAdLoaded(InterstitialAd ad) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onAdFailedToLoad(InterstitialAd ad, VungleError error) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(error.getMessage());
                }
            }

            @Override
            public void onAdClicked(InterstitialAd ad) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onAdImpression(InterstitialAd ad) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onAdClosed(InterstitialAd ad) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }
        });
        interstitialAd.load();
    }

    private void loadRewardedAd(String adUnitId) {
        rewardedAd = new RewardedAd(context, adUnitId, new RewardedAdListener() {
            @Override
            public void onAdLoaded(RewardedAd ad) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onAdFailedToLoad(RewardedAd ad, VungleError error) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(error.getMessage());
                }
            }

            @Override
            public void onAdClicked(RewardedAd ad) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onAdImpression(RewardedAd ad) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onAdClosed(RewardedAd ad) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }

            @Override
            public void onAdRewarded(RewardedAd ad) {
                if (currentListener != null) {
                    currentListener.onAdRewarded();
                }
            }
        });
        rewardedAd.load();
    }
} 