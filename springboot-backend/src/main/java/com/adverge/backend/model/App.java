package com.adverge.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 应用实体类
 */
@Data
@NoArgsConstructor
@Document(collection = "apps")
public class App {
    
    @Id
    private String id;
    
    /**
     * 应用名称
     */
    private String name;
    
    /**
     * 应用包名/Bundle ID
     */
    private String packageName;
    
    /**
     * 应用描述
     */
    private String description;
    
    /**
     * 应用平台：Android、iOS
     */
    private String platform;
    
    /**
     * 应用API密钥
     */
    private String apiKey;
    
    /**
     * 应用关联的广告单元列表
     */
    private List<String> adUnitIds = new ArrayList<>();
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 应用是否启用
     */
    private boolean enabled = true;
    
    /**
     * 生成API密钥
     */
    public void generateApiKey() {
        this.apiKey = UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 添加广告单元ID
     * 
     * @param adUnitId 广告单元ID
     */
    public void addAdUnitId(String adUnitId) {
        if (adUnitIds == null) {
            adUnitIds = new ArrayList<>();
        }
        
        if (!adUnitIds.contains(adUnitId)) {
            adUnitIds.add(adUnitId);
        }
    }
    
    /**
     * 移除广告单元ID
     * 
     * @param adUnitId 广告单元ID
     */
    public void removeAdUnitId(String adUnitId) {
        if (adUnitIds != null) {
            adUnitIds.remove(adUnitId);
        }
    }
} 