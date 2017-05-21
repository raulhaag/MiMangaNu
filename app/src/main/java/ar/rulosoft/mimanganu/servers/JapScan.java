package ar.rulosoft.mimanganu.servers;

import android.content.Context;

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
 * Created by xtj-9182 on 21.02.2017.
 */

class JapScan extends ServerBase {

    public static String HOST = "http://www.japscan.com";

    private static String[] order = new String[]{
            "All"
    };

    private static String[] orderV = new String[]{
            ""
    };

    JapScan(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_fr);
        this.setIcon(R.drawable.japscan);
        this.setServerName("JapScan");
        setServerID(ServerBase.JAPSCAN);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String search) throws Exception {
        String source = getNavigatorAndFlushParameters().get("http://www.japscan.com/mangas/");
        Pattern pattern = Pattern.compile("<a href=\"(/mangas/[^\"].+?)\">(.+?)</a>");
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            if (matcher.group(2).toLowerCase().contains(URLEncoder.encode(search.toLowerCase(), "UTF-8"))) {
                Manga manga = new Manga(getServerID(), matcher.group(2), HOST + matcher.group(1), false);
                mangas.add(manga);
            }
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
        String source = getNavigatorAndFlushParameters().get(manga.getPath());

        // Cover Image
        // JapScan has no cover images ...

        // Summary
        String summary = getFirstMatchDefault("<div id=\"synopsis\">(.+?)</div>", source, defaultSynopsis);
        manga.setSynopsis(Util.getInstance().fromHtml(summary).toString());

        // Status
        manga.setFinished(!getFirstMatchDefault("<div class=\"row\">.+?<div class=\"cell\">.+?<div class=\"cell\">.+?<div class=\"cell\">.+?<div class=\"cell\">.+?<div class=\"cell\">(.+?)</div>", source, "").contains("En Cours"));

        // Author
        manga.setAuthor(Util.getInstance().fromHtml(getFirstMatchDefault("<div class=\"row\">(.+?)</div>", source, "")).toString().trim());

        // Genres
        manga.setGenre((Util.getInstance().fromHtml(getFirstMatchDefault("<div class=\"row\">.+?<div class=\"cell\">.+?<div class=\"cell\">.+?<div class=\"cell\">(.+?)</div>", source, "")).toString().trim()));

        // Chapters
        Pattern pattern = Pattern.compile("<a href=\"(//www\\.japscan\\.com/lecture-en-ligne/[^\"]+?)\">(Scan.+?)</a>");
        Matcher matcher = pattern.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            /*Log.d("JS", "1: " + "http:" + matcher.group(1));
            Log.d("JS", "2: " + matcher.group(2));*/
            chapters.add(0, new Chapter(Util.getInstance().fromHtml(matcher.group(2)).toString(), "http:" + matcher.group(1)));
        }
        manga.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return null;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        //Log.d("JS", "source: " + chapter.getPath() + page + ".html");
        String source = getNavigatorAndFlushParameters().get(chapter.getPath() + page + ".html");
        String img = "";
        try {
            img = getFirstMatchDefault("src=\"(http://cdn.japscan.com/[^\"]+?)\"/>", source, "Error getting image");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Log.d("JS", "img: " + img);
        return img;
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String source = getNavigatorAndFlushParameters().get(chapter.getPath());
        //Log.d("JS", "p: " + chapter.getPath());
        String pagenumber = getFirstMatchDefault("Page (\\d+)</option>[\\s]*</select>", source, "failed to get the number of pages");
        //Log.d("JS", "pa: " + pagenumber);
        chapter.setPages(Integer.parseInt(pagenumber));
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern pattern = Pattern.compile("<a href=\"(/mangas/[^\"].+?)\">(.+?)</a>");
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            /*Log.d("JS", "1: " + matcher.group(1));
            Log.d("JS", "2: " + matcher.group(2));*/
            Manga manga = new Manga(getServerID(), matcher.group(2), HOST +  matcher.group(1), false);
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public FilteredType getFilteredType() {
        return FilteredType.TEXT;
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = HOST;
        if (orderV[filters[0][0]].equals("")) {
            web = "http://www.japscan.com/mangas/";
        }
        //Log.d("JS","web: "+web);
        String source = getNavigatorAndFlushParameters().get(web);
        return getMangasFromSource(source);
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter("Order", order, ServerFilter.FilterType.SINGLE)
        };
    }

    @Override
    public boolean hasList() {
        return false;
    }
}