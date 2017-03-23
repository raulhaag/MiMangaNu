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
 * Created by Raul on 03/12/2015.
 */
class MangaEdenIt extends ServerBase {

    public static String HOST = "http://www.mangaeden.com/";

    private static String[] genre = new String[]{
            "Avventura", "Azione", "Bara", "Commedia", "Demenziale",
            "Dounshinji","Drama","Ecchi","Fantasy","Harem","Hentai",
            "Horror","Josei","Magico","Mecha","Misteri","Musica",
            "Psicologico","Raccolta","Romantico","Sci-Fi","Scolastico","Seinen",
            "Sentimentale","Shota","Shoujo","Shounen","Sovrannaturale","Splatter",
            "Sportivo","Storico","Vita Quotidiana","Yaoi","Yuri"
    };

    private static String[] genreV = new String[]{
            "4e70ea8cc092255ef70073d3", "4e70ea8cc092255ef70073c3", "4e70ea90c092255ef70074b7", "4e70ea8cc092255ef70073d0", "4e70ea8fc092255ef7007475",
            "4e70ea93c092255ef70074e4","4e70ea8cc092255ef70073f9","4e70ea8cc092255ef70073cd","4e70ea8cc092255ef70073c4","4e70ea8cc092255ef70073d1","4e70ea90c092255ef700749a",
            "4e70ea8cc092255ef70073ce","4e70ea90c092255ef70074bd","4e70ea93c092255ef700751b","4e70ea8cc092255ef70073ef","4e70ea8dc092255ef700740a","4e70ea8fc092255ef7007456",
            "4e70ea8ec092255ef7007439","4e70ea90c092255ef70074ae","4e70ea8cc092255ef70073c5","4e70ea8cc092255ef70073e4","4e70ea8cc092255ef70073e5","4e70ea8cc092255ef70073ea",
            "4e70ea8dc092255ef7007432","4e70ea90c092255ef70074b8","4e70ea8dc092255ef7007421","4e70ea8cc092255ef70073c6","4e70ea8cc092255ef70073c7","4e70ea99c092255ef70075a3",
            "4e70ea8dc092255ef7007426","4e70ea8cc092255ef70073f4","4e70ea8ec092255ef700743f","4e70ea8cc092255ef70073de","4e70ea9ac092255ef70075d1"
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

    MangaEdenIt(Context context) {
        super(context);
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
        String source = getNavigatorAndFlushParameters().get("http://www.mangaeden.com/it/it-directory/?title=" + URLEncoder.encode(term, "UTF-8") + "&author=&artist=&releasedType=0&released=");
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
        // Stato
        manga.setFinished(getFirstMatchDefault("Stato</h(.+?)<h", source, "").contains("Completato"));
        // Autor
        manga.setAuthor(Util.getInstance().fromHtml(getFirstMatchDefault("Autore</h4>(.+?)<h4>", source, "")).toString().trim().replaceAll("\n", ""));
        // Genere
        manga.setGenre((Util.getInstance().fromHtml(getFirstMatchDefault("Genere</h4>(.+?)<h4>", source, "").replace("a><a", "a>, <a")).toString().trim()));
        // Chapters
        Pattern p = Pattern.compile("<tr.+?href=\"(/it/it-manga/.+?)\".+?>(.+?)</a");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(Util.getInstance().fromHtml(matcher.group(2).replaceAll("Capitolo", " Cap ")).toString(), HOST + matcher.group(1)));
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
        Pattern p = Pattern.compile("<tr><td><a href=\"/it/it-manga/(.+?)\" class=\"(.+?)\">(.+?)</a>");
        Matcher m = p.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(3), HOST + "it/it-manga/" +  m.group(1), m.group(2).contains("close"));
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

        Pattern pattern1 = Pattern.compile("<img src=\"(//cdn\\.mangaeden\\.com/mangasimg/.+?)\".+?<div class=\"hottestInfo\">[\\s]*<a href=\"(/it/it-manga/[^\"<>]+?)\" class=.+?\">(.+?)</a>");
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
                if (i == 40)
                    break;
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = HOST + "it/it-directory/?author=&title=";
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
            web = "http://www.mangaeden.com/it/";
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
