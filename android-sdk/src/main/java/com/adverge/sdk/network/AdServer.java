package com.adverge.sdk.network;

import android.content.Context;
import android.util.Log;

import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.adverge.sdk.platform.AdPlatformAdapter;
import com.adverge.sdk.platform.AdPlatformManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 广告服务器类
 */
public class AdServer {
    private static final String TAG = "AdServer";
    private final Context context;
    private final AdPlatformManager platformManager;
    
    public AdServer(Context context) {
        this.context = context;
        this.platformManager = AdPlatformManager.getInstance();
    }
    
    public void init(Map<String, Object> configs) {
        platformManager.initAll(context, configs);
    }
    
    public void requestAd(AdRequest request, AdCallback callback) {
        if (request == null || callback == null) {
            Log.e(TAG, "Invalid request or callback");
            return;
        }
        
        // 获取所有已注册的平台
        Set<String> platforms = platformManager.getRegisteredPlatforms();
        if (platforms.isEmpty()) {
            Log.e(TAG, "No platform registered");
            callback.onError("No platform registered");
            return;
        }
        
        // 遍历所有平台请求广告
        for (String platform : platforms) {
            AdPlatformAdapter adapter = platformManager.getAdapter(platform);
            if (adapter != null) {
                adapter.loadAd(request, new AdPlatformAdapter.AdCallback() {
                    @Override
                    public void onSuccess(AdResponse response) {
                        callback.onSuccess(response);
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Failed to load ad from " + platform + ": " + error);
                    }
                });
            }
        }
    }
    
    public void trackPerformance(String adId, String event, Map<String, Object> params) {
        // TODO: 实现性能跟踪逻辑
    }
    
    public void destroy() {
        // 销毁所有平台适配器
        Set<String> platforms = platformManager.getRegisteredPlatforms();
        for (String platform : platforms) {
            platformManager.removeAdapter(platform);
        }
    }
} 