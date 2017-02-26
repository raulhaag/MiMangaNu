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
 * Created by jtx on 07.05.2016.
 */
class MangaEden extends ServerBase {

    public static String HOST = "http://www.mangaeden.com/";

    private static String[] genre = new String[]{
            "Action", "Adult",
            "Adventure", "Comedy", "Doujinshi", "Drama", "Ecchi", "Fantasy",
            "Gender Bender", "Harem", "Historical", "Horror", "Josei", "Martial Arts",
            "Mature", "Mecha", "Mystery", "One Shot", "Psychological", "Romance",
            "School Life", "Sci-fi", "Seinen", "Shoujo", "Shounen", "Slice of Life",
            "Smut", "Sports", "Supernatural", "Tragedy", "Webtoons", "Yaoi",
            "Yuri"
    };

    private static String[] genreV = new String[]{
            "4e70e91bc092255ef70016f8", "4e70e92fc092255ef7001b94",
            "4e70e918c092255ef700168e", "4e70e918c092255ef7001675", "4e70e928c092255ef7001a0a", "4e70e918c092255ef7001693", "4e70e91ec092255ef700175e", "4e70e918c092255ef7001676",
            "4e70e921c092255ef700184b", "4e70e91fc092255ef7001783", "4e70e91ac092255ef70016d8", "4e70e919c092255ef70016a8", "4e70e920c092255ef70017de", "4e70e923c092255ef70018d0",
            "4e70e91bc092255ef7001705", "4e70e922c092255ef7001877", "4e70e918c092255ef7001681", "4e70e91dc092255ef7001747", "4e70e919c092255ef70016a9", "4e70e918c092255ef7001677",
            "4e70e918c092255ef7001688", "4e70e91bc092255ef7001706", "4e70e918c092255ef700168b", "4e70e918c092255ef7001667", "4e70e918c092255ef700166f", "4e70e918c092255ef700167e",
            "4e70e922c092255ef700185a", "4e70e91dc092255ef700172e", "4e70e918c092255ef700166a", "4e70e918c092255ef7001672", "4e70ea70c092255ef7006d9c", "4e70e91ac092255ef70016e5",
            "4e70e92ac092255ef7001a57"
    };

    private static String[] type = new String[]{
            "Japanese Manga", "Korean Manhwa", "Chinese Manhua", "Comic", "Doujinshi"
    };

    private static String[] typeV = new String[]{
            "&type=0", "&type=1", "&type=2", "&type=3", "&type=4"
    };

    private static String[] status = new String[]{
            "Ongoing", "Completed", "Suspended"
    };

    private static String[] statusV = new String[]{
            "&status=1", "&status=2", "&status=0"
    };

    private static String[] order = new String[]{
            "Front page", "Views", "Latest Chapter", "Manga Title", "Chapters"
    };

    private static String[] orderV = new String[]{
            "", "&order=1", "&order=3", "&order=-0", "&order=2"
    };

    MangaEden(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.mangaeden);
        this.setServerName("MangaEden");
        setServerID(ServerBase.MANGAEDEN);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String source = getNavigatorAndFlushParameters().get("http://www.mangaeden.com/en/en-directory/?title=" + URLEncoder.encode(term, "UTF-8") + "&author=&artist=&releasedType=0&released=");
        return getMangasFromSource(source);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 || forceReload)
            loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        String source = getNavigatorAndFlushParameters().get(manga.getPath());
        // Front
        String image = getFirstMatchDefault("<div class=\"mangaImage2\"><img src=\"(.+?)\"", source, "");
        if(image.length() > 2)
            image = "http:" + image;
        manga.setImages(image);
        // Summary
        String summary = getFirstMatchDefault("mangaDescription\">(.+?)</h",
                source, defaultSynopsis).replaceAll("<.+?>", "");
        manga.setSynopsis(Util.getInstance().fromHtml(summary).toString());
        // Status
        manga.setFinished(getFirstMatchDefault("Status</h(.+?)<h", source, "").contains("Completed"));
        // Author
        manga.setAuthor(Util.getInstance().fromHtml(getFirstMatchDefault("Author</h4>(.+?)<h4>", source, "")).toString().trim().replaceAll("\n", ""));
        // Genres
        manga.setGenre((Util.getInstance().fromHtml(getFirstMatchDefault("Genres</h4>(.+?)<h4>", source, "").replace("a><a", "a>, <a")).toString().trim()));
        // Chapters
        Pattern pattern = Pattern.compile("<tr.+?href=\"(/en/en-manga/.+?)\".+?>(.+?)</a");
        Matcher matcher = pattern.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            /*Log.d("ME", "1: " + matcher.group(1));
            Log.d("ME", "2: " + matcher.group(2));*/
            chapters.add(0, new Chapter(Util.getInstance().fromHtml(matcher.group(2).replaceAll("Chapter", " Ch ")).toString(), HOST + matcher.group(1)));
        }
        manga.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return null;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        if (chapter.getExtra() == null || chapter.getExtra().length() < 2) {
            setExtra(chapter);
        }
        return chapter.getExtra().split("\\|")[page];
    }

    private int setExtra(Chapter chapter) throws Exception {
        int pages = 0;
        String source = getNavigatorAndFlushParameters().get(chapter.getPath());
        Pattern pattern = Pattern.compile("fs\":\\s*\"(.+?)\"");
        Matcher matcher = pattern.matcher(source);
        String images = "";
        while (matcher.find()) {
            pages++;
            //Log.d("ME", "1: " + "http:" + matcher.group(1));
            images = images + "|" + "http:" + matcher.group(1);
        }
        chapter.setExtra(images);
        return pages;
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getExtra() == null || chapter.getExtra().length() < 2) {
            chapter.setPages(setExtra(chapter));
        } else
            chapter.setPages(0);
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern pattern = Pattern.compile("<tr><td><a href=\"/en/en-manga/(.+?)\" class=\"(.+?)\">(.+?)</a>");
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            Manga manga = new Manga(getServerID(), matcher.group(3), HOST + "en/en-manga/" +  matcher.group(1), matcher.group(2).contains("close"));
            mangas.add(manga);
        }
        return mangas;
    }

    private ArrayList<Manga> getMangasFromFrontpage(String source) {
        String newSource = "";
        try {
            newSource = getFirstMatchDefault("<ul id=\"news\"(.+?)</ul>", source, "");
            //Log.d("ME","nS: "+newSource);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Pattern pattern1 = Pattern.compile("<img src=\"(//cdn\\.mangaeden\\.com/mangasimg/.+?)\".+?<div class=\"hottestInfo\">[\\s]*<a href=\"(/en/en-manga/[^\"<>]+?)\" class=.+?\">(.+?)</a>");
        Matcher matcher1;
        if (newSource.isEmpty())
            matcher1 = pattern1.matcher(source);
        else
            matcher1 = pattern1.matcher(newSource);
        ArrayList<Manga> mangas = new ArrayList<>();
        int i = 0;
        while (matcher1.find()) {
            i++;
            /*Log.d("ME", "(1): " + "http:" + matcher1.group(1));
            Log.d("ME", "(2): " + matcher1.group(2));
            Log.d("ME", "(3): " + matcher1.group(3));*/
            Manga manga = new Manga(getServerID(), Util.getInstance().fromHtml(matcher1.group(3)).toString(), HOST + matcher1.group(2), false);
            manga.setImages("http:" + matcher1.group(1));
            mangas.add(manga);
            //Log.d("ME", "i: " + i);
            if (newSource.isEmpty())
                if (i == 60)
                    break;
        }
        return mangas;
    }

    //http://www.mangaeden.com/en/en-directory/?status=2&author=&title=&releasedType=0&released=&artist=&type=0&categoriesInc=4e70e91bc092255ef70016f8&categoriesInc=4e70e91ec092255ef700175e&page=2


    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = HOST + "en/en-directory/?author=&title=";
        for (int i = 0; i < filters[3].length; i++) {
            web = web + statusV[filters[3][i]];
        }
        for (int i = 0; i < filters[1].length; i++) {
            web = web + "&categoriesInc=" + genreV[filters[1][i]];
        }
        for (int i = 0; i < filters[2].length; i++) {
            web = web + "&categoriesExcl=" + genreV[filters[2][i]];
        }
        web = web + "&artist=";
        for (int i = 0; i < filters[0].length; i++) {
            web = web + typeV[filters[0][i]];
        }
        if (orderV[filters[4][0]].equals("")) {
            web = "http://www.mangaeden.com/eng/";
            String source = getNavigatorAndFlushParameters().get(web);
            return getMangasFromFrontpage(source);
        } else {
            web = web + orderV[filters[4][0]] + "&page=" + pageNumber;
            String source = getNavigatorAndFlushParameters().get(web);
            return getMangasFromSource(source);
        }
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{new ServerFilter("Type", type, ServerFilter.FilterType.MULTI),
                new ServerFilter("Included Genre(s)", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Excluded Genre(s)", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Status", status, ServerFilter.FilterType.MULTI),
                new ServerFilter("Order", order, ServerFilter.FilterType.SINGLE)
        };
    }

    @Override
    public boolean hasList() {
        return false;
    }
}