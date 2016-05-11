package ar.rulosoft.mimanganu.servers;

import android.text.Html;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;

public class SubManga extends ServerBase {

    public SubManga() {
        setServerID(SUBMANGA);
        setIcon(R.drawable.submanga_icon);
        this.setServerName("SubManga");
        setFlag(R.drawable.flag_es);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        // <td><a href="(http://submanga.com/.+?)".+?</b>(.+?)<
        ArrayList<Manga> mangas = new ArrayList<>();
        String source = getNavWithHeader().get("http://submanga.com/series");
        Pattern p = Pattern.compile("<td><a href=\"(http://submanga.com/.+?)\".+?</b>(.+?)<");
        Matcher m = p.matcher(source);
        while (m.find()) {
            String name = m.group(2);
            if (!name.contains("!") && !name.contains("?") && !name.contains("�") && !name.contains("�")) {
                mangas.add(new Manga(SUBMANGA, name, m.group(1).toLowerCase(Locale.getDefault()), false));
            }
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().size() == 0 || forceReload) {
            Pattern p;
            Matcher m;
            String data = getNavWithHeader().get((manga.getPath() + "/completa"));
            p = Pattern.compile("<tr[^>]*><td[^>]*><a href=\"http://submanga.com/([^\"|#]+)\">(.+?)</a>");
            m = p.matcher(data);

            while (m.find()) {
                String web = "http://submanga.com/c" + m.group(1).substring(m.group(1).lastIndexOf("/"));
                Chapter mc = new Chapter(Html.fromHtml(m.group(2)).toString(), web);
                mc.addChapterFirst(manga);
            }
        }
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        Pattern p;
        Matcher m;
        String data = getNavWithHeader().get((manga.getPath()));

        p = Pattern.compile("<img src=\"(http://.+?)\"/><p>(.+?)</p>");
        m = p.matcher(data);

        if (m.find()) {
            manga.setImages(m.group(1));
            manga.setSynopsis(Html.fromHtml(m.group(2)).toString());
        } else {
            manga.setSynopsis("Sin sinopsis.");
        }
        manga.setAuthor(Html.fromHtml(getFirstMatchDefault("<p>Creado por ().+?</p>", data, "")).toString().trim());
        manga.setGenre(Html.fromHtml(getFirstMatchDefault("(<a class=\"b\" href=\"http://submanga.com/ge.+?</p>)", data, "")).toString().trim());
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return chapter.getPath() + "/" + page;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
            String data;
            data = getNavWithHeader().get(this.getPagesNumber(chapter, page));
            data = getFirstMatchDefault("<img[^>]+src=\"(http:\\/\\/.+?)\"", data, null);
        return data;
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data = getNavWithHeader().get(chapter.getPath());
        chapter.setPages(Integer.parseInt(getFirstMatch("(\\d+)<\\/option><\\/select>", data, "No se pudo obtener la cantidad de páginas")));
        if (chapter.getExtra() == null || chapter.getExtra().length() < 2) {
            data = getFirstMatchDefault("<img src=\"(http://.+?)\"", data, null);
            chapter.setExtra(data.substring(0, data.length() - 4));
        }
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getCategories() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getOrders() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public boolean hasFilteredNavigation() {
        return false;
    }

}
