package ar.rulosoft.mimanganu.servers;

import android.text.Html;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;

public class ItNineMangaCom extends ServerBase {
    public static String HOST = "http://it.ninemanga.com";
    public static String[] generos = new String[]{
            "Tutto", "0-9",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "W", "X", "Y", "Z",
            "Action", "Adult", "Adventure", "Avventura", "Azione", "Bara", "Comedy", "Commedia",
            "Demenziale", "Doujinshi", "Dounshinji", "Drama", "Ecchi", "Fantasy", "Gender Bender",
            "Harem", "Hentai", "Historical", "Horror", "Josei", "Magico", "Martial Arts", "Mature",
            "Mecha", "Misteri", "Musica", "Mystery", "Psicologico", "Psychological", "Raccolta",
            "Romance", "Romantico", "School Life", "Sci-Fi", "Scolastico", "Seinen",
            "Sentimentale", "Shota", "Shoujo", "Shounen", "Slice Of Life", "Smut",
            "Sovrannaturale", "Splatter", "Sportivo", "Sports", "Storico",
            "Supernatural", "Tragedy", "Vita Quotidiana", "Yuri"
    };
    public static String[] generosV = new String[]{
            "index_.html", "0-9_.html",
            "A_.html", "B_.html", "C_.html", "D_.html", "E_.html", "F_.html", "G_.html",
            "H_.html", "I_.html", "J_.html", "K_.html", "L_.html", "M_.html", "N_.html",
            "O_.html", "P_.html", "Q_.html", "R_.html", "S_.html", "T_.html", "U_.html",
            "W_.html", "X_.html", "Y_.html", "Z_.html",
            "Action_.html", "Adult_.html", "Adventure_.html", "Avventura_.html", "Azione_.html",
            "Bara_.html", "Comedy_.html", "Commedia_.html", "Demenziale_.html", "Doujinshi_.html",
            "Dounshinji_.html", "Drama_.html", "Ecchi_.html", "Fantasy_.html",
            "Gender+Bender_.html", "Harem_.html", "Hentai_.html", "Historical_.html",
            "Horror_.html", "Josei_.html", "Magico_.html", "Martial+Arts_.html", "Mature_.html",
            "Mecha_.html", "Misteri_.html", "Musica_.html", "Mystery_.html", "Psicologico_.html",
            "Psychological_.html", "Raccolta_.html", "Romance_.html", "Romantico_.html",
            "School+Life_.html", "Sci-Fi_.html", "Scolastico_.html", "Seinen_.html",
            "Sentimentale_.html", "Shota_.html", "Shoujo_.html", "Shounen_.html",
            "Slice+of+Life_.html", "Smut_.html", "Sovrannaturale_.html", "Splatter_.html",
            "Sportivo_.html", "Sports_.html", "Storico_.html", "Supernatural_.html",
            "Tragedy_.html", "Vita+Quotidiana_.html", "Yuri_.html"
    };
    static String[] order = new String[]{
            "/category/", "/list/New-Update/", "/list/Hot-Book/", "/list/New-Book/"
    };

    public ItNineMangaCom() {
        this.setFlag(R.drawable.flag_it);
        this.setIcon(R.drawable.esninemanga);
        this.setServerName("ItNineManga");
        setServerID(ServerBase.ITNINEMANGA);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String source = getNavWithHeader().get(
                HOST + "/search/?wd=" + URLEncoder.encode(term, "UTF-8"));
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("bookname\" href=\"(/manga/[^\"]+)\">(.+?)<");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(ITNINEMANGA, m.group(2), HOST + m.group(1), false);
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
        String source = getNavWithHeader().get(m.getPath() + "?waring=1");
        // portada
        m.setImages(getFirstMatchDefault("Manga\" src=\"(.+?)\"", source, ""));
        // sinopsis
        String sinopsis = getFirstMatchDefault("<p itemprop=\"description\">(.+?)</p>",
                source, "Senza sinossi").replaceAll("<.+?>", "");
        m.setSinopsis(Html.fromHtml(sinopsis.replaceFirst("Sommario:", "")).toString());
        // estado
        m.setFinished(getFirstMatchDefault("Stato:(.+?)</a>", source, "").contains("Completato"));
        // autor
        m.setAuthor(getFirstMatchDefault("Author.+?\">(.+?)<", source, ""));
        // capitulos
        Pattern p = Pattern.compile(
                "<a class=\"chapter_list_a\" href=\"(/chapter.+?)\" title=\"(.+?)\">(.+?)</a>");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(3), HOST + matcher.group(1)));
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
        String source = getNavWithHeader().get(
                c.getPath().replace(".html", "-" + c.getPages() + "-1.html"));
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
        String source = getNavWithHeader().get(c.getPath());
        String nop = getFirstMatch(
                "\\d+/(\\d+)</option>[\\s]*</select>", source,
                "Error al obtener el n�mero de p�ginas");
        c.setPages(Integer.parseInt(nop));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception {
        String source = getNavWithHeader().get(
                HOST + ItNineMangaCom.order[order] +
                        generosV[categorie].replace("_", "_" + pageNumber));
        return getMangasFromSource(source);
    }

    public ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile(
                "<a href=\"(/manga/[^\"]+)\"><img src=\"(.+?)\".+?alt=\"([^\"]+)\"");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(ITNINEMANGA, m.group(3), HOST + m.group(1), false);
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
        // "/category/", "/list/New-Update/", "/list/Hot-Book", "/list/New-Book/"
        return new String[]{"Lista Manga", "Ultime uscite", "Popolare Manga", "Nuovo Manga"};
    }

    @Override
    public boolean hasList() {
        return false;
    }
}
