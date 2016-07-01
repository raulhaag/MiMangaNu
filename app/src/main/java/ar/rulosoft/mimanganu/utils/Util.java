package ar.rulosoft.mimanganu.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ar.rulosoft.mimanganu.MainActivity;
import ar.rulosoft.mimanganu.R;

public class Util {
    private static Util utilInstance = null;
    protected static NotificationCompat.Builder builder;
    protected static NotificationManager notificationManager;

    private Util() {
    }

    public static Util getInstance() {
        if (utilInstance == null) {
            utilInstance = new Util();
        }
        return utilInstance;
    }

    public void deleteRecursive(File fileOrDirectory) {
        if(fileOrDirectory != null) {
            if (fileOrDirectory.isDirectory() && fileOrDirectory.listFiles().length > 0) {
                for (File child : fileOrDirectory.listFiles()) {
                    deleteRecursive(child);
                }
            }
            fileOrDirectory.delete();
        }
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

    public static ArrayList<String> dirList(String directory) {
        ArrayList<String> list = new ArrayList<>();
        if (directory.length() != 1) {
            list.add("..");
        }
        File dir = new File(directory);
        if (dir.listFiles() != null) {
            for (File child : dir.listFiles()) {
                if (child.isDirectory()) {
                    list.add(child.getName());
                }
            }
        }
        return list;
    }

    public static ArrayList<String> imageList(String directory) {
        ArrayList<String> list = new ArrayList<>();
        File dir = new File(directory);
        if (dir.listFiles() != null) {
            for (File child : dir.listFiles()) {
                if (!child.isDirectory()) {
                    if(child.getName().matches(".+?\\.(jpg|bmp|png|jpeg|gif)+"))
                    list.add(child.getName());
                }
            }
        }
        return list;
    }

    public static String getLastStringInPath(String path) {
        path = path.substring(0,path.length() - 1);
        int idx = path.lastIndexOf("/");
        return path.substring(idx + 1);
    }

    public void toast(final Context context, final String toast) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void toast(final Context context, final String toast, final int length) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, toast, length).show();
            }
        });
    }

    public void createNotification(Context context, boolean isPermanent, int id, Intent intent, String contentTitle, String contentText) {
        Notification notification;
        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder = new NotificationCompat.Builder(context);
        builder.setOngoing(true);
        builder.setContentTitle(contentTitle);
        builder.setContentText(contentText);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentIntent(pIntent);
        builder.setAutoCancel(true);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(contentTitle));
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
        }
        notificationManager = (NotificationManager) context.getSystemService(MainActivity.NOTIFICATION_SERVICE);

        notification = builder.build();
        if (isPermanent) {
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notification.flags = Notification.FLAG_NO_CLEAR;
        } else {
            notification.flags = Notification.FLAG_AUTO_CANCEL;
        }
        notificationManager.notify(id, notification);
    }

    public void createSearchingForUpdatesNotification(Context context, int id) {
        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT);
        builder = new NotificationCompat.Builder(context);
        builder.setOngoing(true);
        builder.setContentTitle(context.getResources().getString(R.string.searching_for_updates));
        builder.setContentText("");
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentIntent(pIntent);
        builder.setAutoCancel(true);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setPriority(Notification.PRIORITY_HIGH);
            builder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(context.getResources().getString(R.string.searching_for_updates)));
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(""));
        }
        builder.setProgress(100, 0, true);

        notificationManager = (NotificationManager) context.getSystemService(MainActivity.NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(id, notification);
    }

    public void changeSearchingForUpdatesNotification(Context context, int max, int progress, int id, String contentTitle, String contentText, boolean ongoing) {
        builder.setContentTitle(contentTitle);
        builder.setContentText(contentText);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setPriority(Notification.PRIORITY_HIGH);
            builder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(contentTitle));
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
        }
        if (ongoing) {
            builder.setOngoing(true);
            if (progress == max) {
                builder.setProgress(max, progress, true);
                builder.setContentText(context.getResources().getString(R.string.finishing_update));
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    builder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getResources().getString(R.string.finishing_update)));
                }
            } else {
                builder.setProgress(max, progress, false);
            }
        } else {
            builder.setOngoing(false);
            builder.setProgress(max, progress, false);
        }
        notificationManager.notify(id, builder.build());
    }

    public void cancelNotification(int id) {
        notificationManager.cancel(id);
    }

}
