package ar.rulosoft.mimanganu.servers;

import android.text.Html;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public class EsMangaHere extends ServerBase {

    private static final String PATTERN_SERIE = "<li><a class=\"manga_info\" rel=\"([^\"]*)\" href=\"([^\"]*)\"><span>[^<]*</span>([^<]*)</a></li>";

    private static final String PATRON_PORTADA = "<img src=\"(.+?cover.+?)\"";
    private static final String PATRON_SINOPSIS = "<p id=\"show\" style=\"display:none;\">(.+?)&nbsp;<a";
    private static final String PATTERN_CAPITULOS = "href=\"(/manga.+?)\".+?>(.+?)</a>";
    private static final String PATRON_SEG_CAP = "<div class=\"detail_list\">(.+?)</ul></div></div><ul";
    private static final String PATRON_CAPS_VIS = "<img src=\"(.+?)\" alt=\"(.+?)\".+?<a href=\"(.+?)\"";

    private static final String PATRON_LAST = ">(\\d+)</option>[^<]+?</select>";
    private static final String PATRON_IMAGEN = "src=\"([^\"]+?.(jpg|gif|jpeg|png|bmp).*?)\"";

    private static String[] categorias = {
            "Todo", "Acción", "Aventura", "Comedia", "Doujinshi", "Drama", "Ecchi", "Fantasía",
            "Gender Bender", "Harem", "Histórico", "Horror", "Josei", "Artes Marciales", "Maduro",
            "Mecha", "Misterio", "Oneshot", "Psicológico", "Romance", "Escolar",
            "Ciencia Ficción", "Seinen", "Shojo", "Shojo Ai", "Shounen", "Vida Cotidiana",
            "Deportes", "Sobrenatural", "Tragedia", "Yuri"
    };

    private static String[] categoriasV = {
            "directory/", "acción/", "aventura/", "comedia/", "doujinshi/", "drama/", "ecchi/",
            "fantasía/", "gender_bender/", "harem/", "histórico/", "horror/", "josei/",
            "artes_marciales/", "maduro/", "mecha/", "misterio/", "oneshot/", "psicológico/",
            "romance/", "escolar/", "ciencia_ficción/", "seinen/", "shojo/", "shojo_ai/",
            "shounen/", "vida_cotidiana/", "deportes/", "sobrenatural/", "tragedia/", "yuri/"
    };

    private static String[] orden = {
            "Lecturas", "A - Z", "Mejor Calificados", "Ultimos Actualizados"
    };
    private static String[] ordenM = {
            "?views.za", "?name.az", "?rating.za", "?last_chapter_time.az"
    };

    public EsMangaHere() {
        this.setFlag(R.drawable.flag_esp);
        this.setIcon(R.drawable.mangahere_icon);
        this.setServerName("EsMangaHere");
        setServerID(ServerBase.ESMANGAHERE);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = new Navegador().get("http://es.mangahere.co/mangalist/");
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
            Pattern p;
            Matcher m;
            String data = new Navegador().get((manga.getPath()));

            // portada
            manga.setImages(getFirstMatchDefault(PATRON_PORTADA, data, ""));

            // sinopsis
            manga.setSynopsis(getFirstMatchDefault(PATRON_SINOPSIS, data, "Sin sinopsis."));

            // estado
            manga.setFinished(getFirstMatchDefault("<li><label>Estado:</label>(.+?)</li>",
                    data, "En desarrollo").length() == 9);

            //autor
            manga.setAuthor(getFirstMatchDefault("Autor.+?\">(.+?)<", data, ""));


            //generos
            manga.setGenre((Html.fromHtml(getFirstMatchDefault("<li>[^:]+nero\\(s\\):(.+?)</li>", data, "")).toString().trim()));

            // capitulos
            data = getFirstMatch(PATRON_SEG_CAP, data, "Error al obtener lista de capítulos");
            p = Pattern.compile(PATTERN_CAPITULOS);
            m = p.matcher(data);

            while (m.find()) {
                Chapter mc = new Chapter(m.group(2).trim(), "http://es.mangahere.co" + m.group(1));
                mc.addChapterFirst(manga);
            }
        }
    }

    @Override
    public void loadMangaInformation(Manga m, boolean forceReload) throws Exception {
        if (m.getChapters().isEmpty() || forceReload)
            loadChapters(m, forceReload);
    }

    @Override
    public String getPagesNumber(Chapter c, int page) {
        if (page > c.getPages()) {
            page = 1;
        }
        return c.getPath() + page + ".html";

    }

    @Override
    public String getImageFrom(Chapter c, int page) throws Exception {
        String data;
        data = new Navegador().get(this.getPagesNumber(c, page));
        return getFirstMatch(PATRON_IMAGEN, data, "Error: no se pudo obtener el enlace a la imagen");
    }

    @Override
    public void chapterInit(Chapter c) throws Exception {
        String data;
        data = new Navegador().get(c.getPath());
        String paginas = getFirstMatch(PATRON_LAST, data, "Error: no se pudo obtener el numero de paginas");
        c.setPages(Integer.parseInt(paginas));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String web = "http://es.mangahere.co/" + categoriasV[categorie] + pageNumber + ".htm" + ordenM[order];
        String data = new Navegador().get(web);
        Pattern p = Pattern.compile(PATRON_CAPS_VIS);
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(2), m.group(3), false);
            manga.setImages(m.group(1).replace("thumb_",""));
            mangas.add(manga);
        }
        hayMas = !mangas.isEmpty();
        return mangas;
    }

    @Override
    public String[] getCategories() {
        return categorias;
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
        String data = nav.get("http://es.mangahere.co/site/search?name=" + term);
        Pattern p = Pattern.compile("<dt>		<a href=\"(http://es.mangahere.co/manga/.+?)\".+?'>(.+?)<");
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(2).trim(), m.group(1), false));
        }
        return mangas;
    }

}
