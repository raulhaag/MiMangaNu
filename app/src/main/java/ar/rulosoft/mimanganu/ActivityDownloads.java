package ar.rulosoft.mimanganu;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.widget.ListView;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.adapters.DownloadAdapter;
import ar.rulosoft.mimanganu.services.ChapterDownload;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.utils.ThemeColors;


public class ActivityDownloads extends ActionBarActivity {
    SharedPreferences pm;
    boolean darkTheme;
    private ListView list;
    private ShowDownloadsTask sh;
    private DownloadAdapter adap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pm = PreferenceManager.getDefaultSharedPreferences(this);
        darkTheme = pm.getBoolean("dark_theme", false);
        setTheme(darkTheme ? R.style.AppTheme_miDark : R.style.AppTheme_miLight);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descargas);
        int[] colors = ThemeColors.getColors(
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()),
                getApplicationContext());
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colors[0]));
        list = (ListView) findViewById(R.id.descargas);
    }

    @Override
    public void onResume() {
        adap = new DownloadAdapter(ActivityDownloads.this, new ArrayList<ChapterDownload>(), darkTheme);
        list.setAdapter(adap);
        sh = new ShowDownloadsTask();
        sh.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        super.onResume();
    }

    @Override
    public void onPause() {
        sh.stop();
        super.onPause();
    }

    private class ShowDownloadsTask extends AsyncTask<Void, Void, Void> {
        boolean _continue = true;

        @Override
        protected Void doInBackground(Void... params) {
            while (_continue) {
                try {
                    adap.updateAll(DownloadPoolService.descargas);
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
            ActivityDownloads.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adap.notifyDataSetChanged();
                }
            });
            super.onProgressUpdate(values);
        }

        public void stop() {
            _continue = false;
        }

    }
}
