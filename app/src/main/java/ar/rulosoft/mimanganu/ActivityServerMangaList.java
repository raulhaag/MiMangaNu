package ar.rulosoft.mimanganu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
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

public class ActivityServerMangaList extends AppCompatActivity {

    private ServerBase s;
    private ListView list;
    private ProgressBar loading;
    private MangaAdapter adapter;
    private MenuItem search;
    private boolean darkTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(this);
        darkTheme = pm.getBoolean("dark_theme", false);
        setTheme(darkTheme ? R.style.AppTheme_miDark : R.style.AppTheme_miLight);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_lista_de_mangas);
        int id = getIntent().getExtras().getInt(ActivityMisMangas.SERVER_ID);
        s = ServerBase.getServer(id);
        android.support.v7.app.ActionBar mActBar = getSupportActionBar();
        if (mActBar != null)
            mActBar.setTitle(getResources().getString(R.string.listaen) + " " + s.getServerName());

        list = (ListView) findViewById(R.id.lista_de_mangas);
        loading = (ProgressBar) findViewById(R.id.cargando);
        int[] colors = ThemeColors.getColors(pm, getApplicationContext());
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colors[0]));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setNavigationBarColor(colors[0]);
            window.setStatusBarColor(colors[4]);
        }
        new LoadMangasTask().execute();

        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Manga m = (Manga) list.getAdapter().getItem(position);
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
        search = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (adapter != null)
                    adapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }

    private class LoadMangasTask extends AsyncTask<Void, Void, List<Manga>> {

        String error = "";

        @Override
        protected void onPreExecute() {
            loading.setVisibility(ProgressBar.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected List<Manga> doInBackground(Void... params) {
            List<Manga> mangas = null;
            try {
                mangas = s.getMangas();
            } catch (Exception e) {
                if (e.getMessage() != null)
                    error = e.getMessage();
                else
                    error = e.toString();
            }
            return mangas;
        }

        @Override
        protected void onPostExecute(List<Manga> result) {
            if (list != null && result != null && !result.isEmpty()) {
                adapter = new MangaAdapter(getApplicationContext(), result, darkTheme);
                list.setAdapter(adapter);
            }
            if (error != null && error.length() > 2) {
                Toast.makeText(getApplicationContext(), "Error: " + error, Toast.LENGTH_LONG).show();
            }
            loading.setVisibility(ProgressBar.INVISIBLE);
        }
    }

}
