package ar.rulosoft.mimanganu;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.support.v7.preference.XpPreferenceFragment;
import android.view.MenuItem;

import com.fedorvlasov.lazylist.FileCache;

import net.xpece.android.support.preference.ListPreference;

import java.io.File;
import java.io.IOException;

import ar.rulosoft.custompref.ColorListDialogFragment;
import ar.rulosoft.custompref.ColorListDialogPref;
import ar.rulosoft.custompref.PreferenceListDirFragment;
import ar.rulosoft.custompref.PreferencesListDir;
import ar.rulosoft.custompref.SeekBarCustomPreference;
import ar.rulosoft.custompref.SeekBarPreference2;
import ar.rulosoft.custompref.SeekbarPreferenceFragment;
import ar.rulosoft.mimanganu.services.AlarmReceiver;
import ar.rulosoft.mimanganu.services.ChapterDownload;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.services.SingleDownload;


public class PreferencesFragment extends XpPreferenceFragment implements PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback {
    private SharedPreferences prefs;
    private FileCache mFileStorage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    @Override
    public void onCreatePreferences2(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.fragment_preferences);
        ColorListDialogPref primaryColor = (ColorListDialogPref) getPreferenceManager().findPreference("primario");
        primaryColor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                ((MainActivity) getActivity()).setColorToBars();
                return false;
            }
        });

        /** Once, create necessary Data */
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        final String current_filepath = prefs.getString("directorio",
                Environment.getExternalStorageDirectory().getAbsolutePath()) + "/MiMangaNu/";

        /** This enables to hide downloaded images from gallery, just a toggle */
        final SwitchPreferenceCompat cBoxPref = (SwitchPreferenceCompat) getPreferenceManager().findPreference("mostrar_en_galeria");
        cBoxPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
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
        final SeekBarPreference2 listPreferenceDT = (SeekBarPreference2) getPreferenceManager().findPreference("download_threads");
        listPreferenceDT.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int threads = (int) newValue;
                int antes = DownloadPoolService.SLOTS;
                DownloadPoolService.SLOTS = threads;
                if (DownloadPoolService.actual != null)
                    DownloadPoolService.actual.slots += (threads - antes);
                return true;
            }
        });

        /** This sets the maximum number of errors to tolerate */
        final SeekBarPreference2 listPrefET = (SeekBarPreference2) getPreferenceManager().findPreference("error_tolerancia");
        listPrefET.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ChapterDownload.MAX_ERRORS = (int) newValue;
                return true;
            }
        });

        /** This sets the number of retries to fetch images */
        SeekBarPreference2 listPrefRT = (SeekBarPreference2) getPreferenceManager().findPreference("reintentos");
        listPrefRT.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SingleDownload.RETRY = (int) newValue;
                return true;
            }
        });

        /** This sets the Update Interval of the mangas (i.e. once per week) */
        final ListPreference listPrefCU = (ListPreference) getPreferenceManager().findPreference("update_interval");
        listPrefCU.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
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
        prefLicense.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((MainActivity) getActivity()).replaceFragment(new LicenseFragment(), "licence_fragment");
                return false;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).enableHomeButton(true);
        ((MainActivity) getActivity()).setTitle(getString(R.string.action_settings));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            getActivity().onBackPressed();
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onPreferenceDisplayDialog(PreferenceFragmentCompat preferenceFragmentCompat, Preference preference) {
        DialogFragment fragment;
        if (preference instanceof PreferencesListDir) {
            fragment = PreferenceListDirFragment.newInstance(preference);
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
            return true;
        } else if (preference instanceof ColorListDialogPref) {
            fragment = ColorListDialogFragment.newInstance(preference);
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
            return true;
        } else if (preference instanceof SeekBarCustomPreference) {
            fragment = SeekbarPreferenceFragment.newInstance(preference);
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
            return true;
        } else return false;
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
            Preference prefStoreStat = getPreferenceManager().findPreference("stat_storage");
            prefStoreStat.setSummary(String.format("%.2f", l / (1024.0 * 1024.0)) + " MB");
            super.onPostExecute(l);
        }
    }
}

