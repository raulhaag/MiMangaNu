package ar.rulosoft.mimanganu.servers;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public class EsNineMangaCom extends ServerBase {

    public static String[] generos = new String[]{"Acción", "Action", "Adult", "Adventure", "Artes Marciales", "Aventura", "Ciencia Ficción", "Comedia",
            "Comedy", "Deporte", "Deportes", "Drama", "Ecchi", "Escolar", "Fantasía", "Fantasy", "Gender Bender", "Genial", "Harem", "Historical", "HistóRico",
            "Horror", "Josei", "Maduro", "Martial", "Martial Arts", "Mecha", "Misterio", "Mystery", "None", "One Shot", "Oneshot", "Parodia", "Psicológico",
            "Psychological", "Romance", "School Life", "Sci-Fi", "Seinen", "Shojo", "Shojo Ai", "Shonen", "Shoujo", "Shoujo Ai", "Shounen", "Shounen Ai",
            "Slice Of Life", "Smut", "Sobrenatural", "Sports", "Supernatural", "Tragedia", "Tragedy", "Vida Cotidiana", "Webcomic", "Yuri"};
    public static String[] generosV = new String[]{"http://es.ninemanga.com/category/Acci%C3%B3n_.html", "http://es.ninemanga.com/category/Action_.html",
            "http://es.ninemanga.com/category/Adult_.html", "http://es.ninemanga.com/category/Adventure_.html",
            "http://es.ninemanga.com/category/Artes+Marciales_.html", "http://es.ninemanga.com/category/Aventura_.html",
            "http://es.ninemanga.com/category/Ciencia+Ficci%C3%B3n_.html", "http://es.ninemanga.com/category/Comedia_.html",
            "http://es.ninemanga.com/category/Comedy_.html", "http://es.ninemanga.com/category/Deporte_.html",
            "http://es.ninemanga.com/category/Deportes_.html", "http://es.ninemanga.com/category/Drama_.html", "http://es.ninemanga.com/category/Ecchi_.html",
            "http://es.ninemanga.com/category/Escolar_.html", "http://es.ninemanga.com/category/Fantas%C3%ADa_.html",
            "http://es.ninemanga.com/category/Fantasy_.html", "http://es.ninemanga.com/category/Gender+Bender_.html",
            "http://es.ninemanga.com/category/Genial_.html", "http://es.ninemanga.com/category/Harem_.html",
            "http://es.ninemanga.com/category/Historical_.html", "http://es.ninemanga.com/category/Hist%C3%B3rico_.html",
            "http://es.ninemanga.com/category/Horror_.html", "http://es.ninemanga.com/category/Josei_.html", "http://es.ninemanga.com/category/Maduro_.html",
            "http://es.ninemanga.com/category/Martial_.html", "http://es.ninemanga.com/category/Martial+Arts_.html",
            "http://es.ninemanga.com/category/Mecha_.html", "http://es.ninemanga.com/category/Misterio_.html",
            "http://es.ninemanga.com/category/Mystery_.html", "http://es.ninemanga.com/category/None_.html", "http://es.ninemanga.com/category/One+Shot_.html",
            "http://es.ninemanga.com/category/Oneshot_.html", "http://es.ninemanga.com/category/Parodia_.html",
            "http://es.ninemanga.com/category/Psicol%C3%B3gico_.html", "http://es.ninemanga.com/category/Psychological_.html",
            "http://es.ninemanga.com/category/Romance_.html", "http://es.ninemanga.com/category/School+Life_.html",
            "http://es.ninemanga.com/category/Sci-fi_.html", "http://es.ninemanga.com/category/Seinen_.html", "http://es.ninemanga.com/category/Shojo_.html",
            "http://es.ninemanga.com/category/Shojo+Ai_.html", "http://es.ninemanga.com/category/Shonen_.html",
            "http://es.ninemanga.com/category/Shoujo_.html", "http://es.ninemanga.com/category/Shoujo+Ai_.html",
            "http://es.ninemanga.com/category/Shounen_.html", "http://es.ninemanga.com/category/Shounen+Ai_.html",
            "http://es.ninemanga.com/category/Slice+Of+Life_.html", "http://es.ninemanga.com/category/Smut_.html",
            "http://es.ninemanga.com/category/Sobrenatural_.html", "http://es.ninemanga.com/category/Sports_.html",
            "http://es.ninemanga.com/category/Supernatural_.html", "http://es.ninemanga.com/category/Tragedia_.html",
            "http://es.ninemanga.com/category/Tragedy_.html", "http://es.ninemanga.com/category/Vida+Cotidiana_.html",
            "http://es.ninemanga.com/category/Webcomic_.html", "http://es.ninemanga.com/category/Yuri_.html"};

    public EsNineMangaCom() {
        this.setFlag(R.drawable.flag_esp);
        this.setIcon(R.drawable.esninemanga);
        this.setServerName("EsNineManga");
        setServerID(ServerBase.ESNINEMANGA);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String source = new Navegador().get("http://es.ninemanga.com/search/?wd=" + URLEncoder.encode(term, "UTF-8"));
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("bookname\" href=\"(/manga/[^\"]+)\">(.+?)<");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(ESNINEMANGA, m.group(2), "http://es.ninemanga.com" + m.group(1), false);
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
        String source = new Navegador().get(m.getPath() + "?waring=1");
        // portada
        String portada = getFirstMacthDefault("Manga\" src=\"(.+?)\"", source, "");
        m.setImages(portada);
        // sinopsis
        String sinopsis = getFirstMacthDefault("<p itemprop=\"description\">(.+?)&nbsp;Show less", source, "Sin sinopsis").replaceAll("<.+?>", "");
        m.setSinopsis(sinopsis);

        //estado
        m.setFinished(getFirstMacthDefault("Estado(.+?)</a>", source, "").contains("Completado"));

        //autor
        m.setAuthor(getFirstMacthDefault("Autor.+?\">(.+?)<", source, ""));

        // cap�tulos
        Pattern p = Pattern.compile("<a class=\"chapter_list_a\" href=\"(/chapter.+?)\" title=\"(.+?)\">(.+?)</a>");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(3), "http://es.ninemanga.com" + matcher.group(1)));
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
        String source = new Navegador().get(c.getPath().replace(".html", "-" + c.getPages() + "-1.html"));
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
        String source = new Navegador().get(c.getPath());
        String nop = getFirstMacth("\\d+/(\\d+)</option>[\\s]*</select>", source, "Error al obtener el n�mero de p�ginas");
        c.setPages(Integer.parseInt(nop));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception {
        String source = new Navegador().get(generosV[categorie].replace("_", "_" + pageNumber));
        return getMangasFromSource(source);
    }

    public ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("<a href=\"(/manga/[^\"]+)\"><img src=\"(.+?)\".+?alt=\"([^\"]+)\"");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(ESNINEMANGA, m.group(3), "http://es.ninemanga.com" + m.group(1), false);
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
        return new String[]{"Popularidad"};
    }

    @Override
    public boolean hasList() {
        return false;
    }

}
