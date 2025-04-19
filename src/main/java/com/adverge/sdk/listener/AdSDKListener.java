package com.adverge.sdk.listener;

/**
 * SDK 监听器接口
 */
public interface AdSDKListener {
    /**
     * SDK 初始化完成
     */
    void onInitialized();

    /**
     * SDK 初始化失败
     */
    void onInitializationFailed(String error);

    /**
     * SDK 释放完成
     */
    void onReleased();
} 