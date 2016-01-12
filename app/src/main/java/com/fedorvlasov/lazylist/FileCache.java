package com.fedorvlasov.lazylist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

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

    /**
     * Calculate size of folder.
     *
     * @param folder your directory to check
     * @return totalSize
     */
    public long dirSize(final File folder) {
        if (folder == null || !folder.exists())
            return 0;
        if (!folder.isDirectory())
            return folder.length();
        final List<File> dirs = new LinkedList<>();
        dirs.add(folder);
        long result = 0;
        while (!dirs.isEmpty()) {
            final File dir = dirs.remove(0);
            if (!dir.exists())
                continue;
            final File[] listFiles = dir.listFiles();
            if (listFiles == null || listFiles.length == 0)
                continue;
            for (final File child : listFiles) {
                result += child.length();
                if (child.isDirectory())
                    dirs.add(child);
            }
        }
        return result;
    }

    public void clearCache() {
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;
        for (File f : files) {
            boolean deleted = f.delete();
        }
    }
}