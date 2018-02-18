package com.fedorvlasov.lazylist;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ar.rulosoft.mimanganu.componentes.Imaginable;
import ar.rulosoft.navegadores.Navigator;

public class ImageLoader {
    private static Map<Imaginable, String> imageViews =
            Collections.synchronizedMap(new WeakHashMap<Imaginable, String>());

    private MemCache mMemCache;
    private FileCache mFileCache;
    private ExecutorService imgThreadPool;
    // handler to display images in UI thread
    private Handler handler = new Handler();

    public ImageLoader(Context context) {
        imageViews.clear();

        mMemCache = MemCache.getInstance();
        mFileCache = new FileCache(context);
        imgThreadPool = Executors.newFixedThreadPool(4);
    }

    public void displayImg(String url, Imaginable imageView) {
       // if (imageViewReUse(imageView, url)) {
            imageViews.put(imageView, url);

            // First, try to fetch image from memory
            Bitmap bitmap = mMemCache.getImageInMem(url);
            if(url != null && !url.isEmpty()) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    queuePhoto(url, imageView);
                }
            }
       // }
    }

    private boolean imageViewReUse(Imaginable imageView, String url) {
        String tag = imageViews.get(imageView);
        return tag == null || !tag.equals(url);
    }

    private void queuePhoto(String url, Imaginable imageView) {
        imgThreadPool.submit(new ImageGet(imageView, url));
    }

    private Bitmap getBitmap(String url) {
        File f = mFileCache.getFile(url);

        // Second, try to get image from local storage, i.e. SD card
        Bitmap imgFile = decodeFile(f);
        if (imgFile != null)
            return imgFile;
        try {
            if (url.indexOf("|") > 0) {
                Navigator nav = Navigator.getInstance();
                String[] parts = url.split("\\|");
                nav.addHeader("Referer", parts[1]);
                FileCache.writeFile(nav.getStream(parts[0]), f);
            } else if (url.startsWith("/")) {
                // FromFolder sets url as an absolute local path, so load file directly
                InputStream is;
                is = new FileInputStream(url);
                FileCache.writeFile(is, f);
                is.close();
            }
            else {
                FileCache.writeFile(Navigator.getInstance().getStream(url), f);
            }
            return decodeFile(f);
        } catch (Throwable ex) {
            if (ex instanceof OutOfMemoryError)
                mMemCache.clearMem();
            return null;
        }
    }

    /**
     * decodes image and scales it to reduce memory consumption
     *
     * @param put_file data from disk
     * @return Bitmap
     */
    private Bitmap decodeFile(File put_file) {
        // if file not exist, skip everything
        if (!put_file.exists())
            return null;
        // We want Image to be equal or smaller than 400px height
        int tempSampleSize = 1, requiredSize = 400;
        try {
            BitmapFactory.Options bmpOpts = new BitmapFactory.Options();
            bmpOpts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(put_file.getAbsolutePath(), bmpOpts);
            while ((bmpOpts.outHeight / tempSampleSize) >= requiredSize) {
                tempSampleSize *= 2;
            }
            bmpOpts.inSampleSize = tempSampleSize;
            bmpOpts.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(put_file.getAbsolutePath(), bmpOpts);
        } catch (Exception e) {
            // usually file not found, but just ignore it
            return null;
        }
    }

    public void clearMem(){
        mMemCache.clearMem();
    }

    /**
     * An image getter, which is called, if Image is not found in memory
     * It is a runnable, which will be submit into the imgThreadPool,
     * so it won't block the UI
     */
    class ImageGet implements Runnable {
        String url;
        Imaginable imageView;

        ImageGet(Imaginable _imageView, String _url) {
            this.url = _url;
            this.imageView = _imageView;
        }

        @Override
        public void run() {
            try {
                if (imageViewReUse(imageView, url))
                    return;
                Bitmap bmp = getBitmap(url);
                mMemCache.putImageInMem(url, bmp);
                if (imageViewReUse(imageView, url))
                    return;
                BitmapDisplay bd = new BitmapDisplay(bmp, imageView, url);
                handler.post(bd);
            } catch (Throwable th) {
                // th.printStackTrace();
            }
        }
    }

    /**
     * Used to display bitmap in the UI thread,
     * if the image finally arrived, then update the imageView with the new image
     */
    class BitmapDisplay implements Runnable {
        Bitmap bitmap;
        String url;
        Imaginable imageView;

        public BitmapDisplay(Bitmap _bmp, Imaginable _imageView, String _url) {
            bitmap = _bmp;
            url = _url;
            imageView = _imageView;
        }

        public void run() {
            if (imageViewReUse(imageView, url))
                return;
            if (bitmap != null) {
                imageView.setAlpha(0f);
                imageView.setImageBitmap(bitmap);
                ObjectAnimator.ofFloat(imageView, "alpha", 1f).start();
            }
            imageViews.remove(imageView);
        }
    }
}
