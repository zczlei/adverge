package com.adverge.backend.model;

import lombok.Data;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 配置实体类，包含各平台配置信息
 */
@Data
@Entity
public class Config {
    
    @Id
    private String id;
    
    /**
     * 应用ID
     */
    @Column(unique = true)
    private String appId;
    
    /**
     * 竞价超时时间（毫秒）
     */
    private int bidTimeout = 5000;
    
    /**
     * 缓存过期时间（秒）
     */
    private int cacheExpiry = 300;
    
    /**
     * 平台配置列表
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "config_id")
    private List<Platform> platforms = new ArrayList<>();
    
    /**
     * 创建时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
    
    /**
     * 更新时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date();
    
    /**
     * 更新时间戳
     */
    public void updateTimestamp() {
        this.updatedAt = new Date();
    }
    
    /**
     * 平台配置信息类
     */
    @Data
    @Entity
    public static class Platform {
        
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        /**
         * 平台名称
         */
        private String name;
        
        /**
         * 应用ID（平台特定）
         */
        private String appId;
        
        /**
         * 应用密钥
         */
        private String appKey;
        
        /**
         * 广告位ID
         */
        private String placementId;
        
        /**
         * 最低出价
         */
        private double bidFloor = 0.0;
        
        /**
         * 是否启用
         */
        private boolean enabled = true;
    }
} 