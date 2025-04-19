package com.adverge.sdk.adapter;

import android.content.Context;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;

/**
 * 广告平台适配器接口
 * 所有广告平台SDK都需要实现此接口
 */
public interface AdPlatformAdapter {
    
    /**
     * 初始化适配器
     * @param context 上下文
     * @param config 配置信息
     */
    void init(Context context, Object config);
    
    /**
     * 获取广告竞价
     * @param request 广告请求
     * @return 广告响应
     */
    AdResponse getBid(AdRequest request);
    
    /**
     * 加载广告
     * @param adUnitId 广告单元ID
     * @param listener 广告监听器
     */
    void loadAd(String adUnitId, AdListener listener);
    
    /**
     * 展示广告
     */
    void showAd();
    
    /**
     * 销毁适配器
     */
    void destroy();
    
    /**
     * 获取平台名称
     * @return 平台名称
     */
    String getPlatformName();
    
    /**
     * 获取历史eCPM
     * @return eCPM值
     */
    double getHistoricalEcpm();
} 