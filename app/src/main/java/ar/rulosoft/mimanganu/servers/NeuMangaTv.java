package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.navegadores.Navigator;

/**
 * Created by Raúl on 26/08/2018.
 */
public class NeuMangaTv extends ServerBase {

    private static final String PATTERN_MANGA_FILTERED = "ss=\"l\">\\s*<img src=\"([^\"]+)[^>]+>\\s*<h2><a href=\"https*://neumanga.tv([^\"]+)\">([^<]+)";
    private static final String PATTERN_MANGA_SUMMARY = "<h2>Sinopsis[^\\n]+\\s*([\\s\\S]+?)</div>";
    private static final String PATTERN_MANGA_GENRE = "Genre\\(s\\):([\\s\\S]+?)</span";
    private static final String PATTERN_MANGA_AUTHOR = "Author\\(s\\):([\\s\\S]+?)</span";
    private static final String PATTERN_MANGA_STATUS = "Status:([\\s\\S]+?)</span";
    private static final String PATTERN_MANGA_IMAGE = "<img class=\"imagemg\" src=\"([^\"]+)";
    private static final String PATTERN_MANGA_CHAPTER = "\"https*://neumanga.tv([^\"]+)\"><h3>([^<]+?)</h3>";
    private static final String PATTERN_CHAPTER_PAGES = "class=\"prevnext\">[\\s\\S]+?\\s*(\\d+)\\s*</option>\\s*</select";
    private static final String PATTERN_CHAPTER_IMAGE = "<img class=\"imagechap\" src=\"([^\"]+)";
    private static String HOST = "https://neumanga.tv";
    private static String[] genres = new String[]{
            "Action", "Adult", "Advanture", "Adventure", "Antihero", "Comedy", "Cooking", "Dachima",
            "Demons", "Drama", "Ecchi", "Fantasy", "fighting", "Game", "Gender Bender", "Harem",
            "Historical", "Horor", "Horror", "Inaka", "Isekai", "Josei", "legend", "live School",
            "Lolicon", "Magic", "Manga", "Manhua", "Manhwa", "Martial Art", "Martial Arts",
            "Mature", "Mecha", "Miatery", "Music", "Mystery", "neco", "Over Power", "Project",
            "Psychological", "Romance", "School", "School Life", "sci fi", "Sci-fi", "Seinen",
            "Shoujo", "Shoujo Ai", "Shounen", "ShounenS", "Slice Of Life", "Smut", "sport",
            "Sports", "Super Power", "Superhero", "Supernatural", "Supranatural", "Suspense",
            "Thriller", "Tragedy", "Vampire", "Webtoon", "Webtoons", "Yuri"
    };


    private static String[] sortBy = new String[]{
            "A-Z", "Views", "Rating", "Latest"
    };
    private static String[] sortByValues = new String[]{
            "name", "views", "rating", "latest", "release_date"
    };


    /**
     * Construct a new ServerBase object.
     *
     * @param context the context for this object
     */
    NeuMangaTv(Context context) {
        super(context);
        setFlag(R.drawable.flag_indo);
        setIcon(R.drawable.neumangatv);
        setServerName("NeuMangaTv");
        setServerID(NEUMANGATV);
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String data = getNavigatorAndFlushParameters().get(HOST + "/advanced_search?name_search_mode=contain&name_search_query=" + term);
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
                    context.getString(R.string.nodisponible)));

            // Summary
            manga.setSynopsis(getFirstMatchDefault(PATTERN_MANGA_SUMMARY, data,
                    context.getString(R.string.nodisponible)));
            // Status
            manga.setFinished(!getFirstMatchDefault(PATTERN_MANGA_STATUS, data, "ongoing")
                    .contains("ongoing"));
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
        String web = HOST + chapter.getPath() + "/" + page;
        String data = getNavigatorAndFlushParameters().get(web);
        web = getFirstMatch(PATTERN_CHAPTER_IMAGE, data, "can't find image");
        web = web.replaceAll("([^:])//", "$1/"); // remove consecutive slashes
        return web + "|" + HOST + chapter.getPath();
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        String data = nav.get(HOST + chapter.getPath());
        String np = getFirstMatchDefault(PATTERN_CHAPTER_PAGES, data, null);
        if (np == null) {
            nav.get(HOST + chapter.getPath() + "?to=001&cid=#/yes_i_am");//request cookie
            np = getFirstMatch(PATTERN_CHAPTER_PAGES, data, "can't init pages");
        }
        chapter.setPages(Integer.parseInt(np));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        StringBuilder web = new StringBuilder(HOST);
        web.append("/advanced_search?name_search_mode=contain&name_search_query=&artist_search_mode=contain&artist_search_query=&author_search_mode=contain&author_search_query=&genre0=%5B%5D&genre1=%5B");

        for (int i = 0; i < filters[0].length; i++) {
            if (filters[0][i] == 1) {
                web.append("%22");
                web.append(genres[i]);
                web.append("%22%2C");
            }
        }
        if (web.substring(web.length() - 3, web.length()).equals("%2C")) {
            web.delete(web.length() - 3, web.length());
        }
        web.append("%5D&genre2=%5B");
        for (int i = 0; i < filters[0].length; i++) {
            if (filters[0][i] == -1) {
                web.append("%22");
                web.append(genres[i]);
                web.append("%22%2C");
            }
        }
        if (web.substring(web.length() - 3, web.length()).equals("%2C")) {
            web.delete(web.length() - 3, web.length());
        }
        web.append("%5D&year_search_mode=on&year_value=&rating_search_mode=is&rating_value=&manga_status=&advpage=");
        web.append(pageNumber);
        web.append("&sortby=");
        web.append(sortByValues[filters[1][0]]);
        String data = getNavigatorAndFlushParameters().get(web.toString());
        return getMangasFromSource(data);
    }

    private ArrayList<Manga> getMangasFromSource(String data) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile(PATTERN_MANGA_FILTERED);
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(3), m.group(2), m.group(1).replace("=60,75", "=200,300")));
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(
                        context.getString(R.string.flt_genre), genres
                        , ServerFilter.FilterType.MULTI_STATES),
                new ServerFilter(
                        context.getString(R.string.flt_order_by), sortBy
                        , ServerFilter.FilterType.SINGLE),
        };
    }

    @Override
    public boolean hasList() {
        return false;
    }

}
