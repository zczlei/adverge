package com.adverge.sdk.adapter;

import android.content.Context;

import com.adverge.sdk.utils.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 广告平台管理器
 * 用于创建和管理不同广告平台的适配器
 */
public class AdPlatformManager {
    private static final String TAG = "AdPlatformManager";
    
    private static AdPlatformManager instance;
    private final Map<String, AdPlatformAdapter> adapters = new HashMap<>();
    private final Context context;
    
    private AdPlatformManager(Context context) {
        this.context = context.getApplicationContext();
        
        // 初始化各平台适配器
        initAdapters();
    }
    
    /**
     * 获取AdPlatformManager实例
     * @param context 上下文
     * @return AdPlatformManager实例
     */
    public static synchronized AdPlatformManager getInstance(Context context) {
        if (instance == null) {
            instance = new AdPlatformManager(context);
        }
        return instance;
    }
    
    /**
     * 初始化各平台适配器
     */
    private void initAdapters() {
        try {
            // 添加AdMob适配器
            AdPlatformAdapter admobAdapter = new AdMobAdapter();
            admobAdapter.init(context, null);
            adapters.put("admob", admobAdapter);
            
            // 添加Facebook适配器
            AdPlatformAdapter facebookAdapter = new FacebookAdapter();
            facebookAdapter.init(context, null);
            adapters.put("facebook", facebookAdapter);
            
            // 添加AppLovin适配器
            AdPlatformAdapter applovinAdapter = new AppLovinAdapter();
            applovinAdapter.init(context, null);
            adapters.put("applovin", applovinAdapter);
            
            // 添加Pangle适配器
            AdPlatformAdapter pangleAdapter = new PangleAdapter();
            pangleAdapter.init(context, null);
            adapters.put("pangle", pangleAdapter);
            
            // 添加Unity适配器
            AdPlatformAdapter unityAdapter = new UnityAdsAdapter();
            unityAdapter.init(context, null);
            adapters.put("unity", unityAdapter);
            
            // 添加Vungle适配器
            AdPlatformAdapter vungleAdapter = new VungleAdapter();
            vungleAdapter.init(context, null);
            adapters.put("vungle", vungleAdapter);
            
            // 添加IronSource适配器
            AdPlatformAdapter ironSourceAdapter = new IronSourceAdapter();
            ironSourceAdapter.init(context, null);
            adapters.put("ironsource", ironSourceAdapter);
            
            // 添加InMobi适配器
            AdPlatformAdapter inMobiAdapter = new InMobiAdapter();
            inMobiAdapter.init(context, null);
            adapters.put("inmobi", inMobiAdapter);
            
            // 添加TopOn适配器
            AdPlatformAdapter topOnAdapter = new TopOnAdapter();
            topOnAdapter.init(context, null);
            adapters.put("topon", topOnAdapter);
            
        } catch (Exception e) {
            Logger.e(TAG, "初始化适配器失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取指定平台的适配器
     * @param platform 平台名称（小写）
     * @return 广告平台适配器，如果不存在则返回null
     */
    public AdPlatformAdapter getAdapter(String platform) {
        if (platform == null || platform.isEmpty()) {
            Logger.e(TAG, "获取适配器失败: 平台名称为空");
            return null;
        }
        
        // 转换为小写以确保匹配
        String platformKey = platform.toLowerCase();
        
        AdPlatformAdapter adapter = adapters.get(platformKey);
        if (adapter == null) {
            Logger.e(TAG, "获取适配器失败: 未找到平台 " + platform);
        }
        
        return adapter;
    }
    
    /**
     * 注册新的适配器
     * @param platform 平台名称（小写）
     * @param adapter 适配器实例
     */
    public void registerAdapter(String platform, AdPlatformAdapter adapter) {
        if (platform == null || platform.isEmpty() || adapter == null) {
            Logger.e(TAG, "注册适配器失败: 参数无效");
            return;
        }
        
        // 转换为小写以确保匹配
        String platformKey = platform.toLowerCase();
        
        adapters.put(platformKey, adapter);
        Logger.d(TAG, "成功注册适配器: " + platform);
    }
    
    /**
     * 清理所有适配器资源
     */
    public void destroy() {
        for (AdPlatformAdapter adapter : adapters.values()) {
            try {
                adapter.destroy();
            } catch (Exception e) {
                Logger.e(TAG, "销毁适配器失败: " + e.getMessage());
            }
        }
        
        adapters.clear();
    }
} 