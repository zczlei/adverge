package com.adverge.sdk.network;

import com.adverge.sdk.model.AdResponse;

/**
 * 广告回调接口
 */
public interface AdCallback {
    /**
     * 广告请求成功
     * @param response 广告响应
     */
    void onSuccess(AdResponse response);
    
    /**
     * 广告请求失败
     * @param error 错误信息
     */
    void onError(String error);
} 