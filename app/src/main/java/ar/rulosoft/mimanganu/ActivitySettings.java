package ar.rulosoft.mimanganu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;

import com.fedorvlasov.lazylist.FileCache;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import ar.rulosoft.custompref.SeekBarDialogPref;
import ar.rulosoft.mimanganu.services.AlarmReceiver;
import ar.rulosoft.mimanganu.services.ChapterDownload;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.services.SingleDownload;
import ar.rulosoft.mimanganu.utils.ThemeColors;

public class ActivitySettings extends AppCompatActivity {
    private boolean darkTheme;

    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        darkTheme = prefs.getBoolean("dark_theme", false);
        setTheme(darkTheme ? R.style.AppTheme_miDark : R.style.AppTheme_miLight);
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PreferencesFragment()).commit();
        int[] colors = ThemeColors.getColors(prefs, getApplicationContext());
        ActionBar mActBar = getSupportActionBar();
        if (mActBar != null) {
            mActBar.setBackgroundDrawable(new ColorDrawable(colors[0]));
            mActBar.setDisplayHomeAsUpEnabled(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setNavigationBarColor(colors[0]);
            window.setStatusBarColor(colors[4]);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PreferencesFragment extends PreferenceFragment {
        private SharedPreferences prefs;
        private FileCache mFileStorage;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            // Load the preferences from an XML resource
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.fragment_preferences);

            /** Once, create necessary Data */
            prefs = PreferenceManager
                    .getDefaultSharedPreferences(getActivity().getApplicationContext());
            final String current_filepath = prefs.getString("directorio",
                    Environment.getExternalStorageDirectory().getAbsolutePath()) + "/MiMangaNu/";

            /** This enables to hide downloaded images from gallery, just a toggle */
            final SwitchPreference cBoxPref =
                    (SwitchPreference) getPreferenceManager().findPreference("mostrar_en_galeria");
            cBoxPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean have_noMedia = (Boolean) newValue;

                    if (android.os.Environment.getExternalStorageState()
                            .equals(android.os.Environment.MEDIA_MOUNTED)) {
                        File mimaFolder = new File(current_filepath, ".nomedia");

                        if (have_noMedia) {
                            try {
                                mimaFolder.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (mimaFolder.exists()) {
                                mimaFolder.delete();
                            }
                        }
                    }

                    return true;
                }
            });

            /** This sets the download threads (parallel downloads) */
            final SeekBarDialogPref listPreferenceDT =
                    (SeekBarDialogPref) getPreferenceManager().findPreference("download_threads");
            listPreferenceDT.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int threads = Integer.parseInt((String) newValue);
                    int antes = DownloadPoolService.SLOTS;
                    DownloadPoolService.SLOTS = threads;
                    if (DownloadPoolService.actual != null)
                        DownloadPoolService.actual.slots += (threads - antes);
                    return true;
                }
            });

            /** This sets the maximum number of errors to tolerate */
            final SeekBarDialogPref listPrefET =
                    (SeekBarDialogPref) getPreferenceManager().findPreference("error_tolerancia");
            listPrefET.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ChapterDownload.MAX_ERRORS =
                            Integer.parseInt((String) newValue);
                    return true;
                }
            });

            /** This sets the number of retries to fetch images */
            SeekBarDialogPref listPrefRT =
                    (SeekBarDialogPref) getPreferenceManager().findPreference("reintentos");
            listPrefRT.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SingleDownload.RETRY = Integer.parseInt((String) newValue);
                    return true;
                }
            });

            /** This sets the Update Interval of the mangas (i.e. once per week) */
            final ListPreference listPrefCU =
                    (ListPreference) getPreferenceManager().findPreference("update_interval");
            listPrefCU.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    long time = Long.parseLong((String) newValue);
                    if (time > 0) {
                        AlarmReceiver.setAlarms(getActivity().getApplicationContext(),
                                System.currentTimeMillis() + time, time);
                    } else {
                        AlarmReceiver.stopAlarms(getActivity().getApplicationContext());
                    }
                    return true;
                }
            });

            /** This.. sets the Version Number, that's all */
            final Preference prefAbout = getPreferenceManager().findPreference("about_text");
            prefAbout.setSummary("v" + BuildConfig.VERSION_NAME);

            /** This will check how much storage is taken by the mangas */
            new calcStorage().execute(current_filepath);

            final Preference prefLicense = getPreferenceManager().findPreference("license_view");
            prefLicense.setIntent(new Intent(getActivity(), ActivityLicenseView.class));
        }

        public class calcStorage extends AsyncTask<String, Void, Long> {
            @Override
            protected Long doInBackground(String... strings) {
                mFileStorage = new FileCache(getActivity().getApplicationContext());

                long store_total = 0;
                File[] listStore = new File(strings[0]).listFiles();
                for (final File oneFold : listStore) {
                    if (oneFold.getName().equals("cache") || oneFold.getName().equals("dbs"))
                        continue;
                    store_total += mFileStorage.dirSize(oneFold);
                }
                return store_total;
            }

            @Override
            protected void onPostExecute(Long l) {
                Preference prefStoreStat =
                        getPreferenceManager().findPreference("stat_storage");
                prefStoreStat.setSummary(
                        String.format("%.2f", l / (1024.0 * 1024.0)) + " MB");

                super.onPostExecute(l);
            }
        }

    }
}
