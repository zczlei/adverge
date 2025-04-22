package com.adverge.sdk.platform;

import android.content.Context;

import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.adverge.sdk.view.AdView;

/**
 * 广告平台适配器接口
 * 用于适配不同广告平台的实现
 */
public interface AdPlatformAdapter {
    /**
     * 广告加载回调接口
     */
    interface AdCallback {
        /**
         * 广告加载成功
         * @param response 广告响应
         */
        void onSuccess(AdResponse response);

        /**
         * 广告加载失败
         * @param error 错误信息
         */
        void onError(String error);
    }

    /**
     * 初始化平台
     * @param context 上下文
     * @param config 配置信息
     */
    void init(Context context, Object config);
    
    /**
     * 获取广告出价
     * @param request 广告请求
     * @return 广告响应
     */
    default AdResponse getBid(AdRequest request) {
        // 默认实现，返回空响应
        return new AdResponse();
    }
    
    /**
     * 加载广告
     * @param request 广告请求
     * @param callback 回调
     */
    void loadAd(AdRequest request, AdCallback callback);
    
    /**
     * 显示广告
     * @param adView 广告视图
     * @param response 广告响应
     */
    void showAd(AdView adView, AdResponse response);

    /**
     * 获取平台名称
     * @return 平台名称
     */
    String getPlatformName();
    
    /**
     * 获取历史eCPM
     * @return 历史eCPM值
     */
    default double getHistoricalEcpm() {
        return 0.0;
    }
    
    /**
     * 销毁平台资源
     */
    void destroy();
} 