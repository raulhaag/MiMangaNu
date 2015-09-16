package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.text.Html;

import java.io.File;

import ar.rulosoft.mimanganu.FragmentMisMangas;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.DownloadPoolService;

public class Chapter {

    public static final int NEW = -1;
    public static final int UNREAD = 0;
    public static final int READ = 1;
    public static final int READING = 2;

    private int id;
    private int pages;
    private int mangaID;
    private int pagesRead;
    private int readStatus;
    private String title;
    private String path;
    private String extra;
    private boolean finished;
    private boolean downloaded;

    public Chapter(String title, String path) {
        super();
        this.title = Html.fromHtml(title).toString();
        this.path = path;
    }

    public int getMangaID() {
        return mangaID;
    }

    public void setMangaID(int mangaID) {
        this.mangaID = mangaID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return title;
    }

    public int getPagesRead() {
        return pagesRead;
    }

    public void setPagesRead(int pagesRead) {
        this.pagesRead = pagesRead;
    }

    public int getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(int readStatus) {
        this.readStatus = readStatus;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public void delete(Context context, Manga manga, ServerBase s) {
        deleteImages(context, manga, s);
        Database.deleteChapter(context, this);
    }

    public void delete(Context context) {
        Manga manga = Database.getManga(context, getMangaID());
        ServerBase s = ServerBase.getServer(manga.getServerId());
        delete(context, manga, s);
    }

    public void deleteImages(Context context) {
        Manga manga = Database.getManga(context, getMangaID());
        ServerBase s = ServerBase.getServer(manga.getServerId());
        deleteImages(context, manga, s);
    }

    private void deleteImages(Context context, Manga manga, ServerBase s) {
        String ruta = DownloadPoolService.generarRutaBase(s, manga, this, context);
        FragmentMisMangas.DeleteRecursive(new File(ruta));
    }

    public void reset(Context context, Manga manga, ServerBase s) {
        String ruta = DownloadPoolService.generarRutaBase(s, manga, this, context);
        FragmentMisMangas.DeleteRecursive(new File(ruta));
        setPages(0);
        setDownloaded(false);
        setPagesRead(0);
        Database.updateChapterPlusDownload(context, this);
    }

    public void freeSpace(Context context) {
        deleteImages(context);
        setDownloaded(false);
        Database.updateChapterPlusDownload(context, this);
    }

    public void freeSpace(Context context, Manga manga, ServerBase s) {
        deleteImages(context, manga, s);
        setDownloaded(false);
        Database.updateChapterPlusDownload(context, this);
    }

    public void reset(Context context) {
        Manga manga = Database.getManga(context, getMangaID());
        ServerBase s = ServerBase.getServer(manga.getServerId());
        reset(context, manga, s);
    }

    public void markRead(Context c, boolean read) {
        Database.markChapter(c, getId(), read);
        setReadStatus(read ? Chapter.READ : Chapter.UNREAD);
    }

    public void addChapterFirst(Manga manga) {
        manga.getChapters().add(0, this);
    }
}
