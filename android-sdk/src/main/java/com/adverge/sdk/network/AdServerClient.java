package com.adverge.sdk.network;

import android.content.Context;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.adverge.sdk.model.Platform;
import com.adverge.sdk.network.AdServerClientImpl;
import java.util.List;
import java.util.Map;

/**
 * 广告服务器网络客户端接口
 */
public interface AdServerClient {

    /**
     * 获取AdServerClient实例
     * @param context 上下文
     * @return AdServerClient实例
     */
    static AdServerClient getInstance(Context context) {
        return AdServerClientImpl.getInstance(context);
    }

    /**
     * 初始化客户端
     * @param configs 配置信息
     */
    void init(Map<String, Object> configs);

    /**
     * 请求广告
     * @param request 广告请求
     * @param callback 回调
     */
    void requestAd(AdRequest request, AdCallback callback);

    /**
     * 记录广告展示
     * @param adUnitId 广告位ID
     * @param platform 平台
     */
    void trackImpression(String adUnitId, String platform);

    /**
     * 记录广告点击
     * @param adUnitId 广告位ID
     * @param platform 平台
     */
    void trackClick(String adUnitId, String platform);

    /**
     * 获取所有平台配置
     * @param callback 回调
     */
    void getPlatforms(PlatformCallback callback);

    /**
     * 保存平台配置
     * @param platform 平台配置
     * @param callback 回调
     */
    void savePlatform(Platform platform, PlatformCallback callback);

    /**
     * 启用平台
     * @param platformName 平台名称
     * @param callback 回调
     */
    void enablePlatform(String platformName, PlatformCallback callback);

    /**
     * 禁用平台
     * @param platformName 平台名称
     * @param callback 回调
     */
    void disablePlatform(String platformName, PlatformCallback callback);

    /**
     * 记录性能数据
     * @param adId 广告ID
     * @param event 事件
     * @param params 参数
     */
    void trackPerformance(String adId, String event, Map<String, Object> params);

    /**
     * 销毁客户端
     */
    void destroy();

    /**
     * 广告回调接口
     */
    interface AdCallback {
        void onSuccess(AdResponse response);
        void onError(String error);
    }

    /**
     * 平台配置回调接口
     */
    interface PlatformCallback {
        void onSuccess(List<Platform> platforms);
        void onError(String error);
    }
} 