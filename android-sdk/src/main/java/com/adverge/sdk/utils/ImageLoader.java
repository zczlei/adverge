package com.adverge.sdk.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.adverge.sdk.config.CacheConfig;
import com.adverge.sdk.utils.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 图片加载器
 */
public class ImageLoader {
    private static final String TAG = "ImageLoader";
    private static ImageLoader instance;
    
    private final Context context;
    private final ExecutorService executorService;
    private final MemoryCache memoryCache;
    private final DiskCache diskCache;
    
    private ImageLoader(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newFixedThreadPool(3);
        this.memoryCache = new MemoryCache();
        this.diskCache = new DiskCache(context);
    }
    
    public static synchronized ImageLoader getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ImageLoader must be initialized first");
        }
        return instance;
    }
    
    public static synchronized void initialize(Context context) {
        if (instance == null) {
            instance = new ImageLoader(context);
        }
    }
    
    public void loadImage(String url, ImageView imageView) {
        if (url == null || url.isEmpty()) {
            Logger.w(TAG, "Invalid image URL");
            return;
        }
        
        // 从内存缓存中获取
        Bitmap bitmap = memoryCache.get(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }
        
        // 从磁盘缓存中获取
        bitmap = diskCache.get(url);
        if (bitmap != null) {
            memoryCache.put(url, bitmap);
            imageView.setImageBitmap(bitmap);
            return;
        }
        
        // 异步加载图片
        Future<?> future = executorService.submit(() -> {
            try {
                // TODO: 实现实际的网络图片加载逻辑
                // 这里使用模拟数据
                Bitmap newBitmap = createMockBitmap();
                
                // 缓存图片
                memoryCache.put(url, newBitmap);
                diskCache.put(url, newBitmap);
                
                // 更新UI
                imageView.post(() -> imageView.setImageBitmap(newBitmap));
            } catch (Exception e) {
                Logger.e(TAG, "Failed to load image: " + url, e);
            }
        });
        
        // 设置取消回调
        imageView.setTag(future);
    }
    
    public void cancelLoad(ImageView imageView) {
        Object tag = imageView.getTag();
        if (tag instanceof Future) {
            ((Future<?>) tag).cancel(true);
            imageView.setTag(null);
        }
    }
    
    public void clearCache() {
        memoryCache.clear();
        diskCache.clear();
    }
    
    private Bitmap createMockBitmap() {
        // TODO: 创建模拟图片
        return null;
    }
    
    private static class MemoryCache {
        private final android.util.LruCache<String, Bitmap> cache;
        
        MemoryCache() {
            int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            int cacheSize = maxMemory / 8;
            cache = new android.util.LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount() / 1024;
                }
            };
        }
        
        void put(String key, Bitmap bitmap) {
            if (key != null && bitmap != null) {
                cache.put(key, bitmap);
            }
        }
        
        Bitmap get(String key) {
            return key != null ? cache.get(key) : null;
        }
        
        void clear() {
            cache.evictAll();
        }
    }
    
    private static class DiskCache {
        private final File cacheDir;
        
        DiskCache(Context context) {
            cacheDir = new File(context.getCacheDir(), "image_cache");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
        }
        
        void put(String key, Bitmap bitmap) {
            if (key == null || bitmap == null) {
                return;
            }
            
            File file = new File(cacheDir, key.hashCode() + "");
            try (FileOutputStream out = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException e) {
                Logger.e(TAG, "Failed to save bitmap to disk", e);
            }
        }
        
        Bitmap get(String key) {
            if (key == null) {
                return null;
            }
            
            File file = new File(cacheDir, key.hashCode() + "");
            if (!file.exists()) {
                return null;
            }
            
            // TODO: 从文件加载Bitmap
            return null;
        }
        
        void clear() {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }
} 