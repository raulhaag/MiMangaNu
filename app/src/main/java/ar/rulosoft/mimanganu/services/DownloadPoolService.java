package ar.rulosoft.mimanganu.services;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import java.io.File;
import java.util.ArrayList;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.ChapterDownload.DownloadStatus;
import ar.rulosoft.mimanganu.services.SingleDownload.Status;
import ar.rulosoft.mimanganu.utils.NetworkUtilsAndReceiver;
import ar.rulosoft.mimanganu.utils.Util;

import static ar.rulosoft.mimanganu.utils.Paths.generateBasePath;

public class DownloadPoolService extends Service implements StateChangeListener {

    public static int SLOTS = 2;
    public static DownloadPoolService actual = null;
    public static ArrayList<ChapterDownload> chapterDownloads = new ArrayList<>();
    public static DownloadsChangesListener mDownloadsChangesListener;
    public static StateChangeListener mChapterStateChangeListener;
    public static int errors = 0;
    private static boolean intentPending = false;
    private static DownloadListener downloadListener = null;
    private static boolean resetN;
    private static boolean resetE;


    public int slots = SLOTS;
    private int mNotifyID = (int) System.currentTimeMillis();

    public static void addChapterDownloadPool(Activity activity, Chapter chapter, boolean reading) throws Exception {
        if (activity == null) {
            Log.d("DPS", "null");
        } else {
            if (!chapter.isDownloaded() && NetworkUtilsAndReceiver.isConnected(activity)) {
                if (isNewDownload(chapter.getId())) {
                    ChapterDownload dc = new ChapterDownload(chapter);
                    if (mDownloadsChangesListener != null) {
                        mDownloadsChangesListener.onChapterAdded(reading, dc);
                    }
                    if (reading) {
                        chapterDownloads.add(0, dc);
                    } else {
                        chapterDownloads.add(dc);
                    }
                } else {
                    for (int i = 0; i < chapterDownloads.size(); i++) {
                        ChapterDownload dc = chapterDownloads.get(i);
                        if (dc.chapter.getId() == chapter.getId()) {
                            if (dc.status == DownloadStatus.ERROR) {
                                dc.chapter.deleteImages(activity);
                                chapterDownloads.remove(dc);
                                if (mDownloadsChangesListener != null) {
                                    mDownloadsChangesListener.onChapterRemoved(i);
                                    mDownloadsChangesListener.onChapterAdded(reading, dc);
                                }
                                dc = null;
                                ChapterDownload ndc = new ChapterDownload(chapter);
                                if (reading) {
                                    chapterDownloads.add(0, ndc);
                                } else {
                                    chapterDownloads.add(ndc);
                                }
                            } else {
                                if (reading) {
                                    chapterDownloads.remove(dc);
                                    if (mDownloadsChangesListener != null) {
                                        mDownloadsChangesListener.onChapterRemoved(i);
                                        mDownloadsChangesListener.onChapterAdded(true, dc);
                                    }
                                    chapterDownloads.add(0, dc);
                                }
                            }
                            break;
                        }
                    }
                }
                initValues(activity);
            }
        }
    }

    private static void initValues(Context context) {
        if (context != null) {
            SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(context);
            int download_threads = Integer.parseInt(pm.getString("download_threads", "2"));
            int tolerance = Integer.parseInt(pm.getString("error_tolerancia", "5"));
            int retry = Integer.parseInt(pm.getString("reintentos", "4"));
            ChapterDownload.MAX_ERRORS = tolerance;
            SingleDownload.RETRY = retry;
            DownloadPoolService.SLOTS = download_threads;
            startService(context);
        }
    }

    public static void startService(Context context) {
        if (!intentPending && actual == null) {
            intentPending = true;
            context.startService(new Intent(context, DownloadPoolService.class));
        }
    }

    public static boolean isNewDownload(int cid) {
        boolean result = true;
        for (ChapterDownload dc : chapterDownloads) {
            if (dc.chapter.getId() == cid) {
                if (dc.status == DownloadStatus.DOWNLOADED) {
                    chapterDownloads.remove(dc);
                    return true;
                }
                result = false;
                break;
            }
        }
        return result;
    }

    public static boolean removeDownload(int cid, Context c) {
        boolean result = true;
        for (int i = 0; i < chapterDownloads.size(); i++) {
            if (chapterDownloads.get(i).chapter.getId() == cid) {
                if (chapterDownloads.get(i).status.ordinal() != DownloadStatus.DOWNLOADING.ordinal()) {
                    chapterDownloads.remove(i);
                    if (mDownloadsChangesListener != null) {
                        mDownloadsChangesListener.onChapterRemoved(i);
                    }
                } else {
                    Toast.makeText(c, R.string.quitar_descarga, Toast.LENGTH_LONG).show();
                    result = false;
                }
                break;
            }
        }
        return result;
    }

    public static void attachListener(ChapterDownload.OnErrorListener lector, int cid) {
        if (chapterDownloads != null && !chapterDownloads.isEmpty()) {
            ChapterDownload cd;
            for (int j = 0; j < chapterDownloads.size(); j++) {
                cd = chapterDownloads.get(j);
                if (cd.chapter.getId() == cid) {
                    cd.setErrorListener(lector);
                    break;
                }
            }
        }
    }

    public static void detachListener(int cid) {
        attachListener(null, cid);
    }

    public static void setDownloadListener(DownloadListener nDownloadListener) {
        downloadListener = nDownloadListener;
    }

    public static void forceStop(int mangaId) {
        for (ChapterDownload cd : chapterDownloads) {
            if (cd.getChapter().getMangaID() == mangaId) {
                cd.status = DownloadStatus.ERROR;
            }
        }
    }

    public static void pauseDownload() {
        if (mDownloadsChangesListener != null)
            for (int i = 0; i < chapterDownloads.size(); i++) {
                ChapterDownload cd = chapterDownloads.get(i);
                if (cd.status != DownloadStatus.ERROR && cd.status != DownloadStatus.DOWNLOADED) {
                    cd.status = DownloadStatus.PAUSED;
                    mDownloadsChangesListener.onStatusChanged(i, cd);
                }
            }
        else {
            for (int i = 0; i < chapterDownloads.size(); i++) {
                ChapterDownload cd = chapterDownloads.get(i);
                if (cd.status != DownloadStatus.ERROR && cd.status != DownloadStatus.DOWNLOADED) {
                    cd.status = DownloadStatus.PAUSED;
                }
            }
        }
    }

    public static void retryError(Context context) {
        resetE = true;
        if (mDownloadsChangesListener != null) {
            for (int i = 0; i < chapterDownloads.size(); i++) {
                ChapterDownload cd = chapterDownloads.get(i);
                if (cd.status == DownloadStatus.ERROR) {
                    chapterDownloads.set(i, new ChapterDownload(chapterDownloads.get(i).chapter));
                    mDownloadsChangesListener.onStatusChanged(i, chapterDownloads.get(i));
                }
            }
        } else {
            for (int i = 0; i < chapterDownloads.size(); i++) {
                ChapterDownload cd = chapterDownloads.get(i);
                if (cd.status == DownloadStatus.ERROR) {
                    chapterDownloads.set(i, new ChapterDownload(chapterDownloads.get(i).chapter));
                }
            }
        }
        startService(context);
    }

    public static void retryError(Context context, Chapter cid, ChapterDownload.OnErrorListener errorListener) {
        resetE = true;
        for (int i = 0; i < chapterDownloads.size(); i++) {
            ChapterDownload cd = chapterDownloads.get(i);
            if (cd.status == DownloadStatus.ERROR && cd.getChapter().getId() == cid.getId()) {
                ChapterDownload ncd = new ChapterDownload(cid);
                ncd.setErrorListener(errorListener);
                chapterDownloads.set(i, ncd);
            }
        }
        startService(context);
    }

    public static void resumeDownloads(Context context) {
        if (mDownloadsChangesListener != null) {
            for (int i = 0; i < chapterDownloads.size(); i++) {
                if (chapterDownloads.get(i).status == DownloadStatus.PAUSED) {
                    chapterDownloads.get(i).status = DownloadStatus.QUEUED;
                    mDownloadsChangesListener.onStatusChanged(i, chapterDownloads.get(i));
                }
            }
        } else {
            for (int i = 0; i < chapterDownloads.size(); i++) {
                if (chapterDownloads.get(i).status == DownloadStatus.PAUSED) {
                    chapterDownloads.get(i).status = DownloadStatus.QUEUED;
                }
            }
        }
        startService(context);
    }

    public static void removeDownloaded() {
        resetN = true;
        ArrayList<ChapterDownload> toRemove = new ArrayList<>();
        for (int i = 0; i < chapterDownloads.size(); i++) {
            if (chapterDownloads.get(i).status == DownloadStatus.DOWNLOADED) {
                toRemove.add(chapterDownloads.get(i));
            }
        }
        chapterDownloads.removeAll(toRemove);
        if (mDownloadsChangesListener != null) {
            mDownloadsChangesListener.onChaptersRemoved(toRemove);
        }
    }

    public static void removeAll() {
        resetN = true;
        resetE = true;
        ArrayList<ChapterDownload> toRemove = new ArrayList<>();
        for (int i = 0; i < chapterDownloads.size(); i++) {
            if (chapterDownloads.get(i).status != DownloadStatus.DOWNLOADING) {
                toRemove.add(chapterDownloads.get(i));
            }
        }
        chapterDownloads.removeAll(toRemove);
        if (mDownloadsChangesListener != null) {
            mDownloadsChangesListener.onChaptersRemoved(toRemove);
        }
    }

    public static void setDownloadsChangesListener(DownloadsChangesListener mDownloadsChangesListener) {
        DownloadPoolService.mDownloadsChangesListener = mDownloadsChangesListener;
    }

    public static void setStateChangeListener(StateChangeListener mChapterStateChangeListener) {
        DownloadPoolService.mChapterStateChangeListener = mChapterStateChangeListener;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        actual = this;
        intentPending = false;
        new Thread(new Runnable() {

            @Override
            public void run() {
                initPool();
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onChange(SingleDownload singleDownload) {
        if (singleDownload.status.ordinal() > Status.POSTPONED.ordinal()) {
            slots++;
        }
        if (downloadListener != null) {
            downloadListener.onImageDownloaded(singleDownload.cid, singleDownload.index + 1);// +1 reader manage from 1 .. n
        }
        if (mDownloadsChangesListener != null) {
            mDownloadsChangesListener.onStatusChanged(chapterDownloads.indexOf(singleDownload.cd), singleDownload.cd);
        }
    }

    @Override
    public void onStatusChanged(ChapterDownload chapterDownload) {
        if (mChapterStateChangeListener != null) {
            mChapterStateChangeListener.onStatusChanged(chapterDownload);
        }
    }

    private void initPool() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MMN:Wakelock");
        try {
            if (wakeLock != null) {
                wakeLock.acquire(Long.MAX_VALUE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Util.getInstance().createNotificationWithProgressbar(getApplicationContext(), mNotifyID, getResources().getString(R.string.downloading), "");
        Manga manga = null;
        ServerBase s = null;
        String path = "";
        int lcId = -1;
        int n = -1;
        errors = 0;
        while (hasDownloadsPending()) {
            if (slots > 0) {
                slots--;
                ChapterDownload dc = null;
                int sig = 1;
                int idx = 0;
                while (idx < chapterDownloads.size()) {
                    ChapterDownload d = chapterDownloads.get(idx);
                    idx++;
                    // start notification code
                    if (n > chapterDownloads.size())
                        n = idx;
                    if (n < idx)
                        n = idx;
                    if (resetN) {
                        n = 0;
                        resetN = false;
                    }
                    if (resetE) {
                        errors = 0;
                        resetE = false;
                    }
                    // end notification code
                    if (d.chapter.getPages() == 0) {
                        if (d.status != DownloadStatus.ERROR && d.status != DownloadStatus.PAUSED)
                            try {
                                ServerBase server;
                                Manga m = Database.getManga(getApplicationContext(), d.chapter.getMangaID());
                                server = ServerBase.getServer(m.getServerId(), getApplicationContext());
                                server.chapterInit(d.chapter);
                                d.reset();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (d.chapter.getPages() == 0)
                                    d.status = DownloadStatus.ERROR;
                            }
                        Database.updateChapter(getApplicationContext(), d.chapter);
                    }
                    if (d.getPagesStatusLength() == 0) {
                        d.reset();
                    }
                    if (d.status != DownloadStatus.ERROR && d.status != DownloadStatus.PAUSED) {
                        sig = d.getNext();
                        if (sig > -1) {
                            dc = d;
                            break;
                        }
                    }
                }
                if (dc != null) {
                    if (manga == null || manga.getId() != dc.chapter.getMangaID()) {
                        try {
                            manga = Database.getManga(actual.getApplicationContext(), dc.chapter.getMangaID());
                            s = ServerBase.getServer(manga.getServerId(), getApplicationContext());
                        } catch (Exception e) {
                            dc.status = DownloadStatus.ERROR;
                        }
                    }
                    if (lcId != dc.chapter.getId()) {
                        try {
                            lcId = dc.chapter.getId();
                            path = generateBasePath(s, manga, dc.chapter, getApplicationContext());
                            new File(path).mkdirs();
                        } catch (Exception e) {
                            dc.status = DownloadStatus.ERROR;
                        }
                    }
                    try {
                        if (errors < 1)
                            Util.getInstance().changeNotificationWithProgressbar(dc.getChapter().getPages(), sig, mNotifyID, getResources().getString(R.string.x_of_y_chapters_downloaded, (n - 1), chapterDownloads.size()), getResources().getString(R.string.downloading) + " " + dc.getChapter().getTitle(), true);
                        else {
                            Util.getInstance().changeNotificationWithProgressbar(dc.getChapter().getPages(), sig, mNotifyID, getResources().getString(R.string.x_of_y_chapters_downloaded, (n - 1), chapterDownloads.size()), "(" + getResources().getString(R.string.chapter_download_errors) + " " + errors + ")\n" + getResources().getString(R.string.downloading) + " " + dc.getChapter().getTitle(), true);
                        }
                        String source_file = s.getImageFrom(dc.chapter, sig);
                        String save_file = path + "/" + sig + ".jpg";
                        SingleDownload des;
                        des = new SingleDownload(getApplicationContext(), source_file, save_file, sig - 1, dc.chapter.getId(), dc, s.needRefererForImages());
                        des.setChangeListener(dc);
                        dc.setChangeListener(this);
                        new Thread(des).start();
                    } catch (Exception e) {
                        dc.setErrorIdx(sig - 1);
                        slots++;
                    }
                } else if (slots == 1) {
                    break;
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    slots++;
                }
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Util.getInstance().cancelNotification(mNotifyID);
        actual = null;
        stopSelf();
        try {
            if (wakeLock.isHeld())
                wakeLock.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasDownloadsPending() {
        for (int i = 0; i < chapterDownloads.size(); i++) {
            if (chapterDownloads.get(i).status == DownloadStatus.DOWNLOADING || chapterDownloads.get(i).status == DownloadStatus.QUEUED) {
                return true;
            }
        }
        return false;
    }


}
