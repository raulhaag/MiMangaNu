package ar.rulosoft.mimanganu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import ar.rulosoft.mimanganu.adapters.MangaAdapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.utils.ThemeColors;

public class ActivityServerListadeMangas extends ActionBarActivity {

    ServerBase s;
    ListView lista;
    ProgressBar cargando;
    MangaAdapter adapter;
    SharedPreferences pm;
    boolean darkTheme;
    private MenuItem buscar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pm = PreferenceManager.getDefaultSharedPreferences(this);
        darkTheme = pm.getBoolean("dark_theme", false);
        if (darkTheme) {
            setTheme(R.style.AppBaseThemeDark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_lista_de_mangas);
        int id = getIntent().getExtras().getInt(ActivityMisMangas.SERVER_ID);
        s = ServerBase.getServer(id);
        getSupportActionBar().setTitle(getResources().getString(R.string.listaen) + " " + s.getServerName());
        lista = (ListView) findViewById(R.id.lista_de_mangas);
        cargando = (ProgressBar) findViewById(R.id.cargando);
        int[] colors = ThemeColors.getColors(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()), getApplicationContext());
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colors[0]));

        new LoadMangasTask().execute();

        lista.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Manga m = (Manga) lista.getAdapter().getItem(position);
                Intent intent = new Intent(getApplication(), ActivityDetails.class);
                intent.putExtra(ActivityMisMangas.SERVER_ID, s.getServerID());
                intent.putExtra(ActivityDetails.TITLE, m.getTitle());
                intent.putExtra(ActivityDetails.PATH, m.getPath());
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manga_server, menu);
        buscar = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(buscar);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }

    private class LoadMangasTask extends AsyncTask<Void, Void, List<Manga>> {

        String error = ".";

        @Override
        protected void onPreExecute() {
            cargando.setVisibility(ProgressBar.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected List<Manga> doInBackground(Void... params) {
            List<Manga> mangas = null;
            try {
                mangas = s.getMangas();
            } catch (Exception e) {
                error = e.getMessage();
            }
            return mangas;
        }

        @Override
        protected void onPostExecute(List<Manga> result) {
            if (lista != null && result != null && !result.isEmpty()) {
                adapter = new MangaAdapter(getApplicationContext(), result);
                lista.setAdapter(adapter);
            }
            if (error.length() > 2) {
                Toast.makeText(getApplicationContext(), "Error: " + error, Toast.LENGTH_LONG).show();
            }
            cargando.setVisibility(ProgressBar.INVISIBLE);
        }
    }

}
