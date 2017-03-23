package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.support.annotation.NonNull;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;

/**
 * Created by Raul on 03/02/2016.
 */
class RawSenManga extends ServerBase {

    public static String HOST = "http://raw.senmanga.com/";

    private static String[] genre = new String[]{
            "All",
            "Action", "Adult", "Adventure", "Comedy",
            "Cooking", "Drama", "Ecchi", "Fantasy",
            "Gender Bender", "Harem", "Historical", "Horror",
            "Josei", "Light Novel", "Martial Arts", "Mature",
            "Music", "Mystery", "Psychological", "Romance",
            "School Life", "Sci-Fi", "Seinen", "Shoujo",
            "Shoujo Ai", "Shounen", "Shounen Ai", "Slice of Life",
            "Smut", "Sports", "Supernatural", "Tragedy",
            "Webtoons", "Yuri"
    };
    private static String[] genreV = new String[]{
            "Manga/",
            "directory/category/Action/", "directory/category/Adult/", "directory/category/Adventure/", "directory/category/Comedy/",
            "directory/category/Cooking/", "directory/category/Drama/", "directory/category/Ecchi/", "directory/category/Fantasy/",
            "directory/category/Gender-Bender/", "directory/category/Harem/", "directory/category/Historical/", "directory/category/Horror/",
            "directory/category/Josei/", "directory/category/Light_Novel/", "directory/category/Martial_Arts/", "directory/category/Mature/",
            "directory/category/Music/", "directory/category/Mystery/", "directory/category/Psychological/", "directory/category/Romance/",
            "directory/category/School_Life/", "directory/category/Sci-Fi/", "directory/category/Seinen/", "directory/category/Shoujo/",
            "directory/category/Shoujo-Ai/", "directory/category/Shounen/", "directory/category/Shounen-Ai/", "directory/category/Slice_of_Life/",
            "directory/category/Smut/", "directory/category/Sports/", "directory/category/Supernatural/", "directory/category/Tragedy/",
            "directory/category/Webtoons/", "directory/category/Yuri/"
    };
    private static String[] order = {"Most Popular", "Rating", "Title"};
    private static String[] orderV = {"Manga/?order=popular", "Manga/?order=rating", "Manga/?order=title"};

    RawSenManga(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_raw);
        this.setIcon(R.drawable.senmanga);
        this.setServerName("SenManga");
        setServerID(ServerBase.RAWSENMANGA);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigatorAndFlushParameters().get(HOST + "Manga/?order=text-version");
        Pattern p = Pattern.compile("\\d</td><td><a href=\"([^\"]+)\"\\s*>([^<]+)");
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
        String data = getNavigatorAndFlushParameters().get(web);
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
            String data = getNavigatorAndFlushParameters().get(manga.getPath());
            String data2 = getFirstMatchDefault("<div class=\"series_desc\">(.+?)<\\/div>", data, "");
            manga.setSynopsis(Util.getInstance().fromHtml(getFirstMatchDefault("<div itemprop=\"description\">(.+?)<", data2, defaultSynopsis)).toString());
            manga.setImages(HOST + getFirstMatchDefault("image\" src=\"(.+?)\"", data, ""));
            manga.setAuthor(Util.getInstance().fromHtml(getFirstMatchDefault("Author:<\\/strong> <span class='desc'>(.+?)<\\/span>", data2, "N/A")).toString());
            manga.setGenre(Util.getInstance().fromHtml(getFirstMatchDefault("in:<\\/strong><\\/p> (.+?)<\\/p>", data2, "N/A")).toString().trim());
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
            String data = getNavigatorAndFlushParameters().get(chapter.getPath());
            chapter.setExtra(getFirstMatchDefault("<img src=\".(vi.+?/)[^/]+?\"", data, "can't get image base"));
        }
        return HOST + chapter.getExtra() + page;
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data = getNavigatorAndFlushParameters().get(chapter.getPath());
        String number = getFirstMatchDefault("</select> of (\\d+)", data, "Can't retrieve page quantity");
        chapter.setPages(Integer.parseInt(number));
        chapter.setExtra(getFirstMatchDefault("<img src=\".(vi.+?/)[^/]+?\"", data, "can't get image base"));
    }

    @NonNull
    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern p = Pattern.compile("<div class=\"cover\"><a href=\"/(.+?)\" title=\"(.+?)\"><img src=\"/(.+?)\"");
        Matcher m = p.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()){
            Manga manga = new Manga(getServerID(),m.group(2),HOST + m.group(1),false);
            manga.setImages(HOST + m.group(3));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{new ServerFilter("Genre", genre, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Order", order, ServerFilter.FilterType.SINGLE)
        };
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = HOST + genreV[filters[0][0]] + "?page=" + pageNumber;
        if (genre[filters[0][0]].equals("All"))
            web = HOST + orderV[filters[1][0]] + "&page=" + pageNumber;
        String source = getNavigatorAndFlushParameters().get(web);
        return getMangasFromSource(source);
    }

    @Override
    public boolean hasList() {
        return true;
    }

}
