package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import com.squareup.duktape.Duktape;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;

class KissManga extends ServerBase {

    private static final String PATTERN_CHAPTER =
            "<td>[\\s]*<a[\\s]*href=\"(/Manga/[^\"]+)\"[\\s]*title=\"[^\"]+\">([^\"]+)</a>[\\s]*</td>";
    private static final String PATTERN_SEARCH =
            "href=\"(/Manga/.*?)\">([^<]+)</a>[^<]+<p>[^<]+<span class=\"info\"";
    private static final String IP = "93.174.95.110";
    private static final String HOST = "kissmanga.com";
    private static final String PAGE_BASE = "http://kissmanga.com/";
    private static final String[] genre = new String[]{
            "Action", "Adult", "Adventure", "Comedy",
            "Comic", "Cooking", "Doujinshi", "Drama",
            "Ecchi", "Fantasy", "Gender Bender", "Harem",
            "Historical", "Horror", "Josei", "Lolicon",
            "Manga", "Manhua", "Manhwa", "Martial Arts",
            "Mature", "Mecha", "Medical", "Music",
            "Mystery", "One shot", "Psychological", "Romance",
            "School Life", "Sci-fi", "Seinen", "Shotacon",
            "Shoujo", "Shoujo Ai", "Shounen", "Shounen Ai",
            "Slice of Life", "Smut", "Sports", "Supernatural",
            "Tragedy", "Webtoon", "Yaoi", "Yuri"
    };
    private static String genreVV = "/Genre/";
    private static String[] order = {"Popularity", "Latest Update", "New Manga", "a-z"};
    private static String[] orderV = new String[]{"/MostPopular", "/LatestUpdate", "/Newest", ""};

    KissManga(Context context) {
        super(context);
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
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addPost("authorArtist", "");
        nav.addPost("mangaName", term);
        nav.addPost("status", "");
        nav.addPost("genres", "");

        String source = nav.post(PAGE_BASE + "/AdvanceSearch");

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
        String source = getNavigatorAndFlushParameters().get(PAGE_BASE + manga.getPath());

        // Summary
        manga.setSynopsis(Util.getInstance().fromHtml(getFirstMatchDefault(
                "<span " + "class=\"info\">Summary:</span>(.+?)</div>", source,
                defaultSynopsis)).toString());

        // Cover Image
        String pictures = getFirstMatchDefault(
                "rel=\"image_src\" href=\"(.+?)" + "\"", source, null);
        if (pictures != null) {
            manga.setImages(pictures);
        }

        // Author
        manga.setAuthor(Util.getInstance().fromHtml(getFirstMatchDefault("Author:(.+?)</p>", source, "")).toString().replaceAll("^\\s+", "").trim());

        // Genre
        manga.setGenre(Util.getInstance().fromHtml(getFirstMatchDefault("Genres:(.+?)</p>", source, "")).toString().replaceAll("^\\s+", "").trim());

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

            String source = getNavigatorAndFlushParameters().post(PAGE_BASE + chapter.getPath());

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

            String source = getNavigatorAndFlushParameters().get(PAGE_BASE + chapter.getPath().replaceAll("[^!-z]+", ""));
            String ca = getNavigatorAndFlushParameters().get(PAGE_BASE + "/Scripts/ca.js");
            String lo = getNavigatorAndFlushParameters().get(PAGE_BASE + "/Scripts/lo.js");
            Duktape duktape = Duktape.create();
            try {
                duktape.evaluate(ca);
                duktape.evaluate(lo);
                Pattern p = Pattern.compile("javascript\">(.+?)<");
                Matcher m = p.matcher(source);
                while (m.find()) {
                    if (m.group(1).contains("CryptoJS")) {
                        duktape.evaluate(m.group(1));
                    }
                }

                p = Pattern.compile("lstImages.push\\((.+?\\))\\)");
                m = p.matcher(source);
                String images = "";
                String image;
                while (m.find()) {
                    pages++;
                    image = (String) duktape.evaluate(m.group(1) + ".toString()");
                    images = images + "|" + image;
                }
                chapter.setExtra(images);
            } finally {
                duktape.close();
            }
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
            manga.setImages(m.group(1));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{new ServerFilter("Included Genre(s) (multiple no order)", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Excluded Genre(s) (multiple no order)", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Order (only applied on single genre selection)", order, ServerFilter.FilterType.SINGLE)};
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        if (filters[0].length == 0 && filters[1].length == 0) { // on first load
            return getMangasFiltered(0, 0, pageNumber);
        } else if (filters[0].length == 1) { // single genre selection
            String web = genreVV + genre[0].replaceAll(" ", "-") + orderV[0];
            for (int i = 0; i < genre.length; i++) {
                if (contains(filters[0], i)) {
                    web = genreVV + genre[i].replaceAll(" ", "-") + orderV[filters[2][0]];
                    if (pageNumber > 1) {
                        web = web + "?page=" + pageNumber;
                    }
                }
            }
            String source = getNavigatorAndFlushParameters().post(PAGE_BASE + web);
            return getMangasSource(source);
        } else {
            // multiple genre selection
            if (pageNumber > 1) {
                return new ArrayList<>();
            } else {
                Navigator nav = getNavigatorAndFlushParameters();
                nav.addPost("mangaName", "");
                nav.addPost("authorArtist", "");
                for (int i = 0; i < genre.length; i++) {
                    if (contains(filters[0], i)) {
                        nav.addPost("genres", "1");
                    } else if (contains(filters[1], i)) {
                        nav.addPost("genres", "2");
                    } else {
                        nav.addPost("genres", "0");
                    }
                }
                nav.addPost("status", ""); //stateV[filters[1][0]])
                String source = nav.post(PAGE_BASE + "/AdvanceSearch");
                return getMangasSource(source);
            }
        }

    }

    public ArrayList<Manga> getMangasFiltered(int category, int order, int pageNumber) throws Exception {
        String web = genreVV + genre[category].replaceAll(" ", "-") + orderV[order];
        if (pageNumber > 1) {
            web = web + "?page=" + pageNumber;
        }
        String source = getNavigatorAndFlushParameters().post(PAGE_BASE + web);
        return getMangasSource(source);
    }
}
