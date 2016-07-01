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

import ar.rulosoft.mimanganu.MainActivity;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.utils.NetworkUtilsAndReciever;
import ar.rulosoft.navegadores.Navegador;

/**
 * Alarm Receiver
 * <p/>
 * Created by Raul on 09/07/2015.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public static final String LAST_CHECK = "last_check_update";
    private static final String CUSTOM_INTENT_ACTION = "ar.rulosoft.CHECK_UPDATES";

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
        try {
            SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(context);

            boolean only_wifi_updates = pm.getBoolean("update_only_wifi", false);
            boolean only_wifi = pm.getBoolean("only_wifi", false);
            NetworkUtilsAndReciever.reset();
            if(only_wifi_updates == true){
                only_wifi = true;
            }
            NetworkUtilsAndReciever.reset();
            if (NetworkUtilsAndReciever.getConnectionStatus(context, only_wifi) == NetworkUtilsAndReciever.ConnectionStatus.CONNECTED) {
                SearchUpdates searchUpdates = new SearchUpdates();
                searchUpdates.setContext(context);
                Navegador.TIME_OUT = Integer.parseInt(pm.getString("connection_timeout", "5"));
                pm.edit().putLong(LAST_CHECK, System.currentTimeMillis()).apply();
                searchUpdates.setSound(pm.getBoolean("update_sound", false));
                int threads = Integer.parseInt(pm.getString("update_threads_manual", "1"));
                searchUpdates.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, threads);
            }
            NetworkUtilsAndReciever.reset();
        } catch (Exception ignore) { //next time on connection go to update
        }
    }


    public class SearchUpdates extends AsyncTask<Integer, String, Void> {
        Context context;
        String res = "";
        int found = 0;
        NotificationManager mNotificationManager;
        NotificationCompat.Builder builder;
        int NOTIF_ID = 12598521;
        boolean sound = false;
        ArrayList<Manga> mangas;
        int keys = 3;


        public void setContext(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Integer... params) {
            keys = params[0];
            mangas = Database.getMangasForUpdates(context);
            for (int i = 0; i < mangas.size(); i++) {
                final int j = i;
                while (keys == 0)
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ignored) {
                    }
                keys--;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Manga manga = mangas.get(j);
                            ServerBase s = ServerBase.getServer(manga.getServerId());
                            publishProgress("(" + (j + 1) + "/" + mangas.size() + ")" + manga.getTitle());
                            s.loadChapters(manga, false);
                            int diff = s.searchForNewChapters(manga.getId(), context);
                            if (diff > 0) {
                                found += diff;
                                res = res + manga.getTitle() + " " + diff + " " + context.getString(R.string.new_manga_found) + "\n";
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            keys++;
                        }
                    }
                }).start();
            }
            while (keys < params[0])
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            builder.setContentTitle(values[0]);
            builder.setContentText(values[0]);
            mNotificationManager.notify(NOTIF_ID, builder.build());
        }

        @Override
        protected void onPreExecute() {
            mNotificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            builder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(context.getString(R.string.search_for_updates))
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
                Intent notificationIntent = new Intent(context, MainActivity.class);
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
