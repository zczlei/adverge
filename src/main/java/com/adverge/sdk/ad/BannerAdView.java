package com.adverge.sdk.ad;

import android.content.Context;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;

import com.adverge.sdk.AdSDK;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.utils.Logger;

/**
 * 横幅广告视图
 */
public class BannerAdView extends AdView {
    private static final String TAG = "BannerAdView";
    
    private WebView webView;
    private Button closeButton;
    private LinearLayout container;
    private int refreshInterval = 0; // 0表示不自动刷新
    
    public BannerAdView(Context context, String adUnitId, AdListener listener) {
        super(context, adUnitId, listener);
        initialize();
    }
    
    private void initialize() {
        // 创建容器布局
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        
        // 创建WebView
        webView = new WebView(context);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        // 创建关闭按钮
        closeButton = new Button(context);
        closeButton.setText("关闭");
        closeButton.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        closeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAdClosed(adUnitId);
            }
            destroy();
        });
        
        // 添加视图
        container.addView(webView);
        container.addView(closeButton);
        addView(container);
    }
    
    @Override
    public void loadAd() {
        if (isLoaded) {
            Logger.w(TAG, "Ad already loaded");
            return;
        }
        
        // 创建广告请求
        AdRequest request = new AdRequest.Builder()
            .setAdUnitId(adUnitId)
            .setAdType("banner")
            .build();
        
        // 添加尺寸信息
        request.addExtra("width", String.valueOf(getWidth()));
        request.addExtra("height", String.valueOf(getHeight()));
        
        // 发送请求
        AdSDK.getInstance().getRetryManager().execute(adUnitId, new RetryManager.RetryTask() {
            @Override
            public void execute() {
                // TODO: 实现实际的广告请求逻辑
                // 这里使用模拟数据
                String mockResponse = "{\"status\":\"success\",\"data\":{\"html\":\"<div>测试横幅广告</div>\"}}";
                handleResponse(mockResponse);
            }
            
            @Override
            public void onSuccess() {
                Logger.d(TAG, "Ad loaded successfully");
            }
            
            @Override
            public void onFailure(String error) {
                Logger.e(TAG, "Failed to load ad: " + error);
                if (listener != null) {
                    listener.onAdLoadFailed(adUnitId, error);
                }
            }
        });
    }
    
    private void handleResponse(String response) {
        try {
            // TODO: 解析实际的响应数据
            // 这里使用模拟数据
            String html = "<div>测试横幅广告</div>";
            
            // 加载广告内容
            webView.loadData(html, "text/html", "UTF-8");
            
            // 更新状态
            isLoaded = true;
            
            // 通知监听器
            if (listener != null) {
                listener.onAdLoaded(adUnitId);
            }
            
            // 设置自动刷新
            if (refreshInterval > 0) {
                postDelayed(this::refreshAd, refreshInterval);
            }
        } catch (Exception e) {
            Logger.e(TAG, "Failed to handle response", e);
            if (listener != null) {
                listener.onAdLoadFailed(adUnitId, e.getMessage());
            }
        }
    }
    
    @Override
    public void showAd() {
        if (!isLoaded) {
            Logger.w(TAG, "Ad not loaded");
            return;
        }
        
        setVisibility(View.VISIBLE);
        isShowing = true;
        
        if (listener != null) {
            listener.onAdShown(adUnitId);
        }
    }
    
    /**
     * 设置自动刷新间隔（毫秒）
     */
    public void setRefreshInterval(int interval) {
        this.refreshInterval = interval;
    }
    
    /**
     * 刷新广告
     */
    public void refreshAd() {
        isLoaded = false;
        loadAd();
    }
    
    @Override
    public void destroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.destroy();
    }
} 
} 