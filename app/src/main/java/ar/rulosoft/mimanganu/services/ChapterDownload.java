package ar.rulosoft.mimanganu.services;

import android.util.Log;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.services.SingleDownload.Status;

public class ChapterDownload implements StateChangeListener {
    public static int MAX_ERRORS = 5;
    public DownloadStatus status;
    public Chapter chapter;
    private OnErrorListener errorListener = null;
    private StateChangeListener changeListener = null;
    private Status[] pagesStatus;
    private int progress = 0;

    public ChapterDownload(Chapter chapter) {
        this.chapter = chapter;
        reset();
    }

    public void reset() {
        pagesStatus = new Status[chapter.getPages()];
        for (int i = 0; i < pagesStatus.length; i++) {
            pagesStatus[i] = Status.QUEUED;
        }
        changeStatus(DownloadStatus.QUEUED);
    }

    private void changeStatus(DownloadStatus newStatus) {
        this.status = newStatus;
        if (changeListener != null) {
            changeListener.onStatusChanged(this);
        }
    }

    int getNext() {
        int j = -2;
        if (status.ordinal() < DownloadStatus.DOWNLOADED.ordinal()) {
            if (status == DownloadStatus.QUEUED)
                changeStatus(DownloadStatus.DOWNLOADING);
            if (chapter.getPages() == 0) {
                changeStatus(DownloadStatus.ERROR);
            }
            if (areErrors()) {
                j = -11;
            } else if (progress < chapter.getPages()) {
                for (int i = 0; i < chapter.getPages(); i++) {
                    if (pagesStatus.length > i) {
                        if (pagesStatus[i] == Status.QUEUED || pagesStatus[i] == Status.POSTPONED) {
                            pagesStatus[i] = Status.INIT;
                            j = i;
                            break;
                        }
                    } else {
                        Log.e("ChapterDownload", "i is too large! pagesStatus.length: " + pagesStatus.length + " i: " + i);
                        break;
                    }
                }
            }
        }
        return (j + 1);
    }

    private boolean areErrors() {
        int errors = 0;
        for (Status e : pagesStatus) {
            if (e.ordinal() > Status.DOWNLOAD_OK.ordinal()) {
                errors++;
                DownloadPoolService.errors++;
                if (errors > MAX_ERRORS) {
                    changeStatus(DownloadStatus.ERROR);
                    if (errorListener != null) {
                        errorListener.onError(chapter);
                    }
                    if (DownloadPoolService.mDownloadsChangesListener != null) {
                        DownloadPoolService.mDownloadsChangesListener.onStatusChanged(DownloadPoolService.chapterDownloads.indexOf(this), this);
                    }
                    break;
                }
            }
        }
        return errors > MAX_ERRORS;
    }

    public boolean isDownloading() {
        boolean ret = false;
        for (Status e : pagesStatus) {
            if (e.ordinal() < Status.POSTPONED.ordinal()) {
                ret = true;
                break;
            }
        }
        if (!ret)
            changeStatus(DownloadStatus.DOWNLOADED);
        return ret;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    void setChangeListener(StateChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    void setErrorIdx(int idx) {
        pagesStatus[idx] = Status.ERROR_ON_UPLOAD;
        progress++;
        areErrors();
        checkProgreso();
    }

    private void checkProgreso() {
        if (progress == chapter.getPages()) {
            Database.updateChapterDownloaded(DownloadPoolService.actual, chapter.getId(), 1);
            changeStatus(DownloadStatus.DOWNLOADED);
        }
    }

    @Override
    public void onStatusChanged(ChapterDownload chapterDownload) {
        //nothing to do here
    }

    @Override
    public void onChange(SingleDownload singleDownload) {
        pagesStatus[singleDownload.index] = singleDownload.status;
        progress++;
        checkProgreso();
        if (changeListener != null)
            changeListener.onChange(singleDownload);
    }

    void setErrorListener(OnErrorListener errorListener) {
        this.errorListener = errorListener;
        if (this.status == DownloadStatus.ERROR && errorListener != null) {
            errorListener.onError(chapter);
        }
    }

    public int getPagesStatusLength() {
        return pagesStatus.length;
    }

    public enum DownloadStatus {
        QUEUED, DOWNLOADING, DOWNLOADED, PAUSED, ERROR
    }

    public interface OnErrorListener {
        void onError(Chapter chapter);
    }
}