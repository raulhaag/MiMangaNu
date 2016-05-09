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
 * Created by jtx on 07.05.2016.
 */
public class MangaEden extends ServerBase {

    public static String HOST = "http://www.mangaeden.com/";

    private static String[] genre = new String[]{
            "Everything","Action", "Adult",
            "Adventure", "Comedy", "Doujinshi", "Drama", "Ecchi", "Fantasy",
            "Gender Bender", "Harem", "Historical", "Horror", "Josei", "Martial Arts",
            "Mature", "Mecha", "Mystery", "One Shot", "Psychological", "Romance",
            "School Life", "Sci-fi", "Seinen", "Shoujo", "Shounen", "Slice of Life",
            "Smut", "Sports", "Supernatural", "Tragedy", "Webtoons", "Yaoi",
            "Yuri"
    };
    private static String[] genreV = new String[]{
            "", "4e70e91bc092255ef70016f8", "4e70e92fc092255ef7001b94",
            "4e70e918c092255ef700168e", "4e70e918c092255ef7001675", "4e70e928c092255ef7001a0a", "4e70e918c092255ef7001693", "4e70e91ec092255ef700175e", "4e70e918c092255ef7001676",
            "4e70e921c092255ef700184b", "4e70e91fc092255ef7001783", "4e70e91ac092255ef70016d8", "4e70e919c092255ef70016a8", "4e70e920c092255ef70017de", "4e70e923c092255ef70018d0",
            "4e70e91bc092255ef7001705", "4e70e922c092255ef7001877", "4e70e918c092255ef7001681", "4e70e91dc092255ef7001747", "4e70e919c092255ef70016a9", "4e70e918c092255ef7001677",
            "4e70e918c092255ef7001688", "4e70e91bc092255ef7001706", "4e70e918c092255ef700168b", "4e70e918c092255ef7001667", "4e70e918c092255ef700166f", "4e70e918c092255ef700167e",
            "4e70e922c092255ef700185a", "4e70e91dc092255ef700172e", "4e70e918c092255ef700166a", "4e70e918c092255ef7001672", "4e70ea70c092255ef7006d9c", "4e70e91ac092255ef70016e5",
            "4e70e92ac092255ef7001a57"
    };

    public MangaEden() {
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
        String source = getNavWithHeader().get("http://www.mangaeden.com/en/en-directory/?title="+ URLEncoder.encode(term, "UTF-8")+"&author=&artist=&releasedType=0&released=");
        return getMangasFromSource(source);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 || forceReload)
            loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        String source = getNavWithHeader().get(manga.getPath());
        // Front
        String image = getFirstMatchDefault("<div class=\"mangaImage2\"><img src=\"(.+?)\"", source, "");
        if(image.length() > 2)
            image = "http:" + image;
        manga.setImages(image);
        // Summary
        String summary = getFirstMatchDefault("mangaDescription\">(.+?)</h",
                source, "no synopsis").replaceAll("<.+?>", "");
        manga.setSynopsis(Html.fromHtml(summary).toString());
        // Status
        manga.setFinished(getFirstMatchDefault("Status</h(.+?)<h", source, "").contains("Completed"));
        // Author
        manga.setAuthor(Html.fromHtml(getFirstMatchDefault("Author</h4>(.+?)<h4>", source, "")).toString().trim().replaceAll("\n", ""));
        // Genres
        manga.setGenre((Html.fromHtml(getFirstMatchDefault("Genres</h4>(.+?)<h4>", source, "").replace("a><a", "a>, <a")).toString().trim()));
        // Chapters
        Pattern pattern = Pattern.compile(
                "<tr.+?href=\"(/en/en-manga/.+?)\".+?>(.+?)</a");
        Matcher matcher = pattern.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(Html.fromHtml(matcher.group(2)).toString(), HOST + matcher.group(1)));
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
            String source = getNavWithHeader().get(chapter.getPath());
            Pattern pattern = Pattern.compile("fs\":\\s*\"(.+?)\"");
            Matcher matcher = pattern.matcher(source);
            String images = "";
            while (matcher.find()) {
                images = images + "|" + "http:" + matcher.group(1);
            }
            chapter.setExtra(images);
        }
        return chapter.getExtra().split("\\|")[page];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        int pages = 0;
        if (chapter.getExtra() == null || chapter.getExtra().length() < 2) {
            String source = getNavWithHeader().get(chapter.getPath());
            Pattern pattern = Pattern.compile("fs\":\\s*\"(.+?)\"");
            Matcher matcher = pattern.matcher(source);
            String images = "";
            while (matcher.find()) {
                pages++;
                images = images + "|" + "http:" + matcher.group(1);
            }
            chapter.setExtra(images);
        }
        chapter.setPages(pages);
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categories, int order, int pageNumber) throws Exception {
        String web = HOST + "en/en-directory/" + "?page=" + pageNumber;
        if(categories > 0){
            web = web + "&categoriesInc=" + genreV[categories];
        }
        String source = getNavWithHeader().get(web);
        return getMangasFromSource(source);
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

    @Override
    public String[] getCategories() {
        return genre;
    }

    @Override
    public String[] getOrders() {
        return new String[]{"Popular"};
    }

    @Override
    public FilteredType getFilteredType() {
        return FilteredType.TEXT;
    }

    @Override
    public boolean hasList() {
        return false;
    }
}