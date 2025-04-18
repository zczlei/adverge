package com.adverge.backend.service;

import com.adverge.backend.dto.AdRequest;
import com.adverge.backend.dto.BidResponse;
import com.adverge.backend.dto.TrackRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface AdService {
    
    /**
     * 获取广告
     * @param adUnitId 广告单元ID
     * @param options 选项参数
     * @param request HTTP请求
     * @return 广告信息
     */
    BidResponse getAd(String adUnitId, Map<String, String> options, HttpServletRequest request);
    
    /**
     * 竞价请求
     * @param adUnitId 广告单元ID
     * @param adRequest 广告请求数据
     * @param request HTTP请求
     * @return 竞价结果
     */
    BidResponse bid(String adUnitId, AdRequest adRequest, HttpServletRequest request);
    
    /**
     * 记录广告展示
     * @param adId 广告ID
     * @param platform 平台名称
     * @param request HTTP请求
     */
    void trackImpression(String adId, String platform, HttpServletRequest request);
    
    /**
     * 记录广告点击
     * @param adId 广告ID
     * @param trackRequest 跟踪请求数据
     * @param request HTTP请求
     */
    void trackClick(String adId, TrackRequest trackRequest, HttpServletRequest request);
} 