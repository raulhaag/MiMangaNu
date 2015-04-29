package ar.rulosoft.mimanganu.componentes;

import android.content.Context;

import java.io.File;

import ar.rulosoft.mimanganu.FragmentMisMangas;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.ServicioColaDeDescarga;

public class Capitulo {

    public static final int NUEVO = -1;
    public static final int SIN_LEER = 0;
    public static final int LEIDO = 1;
    public static final int LEYENDO = 2;

    int id, paginas, mangaID;
    int pagLeidas, estadoLectura;
    String titulo, path, extra;
    boolean finalizado, descargado;

    public Capitulo(String titulo, String path) {
        super();
        this.titulo = titulo;
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isFinalizado() {
        return finalizado;
    }

    public void setFinalizado(boolean finalizado) {
        this.finalizado = finalizado;
    }

    @Override
    public String toString() {
        return titulo;
    }

    public int getPagLeidas() {
        return pagLeidas;
    }

    public void setPagLeidas(int pagLeidas) {
        this.pagLeidas = pagLeidas;
    }

    public int getEstadoLectura() {
        return estadoLectura;
    }

    public void setEstadoLectura(int estadoLectura) {
        this.estadoLectura = estadoLectura;
    }

    public boolean isDescargado() {
        return descargado;
    }

    public void setDescargado(boolean descargado) {
        this.descargado = descargado;
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
        setDescargado(false);
        setPagLeidas(0);
        Database.updateCapituloConDescarga(context, this);
    }

    public void borrarImagenesLiberarEspacio(Context context) {
        borrarImagenes(context);
        setDescargado(false);
        Database.updateCapituloConDescarga(context, this);
    }

    public void borrarImagenesLiberarEspacio(Context context, Manga manga, ServerBase s) {
        borrarImagenes(context, manga, s);
        setDescargado(false);
        Database.updateCapituloConDescarga(context, this);
    }

    public void reset(Context context) {
        Manga manga = Database.getManga(context, getMangaID());
        ServerBase s = ServerBase.getServer(manga.getServerId());
        reset(context, manga, s);
    }

    public void marcarComoLeido(Context c) {
        Database.marcarComoLeido(c, getId());
        setEstadoLectura(Capitulo.LEIDO);
    }
}
