package com.adverge.backend.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(indexes = {
    @Index(name = "platform_timestamp", columnList = "platform,timestamp"),
    @Index(name = "placementId_timestamp", columnList = "placementId,timestamp")
})
public class Metrics {
    
    @Id
    private String id;
    
    @Column(nullable = false)
    private String placementId;
    
    private String platform;
    
    private String adId;
    
    private String adUnitId;
    
    private long bids;
    
    private long wins;
    
    private long impressions;
    
    private long clicks;
    
    private BigDecimal revenue;
    
    private Double price;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastBidTime;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastWinTime;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastImpressionTime;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastClickTime;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date createdAt;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;
    
    /**
     * 获取价格
     * @return 价格
     */
    public Double getPrice() {
        return price;
    }
    
    /**
     * 设置价格
     * @param price 价格
     */
    public void setPrice(Double price) {
        this.price = price;
    }
    
    /**
     * 设置价格（double类型）
     * @param price 价格
     */
    public void setPrice(double price) {
        this.price = price;
    }
    
    /**
     * 获取时间戳
     * @return 时间戳
     */
    public Date getTimestamp() {
        return timestamp;
    }
    
    /**
     * 设置时间戳
     * @param timestamp 时间戳
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
} 