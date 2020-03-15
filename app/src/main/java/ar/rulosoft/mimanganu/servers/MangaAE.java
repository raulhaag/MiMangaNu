package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;

/**
 * Created by Raúl on 25/08/2018.
 */
public class MangaAE extends ServerBase {
    private static final String HOST = "https://www.mangaae.com";
    private static final String PATTERN_MANGA_FILTERED = "mangacontainer\">[\\s]*<a href=\"https*:\\/\\/[w|\\.]*mangaae.com([^\"]+).+?ga\" src=\"([^\"]+)[\\s\\S]+?href[^>]+>([^<]+)[\\s\\S]+?href[^>]+>([^<]+)";
    private static final String PATTERN_MANGA_SUMMARY = "<h3>نبذة عن المانجا</h3>\\s*<h4>(.+?)</h4>";
    private static final String PATTERN_MANGA_GENRE = "<h3>التصنيف:</h3>\\s*<ul>([\\s|\\S]+?)</ul>";
    private static final String PATTERN_MANGA_AUTHOR = "ga/author:.[^\"]+\">([^<]+)<";
    private static final String PATTERN_MANGA_STATUS = "<h3>الحالة :</h3>\\s*<h4>([^<]+)";
    private static final String PATTERN_MANGA_IMAGE = "class=\"manga-cover\" src=\"([^\"]+)";
    private static final String PATTERN_MANGA_CHAPTER = "class=\"chapter\" href=\"https*:\\/\\/[w|\\.]*mangaae.com(.+?)\">(.+?)<";
    private static final String PATTERN_CHAPTER_PAGES = "(\\d+)</a>\\s*</div>";
    private static final String PATTERN_CHAPTER_IMAGE = "\" src=\"([^\"]+)\"";

    private static String[] genres = new String[]{"قائمة المانجا",
            "أكشن", "مغامرة", "كوميديا", "دراما", "خيال", "شونين", "فوق الطبيعة", "فنون قتالية", "غموض",
            "دوجينشي", "رعب", "ميكانيك", "إطلاقة واحدة", "نفسي", "رومانسي", "حياة مدرسية", "خيال علمي",
            "شوجو", "جزء من الحياة", "رياضي", "مأساة", "سينين", "تاريخي", "جوشي", "ايتشي", "ويب تونز", "مانهوا كورية"
            , "كوميكس", "مانجا عربية", "مانوا صينية", "حريم"
    };
    private static String[] genresValues = new String[]{"",
            "/tag:Action", "/tag:Adventure", "/tag:Comedy", "/tag:Drama ", "/tag:Fantasy ",
            "/tag:Shounen ", "/tag:Supernatural ", "/tag:Martial Arts", "/tag:Mystery ",
            "/tag:Doujinshi ", "/tag:Horror ", "/tag:Mecha ", "/tag:One Shot", "/tag:Psychological ",
            "/tag:Romance ", "/tag:School Life", "/tag:Sci-fi", "/tag:Shoujo ", "/tag:Slice of Life",
            "/tag:Sports ", "/tag:Tragedy ", "/tag:Senin ", "/tag:Historical ", "/tag:Josei ",
            "/tag:Ecchi ", "/tag:Webtoons ", "/tag:Manhwa", "/tag:Comics", "/tag:Arab Manga",
            "/tag:Chinese Manhua", "/tag:Harem ",
    };


    private static String[] sortByValues = new String[]{
            "|order:views", "|order:status", "|order:chapter_count", "|order:release_date", "|order:english_name"
    };
    private static String[] sortBy = new String[]{
            "الشهرة ", "الحالة ", "عدد الفصول ", "تاريخ النشر ", "اسم المانجا"
    };
    private static String[] sortOrder = new String[]{
            "↓", "↑"
    };

    private static String[] sortOrderV = new String[]{
            "", "|arrange:plus",
    };


    /**
     * Construct a new ServerBase object.
     *
     * @param context the context for this object
     */
    MangaAE(Context context) {
        super(context);
        setFlag(R.drawable.flag_ar);
        setIcon(R.drawable.mangaae);
        setServerName("MangaAE");
        setServerID(MANGAAE);
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String data = getNavigatorAndFlushParameters().get(HOST + "/manga/search:" + term);
        return getMangasFromSource(data);

    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String data = getNavigatorAndFlushParameters().get(HOST + manga.getPath());
            // Cover
            manga.setImages(getFirstMatchDefault(PATTERN_MANGA_IMAGE, data, ""));

            // Author
            manga.setAuthor(getFirstMatchDefault(PATTERN_MANGA_AUTHOR, data,
                    context.getString(R.string.nodisponible)));
            // Genre
            manga.setGenre(getFirstMatchDefault(PATTERN_MANGA_GENRE, data,
                    context.getString(R.string.nodisponible)).replaceAll("<.+?>|\\n", " "));

            // Summary
            manga.setSynopsis(getFirstMatchDefault(PATTERN_MANGA_SUMMARY, data,
                    context.getString(R.string.nodisponible)));
            // Status
            manga.setFinished(!getFirstMatchDefault(PATTERN_MANGA_STATUS, data, "مستمرة")
                    .contains("مستمرة"));
            // Chapters
            Pattern pattern = Pattern.compile(PATTERN_MANGA_CHAPTER, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(data);
            ArrayList<Chapter> temp = new ArrayList<>();
            while (matcher.find()) {
                temp.add(0, new Chapter(matcher.group(2), matcher.group(1)));
            }
            manga.setChapters(temp);
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String web = HOST + chapter.getPath().substring(0, chapter.getPath().lastIndexOf("/1/")) + "/" + page;
        String data = getNavigatorAndFlushParameters().get(web);
        web = getFirstMatch(PATTERN_CHAPTER_IMAGE, data, "can't find image");
        return web + "|" + HOST + chapter.getPath();
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data = getNavigatorAndFlushParameters().get(HOST + chapter.getPath());
        chapter.setPages(Integer.parseInt(getFirstMatch(PATTERN_CHAPTER_PAGES, data, "can't init pages")));
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = HOST + "/manga/" + genresValues[filters[0][0]] + sortByValues[filters[1][0]] + sortOrderV[filters[2][0]];
        if (pageNumber > 1)
            web += "|page:" + pageNumber;
        String data = getNavigatorAndFlushParameters().get(web);
        return getMangasFromSource(data);
    }

    private ArrayList<Manga> getMangasFromSource(String data) {
        Pattern pattern = Pattern.compile(PATTERN_MANGA_FILTERED);
        Matcher m = pattern.matcher(data);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()) {
            mangas.add(new Manga(getServerID(), "(" + m.group(3) + ") " + m.group(4), m.group(1), m.group(2) + "|https://www.mangaae.com/"));
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(
                        context.getString(R.string.flt_genre), genres
                        , ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_order_by),
                        sortBy, ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_order),
                        sortOrder, ServerFilter.FilterType.SINGLE)};
    }

    @Override
    public boolean needRefererForImages() {
        return true;
    }
}
