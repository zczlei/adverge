package com.adverge.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Document(collection = "configs")
public class Config {
    
    @Id
    private String id;
    
    @Min(1000)
    @Max(30000)
    private int bidTimeout = 5000;
    
    @Min(60)
    @Max(3600)
    private int cacheExpiry = 300;
    
    private List<Platform> platforms = new ArrayList<>();
    
    private Date createdAt = new Date();
    
    private Date updatedAt = new Date();
    
    public void updateTimestamp() {
        this.updatedAt = new Date();
    }
    
    @Data
    public static class Platform {
        private String name;
        private boolean enabled = true;
        private String appId;
        private String appKey;
        private String placementId;
        private double bidFloor = 0.0;
    }
} 