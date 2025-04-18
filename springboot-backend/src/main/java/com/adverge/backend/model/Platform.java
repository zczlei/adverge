package com.adverge.backend.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
public class Platform {
    private String name;
    private boolean enabled = true;
    private String appId;
    private String appKey;
    private String placementId;
    private double bidFloor = 0.0;
} 