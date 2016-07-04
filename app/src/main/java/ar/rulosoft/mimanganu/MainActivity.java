package ar.rulosoft.mimanganu;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.Toast;

import ar.rulosoft.mimanganu.utils.InitGlobals;
import ar.rulosoft.mimanganu.utils.ThemeColors;
import ar.rulosoft.mimanganu.utils.Util;

public class MainActivity extends AppCompatActivity {
    public static int[] colors;
    public static boolean darkTheme;
    public static SharedPreferences pm;
    public static MainFragment.UpdateListTask updateListTask;
    public static boolean isConnected = true;
    public ActionBar mActBar;
    OnBackListener backListener;
    OnKeyUpListener keyUpListener;
    private final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pm = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        darkTheme = pm.getBoolean("dark_theme", false);
        setTheme(darkTheme ? R.style.AppTheme_miDark : R.style.AppTheme_miLight);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (isStoragePermissionGiven()) {
            if (savedInstanceState == null) {
                MainFragment mainFragment = new MainFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mainFragment).commit();
            }
            showUpdateDialog();
        } else {
            requestStoragePermission();
            setContentView(R.layout.activity_main_no_permision);
        }
        new InitGlobals().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pm);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        int mangaIdFromNotification = intent.getIntExtra("manga_id", -1);
        Log.d("MainActivity", "mangaID: " + mangaIdFromNotification);

        if (mangaIdFromNotification > -1) {
            Bundle bundle = new Bundle();
            bundle.putInt(MainFragment.MANGA_ID, mangaIdFromNotification);
            MangaFragment mangaFragment = new MangaFragment();
            mangaFragment.setArguments(bundle);
            replaceFragmentAllowStateLoss(mangaFragment, "MangaFragment");
        } else if (mangaIdFromNotification == -1) {
            if (updateListTask != null)
                updateListTask.cancel(true);
        }
    }

    private void showUpdateDialog(){
        final boolean show_dialog = pm.getBoolean("show_updates", true);
        if (show_dialog) {//! o no segun la version 1.41 sin !
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setMessage(getString(R.string.update_message));
            dlgAlert.setTitle(R.string.app_name);
            dlgAlert.setCancelable(true);
            dlgAlert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    pm.edit().putBoolean("show_updates", false).apply(); //false 1.36
                }
            });
            dlgAlert.setNegativeButton(getString(R.string.see_later), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dlgAlert.create().show();
        }
    }

    private boolean isStoragePermissionGiven() {
        return ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    Util.getInstance().restartApp(getApplicationContext());
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, getString(R.string.storage_permission_denied), Toast.LENGTH_SHORT).show();
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
    protected void onResume() {
        super.onResume();
        if (darkTheme != pm.getBoolean("dark_theme", false)) {
            Util.getInstance().restartApp(getApplicationContext());
        }
        colors = ThemeColors.getColors(pm);
        setColorToBars();
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
        backListener = null;
        keyUpListener = null;
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.fragment_container, fragment).addToBackStack(tag).commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
    }

    public void replaceFragment(Fragment fragment, String tag) {
        backListener = null;
        keyUpListener = null;
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.fragment_container, fragment).addToBackStack(tag).commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    public void onBackPressed() {
        if (backListener != null) {
            if (!backListener.onBackPressed())
                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    getFragmentManager().popBackStack();
                } else {
                    super.onBackPressed();
                }
        } else if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
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
