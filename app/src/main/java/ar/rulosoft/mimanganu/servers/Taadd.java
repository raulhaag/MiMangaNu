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
class Taadd extends ServerBase {
    private static String HOST = "http://www.taadd.com";

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

    Taadd(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.taadd);
        this.setServerName("Taadd");
        setServerID(ServerBase.TAADD);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        //http://my.taadd.com/search/es/?wd=naru
        String source = getNavigatorWithNeededHeader().get("http://my.taadd.com/search/es/?wd=" + URLEncoder.encode(term, "UTF-8"));
        return getMangasFromSource(source);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 || forceReload)
            loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {

        // replace old ninemanga links with new taadd links
        /*if (manga.getPath().contains("ninemanga")) {
            String path = manga.getPath().replace("http://ninemanga.com/manga/", "http://www.taadd.com/book/");
            path = path.replace("https://ninemanga.com/manga/", "https://www.taadd.com/book/");
            path = path.replaceAll("%20", "+");
            manga.setPath(path);
        }*/

        String source = getNavigatorWithNeededHeader().get(manga.getPath()+ "?waring=1");
        //Log.d("NM","m.p: "+manga.getPath()+ "?waring=1");

        // Cover
        if (manga.getImages() == null || manga.getImages().isEmpty()) {
            String img = getFirstMatchDefault("src=\"(http://pic\\.taadd\\.com/files/img/logo/[^\"]+)\"", source, "");
            //Log.d("TD", "img: " + img);
            manga.setImages(img);
        }

        // Summary
        String summary = getFirstMatchDefault("Summary(.+?)</td>",source, defaultSynopsis);
        //Log.d("TD","s: "+summary);
        manga.setSynopsis(Util.getInstance().fromHtml(summary.replaceAll("</b><br/>", "")).toString());

        // Status
        //Log.d("TD","finished: "+getFirstMatchDefault("<td>Status:(.+?)</a>", source, "").contains("Completed"));
        manga.setFinished(getFirstMatchDefault("<td>Status:(.+?)</a>", source, "").contains("Completed"));

        // Author
        manga.setAuthor(getFirstMatchDefault("author-(.+?).html\">", source, ""));

        // Genre
        //FIXME fix genres spacing
        String genre = Util.getInstance().fromHtml(getFirstMatchDefault("Categories:(.+?)</td>", source, "").replaceAll("</a>", ",</a>")).toString();
        //Log.d("TD", "g: " + genre.replaceAll("￼", ""));
        if (genre.endsWith(","))
            genre = genre.substring(2, genre.length() - 1);
        manga.setGenre(genre.replaceAll("￼", ""));

        // Chapters
        Pattern p = Pattern.compile("href=\"(/chapter/[^-\"]+?)\">(.+?)</a>");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            /*Log.d("TD", "1: " + matcher.group(1));
            Log.d("TD", "2: " + matcher.group(2));*/
            chapters.add(0, new Chapter(matcher.group(2), HOST + matcher.group(1)));
        }
        manga.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return null;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        Navigator nav = getNavigatorWithNeededHeader();
        nav.addHeader("Referer", chapter.getPath());
        String source = nav.get(chapter.getPath() + "-" + page + ".html");
        //Log.d("TD", "web: " + chapter.getPath() + "-" + page + ".html");
        return getFirstMatchDefault("src=\"(http[s]?://pic\\.taadd\\.com/comics/[^\"]+?|http[s]?://pic\\d+\\.taadd\\.com/comics/[^\"]+?)\"", source, "Error getting image");
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String source = getNavigatorWithNeededHeader().get(chapter.getPath());
        String pageNumber = getFirstMatchDefault("\">(\\d+)</option>[\\s]*</select>", source,
                "failed to get the number of pages");
        //Log.d("TD", "p: " + pagenumber);
        chapter.setPages(Integer.parseInt(pageNumber));
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

    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern pattern = Pattern.compile("<a href=\"([^\"]+?)\"><img src=\"([^\"]+?)\" alt=\"([^\"]+?)\"");
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            /*Log.d("TD", "1: " + matcher.group(1));
            Log.d("TD", "2: " + matcher.group(2));
            Log.d("TD", "3: " + matcher.group(3));*/
            String title = Util.getInstance().fromHtml(matcher.group(3)).toString();
            //Log.d("TD","t0: "+title);
            if (title.equals(title.toUpperCase())) {
                title = Util.getInstance().toCamelCase(title.toLowerCase());
                //Log.d("TD","t1: "+title);
            }
            Manga manga = new Manga(getServerID(), title, matcher.group(1), false);
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
            web = "http://taadd.com/search/?name_sel=contain&wd=&author_sel=contain&author=&artist_sel=contain&artist=&category_id=" + includedGenres + "&out_category_id=" + excludedGenres + "&completed_series=" + completeV[filters[2][0]] + "&type=high&page=" + pageNumber + ".html";
        //Log.d("TD","web: "+web);
        String source = getNavigatorWithNeededHeader().get(web);
        return getMangasFromSource(source);
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

