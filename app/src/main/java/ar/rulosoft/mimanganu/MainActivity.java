package ar.rulosoft.mimanganu;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.Window;

import com.melnykov.fab.FloatingActionButton;

import ar.rulosoft.mimanganu.utils.ThemeColors;

public class MainActivity extends AppCompatActivity {
    public int[] colors;
    boolean darkTheme;
    private SharedPreferences pm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pm = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        darkTheme = pm.getBoolean("dark_theme", false);
        setTheme(darkTheme ? R.style.AppTheme_miDark : R.style.AppTheme_miLight);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.fragment_preferences, false);

    }

    @Override
    protected void onResume() {
        if (darkTheme != pm.getBoolean("dark_theme", false)) {
            // re start to apply new theme
            Intent i = getPackageManager()
                    .getLaunchIntentForPackage(getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            System.exit(0);
        }

        colors = ThemeColors.getColors(pm, getApplicationContext());
        android.support.v7.app.ActionBar mActBar = getSupportActionBar();
        if (mActBar != null) mActBar.setBackgroundDrawable(new ColorDrawable(colors[0]));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setNavigationBarColor(colors[0]);
            window.setStatusBarColor(colors[4]);
        }
        super.onResume();
    }


}
