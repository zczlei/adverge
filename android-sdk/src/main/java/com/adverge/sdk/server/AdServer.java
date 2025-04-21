package com.adverge.sdk.server;

import android.content.Context;

import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.adverge.sdk.model.Platform;
import com.adverge.sdk.network.AdCallback;
import com.adverge.sdk.network.AdServerClient;
import com.adverge.sdk.network.AdServerClient.PlatformCallback;

public class AdServer {
    private static AdServer instance;
    private final AdServerClient client;
    
    private AdServer(Context context) {
        client = AdServerClient.getInstance(context);
    }
    
    public static synchronized AdServer getInstance(Context context) {
        if (instance == null) {
            instance = new AdServer(context);
        }
        return instance;
    }
    
    public void requestAd(AdRequest request, AdCallback callback) {
        client.requestAd(request, new AdServerClient.AdCallback() {
            @Override
            public void onSuccess(AdResponse response) {
                callback.onSuccess(response);
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    public void trackImpression(String adUnitId, String platform) {
        client.trackImpression(adUnitId, platform);
    }
    
    public void trackClick(String adUnitId, String platform) {
        client.trackClick(adUnitId, platform);
    }
    
    public void getPlatforms(PlatformCallback callback) {
        client.getPlatforms(callback);
    }
    
    public void savePlatform(Platform platform, PlatformCallback callback) {
        client.savePlatform(platform, callback);
    }
    
    public void enablePlatform(String platformName, PlatformCallback callback) {
        client.enablePlatform(platformName, callback);
    }
    
    public void disablePlatform(String platformName, PlatformCallback callback) {
        client.disablePlatform(platformName, callback);
    }
    
    public void destroy() {
        client.destroy();
    }
} 