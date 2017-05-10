package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;

/**
 * Created by xtj-9182 on 01.12.2016.
 */
class ReadMangaToday extends ServerBase {
    private static String HOST = "http://www.readmanga.today";
    private static String[] genre = new String[]{
            "All",
            "Action",
            "Adventure",
            "Comedy",
            "Doujinshi",
            "Drama",
            "Ecchi",
            "Fantasy",
            "Gender Bender",
            "Harem",
            "Historical",
            "Horror",
            "Josei",
            "Lolicon",
            "Martial Arts",
            "Mature",
            "Mecha",
            "Mystery",
            "One shot",
            "Psychological",
            "Romance",
            "School Life",
            "Sci-fi",
            "Seinen",
            "Shotacon",
            "Shoujo",
            "Shoujo Ai",
            "Shounen",
            "Shounen Ai",
            "Slice of Life",
            "Smut",
            "Sports",
            "Supernatural",
            "Tragedy",
            "Yaoi",
            "Yuri"
    };
    private static String genreVV = "/category/";

    ReadMangaToday(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.readmangatoday);
        this.setServerName("ReadMangaToday");
        setServerID(ServerBase.READMANGATODAY);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String search) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String web = "http://www.readmanga.today/manga-list/";
        if (Character.isLetter(search.charAt(0)))
            web = web + search.toLowerCase().charAt(0);
        int count = -1;
        String[] alphabet = {"t", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "v", "w", "x", "y", "z"};
        while (mangas.isEmpty()) {
            //Log.d("RMT", "web: " + web);
            String source = getNavigatorAndFlushParameters().get(web);
            Pattern pattern = Pattern.compile("<a href=\"(http://www\\.readmanga\\.today/[^\"]+?)\">(.+?)</a>");
            Matcher matcher = pattern.matcher(source);
            while (matcher.find()) {
                if (matcher.group(2).toLowerCase().contains(search.toLowerCase())) {
                    /*Log.d("RMT", "1: " + matcher.group(1));
                    Log.d("RMT", "2: " + matcher.group(2));*/
                    Manga manga = new Manga(getServerID(), matcher.group(2), matcher.group(1), false);
                    mangas.add(manga);
                }
            }
            if (count == alphabet.length)
                break;
            if (count == -1)
                web = "http://www.readmanga.today/manga-list/";
            else {
                web = "http://www.readmanga.today/manga-list/";
                web = web + alphabet[count];
            }
            count++;
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
        String source = getNavigatorAndFlushParameters().getAndReturnResponseCodeOnFailure(manga.getPath());
        if (source.equals("400")) {
            // ReadMangaToday returns 400 Bad Request sometimes
            // deleting it's cookies will usually get rid of the error
            Util.getInstance().removeSpecificCookies(context, HOST);
        }

        // Cover
        if (manga.getImages() == null || manga.getImages().isEmpty() || manga.getImages().contains("thumb")) {
            String img = getFirstMatchDefault("<div class=\"col-md-3\">.+?<img src=\"(.+?)\" alt=", source, "");
            manga.setImages(img);
        }

        // Summary
        String summary = getFirstMatchDefault("<li class=\"list-group-item movie-detail\">(.+?)</li>", source, "");
        manga.setSynopsis(Util.getInstance().fromHtml(summary.trim()).toString());

        // Status
        boolean status = !getFirstMatchDefault("<dt>Status:</dt>.+?<dd>(.+?)</dd>", source, "").contains("Ongoing");
        manga.setFinished(status);

        // Author
        String author = "";
        //String author = getFirstMatchDefault("<li class=\"director\">.+?<li><a href=\".+?\">(.+?)</a>", source, "");
        Pattern p1 = Pattern.compile("<li><a href=\"http://www\\.readmanga\\.today/people/[^\"]+?\">([^\"]+?)</a>");
        Matcher matcher1 = p1.matcher(source);
        while (matcher1.find()) {
            //Log.d("RMT", "(1): " + matcher1.group(1));
            if (!author.equals(matcher1.group(1) + ", ")) {
                author += matcher1.group(1);
                author += ", ";
            }
        }
        if (author.endsWith(", "))
            author = author.substring(0, author.length() - 2);
        manga.setAuthor(author);

        // Genre
        String genre = Util.getInstance().fromHtml(getFirstMatchDefault("<dt>Categories:</dt>.+?<dd>(.+?)</dd>", source, "").replaceAll("</a>", ",</a>")).toString().trim();
        //Log.d("RMT", "g: " + genre);
        if (genre.endsWith(","))
            genre = genre.substring(0, genre.length() - 1);
        manga.setGenre(genre);

        // Chapters
        //<li>.+?<a href="(.+?)">.+?<span class="val"><span class="icon-arrow-2"></span>(.+?)</span>
        Pattern p = Pattern.compile("<li>[\\s]*<a href=\"([^\"]+?)\">[\\s]*<span class=\"val\"><span class=\"icon-arrow-.\"></span>(.+?)</span>");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            /*Log.d("RMT", "(2): " + matcher.group(2).trim());
            Log.d("RMT", "(1): " + matcher.group(1));*/
            chapters.add(0, new Chapter(matcher.group(2).trim(), matcher.group(1)));
        }
        manga.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return null;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        if (chapter.getExtra() == null)
            setExtra(chapter);
        String[] images = chapter.getExtra().split("\\|");
        return images[page];
    }

    private void setExtra(Chapter chapter) throws Exception {
        String source = getNavigatorAndFlushParameters().getAndReturnResponseCodeOnFailure(chapter.getPath() + "/all-pages");
        if (source.equals("400")) {
            Util.getInstance().removeSpecificCookies(context, HOST);
        }
        //Log.d("RMT", "s: " + source);
        Pattern p = Pattern.compile("<img src=\"([^\"]+)\" class=\"img-responsive-2\">");
        Matcher matcher = p.matcher(source);
        String images = "";
        while (matcher.find()) {
            //Log.d("RMT","(1): "+matcher.group(1));
            images = images + "|" + matcher.group(1);
        }
        chapter.setExtra(images);
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String source = getNavigatorAndFlushParameters().getAndReturnResponseCodeOnFailure(chapter.getPath());
        if (source.equals("400")) {
            Util.getInstance().removeSpecificCookies(context, HOST);
        }
        //Log.d("RMT","p: "+chapter.getPath());
        String pageNumber = getFirstMatchDefault("\">(\\d+)</option>[\\s]*</select>", source,
                "failed to get the number of pages");
        //Log.d("RMT","pa: "+pageNumber);
        chapter.setPages(Integer.parseInt(pageNumber));
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter("Genre(s)", genre, ServerFilter.FilterType.SINGLE),
        };
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web;
        if (genre[filters[0][0]].equals("All")) {
            if(pageNumber == 1)
                web = HOST + "/hot-manga/";
            else
                web = HOST + "/hot-manga/" + pageNumber;
        }
        else
            web = HOST + genreVV + genre[filters[0][0]].toLowerCase().replaceAll(" ","-") +"/"+ pageNumber;
        //Log.d("RMT", "web: " + web);
        String source = getNavigatorAndFlushParameters().getAndReturnResponseCodeOnFailure(web);
        if (source.equals("400")) {
            Util.getInstance().removeSpecificCookies(context, HOST);
        }
        // regex to generate genre ids: <li>.+?title="All Categories - (.+?)">
        Pattern pattern = Pattern.compile("<div class=\"left\">.+?<a href=\"(.+?)\" title=\"(.+?)\"><img src=\"(.+?)\" alt=\"");
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            /*Log.d("RMT","(2): "+matcher.group(2));
            Log.d("RMT","(1): "+matcher.group(1));
            Log.d("RMT","(3): "+matcher.group(3));*/
            Manga m = new Manga(getServerID(), matcher.group(2), matcher.group(1), false);
            //Log.d("RMT","img: "+matcher.group(3).replace("thumb/",""));
            m.setImages(matcher.group(3).replace("thumb/",""));
            mangas.add(m);
        }
        return mangas;
    }

    @Override
    public boolean hasList() {
        return false;
    }
}

