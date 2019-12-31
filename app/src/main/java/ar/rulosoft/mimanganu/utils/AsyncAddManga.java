package ar.rulosoft.mimanganu.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import ar.rulosoft.mimanganu.MainFragment;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

/**
 * Created by Raul on 17/07/2016.
 */
public class AsyncAddManga extends AsyncTask<Void, Integer, Void> {
    private Activity mActivity;
    private String error = null;
    private boolean allOk = true;
    private boolean backOnFinish;
    private Manga manga;
    private int total = 0;
    private int mNotifyID = (int) System.currentTimeMillis();
    private boolean loadMangaInformation;
    private boolean showProgressDialog;
    private ProgressDialog addingProgressDialog;
    private View view;

    public AsyncAddManga(Manga manga, Activity mActivity, View view, boolean backOnFinish, boolean loadMangaInformation, boolean showProgressDialog) {
        this.mActivity = mActivity;
        this.view = view;
        this.backOnFinish = backOnFinish;
        this.manga = manga;
        this.loadMangaInformation = loadMangaInformation;
        this.showProgressDialog = showProgressDialog;
        this.addingProgressDialog = new ProgressDialog(mActivity);
    }

    @Override
    protected void onPreExecute() {
        if (showProgressDialog && addingProgressDialog != null) {
            addingProgressDialog.setMessage(mActivity.getResources().getString(R.string.adding_to_db));
            addingProgressDialog.show();
        }
        Util.getInstance().showFastSnackBar(mActivity.getString(R.string.adding_to_db) + " " + manga.getTitle(), view, mActivity);
        Util.getInstance().createNotificationWithProgressbar(mActivity, mNotifyID, mActivity.getResources().getString(R.string.adding_to_db), "");
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        ServerBase serverBase = ServerBase.getServer(manga.getServerId(), mActivity);
        try {
            if (loadMangaInformation)
                serverBase.loadMangaInformation(manga, false);
            serverBase.loadChapters(manga, false);
            total = manga.getChapters().size();
            manga.setVault(MainFragment.currentVault);
            int mid = Database.addManga(mActivity, manga);
            if (mid > -1) {
                long initTime = System.currentTimeMillis();
                for (int i = 0; i < manga.getChapters().size(); i++) {
                    if (System.currentTimeMillis() - initTime > 500) {
                        publishProgress(i);
                        initTime = System.currentTimeMillis();
                    }
                    Database.addChapter(mActivity, manga.getChapter(i), mid);
                }
                publishProgress(total);
            } else {
                allOk = false;
            }
        } catch (Exception e) {
            allOk = false;
            error = Log.getStackTraceString(e);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(final Integer... values) {
        super.onProgressUpdate(values);
        if (showProgressDialog) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (addingProgressDialog != null && addingProgressDialog.isShowing()) {
                        addingProgressDialog.setMessage(mActivity.getResources().getString(R.string.adding_to_db) + " " + values[0] + "/" + total);
                    }
                }
            });
        }
        Util.getInstance().changeNotificationWithProgressbar(total, values[0], mNotifyID, mActivity.getResources().getString(R.string.adding_to_db), manga.getTitle() + " " + values[0] + "/" + total, true);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        try {
            if (showProgressDialog && addingProgressDialog != null && addingProgressDialog.isShowing())
                addingProgressDialog.dismiss();
        } catch (Exception e) {
            Log.e("AsyncAddManga", "Exception", e);
        }
        if (mActivity != null) {
            if (backOnFinish)
                mActivity.onBackPressed();
            if (!allOk && error != null) {
                Util.getInstance().showFastSnackBar(error, view, mActivity);
            } else if (allOk) {
                Util.getInstance().showFastSnackBar(mActivity.getString(R.string.agregado) + " " + manga.getTitle(), view, mActivity);
            }
        }
        Util.getInstance().cancelNotification(mNotifyID);
        super.onPostExecute(aVoid);
    }
}
