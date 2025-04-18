package com.adverge.sdk.network;

import android.content.Context;
import android.util.Log;

import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.adverge.sdk.model.BidRequest;
import com.adverge.sdk.model.BidResponse;
import com.adverge.sdk.model.Platform;
import com.adverge.sdk.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * 广告服务器网络客户端接口
 */
public interface AdServerClient {

    /**
     * 获取AdServerClient实例
     * @param context 上下文
     * @return AdServerClient实例
     */
    static AdServerClient getInstance(Context context) {
        return AdServerClientImpl.getInstance(context);
    }

    /**
     * 请求广告
     * @param request 广告请求
     * @param callback 回调
     */
    void requestAd(AdRequest request, AdServerCallback callback);

    /**
     * 记录广告展示
     * @param adUnitId 广告单元ID
     * @param platform 平台
     */
    void trackImpression(String adUnitId, String platform);

    /**
     * 记录广告点击
     * @param adUnitId 广告单元ID
     * @param platform 平台
     */
    void trackClick(String adUnitId, String platform);

    /**
     * 获取平台列表
     * @param callback 回调
     */
    void getPlatforms(PlatformCallback callback);

    /**
     * 平台回调接口
     */
    interface PlatformCallback {
        void onSuccess(List<Platform> platforms);
        void onError(String error);
    }
} 