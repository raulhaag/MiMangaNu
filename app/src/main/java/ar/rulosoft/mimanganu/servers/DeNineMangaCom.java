package ar.rulosoft.mimanganu.servers;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

/**
 * Created by Raul on 19/08/2015.
 */
public class DeNineMangaCom extends ServerBase {
    public static String[] generos = new String[]{"Alle", "0-9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "W", "X", "Y", "Z"};
    public static String[] generosV = new String[]{
            "http://de.ninemanga.com/category/index_.html", "http://de.ninemanga.com/category/0-9_.html", "http://de.ninemanga.com/category/A_.html",
            "http://de.ninemanga.com/category/B_.html", "http://de.ninemanga.com/category/C_.html", "http://de.ninemanga.com/category/D_.html",
            "http://de.ninemanga.com/category/E_.html", "http://de.ninemanga.com/category/F_.html", "http://de.ninemanga.com/category/G_.html",
            "http://de.ninemanga.com/category/H_.html", "http://de.ninemanga.com/category/I_.html", "http://de.ninemanga.com/category/J_.html",
            "http://de.ninemanga.com/category/K_.html", "http://de.ninemanga.com/category/L_.html", "http://de.ninemanga.com/category/M_.html",
            "http://de.ninemanga.com/category/N_.html", "http://de.ninemanga.com/category/O_.html", "http://de.ninemanga.com/category/P_.html",
            "http://de.ninemanga.com/category/Q_.html", "http://de.ninemanga.com/category/R_.html", "http://de.ninemanga.com/category/S_.html",
            "http://de.ninemanga.com/category/T_.html", "http://de.ninemanga.com/category/U_.html", "http://de.ninemanga.com/category/W_.html",
            "http://de.ninemanga.com/category/X_.html", "http://de.ninemanga.com/category/Y_.html", "http://de.ninemanga.com/category/Z_.html"
    };

    public DeNineMangaCom() {
        this.setFlag(R.drawable.flag_de);
        this.setIcon(R.drawable.esninemanga);
        this.setServerName("DeNineManga");
        setServerID(ServerBase.DENINEMANGA);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String source = new Navegador().get("http://de.ninemanga.com/search/?wd=" + URLEncoder.encode(term, "UTF-8"));
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("bookname\" href=\"(/manga/[^\"]+)\">(.+?)<");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(DENINEMANGA, m.group(2), "http://de.ninemanga.com" + m.group(1), false);
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
        String portada = getFirstMatchDefault("Manga\" src=\"(.+?)\"", source, "");
        m.setImages(portada);
        // sinopsis
        String sinopsis = getFirstMatchDefault("<p itemprop=\"description\">(.+?)</p>", source, "Keine inhaltsangabe").replaceAll("<.+?>", "");
        m.setSinopsis(sinopsis);

        //estado
        m.setFinished(!getFirstMatchDefault("<b>Status:</b>(.+?)</a>", source, "").contains("Laufende"));

        //autor
        m.setAuthor(getFirstMatchDefault("Autor.+?\">(.+?)<", source, ""));

        // cap�tulos
        Pattern p = Pattern.compile("<a class=\"chapter_list_a\" href=\"(/chapter.+?)\" title=\"(.+?)\">(.+?)</a>");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(3), "http://de.ninemanga.com" + matcher.group(1)));
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
        String nop = getFirstMatch("\\d+/(\\d+)</option>[\\s]*</select>", source, "Es versäumt, die Anzahl der Seiten zu bekommen");
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
            Manga manga = new Manga(DENINEMANGA, m.group(3), "http://de.ninemanga.com" + m.group(1), false);
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
        return new String[]{""};
    }

    @Override
    public boolean hasList() {
        return false;
    }
}
