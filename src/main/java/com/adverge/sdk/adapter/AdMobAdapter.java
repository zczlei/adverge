package com.adverge.sdk.adapter;

import android.content.Context;
import android.util.Log;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import java.util.Map;

/**
 * AdMob广告平台适配器
 */
public class AdMobAdapter implements AdPlatformAdapter {
    private static final String TAG = "AdMobAdapter";
    private static final String PLATFORM_NAME = "admob";
    private Context context;
    private Map<String, Object> config;
    private double historicalEcpm = 0.0;
    private AdListener currentListener;
    private AdView bannerAd;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    @Override
    public void init(Context context, Object config) {
        this.context = context;
        this.config = (Map<String, Object>) config;
        
        try {
            // 初始化AdMob SDK
            MobileAds.initialize(context, initializationStatus -> {
                Log.d(TAG, "AdMob SDK初始化成功");
            });
            
            // 设置测试设备
            MobileAds.setRequestConfiguration(
                new com.google.android.gms.ads.RequestConfiguration.Builder()
                    .setTestDeviceIds((String) this.config.get("testDeviceIds"))
                    .build()
            );
        } catch (Exception e) {
            Log.e(TAG, "AdMob SDK初始化失败: " + e.getMessage());
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
                    "AdMob广告",
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
                interstitialAd.show(context);
            } else if (rewardedAd != null) {
                rewardedAd.show(context, rewardItem -> {
                    if (currentListener != null) {
                        currentListener.onAdRewarded();
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
        bannerAd = new AdView(context);
        bannerAd.setAdUnitId(adUnitId);
        bannerAd.setAdSize(AdSize.BANNER);
        
        bannerAd.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdLoaded() {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError error) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(error.getMessage());
                }
            }

            @Override
            public void onAdClicked() {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onAdOpened() {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onAdClosed() {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }
        });
        
        bannerAd.loadAd(new com.google.android.gms.ads.AdRequest.Builder().build());
    }

    private void loadInterstitialAd(String adUnitId) {
        InterstitialAd.load(context, adUnitId, new com.google.android.gms.ads.AdRequest.Builder().build(),
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(InterstitialAd ad) {
                    interstitialAd = ad;
                    if (currentListener != null) {
                        currentListener.onAdLoaded();
                    }
                    
                    ad.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdClicked() {
                            if (currentListener != null) {
                                currentListener.onAdClicked();
                            }
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            if (currentListener != null) {
                                currentListener.onAdClosed();
                            }
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            if (currentListener != null) {
                                currentListener.onAdLoadFailed(adError.getMessage());
                            }
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            if (currentListener != null) {
                                currentListener.onAdOpened();
                            }
                        }
                    });
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    if (currentListener != null) {
                        currentListener.onAdLoadFailed(loadAdError.getMessage());
                    }
                }
            });
    }

    private void loadRewardedAd(String adUnitId) {
        RewardedAd.load(context, adUnitId, new com.google.android.gms.ads.AdRequest.Builder().build(),
            new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(RewardedAd ad) {
                    rewardedAd = ad;
                    if (currentListener != null) {
                        currentListener.onAdLoaded();
                    }
                    
                    ad.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdClicked() {
                            if (currentListener != null) {
                                currentListener.onAdClicked();
                            }
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            if (currentListener != null) {
                                currentListener.onAdClosed();
                            }
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            if (currentListener != null) {
                                currentListener.onAdLoadFailed(adError.getMessage());
                            }
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            if (currentListener != null) {
                                currentListener.onAdOpened();
                            }
                        }
                    });
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    if (currentListener != null) {
                        currentListener.onAdLoadFailed(loadAdError.getMessage());
                    }
                }
            });
    }
} 