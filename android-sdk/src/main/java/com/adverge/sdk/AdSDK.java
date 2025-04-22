package com.adverge.sdk;

import android.content.Context;
import android.util.Log;

import com.adverge.sdk.adapter.AdMobAdapter;
import com.adverge.sdk.adapter.UnityAdapter;
import com.adverge.sdk.config.AdConfig;
import com.adverge.sdk.network.AdServerClient;
import com.adverge.sdk.platform.AdPlatformAdapter;
import com.adverge.sdk.platform.AdPlatformManager;
import com.adverge.sdk.server.AdServer;
import com.adverge.sdk.utils.SecurityUtils;

/**
 * AdVerge SDK 主类
 */
public class AdSDK {
    private static final String TAG = "AdSDK";
    private static AdSDK instance;
    private Context context;
    private AdPlatformManager platformManager;
    private AdServer adServer;
    private AdServerClient adServerClient;
    private AdConfig config;
    private SecurityUtils securityUtils;
    
    private AdSDK(Context context) {
        this.context = context.getApplicationContext();
        this.platformManager = AdPlatformManager.getInstance();
        this.adServer = AdServer.getInstance(context);
        this.adServerClient = AdServerClient.getInstance(context);
        this.config = new AdConfig();
        this.securityUtils = new SecurityUtils();
        
        // 注册广告平台适配器
        initAdPlatformAdapters();
    }
    
    public static synchronized void initialize(Context context) {
        if (instance == null) {
            instance = new AdSDK(context);
            Log.d(TAG, "AdSDK initialized");
        }
    }
    
    public static AdSDK getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AdSDK must be initialized first");
        }
        return instance;
    }
    
    public void registerPlatformAdapter(AdPlatformAdapter adapter) {
        platformManager.registerAdapter(adapter);
    }
    
    public AdPlatformManager getPlatformManager() {
        return platformManager;
    }
    
    public AdServer getAdServer() {
        return adServer;
    }
    
    public AdServerClient getAdServerClient() {
        return adServerClient;
    }
    
    /**
     * 获取应用上下文
     */
    public Context getContext() {
        return context;
    }
    
    /**
     * 获取配置
     */
    public AdConfig getConfig() {
        return config;
    }
    
    /**
     * 获取安全工具
     */
    public SecurityUtils getSecurityUtils() {
        return securityUtils;
    }
    
    /**
     * 初始化广告适配器
     */
    private void initAdPlatformAdapters() {
        // 注册AdMob适配器
        registerPlatformAdapter(new AdMobAdapter(context));
        
        // 注册Unity适配器
        registerPlatformAdapter(new UnityAdapter(context));
        
        // 其他平台适配器
        // registerPlatformAdapter(new FacebookAdapter(context));
        // registerPlatformAdapter(new IronSourceAdapter(context));
    }
} 