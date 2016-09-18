package ar.rulosoft.mimanganu;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.utils.NetworkUtilsAndReciever;
import ar.rulosoft.mimanganu.utils.Util;

/**
 * Created by jtx on 15.09.2016.
 */
public class AutomaticUpdateTask extends AsyncTask<Void, Integer, Integer> {
    ArrayList<Manga> mangaList;
    int threads = 2;
    int ticket = threads;
    int result = 0;
    int numNow = 0;
    String errorMsg = "";
    Context context;
    int mNotifyID = (int) System.currentTimeMillis();
    SharedPreferences pm;

    public AutomaticUpdateTask(Context context, SharedPreferences pm) {
        this.context = context;
        this.pm = pm;
        if (pm.getBoolean("include_finished_manga", false))
            mangaList = Database.getMangas(context, null, true);
        else
            mangaList = Database.getMangasForUpdates(context);
        threads = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("update_threads_manual", "2"));
        ticket = threads;
        mNotifyID = (int) System.currentTimeMillis();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (context != null) {
            try {
                if (NetworkUtilsAndReciever.isConnected(context)) {
                    Util.getInstance().createSearchingForUpdatesNotification(context, mNotifyID);
                    Util.getInstance().showFastSnackBar(context.getResources().getString(R.string.searching_for_updates), context);
                }
            } catch (Exception e) {
                if (e.getMessage() != null)
                    errorMsg = e.getMessage();
            }
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
        if (context != null && errorMsg.equals("")) {
            final boolean fast = pm.getBoolean("fast_update", true);
            ticket = threads;
            for (int idx = 0; idx < mangaList.size(); idx++) {
                if (Util.n > (48 - threads))
                    cancel(true);
                try {
                    if (NetworkUtilsAndReciever.isConnected(context)) {
                        final int idxNow = idx;
                        // If there is no ticket, sleep for 1 second and ask again
                        while (ticket < 1) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Log.e("ULT", "Update sleep failure", e);
                            }
                        }
                        ticket--;

                        // If tickets were passed, create new requests
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (!isCancelled()) {
                                        Manga manga = mangaList.get(idxNow);
                                        ServerBase serverBase = ServerBase.getServer(manga.getServerId());
                                        publishProgress(idxNow);
                                        serverBase.loadChapters(manga, false);
                                        result += serverBase.searchForNewChapters(manga.getId(), context, fast);
                                    }
                                } catch (Exception e) {
                                    Log.e("ULT", "Update server failure", e);
                                } finally {
                                    ticket++;
                                }
                            }
                        }).start();

                    } else {
                        Util.getInstance().cancelNotification(mNotifyID);
                        break;
                    }
                } catch (Exception e) {
                    if (e.getMessage() != null) {
                        errorMsg = e.getMessage();
                    }
                }
            }

            // After finishing the loop, wait for all threads to finish their task before ending
            while (ticket < threads) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("ULT", "After sleep failure", e);
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
                Util.getInstance().showFastSnackBar(context.getResources().getString(R.string.mgs_update_found, result), context);
            } else {
                Util.getInstance().cancelNotification(mNotifyID);
                if (!errorMsg.equals("")) {
                    Util.getInstance().showFastSnackBar(errorMsg, context);
                } else {
                    Util.getInstance().showFastSnackBar(context.getResources().getString(R.string.no_new_updates_found), context);
                }
            }
        } else {
            Util.getInstance().cancelNotification(mNotifyID);
        }
    }

    @Override
    protected void onCancelled() {
        Util.getInstance().cancelNotification(mNotifyID);
        if (context != null) {
            Util.getInstance().toast(context, context.getString(R.string.update_search_cancelled));
            if (Util.n > (48 - threads)) {
                Util.getInstance().toast(context, context.getString(R.string.notification_tray_is_full));
            }
        }
    }
}