package com.adverge.backend.service;

import com.adverge.backend.dto.AdRequest;
import com.adverge.backend.dto.BidResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 广告网络管理服务接口
 */
public interface AdNetworkManager {
    
    /**
     * 获取所有可用的广告平台
     * @return 广告平台列表
     */
    List<AdNetworkService> getAvailableNetworks();
    
    /**
     * 向所有广告平台发送竞价请求
     * @param adRequest 广告请求
     * @return 竞价响应列表的Future
     */
    CompletableFuture<List<BidResponse>> bid(AdRequest adRequest);
    
    /**
     * 通知广告平台竞价胜出
     * @param network 广告平台名称
     * @param bidToken 竞价标识
     * @return 是否通知成功
     */
    CompletableFuture<Boolean> notifyWin(String network, String bidToken);
} 