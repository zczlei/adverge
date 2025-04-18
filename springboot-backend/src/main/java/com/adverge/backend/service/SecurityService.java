package com.adverge.backend.service;

import javax.servlet.http.HttpServletRequest;

public interface SecurityService {
    
    /**
     * 生成请求签名
     * @param request HTTP请求
     * @param timestamp 时间戳
     * @return 签名字符串
     */
    String generateRequestSignature(HttpServletRequest request, long timestamp);
    
    /**
     * 验证请求签名
     * @param request HTTP请求
     * @param timestamp 时间戳
     * @param signature 签名字符串
     * @return 是否验证通过
     */
    boolean verifyRequestSignature(HttpServletRequest request, long timestamp, String signature);
} 