package ar.rulosoft.mimanganu;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.adapters.MangaRecAdapterBase;
import ar.rulosoft.mimanganu.adapters.MangaRecAdapterBase.OnLastItem;
import ar.rulosoft.mimanganu.adapters.MangaRecAdapterBase.OnMangaClick;
import ar.rulosoft.mimanganu.adapters.MangasRecAdapter;
import ar.rulosoft.mimanganu.adapters.MangasRecAdapterText;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.utils.ThemeColors;

public class ServerFilteredNavigationFragment extends Fragment implements OnLastItem, OnMangaClick {

    int serverID;
    private boolean mStart = true;
    private ServerBase serverBase;
    private RecyclerView grid;
    private ProgressBar loading;
    private MangaRecAdapterBase mAdapter;
    private boolean newTask = false;
    private int page = 1;
    private MenuItem search;
    private int filter = 0;
    private int order = 0;
    private int firstVisibleItem;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
        int[] colors = ThemeColors.getColors(pm);
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


        serverBase = ServerBase.getServer(serverID);
        grid = (RecyclerView) getView().findViewById(R.id.grilla);
        loading = (ProgressBar) getView().findViewById(R.id.loading);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = getResources().getDisplayMetrics().density;
        float dpWidth = outMetrics.widthPixels / density;
        int columnas = (int) (dpWidth / 150);
        if (serverBase.getFilteredType() == ServerBase.FilteredType.TEXT)
            columnas = 1;
        else if (columnas == 0)
            columnas = 2;
        else if (columnas > 6)
            columnas = 6;
        grid.setLayoutManager(new GridLayoutManager(getActivity(), columnas));
        if (mAdapter != null) {
            grid.setAdapter(mAdapter);
            grid.getLayoutManager().scrollToPosition(firstVisibleItem);
            loading.setVisibility(View.INVISIBLE);
        } else {
            new LoadLastTask().execute(page);
        }
    }

    @Override
    public void onRequestedLastItem() {
        if (serverBase.hasMore && !loading.isShown() && !mStart)
            new LoadLastTask().execute(page);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setTitle(getResources().getString(R.string.listaen) + " " + serverBase.getServerName());
        ((MainActivity) getActivity()).enableHomeButton(true);
    }

    @Override
    public void onPause() {
        firstVisibleItem = ((GridLayoutManager) grid.getLayoutManager()).findFirstVisibleItemPosition();
        super.onPause();
    }

    @Override
    public void onMangaClick(Manga manga) {
        Bundle bundle = new Bundle();
        bundle.putInt(MainFragment.SERVER_ID, serverBase.getServerID());
        bundle.putString(DetailsFragment.TITLE, manga.getTitle());
        bundle.putString(DetailsFragment.PATH, manga.getPath());
        DetailsFragment detailsFragment = new DetailsFragment();
        detailsFragment.setArguments(bundle);
        ((MainActivity) getActivity()).replaceFragment(detailsFragment, "DetailsFragment");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.manga_server_visual, menu);
        search = menu.findItem(R.id.action_search);
        MenuItem vcl = menu.findItem(R.id.ver_como_lista);
        if (!serverBase.hasList())
            vcl.setVisible(false);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.filtrar)
                    .setItems(serverBase.getCategories(), new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!loading.isShown()) {
                                        filter = which;
                                        mAdapter = null;
                                        page = 1;
                                        mStart = true;
                                        serverBase.hasMore = true;
                                        new LoadLastTask().execute(page);
                                    } else {
                                        newTask = true;
                                    }
                                }
                            });

            builder.create().show();
        } else if (item.getItemId() == R.id.sort) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.filtrar)
                    .setItems(serverBase.getOrders(), new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!loading.isShown()) {
                                        order = which;
                                        mAdapter = null;
                                        page = 1;
                                        mStart = true;
                                        serverBase.hasMore = true;
                                        new LoadLastTask().execute(page);
                                    } else {
                                        newTask = true;
                                    }
                                }
                            });

            builder.create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    public class LoadLastTask extends AsyncTask<Integer, Void, ArrayList<Manga>> {

        String error = "";

        @Override
        protected void onPreExecute() {
            loading.setVisibility(ProgressBar.VISIBLE);
        }

        @SuppressWarnings("ResourceType")//TODO ver problema
        @Override
        protected ArrayList<Manga> doInBackground(Integer... params) {
            ArrayList<Manga> mangas = null;
            try {
                mangas = serverBase.getMangasFiltered(filter, order, params[0]);
            } catch (Exception e) {
                error = e.getMessage();
            }
            return mangas;
        }

        @Override
        protected void onPostExecute(ArrayList<Manga> result) {
            if (error != null && error.length() > 1) {
                Toast.makeText(getActivity(), "Error: " + error, Toast.LENGTH_SHORT).show();
            } else {
                page++;
                if (result != null && result.size() != 0 && grid != null) {
                    if(isAdded()) {
                        if (mAdapter == null) {
                            if (serverBase.getFilteredType() == ServerBase.FilteredType.VISUAL) {
                                mAdapter = new MangasRecAdapter(result, getActivity(), MainActivity.darkTheme);
                            } else {
                                mAdapter = new MangasRecAdapterText(result, getActivity(), MainActivity.darkTheme);
                            }
                            mAdapter.setLastItemListener(ServerFilteredNavigationFragment.this);
                            mAdapter.setMangaClickListener(ServerFilteredNavigationFragment.this);
                            grid.setAdapter(mAdapter);
                        } else {
                            mAdapter.addAll(result);
                        }
                    }
                }
                mStart = false;
                if (newTask) {
                    mAdapter = null;
                    page = 1;
                    mStart = true;
                    serverBase.hasMore = true;
                    new LoadLastTask().execute(page);
                    newTask = false;
                }
            }
            loading.setVisibility(ProgressBar.INVISIBLE);
        }
    }

}
