package com.adverge.sdk.adapter;

import android.content.Context;
import android.util.Log;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.inmobi.ads.AdMetaInfo;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.ads.listeners.BannerAdEventListener;
import com.inmobi.ads.listeners.InterstitialAdEventListener;
import com.inmobi.ads.listeners.RewardedAdEventListener;
import com.inmobi.sdk.InMobiSdk;
import java.util.Map;

/**
 * InMobi广告平台适配器
 */
public class InMobiAdapter implements AdPlatformAdapter {
    private static final String TAG = "InMobiAdapter";
    private static final String PLATFORM_NAME = "inmobi";
    private Context context;
    private Map<String, Object> config;
    private double historicalEcpm = 0.0;
    private AdListener currentListener;
    private InMobiBanner bannerAd;
    private InMobiInterstitial interstitialAd;
    private InMobiInterstitial rewardedAd;

    @Override
    public void init(Context context, Object config) {
        this.context = context;
        this.config = (Map<String, Object>) config;
        
        try {
            // 初始化InMobi SDK
            InMobiSdk.init(context, (String) this.config.get("accountId"));
            
            // 设置全局监听器
            InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG);
            
            Log.d(TAG, "InMobi SDK初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "InMobi SDK初始化失败: " + e.getMessage());
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
                    "InMobi广告",
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
        bannerAd = new InMobiBanner(context, Long.parseLong(adUnitId));
        
        bannerAd.setListener(new BannerAdEventListener() {
            @Override
            public void onAdLoadSucceeded(InMobiBanner ad, AdMetaInfo info) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onAdLoadFailed(InMobiBanner ad, InMobiAdRequestStatus status) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(status.getMessage());
                }
            }

            @Override
            public void onAdClicked(InMobiBanner ad, Map<Object, Object> params) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onAdDisplayed(InMobiBanner ad) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onAdDismissed(InMobiBanner ad) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }
        });
        
        bannerAd.load();
    }

    private void loadInterstitialAd(String adUnitId) {
        interstitialAd = new InMobiInterstitial(context, Long.parseLong(adUnitId), new InterstitialAdEventListener() {
            @Override
            public void onAdLoadSucceeded(InMobiInterstitial ad, AdMetaInfo info) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onAdLoadFailed(InMobiInterstitial ad, InMobiAdRequestStatus status) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(status.getMessage());
                }
            }

            @Override
            public void onAdClicked(InMobiInterstitial ad, Map<Object, Object> params) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onAdDisplayed(InMobiInterstitial ad) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onAdDismissed(InMobiInterstitial ad) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }
        });
        
        interstitialAd.load();
    }

    private void loadRewardedAd(String adUnitId) {
        rewardedAd = new InMobiInterstitial(context, Long.parseLong(adUnitId), new RewardedAdEventListener() {
            @Override
            public void onAdLoadSucceeded(InMobiInterstitial ad, AdMetaInfo info) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onAdLoadFailed(InMobiInterstitial ad, InMobiAdRequestStatus status) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(status.getMessage());
                }
            }

            @Override
            public void onAdClicked(InMobiInterstitial ad, Map<Object, Object> params) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onAdDisplayed(InMobiInterstitial ad) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onAdDismissed(InMobiInterstitial ad) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }

            @Override
            public void onUserRewarded(InMobiInterstitial ad, Map<Object, Object> rewards) {
                if (currentListener != null) {
                    currentListener.onAdRewarded();
                }
            }
        });
        
        rewardedAd.load();
    }
} 