package ar.rulosoft.mimanganu.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.MainActivity;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.navegadores.Navigator;

import static ar.rulosoft.mimanganu.MainActivity.pm;

public class Util {

    public static final String channelIdNews = "MiMangaNu_News";
    private static final String channelIdUpdate = "MiMangaNu_Update";
    public static int n = 0;
    private static NotificationCompat.Builder searchingForUpdatesNotificationBuilder;
    private static NotificationCompat.Builder notificationWithProgressbarBuilder;
    private static NotificationManager notificationManager;

    private Util() {
    }


    public static void initGlobals(Context context) {
        try {
            NetworkUtilsAndReceiver.ONLY_WIFI = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("only_wifi", false);
            Navigator.initialiseInstance(context);
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }

    public static Util getInstance() {
        return LazyHolder.utilInstance;
    }

    public static void createNotificationChannels(Context context) {
        // Creates android O notification channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(MainActivity.NOTIFICATION_SERVICE);
            assert notificationManager != null;
            if (null == notificationManager.getNotificationChannel(channelIdUpdate)) {
                /* Sound and vibrate */
                NotificationChannel notificationChannel = new NotificationChannel(channelIdNews,
                        context.getResources().getString(R.string.notification_channel_new),
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setDescription(context.getResources().getString(R.string.notification_channel_new));
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{0, 500, 500, 1000});
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.WHITE);
                notificationManager.createNotificationChannel(notificationChannel);
                /* No sound, no vibrate */
                notificationChannel = new NotificationChannel(channelIdUpdate,
                        context.getResources().getString(R.string.notification_channel_update),
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setDescription(context.getResources().getString(R.string.notification_channel_update));
                notificationChannel.enableVibration(false);
                notificationChannel.setSound(null, null);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.WHITE);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public ArrayList<String> dirList(String directory) {
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

    public ArrayList<String> imageList(String directory) {
        ArrayList<String> list = new ArrayList<>();
        File dir = new File(directory);
        if (dir.listFiles() != null) {
            for (File child : dir.listFiles()) {
                if (!child.isDirectory()) {
                    if (child.getName().matches(".+?\\.(jpg|bmp|png|jpeg|gif)+"))
                        list.add(child.getName());
                }
            }
        }
        return list;
    }

    public String getLastStringInPath(String path) {
        path = path.substring(0, path.length() - 1);
        int idx = path.lastIndexOf("/");
        return path.substring(idx + 1);
    }

    public String getLastStringInPathDontRemoveLastChar(String path) {
        path = path;
        int idx = path.lastIndexOf("/");
        return path.substring(idx + 1);
    }

    public void showFastSnackBar(String message, View view, Context context) {
        showTimeSnackBar(message, view, context, Snackbar.LENGTH_SHORT);
    }

    public void showSlowSnackBar(String message, View view, Context context) {
        showTimeSnackBar(message, view, context, Snackbar.LENGTH_LONG);
    }

    public void showTimeSnackBar(final String message, View view, final Context context, final int duration) {
        if (view != null) {
            Snackbar snackbar = Snackbar.make(view, message, duration);
            if (MainActivity.colors != null)
                snackbar.getView().setBackgroundColor(MainActivity.colors[0]);
            snackbar.show();
        } else {
            if (context != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, message, duration).show();
                    }
                });
            }
        }
    }

    public void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory != null) {
            if (fileOrDirectory.isDirectory() && fileOrDirectory.listFiles() != null) {
                if (fileOrDirectory.listFiles().length > 0) {
                    for (File child : fileOrDirectory.listFiles()) {
                        deleteRecursive(child);
                    }
                }
            }
            fileOrDirectory.delete();
        }
    }

    public void deleteEmptyDirectoriesRecursive(File fileOrDirectory) {
        if (fileOrDirectory != null) {
            if (fileOrDirectory.isDirectory() && fileOrDirectory.listFiles() != null) {
                if (fileOrDirectory.listFiles().length > 0) {
                    for (File child : fileOrDirectory.listFiles()) {
                        deleteEmptyDirectoriesRecursive(child);
                    }
                }
            }
            if (fileOrDirectory.isDirectory() && fileOrDirectory.listFiles() != null) {
                if (fileOrDirectory.listFiles().length == 0) {
                    fileOrDirectory.delete();
                    Util.getInstance().changeNotificationWithProgressbar(0, 0, 69, fileOrDirectory.getAbsolutePath(), true);
                }
            }
        }
    }

    // see https://stackoverflow.com/a/9293885
    public void copyFile(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
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

    public void toast(final Context context, final String toast) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (context != null)
                    Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
                else
                    Log.e("Util", "Failed to deliver toast! Context was null. Message was: " + toast);
            }
        });
    }

    public void toast(final Context context, final String toast, final int length) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (context != null)
                    Toast.makeText(context, toast, length).show();
                else
                    Log.e("Util", "Failed to deliver toast! Context was null. Message was: " + toast);
            }
        });
    }

    private int getCorrectIcon() {
        int icon;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            icon = R.mipmap.ic_launcher_white;
        } else {
            icon = R.mipmap.ic_launcher;
        }
        return icon;
    }

    public void createNotification(Context context, boolean isPermanent, int id, Intent intent, String contentTitle, String contentText) {
        Notification notification;
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent deleteIntent = new Intent(context, NotificationDeleteIntentReceiver.class);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis() + 1, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelIdNews);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setContentTitle(contentTitle);
        notificationBuilder.setContentText(contentText);
        notificationBuilder.setSmallIcon(getCorrectIcon());
        notificationBuilder.setContentIntent(contentPendingIntent);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setDeleteIntent(deletePendingIntent);
        if (pm != null) {
            if (pm.getBoolean("update_sound", false))
                notificationBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        }
        ++n;
        //notificationBuilder.setNumber(n); // don't delete this I need this for debugging ~ xtj9182
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(contentTitle));
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
        notificationManager = (NotificationManager) context.getSystemService(MainActivity.NOTIFICATION_SERVICE);

        notification = notificationBuilder.build();
        if (isPermanent) {
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notification.flags = Notification.FLAG_NO_CLEAR;
        } else {
            notification.flags = Notification.FLAG_AUTO_CANCEL;
        }
        if (pm != null) {
            if (pm.getBoolean("update_vibrate", false))
                notification.defaults |= Notification.DEFAULT_VIBRATE;
        }
        notificationManager.notify(id, notification);
    }

    public void createSearchingForUpdatesNotification(Context context, int id) {
        Intent cancelIntent = new Intent(context, CancelIntentReceiver.class);
        cancelIntent.putExtra("manga_id", -1);
        cancelIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Intent contentIntent = new Intent(context, MainActivity.class);
        contentIntent.putExtra("manga_id", -2);
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis() + 1, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        searchingForUpdatesNotificationBuilder = new NotificationCompat.Builder(context, channelIdUpdate);
        searchingForUpdatesNotificationBuilder.setOngoing(true);
        searchingForUpdatesNotificationBuilder.setContentTitle(context.getResources().getString(R.string.searching_for_updates));
        searchingForUpdatesNotificationBuilder.setContentText("");
        searchingForUpdatesNotificationBuilder.setSmallIcon(R.drawable.ic_action_av_reload);
        searchingForUpdatesNotificationBuilder.setContentIntent(contentPendingIntent);
        searchingForUpdatesNotificationBuilder.setAutoCancel(true);
        searchingForUpdatesNotificationBuilder.addAction(R.drawable.ic_action_x_light, context.getResources().getString(R.string.cancel), cancelPendingIntent);
        searchingForUpdatesNotificationBuilder.setGroup("searchingForUpdates");
        searchingForUpdatesNotificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        searchingForUpdatesNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(context.getResources().getString(R.string.searching_for_updates)));
        searchingForUpdatesNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(""));
        searchingForUpdatesNotificationBuilder.setProgress(100, 0, true);

        notificationManager = (NotificationManager) context.getSystemService(MainActivity.NOTIFICATION_SERVICE);
        Notification notification = searchingForUpdatesNotificationBuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(id, notification);
    }

    public void changeSearchingForUpdatesNotification(Context context, int max, int progress, int id, String contentTitle, String contentText, boolean ongoing) {
        searchingForUpdatesNotificationBuilder.setContentTitle(contentTitle);
        searchingForUpdatesNotificationBuilder.setContentText(contentText);
        searchingForUpdatesNotificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        searchingForUpdatesNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(contentTitle));
        searchingForUpdatesNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));

        if (ongoing) {
            searchingForUpdatesNotificationBuilder.setOngoing(true);
            if (progress == max) {
                searchingForUpdatesNotificationBuilder.setProgress(max, progress, true);
                searchingForUpdatesNotificationBuilder.setContentText(context.getResources().getString(R.string.finishing_update));
                searchingForUpdatesNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getResources().getString(R.string.finishing_update)));

            } else {
                searchingForUpdatesNotificationBuilder.setProgress(max, progress, false);
            }
        } else {
            searchingForUpdatesNotificationBuilder.setOngoing(false);
            searchingForUpdatesNotificationBuilder.setProgress(max, progress, false);
        }
        notificationManager.notify(id, searchingForUpdatesNotificationBuilder.build());
    }

    public void createNotificationWithProgressbar(Context context, int id, String contentTitle, String contentText) {
        Intent defaultIntent = new Intent(context, MainActivity.class);
        defaultIntent.putExtra("manga_id", -2);
        defaultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent defaultPendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), defaultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationWithProgressbarBuilder = new NotificationCompat.Builder(context, channelIdUpdate);
        notificationWithProgressbarBuilder.setOngoing(true);
        notificationWithProgressbarBuilder.setContentTitle(contentTitle);
        notificationWithProgressbarBuilder.setContentText(contentText);
        notificationWithProgressbarBuilder.setSmallIcon(getCorrectIcon());
        notificationWithProgressbarBuilder.setContentIntent(defaultPendingIntent);
        notificationWithProgressbarBuilder.setAutoCancel(true);
        notificationWithProgressbarBuilder.setPriority(Notification.PRIORITY_HIGH);
        notificationWithProgressbarBuilder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(contentTitle));
        notificationWithProgressbarBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
        notificationWithProgressbarBuilder.setProgress(100, 0, true);

        notificationManager = (NotificationManager) context.getSystemService(MainActivity.NOTIFICATION_SERVICE);
        Notification notification = notificationWithProgressbarBuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(id, notification);
    }

    public void changeNotificationWithProgressbar(int max, int progress, int id, String contentTitle, String contentText, boolean ongoing) {
        notificationWithProgressbarBuilder.setContentTitle(contentTitle);
        notificationWithProgressbarBuilder.setContentText(contentText);
        notificationWithProgressbarBuilder.setPriority(Notification.PRIORITY_HIGH);
        notificationWithProgressbarBuilder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(contentTitle));
        notificationWithProgressbarBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));

        if (ongoing) {
            notificationWithProgressbarBuilder.setOngoing(true);
            if (progress == max) {
                notificationWithProgressbarBuilder.setProgress(max, progress, true);
            } else {
                notificationWithProgressbarBuilder.setProgress(max, progress, false);
            }
        } else {
            notificationWithProgressbarBuilder.setOngoing(false);
            notificationWithProgressbarBuilder.setProgress(max, progress, false);
        }
        notificationManager.notify(id, notificationWithProgressbarBuilder.build());
    }

    // same as above but without contentTitle
    public void changeNotificationWithProgressbar(int max, int progress, int id, String contentText, boolean ongoing) {
        notificationWithProgressbarBuilder.setContentText(contentText);
        notificationWithProgressbarBuilder.setPriority(Notification.PRIORITY_HIGH);
        notificationWithProgressbarBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
        if (ongoing) {
            notificationWithProgressbarBuilder.setOngoing(true);
            if (progress == max) {
                notificationWithProgressbarBuilder.setProgress(max, progress, true);
            } else {
                notificationWithProgressbarBuilder.setProgress(max, progress, false);
            }
        } else {
            notificationWithProgressbarBuilder.setOngoing(false);
            notificationWithProgressbarBuilder.setProgress(max, progress, false);
        }
        notificationManager.notify(id, notificationWithProgressbarBuilder.build());
    }

    public void cancelNotification(int id) {
        try {
            if (notificationManager != null)
                notificationManager.cancel(id);
        } catch (Exception e) {
            Log.e("Util", "Exception", e);
        }
    }

    public void cancelAllNotification() {
        try {
            if (notificationManager != null)
                notificationManager.cancelAll();
        } catch (Exception e) {
            Log.e("Util", "Exception", e);
        }
    }

    @SuppressWarnings("deprecation")
    public Spanned fromHtml(String source) {
        // https://stackoverflow.com/questions/37904739/html-fromhtml-deprecated-in-android-n
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    public int getGridColumnSizeFromWidth(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float dpWidth = displayMetrics.widthPixels / activity.getResources().getDisplayMetrics().density;
        int columnSize = (int) (dpWidth / 150);
        if (columnSize < 2)
            columnSize = 2;
        else if (columnSize > 6)
            columnSize = 6;
        return columnSize;
    }

    public String toCamelCase(String input) {
        StringBuilder camelCase = new StringBuilder();
        boolean nextCamelCase = true;
        for (char c : input.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '\'') { //Character.isSpaceChar(c)
                nextCamelCase = true;
            } else if (nextCamelCase) {
                c = Character.toTitleCase(c);
                nextCamelCase = false;
            }
            camelCase.append(c);
        }
        return camelCase.toString();
    }

    public String getFirstMatchDefault(String patron, String source, String mDefault) {
        Pattern p = Pattern.compile(patron, Pattern.DOTALL);
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group(1);
        } else {
            return mDefault;
        }
    }

    public String getFilePath(String url) {
        try {
            return getFirstMatchDefault(".+?\\.\\S{2,4}(\\/.+)", url, url);
        } catch (Exception e) {
            return url;
        }
    }

    public void removeSpecificCookies(Context context, String cookie) {
        int count = 0, subCount = 0, n = 0;
        SharedPreferences cookies = context.getSharedPreferences("CookiePersistence", Context.MODE_PRIVATE);
        Map cookieMap = cookies.getAll();
        Iterator entries = cookieMap.entrySet().iterator();
        String[] cookiesForToast = {"", "", "", "", ""}; // 125 cookies ought to be enough for anyone. ~Bill Gates
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            Object key = entry.getKey();
            //Object value = entry.getValue();
            if (key.toString().contains(cookie)) {
                /*Log.d("Util", "k: " + key.toString());
                Log.d("Util", "v: " + value.toString());*/
                if (subCount > 24) {
                    n++;
                    if (n > 4)
                        n = 4;
                    subCount = 0;
                }
                cookiesForToast[n] = cookiesForToast[n] + key.toString() + "\n";
                cookies.edit().remove(key.toString()).apply();
                count++;
                subCount++;
            }
        }
        if (count > 0)
            for (int i = 0; i <= n; i++) {
                toast(context, context.getString(R.string.deleted_no_var) + ": \n" + cookiesForToast[i]);
            }
        if (count == 1)
            toast(context, context.getString(R.string.deleted_no_var) + " " + count + " cookie");
        else
            toast(context, context.getString(R.string.deleted_no_var) + " " + count + " cookies");
        try {
            Navigator.getInstance().clearCookieJar(context);
        } catch (Exception e) {
            //todo
        }
    }

    public void removeAllCookies(Context context) {
        int count = 0, subCount = 0, n = 0;
        SharedPreferences cookies = context.getSharedPreferences("CookiePersistence", Context.MODE_PRIVATE);
        Map cookieMap = cookies.getAll();
        Iterator entries = cookieMap.entrySet().iterator();
        String[] cookiesForToast = {"", "", "", "", ""}; // 125 cookies ought to be enough for anyone. ~Bill Gates
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            Object key = entry.getKey();
            if (subCount > 24) {
                n++;
                if (n > 4)
                    n = 4;
                subCount = 0;
            }
            cookiesForToast[n] = cookiesForToast[n] + key.toString() + "\n";
            cookies.edit().remove(key.toString()).apply();
            count++;
            subCount++;
        }
        if (count > 0)
            for (int i = 0; i <= n; i++) {
                toast(context, context.getString(R.string.deleted_no_var) + ": \n" + cookiesForToast[i]);
            }
        if (count == 1)
            toast(context, context.getString(R.string.deleted_no_var) + " " + count + " cookie");
        else
            toast(context, context.getString(R.string.deleted_no_var) + " " + count + " cookies");

        try {
            Navigator.getInstance().clearCookieJar(context);
        } catch (Exception e) {
            //todo
        }
    }

    public boolean contains(int[] array, int value) {
        for (int i : array) {
            if (i == value) {
                return true;
            }
        }
        return false;
    }

    public boolean isGPServicesAvailable(Context context) {
        final int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        return status == ConnectionResult.SUCCESS;
    }

    private static class LazyHolder {
        private static final Util utilInstance = new Util();
    }

    public String unpack(String source) {
        String decoded = null;
        Pattern pat = Pattern.compile("eval(.+),(\\d+),(\\d+),'(.+?)'");
        Matcher m = pat.matcher(source);
        try {
            m.find();
            String p = m.group(1).replaceAll("\\\\", "");
            int a = Integer.parseInt(m.group(2));
            int c = Integer.parseInt(m.group(3));
            String[] k = m.group(4).split("\\|");
            while (c != 0) {
                c--;
                if ((k.length > c)) {
                    if (k[c].length() != 0) {
                        p = p.replaceAll("\\b" + baseT(c, a) + "\\b", k[c]);
                    }
                }
            }
            decoded = p;
        } catch (NumberFormatException e) {
            decoded = "Error";
        }
        return decoded;
    }

    private String baseT(int num, int radix) {
        int mNum = num;
        char[] digits = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        if (radix < 2 || radix > 62) {
            radix = 10;
        }
        if (mNum < radix) {
            return "" + digits[mNum];
        }
        boolean hayMas = true;
        String cadena = "";
        while (hayMas) {
            cadena = digits[mNum % radix] + cadena;
            mNum = mNum / radix;
            if (!(mNum > radix)) {
                hayMas = false;
                cadena = digits[mNum] + cadena;
            }
        }
        return cadena;
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String xorEncode(String s, String key) {
        return new String(Base64.encode(xor(s.getBytes(), key.getBytes()), Base64.DEFAULT));
    }

    public static String xorDecode(String s, String key) {
        return new String(xor(Base64.decode(s, Base64.DEFAULT), key.getBytes()));
    }

    private static byte[] xor(byte[] a, byte[] key) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ key[i % key.length]);
        }
        return out;
    }

}
