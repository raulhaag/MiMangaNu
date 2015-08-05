package ar.rulosoft.mimanganu.servers;

import android.text.Html;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public class KissManga extends ServerBase {
    public static String IP = "93.174.95.110";
    public static String HOST = "kissmanga.com";
    static String[] generos = new String[]{"All", "Action", "Adult", "Adventure", "Comedy", "Comic", "Doujinshi", "Drama", "Ecchi", "Fantasy", "Harem",
            "Historical", "Horror", "Lolicon", "Manga", "Manhua", "Manhwa", "Mature", "Mecha", "Mystery", "Psychological", "Romance", "Sci-fi", "Seinen",
            "Shotacon", "Shoujo", "Shounen", "Smut", "Sports", "Supernatural", "Webtoon", "Yuri"};
    static String[] generosV = new String[]{"/MangaList", "/Genre/Action", "/Genre/Adult", "/Genre/Adventure", "/Genre/Comedy", "/Genre/Comic",
            "/Genre/Doujinshi", "/Genre/Drama", "/Genre/Ecchi", "/Genre/Fantasy", "/Genre/Harem", "/Genre/Historical", "/Genre/Horror", "/Genre/Lolicon",
            "/Genre/Manga", "/Genre/Manhua", "/Genre/Manhwa", "/Genre/Mature", "/Genre/Mecha", "/Genre/Mystery", "/Genre/Psychological", "/Genre/Romance",
            "/Genre/Sci-fi", "/Genre/Seinen", "/Genre/Shotacon", "/Genre/Shoujo", "/Genre/Shounen", "/Genre/Smut", "/Genre/Sports", "/Genre/Supernatural",
            "/Genre/Webtoon", "/Genre/Yuri"};
    static String[] ordenes = new String[]{"", "/MostPopular", "/LatestUpdate", "/Newest"};

    public KissManga() {
        this.setFlag(R.drawable.flag_eng);
        this.setIcon(R.drawable.kissmanga);
        this.setServerName("KissManga");
        setServerID(ServerBase.KISSMANGA);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        Navegador nav = new Navegador();
        nav.addPost("keyword", URLEncoder.encode(term, "UTF-8"));
        String source = nav.post(IP, "/Search/Manga", HOST);
        ArrayList<Manga> lista = null;
        Pattern p = Pattern.compile("href=\"(/Manga/.*?)\">([^<]+)</a>[^<]+<p>[^<]+<span class=\"info\"");
        Matcher m = p.matcher(source);
        if (m.find()) {
            lista = new ArrayList<>();
            boolean status = getFirstMacthDefault("Status:</span>&nbsp;([\\S]+)", source, "Ongoing").length() == 9;
            lista.add(new Manga(KISSMANGA, m.group(2), m.group(1), status));
        } else {
            lista = getMangasSource(source);
        }

        return lista;
    }

    @Override
    public void loadChapters(Manga m, boolean forceReload) throws Exception {
        if (m.getChapters() == null || m.getChapters().size() == 0 || forceReload)
            loadMangaInformation(m, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga m, boolean forceReload) throws Exception {
        String source = new Navegador().get(IP, m.getPath(), HOST);
        // sinopsis
        m.setSinopsis(Html.fromHtml(getFirstMacthDefault("<span class=\"info\">Summary:</span>(.+?)</div>", source, "Without synopsis.")).toString());
        // portada
        String imagen = getFirstMacthDefault("rel=\"image_src\" href=\"(.+?)\"", source, null);
        if (imagen != null) {
            m.setImages("http://" + IP + imagen.replace("http://kissmanga.com", "") + "|" + HOST);
        }

        //autor

        m.setAuthor(getFirstMacthDefault("href=\"/AuthorArtist/.+?>(.+?)<", source, ""));

        // capitulos
        Pattern p = Pattern.compile("<td>[\\s]+<a[\\s]+href=\"(.+?)\".+?>[\\s]+(.+?)<");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(2), matcher.group(1)));
        }
        m.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter c, int page) {
        return c.getPath();
    }

    @Override
    public String getImageFrom(Chapter c, int page) throws Exception {
        if (c.getExtra() == null || c.getExtra().length() < 2) {
            String source = new Navegador().get(IP, c.getPath(), HOST);
            Pattern p = Pattern.compile("lstImages.push\\(\"(.+?)\"");
            Matcher m = p.matcher(source);
            String imagenes = "";
            while (m.find()) {
                imagenes = imagenes + "|" + m.group(1);
            }
            c.setExtra(imagenes);
        }
        String[] imagenes = c.getExtra().split("\\|");
        return imagenes[page];
    }

    @Override
    public void chapterInit(Chapter c) throws Exception {
        int pages = 0;
        if (c.getExtra() == null || c.getExtra().length() < 2) {
            String source = new Navegador().get(IP, c.getPath(), HOST);
            Pattern p = Pattern.compile("lstImages.push\\(\"(.+?)\"");
            Matcher m = p.matcher(source);
            String imagenes = "";
            while (m.find()) {
                pages++;
                imagenes = imagenes + "|" + m.group(1);
            }
            c.setExtra(imagenes);
        }
        c.setPages(pages);
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception {
        String web = generosV[categorie] + ordenes[order];
        if (pageNumber > 1) {
            web = web + "?page=" + pageNumber;
        }
        String source = new Navegador().get(IP, web, HOST);
        return getMangasSource(source);
    }

    public ArrayList<Manga> getMangasSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("src=\"([^\"]+)\" style=\"float.+?href=\"(.+?)\">(.+?)<");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(KISSMANGA, m.group(3), m.group(2), false);
            manga.setImages("http://" + IP + m.group(1).replace("http://kissmanga.com", "") + "|" + HOST);
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
        return new String[]{"a-z", "Popularity", "Lastest Update", "New Manga"};
    }

    @Override
    public boolean hasList() {
        return false;
    }

}
