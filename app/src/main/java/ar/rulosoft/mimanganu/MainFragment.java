package ar.rulosoft.mimanganu;

import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
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

import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

import ar.rulosoft.mimanganu.adapters.MisMangasAdapter;
import ar.rulosoft.mimanganu.adapters.ServerRecAdapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.MoreMangasPageTransformer;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.utils.ThemeColors;
import ar.rulosoft.mimanganu.utils.Util;

/**
 * Created by Raul
 */

public class MainFragment extends Fragment implements View.OnClickListener, MainActivity.OnBackListener, MainActivity.OnKeyUpListener {

    public static final String SERVER_ID = "server_id";
    public static final String MANGA_ID = "manga_id";
    public static final String SELECT_MODE = "selector_modo";
    public static final int MODE_SHOW_ALL = 0;
    public static final int MODE_HIDE_READ = 1;
    private static final String TAG = "MainFragment";
    Menu menu;
    FloatingActionButton floatingActionButton_add;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private SharedPreferences pm;
    private GridView grid;
    private MisMangasAdapter adapter;
    private SwipeRefreshLayout swipeReLayout;
    private UpdateListTask newUpdate;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int mNotifyID = 1246502;

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
        }
        pm = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(false, new MoreMangasPageTransformer());
        MainActivity activity = (MainActivity) getActivity();
        activity.colors = ThemeColors.getColors(pm, getActivity());
        activity.setColorToBars();
        if (activity.darkTheme != pm.getBoolean("dark_theme", false)) {
            Util.getInstance().restartApp(getContext());
        }
        activity.enableHomeButton(false);
        activity.setTitle(getString(R.string.app_name));
        activity.backListener = this;
        activity.keyUpListener = this;
        floatingActionButton_add.setColorNormal(activity.colors[1]);
        floatingActionButton_add.setColorPressed(activity.colors[3]);
        floatingActionButton_add.setColorRipple(activity.colors[0]);
    }

    @Override
    public void onClick(View v) {
        if (mViewPager.getCurrentItem() == 0) {
            ObjectAnimator anim =
                    ObjectAnimator.ofFloat(v, "rotation", 360.0f, 315.0f);
            anim.setDuration(200);
            anim.start();
            mViewPager.setCurrentItem(1);
        } else {
            ObjectAnimator anim =
                    ObjectAnimator.ofFloat(v, "rotation", 315.0f, 360.0f);
            anim.setDuration(200);
            anim.start();
            mViewPager.setCurrentItem(0);
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
                R.id.sort_finished, R.id.sort_finished_asc
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
                pm.edit().putInt(SELECT_MODE,item.isChecked() ? MODE_HIDE_READ : MODE_SHOW_ALL
                ).apply();
                setListManga(true);
                break;
            }
            case R.id.action_settings: {
                ((MainActivity) getActivity()).replaceFragment(new PreferencesFragment(), "PreferencesFragment");
                break;
            }
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
        }
        return super.onOptionsItemSelected(item);
    }

    public ViewGroup getServerListView(ViewGroup container) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_add_manga, container, false);
        RecyclerView server_list = (RecyclerView) viewGroup.findViewById(R.id.lista_de_servers);
        server_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        ServerRecAdapter adapter = new ServerRecAdapter(ServerBase.getServers());
        server_list.setAdapter(adapter);
        adapter.setOnServerClickListener(new ServerRecAdapter.OnServerClickListener() {
            @Override
            public void onServerClick(ServerBase server) {
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
            if (adapter == null || sort_val < 2 || mangaList.size() > adapter.getCount() || force) {
                adapter = new MisMangasAdapter(getActivity(), mangaList, ((MainActivity) getActivity()).darkTheme);
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
        Manga m = (Manga) grid.getAdapter().getItem(info.position);
        if (item.getItemId() == R.id.delete) {
            DownloadPoolService.forceStop(m.getId());
            ServerBase s = ServerBase.getServer(m.getServerId());
            String path = DownloadPoolService.generateBasePath(s, m, getActivity());
            Util.getInstance().deleteRecursive(new File(path));
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


    public ViewGroup getMMView(ViewGroup container) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_mis_mangas, container, false);
        grid = (GridView) viewGroup.findViewById(R.id.grilla_mis_mangas);
        swipeReLayout = (SwipeRefreshLayout) viewGroup.findViewById(R.id.str);
        swipeReLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (newUpdate == null || newUpdate.getStatus() == AsyncTask.Status.FINISHED) {
                    mNotifyManager = (NotificationManager)
                            getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    mBuilder = new NotificationCompat.Builder(getActivity());

                    newUpdate = new UpdateListTask();
                    newUpdate.execute();
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
        if (newUpdate != null && newUpdate.getStatus() == AsyncTask.Status.RUNNING) {
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
                Bundle bundle = new Bundle();
                bundle.putInt(MainFragment.MANGA_ID, adapter.getItem(position).getId());
                MangaFragment fm = new MangaFragment();
                fm.setArguments(bundle);
                ((MainActivity) getActivity()).replaceFragment(fm, "MangaFragment");
            }
        });
        registerForContextMenu(grid);
        setListManga(true);
        return viewGroup;
    }

    @Override
    public boolean onBackPressed() {
        if (mViewPager.getCurrentItem() == 1) {
            ObjectAnimator anim =
                    ObjectAnimator.ofFloat(getView().findViewById(R.id.floatingActionButton_add), "rotation", 315.0f, 360.0f);
            anim.setDuration(200);
            anim.start();
            mViewPager.setCurrentItem(0);
            return true;
        }
        return false;
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
        @Override
        public void destroyItem(View container, int position, Object object) {
            destroyItem((ViewGroup) container, position, object);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            menu.performIdentifierAction(R.id.submenu, 0);
            return true;
        }
        return false;
    }

    public class UpdateListTask extends AsyncTask<Void, Integer, Integer> {
        final ArrayList<Manga> mList = Database.getMangasForUpdates(getActivity());
        int threads = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("update_threads_manual", "2"));
        int ticket = threads;
        int result = 0;
        int numNow = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Displays the progress bar for the first time.
            mBuilder.setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(getResources().getString(R.string.buscandonuevo))
                    .setContentText("")
                    .setAutoCancel(true)
                    .setOngoing(true);
            mBuilder.setProgress(100, 0, false);
            mNotifyManager.notify(mNotifyID, mBuilder.build());
            Context mContent = getActivity();
            if (mContent != null)
                Toast.makeText(mContent, getResources().getString(R.string.buscandonuevo),
                        Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // Update progress
            mBuilder.setProgress(mList.size(), ++numNow, false);
            mBuilder.setContentText(numNow + "/" + mList.size() + " - " +
                    mList.get(values[0]).getTitle());
            mNotifyManager.notify(mNotifyID, mBuilder.build());
            super.onProgressUpdate(values);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            ticket = threads;
            // Starting searching for new chapters
            for (int idx = 0; idx < mList.size(); idx++) {
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
                // If ticked were passed, create new request
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Manga mManga = mList.get(idxNow);
                        ServerBase servBase = ServerBase.getServer(mManga.getServerId());
                        publishProgress(idxNow);
                        try {
                            servBase.loadChapters(mManga, false);
                            int diff = servBase.searchForNewChapters(mManga.getId(), getActivity());
                            result += diff;
                        } catch (Exception e) {
                            Log.e(TAG, "Update server failure", e);
                        } finally {
                            ticket++;
                        }
                    }
                }).start();
            }
            // After finishing the loop, wait for all threads to finish their task before ending
            while (ticket < threads) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "After sleep failure", e);
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            Context mContent = getActivity();
            if (mContent != null) {
                if (result > 0) {
                    mBuilder.setContentTitle(
                            mContent.getResources()
                                    .getString(R.string.update_complete))
                            .setProgress(0, 0, false)
                            .setOngoing(false)
                            .setContentText(String.format(mContent.getResources()
                                    .getString(R.string.mgs_update_found), result));
                    mNotifyManager.notify(mNotifyID, mBuilder.build());
                    setListManga(true);
                    Toast.makeText(mContent, mContent.getResources().getString(R.string.mgs_update_found, result), Toast.LENGTH_LONG).show();
                } else {
                    mNotifyManager.cancel(mNotifyID);
                    Toast.makeText(mContent, mContent.getResources().getString(R.string.no_new_updates_found),Toast.LENGTH_LONG).show();
                }
                swipeReLayout.setRefreshing(false);
            } else {
                mNotifyManager.cancel(mNotifyID);
            }
        }
    }
}
