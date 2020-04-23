package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.net.URLEncoder;
import java.util.ArrayList;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;

/**
 * Created by Raul on 05/04/2016.
 */
public class TuMangaOnline extends ServerBase {

    private static final String HOST = "https://tmofans.com";
    public static String script = null;
    public JSServerHelper scriptHelper;

    public static String[] type = new String[]{
            "Todos", "Manga", "Manhua", "Manhwa", "Novela", "One Shot", "Dounjinshi", "Oel"
    };
    private static String[] genres = new String[]{
            "Acción", "Aventura", "Comedia", "Drama", "Recuentos de la vida", "Ecchi", "Fantasia",
            "Magia", "Supernatural", "Horror", "Misterio", "Psicológico", "Romance",
            "Ciencia Ficción", "Thriller", "Deporte", "Girls Love", "Boys Love", "Harem", "Mecha",
            "Supervivencia", "Reencarnación", "Gore", "Apocalíptico", "Tragedia", "Vida Escolar",
            "Historia", "Militar", "Policiaco", "Crimen", "Superpoderes", "Vampiros",
            "Artes Marciales", "Samurái", "Género Bender", "Realidad Virtual", "Ciberpunk",
            "Musica", "Parodia", "Animación", "Demonios", "Familia", "Extranjero", "Niños",
            "Realidad", "Telenovela", "Guerra", "Oeste"
    };
    private static String[] demografia = {
            "Todos", "Seinen", "Shoujo", "Shounen", "Josei", "Kodomo"
    };
    private static String[] estado = {
            "Todos", "Activo", "Abandonado", "Finalizado", "Pausado"
    };
    private static String[] sortBy = new String[]{
            "Me gusta", "Alfabetico", "Puntuación", "Creación", "Fecha de Esterno",
    };
    private static String[] sortOrder = new String[]{
            "Descendiente", "Ascendiente"
    };

    public final int VERSION = 2;

    TuMangaOnline(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_es);
        this.setIcon(R.drawable.tumangaonline_icon);
        this.setServerName("TuMangaOnline");
        setServerID(ServerBase.TUMANGAONLINE);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        checkScript();
        return scriptHelper.search(term);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (forceReload)
            loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            checkScript();
            scriptHelper.loadMangaInformation(manga);
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String[] d1 = chapter.getExtra().split("\\|");
        return d1[page] + "|" + d1[0];
    }

    @Override
    public synchronized void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getPages() == 0) {
            checkScript();
            Manga m = Database.getManga(context, chapter.getMangaID());
            String images = "";
            images = scriptHelper.pi.chapterInit(HOST + "/goto/" + chapter.getPath(),
                    String.format(HOST + "/library/manga/%s/%s", m.getPath(),
                            URLEncoder.encode(m.getTitle(), "UTF-8")
                                    .replaceAll("\\.", "")));
            if (images.trim().isEmpty()) {
                throw new Exception("error in tmo js plugin");
            }
            chapter.setPages(images.split("\\|").length - 1);
            chapter.setExtra(images);
        }
    }

    private void checkScript() throws Exception {

        if (scriptHelper == null) {
            String d = " " + context.getString(R.string.factor_suffix).hashCode() + getServerID() + TUMANGAONLINE;
            try {
                script = Util.xorDecode(getNavWithNeededHeaders().get("https://raw.githubusercontent.com/raulhaag/MiMangaNu/master/js_plugin/" + getServerID() + "_5.js"), d);
            } catch (Exception e) {
                script = getNavWithNeededHeaders().get("https://github.com/raulhaag/MiMangaNu/blob/master/js_plugin/22_5.js");
                script = Util.xorDecode(Util.getInstance().fromHtml(getFirstMatch("(<table class=\"highlight tab-size js-file-line-container\"[\\s\\S]+<\\/table>)", script, "error obteniendo script")).toString(), d);
            }
            if (!script.isEmpty())
                scriptHelper = new JSServerHelper(context, script);
        }

    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public boolean needRefererForImages() {
        return true;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter("Tipo", type, ServerFilter.FilterType.SINGLE),//0
                new ServerFilter("Demografia", demografia, ServerFilter.FilterType.SINGLE),//1
                new ServerFilter("Generos", genres, ServerFilter.FilterType.MULTI_STATES),//2
                new ServerFilter("Estado", estado, ServerFilter.FilterType.SINGLE),//3
                new ServerFilter("Ordenado por", sortBy, ServerFilter.FilterType.SINGLE), //4
                new ServerFilter("En dirección", sortOrder, ServerFilter.FilterType.SINGLE) //5
        };
    }

    // ?order_item=     0
    // &order_dir=      1
    // &title=&filter_by=title
    // &type=           2
    // &demography=     3
    // &status=         4
    // &webcomic=&yonkoma=&amateur=
    // &genders%5B%5D=1&genders%5B%5D=3&genders%5B%5D=6

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        checkScript();
        return scriptHelper.getMangasFiltered(filters, pageNumber);
    }

    public Navigator getNavWithNeededHeaders() {
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addHeader("Cache-mode", "no-cache");
        nav.addHeader("Referer", HOST + "/library/manga/");
        return nav;
    }

    public boolean updateServerVersion() {
        /*
        int k = 0;
        ArrayList<Manga> mangas = Database.getMangasCondition(context,
                Database.COL_SERVER_ID + " = " + getServerID(), Database.COL_SERVER_ID, true);
        for (Manga m : mangas) {
            Util.getInstance().toast(context, k + "/" + mangas.size() + context.getString(R.string.processed));
            k++;
            Manga mangaDb = Database.getFullManga(context, m.getId());
            Manga manga = new Manga(mangaDb.getServerId(), mangaDb.getTitle(), mangaDb.getPath(), false);
            manga.setId(mangaDb.getId());
            try {
                this.loadMangaInformation(manga, true);
                this.loadChapters(manga, false);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            for (int j = mangaDb.getChapters().size() - 1; j >= 0; j--) {
                Chapter cdb = mangaDb.getChapter(j);
                for (int i = 0; i < manga.getChapters().size(); i++) {
                    Chapter c = manga.getChapter(i);
                    try {
                        if (c.getTitle().contains(" " + cdb.getPath().split("_")[2])) {
                            manga.getChapters().remove(i);
                            cdb.setPath(c.getPath());
                            cdb.setExtra("");
                            Database.updateChapter(context, cdb);
                            break;
                        }
                    } catch (Exception e) {
                        Log.i("Error mapping", cdb.getPath());
                    }
                }
            }
        }*/
        return true;
    }

    @Override
    public int getServerVersion() {
        return VERSION;
    }

    @Override
    public synchronized int searchForNewChapters(int id, Context context, boolean fast) {
        int count = super.searchForNewChapters(id, context, fast);
        try {
            Thread.sleep(3000); /// try to avoid too many request forcing single thread on this server and a wait;
        } catch (InterruptedException e) {
        }
        return count;
    }
}
