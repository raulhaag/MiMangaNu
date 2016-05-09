package ar.rulosoft.mimanganu.servers;

import android.support.annotation.NonNull;
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
 * Created by Raul on 03/02/2016.
 */
public class RawSenManga extends ServerBase {

    public static String HOST = "http://raw.senmanga.com/";

    private static String[] generos = new String[]{
            "All", "Action", "Adult", "Adventure", "Comedy", "Cooking", "Drama", "Ecchi", "Fantasy", "Gender Bender", "Harem", "Historical", "Horror", "Josei", "Light Novel", "Martial Arts", "Mature", "Music", "Mystery", "Psychological", "Romance", "School Life", "Sci-Fi", "Seinen", "Shoujo", "Shoujo Ai", "Shounen", "Shounen Ai", "Slice of Life", "Smut", "Sports", "Supernatural", "Tragedy", "Webtoons", "Yuri"
    };
    private static String[] generosV = new String[]{"Manga/", "directory/category/Action/", "directory/category/Adult/", "directory/category/Adventure/", "directory/category/Comedy/", "directory/category/Cooking/", "directory/category/Drama/", "directory/category/Ecchi/", "directory/category/Fantasy/", "directory/category/Gender-Bender/", "directory/category/Harem/", "directory/category/Historical/", "directory/category/Horror/", "directory/category/Josei/", "directory/category/Light_Novel/", "directory/category/Martial_Arts/", "directory/category/Mature/", "directory/category/Music/", "directory/category/Mystery/", "directory/category/Psychological/", "directory/category/Romance/", "directory/category/School_Life/", "directory/category/Sci-Fi/", "directory/category/Seinen/", "directory/category/Shoujo/", "directory/category/Shoujo-Ai/", "directory/category/Shounen/", "directory/category/Shounen-Ai/", "directory/category/Slice_of_Life/", "directory/category/Smut/", "directory/category/Sports/", "directory/category/Supernatural/", "directory/category/Tragedy/", "directory/category/Webtoons/", "directory/category/Yuri/"
    };

    public RawSenManga() {
        this.setFlag(R.drawable.flag_raw);
        this.setIcon(R.drawable.senmanga);
        this.setServerName("SenManga");
        setServerID(ServerBase.RAWSENMANGA);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = new Navegador().get(HOST + "Manga/?order=text-version");
        Pattern p = Pattern.compile("<td><a href=\"(.+?)\">(.+?)<\\/a><\\/td><td><(a|b)");
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(2),HOST + m.group(1), false);
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String web = HOST + "Search.php?q=" + URLEncoder.encode(term,"UTF-8");
        String data = getNavWithHeader().get(web);
        Pattern p = Pattern.compile("<div class='search-results'>.+?<a href='(.+?)' title='(.+?)'");
        Matcher m = p.matcher(data);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()){
            Manga manga = new Manga(getServerID(),m.group(2),HOST + m.group(1),false);
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().size() == 0 || forceReload) {
            String data = getNavWithHeader().get(manga.getPath());
            String data2 = getFirstMatchDefault("<div class=\"series_desc\">(.+?)<\\/div>", data, "");
            manga.setSynopsis(Html.fromHtml(getFirstMatchDefault("<div itemprop=\"description\">(.+?)<", data2, "n/a")).toString());
            manga.setImages(HOST + getFirstMatchDefault("image\" src=\"(.+?)\"", data, ""));
            manga.setAuthor(Html.fromHtml(getFirstMatchDefault("Author:<\\/strong> <span class='desc'>(.+?)<\\/span>", data2, "n/a")).toString());
            manga.setGenre(Html.fromHtml(getFirstMatchDefault("in:<\\/strong><\\/p> (.+?)<\\/p>", data2, "n/a")).toString());
            manga.setFinished(data2.contains("Complete"));
            Pattern p = Pattern.compile("<td><a href=\"(/.+?)\" title=\"(.+?)\"");
            Matcher m = p.matcher(data);
            while (m.find()) {
                Chapter mc = new Chapter(m.group(2).trim(), HOST + m.group(1));
                mc.addChapterFirst(manga);
            }
        }
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload)
            loadChapters(manga, forceReload);
    }


    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return null;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        if (chapter.getExtra() == null) {
            String data = getNavWithHeader().get(chapter.getPath());
            chapter.setExtra(getFirstMatch("<img src=\".(vi.+?/)[^/]+?\"", data, "can't get image base"));
        }
        return HOST + chapter.getExtra() + page;
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data = getNavWithHeader().get(chapter.getPath());
        String number = getFirstMatch("</select> of (\\d+)", data, "Can't retrieve page quantity");
        chapter.setPages(Integer.parseInt(number));
        chapter.setExtra(getFirstMatch("<img src=\".(vi.+?/)[^/]+?\"", data, "can't get image base"));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception {
        String web = HOST + generosV[categorie] + "?page=" + pageNumber;
        String data = getNavWithHeader().get(web);
        return getMangasFromData(data);
    }

    @NonNull
    private ArrayList<Manga> getMangasFromData(String data) {
        Pattern p = Pattern.compile("<div class=\"cover\"><a href=\"\\/(.+?)\" title=\"(.+?)\"><img src=\"\\/(.+?)\"");
        Matcher m = p.matcher(data);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()){
            Manga manga = new Manga(getServerID(),m.group(2),HOST + m.group(1),false);
            manga.setImages(HOST + m.group(3));
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
        return new String[]{"Title"};
    }

    @Override
    public boolean hasList() {
        return true;
    }

}
