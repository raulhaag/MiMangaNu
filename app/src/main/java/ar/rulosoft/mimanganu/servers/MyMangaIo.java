package ar.rulosoft.mimanganu.servers;

import android.text.Html;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

/**
 * Created by Raul on 04/12/2015.
 */
public class MyMangaIo extends ServerBase {

    private static final String[] genre = {
            "Tous", "Josei", "Seinen", "Shojo", "Shonen", "Action", "Adulte", "Arts martiaux", "Aventure", "Comédie", "Drame", "Ecchi", "Fantaisie", "Harem", "Historique", "Horreur", "Lolicon", "Mature", "Mecha", "Mystère", "Pervers", "Psychologique", "Romance", "Science fiction", "Shotacon", "Sports", "Surnaturel", "Tragédie", "Tranche de vie", "Travelo", "Vie scolaire", "Yaoi", "Yuri"
    };

    private static final String[] genreV = {
            "", "genre%5B4%5D=1", "genre%5B2%5D=1", "genre%5B3%5D=1", "genre%5B1%5D=1", "subgenre%5B5%5D=1", "subgenre%5B23%5D=1", "subgenre%5B3%5D=1", "subgenre%5B10%5D=1", "subgenre%5B12%5D=1", "subgenre%5B2%5D=1", "subgenre%5B20%5D=1", "subgenre%5B25%5D=1", "subgenre%5B19%5D=1", "subgenre%5B6%5D=1", "subgenre%5B30%5D=1", "subgenre%5B26%5D=1", "subgenre%5B15%5D=1", "subgenre%5B22%5D=1", "subgenre%5B8%5D=1", "subgenre%5B13%5D=1", "subgenre%5B1%5D=1", "subgenre%5B21%5D=1", "subgenre%5B29%5D=1", "subgenre%5B17%5D=1", "subgenre%5B9%5D=1", "subgenre%5B18%5D=1", "subgenre%5B16%5D=1", "subgenre%5B27%5D=1", "subgenre%5B11%5D=1", "subgenre%5B28%5D=1", "subgenre%5B24%5D=1"};

    private static String HOST = "http://www.mymanga.io/";

    private static String[] orden = {
            "Alphabétique"
    };


    public MyMangaIo() {
        this.setFlag(R.drawable.flag_fr);
        this.setIcon(R.drawable.mymanga);
        this.setServerName("MyMangaIo");
        setServerID(ServerBase.MYMANGAIO);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return getMangasFiltered(0, 0, 0);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().size() == 0 || forceReload) {
            String data = new Navegador().get(manga.getPath());
            // Front
            manga.setImages("http://www.mymanga.io/" + getFirstMatchDefault("<img src=\"(images/mangas_thumb/.+?)\"", data, ""));
            // Summary
            manga.setSynopsis(getFirstMatchDefault("Synopsis</h1><p>(.+?)</p>", data, "Without synopsis.").replaceAll("<.+?>", ""));
            // Status
            manga.setFinished(!data.contains("en cours</a>"));
            // Author
            manga.setAuthor(Html.fromHtml(getFirstMatchDefault("Auteur\\s*:\\s*(.+?)</tr>", data, "")).toString());
            // Genre
            manga.setGenre(Html.fromHtml(getFirstMatchDefault("Genre\\s*:\\s*(.+?)</tr>", data, "")).toString());
            // Chapter
            Pattern p = Pattern.compile("<div class=clearfix>.+?<a href=\"(http://www.mymanga.io/[^\"]+)\".+?chapter>(.+?)<");
            Matcher m = p.matcher(data);

            while (m.find()) {
                Chapter mc = new Chapter(m.group(2).trim(), m.group(1).replace("http://www.mymanga.io/mangas/", "http://www.topmanga.eu/"));
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
        data = new Navegador().get(this.getPagesNumber(chapter, page));
        return getFirstMatch("<img src=\"(.+?)\"", data, "Error: Could not get the link to the image");
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data;
        data = getNavWithHeader().get(chapter.getPath());
        String pages =
                getFirstMatch("(\\d+)</option></select></span>", data, "Error: Could not get the number of pages");
        chapter.setPages(Integer.parseInt(pages));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception {
        if (categorie == 0) {
            String data = getNavWithHeader().get("http://www.mymanga.io/mangas/");
            return getMangasFromSource(data);
        } else {
            String web = "http://www.mymanga.io/search?" + genreV[categorie];
            String data = getNavWithHeader().get(web);
            return getMangasFromSource(data);
        }
    }

    public ArrayList<Manga> getMangasFromSource(String source) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("<a href=\"(mangas/[^\"]+?)\">(.+?)<");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga =
                    new Manga(getServerID(), m.group(2), HOST + m.group(1), false);
            manga.setImages("http://www.mymanga.io/images/mangas_thumb/" + getFirstMatchDefault("mangas/(.+?)/", manga.getPath(), "") + ".jpg");
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
        return orden;
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String web = "http://www.mymanga.io/search?name=" + URLEncoder.encode(term, "UTF-8");
        String data = getNavWithHeader().get(web);
        return getMangasFromSource(data);
    }
}
