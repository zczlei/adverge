package com.adverge.backend.dto;

import com.adverge.backend.model.UserData;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdRequest {
    
    @NotBlank(message = "应用ID不能为空")
    private String appId;
    
    @NotBlank(message = "广告单元ID不能为空")
    private String adUnitId;
    
    @NotNull(message = "设备信息不能为空")
    private DeviceInfo deviceInfo;
    
    private UserData userData;
    
    private String sessionId;
    
    private String requestId;
    
    private String platform;
    
    private Map<String, Object> customParams;
    
    /**
     * 广告类型（横幅、插屏、激励视频等）
     */
    private String type;
    
    /**
     * 最低出价
     */
    private BigDecimal floorPrice;
    
    /**
     * 获取广告类型
     */
    public String getType() {
        return type;
    }
    
    /**
     * 设置广告类型
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * 获取最低出价
     */
    public BigDecimal getFloorPrice() {
        return floorPrice;
    }
    
    /**
     * 设置最低出价
     */
    public void setFloorPrice(BigDecimal floorPrice) {
        this.floorPrice = floorPrice;
    }
    
    @Data
    public static class DeviceInfo {
        private String type;
        private String os;
        private String osVersion;
        private String model;
        private String manufacturer;
        private String screenWidth;
        private String screenHeight;
        private String language;
    }
} 