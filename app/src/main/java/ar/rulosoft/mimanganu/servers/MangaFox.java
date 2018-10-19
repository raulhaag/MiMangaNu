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

class MangaFox extends ServerBase {
    private static final String HOST = "http://mangafox.me/";

    private static final String PATTERN_SERIES =
            "<li><a href=\"([^\"]+)\" rel=\"\\d+\" class=\"series_preview manga_(close|open)\">([^<]+)</a></li>";
    private static final String PATTERN_SEGMENT =
            "<div class=\"manga_list\">(.+?)<div class=\"clear gap\">";
    private static final String PATTERN_COVER =
            "<div class=\"cover\">.+?src=\"([^\"]+)\"";
    private static final String PATTERN_SUMMARY =
            "<p class=\"summary\">(.+?)</p>";
    private static final String PATTERN_CHAPTERS =
            "<h\\d>[\\s]+<a href=\"([^\"]+)\".+?>([^<]+)([^\"]+<span class=\"title nowrap\">([^<]+)<)?";
    private static final String PATTERN_LAST =
            "(\\d+)</option>\\s*<option value=\"0\""; // last page is for comments
    private static final String PATTERN_MANGA =
            "\"([^\"]+store.manga.+?)\".+?href=\"([^\"]+)[^>]+>([^<]+)";
    private static final String PATTERN_MANGA_SEARCH =
            "<a class=\"title series_preview top\" href=\"([^\"]+)\"[^>]+>([^<]+)";

    private static final int[] fltGenre = {
            R.string.flt_tag_action,
            R.string.flt_tag_adult,
            R.string.flt_tag_adventure,
            R.string.flt_tag_comedy,
            R.string.flt_tag_doujinshi,
            R.string.flt_tag_drama,
            R.string.flt_tag_ecchi,
            R.string.flt_tag_fantasy,
            R.string.flt_tag_gender_bender,
            R.string.flt_tag_harem,
            R.string.flt_tag_historical,
            R.string.flt_tag_horror,
            R.string.flt_tag_josei,
            R.string.flt_tag_martial_arts,
            R.string.flt_tag_mecha,
            R.string.flt_tag_mystery,
            R.string.flt_tag_one_shot,
            R.string.flt_tag_psychological,
            R.string.flt_tag_romance,
            R.string.flt_tag_school_life,
            R.string.flt_tag_sci_fi,
            R.string.flt_tag_seinen,
            R.string.flt_tag_shoujo,
            R.string.flt_tag_shoujo_ai,
            R.string.flt_tag_shounen,
            R.string.flt_tag_shounen_ai,
            R.string.flt_tag_slice_of_life,
            R.string.flt_tag_smut,
            R.string.flt_tag_sports,
            R.string.flt_tag_supernatural,
            R.string.flt_tag_tragedy,
            R.string.flt_tag_webtoon,
            R.string.flt_tag_yaoi,
            R.string.flt_tag_yuri
    };
    private static final String[] valGenre = {
            "&genres[Action]=",
            "&genres[Adult]=",
            "&genres[Adventure]=",
            "&genres[Comedy]=",
            "&genres[Doujinshi]=",
            "&genres[Drama]=",
            "&genres[Ecchi]=",
            "&genres[Fantasy]=",
            "&genres[Gender+Bender]=",
            "&genres[Harem]=",
            "&genres[Historical]=",
            "&genres[Horror]=",
            "&genres[Josei]=",
            "&genres[Martial+Arts]=",
            "&genres[Mecha]=",
            "&genres[Mystery]=",
            "&genres[One+Shot]=",
            "&genres[Psychological]=",
            "&genres[Romance]=",
            "&genres[School+Life]=",
            "&genres[Sci-fi]=",
            "&genres[Seinen]=",
            "&genres[Shoujo]=",
            "&genres[Shoujo+Ai]=",
            "&genres[Shounen]=",
            "&genres[Shounen+Ai]=",
            "&genres[Slice+of+Life]=",
            "&genres[Smut]=",
            "&genres[Sports]=",
            "&genres[Supernatural]=",
            "&genres[Tragedy]=",
            "&genres[Webtoons]=",
            "&genres[Yaoi]=",
            "&genres[Yuri]=",
    };

    private static final int[] fltType = {
            R.string.flt_tag_all,
            R.string.flt_tag_manga,
            R.string.flt_tag_manhwa,
            R.string.flt_tag_manhua
    };
    private static final String[] valType = {
            "&type=",
            "&type=1",
            "&type=2",
            "&type=3"
    };

    private static final int[] fltStatus = {
            R.string.flt_status_all,
            R.string.flt_status_ongoing,
            R.string.flt_status_completed
    };
    private static final String[] valStatus = {
            "&is_completed=",
            "&is_completed=1",
            "&is_completed=0"
    };

    private static final int[] fltOrder = {
            R.string.flt_order_views,
            R.string.flt_order_rating,
            R.string.flt_order_numchapters,
            R.string.flt_order_last_update,
            R.string.flt_order_alpha
    };
    private static final String[] valOrder = {
            "&sort=views&order=za",
            "&sort=rating&order=za",
            "&sort=total_chapters&order=az",
            "&sort=last_chapter_time&order=za",
            "&sort=name"
    };

    MangaFox(Context context) {
        super(context);
        setFlag(R.drawable.flag_en);
        setIcon(R.drawable.mangafox_icon);
        setServerName("MangaFox");
        setServerID(MANGAFOX);
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigatorAndFlushParameters().get(HOST + "/manga/");
        data = getFirstMatch(PATTERN_SEGMENT, data, "Error: failed to get segment");
        Pattern p = Pattern.compile(PATTERN_SERIES, Pattern.DOTALL);
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(ServerBase.MANGAFOX, m.group(3), "http:" + m.group(1), false);
            if (m.group(2).length() > 4) {
                manga.setFinished(true);
            }
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        loadChapters(manga, forceReload);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String data = getNavigatorAndFlushParameters().get((manga.getPath()));

            // Cover
            manga.setImages(getFirstMatchDefault(PATTERN_COVER, data, ""));

            // Summary
            manga.setSynopsis(getFirstMatchDefault(PATTERN_SUMMARY, data, context.getString(R.string.nodisponible)));

            // Status
            manga.setFinished(data.contains("<h\\d>Status:</h\\d>\\s*<span>\\s*Completed</span>"));

            // Author
            manga.setAuthor(getFirstMatchDefault("\"/search/author/.+?>(.+?)<", data, context.getString(R.string.nodisponible)));

            // Genre
            manga.setGenre(getFirstMatchDefault("(<a href=\"//(mangafox|fanfox).[^/]+/search/genres/.+?</td>)", data, context.getString(R.string.nodisponible)));

            // Chapter
            Pattern p = Pattern.compile(PATTERN_CHAPTERS, Pattern.DOTALL);
            Matcher m = p.matcher(data);
            while (m.find()) {
                if (m.group(4) != null) {
                    manga.addChapterFirst(new Chapter(m.group(2).trim() + ": " + m.group(4), "http:" + m.group(1).replace("1.html", "")));
                }
                else {
                    manga.addChapterFirst(new Chapter(m.group(2).trim(), "http:" + m.group(1).replace("1.html", "")));
                }
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String source = getNavigatorAndFlushParameters().get(
                chapter.getPath() + page + ".html");
        return getFirstMatch(
                ">[\\s]*<img src=\"(.+?)\"", source,
                context.getString(R.string.server_failed_loading_image));
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if(chapter.getPages() == 0) {
            if (chapter.getPath().endsWith("html") && chapter.getPath().indexOf("/") > 0) {
                chapter.setPath(chapter.getPath().substring(0, chapter.getPath().lastIndexOf("/") + 1));
            }
            String source = getNavigatorAndFlushParameters().get(chapter.getPath());
            String pages = getFirstMatch(
                    PATTERN_LAST, source,
                    context.getString(R.string.server_failed_loading_page_count));
            chapter.setPages(Integer.parseInt(pages));
        }
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigatorAndFlushParameters()
                .get("http://mangafox.me/search.php?name_method=cw&name="
                        + URLEncoder.encode(term.replaceAll(" ", "+"), "UTF-8")
                        + "&type=&author_method=cw&author=&artist_method=cw&artist=&genres%5BAction%5D=0&genres%5BAdult%5D=0&genres%5BAdventure%5D=0&genres%5BComedy%5D=0&genres%5BDoujinshi%5D=0&genres%5BDrama%5D=0&genres%5BEcchi%5D=0&genres%5BFantasy%5D=0&genres%5BGender+Bender%5D=0&genres%5BHarem%5D=0&genres%5BHistorical%5D=0&genres%5BHorror%5D=0&genres%5BJosei%5D=0&genres%5BMartial+Arts%5D=0&genres%5BMature%5D=0&genres%5BMecha%5D=0&genres%5BMystery%5D=0&genres%5BOne+Shot%5D=0&genres%5BPsychological%5D=0&genres%5BRomance%5D=0&genres%5BSchool+Life%5D=0&genres%5BSci-fi%5D=0&genres%5BSeinen%5D=0&genres%5BShoujo%5D=0&genres%5BShoujo+Ai%5D=0&genres%5BShounen%5D=0&genres%5BShounen+Ai%5D=0&genres%5BSlice+of+Life%5D=0&genres%5BSmut%5D=0&genres%5BSports%5D=0&genres%5BSupernatural%5D=0&genres%5BTragedy%5D=0&genres%5BWebtoons%5D=0&genres%5BYaoi%5D=0&genres%5BYuri%5D=0&released_method=eq&released=&rating_method=eq&rating=&is_completed=&advopts=1");
        Pattern p = Pattern.compile(PATTERN_MANGA_SEARCH, Pattern.DOTALL);
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(2), "http:" + m.group(1), false));
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        StringBuilder web = new StringBuilder();
        web.append(HOST + "/search.php?name_method=cw&name=");
        web.append(valType[filters[0][0]]);
        web.append("&author_method=cw&author=&artist_method=cw&artist=");
        for(int i = 0; i < fltGenre.length; i++) {
            // no preference
            String selection = "0";
            // include tag
            for(int j = 0; j < filters[1].length; j++) {
                if(filters[1][j] == i) {
                    selection = "1";
                    break;
                }
            }
            // exclude tag (has precedence - for simplicity)
            for(int j = 0; j < filters[2].length; j++) {
                if(filters[2][j] == i) {
                    selection = "2";
                    break;
                }
            }

            web.append(valGenre[i]).append(selection);
        }
        web.append("&released_method=eq&released=");
        web.append("&rating_method=eq&rating=");
        web.append(valStatus[filters[3][0]]);
        web.append("&advopts=1");
        web.append(valOrder[filters[4][0]]);
        web.append("&page=").append(pageNumber);

        String source = getNavigatorAndFlushParameters().get(web.toString());
        Pattern p = Pattern.compile(PATTERN_MANGA, Pattern.DOTALL);
        Matcher m = p.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(3), "http:" + m.group(2), m.group(1)));
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
            new ServerFilter(
                    context.getString(R.string.flt_type),
                    buildTranslatedStringArray(fltType), ServerFilter.FilterType.SINGLE),
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
                    context.getString(R.string.flt_order),
                    buildTranslatedStringArray(fltOrder), ServerFilter.FilterType.SINGLE)
        };
    }
}
