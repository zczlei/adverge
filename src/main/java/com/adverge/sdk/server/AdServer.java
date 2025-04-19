package com.adverge.sdk.server;

import android.content.Context;
import android.util.Log;

import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.BidResponse;
import com.adverge.sdk.network.AdNetworkClient;
import com.adverge.sdk.network.AdServerCallback;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdServer {
    private static final String TAG = "AdServer";
    private static AdServer instance;
    private Map<String, AdNetworkClient> networkClients = new HashMap<>();
    private Context context;

    private AdServer(Context context) {
        this.context = context;
        initNetworkClients();
    }

    public static AdServer getInstance(Context context) {
        if (instance == null) {
            instance = new AdServer(context);
        }
        return instance;
    }

    private void initNetworkClients() {
        // 初始化各广告网络的客户端
        networkClients.put("ironsource", new AdNetworkClient("ironsource"));
        networkClients.put("mintegral", new AdNetworkClient("mintegral"));
        networkClients.put("pangle", new AdNetworkClient("pangle"));
        networkClients.put("applovin", new AdNetworkClient("applovin"));
        networkClients.put("unity", new AdNetworkClient("unity"));
        networkClients.put("mahimeta", new AdNetworkClient("mahimeta"));
    }

    public void handleAdRequest(AdRequest request, AdServerCallback callback) {
        Log.d(TAG, "开始处理广告请求: " + request.getAdUnitId());
        
        // 2. 向支持bidding的AN服务器发起询价请求
        List<BidResponse> bidResponses = new ArrayList<>();
        for (AdNetworkClient client : networkClients.values()) {
            if (client.supportsBidding()) {
                BidResponse response = client.requestBid(request);
                if (response != null) {
                    bidResponses.add(response);
                    Log.d(TAG, "收到竞价响应: " + response.getPlatform() + 
                        ", eCPM: " + response.getEcpm());
                }
            }
        }

        // 4. 进行竞价胜出判断
        BidResponse winningBid = determineWinningBid(bidResponses);
        
        if (winningBid != null) {
            // 5. 向胜出AN反馈响应参数
            notifyWinningNetwork(winningBid);
            Log.d(TAG, "竞价胜出平台: " + winningBid.getPlatform() + 
                ", eCPM: " + winningBid.getEcpm());
            
            // 6. 通知客户端胜出AN
            callback.onBidResponse(winningBid);
        } else {
            Log.e(TAG, "无有效竞价响应");
            callback.onError("No valid bid response");
        }
    }

    private BidResponse determineWinningBid(List<BidResponse> responses) {
        // 根据eCPM选择最高出价
        return responses.stream()
            .max(Comparator.comparingDouble(BidResponse::getEcpm))
            .orElse(null);
    }

    private void notifyWinningNetwork(BidResponse winningBid) {
        AdNetworkClient client = networkClients.get(winningBid.getPlatform());
        if (client != null) {
            client.notifyWin(winningBid.getBidToken());
        }
    }
} 