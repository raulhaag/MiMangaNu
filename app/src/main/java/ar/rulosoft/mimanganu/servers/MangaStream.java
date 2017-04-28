package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;

/**
 * Created by xtj-9182 on 23.04.2017.
 */
class MangaStream extends ServerBase {
    private static String HOST = "http://mangastream.com";
    private static String[] order = {"All"};

    MangaStream(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.mangastream);
        this.setServerName("MangaStream");
        setServerID(ServerBase.MANGASTREAM);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String search) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        ArrayList<String> tmpMangaPathList = new ArrayList<>();
        String web = "http://mangastream.com/manga";
        String source = getNavigatorAndFlushParameters().get(web);

        Pattern pattern = Pattern.compile("href=\"(http://mangastream\\.com/manga/[^\"]+?)\">([^\"]+?)</a>");
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            if (matcher.group(2).toLowerCase().contains(search.toLowerCase())) {
                /*Log.d("MS", "1: " + matcher.group(1));
                Log.d("MS", "2: " + matcher.group(2));*/
                Manga manga = new Manga(getServerID(), matcher.group(2), matcher.group(1), false);
                if(!tmpMangaPathList.contains(manga.getPath())) {
                    mangas.add(manga);
                    tmpMangaPathList.add(manga.getPath());
                }
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
        //Log.d("MS", "m.p: " + manga.getPath());

        // no Summary

        // no Status

        // no Authors

        // no Genres

        // Chapters
        Pattern p = Pattern.compile("href=\"(http://readms\\.net/[^\"]+?)\">([^\"]+?)</a>");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        String latestChapterPath = "";
        while (matcher.find()) {
            /*Log.d("MS", "1: " + matcher.group(1));
            Log.d("MS", "2: " + matcher.group(2));*/
            chapters.add(0, new Chapter(matcher.group(2), matcher.group(1)));
            if (latestChapterPath.isEmpty())
                latestChapterPath = matcher.group(1);
        }
        manga.setChapters(chapters);

        // Cover
        if (manga.getImages() == null || manga.getImages().isEmpty()) {
            manga.setImages(generateImageLink(latestChapterPath));
        }
    }

    private String generateImageLink(String firstLink) {
        String image = "";
        try {
            //Log.d("MS", "in: " + firstLink);
            String chapterLink;
            if (!firstLink.contains("http://readms")) {
                String source2 = getNavigatorAndFlushParameters().get(firstLink);
                chapterLink = getFirstMatchDefault("href=\"(http://readms\\.net/r/[^\"]+?)\">", source2, "");
            } else {
                chapterLink = firstLink;
            }
            //Log.d("MS", "chapterLink: " + chapterLink);
            String source3 = getNavigatorAndFlushParameters().get(chapterLink);
            image = getFirstMatchDefault("\"(//img\\.readms\\.net/cdn/manga/[^\"]+?)\"", source3, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (image.length() > 2) {
            image = "http:" + image;
            Log.d("MS", "image: " + image);
        }
        return image;
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return chapter.getPath().replace("/1", "/") + page;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        //Log.d("MS", "source: " + this.getPagesNumber(chapter, page));
        String source = "";
        try {
            source = getNavigatorAndFlushParameters().get(this.getPagesNumber(chapter, page));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String img = "";
        try {
            img = getFirstMatchDefault("\"(//img\\.readms\\.net/cdn/manga/[^\"]+?)\"", source, "Error getting image");
        } catch (Exception e) {
            e.printStackTrace();
        }
        img = "http:" + img;
        //Log.d("MS", "img: " + img);
        return img;
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String source = getNavigatorAndFlushParameters().get(chapter.getPath());
        //Log.d("MS", "p: " + chapter.getPath());
        String pageNumber = getFirstMatchDefault("Last Page \\((\\d+)\\)</a>", source,
                "failed to get the number of pages");
        //Log.d("MS", "pa: " + pageNumber);
        chapter.setPages(Integer.parseInt(pageNumber));
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter("Order", order, ServerFilter.FilterType.SINGLE)
        };
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern pattern = Pattern.compile("href=\"(http://mangastream\\.com/manga/[^\"]+?)\">([^\"]+?)</a>");
        final Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        ArrayList<String> tmpMangaPathList = new ArrayList<>();
        while (matcher.find()) {
            /*Log.d("MS", "1: " + matcher.group(1));
            Log.d("MS", "2: " + matcher.group(2));*/
            Manga manga = new Manga(getServerID(), matcher.group(2), matcher.group(1), false);
            if(!tmpMangaPathList.contains(manga.getPath())) {
                mangas.add(manga);
                tmpMangaPathList.add(manga.getPath());
            }
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = "http://mangastream.com/manga";
        String source = getNavigatorAndFlushParameters().get(web);
        return getMangasFromSource(source);
    }

    @Override
    public boolean hasList() {
        return false;
    }
}

