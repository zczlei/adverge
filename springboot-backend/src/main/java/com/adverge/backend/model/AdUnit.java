package com.adverge.backend.model;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ForeignKey;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 广告单元实体类
 */
@Data
@Entity
public class AdUnit {
    
    @Id
    private String id;
    
    /**
     * 广告单元名称
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * 关联的应用ID
     */
    @Column(nullable = false)
    private String appId;
    
    /**
     * 广告类型（横幅、插屏、激励视频等）
     */
    @Column(nullable = false)
    private String type;
    
    /**
     * 广告单元说明
     */
    private String description;
    
    /**
     * 是否启用
     */
    private boolean active = true;
    
    /**
     * 最低出价
     */
    private BigDecimal floorPrice;
    
    /**
     * 刷新间隔（秒）
     */
    private Integer refreshInterval;
    
    /**
     * 广告位位置
     */
    private String position;
    
    /**
     * 广告尺寸
     */
    private String size;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 