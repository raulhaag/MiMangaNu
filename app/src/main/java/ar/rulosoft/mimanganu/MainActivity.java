package ar.rulosoft.mimanganu;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

import java.lang.reflect.Field;

import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.utils.RequestWebViewUserAction;
import ar.rulosoft.mimanganu.utils.ThemeColors;
import ar.rulosoft.mimanganu.utils.Util;

public class MainActivity extends AppCompatActivity {
    public static int[] colors;
    public static boolean darkTheme;
    public static SharedPreferences pm;
    public static boolean isCancelled;
    public static boolean isConnected = true;
    public static boolean coldStart;
    private final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 0;
    public ActionBar mActBar;
    private OnBackListener backListener;
    private OnKeyUpListener keyUpListener;
    MainFragment mainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Util.initGlobals(getApplicationContext());
        RequestWebViewUserAction.init(this); //only active when a activity is available
        //devices with hardware menu button, don't show action overflow menu and menu button don't work
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception e) {
        }
        pm = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        boolean hide = pm.getBoolean("preview_on_listing", false);
        if (hide) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        darkTheme = pm.getBoolean("dark_theme", false);
        setTheme(darkTheme ? R.style.AppTheme_Dark_NoActionbar : R.style.AppTheme_Light_NoActionbar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (isStoragePermissionGiven()) {
            if (savedInstanceState == null) {
                coldStart = true;
                mainFragment = new MainFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.coordinator_layout, mainFragment).commit();
            }
            showUpdateDialog();
        } else {
            requestStoragePermission();
            setContentView(R.layout.activity_main_no_permision);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getIntent() != null) {
            onNewIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int mangaIdFromNotification = intent.getIntExtra("manga_id", -1);
        Log.i("MainActivity", "mangaID: " + mangaIdFromNotification);

        if (mangaIdFromNotification > -1) {
            Bundle bundle = new Bundle();
            bundle.putInt(MainFragment.MANGA_ID, mangaIdFromNotification);
            MangaFragment mangaFragment = new MangaFragment();
            mangaFragment.setArguments(bundle);
            replaceFragmentAllowStateLoss(mangaFragment, "MangaFragment");
            if (Util.n > 0)
                Util.n--;
            //Util.getInstance().toast(this, "n: " + Util.n, 1);
            Log.i("MA", "n: " + Util.n);
            intent.removeExtra("manga_id");
        }
    }

    private void showUpdateDialog() {
        int currentVersionCode;
        try {
            currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            Log.e("MA", "Exception", e);
            return;
        }
        int savedVersionCode = pm.getInt("version_code0", -1);

        final boolean show_dialog = pm.getBoolean("show_updates", false);
        if (show_dialog || savedVersionCode == -1 || currentVersionCode > savedVersionCode) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setMessage(getString(R.string.update_message));
            dlgAlert.setTitle(R.string.app_name);
            dlgAlert.setCancelable(true);
            dlgAlert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    pm.edit().putBoolean("show_updates", false).apply();
                    dialog.dismiss();
                    executeServerUpdates();
                }
            });
            dlgAlert.setNegativeButton(getString(R.string.see_later), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    pm.edit().putBoolean("show_updates", true).apply();
                    dialog.dismiss();
                    executeServerUpdates();
                }
            });
            dlgAlert.create().show();
            pm.edit().putInt("version_code0", currentVersionCode).apply();
        }
    }

    private void executeServerUpdates() {
        for (final ServerBase s : ServerBase.getServers(getApplicationContext())) {
            final String id = "server_version_" + s.getServerID();
            if (pm.getInt(id, 1) < s.getServerVersion()) {
                if (Database.getMangasCondition(getApplicationContext(),
                        Database.COL_SERVER_ID + " = " + s.getServerID(), Database.COL_SERVER_ID, true).size() > 0) {
                    String serverUpdateText = s.getServerName() + " " + getString(R.string.update_server_information);
                    Snackbar snackbar = Snackbar.make(mainFragment.getView(), serverUpdateText, Snackbar.LENGTH_INDEFINITE);
                    TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
                    textView.setMaxLines(5);
                    if (MainActivity.colors != null)
                        snackbar.getView().setBackgroundColor(MainActivity.colors[0]);
                    snackbar.setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    });
                    snackbar.show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (s.updateServerVersion()) {
                                pm.edit().putInt(id, s.getServerVersion()).apply();
                                Util.getInstance().showTimeSnackBar(getString(R.string.server_update_process_finished),
                                        mainFragment.getView(), getApplicationContext(), Snackbar.LENGTH_INDEFINITE);
                            }
                        }
                    }).start();
                } else {
                    pm.edit().putInt(id, s.getServerVersion()).apply();
                }
            }
        }
    }

    private boolean isStoragePermissionGiven() {
        return ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    Util.getInstance().restartApp(getApplicationContext());
                } else {
                    // Permission Denied
                    Util.getInstance().toast(this, getString(R.string.storage_permission_denied));
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        Bundle bundle = new Bundle();
        super.onSaveInstanceState(bundle, outPersistentState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (darkTheme != pm.getBoolean("dark_theme", false)) {
            Util.getInstance().restartApp(getApplicationContext());
        }
        colors = ThemeColors.getColors(pm);
        setColorToBars();
        checkFragmentOptions(getSupportFragmentManager().findFragmentById(R.id.coordinator_layout));
    }

    public void setColorToBars() {
        colors = ThemeColors.getColors(pm);
        mActBar = getSupportActionBar();
        if (mActBar != null) mActBar.setBackgroundDrawable(new ColorDrawable(colors[0]));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setNavigationBarColor(colors[0]);
            window.setStatusBarColor(colors[4]);
        }
    }

    public void setTitle(String title) {
        if (mActBar == null)
            mActBar = getSupportActionBar();
        if (mActBar != null)
            mActBar.setTitle(title);
    }

    public void enableHomeButton(boolean enable) {
        if (mActBar == null)
            mActBar = getSupportActionBar();
        if (mActBar != null)
            mActBar.setDisplayHomeAsUpEnabled(enable);
    }

    public void replaceFragmentAllowStateLoss(Fragment fragment, String tag) {
        // introduced in support lib v25.1.0
        // setAllowOptimization(false)
        // fA -> fB
        // fA.onStop -> fB.onStart
        // setAllowOptimization(true) (new default)
        // fA -> fB
        // fB.onStart -> fA.onStop
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(
                R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out
        ).replace(R.id.coordinator_layout, fragment).addToBackStack(tag);
        //System.gc();
        checkFragmentOptions(fragment);
        ft.commitAllowingStateLoss();
    }

    public void replaceFragmentNoBackStack(Fragment fragment) {
        // introduced in support lib v25.1.0
        // setAllowOptimization(false)
        // fA -> fB
        // fA.onStop -> fB.onStart
        // setAllowOptimization(true) (new default)
        // fA -> fB
        // fB.onStart -> fA.onStop
        getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out
        ).replace(R.id.coordinator_layout, fragment).commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
        //System.gc();
        checkFragmentOptions(fragment);
    }

    public void replaceFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out
        ).replace(R.id.coordinator_layout, fragment).addToBackStack(tag).commit();
        getSupportFragmentManager().executePendingTransactions();
        checkFragmentOptions(fragment);
    }

    public void replaceFragment(Fragment fragment, String tag, int f1_animation_out,
                                int f2_animation_in, int f2_animation_out, int f1_animation_in) {
        getSupportFragmentManager().beginTransaction().setCustomAnimations(f2_animation_in,
                f1_animation_out, f1_animation_in, f2_animation_out).replace(R.id.coordinator_layout,
                fragment).addToBackStack(tag).commit();
        getSupportFragmentManager().executePendingTransactions();
        checkFragmentOptions(fragment);
    }

    @Override
    public void onBackPressed() {
        if (backListener != null) {
            if (!backListener.onBackPressed()) {
                backListener = null;
                keyUpListener = null;
                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    getFragmentManager().popBackStack();
                } else {
                    super.onBackPressed();
                }
            }
        } else if (getFragmentManager().getBackStackEntryCount() > 0) {
            backListener = null;
            keyUpListener = null;
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
        try {
            Fragment cf = getSupportFragmentManager().findFragmentById(R.id.coordinator_layout);
            checkFragmentOptions(cf);
        } catch (Exception e) {
            //ignore
        }
    }

    private void checkFragmentOptions(Fragment fr) {
        if (fr != null) {
            if (fr instanceof OnBackListener) {
                backListener = (OnBackListener) fr;
            }
            if (fr instanceof OnKeyUpListener) {
                keyUpListener = (OnKeyUpListener) fr;
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyUpListener != null)
            if (!keyUpListener.onKeyUp(keyCode, event))
                return super.onKeyUp(keyCode, event);
        return super.onKeyUp(keyCode, event);
    }

    public interface OnBackListener {
        boolean onBackPressed();
    }

    public interface OnKeyUpListener {
        boolean onKeyUp(int keyCode, KeyEvent event);
    }
}
