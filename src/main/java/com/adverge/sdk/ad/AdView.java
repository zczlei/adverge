package com.adverge.sdk.ad;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.adverge.sdk.AdSDK;
import com.adverge.sdk.adapter.AdPlatformAdapter;
import com.adverge.sdk.adapter.AdPlatformManager;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
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
    protected AdResponse adResponse;
    protected AdPlatformAdapter currentAdapter;
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
        
        // 调用后端API获取竞价结果
        AdServerClient.getInstance(context).requestAd(request, new AdServerCallback() {
            @Override
            public void onSuccess(AdResponse response) {
                Logger.d(TAG, "收到广告响应: platform=" + response.getPlatform() + ", adId=" + response.getAdId());
                adResponse = response;
                
                // 使用对应平台的适配器加载广告
                loadAdWithAdapter(response.getPlatform(), response.getAdId(), response);
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
    protected void loadAdWithAdapter(String platform, String adId, AdResponse response) {
        Logger.d(TAG, "使用适配器加载广告: platform=" + platform + ", adId=" + adId);
        
        try {
            // 通过平台名称获取对应的适配器
            currentAdapter = AdPlatformManager.getInstance(context).getAdapter(platform);
            
            if (currentAdapter == null) {
                onAdFailedToLoad("未找到平台适配器: " + platform);
                return;
            }
            
            // 设置监听器
            AdListener adapterListener = new AdListener() {
                @Override
                public void onAdLoaded() {
                    isLoaded = true;
                    if (listener != null) {
                        listener.onAdLoaded();
                    }
                }
                
                @Override
                public void onAdFailedToLoad(String errorMessage) {
                    Logger.e(TAG, "适配器加载广告失败: " + errorMessage);
                    isLoaded = false;
                    if (listener != null) {
                        listener.onAdFailedToLoad(errorMessage);
                    }
                }
                
                @Override
                public void onAdClicked() {
                    if (listener != null) {
                        listener.onAdClicked();
                    }
                    
                    // 记录点击事件
                    AdServerClient.getInstance(context).trackClick(adUnitId, platform);
                }
                
                @Override
                public void onAdImpression() {
                    if (listener != null) {
                        listener.onAdImpression();
                    }
                    
                    // 记录展示事件
                    AdServerClient.getInstance(context).trackImpression(adUnitId, platform);
                }
                
                @Override
                public void onAdOpened() {
                    isShowing = true;
                    if (listener != null) {
                        listener.onAdOpened();
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
            };
            
            // 加载广告
            currentAdapter.loadAd(adId, adapterListener);
            
        } catch (Exception e) {
            Logger.e(TAG, "加载广告异常: " + e.getMessage());
            onAdFailedToLoad("加载广告异常: " + e.getMessage());
        }
    }
    
    /**
     * 显示广告
     */
    public void show() {
        if (!isLoaded) {
            Logger.e(TAG, "广告未加载或加载失败，无法显示");
            return;
        }
        
        if (currentAdapter != null) {
            try {
                currentAdapter.showAd();
            } catch (Exception e) {
                Logger.e(TAG, "显示广告失败: " + e.getMessage());
                if (listener != null) {
                    listener.onAdFailedToShow("显示广告失败: " + e.getMessage());
                }
            }
        }
    }
    
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
     * 广告加载失败回调
     */
    protected void onAdFailedToLoad(String errorMessage) {
        isLoaded = false;
        if (listener != null) {
            listener.onAdFailedToLoad(errorMessage);
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
        if (currentAdapter != null) {
            currentAdapter.destroy();
            currentAdapter = null;
        }
        
        isLoaded = false;
        isShowing = false;
        AdLifecycleMonitor.getInstance(context).unregisterAdView(this);
    }
} 