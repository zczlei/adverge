package com.adverge.backend.dto;

import com.adverge.backend.model.AdUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 广告位响应数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdUnitResponse {

    /**
     * 广告位ID
     */
    private String id;

    /**
     * 广告位名称
     */
    private String name;

    /**
     * 所属应用ID
     */
    private String appId;

    /**
     * 所属应用名称
     */
    private String appName;

    /**
     * 广告位类型
     */
    private String type;

    /**
     * 广告位描述
     */
    private String description;

    /**
     * 是否启用
     */
    private boolean active;

    /**
     * 底价
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
     * 广告位尺寸
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

    /**
     * 从AdUnit实体转换为AdUnitResponse对象
     *
     * @param adUnit AdUnit实体
     * @param appName 应用名称
     * @return AdUnitResponse对象
     */
    public static AdUnitResponse fromAdUnit(AdUnit adUnit, String appName) {
        return AdUnitResponse.builder()
                .id(adUnit.getId())
                .name(adUnit.getName())
                .appId(adUnit.getAppId())
                .appName(appName)
                .type(adUnit.getType())
                .description(adUnit.getDescription())
                .active(adUnit.isActive())
                .floorPrice(adUnit.getFloorPrice())
                .refreshInterval(adUnit.getRefreshInterval())
                .position(adUnit.getPosition())
                .size(adUnit.getSize())
                .createdAt(adUnit.getCreatedAt())
                .updatedAt(adUnit.getUpdatedAt())
                .build();
    }
} 