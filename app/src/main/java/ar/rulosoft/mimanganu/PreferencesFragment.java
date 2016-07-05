package ar.rulosoft.mimanganu;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.view.MenuItem;

import com.fedorvlasov.lazylist.FileCache;

import java.io.File;
import java.io.IOException;

import ar.rulosoft.custompref.ColorListDialogFragment;
import ar.rulosoft.custompref.ColorListDialogPref;
import ar.rulosoft.custompref.PreferenceListDirFragment;
import ar.rulosoft.custompref.PreferencesListDir;
import ar.rulosoft.custompref.SeekBarCustomPreference;
import ar.rulosoft.mimanganu.services.AlarmReceiver;
import ar.rulosoft.mimanganu.services.ChapterDownload;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.services.SingleDownload;
import ar.rulosoft.mimanganu.utils.NetworkUtilsAndReciever;
import ar.rulosoft.mimanganu.utils.Util;


public class PreferencesFragment extends PreferenceFragmentCompat {
    private SharedPreferences prefs;

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
    public void onCreatePreferences(Bundle bundle, String s) {
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

        /** Set summary for Reader preference + seamless chapter transitions summary **/
        // true: Paged Reader; false: Continuous Reader
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Boolean readType = prefs.getBoolean("reader_type", false);
        Preference reader_type = getPreferenceManager().findPreference("reader_type");
        Preference seamlessChapterTransitionsPref = getPreferenceManager().findPreference("seamless_chapter_transitions");
        if (readType) {
            reader_type.setTitle(getString(R.string.paged_reader));
            seamlessChapterTransitionsPref.setSummary(getString(R.string.seamless_chapter_transitions_paged_reader_subtitle));
        } else {
            reader_type.setTitle(getString(R.string.continuous_reader));
            seamlessChapterTransitionsPref.setSummary(getString(R.string.seamless_chapter_transitions_continuous_reader_subtitle));
        }
        final SwitchPreferenceCompat readerTypePref = (SwitchPreferenceCompat) getPreferenceManager().findPreference("reader_type");
        readerTypePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                Boolean readType = prefs.getBoolean("reader_type", false);
                Preference reader_type = getPreferenceManager().findPreference("reader_type");
                Preference seamlessChapterTransitions = getPreferenceManager().findPreference("seamless_chapter_transitions");
                if (!readType) {
                    reader_type.setTitle(getString(R.string.paged_reader));
                    seamlessChapterTransitions.setSummary(getString(R.string.seamless_chapter_transitions_paged_reader_subtitle));
                } else {
                    reader_type.setTitle(getString(R.string.continuous_reader));
                    seamlessChapterTransitions.setSummary(getString(R.string.seamless_chapter_transitions_continuous_reader_subtitle));
                }
                return true;
            }
        });

        /** enable / disable seamless_chapter_transitions_delete_read depending on the state of seamless_chapter_transitions **/
        Boolean seamlessChapterTransitions = prefs.getBoolean("seamless_chapter_transitions", false);
        Preference seamlessChapterTransitionsDeleteReadPreference = getPreferenceManager().findPreference("seamless_chapter_transitions_delete_read");
        if (seamlessChapterTransitions) {
            seamlessChapterTransitionsDeleteReadPreference.setEnabled(true);
        } else {
            seamlessChapterTransitionsDeleteReadPreference.setEnabled(false);
        }
        final SwitchPreferenceCompat seamlessChapterTransitionsSPC = (SwitchPreferenceCompat) getPreferenceManager().findPreference("seamless_chapter_transitions");
        seamlessChapterTransitionsSPC.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                Boolean seamlessChapterTransitions = prefs.getBoolean("seamless_chapter_transitions", false);
                Preference seamlessChapterTransitionsDeleteReadPreference = getPreferenceManager().findPreference("seamless_chapter_transitions_delete_read");
                if (!seamlessChapterTransitions) {
                    seamlessChapterTransitionsDeleteReadPreference.setEnabled(true);
                } else {
                    seamlessChapterTransitionsDeleteReadPreference.setEnabled(false);

                    SharedPreferences.Editor prefEdit = prefs.edit();
                    prefEdit.putBoolean("seamless_chapter_transitions_delete_read", false);
                    prefEdit.apply();
                }
                return true;
            }
        });

        /** This sets the download threads (parallel downloads) */
        final SeekBarCustomPreference listPreferenceDT = (SeekBarCustomPreference) getPreferenceManager().findPreference("download_threads");
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
        final SeekBarCustomPreference listPrefET = (SeekBarCustomPreference) getPreferenceManager().findPreference("error_tolerancia");
        listPrefET.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ChapterDownload.MAX_ERRORS = (int) newValue;
                return true;
            }
        });

        /** This sets the number of retries to fetch images */
        SeekBarCustomPreference listPrefRT = (SeekBarCustomPreference) getPreferenceManager().findPreference("reintentos");
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

        final SwitchPreferenceCompat onlyWifiUpdateSwitch = (SwitchPreferenceCompat)getPreferenceManager().findPreference("update_only_wifi");

        SwitchPreferenceCompat onlyWifiSwitch = (SwitchPreferenceCompat) getPreferenceManager().findPreference("only_wifi");
        onlyWifiSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                NetworkUtilsAndReciever.ONLY_WIFI = (Boolean) o;
                NetworkUtilsAndReciever.connectionStatus = NetworkUtilsAndReciever.ConnectionStatus.UNCHECKED;
                onlyWifiUpdateSwitch.setEnabled(!(Boolean) o);
                return true;
            }
        });

        if(onlyWifiSwitch.isChecked()){
            onlyWifiUpdateSwitch.setEnabled(false);
        }else{
            onlyWifiUpdateSwitch.setEnabled(true);
        }


        final Preference prefClearCache = getPreferenceManager().findPreference("clear_cache");
        prefClearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Preference clearCache = getPreferenceManager().findPreference("clear_cache");
                clearCache.setEnabled(false);
                new FileCache(getActivity()).clearCache();
                return true;
            }
        });

        final Preference prefResetServersToDefaults = getPreferenceManager().findPreference("reset_server_list_to_defaults");
        prefResetServersToDefaults.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Preference resetServerListToDefaults = getPreferenceManager().findPreference("reset_server_list_to_defaults");
                resetServerListToDefaults.setEnabled(false);
                prefs.edit().putString("unused_servers", "").apply();
                return true;
            }
        });

        setFirstRunDefaults();
    }

    private void setFirstRunDefaults() {
        final String PREFS_NAME = "fragment_preferences";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        // Get current version code
        int currentVersionCode;
        try {
            currentVersionCode = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }
        // Get saved version code
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {
            // This is just a normal run
            return;
        } else if (savedVersionCode == DOESNT_EXIST || currentVersionCode > savedVersionCode) {
            // This is a new install or upgrade

            setNumberOfThreadsToBeEqualToNumberOfCores(4, "update_threads_manual");
            setNumberOfThreadsToBeEqualToNumberOfCores(4, "download_threads");
            setNumberOfThreadsToBeEqualToNumberOfCores(4, "update_threads_background");
        }
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }

    private void setNumberOfThreadsToBeEqualToNumberOfCores(int threadsMax, String preference) {
        int threads, availableProcessors = Runtime.getRuntime().availableProcessors();
        if (availableProcessors <= threadsMax) {
            threads = availableProcessors;
        } else {
            threads = threadsMax;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(preference, "" + threads).apply();

        final SeekBarCustomPreference tmpSeekbar = (SeekBarCustomPreference) getPreferenceManager().findPreference(preference);
        tmpSeekbar.setProgress(threads);
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
    public final void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment fragment;
        if (preference instanceof PreferencesListDir) {
            fragment = PreferenceListDirFragment.newInstance(preference);
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else if (preference instanceof ColorListDialogPref) {
            fragment = ColorListDialogFragment.newInstance(preference);
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else super.onDisplayPreferenceDialog(preference);
    }

    public class calcStorage extends AsyncTask<String, Void, Long> {
        Preference prefStoreStat = getPreferenceManager().findPreference("stat_storage");
        Preference prefRamUsage = getPreferenceManager().findPreference("ram_usage");

        @Override
        protected void onPreExecute() {
            prefRamUsage.setSummary(Debug.getPss() / 1024 + " MB");
        }

        @Override
        protected Long doInBackground(String... strings) {
            long store_total = 0;
            File[] listStore = new File(strings[0]).listFiles();
            for (final File oneFold : listStore) {
                if (oneFold.getName().equals("cache") || oneFold.getName().equals("dbs"))
                    continue;
                store_total += Util.getInstance().dirSize(oneFold);

                final double cSize = store_total / (1024.0 * 1024.0);
                if (cSize > 1024.0) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            prefStoreStat.setSummary("calculating... ~" + String.format("%.2f", cSize / 1024.0) + " GB");
                        }
                    });
                }
                else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            prefStoreStat.setSummary("calculating... ~" + String.format("%.2f", cSize) + " MB");
                        }
                    });
                }
                publishProgress();
            }
            return store_total;
        }

        @Override
        protected void onPostExecute(Long l) {
            double cSize = l / (1024.0 * 1024.0);
            if (cSize > 1024.0)
                prefStoreStat.setSummary(String.format("%.2f", cSize / 1024.0) + " GB");
            else
                prefStoreStat.setSummary(String.format("%.2f", cSize) + " MB");
            super.onPostExecute(l);
        }
    }
}

