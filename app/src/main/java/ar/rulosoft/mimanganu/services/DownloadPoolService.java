package ar.rulosoft.mimanganu.services;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.ChapterDownload.DownloadStatus;
import ar.rulosoft.mimanganu.services.SingleDownload.Status;
import ar.rulosoft.mimanganu.utils.NetworkUtilsAndReciever;

public class DownloadPoolService extends Service implements StateChangeListener {

    private final static int[] illegalChars = {
            34, 60, 62, 124,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
            21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
            58, 42, 63, 92, 47
    };
    public static int SLOTS = 2;
    public static DownloadPoolService actual = null;
    public static ArrayList<ChapterDownload> chapterDownloads = new ArrayList<>();
    public static DownloadsChangesListener mDownloadsChangesListener;
    private static boolean intentPending = false;
    private static DownloadListener downloadListener = null;

    static {
        Arrays.sort(illegalChars);
    }

    public int slots = SLOTS;

    public static void addChapterDownloadPool(Activity activity, Chapter chapter, boolean lectura) throws Exception{
        if(activity == null)
            Log.d("DPS","null");

        if (!chapter.isDownloaded() && NetworkUtilsAndReciever.isConnected(activity)) {
            if (isNewDownload(chapter.getId())) {
                ChapterDownload dc = new ChapterDownload(chapter);
                if (mDownloadsChangesListener != null) {
                    mDownloadsChangesListener.onChapterAdded(lectura, dc);
                }
                if (lectura) {
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
                            dc = null;
                            ChapterDownload ndc = new ChapterDownload(chapter);
                            if (mDownloadsChangesListener != null) {
                                mDownloadsChangesListener.onChapterRemoved(i);
                                mDownloadsChangesListener.onChapterAdded(lectura, dc);
                            }
                            if (lectura) {
                                chapterDownloads.add(0, ndc);
                            } else {
                                chapterDownloads.add(ndc);
                            }
                        } else {
                            if (lectura) {
                                chapterDownloads.remove(dc);
                                if (mDownloadsChangesListener != null) {
                                    mDownloadsChangesListener.onChapterRemoved(i);
                                    mDownloadsChangesListener.onChapterAdded(lectura, dc);
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

    private static void initValues(Context context) {
        if(context != null) {
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

    private static boolean isNewDownload(int cid) {
        boolean result = true;
        for (ChapterDownload dc : chapterDownloads) {
            if (dc.chapter.getId() == cid) {
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
        for (ChapterDownload dc : chapterDownloads) {
            if (dc.chapter.getId() == cid) {
                dc.setErrorListener(lector);
                break;
            }
        }
    }

    public static void detachListener(int cid) {
        attachListener(null, cid);
    }

    public static String generateBasePath(ServerBase serverBase, Manga manga, Chapter chapter, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String dir = prefs.getString("directorio",
                Environment.getExternalStorageDirectory().getAbsolutePath());
        return dir + "/MiMangaNu/" + cleanFileName(serverBase.getServerName()) + "/" +
                cleanFileName(manga.getTitle()).trim() + "/" + cleanFileName(chapter.getTitle()).trim();
    }

    public static String generateBasePath(ServerBase serverBase, Manga manga, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String dir = prefs.getString("directorio",
                Environment.getExternalStorageDirectory().getAbsolutePath());
        return dir + "/MiMangaNu/" + cleanFileName(serverBase.getServerName()).trim() + "/" +
                cleanFileName(manga.getTitle()).trim();
    }

    private static String cleanFileName(String badFileName) {
        StringBuilder cleanName = new StringBuilder();
        for (int i = 0; i < badFileName.length(); i++) {
            int c = (int) badFileName.charAt(i);
            if (Arrays.binarySearch(illegalChars, c) < 0) {
                cleanName.append((char) c);
            }
        }
        return cleanName.toString();
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

    public static void retryError(Context context , Chapter cid, ChapterDownload.OnErrorListener errorListener) {
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
            downloadListener.onImageDownloaded(singleDownload.cid, singleDownload.index);
        }
        if (mDownloadsChangesListener != null) {
            mDownloadsChangesListener.onStatusChanged(chapterDownloads.indexOf(singleDownload.cd), singleDownload.cd);
        }
    }

    private void initPool() {
        Manga manga = null;
        ServerBase s = null;
        String path = "";
        int lcId = -1;
        while (hasDownloadsPending()) {
            if (slots > 0) {
                slots--;
                ChapterDownload dc = null;
                int sig = 1;
                int idx = 0;
                while (idx < chapterDownloads.size()) {
                    ChapterDownload d = chapterDownloads.get(idx);
                    idx++;
                    if (d.chapter.getPages() == 0) {
                        if (d.status != DownloadStatus.ERROR && d.status != DownloadStatus.PAUSED)
                            try {
                                ServerBase server;
                                Manga m = Database.getManga(getApplicationContext(), d.chapter.getMangaID());
                                server = ServerBase.getServer(m.getServerId());
                                server.chapterInit(d.chapter);
                                d.reset();
                            } catch (Exception e) {
                                d.status = DownloadStatus.ERROR;
                            }
                        Database.updateChapter(getApplicationContext(), d.chapter);
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
                            s = ServerBase.getServer(manga.getServerId());
                        } catch (Exception e) {
                            dc.status = DownloadStatus.ERROR;
                        }
                    }
                    if (lcId != dc.chapter.getId()) {
                        lcId = dc.chapter.getId();
                        path = generateBasePath(s, manga, dc.chapter, getApplicationContext());
                        new File(path).mkdirs();
                    }
                    try {
                        String origen = s.getImageFrom(dc.chapter, sig);
                        String destino = path + "/" + sig + ".jpg";
                        SingleDownload des;
                        des = new SingleDownload(origen, destino, sig - 1, dc.chapter.getId(), dc, s.needRefererForImages());
                        des.setChangeListener(dc);
                        dc.setChagesListener(this);
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
        actual = null;
        stopSelf();
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
