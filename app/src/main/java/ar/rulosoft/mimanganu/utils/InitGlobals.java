package ar.rulosoft.mimanganu.utils;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import ar.rulosoft.mimanganu.MainActivity;
import ar.rulosoft.navegadores.Navigator;

public class InitGlobals extends AsyncTask<SharedPreferences, Void, Void> {

    @Override
    protected Void doInBackground(SharedPreferences... pm) {
        try {
            NetworkUtilsAndReciever.ONLY_WIFI = pm[0].getBoolean("only_wifi", false);
            MainActivity.navigator = new Navigator();
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        return null;
    }
}