package com.adverge.sdk.bidding;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class BiddingFallbackManager {
    private static final String TAG = "BiddingFallbackManager";
    
    private Map<String, BidResponse> lastSuccessfulBids;
    private BiddingCacheManager cacheManager;
    
    public BiddingFallbackManager(BiddingCacheManager cacheManager) {
        this.cacheManager = cacheManager;
        this.lastSuccessfulBids = new HashMap<>();
    }
    
    // 处理竞价失败
    public BidResponse handleBiddingFailure(BiddingException e, String adUnitId) {
        Log.e(TAG, "Bidding failed for adUnitId: " + adUnitId, e);
        
        // 根据错误类型选择不同的降级策略
        switch (e.getErrorType()) {
            case TIMEOUT:
                return useCachedBid(adUnitId);
            case NETWORK_ERROR:
                return useDefaultBid(adUnitId);
            case INVALID_RESPONSE:
                return retryWithBackupPlatform(adUnitId);
            default:
                return useLastSuccessfulBid(adUnitId);
        }
    }
    
    // 使用缓存的竞价
    private BidResponse useCachedBid(String adUnitId) {
        BidResponse cachedBid = cacheManager.getCachedBid(adUnitId);
        if (cachedBid != null) {
            Log.d(TAG, "Using cached bid for adUnitId: " + adUnitId);
            return cachedBid;
        }
        return useDefaultBid(adUnitId);
    }
    
    // 使用默认竞价
    private BidResponse useDefaultBid(String adUnitId) {
        // 这里可以根据广告单元ID设置不同的默认值
        double defaultBid = 0.1;
        String defaultPlatform = "default";
        Log.d(TAG, "Using default bid for adUnitId: " + adUnitId);
        return new BidResponse(defaultBid, defaultPlatform);
    }
    
    // 使用备用平台重试
    private BidResponse retryWithBackupPlatform(String adUnitId) {
        // 这里可以实现备用平台的逻辑
        // 简化实现，实际应该尝试其他平台
        return useDefaultBid(adUnitId);
    }
    
    // 使用最后一次成功的竞价
    private BidResponse useLastSuccessfulBid(String adUnitId) {
        BidResponse lastBid = lastSuccessfulBids.get(adUnitId);
        if (lastBid != null && !lastBid.isExpired()) {
            Log.d(TAG, "Using last successful bid for adUnitId: " + adUnitId);
            return lastBid;
        }
        return useDefaultBid(adUnitId);
    }
    
    // 记录成功的竞价
    public void recordSuccessfulBid(String adUnitId, BidResponse bid) {
        lastSuccessfulBids.put(adUnitId, bid);
        Log.d(TAG, "Recorded successful bid for adUnitId: " + adUnitId);
    }
    
    // 清除过期的记录
    public void clearExpiredRecords() {
        for (Map.Entry<String, BidResponse> entry : lastSuccessfulBids.entrySet()) {
            if (entry.getValue().isExpired()) {
                lastSuccessfulBids.remove(entry.getKey());
                Log.d(TAG, "Cleared expired record for adUnitId: " + entry.getKey());
            }
        }
    }
} 