package ar.rulosoft.mimanganu.servers;

import android.text.Html;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public class KissManga extends ServerBase {

    private static final String PATTERN_CHAPTER =
            "<td>[\\s]*<a[\\s]*href=\"(/Manga/[^\"]+)\"[\\s]*title=\"[^\"]+\">([^\"]+)</a>[\\s]*</td>";
    private static final String PATTERN_SEARCH =
            "href=\"(/Manga/.*?)\">([^<]+)</a>[^<]+<p>[^<]+<span class=\"info\"";
    public static String HOST = "http://kissmanga.com";
    static String[] genre = new String[]{
            "All", "Action", "Adult", "Adventure", "Comedy", "Comic",
            "Doujinshi", "Drama", "Ecchi", "Fantasy", "Harem", "Historical",
            "Horror", "Lolicon", "Manga", "Manhua", "Manhwa", "Mature", "Mecha",
            "Mystery", "Psychological", "Romance", "Sci-fi", "Seinen",
            "Shotacon", "Shoujo", "Shounen", "Smut", "Sports", "Supernatural",
            "Webtoon", "Yuri"
    };
    static String[] genreV = new String[]{
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
    static String[] order = new String[]{
            "/MostPopular", "/LatestUpdate", "/Newest", ""
    };

    public KissManga() {
        this.setFlag(R.drawable.flag_eng);
        this.setIcon(R.drawable.kissmanga);
        this.setServerName("KissManga");
        setServerID(ServerBase.KISSMANGA);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {

        Navegador nav = getNavWithHeader();

        // make use of AdvanceSearch, mor data is then needed
        nav.addPost("authorArtist", "");
        nav.addPost("mangaName", term);
        nav.addPost("status", "");
        nav.addPost("genres", "");

        String source = nav.post(HOST + "/AdvanceSearch");

        ArrayList<Manga> searchList;
        Pattern p = Pattern.compile(PATTERN_SEARCH);
        Matcher m = p.matcher(source);
        if (m.find()) {
            searchList = new ArrayList<>();
            boolean status = getFirstMatchDefault("Status:</span>&nbsp;([\\S]+)", source, "Ongoing").length() == 9;
            searchList.add(new Manga(KISSMANGA, m.group(2), m.group(1), status));
        } else {
            searchList = getMangasSource(source);
        }

        return searchList;
    }

    @Override
    public void loadChapters(Manga m, boolean forceReload) throws Exception {
        if (m.getChapters() == null || m.getChapters().size() == 0 ||
                forceReload) loadMangaInformation(m, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga m, boolean forceReload) throws Exception {
        String source = getNavWithHeader().post(HOST + m.getPath());

        // Summary
        m.setSinopsis(Html.fromHtml(getFirstMatchDefault(
                "<span " + "class=\"info\">Summary:</span>(.+?)</div>", source,
                "Without" + " synopsis.")).toString());
        // Title
        String pictures = getFirstMatchDefault(
                "rel=\"image_src\" href=\"(.+?)" + "\"", source, null);
        if (pictures != null) {
            m.setImages(
                    HOST + pictures.replace(HOST, "") + "|kissmanga.com");
        }

        // Author
        m.setAuthor(getFirstMatchDefault("href=\"/AuthorArtist/.+?>(.+?)<", source, ""));

        // Chapter
        Pattern p = Pattern.compile(PATTERN_CHAPTER);
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(2).replace(" Read Online", ""), matcher.group(1)));
        }
        m.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter c, int page) {
        return c.getPath();
    }

    @Override
    public String getImageFrom(Chapter c, int page) throws Exception {
        if (c.getExtra() == null || c.getExtra().length() < 2) {

            String source = getNavWithHeader().post(HOST + c.getPath());

            Pattern p = Pattern.compile("lstImages.push\\(\"(.+?)\"");
            Matcher m = p.matcher(source);
            String images = "";
            while (m.find()) {
                images = images + "|" + m.group(1);
            }
            c.setExtra(images);
        }

        return c.getExtra().split("\\|")[page];
    }

    @Override
    public void chapterInit(Chapter c) throws Exception {
        int pages = 0;
        if (c.getExtra() == null || c.getExtra().length() < 2) {

            String source = getNavWithHeader().post(HOST + c.getPath());

            Pattern p = Pattern.compile("lstImages.push\\(\"(.+?)\"");
            Matcher m = p.matcher(source);
            String images = "";
            while (m.find()) {
                pages++;
                images = images + "|" + m.group(1);
            }
            c.setExtra(images);
        }
        c.setPages(pages);
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int category, int order, int pageNumber) throws Exception {
        String web = genreV[category] + KissManga.order[order];
        if (pageNumber > 1) {
            web = web + "?page=" + pageNumber;
        }
        String source = getNavWithHeader().post(HOST + web);
        return getMangasSource(source);
    }

    public ArrayList<Manga> getMangasSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p =
                Pattern.compile("src=\"([^\"]+)\" style=\"float.+?href=\"(.+?)\">(.+?)<");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga =
                    new Manga(KISSMANGA, m.group(3), m.group(2), false);
            manga.setImages(HOST + m.group(1).replace(HOST, "") +
                    "|kissmanga.com");
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public String[] getCategories() {
        return genre;
    }

    @Override
    public String[] getOrders() {
        return new String[]{
                "Popularity", "Latest Update", "New Manga", "a-z"
        };
    }

    @Override
    public boolean hasList() {
        return false;
    }

}
