package com.adverge.sdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

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

import com.ironsource.sdk.IronSource;
import com.mbridge.msdk.MBridgeSDK;
import com.inmobi.sdk.InMobiSdk;
import com.anythink.core.api.ATSDK;
import com.vungle.warren.InitCallback;
import com.vungle.warren.Vungle;
import com.google.android.gms.ads.MobileAds;
import com.facebook.ads.AudienceNetworkAds;
import com.chartboost.sdk.Chartboost;
import com.unity3d.ads.UnityAds;
import com.fyber.marketplace.sdk.Fyber;
import com.mahimeta.sdk.MahimetaSDK;
import com.bigo.sdk.BigoAds;

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
    
    // 配置键名常量
    private static final String KEY_MINTERGRAL_APP_ID = "com.mintegral.msdk.APP_ID";
    private static final String KEY_MINTERGRAL_APP_KEY = "com.mintegral.msdk.APP_KEY";
    private static final String KEY_IRONSOURCE_APP_KEY = "com.ironsource.app_key";
    private static final String KEY_INMOBI_APP_ID = "com.inmobi.sdk.APP_ID";
    private static final String KEY_TOPON_APP_ID = "com.anythink.sdk.APP_ID";
    private static final String KEY_VUNGLE_APP_ID = "com.vungle.sdk.APP_ID";
    private static final String KEY_FACEBOOK_APP_ID = "com.facebook.sdk.ApplicationId";
    private static final String KEY_CHARTBOOST_APP_ID = "com.chartboost.app_id";
    private static final String KEY_CHARTBOOST_APP_SIGNATURE = "com.chartboost.app_signature";
    private static final String KEY_UNITY_APP_ID = "com.unity3d.ads.unityads.appid";
    private static final String KEY_FYBER_APP_ID = "com.fyber.sdk.APP_ID";
    private static final String KEY_MAHIMETA_APP_ID = "com.mahimeta.sdk.APP_ID";
    private static final String KEY_BIGO_APP_ID = "com.bigo.sdk.APP_ID";
    
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

    public static void init(Context context) {
        if (isInitialized) {
            Log.w(TAG, "SDK already initialized");
            return;
        }

        try {
            // 从 manifest 读取配置
            String mintegralAppId = getMetaData(context, KEY_MINTERGRAL_APP_ID);
            String mintegralAppKey = getMetaData(context, KEY_MINTERGRAL_APP_KEY);
            String ironsourceAppKey = getMetaData(context, KEY_IRONSOURCE_APP_KEY);
            String inmobiAppId = getMetaData(context, KEY_INMOBI_APP_ID);
            String toponAppId = getMetaData(context, KEY_TOPON_APP_ID);
            String vungleAppId = getMetaData(context, KEY_VUNGLE_APP_ID);
            String facebookAppId = getMetaData(context, KEY_FACEBOOK_APP_ID);
            String chartboostAppId = getMetaData(context, KEY_CHARTBOOST_APP_ID);
            String chartboostAppSignature = getMetaData(context, KEY_CHARTBOOST_APP_SIGNATURE);
            String unityAppId = getMetaData(context, KEY_UNITY_APP_ID);
            String fyberAppId = getMetaData(context, KEY_FYBER_APP_ID);
            String mahimetaAppId = getMetaData(context, KEY_MAHIMETA_APP_ID);
            String bigoAppId = getMetaData(context, KEY_BIGO_APP_ID);

            // 初始化各个平台
            initIronSource(context, ironsourceAppKey);
            initMintegral(context, mintegralAppId, mintegralAppKey);
            initInMobi(context, inmobiAppId);
            initTopOn(context, toponAppId);
            initVungle(context, vungleAppId);
            initAdMob(context);
            initFacebook(context, facebookAppId);
            initChartboost(context, chartboostAppId, chartboostAppSignature);
            initUnityAds(context, unityAppId);
            initFyber(context, fyberAppId);
            initMahimeta(context, mahimetaAppId);
            initBigoAds(context, bigoAppId);

            isInitialized = true;
            Log.i(TAG, "All SDKs initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "SDK initialization failed", e);
        }
    }

    private static void initIronSource(Context context, String appKey) {
        if (appKey != null) {
            IronSource.init(context, appKey);
            Log.d(TAG, "IronSource initialized");
        } else {
            Log.e(TAG, "IronSource app key not found");
        }
    }

    private static void initMintegral(Context context, String appId, String appKey) {
        if (appId != null && appKey != null) {
            MBridgeSDK.init(context, appId, appKey);
            Log.d(TAG, "Mintegral initialized");
        } else {
            Log.e(TAG, "Mintegral app id or key not found");
        }
    }

    private static void initInMobi(Context context, String appId) {
        if (appId != null) {
            InMobiSdk.init(context, appId);
            Log.d(TAG, "InMobi initialized");
        } else {
            Log.e(TAG, "InMobi app id not found");
        }
    }

    private static void initTopOn(Context context, String appId) {
        if (appId != null) {
            ATSDK.init(context, appId);
            Log.d(TAG, "TopOn initialized");
        } else {
            Log.e(TAG, "TopOn app id not found");
        }
    }

    private static void initVungle(Context context, String appId) {
        if (appId != null) {
            Vungle.init(appId, context, new InitCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Vungle initialized");
                }

                @Override
                public void onError(Throwable throwable) {
                    Log.e(TAG, "Vungle initialization failed", throwable);
                }

                @Override
                public void onAutoCacheAdAvailable(String placementId) {
                    Log.d(TAG, "Vungle ad cached: " + placementId);
                }
            });
        } else {
            Log.e(TAG, "Vungle app id not found");
        }
    }

    private static void initAdMob(Context context) {
        MobileAds.initialize(context, initializationStatus -> {
            Log.d(TAG, "AdMob initialized");
        });
    }

    private static void initFacebook(Context context, String appId) {
        if (appId != null) {
            AudienceNetworkAds.initialize(context);
            Log.d(TAG, "Facebook initialized");
        } else {
            Log.e(TAG, "Facebook app id not found");
        }
    }

    private static void initChartboost(Context context, String appId, String appSignature) {
        if (appId != null && appSignature != null) {
            Chartboost.startWithAppId(context, appId, appSignature);
            Log.d(TAG, "Chartboost initialized");
        } else {
            Log.e(TAG, "Chartboost app id or signature not found");
        }
    }

    private static void initUnityAds(Context context, String appId) {
        if (appId != null) {
            UnityAds.initialize(context, appId, false);
            Log.d(TAG, "Unity Ads initialized");
        } else {
            Log.e(TAG, "Unity Ads app id not found");
        }
    }

    private static void initFyber(Context context, String appId) {
        if (appId != null) {
            Fyber.init(context, appId);
            Log.d(TAG, "Fyber initialized");
        } else {
            Log.e(TAG, "Fyber app id not found");
        }
    }

    private static void initMahimeta(Context context, String appId) {
        if (appId != null) {
            MahimetaSDK.init(context, appId);
            Log.d(TAG, "Mahimeta initialized");
        } else {
            Log.e(TAG, "Mahimeta app id not found");
        }
    }

    private static void initBigoAds(Context context, String appId) {
        if (appId != null) {
            BigoAds.init(context, appId);
            Log.d(TAG, "Bigo Ads initialized");
        } else {
            Log.e(TAG, "Bigo Ads app id not found");
        }
    }

    private static String getMetaData(Context context, String key) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            return bundle.getString(key);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get meta-data: " + key, e);
            return null;
        }
    }

    public static boolean isInitialized() {
        return isInitialized;
    }
} 