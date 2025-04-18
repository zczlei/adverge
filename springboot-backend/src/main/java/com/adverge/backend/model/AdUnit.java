package com.adverge.backend.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 广告位实体类
 */
@Data
@Document(collection = "adUnits")
@CompoundIndex(name = "app_name", def = "{'appId': 1, 'name': 1}", unique = true)
public class AdUnit {
    
    @Id
    private String id;
    
    private String name;
    
    @Indexed
    private String appId;
    
    private String type; // banner, interstitial, rewarded, native
    
    private String description;
    
    private boolean active = true;
    
    private BigDecimal floorPrice;
    
    private Integer refreshInterval;
    
    private String position;
    
    private String size;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
} 