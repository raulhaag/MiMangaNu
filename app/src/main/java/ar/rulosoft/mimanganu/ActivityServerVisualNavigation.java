package ar.rulosoft.mimanganu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.adapters.MangasRecAdapter;
import ar.rulosoft.mimanganu.adapters.MangasRecAdapter.OnLastItem;
import ar.rulosoft.mimanganu.adapters.MangasRecAdapter.OnMangaClick;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.utils.ThemeColors;

public class ActivityServerVisualNavigation extends AppCompatActivity implements OnLastItem, OnMangaClick {

    private boolean mStart = true;
    private ServerBase sBase;
    private Spinner generos;
    private Spinner orden;
    private RecyclerView grilla;
    private ProgressBar cargando;
    private MangasRecAdapter adap;
    private boolean neuvaTarea = false;
    private int pagina = 1;
    private MenuItem buscar;
    private boolean darkTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(this);
        darkTheme = pm.getBoolean("dark_theme", false);
        setTheme(darkTheme ? R.style.AppTheme_miDark : R.style.AppTheme_miLight);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_visual_navegacion);
        int id = getIntent().getExtras().getInt(ActivityMisMangas.SERVER_ID);
        sBase = ServerBase.getServer(id);
        int[] colors = ThemeColors.getColors(pm, getApplicationContext());
        android.support.v7.app.ActionBar mActBar = getSupportActionBar();
        if (mActBar != null) {
            mActBar.setTitle(getResources()
                    .getString(R.string.listaen) + " " + sBase.getServerName());
            mActBar.setBackgroundDrawable(new ColorDrawable(colors[0]));
        }

        grilla = (RecyclerView) findViewById(R.id.grilla);
        generos = (Spinner) findViewById(R.id.generos);
        orden = (Spinner) findViewById(R.id.ordenar_por);
        cargando = (ProgressBar) findViewById(R.id.cargando);
        if (sBase.getCategories() != null)
            generos.setAdapter(new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, sBase.getCategories()));
        else
            generos.setVisibility(Spinner.INVISIBLE);

        if (sBase.getOrders() != null)
            orden.setAdapter(new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, sBase.getOrders()));
        else
            orden.setVisibility(Spinner.INVISIBLE);

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = getResources().getDisplayMetrics().density;
        float dpWidth = outMetrics.widthPixels / density;
        int columnas = (int) (dpWidth / 150);
        if (columnas == 0)
            columnas = 2;
        else if (columnas > 6)
            columnas = 6;
        grilla.setLayoutManager(new GridLayoutManager(ActivityServerVisualNavigation.this, columnas));
        orden.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!cargando.isShown()) {
                    adap = null;
                    pagina = 1;
                    mStart = true;
                    sBase.hayMas = true;
                    new LoadLastTask().execute(pagina);
                } else {
                    neuvaTarea = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        generos.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!cargando.isShown()) {
                    adap = null;
                    pagina = 1;
                    mStart = true;
                    sBase.hayMas = true;
                    new LoadLastTask().execute(pagina);
                } else {
                    neuvaTarea = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        new LoadLastTask().execute(pagina);
    }

    @Override
    public void onRequestedLastItem() {
        if (sBase.hayMas && !cargando.isShown() && !mStart)
            new LoadLastTask().execute(pagina);
    }

    @Override
    public void onMangaClick(Manga manga) {
        Intent intent = new Intent(getApplication(), ActivityDetails.class);
        intent.putExtra(ActivityMisMangas.SERVER_ID, sBase.getServerID());
        intent.putExtra(ActivityDetails.TITLE, manga.getTitle());
        intent.putExtra(ActivityDetails.PATH, manga.getPath());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manga_server_visual, menu);
        buscar = menu.findItem(R.id.action_search);
        MenuItem vcl = menu.findItem(R.id.ver_como_lista);
        if (!sBase.hasList())
            vcl.setVisible(false);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(buscar);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String st) {
                Intent intent = new Intent(ActivityServerVisualNavigation.this,
                        ActivityResultadoDeBusqueda.class);
                intent.putExtra(ActivityResultadoDeBusqueda.TERMINO, st);
                intent.putExtra(ActivityMisMangas.SERVER_ID, sBase.getServerID());
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ver_como_lista) {
            Intent intent = new Intent(this, ActivityServerListadeMangas.class);
            intent.putExtra(ActivityMisMangas.SERVER_ID, sBase.getServerID());
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class LoadLastTask extends AsyncTask<Integer, Void, ArrayList<Manga>> {

        String error = "";

        @Override
        protected void onPreExecute() {
            cargando.setVisibility(ProgressBar.VISIBLE);
        }

        @SuppressWarnings("ResourceType")//TODO ver problema
        @Override
        protected ArrayList<Manga> doInBackground(Integer... params) {
            ArrayList<Manga> mangas = null;
            try {
                mangas = sBase.getMangasFiltered(
                        generos.getSelectedItemPosition(), orden.getSelectedItemPosition(), params[0]);
            } catch (Exception e) {
                error = e.getMessage();
            }
            return mangas;
        }

        @Override
        protected void onPostExecute(ArrayList<Manga> result) {
            if (error != null && error.length() > 1) {
                Toast.makeText(ActivityServerVisualNavigation.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show();
            } else {
                pagina++;
                if (result != null && result.size() != 0 && grilla != null) {
                    if (adap == null) {
                        adap = new MangasRecAdapter(result,
                                ActivityServerVisualNavigation.this, darkTheme);
                        adap.setLastItemListener(ActivityServerVisualNavigation.this);
                        adap.setMangaClickListener(ActivityServerVisualNavigation.this);
                        grilla.setAdapter(adap);
                    } else {
                        adap.addAll(result);
                    }
                }
                mStart = false;
                if (neuvaTarea) {
                    adap = null;
                    pagina = 1;
                    mStart = true;
                    sBase.hayMas = true;
                    new LoadLastTask().execute(pagina);
                    neuvaTarea = false;
                }
            }
            cargando.setVisibility(ProgressBar.INVISIBLE);
        }
    }

}
