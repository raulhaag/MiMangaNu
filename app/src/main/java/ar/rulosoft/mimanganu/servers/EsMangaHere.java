package ar.rulosoft.mimanganu.servers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Capitulo;
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
    private static final String PATRON_IMAGEN = "src=\"([^\"]+?.(jpg|gif|jpeg|png|bmp))";

    public static String[] categorias = {"Todo", "Acción", "Aventura", "Comedia", "Doujinshi", "Drama", "Ecchi", "Fantasía", "Gender Bender", "Harem",
            "Histórico", "Horror", "Josei", "Artes Marciales", "Maduro", "Mecha", "Misterio", "Oneshot", "Psicológico", "Romance", "Escolar",
            "Ciencia Ficción", "Seinen", "Shojo", "Shojo Ai", "Shounen", "Vida Cotidiana", "Deportes", "Sobrenatural", "Tragedia", "Yuri"};

    public static String[] categoriasV = {"directory/", "acción/", "aventura/", "comedia/", "doujinshi/", "drama/", "ecchi/", "fantasía/",
            "gender_bender/", "harem/", "histórico/", "horror/", "josei/", "artes_marciales/", "maduro/", "mecha/", "misterio/", "oneshot/", "psicológico/",
            "romance/", "escolar/", "ciencia_ficción/", "seinen/", "shojo/", "shojo_ai/", "shounen/", "vida_cotidiana/", "deportes/", "sobrenatural/",
            "tragedia/", "yuri/"};

    public static String[] orden = {"Lecturas", "A - Z", "Mejor Calificados", "Ultimos Actualizados"};
    public static String[] ordenM = {"?views.za", "?name.az", "?rating.za", "?last_chapter_time.az"};

    public EsMangaHere() {
        this.setBandera(R.drawable.flag_esp);
        this.setIcon(R.drawable.mangahere_icon);
        this.setServerName("EsMangaHere");
        setServerID(ServerBase.ESMANGAHERE);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<Manga>();
        String data = new Navegador().get("http://es.mangahere.co/mangalist/");
        Pattern p = Pattern.compile(PATTERN_SERIE);
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(ServerBase.ESMANGAHERE, m.group(1), m.group(2), false));
        }
        return mangas;
    }

    @Override
    public void cargarCapitulos(Manga manga, boolean reinicia) throws Exception {
        if (manga.getCapitulos().size() == 0||reinicia) {
            Pattern p;
            Matcher m;
            String data = new Navegador().get((manga.getPath()));

            // portada
            manga.setImages(getFirstMacthDefault(PATRON_PORTADA, data, ""));

            // sinopsis
            manga.setSinopsis(getFirstMacthDefault(PATRON_SINOPSIS, data, "Sin sinopsis."));

            // estado
            manga.setFinalizado(getFirstMacthDefault("<li><label>Estado:</label>(.+?)</li>", data, "En desarrollo").length() == 9);

            //autor
            manga.setAutor(getFirstMacthDefault("Autor.+?\">(.+?)<",data,""));

            // capitulos
            data = getFirstMacth(PATRON_SEG_CAP, data, "Error al obtener lista de cap�tulos");
            p = Pattern.compile(PATTERN_CAPITULOS);
            m = p.matcher(data);

            while (m.find()) {
                Capitulo mc = new Capitulo(m.group(2).trim(), "http://es.mangahere.co" + m.group(1));
                manga.addCapituloFirst(mc);
            }
        }
    }

    @Override
    public void cargarPortada(Manga m, boolean reinicia) throws Exception {
        if (m.getCapitulos().isEmpty()||reinicia)
            cargarCapitulos(m,reinicia);
    }

    @Override
    public String getPagina(Capitulo c, int pagina) {
        if (pagina > c.getPaginas()) {
            pagina = 1;
        }
        return c.getPath() + pagina + ".html";

    }

    @Override
    public String getImagen(Capitulo c, int pagina) throws Exception {
        String data;
        data = new Navegador().get(this.getPagina(c, pagina));
        return getFirstMacth(PATRON_IMAGEN, data, "Error: no se pudo obtener el enlace a la imagen");
    }

    @Override
    public void iniciarCapitulo(Capitulo c) throws Exception {
        String data;
        data = new Navegador().get(c.getPath());
        String paginas = getFirstMacth(PATRON_LAST, data, "Error: no se pudo obtener el numero de paginas");
        c.setPaginas(Integer.parseInt(paginas));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categoria, int ordentipo, int pagina) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<Manga>();
        String web = "http://es.mangahere.co/" + categoriasV[categoria] + pagina + ".htm" + ordenM[ordentipo];
        String data = new Navegador().get(web);
        Pattern p = Pattern.compile(PATRON_CAPS_VIS);
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(2), m.group(3), false);
            manga.setImages(m.group(1));
            mangas.add(manga);
        }
        hayMas = !mangas.isEmpty();
        return mangas;
    }

    @Override
    public String[] getCategorias() {
        return categorias;
    }

    @Override
    public String[] getOrdenes() {
        return orden;
    }

    @Override
    public boolean tieneListado() {
        return true;
    }

    @Override
    public ArrayList<Manga> getBusqueda(String termino) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<Manga>();
        Navegador nav = new Navegador();
        String data = nav.get("http://es.mangahere.co/site/search?name=" + termino);
        Pattern p = Pattern.compile("<dt>		<a href=\"(http://es.mangahere.co/manga/.+?)\".+?'>(.+?)<");
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(2).trim(), m.group(1), false));
        }
        return mangas;
    }

}
