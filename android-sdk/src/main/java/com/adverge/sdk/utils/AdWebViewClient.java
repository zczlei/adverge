package com.adverge.sdk.utils;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.adverge.sdk.utils.Logger;

/**
 * 广告WebView客户端
 */
public class AdWebViewClient extends WebViewClient {
    private static final String TAG = "AdWebViewClient";
    
    private final AdWebViewListener listener;
    
    public AdWebViewClient(AdWebViewListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        Logger.d(TAG, "Page started loading: " + url);
        if (listener != null) {
            listener.onPageStarted(url);
        }
    }
    
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        Logger.d(TAG, "Page finished loading: " + url);
        if (listener != null) {
            listener.onPageFinished(url);
        }
    }
    
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        String url = request.getUrl().toString();
        Logger.e(TAG, "Page load error: " + url + ", error: " + error.getDescription());
        if (listener != null) {
            listener.onPageError(url, error.getDescription().toString());
        }
    }
    
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        String url = request.getUrl().toString();
        Logger.e(TAG, "HTTP error: " + url + ", status: " + errorResponse.getStatusCode());
        if (listener != null) {
            listener.onHttpError(url, errorResponse.getStatusCode());
        }
    }
    
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
        Logger.e(TAG, "SSL error: " + error.getUrl() + ", primary error: " + error.getPrimaryError());
        if (listener != null) {
            listener.onSslError(error.getUrl(), error.getPrimaryError());
        }
    }
    
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        Logger.d(TAG, "Should override URL loading: " + url);
        
        // 处理特殊URL
        if (url.startsWith("market://") || url.startsWith("https://play.google.com")) {
            // 处理应用市场链接
            if (listener != null) {
                listener.onMarketUrl(url);
            }
            return true;
        }
        
        // 处理其他URL
        if (listener != null) {
            return listener.shouldOverrideUrlLoading(url);
        }
        
        return super.shouldOverrideUrlLoading(view, request);
    }
    
    public interface AdWebViewListener {
        void onPageStarted(String url);
        void onPageFinished(String url);
        void onPageError(String url, String error);
        void onHttpError(String url, int statusCode);
        void onSslError(String url, int primaryError);
        void onMarketUrl(String url);
        boolean shouldOverrideUrlLoading(String url);
    }
} 