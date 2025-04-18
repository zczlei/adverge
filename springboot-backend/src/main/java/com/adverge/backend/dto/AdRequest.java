package com.adverge.backend.dto;

import com.adverge.backend.model.UserData;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AdRequest {
    
    @NotBlank(message = "应用ID不能为空")
    private String appId;
    
    @NotBlank(message = "广告单元ID不能为空")
    private String adUnitId;
    
    @NotNull(message = "设备信息不能为空")
    private DeviceInfo deviceInfo;
    
    private UserData userData;
    
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