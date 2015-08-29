package com.fedorvlasov.lazylist;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * A nicer Memory cache than the previous one, this is inspired and taken from
 * http://stackoverflow.com/questions/1945201/android-image-caching
 */
public class MemCache {
    private LruCache<String, Bitmap> imagesWarehouse;
    private static MemCache cache;

    public static MemCache getInstance() {
        if (cache == null) {
            cache = new MemCache();
        }
        return cache;
    }

    private MemCache() {
        /** We want 1/8 of the available memory */
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        imagesWarehouse = new LruCache<String, Bitmap>(cacheSize) {
            protected int sizeOf(String key, Bitmap value) {
                int bitmapByteCount = value.getRowBytes() * value.getHeight();
                return bitmapByteCount / 1024;
            }
        };
    }

    public void putImageInMem(String key, Bitmap value) {
        if (imagesWarehouse != null && imagesWarehouse.get(key) == null) {
            imagesWarehouse.put(key, value);
        }
    }

    public Bitmap getImageInMem(String key) {
        if (key != null)
            return imagesWarehouse.get(key);
        else
            return null;
    }

    public void clearMem() {
        if (imagesWarehouse != null) {
            imagesWarehouse.evictAll();
        }
    }
}
