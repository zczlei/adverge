package com.adverge.backend.service.impl;

import com.adverge.backend.service.SecurityService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class SecurityServiceImpl implements SecurityService {

    @Value("${ad.security.jwt.secret}")
    private String secretKey;

    @Override
    public String generateRequestSignature(HttpServletRequest request, long timestamp) {
        SortedMap<String, String> parameters = collectParameters(request);
        parameters.put("timestamp", String.valueOf(timestamp));
        
        // 构建签名字符串
        StringBuilder builder = new StringBuilder();
        for (String key : parameters.keySet()) {
            builder.append(key).append('=').append(parameters.get(key)).append('&');
        }
        
        // 添加密钥
        builder.append("key=").append(secretKey);
        
        // 计算SHA-256哈希
        return calculateSHA256(builder.toString());
    }

    @Override
    public boolean verifyRequestSignature(HttpServletRequest request, long timestamp, String signature) {
        // 生成签名并比较
        String calculatedSignature = generateRequestSignature(request, timestamp);
        return StringUtils.equals(calculatedSignature, signature);
    }
    
    private SortedMap<String, String> collectParameters(HttpServletRequest request) {
        SortedMap<String, String> parameters = new TreeMap<>();
        
        // 收集请求参数
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            parameters.put(name, request.getParameter(name));
        }
        
        // 添加请求路径
        parameters.put("path", request.getRequestURI());
        
        return parameters;
    }
    
    private String calculateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无法计算SHA-256哈希", e);
        }
    }
} 