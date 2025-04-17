package com.adverge.sdk.bidding;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class BiddingCacheManager {
    private static final String TAG = "BiddingCacheManager";
    private static final long CACHE_TTL = 5 * 60 * 1000; // 5分钟
    
    private Map<String, BidResponse> bidCache;
    private Map<String, Long> cacheTimestamps;
    
    public BiddingCacheManager() {
        bidCache = new HashMap<>();
        cacheTimestamps = new HashMap<>();
    }
    
    // 获取缓存的竞价结果
    public BidResponse getCachedBid(String adUnitId) {
        BidResponse cachedBid = bidCache.get(adUnitId);
        if (cachedBid != null && !isCacheExpired(adUnitId)) {
            Log.d(TAG, "Using cached bid for adUnitId: " + adUnitId);
            return cachedBid;
        }
        return null;
    }
    
    // 缓存竞价结果
    public void cacheBid(String adUnitId, BidResponse bid) {
        bidCache.put(adUnitId, bid);
        cacheTimestamps.put(adUnitId, System.currentTimeMillis());
        Log.d(TAG, "Cached bid for adUnitId: " + adUnitId);
    }
    
    // 检查缓存是否过期
    private boolean isCacheExpired(String adUnitId) {
        Long timestamp = cacheTimestamps.get(adUnitId);
        if (timestamp == null) {
            return true;
        }
        return System.currentTimeMillis() - timestamp > CACHE_TTL;
    }
    
    // 清除过期缓存
    public void clearExpiredCache() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : cacheTimestamps.entrySet()) {
            if (currentTime - entry.getValue() > CACHE_TTL) {
                String adUnitId = entry.getKey();
                bidCache.remove(adUnitId);
                cacheTimestamps.remove(adUnitId);
                Log.d(TAG, "Cleared expired cache for adUnitId: " + adUnitId);
            }
        }
    }
    
    // 清除所有缓存
    public void clearAllCache() {
        bidCache.clear();
        cacheTimestamps.clear();
        Log.d(TAG, "Cleared all cache");
    }
    
    // 获取缓存大小
    public int getCacheSize() {
        return bidCache.size();
    }
} 