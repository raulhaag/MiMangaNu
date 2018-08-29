package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;

/**
 * Created by xtj-9182 on 21.02.2017.
 */
class JapScan extends ServerBase {

    private static final String HOST = "http://www.japscan.cc";

    JapScan(Context context) {
        super(context);
        setFlag(R.drawable.flag_fr);
        setIcon(R.drawable.japscan);
        setServerName("JapScan");
        setServerID(JAPSCAN);
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String search) throws Exception {
        String source = getNavigatorAndFlushParameters().get(HOST + "/mangas/");
        Pattern pattern = Pattern.compile("<a href=\"(/mangas/[^\"].+?)\">(.+?)</a>", Pattern.DOTALL);
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
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String source = getNavigatorAndFlushParameters().get(manga.getPath());

            // Cover Image
            // JapScan has no cover images ...
            manga.setImages("");

            // Summary
            manga.setSynopsis(getFirstMatchDefault("<div id=\"synopsis\">(.+?)</div>", source, context.getString(R.string.nodisponible)));

            // Status
            manga.setFinished(!getFirstMatchDefault("<div class=\"row\">.+?<div class=\"cell\">.+?<div class=\"cell\">.+?<div class=\"cell\">.+?<div class=\"cell\">.+?<div class=\"cell\">(.+?)</div>", source, "").contains("En Cours"));

            // Author
            manga.setAuthor(getFirstMatchDefault("<div class=\"row\">(.+?)</div>", source, context.getString(R.string.nodisponible)));

            // Genres
            manga.setGenre(getFirstMatchDefault("<div class=\"row\">.+?<div class=\"cell\">.+?<div class=\"cell\">.+?<div class=\"cell\">(.+?)</div>", source, context.getString(R.string.nodisponible)));

            // Chapters
            Pattern pattern = Pattern.compile("<a href=\"[//www\\.japscan\\.cc]*(/lecture-en-ligne/[^\"]+?)\">(Scan.+?)</a>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(source);
            while (matcher.find()) {
                manga.addChapterFirst(new Chapter(matcher.group(2), HOST + matcher.group(1)));
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        return chapter.getExtra().split("\\|")[page];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getPages() == 0) {
            String source = getNavigatorAndFlushParameters().get(chapter.getPath());
            ArrayList<String> images = getAllMatch("<option\\s+data-img=\"([^\"]+)\"", source);

            String baseImg = getFirstMatch("<img data-img.+?src=\"([^\"]+)", source,
                    context.getString(R.string.server_failed_loading_image));
            String imagesString = "|" + baseImg;
            baseImg = baseImg.substring(0, baseImg.lastIndexOf("/") + 1);
            int count = 1;
            for (int i = 0; i < images.size(); i++) {
                if (!images.get(i).matches("IMG__\\d+.jpg")) {
                    imagesString = imagesString + "|" + baseImg + images.get(i);
                    count++;
                }
            }
            chapter.setExtra(imagesString);
            chapter.setPages(count);
        }
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern pattern = Pattern.compile("<a href=\"(/mangas/[^\"].+?)\">(.+?)</a>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            Manga manga = new Manga(getServerID(), matcher.group(2), HOST + matcher.group(1), false);
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
        String source = getNavigatorAndFlushParameters().get(HOST + "/mangas/");
        return getMangasFromSource(source);
    }
}