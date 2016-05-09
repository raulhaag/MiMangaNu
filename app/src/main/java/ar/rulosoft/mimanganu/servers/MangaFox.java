package ar.rulosoft.mimanganu.servers;

import android.text.Html;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public class MangaFox extends ServerBase {

    private static final String[] genre = {
            "All", "Action", "Adult", "Adventure", "Comedy", "Doujinshi", "Drama", "Ecchi",
            "Fantasy", "Gender Bender", "Harem", "Historical", "Horror", "Josei", "Martial Arts",
            "Mecha", "Mystery", "One Shot", "Psychological", "Romance", "School Life", "Sci-fi",
            "Seinen", "Shoujo", "Shoujo Ai", "Shounen", "Slice of Life", "Smut", "Sports",
            "Supernatural", "Tragedy", "Webtoons", "Yuri"
    };
    private static final String[] genreV = {
            "directory", "action", "adult", "adventure", "comedy", "doujinshi", "Drama", "ecchi",
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
    private static String[] orden = {"Popularity", "A - Z", "Rating", "Last Update"};
    private static String[] ordenM = {"", "?az", "?rating", "?latest"};

    public MangaFox() {
        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.mangafox_icon);
        this.setServerName("MangaFox");
        setServerID(ServerBase.MANGAFOX);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = new Navegador().get("http://mangafox.me/manga/");
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
            String data = new Navegador().get((manga.getPath()));

            // Title
            manga.setImages(getFirstMatchDefault(PATRON_PORTADA, data, ""));
            // Summary
            manga.setSynopsis(getFirstMatchDefault(PATRON_SINOPSIS, data, "Without synopsis."));

            manga.setFinished(data.contains("<h\\d>Status:</h\\d>    <span>        Completed"));

            // Author
            manga.setAuthor(getFirstMatchDefault("\"/search/author/.+?>(.+?)<", data, ""));

            // Genre
            manga.setGenre(Html.fromHtml(getFirstMatchDefault("(<a href=\"http://mangafox.me/search/genres/.+?</td>)", data, "")).toString());

            // Chapter
            p = Pattern.compile(PATTERN_CAPITULOS);
            m = p.matcher(data);

            while (m.find()) {
                Chapter mc;
                if (m.group(4) != null)
                    mc = new Chapter(m.group(2).trim() + ": " + m.group(4), m.group(1));
                else
                    mc = new Chapter(m.group(2).trim(), m.group(1));
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
        data = new Navegador().get(this.getPagesNumber(chapter, page));
        return getFirstMatch(PATRON_IMAGEN, data, "Error: no se pudo obtener el enlace a la imagen");
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data;
        data = new Navegador().get(chapter.getPath());
        String paginas = getFirstMatch(PATRON_LAST, data, "Error: no se pudo obtener el numero de paginas");
        chapter.setPages(Integer.parseInt(paginas)-1);//last page is for comments
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String web = "http://mangafox.me/" + genreV[categorie] + "/" + pageNumber + ".htm" + ordenM[order];
        String data = new Navegador().get(web);
        Pattern p = Pattern.compile(PATRON_CAPS_VIS);
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(4), m.group(1), false);
            manga.setImages(m.group(2));
            if (m.group(3).length() > 6) {
                manga.setFinished(true);
            }
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
        String data = nav
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

}
