package ar.rulosoft.mimanganu;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class ActivityLicencia extends ActionBarActivity {

    TextView lic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licence);
        lic = (TextView) findViewById(R.id.licencia);
        mostrarLicencia("MIT.txt");
        Button licApa, licMIT;
        licApa = (Button) findViewById(R.id.LicenciaAPACHE);
        licMIT = (Button) findViewById(R.id.licenciaMIT);
        licApa.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarLicencia("APACHE-LICENSE-2.0.txt");
            }
        });
        licMIT.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarLicencia("MIT.txt");
            }
        });
    }

    public void mostrarLicencia(String asset) {
        try {
            InputStream is = getAssets().open(asset);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            String licencia = new String(buffer);
            is.close();
            lic.setText(licencia);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
