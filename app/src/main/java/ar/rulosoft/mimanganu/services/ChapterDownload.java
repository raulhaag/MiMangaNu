package ar.rulosoft.mimanganu.services;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.services.SingleDownload.Status;

public class ChapterDownload implements StateChange {
    public static int MAX_ERRORS = 5;
    public DownloadStatus status;
    public Chapter chapter;
    private OnErrorListener errorListener = null;
    private StateChange chagesListener = null;
    private Status[] pagesStatus;
    private int progess = 0;

    public ChapterDownload(Chapter chapter) {
        this.chapter = chapter;
        reset();
    }

    private void reset() {
        pagesStatus = new Status[chapter.getPages()];
        for (int i = 0; i < pagesStatus.length; i++) {
            pagesStatus[i] = Status.QUEUED;
        }
        status = DownloadStatus.QUEUED;
    }

    private void changeStatus(DownloadStatus newStatus) {
        this.status = newStatus;
    }

    public int getNext() {
        int j = -2;
        if (status.ordinal() < DownloadStatus.DOWNLOADED.ordinal()) {
            if (status == DownloadStatus.QUEUED)
                changeStatus(DownloadStatus.DOWNLOADIND);
            if (areErrors()) {
                j = -11;
            } else if (progess < chapter.getPages()) {
                for (int i = 0; i < chapter.getPages(); i++) {
                    if (pagesStatus[i] == Status.QUEUED || pagesStatus[i] == Status.POSTPONED) {
                        pagesStatus[i] = Status.INIT;
                        j = i;
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
                if (errors > MAX_ERRORS) {
                    changeStatus(DownloadStatus.ERROR);
                    if (errorListener != null) {
                        errorListener.onError(chapter);
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

    public int getProgess() {
        return progess;
    }

    public void setProgess(int progess) {
        this.progess = progess;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public void setChagesListener(StateChange chagesListener) {
        this.chagesListener = chagesListener;
    }

    public void setErrorIdx(int idx) {
        pagesStatus[idx] = Status.ERROR_ON_UPLOAD;
        progess++;
        areErrors();
        checkProgreso();
    }

    private void checkProgreso() {
        if (progess == chapter.getPages()) {
            Database.updateChapterDownloaded(DownloadPoolService.actual, chapter.getId(), 1);
            changeStatus(DownloadStatus.DOWNLOADED);
        }
    }

    @Override
    public void onChange(SingleDownload singleDownload) {
        pagesStatus[singleDownload.index] = singleDownload.status;
        progess++;
        checkProgreso();
        if (chagesListener != null)
            chagesListener.onChange(singleDownload);
    }

    public void setErrorListener(OnErrorListener errorListener) {
        this.errorListener = errorListener;
        if (this.status == DownloadStatus.ERROR && errorListener != null) {
            errorListener.onError(chapter);
        }
    }

    public enum DownloadStatus {
        QUEUED, DOWNLOADIND, DOWNLOADED, ERROR
    }

    public interface OnErrorListener {
        void onError(Chapter chapter);
    }
}