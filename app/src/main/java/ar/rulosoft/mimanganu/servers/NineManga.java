package ar.rulosoft.mimanganu.servers;

import android.text.Html;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;

/**
 * Created by jtx on 09.05.2016.
 */
public class NineManga extends ServerBase {
    private static String HOST = "http://ninemanga.com";

    private static String[] genre = new String[]{
            "Everything",
            "4 Koma",
            "4-Koma",
            "Action",
            "Adult",
            "Adventure",
            "Anime",
            "Award Winning",
            "Bara",
            "Comedy",
            "Cooking",
            "Demons",
            "Doujinshi",
            "Drama",
            "Ecchi",
            "Fantasy",
            "Gender Bender",
            "Harem",
            "Historical",
            "Horror",
            "Josei",
            "Live Action",
            "Magic",
            "Manhua",
            "Manhwa",
            "Martial Arts",
            "Matsumoto Tomokicomedy",
            "Mature",
            "Mecha",
            "Medical",
            "Military",
            "Music",
            "Mystery",
            "N/A",
            "None",
            "One Shot",
            "Oneshot",
            "Psychological",
            "Reverse Harem",
            "Romance",
            "Romance Shoujo",
            "School Life",
            "Sci Fi",
            "Sci-Fi",
            "Seinen",
            "Shoujo",
            "Shoujo Ai",
            "Shoujo-Ai",
            "Shoujoai",
            "Shounen",
            "Shounen Ai",
            "Shounen-Ai",
            "Shounenai",
            "Slice Of Life",
            "Smut",
            "Sports",
            "Staff Pick",
            "Super Power",
            "Supernatural",
            "Suspense",
            "Tragedy",
            "Vampire",
            "Webtoon",
            "Webtoons",
            "Yaoi",
            "Yuri",
            "[No Chapters]"
    };
    private static String[] genreV = new String[genre.length];

    private static String[] order = new String[]{
            "/category/", "/list/New-Update/", "/list/Hot-Book/", "/list/New-Book/"
    };

    public NineManga() {
        if(genreV[0] == null) {
            Thread t0 = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < genre.length; i++) {
                        //Log.d("NineManga: ", "i: " + i + " genre: " + genre[i]);
                        if (i == 0) {
                            genreV[i] = "index.html";
                        } else {
                            genreV[i] = genre[i] + ".html";
                        }
                        //Log.d("NineManga: ", "i: " + i + " genreV: " + genreV[i]);
                    }
                }
            });
            t0.start();
        }

        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.ninemanga);
        this.setServerName("NineManga");
        setServerID(ServerBase.NINEMANGA);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String source = getNavWithHeader().get(
                HOST + "/search/?wd=" + URLEncoder.encode(term, "UTF-8"));
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern pattern = Pattern.compile("bookname\" href=\"(/manga/[^\"]+)\">(.+?)<");
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            Manga manga = new Manga(getServerID(), matcher.group(2), HOST + matcher.group(1), false);
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
        String source = getNavWithHeader().get(manga.getPath() + "?waring=1");
        // Front
        manga.setImages(getFirstMatchDefault("Manga\" src=\"(.+?)\"", source, ""));
        // Summary
        String summary = getFirstMatchDefault("<p itemprop=\"description\">(.+?)</p>",
                source, "no synopsis").replaceAll("<.+?>", "");
        manga.setSynopsis(Html.fromHtml(summary.replaceFirst("Summary:", "")).toString());
        // Status
        manga.setFinished(!getFirstMatchDefault("<b>Status:</b>(.+?)</a>", source, "").contains("Ongoing"));
        // Author
        manga.setAuthor(getFirstMatchDefault("author.+?\">(.+?)<", source, ""));
        // Genre
        manga.setGenre((Html.fromHtml(getFirstMatchDefault("<li itemprop=\"genre\".+?</b>(.+?)</li>", source, "").replace("a><a", "a>, <a") + ".").toString().trim()));
        // Chapter
        Pattern p = Pattern.compile(
                "<a class=\"chapter_list_a\" href=\"(/chapter.+?)\" title=\"(.+?)\">(.+?)</a>");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(3), HOST + matcher.group(1)));
        }
        manga.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return chapter.getPath().replace(".html", "-" + page + ".html");
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        if (chapter.getExtra() == null)
            setExtra(chapter);
        String[] images = chapter.getExtra().split("\\|");
        return images[page];
    }

    private void setExtra(Chapter chapter) throws Exception {
        String source = getNavWithHeader().get(
                chapter.getPath().replace(".html", "-" + chapter.getPages() + "-1.html"));
        Pattern p = Pattern.compile("<img class=\"manga_pic.+?src=\"([^\"]+)");
        Matcher matcher = p.matcher(source);
        String images = "";
        while (matcher.find()) {
            images = images + "|" + matcher.group(1);
        }
        chapter.setExtra(images);
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String source = getNavWithHeader().get(chapter.getPath());
        String nop = getFirstMatch(
                "\\d+/(\\d+)</option>[\\s]*</select>", source,
                "failed to get the number of pages");
        chapter.setPages(Integer.parseInt(nop));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int category, int order, int pageNumber) throws Exception {
        String source = getNavWithHeader().get(
                HOST + NineManga.order[order] +
                        genreV[category].replace("_", "_" + pageNumber));
        return getMangasFromSource(source);
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile(
                "<a href=\"(/manga/[^\"]+)\"><img src=\"(.+?)\".+?alt=\"([^\"]+)\"");
        Matcher matcher = p.matcher(source);
        while (matcher.find()) {
            Manga manga = new Manga(getServerID(), matcher.group(3), HOST + matcher.group(1), false);
            manga.setImages(matcher.group(2));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public String[] getCategories() {
        return genre;
    }

    @Override
    public String[] getOrders() {
        // "/category/", "/list/New-Update/", "/list/Hot-Book", "/list/New-Book/"
        return new String[]{"Manga Directory", "Latest Releases", "Popular Manga", "New Manga"};
    }

    @Override
    public boolean hasList() {
        return false;
    }
}

