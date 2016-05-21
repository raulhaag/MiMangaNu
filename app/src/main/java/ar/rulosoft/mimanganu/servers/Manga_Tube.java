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
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 || forceReload)
            loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {

        Navegador nav = getNavWithHeader();
        nav.addPost("adult","true");
        String source = nav.post(manga.getPath());
        // Front
        manga.setImages(getFirstMatchDefault("<img src=\"(http://www.manga-tube.com/content/comics\\/.+?)\"", source, ""));
        // Summary
        String summary = getFirstMatchDefault("<li><b>Beschreibung</b>:(.*?)</li>",
                source, "Keine inhaltsangabe").replaceAll("<.+?>", "");
        manga.setSynopsis(Html.fromHtml(summary.replaceFirst("Zusammenfassung:", "")).toString());

        // Chapter
        Pattern p = Pattern.compile(
                "<a href=\"(http://www.manga-tube.com/reader/read.+?)\"[^>]+>(.+?)<");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(2), matcher.group(1)));
        }
        manga.setChapters(chapters);
    }


    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return chapter.getPath() + "page/"+ page;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        Navegador nav = getNavWithHeader();
        nav.addPost("adult","true");
        String source = nav.post(getPagesNumber(chapter,page));
        return getFirstMatch("<img class=\"open\" src=\"(.+?)\"",source,"Error getting image");
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        Navegador nav = getNavWithHeader();
        nav.addPost("adult","true");
        String source = nav.post(chapter.getPath());
        int pages = Integer.parseInt(getFirstMatch("<div class=\"tbtitle dropdown_parent dropdown_right\"><div class=\"text\">(\\d+)",source,"Error"));
        chapter.setPages(pages);
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
