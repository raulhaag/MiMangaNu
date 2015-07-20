package ar.rulosoft.mimanganu.services;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.ActivityMisMangas;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

/**
 * Created by Raul on 09/07/2015.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String CUSTOM_INTENT_ACTION = "ar.rulosoft.CHECK_UPDATES";
    private static final String LAST_CHECK = "last_check_update";

    public static void stopAlarms(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.setAction(CUSTOM_INTENT_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        alarmManager.cancel(pendingIntent);
    }

    public static void setAlarms(Context context, long start, long interval) {
        stopAlarms(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pintent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        alarmIntent.setAction(CUSTOM_INTENT_ACTION);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, start, interval, pintent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SearchUpdates su = new SearchUpdates();
        su.setContext(context);
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(context);
        pm.edit().putLong(LAST_CHECK, System.currentTimeMillis()).commit();
        su.setSound(pm.getBoolean("update_sound", false));
        su.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public class SearchUpdates extends AsyncTask<Void, Void, Void> {
        Context context;
        String res = "";
        int found = 0;
        NotificationManager mNotificationManager;
        NotificationCompat.Builder builder;
        int NOTIF_ID = 12598521;
        boolean sound = false;

        public void setContext(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<Manga> mangas = Database.getMangasForUpdates(context);
            for (int i = 0; i < mangas.size(); i++) {
                Manga manga = mangas.get(i);
                ServerBase s = ServerBase.getServer(manga.getServerId());
                try {
                    s.loadChapters(manga, false);
                    int diff = s.searchForNewChapters(manga.getId(), context);
                    if (diff > 0) {
                        found = found + diff;
                        res = res + manga.getTitle() + " " + diff + " " + context.getString(R.string.new_manga_found) + "\n";
                    }
                } catch (Exception e) {

                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            builder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("Search for updates")
                            .setContentText("MiMangaNu");
            mNotificationManager.notify(NOTIF_ID, builder.build());
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (found > 0) {
                NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle(builder);
                bigTextStyle.bigText(res);
                if (found > 1) {
                    builder.setContentTitle(context.getString(R.string.new_mangas_found));
                    bigTextStyle.setBigContentTitle(context.getString(R.string.new_mangas_found));
                } else {
                    builder.setContentTitle(found + " " + context.getString(R.string.new_mangas_found));
                    bigTextStyle.setBigContentTitle(found + " " + context.getString(R.string.new_mangas_found));
                }
                builder.setContentText(res.substring(0, res.indexOf("\n")));
                builder.setStyle(bigTextStyle);
                builder.setAutoCancel(true);
                if (sound) {
                    builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
                }
                Intent notificationIntent = new Intent(context, ActivityMisMangas.class);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent);
                mNotificationManager.notify(NOTIF_ID, builder.build());
                super.onPostExecute(aVoid);
            } else {
                mNotificationManager.cancel(NOTIF_ID);
            }
            super.onPostExecute(aVoid);
        }

        public void setSound(boolean sound) {
            this.sound = sound;
        }
    }

}
