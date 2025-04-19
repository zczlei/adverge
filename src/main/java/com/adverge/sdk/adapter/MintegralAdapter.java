package com.adverge.sdk.adapter;

import android.content.Context;
import android.util.Log;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.mbridge.msdk.MBridgeSDK;
import com.mbridge.msdk.out.MBBannerView;
import com.mbridge.msdk.out.MBInterstitialHandler;
import com.mbridge.msdk.out.MBRewardVideoHandler;
import com.mbridge.msdk.out.MBridgeIds;
import com.mbridge.msdk.out.RewardInfo;
import java.util.Map;

/**
 * Mintegral广告平台适配器
 */
public class MintegralAdapter implements AdPlatformAdapter {
    private static final String TAG = "MintegralAdapter";
    private static final String PLATFORM_NAME = "mintegral";
    private Context context;
    private Map<String, Object> config;
    private double historicalEcpm = 0.0;
    private MBBannerView bannerView;
    private MBInterstitialHandler interstitialHandler;
    private MBRewardVideoHandler rewardVideoHandler;
    private AdListener currentListener;

    @Override
    public void init(Context context, Object config) {
        this.context = context;
        this.config = (Map<String, Object>) config;
        
        try {
            // 初始化Mintegral SDK
            MBridgeSDK sdk = MBridgeSDK.getMBridgeSDK();
            sdk.init(context, this.config);
            Log.d(TAG, "Mintegral SDK初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "Mintegral SDK初始化失败: " + e.getMessage());
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
                    "Mintegral广告",
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
            if (interstitialHandler != null && interstitialHandler.isReady()) {
                interstitialHandler.show();
            } else if (rewardVideoHandler != null && rewardVideoHandler.isReady()) {
                rewardVideoHandler.show();
            }
        } catch (Exception e) {
            Log.e(TAG, "展示广告失败: " + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        try {
            if (bannerView != null) {
                bannerView.release();
            }
            if (interstitialHandler != null) {
                interstitialHandler = null;
            }
            if (rewardVideoHandler != null) {
                rewardVideoHandler = null;
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
        bannerView = new MBBannerView(context);
        bannerView.init(new MBridgeIds(adUnitId));
        bannerView.setBannerAdListener(new MBBannerView.MBBannerViewListener() {
            @Override
            public void onLoadSuccessed(MBridgeIds mBridgeIds) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onLoadFailed(MBridgeIds mBridgeIds, String errorMsg) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(errorMsg);
                }
            }

            @Override
            public void onClick(MBridgeIds mBridgeIds) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }

            @Override
            public void onLeaveApp(MBridgeIds mBridgeIds) {
                if (currentListener != null) {
                    currentListener.onAdLeftApplication();
                }
            }

            @Override
            public void showFullScreen(MBridgeIds mBridgeIds) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void closeFullScreen(MBridgeIds mBridgeIds) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }

            @Override
            public void onLogImpression(MBridgeIds mBridgeIds) {
                if (currentListener != null) {
                    currentListener.onAdImpression();
                }
            }
        });
        bannerView.load();
    }

    private void loadInterstitialAd(String adUnitId) {
        interstitialHandler = new MBInterstitialHandler(context, adUnitId);
        interstitialHandler.setInterstitialVideoListener(new MBInterstitialHandler.InterstitialVideoListener() {
            @Override
            public void onLoadSuccess(MBridgeIds mBridgeIds) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onLoadFail(MBridgeIds mBridgeIds, String errorMsg) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(errorMsg);
                }
            }

            @Override
            public void onShowSuccess(MBridgeIds mBridgeIds) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onShowFail(MBridgeIds mBridgeIds, String errorMsg) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(errorMsg);
                }
            }

            @Override
            public void onAdClose(MBridgeIds mBridgeIds, RewardInfo rewardInfo) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                }
            }

            @Override
            public void onVideoComplete(MBridgeIds mBridgeIds) {
                if (currentListener != null) {
                    currentListener.onAdCompleted();
                }
            }

            @Override
            public void onAdClick(MBridgeIds mBridgeIds) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }
        });
        interstitialHandler.load();
    }

    private void loadRewardedAd(String adUnitId) {
        rewardVideoHandler = new MBRewardVideoHandler(context, adUnitId);
        rewardVideoHandler.setRewardVideoListener(new MBRewardVideoHandler.RewardVideoListener() {
            @Override
            public void onLoadSuccess(MBridgeIds mBridgeIds) {
                if (currentListener != null) {
                    currentListener.onAdLoaded();
                }
            }

            @Override
            public void onLoadFail(MBridgeIds mBridgeIds, String errorMsg) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(errorMsg);
                }
            }

            @Override
            public void onShowSuccess(MBridgeIds mBridgeIds) {
                if (currentListener != null) {
                    currentListener.onAdOpened();
                }
            }

            @Override
            public void onShowFail(MBridgeIds mBridgeIds, String errorMsg) {
                if (currentListener != null) {
                    currentListener.onAdLoadFailed(errorMsg);
                }
            }

            @Override
            public void onVideoComplete(MBridgeIds mBridgeIds) {
                if (currentListener != null) {
                    currentListener.onAdCompleted();
                }
            }

            @Override
            public void onAdClose(MBridgeIds mBridgeIds, RewardInfo rewardInfo) {
                if (currentListener != null) {
                    currentListener.onAdClosed();
                    if (rewardInfo != null && rewardInfo.isCompleteView()) {
                        currentListener.onAdRewarded();
                    }
                }
            }

            @Override
            public void onAdClick(MBridgeIds mBridgeIds) {
                if (currentListener != null) {
                    currentListener.onAdClicked();
                }
            }
        });
        rewardVideoHandler.load();
    }
} 