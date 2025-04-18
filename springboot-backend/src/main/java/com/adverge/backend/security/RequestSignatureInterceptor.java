package com.adverge.backend.security;

import com.adverge.backend.service.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestSignatureInterceptor implements HandlerInterceptor {
    
    private final SecurityService securityService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String timestampHeader = request.getHeader("X-Timestamp");
        String signatureHeader = request.getHeader("X-Signature");
        
        // 开发环境可以暂时禁用签名验证
        if (System.getenv("DISABLE_SIGNATURE_CHECK") != null) {
            log.warn("签名验证已禁用");
            return true;
        }
        
        // 检查是否提供了必要的头信息
        if (timestampHeader == null || signatureHeader == null) {
            log.warn("请求缺少必要的头信息: timestamp={}, signature={}", timestampHeader, signatureHeader);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        
        try {
            // 解析时间戳
            long timestamp = Long.parseLong(timestampHeader);
            long currentTime = System.currentTimeMillis();
            long timeDiff = Math.abs(currentTime - timestamp);
            
            // 检查时间戳是否在有效范围内（5分钟）
            if (timeDiff > 300000) {
                log.warn("请求时间戳过期: timestamp={}, currentTime={}, diff={}ms", timestamp, currentTime, timeDiff);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            
            // 验证签名
            boolean isValid = securityService.verifyRequestSignature(request, timestamp, signatureHeader);
            if (!isValid) {
                log.warn("请求签名无效: timestamp={}, signature={}", timestamp, signatureHeader);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            
            // 签名验证通过
            return true;
            
        } catch (Exception e) {
            log.error("验证请求签名时出错", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return false;
        }
    }
} 