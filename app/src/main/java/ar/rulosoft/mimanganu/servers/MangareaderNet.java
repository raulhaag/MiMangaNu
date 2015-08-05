package ar.rulosoft.mimanganu.servers;

import android.text.Html;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

/**
 * Created by Raul on 05/08/2015.
 */
public class MangareaderNet extends ServerBase {

    public MangareaderNet() {
        this.setFlag(R.drawable.flag_eng);
        this.setIcon(R.drawable.mangareadernet);
        this.setServerName("MangaReader");
        setServerID(ServerBase.MANGAREADERNET);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        String data = new Navegador().get("http://www.mangareader.net/alphabetical");
        Pattern p = Pattern.compile("<li><a href=\"(/[^\"]+)\">([^<]+)</a>(<span class=\"mangacompleted\">\\[Completed\\]</span>)*</li>");
        Matcher m = p.matcher(data);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(2), "http://www.mangareader.net" + m.group(1), !isNullOrVoid(m.group(3))));
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        return null;
    }

    @Override
    public void loadChapters(Manga m, boolean forceReload) throws Exception {
        if (m.getChapters() == null || m.getChapters().size() == 0 || forceReload)
            loadMangaInformation(m, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga m, boolean forceReload) throws Exception {
        String source = new Navegador().get(m.getPath());
        //sinopsis
        m.setSinopsis(Html.fromHtml(getFirstMacthDefault("<div id=\"readmangasum\"><h2>.+?</h2><p>(.+?)</p>", source, "Without synopsis.")).toString());
        //portada
        m.setImages(getFirstMacthDefault("<div id=\"mangaimg\"><img src=\"([^\"]+)\"", source, null));
        //autor
        m.setAuthor(getFirstMacthDefault("Author:</td><td>([^<]+)</td>", source, ""));
        //capitulos
        Pattern p = Pattern.compile("</div><a href=\"(/[^\"]+)\">([^<]+)</a>([^<]*)</td><td>\\d+/\\d+/\\d+</td>");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(new Chapter(matcher.group(2) + matcher.group(3), "http://www.mangareader.net" + matcher.group(1)));
        }
        m.setChapters(chapters);
        //status
        m.setFinished(getFirstMacthDefault("Status:</td><td>(.+?)<", source, "Ongoing").length() < 9);
    }

    @Override
    public String getPagesNumber(Chapter c, int page) {
        return c.getPath() + "/" + page;
    }

    @Override
    public String getImageFrom(Chapter c, int page) throws Exception {
        String data = new Navegador().get(getPagesNumber(c, page));
        return getFirstMacthDefault("src=\"(http:.+?)\"", data, "Plugin error");
    }

    @Override
    public void chapterInit(Chapter c) throws Exception {
        String data = new Navegador().get(c.getPath());
        String num = getFirstMacth("</select> of (\\d+)", data, "Plugin error");
        c.setPages(Integer.parseInt(num));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception {
        return null;
    }

    @Override
    public String[] getCategories() {
        return new String[0];
    }

    @Override
    public String[] getOrders() {
        return new String[0];
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public boolean hasVisualNavegation() {
        return false;
    }
}
