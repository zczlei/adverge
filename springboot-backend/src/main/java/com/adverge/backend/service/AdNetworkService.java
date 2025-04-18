package com.adverge.backend.service;

import com.adverge.backend.dto.AdRequest;
import com.adverge.backend.dto.BidResponse;

import java.util.concurrent.CompletableFuture;

/**
 * 广告平台服务接口
 * 所有广告平台应实现此接口
 */
public interface AdNetworkService {
    
    /**
     * 发送竞价请求到广告平台
     * @param adRequest 广告请求信息
     * @return 竞价响应
     */
    CompletableFuture<BidResponse> bid(AdRequest adRequest);
    
    /**
     * 通知广告平台竞价胜出
     * @param bidToken 竞价标识
     * @return 是否通知成功
     */
    CompletableFuture<Boolean> notifyWin(String bidToken);
    
    /**
     * 获取平台名称
     * @return 平台名称
     */
    String getPlatformName();
    
    /**
     * 获取广告平台最低出价
     * @return 最低出价
     */
    double getBidFloor();
} 