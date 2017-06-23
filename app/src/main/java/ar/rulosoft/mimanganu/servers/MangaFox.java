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

class MangaFox extends ServerBase {
    private static final String[] genre = {
            "All",
            "Action", "Adult", "Adventure", "Comedy", "Doujinshi", "Drama", "Ecchi",
            "Fantasy", "Gender Bender", "Harem", "Historical", "Horror", "Josei", "Martial Arts",
            "Mecha", "Mystery", "One Shot", "Psychological", "Romance", "School Life", "Sci-fi",
            "Seinen", "Shoujo", "Shoujo Ai", "Shounen", "Shounen Ai", "Slice of Life", "Smut", "Sports",
            "Supernatural", "Tragedy", "Webtoons", "Yaoi", "Yuri"
    };
    private static final String PATTERN_SERIE = "<li><a href=\"(.+?)\" rel=\"\\d+\" class=\"series_preview manga_(close|open)\">(.+?)</a></li>";
    private static final String SEGMENTO = "<div class=\"manga_list\">(.+?)<div class=\"clear gap\">";
    private static final String PATRON_PORTADA = "<div class=\"cover\">.+?src=\"(.+?)\"";
    private static final String PATRON_SINOPSIS = "<p class=\"summary\">(.+?)</p>";
    private static final String PATTERN_CAPITULOS = "<h\\d>[\\s]+<a href=\"([^\"]+)\".+?>([^<]+)([^\"]+<span class=\"title nowrap\">(.+?)<)?";
    private static final String PATRON_LAST = "(\\d+)</option>					<option value=\"0\"";
    private static String HOST = "http://mangafox.me";
    private static String genreVV = "/directory/";
    /*private static String[] type = new String[]{
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
    };*/
    private static String[] order = new String[]{
            "Popularity", "Rating", "Latest Chapter", "Alphabetical"
    };
    private static String[] orderV = new String[]{
            "", "?rating", "?latest", "?az"
    };
    /*private static String[] order = new String[]{
            "Views", "Rating", "Latest Chapter", "Manga Title", "Chapters"
    };
    private static String[] orderV = new String[]{
            "&sort=views", "&sort=rating", "&sort=last_chapter_time", "&sort=name&order=az", "&sort=total_chapters&order=za"
    };*/

    MangaFox(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.mangafox_icon);
        this.setServerName("MangaFox");
        setServerID(ServerBase.MANGAFOX);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigatorAndFlushParameters().getWithTimeout("http://mangafox.me/manga/");
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
            String data = getNavigatorAndFlushParameters().getWithTimeout((manga.getPath()));

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
        //Log.d("Mfox", "getIF url: " + this.getPagesNumber(chapter, page));
        String source = getNavigatorAndFlushParameters().getWithTimeout(this.getPagesNumber(chapter, page));
        //Log.d("Mfox", "source: " + source);
        String img = "";
        if (!source.isEmpty()) {
            try {
                //><img src="([^"]+?.(jpg|gif|jpeg|png|bmp))
                img = getFirstMatch(">[\\s]*<img src=\"(.+?)\"", source, "Error getting image link");
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Log.d("Mfox", "img: " + img);
        }
        return img;
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String source;
        source = getNavigatorAndFlushParameters().getWithTimeout(chapter.getPath());
        String paginas = getFirstMatch(PATRON_LAST, source, "Error: no se pudo obtener el numero de paginas");
        chapter.setPages(Integer.parseInt(paginas)); //last page is for comments
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigatorAndFlushParameters()
                .getWithTimeout("http://mangafox.me/search.php?name_method=cw&name="
                        + term
                        + "&type=&author_method=cw&author=&artist_method=cw&artist=&genres%5BAction%5D=0&genres%5BAdult%5D=0&genres%5BAdventure%5D=0&genres%5BComedy%5D=0&genres%5BDoujinshi%5D=0&genres%5BDrama%5D=0&genres%5BEcchi%5D=0&genres%5BFantasy%5D=0&genres%5BGender+Bender%5D=0&genres%5BHarem%5D=0&genres%5BHistorical%5D=0&genres%5BHorror%5D=0&genres%5BJosei%5D=0&genres%5BMartial+Arts%5D=0&genres%5BMature%5D=0&genres%5BMecha%5D=0&genres%5BMystery%5D=0&genres%5BOne+Shot%5D=0&genres%5BPsychological%5D=0&genres%5BRomance%5D=0&genres%5BSchool+Life%5D=0&genres%5BSci-fi%5D=0&genres%5BSeinen%5D=0&genres%5BShoujo%5D=0&genres%5BShoujo+Ai%5D=0&genres%5BShounen%5D=0&genres%5BShounen+Ai%5D=0&genres%5BSlice+of+Life%5D=0&genres%5BSmut%5D=0&genres%5BSports%5D=0&genres%5BSupernatural%5D=0&genres%5BTragedy%5D=0&genres%5BWebtoons%5D=0&genres%5BYaoi%5D=0&genres%5BYuri%5D=0&released_method=eq&released=&rating_method=eq&rating=&is_completed=&advopts=1");
        Pattern p = Pattern.compile("<td><a href=\"(http://mangafox.me/manga/.+?)\".+?\">(.+?)<");
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(2).trim(), m.group(1), false));
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web;
        if (genre[filters[0][0]].equals("All")) {
            if (pageNumber == 1)
                web = HOST + genreVV + orderV[filters[1][0]];
            else
                web = HOST + genreVV + pageNumber + ".htm" + orderV[filters[1][0]];
        } else
            web = HOST + genreVV + genre[filters[0][0]].toLowerCase().replaceAll(" ", "-") + "/" + pageNumber + ".htm" + orderV[filters[1][0]];
        //Log.d("Mfox","web: "+web);
        String source = getNavigatorAndFlushParameters().getWithTimeout(web);
        Pattern p = Pattern.compile("<img src=\"(http://h\\.mfcdn\\.net/store/manga/.+?)\".+?<a class=\"title\" href=\"(.+?)\" rel=\"\\d+\">(.+?)</a>");
        Matcher m = p.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        //Log.d("Mfox","prematch");
        while (m.find()) {
            /*Log.d("Mfox","(1): "+m.group(1));
            Log.d("Mfox","(2): "+m.group(2));
            Log.d("Mfox","(3): "+m.group(3));*/
            Manga manga = new Manga(getServerID(), m.group(3), m.group(2), false);
            manga.setImages(m.group(1));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{//new ServerFilter("Type", type, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Genres", genre, ServerFilter.FilterType.SINGLE),
                //new ServerFilter("Completed Series", status, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Order", order, ServerFilter.FilterType.SINGLE)};
    }
}
