package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;

public class MangaShiroNet extends ServerBase {

    private static final String HOST = "https://mangashiro.net/";
    private static final int[] fltOrder = {
            R.string.flt_order_alpha,
            R.string.flt_order_last_update,
            R.string.flt_order_created,
            R.string.flt_order_views
    };
    private static final String[] valOrder = {
            "?order=title",
            "?order=update",
            "?order=create",
            "?order=popular",
    };
    private static String[] genres = new String[]{
            "All", "4-Koma", "Action", "Adult", "Adventure", "Comedy", "Cooking", "Demons", "Doujinshi",
            "Drama", "Ecchi", "Echi", "Fantasy", "Game", "Gender Bender", "Gore", "Harem",
            "Historical", "Horror", "Isekai", "Josei", "Magic", "Manga", "Manhua", "Manhwa",
            "Martial Arts", "Mature", "Mecha", "Military", "Music", "Mystery", "One Shot",
            "Oneshot", "Parody", "Police", "Psychological", "Romance", "Samurai", "School",
            "School Life", "Sci-fi", "Seinen", "Shoujo", "Shoujo Ai", "Shounen", "Shounen Ai",
            "Slice of Life", "Smut", "Sports", "Super Power", "Supernatural", "Thriller", "Tragedy",
            "Vampire", "Webtoon", "Webtoons", "Yuri",
    };
    private static String[] genresValues = new String[]{
            "daftar-manga/", "genres/4-koma/", "genres/action/", "genres/adult/", "genres/adventure/",
            "genres/comedy/", "genres/cooking/", "genres/demons/", "genres/doujinshi/",
            "genres/drama/", "genres/ecchi/", "genres/echi/", "genres/fantasy/", "genres/game/",
            "genres/gender-bender/", "genres/gore/", "genres/harem/", "genres/historical/",
            "genres/horror/", "genres/isekai/", "genres/josei/", "genres/magic/", "genres/manga/",
            "genres/manhua/", "genres/manhwa/", "genres/martial-arts/", "genres/mature/",
            "genres/mecha/", "genres/military/", "genres/music/", "genres/mystery/",
            "genres/one-shot/", "genres/oneshot/", "genres/parody/", "genres/police/",
            "genres/psychological/", "genres/romance/", "genres/samurai/", "genres/school/",
            "genres/school-life/", "genres/sci-fi/", "genres/seinen/", "genres/shoujo/",
            "genres/shoujo-ai/", "genres/shounen/", "genres/shounen-ai/", "genres/slice-of-life/",
            "genres/smut/", "genres/sports/", "genres/super-power/", "genres/supernatural/",
            "genres/thriller/", "genres/tragedy/", "genres/vampire/", "genres/webtoon/",
            "genres/webtoons/", "genres/yuri/",
    };

    /**
     * Construct a new ServerBase object.
     *
     * @param context the context for this object
     */
    public MangaShiroNet(Context context) {
        super(context);
        setFlag(R.drawable.flag_indo);
        setIcon(R.drawable.mangashironet);
        setServerName("MangaShiroNet");
        setServerID(MANGASHIRONET);
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String src = getNavigatorAndFlushParameters().get(HOST + "?s=" + URLEncoder
                .encode(term, "UTF-8"));
        return getMangasFromSource(src);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String data = getNavigatorAndFlushParameters().get(HOST + (manga.getPath()));

            // Cover
            manga.setImages(getFirstMatchDefault("itemprop=\"image\"[\\s\\S]+?src=\"*(.+?)\"* ", data, ""));

            // Summary
            manga.setSynopsis(Util.getInstance().fromHtml(
                    getFirstMatchDefault(
                            "class=\"desc\" itemprop=\"mainContentOfPage\">([\\s\\S]+?)</span>",
                            data, context.getString(R.string.nodisponible))).toString());

            // Status
            manga.setFinished(!data.contains("</b> Ongoing</li>"));

            // Author
            manga.setAuthor(getFirstMatchDefault("<li><b>Author:</b>\\s*(.+?)</li>",
                    data, context.getString(R.string.nodisponible)));

            // Genre <div class="gnr">(.+?)</div>
            manga.setGenre(getFirstMatchDefault("<div class=\"gnr\">(.+?)</div>", data,
                    context.getString(R.string.nodisponible)).replaceAll("</a>\\s*<a",
                    "</a>, <a"));

            // Chapter
            Pattern p = Pattern.compile("<div class=\"lch\"><a href=\"https://mangashiro.net/(.+?)\">(.+?)<", Pattern.DOTALL);
            Matcher m = p.matcher(data);
            while (m.find()) {
                manga.addChapterFirst(new Chapter(m.group(2).trim(), m.group(1).trim()));
            }
        }
        manga.getPath();
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        return chapter.getExtra().split("\\|")[page];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String src = getNavigatorAndFlushParameters().get(HOST + chapter.getPath());
        src = getFirstMatch("<div id=\"readerarea\">([\\s\\S]+?</a>)</p>", src,
                context.getString(R.string.error));
        ArrayList<String> images = getAllMatch("href=\"(.+?)\"", src);
        chapter.setPages(images.size());
        chapter.setExtra("|" + TextUtils.join("|", images));
    }


    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        StringBuilder url = new StringBuilder(HOST);
        url.append(genresValues[filters[0][0]]);
        if (pageNumber > 1)
            url.append("page/").append(pageNumber).append("/");
        url.append(valOrder[filters[1][0]]);
        String src = getNavigatorAndFlushParameters().get(url.toString());
        return getMangasFromSource(src);
    }

    @NonNull
    private ArrayList<Manga> getMangasFromSource(String src) {
        Pattern pattern = Pattern.compile("series\" href=\"https://mangashiro.net/(manga/.+?/)\" title=\"(.+?)\".+?src=\"*(.+?)\"* ");
        Matcher matcher = pattern.matcher(src);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            mangas.add(new Manga(getServerID(), matcher.group(2), matcher.group(1), matcher.group(3)));
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(
                        context.getString(R.string.flt_genre),
                        genres, ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_order),
                        buildTranslatedStringArray(fltOrder), ServerFilter.FilterType.SINGLE)
        };
    }

    @Override
    public boolean hasList() {
        return false;
    }
}
