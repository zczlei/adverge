package com.adverge.sdk.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.adverge.sdk.network.AdServerClient;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.platform.AdPlatformAdapter;
import com.adverge.sdk.platform.AdPlatformManager;
import com.adverge.sdk.utils.AdLifecycleMonitor;

/**
 * 广告视图基类
 * 所有广告类型的基类,提供通用的广告加载、展示和事件通知功能
 */
public abstract class AdView extends FrameLayout {
    protected String adUnitId;
    protected AdListener adListener;
    protected AdPlatformAdapter platformAdapter;
    protected boolean isLoaded;
    protected boolean isShowing;
    protected AdServerClient adServerClient;
    protected AdResponse currentAdResponse;

    public AdView(Context context) {
        super(context);
        init();
    }

    public AdView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        isLoaded = false;
        isShowing = false;
        adServerClient = AdServerClient.getInstance(getContext());
    }

    public void setAdUnitId(String adUnitId) {
        this.adUnitId = adUnitId;
    }

    public String getAdUnitId() {
        return adUnitId;
    }

    public void setAdListener(AdListener listener) {
        this.adListener = listener;
        // 延迟注册到 AdLifecycleMonitor
        new Handler(Looper.getMainLooper()).post(() -> {
            AdLifecycleMonitor.getInstance(getContext()).registerAdView(this, listener);
        });
    }

    public AdListener getAdListener() {
        return adListener;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public boolean isShowing() {
        return isShowing;
    }

    /**
     * 加载广告
     * 1. 检查广告单元ID是否有效
     * 2. 向广告服务器请求广告
     * 3. 根据返回的平台信息获取对应的平台适配器
     * 4. 使用平台适配器加载广告
     */
    public void loadAd(AdRequest request) {
        if (request == null) {
            notifyAdLoadFailed("Ad request cannot be null");
            return;
        }

        if (adUnitId == null || adUnitId.isEmpty()) {
            notifyAdLoadFailed("Ad unit ID is not set");
            return;
        }

        request.setAdUnitId(adUnitId);

        isLoaded = false;
        adServerClient.requestAd(request, new AdServerClient.AdCallback() {
            @Override
            public void onSuccess(AdResponse response) {
                if (response != null && response.getPlatform() != null) {
                    String platform = response.getPlatform();
                    platformAdapter = AdPlatformManager.getInstance().getAdapter(platform);
                    if (platformAdapter != null) {
                        platformAdapter.loadAd(request, new AdPlatformAdapter.AdCallback() {
                            @Override
                            public void onSuccess(AdResponse response) {
                                isLoaded = true;
                                currentAdResponse = response;
                                notifyAdLoaded(response);
                            }

                            @Override
                            public void onError(String error) {
                                notifyAdLoadFailed(error);
                            }
                        });
                    } else {
                        notifyAdLoadFailed("Platform adapter not found: " + platform);
                    }
                } else {
                    notifyAdLoadFailed("Invalid ad response");
                }
            }

            @Override
            public void onError(String error) {
                notifyAdLoadFailed(error);
            }
        });
    }

    /**
     * 通知广告加载成功
     * @param response 广告响应
     */
    protected void notifyAdLoaded(AdResponse response) {
        post(() -> {
            if (adListener != null) {
                adListener.onAdLoaded(response);
            }
        });
    }

    /**
     * 通知广告加载失败
     * @param error 错误信息
     */
    protected void notifyAdLoadFailed(String error) {
        post(() -> {
            if (adListener != null) {
                adListener.onAdLoadFailed(error);
            }
        });
    }

    /**
     * 通知广告展示
     */
    protected void notifyAdShown() {
        isShowing = true;
        if (adUnitId != null && platformAdapter != null) {
            adServerClient.trackImpression(adUnitId, platformAdapter.getPlatformName());
        }
        
        post(() -> {
            if (adListener != null) {
                adListener.onAdShown();
            }
        });
    }

    /**
     * 通知广告点击
     */
    protected void notifyAdClicked() {
        if (adUnitId != null && platformAdapter != null) {
            adServerClient.trackClick(adUnitId, platformAdapter.getPlatformName());
        }
        
        post(() -> {
            if (adListener != null) {
                adListener.onAdClicked();
            }
        });
    }

    /**
     * 通知广告关闭
     */
    protected void notifyAdClosed() {
        isShowing = false;
        post(() -> {
            if (adListener != null) {
                adListener.onAdClosed();
            }
        });
    }

    /**
     * 通知广告奖励发放
     * @param type 奖励类型
     * @param amount 奖励数量
     */
    protected void notifyAdRewarded(String type, int amount) {
        post(() -> {
            if (adListener != null) {
                adListener.onRewarded(type, amount);
            }
        });
    }

    /**
     * 销毁广告视图
     * 释放资源，移除监听器
     */
    public void destroy() {
        AdLifecycleMonitor.getInstance(getContext()).unregisterAdView(this);
        adListener = null;
        if (platformAdapter != null) {
            platformAdapter = null;
        }
    }

    /**
     * 展示广告
     * 检查广告是否已加载，然后调用平台适配器的showAd方法
     */
    public void show() {
        if (!isLoaded) {
            if (adListener != null) {
                adListener.onAdLoadFailed("Ad not loaded yet");
            }
            return;
        }
        
        if (platformAdapter != null && currentAdResponse != null) {
            platformAdapter.showAd(this, currentAdResponse);
            notifyAdShown();
        } else {
            if (adListener != null) {
                adListener.onAdLoadFailed("Platform adapter not available");
            }
        }
    }
} 