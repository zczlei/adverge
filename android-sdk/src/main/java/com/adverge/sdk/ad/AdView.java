package com.adverge.sdk.ad;

import android.content.Context;
import android.view.ViewGroup;

import com.adverge.sdk.AdSDK;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.BidResponse;
import com.adverge.sdk.network.AdServerCallback;
import com.adverge.sdk.network.AdServerClient;
import com.adverge.sdk.utils.AdLifecycleMonitor;
import com.adverge.sdk.utils.AdPreloadManager;
import com.adverge.sdk.utils.Logger;

/**
 * 广告视图基类
 */
public abstract class AdView extends ViewGroup {
    protected static final String TAG = "AdView";
    
    protected final Context context;
    protected final AdSDK sdk;
    protected final String adUnitId;
    protected AdListener listener;
    protected BidResponse bidResponse;
    protected boolean isLoaded;
    protected boolean isShowing;
    
    public AdView(Context context, String adUnitId) {
        super(context);
        this.context = context;
        this.sdk = AdSDK.getInstance();
        this.adUnitId = adUnitId;
        this.isLoaded = false;
        this.isShowing = false;
        
        // 注册生命周期监控
        AdLifecycleMonitor.getInstance(context).registerAdView(this);
    }
    
    /**
     * 设置广告监听器
     */
    public void setAdListener(AdListener listener) {
        this.listener = listener;
    }
    
    /**
     * 获取广告单元ID
     */
    public String getAdUnitId() {
        return adUnitId;
    }
    
    /**
     * 加载广告
     */
    public void loadAd() {
        Logger.d(TAG, "开始加载广告: " + adUnitId);
        
        // 创建广告请求
        AdRequest request = createAdRequest();
        
        // 直接调用后端API获取竞价结果
        AdServerClient.getInstance(context).requestBid(request, new AdServerCallback() {
            @Override
            public void onBidResponse(BidResponse response) {
                Logger.d(TAG, "收到竞价响应: " + response.getPlatform());
                bidResponse = response;
                
                // 使用对应平台的适配器加载广告
                loadAdWithAdapter(response.getPlatform(), response);
            }
            
            @Override
            public void onError(String error) {
                Logger.e(TAG, "广告请求失败: " + error);
                onAdFailedToLoad(error);
            }
        });
    }
    
    /**
     * 使用指定平台的适配器加载广告
     */
    protected abstract void loadAdWithAdapter(String platform, BidResponse response);
    
    /**
     * 显示广告
     */
    public abstract void show();
    
    /**
     * 创建广告请求
     */
    protected abstract AdRequest createAdRequest();
    
    /**
     * 预加载广告
     */
    public void preloadAd() {
        AdPreloadManager.getInstance(context).preloadAd(adUnitId, getAdType(), new AdListener() {
            @Override
            public void onAdLoaded() {
                isLoaded = true;
                if (listener != null) {
                    listener.onAdLoaded();
                }
            }
            
            @Override
            public void onAdFailedToLoad(String error) {
                isLoaded = false;
                if (listener != null) {
                    listener.onAdFailedToLoad(error);
                }
            }
            
            @Override
            public void onAdShown() {
                isShowing = true;
                if (listener != null) {
                    listener.onAdShown();
                }
            }
            
            @Override
            public void onAdClicked() {
                if (listener != null) {
                    listener.onAdClicked();
                }
            }
            
            @Override
            public void onAdClosed() {
                isShowing = false;
                if (listener != null) {
                    listener.onAdClosed();
                }
            }
            
            @Override
            public void onAdRewarded() {
                if (listener != null) {
                    listener.onAdRewarded();
                }
            }
        });
    }
    
    /**
     * 获取广告类型
     */
    protected abstract AdRequest.AdType getAdType();
    
    /**
     * 广告加载失败处理
     */
    protected void onAdFailedToLoad(String error) {
        Logger.e(TAG, "广告加载失败: " + error);
        if (listener != null) {
            listener.onAdLoadFailed(adUnitId, error);
        }
    }
    
    /**
     * 广告加载成功处理
     */
    protected void onAdLoaded() {
        Logger.d(TAG, "广告加载成功: " + adUnitId);
        isLoaded = true;
        if (listener != null) {
            listener.onAdLoaded(adUnitId);
        }
    }
    
    /**
     * 广告展示处理
     */
    protected void onAdShown() {
        Logger.d(TAG, "广告展示: " + adUnitId);
        isShowing = true;
        if (listener != null) {
            listener.onAdShown(adUnitId);
        }
    }
    
    /**
     * 广告点击处理
     */
    protected void onAdClicked() {
        Logger.d(TAG, "广告点击: " + adUnitId);
        if (listener != null) {
            listener.onAdClicked(adUnitId);
        }
    }
    
    /**
     * 广告关闭处理
     */
    protected void onAdClosed() {
        Logger.d(TAG, "广告关闭: " + adUnitId);
        isShowing = false;
        if (listener != null) {
            listener.onAdClosed(adUnitId);
        }
    }
    
    /**
     * 检查广告是否已加载
     */
    public boolean isLoaded() {
        return isLoaded;
    }
    
    /**
     * 检查广告是否正在显示
     */
    public boolean isShowing() {
        return isShowing;
    }
    
    /**
     * 销毁广告
     */
    public void destroy() {
        // 注销生命周期监控
        AdLifecycleMonitor.getInstance(context).unregisterAdView(this);
        
        // 清理资源
        listener = null;
        bidResponse = null;
    }
} 