package ar.rulosoft.mimanganu.servers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public class MangaHere extends ServerBase {

    private static final String[] genre = {
            "All", "Action", "Adventure", "Comedy", "Doujinshi", "Drama",
            "Ecchi", "Fantasy", "Gender Bender", "Harem", "Historical",
            "Horror", "Josei", "Martial Arts", "Mature", "Mecha", "Mystery",
            "One Shot", "Psychological", "Romance", "School Life", "Sci-fi",
            "Seinen", "Shoujo", "Shoujo Ai", "Shounen", "Slice of Life",
            "Sports", "Supernatural", "Tragedy", "Yuri"
    };
    private static final String[] genreV = {
            "directory", "action", "adventure", "comedy", "doujinshi", "Drama",
            "ecchi", "fantasy", "gender_bender", "harem", "historical",
            "horror", "josei", "martial_arts", "mature", "mecha", "mystery",
            "one_shot", "psychological", "romance", "school_life", "sci-fi",
            "seinen", "shoujo", "shoujo Ai", "shounen", "slice_of_life",
            "sports", "supernatural", "tragedy", "yuri"
    };
    private static final String PATRON_CAPS_VIS =
            "<img src=\"(.+?)\".+?alt=\"(.+?)\".+?<a href=\"(.+?)\"";
    private static final String PATTERN_SERIE =
            "<li><a class=\"manga_info\" rel=\"([^\"]*)\" href=\"([^\"]*)\"><span>[^<]*</span>([^<]*)</a></li>";
    private static final String PATRON_PORTADA = "<img src=\"(.+?cover.+?)\"";
    private static final String PATRON_SINOPSIS =
            "<p id=\"show\" style=\"display:none;\">(.+?)&nbsp;<a";
    private static final String PATTERN_CAPITULOS =
            "<li>[^<]*<span class=\"left\">[^<]*<a class=\"color_0077\" href=\"([^\"]*)\"[^>]*>([^<]*)</a>";
    private static final String PATRON_LAST = ">(\\d+)</option>[^<]+?</select>";
    private static final String PATRON_IMAGEN = "src=\"([^\"]+?/manga/.+?.(jpg|gif|jpeg|png|bmp).*?)\"";
    private static String HOST = "http://www.mangahere.co";
    private static String[] orden = {
            "Views", "A - Z", "Rating", "Last Update"
    };
    private static String[] ordenM = {
            "?views.za", "?name.az", "?rating.za", "?last_chapter_time.az"
    };

    public MangaHere() {
        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.mangahere_icon);
        this.setServerName("MangaHere");
        setServerID(ServerBase.MANGAHERE);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = new Navegador().get(HOST + "/mangalist/");
        Pattern p = Pattern.compile(PATTERN_SERIE);
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(ServerBase.ESMANGAHERE, m.group(1), m.group(2), false));
        }
        return mangas;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().size() == 0 || forceReload) {
            String data = new Navegador().get(manga.getPath());
            // Front
            manga.setImages(getFirstMatchDefault(PATRON_PORTADA, data, ""));
            // Summary
            manga.setSynopsis(getFirstMatchDefault(PATRON_SINOPSIS, data, "Without synopsis."));
            // Status
            manga.setFinished(data.contains("</label>Completed</li>"));
            // Author
            manga.setAuthor(getFirstMatchDefault("Author.+?\">(.+?)<", data, ""));
            // Genre
            manga.setGenre(getFirstMatchDefault("<li><label>Genre\\(s\\):</label>(.+?)</li>", data, ""));
            // Chapter
            Pattern p = Pattern.compile(PATTERN_CAPITULOS);
            Matcher m = p.matcher(data);

            while (m.find()) {
                Chapter mc = new Chapter(m.group(2).trim(), m.group(1));
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
        return chapter.getPath() + page + ".html";

    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String data;
        data = new Navegador().get(this.getPagesNumber(chapter, page));
        return getFirstMatch(PATRON_IMAGEN, data, "Error: Could not get the link to the image");
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data;
        data = new Navegador().get(chapter.getPath());
        String paginas =
                getFirstMatch(PATRON_LAST, data, "Error: Could not get the number of pages");
        chapter.setPages(Integer.parseInt(paginas));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String web = HOST + "/" + genreV[categorie] + "/" + pageNumber + ".htm" + ordenM[order];
        String data = new Navegador().get(web);
        Pattern p = Pattern.compile(PATRON_CAPS_VIS);
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga =
                    new Manga(getServerID(), m.group(2), m.group(3), false);
            manga.setImages(m.group(1).replace("thumb_",""));
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
        return orden;
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        Navegador nav = new Navegador();
        String data = nav.get(HOST + "/search.php?name=" + term);
        Pattern p = Pattern.compile("<dt>				<a href=\"(" + HOST +
                "/manga/.+?)\".+?\">(.+?)<");
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(2).trim(), m.group(1), false));
        }
        return mangas;
    }
}
