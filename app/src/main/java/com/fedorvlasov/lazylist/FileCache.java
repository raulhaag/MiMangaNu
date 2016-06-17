package com.fedorvlasov.lazylist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileCache {
    private File cacheDir;

    public FileCache(Context context) {
        //Find the dir to save cached images
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String dir = prefs.getString("directorio",
                Environment.getExternalStorageDirectory().getAbsolutePath()) + "/MiMangaNu/";
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir = new File(dir, "cache");
        else
            cacheDir = context.getCacheDir();
        if (!cacheDir.exists()) {
            boolean created = cacheDir.mkdirs();
        }
    }

    public FileCache() {
        //default constructor, in case when we only need to access one method from a FileCache object
    }

    public static void writeFile(InputStream is, File f) {
        try {
            OutputStream os = new FileOutputStream(f);
            int buffer_size = 1024;
            try {
                byte[] bytes = new byte[buffer_size];
                for (; ; ) {
                    int count = is.read(bytes, 0, buffer_size);
                    if (count == -1)
                        break;
                    os.write(bytes, 0, count);
                }
            } catch (Exception ex) {
                // This happens, if writing or reading throws IOException
            }
            os.close();
        } catch (Exception e) {
            // This happens, if FileOutputStream throws FileNotFoundException
        }
    }

    public File getFile(String url) {
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        String filename = String.valueOf(url.hashCode());
        return new File(cacheDir, filename);
    }

    public void clearCache() {
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;
        for (File f : files) {
            //Log.d("FileCache: ", "deleting: " + f.getAbsolutePath());
            f.delete();
        }
    }

}