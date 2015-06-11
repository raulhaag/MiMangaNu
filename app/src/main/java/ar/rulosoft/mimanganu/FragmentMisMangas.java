package ar.rulosoft.mimanganu;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;

import ar.rulosoft.mimanganu.adapters.MangasRecAdapter;
import ar.rulosoft.mimanganu.adapters.MangasRecAdapter.OnMangaClick;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.DownloadPoolService;

public class FragmentMisMangas extends Fragment implements OnMangaClick, OnCreateContextMenuListener {

    public static final String SELECTOR_MODO = "selector_modo";
    public static final int MODO_ULTIMA_LECTURA_Y_NUEVOS = 0;
    public static final int MODO_SIN_LEER = 1;

    public boolean buscar = false;
    RecyclerView grilla;
    MangasRecAdapter adapter;
    SwipeRefreshLayout str;
    BuscarNuevo buscarNuevo;
    private Integer menuFor;

    public static void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);
        fileOrDirectory.delete();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rView = inflater.inflate(R.layout.fragment_mis_mangas, container, false);
        grilla = (RecyclerView) rView.findViewById(R.id.grilla_mis_mangas);
        str = (SwipeRefreshLayout) rView.findViewById(R.id.str);
        str.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(buscarNuevo == null ||buscarNuevo.getStatus() == AsyncTask.Status.FINISHED){
                buscarNuevo = new BuscarNuevo();
                buscarNuevo.execute();
                }
            }
        });
        return rView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = getResources().getDisplayMetrics().density;
        float dpWidth = outMetrics.widthPixels / density;
        int columnas = (int) (dpWidth / 150);
        if (columnas < 2)
            columnas = 2;
        else if (columnas > 6)
            columnas = 6;
        grilla.setLayoutManager(new GridLayoutManager(getActivity(), columnas));
        if(buscarNuevo != null && buscarNuevo.getStatus() == AsyncTask.Status.RUNNING){
            str.post(new Runnable() {
                @Override
                public void run() {
                    str.setRefreshing(true);
                }
            });
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.gridview_mismangas, menu);
        MenuItem m = menu.findItem(R.id.noupdate);
        menuFor = (Integer) v.getTag();
        if (adapter.getItem(menuFor).isFinished()) {
            m.setTitle(getActivity().getResources().getString(R.string.buscarupdates));
        } else {
            m.setTitle(getActivity().getResources().getString(R.string.nobuscarupdate));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Manga m = adapter.getItem(menuFor);
        if (item.getItemId() == R.id.borrar) {
            ServerBase s = ServerBase.getServer(m.getServerId());
            String ruta = DownloadPoolService.generarRutaBase(s, m, getActivity());
            DeleteRecursive(new File(ruta));
            Database.deleteManga(getActivity(), m.getId());
            adapter.remove(m);
        } else if (item.getItemId() == R.id.noupdate) {
            if (m.isFinished()) {
                m.setFinished(false);
                Database.setUpgradable(getActivity(), m.getId(), false);
            } else {
                m.setFinished(true);
                Database.setUpgradable(getActivity(), m.getId(), true);
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onResume() {
        cargarMangas();
        ((ActivityMisMangas) getActivity()).button_add.attachToRecyclerView(grilla);
        int[] colors = ((ActivityMisMangas) getActivity()).colors;
        str.setColorSchemeColors(colors[0], colors[1]);
        super.onResume();
    }

    public void cargarMangas() {
        ArrayList<Manga> mangas = new ArrayList<Manga>();
        int value = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(SELECTOR_MODO, MODO_ULTIMA_LECTURA_Y_NUEVOS);
        switch (value) {
            case MODO_ULTIMA_LECTURA_Y_NUEVOS:
                mangas = Database.getMangas(getActivity());
                break;
            case MODO_SIN_LEER:
                mangas = Database.getMangasCondition(getActivity(),
                        "id in (select manga_id from capitulos where estado != 1 group by manga_id order by count(*) desc)");
                break;
            default:
                break;
        }
        adapter = new MangasRecAdapter(mangas, getActivity());
        adapter.setMangaClickListener(FragmentMisMangas.this);
        adapter.setOnCreateContextMenuListener(FragmentMisMangas.this);
        grilla.setAdapter(adapter);
    }

    @Override
    public void onMangaClick(Manga manga) {
        Intent intent = new Intent(getActivity(), ActivityManga.class);
        intent.putExtra(ActivityMisMangas.MANGA_ID, manga.getId());
        getActivity().startActivity(intent);
    }

    public class BuscarNuevo extends AsyncTask<Void, String, Integer> {
        String msg;
        String titulo;

        @Override
        protected void onPreExecute() {
            buscar = true;
            msg = getActivity().getResources().getString(R.string.buscandonuevo);
            titulo = getActivity().getTitle().toString();
            getActivity().setTitle(msg);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            final String s = values[0];
            msg = s;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().setTitle(s);
                }
            });
            super.onProgressUpdate(values);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            ArrayList<Manga> mangas = Database.getMangasForUpdates(getActivity());
            int result = 0;
            Database.removeOrphanedChapters(getActivity());
            for (int i = 0; i < mangas.size(); i++) {
                Manga manga = mangas.get(i);
                ServerBase s = ServerBase.getServer(manga.getServerId());
                try {
                    onProgressUpdate(manga.getTitle());
                    s.loadChapters(manga, false);
                    int diff = s.searchForNewChapters(manga.getId(), getActivity());
                    result += diff;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            try {
                getActivity().setTitle(titulo);
                cargarMangas();
            } catch (Exception e) {
                e.printStackTrace();
            }
            str.setRefreshing(false);
            buscar = false;
        }
    }
}
