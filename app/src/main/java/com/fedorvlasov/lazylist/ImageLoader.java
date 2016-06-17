package com.fedorvlasov.lazylist;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Handler;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ar.rulosoft.mimanganu.componentes.Imaginable;
import ar.rulosoft.navegadores.Navegador;

public class ImageLoader {
    private static Map<Imaginable, String> imageViews =
            Collections.synchronizedMap(new WeakHashMap<Imaginable, String>());
    public static Navegador NAVEGADOR = null;

    private MemCache mMemCache;
    private FileCache mFileCache;
    private ExecutorService imgThreadPool;
    // handler to display images in UI thread
    private Handler handler = new Handler();

    public ImageLoader(Context context) {
        imageViews.clear();

        mMemCache = MemCache.getInstance();
        mFileCache = new FileCache(context);
        imgThreadPool = Executors.newFixedThreadPool(3);
    }

    public static Navegador initAndGetNavegador() throws Exception {
        if (NAVEGADOR == null) NAVEGADOR = new Navegador();
        return NAVEGADOR;
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
    private static Bitmap convertBitmap(String path) {
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
                //imageView.setImageResource(stub_id);
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
            String host = null;
            {
                int idx;
                if ((idx = url.indexOf("|")) > 0) {
                    host = url.substring(idx + 1);
                    url = url.substring(0, idx);
                }
            }
            OkHttpClient client = initAndGetNavegador().getHttpClient();
            client.setConnectTimeout(5, TimeUnit.SECONDS);
            client.setReadTimeout(5, TimeUnit.SECONDS);
            Request.Builder builder = new Request.Builder().url(url);
            if (host != null) {
                builder.addHeader("Host", host);
            }
            Response response = client.newCall(builder.build()).execute();
            FileCache.writeFile(response.body().byteStream(), f);
            response.body().close();
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
            if (bitmap != null) {
                imageView.setAlpha(0f);
                imageView.setImageBitmap(bitmap);
                ObjectAnimator.ofFloat(imageView, "alpha", 1f).start();
            }
            imageViews.remove(imageView);
        }
    }
}
