package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;

/**
 * Created by Raul on 04/12/2015.
 */
public class MyMangaIo extends ServerBase {

    private static final String[] genre = {
            "Josei", "Seinen", "Shojo", "Shonen"
    };

    private static final String[] genreV = {
            "&genre%5B4%5D=1", "&genre%5B2%5D=1", "&genre%5B3%5D=1", "&genre%5B1%5D=1"
    };
    private static final String[] type = {
            "Doujinshi", "Magazine", "Manfra", "Manga", "Manhua", "Manhwa"
    };
    private static final String[] typeV = {
            "&type%5B3%5D=1", "&type%5B5%5D=1", "&type%5B6%5D=1", "&type%5B1%5D=1", "&type%5B4%5D=1", "&type%5B2%5D=1",
    };
    private static final String[] statut = {
            "Abandonné", "En cours", "One shot", "Terminé"
    };
    private static final String[] statutV = {
            "&statut%5B4%5D=1", "&statut%5B3%5D=1", "&statut%5B1%5D=1", "&statut%5B2%5D=1"
    };
    private static final String[] subGenre = {
            "Action", "Adulte", "Arts martiaux", "Aventure", "Comédie", "Drame", "Ecchi",
            "Fantaisie", "Harem", "Historique", "Horreur", "Lolicon", "Mature", "Mecha",
            "Mystère", "Pervers", "Psychologique", "Romance", "Science fiction", "Shotacon",
            "Sports", "Surnaturel", "Tragédie", "Tranche de vie", "Travelo", "Vie scolaire",
            "Yaoi", "Yuri"
    };
    private static final String[] subGenreV = {
            "&subgenre%5B4%5D=1", "&subgenre%5B5%5D=1", "&subgenre%5B23%5D=1", "&subgenre%5B3%5D=1",
            "&subgenre%5B10%5D=1", "&subgenre%5B12%5D=1", "&subgenre%5B2%5D=1", "&subgenre%5B20%5D=1",
            "&subgenre%5B25%5D=1", "&subgenre%5B19%5D=1", "&subgenre%5B6%5D=1", "&subgenre%5B30%5D=1",
            "&subgenre%5B26%5D=1", "&subgenre%5B15%5D=1", "&subgenre%5B22%5D=1", "&subgenre%5B8%5D=1",
            "&subgenre%5B13%5D=1", "&subgenre%5B1%5D=1", "&subgenre%5B21%5D=1", "&subgenre%5B29%5D=1",
            "&subgenre%5B17%5D=1", "&subgenre%5B9%5D=1", "&subgenre%5B18%5D=1", "&subgenre%5B16%5D=1",
            "&subgenre%5B27%5D=1", "&subgenre%5B11%5D=1", "&subgenre%5B28%5D=1", "&subgenre%5B24%5D=1"
    };
    private static String HOST = "http://www.mymanga.io/";
    private static String[] orden = {
            "Alphabétique"
    };

    public MyMangaIo(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_fr);
        this.setIcon(R.drawable.mymanga);
        this.setServerName("MyMangaIo");
        setServerID(ServerBase.MYMANGAIO);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return getMangasFiltered(getBasicFilter(), 0);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().size() == 0 || forceReload) {
            String data = getNavigatorWithNeededHeader().get(manga.getPath());
            // Front
            manga.setImages("http://www.mymanga.io/" + getFirstMatchDefault("<img src=\"(images/mangas_thumb/.+?)\"", data, ""));
            // Summary
            manga.setSynopsis(getFirstMatchDefault("Synopsis</h1>\\s+<p>(.+?)</p>", data, defaultSynopsis).replaceAll("<.+?>", ""));
            // Status
            manga.setFinished(!data.contains("en cours</a>"));
            // Author
            manga.setAuthor(Util.getInstance().fromHtml(getFirstMatchDefault("Auteur\\s*:\\s*(.+?)</tr>", data, "")).toString());
            // Genre
            manga.setGenre(Util.getInstance().fromHtml(getFirstMatchDefault("Genre\\s*:\\s*(.+?)</tr>", data, "")).toString());
            // Chapter
            Pattern p = Pattern.compile("href=\"([^\"]+)\"[^>]+title=\"Li.+?<span class=\"chapter\">(.+?)<");
            Matcher m = p.matcher(data);
            while (m.find()) {
                /*Log.d("MyMIO", "1: " + m.group(1));
                Log.d("MyMIO", "2: " + m.group(2));*/
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
        return chapter.getPath() + page;

    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String data;
        data = getNavigatorWithNeededHeader().get(this.getPagesNumber(chapter, page));
        return getFirstMatch("<img src=\"(.+?)\"", data, "Error: Could not get the link to the image");
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String source = getNavigatorWithNeededHeader().get(chapter.getPath());
        /*Log.d("MYIO", "web: " + chapter.getPath());
        Log.d("MYIO", "source: " + source);*/
        String pages = getFirstMatch("<span>sur (\\d+)</span>", source, "Error: Could not get the number of pages"); //(\d+)</option></select></span>
        chapter.setPages(Integer.parseInt(pages));
    }

    public ArrayList<Manga> getMangasFromSource(String source) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("<a href=\"(mangas/[^\"]+?)\">(.+?)<");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(getServerID(), Util.getInstance().fromHtml(m.group(2)).toString(), HOST + m.group(1), false);
            manga.setImages("http://www.mymanga.io/images/mangas_thumb/" + getFirstMatchDefault("mangas/(.+?)/", manga.getPath(), "") + ".jpg");
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String web = "http://www.mymanga.io/search?name=" + URLEncoder.encode(term, "UTF-8");
        String data = getNavigatorWithNeededHeader().get(web);
        return getMangasFromSource(data);
    }


    //http://www.mymanga.io/search?name=&author=&illustrator=&parution_span=0&parution=
    // &type%5B3%5D=0&type%5B5%5D=0&type%5B6%5D=0&type%5B1%5D=0&type%5B4%5D=0&type%5B2%5D=0
    // &statut%5B4%5D=1&statut%5B3%5D=0&statut%5B1%5D=0&statut%5B2%5D=0
    // &genre%5B4%5D=0&genre%5B2%5D=1&genre%5B3%5D=0&genre%5B1%5D=0
    // &subgenre%5B4%5D=0&subgenre%5B5%5D=0&subgenre%5B23%5D=0&subgenre%5B3%5D=0&subgenre%5B10%5D=0&subgenre%5B12%5D=0&subgenre%5B2%5D=0&subgenre%5B20%5D=0&subgenre%5B25%5D=1&subgenre%5B19%5D=0&subgenre%5B6%5D=0&subgenre%5B30%5D=0&subgenre%5B26%5D=0&subgenre%5B15%5D=0&subgenre%5B22%5D=0&subgenre%5B8%5D=0&subgenre%5B13%5D=0&subgenre%5B1%5D=0&subgenre%5B21%5D=0&subgenre%5B29%5D=0&subgenre%5B17%5D=0&subgenre%5B9%5D=0&subgenre%5B18%5D=0&subgenre%5B16%5D=0&subgenre%5B27%5D=0&subgenre%5B11%5D=0&subgenre%5B28%5D=0&subgenre%5B24%5D=0
    // &chapter_span=0&chapter_count=&last_update=&like_span=0&like=&dislike_span=0&dislike=&search=Rechercher

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = "http://www.mymanga.io/search?name=&author=&illustrator=&parution_span=0&parution=";
        for (int i = 0; i < filters[0].length; i++) {
            web = web + typeV[filters[0][i]];
        }
        for (int i = 0; i < filters[1].length; i++) {
            web = web + statutV[filters[1][i]];
        }
        for (int i = 0; i < filters[2].length; i++) {
            web = web + genreV[filters[2][i]];
        }
        for (int i = 0; i < filters[3].length; i++) {
            web = web + subGenreV[filters[3][i]];
        }
        Navigator nav = getNavigatorWithNeededHeader();
        web = web + "&chapter_span=0&chapter_count=&last_update=&like_span=0&like=&dislike_span=0&dislike=&search=Rechercher";
        String source = nav.get(web);
        return getMangasFromSource(source);
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter("Type", type, ServerFilter.FilterType.MULTI),
                new ServerFilter("Statut", statut, ServerFilter.FilterType.MULTI),
                new ServerFilter("Genre", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Sous-Genres", subGenre, ServerFilter.FilterType.MULTI)
        };
    }

    public Navigator getNavigatorWithNeededHeader() throws Exception {
        Navigator nav = new Navigator(context);
        nav.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        return nav;
    }

}
