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
    private static String HOST = "http://www.mangapanda.com";
    private static String[] genre = new String[]{
            "Action", "Adventure", "Comedy", "Demons", "Drama", "Ecchi",
            "Fantasy", "Gender bender", "Harem", "Historical", "Horror",
            "Josei", "Magic", "Martial arts", "Mature", "Mecha", "Military",
            "Mystery", "One Shot", "Psychological", "Romance", "School life",
            "Sci-fi", "Seinen", "Shoujo", "Shoujoai", "Shounen", "Shounenai",
            "Slice of Life", "Smut", "Sports", "Super Power", "Supernatural",
            "Tragedy", "Vampire", "Yaoi", "Yuri"
    };

    private static String[] type = new String[]{
            "Both", "Manhwa", "Manga"
    };

    private static String[] typeV = new String[]{
            "&rd=0", "&rd=1", "&rd=2"
    };

    private static String[] status = new String[]{
            "Both", "Ongoing", "Completed"
    };

    private static String[] statusV = new String[]{
            "&status=", "&status=1", "&status=2"
    };

    private static String[] order = new String[]{
            "Popularity", "Alphabetical", "Similarity"
    };
    private static String[] orderV = new String[]{
            "&order=2", "&order=1", "&order="
    };


    MangaPanda(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.mangapanda_icon);
        this.setServerName("Mangapanda.com");
        setServerID(ServerBase.MANGAPANDA);
    }

    void SetHost(String new_host) {
        HOST = new_host;
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigatorAndFlushParameters().get(HOST + "/alphabetical");
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
        String data = getNavigatorAndFlushParameters().get(HOST + "/actions/search/?q=" + term + "&limit=100");
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
        String data = getNavigatorAndFlushParameters().get(manga.getPath());
        Pattern p = Pattern.compile(PATTERN_FRAG_CHAPTER);
        Matcher m = p.matcher(data);
        if (m.find()) {
            manga.getChapters().clear();
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
        manga.setSynopsis(getFirstMatchDefault("<p>(.+)</p>", data, defaultSynopsis));
        // Title
        manga.setImages(getFirstMatchDefault("mangaimg\"><img src=\"([^\"]+)", data, ""));
        // Status
        manga.setFinished(data.contains("</td><td>Completed</td>"));
        // Genre
        manga.setGenre(Util.getInstance().fromHtml(getFirstMatchDefault("Genre:</td><td>(.+?)</td>", data, "").replace("a> <a", "a>, <a")).toString());
        // Author
        manga.setAuthor(Util.getInstance().fromHtml(getFirstMatchDefault("Author:</td><td>(.+?)<", data, "")).toString());
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        page = (page > chapter.getPages()) ? 1 : page;
        return chapter.getPath() + "/" + page;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String data;
        data = getNavigatorAndFlushParameters().get(this.getPagesNumber(chapter, page));
        return getFirstMatch("src=\"([^\"]+?.(jpg|gif|jpeg|png|bmp))", data, "Error: Could not get the link to the image");
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data;
        data = getNavigatorAndFlushParameters().get(chapter.getPath());
        String pages =
                getFirstMatch("of (\\d+)</div>", data, "Error: Could not get the number of pages");
        chapter.setPages(Integer.parseInt(pages));
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{new ServerFilter("Genre", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Manga Type", type, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Manga Status", status, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Sorting Order", order, ServerFilter.FilterType.SINGLE)};
    }

    ///search/?w=&rd=0&status=0&order=0&genre=1000010000000000000000000000000000000&p=0

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String gens = "";
        for (int i = 0; i < genre.length; i++) {
            if (contains(filters[0], i)) {
                gens = gens + "1";
            } else {
                gens = gens + "0";
            }
        }
        ArrayList<Manga> mangas = new ArrayList<>();
        String web = HOST + "/search/?w=" + typeV[filters[1][0]] + statusV[filters[2][0]] + orderV[filters[3][0]] + "&genre=" + gens + "&p=" + ((pageNumber - 1) * 30);
        String data = getNavigatorAndFlushParameters().get(web);
        Pattern p = Pattern.compile("(http:[^']+/cover/.+?)'.+?<h3><a href=\"(.+?)\">(.+?)<");
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(3), HOST + m.group(2), false);
            manga.setImages(m.group(1).replace("r0", "l0"));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public boolean hasList() {
        return true;
    }

}
