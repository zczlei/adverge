package com.adverge.sdk.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.adverge.sdk.platform.AdPlatformAdapter;
import com.adverge.sdk.view.AdView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AdMob广告平台适配器
 */
public class AdMobAdapter implements AdPlatformAdapter {
    private static final String TAG = "AdMobAdapter";
    private static final String PLATFORM_NAME = "admob";
    private Context context;
    private String appId;
    private double historicalEcpm = 0.0;
    private com.google.android.gms.ads.AdView bannerAd;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;
    private AdPlatformAdapter.AdCallback currentCallback;

    public AdMobAdapter(Context context) {
        this.context = context;
        MobileAds.initialize(context);
    }

    @Override
    public void init(Context context, Object config) {
        this.context = context;
        if (config instanceof String) {
            this.appId = (String) config;
            MobileAds.initialize(context, initializationStatus -> {
                Log.d(TAG, "AdMob SDK initialized: " + initializationStatus);
            });
        } else {
            Log.e(TAG, "Invalid config for AdMob, expected String appId");
        }
    }

    @Override
    public AdResponse getBid(AdRequest request) {
        AdResponse response = new AdResponse();
        response.setPlatform(PLATFORM_NAME);
        response.setAdUnitId(request.getAdUnitId());
        
        // 计算预估eCPM
        double ecpm = calculateEcpm(request);
        response.setEcpm(ecpm);
        
        return response;
    }

    @Override
    public void loadAd(AdRequest request, AdPlatformAdapter.AdCallback callback) {
        this.currentCallback = callback;
        String adUnitId = request.getAdUnitId();
        String adType = request.getExtra("ad_type");

        if (adType == null) {
            callback.onError("Ad type not specified");
            return;
        }

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
                callback.onError("Unsupported ad type: " + adType);
        }
    }

    @Override
    public void showAd(AdView adView, AdResponse response) {
        if (!(context instanceof Activity)) {
            Log.e(TAG, "Context is not an Activity");
            return;
        }

        Activity activity = (Activity) context;
        String adType = response.getExtra("ad_type");

        if (adType == null) {
            Log.e(TAG, "Ad type not specified");
            return;
        }

        switch (adType) {
            case "banner":
                if (bannerAd != null) {
                    adView.addView(bannerAd);
                }
                break;
            case "interstitial":
                if (interstitialAd != null) {
                    interstitialAd.show(activity);
                }
                break;
            case "rewarded":
                if (rewardedAd != null) {
                    rewardedAd.show(activity, rewardItem -> {
                        // 处理奖励
                        Log.d(TAG, "User earned reward: " + rewardItem.getAmount() + " " + rewardItem.getType());
                    });
                }
                break;
            default:
                Log.e(TAG, "Unsupported ad type: " + adType);
        }
    }

    @Override
    public void destroy() {
        if (bannerAd != null) {
            bannerAd.destroy();
            bannerAd = null;
        }
        interstitialAd = null;
        rewardedAd = null;
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
        // 根据历史数据和请求参数计算预估eCPM
        return historicalEcpm;
    }

    private void loadBannerAd(String adUnitId) {
        bannerAd = new com.google.android.gms.ads.AdView(context);
        bannerAd.setAdUnitId(adUnitId);
        bannerAd.setAdSize(AdSize.BANNER);
        
        bannerAd.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdLoaded() {
                if (currentCallback != null) {
                    AdResponse response = new AdResponse();
                    response.setPlatform(PLATFORM_NAME);
                    response.setAdUnitId(adUnitId);
                    response.setExtra("ad_type", "banner");
                    currentCallback.onSuccess(response);
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError error) {
                if (currentCallback != null) {
                    currentCallback.onError(error.getMessage());
                }
            }
        });
        
        bannerAd.loadAd(new Builder().build());
    }

    private void loadInterstitialAd(String adUnitId) {
        InterstitialAd.load(context, adUnitId, new Builder().build(), new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                interstitialAd = ad;
                setInterstitialCallbacks(ad, adUnitId);
                
                if (currentCallback != null) {
                    AdResponse response = new AdResponse();
                    response.setPlatform(PLATFORM_NAME);
                    response.setAdUnitId(adUnitId);
                    response.setExtra("ad_type", "interstitial");
                    currentCallback.onSuccess(response);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                interstitialAd = null;
                if (currentCallback != null) {
                    currentCallback.onError(error.getMessage());
                }
            }
        });
    }
    
    private void setInterstitialCallbacks(InterstitialAd ad, String adUnitId) {
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                // 广告关闭
                interstitialAd = null;
            }
            
            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // 广告展示失败
                interstitialAd = null;
            }
        });
    }

    private void loadRewardedAd(String adUnitId) {
        RewardedAd.load(context, adUnitId, new Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewardedAd = ad;
                setRewardedCallbacks(ad, adUnitId);
                
                if (currentCallback != null) {
                    AdResponse response = new AdResponse();
                    response.setPlatform(PLATFORM_NAME);
                    response.setAdUnitId(adUnitId);
                    response.setExtra("ad_type", "rewarded");
                    currentCallback.onSuccess(response);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                rewardedAd = null;
                if (currentCallback != null) {
                    currentCallback.onError(error.getMessage());
                }
            }
        });
    }
    
    private void setRewardedCallbacks(RewardedAd ad, String adUnitId) {
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                // 广告关闭
                rewardedAd = null;
            }
            
            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // 广告展示失败
                rewardedAd = null;
            }
        });
    }
} 