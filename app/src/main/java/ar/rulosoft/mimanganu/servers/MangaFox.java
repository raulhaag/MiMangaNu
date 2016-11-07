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

public class MangaFox extends ServerBase {

    private static final String[] genre = {
            "Action", "Adult", "Adventure", "Comedy", "Doujinshi", "Drama", "Ecchi",
            "Fantasy", "Gender Bender", "Harem", "Historical", "Horror", "Josei", "Martial Arts",
            "Mecha", "Mystery", "One Shot", "Psychological", "Romance", "School Life", "Sci-fi",
            "Seinen", "Shoujo", "Shoujo Ai", "Shounen", "Slice of Life", "Smut", "Sports",
            "Supernatural", "Tragedy", "Webtoons", "Yuri"
    };
    private static final String[] genreV = {
            "action", "adult", "adventure", "comedy", "doujinshi", "Drama", "ecchi",
            "fantasy", "gender-bender", "harem", "historical", "horror", "josei", "martial-arts",
            "mecha", "mystery", "one_shot", "psychological", "romance", "school-life", "sci-fi",
            "seinen", "shoujo", "shoujo-Ai", "shounen", "slice-of-life", "smut", "sports",
            "supernatural", "tragedy", "webtoons", "yuri"
    };
    private static final String PATRON_CAPS_VIS = "<a class=\"manga_img\" href=\"(.+?)\".+?src=\"(.+?)\".+?(<em class=\"tag_completed\"></em>						</a>|</a>).+?rel=\".+?\">(.+?)<";
    private static final String PATTERN_SERIE = "<li><a href=\"(.+?)\" rel=\"\\d+\" class=\"series_preview manga_(close|open)\">(.+?)</a></li>";
    private static final String SEGMENTO = "<div class=\"manga_list\">(.+?)<div class=\"clear gap\">";
    private static final String PATRON_PORTADA = "<div class=\"cover\">.+?src=\"(.+?)\"";
    private static final String PATRON_SINOPSIS = "<p class=\"summary\">(.+?)</p>";
    private static final String PATTERN_CAPITULOS = "<h\\d>[\\s]+<a href=\"([^\"]+)\".+?>([^<]+)([^\"]+<span class=\"title nowrap\">(.+?)<)?";
    private static final String PATRON_LAST = "(\\d+)</option>					<option value=\"0\"";
    private static final String PATRON_IMAGEN = "><img src=\"([^\"]+?.(jpg|gif|jpeg|png|bmp))";
    private static String[] type = new String[]{
            "Any", "Japanese Manga", "Korean Manhwa", "Chinese Manhua"
    };
    private static String[] typeV = new String[]{
            "&type=", "&type=1", "&type=2", "&type=3"
    };
    private static String[] status = new String[]{
            "Either", "Ongoing", "Completed"
    };
    private static String[] statusV = new String[]{
            "&is_completed=", "&is_completed=1", "&is_completed=0"
    };
    private static String[] order = new String[]{
            "Manga Title", "Views", "Chapters"
    };
    private static String[] orderV = new String[]{
            "&sort=name&order=az", "&sort=views&order=za", "&sort=total_chapters&order=za"
    };
    private static String[] orden = {"Popularity", "A - Z", "Rating", "Last Update"};
    private static String[] ordenM = {"", "?az", "?rating", "?latest"};
    long last_search;

    public MangaFox() {
        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.mangafox_icon);
        this.setServerName("MangaFox");
        setServerID(ServerBase.MANGAFOX);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigator().get("http://mangafox.me/manga/");
        data = getFirstMatch(SEGMENTO, data, "no se ha obtenido el segmento");
        Pattern p = Pattern.compile(PATTERN_SERIE);
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(ServerBase.MANGAFOX, m.group(3), m.group(1), false);
            if (m.group(2).length() > 4) {
                manga.setFinished(true);
            }
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().size() == 0 || forceReload) {
            Pattern p;
            Matcher m;
            String data = getNavigator().get((manga.getPath()));

            // Title
            manga.setImages(getFirstMatchDefault(PATRON_PORTADA, data, ""));
            // Summary
            manga.setSynopsis(getFirstMatchDefault(PATRON_SINOPSIS, data, defaultSynopsis));

            manga.setFinished(data.contains("<h\\d>Status:</h\\d>    <span>        Completed"));

            // Author
            manga.setAuthor(getFirstMatchDefault("\"/search/author/.+?>(.+?)<", data, ""));

            // Genre
            manga.setGenre(Util.getInstance().fromHtml(getFirstMatchDefault("(<a href=\"http://mangafox.me/search/genres/.+?</td>)", data, "")).toString());

            // Chapter
            p = Pattern.compile(PATTERN_CAPITULOS);
            m = p.matcher(data);

            while (m.find()) {
                Chapter mc;
                if (m.group(4) != null)
                    mc = new Chapter(m.group(2).trim() + ": " + m.group(4), m.group(1).replace("1.html", ""));
                else
                    mc = new Chapter(m.group(2).trim(), m.group(1).replace("1.html", ""));
                mc.addChapterFirst(manga);
            }
        }
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload)
            loadChapters(manga, forceReload);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        if (page > chapter.getPages()) {
            page = 1;
        }
        if (chapter.getPath().endsWith("html") && chapter.getPath().indexOf("/") > 0) {
            chapter.setPath(chapter.getPath().substring(0, chapter.getPath().lastIndexOf("/") + 1));
        }
        return chapter.getPath() + page + ".html";

    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String data;
        data = getNavigator().get(this.getPagesNumber(chapter, page));
        return getFirstMatch(PATRON_IMAGEN, data, "Error: no se pudo obtener el enlace a la imagen");
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data;
        data = getNavigator().get(chapter.getPath());
        String paginas = getFirstMatch(PATRON_LAST, data, "Error: no se pudo obtener el numero de paginas");
        chapter.setPages(Integer.parseInt(paginas) - 1);//last page is for comments
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigator()
                .get("http://mangafox.me/search.php?name_method=cw&name="
                        + term
                        + "&type=&author_method=cw&author=&artist_method=cw&artist=&genres%5BAction%5D=0&genres%5BAdult%5D=0&genres%5BAdventure%5D=0&genres%5BComedy%5D=0&genres%5BDoujinshi%5D=0&genres%5BDrama%5D=0&genres%5BEcchi%5D=0&genres%5BFantasy%5D=0&genres%5BGender+Bender%5D=0&genres%5BHarem%5D=0&genres%5BHistorical%5D=0&genres%5BHorror%5D=0&genres%5BJosei%5D=0&genres%5BMartial+Arts%5D=0&genres%5BMature%5D=0&genres%5BMecha%5D=0&genres%5BMystery%5D=0&genres%5BOne+Shot%5D=0&genres%5BPsychological%5D=0&genres%5BRomance%5D=0&genres%5BSchool+Life%5D=0&genres%5BSci-fi%5D=0&genres%5BSeinen%5D=0&genres%5BShoujo%5D=0&genres%5BShoujo+Ai%5D=0&genres%5BShounen%5D=0&genres%5BShounen+Ai%5D=0&genres%5BSlice+of+Life%5D=0&genres%5BSmut%5D=0&genres%5BSports%5D=0&genres%5BSupernatural%5D=0&genres%5BTragedy%5D=0&genres%5BWebtoons%5D=0&genres%5BYaoi%5D=0&genres%5BYuri%5D=0&released_method=eq&released=&rating_method=eq&rating=&is_completed=&advopts=1");
        Pattern p = Pattern.compile("<td><a href=\"(http://mangafox.me/manga/.+?)\".+?\">(.+?)<");
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(2).trim(), m.group(1), false));
        }
        return mangas;
    }

    //http://mangafox.me/search.php?name_method=cw&name=&type=&author_method=cw&author=&artist_method=cw&artist=
    // &genres%5BAction%5D=1&genres%5BAdult%5D=0&genres%5BAdventure%5D=0&genres%5BComedy%5D=0&genres%5BDoujinshi%5D=0&genres%5BDrama%5D=1&genres%5BEcchi%5D=0
    // &genres%5BFantasy%5D=0&genres%5BGender+Bender%5D=0&genres%5BHarem%5D=0&genres%5BHistorical%5D=0&genres%5BHorror%5D=0&genres%5BJosei%5D=0
    // &genres%5BMartial+Arts%5D=0&genres%5BMature%5D=0&genres%5BMecha%5D=0&genres%5BMystery%5D=0&genres%5BOne+Shot%5D=0&genres%5BPsychological%5D=0
    // &genres%5BRomance%5D=0&genres%5BSchool+Life%5D=0&genres%5BSci-fi%5D=0&genres%5BSeinen%5D=1&genres%5BShoujo%5D=0&genres%5BShoujo+Ai%5D=0&genres%5BShounen%5D=0
    // &genres%5BShounen+Ai%5D=0&genres%5BSlice+of+Life%5D=0&genres%5BSmut%5D=0&genres%5BSports%5D=0&genres%5BSupernatural%5D=0&genres%5BTragedy%5D=0
    // &genres%5BWebtoons%5D=0&genres%5BYaoi%5D=0&genres%5BYuri%5D=0
    // &released_method=eq&released=&rating_method=eq&rating=&is_completed=&advopts=1&sort=total_chapters&order=za

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String gens = "";
        long diff = System.currentTimeMillis() - last_search;
        if (diff < 5000) {
            Thread.sleep(diff);
        }
        for (int i = 0; i < genre.length; i++) {
            if (contains(filters[1], i)) {
                gens = gens + "&genres%5B" + genre[i].replaceAll(" ", "+") + "%5D=1";
            } else {
                gens = gens + "&genres%5B" + genre[i].replaceAll(" ", "+") + "%5D=0";
            }
        }

        String web = "http://mangafox.me/search.php?name_method=cw&name=" + typeV[filters[0][0]] +
                "&author_method=cw&author=&artist_method=cw&artist=" + gens +
                "&released_method=eq&released=&rating_method=eq&rating=" + statusV[filters[2][0]] +
                "&advopts=1" + orderV[filters[3][0]] + "&page=" + pageNumber;
        String data = getNavigator().get(web);
        last_search = System.currentTimeMillis();
        long ctime = last_search / 1000;
        Pattern p = Pattern.compile("<td><a href=\"(http://mangafox.me/manga/.+?)\".+?rel=\"(\\d+)\">(.+?)<");
        Matcher m = p.matcher(data);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(3).trim(), m.group(1), false);
            manga.setImages("http://h.mfcdn.net/store/manga/" + m.group(2) + "/cover.jpg?v=" + ctime);
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters(Context context) {
        return new ServerFilter[]{new ServerFilter("Type", type, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Genres", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Completed Series", status, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Order", order, ServerFilter.FilterType.SINGLE)};
    }
}
