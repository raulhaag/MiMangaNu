package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public abstract class ServerBase {

    public static final int MANGAPANDA = 1;
    public static final int ESMANGAHERE = 3;
    public static final int MANGAHERE = 4;
    public static final int MANGAFOX = 5;
    public static final int SUBMANGA = 6;
    public static final int ESMANGA = 7;
    public static final int HEAVENMANGACOM = 8;
    public static final int STARKANACOM = 9;
    public static final int ESNINEMANGA = 10;
    public static final int LECTUREENLIGNE = 11;
    public static final int KISSMANGA = 12;
    public static final int ITNINEMANGA = 13;
    public static final int TUSMANGAS = 14;
    public boolean hayMas = true;
    private String serverName;
    private int icon;
    private int bandera;
    private int serverID;

    public static ServerBase getServer(int id) {
        ServerBase s = null;
        switch (id) {
            case MANGAPANDA:
                s = new MangaPanda();
                break;
            case ESMANGAHERE:
                s = new EsMangaHere();
                break;
            case MANGAHERE:
                s = new MangaHere();
                break;
            case MANGAFOX:
                s = new MangaFox();
                break;
            case SUBMANGA:
                s = new SubManga();
                break;
            case ESMANGA:
                s = new EsMangaCom();
                break;
            case HEAVENMANGACOM:
                s = new HeavenMangaCom();
                break;
            case STARKANACOM:
                s = new StarkanaCom();
                break;
            case ESNINEMANGA:
                s = new EsNineMangaCom();
                break;
            case LECTUREENLIGNE:
                s = new LectureEnLigne();
                break;
            case KISSMANGA:
                s = new KissManga();
                break;
            case ITNINEMANGA:
                s = new ItNineMangaCom();
                break;
            case TUSMANGAS:
                s = new TusMangasOnlineCom();
                break;
            default:
                break;
        }
        return s;
    }

    // server
    public abstract ArrayList<Manga> getMangas() throws Exception;

    public abstract ArrayList<Manga> getBusqueda(String termino) throws Exception;

    // capitulos
    public abstract void cargarCapitulos(Manga m, boolean recarga) throws Exception;

    public abstract void cargarPortada(Manga m, boolean recarga) throws Exception;

    // manga
    public abstract String getPagina(Chapter c, int pagina);

    public abstract String getImagen(Chapter c, int pagina) throws Exception;

    public abstract void iniciarCapitulo(Chapter c) throws Exception;

    // server visual
    public abstract ArrayList<Manga> getMangasFiltered(int categoria, int ordentipo, int pagina) throws Exception;

    public abstract String[] getCategorias();

    public abstract String[] getOrdenes();

    public abstract boolean tieneListado();

    // public abstract boolean supportStatus();

    public int buscarNuevosCapitulos(int id, Context context) throws Exception {
        int returnValue = 0;
        Manga mangaDb = Database.getFullManga(context, id);
        Manga manga = new Manga(mangaDb.getServerId(), mangaDb.getTitle(), mangaDb.getPath(), false);
        manga.setId(mangaDb.getId());
        this.cargarPortada(manga, true);
        this.cargarCapitulos(manga, false);
        int diff = manga.getChapters().size() - mangaDb.getChapters().size();
        if (diff > 0) {
            ArrayList<Chapter> simpleList = new ArrayList<>();
            if (manga.getChapters().size() < diff) {
                simpleList.addAll(manga.getChapters().subList(0, diff));
                simpleList.addAll(manga.getChapters().subList(manga.getChapters().size() - diff, manga.getChapters().size()));
                ArrayList<Chapter> simpleListC = new ArrayList<>();
                simpleListC.addAll(mangaDb.getChapters().subList(0, diff));
                simpleListC.addAll(mangaDb.getChapters().subList(mangaDb.getChapters().size() - diff, mangaDb.getChapters().size()));
                for (Chapter c : simpleListC) {
                    for (Chapter csl : simpleList) {
                        if (c.getPath().equalsIgnoreCase(csl.getPath())) {
                            simpleList.remove(csl);
                            break;
                        }
                    }
                }
            }
            if (simpleList.size() == 1) {
                Chapter c = simpleList.get(0);
                for (Chapter cap : manga.getChapters()) {
                    if (cap.getPath().equalsIgnoreCase(c.getPath())) {
                        simpleList.remove(0);
                        break;
                    }
                }
            }

            if (!(simpleList.size() >= diff)) {
                simpleList = new ArrayList<>();
                for (Chapter c : manga.getChapters()) {
                    boolean masUno = true;
                    for (Chapter csl : mangaDb.getChapters()) {
                        if (c.getPath().equalsIgnoreCase(csl.getPath())) {
                            mangaDb.getChapters().remove(csl);
                            masUno = false;
                            break;
                        }
                    }
                    if (masUno) {
                        simpleList.add(c);
                    }
                }
                // simpleList = manga.getChapters();
            }
            for (Chapter c : simpleList) {
                c.setMangaID(mangaDb.getId());
                c.setReadStatus(Chapter.NEW);
                Database.addChapter(context, c, mangaDb.getId());
            }

            if (simpleList.size() > 0) {
                Database.updateMangaRead(context, mangaDb.getId());
                Database.updateNewMangas(context, mangaDb, diff);
            }

            returnValue = simpleList.size();
        }

        boolean cambios = false;
        if (!mangaDb.getAuthor().equals(manga.getAuthor()) && manga.getAuthor().length() > 2) {
            mangaDb.setAuthor(manga.getAuthor());
            cambios = true;
        }

        if (!mangaDb.getImages().equals(manga.getImages()) && manga.getImages().length() > 2) {
            mangaDb.setImages(manga.getImages());
            cambios = true;
        }

        if (!mangaDb.getSynopsis().equals(manga.getSynopsis()) && manga.getSynopsis().length() > 2) {
            mangaDb.setSinopsis(manga.getSynopsis());
            cambios = true;
        }

        if (cambios)
            Database.updateMangaNotime(context, mangaDb);

        return returnValue;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getBandera() {
        return bandera;
    }

    public void setBandera(int bandera) {
        this.bandera = bandera;
    }

    public int getServerID() {
        return serverID;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getFirstMacth(String patron, String source, String errorMsj) throws Exception {
        Pattern p = Pattern.compile(patron);
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group(1);
        }
        throw new Exception(errorMsj);
    }

    public String getFirstMacthDefault(String patron, String source, String mDefault) throws Exception {
        Pattern p = Pattern.compile(patron);
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group(1);
        } else {
            return mDefault;
        }
    }

    public boolean tieneNavegacionVisual() {
        return true;
    }

    Navegador getNavegadorConHeader() {
        Navegador nav = new Navegador();
        nav.addHeader("User-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
        return nav;
    }

}
