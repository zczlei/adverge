package com.adverge.sdk.utils;

import android.os.Handler;
import android.os.Looper;

import com.adverge.sdk.ad.AdRequest;
import com.adverge.sdk.ad.AdResponse;
import com.adverge.sdk.listener.AdListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 广告请求重试管理器
 */
public class RetryManager {
    private static final String TAG = "RetryManager";
    private static final int MAX_RETRY_COUNT = 3;
    private static final long INITIAL_RETRY_DELAY = 1000; // 1秒
    private static final long MAX_RETRY_DELAY = 10000; // 10秒
    
    private static RetryManager instance;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private final Map<String, RetryTask> retryTasks;
    
    private RetryManager() {
        this.executorService = Executors.newFixedThreadPool(3);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.retryTasks = new HashMap<>();
    }
    
    public static synchronized RetryManager getInstance() {
        if (instance == null) {
            instance = new RetryManager();
        }
        return instance;
    }
    
    /**
     * 执行带重试的广告请求
     * @param request 广告请求
     * @param listener 广告监听器
     */
    public void executeWithRetry(AdRequest request, AdListener listener) {
        String adUnitId = request.getAdUnitId();
        if (adUnitId == null || adUnitId.isEmpty()) {
            Logger.e(TAG, "Invalid ad unit ID");
            return;
        }
        
        // 创建重试任务
        RetryTask task = new RetryTask(request, listener);
        retryTasks.put(adUnitId, task);
        
        // 执行任务
        executorService.execute(task);
    }
    
    /**
     * 取消重试任务
     * @param adUnitId 广告单元ID
     */
    public void cancelRetry(String adUnitId) {
        RetryTask task = retryTasks.remove(adUnitId);
        if (task != null) {
            task.cancel();
        }
    }
    
    /**
     * 重试任务类
     */
    private class RetryTask implements Runnable {
        private final AdRequest request;
        private final AdListener listener;
        private int retryCount;
        private long retryDelay;
        private boolean isCancelled;
        
        RetryTask(AdRequest request, AdListener listener) {
            this.request = request;
            this.listener = listener;
            this.retryCount = 0;
            this.retryDelay = INITIAL_RETRY_DELAY;
            this.isCancelled = false;
        }
        
        @Override
        public void run() {
            if (isCancelled) {
                return;
            }
            
            try {
                // 模拟广告请求
                AdResponse response = simulateAdRequest(request);
                
                // 请求成功
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onAdLoaded(response);
                    }
                    retryTasks.remove(request.getAdUnitId());
                });
                
            } catch (Exception e) {
                Logger.e(TAG, "Ad request failed: " + request.getAdUnitId(), e);
                
                // 检查是否需要重试
                if (retryCount < MAX_RETRY_COUNT && !isCancelled) {
                    retryCount++;
                    retryDelay = Math.min(retryDelay * 2, MAX_RETRY_DELAY);
                    
                    // 延迟后重试
                    executorService.schedule(this, retryDelay, TimeUnit.MILLISECONDS);
                    
                } else {
                    // 重试次数达到上限或任务被取消
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onAdFailedToLoad(e.getMessage());
                        }
                        retryTasks.remove(request.getAdUnitId());
                    });
                }
            }
        }
        
        void cancel() {
            isCancelled = true;
        }
    }
    
    /**
     * 模拟广告请求
     */
    private AdResponse simulateAdRequest(AdRequest request) {
        // TODO: 实现实际的广告请求逻辑
        // 这里使用模拟数据
        return new AdResponse.Builder()
                .setAdUnitId(request.getAdUnitId())
                .setAdType(request.getAdType())
                .setAdContent("Mock ad content")
                .build();
    }
    
    /**
     * 销毁管理器
     */
    public void destroy() {
        executorService.shutdown();
        retryTasks.clear();
        instance = null;
    }
} 