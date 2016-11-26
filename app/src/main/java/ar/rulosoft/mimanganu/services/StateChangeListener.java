package ar.rulosoft.mimanganu.services;

public interface StateChangeListener {
    void onChange(SingleDownload singleDownload);

    void onStatusChanged(ChapterDownload chapterDownload);
}