package com.adverge.backend.service.impl;

import com.adverge.backend.dto.AdRequest;
import com.adverge.backend.dto.BidResponse;
import com.adverge.backend.service.AdNetworkManager;
import com.adverge.backend.service.AdNetworkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 广告网络管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdNetworkManagerImpl implements AdNetworkManager {
    
    private final List<AdNetworkService> adNetworks;
    
    @Value("${ad.bid.timeout:5000}")
    private int bidTimeout;
    
    private final Map<String, AdNetworkService> networkMap = new ConcurrentHashMap<>();
    
    @Override
    public List<AdNetworkService> getAvailableNetworks() {
        return adNetworks;
    }
    
    @Override
    public CompletableFuture<List<BidResponse>> bid(AdRequest adRequest) {
        log.debug("向所有广告平台发送竞价请求");
        
        // 初始化网络映射
        initNetworkMap();
        
        // 并行向所有广告平台发送竞价请求
        List<CompletableFuture<BidResponse>> bidFutures = adNetworks.stream()
                .map(network -> network.bid(adRequest))
                .collect(Collectors.toList());
        
        // 等待所有响应或超时
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                bidFutures.toArray(new CompletableFuture[0])
        );
        
        try {
            allFutures.get(bidTimeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("部分广告平台竞价请求超时", e);
        }
        
        // 收集所有完成的响应
        return CompletableFuture.supplyAsync(() -> 
            bidFutures.stream()
                    .map(future -> {
                        try {
                            return future.isDone() ? future.get() : null;
                        } catch (Exception e) {
                            log.error("获取竞价响应失败", e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
        );
    }
    
    @Override
    public CompletableFuture<Boolean> notifyWin(String network, String bidToken) {
        log.debug("通知{}平台竞价胜出: {}", network, bidToken);
        
        // 初始化网络映射
        initNetworkMap();
        
        AdNetworkService adNetwork = networkMap.get(network.toLowerCase());
        if (adNetwork == null) {
            log.warn("未找到广告平台: {}", network);
            return CompletableFuture.completedFuture(false);
        }
        
        return adNetwork.notifyWin(bidToken);
    }
    
    /**
     * 初始化网络映射
     */
    private void initNetworkMap() {
        if (networkMap.isEmpty()) {
            synchronized (networkMap) {
                if (networkMap.isEmpty()) {
                    adNetworks.forEach(network -> 
                            networkMap.put(network.getPlatformName().toLowerCase(), network));
                }
            }
        }
    }
} 