package ar.rulosoft.mimanganu.servers;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public class StarkanaCom extends ServerBase {

    public static String[] generos = new String[]{"All", "Action", "Adult", "Adventure", "Anime", "Comedy", "Cooking", "Doujinshi", "Drama", "Ecchi",
            "Fantasy", "Gender bender", "Harem", "Hentai", "Historical", "Horror", "Josei", "Live action", "Lolicon", "Magic", "Manhua", "Manhwa",
            "Martial arts", "Mature", "Mecha", "Medical ", "Music", "Mystery", "One shot", "Psychological", "Romance", "School life", "Sci-fi",
            "Science Fiction", "Seinen", "Shotacon", "Shoujo", "Shoujo Ai", "Shoujo-ai", "Shounen", "Shounen Ai", "Shounen-ai", "Slice of life", "Smut",
            "Sports", "Supernatural", "Tournament", "Tragedy", "Webtoons", "Yuri"};
    public static String[] generosV = new String[]{"", "g=1", "g=53", "g=21", "g=13", "g=2", "g=29", "g=46", "g=3", "g=27", "g=14", "g=22", "g=37", "g=52",
            "g=35", "g=6", "g=15", "g=25", "g=54", "g=50", "g=23", "g=28", "g=31", "g=32", "g=39", "g=36", "g=38", "g=5", "g=17", "g=7", "g=18", "g=19",
            "g=24", "g=8", "g=26", "g=56", "g=20", "g=55", "g=40", "g=16", "g=51", "g=41", "g=33", "g=42", "g=30", "g=4", "g=49", "g=34", "g=47", "g=44"};
    public static String[] paginas = new String[]{"/manga/0", "/manga/A", "/manga/B", "/manga/C", "/manga/D", "/manga/E", "/manga/F", "/manga/G", "/manga/H",
            "/manga/I", "/manga/J", "/manga/K", "/manga/L", "/manga/M", "/manga/N", "/manga/O", "/manga/P", "/manga/Q", "/manga/R", "/manga/S", "/manga/T",
            "/manga/U", "/manga/V", "/manga/W", "/manga/X", "/manga/Y", "/manga/Z"};

    public StarkanaCom() {
        this.setFlag(R.drawable.flag_eng);
        this.setIcon(R.drawable.rip);
        this.setServerName("Starkana");
        setServerID(ServerBase.STARKANACOM);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        String source = new Navegador().get("http://starkana.com/manga/list");
        Pattern p = Pattern.compile("http://starkana.(jp|com)/img/icons/tick_(.+?).png\".+?href=\"(.+?)\">(.+?)<");
        Matcher m = p.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()) {
            if (m.group(2).length() == 4) {
                mangas.add(new Manga(STARKANACOM, m.group(4), "http://starkana.com" + m.group(3), false));
            } else {
                mangas.add(new Manga(STARKANACOM, m.group(4), "http://starkana.com" + m.group(3), true));
            }
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String source = new Navegador().get("http://starkana.com/manga/search?k=" + URLEncoder.encode(term, "UTF-8"));
        return getMangasFromSource(source);
    }

    @Override
    public void loadChapters(Manga m, boolean forceReload) throws Exception {
        if (m.getChapters() == null || m.getChapters().size() == 0 || forceReload)
            loadMangaInformation(m, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga m, boolean forceReload) throws Exception {
        String source = new Navegador().get(m.getPath());
        // portada
        String portada = getFirstMacthDefault("<img class=\"a_img\" src=\"(.+?)\"", source, "");
        m.setImages(portada);
        // Sinopsis
        String sinopsis = getFirstMacthDefault("<b>Summary:.+?<div>(.+?)<", source, "Without synopsis");
        m.setSinopsis(sinopsis);
        //status
        m.setFinished(source.contains("<b>Completed</b></span>"));
        // capitulos
        Pattern p = Pattern.compile("<a class=\"download-link\" href=\"(.+?)\">(.+?)</a>");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(2).replaceAll("<.+?>", ""), "http://starkana.com" + matcher.group(1)));
        }
        m.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter c, int page) {
        return c.getPath() + "/" + page;
    }

    @Override
    public String getImageFrom(Chapter c, int page) throws Exception {
        if (c.getExtra() == null)
            setExtra(c);
        String[] imagenes = c.getExtra().split("\\|");
        return imagenes[page];
    }

    public void setExtra(Chapter c) throws Exception {
        String source = new Navegador().get(c.getPath() + "?scroll");
        Pattern p = Pattern.compile("<img src=\"([^\"]+)\" alt=\"[^\"]*\" class=\"dyn\">");
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
        c.setPages(Integer.parseInt(getFirstMacth("of <strong>(\\d+)</strong>", source, "Error al buscar número de páginas")));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception {
        int paginaLoc = pageNumber - 1;
        ArrayList<Manga> mangas = null;
        String web = "";
        if (categorie == 0 && paginaLoc < paginas.length) {
            web = paginas[paginaLoc];
        } else if (paginaLoc < 1) {
            web = "/manga/search?" + generosV[categorie];
        }
        if (web.length() > 2) {

            String source = new Navegador().get("http://starkana.com" + web);
            mangas = getMangasFromSource(source);
        }
        return mangas;
    }

    ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("title=\"([^\"]+)\" href=\"(/manga/.+?)\".+?src=\"(.+?)\".+?tick_(.+?)\\.");// "<img class=\"a_img\" src=\"(.+?)\".+?<img src=\"http://starkana.com/img/icons/tick_(.+?).png\".+?href=\"(.+?)\".+?>(.+?)</a>");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga;
            String title = m.group(1).replaceAll("<.+?>", "");
            if (m.group(4).length() == 4) {
                manga = new Manga(STARKANACOM, title, "http://starkana.com" + m.group(2), false);
            } else {
                manga = new Manga(STARKANACOM, title, "http://starkana.com" + m.group(2), true);
            }
            manga.setImages(m.group(3).replace("small_", "default_"));
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
        return new String[]{"a-z"};
    }

    @Override
    public boolean hasList() {
        return true;
    }

}
