package ar.rulosoft.mimanganu.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootAndUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(context);
            long interval = Long.parseLong(pm.getString("update_interval", "0"));
            if (interval != 0) {
                long last_check = pm.getLong("last_check_update", 0);
                long dif = System.currentTimeMillis() - last_check;
                if (dif > interval) {
                    AlarmReceiver.setAlarms(context, System.currentTimeMillis() + 30000, interval);
                } else {
                    AlarmReceiver.setAlarms(context, System.currentTimeMillis() - dif + interval, interval);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}