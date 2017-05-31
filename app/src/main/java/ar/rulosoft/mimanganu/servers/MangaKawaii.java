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
import ar.rulosoft.mimanganu.utils.Util;

/**
 * Created by xtj-9182 on 09.05.2017.
 */
/* Je suis Charlie */
class MangaKawaii extends ServerBase {
    private static String HOST = "https://www.mangakawaii.com";
    private static String[] genre = new String[]{
            "All"
    };

    MangaKawaii(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_fr);
        this.setIcon(R.drawable.mangakawaii);
        this.setServerName("MangaKawaii");
        setServerID(ServerBase.MANGAKAWAII);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String search) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String web = "https://www.mangakawaii.com/liste-mangas-texte";
        String source = getNavigatorAndFlushParameters().get(web);
        Pattern pattern = Pattern.compile("href=\"(https://www\\.mangakawaii\\.com/manga/[^\"]+?)\" style=\"display: inline-block\"><h6 style=\"margin: 0\">([^\"]+?)</h6></a>"); //manga-list(.+?)</ul>
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            /*Log.d("MKA", "1: " + matcher.group(1));
            Log.d("MKA", "2: " + matcher.group(2));*/
            if (matcher.group(2).toLowerCase().contains(search.toLowerCase())) {
                    /*Log.d("MKA", "1: " + matcher.group(1));
                    Log.d("MKA", "2: " + matcher.group(2));*/
                Manga manga = new Manga(getServerID(), matcher.group(2), matcher.group(1), false);
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

        // Cover
        if (manga.getImages() == null || manga.getImages().isEmpty()) {
            String img = getFirstMatchDefault("src='(https://www.mangakawaii.com/uploads/manga/[^\"]+?)'", source, "");
            manga.setImages(img);
        }

        // Summary
        String summary = getFirstMatchDefault("id=\"synopsis\">[\\s]*<p>(.+?)</p>", source, "");
        manga.setSynopsis(Util.getInstance().fromHtml(summary.trim()).toString());

        // Status
        boolean status = !getFirstMatchDefault("Statut(.+?)</span>", source, "").contains("En Cours");
        manga.setFinished(status);

        // Author
        String author = getFirstMatchDefault("author/.+?\">(.+?)</a>", source, "");
        manga.setAuthor(author);

        // Genre
        String genre = Util.getInstance().fromHtml(getFirstMatchDefault("Genres</span>(.+?)</span>", source, "")).toString();
        //Log.d("MKA", "g: " + genre);
        manga.setGenre(genre.replaceAll(",,",","));

        // Chapters
        Pattern p = Pattern.compile("href=\"(https://www\\.mangakawaii\\.com/manga/[^\"]+?)\">([^\"]+?)</a>");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        ArrayList<String> tmpChapterList = new ArrayList<>();
        while (matcher.find()) {
            /*Log.d("MKA", "(1): " + matcher.group(1));
            Log.d("MKA", "(2): " + matcher.group(2));*/
            if(!tmpChapterList.contains(matcher.group(1))) {
                /*Log.d("MKA", "(1): " + matcher.group(1));
                Log.d("MKA", "(2): " + matcher.group(2));*/
                chapters.add(0, new Chapter(matcher.group(2), matcher.group(1)));
                tmpChapterList.add(matcher.group(1));
            }
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
        String source = "";
        try {
            source = getNavigatorAndFlushParameters().get(chapter.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String tmpImages = getFirstMatchDefault("<div id=\"all\"(.+?)</div>", source,"");
        Pattern pattern = Pattern.compile("data-src='.(https://www\\.mangakawaii\\.com/uploads/[^\"]+?).'");
        Matcher matcher = pattern.matcher(tmpImages);
        String images = "";
        while (matcher.find()) {
            pages++;
            //Log.d("MKA", "1: " + matcher.group(1));
            images = images + "|" + matcher.group(1);
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

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter("Genre(s)", genre, ServerFilter.FilterType.SINGLE),
        };
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern pattern = Pattern.compile("<div class=\"img\">(.+?)</a>");
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            /*Log.d("MKA", "(0): " + matcher.group(1));
            Log.d("MKA", "(1): " + getFirstMatchDefault("href=\"([^\"]+?)\"", matcher.group(1), ""));
            Log.d("MKA", "(2): " + getFirstMatchDefault("src='([^\"]+?)'", matcher.group(1), ""));
            Log.d("MKA", "(3): " + getFirstMatchDefault("alt='([^\"]+?)'", matcher.group(1), ""));*/
            Manga m = new Manga(getServerID(), getFirstMatchDefault("alt='([^\"]+?)'", matcher.group(1), ""), getFirstMatchDefault("href=\"([^\"]+?)\"", matcher.group(1), ""), false);
            m.setImages(getFirstMatchDefault("src='([^\"]+?)'", matcher.group(1), ""));
            mangas.add(m);
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = "https://www.mangakawaii.com/liste-mangas?page=" + pageNumber;
        Log.d("MKA", "web: " + web);
        String source = getNavigatorAndFlushParameters().get(web);
        if (source.isEmpty()) // page 3 is broken for some reason ¯\_(ツ)_/¯
            source = getNavigatorAndFlushParameters().get(web + (pageNumber + 1));
        return getMangasFromSource(source);
    }

    @Override
    public boolean hasList() {
        return false;
    }
}

