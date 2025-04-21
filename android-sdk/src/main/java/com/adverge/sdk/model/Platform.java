package com.adverge.sdk.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 平台模型类，表示一个广告平台
 */
public class Platform {
    private String id;
    private String name;
    private String description;
    private boolean enabled;
    private Map<String, Object> configs;
    private String appId;
    private String appKey;

    public Platform() {
        this.configs = new HashMap<>();
        this.enabled = false;
    }

    public Platform(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.configs = new HashMap<>();
        this.enabled = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, Object> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, Object> configs) {
        this.configs = configs;
    }

    public void addConfig(String key, Object value) {
        this.configs.put(key, value);
    }

    public Object getConfig(String key) {
        return this.configs.get(key);
    }
    
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    @Override
    public String toString() {
        return "Platform{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", enabled=" + enabled +
                ", configs=" + configs +
                ", appId='" + appId + '\'' +
                ", appKey='" + appKey + '\'' +
                '}';
    }
} 