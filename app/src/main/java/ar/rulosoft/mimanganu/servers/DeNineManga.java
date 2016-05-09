package ar.rulosoft.mimanganu.servers;

import android.text.Html;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;

public class DeNineManga extends ServerBase {
    private static String HOST = "http://de.ninemanga.com";

    // As you can guess, "V" is missing, but this is the fault of the website, somehow
    private static String[] genre = new String[]{
            "Alle", "0-9",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "W", "X", "Y", "Z"
    };
    private static String[] genreV = new String[]{
            "index_.html", "0-9_.html",
            "A_.html", "B_.html", "C_.html", "D_.html", "E_.html", "F_.html", "G_.html",
            "H_.html", "I_.html", "J_.html", "K_.html", "L_.html", "M_.html", "N_.html",
            "O_.html", "P_.html", "Q_.html", "R_.html", "S_.html", "T_.html", "U_.html",
            "W_.html", "X_.html", "Y_.html", "Z_.html"
    };
    private static String[] order = new String[]{
            "/category/", "/list/New-Update/", "/list/Hot-Book/", "/list/New-Book/"
    };

    public DeNineManga() {
        this.setFlag(R.drawable.flag_de);
        this.setIcon(R.drawable.ninemanga);
        this.setServerName("DeNineManga");
        setServerID(ServerBase.DENINEMANGA);
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
            Manga manga = new Manga(getServerID(), m.group(2), HOST + m.group(1), false);
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 || forceReload)
            loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        String source = getNavWithHeader().get(manga.getPath() + "?waring=1");
        // Front
        manga.setImages(getFirstMatchDefault("Manga\" src=\"(.+?)\"", source, ""));
        // Summary
        String summary = getFirstMatchDefault("<p itemprop=\"description\">(.+?)</p>",
                source, "Keine inhaltsangabe").replaceAll("<.+?>", "");
        manga.setSynopsis(Html.fromHtml(summary.replaceFirst("Zusammenfassung:", "")).toString());
        // Status
        manga.setFinished(!getFirstMatchDefault("<b>Status:</b>(.+?)</a>", source, "").contains("Laufende"));
        // Author
        manga.setAuthor(getFirstMatchDefault("Autor.+?\">(.+?)<", source, ""));
        // Chapter
        Pattern p = Pattern.compile(
                "<a class=\"chapter_list_a\" href=\"(/chapter.+?)\" title=\"(.+?)\">(.+?)</a>");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(3), HOST + matcher.group(1)));
        }
        manga.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return chapter.getPath().replace(".html", "-" + page + ".html");
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        if (chapter.getExtra() == null)
            setExtra(chapter);
        String[] images = chapter.getExtra().split("\\|");
        return images[page];
    }

    private void setExtra(Chapter chapter) throws Exception {
        String source = getNavWithHeader().get(
                chapter.getPath().replace(".html", "-" + chapter.getPages() + "-1.html"));
        Pattern p = Pattern.compile("<img class=\"manga_pic.+?src=\"([^\"]+)");
        Matcher m = p.matcher(source);
        String images = "";
        while (m.find()) {
            images = images + "|" + m.group(1);
        }
        chapter.setExtra(images);
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String source = getNavWithHeader().get(chapter.getPath());
        String nop = getFirstMatch(
                "\\d+/(\\d+)</option>[\\s]*</select>", source,
                "Es versäumt, die Anzahl der Seiten zu bekommen");
        chapter.setPages(Integer.parseInt(nop));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int category, int order, int pageNumber) throws Exception {
        String source = getNavWithHeader().get(
                HOST + DeNineManga.order[order] +
                        genreV[category].replace("_", "_" + pageNumber));
        return getMangasFromSource(source);
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile(
                "<a href=\"(/manga/[^\"]+)\"><img src=\"(.+?)\".+?alt=\"([^\"]+)\"");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(3), HOST + m.group(1), false);
            manga.setImages(m.group(2));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public String[] getCategories() {
        return genre;
    }

    @Override
    public String[] getOrders() {
        // "/category/", "/list/New-Update/", "/list/Hot-Book", "/list/New-Book/"
        return new String[]{"Manga Liste", "Updates", "Beliebte Manga", "Neue Manga"};
    }

    @Override
    public boolean hasList() {
        return false;
    }
}
