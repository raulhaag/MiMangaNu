package ar.rulosoft.mimanganu.servers;

import android.text.Html;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public class MangaPanda extends ServerBase {

    private static final String PATTERN_SERIE =
            "<li><a href=\"([^\"]+)\">([^<]+)";
    private static final String PATTERN_SUB =
            "<div class=\"series_col\">([\\s\\S]+?)<div id=\"adfooter\">";
    private static final String PATTERN_FRAG_CHAPTER =
            "<div id=\"chapterlist\">([\\s\\S]+?)</table>";
    private static final String PATTERN_CHAPTER =
            "<a href=\"([^\"]+)\">([^\"]+?)</a>.:([^\"]+?)</td>";
    private static final String PATTERN_CHAPTER_WEB =
            "/[-|\\d]+/([^/]+)/chapter-(\\d+).html";
    private static final String[] genreV = {
            "", "action", "adventure", "comedy", "demons", "drama", "ecchi",
            "fantasy", "gender-bender", "harem", "historical", "horror",
            "josei", "magic", "martial-arts", "mature", "mecha", "military",
            "mystery", "one-shot", "psychological", "romance", "school-life",
            "sci-fi", "seinen", "shoujo", "shoujoai", "shounen", "shounenai",
            "slice-of-life", "smut", "sports", "super-power", "supernatural",
            "tragedy", "vampire", "yaoi", "yuri"
    };
    private static final String[] order = {"Popular"};
    private static String HOST = "http://www.mangapanda.com";
    private static String[] genre = new String[]{
            "All", "Action", "Adventure", "Comedy", "Demons", "Drama", "Ecchi",
            "Fantasy", "Gender bender", "Harem", "Historical", "Horror",
            "Josei", "Magic", "Martial arts", "Mature", "Mecha", "Military",
            "Mystery", "One Shot", "Psychological", "Romance", "School life",
            "Sci-fi", "Seinen", "Shoujo", "Shoujoai", "Shounen", "Shounenai",
            "Slice of Life", "Smut", "Sports", "Super Power", "Supernatural",
            "Tragedy", "Vampire", "Yaoi", "Yuri"
    };

    public MangaPanda() {
        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.mangapanda_icon);
        this.setServerName("Mangapanda.com");
        setServerID(ServerBase.MANGAPANDA);
    }

    protected void SetHost(String new_host) {
        HOST = new_host;
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = new Navegador().get(HOST + "/alphabetical");
        Pattern p = Pattern.compile(PATTERN_SUB);
        Matcher m = p.matcher(data);
        if (m.find()) {
            String b = m.group(1);
            Pattern p1 = Pattern.compile(PATTERN_SERIE);
            Matcher m1 = p1.matcher(b);
            while (m1.find()) {
                mangas.add(new Manga(this.getServerID(), m1.group(2),
                        HOST + m1.group(1), false));
            }
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        Navegador nav = new Navegador();
        String data = nav.get(HOST + "/actions/search/?q=" + term + "&limit=100");
        Pattern p = Pattern.compile("(.+?)\\|.+?\\|(/.+?)\\|\\d+");
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(1).trim(),
                    HOST + m.group(2), false));
        }
        return mangas;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 ||
                forceReload) loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        String data = new Navegador().get(manga.getPath());
        Pattern p = Pattern.compile(PATTERN_FRAG_CHAPTER);
        Matcher m = p.matcher(data);
        if (m.find()) {
            Pattern p1 = Pattern.compile(PATTERN_CHAPTER);
            Matcher m1 = p1.matcher(m.group(1));
            while (m1.find()) {
                String web = m1.group(1);
                if (web.matches(PATTERN_CHAPTER_WEB)) {
                    Pattern p2 = Pattern.compile(PATTERN_CHAPTER_WEB);
                    Matcher m2 = p2.matcher(web);
                    if (m2.find()) web = m2.group(1) + "/" + m2.group(2);
                }
                String chName = m1.group(2);
                if (!m1.group(3).trim().isEmpty())
                    chName += " :" + m1.group(3);
                manga.addChapter(new Chapter(chName, HOST + web));
            }
        }
        // Summary
        manga.setSynopsis(getFirstMatchDefault("<p>(.+)</p>", data, "Without synopsis"));
        // Title
        manga.setImages(getFirstMatchDefault("mangaimg\"><img src=\"([^\"]+)", data, ""));
        // Status
        manga.setFinished(data.contains("</td><td>Completed</td>"));
        // Genre
        manga.setGenre(Html.fromHtml(getFirstMatchDefault("Genre:</td><td>(.+?)</td>", data, "").replace("a> <a", "a>, <a")).toString());
        // Author
        manga.setAuthor(Html.fromHtml(getFirstMatchDefault("Author:</td><td>(.+?)<", data, "")).toString());
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        page = (page > chapter.getPages()) ? 1 : page;
        return chapter.getPath() + "/" + page;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String data;
        data = new Navegador().get(this.getPagesNumber(chapter, page));
        return getFirstMatch("src=\"([^\"]+?.(jpg|gif|jpeg|png|bmp))", data, "Error: Could not get the link to the image");
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data;
        data = new Navegador().get(chapter.getPath());
        String pages =
                getFirstMatch("of (\\d+)</div>", data, "Error: Could not get the number of pages");
        chapter.setPages(Integer.parseInt(pages));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int category, int order, int pageNumber) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String web;
        if (category == 0)
            web = HOST + "/popular" + "/" + (pageNumber - 1) * 20;
        else
            web = HOST + "/popular" + "/" + genreV[category] + "/" + (pageNumber - 1) * 20;

        String data = new Navegador().get(web);
        Pattern p =
                Pattern.compile("url\\('(.+?)'.+?href=\"(.+?)\">(.+?)</a>");
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga;
            if (m.group(2).startsWith("/")) manga =
                    new Manga(getServerID(), m.group(3),
                            HOST + m.group(2), false);
            else manga = new Manga(getServerID(), m.group(3),
                    HOST + m.group(2), false);
            manga.setImages(m.group(1));
            mangas.add(manga);
        }
        hasMore = !mangas.isEmpty();
        return mangas;
    }

    @Override
    public String[] getCategories() {
        return genre;
    }

    @Override
    public String[] getOrders() {
        return order;
    }

    @Override
    public boolean hasList() {
        return true;
    }

}
