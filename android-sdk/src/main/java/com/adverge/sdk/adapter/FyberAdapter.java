package com.adverge.sdk.adapter;

import android.content.Context;
import android.util.Log;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.fyber.ads.AdFormat;
import com.fyber.ads.AdListener;
import com.fyber.ads.AdRequest;
import com.fyber.ads.AdResponse;
import com.fyber.ads.BannerAd;
import com.fyber.ads.InterstitialAd;
import com.fyber.ads.RewardedVideoAd;
import com.fyber.ads.SplashAd;
import com.fyber.ads.VideoAd;
import java.util.Map;

/**
 * Fyber广告平台适配器
 */
public class FyberAdapter implements AdPlatformAdapter {
    private static final String TAG = "FyberAdapter";
    private static final String PLATFORM_NAME = "fyber";
    private Context context;
    private Map<String, Object> config;
    private double historicalEcpm = 0.0;
    private AdListener currentListener;
    private BannerAd bannerAd;
    private InterstitialAd interstitialAd;
    private RewardedVideoAd rewardedAd;
    private SplashAd splashAd;

    @Override
    public void init(Context context, Object config) {
        this.context = context;
        this.config = (Map<String, Object>) config;
        
        try {
            // 初始化Fyber SDK
            com.fyber.ads.FyberAds.init(context, (String) this.config.get("appId"));
            Log.d(TAG, "Fyber SDK初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "Fyber SDK初始化失败: " + e.getMessage());
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
                    "Fyber广告",
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
                case "splash":
                    loadSplashAd(adUnitId);
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
            } else if (splashAd != null) {
                splashAd.show();
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
            if (splashAd != null) {
                splashAd.destroy();
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
        } else if (adUnitId.contains("splash")) {
            return "splash";
        }
        return "";
    }

    private void loadBannerAd(String adUnitId) {
        bannerAd = new BannerAd(context, adUnitId);
        
        bannerAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onAdFailedToLoad(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(adResponse.getErrorMessage());
                }
            }

            @Override
            public void onAdClicked(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onAdOpened(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onAdClosed(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }
        });
        
        bannerAd.loadAd(new AdRequest.Builder().build());
    }

    private void loadInterstitialAd(String adUnitId) {
        interstitialAd = new InterstitialAd(context, adUnitId);
        
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onAdFailedToLoad(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(adResponse.getErrorMessage());
                }
            }

            @Override
            public void onAdClicked(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onAdOpened(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onAdClosed(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }
        });
        
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void loadRewardedAd(String adUnitId) {
        rewardedAd = new RewardedVideoAd(context, adUnitId);
        
        rewardedAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onAdFailedToLoad(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(adResponse.getErrorMessage());
                }
            }

            @Override
            public void onAdClicked(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onAdOpened(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onAdClosed(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }

            @Override
            public void onRewarded(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdRewarded();
                }
            }
        });
        
        rewardedAd.loadAd(new AdRequest.Builder().build());
    }

    private void loadSplashAd(String adUnitId) {
        splashAd = new SplashAd(context, adUnitId);
        
        splashAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onAdFailedToLoad(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(adResponse.getErrorMessage());
                }
            }

            @Override
            public void onAdClicked(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onAdOpened(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onAdClosed(AdResponse adResponse) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }
        });
        
        splashAd.loadAd(new AdRequest.Builder().build());
    }
} 