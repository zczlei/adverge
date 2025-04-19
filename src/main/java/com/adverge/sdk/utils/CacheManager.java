package com.adverge.sdk.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.adverge.sdk.AdSDK;

import java.util.Map;
import java.util.Set;

/**
 * 缓存管理工具类
 */
public class CacheManager {
    private static final String PREF_NAME = "ad_sdk_cache";
    private final SharedPreferences preferences;
    private final AdSDK sdk;

    public CacheManager(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.sdk = AdSDK.getInstance();
    }

    /**
     * 保存字符串
     */
    public void putString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    /**
     * 获取字符串
     */
    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    /**
     * 保存整数
     */
    public void putInt(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    /**
     * 获取整数
     */
    public int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    /**
     * 保存长整数
     */
    public void putLong(String key, long value) {
        preferences.edit().putLong(key, value).apply();
    }

    /**
     * 获取长整数
     */
    public long getLong(String key, long defaultValue) {
        return preferences.getLong(key, defaultValue);
    }

    /**
     * 保存浮点数
     */
    public void putFloat(String key, float value) {
        preferences.edit().putFloat(key, value).apply();
    }

    /**
     * 获取浮点数
     */
    public float getFloat(String key, float defaultValue) {
        return preferences.getFloat(key, defaultValue);
    }

    /**
     * 保存布尔值
     */
    public void putBoolean(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    /**
     * 获取布尔值
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    /**
     * 保存字符串集合
     */
    public void putStringSet(String key, Set<String> values) {
        preferences.edit().putStringSet(key, values).apply();
    }

    /**
     * 获取字符串集合
     */
    public Set<String> getStringSet(String key, Set<String> defaultValues) {
        return preferences.getStringSet(key, defaultValues);
    }

    /**
     * 移除指定键的值
     */
    public void remove(String key) {
        preferences.edit().remove(key).apply();
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        preferences.edit().clear().apply();
    }

    /**
     * 获取所有缓存数据
     */
    public Map<String, ?> getAll() {
        return preferences.getAll();
    }

    /**
     * 检查键是否存在
     */
    public boolean contains(String key) {
        return preferences.contains(key);
    }
} 