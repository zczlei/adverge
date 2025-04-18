package com.adverge.backend.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
public class TrackRequest {
    
    @NotBlank(message = "平台不能为空")
    private String platform;
    
    @Min(value = 0, message = "收益不能为负数")
    private double revenue;
} 