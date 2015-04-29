package ar.rulosoft.mimanganu;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
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

public class ActivityServerVisualNavegacion extends ActionBarActivity implements OnLastItem, OnMangaClick {

    public boolean mStart = true;
    ServerBase s;
    Spinner generos, orden;
    RecyclerView grilla;
    ProgressBar cargando;
    MangasRecAdapter adap;
    boolean neuvaTarea = false;
    private int pagina = 1;
    private MenuItem buscar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_visual_navegacion);
        int id = getIntent().getExtras().getInt(ActivityMisMangas.SERVER_ID);
        s = ServerBase.getServer(id);
        getSupportActionBar().setTitle(getResources().getString(R.string.listaen) + " " + s.getServerName());
        grilla = (RecyclerView) findViewById(R.id.grilla);
        generos = (Spinner) findViewById(R.id.generos);
        orden = (Spinner) findViewById(R.id.ordenar_por);
        cargando = (ProgressBar) findViewById(R.id.cargando);

        if (s.getCategorias() != null)
            generos.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, s.getCategorias()));
        else
            generos.setVisibility(Spinner.INVISIBLE);

        if (s.getOrdenes() != null)
            orden.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, s.getOrdenes()));
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
        grilla.setLayoutManager(new GridLayoutManager(ActivityServerVisualNavegacion.this, columnas));
        orden.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!cargando.isShown()) {
                    adap = null;
                    pagina = 1;
                    mStart = true;
                    s.hayMas = true;
                    new CargarUltima().execute(pagina);
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
                    s.hayMas = true;
                    new CargarUltima().execute(pagina);
                } else {
                    neuvaTarea = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });

		/*
         * grilla.setOnItemClickListener(new OnItemClickListener() {
		 * 
		 * @Override public void onItemClick(AdapterView<?> parent, View view,
		 * int position, long id) { Manga m = (Manga)
		 * grilla.getAdapter().getItem(position); Intent intent = new
		 * Intent(getApplication(), ActivityDetalles.class);
		 * intent.putExtra(ActivityMisMangas.SERVER_ID, s.getServerID());
		 * intent.putExtra(ActivityDetalles.TITULO, m.getTitulo());
		 * intent.putExtra(ActivityDetalles.PATH, m.getPath());
		 * startActivity(intent); } });/
		 */

        new CargarUltima().execute(pagina);
    }

    @Override
    public void onRequestedLastItem() {
        if (s.hayMas && !cargando.isShown() && !mStart)
            new CargarUltima().execute(pagina);
    }

    @Override
    public void onMangaClick(Manga manga) {
        Intent intent = new Intent(getApplication(), ActivityDetalles.class);
        intent.putExtra(ActivityMisMangas.SERVER_ID, s.getServerID());
        intent.putExtra(ActivityDetalles.TITULO, manga.getTitulo());
        intent.putExtra(ActivityDetalles.PATH, manga.getPath());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manga_server_visual, menu);
        buscar = menu.findItem(R.id.action_search);
        MenuItem vcl = menu.findItem(R.id.ver_como_lista);
        if (!s.tieneListado())
            vcl.setVisible(false);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(buscar);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String st) {
                Intent intent = new Intent(ActivityServerVisualNavegacion.this, ActivityResultadoDeBusqueda.class);
                intent.putExtra(ActivityResultadoDeBusqueda.TERMINO, st);
                intent.putExtra(ActivityMisMangas.SERVER_ID, s.getServerID());
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
            intent.putExtra(ActivityMisMangas.SERVER_ID, s.getServerID());
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class CargarUltima extends AsyncTask<Integer, Void, ArrayList<Manga>> {

        String error = "";

        @Override
        protected void onPreExecute() {
            cargando.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected ArrayList<Manga> doInBackground(Integer... params) {
            ArrayList<Manga> mangas = null;
            try {
                mangas = s.getMangasFiltered(generos.getSelectedItemPosition(), orden.getSelectedItemPosition(), params[0]);
            } catch (Exception e) {
                error = e.getMessage();
            }
            return mangas;
        }

        @Override
        protected void onPostExecute(ArrayList<Manga> result) {
            if (error != null && error.length() > 1) {
                Toast.makeText(ActivityServerVisualNavegacion.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            } else {
                pagina++;
                if (result != null && result.size() != 0 && grilla != null) {
                    if (adap == null) {
                        adap = new MangasRecAdapter(result, ActivityServerVisualNavegacion.this);
                        adap.setLastItemListener(ActivityServerVisualNavegacion.this);
                        adap.setMangaClickListener(ActivityServerVisualNavegacion.this);
                        grilla.setAdapter(adap);
                    } else {
                        adap.addAll(result);
                    }
                }
                cargando.setVisibility(ProgressBar.INVISIBLE);
                mStart = false;
                if (neuvaTarea) {
                    adap = null;
                    pagina = 1;
                    mStart = true;
                    s.hayMas = true;
                    new CargarUltima().execute(pagina);
                    neuvaTarea = false;
                }
            }
        }
    }

}
