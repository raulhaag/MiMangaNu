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
            "<a href=\"(/r/[^\"]+)\">(.+?)</a>";
    private static final String PATTERN_MANGA =
            "<td><strong><a href=\"(.*?manga[^\"]+)\">([^<]+)";
    private static final String PATTERN_IMAGE =
            "src=\"(//[^/]+/cdn/manga/[^\"]+)";

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
                Manga manga = new Manga(getServerID(), matcher.group(2), HOST +  matcher.group(1), false);
                if (!tmpMangaPathList.contains(manga.getPath())) {
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
            while (matcher.find()) {
                manga.addChapterFirst(new Chapter(matcher.group(2), HOST + matcher.group(1)));
            }

            // Cover - use first image of latest chapter (if present)
            ArrayList<Chapter> chapters = manga.getChapters();
            manga.setImages("");
            if (!chapters.isEmpty()) {
                source = getNavigatorAndFlushParameters().get(
                        chapters.get(chapters.size() - 1).getPath());
                String image = getFirstMatchDefault(PATTERN_IMAGE, source, "");
                if (!image.isEmpty()) {
                    manga.setImages("http:" + image);
                }
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        // strip off initial page number (i.e. '1') and append requested page number
        String web = chapter.getPath().substring(0, chapter.getPath().length() - 1) + page;
        return "http:" + getFirstMatch(
                PATTERN_IMAGE, getNavigatorAndFlushParameters().get(web),
                context.getString(R.string.server_failed_loading_image));
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getPages() == 0) {
            String source = getNavigatorAndFlushParameters().get(chapter.getPath());
            if(source.contains("been removed from the website.")){
                throw new Exception("Licenced or removed chapter");
            }            String pageNumber = getFirstMatchDefault(
                    "Last Page \\((\\d+)\\)</a>", source, null);
            // handle case, where only one page is listed (as "First Page")
            if (pageNumber == null) {
                pageNumber = getFirstMatch(
                        "First Page \\((\\d+)\\)</a>", source,
                        context.getString(R.string.server_failed_loading_page_count));
            }
            chapter.setPages(Integer.parseInt(pageNumber));
        }
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
            Manga manga;
            if (matcher.group(1).startsWith("/"))
                manga = new Manga(getServerID(), matcher.group(2), HOST +  matcher.group(1), false);
            else
                manga = new Manga(getServerID(), matcher.group(2), matcher.group(1), false);
            if (!tmpMangaPathList.contains(manga.getPath())) {
                mangas.add(manga);
                tmpMangaPathList.add(manga.getPath());
            }
        }
        return mangas;
    }

    @Override
    public FilteredType getFilteredType() {
        return FilteredType.TEXT;
    }
}
