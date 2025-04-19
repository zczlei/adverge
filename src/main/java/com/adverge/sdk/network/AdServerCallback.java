package com.adverge.sdk.network;

import com.adverge.sdk.model.AdResponse;

/**
 * 广告服务器回调接口
 */
public interface AdServerCallback {
    
    /**
     * 当广告响应成功时调用
     * @param response 广告响应
     */
    void onSuccess(AdResponse response);
    
    /**
     * 当广告请求出错时调用
     * @param error 错误信息
     */
    void onError(String error);
} 