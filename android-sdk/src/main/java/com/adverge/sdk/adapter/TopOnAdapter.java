package com.adverge.sdk.adapter;

import android.content.Context;
import android.util.Log;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATNetworkConfig;
import com.anythink.core.api.ATSDK;
import com.anythink.banner.api.ATBannerView;
import com.anythink.interstitial.api.ATInterstitial;
import com.anythink.rewardedvideo.api.ATRewardedVideo;
import com.anythink.nativead.api.ATNative;
import java.util.Map;

/**
 * TopOn广告平台适配器
 */
public class TopOnAdapter implements AdPlatformAdapter {
    private static final String TAG = "TopOnAdapter";
    private static final String PLATFORM_NAME = "topon";
    private Context context;
    private Map<String, Object> config;
    private double historicalEcpm = 0.0;
    private AdListener currentListener;
    private ATBannerView bannerAd;
    private ATInterstitial interstitialAd;
    private ATRewardedVideo rewardedAd;
    private ATNative nativeAd;

    @Override
    public void init(Context context, Object config) {
        this.context = context;
        this.config = (Map<String, Object>) config;
        
        try {
            // 初始化TopOn SDK
            ATSDK.init(context, (String) this.config.get("appId"), (String) this.config.get("appKey"));
            
            // 设置日志级别
            ATSDK.setNetworkLogDebug(true);
            
            Log.d(TAG, "TopOn SDK初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "TopOn SDK初始化失败: " + e.getMessage());
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
                    "TopOn广告",
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
                case "native":
                    loadNativeAd(adUnitId);
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
            if (interstitialAd != null && interstitialAd.isAdReady()) {
                interstitialAd.show();
            } else if (rewardedAd != null && rewardedAd.isAdReady()) {
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
            if (nativeAd != null) {
                nativeAd.destroy();
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
        } else if (adUnitId.contains("native")) {
            return "native";
        }
        return "";
    }

    private void loadBannerAd(String adUnitId) {
        bannerAd = new ATBannerView(context);
        bannerAd.setPlacementId(adUnitId);
        bannerAd.setAdListener(new ATBannerView.BannerAdListener() {
            @Override
            public void onBannerLoaded() {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onBannerFailed(String errorMsg) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(errorMsg);
                }
            }

            @Override
            public void onBannerClicked(ATAdInfo adInfo) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onBannerShow(ATAdInfo adInfo) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onBannerClose(ATAdInfo adInfo) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }
        });
        bannerAd.loadAd();
    }

    private void loadInterstitialAd(String adUnitId) {
        interstitialAd = new ATInterstitial(context, adUnitId);
        interstitialAd.setAdListener(new ATInterstitial.InterstitialAdListener() {
            @Override
            public void onInterstitialAdLoaded() {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onInterstitialAdLoadFail(String errorMsg) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(errorMsg);
                }
            }

            @Override
            public void onInterstitialAdClicked(ATAdInfo adInfo) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onInterstitialAdShow(ATAdInfo adInfo) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onInterstitialAdClose(ATAdInfo adInfo) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }
        });
        interstitialAd.load();
    }

    private void loadRewardedAd(String adUnitId) {
        rewardedAd = new ATRewardedVideo(context, adUnitId);
        rewardedAd.setAdListener(new ATRewardedVideo.RewardedVideoListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onRewardedVideoAdFailed(String errorMsg) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(errorMsg);
                }
            }

            @Override
            public void onRewardedVideoAdPlayStart(ATAdInfo adInfo) {
                if (currentListener != null) {
                    currentListener.onAdStarted();
                }
            }

            @Override
            public void onRewardedVideoAdPlayEnd(ATAdInfo adInfo) {
                if (currentListener != null) {
                    currentListener.onAdCompleted();
                }
            }

            @Override
            public void onRewardedVideoAdPlayFailed(String errorMsg, ATAdInfo adInfo) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(errorMsg);
                }
            }

            @Override
            public void onRewardedVideoAdClosed(ATAdInfo adInfo) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }

            @Override
            public void onRewardedVideoAdPlayClicked(ATAdInfo adInfo) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onReward(ATAdInfo adInfo) {
                if (currentListener != null) {
                    currentListener.onAdRewarded();
                }
            }
        });
        rewardedAd.load();
    }

    private void loadNativeAd(String adUnitId) {
        nativeAd = new ATNative(context, adUnitId);
        nativeAd.setAdListener(new ATNative.NativeAdListener() {
            @Override
            public void onNativeAdLoaded() {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onNativeAdLoadFail(String errorMsg) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(errorMsg);
                }
            }

            @Override
            public void onNativeAdClick(ATAdInfo adInfo) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }
        });
        nativeAd.load();
    }
} 