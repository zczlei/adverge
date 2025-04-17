package com.adverge.sdk.ad;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;

import com.adverge.sdk.AdSDK;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.utils.Logger;

/**
 * 插页广告
 */
public class InterstitialAd extends AdView {
    private static final String TAG = "InterstitialAd";
    
    private WebView webView;
    private Button closeButton;
    private FrameLayout container;
    private Activity activity;
    
    public InterstitialAd(Context context, String adUnitId, AdListener listener) {
        super(context, adUnitId, listener);
        if (!(context instanceof Activity)) {
            throw new IllegalArgumentException("Context must be an Activity");
        }
        this.activity = (Activity) context;
        initialize();
    }
    
    private void initialize() {
        // 创建容器布局
        container = new FrameLayout(context);
        container.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));
        
        // 创建WebView
        webView = new WebView(context);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));
        
        // 创建关闭按钮
        closeButton = new Button(context);
        closeButton.setText("关闭");
        closeButton.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            android.view.Gravity.TOP | android.view.Gravity.END
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
            .setAdType("interstitial")
            .build();
        
        // 发送请求
        AdSDK.getInstance().getRetryManager().execute(adUnitId, new RetryManager.RetryTask() {
            @Override
            public void execute() {
                // TODO: 实现实际的广告请求逻辑
                // 这里使用模拟数据
                String mockResponse = "{\"status\":\"success\",\"data\":{\"html\":\"<div>测试插页广告</div>\"}}";
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
            String html = "<div>测试插页广告</div>";
            
            // 加载广告内容
            webView.loadData(html, "text/html", "UTF-8");
            
            // 更新状态
            isLoaded = true;
            
            // 通知监听器
            if (listener != null) {
                listener.onAdLoaded(adUnitId);
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
        
        // 添加到Activity的根视图
        View rootView = activity.getWindow().getDecorView().getRootView();
        if (rootView instanceof FrameLayout) {
            FrameLayout root = (FrameLayout) rootView;
            root.addView(this);
            setVisibility(View.VISIBLE);
            isShowing = true;
            
            if (listener != null) {
                listener.onAdShown(adUnitId);
            }
        } else {
            Logger.e(TAG, "Root view is not a FrameLayout");
        }
    }
    
    @Override
    public void destroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        
        // 从Activity的根视图中移除
        View rootView = activity.getWindow().getDecorView().getRootView();
        if (rootView instanceof FrameLayout) {
            FrameLayout root = (FrameLayout) rootView;
            root.removeView(this);
        }
        
        super.destroy();
    }
} 