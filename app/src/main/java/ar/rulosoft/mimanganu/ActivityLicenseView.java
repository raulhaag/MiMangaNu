package ar.rulosoft.mimanganu;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import ar.rulosoft.mimanganu.utils.ThemeColors;

public class ActivityLicenseView extends AppCompatActivity {
    private TextView lic;
    private boolean darkTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(this);
        darkTheme = pm.getBoolean("dark_theme", false);
        setTheme(darkTheme ? R.style.AppTheme_miDark : R.style.AppTheme_miLight);

        lic = new TextView(getBaseContext());
        lic.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Medium);
        lic.setPadding(10, 10, 10, 10);
        ScrollView newScroll = new ScrollView(getApplicationContext());
        newScroll.addView(lic);
        setContentView(newScroll);

        showLicense("MIT.txt");

        int[] colors = ThemeColors.getColors(pm, getApplicationContext());
        android.support.v7.app.ActionBar mActBar = getSupportActionBar();
        if (mActBar != null) {
            mActBar.setBackgroundDrawable(new ColorDrawable(colors[0]));
            mActBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_license, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.licenseViewAPACHE: {
                showLicense("APACHE-LICENSE-2.0.txt");
                break;
            }
            case R.id.licenseViewMIT: {
                showLicense("MIT.txt");
                break;
            }
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLicense(String asset) {
        try {
            InputStream is = getAssets().open(asset);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            String licenseStr = new String(buffer);
            is.close();
            lic.setText(licenseStr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
