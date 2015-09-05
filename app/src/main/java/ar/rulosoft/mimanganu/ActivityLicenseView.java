package ar.rulosoft.mimanganu;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import ar.rulosoft.mimanganu.utils.ThemeColors;

public class ActivityLicenseView extends ActionBarActivity {

    TextView lic;
    SharedPreferences pm;
    boolean darkTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pm = PreferenceManager.getDefaultSharedPreferences(this);
        darkTheme = pm.getBoolean("dark_theme", false);
        setTheme(darkTheme ? R.style.AppTheme_miDark : R.style.AppTheme_miLight);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licence);
        lic = (TextView) findViewById(R.id.licencia);
        showLicense("MIT.txt");
        Button licApa, licMIT;
        licApa = (Button) findViewById(R.id.LicenciaAPACHE);
        licMIT = (Button) findViewById(R.id.licenciaMIT);
        licApa.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showLicense("APACHE-LICENSE-2.0.txt");
            }
        });
        licMIT.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showLicense("MIT.txt");
            }
        });
        int[] colors =
                ThemeColors.getColors(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()), getApplicationContext());
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colors[0]));
    }

    public void showLicense(String asset) {
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
