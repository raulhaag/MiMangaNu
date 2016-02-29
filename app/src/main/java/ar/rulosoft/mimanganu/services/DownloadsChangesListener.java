package ar.rulosoft.mimanganu.services;

import java.util.ArrayList;

/**
 * Created by Raul on 27/02/2016.
 */
public interface DownloadsChangesListener {
    void onProgressChanged(int idx, ChapterDownload cd);
    void onStatusChanged(int idx, ChapterDownload cd);
    void onChapterAdded(boolean atStart, ChapterDownload cd);
    void onChapterRemoved(int idx);
    void onChaptersRemoved(ArrayList<ChapterDownload> toRemove);
}
