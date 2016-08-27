package ar.rulosoft.mimanganu.utils;

import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;

import ar.rulosoft.mimanganu.MainActivity;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

/**
 * Created by Raul on 17/07/2016.
 */
public class AsyncAddManga extends AsyncTask<Manga, Integer, Void> {
    MainActivity mContext;
    String error;
    boolean allOk = true;
    CoordinatorLayout cLayout;
    Manga manga;
    int total = 0;
    int mNotifyID = (int) System.currentTimeMillis();

    public AsyncAddManga(MainActivity mActivity, CoordinatorLayout cLayout) {
        this.cLayout = cLayout;
        this.mContext = mActivity;
    }

    @Override
    protected void onPreExecute() {
        Util.getInstance().createNotificationWithProgressbar(mContext, mNotifyID, mContext.getResources().getString(R.string.adding_to_db), "");
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Manga... mangas) {
        manga = mangas[0];
        ServerBase serverBase = ServerBase.getServer(manga.getServerId());
        try {
            serverBase.loadMangaInformation(manga, false);
            serverBase.loadChapters(manga, false);
            total = manga.getChapters().size();
            int mid = Database.addManga(mContext, manga);
            if (mid > -1) {
                long initTime = System.currentTimeMillis();
                for (int i = 0; i < manga.getChapters().size(); i++) {
                    if (System.currentTimeMillis() - initTime > 500) {
                        publishProgress(i);
                        initTime = System.currentTimeMillis();
                    }
                    Database.addChapter(mContext, manga.getChapter(i), mid);
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
        Util.getInstance().changeNotificationWithProgressbar(total, values[0], mNotifyID, manga.getTitle(), mContext.getResources().getString(R.string.adding_to_db) + " " + values[0] + "/" + total, true);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mContext != null) {
            if (!allOk) {
                Util.showFastSnackBar(error, cLayout, mContext);
            } else {
                Util.showFastSnackBar(mContext.getString(R.string.agregado) + " " + manga.getTitle(), cLayout, mContext);
            }
        }
        Util.getInstance().cancelNotification(mNotifyID);
        super.onPostExecute(aVoid);
    }
}
