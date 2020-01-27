package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
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
            "<li>\\s*<a href=\"(/manga/[^\"]+)[^>]+>(.+?)<span class=\"time\">";
    private static final String PATTERN_IMAGE =
            "src=\"([^\"]+?/manga/.+?.(jpg|gif|jpeg|png|bmp).*?)\"";
    private static final String PATTERN_MANGA =
            "<a class=\"manga_cover\" href=\"([^\"]+)\" title=\"([^\"]+)\">\\s*<img\\s+src=\"(.+?)\"";

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
            R.string.flt_tag_all,
            R.string.flt_tag_josei,
            R.string.flt_tag_seinen,
            R.string.flt_tag_shoujo,
            R.string.flt_tag_shoujo_ai,
            R.string.flt_tag_shounen,
            R.string.flt_tag_shounen_ai,
            R.string.flt_tag_yaoi,
            R.string.flt_tag_yuri
    };
    private static String[] valDemographic = {
            "0", "josei", "seinen", "shoujo", "shoujo_ai", "shounen", "shounen_ai", "yaoi", "yuri"
    };

    // filter by genre /0-X-0-0-0-0/
    private static int[] fltGenre = {
            R.string.flt_tag_all,
            R.string.flt_tag_4_koma,
            R.string.flt_tag_action,
            R.string.flt_tag_adventure,
            R.string.flt_tag_comedy,
            R.string.flt_tag_cooking,
            R.string.flt_tag_doujinshi,
            R.string.flt_tag_drama,
            R.string.flt_tag_ecchi,
            R.string.flt_tag_fantasy,
            R.string.flt_tag_gender_bender,
            R.string.flt_tag_harem,
            R.string.flt_tag_historical,
            R.string.flt_tag_horror,
            R.string.flt_tag_martial_arts,
            R.string.flt_tag_mature,
            R.string.flt_tag_mecha,
            R.string.flt_tag_music,
            R.string.flt_tag_mystery,
            R.string.flt_tag_one_shot,
            R.string.flt_tag_psychological,
            R.string.flt_tag_reverse_harem,
            R.string.flt_tag_romance,
            R.string.flt_tag_school_life,
            R.string.flt_tag_sci_fi,
            R.string.flt_tag_slice_of_life,
            R.string.flt_tag_sports,
            R.string.flt_tag_supernatural,
            R.string.flt_tag_suspense,
            R.string.flt_tag_tragedy,
            R.string.flt_tag_vampire,
            R.string.flt_tag_webtoon,
            R.string.flt_tag_youkai
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
            R.string.flt_tag_all,
            R.string.flt_tag_manga,
            R.string.flt_tag_manhwa,
            R.string.flt_tag_manhua
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
            manga.setGenre(getFirstMatchDefault(PATTERN_GENRE, data, context.getString(R.string.nodisponible)));
            String message = getFirstMatchDefault("<div style=\"text-align: center;\">(The series .+? available in MangaTown)", data, "");
            if (!"".equals(message)) {
                Util.getInstance().toast(context, message, Toast.LENGTH_LONG);
                return;
            }
            // chapter
            Pattern p = Pattern.compile(PATTERN_CHAPTER, Pattern.DOTALL);
            Matcher m = p.matcher(data);
            while (m.find()) {
                manga.addChapterFirst(new Chapter(m.group(2).replace("<span class=\"new\">new</span>", ""), HOST + m.group(1)));
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        assert chapter.getExtra() != null;
        String web = HOST + chapter.getExtra().split("\\|")[page - 1];
        String data = getNavigatorAndFlushParameters().get(web);
        return "https:" + getFirstMatch(
                PATTERN_IMAGE, data,
                context.getString(R.string.server_failed_loading_image));
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getPages() == 0) {
            String data = getNavigatorAndFlushParameters().get(chapter.getPath());
            String page_selection = getFirstMatch(
                    "<select onchange=\"javascript:location.href=this.value;\">(.+?)</select>", data,
                    context.getString(R.string.server_failed_loading_page_count)
            );

            // only match numeric page indices, this automatically skips the 'featured' ad page
            ArrayList<String> page_links = getAllMatch("<option value=\"([^\"]+)\"[^>]*>\\d+</option>", page_selection);
            if (page_links.isEmpty()) {
                throw new Exception(context.getString(R.string.server_failed_loading_page_count));
            }

            chapter.setExtra(TextUtils.join("|", page_links));
            chapter.setPages(page_links.size());
        }
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigatorAndFlushParameters().get(
                HOST + "/search.php?name=" + URLEncoder.encode(term, "UTF-8"));
        Pattern p = Pattern.compile(PATTERN_MANGA, Pattern.DOTALL);
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(2), HOST + m.group(1), false));
        }
        return mangas;
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
    public boolean hasFilteredNavigation() {
        return true;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
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
            mangas.add(new Manga(getServerID(), m.group(2), HOST + m.group(1), m.group(3)));
        }
        return mangas;
    }
}
