package com.fedorvlasov.lazylist;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Hopefully a nicer Memory cache than the previous one, this is inspired and taken from
 * http://stackoverflow.com/questions/1945201/android-image-caching
 * Thanks to Zubair Ahmad Khan
 */
public class RamCache {
    private LruCache<String, Bitmap> imagesWarehouse;

    private static RamCache cache;

    public static RamCache getInstance() {
        if (cache == null) {
            cache = new RamCache();
        }

        return cache;
    }

    public RamCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        System.out.println("cache size = " + cacheSize);

        imagesWarehouse = new LruCache<String, Bitmap>(cacheSize) {
            protected int sizeOf(String key, Bitmap value) {
                // The cache size will be measured in kilobytes rather than number of items.
                int bitmapByteCount = value.getRowBytes() * value.getHeight();
                return bitmapByteCount / 1024;
            }
        };
    }

    public void putImageToMem(String key, Bitmap value) {
        if (imagesWarehouse != null && imagesWarehouse.get(key) == null) {
            imagesWarehouse.put(key, value);
        }
    }

    public Bitmap getImageToMem(String key) {
        if (key != null) {
            return imagesWarehouse.get(key);
        } else {
            return null;
        }
    }

    public void clearMem() {
        if (imagesWarehouse != null) {
            imagesWarehouse.evictAll();
        }
    }
}
