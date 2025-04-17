package com.adverge.sdk.config;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;

/**
 * 广告样式配置
 */
public class AdStyle {
    // 标题样式
    private int titleTextColor = Color.BLACK;
    private float titleTextSize = 16f;
    private Typeface titleTypeface;
    private int titleMaxLines = 1;
    private TextUtils.TruncateAt titleEllipsize = TextUtils.TruncateAt.END;
    
    // 描述样式
    private int descriptionTextColor = Color.GRAY;
    private float descriptionTextSize = 14f;
    private Typeface descriptionTypeface;
    private int descriptionMaxLines = 2;
    private TextUtils.TruncateAt descriptionEllipsize = TextUtils.TruncateAt.END;
    
    // 按钮样式
    private int buttonTextColor = Color.WHITE;
    private float buttonTextSize = 14f;
    private Typeface buttonTypeface;
    private int buttonBackgroundColor = Color.BLUE;
    private int buttonCornerRadius = 4;
    
    // 图标样式
    private int iconWidth = 48;
    private int iconHeight = 48;
    private int iconCornerRadius = 0;
    
    // 媒体样式
    private int mediaWidth = 320;
    private int mediaHeight = 180;
    private int mediaCornerRadius = 0;
    
    // 容器样式
    private int containerBackgroundColor = Color.WHITE;
    private int containerPadding = 8;
    private int containerCornerRadius = 0;
    
    // 获取标题样式
    public int getTitleTextColor() {
        return titleTextColor;
    }
    
    public float getTitleTextSize() {
        return titleTextSize;
    }
    
    public Typeface getTitleTypeface() {
        return titleTypeface;
    }
    
    public int getTitleMaxLines() {
        return titleMaxLines;
    }
    
    public TextUtils.TruncateAt getTitleEllipsize() {
        return titleEllipsize;
    }
    
    // 获取描述样式
    public int getDescriptionTextColor() {
        return descriptionTextColor;
    }
    
    public float getDescriptionTextSize() {
        return descriptionTextSize;
    }
    
    public Typeface getDescriptionTypeface() {
        return descriptionTypeface;
    }
    
    public int getDescriptionMaxLines() {
        return descriptionMaxLines;
    }
    
    public TextUtils.TruncateAt getDescriptionEllipsize() {
        return descriptionEllipsize;
    }
    
    // 获取按钮样式
    public int getButtonTextColor() {
        return buttonTextColor;
    }
    
    public float getButtonTextSize() {
        return buttonTextSize;
    }
    
    public Typeface getButtonTypeface() {
        return buttonTypeface;
    }
    
    public int getButtonBackgroundColor() {
        return buttonBackgroundColor;
    }
    
    public int getButtonCornerRadius() {
        return buttonCornerRadius;
    }
    
    // 获取图标样式
    public int getIconWidth() {
        return iconWidth;
    }
    
    public int getIconHeight() {
        return iconHeight;
    }
    
    public int getIconCornerRadius() {
        return iconCornerRadius;
    }
    
    // 获取媒体样式
    public int getMediaWidth() {
        return mediaWidth;
    }
    
    public int getMediaHeight() {
        return mediaHeight;
    }
    
    public int getMediaCornerRadius() {
        return mediaCornerRadius;
    }
    
    // 获取容器样式
    public int getContainerBackgroundColor() {
        return containerBackgroundColor;
    }
    
    public int getContainerPadding() {
        return containerPadding;
    }
    
    public int getContainerCornerRadius() {
        return containerCornerRadius;
    }
    
    /**
     * 构建器类
     */
    public static class Builder {
        private final AdStyle style;
        
        public Builder() {
            style = new AdStyle();
        }
        
        // 设置标题样式
        public Builder setTitleTextColor(int color) {
            style.titleTextColor = color;
            return this;
        }
        
        public Builder setTitleTextSize(float size) {
            style.titleTextSize = size;
            return this;
        }
        
        public Builder setTitleTypeface(Typeface typeface) {
            style.titleTypeface = typeface;
            return this;
        }
        
        public Builder setTitleMaxLines(int maxLines) {
            style.titleMaxLines = maxLines;
            return this;
        }
        
        public Builder setTitleEllipsize(TextUtils.TruncateAt ellipsize) {
            style.titleEllipsize = ellipsize;
            return this;
        }
        
        // 设置描述样式
        public Builder setDescriptionTextColor(int color) {
            style.descriptionTextColor = color;
            return this;
        }
        
        public Builder setDescriptionTextSize(float size) {
            style.descriptionTextSize = size;
            return this;
        }
        
        public Builder setDescriptionTypeface(Typeface typeface) {
            style.descriptionTypeface = typeface;
            return this;
        }
        
        public Builder setDescriptionMaxLines(int maxLines) {
            style.descriptionMaxLines = maxLines;
            return this;
        }
        
        public Builder setDescriptionEllipsize(TextUtils.TruncateAt ellipsize) {
            style.descriptionEllipsize = ellipsize;
            return this;
        }
        
        // 设置按钮样式
        public Builder setButtonTextColor(int color) {
            style.buttonTextColor = color;
            return this;
        }
        
        public Builder setButtonTextSize(float size) {
            style.buttonTextSize = size;
            return this;
        }
        
        public Builder setButtonTypeface(Typeface typeface) {
            style.buttonTypeface = typeface;
            return this;
        }
        
        public Builder setButtonBackgroundColor(int color) {
            style.buttonBackgroundColor = color;
            return this;
        }
        
        public Builder setButtonCornerRadius(int radius) {
            style.buttonCornerRadius = radius;
            return this;
        }
        
        // 设置图标样式
        public Builder setIconWidth(int width) {
            style.iconWidth = width;
            return this;
        }
        
        public Builder setIconHeight(int height) {
            style.iconHeight = height;
            return this;
        }
        
        public Builder setIconCornerRadius(int radius) {
            style.iconCornerRadius = radius;
            return this;
        }
        
        // 设置媒体样式
        public Builder setMediaWidth(int width) {
            style.mediaWidth = width;
            return this;
        }
        
        public Builder setMediaHeight(int height) {
            style.mediaHeight = height;
            return this;
        }
        
        public Builder setMediaCornerRadius(int radius) {
            style.mediaCornerRadius = radius;
            return this;
        }
        
        // 设置容器样式
        public Builder setContainerBackgroundColor(int color) {
            style.containerBackgroundColor = color;
            return this;
        }
        
        public Builder setContainerPadding(int padding) {
            style.containerPadding = padding;
            return this;
        }
        
        public Builder setContainerCornerRadius(int radius) {
            style.containerCornerRadius = radius;
            return this;
        }
        
        public AdStyle build() {
            return style;
        }
    }
} 