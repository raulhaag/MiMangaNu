package ar.rulosoft.mimanganu.utils;

import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class Util {
    private static Util utilInstance = null;

    private Util() {
    }

    public static Util getInstance() {
        if (utilInstance == null) {
            utilInstance = new Util();
        }
        return utilInstance;
    }

    public void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

    public void restartApp(Context context) {
        context.startActivity(context.getPackageManager().getLaunchIntentForPackage(context.getPackageName()).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        System.exit(0);
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

}
