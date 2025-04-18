package com.adverge.backend.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AppRequest {
    
    @NotBlank(message = "应用名称不能为空")
    private String name;
    
    @NotBlank(message = "应用包名不能为空")
    private String packageName;
    
    private String description;
    
    private String platform; // android, ios
    
    private String category;
    
    private String icon;
    
    private boolean active = true;
} 