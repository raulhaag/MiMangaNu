package ar.rulosoft.mimanganu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.fedorvlasov.lazylist.FileCache;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ar.rulosoft.custompref.ColorListDialogFragment;
import ar.rulosoft.custompref.ColorListDialogPref;
import ar.rulosoft.custompref.PreferenceListDirFragment;
import ar.rulosoft.custompref.PreferencesListDir;
import ar.rulosoft.custompref.SeekBarCustomPreference;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.LoginDialog;
import ar.rulosoft.mimanganu.servers.DeadServer;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.ChapterDownload;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.services.SingleDownload;
import ar.rulosoft.mimanganu.services.UpdateJobCreator;
import ar.rulosoft.mimanganu.utils.NetworkUtilsAndReceiver;
import ar.rulosoft.mimanganu.utils.Paths;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.mimanganu.utils.autotest.RunAutoTest;
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
        ColorListDialogPref primaryColor = getPreferenceManager().findPreference("primario");
        primaryColor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                ((MainActivity) getActivity()).setColorToBars();
                return false;
            }
        });
        PreferenceGroup preferenceGroup = findPreference("account");
        ServerBase[] servers = ServerBase.getServers(getContext());
        boolean isThereAnyServerUsingAccount = false;
        for (final ServerBase sb : servers) {
            if (sb.needLogin()) {
                isThereAnyServerUsingAccount = true;
                Preference preference = new Preference(getContext());
                preference.setTitle(sb.getServerName());
                preference.setSelectable(true);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        new LoginDialog(getContext(), sb).show();
                        return false;
                    }
                });
                preferenceGroup.addPreference(preference);
            }
        }
        if (!isThereAnyServerUsingAccount)
            preferenceGroup.setVisible(false);

        /* Once, create necessary Data */
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        final String current_filepath = prefs.getString("directorio",
                Environment.getExternalStorageDirectory().getAbsolutePath()) + "/MiMangaNu/";

        /* This enables to hide downloaded images from gallery, just a toggle */
        final SwitchPreferenceCompat cBoxPref = getPreferenceManager().findPreference("mostrar_en_galeria");

        // actuate the switch based on the existence of the .nomedia file
        final File noMedia = new File(current_filepath, ".nomedia");
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            cBoxPref.setChecked(noMedia.exists());
        } else {
            cBoxPref.setEnabled(false);
        }
        cBoxPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // reject change event in case the external storage is not available
                if (!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                    return false;
                }

                try {
                    if ((Boolean) newValue) {
                        if (!noMedia.createNewFile()) {
                            Log.w("PF", "failed to create .nomedia file");
                        }
                    } else {
                        if (!noMedia.delete()) {
                            Log.w("PF", "failed to delete .nomedia file");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
            }
        });

        /* Set summary for Reader preference + seamless chapter transitions summary */
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
        final SwitchPreferenceCompat readerTypePref = getPreferenceManager().findPreference("reader_type");
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

        /* enable / disable seamless_chapter_transitions_delete_read depending on the state of seamless_chapter_transitions */
        Preference seamlessChapterTransitionsDeleteReadPreference = getPreferenceManager().findPreference("seamless_chapter_transitions_delete_read");
        if (prefs.getBoolean("seamless_chapter_transitions", false)) {
            seamlessChapterTransitionsDeleteReadPreference.setEnabled(true);
        } else {
            seamlessChapterTransitionsDeleteReadPreference.setEnabled(false);
        }
        final SwitchPreferenceCompat seamlessChapterTransitionsSPC = getPreferenceManager().findPreference("seamless_chapter_transitions");
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

        /* enable / disable auto_import_path depending on the state of auto_import */
        final Preference autoImportPath = getPreferenceManager().findPreference("auto_import_path");
        if (prefs.getBoolean("auto_import", false)) {
            autoImportPath.setEnabled(true);
        } else {
            autoImportPath.setEnabled(false);
        }
        final SwitchPreferenceCompat autoImportSPC = getPreferenceManager().findPreference("auto_import");
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

        /* This sets the download threads (parallel downloads) */
        final SeekBarCustomPreference listPreferenceDT = getPreferenceManager().findPreference("download_threads");
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

        /* This sets the maximum number of errors to tolerate */
        final SeekBarCustomPreference listPrefET = getPreferenceManager().findPreference("error_tolerancia");
        listPrefET.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ChapterDownload.MAX_ERRORS = (int) newValue;
                return true;
            }
        });

        /* This sets the number of retries to fetch images */
        SeekBarCustomPreference listPrefRT = getPreferenceManager().findPreference("reintentos");
        listPrefRT.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SingleDownload.RETRY = (int) newValue;
                return true;
            }
        });

        /* This sets the Update Interval of the mangas (i.e. once per week) */
        final ListPreference listPrefCU = getPreferenceManager().findPreference("update_interval");
        listPrefCU.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                long time = Long.parseLong((String) newValue);
                JobManager.instance().cancelAllForTag(UpdateJobCreator.UPDATE_TAG);
                if (time > 0) {
                    new JobRequest.Builder(UpdateJobCreator.UPDATE_TAG)
                            .setPeriodic(time, TimeUnit.MINUTES.toMillis(5))
                            .setRequiresCharging(false)
                            .setRequiresDeviceIdle(false)
                            .setUpdateCurrent(true)
                            .build()
                            .schedule();
                }
                if (time < 0)
                    MainActivity.coldStart = false;
                return true;
            }
        });

        /* This sets the Version Number, that's all */
        final Preference prefAbout = getPreferenceManager().findPreference("about_text");
        prefAbout.setSummary("v" + BuildConfig.VERSION_NAME);

        /* Hide app update pref o f-droid users */
        if (!BuildConfig.VERSION_NAME.contains("github")) {
            getPreferenceManager().findPreference("app_update").setVisible(false);
        }

        /* This will check how much storage is taken by the mangas */
        new calcStorage().execute(current_filepath);

        final Preference prefLicense = getPreferenceManager().findPreference("license_view");
        prefLicense.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((MainActivity) getActivity()).replaceFragment(new LicenseFragment(), "licence_fragment");
                return false;
            }
        });

        final SwitchPreferenceCompat onlyWifiUpdateSwitch = getPreferenceManager().findPreference("update_only_wifi");

        SwitchPreferenceCompat onlyWifiSwitch = getPreferenceManager().findPreference("only_wifi");
        onlyWifiSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                NetworkUtilsAndReceiver.ONLY_WIFI = (Boolean) o;
                NetworkUtilsAndReceiver.connectionStatus = NetworkUtilsAndReceiver.ConnectionStatus.UNCHECKED;
                onlyWifiUpdateSwitch.setEnabled(!(Boolean) o);
                return true;
            }
        });

        if (onlyWifiSwitch.isChecked()) {
            onlyWifiUpdateSwitch.setEnabled(false);
        } else {
            onlyWifiUpdateSwitch.setEnabled(true);
        }

        final SeekBarCustomPreference seekBarConnectionRetry = getPreferenceManager().findPreference("connection_retry");
        seekBarConnectionRetry.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Navigator.connectionRetry = Integer.parseInt(prefs.getString("connection_retry", "10"));
                return true;
            }
        });

        final SeekBarCustomPreference seekBarConnectionTimeout = getPreferenceManager().findPreference("connection_timeout");
        seekBarConnectionTimeout.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Navigator.connectionTimeout = Integer.parseInt(prefs.getString("connection_timeout", "10"));
                return true;
            }
        });

        final SeekBarCustomPreference seekBarWriteTimeout = getPreferenceManager().findPreference("write_timeout");
        seekBarWriteTimeout.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Navigator.writeTimeout = Integer.parseInt(prefs.getString("write_timeout", "10"));
                return true;
            }
        });

        final SeekBarCustomPreference seekBarReadTimeout = getPreferenceManager().findPreference("read_timeout");
        seekBarReadTimeout.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Navigator.readTimeout = Integer.parseInt(prefs.getString("read_timeout", "30"));
                return true;
            }
        });

        Preference gridColumnsPref = getPreferenceManager().findPreference("grid_columns");
        if (prefs.getBoolean("grid_columns_automatic_detection", true)) {
            gridColumnsPref.setEnabled(false);
        } else {
            gridColumnsPref.setEnabled(true);
        }
        final SwitchPreferenceCompat gridColumnsAutomaticDetectionSPC = getPreferenceManager().findPreference("grid_columns_automatic_detection");
        gridColumnsAutomaticDetectionSPC.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                Preference gridColumns = getPreferenceManager().findPreference("grid_columns");
                if (prefs.getBoolean("grid_columns_automatic_detection", true)) {
                    gridColumns.setEnabled(true);
                } else {
                    gridColumns.setEnabled(false);
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

        final Preference prefClearSpecificCookies = getPreferenceManager().findPreference("clear_specific_cookies");
        prefClearSpecificCookies.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Preference clearSpecificCookies = getPreferenceManager().findPreference("clear_specific_cookies");
                clearSpecificCookies.setEnabled(false);

                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rootView = inflater.inflate(R.layout.simple_input_dialog, null);
                final EditText SpecificCookiesToClear = rootView.findViewById(R.id.txtCustomLang);
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                dialogBuilder.setTitle("Cookies to clear contain");
                dialogBuilder.setView(rootView);
                dialogBuilder.setPositiveButton("Ok", null);
                dialogBuilder.setNegativeButton(getString(R.string.cancel), null);
                AlertDialog dialog = dialogBuilder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button accept = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        accept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Thread t0 = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Util.getInstance().removeSpecificCookies(getContext(), SpecificCookiesToClear.getText().toString());
                                    }
                                });
                                t0.start();

                                dialog.dismiss();
                            }
                        });
                        Button cancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                    }
                });
                dialog.show();

                return true;
            }
        });

        final Preference prefClearAllCookies = getPreferenceManager().findPreference("clear_all_cookies");
        prefClearAllCookies.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Preference clearAllCookies = getPreferenceManager().findPreference("clear_all_cookies");
                clearAllCookies.setEnabled(false);
                Thread t0 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Util.getInstance().removeAllCookies(getContext());
                    }
                });
                t0.start();

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

        /* backup preferences */
        final Preference backupPreferences = getPreferenceManager().findPreference("backup_settings");
        backupPreferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                File cf = new File(prefs.getString("directorio", Environment.getExternalStorageDirectory().getAbsolutePath()) + "/MiMangaNu/shared_prefs_backup");
                if (cf.list() != null && cf.list().length > 0) {
                    Snackbar snackbar = Snackbar.make(getView(), R.string.replace_backup, Snackbar.LENGTH_LONG)
                            .setActionTextColor(Color.WHITE)
                            .setAction(android.R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    backupPreferences();
                                }
                            });
                    if (MainActivity.colors != null)
                        snackbar.getView().setBackgroundColor(MainActivity.colors[0]);
                    snackbar.show();
                } else {
                    backupPreferences();
                }
                return true;
            }
        });

        /* restore preferences */
        final Preference restorePreferences = getPreferenceManager().findPreference("restore_settings");
        restorePreferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Snackbar snackbar = Snackbar.make(getView(), R.string.backup_restore, Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.WHITE)
                        .setAction(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                restorePreferences();
                            }
                        });
                if (MainActivity.colors != null)
                    snackbar.getView().setBackgroundColor(MainActivity.colors[0]);
                snackbar.show();
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

        final Preference deleteAllImages = getPreferenceManager().findPreference("clear_all_manga_images");
        deleteAllImages.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getContext()).setTitle(R.string.clear_images_title).setMessage(R.string.are_you_sure).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    private void delete(String base, String dir) {
                        File f = new File(base, dir);
                        if (f.exists()) {
                            Util.getInstance().deleteRecursive(f);
                        }
                    }

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Util.getInstance().toast(getContext(), getString(R.string.deleting_all_images_message));
                        String base = Paths.generateBasePath(getContext());
                        for (int i = 0, nsize = DeadServer.getServersName().size(); i < nsize; i++) {
                            String dir = DeadServer.getServersName().valueAt(i);
                            delete(base, dir);
                        }
                        for (ServerBase sb : ServerBase.getServers(getContext())) {
                            delete(base, sb.getServerName());
                        }
                        Database.setNotDownloadedAllChapter(getContext());
                        Util.getInstance().toast(getContext(), getString(R.string.process_finished));
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
                return false;
            }
        });

        /* server test */
        final Preference runServerTest = getPreferenceManager().findPreference("run_test");
        runServerTest.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), RunAutoTest.class);
                getActivity().startActivity(intent);
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getPreferenceManager().findPreference("update_sound").setVisible(false);
            getPreferenceManager().findPreference("update_vibrate").setVisible(false);
            getPreferenceManager().findPreference("update_notif_options").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, Util.channelIdNews);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName());
                    startActivity(intent);
                    return true;
                }
            });
        } else {
            getPreferenceManager().findPreference("update_notif_options").setVisible(false);
        }

        setFirstRunDefaults();
    }

    private void backupPreferences() {
        File from = new File(getContext().getApplicationInfo().dataDir + "/shared_prefs/");
        String dir = prefs.getString("directorio", Environment.getExternalStorageDirectory().getAbsolutePath()) + "/MiMangaNu/shared_prefs_backup";
        File to = new File(dir);
        Util.getInstance().deleteRecursive(to);
        to.mkdirs();
        for (String f : from.list()) {
            try {
                File toFile = new File(to.getPath(), f);
                Util.getInstance().copyFile(new File(from.getPath(), f),
                        toFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void restorePreferences() {
        File to = new File(getContext().getApplicationInfo().dataDir + "/shared_prefs/");
        String dir = prefs.getString("directorio", Environment.getExternalStorageDirectory().getAbsolutePath()) + "/MiMangaNu/shared_prefs_backup";
        File from = new File(dir);
        if (!from.exists()) {
            Snackbar snackbar = Snackbar.make(getView(), getText(R.string.backup_dont_found), Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.WHITE);
            if (MainActivity.colors != null)
                snackbar.getView().setBackgroundColor(MainActivity.colors[0]);
            snackbar.show();
        } else {
            Util.getInstance().deleteRecursive(to);
            to.mkdirs();
            if (from.list() != null && from.list().length == 0) {
                Util.getInstance().showFastSnackBar(getString(R.string.preferences_backup_not_found), getView(), getContext());
                return;
            }
            for (String f : from.list()) {
                try {
                    File toFile = new File(to.getPath() + "/" + f);
                    Util.getInstance().copyFile(new File(from.getPath() + "/" + f), toFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Util.getInstance().restartApp(getContext());
        }
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
            final SeekBarCustomPreference tmpSeekBar = getPreferenceManager().findPreference("grid_columns");
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

        final SeekBarCustomPreference tmpSeekBar = getPreferenceManager().findPreference(preference);
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
                                prefStoreStat.setSummary("calculating... ~" + String.format(Locale.getDefault(), "%.2f", cSize / 1024.0) + " GB");
                            }
                        });
                    } else {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                prefStoreStat.setSummary("calculating... ~" + String.format(Locale.getDefault(), "%.2f", cSize) + " MB");
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
                    prefStoreStat.setSummary(String.format(Locale.getDefault(), "%.2f", cSize / 1024.0) + " GB");
                else
                    prefStoreStat.setSummary(String.format(Locale.getDefault(), "%.2f", cSize) + " MB");
            } else {
                Log.e("PrefFragment", "" + error);
                Util.getInstance().toast(getContext(), "" + error);
            }
            super.onPostExecute(l);
        }
    }
}

