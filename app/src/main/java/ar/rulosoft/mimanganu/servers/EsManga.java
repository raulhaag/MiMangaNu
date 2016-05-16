package ar.rulosoft.mimanganu.servers;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;
@Deprecated
public class EsManga extends ServerBase {

    private static String[] generos = new String[]{
            "Todos", "Acción", "Artes Marciales", "Aventura", "Ciencia Ficción", "Comedia",
            "Deportes", "Drama", "Ecchi", "Escolar", "Fantasía", "Harem", "Hentai", "Histórico",
            "Horror", "Josei", "Mecha", "Misterio", "Oneshot", "Psicológico", "Romance",
            "Seinen", "Shojo", "Shounen", "Sobrenatural", "Tragedia", "Vida Cotidiana", "Yuri"
    };
    private static String[] generosV = new String[]{
            "/lista-mangas/orden/vistas", "/genero/accion", "/genero/artes-marciales",
            "/genero/aventura", "/genero/ciencia-ficcion", "/genero/comedia", "/genero/deportes",
            "/genero/drama", "/genero/ecchi", "/genero/escolar", "/genero/fantasia",
            "/genero/harem", "/genero/hentai", "/genero/historico", "/genero/horror",
            "/genero/josei", "/genero/mecha", "/genero/misterio", "/genero/oneshot",
            "/genero/psicologico", "/genero/romance", "/genero/seinen", "/genero/shojo",
            "/genero/shounen", "/genero/sobrenatural", "/genero/tragedia",
            "/genero/vida-cotidiana", "/genero/yuri"
    };

    public EsManga() {
        this.setFlag(R.drawable.flag_es);
        this.setIcon(R.drawable.esmanga);
        this.setServerName("EsManga");
        setServerID(ServerBase.ESMANGA);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        String source = new Navegador().get("http://esmanga.com");
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern pre = Pattern.compile("<div class=\"blk-hd\"><span>Todas las series Manga</span></div>[\\s\\S]+");
        Matcher preMatcher = pre.matcher(source);
        if (preMatcher.find()) {
            source = preMatcher.group();
        }
        Pattern p = Pattern.compile("<li>[^<]+<article>[\\S\\s]+?<h2><a href=\"(.+?)\">(.+?)<");
        Matcher m = p.matcher(source);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(2), m.group(1), false));
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String web = "http://esmanga.com/search/results?q=" + URLEncoder.encode(term, "UTF-8");
        return getMangasWeb(web);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 || forceReload)
            loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        Navegador nav = new Navegador();
        String source = nav.get(manga.getPath());
        // sinopsis
        manga.setSynopsis(getFirstMatchDefault("<b>Sinopsis</b><br>([\\s\\S]+?)</s",
                source, "Sin Sinopsis").replaceAll("<.+?>", ""));
        // imagen
        manga.setImages(getFirstMatchDefault("(http://esmanga.com/img/mangas/.+?)\"", source, ""));
        // status
        manga.setFinished(getFirstMatchDefault("<b>Estado:(.+?)</span>", source, "").contains("Finalizado"));
        // capitulos
        ArrayList<Chapter> chapters = new ArrayList<>();
        Pattern p = Pattern.compile("<a href=\"(http://esmanga.com/[^\"]+/c\\d+)\">(.+?)</a><");
        Matcher matcher = p.matcher(source);
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(2).trim(), matcher.group(1)));
        }
        manga.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return chapter.getPath() + "/" + page;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        Navegador nav = new Navegador();
        String source = nav.get(this.getPagesNumber(chapter, page));
        return getFirstMatch("src=\"([^\"]+\\d.(jpg|png|bmp))", source, "Error en plugin (obtener imager)");
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        Navegador nav = new Navegador();
        String source = nav.get(chapter.getPath());
        String textNum = getFirstMatch("option value=\"(\\d+)[^=]+</option></select>",
                source, "Error en plugin (obtener p�ginas)");
        chapter.setPages(Integer.parseInt(textNum));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception {
        String web = "http://esmanga.com" + generosV[categorie] + "?page=" + pageNumber;
        return getMangasWeb(web);
    }

    private ArrayList<Manga> getMangasWeb(String web) throws Exception {
        String source = new Navegador().get(web);
        Pattern p = Pattern.compile("src=\"([^\"]+)\".+?<a href=\"(http://esmanga.com/manga/.+?)\">(.+?)<");
        Matcher matcher = p.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            Manga manga = new Manga(getServerID(), matcher.group(3), matcher.group(2), false);
            manga.setImages(matcher.group(1));
            mangas.add(0, manga);
        }
        return mangas;
    }

    @Override
    public String[] getCategories() {
        return generos;
    }

    @Override
    public String[] getOrders() {
        return new String[]{"Lecturas"};//, "Ranking"
    }

    @Override
    public boolean hasList() {
        return true;
    }


}
