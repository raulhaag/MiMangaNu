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

    public static final String SELECT_MODE = "selector_modo";
    public static final int MODE_SHOW_ALL = 0;
    public static final int MODE_HIDE_READ = 1;

    public boolean buscar = false;

    private RecyclerView grilla;
    private MangasRecAdapter adapter;
    private SwipeRefreshLayout str;
    private NewSearchTask newSearch;
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
                if (newSearch == null || newSearch.getStatus() == AsyncTask.Status.FINISHED) {
                    newSearch = new NewSearchTask();
                    newSearch.execute();
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
        if (newSearch != null && newSearch.getStatus() == AsyncTask.Status.RUNNING) {
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
        setListManga();
        ((ActivityMisMangas) getActivity()).button_add.attachToRecyclerView(grilla);
        int[] colors = ((ActivityMisMangas) getActivity()).colors;
        str.setColorSchemeColors(colors[0], colors[1]);
        super.onResume();
    }

    public void setListManga() {
        ArrayList<Manga> mangaList = new ArrayList<>();

        boolean sort_asc = PreferenceManager.getDefaultSharedPreferences(
                getActivity()).getBoolean("manga_view_sort_asc", false);

        /**
         * sortBy 0 = last_read (default), 1 = title, 2 = author
         * feel free to add more sort type */
        String sort_by;
        switch (PreferenceManager.getDefaultSharedPreferences(
                getActivity()).getInt("manga_view_sort_by", 0)) {
            case 1:
                sort_by = Database.COL_NAME;
                break;
            case 2:
                sort_by = Database.COL_AUTHOR;
                break;
            case 0:
            default:
                sort_by = Database.COL_LAST_READ;
                sort_asc = !sort_asc;
        }
        int value = PreferenceManager.getDefaultSharedPreferences(
                getActivity()).getInt(SELECT_MODE, MODE_SHOW_ALL);
        switch (value) {
            case MODE_SHOW_ALL:
                mangaList = Database.getMangas(getActivity(), sort_by, sort_asc);
                break;
            case MODE_HIDE_READ:
                mangaList = Database.getMangasCondition(getActivity(), "id IN (" +
                        "SELECT manga_id " +
                        "FROM capitulos " +
                        "WHERE estado != 1 GROUP BY manga_id " +
                        "ORDER BY Count(*) DESC)", sort_by, sort_asc);
                break;
            default:
                break;
        }
        adapter = new MangasRecAdapter(mangaList, getActivity(), ((ActivityMisMangas) getActivity()).darkTheme);
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

    public class NewSearchTask extends AsyncTask<Void, String, Integer> {
        String mMessage;
        String mTitle;

        @Override
        protected void onPreExecute() {
            buscar = true;
            mMessage = getActivity().getResources().getString(R.string.buscandonuevo);
            mTitle = getActivity().getTitle().toString();
            getActivity().setTitle(mMessage);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            final String s = values[0];
            mMessage = s;
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
                    publishProgress(manga.getTitle());
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
                getActivity().setTitle(mTitle);
                setListManga();
            } catch (Exception e) {
                e.printStackTrace();
            }
            str.setRefreshing(false);
            buscar = false;
        }
    }
}
