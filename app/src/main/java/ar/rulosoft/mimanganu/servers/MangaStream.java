package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;

/**
 * Created by xtj-9182 on 23.04.2017.
 */
class MangaStream extends ServerBase {
    private static final String HOST = "http://mangastream.com";

    private static final String PATTERN_CHAPTER =
            "href=\"(http://readms\\.net/[^\"]+?)\">([^\"]+?)</a>";
    private static final String PATTERN_MANGA =
            "href=\"(http://mangastream\\.com/manga/[^\"]+?)\">([^\"]+?)</a>";

    private static final String PATTERN_IMAGE =
            "\"(//img\\.readms\\.net/cdn/manga/[^\"]+?)\"";

    MangaStream(Context context) {
        super(context);
        setFlag(R.drawable.flag_en);
        setIcon(R.drawable.mangastream);
        setServerName("MangaStream");
        setServerID(MANGASTREAM);
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
        ArrayList<Manga> mangas = new ArrayList<>();
        ArrayList<String> tmpMangaPathList = new ArrayList<>();
        String web = HOST + "/manga";
        String source = getNavigatorAndFlushParameters().get(web);

        Pattern pattern = Pattern.compile(PATTERN_MANGA, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            if (matcher.group(2).toLowerCase().contains(search.toLowerCase())) {
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
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String source = getNavigatorAndFlushParameters().get(manga.getPath());

            // no Summary
            manga.setSynopsis(context.getString(R.string.nodisponible));

            // no Status
            manga.setFinished(true);

            // no Authors
            manga.setAuthor(context.getString(R.string.nodisponible));

            // no Genres
            manga.setGenre(context.getString(R.string.nodisponible));

            // Chapters
            Pattern p = Pattern.compile(PATTERN_CHAPTER, Pattern.DOTALL);
            Matcher matcher = p.matcher(source);
            String latestChapterPath = "";
            while (matcher.find()) {
                Chapter mc = new Chapter(matcher.group(2), matcher.group(1));
                mc.addChapterFirst(manga);
                if (latestChapterPath.isEmpty()) {
                    latestChapterPath = matcher.group(1);
                }
            }

            // Cover
            String image, chapterLink;
            if (!latestChapterPath.contains("http://readms")) {
                source = getNavigatorAndFlushParameters().get(latestChapterPath);
                chapterLink = getFirstMatchDefault("href=\"(http://readms\\.net/r/[^\"]+?)\">", source, "");
            } else {
                chapterLink = latestChapterPath;
            }

            source = getNavigatorAndFlushParameters().get(chapterLink);
            image = getFirstMatchDefault(PATTERN_IMAGE, source, null);
            if (image != null) {
                image = "http:" + image;
            }
            manga.setImages(image);
        }
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return chapter.getPath().replace("/1", "/") + page;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String src = getNavigatorAndFlushParameters().get(this.getPagesNumber(chapter, page));
        String img = getFirstMatchDefault(PATTERN_IMAGE, src, "Error getting image");
        return "http:" + img;
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String source = getNavigatorAndFlushParameters().get(chapter.getPath());
        String pageNumber = getFirstMatchDefault("Last Page \\((\\d+)\\)</a>", source,
                "failed to get the number of pages");
        chapter.setPages(Integer.parseInt(pageNumber));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = HOST + "/manga";
        String source = getNavigatorAndFlushParameters().get(web);

        Pattern pattern = Pattern.compile(PATTERN_MANGA, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(source);

        ArrayList<Manga> mangas = new ArrayList<>();
        ArrayList<String> tmpMangaPathList = new ArrayList<>();
        while (matcher.find()) {
            Manga manga = new Manga(getServerID(), matcher.group(2), matcher.group(1), false);
            if(!tmpMangaPathList.contains(manga.getPath())) {
                mangas.add(manga);
                tmpMangaPathList.add(manga.getPath());
            }
        }
        return mangas;
    }
}
