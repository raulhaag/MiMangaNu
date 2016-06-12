package ar.rulosoft.mimanganu.utils;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import ar.rulosoft.navegadores.Navegador;

public class InitGlobals extends AsyncTask<SharedPreferences,Void,Void> {

        @Override
        protected Void doInBackground(SharedPreferences... pm) {
            try{
                Navegador.TIME_OUT = Integer.parseInt(pm[0].getString("connection_timeout","5"));
            }catch (Exception ignore){
                ignore.printStackTrace();
            }
            return null;
        }
    }