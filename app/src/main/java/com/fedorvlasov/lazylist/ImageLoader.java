package com.fedorvlasov.lazylist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Imaginable;

public class ImageLoader {

    private static Map<Imaginable, String> imageViews =
            Collections.synchronizedMap(new WeakHashMap<Imaginable, String>());
    final int stub_id = R.drawable.stub;

    MemCache mMemCache;
    FileCache mFileCache;
    ExecutorService imgThreadPool;
    // handler to display images in UI thread
    Handler handler = new Handler();

    public ImageLoader(Context context) {
        imageViews.clear();

        mMemCache = MemCache.getInstance();
        mFileCache = new FileCache(context);
        imgThreadPool = Executors.newFixedThreadPool(3);
    }

    /**
     * Android lollipop automaticamente ignora estas lineas para
     * verciones anteriores es realmente necesario
     * <p/>
     * Android lollipop automatically ignores these lines for
     * previous versions, but it's necessary
     *
     * @param path to file
     * @return bitmap, which is converted
     */
    public static Bitmap convertBitmap(String path) {
        Bitmap bitmap = null;
        BitmapFactory.Options bfOptions = new BitmapFactory.Options();
        // Disable Dithering mode
        bfOptions.inDither = false;
        // Tell to gc that whether it needs free memory, the Bitmap can be cleared
        bfOptions.inPurgeable = true;
        // Which kind of reference will be used to recover the Bitmap data after being clear,
        // when it will be used in the future
        bfOptions.inInputShareable = true;
        bfOptions.inPreferredConfig = Config.RGB_565;
        bfOptions.inTempStorage = new byte[32 * 1024];

        File file = new File(path);
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            // e.printStackTrace();
        }

        try {
            if (fs != null) {
                bitmap = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, bfOptions);
            }
        } catch (IOException e) {
            // e.printStackTrace();
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    public void displayImg(String url, Imaginable imageView) {
        if (imageViewReUse(imageView, url)) {
            imageViews.put(imageView, url);

            // First, try to fetch image from memory
            Bitmap bitmap = mMemCache.getImageInMem(url);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                queuePhoto(url, imageView);
                imageView.setImageResource(stub_id);
            }
        }
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

        // Last, if locally nothing works, try to get image from web
        try {
            URL imageUrl;
            String host = null;
            {
                int idx;
                if ((idx = url.indexOf("|")) > 0) {
                    host = url.substring(idx + 1);
                    url = url.substring(0, idx);
                }
            }
            imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(true);
            if (host != null) {
                conn.addRequestProperty("Host", host);
            }
            FileCache.writeFile(conn.getInputStream(), f);
            conn.disconnect();
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
        return convertBitmap(put_file.getPath());
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
            if (bitmap != null)
                imageView.setImageBitmap(bitmap);
            else
                imageView.setImageResource(stub_id);
            imageViews.remove(imageView);
        }
    }
}
