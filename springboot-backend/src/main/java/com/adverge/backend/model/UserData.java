package com.adverge.backend.model;

import lombok.Data;

@Data
public class UserData {
    private GeoData geo;
    private String device;
    private String os;
} 