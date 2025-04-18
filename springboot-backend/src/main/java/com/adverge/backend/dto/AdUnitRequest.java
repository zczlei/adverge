package com.adverge.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 广告位请求数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdUnitRequest {

    /**
     * 广告位名称
     */
    @NotBlank(message = "广告位名称不能为空")
    @Size(max = 100, message = "广告位名称不能超过100个字符")
    private String name;

    /**
     * 所属应用ID
     */
    @NotBlank(message = "应用ID不能为空")
    private String appId;

    /**
     * 广告位类型 (例如: banner, interstitial, rewarded, native)
     */
    @NotBlank(message = "广告位类型不能为空")
    private String type;

    /**
     * 广告位描述
     */
    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;

    /**
     * 是否启用
     */
    private boolean active = true;

    /**
     * 底价
     */
    @NotNull(message = "底价不能为空")
    @Positive(message = "底价必须大于0")
    private BigDecimal floorPrice;

    /**
     * 刷新间隔（秒）
     */
    private Integer refreshInterval;

    /**
     * 广告位位置
     */
    @Size(max = 100, message = "位置描述不能超过100个字符")
    private String position;

    /**
     * 广告位尺寸 (例如: "320x50", "300x250")
     */
    @Size(max = 50, message = "尺寸描述不能超过50个字符")
    private String size;
} 