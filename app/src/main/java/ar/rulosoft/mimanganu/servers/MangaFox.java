package ar.rulosoft.mimanganu.servers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public class MangaFox extends ServerBase {

    public static final String[] generos = {"All", "Action", "Adult", "Adventure", "Comedy", "Doujinshi", "Drama", "Ecchi", "Fantasy", "Gender Bender",
            "Harem", "Historical", "Horror", "Josei", "Martial Arts", "Mecha", "Mystery", "One Shot", "Psychological", "Romance", "School Life", "Sci-fi",
            "Seinen", "Shoujo", "Shoujo Ai", "Shounen", "Slice of Life", "Smut", "Sports", "Supernatural", "Tragedy", "Webtoons", "Yuri"};
    public static final String[] generosV = {"directory", "action", "adult", "adventure", "comedy", "doujinshi", "Drama", "ecchi", "fantasy", "gender-bender",
            "harem", "historical", "horror", "josei", "martial-arts", "mecha", "mystery", "one_shot", "psychological", "romance", "school-life", "sci-fi",
            "seinen", "shoujo", "shoujo-Ai", "shounen", "slice-of-life", "smut", "sports", "supernatural", "tragedy", "webtoons", "yuri"};
    private static final String PATRON_CAPS_VIS = "<a class=\"manga_img\" href=\"(.+?)\".+?src=\"(.+?)\".+?(<em class=\"tag_completed\"></em>						</a>|</a>).+?rel=\".+?\">(.+?)<";
    private static final String PATTERN_SERIE = "<li><a href=\"(.+?)\" rel=\"\\d+\" class=\"series_preview manga_(close|open)\">(.+?)</a></li>";
    private static final String SEGMENTO = "<div class=\"manga_list\">(.+?)<div class=\"clear gap\">";
    private static final String PATRON_PORTADA = "<div class=\"cover\">.+?src=\"(.+?)\"";
    private static final String PATRON_SINOPSIS = "<p class=\"summary\">(.+?)</p>";
    private static final String PATTERN_CAPITULOS = "<h\\d>[\\s]+<a href=\"([^\"]+)\".+?>([^<]+)([^\"]+<span class=\"title nowrap\">(.+?)<)?";
    private static final String PATRON_LAST = "(\\d+)</option>					<option value=\"0\"";
    private static final String PATRON_IMAGEN = "src=\"([^\"]+?.(jpg|gif|jpeg|png|bmp))";
    public static String[] orden = {"Popularity", "A - Z", "Rating", "Last Update"};
    public static String[] ordenM = {"", "?az", "?rating", "?latest"};

    public MangaFox() {
        this.setBandera(R.drawable.flag_eng);
        this.setIcon(R.drawable.mangafox_icon);
        this.setServerName("MangaFox");
        setServerID(ServerBase.MANGAFOX);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<Manga>();
        String data = new Navegador().get("http://mangafox.me/manga/");
        data = getFirstMacth(SEGMENTO, data, "no se ha obtenido el segmento");
        Pattern p = Pattern.compile(PATTERN_SERIE);
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(ServerBase.MANGAFOX, m.group(3), m.group(1), false);
            if (m.group(2).length() > 4) {
                manga.setFinalizado(true);
            }
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public void cargarCapitulos(Manga manga, boolean reinicia) throws Exception {
        if (manga.getCapitulos().size() == 0 || reinicia) {
            Pattern p;
            Matcher m;
            String data = new Navegador().get((manga.getPath()));

            // portada
            manga.setImages(getFirstMacthDefault(PATRON_PORTADA, data, ""));
            // sinopsis
            manga.setSinopsis(getFirstMacthDefault(PATRON_SINOPSIS, data, "Without synopsis."));

            manga.setFinalizado(data.contains("<h\\d>Status:</h\\d>    <span>        Completed"));

            //autor
            manga.setAutor(getFirstMacthDefault("\"/search/author/.+?>(.+?)<", data, ""));

            // capitulos
            p = Pattern.compile(PATTERN_CAPITULOS);
            m = p.matcher(data);

            while (m.find()) {
                Capitulo mc = null;
                if (m.group(4) != null)
                    mc = new Capitulo(m.group(2).trim() + ": " + m.group(4), m.group(1));
                else
                    mc = new Capitulo(m.group(2).trim(), m.group(1));
                manga.addCapituloFirst(mc);
            }
        }
    }

    @Override
    public void cargarPortada(Manga m, boolean reinicia) throws Exception {
        if (m.getCapitulos().isEmpty()|| reinicia)
            cargarCapitulos(m, reinicia);
    }

    @Override
    public String getPagina(Capitulo c, int pagina) {
        if (pagina > c.getPaginas()) {
            pagina = 1;
        }
        if (c.getPath().endsWith("html") && c.getPath().indexOf("/") > 0) {
            c.setPath(c.getPath().substring(0, c.getPath().lastIndexOf("/") + 1));
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
        String web = "http://mangafox.me/" + generosV[categoria] + "/" + pagina + ".htm" + ordenM[ordentipo];
        String data = new Navegador().get(web);
        Pattern p = Pattern.compile(PATRON_CAPS_VIS);
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(4), m.group(1), false);
            manga.setImages(m.group(2));
            if (m.group(3).length() > 6) {
                manga.setFinalizado(true);
            }
            mangas.add(manga);
        }
        hayMas = !mangas.isEmpty();
        return mangas;
    }

    @Override
    public String[] getCategorias() {
        return generos;
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
        String data = nav
                .get("http://mangafox.me/search.php?name_method=cw&name="
                        + termino
                        + "&type=&author_method=cw&author=&artist_method=cw&artist=&genres%5BAction%5D=0&genres%5BAdult%5D=0&genres%5BAdventure%5D=0&genres%5BComedy%5D=0&genres%5BDoujinshi%5D=0&genres%5BDrama%5D=0&genres%5BEcchi%5D=0&genres%5BFantasy%5D=0&genres%5BGender+Bender%5D=0&genres%5BHarem%5D=0&genres%5BHistorical%5D=0&genres%5BHorror%5D=0&genres%5BJosei%5D=0&genres%5BMartial+Arts%5D=0&genres%5BMature%5D=0&genres%5BMecha%5D=0&genres%5BMystery%5D=0&genres%5BOne+Shot%5D=0&genres%5BPsychological%5D=0&genres%5BRomance%5D=0&genres%5BSchool+Life%5D=0&genres%5BSci-fi%5D=0&genres%5BSeinen%5D=0&genres%5BShoujo%5D=0&genres%5BShoujo+Ai%5D=0&genres%5BShounen%5D=0&genres%5BShounen+Ai%5D=0&genres%5BSlice+of+Life%5D=0&genres%5BSmut%5D=0&genres%5BSports%5D=0&genres%5BSupernatural%5D=0&genres%5BTragedy%5D=0&genres%5BWebtoons%5D=0&genres%5BYaoi%5D=0&genres%5BYuri%5D=0&released_method=eq&released=&rating_method=eq&rating=&is_completed=&advopts=1");
        Pattern p = Pattern.compile("<td><a href=\"(http://mangafox.me/manga/.+?)\".+?\">(.+?)<");
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(2).trim(), m.group(1), false));
        }
        return mangas;
    }

}
