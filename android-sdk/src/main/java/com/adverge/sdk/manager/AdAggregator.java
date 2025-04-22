package com.adverge.sdk.manager;

import android.content.Context;
import android.util.Log;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.platform.AdPlatformAdapter;
import com.adverge.sdk.model.AdResponse;
import com.adverge.sdk.network.AdCallback;
import com.adverge.sdk.listener.AdListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 广告聚合管理器
 * 管理多个广告平台的适配器，实现Header Bidding和Waterfall
 */
public class AdAggregator {
    private static final String TAG = "AdAggregator";
    private static AdAggregator instance;
    private final Context context;
    private final List<AdPlatformAdapter> adapters;
    private final ExecutorService executorService;
    private boolean isInitialized = false;

    private AdAggregator(Context context) {
        this.context = context;
        adapters = new ArrayList<>();
        executorService = Executors.newCachedThreadPool();
    }

    public static synchronized AdAggregator getInstance(Context context) {
        if (instance == null) {
            instance = new AdAggregator(context);
        }
        return instance;
    }

    /**
     * 初始化聚合器
     * @param context 上下文
     * @param configs 各平台配置
     */
    public void init(Context context, List<Object> configs) {
        if (isInitialized) {
            return;
        }

        // 初始化所有适配器
        for (Object config : configs) {
            try {
                AdPlatformAdapter adapter = createAdapter(config);
                if (adapter != null) {
                    adapter.init(context, config);
                    adapters.add(adapter);
                }
            } catch (Exception e) {
                Log.e(TAG, "初始化适配器失败: " + e.getMessage());
            }
        }

        // 按历史eCPM排序
        sortAdaptersByEcpm();
        isInitialized = true;
    }

    /**
     * 请求广告
     * @param request 广告请求
     * @param strategy 策略（headerBidding/waterfall）
     * @param listener 广告监听器
     */
    public void requestAd(AdRequest request, String strategy, AdListener listener) {
        if (!isInitialized) {
            listener.onAdLoadFailed("聚合器未初始化");
            return;
        }

        if ("headerBidding".equals(strategy)) {
            requestAdHeaderBidding(request, listener);
        } else if ("waterfall".equals(strategy)) {
            requestAdWaterfall(request, listener);
        } else {
            listener.onAdLoadFailed("无效的策略");
        }
    }

    /**
     * Header Bidding模式请求广告
     */
    private void requestAdHeaderBidding(AdRequest request, AdListener listener) {
        executorService.execute(() -> {
            try {
                List<AdResponse> bids = new ArrayList<>();
                for (AdPlatformAdapter adapter : adapters) {
                    try {
                        AdResponse bid = adapter.getBid(request);
                        if (bid != null) {
                            bids.add(bid);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, adapter.getPlatformName() + " 竞价失败: " + e.getMessage());
                    }
                }

                if (bids.isEmpty()) {
                    // 如果没有有效出价，切换到Waterfall
                    requestAdWaterfall(request, listener);
                    return;
                }

                // 选择最高出价
                AdResponse winningBid = Collections.max(bids, Comparator.comparingDouble(AdResponse::getEcpm));
                loadWinningAd(winningBid, listener);
            } catch (Exception e) {
                listener.onAdLoadFailed("Header Bidding失败: " + e.getMessage());
            }
        });
    }

    /**
     * Waterfall模式请求广告
     */
    private void requestAdWaterfall(AdRequest request, AdListener listener) {
        executorService.execute(() -> {
            for (AdPlatformAdapter adapter : adapters) {
                try {
                    AdResponse bid = adapter.getBid(request);
                    if (bid != null) {
                        loadWinningAd(bid, listener);
                        return;
                    }
                } catch (Exception e) {
                    Log.e(TAG, adapter.getPlatformName() + " 请求失败: " + e.getMessage());
                }
            }
            listener.onAdLoadFailed("无有效广告");
        });
    }

    /**
     * 加载获胜广告
     */
    private void loadWinningAd(AdResponse bid, AdListener listener) {
        for (AdPlatformAdapter adapter : adapters) {
            if (adapter.getPlatformName().equals(bid.getPlatformName())) {
                AdRequest request = new AdRequest();
                request.setAdUnitId(bid.getAdUnitId());
                adapter.loadAd(request, new AdPlatformAdapter.AdCallback() {
                    @Override
                    public void onSuccess(AdResponse response) {
                        if (listener != null) {
                            listener.onAdLoaded(response);
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        if (listener != null) {
                            listener.onAdLoadFailed(error);
                        }
                    }
                });
                return;
            }
        }
        listener.onAdLoadFailed("未找到对应的广告平台");
    }

    /**
     * 创建适配器实例
     */
    private AdPlatformAdapter createAdapter(Object config) {
        // TODO: 根据配置创建具体的适配器实例
        return null;
    }

    /**
     * 按历史eCPM排序适配器
     */
    private void sortAdaptersByEcpm() {
        Collections.sort(adapters, (a1, a2) -> 
            Double.compare(a2.getHistoricalEcpm(), a1.getHistoricalEcpm()));
    }

    /**
     * 销毁聚合器
     */
    public void destroy() {
        for (AdPlatformAdapter adapter : adapters) {
            adapter.destroy();
        }
        adapters.clear();
        executorService.shutdown();
        isInitialized = false;
    }

    public void preloadAd(String adUnitId, AdCallback listener) {
        AdRequest request = new AdRequest();
        request.setAdUnitId(adUnitId);
        
        for (AdPlatformAdapter adapter : adapters) {
            AdResponse bid = adapter.getBid(request);
            if (bid != null) {
                adapter.loadAd(request, new AdPlatformAdapter.AdCallback() {
                    @Override
                    public void onSuccess(AdResponse response) {
                        if (listener != null) {
                            listener.onSuccess(response);
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        if (listener != null) {
                            listener.onError(error);
                        }
                    }
                });
                break;
            }
        }
    }
} 