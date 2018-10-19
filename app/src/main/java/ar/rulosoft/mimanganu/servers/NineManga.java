package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.navegadores.Navigator;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

class NineManga extends ServerBase {
    private static final String PATTERN_MANGA =
            "bookname\" href=\".*?(/manga/[^\"]+)\">(.+?)<";
    private static final String PATTERN_MANGA_SEARCHED =
            "<dl class=\"bookinfo\">.+?href=\".*?(/manga/.+?)\"><img src=\"(.+?)\".+?\">(.+?)<";
    private static final String PATTERN_SUMMARY =
            "<p itemprop=\"description\">(.+?)</p>";
    private static final String PATTERN_COMPLETED =
            "<a class=\"red\" href=\"/category/completed.html\">";
    private static final String PATTERN_AUTHOR =
            "<a itemprop=\"author\"[^>]+>([^<]+)</a>";
    private static final String PATTERN_GENRE =
            "<li itemprop=\"genre\".+?</b>(.+?)</a>[^<]*</li>";
    private static final String PATTERN_CHAPTER =
            "<a class=\"chapter_list_a\" href=\".*?(/chapter[^<\"]+)\" title=\"([^\"]+)\">([^<]+)</a>";
    private static final String PATTERN_PAGES =
            "\\d+/(\\d+)</option>[\\s]*</select>";
    private static final int[] fltCategory = {
            R.string.flt_category_all,
            R.string.flt_category_hot,
            R.string.flt_category_new,
            R.string.flt_category_latest_release
    };
    private static final String[] valCategory = {
            "/category/", "/list/Hot-Book/", "/list/New-Book/", "/list/New-Update/"
    };
    private static final int[] fltStatus = {
            R.string.flt_status_all,
            R.string.flt_status_completed,
            R.string.flt_status_ongoing
    };
    private static final String[] valStatus = {
            "either", "yes", "no"};
    private static boolean cookieInit = false;
    protected String HOST = "http://ninemanga.com";
    @SuppressWarnings("WeakerAccess")
    protected String PATTERN_COVER =
            "Manga\" src=\"(.+?)\"";
    @SuppressWarnings("WeakerAccess")
    protected String PATTERN_IMAGE =
            "(http[^\"]*?/comic[^\"]*?)\"";
    @SuppressWarnings("WeakerAccess")
    protected int[] fltGenre = {
            R.string.flt_tag_4_koma,
            R.string.flt_tag_action,
            R.string.flt_tag_adult,
            R.string.flt_tag_adventure,
            R.string.flt_tag_anime,
            R.string.flt_tag_award_winning,
            R.string.flt_tag_bara,
            R.string.flt_tag_comedy,
            R.string.flt_tag_cooking,
            R.string.flt_tag_daemons,
            R.string.flt_tag_doujinshi,
            R.string.flt_tag_drama,
            R.string.flt_tag_ecchi,
            R.string.flt_tag_fantasy,
            R.string.flt_tag_gender_bender,
            R.string.flt_tag_harem,
            R.string.flt_tag_historical,
            R.string.flt_tag_horror,
            R.string.flt_tag_josei,
            R.string.flt_tag_live_action,
            R.string.flt_tag_magic,
            R.string.flt_tag_manhua,
            R.string.flt_tag_manhwa,
            R.string.flt_tag_martial_arts,
            R.string.flt_tag_matsumoto,
            R.string.flt_tag_mature,
            R.string.flt_tag_mecha,
            R.string.flt_tag_medical,
            R.string.flt_tag_military,
            R.string.flt_tag_music,
            R.string.flt_tag_mystery,
            R.string.flt_tag_none, // 54,64
            R.string.flt_tag_one_shot, // 22,57
            R.string.flt_tag_psychological,
            R.string.flt_tag_reverse_harem,
            R.string.flt_tag_romance, // 24,38
            R.string.flt_tag_school_life,
            R.string.flt_tag_sci_fi,
            R.string.flt_tag_seinen,
            R.string.flt_tag_shoujo,
            R.string.flt_tag_shoujo_ai, // 44,29,48
            R.string.flt_tag_shounen,
            R.string.flt_tag_shounen_ai, //42,31,46
            R.string.flt_tag_slice_of_life,
            R.string.flt_tag_smut,
            R.string.flt_tag_sports,
            R.string.flt_tag_staff_pick,
            R.string.flt_tag_super_powers,
            R.string.flt_tag_supernatural,
            R.string.flt_tag_suspense,
            R.string.flt_tag_tragedy,
            R.string.flt_tag_vampire,
            R.string.flt_tag_webtoon, // 58,50
            R.string.flt_tag_yaoi,
            R.string.flt_tag_yuri,
            R.string.flt_tag_no_chapters
    };
    @SuppressWarnings("WeakerAccess")
    protected String[] valGenre = {
            "56", "1", "39", "2", "3", "59", "84", "4", "5", "49", "45", "6", "7", "8", "9", "10",
            "11", "12", "13", "14", "47", "15", "16", "17", "37", "36", "18", "19", "51", "20",
            "21", "54%2C64", "22%2C57", "23", "55", "24%2C38", "25", "26", "27", "28",
            "44%2C29%2C48", "30", "42%2C31%2C46", "32", "41", "33", "60", "62", "34", "53", "35",
            "52", "58%2C50", "40", "43", "61"
    };

    NineManga(Context context) {
        super(context);
        setFlag(R.drawable.flag_en);
        setIcon(R.drawable.ninemanga);
        setServerName("NineManga");
        setServerID(ServerBase.NINEMANGA);
    }

    /**
     * Helper function to generate the Cookie needed by NineManga.
     */
    private static void generateNeededCookie() {
        long aTime = System.currentTimeMillis() / 1000 - (int) (Math.random() * 100);
        HttpUrl url = HttpUrl.parse("https://www.ninemanga.com/");
        assert url != null; // pasring the valid constant URL will never fail
        Navigator.getCookieJar().saveFromResponse(url, Arrays.asList(
                Cookie.parse(url, "ninemanga_country_code=AR; Domain=ninemanga.com"),
                Cookie.parse(url, "__utma=128769555.619121721." + aTime + "." + aTime + "." + aTime
                        + ".1; Domain=ninemanga.com"),
                Cookie.parse(url, "__utmc=128769555; Domain=ninemanga.com"),
                Cookie.parse(url, "__utmz=128769555." + aTime
                        + ".1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); Domain=ninemanga.com")
        ));
        cookieInit = true;
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigatorWithNeededHeader().get(
                HOST + "/search/?wd=" + URLEncoder.encode(term, "UTF-8"));
        Pattern p = Pattern.compile(PATTERN_MANGA, Pattern.DOTALL);
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(2), m.group(1), false);
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String data = getNavigatorWithNeededHeader().get(HOST + manga.getPath());
            if (data.contains("?waring=1")) {
                // es.ninemanga delivers invalid chapter links if ?waring=1 is already passed on
                // non-warned Manga - so check if there is a link and then retry with the extension
                data = getNavigatorWithNeededHeader().get(HOST + manga.getPath().replaceAll(" ", "+") + "?waring=1");
            }

            // cover image
            manga.setImages(getFirstMatchDefault(PATTERN_COVER, data, ""));
            // summary
            manga.setSynopsis(getFirstMatchDefault(PATTERN_SUMMARY, data, context.getString(R.string.nodisponible)));
            assert manga.getSynopsis() != null;
            manga.setSynopsis(manga.getSynopsis().replaceFirst("^.+?: ", "").replace("\\", ""));
            // ongoing or completed
            manga.setFinished(data.contains(PATTERN_COMPLETED));
            // author
            manga.setAuthor(getFirstMatchDefault(PATTERN_AUTHOR, data, context.getString(R.string.nodisponible)));
            // genre
            manga.setGenre(
                    getFirstMatchDefault(PATTERN_GENRE, data, context.getString(R.string.nodisponible)).replace("</a>", "</a>,"));
            // chapter
            Pattern p = Pattern.compile(PATTERN_CHAPTER, Pattern.DOTALL);
            Matcher m = p.matcher(data);
            while (m.find()) {
                manga.addChapterFirst(new Chapter(m.group(3), m.group(1)));
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        Navigator nav = getNavigatorWithNeededHeader();
        if (page == 1)
            nav.addHeader("Referer", HOST + chapter.getPath());
        else
            nav.addHeader("Referer", HOST + chapter.getPath().replace(".html", "-" + (page - 1) + ".html"));
        String data = nav.get(HOST + chapter.getPath().replace(".html", "-"
                + page + ".html"));
        data = getFirstMatch(PATTERN_IMAGE, data, context.getString(R.string.server_failed_loading_image));
        if (data.contains("////")) {
            throw new Exception(context.getString(R.string.server_failed_loading_image));
        }
        return data;
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getPages() == 0) {
            String data = getNavigatorWithNeededHeader().get(HOST + chapter.getPath());
            String pages = getFirstMatch(
                    PATTERN_PAGES, data,
                    context.getString(R.string.server_failed_loading_page_count));
            chapter.setPages(Integer.parseInt(pages));
        }
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(
                        context.getString(R.string.flt_include_tags),
                        buildTranslatedStringArray(fltGenre), ServerFilter.FilterType.MULTI),
                new ServerFilter(
                        context.getString(R.string.flt_exclude_tags),
                        buildTranslatedStringArray(fltGenre), ServerFilter.FilterType.MULTI),
                new ServerFilter(
                        context.getString(R.string.flt_status),
                        buildTranslatedStringArray(fltStatus), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_category),
                        buildTranslatedStringArray(fltCategory), ServerFilter.FilterType.SINGLE)
        };
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String includedGenres = "";
        if (filters[0].length > 0) {
            for (int i = 0; i < filters[0].length; i++) {
                includedGenres = includedGenres + valGenre[filters[0][i]] + "%2C"; // comma
            }
        }
        String excludedGenres = "";
        if (filters[1].length > 0) {
            for (int i = 0; i < filters[1].length; i++) {
                excludedGenres = excludedGenres + valGenre[filters[1][i]] + "%2C"; // comma
            }
        }
        String web;
        web = HOST + "/search/?name_sel=contain&wd=&author_sel=contain&author=&artist_sel=contain&artist=&category_id=" + includedGenres + "&out_category_id=" + excludedGenres + "&completed_series=" + valStatus[filters[2][0]] + "&page=" + pageNumber + ".html";
        String data = getNavigatorWithNeededHeader().get(web);
        Pattern pattern = Pattern.compile(PATTERN_MANGA_SEARCHED, Pattern.DOTALL);
        Matcher m = pattern.matcher(data);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(3), m.group(1), m.group(2)));
        }
        return mangas;
    }

    /**
     * Helper function to set up the <code>Navigator</code> with additional headers.
     * Some servers need additional information to be added to the request header in order to work.
     * This function provides such an object.
     *
     * @return a <code>Navigator</code> object with extended headers
     */
    private Navigator getNavigatorWithNeededHeader() {
        if (!cookieInit) {
            generateNeededCookie();
        }
        Navigator nav = Navigator.getInstance();
        nav.addHeader("Accept-Language", "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3");
        nav.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        return nav;
    }
}
