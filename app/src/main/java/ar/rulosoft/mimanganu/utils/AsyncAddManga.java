package ar.rulosoft.mimanganu.utils;

import android.app.Activity;
import android.os.AsyncTask;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

/**
 * Created by Raul on 17/07/2016.
 */
public class AsyncAddManga extends AsyncTask<Void, Integer, Void> {
    Activity mActivity;
    String error;
    boolean allOk = true;
    boolean backOnFinish = false;
    Manga manga;
    int total = 0;
    int mNotifyID = (int) System.currentTimeMillis();

    public AsyncAddManga(Manga manga, Activity mActivity, boolean backOnFinish) {
        this.mActivity = mActivity;
        this.backOnFinish = backOnFinish;
        this.manga = manga;
    }

    @Override
    protected void onPreExecute() {
        Util.showFastSnackBar(mActivity.getString(R.string.adding_to_db) + " " + manga.getTitle(), mActivity);
        Util.getInstance().createNotificationWithProgressbar(mActivity, mNotifyID, mActivity.getResources().getString(R.string.adding_to_db), "");
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        ServerBase serverBase = ServerBase.getServer(manga.getServerId());
        try {
            serverBase.loadMangaInformation(manga, false);
            serverBase.loadChapters(manga, false);
            total = manga.getChapters().size();
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
            } else {
                allOk = false;
            }
        } catch (Exception e) {
            allOk = false;
            if (e.getMessage() != null) {
                error = e.getMessage();
            } else {
                error = "NullPointerException";
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(final Integer... values) {
        super.onProgressUpdate(values);
        Util.getInstance().changeNotificationWithProgressbar(total, values[0], mNotifyID, manga.getTitle(), mActivity.getResources().getString(R.string.adding_to_db) + " " + values[0] + "/" + total, true);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mActivity != null) {
            if (backOnFinish)
                mActivity.onBackPressed();
            if (!allOk) {
                Util.showFastSnackBar(error, mActivity);
            } else {
                Util.showFastSnackBar(mActivity.getString(R.string.agregado) + " " + manga.getTitle(), mActivity);
            }
        }
        Util.getInstance().cancelNotification(mNotifyID);
        super.onPostExecute(aVoid);
    }
}
