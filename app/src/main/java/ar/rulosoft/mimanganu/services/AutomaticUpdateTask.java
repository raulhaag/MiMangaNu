package ar.rulosoft.mimanganu.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;

import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ar.rulosoft.mimanganu.MainActivity;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.utils.NetworkUtilsAndReceiver;
import ar.rulosoft.mimanganu.utils.Util;

import static android.content.Context.POWER_SERVICE;
import static ar.rulosoft.mimanganu.componentes.Database.COL_IS_FINISHED;
import static ar.rulosoft.mimanganu.componentes.Database.COL_VAULT;

/**
 * Created by jtx on 15.09.2016.
 */
public class AutomaticUpdateTask extends AsyncTask<Void, Integer, Integer> {
    public static int mNotifyID = (int) System.currentTimeMillis();
    public String error = "";
    private ArrayList<Manga> mangaList;
    private ArrayList<Manga> fromFolderMangaList;
    private int threads = 2;
    private int[] result;
    private int numNow = 0;
    private Context context;
    private SharedPreferences pm;
    private View view;
    PowerManager.WakeLock wakeLock;

    private long maxWakeLockTime;

    public AutomaticUpdateTask(Context context, View view, SharedPreferences pm, String vault) {
        this.context = context;
        this.view = view;
        this.pm = pm;
        if (vault == null) {
            vault = "";
        }
        if (pm.getBoolean("include_finished_manga", false)) {
            mangaList = Database.getMangasCondition(context, COL_VAULT + " = '" + vault + "'", null, true);
        } else {
            mangaList = Database.getMangasCondition(context, COL_IS_FINISHED + "= 0 AND " + COL_VAULT + " = '" + vault + "'", null, false);
        }

        fromFolderMangaList = Database.getFromFolderMangas(context);
        threads = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("update_threads_manual", "2"));
        mNotifyID = (int) System.currentTimeMillis();
        maxWakeLockTime = mangaList.size() * 15000;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MiMangaNu::MMNWakelock");
        if (context != null) {
            Util.getInstance().createSearchingForUpdatesNotification(context, mNotifyID);
            Util.getInstance().showFastSnackBar(context.getResources().getString(R.string.searching_for_updates), view, context);
        }
        wakeLock.acquire(maxWakeLockTime);
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
        if (context != null && error.isEmpty()) {
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            if (!NetworkUtilsAndReceiver.isConnectedNonDestructive(context)) {
                mangaList = fromFolderMangaList;
            }

            final HashMap<Integer, ServerBase> hosts = new HashMap<>();
            {
                // get a simple hash map of needed server to be checked for cfi
                for (Manga m : mangaList) {
                    if (!hosts.containsKey(m.getServerId())) {
                        hosts.put(m.getServerId(), ServerBase.getServer(m.getServerId(), context));
                    }
                }

                // check status of server and run cfi once per server if needed
                final ExecutorService executorLocal = Executors.newFixedThreadPool(threads);
                for (final int sid : hosts.keySet()) {
                    executorLocal.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ServerBase s = hosts.get(sid);
                                Log.i(s.getServerName(), "check Started");
                                if (s.hasFilteredNavigation()) {
                                    s.getMangasFiltered(s.getBasicFilter(), 1);
                                } else {
                                    s.getMangas();
                                }
                                Log.i(s.getServerName(), "check finished");
                            } catch (Exception e) {
                                Log.i("can' t confirm", "check failed");
                            }
                        }
                    });
                }
                executorLocal.shutdown();
                try {
                    executorLocal.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            result = new int[mangaList.size()];
            for (int idx = 0; idx < mangaList.size(); idx++) {
                if (MainActivity.isCancelled || Util.n > (48 - threads))
                    cancel(true);
                executor.execute(new SingleUpdateSearch(hosts.get(mangaList.get(idx).getServerId()), mangaList.get(idx), idx));
            }
            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int sum = 0;
        for (int i : result)
            sum += i;
        return sum;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        Util.getInstance().cancelNotification(mNotifyID);
        if (context != null) {
            if (result > 0) {
                Util.getInstance().showFastSnackBar(context.getResources().getString(R.string.mgs_update_found, "" + result), view, context);
            } else {
                if (!error.isEmpty()) {
                    Util.getInstance().toast(context, error);
                } else {
                    Util.getInstance().showFastSnackBar(context.getResources().getString(R.string.no_new_updates_found), view, context);
                }
            }
        }
        try {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        } catch (Exception e) {
            //ignore
        }
    }

    @Override
    protected void onCancelled() {
        Util.getInstance().cancelAllNotification();
        if (context != null) {
            Util.getInstance().toast(context, context.getString(R.string.update_search_cancelled));
            if (Util.n > (48 - threads)) {
                Util.getInstance().toast(context, context.getString(R.string.notification_tray_is_full));
            }
        }
        wakeLock.release();
    }

    private class SingleUpdateSearch implements Runnable {
        Manga manga;
        int idx;
        ServerBase serverBase;

        SingleUpdateSearch(ServerBase serverBase, Manga manga, int idx) {
            this.manga = manga;
            this.idx = idx;
            this.serverBase = serverBase;
        }


        SingleUpdateSearch(Manga manga, int idx) {
            this.manga = manga;
            this.idx = idx;
            this.serverBase = ServerBase.getServer(manga.getServerId(), context);

        }

        @Override
        public void run() {
            boolean fast = pm.getBoolean("fast_update", true);
            try {
                if (!isCancelled()) {
                    result[idx] += serverBase.searchForNewChapters(manga.getId(), context, fast);
                }
            } catch (Exception e) {
                //do nothing
            }
            publishProgress(idx);
        }
    }
}