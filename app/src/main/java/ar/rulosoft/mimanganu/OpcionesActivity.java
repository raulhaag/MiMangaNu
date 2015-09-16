package ar.rulosoft.mimanganu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;

import ar.rulosoft.custompref.SeekBarDialogPref;
import ar.rulosoft.mimanganu.services.AlarmReceiver;
import ar.rulosoft.mimanganu.services.ChapterDownload;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.services.SingleDownload;

public class OpcionesActivity extends PreferenceActivity {

    SharedPreferences pm;
    boolean darkTheme;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        pm = PreferenceManager.getDefaultSharedPreferences(this);
        darkTheme = pm.getBoolean("dark_theme", false);
        setTheme(darkTheme ? R.style.AppTheme_miDark : R.style.AppTheme_miLight);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_preferences);

        /** This enables to hide finished mangas, just a toggle */
        final CheckBoxPreference cBoxPref =
                (CheckBoxPreference) getPreferenceManager().findPreference("mostrar_en_galeria");
        cBoxPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean valor = (Boolean) newValue;
                File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/MiMangaNu/", ".nomedia");
                if (!valor) if (f.exists()) f.delete();
                else if (!f.exists()) try {
                    f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
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
                    AlarmReceiver.setAlarms(getApplicationContext(),
                            System.currentTimeMillis() + time, time);
                } else {
                    AlarmReceiver.stopAlarms(getApplicationContext());
                }
                return true;
            }
        });

        /** This.. sets the Version Number, that's all */
        Preference prefAbout =
                getPreferenceManager().findPreference("about_text");
        prefAbout.setSummary("v" + BuildConfig.VERSION_NAME);

        Preference prefLicense = getPreferenceManager().findPreference("license_view");
        prefLicense.setIntent(new Intent(this, ActivityLicenseView.class));
    }
}
