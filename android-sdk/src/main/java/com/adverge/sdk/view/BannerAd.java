package com.adverge.sdk.view;

import android.content.Context;
import android.util.AttributeSet;

import com.adverge.sdk.AdSDK;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.adverge.sdk.network.AdCallback;
import com.adverge.sdk.platform.AdPlatformAdapter;
import com.adverge.sdk.platform.AdPlatformManager;

import java.util.HashMap;
import java.util.Map;

/**
 * 横幅广告
 */
public class BannerAd extends AdView {
    private AdResponse adResponse;
    
    public BannerAd(Context context) {
        super(context);
    }
    
    public BannerAd(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public BannerAd(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    /**
     * 加载横幅广告
     */
    public void loadAd() {
        if (adUnitId == null || adUnitId.isEmpty()) {
            notifyAdLoadFailed("Ad unit ID is not set");
            return;
        }
        
        AdRequest request = new AdRequest(adUnitId);
        Map<String, String> extras = new HashMap<>();
        extras.put("ad_type", "banner");
        request.setExtras(extras);
        
        AdSDK.getInstance().getAdServer().requestAd(request, new AdCallback() {
            @Override
            public void onSuccess(AdResponse response) {
                adResponse = response;
                platformAdapter = AdPlatformManager.getInstance().getAdapter(response.getPlatform());
                if (platformAdapter == null) {
                    notifyAdLoadFailed("No adapter found for platform: " + response.getPlatform());
                    return;
                }
                
                platformAdapter.loadAd(request, new AdPlatformAdapter.AdCallback() {
                    @Override
                    public void onSuccess(AdResponse response) {
                        notifyAdLoaded(response);
                    }
                    
                    @Override
                    public void onError(String error) {
                        notifyAdLoadFailed(error);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                notifyAdLoadFailed(error);
            }
        });
    }
    
    /**
     * 显示横幅广告
     */
    public void show() {
        if (!isLoaded) {
            notifyAdLoadFailed("Ad not loaded");
            return;
        }
        
        if (platformAdapter != null && adResponse != null) {
            platformAdapter.showAd(this, adResponse);
            notifyAdShown();
        } else {
            notifyAdLoadFailed("Invalid adapter or response");
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }
} 