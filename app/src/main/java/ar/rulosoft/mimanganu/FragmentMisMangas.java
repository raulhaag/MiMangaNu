package ar.rulosoft.mimanganu;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.File;
import java.util.ArrayList;

import ar.rulosoft.mimanganu.adapters.MangaRecAdapterBase.OnMangaClick;
import ar.rulosoft.mimanganu.adapters.MisMangasAdapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.DownloadPoolService;

public class FragmentMisMangas extends Fragment implements OnMangaClick, OnCreateContextMenuListener {

    public static final String SELECT_MODE = "selector_modo";
    public static final int MODE_SHOW_ALL = 0;
    public static final int MODE_HIDE_READ = 1;

    public boolean search = false;

    private GridView grid;
    private MisMangasAdapter adapter;
    private SwipeRefreshLayout str;
    private NewSearchTask newSearch;


    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rView = inflater.inflate(R.layout.fragment_mis_mangas, container, false);
        grid = (GridView) rView.findViewById(R.id.grilla_mis_mangas);
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
        grid.setNumColumns(columnas);
        if (newSearch != null && newSearch.getStatus() == AsyncTask.Status.RUNNING) {
            str.post(new Runnable() {
                @Override
                public void run() {
                    str.setRefreshing(true);
                }
            });
        }
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ActivityManga.class);
                intent.putExtra(ActivityMisMangas.MANGA_ID, adapter.getItem(position).getId());
                getActivity().startActivity(intent);
            }
        });
        registerForContextMenu(grid);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.gridview_mismangas, menu);
        MenuItem m = menu.findItem(R.id.noupdate);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (adapter.getItem(info.position).isFinished()) {
            m.setTitle(getActivity().getResources().getString(R.string.buscarupdates));
        } else {
            m.setTitle(getActivity().getResources().getString(R.string.nobuscarupdate));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Manga m = (Manga) grid.getAdapter().getItem(info.position);
        if (item.getItemId() == R.id.borrar) {
            DownloadPoolService.forceStop(m.getId());
            ServerBase s = ServerBase.getServer(m.getServerId());
            String path = DownloadPoolService.generateBasePath(s, m, getActivity());
            deleteRecursive(new File(path));
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
        // ((ActivityMisMangas) getActivity()).button_add.attachToRecyclerView(grilla);
        int[] colors = ((ActivityMisMangas) getActivity()).colors;
        str.setColorSchemeColors(colors[0], colors[1]);
        super.onResume();
    }

    public void setListManga() {
        ArrayList<Manga> mangaList = new ArrayList<>();

        /**
         * sort_val: 0,1 = last_read (default), 2,3 = title, 4,5 = author
         *                  all odd numbers are asc, even numbers are desc
         *
         * feel free to add more sort type */
        int sort_val = PreferenceManager.getDefaultSharedPreferences(
                getActivity()).getInt("manga_view_sort_by", 0);

        String sort_by;
        boolean sort_ord = sort_val % 2 == 0;
        switch (sort_val) {
            case 2:
            case 3:
                sort_by = Database.COL_NAME;
                break;
            case 4:
            case 5:
                sort_by = Database.COL_AUTHOR;
                break;
            case 7:
            case 6:
                sort_by = Database.COL_SEARCH;
                sort_ord = !sort_ord;
                break;
            case 0:
            case 1:
            default:
                sort_by = Database.COL_LAST_READ;
                sort_ord = !sort_ord;
        }
        int value = PreferenceManager.getDefaultSharedPreferences(
                getActivity()).getInt(SELECT_MODE, MODE_SHOW_ALL);
        switch (value) {
            case MODE_SHOW_ALL:
                mangaList = Database.getMangas(getActivity(), sort_by, sort_ord);
                break;
            case MODE_HIDE_READ:
                mangaList = Database.getMangasCondition(getActivity(), "id IN (" +
                        "SELECT manga_id " +
                        "FROM capitulos " +
                        "WHERE estado != 1 GROUP BY manga_id)", sort_by, sort_ord);
                break;
            default:
                break;
        }
        adapter = new MisMangasAdapter(getActivity(), mangaList, ((ActivityMisMangas) getActivity()).darkTheme);
        //  adapter.setMangaClickListener(FragmentMisMangas.this);
        //adapter.setOnCreateContextMenuListener(FragmentMisMangas.this);
        grid.setAdapter(adapter);
    }

    @Override
    public void onMangaClick(Manga manga) {
        Intent intent = new Intent(getActivity(), ActivityManga.class);
        intent.putExtra(ActivityMisMangas.MANGA_ID, manga.getId());
        getActivity().startActivity(intent);
    }

    public class NewSearchTask extends AsyncTask<Void, String, Integer> {
        final ArrayList<Manga> mangas = Database.getMangasForUpdates(getActivity());
        String mMessage;
        String mTitle;
        int result = 0;
        int keys = 2;
        int threads;

        @Override
        protected void onPreExecute() {
            search = true;
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
            threads = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("update_threads_manual", "2"));
            keys = threads;
            for (int i = 0; i < mangas.size(); i++) {
                final int j = i;
                while (keys == 0)
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                keys--;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Manga manga = mangas.get(j);
                        ServerBase s = ServerBase.getServer(manga.getServerId());
                        try {
                            publishProgress("(" + (j + 1) + "/" + mangas.size() + ")" + manga.getTitle());
                            s.loadChapters(manga, false);
                            int diff = s.searchForNewChapters(manga.getId(), getActivity());
                            result += diff;
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            keys++;
                        }
                    }
                }).start();
            }
            while (keys < threads)
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
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
            search = false;
        }
    }
}
