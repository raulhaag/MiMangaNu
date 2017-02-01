package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.util.Log;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;

class EsNineManga extends ServerBase {
    private static String HOST = "http://es.ninemanga.com";
    private static String[] genre = new String[]{
            "4-Koma", "AccióN", "Action", "Adult", "Adulto", "Adventure", "ApocalíPtico",
            "Artes Marciales", "Aventura", "Aventuras", "Ciencia FiccióN", "Comedia", "Comedy",
            "Cotidiano", "Cyberpunk", "Delincuentes", "Demonios", "Deporte", "Deportes", "Drama", "Ecchi",
            "Escolar", "Fantacia", "FantasíA", "Fantasy", "Gender Bender", "Gore", "Harem", "HaréN",
            "Hentai", "Historical", "HistóRico", "Horror", "Josei", "Karate", "Maduro", "Mafia", "Magia",
            "Makoto", "Mangasutra", "Manhwa", "Manwha", "Martial", "Martial Arts", "Mecha", "Militar",
            "Misterio", "MúSica", "Musical", "Mystery", "None", "One Shot", "Oneshot", "OrgíA", "Parodia",
            "Policial", "Porno", "PsicolóGico", "Psychological", "Realidad Virtual",
            "Recuentos De La Vida", "ReencarnacióN", "Romance", "RomáNtica", "RomáNtico", "Samurai",
            "School Life", "Sci-Fi", "Seinen", "Sexo", "Shojo", "Shojo Ai", "Shonen", "Shonen Ai", "Shonen-Ai",
            "Shoujo", "Shoujo Ai", "Shoujo-Ai", "Shounen", "Shounen Ai", "Shounen-Ai", "Slice Of Life", "Smut",
            "Sobrenatural", "Sports", "Super Natural", "Super Poderes", "Superheroes", "Supernatural",
            "Supervivencia", "Suspense", "Terror", "Terror PsicolóGico", "Thiller", "Thriller", "Tragedia",
            "Tragedy", "Transexual", "Vampiros", "Vida Cotidiana", "Vida Escolar", "Vida Escolar.",
            "Webcomic", "Webtoon", "Yura", "Yuri"
    };
    private static String[] genreV = new String[]{
            "201", "69", "177", "193", "86", "179", "202", "66", "64", "120", "93", "75", "178", "110", "199",
            "125", "126", "76", "111", "79", "65", "81", "100", "70", "180", "175", "108", "78", "82", "83", "190",
            "95", "99", "112", "113", "72", "90", "172", "102", "103", "94", "114", "189", "181", "115", "205",
            "88", "121", "197", "187", "71", "184", "195", "91", "198", "208", "109", "96", "192", "196", "169",
            "207", "67", "98", "89", "210", "176", "123", "73", "104", "80", "186", "77", "128", "174", "85", "194",
            "173", "68", "185", "118", "182", "183", "74", "188", "124", "206", "116", "119", "203", "171", "106",
            "107", "204", "97", "87", "191", "117", "209", "84", "170", "122", "92", "200", "101", "127"
    };
    private static String[] orderV = {"/list/Hot-Book/", "/list/New-Update/", "/category/", "/list/New-Book/"};
    private static String[] orders = new String[]{"Popular", "Recientes", "Lista de Manga", "Manga Nueva"};
    private static String[] complete = new String[]{"O", "Si", "No"};
    private static String[] completeV = new String[]{"either", "yes", "no"};

    EsNineManga(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_es);
        this.setIcon(R.drawable.ninemanga);
        this.setServerName("EsNineManga");
        setServerID(ServerBase.ESNINEMANGA);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String source = getNavigatorWithNeededHeader().get(
                HOST + "/search/?wd=" + URLEncoder.encode(term, "UTF-8"));
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("bookname\" href=\"(/manga/[^\"]+)\">(.+?)<");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(ESNINEMANGA, m.group(2), HOST + m.group(1), false);
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 || forceReload)
            loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        String source = getNavigatorWithNeededHeader().get(manga.getPath() + "?waring=1");
        // portada
        manga.setImages(getFirstMatchDefault("Manga\" src=\"(.+?)\"", source, ""));
        // sinopsis
        String sinopsis = getFirstMatchDefault("<p itemprop=\"description\">(.+?)&nbsp;Show less",
                source, defaultSynopsis).replaceAll("<.+?>", "");
        manga.setSynopsis(Util.getInstance().fromHtml(sinopsis).toString());
        // estado
        manga.setFinished(getFirstMatchDefault("Estado(.+?)</a>", source, "").contains("Completado"));
        // autor
        manga.setAuthor(getFirstMatchDefault("Autor.+?\">(.+?)<", source, ""));
        // genre
        manga.setGenre((Util.getInstance().fromHtml(getFirstMatchDefault("<li itemprop=\"genre\".+?</b>(.+?)</li>", source, "").replace("a><a", "a>, <a") + ".").toString().trim()));
        // capitulos
        Pattern p = Pattern.compile(
                "<a class=\"chapter_list_a\" href=\"(/chapter.+?)\" title=\"(.+?)\">(.+?)</a>");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(3), HOST + matcher.group(1)));
        }
        manga.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return chapter.getPath().replace(".html", "-" + page + ".html");
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        if (chapter.getExtra() == null)
            setExtra(chapter);
        String[] imagenes = chapter.getExtra().split("\\|");
        return imagenes[page];
    }

    private void setExtra(Chapter chapter) throws Exception {
        String source = getNavigatorWithNeededHeader().get(
                chapter.getPath().replace(".html", "-" + chapter.getPages() + "-1.html"));
        Pattern p = Pattern.compile("<img class=\"manga_pic.+?src=\"([^\"]+)");
        Matcher m = p.matcher(source);
        String imagenes = "";
        while (m.find()) {
            imagenes = imagenes + "|" + m.group(1);
        }
        chapter.setExtra(imagenes);
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String source = getNavigatorWithNeededHeader().get(chapter.getPath());
        String nop = getFirstMatch(
                "\\d+/(\\d+)</option>[\\s]*</select>", source,
                "Error al obtener el número de páginas");
        chapter.setPages(Integer.parseInt(nop));
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{new ServerFilter("Incluido Genero(s)", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Excluido Genero(s)", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Series Completado", complete, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Orden", orders, ServerFilter.FilterType.SINGLE)
        };
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String includedGenres = "";
        if (filters[0].length > 0) {
            for (int i = 0; i < filters[0].length; i++) {
                includedGenres = includedGenres + genreV[filters[0][i]] + "%2C"; // comma
            }
        }
        String excludedGenres = "";
        if (filters[1].length > 0) {
            for (int i = 0; i < filters[1].length; i++) {
                excludedGenres = excludedGenres + genreV[filters[1][i]] + "%2C"; // comma
            }
        }
        String web;
        if(filters[0].length < 1 && filters[1].length < 1)
            web = HOST + orderV[filters[3][0]];
        else
            web = "http://es.ninemanga.com/search/?name_sel=contain&wd=&author_sel=contain&author=&artist_sel=contain&artist=&category_id=" + includedGenres + "&out_category_id=" + excludedGenres + "&completed_series=" + completeV[filters[2][0]] + "&type=high&page=" + pageNumber + ".html";
        Log.d("NM","web: "+web);
        String source = getNavigatorWithNeededHeader().get(web);
        Pattern pattern = Pattern.compile("<dl class=\"bookinfo\">.+?href=\"(.+?)\"><img src=\"(.+?)\".+?\">(.+?)<");
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            Manga m = new Manga(getServerID(), matcher.group(3), HOST + matcher.group(1), false);
            m.setImages(matcher.group(2));
            mangas.add(m);
        }
        return mangas;
    }

    public Navigator getNavigatorWithNeededHeader() throws Exception {
        Navigator nav = new Navigator(context);
        nav.addHeader("Accept-Language", "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3");
        return nav;
    }

    @Override
    public boolean hasList() {
        return false;
    }
}
