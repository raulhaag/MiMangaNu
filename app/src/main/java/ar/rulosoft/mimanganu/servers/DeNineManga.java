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

class DeNineManga extends ServerBase {
    private static String HOST = "http://de.ninemanga.com";
    private static String[] genre = new String[]{
            "Abenteuer", "Action", "Alltagsdrama", "DäMonen", "Drama",
            "Ecchi", "Erotik", "Fantasy", "Gender Bender", "Harem",
            "Historisch", "Horror", "Josei", "Kampfsport", "Kartenspiel",
            "KomöDie", "Magie", "Mecha", "MilitäR", "Musik",
            "Mystery", "Romanze", "Schule", "Sci-Fi", "Shoujo",
            "Shounen", "Spiel", "Sport", "Super KräFte", "Thriller",
            "Vampire", "Videospiel", "Yaoi"
    };
    private static String[] genreV = new String[]{
            "63", "64", "82", "76", "65",
            "79", "88", "66", "91", "73",
            "84", "72", "95", "81", "78",
            "67", "68", "89", "90", "83",
            "69", "74", "70", "86", "85",
            "75", "92", "87", "80", "94",
            "71", "77", "93"
    };
    private static String[] orderV = new String[]{"/list/Hot-Book/", "/list/New-Update/", "/category/", "/list/New-Book/"};
    private static String[] order = new String[]{"Beliebte Manga", "Updates", "Manga Liste", "Neue Manga"};
    private static String[] complete = new String[]{"Entweder", "Ja", "Nein"};
    private static String[] completeV = new String[]{"either", "yes", "no"};

    DeNineManga(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_de);
        this.setIcon(R.drawable.ninemanga);
        this.setServerName("DeNineManga");
        setServerID(ServerBase.DENINEMANGA);
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
            Manga manga = new Manga(getServerID(), m.group(2), HOST + m.group(1), false);
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
        // Front
        manga.setImages(getFirstMatchDefault("Manga\" src=\"(.+?)\"", source, ""));
        // Summary
        String summary = getFirstMatchDefault("<p itemprop=\"description\">(.+?)</p>",
                source, defaultSynopsis).replaceAll("<.+?>", "");
        manga.setSynopsis(Util.getInstance().fromHtml(summary.replaceFirst("Zusammenfassung:", "")).toString());
        // Status
        manga.setFinished(!getFirstMatchDefault("<b>Status:</b>(.+?)</a>", source, "").contains("Laufende"));
        // Author
        manga.setAuthor(getFirstMatchDefault("Autor.+?\">(.+?)<", source, ""));
        // Genre
        manga.setGenre((Util.getInstance().fromHtml(getFirstMatchDefault("<li itemprop=\"genre\".+?</b>(.+?)</li>", source, "").replace("a><a", "a>, <a") + ".").toString().trim()));
        // Chapter
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
        String[] images = chapter.getExtra().split("\\|");
        return images[page];
    }

    private void setExtra(Chapter chapter) throws Exception {
        Navigator nav = getNavigatorWithNeededHeader();
        nav.addHeader("Referer", chapter.getPath());
        nav.get(HOST + "/show_ads/google/");
        String source = nav.get(chapter.getPath().replace(".html", "-" + chapter.getPages() + "-1.html"));
        Pattern p = Pattern.compile("src=\"(http://www\\.wiemanga\\.com/comics/pic/[^\"]+?)\""); //<img class="manga_pic.+?src="([^"]+)
        Matcher m = p.matcher(source);
        String images = "";
        while (m.find()) {
            images = images + "|" + m.group(1);
        }
        chapter.setExtra(images);
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String source = getNavigatorWithNeededHeader().get(chapter.getPath());
        String nop = getFirstMatchDefault(
                "\\d+/(\\d+)</option>[\\s]*</select>", source,
                "Es versäumt, die Anzahl der Seiten zu bekommen");
        chapter.setPages(Integer.parseInt(nop));
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile(
                "<a href=\"(/manga/[^\"]+)\"><img src=\"(.+?)\".+?alt=\"([^\"]+)\"");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(3), HOST + m.group(1), false);
            manga.setImages(m.group(2));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{new ServerFilter("Included Genre(s)", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Excluded Genre(s)", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Completed Series", complete, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Order", order, ServerFilter.FilterType.SINGLE)
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
            web = "http://de.ninemanga.com/search/?name_sel=contain&wd=&author_sel=contain&author=&artist_sel=contain&artist=&category_id=" + includedGenres + "&out_category_id=" + excludedGenres + "&completed_series=" + completeV[filters[2][0]] + "&type=high&page=" + pageNumber + ".html";
        //Log.d("NM","web: "+web);
        String source = getNavigatorWithNeededHeader().get(web);
        // regex to generate genre ids: <li id="cate_.+?" cate_id="(.+?)" cur="none" class="cate_list"><label><a class="sub_clk cirmark">(.+?)</a></label></li>
        Pattern pattern = Pattern.compile("<dl class=\"bookinfo\">.+?href=\"(.+?)\"><img src=\"(.+?)\".+?\">(.+?)<");
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            Manga m = new Manga(getServerID(), Util.getInstance().fromHtml(matcher.group(3)).toString(), HOST + matcher.group(1), false);
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
