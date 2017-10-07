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
import ar.rulosoft.mimanganu.utils.HtmlUnescape;
import ar.rulosoft.mimanganu.utils.Util;

class MangaTown extends ServerBase {
    private static final String HOST = "https://www.mangatown.com";

    private static final String PATTERN_COVER =
            "<img src=\"(.+?cover.+?)\"";
    private static final String PATTERN_SUMMARY =
            "<span id=\"show\" style=\"display: none;\">(.+?)&nbsp;<a";
    private static final String PATTERN_COMPLETED =
            "</b>Completed</li>";
    private static final String PATTERN_AUTHOR =
            "Author.+?\">(.+?)<";
    private static final String PATTERN_GENRE =
            "<li><b>Genre\\(s\\):</b>(.+?)</li>";
    private static final String PATTERN_CHAPTER =
            "<li>[^<]*<a href=\"([^\"]+?/manga/[^\"]+)\"[^>]*>([^<]+)</a>[^<]+<span";
    private static final String PATTERN_IMAGE =
            "src=\"([^\"]+?/manga/.+?.(jpg|gif|jpeg|png|bmp).*?)\"";
    private static final String PATTERN_PAGES =
            ">(\\d+)</option>[^<]+?</select>";
    private static final String PATTERN_MANGA =
            "<a class=\"manga_cover\" href=\"(.+?)\" title=\"(.+?)\">\\s*<img src=\"(.+?)\"";

    // filter by status /0-0-0-X-0-0/
    private static int[] fltStatus = {
            R.string.flt_status_all,
            R.string.flt_status_new,
            R.string.flt_status_ongoing,
            R.string.flt_status_completed
    };
    private static String[] valStatus = {
            "0", "new", "ongoing", "completed"
    };

    // filter by demographic /X-0-0-0-0-0/
    private static int[] fltDemographic = {
            R.string.flt_demographic_all,
            R.string.flt_demographic_josei,
            R.string.flt_demographic_seinen,
            R.string.flt_demographic_shoujo,
            R.string.flt_demographic_shoujo_ai,
            R.string.flt_demographic_shounen,
            R.string.flt_demographic_shounen_ai,
            R.string.flt_demographic_yaoi,
            R.string.flt_demographic_yuri
    };
    private static String[] valDemographic = {
            "0", "josei", "seinen", "shoujo", "shoujo_ai", "shounen", "shounen_ai", "yaoi", "yuri"
    };

    // filter by genre /0-X-0-0-0-0/
    private static int[] fltGenre = {
            R.string.flt_genre_all,
            R.string.flt_genre_4_koma,
            R.string.flt_genre_action,
            R.string.flt_genre_adventure,
            R.string.flt_genre_comedy,
            R.string.flt_genre_cooking,
            R.string.flt_genre_doujinshi,
            R.string.flt_genre_drama,
            R.string.flt_genre_ecchi,
            R.string.flt_genre_fantasy,
            R.string.flt_genre_gender_bender,
            R.string.flt_genre_harem,
            R.string.flt_genre_historical,
            R.string.flt_genre_horror,
            R.string.flt_genre_martial_arts,
            R.string.flt_genre_mature,
            R.string.flt_genre_mecha,
            R.string.flt_genre_music,
            R.string.flt_genre_mystery,
            R.string.flt_genre_one_shot,
            R.string.flt_genre_psychological,
            R.string.flt_genre_reverse_harem,
            R.string.flt_genre_romance,
            R.string.flt_genre_school_life,
            R.string.flt_genre_sci_fi,
            R.string.flt_genre_slice_of_life,
            R.string.flt_genre_sports,
            R.string.flt_genre_supernatural,
            R.string.flt_genre_suspense,
            R.string.flt_genre_tragedy,
            R.string.flt_genre_vampire,
            R.string.flt_genre_webtoons,
            R.string.flt_genre_youkai
    };
    private static String[] valGenre = {
            "0", "4_koma", "action", "adventure", "comedy", "cooking", "doujinshi", "drama",
            "ecchi", "fantasy", "gender_bender", "harem", "historical", "horror", "martial_arts",
            "mature", "mecha", "music", "mystery", "one_shot", "psychological", "reverse_harem",
            "romance", "school_life", "sci_fi", "slice_of_life", "sports", "supernatural",
            "suspense", "tragedy", "vampire", "webtoons", "youkai"
    };

    // filter by type /0-0-0-0-0-X/
    private static int[] fltType = {
            R.string.flt_type_all,
            R.string.flt_type_manga,
            R.string.flt_type_manhwa,
            R.string.flt_type_manhua
    };
    private static String[] valType = {
            "0", "manga", "manhwa", "manhua"
    };

    // filter by order /0-0-0-0-0-0/?
    private static int[] fltOrder = {
            R.string.flt_order_views,
            R.string.flt_order_rating,
            R.string.flt_order_alpha,
            R.string.flt_order_last_update
    };
    private static String[] valOrder = {
            "?views.za", "?rating.za", "?name.az", "last_chapter_time.za"
    };

    MangaTown(Context context) {
        super(context);
        setFlag(R.drawable.flag_en);
        setIcon(R.drawable.mangatown);
        setServerName("MangaTown");
        setServerID(MANGATOWN);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        loadChapters(manga, forceReload);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String data = getNavigatorAndFlushParameters().get(manga.getPath());

            // cover image
            manga.setImages(getFirstMatchDefault(PATTERN_COVER, data, ""));
            // summary
            manga.setSynopsis(getFirstMatchDefault(PATTERN_SUMMARY, data,
                    context.getString(R.string.nodisponible)));
            // ongoing or completed
            manga.setFinished(data.contains(PATTERN_COMPLETED));
            // author
            manga.setAuthor(getFirstMatchDefault(PATTERN_AUTHOR, data,
                    context.getString(R.string.nodisponible)));
            // genre
            manga.setGenre(Util.getInstance().fromHtml(
                    getFirstMatchDefault(PATTERN_GENRE, data, context.getString(R.string.nodisponible))
                    ).toString().trim());
            // chapter
            Pattern p = Pattern.compile(PATTERN_CHAPTER, Pattern.DOTALL);
            Matcher m = p.matcher(data);
            while (m.find()) {
                Chapter mc = new Chapter(Util.getInstance().fromHtml(m.group(2)).toString().trim(), "http:" + m.group(1));
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
            return chapter.getPath() + page + ".html";
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String data;
        data = getNavigatorAndFlushParameters().get(getPagesNumber(chapter, page));
        return getFirstMatch(PATTERN_IMAGE, data, "Error: failed to locate page image link");
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data, pages;
        data = getNavigatorAndFlushParameters().get(chapter.getPath());
        pages = getFirstMatch(PATTERN_PAGES, data, "Error: failed to get the number of pages");
        chapter.setPages(Integer.parseInt(pages));
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigatorAndFlushParameters().get(
                HOST + "/search.php?name=" + URLEncoder.encode(term, "UTF-8"));
        Pattern p = Pattern.compile(PATTERN_MANGA, Pattern.DOTALL);
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), HtmlUnescape.Unescape(m.group(2).trim()), "https:" + m.group(1), false));
        }
        return mangas;
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        throw new UnsupportedOperationException("Error: getMangas() not implemented for MangaTown");
    }

    @Override
    public boolean hasFilteredNavigation() {
        return true;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[] {
                new ServerFilter(
                        context.getString(R.string.flt_status),
                        buildTranslatedStringArray(fltStatus), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_demographic),
                        buildTranslatedStringArray(fltDemographic), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_genre),
                        buildTranslatedStringArray(fltGenre), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_type),
                        buildTranslatedStringArray(fltType), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_order),
                        buildTranslatedStringArray(fltOrder), ServerFilter.FilterType.SINGLE)
        };
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String filter = String.format(
                "%s-%s-%s-%s-%s-%s",
                valDemographic[filters[1][0]],
                valGenre[filters[2][0]],
                "0", // year
                valStatus[filters[0][0]],
                "0", // a-z
                valType[filters[3][0]]
                );
        String order = valOrder[filters[4][0]];

        String data = getNavigatorAndFlushParameters().get(
                HOST + "/directory/" + filter + "/" + pageNumber + ".htm" + order);
        Pattern p = Pattern.compile(PATTERN_MANGA, Pattern.DOTALL);
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(
                    getServerID(), HtmlUnescape.Unescape(m.group(2)), "https:" + m.group(1), false);
            manga.setImages(m.group(3));
            mangas.add(manga);
        }
        hasMore = mangas.size() > 0;
        return mangas;
    }
}
