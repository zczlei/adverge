package com.adverge.backend.service.impl;

import com.adverge.backend.dto.AdRequest;
import com.adverge.backend.dto.BidResponse;
import com.adverge.backend.service.AdNetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 广告平台服务抽象基类
 */
@Slf4j
public abstract class AbstractAdNetworkService implements AdNetworkService {

    protected final RestTemplate restTemplate;
    
    protected String apiUrl;
    protected String appId;
    protected String appKey;
    protected String placementId;
    protected double bidFloor;

    public AbstractAdNetworkService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public double getBidFloor() {
        return bidFloor;
    }

    /**
     * 构建HTTP请求头
     * @return HTTP请求头
     */
    protected HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * 构建竞价请求体
     * @param adRequest 广告请求
     * @return 竞价请求体
     */
    protected abstract Object buildBidRequest(AdRequest adRequest);

    /**
     * 解析竞价响应
     * @param response 响应数据
     * @return 竞价响应
     */
    protected abstract BidResponse parseBidResponse(Object response);

    /**
     * 生成唯一竞价ID
     * @return 竞价ID
     */
    protected String generateBidId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 处理请求异常
     * @param e 异常
     * @param operation 操作名称
     */
    protected void handleException(Exception e, String operation) {
        log.error("{}平台{}操作失败: {}", getPlatformName(), operation, e.getMessage());
    }
} 