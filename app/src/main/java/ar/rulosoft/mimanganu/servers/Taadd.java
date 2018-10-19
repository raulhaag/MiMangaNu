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
import ar.rulosoft.navegadores.Navigator;

/**
 * Created by xtj-9182 on 11.04.2017.
 */
class Taadd extends ServerBase {
    private static String HOST = "http://www.taadd.com";

    private static final int[] fltGenre = {
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
            R.string.flt_tag_none,
            R.string.flt_tag_one_shot,
            R.string.flt_tag_psychological,
            R.string.flt_tag_reverse_harem,
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
            R.string.flt_tag_staff_pick,
            R.string.flt_tag_super_powers,
            R.string.flt_tag_supernatural,
            R.string.flt_tag_suspense,
            R.string.flt_tag_tragedy,
            R.string.flt_tag_vampire,
            R.string.flt_tag_webtoon,
            R.string.flt_tag_yaoi,
            R.string.flt_tag_yuri,
            R.string.flt_tag_no_chapters

    };
    private static final String[] valGenre = {
            "56",
            "1",
            "39",
            "2",
            "3",
            "59",
            "84",
            "4",
            "5",
            "49",
            "45",
            "6",
            "7",
            "8",
            "9",
            "10",
            "11",
            "12",
            "13",
            "14",
            "47",
            "15",
            "16",
            "17",
            "37",
            "36",
            "18",
            "19",
            "51",
            "20",
            "21",
            "54%2C64",
            "22%2C57",
            "23",
            "55",
            "24%2C38",
            "25",
            "26",
            "27",
            "28",
            "44%2C29%2C48",
            "30",
            "42%2C31%2C46",
            "32",
            "41",
            "33",
            "60",
            "62",
            "34",
            "53",
            "35",
            "52",
            "58%2C50",
            "40",
            "43",
            "61"
    };

    private static final int[] fltOrder = {
            R.string.flt_order_views,
            R.string.flt_order_last_update,
            R.string.flt_order_alpha,
            R.string.flt_order_newest
    };
    private static final String[] valOrder = {
            "/list/Hot-Book/",
            "/list/New-Update/",
            "/category/",
            "/list/New-Book/"
    };

    private static final int[] fltComplete = {
            R.string.flt_status_all,
            R.string.flt_status_completed,
            R.string.flt_status_ongoing
    };
    private static final String[] valComplete = {
            "either",
            "yes",
            "no"
    };

    Taadd(Context context) {
        super(context);
        setFlag(R.drawable.flag_en);
        setIcon(R.drawable.taadd);
        setServerName("Taadd");
        setServerID(TAADD);
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
        term = URLEncoder.encode(term.replaceAll(" ", "+"), "UTF-8");
        String source = getNavigatorAndFlushParameters()
                .get("http://my.taadd.com/search/es/?wd=" + term);
        return getMangasFromSource(source);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String source = getNavigatorAndFlushParameters().get(manga.getPath() + "?waring=1");

            // Cover
            if (manga.getImages() == null || manga.getImages().isEmpty()) {
                String img = getFirstMatchDefault("src=\"(http://pic\\.taadd\\.com/files/img/logo/[^\"]+)\"", source, "");
                manga.setImages(img);
            }

            // Summary
            String summary = getFirstMatchDefault("Summary(.+?)</td>", source, context.getString(R.string.nodisponible));
            manga.setSynopsis(summary);

            // Status
            manga.setFinished(source.contains(">Completed</a>"));

            // Author
            manga.setAuthor(getFirstMatchDefault("author-(.+?).html\">", source, context.getString(R.string.nodisponible)));

            // Genre
            manga.setGenre(getFirstMatchDefault("Categories:(.+?)</a>[^<]*</td>", source, context.getString(R.string.nodisponible))
                    .replaceAll("<img[^>]+>", "").replaceAll("&nbsp;", "").replaceAll("</a>", ","));

            // Chapters
            Pattern p = Pattern.compile("href=\"(/chapter/[^-\"]+?)\">(.+?)</a>", Pattern.DOTALL);
            Matcher matcher = p.matcher(source);
            while (matcher.find()) {
                manga.addChapterFirst(new Chapter(matcher.group(2), HOST + matcher.group(1)));
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addHeader("Referer", chapter.getPath());
        String source = nav.get(chapter.getPath() + "-" + page + ".html");
        return getFirstMatch(
                "src=\"(http[s]?://.{2,4}\\.taadd\\.com/comics/[^\"]+?)\"", source,
                context.getString(R.string.server_failed_loading_image));
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if(chapter.getPages() == 0) {
            String source = getNavigatorAndFlushParameters().get(chapter.getPath());
            String pageNumber = getFirstMatch(
                    ">(\\d+)</option>\\s*</select>", source,
                    context.getString(R.string.server_failed_loading_page_count));
            chapter.setPages(Integer.parseInt(pageNumber));
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
                        buildTranslatedStringArray(fltComplete), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_order) + " (" + context.getString(R.string.flt_is_exclusive) + ")",
                        buildTranslatedStringArray(fltOrder), ServerFilter.FilterType.SINGLE)
        };
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern pattern = Pattern.compile("<a href=\"([^\"]+?)\"><img src=\"([^\"]+?)\" alt=\"([^\"]+?)\"", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            mangas.add(new Manga(getServerID(), matcher.group(3), matcher.group(1), matcher.group(2)));
        }
        return mangas;
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
        if (filters[0].length < 1 && filters[1].length < 1) {
            web = HOST + valOrder[filters[3][0]];
        }
        else {
            web = HOST + "/search/?name_sel=contain&wd=&author_sel=contain&author=&artist_sel=contain&artist=&category_id=" + includedGenres + "&out_category_id=" + excludedGenres + "&completed_series=" + valComplete[filters[2][0]] + "&type=high&page=" + pageNumber + ".html";
        }
        return getMangasFromSource(getNavigatorAndFlushParameters().get(web));
    }
}
