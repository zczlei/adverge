package com.adverge.sdk.adapter;

import android.content.Context;
import android.util.Log;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTBannerAd;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import java.util.List;
import android.view.View;

/**
 * Pangle广告平台适配器
 */
public class PangleAdapter implements AdPlatformAdapter {
    private static final String TAG = "PangleAdapter";
    private static final String PLATFORM_NAME = "pangle";
    private Context context;
    private Map<String, Object> config;
    private double historicalEcpm = 0.0;
    private TTBannerAd bannerAd;
    private TTFullScreenVideoAd interstitialAd;
    private TTRewardVideoAd rewardedAd;
    private AdListener currentListener;
    private TTAdNative adNative;

    @Override
    public void init(Context context, Object config) {
        this.context = context;
        this.config = (Map<String, Object>) config;
        
        try {
            // 初始化Pangle SDK
            TTAdConfig adConfig = new TTAdConfig.Builder()
                .appId((String) this.config.get("appId"))
                .useTextureView(true)
                .appName((String) this.config.get("appName"))
                .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)
                .allowShowNotify(true)
                .debug(true)
                .build();
            
            TTAdSdk.init(context, adConfig);
            adNative = TTAdSdk.getAdManager().createAdNative(context);
            Log.d(TAG, "Pangle SDK初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "Pangle SDK初始化失败: " + e.getMessage());
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
                    "Pangle广告",
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
                interstitialAd.showFullScreenVideoAd(context);
            } else if (rewardedAd != null) {
                rewardedAd.showRewardVideoAd(context);
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
        adNative.loadBannerExpressAd(adUnitId, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(message);
                }
            }

            @Override
            public void onNativeExpressAdLoad(List<TTExpressAd> ads) {
                if (ads != null && !ads.isEmpty()) {
                    bannerAd = ads.get(0);
                    bannerAd.setExpressInteractionListener(new TTExpressAd.AdInteractionListener() {
                        @Override
                        public void onAdClicked(View view, int type) {
                            if (currentListener != null) {
                                currentListener.onAdClicked();
                            }
                        }

                        @Override
                        public void onAdShow(View view, int type) {
                            if (currentListener != null) {
                                currentListener.onAdImpression();
                            }
                        }

                        @Override
                        public void onRenderFail(View view, String msg, int code) {
                            if (currentListener != null) {
                                currentListener.onAdLoadFailed(msg);
                            }
                        }

                        @Override
                        public void onRenderSuccess(View view, float width, float height) {
                            if (currentListener != null) {
                                currentListener.onAdLoaded();
                            }
                        }
                    });
                }
            }
        });
    }

    private void loadInterstitialAd(String adUnitId) {
        adNative.loadFullScreenVideoAd(adUnitId, new TTAdNative.FullScreenVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(message);
                }
            }

            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
                interstitialAd = ad;
                interstitialAd.setFullScreenVideoAdInteractionListener(new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {
                    @Override
                    public void onAdShow() {
                        if (currentListener != null) {
                            currentListener.onAdOpened();
                        }
                    }

                    @Override
                    public void onAdVideoBarClick() {
                        if (currentListener != null) {
                            currentListener.onAdClicked();
                        }
                    }

                    @Override
                    public void onAdClose() {
                        if (currentListener != null) {
                            currentListener.onAdClosed();
                        }
                    }

                    @Override
                    public void onVideoComplete() {
                        if (currentListener != null) {
                            currentListener.onAdCompleted();
                        }
                    }

                    @Override
                    public void onSkippedVideo() {
                        if (currentListener != null) {
                            currentListener.onAdSkipped();
                        }
                    }
                });
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onFullScreenVideoCached() {
                // 视频缓存完成
            }
        });
    }

    private void loadRewardedAd(String adUnitId) {
        adNative.loadRewardVideoAd(adUnitId, new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(message);
                }
            }

            @Override
            public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
                rewardedAd = ad;
                rewardedAd.setRewardAdInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {
                    @Override
                    public void onAdShow() {
                        if (currentListener != null) {
                            currentListener.onAdOpened();
                        }
                    }

                    @Override
                    public void onAdVideoBarClick() {
                        if (currentListener != null) {
                            currentListener.onAdClicked();
                        }
                    }

                    @Override
                    public void onAdClose() {
                        if (currentListener != null) {
                            currentListener.onAdClosed();
                        }
                    }

                    @Override
                    public void onVideoComplete() {
                        if (currentListener != null) {
                            currentListener.onAdCompleted();
                        }
                    }

                    @Override
                    public void onVideoError() {
                        if (currentListener != null) {
                            currentListener.onAdLoadFailed("视频播放错误");
                        }
                    }

                    @Override
                    public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName) {
                        if (currentListener != null && rewardVerify) {
                            currentListener.onAdRewarded();
                        }
                    }

                    @Override
                    public void onSkippedVideo() {
                        if (currentListener != null) {
                            currentListener.onAdSkipped();
                        }
                    }
                });
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onRewardVideoCached() {
                // 视频缓存完成
            }
        });
    }
} 