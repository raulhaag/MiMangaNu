package ar.rulosoft.mimanganu.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

import ar.rulosoft.mimanganu.MainActivity;
import ar.rulosoft.mimanganu.utils.NetworkUtilsAndReceiver;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;

/**
 * Created by Raúl on 27/12/2017.
 */

public class UpdateJobCreator implements JobCreator {
    public static final String UPDATE_TAG = "MIMANGANU_UPDATE";

    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case UPDATE_TAG:
                return new UpdateJob();
            default:
                return null;
        }
    }

    public static class UpdateJob extends Job {
        static final String LAST_CHECK = "last_check_update";
        private SharedPreferences pm;
        private AutomaticUpdateTask automaticUpdateTask;

        public static void scheduleJob(Context context) {
            SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(context);
            long time = Long.parseLong(pm.getString("update_interval", "0"));
            JobManager.instance().cancelAllForTag(UpdateJobCreator.UPDATE_TAG);
            if (time > 0) {
                new JobRequest.Builder(UpdateJobCreator.UPDATE_TAG)
                        .setPeriodic(time, TimeUnit.MINUTES.toMillis(5))
                        .setRequiresCharging(false)
                        .setRequiresDeviceIdle(false)
                        .setUpdateCurrent(true)
                        .build()
                        .schedule();
            }
            if (time < 0)
                MainActivity.coldStart = false;
        }

        @NonNull
        @Override
        protected Result onRunJob(@NonNull Params params) {
            try {
                pm = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean only_wifi_updates = pm.getBoolean("update_only_wifi", false);
                boolean only_wifi = pm.getBoolean("only_wifi", false);
                if (only_wifi_updates) {
                    only_wifi = true;
                }
                NetworkUtilsAndReceiver.reset();
                if (NetworkUtilsAndReceiver.getConnectionStatus(getContext(), only_wifi) == NetworkUtilsAndReceiver.ConnectionStatus.CONNECTED) {
                    Navigator.connectionTimeout = Integer.parseInt(pm.getString("connection_timeout", "10"));
                    pm.edit().putLong(LAST_CHECK, System.currentTimeMillis()).apply();
                    NetworkUtilsAndReceiver.ONLY_WIFI = pm.getBoolean("only_wifi", false);
                    Navigator.initialiseInstance(getContext());
                    automaticUpdateTask = new AutomaticUpdateTask(getContext(), null, pm, null);
                    Handler mainHandler = new Handler(getContext().getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            automaticUpdateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    });
                }
                NetworkUtilsAndReceiver.reset();
            } catch (Exception ignore) { //next time on connection go to update
                ignore.printStackTrace();
                Util.getInstance().cancelAllNotification();
            }
            return Result.SUCCESS;
        }

    }
}
