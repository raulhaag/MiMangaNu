package ar.rulosoft.mimanganu;

import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

import java.io.File;
import java.io.IOException;

import ar.rulosoft.mimanganu.services.DescargaCapitulo;
import ar.rulosoft.mimanganu.services.DescargaIndividual;
import ar.rulosoft.mimanganu.services.ServicioColaDeDescarga;

public class OpcionesActivity extends PreferenceActivity {
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_preferences);
        CheckBoxPreference cBoxPreference = (CheckBoxPreference) getPreferenceManager().findPreference("mostrar_en_galeria");

		/*
         * Esconder de galeria
		 */

        cBoxPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean valor = (Boolean) newValue;
                File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MiMangaNu/", ".nomedia");
                if (!valor) {
                    if (f.exists())
                        f.delete();
                } else {
                    if (!f.exists())
                        try {
                            f.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
                return true;
            }
        });

        ListPreference listPreferenceDT = (ListPreference) getPreferenceManager().findPreference("download_threads");
        listPreferenceDT.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int threads = Integer.parseInt((String) newValue);
                int antes = ServicioColaDeDescarga.SLOTS;
                ServicioColaDeDescarga.SLOTS = threads;
                if (ServicioColaDeDescarga.actual != null) {
                    ServicioColaDeDescarga.actual.slots += (threads - antes);
                }
                return true;
            }
        });

        ListPreference listPreferenceET = (ListPreference) getPreferenceManager().findPreference("error_tolerancia");
        listPreferenceET.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int tol = Integer.parseInt((String) newValue);
                DescargaCapitulo.MAX_ERRORS = tol;
                return true;
            }
        });

        ListPreference listPreferenceRT = (ListPreference) getPreferenceManager().findPreference("reintentos");
        listPreferenceRT.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int rt = Integer.parseInt((String) newValue);
                DescargaIndividual.REINTENTOS = rt;
                return true;
            }
        });
    }
}