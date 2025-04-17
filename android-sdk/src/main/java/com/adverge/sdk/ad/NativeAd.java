package com.adverge.sdk.ad;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adverge.sdk.AdSDK;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.utils.ImageLoader;
import com.adverge.sdk.utils.Logger;

/**
 * 原生广告
 */
public class NativeAd extends AdView {
    private static final String TAG = "NativeAd";
    
    private FrameLayout container;
    private ImageView iconView;
    private ImageView mainImageView;
    private TextView titleView;
    private TextView descriptionView;
    private TextView advertiserView;
    private Button actionButton;
    private LinearLayout contentLayout;
    
    public NativeAd(Context context, String adUnitId, AdListener listener) {
        super(context, adUnitId, listener);
        initialize();
    }
    
    private void initialize() {
        // 创建容器布局
        container = new FrameLayout(context);
        container.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ));
        
        // 创建内容布局
        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ));
        
        // 创建图标视图
        iconView = new ImageView(context);
        iconView.setLayoutParams(new LinearLayout.LayoutParams(
            50, 50
        ));
        
        // 创建主图视图
        mainImageView = new ImageView(context);
        mainImageView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            200
        ));
        mainImageView.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        
        // 创建标题视图
        titleView = new TextView(context);
        titleView.setTextSize(16);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        // 创建描述视图
        descriptionView = new TextView(context);
        descriptionView.setTextSize(14);
        descriptionView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        // 创建广告主视图
        advertiserView = new TextView(context);
        advertiserView.setTextSize(12);
        advertiserView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        // 创建行动按钮
        actionButton = new Button(context);
        actionButton.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        actionButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAdClicked(adUnitId);
            }
        });
        
        // 添加视图
        contentLayout.addView(iconView);
        contentLayout.addView(mainImageView);
        contentLayout.addView(titleView);
        contentLayout.addView(descriptionView);
        contentLayout.addView(advertiserView);
        contentLayout.addView(actionButton);
        container.addView(contentLayout);
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
            .setAdType("native")
            .build();
        
        // 发送请求
        AdSDK.getInstance().getRetryManager().execute(adUnitId, new RetryManager.RetryTask() {
            @Override
            public void execute() {
                // TODO: 实现实际的广告请求逻辑
                // 这里使用模拟数据
                String mockResponse = "{\"status\":\"success\",\"data\":{\"title\":\"测试原生广告\",\"description\":\"这是一个测试原生广告的描述\",\"icon\":\"https://example.com/icon.png\",\"image\":\"https://example.com/image.png\",\"advertiser\":\"测试广告主\",\"action\":\"立即下载\"}}";
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
            String title = "测试原生广告";
            String description = "这是一个测试原生广告的描述";
            String iconUrl = "https://example.com/icon.png";
            String imageUrl = "https://example.com/image.png";
            String advertiser = "测试广告主";
            String action = "立即下载";
            
            // 更新视图
            titleView.setText(title);
            descriptionView.setText(description);
            advertiserView.setText(advertiser);
            actionButton.setText(action);
            
            // 加载图片
            ImageLoader.getInstance().loadImage(iconUrl, iconView);
            ImageLoader.getInstance().loadImage(imageUrl, mainImageView);
            
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
        
        setVisibility(View.VISIBLE);
        isShowing = true;
        
        if (listener != null) {
            listener.onAdShown(adUnitId);
        }
    }
    
    @Override
    public void destroy() {
        if (iconView != null) {
            iconView.setImageDrawable(null);
        }
        if (mainImageView != null) {
            mainImageView.setImageDrawable(null);
        }
        super.destroy();
    }
} 