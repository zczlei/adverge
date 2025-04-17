package com.adverge.sdk;

import android.content.Context;
import android.text.TextUtils;

import com.adverge.sdk.ad.AdView;
import com.adverge.sdk.ad.BannerAdView;
import com.adverge.sdk.ad.InterstitialAd;
import com.adverge.sdk.ad.NativeAd;
import com.adverge.sdk.ad.RewardedAd;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.utils.AdLifecycleMonitor;
import com.adverge.sdk.utils.AdPreloadManager;
import com.adverge.sdk.utils.CacheManager;
import com.adverge.sdk.utils.Logger;
import com.adverge.sdk.utils.RetryManager;
import com.adverge.sdk.utils.SecurityUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 广告SDK主类
 */
public class AdSDK {
    private static final String TAG = "AdSDK";
    private static AdSDK instance;
    
    private Context context;
    private String appId;
    private boolean isInitialized;
    private Map<String, AdView> activeAds;
    
    // 工具类实例
    private CacheManager cacheManager;
    private RetryManager retryManager;
    private AdPreloadManager preloadManager;
    private AdLifecycleMonitor lifecycleMonitor;
    private SecurityUtils securityUtils;
    
    private AdSDK() {
        this.activeAds = new HashMap<>();
    }
    
    public static AdSDK getInstance() {
        if (instance == null) {
            synchronized (AdSDK.class) {
                if (instance == null) {
                    instance = new AdSDK();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化SDK
     * @param context 应用上下文
     * @param appId 应用ID
     * @param config SDK配置
     */
    public void initialize(Context context, String appId, AdSDKConfig config) {
        if (isInitialized) {
            Logger.w(TAG, "SDK already initialized");
            return;
        }
        
        if (context == null || TextUtils.isEmpty(appId)) {
            throw new IllegalArgumentException("Context and appId cannot be null or empty");
        }
        
        this.context = context.getApplicationContext();
        this.appId = appId;
        
        // 初始化工具类
        initializeUtils(config);
        
        isInitialized = true;
        Logger.i(TAG, "SDK initialized successfully");
    }
    
    /**
     * 初始化工具类
     */
    private void initializeUtils(AdSDKConfig config) {
        cacheManager = CacheManager.getInstance();
        retryManager = RetryManager.getInstance();
        preloadManager = AdPreloadManager.getInstance();
        lifecycleMonitor = AdLifecycleMonitor.getInstance();
        securityUtils = SecurityUtils.getInstance();
        
        // 配置工具类
        cacheManager.initialize(context, config.getCacheConfig());
        preloadManager.initialize(config.getPreloadConfig());
        securityUtils.initialize(config.getSecurityConfig());
    }
    
    /**
     * 创建横幅广告
     * @param adUnitId 广告单元ID
     * @param listener 广告监听器
     * @return BannerAdView实例
     */
    public BannerAdView createBannerAd(String adUnitId, AdListener listener) {
        checkInitialization();
        BannerAdView adView = new BannerAdView(context, adUnitId, listener);
        registerAdView(adUnitId, adView);
        return adView;
    }
    
    /**
     * 创建插页广告
     * @param adUnitId 广告单元ID
     * @param listener 广告监听器
     * @return InterstitialAd实例
     */
    public InterstitialAd createInterstitialAd(String adUnitId, AdListener listener) {
        checkInitialization();
        InterstitialAd ad = new InterstitialAd(context, adUnitId, listener);
        registerAdView(adUnitId, ad);
        return ad;
    }
    
    /**
     * 创建激励广告
     * @param adUnitId 广告单元ID
     * @param listener 广告监听器
     * @return RewardedAd实例
     */
    public RewardedAd createRewardedAd(String adUnitId, AdListener listener) {
        checkInitialization();
        RewardedAd ad = new RewardedAd(context, adUnitId, listener);
        registerAdView(adUnitId, ad);
        return ad;
    }
    
    /**
     * 创建原生广告
     * @param adUnitId 广告单元ID
     * @param listener 广告监听器
     * @return NativeAd实例
     */
    public NativeAd createNativeAd(String adUnitId, AdListener listener) {
        checkInitialization();
        NativeAd ad = new NativeAd(context, adUnitId, listener);
        registerAdView(adUnitId, ad);
        return ad;
    }
    
    /**
     * 注册广告视图
     */
    private void registerAdView(String adUnitId, AdView adView) {
        activeAds.put(adUnitId, adView);
        lifecycleMonitor.registerAdView(adUnitId, adView);
    }
    
    /**
     * 注销广告视图
     */
    public void unregisterAdView(String adUnitId) {
        AdView adView = activeAds.remove(adUnitId);
        if (adView != null) {
            lifecycleMonitor.unregisterAdView(adUnitId);
            adView.destroy();
        }
    }
    
    /**
     * 预加载广告
     * @param adUnitId 广告单元ID
     * @param adType 广告类型
     */
    public void preloadAd(String adUnitId, String adType) {
        checkInitialization();
        preloadManager.preloadAd(adUnitId, adType);
    }
    
    /**
     * 检查SDK是否已初始化
     */
    private void checkInitialization() {
        if (!isInitialized) {
            throw new IllegalStateException("SDK not initialized. Call initialize() first.");
        }
    }
    
    /**
     * 获取缓存管理器
     */
    public CacheManager getCacheManager() {
        return cacheManager;
    }
    
    /**
     * 获取重试管理器
     */
    public RetryManager getRetryManager() {
        return retryManager;
    }
    
    /**
     * 获取预加载管理器
     */
    public AdPreloadManager getPreloadManager() {
        return preloadManager;
    }
    
    /**
     * 获取生命周期监控器
     */
    public AdLifecycleMonitor getLifecycleMonitor() {
        return lifecycleMonitor;
    }
    
    /**
     * 获取安全工具类
     */
    public SecurityUtils getSecurityUtils() {
        return securityUtils;
    }
    
    /**
     * 销毁SDK
     */
    public void destroy() {
        // 销毁所有活跃的广告
        for (AdView adView : activeAds.values()) {
            adView.destroy();
        }
        activeAds.clear();
        
        // 销毁工具类
        cacheManager.destroy();
        retryManager.destroy();
        preloadManager.destroy();
        lifecycleMonitor.destroy();
        
        isInitialized = false;
        instance = null;
        Logger.i(TAG, "SDK destroyed");
    }
} 