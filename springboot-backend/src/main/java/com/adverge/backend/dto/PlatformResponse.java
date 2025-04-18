package com.adverge.backend.dto;

import com.adverge.backend.model.Config;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 平台响应DTO，与Android SDK兼容
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformResponse {
    
    /**
     * 平台名称
     */
    private String name;
    
    /**
     * 平台类型
     */
    private String type;
    
    /**
     * 应用ID
     */
    private String appId;
    
    /**
     * 应用密钥
     */
    private String appKey;
    
    /**
     * 是否启用
     */
    private boolean enabled;
    
    /**
     * 最低出价
     */
    private double bidFloor;
    
    /**
     * 转换平台配置为平台响应
     * @param platform 平台配置
     * @return 平台响应
     */
    public static PlatformResponse fromPlatform(Config.Platform platform) {
        if (platform == null) {
            return null;
        }
        
        return PlatformResponse.builder()
                .name(platform.getName())
                .type("bidding") // 默认类型
                .appId(platform.getAppId())
                .appKey(platform.getAppKey())
                .enabled(platform.isEnabled())
                .bidFloor(platform.getBidFloor())
                .build();
    }
    
    /**
     * 转换平台列表
     * @param platforms 平台配置列表
     * @return 平台响应列表
     */
    public static List<PlatformResponse> fromPlatforms(List<Config.Platform> platforms) {
        if (platforms == null) {
            return List.of();
        }
        
        return platforms.stream()
                .map(PlatformResponse::fromPlatform)
                .collect(Collectors.toList());
    }
} 