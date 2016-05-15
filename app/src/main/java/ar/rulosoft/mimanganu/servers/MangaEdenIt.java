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
 * Created by Raul on 03/12/2015.
 */
public class MangaEdenIt extends ServerBase {

    public static String HOST = "http://www.mangaeden.com/";

    private static String[] genre = new String[]{
            "Tutto","Avventura","Azione","Bara","Commedia","Demenziale",
            "Dounshinji","Drama","Ecchi","Fantasy","Harem","Hentai",
            "Horror","Josei","Magico","Mecha","Misteri","Musica",
            "Psicologico","Raccolta","Romantico","Sci-Fi","Scolastico","Seinen",
            "Sentimentale","Shota","Shoujo","Shounen","Sovrannaturale","Splatter",
            "Sportivo","Storico","Vita Quotidiana","Yaoi","Yuri"
    };
    private static String[] genreV = new String[]{
            "", "4e70ea8cc092255ef70073d3","4e70ea8cc092255ef70073c3","4e70ea90c092255ef70074b7","4e70ea8cc092255ef70073d0","4e70ea8fc092255ef7007475",
            "4e70ea93c092255ef70074e4","4e70ea8cc092255ef70073f9","4e70ea8cc092255ef70073cd","4e70ea8cc092255ef70073c4","4e70ea8cc092255ef70073d1","4e70ea90c092255ef700749a",
            "4e70ea8cc092255ef70073ce","4e70ea90c092255ef70074bd","4e70ea93c092255ef700751b","4e70ea8cc092255ef70073ef","4e70ea8dc092255ef700740a","4e70ea8fc092255ef7007456",
            "4e70ea8ec092255ef7007439","4e70ea90c092255ef70074ae","4e70ea8cc092255ef70073c5","4e70ea8cc092255ef70073e4","4e70ea8cc092255ef70073e5","4e70ea8cc092255ef70073ea",
            "4e70ea8dc092255ef7007432","4e70ea90c092255ef70074b8","4e70ea8dc092255ef7007421","4e70ea8cc092255ef70073c6","4e70ea8cc092255ef70073c7","4e70ea99c092255ef70075a3",
            "4e70ea8dc092255ef7007426","4e70ea8cc092255ef70073f4","4e70ea8ec092255ef700743f","4e70ea8cc092255ef70073de","4e70ea9ac092255ef70075d1"
    };

    public MangaEdenIt() {
        this.setFlag(R.drawable.flag_it);
        this.setIcon(R.drawable.mangaeden);
        this.setServerName("MangaEdentIt");
        setServerID(ServerBase.MANGAEDENIT);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String source = getNavWithHeader().get("http://www.mangaeden.com/it/it-directory/?title="+ URLEncoder.encode(term, "UTF-8")+"&author=&artist=&releasedType=0&released=");
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
                source, "Senza sinossi").replaceAll("<.+?>", "");
        manga.setSynopsis(Html.fromHtml(summary).toString());
        // Stato
        manga.setFinished(getFirstMatchDefault("Stato</h(.+?)<h", source, "").contains("Completato"));
        // Autor
        manga.setAuthor(Html.fromHtml(getFirstMatchDefault("Autore</h4>(.+?)<h4>", source, "")).toString().trim().replaceAll("\n", ""));
        // Genere
        manga.setGenre((Html.fromHtml(getFirstMatchDefault("Genere</h4>(.+?)<h4>", source, "").replace("a><a", "a>, <a")).toString().trim()));
        // Chapters
        Pattern p = Pattern.compile(
                "<tr.+?href=\"(/it/it-manga/.+?)\".+?>(.+?)</a");
        Matcher matcher = p.matcher(source);
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
            Pattern p = Pattern.compile("fs\":\\s*\"(.+?)\"");
            Matcher m = p.matcher(source);
            String images = "";
            while (m.find()) {
                images = images + "|" + "http:" + m.group(1);
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
            Pattern p = Pattern.compile("fs\":\\s*\"(.+?)\"");
            Matcher m = p.matcher(source);
            String images = "";
            while (m.find()) {
                pages++;
                images = images + "|" + "http:" + m.group(1);
            }
            chapter.setExtra(images);
        }
        chapter.setPages(pages);
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categories, int order, int pageNumber) throws Exception {
        String web = HOST + "it/it-directory/" + "?page=" + pageNumber;
        if(categories > 0){
            web = web + "&categoriesInc=" + genreV[categories];
        }
        String source = getNavWithHeader().get(web);
        return getMangasFromSource(source);
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern p = Pattern.compile("<tr><td><a href=\"/it/it-manga/(.+?)\" class=\"(.+?)\">(.+?)</a>");
        Matcher m = p.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(3), HOST + "it/it-manga/" +  m.group(1), m.group(2).contains("close"));
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
        return new String[]{"Popularite"};
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
