package ar.rulosoft.mimanganu;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;

import java.util.HashMap;

import ar.rulosoft.mimanganu.adapters.DownloadAdapter;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.utils.ThemeColors;


public class ActivityDownloads extends AppCompatActivity {
    public boolean darkTheme;
    private ListView list;
//    private ShowDownloadsTask sh;
    private DownloadAdapter downloadAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(this);
        darkTheme = pm.getBoolean("dark_theme", false);
        setTheme(darkTheme ? R.style.AppTheme_miDark : R.style.AppTheme_miLight);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descargas);
        int[] colors = ThemeColors.getColors(pm, getApplicationContext());
        android.support.v7.app.ActionBar mActBar = getSupportActionBar();
        if (mActBar != null) {
            mActBar.setBackgroundDrawable(new ColorDrawable(colors[0]));
            mActBar.setDisplayHomeAsUpEnabled(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setNavigationBarColor(colors[0]);
            window.setStatusBarColor(colors[4]);
        }
        list = (ListView) findViewById(R.id.descargas);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.remove_downloaded:
                DownloadPoolService.removeDownloaded();
                downloadAdapter = new DownloadAdapter(ActivityDownloads.this, ActivityDownloads.this, darkTheme);
                list.setAdapter(downloadAdapter);
                break;
            case R.id.pause_downloads:
                DownloadPoolService.pauseDownload();
                break;
            case R.id.retry_errors:
                DownloadPoolService.retryError(ActivityDownloads.this);
                break;
            case R.id.resume_downloads:
                DownloadPoolService.resumeDownloads(ActivityDownloads.this);
                break;
            case R.id.remove_all:
                DownloadPoolService.removeAll();
                downloadAdapter = new DownloadAdapter(ActivityDownloads.this, ActivityDownloads.this, darkTheme);
                list.setAdapter(downloadAdapter);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        downloadAdapter = new DownloadAdapter(ActivityDownloads.this, ActivityDownloads.this, darkTheme);
        list.setAdapter(downloadAdapter);
    }

    @Override
    public void onPause() {
        downloadAdapter.onPause();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_downloads, menu);
        return true;
    }
}
