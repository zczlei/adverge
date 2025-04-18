package com.adverge.backend.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 数据源配置类
 * 禁用 MongoDB 自动配置，启用 JPA
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.adverge.backend.repository")
@EnableTransactionManagement
@AutoConfigureAfter({MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class DataSourceConfig {
    // 配置由 application.yml 完成
} 