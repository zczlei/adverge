package com.adverge.sdk.network;

import com.adverge.sdk.model.Platform;
import java.util.List;

/**
 * 平台配置回调接口
 */
public interface PlatformCallback {
    /**
     * 请求成功回调
     * @param platforms 平台列表
     */
    void onSuccess(List<Platform> platforms);

    /**
     * 请求失败回调
     * @param error 错误信息
     */
    void onError(String error);
} 