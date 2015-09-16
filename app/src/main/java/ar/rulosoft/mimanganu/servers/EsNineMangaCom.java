package ar.rulosoft.mimanganu.servers;

import android.text.Html;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;

public class EsNineMangaCom extends ServerBase {
    public static String HOST = "http://es.ninemanga.com";
    public static String[] generos = new String[]{
            "Todo", "Acción", "Action", "Adult", "Adventure", "Artes Marciales", "Aventura",
            "Ciencia Ficción", "Comedia", "Comedy", "Deporte", "Deportes", "Drama", "Ecchi",
            "Escolar", "Fantasía", "Fantasy", "Gender Bender", "Genial", "Harem", "Historical",
            "HistóRico", "Horror", "Josei", "Maduro", "Martial", "Martial Arts", "Mecha",
            "Misterio", "Mystery", "None", "One Shot", "Oneshot", "Parodia", "Psicológico",
            "Psychological", "Romance", "School Life", "Sci-Fi", "Seinen", "Shojo", "Shojo Ai",
            "Shonen", "Shoujo", "Shoujo Ai", "Shounen", "Shounen Ai", "Slice Of Life", "Smut",
            "Sobrenatural", "Sports", "Supernatural", "Tragedia", "Tragedy", "Vida Cotidiana",
            "Webcomic", "Yuri"
    };
    public static String[] generosV = new String[]{
            "index_.html", "Acci%C3%B3n_.html", "Action_.html", "Adult_.html", "Adventure_.html",
            "Artes+Marciales_.html", "Aventura_.html", "Ciencia+Ficci%C3%B3n_.html", "Comedia_.html",
            "Comedy_.html", "Deporte_.html", "Deportes_.html", "Drama_.html", "Ecchi_.html",
            "Escolar_.html", "Fantas%C3%ADa_.html", "Fantasy_.html", "Gender+Bender_.html",
            "Genial_.html", "Harem_.html", "Historical_.html", "Hist%C3%B3rico_.html",
            "Horror_.html", "Josei_.html", "Maduro_.html", "Martial_.html", "Martial+Arts_.html",
            "Mecha_.html", "Misterio_.html", "Mystery_.html", "None_.html", "One+Shot_.html",
            "Oneshot_.html", "Parodia_.html", "Psicol%C3%B3gico_.html", "Psychological_.html",
            "Romance_.html", "School+Life_.html", "Sci-fi_.html", "Seinen_.html", "Shojo_.html",
            "Shojo+Ai_.html", "Shonen_.html", "Shoujo_.html", "Shoujo+Ai_.html",
            "Shounen_.html", "Shounen+Ai_.html", "Slice+Of+Life_.html", "Smut_.html",
            "Sobrenatural_.html", "Sports_.html", "Supernatural_.html", "Tragedia_.html",
            "Tragedy_.html", "Vida+Cotidiana_.html", "Webcomic_.html", "Yuri_.html"
    };
    static String[] order = new String[]{
            "/category/", "/list/New-Update/", "/list/Hot-Book/", "/list/New-Book/"
    };

    public EsNineMangaCom() {
        this.setFlag(R.drawable.flag_esp);
        this.setIcon(R.drawable.esninemanga);
        this.setServerName("EsNineManga");
        setServerID(ServerBase.ESNINEMANGA);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String source = getNavWithHeader().get(
                HOST + "/search/?wd=" + URLEncoder.encode(term, "UTF-8"));
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("bookname\" href=\"(/manga/[^\"]+)\">(.+?)<");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(ESNINEMANGA, m.group(2), HOST + m.group(1), false);
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public void loadChapters(Manga m, boolean forceReload) throws Exception {
        if (m.getChapters() == null || m.getChapters().size() == 0 || forceReload)
            loadMangaInformation(m, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga m, boolean forceReload) throws Exception {
        String source = getNavWithHeader().get(m.getPath() + "?waring=1");
        // portada
        m.setImages(getFirstMatchDefault("Manga\" src=\"(.+?)\"", source, ""));
        // sinopsis
        String sinopsis = getFirstMatchDefault("<p itemprop=\"description\">(.+?)&nbsp;Show less",
                source, "Sin sinopsis").replaceAll("<.+?>", "");
        m.setSynopsis(Html.fromHtml(sinopsis).toString());
        // estado
        m.setFinished(getFirstMatchDefault("Estado(.+?)</a>", source, "").contains("Completado"));
        // autor
        m.setAuthor(getFirstMatchDefault("Autor.+?\">(.+?)<", source, ""));
        // capitulos
        Pattern p = Pattern.compile(
                "<a class=\"chapter_list_a\" href=\"(/chapter.+?)\" title=\"(.+?)\">(.+?)</a>");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(3), HOST + matcher.group(1)));
        }
        m.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter c, int page) {
        return c.getPath().replace(".html", "-" + page + ".html");
    }

    @Override
    public String getImageFrom(Chapter c, int page) throws Exception {
        if (c.getExtra() == null)
            setExtra(c);
        String[] imagenes = c.getExtra().split("\\|");
        return imagenes[page];
    }

    public void setExtra(Chapter c) throws Exception {
        String source = getNavWithHeader().get(
                c.getPath().replace(".html", "-" + c.getPages() + "-1.html"));
        Pattern p = Pattern.compile("<img class=\"manga_pic.+?src=\"([^\"]+)");
        Matcher m = p.matcher(source);
        String imagenes = "";
        while (m.find()) {
            imagenes = imagenes + "|" + m.group(1);
        }
        c.setExtra(imagenes);
    }

    @Override
    public void chapterInit(Chapter c) throws Exception {
        String source = getNavWithHeader().get(c.getPath());
        String nop = getFirstMatch(
                "\\d+/(\\d+)</option>[\\s]*</select>", source,
                "Error al obtener el n�mero de p�ginas");
        c.setPages(Integer.parseInt(nop));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception {
        String source = getNavWithHeader().get(
                HOST + EsNineMangaCom.order[order] +
                        generosV[categorie].replace("_", "_" + pageNumber));
        return getMangasFromSource(source);
    }

    public ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile(
                "<a href=\"(/manga/[^\"]+)\"><img src=\"(.+?)\".+?alt=\"([^\"]+)\"");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(ESNINEMANGA, m.group(3), HOST + m.group(1), false);
            manga.setImages(m.group(2));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public String[] getCategories() {
        return generos;
    }

    @Override
    public String[] getOrders() {
        // "/category/", "/list/New-Update/", "/list/Hot-Book", "/list/New-Book/"
        return new String[]{"Lista de Manga", "Recientes", "Popular", "Manga Nueva"};
    }

    @Override
    public boolean hasList() {
        return false;
    }
}
