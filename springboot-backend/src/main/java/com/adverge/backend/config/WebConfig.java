package com.adverge.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${ad.cors.allowed-origins}")
    private String allowedOrigins;
    
    @Value("${ad.cors.allowed-methods}")
    private String allowedMethods;
    
    @Value("${ad.cors.allowed-headers}")
    private String allowedHeaders;
    
    @Value("${ad.cors.exposed-headers}")
    private String exposedHeaders;

    /**
     * 配置CORS跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders(allowedHeaders.split(","))
                .exposedHeaders(exposedHeaders.split(","))
                .allowCredentials(true)
                .maxAge(3600);
    }
} 