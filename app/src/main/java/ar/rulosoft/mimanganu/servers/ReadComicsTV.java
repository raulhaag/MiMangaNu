package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.util.Log;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;

/**
 * Created by jtx on 04.12.2016.
 */
class ReadComicsTV extends ServerBase {
    private static String HOST = "http://readcomics.tv";

    private static String[] genre = new String[]{
            "Marvel", "DC Comics", "Vertigo", "Dark Horse", "Action",
            "Adventure", "Comedy", "Crime", "Cyborgs", "Demons",
            "Drama", "Fantasy", "Gore", "Graphic Novels", "Historical",
            "Horror", "Magic", "Martial Arts", "Mature", "Mecha",
            "Military", "Movie Cinematic Link", "Mystery", "Mythology", "Psychological",
            "Robots", "Romance", "Science Fiction", "Sports", "Spy",
            "Supernatural", "Suspense", "Tragedy"
    };

    ReadComicsTV() {
        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.readcomicstv);
        this.setServerName("ReadComicsTV");
        setServerID(ServerBase.READCOMICSTV);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        //?key=civil&wg=&wog=&status=
        String source = getNavigatorAndFlushParameters().get("http://readcomics.tv/advanced-search?key=" + URLEncoder.encode(term, "UTF-8"));
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern pattern = Pattern.compile("class=\"image\"><img src=\"(.+?)\" alt=\".+?<div class=\"mb-right\">[\\s]*<h3><a href=\"(.+?)\">(.+?)</a></h3>");
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            /*Log.d("RCTV","(3): "+matcher.group(3));
            Log.d("RCTV","(2): "+matcher.group(2));
            Log.d("RCTV","(1): "+matcher.group(1));*/
            Manga manga = new Manga(getServerID(), matcher.group(3), matcher.group(2), false);
            //manga.setImages(matcher.group(1));
            mangas.add(manga);
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

        // Front
        String img = getFirstMatchDefault("<div class=\"manga-image\"><img src=\"(.+?)\" id=\"", source, "");
        manga.setImages(img);

        // Summary
        String summary = getFirstMatchDefault("<p class=\"pdesc\">(.+?)</p>", source, "");
        manga.setSynopsis(summary.trim());

        // Status
        boolean status = !getFirstMatchDefault("<td><span>Status:</span></td>[\\s]*<td>(.+?)</td>", source, "").contains("Ongoing");
        manga.setFinished(status);

        // Author
        // <td><span>Author:</span></td>[\s]*<td>[\s]*(.+?)</td>
        String author = getFirstMatchDefault("<td><span>Author:</span></td>[\\s]*<td>[\\s]*([^/<>]+)</td>", source, "");
        manga.setAuthor(author.trim());

        // Genre
        String genre = Util.getInstance().fromHtml(getFirstMatchDefault("<td><span>Genre:</span></td>[\\s]*<td>(.+?)[\\s]*</td>", source, "")).toString().trim();
        manga.setGenre(genre);

        // Chapters
        Pattern p = Pattern.compile("class=\"ch-name\" href=\"(.+?)\">(.+?)</a>");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            Log.d("RMT", "(2): " + matcher.group(2));
            Log.d("RMT", "(1): " + matcher.group(1));
            chapters.add(0, new Chapter(matcher.group(2).trim(), matcher.group(1)));
        }
        Collections.reverse(chapters); // original is #1 to #7, we want #7 to #1
        manga.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return chapter.getPath() + "/" + page;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        if (chapter.getExtra() == null)
            setExtra(chapter);
        String[] images = chapter.getExtra().split("\\|");
        return images[page];
    }

    private void setExtra(Chapter chapter) throws Exception {
        String source = getNavigatorAndFlushParameters().get(chapter.getPath() + "/full");
        Pattern p = Pattern.compile("src=\"([^\"]+)\" alt=\"");
        Matcher matcher = p.matcher(source);
        String images = "";
        while (matcher.find()) {
            images = images + "|" + matcher.group(1);
        }
        chapter.setExtra(images);
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String source = getNavigatorAndFlushParameters().get(chapter.getPath());
        String pagenumber = getFirstMatch(
                "<div class=\"label\">of.(\\d+)</div>", source,
                "failed to get the number of pages");
        chapter.setPages(Integer.parseInt(pagenumber));
    }

    @Override
    public ServerFilter[] getServerFilters(Context context) {
        return new ServerFilter[]{
                new ServerFilter("Included Genre(s)", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Excluded Genre(s)", genre, ServerFilter.FilterType.MULTI),
        };
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        //?key=&wg=Marvel%2CVertigo&wog=DC+Comics%2CCrime&status=

        String web;
        if (filters[0].length == 0 && filters[1].length == 0) {
            if (pageNumber == 1)
                web = HOST + "/popular-comic/";
            else
                web = HOST + "/popular-comic/" + pageNumber;
        } else {
            String includedGenres = "";
            if (filters[0].length > 1) {
                for (int i = 0; i < filters[0].length; i++) {
                    includedGenres = includedGenres + genre[filters[0][i]].replaceAll(" ", "+"); // + "%2C"; // comma
                    if (i + 1 != filters[0].length)
                        includedGenres = includedGenres + "%2C"; // comma
                }
            } else if (filters[0].length > 0) {
                includedGenres = includedGenres + genre[filters[0][0]].replaceAll(" ", "+");
            }
            String excludedGenres = "";
            if (filters[1].length > 1) {
                for (int i = 0; i < filters[1].length; i++) {
                    excludedGenres = excludedGenres + genre[filters[1][i]].replaceAll(" ", "+"); // + "%2C"; // comma
                    if (i + 1 != filters[1].length)
                        excludedGenres = excludedGenres + "%2C"; // comma
                }
            } else if (filters[1].length > 0) {
                excludedGenres = excludedGenres + genre[filters[1][0]].replaceAll(" ", "+");
            }
            web = "http://readcomics.tv/advanced-search?key=&wg=" + includedGenres + "&wog=" + excludedGenres + "&status=";
        }

        //Log.d("RCTV", "web: " + web);
        String source = getNavigatorAndFlushParameters().get(web);
        // regex to generate genre ids: <li><div class="fchecbox v0" data-col="1"></div>(.+?)</li>
        Pattern pattern = Pattern.compile("class=\"image\"><img src=\"(.+?)\" alt=\".+?<div class=\"mb-right\">[\\s]*<h3><a href=\"(.+?)\">(.+?)</a></h3>");
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            //Log.d("RCTV","(3): "+matcher.group(3));
            //Log.d("RCTV","(2): "+matcher.group(2));
            //Log.d("RCTV","(1): "+matcher.group(1));
            Manga manga = new Manga(getServerID(), matcher.group(3), matcher.group(2), false);
            manga.setImages(matcher.group(1));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public boolean hasList() {
        return false;
    }
}

