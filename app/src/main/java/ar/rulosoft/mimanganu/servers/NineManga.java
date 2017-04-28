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

/**
 * Created by xtj-9182 on 11.04.2017.
 */
class NineManga extends ServerBase {
    private static String HOST = "http://ninemanga.com";

    private static String[] genre = new String[]{
            "4-Koma", "Action", "Adult", "Adventure", "Anime", "Award Winning",
            "Bara", "Comedy", "Cooking", "Demons", "Doujinshi", "Drama", "Ecchi", "Fantasy", "Gender Bender",
            "Harem", "Historical", "Horror", "Josei", "Live Action", "Magic", "Manhua", "Manhwa",
            "Martial Arts", "Matsumoto...", "Mature", "Mecha", "Medical", "Military", "Music",
            "Mystery", "N/A", "None", "One Shot", "Oneshot", "Psychological", "Reverse Harem",
            "Romance", "Romance Shoujo", "School Life", "Sci-Fi", "Seinen", "Shoujo", "Shoujo Ai",
            "Shoujo-Ai", "Shoujoai", "Shounen", "Shounen Ai", "Shounen-Ai", "Shounenai", "Slice Of Life",
            "Smut", "Sports", "Staff Pick", "Super Power", "Supernatural", "Suspense", "Tragedy",
            "Vampire", "Webtoon", "Webtoons", "Yaoi", "Yuri", "[No Chapters]"
    };
    private static String[] genreV = new String[]{
            "56", "1", "39", "2", "3", "59",
            "84", "4", "5", "49", "45", "6", "7", "8", "9",
            "10", "11", "12", "13", "14", "47", "15", "16",
            "17", "37", "36", "18", "19", "51", "20",
            "21", "54", "64", "22", "57", "23", "55",
            "24", "38", "25", "26", "27", "28", "44",
            "29", "48", "30", "42", "31", "46", "32",
            "41", "33", "60", "62", "34", "53", "35",
            "52", "58", "50", "40", "43", "61"
    };

    private static String[] orderV = {"/list/Hot-Book/", "/list/New-Update/", "/category/", "/list/New-Book/"};
    private static String[] order = {"Popular Manga", "Latest Releases", "Manga Directory", "New Manga"};
    private static String[] complete = new String[]{"Either", "Yes", "No"};
    private static String[] completeV = new String[]{"either", "yes", "no"};

    NineManga(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.ninemanga);
        this.setServerName("NineManga");
        setServerID(ServerBase.NINEMANGA);
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
        Pattern pattern = Pattern.compile("bookname\" href=\"(/manga/[^\"]+)\">(.+?)<");
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            String title = Util.getInstance().fromHtml(matcher.group(2)).toString();
            //Log.d("NM","t0: "+title);
            if (title.equals(title.toUpperCase())) {
                title = Util.getInstance().toCamelCase(title.toLowerCase());
                //Log.d("NM","t1: "+title);
            }
            Manga manga = new Manga(getServerID(), title, HOST + matcher.group(1), false);
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
        manga.setSynopsis(Util.getInstance().fromHtml(summary.replaceFirst("Summary:", "")).toString());
        // Status
        manga.setFinished(!getFirstMatchDefault("<b>Status:</b>(.+?)</a>", source, "").contains("Ongoing"));
        // Author
        manga.setAuthor(getFirstMatchDefault("author.+?\">(.+?)<", source, ""));
        // Genre
        manga.setGenre((Util.getInstance().fromHtml(getFirstMatchDefault("<li itemprop=\"genre\".+?</b>(.+?)</li>", source, "").replace("a><a", "a>, <a") + ".").toString().trim()));
        // Chapters
        Pattern p = Pattern.compile("<a class=\"chapter_list_a\" href=\"(/chapter.+?)\" title=\"(.+?)\">(.+?)</a>");
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
        Pattern p = Pattern.compile("src=\"(http[s]?://pic\\.taadd\\.com/comics/[^\"]+?|http[s]?://pic\\d+\\.taadd\\.com/comics/[^\"]+?)\""); //<img class="manga_pic.+?src="([^"]+)
        Matcher matcher = p.matcher(source);
        String images = "";
        while (matcher.find()) {
            images = images + "|" + matcher.group(1);
        }
        chapter.setExtra(images);
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String source = getNavigatorWithNeededHeader().get(chapter.getPath());
        String nop = getFirstMatchDefault(
                "\\d+/(\\d+)</option>[\\s]*</select>", source,
                "failed to get the number of pages");
        chapter.setPages(Integer.parseInt(nop));
    }

    @Deprecated
    private ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("<a href=\"(/manga/[^\"]+)\"><img src=\"(.+?)\".+?alt=\"([^\"]+)\"");
        Matcher matcher = p.matcher(source);
        while (matcher.find()) {
            Manga manga = new Manga(getServerID(), Util.getInstance().fromHtml(matcher.group(3)).toString(), HOST + matcher.group(1), false);
            manga.setImages(matcher.group(2));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter("Included Genre(s)", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Excluded Genre(s)", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Completed Series", complete, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Order (only applied when no genre is selected)", order, ServerFilter.FilterType.SINGLE)
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
        if (filters[0].length < 1 && filters[1].length < 1)
            web = HOST + orderV[filters[3][0]];
        else
            web = "http://ninemanga.com/search/?name_sel=contain&wd=&author_sel=contain&author=&artist_sel=contain&artist=&category_id=" + includedGenres + "&out_category_id=" + excludedGenres + "&completed_series=" + completeV[filters[2][0]] + "&type=high&page=" + pageNumber + ".html";
        //Log.d("NM","web: "+web);
        String source = getNavigatorWithNeededHeader().get(web);
        // regex to generate genre ids: <li id="cate_.+?" cate_id="(.+?)" cur="none" class="cate_list"><label><a class="sub_clk cirmark">(.+?)</a></label></li>
        Pattern pattern = Pattern.compile("<dl class=\"bookinfo\">.+?href=\"(.+?)\"><img src=\"(.+?)\".+?\">(.+?)<");
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            String title = Util.getInstance().fromHtml(matcher.group(3)).toString();
            //Log.d("NM","t0: "+title);
            if (title.equals(title.toUpperCase())) {
                title = Util.getInstance().toCamelCase(title.toLowerCase());
                //Log.d("NM","t1: "+title);
            }
            Manga manga = new Manga(getServerID(), title, HOST + matcher.group(1), false);
            manga.setImages(matcher.group(2));
            mangas.add(manga);
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

