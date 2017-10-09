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

class NineManga extends ServerBase {
    protected String HOST = "http://ninemanga.com";

    private static final String PATTERN_MANGA =
            "bookname\" href=\"(/manga/[^\"]+)\">(.+?)<";
    private static final String PATTERN_MANGA_SEARCHED =
            "<dl class=\"bookinfo\">.+?href=\"(.+?)\"><img src=\"(.+?)\".+?\">(.+?)<";
    private static final String PATTERN_COVER =
            "Manga\" src=\"(.+?)\"";
    private static final String PATTERN_SUMMARY =
            "<p itemprop=\"description\">(.+?)</p>";
    private static final String PATTERN_COMPLETED =
            "<a class=\"red\" href=\"/category/completed.html\">";
    private static final String PATTERN_AUTHOR =
            "<a itemprop=\"author\"[^>]+>(.+?)</a>";
    private static final String PATTERN_GENRE =
            "<li itemprop=\"genre\".+?</b>(.+?)</li>";
    private static final String PATTERN_CHAPTER =
            "<a class=\"chapter_list_a\" href=\"(/chapter.+?)\" title=\"(.+?)\">(.+?)</a>";
    private static final String PATTERN_PAGES =
            "\\d+/(\\d+)</option>[\\s]*</select>";
    @SuppressWarnings("WeakerAccess")
    protected String PATTERN_IMAGE =
            "src=\"(http[s]?://pic\\.taadd\\.com/comics/[^\"]+?|http[s]?://pic\\d+\\.taadd\\.com/comics/[^\"]+?)\"";
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

    NineManga(Context context) {
        super(context);
        setFlag(R.drawable.flag_en);
        setIcon(R.drawable.ninemanga);
        setServerName("NineManga");
        setServerID(ServerBase.NINEMANGA);
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        throw new UnsupportedOperationException("Error: getMangas() not implemented");
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigatorAndFlushParameters().get(
                HOST + "/search/?wd=" + URLEncoder.encode(term, "UTF-8"));
        Pattern p = Pattern.compile(PATTERN_MANGA, Pattern.DOTALL);
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(getServerID(), Util.getInstance().fromHtml(m.group(2)).toString(), HOST + m.group(1), false);
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
            String data = getNavigatorAndFlushParameters().get(manga.getPath() + "?waring=1");

            // cover image
            manga.setImages(getFirstMatchDefault(PATTERN_COVER, data, ""));
            // summary
            manga.setSynopsis(Util.getInstance().fromHtml(getFirstMatchDefault(PATTERN_SUMMARY, data,
                    context.getString(R.string.nodisponible))).toString().replaceFirst("^.+?: ", "").replace("\\", ""));
            // ongoing or completed
            manga.setFinished(data.contains(PATTERN_COMPLETED));
            // author
            manga.setAuthor(getFirstMatchDefault(PATTERN_AUTHOR, data,
                    context.getString(R.string.nodisponible)));
            // genre
            manga.setGenre(Util.getInstance().fromHtml(
                    getFirstMatchDefault(PATTERN_GENRE, data, context.getString(R.string.nodisponible))
            ).toString().trim().replace(" ", ", "));
            // chapter
            Pattern p = Pattern.compile(PATTERN_CHAPTER, Pattern.DOTALL);
            Matcher m = p.matcher(data);
            while (m.find()) {
                Chapter mc = new Chapter(Util.getInstance().fromHtml(m.group(3)).toString().trim(), HOST + m.group(1));
                mc.addChapterFirst(manga);
            }
        }
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        if (page < 1) {
            page = 1;
        }
        if (page > chapter.getPages()) {
            page = chapter.getPages();
        }

        if (page == 1) {
            return chapter.getPath();
        }
        else {
            return chapter.getPath().replace(".html", "-" + page + ".html");
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        if (chapter.getExtra() == null) {
            setExtra(chapter);
        }
        String[] images = chapter.getExtra().split("\\|");
        return images[page];
    }

    private void setExtra(Chapter chapter) throws Exception {
        String source = getNavigatorAndFlushParameters().get(chapter.getPath().replace(".html", "-" + chapter.getPages() + "-1.html"));
        Pattern p = Pattern.compile(PATTERN_IMAGE, Pattern.DOTALL);
        Matcher m = p.matcher(source);
        String images = "";
        while (m.find()) {
            images = images + "|" + m.group(1);
        }
        chapter.setExtra(images);
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data, pages;
        data = getNavigatorAndFlushParameters().get(chapter.getPath());
        pages = getFirstMatch(PATTERN_PAGES, data, "Error: failed to get the number of pages");
        chapter.setPages(Integer.parseInt(pages));
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
        if(filters[0].length < 1 && filters[1].length < 1)
            web = HOST + valCategory[filters[3][0]];
        else
            web = HOST + "/search/?name_sel=contain&wd=&author_sel=contain&author=&artist_sel=contain&artist=&category_id=" + includedGenres + "&out_category_id=" + excludedGenres + "&completed_series=" + valStatus[filters[2][0]] + "&type=high&page=" + pageNumber + ".html";

        String data = getNavigatorAndFlushParameters().get(web);
        Pattern pattern = Pattern.compile(PATTERN_MANGA_SEARCHED, Pattern.DOTALL);
        Matcher m = pattern.matcher(data);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()) {
            Manga manga = new Manga(getServerID(), Util.getInstance().fromHtml(m.group(3)).toString(), HOST + m.group(1), false);
            manga.setImages(m.group(2));
            mangas.add(manga);
        }
        hasMore = mangas.size() > 0;
        return mangas;
    }
}
