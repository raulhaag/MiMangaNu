package ar.rulosoft.mimanganu;

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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;

import com.fedorvlasov.lazylist.FileCache;

import java.io.File;
import java.io.IOException;

import ar.rulosoft.custompref.ColorListDialogFragment;
import ar.rulosoft.custompref.ColorListDialogPref;
import ar.rulosoft.custompref.PreferenceListDirFragment;
import ar.rulosoft.custompref.PreferencesListDir;
import ar.rulosoft.custompref.SeekBarCustomPreference;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.services.AlarmReceiver;
import ar.rulosoft.mimanganu.services.ChapterDownload;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.services.SingleDownload;
import ar.rulosoft.mimanganu.utils.NetworkUtilsAndReceiver;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;


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
        Preference seamlessChapterTransitionsDeleteReadPreference = getPreferenceManager().findPreference("seamless_chapter_transitions_delete_read");
        if (prefs.getBoolean("seamless_chapter_transitions", false)) {
            seamlessChapterTransitionsDeleteReadPreference.setEnabled(true);
        } else {
            seamlessChapterTransitionsDeleteReadPreference.setEnabled(false);
        }
        final SwitchPreferenceCompat seamlessChapterTransitionsSPC = (SwitchPreferenceCompat) getPreferenceManager().findPreference("seamless_chapter_transitions");
        seamlessChapterTransitionsSPC.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                Preference seamlessChapterTransitionsDeleteReadPreference = getPreferenceManager().findPreference("seamless_chapter_transitions_delete_read");
                if (!prefs.getBoolean("seamless_chapter_transitions", false)) {
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

        /** enable / disable auto_import_path depending on the state of auto_import **/
        Preference autoImportPath = getPreferenceManager().findPreference("auto_import_path");
        if (prefs.getBoolean("auto_import", false)) {
            autoImportPath.setEnabled(true);
        } else {
            autoImportPath.setEnabled(false);
        }
        final SwitchPreferenceCompat autoImportSPC = (SwitchPreferenceCompat) getPreferenceManager().findPreference("auto_import");
        autoImportSPC.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                Preference autoImportPath = getPreferenceManager().findPreference("auto_import_path");
                if (!prefs.getBoolean("auto_import", false)) {
                    autoImportPath.setEnabled(true);
                } else {
                    autoImportPath.setEnabled(false);
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

                if(time < 0)
                    MainActivity.coldStart = false;

                return true;
            }
        });

        /** This sets the Version Number, that's all */
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
                NetworkUtilsAndReceiver.ONLY_WIFI = (Boolean) o;
                NetworkUtilsAndReceiver.connectionStatus = NetworkUtilsAndReceiver.ConnectionStatus.UNCHECKED;
                onlyWifiUpdateSwitch.setEnabled(!(Boolean) o);
                return true;
            }
        });

        if(onlyWifiSwitch.isChecked()){
            onlyWifiUpdateSwitch.setEnabled(false);
        }else{
            onlyWifiUpdateSwitch.setEnabled(true);
        }

        final SeekBarCustomPreference seekBarConnectionTimeout = (SeekBarCustomPreference) getPreferenceManager().findPreference("connection_timeout");
        seekBarConnectionTimeout.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Navigator.connectionTimeout = Integer.parseInt(prefs.getString("connection_timeout", "10"));
                return true;
            }
        });

        final SeekBarCustomPreference seekBarWriteTimeout = (SeekBarCustomPreference) getPreferenceManager().findPreference("write_timeout");
        seekBarWriteTimeout.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Navigator.writeTimeout = Integer.parseInt(prefs.getString("write_timeout", "10"));
                return true;
            }
        });

        final SeekBarCustomPreference seekBarReadTimeout = (SeekBarCustomPreference) getPreferenceManager().findPreference("read_timeout");
        seekBarReadTimeout.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("PF","rt: "+Integer.parseInt(prefs.getString("read_timeout", "30")));
                Navigator.readTimeout = Integer.parseInt(prefs.getString("read_timeout", "30"));
                return true;
            }
        });

        Preference gridColumnsPref = getPreferenceManager().findPreference("grid_columns");
        if (Integer.parseInt(prefs.getString("grid_columns", "2")) == 1) {
            gridColumnsPref.setSummary("Automatic Detection");
        } else {
            gridColumnsPref.setSummary(getString(R.string.grid_columns_subtitle));
        }
        final SeekBarCustomPreference seekBarGridColumns = (SeekBarCustomPreference) getPreferenceManager().findPreference("grid_columns");
        seekBarGridColumns.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("PF","onPrefChange");
                Preference gridColumnsPref = getPreferenceManager().findPreference("grid_columns");
                Log.d("PF", "gridColumns: " + Integer.parseInt(prefs.getString("grid_columns", "2")));
                if (Integer.parseInt(prefs.getString("grid_columns", "2")) == 1) {
                    gridColumnsPref.setSummary("Automatic Detection");
                } else {
                    gridColumnsPref.setSummary(getString(R.string.grid_columns_subtitle));
                }
                return true;
            }
        });

        final Preference prefClearCache = getPreferenceManager().findPreference("clear_cache");
        prefClearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Preference clearCache = getPreferenceManager().findPreference("clear_cache");
                clearCache.setEnabled(false);
                Thread t0 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new FileCache(getActivity()).clearCache();
                    }
                });

                Thread t1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Util.getInstance().createNotificationWithProgressbar(getContext(), 69, getString(R.string.deleting_empty_directories), "");
                        Util.getInstance().deleteEmptyDirectoriesRecursive(new File(current_filepath));
                        Util.getInstance().cancelNotification(69);
                    }
                });

                t0.start();
                t1.start();
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

        final Preference prefVacuumDatabase = getPreferenceManager().findPreference("vacuum_database");
        prefVacuumDatabase.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Preference vacuumDatabase = getPreferenceManager().findPreference("vacuum_database");
                vacuumDatabase.setEnabled(false);
                Thread t0 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Util.getInstance().createNotificationWithProgressbar(getContext(), 70, getString(R.string.vacuum_database_notification_text), "");
                        Database.vacuumDatabase(getContext());
                        Util.getInstance().cancelNotification(70);
                    }
                });
                t0.start();
                return true;
            }
        });

        setFirstRunDefaults();
    }

    private void setFirstRunDefaults() {
        final String PREF_VERSION_CODE_KEY = "version_code";
        int currentVersionCode;
        try {
            currentVersionCode = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, -1);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {
            // This is just a normal run
            return;
        } else if (savedVersionCode == -1 || currentVersionCode > savedVersionCode) {
            // This is a new install or upgrade

            setNumberOfThreadsToBeEqualToNumberOfCores(4, "update_threads_manual");
            setNumberOfThreadsToBeEqualToNumberOfCores(4, "download_threads");
            setNumberOfThreadsToBeEqualToNumberOfCores(4, "update_threads_background");

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("connection_timeout", "10").apply();
            editor.putString("write_timeout", "10").apply();
            editor.putString("read_timeout", "30").apply();
            int tmpGridColumns = getGridColumnSizeFromWidth();
            editor.putString("grid_columns", "" + tmpGridColumns).apply();
            final SeekBarCustomPreference tmpSeekBar = (SeekBarCustomPreference) getPreferenceManager().findPreference("grid_columns");
            tmpSeekBar.setProgress(tmpGridColumns);
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

        final SeekBarCustomPreference tmpSeekBar = (SeekBarCustomPreference) getPreferenceManager().findPreference(preference);
        tmpSeekBar.setProgress(threads);
    }

    private int getGridColumnSizeFromWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float dpWidth = displayMetrics.widthPixels / getResources().getDisplayMetrics().density;
        int columnSize = (int) (dpWidth / 150);
        if (columnSize < 2)
            columnSize = 2;
        else if (columnSize > 6)
            columnSize = 6;
        return columnSize;
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
        boolean allOk = true;
        String error = "";

        @Override
        protected void onPreExecute() {
            prefRamUsage.setSummary(Debug.getPss() / 1024 + " MB");
        }

        @Override
        protected Long doInBackground(String... strings) {
            long store_total = 0;
            try {
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
                    } else {
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
            } catch (Exception e) {
                allOk = false;
                error = Log.getStackTraceString(e);
            }
            return store_total;
        }

        @Override
        protected void onPostExecute(Long l) {
            if (allOk) {
                double cSize = l / (1024.0 * 1024.0);
                if (cSize > 1024.0)
                    prefStoreStat.setSummary(String.format("%.2f", cSize / 1024.0) + " GB");
                else
                    prefStoreStat.setSummary(String.format("%.2f", cSize) + " MB");
            } else {
                Log.e("PrefFragment", "" + error);
                Util.getInstance().toast(getContext(), "" + error);
            }
            super.onPostExecute(l);
        }
    }
}

