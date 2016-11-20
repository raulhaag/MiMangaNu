package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;

public class KissManga extends ServerBase {

    private static final String PATTERN_CHAPTER =
            "<td>[\\s]*<a[\\s]*href=\"(/Manga/[^\"]+)\"[\\s]*title=\"[^\"]+\">([^\"]+)</a>[\\s]*</td>";
    private static final String PATTERN_SEARCH =
            "href=\"(/Manga/.*?)\">([^<]+)</a>[^<]+<p>[^<]+<span class=\"info\"";
    private static String IP = "93.174.95.110";
    private static String HOST = "kissmanga.com";

    /**
     * single
     **/
    private static String[] genre = new String[]{
            "All", "Action", "Adult", "Adventure", "Comedy", "Comic",
            "Doujinshi", "Drama", "Ecchi", "Fantasy", "Harem", "Historical",
            "Horror", "Lolicon", "Manga", "Manhua", "Manhwa", "Mature", "Mecha",
            "Mystery", "Psychological", "Romance", "Sci-fi", "Seinen",
            "Shotacon", "Shoujo", "Shounen", "Smut", "Sports", "Supernatural",
            "Webtoon", "Yuri"
    };
    private static String[] genreV = new String[]{
            "/MangaList", "/Genre/Action", "/Genre/Adult", "/Genre/Adventure",
            "/Genre/Comedy", "/Genre/Comic", "/Genre/Doujinshi", "/Genre/Drama",
            "/Genre/Ecchi", "/Genre/Fantasy", "/Genre/Harem",
            "/Genre/Historical", "/Genre/Horror", "/Genre/Lolicon",
            "/Genre/Manga", "/Genre/Manhua", "/Genre/Manhwa", "/Genre/Mature",
            "/Genre/Mecha", "/Genre/Mystery", "/Genre/Psychological",
            "/Genre/Romance", "/Genre/Sci-fi", "/Genre/Seinen",
            "/Genre/Shotacon", "/Genre/Shoujo", "/Genre/Shounen", "/Genre/Smut",
            "/Genre/Sports", "/Genre/Supernatural", "/Genre/Webtoon",
            "/Genre/Yuri"
    };
    private static String[] order = {"Popularity", "Latest Update", "New Manga", "a-z"};
    private static String[] orderV = new String[]{"/MostPopular", "/LatestUpdate", "/Newest", ""};

    /**
     * multi
     **/
    private static String[] genreM = new String[]{
            "Action", "Adult", "Adventure", "Comedy", "Comic", "Cooking", "Doujinshi", "Drama",
            "Ecchi", "Fantasy", "Gender Bender", "Harem", "Historical", "Horror", "Josei", "Lolicon",
            "Manga", "Manhua", "Manhwa", "Martial Arts", "Mature", "Mecha", "Medical", "Music",
            "Mystery", "One shot", "Psychological", "Romance", "School Life", "Sci-fi", "Seinen",
            "Shotacon", "Shoujo", "Shoujo Ai", "Shounen", "Shounen Ai", "Slice of Life", "Smut",
            "Sports", "Supernatural", "Tragedy", "Webtoon", "Yaoi", "Yuri"
    };
    private static String[] state = new String[]{
            "Any", "Ongoing", "Completed"
    };
    private static String[] stateV = new String[]{
            "", "Ongoing", "Completed"
    };

    public KissManga() {
        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.kissmanga_icon);
        this.setServerName("KissManga");
        setServerID(ServerBase.KISSMANGA);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        // make use of AdvanceSearch, more data is then needed
        Navigator nav = getNavigator();
        nav.addPost("authorArtist", "");
        nav.addPost("mangaName", term);
        nav.addPost("status", "");
        nav.addPost("genres", "");

        String source = nav.post(IP, "/AdvanceSearch", HOST);

        ArrayList<Manga> searchList;
        Pattern p = Pattern.compile(PATTERN_SEARCH);
        Matcher m = p.matcher(source);
        if (m.find()) {
            searchList = new ArrayList<>();
            boolean status = getFirstMatchDefault("Status:</span>&nbsp;([\\S]+)", m.group(), "Ongoing").length() == 9;
            searchList.add(new Manga(KISSMANGA, m.group(2), m.group(1), status));
        } else {
            searchList = getMangasSource(source);
        }
        return searchList;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 ||
                forceReload) loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        String source = getNavigator().get(IP, manga.getPath(), HOST);

        // Summary
        manga.setSynopsis(Util.getInstance().fromHtml(getFirstMatchDefault(
                "<span " + "class=\"info\">Summary:</span>(.+?)</div>", source,
                defaultSynopsis)).toString());
        // Title
        String pictures = getFirstMatchDefault(
                "rel=\"image_src\" href=\"(.+?)" + "\"", source, null);
        if (pictures != null) {
            manga.setImages("http://" + IP + pictures.replace("http://kissmanga.com", "") + "|" + HOST);
        }

        // Author
        manga.setAuthor(getFirstMatchDefault("href=\"/AuthorArtist/.+?>(.+?)<", source, ""));

        // Genre
        manga.setGenre((Util.getInstance().fromHtml(getFirstMatchDefault("Genres:(.+?)</p>", source, "")).toString().replaceAll("^\\s+", "").trim()));

        manga.setFinished(getFirstMatchDefault("Status:</span>&nbsp;([\\S]+)", source, "Ongoing").length() == 9);

        // Chapter
        Pattern p = Pattern.compile(PATTERN_CHAPTER);
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(2).replace(" Read Online", ""), matcher.group(1)));
        }
        manga.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return chapter.getPath();
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        if (chapter.getExtra() == null || chapter.getExtra().length() < 2) {

            String source = getNavigator().post(IP, chapter.getPath(), HOST);

            Pattern p = Pattern.compile("lstImages.push\\(\"(.+?)\"");
            Matcher m = p.matcher(source);
            String images = "";
            while (m.find()) {
                images = images + "|" + m.group(1);
            }
            chapter.setExtra(images);
        }

        return chapter.getExtra().split("\\|")[page];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        int pages = 0;
        if (chapter.getExtra() == null || chapter.getExtra().length() < 2) {

            String source = getNavigator().get(IP, chapter.getPath().replaceAll("[^!-z]+", ""), HOST);

            Pattern p = Pattern.compile("lstImages.push\\(\"(.+?)\"");
            Matcher m = p.matcher(source);
            String images = "";
            while (m.find()) {
                pages++;
                images = images + "|" + m.group(1);
            }
            chapter.setExtra(images);
        }
        chapter.setPages(pages);
    }

    private ArrayList<Manga> getMangasSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p =
                Pattern.compile("src=\"([^\"]+)\" style=\"float.+?href=\"(.+?)\">(.+?)<");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga =
                    new Manga(KISSMANGA, m.group(3), m.group(2), false);
            manga.setImages("http://" + IP + m.group(1).replace("http://kissmanga.com", "") + "|" + HOST);
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public ServerFilter[] getServerFilters(Context context) {
        return new ServerFilter[]{new ServerFilter("Genres", genre, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Order", order, ServerFilter.FilterType.SINGLE)};
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        /*if (pageNumber > 1) {
            return new ArrayList<>();
        } else {
            Navigator nav = getNavigator();
            nav.addPost("mangaName", "");
            nav.addPost("authorArtist", "");
            if (filters[0].length == 0) {
                for (int i = 1; i < genreM.length; i++) {
                    nav.addPost("genres", "0");
                }
            } else {
                for (int i = 0; i < genreM.length; i++) {
                    if (contains(filters[0], i)) {

                        nav.addPost("genres", "1");
                    } else {
                        nav.addPost("genres", "0");
                    }
                }
            }
            nav.addPost("status", stateV[filters[1][0]]);
            String source = nav.post(IP, "/AdvanceSearch", HOST);
            return getMangasSource(source);*/
        return getMangasFiltered(filters[0][0], filters[1][0], pageNumber);
    }

    public ArrayList<Manga> getMangasFiltered(int category, int order, int pageNumber) throws Exception {
        String web = genreV[category] + orderV[order];
        if (pageNumber > 1) {
            web = web + "?page=" + pageNumber;
        }
        String source = getNavigator().post(IP, web, HOST);
        return getMangasSource(source);
    }
}
