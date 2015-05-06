package ar.rulosoft.mimanganu.servers;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public class ItNineMangaCom extends ServerBase {

    public static String[] generos = new String[]{"0-9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "W", "X", "Y", "Z", "Action", "Adult", "Adventure", "Avventura", "Azione", "Bara", "Comedy", "Commedia", "Demenziale", "Doujinshi",
            "Dounshinji", "Drama", "Ecchi", "Fantasy", "Gender Bender", "Harem", "Hentai", "Historical", "Horror", "Josei", "Magico", "Martial Arts", "Mature",
            "Mecha", "Misteri", "Musica", "Mystery", "Psicologico", "Psychological", "Raccolta", "Romance", "Romantico", "School Life", "Sci-Fi", "Scolastico",
            "Seinen", "Sentimentale", "Shota", "Shoujo", "Shounen", "Slice Of Life", "Smut", "Sovrannaturale", "Splatter", "Sportivo", "Sports", "Storico",
            "Supernatural", "Tragedy", "Vita Quotidiana", "Yuri"};
    public static String[] generosV = new String[]{"http://it.ninemanga.com/category/0-9_.html", "http://it.ninemanga.com/category/A_.html",
            "http://it.ninemanga.com/category/B_.html", "http://it.ninemanga.com/category/C_.html", "http://it.ninemanga.com/category/D_.html",
            "http://it.ninemanga.com/category/E_.html", "http://it.ninemanga.com/category/F_.html", "http://it.ninemanga.com/category/G_.html",
            "http://it.ninemanga.com/category/H_.html", "http://it.ninemanga.com/category/I_.html", "http://it.ninemanga.com/category/J_.html",
            "http://it.ninemanga.com/category/K_.html", "http://it.ninemanga.com/category/L_.html", "http://it.ninemanga.com/category/M_.html",
            "http://it.ninemanga.com/category/N_.html", "http://it.ninemanga.com/category/O_.html", "http://it.ninemanga.com/category/P_.html",
            "http://it.ninemanga.com/category/Q_.html", "http://it.ninemanga.com/category/R_.html", "http://it.ninemanga.com/category/S_.html",
            "http://it.ninemanga.com/category/T_.html", "http://it.ninemanga.com/category/U_.html", "http://it.ninemanga.com/category/W_.html",
            "http://it.ninemanga.com/category/X_.html", "http://it.ninemanga.com/category/Y_.html", "http://it.ninemanga.com/category/Z_.html",
            "http://it.ninemanga.com/category/Action_.html", "http://it.ninemanga.com/category/Adult_.html", "http://it.ninemanga.com/category/Adventure_.html",
            "http://it.ninemanga.com/category/Avventura_.html", "http://it.ninemanga.com/category/Azione_.html", "http://it.ninemanga.com/category/Bara_.html",
            "http://it.ninemanga.com/category/Comedy_.html", "http://it.ninemanga.com/category/Commedia_.html",
            "http://it.ninemanga.com/category/Demenziale_.html", "http://it.ninemanga.com/category/Doujinshi_.html",
            "http://it.ninemanga.com/category/Dounshinji_.html", "http://it.ninemanga.com/category/Drama_.html", "http://it.ninemanga.com/category/Ecchi_.html",
            "http://it.ninemanga.com/category/Fantasy_.html", "http://it.ninemanga.com/category/Gender+Bender_.html",
            "http://it.ninemanga.com/category/Harem_.html", "http://it.ninemanga.com/category/Hentai_.html", "http://it.ninemanga.com/category/Historical_.html",
            "http://it.ninemanga.com/category/Horror_.html", "http://it.ninemanga.com/category/Josei_.html", "http://it.ninemanga.com/category/Magico_.html",
            "http://it.ninemanga.com/category/Martial+Arts_.html", "http://it.ninemanga.com/category/Mature_.html",
            "http://it.ninemanga.com/category/Mecha_.html", "http://it.ninemanga.com/category/Misteri_.html", "http://it.ninemanga.com/category/Musica_.html",
            "http://it.ninemanga.com/category/Mystery_.html", "http://it.ninemanga.com/category/Psicologico_.html",
            "http://it.ninemanga.com/category/Psychological_.html", "http://it.ninemanga.com/category/Raccolta_.html",
            "http://it.ninemanga.com/category/Romance_.html", "http://it.ninemanga.com/category/Romantico_.html",
            "http://it.ninemanga.com/category/School+Life_.html", "http://it.ninemanga.com/category/Sci-Fi_.html",
            "http://it.ninemanga.com/category/Scolastico_.html", "http://it.ninemanga.com/category/Seinen_.html",
            "http://it.ninemanga.com/category/Sentimentale_.html", "http://it.ninemanga.com/category/Shota_.html",
            "http://it.ninemanga.com/category/Shoujo_.html", "http://it.ninemanga.com/category/Shounen_.html",
            "http://it.ninemanga.com/category/Slice+of+Life_.html", "http://it.ninemanga.com/category/Smut_.html",
            "http://it.ninemanga.com/category/Sovrannaturale_.html", "http://it.ninemanga.com/category/Splatter_.html",
            "http://it.ninemanga.com/category/Sportivo_.html", "http://it.ninemanga.com/category/Sports_.html", "http://it.ninemanga.com/category/Storico_.html",
            "http://it.ninemanga.com/category/Supernatural_.html", "http://it.ninemanga.com/category/Tragedy_.html",
            "http://it.ninemanga.com/category/Vita+Quotidiana_.html", "http://it.ninemanga.com/category/Yuri_.html"};

    public ItNineMangaCom() {
        this.setBandera(R.drawable.flag_it);
        this.setIcon(R.drawable.esninemanga);
        this.setServerName("ItNineManga");
        setServerID(ServerBase.ITNINEMANGA);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ArrayList<Manga> getBusqueda(String termino) throws Exception {
        String source = new Navegador().get("http://it.ninemanga.com/search/?wd=" + URLEncoder.encode(termino, "UTF-8"));
        ArrayList<Manga> mangas = new ArrayList<Manga>();
        Pattern p = Pattern.compile("bookname\" href=\"(/manga/[^\"]+)\">(.+?)<");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(ITNINEMANGA, m.group(2), "http://it.ninemanga.com" + m.group(1), false);
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public void cargarCapitulos(Manga m, boolean reinicia) throws Exception {
        if (m.getCapitulos() == null || m.getCapitulos().size() == 0 || reinicia)
            cargarPortada(m, reinicia);
    }

    @Override
    public void cargarPortada(Manga m, boolean reinicia) throws Exception {
        String source = new Navegador().get(m.getPath() + "?waring=1");
        // portada
        String portada = getFirstMacthDefault("Manga\" src=\"(.+?)\"", source, "");
        m.setImages(portada);
        // sinopsis
        String sinopsis = getFirstMacthDefault("<p itemprop=\"description\">(.+?)</p>", source, "Senza sinossi").replaceAll("<.+?>", "");
        m.setSinopsis(sinopsis);

        // estado
        m.setFinalizado(getFirstMacthDefault("Stato:(.+?)</a>", source, "").contains("Completato"));

        //autor

        m.setAutor(getFirstMacthDefault("Author.+?\">(.+?)<", source, ""));

        // capítulos
        Pattern p = Pattern.compile("<a class=\"chapter_list_a\" href=\"(/chapter.+?)\" title=\"(.+?)\">(.+?)</a>");
        Matcher matcher = p.matcher(source);
        ArrayList<Capitulo> capitulos = new ArrayList<Capitulo>();
        while (matcher.find()) {
            capitulos.add(0, new Capitulo(matcher.group(3), "http://it.ninemanga.com" + matcher.group(1)));
        }
        m.setCapitulos(capitulos);

    }

    @Override
    public String getPagina(Capitulo c, int pagina) {
        return c.getPath().replace(".html", "-" + pagina + ".html");
    }

    @Override
    public String getImagen(Capitulo c, int pagina) throws Exception {
        if (c.getExtra() == null)
            setExtra(c);
        String[] imagenes = c.getExtra().split("\\|");
        return imagenes[pagina];
    }

    public void setExtra(Capitulo c) throws Exception {
        String source = new Navegador().get(c.getPath().replace(".html", "-" + c.getPaginas() + "-1.html"));
        Pattern p = Pattern.compile("<img class=\"manga_pic.+?src=\"([^\"]+)");
        Matcher m = p.matcher(source);
        String imagenes = "";
        while (m.find()) {
            imagenes = imagenes + "|" + m.group(1);
        }
        c.setExtra(imagenes);
    }

    @Override
    public void iniciarCapitulo(Capitulo c) throws Exception {
        String source = new Navegador().get(c.getPath());
        String nop = getFirstMacth("\\d+/(\\d+)</option>[\\s]*</select>", source, "Error al obtener el número de páginas");
        c.setPaginas(Integer.parseInt(nop));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categoria, int ordentipo, int pagina) throws Exception {
        String source = new Navegador().get(generosV[categoria].replace("_", "_" + pagina));
        return getMangasFromSource(source);
    }

    public ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<Manga>();
        Pattern p = Pattern.compile("<a href=\"(/manga/[^\"]+)\"><img src=\"(.+?)\".+?alt=\"([^\"]+)\"");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(ITNINEMANGA, m.group(3), "http://it.ninemanga.com" + m.group(1), false);
            manga.setImages(m.group(2));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public String[] getCategorias() {
        return generos;
    }

    @Override
    public String[] getOrdenes() {
        return new String[]{"Popolarità"};
    }

    @Override
    public boolean tieneListado() {
        return false;
    }

}
