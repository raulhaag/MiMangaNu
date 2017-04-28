package ar.rulosoft.mimanganu.servers;

import android.content.Context;

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

class ItNineManga extends ServerBase {
    private static String HOST = "http://it.ninemanga.com";
    private static String[] genre = new String[]{
            "Action", "Adult", "Adventure", "Avventura", "Azione",
            "Bara", "Comedy", "Commedia", "Demenziale", "Doujinshi",
            "Dounshinji", "Drama", "Ecchi", "Fantasy", "Gender Bender",
            "Harem", "Hentai", "Historical", "Horror", "Josei",
            "Magico", "Martial Arts", "Mature", "Mecha", "Misteri",
            "Musica", "Mystery", "Psicologico", "Psychological", "Raccolta",
            "Romance", "Romantico", "School Life", "Sci-Fi", "Scolastico",
            "Seinen", "Sentimentale", "Shota", "Shoujo", "Shounen",
            "Slice Of Life", "Smut", "Sovrannaturale", "Splatter", "Sportivo",
            "Sports", "Storico", "Supernatural", "Tragedy", "Vita Quotidiana",
            "Yaoi", "Yuri"
    };
    private static String[] genreV = new String[]{
            "98", "113", "108", "63", "65",
            "88", "101", "71", "79", "114",
            "92", "82", "70", "74", "109",
            "76", "90", "107", "80", "95",
            "91", "99", "106", "68", "87",
            "96", "105", "83", "97", "93",
            "104", "75", "103", "66", "64",
            "67", "72", "89", "73", "69",
            "102", "111", "78", "81", "85",
            "110", "84", "100", "112", "77",
            "86", "94"
    };
    private static String[] orderV = {"/list/Hot-Book/", "/list/New-Update/", "/category/", "/list/New-Book/"};
    private static String[] order = new String[]{"Popolare Manga", "Ultime uscite", "Lista Manga", "Nuovo Manga"};
    private static String[] complete = new String[]{"O", "Si", "No"};
    private static String[] completeV = new String[]{"either", "yes", "no"};

    ItNineManga(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_it);
        this.setIcon(R.drawable.ninemanga);
        this.setServerName("ItNineManga");
        setServerID(ServerBase.ITNINEMANGA);
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
            Manga manga = new Manga(ITNINEMANGA, m.group(2), HOST + m.group(1), false);
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
        String sinopsis = getFirstMatchDefault("<p itemprop=\"description\">(.+?)</p>",
                source, defaultSynopsis).replaceAll("<.+?>", "");
        manga.setSynopsis(Util.getInstance().fromHtml(sinopsis.replaceFirst("Sommario:", "")).toString());
        // estado
        manga.setFinished(getFirstMatchDefault("Stato:(.+?)</a>", source, "").contains("Completato"));
        // autor
        manga.setAuthor(getFirstMatchDefault("Author.+?\">(.+?)<", source, ""));
        // genere
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
        Navigator nav = getNavigatorWithNeededHeader();
        nav.addHeader("Referer", chapter.getPath());
        nav.get(HOST + "/show_ads/google/");
        String source = nav.get(chapter.getPath().replace(".html", "-" + chapter.getPages() + "-1.html"));
        Pattern p = Pattern.compile("src=\"(http://img\\.it\\.ninemanga\\.com/it_manga/[^\"]+?)\"");
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
                "Error al obtener el numero de paginas");
        chapter.setPages(Integer.parseInt(nop));
    }

    @Deprecated
    private ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("<a href=\"(/manga/[^\"]+)\"><img src=\"(.+?)\".+?alt=\"([^\"]+)\"");
        Matcher matcher = p.matcher(source);
        while (matcher.find()) {
            Manga manga = new Manga(ITNINEMANGA, matcher.group(3), HOST + matcher.group(1), false);
            manga.setImages(matcher.group(2));
            mangas.add(manga);
        }
        return mangas;
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
        if (filters[0].length < 1 && filters[1].length < 1)
            web = HOST + orderV[filters[3][0]];
        else
            web = "http://it.ninemanga.com/search/?name_sel=contain&wd=&author_sel=contain&author=&artist_sel=contain&artist=&category_id=" + includedGenres + "&out_category_id=" + excludedGenres + "&completed_series=" + completeV[filters[2][0]] + "&type=high&page=" + pageNumber + ".html";
        //Log.d("NM","web: "+web);
        String source = getNavigatorWithNeededHeader().get(web);
        // regex to generate genre ids: <li id="cate_.+?" cate_id="(.+?)" cur="none" class="cate_list"><label><a class="sub_clk cirmark">(.+?)</a></label></li>
        Pattern pattern = Pattern.compile("<dl class=\"bookinfo\">.+?href=\"(.+?)\"><img src=\"(.+?)\".+?\">(.+?)<");
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            /*Log.d("NM","(2): "+matcher.group(2));
            Log.d("NM","(1): "+matcher.group(1));*/
            Manga m = new Manga(getServerID(), Util.getInstance().fromHtml(matcher.group(3)).toString(), HOST + matcher.group(1), false);
            m.setImages(matcher.group(2));
            mangas.add(m);
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter("Included Genre(s)", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Excluded Genre(s)", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Completed Series", complete, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Order", order, ServerFilter.FilterType.SINGLE)
        };
    }

    @Override
    public boolean hasList() {
        return false;
    }

    public Navigator getNavigatorWithNeededHeader() throws Exception {
        Navigator nav = new Navigator(context);
        nav.addHeader("Accept-Language", "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3");
        return nav;
    }
}
