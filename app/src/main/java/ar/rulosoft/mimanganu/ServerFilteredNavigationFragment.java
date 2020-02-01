package ar.rulosoft.mimanganu;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ar.rulosoft.mimanganu.adapters.MangaRecAdapterBase;
import ar.rulosoft.mimanganu.adapters.MangaRecAdapterBase.OnLastItem;
import ar.rulosoft.mimanganu.adapters.MangaRecAdapterBase.OnMangaClick;
import ar.rulosoft.mimanganu.adapters.MangasRecAdapter;
import ar.rulosoft.mimanganu.adapters.MangasRecAdapterText;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.FilterViewGenerator;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.utils.AsyncAddManga;
import ar.rulosoft.mimanganu.utils.ThemeColors;
import ar.rulosoft.mimanganu.utils.Util;

import static ar.rulosoft.mimanganu.MainActivity.colors;

public class ServerFilteredNavigationFragment extends Fragment implements OnLastItem, OnMangaClick, FilterViewGenerator.FilterListener {

    int serverID;
    private boolean mStart = true;
    private ServerBase serverBase;
    private RecyclerView recyclerViewGrid;
    private ProgressBar loading;
    private MangaRecAdapterBase mAdapter;
    private boolean newTask = false;
    private int top_page = 0;
    private int req_page = 1;
    private int[][] filters = null;
    private int firstVisibleItem;
    private LoadLastTask loadLastTask = new LoadLastTask();
    private int lastContextMenuIndex = 0;
    private boolean mangaAlreadyAdded;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Thread t0 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Manga> mangas = Database.getMangas(getContext(), null, true);
                    if (mAdapter != null) {
                        for (Manga m : mangas) {
                            if (m.getPath().equals(mAdapter.getItem(lastContextMenuIndex).getPath()))
                                mangaAlreadyAdded = true;
                        }
                    }
                } catch (Exception e) {
                    Log.e("SFNF", "Exception", e);
                    Util.getInstance().toast(getContext(), Log.getStackTraceString(e));
                }
            }
        });
        t0.start();

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_manga_item_server_nav, menu);
        if (mAdapter != null) {
            menu.setHeaderTitle(mAdapter.getItem((int) v.getTag()).getTitle());
        }
        lastContextMenuIndex = (int) v.getTag();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!mangaAlreadyAdded) {
            AsyncAddManga nAsyncAddManga = new AsyncAddManga(mAdapter.getItem(lastContextMenuIndex), getActivity(), getView(), false, true, false);
            nAsyncAddManga.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            Util.getInstance().showFastSnackBar(getString(R.string.already_on_db), getView(), getActivity());
        }
        mangaAlreadyAdded = false;
        return super.onContextItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        serverID = getArguments().getInt(MainFragment.SERVER_ID);
        return inflater.inflate(R.layout.fragment_server_visual_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(getActivity());
        colors = ThemeColors.getColors(pm);
        ActionBar mActBar = getActivity().getActionBar();
        if (mActBar != null) {
            mActBar.setTitle(getResources()
                    .getString(R.string.listaen) + " " + serverBase.getServerName());
            mActBar.setDisplayHomeAsUpEnabled(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.setNavigationBarColor(colors[0]);
            window.setStatusBarColor(colors[4]);
        }
        serverBase = ServerBase.getServer(serverID, getContext());
        if (filters == null) {
            filters = serverBase.getBasicFilter();
        }
        recyclerViewGrid = getView().findViewById(R.id.grilla);
        if (serverBase.getFilteredType() == ServerBase.FilteredType.TEXT) {
            DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
            Drawable sd = ContextCompat.getDrawable(getContext(), R.drawable.divider);
            sd.setColorFilter(colors[0], PorterDuff.Mode.DARKEN);
            divider.setDrawable(sd);
            recyclerViewGrid.addItemDecoration(divider);
        }
        loading = getView().findViewById(R.id.loading);
        int columnSize = Integer.parseInt(pm.getString("grid_columns", "-1"));
        if (columnSize == -1 || pm.getBoolean("grid_columns_automatic_detection", true))
            columnSize = Util.getInstance().getGridColumnSizeFromWidth(getActivity());
        if (serverBase.getFilteredType() == ServerBase.FilteredType.TEXT)
            columnSize = 1;
        recyclerViewGrid.setLayoutManager(new GridLayoutManager(getActivity(), columnSize));
        if (mAdapter != null) {
            mAdapter.setOnCreateContextMenuListener(this);
            recyclerViewGrid.setAdapter(mAdapter);
            recyclerViewGrid.getLayoutManager().scrollToPosition(firstVisibleItem);
            loading.setVisibility(View.INVISIBLE);
        } else {
            loadLastTask = (LoadLastTask) new LoadLastTask().execute(req_page);
        }
    }

    @Override
    public void onRequestedLastItem() {
        if (!loading.isShown() && !mStart)
            loadLastTask = (LoadLastTask) new LoadLastTask().execute(req_page);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setTitle(getResources().getString(R.string.listaen) + " " + serverBase.getServerName());
        ((MainActivity) getActivity()).enableHomeButton(true);
    }

    @Override
    public void onPause() {
        firstVisibleItem = ((GridLayoutManager) recyclerViewGrid.getLayoutManager()).findFirstVisibleItemPosition();

        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        loadLastTask.cancel(true);
    }

    @Override
    public void onMangaClick(Manga manga) {
        Bundle bundle = new Bundle();
        bundle.putInt(MainFragment.SERVER_ID, serverBase.getServerID());
        bundle.putString(DetailsFragment.TITLE, manga.getTitle());
        bundle.putString(DetailsFragment.PATH, manga.getPath());
        bundle.putString(DetailsFragment.IMG, manga.getImages());
        DetailsFragment detailsFragment = new DetailsFragment();
        detailsFragment.setArguments(bundle);
        ((MainActivity) getActivity()).replaceFragment(detailsFragment, "DetailsFragment");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.manga_server_visual, menu);
        MenuItem search = menu.findItem(R.id.action_search);
        if (!serverBase.hasList()) {
            MenuItem item = menu.findItem(R.id.ver_como_lista);
            item.setVisible(false);
        }
        if (serverBase.getServerFilters().length == 0) {
            MenuItem item = menu.findItem(R.id.filter);
            item.setVisible(false);
        }
        if (!serverBase.hasSearch()) {
            MenuItem item = menu.findItem(R.id.action_search);
            item.setVisible(false);
        }
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String value) {
                Bundle bundle = new Bundle();
                bundle.putInt(MainFragment.SERVER_ID, serverBase.getServerID());
                bundle.putString(SearchResultsFragment.TERM, value);
                SearchResultsFragment searchResultsFragment = new SearchResultsFragment();
                searchResultsFragment.setArguments(bundle);
                ((MainActivity) getActivity()).replaceFragment(searchResultsFragment, "SearchFragment");
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.ver_como_lista) {
            ServerListFragment fragment = new ServerListFragment();
            Bundle b = new Bundle();
            b.putInt(MainFragment.SERVER_ID, serverBase.getServerID());
            fragment.setArguments(b);
            ((MainActivity) getActivity()).replaceFragment(fragment, "FilteredServerList");
        } else if (item.getItemId() == R.id.filter) {
            FilterViewGenerator mFilter = new FilterViewGenerator(getActivity(), "Filter", serverBase.getServerFilters(), filters, colors[0]);
            mFilter.getDialog().show();
            mFilter.setFilterListener(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void applyFilter(int[][] selectedIndexes) {
        if (!loading.isShown()) {
            filters = selectedIndexes;
            mAdapter = null;
            top_page = 0;
            req_page = 1;
            mStart = true;
            loadLastTask = (LoadLastTask) new LoadLastTask().execute(req_page);
        } else {
            newTask = true;
        }
    }


    public class LoadLastTask extends AsyncTask<Integer, Void, ArrayList<Manga>> {
        String error = "";

        @Override
        protected void onPreExecute() {
            loading.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected ArrayList<Manga> doInBackground(Integer... params) {
            ArrayList<Manga> mangas = null;
            if (req_page > top_page) {
                try {
                    mangas = serverBase.getMangasFiltered(filters, params[0]);
                    top_page = req_page;
                } catch (Exception e) {
                    Log.e("SFNF", "Exception", e);
                    if (e.getMessage() != null)
                        error = e.getMessage();
                    else {
                        error = "null pointer";
                    }
                }
            }
            return mangas;
        }

        @Override
        protected void onPostExecute(ArrayList<Manga> result) {
            if (!error.isEmpty()) {
                Util.getInstance().toast(getContext(), error);
            } else {
                req_page++;
                if (result != null && result.size() != 0 && recyclerViewGrid != null) {
                    if (isAdded()) {
                        if (mAdapter == null) {
                            if (serverBase.getFilteredType() == ServerBase.FilteredType.VISUAL) {
                                mAdapter = new MangasRecAdapter(result, getActivity());
                            } else {
                                mAdapter = new MangasRecAdapterText(result, getActivity());
                            }
                            mAdapter.setOnCreateContextMenuListener(ServerFilteredNavigationFragment.this);
                            mAdapter.setLastItemListener(ServerFilteredNavigationFragment.this);
                            mAdapter.setMangaClickListener(ServerFilteredNavigationFragment.this);
                            recyclerViewGrid.setAdapter(mAdapter);
                        } else {
                            mAdapter.addAll(result);
                        }
                    }
                }
                mStart = false;
                if (newTask) {
                    mAdapter = null;
                    top_page = 0;
                    req_page = 1;
                    mStart = true;
                    loadLastTask = (LoadLastTask) new LoadLastTask().execute(req_page);
                    newTask = false;
                }
            }
            loading.setVisibility(ProgressBar.INVISIBLE);
        }
    }

}
