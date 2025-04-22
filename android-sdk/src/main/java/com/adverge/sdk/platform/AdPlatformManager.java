package com.adverge.sdk.platform;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 广告平台管理器
 */
public class AdPlatformManager {
    private static final String TAG = "AdPlatformManager";
    private static AdPlatformManager instance;
    private final Map<String, AdPlatformAdapter> adapters = new HashMap<>();
    
    private AdPlatformManager() {
        // 默认不初始化适配器，在AdSDK初始化时才注册
    }
    
    public static AdPlatformManager getInstance() {
        if (instance == null) {
            synchronized (AdPlatformManager.class) {
                if (instance == null) {
                    instance = new AdPlatformManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 注册平台适配器
     * @param adapter 适配器
     */
    public void registerAdapter(AdPlatformAdapter adapter) {
        if (adapter == null) {
            Log.e(TAG, "Cannot register null adapter");
            return;
        }
        
        String platformName = adapter.getPlatformName();
        if (platformName == null || platformName.isEmpty()) {
            Log.e(TAG, "Adapter platform name is null or empty");
            return;
        }
        
        adapters.put(platformName, adapter);
        Log.d(TAG, "Registered adapter: " + platformName);
    }
    
    /**
     * 获取平台适配器
     * @param platform 平台名称
     * @return 适配器
     */
    public AdPlatformAdapter getAdapter(String platform) {
        return adapters.get(platform);
    }
    
    /**
     * 获取所有已注册的平台名称
     * @return 平台名称集合
     */
    public Set<String> getRegisteredPlatforms() {
        return adapters.keySet();
    }
    
    /**
     * 检查平台是否已注册
     * @param platform 平台名称
     * @return 是否已注册
     */
    public boolean isPlatformRegistered(String platform) {
        return adapters.containsKey(platform);
    }
    
    /**
     * 移除平台适配器
     * @param platform 平台名称
     */
    public void removeAdapter(String platform) {
        AdPlatformAdapter adapter = adapters.remove(platform);
        if (adapter != null) {
            adapter.destroy();
            Log.d(TAG, "Removed adapter: " + platform);
        }
    }
    
    /**
     * 初始化所有平台
     * @param context 上下文
     * @param configs 配置信息
     */
    public void initAll(Context context, Map<String, Object> configs) {
        if (configs == null) {
            Log.w(TAG, "Config map is null, using default configurations");
            configs = new HashMap<>();
        }
        
        for (Map.Entry<String, AdPlatformAdapter> entry : adapters.entrySet()) {
            String platform = entry.getKey();
            AdPlatformAdapter adapter = entry.getValue();
            Object config = configs.get(platform);
            
            try {
                adapter.init(context, config);
                Log.d(TAG, "Initialized platform: " + platform);
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize platform: " + platform, e);
            }
        }
    }
} 