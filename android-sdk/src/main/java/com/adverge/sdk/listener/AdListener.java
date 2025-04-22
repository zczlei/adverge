package com.adverge.sdk.listener;

import com.adverge.sdk.model.AdResponse;

/**
 * 广告监听器接口
 */
public interface AdListener {
    /**
     * 广告加载成功
     */
    void onAdLoaded();
    
    /**
     * 带响应数据的广告加载成功
     * @param response 广告响应
     */
    default void onAdLoaded(AdResponse response) {
        onAdLoaded();
    }

    /**
     * 广告加载失败
     * @param error 错误信息
     */
    void onAdLoadFailed(String error);

    /**
     * 广告展示
     */
    void onAdShown();

    /**
     * 广告点击
     */
    void onAdClicked();

    /**
     * 广告关闭
     */
    void onAdClosed();
    
    /**
     * 广告奖励发放
     */
    default void onAdRewarded() {
        // 默认空实现
    }

    /**
     * 广告奖励回调
     * @param type 奖励类型
     * @param amount 奖励数量
     */
    void onRewarded(String type, int amount);
} 