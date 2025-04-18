package com.adverge.sdk.network;

import android.content.Context;
import android.util.Log;

import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.model.AdResponse;
import com.adverge.sdk.model.BidRequest;
import com.adverge.sdk.model.BidResponse;
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

public interface AdServerClient {
    @POST("/api/v1/bid")
    Call<BidResponse> requestBid(@Body BidRequest request);

    @POST("/api/v1/ad")
    Call<AdResponse> requestAd(@Body AdRequest request);

    void trackImpression(String adUnitId, String platform);
    void trackClick(String adUnitId, String platform);

    void getPlatforms(PlatformCallback callback);
    void savePlatform(Platform platform, PlatformCallback callback);
    void enablePlatform(String name, PlatformCallback callback);
    void disablePlatform(String name, PlatformCallback callback);

    interface PlatformCallback {
        void onSuccess(List<Platform> platforms);
        void onError(String error);
    }
} 