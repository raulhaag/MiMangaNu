package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;

/**
 * Created by xtj-9182 on 09.05.2017.
 */
/* Je suis Charlie */
class MangaKawaii extends ServerBase {
    private static final String HOST = "https://www.mangakawaii.com";

    MangaKawaii(Context context) {
        super(context);
        setFlag(R.drawable.flag_fr);
        setIcon(R.drawable.mangakawaii);
        setServerName("MangaKawaii");
        setServerID(MANGAKAWAII);
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        // FIXME a list of Manga could be retrieved using JavaScript
        return null;
    }

    @Override
    public boolean hasSearch() {
        return false;
    }

    @Override
    public ArrayList<Manga> search(String search) throws Exception {
        // FIXME search functionality has to be implemented bases on Manga list
        return null;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String source = getNavigatorAndFlushParameters().get(manga.getPath());

            // Cover
            if (manga.getImages() == null || manga.getImages().isEmpty()) {
                String img = getFirstMatchDefault("src='(https://www.mangakawaii.com/uploads/manga/[^\"]+?)'", source, "");
                manga.setImages(img);
            }

            // Summary
            manga.setSynopsis(getFirstMatchDefault("id=\"synopsis\">[\\s]*<p>(.+?)</p>", source, context.getString(R.string.nodisponible)));

            // Status
            manga.setFinished(!getFirstMatchDefault("Statut(.+?)</span>", source, "").contains("En Cours"));

            // Author
            manga.setAuthor(getFirstMatchDefault("author/.+?\">(.+?)</a>", source, context.getString(R.string.nodisponible)));

            // Genre
            manga.setGenre(getFirstMatchDefault("Genres</span>(.+?)</span>", source, context.getString(R.string.nodisponible)));

            // Chapters
            Pattern p = Pattern.compile("href=\"(https://www\\.mangakawaii\\.com/manga/[^\"]+?)\">([^\"]+?)</a>", Pattern.DOTALL);
            Matcher matcher = p.matcher(source);
            ArrayList<String> tmpChapterList = new ArrayList<>();
            while (matcher.find()) {
                if(!tmpChapterList.contains(matcher.group(1))) {
                    Chapter chapter = new Chapter(matcher.group(2), matcher.group(1));
                    chapter.addChapterFirst(manga);
                    tmpChapterList.add(matcher.group(1));
                }
            }
        }
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
        String source = getNavigatorAndFlushParameters().get(chapter.getPath());
        String tmpImages = getFirstMatchDefault("<div id=\"all\"(.+?)</div>", source, "");

        ArrayList<String> matches = getAllMatch("data-src='.(https://www\\.mangakawaii\\.com/uploads/[^\"]+?).'", tmpImages);
        chapter.setExtra(TextUtils.join("|", matches));

        return matches.size();
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getExtra() == null || chapter.getExtra().length() < 2) {
            chapter.setPages(setExtra(chapter));
        } else {
            chapter.setPages(0);
        }
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        // TODO implement filtering

        String web = HOST + "/liste-mangas?page=" + pageNumber;
        String source = getNavigatorAndFlushParameters().get(web);

        Pattern pattern = Pattern.compile("<div class=\"media-left\">(.+?)</div>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            Manga m = new Manga(getServerID(),
                    getFirstMatchDefault("alt='([^']+)", matcher.group(1), ""),
                    getFirstMatchDefault("href=\"([^\"]+)", matcher.group(1), ""),
                    false
            );
            m.setImages(getFirstMatchDefault("src='([^\']+)", matcher.group(1), ""));
            mangas.add(m);
        }
        return mangas;
    }
}
