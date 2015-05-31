package ar.rulosoft.mimanganu.componentes;

import android.content.Context;

import java.io.File;

import ar.rulosoft.mimanganu.FragmentMisMangas;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.ServicioColaDeDescarga;

public class Chapter {

    public static final int NEW = -1;
    public static final int UNREAD = 0;
    public static final int READED = 1;
    public static final int READING = 2;

    int id, paginas, mangaID;
    int pagesReaded, readStatus;
    String title, path, extra;
    boolean finished, downloaded;

    public Chapter(String title, String path) {
        super();
        this.title = title;
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

    public int getPaginas() {
        return paginas;
    }

    public void setPaginas(int pagina) {
        this.paginas = pagina;
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

    public int getPagesReaded() {
        return pagesReaded;
    }

    public void setPagesReaded(int pagesReaded) {
        this.pagesReaded = pagesReaded;
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

    public void borrar(Context context, Manga manga, ServerBase s) {
        borrarImagenes(context, manga, s);
        Database.borrarCapitulo(context, this);
    }

    public void borrar(Context context) {
        Manga manga = Database.getManga(context, getMangaID());
        ServerBase s = ServerBase.getServer(manga.getServerId());
        borrar(context, manga, s);
    }

    public void borrarImagenes(Context context) {
        Manga manga = Database.getManga(context, getMangaID());
        ServerBase s = ServerBase.getServer(manga.getServerId());
        borrarImagenes(context, manga, s);
    }

    private void borrarImagenes(Context context, Manga manga, ServerBase s) {
        String ruta = ServicioColaDeDescarga.generarRutaBase(s, manga, this, context);
        FragmentMisMangas.DeleteRecursive(new File(ruta));
    }

    public void reset(Context context, Manga manga, ServerBase s) {
        String ruta = ServicioColaDeDescarga.generarRutaBase(s, manga, this, context);
        FragmentMisMangas.DeleteRecursive(new File(ruta));
        setPaginas(0);
        setDownloaded(false);
        setPagesReaded(0);
        Database.updateCapituloConDescarga(context, this);
    }

    public void borrarImagenesLiberarEspacio(Context context) {
        borrarImagenes(context);
        setDownloaded(false);
        Database.updateCapituloConDescarga(context, this);
    }

    public void borrarImagenesLiberarEspacio(Context context, Manga manga, ServerBase s) {
        borrarImagenes(context, manga, s);
        setDownloaded(false);
        Database.updateCapituloConDescarga(context, this);
    }

    public void reset(Context context) {
        Manga manga = Database.getManga(context, getMangaID());
        ServerBase s = ServerBase.getServer(manga.getServerId());
        reset(context, manga, s);
    }

    public void marcarComoLeido(Context c) {
        Database.marcarComoLeido(c, getId());
        setReadStatus(Chapter.READED);
    }
}
