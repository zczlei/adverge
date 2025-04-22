package com.adverge.sdk.utils;

import android.os.Handler;
import android.os.Looper;

import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.adverge.sdk.listener.AdListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 重试管理器
 * 用于处理广告请求失败时的重试逻辑
 */
public class RetryManager {
    private static final String TAG = "RetryManager";
    
    private final ScheduledExecutorService executorService;
    private final Handler mainHandler;
    private final int maxRetries;
    private final long initialDelay;
    private final long maxDelay;
    
    /**
     * 构造函数
     * @param maxRetries 最大重试次数
     * @param initialDelay 初始延迟时间（毫秒）
     * @param maxDelay 最大延迟时间（毫秒）
     */
    public RetryManager(int maxRetries, long initialDelay, long maxDelay) {
        this.executorService = Executors.newScheduledThreadPool(1);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.maxRetries = maxRetries;
        this.initialDelay = initialDelay;
        this.maxDelay = maxDelay;
    }
    
    /**
     * 构造函数（使用默认参数）
     */
    public RetryManager() {
        this(3, 1000, 10000); // 默认最多重试3次，初始延迟1秒，最大延迟10秒
    }
    
    /**
     * 销毁管理器
     */
    public void destroy() {
        executorService.shutdown();
    }
    
    /**
     * 执行带重试的任务
     * @param task 任务
     */
    public void executeWithRetry(Runnable task) {
        executeWithRetry(task, 0);
    }
    
    /**
     * 使用指定的重试次数执行任务
     * @param task 任务
     * @param retryCount 当前重试次数
     */
    private void executeWithRetry(Runnable task, int retryCount) {
        try {
            task.run();
        } catch (Exception e) {
            Logger.e(TAG, "Task execution failed: " + e.getMessage());
            if (retryCount < maxRetries) {
                long retryDelay = calculateRetryDelay(retryCount);
                Logger.d(TAG, "Retrying in " + retryDelay + "ms. Retry: " + (retryCount + 1) + "/" + maxRetries);
                
                executorService.schedule(() -> {
                    mainHandler.post(() -> executeWithRetry(task, retryCount + 1));
                }, retryDelay, TimeUnit.MILLISECONDS);
            }
        }
    }
    
    /**
     * 计算重试延迟
     * @param retryCount 重试次数
     * @return 延迟时间
     */
    private long calculateRetryDelay(int retryCount) {
        // 指数退避算法: delay = initialDelay * 2^retryCount
        long delay = initialDelay * (long) Math.pow(2, retryCount);
        return Math.min(delay, maxDelay);
    }
    
    /**
     * 重试任务类
     */
    public class RetryTask implements Runnable {
        private final AdRequest request;
        private final AdListener listener;
        private int retryCount = 0;
        
        public RetryTask(AdRequest request, AdListener listener) {
            this.request = request;
            this.listener = listener;
        }
        
        @Override
        public void run() {
            try {
                // 模拟请求广告
                if (Math.random() > 0.7) { // 模拟30%的失败率
                    // 请求成功
                    AdResponse response = createMockResponse(request);
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onAdLoaded(response);
                        }
                    });
                } else {
                    // 请求失败，进行重试
                    if (retryCount < maxRetries) {
                        retryCount++;
                        long retryDelay = calculateRetryDelay(retryCount - 1);
                        Logger.d(TAG, "Ad request failed. Retrying in " + retryDelay + "ms. Retry: " + retryCount + "/" + maxRetries);
                        
                        executorService.schedule(this, retryDelay, TimeUnit.MILLISECONDS);
                    } else {
                        // 超过最大重试次数
                        String error = "Max retry attempts reached";
                        mainHandler.post(() -> {
                            if (listener != null) {
                                listener.onAdLoadFailed(error);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                String errorMessage = e.getMessage();
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onAdLoadFailed(errorMessage);
                    }
                });
            }
        }
        
        private AdResponse createMockResponse(AdRequest request) {
            // 创建模拟响应，用于测试
            return new AdResponse.Builder()
                .setId("mock_ad_id_" + System.currentTimeMillis())
                .setAdUnitId(request.getAdUnitId())
                .setPlatform("mock_platform")
                .setCreativeType("mock_creative")
                .setEcpm(1.5)
                .build();
        }
    }
} 