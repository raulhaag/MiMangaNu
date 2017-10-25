package ar.rulosoft.mimanganu.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import ar.rulosoft.navegadores.Navigator;

public class InitGlobals extends AsyncTask<Context, Void, Void> {
    @Override
    protected Void doInBackground(Context... contexts) {
        try {
            NetworkUtilsAndReceiver.ONLY_WIFI = PreferenceManager.getDefaultSharedPreferences(contexts[0]).getBoolean("only_wifi", false);
            Navigator.initialiseInstance(contexts[0]);
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        return null;
    }
}