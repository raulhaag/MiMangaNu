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

    private static int[] fltGenre = {
            R.string.flt_genre_adventure,
            R.string.flt_genre_action,
            R.string.flt_genre_kitchen_sink_drama,
            R.string.flt_genre_daemons,
            R.string.flt_genre_drama,
            R.string.flt_genre_ecchi,
            R.string.flt_genre_erotic,
            R.string.flt_genre_fantasy,
            R.string.flt_genre_gender_bender,
            R.string.flt_genre_harem,
            R.string.flt_genre_historical,
            R.string.flt_genre_horror,
            R.string.flt_demographic_josei,
            R.string.flt_genre_martial_arts,
            R.string.flt_genre_card_game,
            R.string.flt_genre_comedy,
            R.string.flt_genre_magic,
            R.string.flt_genre_mecha,
            R.string.flt_genre_military,
            R.string.flt_genre_music,
            R.string.flt_genre_mystery,
            R.string.flt_genre_romance,
            R.string.flt_genre_school_life,
            R.string.flt_genre_sci_fi,
            R.string.flt_demographic_shoujo,
            R.string.flt_demographic_shounen,
            R.string.flt_genre_game,
            R.string.flt_genre_sports,
            R.string.flt_genre_super_powers,
            R.string.flt_genre_thriller,
            R.string.flt_genre_vampire,
            R.string.flt_genre_video_game,
            R.string.flt_demographic_yaoi
    };
    private static String[] valGenre = {
            "63", "64", "82", "76", "65", "79", "88", "66", "91", "73", "84", "72", "95", "81",
            "78", "67", "68", "89", "90", "83", "69", "74", "70", "86", "85", "75", "92", "87",
            "80", "94", "71", "77", "93"
    };

    private static int[] fltCategory = {
            R.string.flt_type_all,
            R.string.flt_order_views,
            R.string.flt_type_new,
            R.string.flt_type_latest_releases
    };
    private static String[] valCategory = {
            "/category/", "/list/Hot-Book/", "/list/New-Book/", "/list/New-Update/"
    };

    private static int[] fltStatus = {
            R.string.flt_status_all,
            R.string.flt_status_completed,
            R.string.flt_status_ongoing
    };
    private static String[] valStatus = {
            "either", "yes", "no"};

    DeNineManga(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_de);
        this.setIcon(R.drawable.ninemanga);
        this.setServerName("DeNineManga");
        setServerID(ServerBase.DENINEMANGA);
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        throw new UnsupportedOperationException("Error: getMangas() not implemented for DeNineManga");
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigatorAndFlushParameters().get(
                HOST + "/search/?wd=" + URLEncoder.encode(term, "UTF-8"));
        Pattern p = Pattern.compile(PATTERN_MANGA, Pattern.DOTALL);
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(2), HOST + m.group(1), false);
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
                    context.getString(R.string.nodisponible))).toString().replaceFirst("^Zusammenfassung: ", "").replace("\\", ""));
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
        Pattern p = Pattern.compile("src=\"(http://pic\\.wiemanga\\.com/comics/pic/[^\"]+?)\"", Pattern.DOTALL);
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
                new ServerFilter(context.getString(R.string.flt_exclude_tags),
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
