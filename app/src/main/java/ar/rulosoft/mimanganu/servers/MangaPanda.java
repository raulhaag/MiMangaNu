package ar.rulosoft.mimanganu.servers;

import android.text.Html;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public class MangaPanda extends ServerBase {

    private static final String PATTERN_SERIE = "<li><a href=\"([^\"]+)\">([^<]+)";
    private static final String PATTERN_SUB = "<div class=\"series_col\">([\\s\\S]+?)<div id=\"adfooter\">";

    private static final String PATTERN_FRAG_CAPITULOS = "<div id=\"chapterlist\">([\\s\\S]+?)</table>";
    private static final String PATTERN_CAPITULOS = "<a href=\"([^\"]+)\">([^\"]+?)</td>";

    private static final String PATRON_LAST = "of (\\d+)</div>";
    private static final String PATRON_IMAGEN = "src=\"([^\"]+?.(jpg|gif|jpeg|png|bmp))";

    private static final String PATRON_VISUAL = "url\\('(.+?)'.+?href=\"(.+?)\">(.+?)</a>";

    private static final String[] generos = {"All", "Action", "Adventure", "Comedy", "Demons", "Drama", "Ecchi", "Fantasy", "Gender Bender", "Harem",
            "Historical", "Horror", "Josei", "Magic", "Martial Arts", "Mature", "Mecha", "Military", "Mystery", "One Shot", "Psychological", "Romance",
            "School Life", "Sci-Fi", "Seinen", "Shoujo", "Shoujoai", "Shounen", "Slice of Life", "Smut", "Sports", "Super Power", "Supernatural", "Tragedy",
            "Vampire", "Yuri"};

    private static final String[] generosV = {"", "action", "adventure", "comedy", "demons", "drama", "ecchi", "fantasy", "gender-bender", "harem",
            "historical", "horror", "josei", "magic", "martial-arts", "mature", "mecha", "military", "mystery", "one-shot", "psychological", "romance",
            "school-life", "sci-fi", "seinen", "shoujo", "shoujoai", "shounen", "slice-of-life", "smut", "sports", "super-power", "supernatural", "tragedy",
            "vampire", "yuri"};

    private static final String[] orden = {"Popular"};

    public MangaPanda() {
        this.setBandera(R.drawable.flag_eng);
        this.setIcon(R.drawable.mangapanda);
        this.setServerName("MangaPanda");
        setServerID(ServerBase.MANGAPANDA);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = new Navegador().get("http://www.mangapanda.com/alphabetical");
        Pattern p = Pattern.compile(PATTERN_SUB);
        Matcher m = p.matcher(data);
        if (m.find()) {
            String b = m.group(1);
            Pattern p1 = Pattern.compile(PATTERN_SERIE);
            Matcher m1 = p1.matcher(b);
            while (m1.find()) {
                mangas.add(new Manga(this.getServerID(), m1.group(2), "http://www.mangapanda.com" + m1.group(1), false));
            }
        }
        return mangas;
    }

    @Override
    public void cargarCapitulos(Manga manga, boolean reinicia) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 || reinicia)
            cargarPortada(manga, reinicia);
    }

    @Override
    public void cargarPortada(Manga manga, boolean reinicia) throws Exception {

        String data = new Navegador().get((manga.getPath()));
        Pattern p = Pattern.compile(PATTERN_FRAG_CAPITULOS);
        Matcher m = p.matcher(data);
        if (m.find()) {
            Pattern p1 = Pattern.compile(PATTERN_CAPITULOS);
            Matcher m1 = p1.matcher(m.group(1));
            while (m1.find()) {
                String web = m1.group(1);
                if (web.matches("/[-|\\d]+/([^/]+)/chapter-(\\d+).html")) {
                    Pattern p2 = Pattern.compile("/[-|\\d]+/([^/]+)/chapter-(\\d+).html");
                    Matcher m2 = p2.matcher(web);
                    if (m2.find())
                        web = m2.group(1) + "/" + m2.group(2);
                }
                if (web.startsWith("/")) {
                    manga.addCapitulo(new Chapter(m1.group(2).replace("</a>", ""), "http://www.mangapanda.com" + web));
                } else {
                    manga.addCapitulo(new Chapter(m1.group(2).replace("</a>", ""), "http://www.mangapanda.com/" + web));
                }
            }
        }

        // sinopsis
        manga.setSinopsis(getFirstMacthDefault("<p>(.+)</p>", data, "Without synopsis"));
        // portada
        manga.setImages(getFirstMacthDefault("mangaimg\"><img src=\"([^\"]+)", data, ""));
        //status
        manga.setFinalizado(data.contains("</td><td>Completed</td>"));
        //autor
        manga.setAutor(Html.fromHtml(getFirstMacthDefault("Author:</td><td>(.+?)<", data, "")).toString());
    }

    @Override
    public String getPagina(Chapter c, int pagina) {
        if (pagina > c.getPaginas()) {
            pagina = 1;
        }
        return c.getPath() + "/" + pagina;

    }

    @Override
    public String getImagen(Chapter c, int pagina) throws Exception {
        String data;
        data = new Navegador().get(this.getPagina(c, pagina));
        return getFirstMacth(PATRON_IMAGEN, data, "Error: no se pudo obtener el enlace a la imagen");
    }

    @Override
    public void iniciarCapitulo(Chapter c) throws Exception {
        String data;
        data = new Navegador().get(c.getPath());
        String paginas = getFirstMacth(PATRON_LAST, data, "Error: no se pudo obtener el numero de paginas");
        c.setPaginas(Integer.parseInt(paginas));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categoria, int ordentipo, int pagina) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String web;
        if (categoria == 0)
            web = "http://www.mangapanda.com/popular" + "/" + (pagina - 1) * 20;
        else
            web = "http://www.mangapanda.com/popular" + "/" + generosV[categoria] + "/" + (pagina - 1) * 20;

        String data = new Navegador().get(web);
        Pattern p = Pattern.compile(PATRON_VISUAL);
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga;
            if (m.group(2).startsWith("/"))
                manga = new Manga(getServerID(), m.group(3), "http://www.mangapanda.com" + m.group(2), false);
            else
                manga = new Manga(getServerID(), m.group(3), "http://www.mangapanda.com/" + m.group(2), false);

            manga.setImages(m.group(1));
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
        ArrayList<Manga> mangas = new ArrayList<>();
        Navegador nav = new Navegador();
        String data = nav.get("http://www.mangapanda.com/actions/search/?q=" + termino + "&limit=100");
        Pattern p = Pattern.compile("(.+?)\\|.+?\\|(/.+?)\\|\\d+");
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(1).trim(), "http://www.mangapanda.com" + m.group(2), false));
        }
        return mangas;
    }

}
