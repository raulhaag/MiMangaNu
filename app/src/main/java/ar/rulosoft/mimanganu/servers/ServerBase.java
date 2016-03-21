package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ServerBase {

    public enum FilteredType {VISUAL, TEXT}

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
    public static final int MANGAREADER = 15;
    public static final int DENINEMANGA = 16;
    public static final int RUNINEMANGA = 17;
    public static final int MANGATUBE = 18;
    public static final int MANGAEDENIT = 19;
    public static final int MYMANGAIO = 20;
    public static final int RAWSENMANGA = 21;

    public boolean hayMas = true;
    private String serverName;
    private int icon;
    private int flag;
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
            case MANGAREADER:
                s = new MangaReader();
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
            case STARKANACOM:
                s = new StarkanaCom();
                break;
            case DENINEMANGA:
                s = new DeNineMangaCom();
                break;
            case RUNINEMANGA:
                s = new RuNineMangaCom();
                break;
            case MANGATUBE:
                s = new Manga_Tube();
                break;
            case MANGAEDENIT:
                s = new MangaEdenIt();
                break;
            case MYMANGAIO:
                s = new MyMangaIo();
                break;
            case RAWSENMANGA:
                s = new RawSenManga();
                break;
            default:
                break;
        }
        return s;
    }

    // server
    public abstract ArrayList<Manga> getMangas() throws Exception;

    public abstract ArrayList<Manga> search(String term) throws Exception;

    // chapter
    public abstract void loadChapters(Manga m, boolean forceReload) throws Exception;

    public abstract void loadMangaInformation(Manga m, boolean forceReload) throws Exception;

    // manga
    public abstract String getPagesNumber(Chapter c, int page);

    public abstract String getImageFrom(Chapter c, int page) throws Exception;

    public abstract void chapterInit(Chapter c) throws Exception;

    // server visual
    public abstract ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception;

    public abstract String[] getCategories();

    public abstract String[] getOrders();

    public abstract boolean hasList();

    // public abstract boolean supportStatus();

    public int searchForNewChapters(int id, Context context) throws Exception {
        int returnValue = 0;
        Manga mangaDb = Database.getFullManga(context, id);
        Manga manga =
                new Manga(mangaDb.getServerId(), mangaDb.getTitle(), mangaDb.getPath(), false);
        manga.setId(mangaDb.getId());
        this.loadMangaInformation(manga, true);
        this.loadChapters(manga, false);
        int diff = manga.getChapters().size() - mangaDb.getChapters().size();
        if (diff > 0) {
            ArrayList<Chapter> simpleList = new ArrayList<>();
            if (manga.getChapters().size() < diff) {
                simpleList.addAll(manga.getChapters().subList(0, diff));
                simpleList.addAll(manga.getChapters().subList(
                        manga.getChapters().size() -
                                diff, manga.getChapters().size()));
                ArrayList<Chapter> simpleListC = new ArrayList<>();
                simpleListC.addAll(mangaDb.getChapters().subList(0, diff));
                simpleListC.addAll(mangaDb.getChapters().subList(
                        mangaDb.getChapters().size() -
                                diff, mangaDb.getChapters().size()));
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

        boolean changes = false;
        if (!mangaDb.getAuthor().equals(manga.getAuthor()) &&
                manga.getAuthor().length() > 2) {
            mangaDb.setAuthor(manga.getAuthor());
            changes = true;
        }

        if (!mangaDb.getImages().equals(manga.getImages()) &&
                manga.getImages().length() > 2) {
            mangaDb.setImages(manga.getImages());
            changes = true;
        }

        if (!mangaDb.getSynopsis().equals(manga.getSynopsis()) &&
                manga.getSynopsis().length() > 2) {
            mangaDb.setSynopsis(manga.getSynopsis());
            changes = true;
        }

        if (!mangaDb.getGenre().equals(manga.getGenre()) &&
                manga.getGenre().length() > 2) {
            mangaDb.setGenre(manga.getGenre());
            changes = true;
        }
        if (mangaDb.isFinished() != manga.isFinished()) {
            mangaDb.setFinished(manga.isFinished());
            changes = true;
        }

        if (changes) Database.updateManga(context, mangaDb, false);

        return returnValue;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
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

    public static String getFirstMatch(String patron, String source, String errorMsj) throws Exception {
        Pattern p = Pattern.compile(patron);
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group(1);
        }
        throw new Exception(errorMsj);
    }

    public ArrayList<String> getAllMatch(String patron, String source) throws Exception {
        Pattern p = Pattern.compile(patron);
        Matcher m = p.matcher(source);
        ArrayList<String> matches = new ArrayList<>();
        if (m.find()) {
            matches.add(m.group(1));
        }
        return matches;
    }

    public String getFirstMatchDefault(String patron, String source, String mDefault) throws Exception {
        Pattern p = Pattern.compile(patron);
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group(1);
        } else {
            return mDefault;
        }
    }

    public boolean needRefererForImages() {
        return true;
    }

    public boolean hasFilteredNavigation() {
        return true;
    }

    Navegador getNavWithHeader() {
        return new Navegador();
    }

    public FilteredType getFilteredType() {
        return FilteredType.VISUAL;
    }

    public static ServerBase[] getServers() {
        return (new ServerBase[]{
                new HeavenMangaCom(),
                new SubManga(),
                new EsNineMangaCom(),
                new EsMangaHere(),
                new MangaPanda(),
                new MangaHere(),
                new MangaFox(),
                new MangaReader(),
                new KissManga(),
                new RuNineMangaCom(),
                new LectureEnLigne(),
                new MyMangaIo(),
                new ItNineMangaCom(),
                new MangaEdenIt(),
                new DeNineMangaCom(),
                new Manga_Tube(),
                new RawSenManga()
        });
    }

}
