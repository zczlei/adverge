package com.adverge.sdk.listener;

/**
 * 广告监听器接口
 */
public interface AdListener {
    /**
     * 广告加载成功
     */
    void onAdLoaded();

    /**
     * 广告加载失败
     */
    void onAdFailedToLoad(String error);

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
     * 广告奖励
     */
    void onAdRewarded();
} 