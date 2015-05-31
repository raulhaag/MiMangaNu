package ar.rulosoft.mimanganu;

import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.widget.ListView;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.adapters.DownloadAdapter;
import ar.rulosoft.mimanganu.services.DescargaCapitulo;
import ar.rulosoft.mimanganu.services.ServicioColaDeDescarga;
import ar.rulosoft.mimanganu.utils.ThemeColors;


public class ActivityDescargas extends ActionBarActivity {

    ListView lista;
    MostrarDescargas md;
    DownloadAdapter adap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descargas);
        int[] colors = ThemeColors.getColors(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()), getApplicationContext());
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colors[0]));
        lista = (ListView) findViewById(R.id.descargas);
    }

    @Override
    public void onResume() {
        adap = new DownloadAdapter(ActivityDescargas.this, new ArrayList<DescargaCapitulo>());
        lista.setAdapter(adap);
        md = new MostrarDescargas();
        md.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        super.onResume();
    }

    @Override
    public void onPause() {
        md.stop();
        super.onPause();
    }


    private class MostrarDescargas extends AsyncTask<Void, Void, Void> {
        boolean seguir = true;

        @Override
        protected Void doInBackground(Void... params) {
            while (seguir) {
                try {
                    adap.updateAll(ServicioColaDeDescarga.descargas);
                    publishProgress();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ActivityDescargas.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adap.notifyDataSetChanged();
                }
            });
            super.onProgressUpdate(values);
        }

        public void stop() {
            seguir = false;
        }

    }
}
