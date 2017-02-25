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
 * Created by xtj-9182 on 13.02.2016.
 */
class GoGoComic extends ServerBase {
    private static String HOST = "http://gogocomic.net";
    private static String[] genre = new String[]{
            "All",
            //"-",
            "Action",
            "Adventure",
            "Chinese Comics",
            "Comedy",
            "Crime",
            "Cyborgs",
            "Dark Horse",
            "DC Comics",
            "Demons",
            "Drama",
            "Family",
            "Fantasy",
            "Fiction",
            "Gore",
            "Graphic Novels",
            "Historical",
            "Horror",
            "Japanese Comics",
            "Korean Comics",
            "Magic",
            "Manga",
            "Manhua",
            "Manhwa",
            "Martial Arts",
            "Marvel",
            "Mature",
            "Mecha",
            "Military",
            "Millitary",
            "Movie Cinematic Link",
            "Mystery",
            "Mythology",
            "Non",
            "Psychological",
            "Robots",
            "Romance",
            "Sci-Fi",
            "Science Fiction",
            "Sports",
            "Spy",
            "Steampunk",
            "Superhero",
            "Supernatural",
            "Suspense",
            "Thriller",
            "Vertigo"
    };
    private static String genreVV = "/genre/";

    GoGoComic(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.gogocomic);
        this.setServerName("GoGoComic");
        setServerID(ServerBase.GOGOCOMIC);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String search) throws Exception {
        String source = getNavigatorAndFlushParameters().get("http://gogocomic.net/search/" + URLEncoder.encode(search, "UTF-8") + ".html");
        return getMangasFromSourcePopular(source);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 || forceReload)
            loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        String source = getNavigatorAndFlushParameters().get(manga.getPath());

        // detect who provides the content
        String chapterLink = getFirstMatchDefault("</span><a href='(.+?)'>(.+?)</a>", source, "");
        //Log.d("GO", "chapterlink: " + HOST + chapterLink);
        String source1 = getNavigatorAndFlushParameters().get(HOST + chapterLink);
        String contentProvidedBy = getFirstMatchDefault("<img src='(.+?)' alt=\"", source1, "");
        if(contentProvidedBy.isEmpty())
            contentProvidedBy = getFirstMatchDefault("src='([^\"]+?)' alt=\"", source1, "");
        //Log.d("GO", "contentP0: " + contentProvidedBy);
        contentProvidedBy = getFirstMatchDefault("http://(.+?)/", contentProvidedBy, "");
        //Log.d("GO", "contentP1: " + contentProvidedBy);

        // Cover Img
        //Log.d("GO", "m.gI0: " + manga.getImages());

        // Summary
        String views = getFirstMatchDefault("<p>Viewed:(.+?)</p>",
                source,"");
        String summary = getFirstMatchDefault("<p>Summary:(.+?)</p>",
                source, defaultSynopsis);
        if(!contentProvidedBy.isEmpty())
            manga.setSynopsis(Util.getInstance().fromHtml(summary).toString().trim() + "\n\n" + "Viewed:" + views + "\n\n" + "content provided by: " + contentProvidedBy);
        else
            manga.setSynopsis(Util.getInstance().fromHtml(summary).toString().trim() + "\n\n" + "Viewed:" + views);

        // Status
        manga.setFinished(!getFirstMatchDefault("<p>Status:(.+?)</p>", source, "").contains("Ongoing"));

        // Author
        // GoGoComic lists no authors ...

        // Genre
        manga.setGenre(Util.getInstance().fromHtml(getFirstMatchDefault("<li>Genres:(.+?)</ul>", source, "").replaceAll("</a></li><li>","</a></li>, <li>")).toString().replaceAll("\n\n",""));

        // Chapters
        Pattern p = Pattern.compile("</span><a href='(.+?)'>(.+?)</a>");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            /*Log.d("GO", "1: " + HOST + matcher.group(1));
            Log.d("GO", "2: " + matcher.group(2));*/
            chapters.add(0, new Chapter(matcher.group(2), HOST + matcher.group(1)));
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
        String source = getNavigatorAndFlushParameters().get(chapter.getPath());
        Pattern pattern = Pattern.compile("<img src='(.+?)' alt=\"");
        Matcher matcher = pattern.matcher(source);
        String images = "";
        int i = 0;
        while (matcher.find()) {
            i++;
            //Log.d("GO", "(1_0): " + matcher.group(1));
            images = images + "|" + matcher.group(1);
        }

        if (i == 0) {
            Pattern pattern1 = Pattern.compile("src='([^\"]+?)' alt=\"");
            Matcher matcher1 = pattern1.matcher(source);
            while (matcher1.find()) {
                i++;
                //Log.d("GO", "(1_1): " + matcher1.group(1));
                images = images + "|" + matcher1.group(1);
            }
        }
        chapter.setExtra(images);
        return i;
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
                new ServerFilter("Genre", genre, ServerFilter.FilterType.SINGLE)
        };
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web;
        if (genre[filters[0][0]].equals("All")) {
            web = HOST + "/popular/" + pageNumber + ".html";
        } else {
            web = HOST + genreVV + genre[filters[0][0]].replaceAll(" ", "-").toLowerCase() + ".html";
            //Log.d("GO", "web: " + web);
            String source = getNavigatorAndFlushParameters().get(web);
            return getMangasFromSourceGenre(source);
        }
        //Log.d("GO", "web: " + web);
        String source = getNavigatorAndFlushParameters().get(web);
        return getMangasFromSourcePopular(source);
    }

    private ArrayList<Manga> getMangasFromSourceGenre(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        // regex to generate genre ids: <ul class="list-genre"(.+?)</ul>
        Pattern pattern = Pattern.compile("<li><a href='(/comic/[^\"<>]+)\\.html' title=\"(.+?)\">");
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            /*Log.d("GO", "(1): " + matcher.group(1));
            Log.d("GO", "(2): " + matcher.group(2));
            Log.d("GO", "(3): " + matcher.group(3));*/
            Manga manga = new Manga(getServerID(), matcher.group(2), HOST + matcher.group(1) + ".html", false);
            //manga.setImages(matcher.group(2));
            mangas.add(manga);
        }
        return mangas;
    }

    private ArrayList<Manga> getMangasFromSourcePopular(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        // regex to generate genre ids: <ul class="list-genre"(.+?)</ul>
        Pattern pattern = Pattern.compile("<div class='img'><a href='([^\"<>]+?)\\.html'><img src='(.+?)' alt=\"(.+?)\"></a></div>");
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            /*Log.d("GO", "(1): " + matcher.group(1));
            Log.d("GO", "(2): " + matcher.group(2));
            Log.d("GO", "(3): " + matcher.group(3));*/
            Manga manga = new Manga(getServerID(), matcher.group(3), HOST + matcher.group(1) + ".html", false);
            manga.setImages(matcher.group(2));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public boolean hasList() {
        return false;
    }
}