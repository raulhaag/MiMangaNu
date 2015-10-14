package ar.rulosoft.mimanganu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.utils.ThemeColors;

public class ActivityResultadoDeBusqueda extends AppCompatActivity {
    public static final String TERMINO = "termino_busqueda";
    private String termino = "";
    private int serverId;
    private ProgressBar cargando;
    private ListView lista;
    private boolean darkTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(this);
        darkTheme = pm.getBoolean("dark_theme", false);
        setTheme(darkTheme ? R.style.AppTheme_miDark : R.style.AppTheme_miLight);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_resultado_de_busqueda);
        serverId = getIntent().getExtras().getInt(ActivityMisMangas.SERVER_ID);
        termino = getIntent().getExtras().getString(TERMINO);
        lista = (ListView) findViewById(R.id.resultados);
        cargando = (ProgressBar) findViewById(R.id.cargando);
        lista.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Manga m = (Manga) lista.getAdapter().getItem(position);
                Intent intent = new Intent(getApplication(), ActivityDetails.class);
                intent.putExtra(ActivityMisMangas.SERVER_ID, serverId);
                intent.putExtra(ActivityDetails.TITLE, m.getTitle());
                intent.putExtra(ActivityDetails.PATH, m.getPath());
                startActivity(intent);
            }
        });
        new PerformSearchTask().execute();
        int[] colors = ThemeColors.getColors(pm, getApplicationContext());
        android.support.v7.app.ActionBar mActBar = getSupportActionBar();
        if (mActBar != null) mActBar.setBackgroundDrawable(new ColorDrawable(colors[0]));
    }

    public class PerformSearchTask extends AsyncTask<Void, Void, ArrayList<Manga>> {
        public String error = "";

        @Override
        protected void onPreExecute() {
            cargando.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected ArrayList<Manga> doInBackground(Void... params) {
            ArrayList<Manga> mangas = new ArrayList<>();
            ServerBase s = ServerBase.getServer(serverId);
            try {
                mangas = s.search(termino);
            } catch (Exception e) {
                error = e.getMessage();
            }
            return mangas;
        }

        @Override
        protected void onPostExecute(ArrayList<Manga> result) {
            cargando.setVisibility(ProgressBar.INVISIBLE);
            if (error.length() < 2) {
                if (result != null && !result.isEmpty() && lista != null) {
                    lista.setAdapter(new ArrayAdapter<>(
                            ActivityResultadoDeBusqueda.this, android.R.layout.simple_list_item_1, result
                    ));
                } else if (result == null || result.isEmpty()) {
                    Toast.makeText(ActivityResultadoDeBusqueda.this,
                            getResources().getString(R.string.busquedanores), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(ActivityResultadoDeBusqueda.this, error, Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(result);
        }
    }
}
