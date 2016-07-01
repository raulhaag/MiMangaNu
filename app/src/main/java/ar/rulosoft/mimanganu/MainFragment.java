package ar.rulosoft.mimanganu;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import ar.rulosoft.mimanganu.adapters.MisMangasAdapter;
import ar.rulosoft.mimanganu.adapters.ServerRecAdapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.MangaFolderSelect;
import ar.rulosoft.mimanganu.componentes.MoreMangasPageTransformer;
import ar.rulosoft.mimanganu.servers.FromFolder;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.utils.NetworkUtilsAndReciever;
import ar.rulosoft.mimanganu.utils.ThemeColors;
import ar.rulosoft.mimanganu.utils.Util;

/**
 * Created by Raul
 */

public class MainFragment extends Fragment implements View.OnClickListener, MainActivity.OnBackListener, MainActivity.OnKeyUpListener, ServerRecAdapter.OnEndActionModeListener{

    public static final String SERVER_ID = "server_id";
    public static final String MANGA_ID = "manga_id";
    public static final String SELECT_MODE = "selector_modo";
    public static final int MODE_SHOW_ALL = 0;
    public static final int MODE_HIDE_READ = 1;
    private static final String TAG = "MainFragment";
    private SharedPreferences pm;
    private Menu menu;
    private FloatingActionButton floatingActionButton_add;
    private boolean is_server_list_open = false;
    private ServerRecAdapter serverRecAdapter;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private GridView grid;
    private MisMangasAdapter adapter;
    private SwipeRefreshLayout swipeReLayout;
    private int mNotifyID = 1246502;
    private boolean returnToMangaList = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setRetainInstance(true);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mSectionsPagerAdapter = new SectionsPagerAdapter();
        if (getView() != null) {
            mViewPager = (ViewPager) getView().findViewById(R.id.pager);
            floatingActionButton_add = (FloatingActionButton) getView().findViewById(R.id.floatingActionButton_add);
            floatingActionButton_add.setOnClickListener(this);
            if (is_server_list_open) {
                ObjectAnimator anim = ObjectAnimator.ofFloat(floatingActionButton_add, "rotation", 360.0f, 315.0f);
                anim.setDuration(0);
                anim.start();
            }
        }
        pm = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (is_server_list_open)
            is_server_list_open = false;
        if(swipeReLayout != null)
            swipeReLayout.clearAnimation();
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(false, new MoreMangasPageTransformer());
        MainActivity activity = (MainActivity) getActivity();
        MainActivity.colors = ThemeColors.getColors(pm);
        activity.setColorToBars();
        if (MainActivity.darkTheme != pm.getBoolean("dark_theme", false)) {
            Util.getInstance().restartApp(getContext());
        }
        activity.enableHomeButton(false);
        activity.setTitle(getString(R.string.app_name));
        activity.backListener = this;
        activity.keyUpListener = this;
        floatingActionButton_add.setBackgroundTintList(ColorStateList.valueOf(MainActivity.colors[1]));
        if (!is_server_list_open && getView() != null) {
            ObjectAnimator anim =
                    ObjectAnimator.ofFloat(getView().findViewById(R.id.floatingActionButton_add), "rotation", 315.0f, 360.0f);
            anim.setDuration(0);
            anim.start();
        }
    }

    @Override
    public void onClick(View v) {
        if (serverRecAdapter.actionMode == null) {
            if (mViewPager.getCurrentItem() == 0) {
                is_server_list_open = true;
                ObjectAnimator anim =
                        ObjectAnimator.ofFloat(v, "rotation", 360.0f, 315.0f);
                anim.setDuration(200);
                anim.start();
                mViewPager.setCurrentItem(1);
            } else {
                is_server_list_open = false;
                ObjectAnimator anim =
                        ObjectAnimator.ofFloat(v, "rotation", 315.0f, 360.0f);
                anim.setDuration(200);
                anim.start();
                mViewPager.setCurrentItem(0);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.view_mismangas, menu);

        /** Set hide/unhide checkbox */
        boolean checkedRead = pm.getInt(SELECT_MODE, MODE_SHOW_ALL) > 0;
        menu.findItem(R.id.action_hide_read).setChecked(checkedRead);

        /** Set sort mode */
        int sortList[] = {
                R.id.sort_last_read, R.id.sort_last_read_desc,
                R.id.sort_name, R.id.sort_name_desc,
                R.id.sort_author, R.id.sort_author_desc,
                R.id.sort_finished, R.id.sort_finished_asc,
                R.id.sort_as_added_to_db_asc, R.id.sort_as_added_to_db_desc
        };
        menu.findItem(sortList[pm.getInt("manga_view_sort_by", 0)]).setChecked(true);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_view_download: {
                ((MainActivity) getActivity()).replaceFragment(new DownloadsFragment(), "DownloadFragment");
                break;
            }
            case R.id.action_hide_read: {
                item.setChecked(!item.isChecked());
                pm.edit().putInt(SELECT_MODE, item.isChecked() ? MODE_HIDE_READ : MODE_SHOW_ALL).apply();
                setListManga(true);
                break;
            }
            case R.id.action_settings: {
                ((MainActivity) getActivity()).replaceFragment(new PreferencesFragment(), "PreferencesFragment");
                break;
            }
            case R.id.action_edit_server_list:
                if(mViewPager.getCurrentItem() == 0){
                    mViewPager.setCurrentItem(1);
                    returnToMangaList = true;
                }
                serverRecAdapter.startActionMode();
                break;
            case R.id.sort_last_read: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 0).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_last_read_desc: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 1).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_name: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 2).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_name_desc: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 3).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_author: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 4).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_author_desc: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 5).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_finished: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 6).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_finished_asc: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 7).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_as_added_to_db_asc: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 8).apply();
                setListManga(true);
                break;
            }
            case R.id.sort_as_added_to_db_desc: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 9).apply();
                setListManga(true);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public ViewGroup getServerListView(ViewGroup container) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_add_manga, container, false);
        final RecyclerView server_list = (RecyclerView) viewGroup.findViewById(R.id.lista_de_servers);
        server_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        serverRecAdapter = new ServerRecAdapter(ServerBase.getServers(), pm, getActivity());
        serverRecAdapter.setEndActionModeListener(this);
        server_list.setAdapter(serverRecAdapter);
        serverRecAdapter.setOnServerClickListener(new ServerRecAdapter.OnServerClickListener() {
            @Override
            public void onServerClick(ServerBase server) {
                if (!(server instanceof FromFolder)) {
                    if (server.hasFilteredNavigation()) {
                        ServerFilteredNavigationFragment fragment = new ServerFilteredNavigationFragment();
                        Bundle b = new Bundle();
                        b.putInt(MainFragment.SERVER_ID, server.getServerID());
                        fragment.setArguments(b);
                        ((MainActivity) getActivity()).replaceFragment(fragment, "FilteredNavigation");
                    } else {
                        ServerListFragment fragment = new ServerListFragment();
                        Bundle b = new Bundle();
                        b.putInt(MainFragment.SERVER_ID, server.getServerID());
                        fragment.setArguments(b);
                        ((MainActivity) getActivity()).replaceFragment(fragment, "FilteredServerList");
                    }
                } else {
                    MangaFolderSelect dialog = new MangaFolderSelect();
                    dialog.setMainFragment(MainFragment.this);
                    dialog.show(getChildFragmentManager(), "fragment_find_folder");

                    Log.e("from file", "selected");
                }
            }
        });
        return viewGroup;
    }

    public void setListManga(boolean force) {
        if (grid != null) {
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
                case 6:
                case 7:
                    sort_by = Database.COL_SEARCH;
                    sort_ord = !sort_ord;
                    break;
                case 8:
                case 9:
                    sort_by = Database.COL_ID;
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
            if (adapter == null || sort_val < 2 || mangaList.size() > adapter.getCount() || force) {
                adapter = new MisMangasAdapter(getActivity(), mangaList, MainActivity.darkTheme);
                grid.setAdapter(adapter);
            }
        } else {
            Log.i(TAG, "grid was null");
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
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
        Manga manga = (Manga) grid.getAdapter().getItem(info.position);
        if (item.getItemId() == R.id.delete) {
            DownloadPoolService.forceStop(manga.getId());
            ServerBase serverBase = ServerBase.getServer(manga.getServerId());
            String path = DownloadPoolService.generateBasePath(serverBase, manga, getActivity());
            Util.getInstance().deleteRecursive(new File(path));
            Database.deleteManga(getActivity(), manga.getId());
            adapter.remove(manga);
            Util.getInstance().toast(getActivity(), getResources().getString(R.string.deleted, manga.getTitle()));
        } else if (item.getItemId() == R.id.noupdate) {
            if (manga.isFinished()) {
                manga.setFinished(false);
                Database.setUpgradable(getActivity(), manga.getId(), false);
            } else {
                manga.setFinished(true);
                Database.setUpgradable(getActivity(), manga.getId(), true);
            }
        }
        return super.onContextItemSelected(item);
    }


    public ViewGroup getMMView(ViewGroup container) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_mis_mangas, container, false);
        grid = (GridView) viewGroup.findViewById(R.id.grilla_mis_mangas);
        swipeReLayout = (SwipeRefreshLayout) viewGroup.findViewById(R.id.str);
        swipeReLayout.setColorSchemeColors(MainActivity.colors[0], MainActivity.colors[1]);
        swipeReLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                switch (NetworkUtilsAndReciever.getConnectionStatus(getActivity())){
                    case CONNECTED:
                        if (MainActivity.updateListTask == null || MainActivity.updateListTask.getStatus() == AsyncTask.Status.FINISHED) {
                            MainActivity.updateListTask = new UpdateListTask(getActivity());
                            MainActivity.updateListTask.execute();
                        }
                        break;
                    case NO_INET_CONNECTED:
                        Util.getInstance().toast(getActivity(), getString(R.string.no_internet_connection));
                        swipeReLayout.setRefreshing(false);
                        break;
                    case NO_WIFI_CONNECTED:
                        Util.getInstance().toast(getActivity(), getString(R.string.no_wifi_connection));
                        swipeReLayout.setRefreshing(false);
                        break;
                }
            }
        });
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
        if (MainActivity.updateListTask != null && MainActivity.updateListTask.getStatus() == AsyncTask.Status.RUNNING) {
            swipeReLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeReLayout.setRefreshing(true);
                }
            });
        }
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (serverRecAdapter.actionMode == null) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(MainFragment.MANGA_ID, adapter.getItem(position).getId());
                    MangaFragment mangaFragment = new MangaFragment();
                    mangaFragment.setArguments(bundle);
                    ((MainActivity) getActivity()).replaceFragment(mangaFragment, "MangaFragment");
                }
            }
        });
        registerForContextMenu(grid);
        setListManga(true);
        return viewGroup;
    }

    @Override
    public boolean onBackPressed() {
        if (mViewPager.getCurrentItem() == 1 && getView() != null) {
            ObjectAnimator anim =
                    ObjectAnimator.ofFloat(getView().findViewById(R.id.floatingActionButton_add), "rotation", 315.0f, 360.0f);
            anim.setDuration(200);
            anim.start();
            mViewPager.setCurrentItem(0);
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            menu.performIdentifierAction(R.id.submenu, 0);
            return true;
        }
        return false;
    }

    @Override
    public void onEndActionMode() {
        if(returnToMangaList){
            returnToMangaList = false;
            mViewPager.setCurrentItem(0);
        }
    }

    public class SectionsPagerAdapter extends PagerAdapter {
        ViewGroup[] pages;

        public SectionsPagerAdapter() {
            super();
            this.pages = new ViewGroup[2];

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(pages[position]);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ViewGroup viewGroup;
            if (pages[position] == null)
                if (position == 0) {
                    viewGroup = getMMView(container);
                    pages[0] = viewGroup;
                } else {
                    if (pages[1] == null) {
                        viewGroup = getServerListView(container);
                        pages[1] = viewGroup;
                    } else {
                        viewGroup = pages[1];
                    }
                }
            else {
                viewGroup = pages[position];
            }
            container.addView(viewGroup);
            return viewGroup;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        //deprecated but error if not found
        @SuppressWarnings("deprecation")
        @Override
        public void destroyItem(View container, int position, Object object) {
            destroyItem((ViewGroup) container, position, object);
        }
    }

    public class UpdateListTask extends AsyncTask<Void, Integer, Integer> {
        ArrayList<Manga> mangaList = Database.getMangasForUpdates(getContext());
        int threads = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("update_threads_manual", "2"));
        int ticket = threads;
        int result = 0;
        int numNow = 0;
        Context context;

        public UpdateListTask(Context context) {
            this.context = context;
            if (pm.getBoolean("include_finished_manga", false))
                mangaList = Database.getMangas(getContext(), null, true);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (context != null) {
                Util.getInstance().createSearchingForUpdatesNotification(getContext(), mNotifyID);
                Toast.makeText(context, getResources().getString(R.string.searching_for_updates), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (context != null) {
                Util.getInstance().changeSearchingForUpdatesNotification(context, mangaList.size(), ++numNow, mNotifyID, context.getResources().getString(R.string.searching_for_updates), numNow + "/" + mangaList.size() + " - " +
                        mangaList.get(values[0]).getTitle(), true);
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if (context != null) {
                ticket = threads;
                for (int idx = 0; idx < mangaList.size(); idx++) {
                    if (NetworkUtilsAndReciever.isWifiConnected(context) || NetworkUtilsAndReciever.isMobileConnected(context)) {
                        final int idxNow = idx;

                        // If there is no ticket, sleep for 1 second and ask again
                        while (ticket < 1) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Log.e(TAG, "Update sleep failure", e);
                            }
                        }
                        ticket--;

                        // If tickets were passed, create new requests
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Manga mManga = mangaList.get(idxNow);
                                ServerBase serverBase = ServerBase.getServer(mManga.getServerId());
                                publishProgress(idxNow);
                                try {
                                    if (!isCancelled()) {
                                        int diff = serverBase.searchForNewChapters(mManga.getId(), getActivity());
                                        result += diff;
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Update server failure", e);
                                } finally {
                                    ticket++;
                                }

                            }
                        }).start();

                    } else {
                        Util.getInstance().cancelNotification(mNotifyID);
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeReLayout.setRefreshing(false);
                            }
                        });
                        break;
                    }
                }

                // After finishing the loop, wait for all threads to finish their task before ending
                while (ticket < threads) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "After sleep failure", e);
                    }
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (context != null) {
                if (result > 0) {
                    Util.getInstance().cancelNotification(mNotifyID);
                    setListManga(true);
                    Toast.makeText(context, context.getResources().getString(R.string.mgs_update_found, result), Toast.LENGTH_LONG).show();
                } else {
                    Util.getInstance().cancelNotification(mNotifyID);
                    Toast.makeText(context, context.getResources().getString(R.string.no_new_updates_found), Toast.LENGTH_LONG).show();
                }
                swipeReLayout.setRefreshing(false);
            } else {
                Util.getInstance().cancelNotification(mNotifyID);
            }
        }

        @Override
        protected void onCancelled() {
            if (context != null)
                Util.getInstance().toast(context, getString(R.string.update_search_cancelled));
            swipeReLayout.setRefreshing(false);
        }
    }
}

