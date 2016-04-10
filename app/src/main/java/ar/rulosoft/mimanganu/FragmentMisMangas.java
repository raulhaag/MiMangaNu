package ar.rulosoft.mimanganu;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.util.Log;
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
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import ar.rulosoft.mimanganu.adapters.MangaRecAdapterBase.OnMangaClick;
import ar.rulosoft.mimanganu.adapters.MisMangasAdapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.DownloadPoolService;

public class FragmentMisMangas extends Fragment implements OnMangaClick, OnCreateContextMenuListener {

    private static final String TAG = "FragmentMisManga";

    public static final String SELECT_MODE = "selector_modo";
    public static final int MODE_SHOW_ALL = 0;
    public static final int MODE_HIDE_READ = 1;

    public boolean search = false;

    private GridView grid;
    private MisMangasAdapter adapter;
    private SwipeRefreshLayout swipeReLayout;
    private boolean attached = false, waiting = false, waitingForce;

    private UpdateListTask newUpdate;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int mNotifyID = 1246502;

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rView = inflater.inflate(R.layout.fragment_mis_mangas, container, false);
        grid = (GridView) rView.findViewById(R.id.grilla_mis_mangas);
        swipeReLayout = (SwipeRefreshLayout) rView.findViewById(R.id.str);
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
        return rView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
                Intent intent = new Intent(getActivity(), ActivityManga.class);
                intent.putExtra(FragmentMainMisMangas.MANGA_ID, adapter.getItem(position).getId());
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
        if (item.getItemId() == R.id.delete) {
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
        setListManga(false);
        // ((ActivityMisMangas) getActivity()).button_add.attachToRecyclerView(grilla);
        int[] colors = ((MainActivity) getActivity()).colors;
        swipeReLayout.setColorSchemeColors(colors[0], colors[1]);
        super.onResume();
    }

    public void setListManga(boolean force) {
        if (attached) {
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
            waiting = true;
            waitingForce = force;
        }
    }

    @Override
    public void onMangaClick(Manga manga) {
        Intent intent = new Intent(getActivity(), ActivityManga.class);
        intent.putExtra(FragmentMainMisMangas.MANGA_ID, manga.getId());
        getActivity().startActivity(intent);
    }

    public class UpdateListTask extends AsyncTask<Void, Integer, Integer> {
        final ArrayList<Manga> mList = Database.getMangasForUpdates(getActivity());
        int threads = Integer.parseInt(PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getString("update_threads_manual", "2"));
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
                } else {
                    mNotifyManager.cancel(mNotifyID);
                }
                Toast.makeText(mContent, mContent.getResources()
                                .getString(R.string.update_complete),
                        Toast.LENGTH_LONG).show();
                swipeReLayout.setRefreshing(false);
            } else {
                mNotifyManager.cancel(mNotifyID);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        attached = true;
        if (waiting) {
            setListManga(waitingForce);
            waiting = false;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        attached = false;
    }
}
