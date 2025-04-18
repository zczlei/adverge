package com.adverge.backend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.adverge.backend.service.AdNetworkManager;
import com.adverge.backend.service.impl.AdNetworkManagerImpl;
import com.adverge.backend.service.impl.DummyAdNetworkManagerImpl;

@Configuration
public class AdNetworkConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "ad.network.enabled", havingValue = "true", matchIfMissing = true)
    public AdNetworkManager realAdNetworkManager(AdNetworkManagerImpl adNetworkManager) {
        return adNetworkManager;
    }

    @Bean
    @ConditionalOnProperty(name = "ad.network.enabled", havingValue = "false")
    public AdNetworkManager dummyAdNetworkManager() {
        return new DummyAdNetworkManagerImpl();
    }
} 