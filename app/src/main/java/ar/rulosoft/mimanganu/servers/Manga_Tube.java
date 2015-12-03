package ar.rulosoft.mimanganu.servers;

import android.text.Html;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;

/**
 * Created by Raul on 02/12/2015.
 */
public class Manga_Tube extends ServerBase {

    private static String[] genre = new String[]{
            "Alle",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "W", "X", "Y", "Z"
    };
    private static String[] genreV = new String[]{
            "",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "W", "X", "Y", "Z"
    };

    public Manga_Tube() {
        this.setFlag(R.drawable.flag_de);
        this.setIcon(R.drawable.mangatube);
        this.setServerName("Manga-tube");
        setServerID(ServerBase.MANGATUBE);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String source = getNavWithHeader().get("http://search-api.swiftype.com/api/v1/public/engines/search.embed?callback=jQuery181052988676800162_1449080309096&spelling=strict&per_page=50&page=1&q="+ URLEncoder.encode(term,"UTF-8")+"&engine_key=4YUjBG1L2kEywrZY1_RV&_=1449080411607");
        return getMangasFromSource(source);
    }


    @Override
    public void loadChapters(Manga m, boolean forceReload) throws Exception {
        if (m.getChapters() == null || m.getChapters().size() == 0 || forceReload)
            loadMangaInformation(m, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga m, boolean forceReload) throws Exception {
        String source = getNavWithHeader().get(m.getPath() + "?waring=1");
        // Front
        m.setImages(getFirstMatchDefault("p=\"image\" content=\"(.+?)\"", source, ""));
        // Summary
        String summary = getFirstMatchDefault("ion\" content=\"(.+?)\"",
                source, "Keine inhaltsangabe").replaceAll("<.+?>", "");
        m.setSynopsis(Html.fromHtml(summary.replaceFirst("Zusammenfassung:", "")).toString());

        // Chapter
        Pattern p = Pattern.compile(
                "<a href=\"(http://www.manga-tube.com/reader/read.+?)\"[^>]+>(.+?)<");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(2), matcher.group(1)));
        }
        m.setChapters(chapters);
    }


    @Override
    public String getPagesNumber(Chapter c, int page) {
        return null;
    }

    @Override
    public String getImageFrom(Chapter c, int page) throws Exception {
        if (c.getExtra() == null || c.getExtra().length() < 2) {

            String source = getNavWithHeader().get(c.getPath());

            Pattern p = Pattern.compile(",\"url\":\"(.+?)\"");
            Matcher m = p.matcher(source);
            String images = "";
            while (m.find()) {
                images = images + "|" + m.group(1);
            }
            c.setExtra(images.replaceAll("\\\\",""));
        }
        return c.getExtra().split("\\|")[page];
    }

    @Override
    public void chapterInit(Chapter c) throws Exception {
        int pages = 0;
        if (c.getExtra() == null || c.getExtra().length() < 2) {

            String source = getNavWithHeader().get(c.getPath());

            Pattern p = Pattern.compile(",\"url\":\"(.+?)\"");
            Matcher m = p.matcher(source);
            String images = "";
            while (m.find()) {
                pages++;
                images = images + "|" + m.group(1);
            }
            c.setExtra(images.replaceAll("\\\\",""));
        }
        c.setPages(pages);
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception {
        String source = getNavWithHeader().get("http://search-api.swiftype.com/api/v1/public/engines/search.embed?callback=jQuery181052988676800162_1449080309096&spelling=strict&per_page=50&page=" + pageNumber + "&q="+genreV[categorie]+"&engine_key=4YUjBG1L2kEywrZY1_RV&_=1449080411607");
        return getMangasFromSource(source);
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern p = Pattern.compile(",\"title\":\"(.+?)\".+?image\":\"(.+?)\".+?url\":\"(.+?)\"");
        Matcher m = p.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(1), m.group(3), false);
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
        return new String[]{""};
    }

    @Override
    public boolean hasList() {
        return false;
    }
}
