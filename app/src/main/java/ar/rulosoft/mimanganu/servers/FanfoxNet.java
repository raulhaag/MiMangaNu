package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class FanfoxNet extends ServerBase {

    public static final String HOST = "https://fanfox.net";
    private static final int[] fltGenre = {
            R.string.flt_tag_action,
            R.string.flt_tag_adventure,
            R.string.flt_tag_comedy,
            R.string.flt_tag_drama,
            R.string.flt_tag_fantasy,
            R.string.flt_tag_martial_arts,
            R.string.flt_tag_shounen,
            R.string.flt_tag_horror,
            R.string.flt_tag_supernatural,
            R.string.flt_tag_harem,
            R.string.flt_tag_psychological,
            R.string.flt_tag_romance,
            R.string.flt_tag_school_life,
            R.string.flt_tag_shoujo,
            R.string.flt_tag_mystery,
            R.string.flt_tag_sci_fi,
            R.string.flt_tag_seinen,
            R.string.flt_tag_tragedy,
            R.string.flt_tag_ecchi,
            R.string.flt_tag_sports,
            R.string.flt_tag_slice_of_life,
            R.string.flt_tag_mature,
            R.string.flt_tag_shoujo_ai,
            R.string.flt_tag_webtoon,
            R.string.flt_tag_doujinshi,
            R.string.flt_tag_one_shot,
            R.string.flt_tag_smut,
            R.string.flt_tag_yaoi,
            R.string.flt_tag_josei,
            R.string.flt_tag_historical,
            R.string.flt_tag_shounen_ai,
            R.string.flt_tag_gender_bender,
            R.string.flt_tag_adult,
            R.string.flt_tag_yuri,
            R.string.flt_tag_mecha
    };
    private static final int[] fltOrder = {
            R.string.flt_order_alpha,
            R.string.flt_order_views,
            R.string.flt_order_rating,
            R.string.flt_order_last_update,
    };
    private static final int[] fltStatus = {
            R.string.flt_status_all,
            R.string.flt_status_ongoing,
            R.string.flt_status_completed
    };
    private static boolean cookieInit = false;

    /**
     * Construct a new ServerBase object.
     *
     * @param context the context for this object
     */
    FanfoxNet(Context context) {
        super(context);
        setFlag(R.drawable.flag_en);
        setIcon(R.drawable.mangafox_icon);
        setServerName("FanFox");
        setServerID(FANFOXNET);
    }

    private static void generateNeededCookie() {
        HttpUrl url = HttpUrl.parse(HOST);
        Navigator.getCookieJar().saveFromResponse(url, Arrays.asList(
                Cookie.parse(url, "isAdult=1; Domain=fanfox.net"))
        );
        cookieInit = true;
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        StringBuilder web = new StringBuilder(HOST);
        web.append("/search?title=&genres=");
        for (int i : filters[0]) {
            web.append((i + 1));
            web.append("%2C");
        }
        web.append("&st=");
        web.append(filters[1][0]);
        web.append("&sort=");
        web.append((filters[2][0] + 1));
        web.append("&stype=1&name_method=cw&name=&author_method=cw&author=&artist_method=cw&artist=&type=&rating_method=eq&rating=&released_method=eq&released=&page=");
        web.append(pageNumber);
        String data = getNavigatorAndFlushParameters().get(web.toString());
        return getMangasFromSource(data);
    }

    ArrayList<Manga> getMangasFromSource(String source) {
        Pattern pattern = Pattern.compile("<a href=\"(/manga/[^\"]+)[^<]+<img .+?src=\"([^\"]+)\".+?<a[^>]+>([^<]+)");
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            mangas.add(new Manga(getServerID(), matcher.group(3), matcher.group(1), matcher.group(2)));
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        return getMangasFromSource(getNavigatorWithNeededHeader()
                .get("https://fanfox.net/search?title=&genres=&st=0&sort=&stype=1&name_method=cw&name=" +
                        URLEncoder.encode(term, "UTF-8") +
                        "&author_method=cw&author=&artist_method=cw&artist=&type=&rating_method=eq&rating=&released_method=eq&released="));
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {

    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        String data = getNavigatorWithNeededHeader().get(HOST + manga.getPath());
        manga.setImages(getFirstMatchDefault("over-img\" src=\"([^\"]+)", data, ""));
        manga.setAuthor(getFirstMatchDefault("say\">Author:(.+)</p>", data, context.getString(R.string.nodisponible)));
        manga.setGenre(getFirstMatchDefault("tag-list\">(.+?)</p>", data, context.getString(R.string.nodisponible)));
        manga.setSynopsis(getFirstMatchDefault("right-content\">([^<]+?)<", data, context.getString(R.string.nodisponible)));
        manga.setFinished(getFirstMatchDefault("title-tip\">([^<]+)", data, "").contains("Complete"));
        // Chapter
        Pattern p = Pattern.compile("[li|none']{2,5}> <a href=\"(.+?)\".+?title=\"([^\"]+)", Pattern.DOTALL);
        Matcher m = p.matcher(data);
        while (m.find()) {
            manga.addChapterFirst(new Chapter(m.group(2), m.group(1)));
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String[] vars = chapter.getExtra().split("\\|");
        if (vars[0].equals("1")) {
            Navigator nav = getNavigatorWithNeededHeader();
            nav.addHeader("Referer", HOST + chapter.getPath());
            String data = nav.get(vars[2] + "chapterfun.ashx?cid=" + vars[1] + "&page=" + page + "&key=" + (page != 1 ? vars[3] : ""));
            data = Util.getInstance().unpack(data);
            String dir = getFirstMatch("\"([^\"]+)\";", data, "Error getting image(0)");
            String image = getFirstMatch("\\[\"([^\"]+)\"", data, "Error getting image(1)");
            return dir + image;
        } else {
            return "https:" + vars[page];
        }
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String web = HOST + chapter.getPath();
        String data = getNavigatorWithNeededHeader().get(web);
        int pages = Integer.parseInt(getLastMatchDefault("data-page=\"(\\d+)\">(\\d+)", data, "-1"));
        if (pages != -1) {
            String cid = getFirstMatch("chapterid\\s*=\\s*(\\d+)", data, "Error on chapter initialization (1)");
            data = getFirstMatch("javascript\">\\s*(eval\\(.+?)</script>", data, "Error on chapter initialization (3)");
            data = getFirstMatch("guidkey\\s*=(.+?);", Util.getInstance().unpack(data), "Error on chapter initialization (4)");
            ArrayList<String> keyp = getAllMatch("'([\\d|a|b|c|d|e|f])'", data);
            StringBuilder key = new StringBuilder();
            for (String string : keyp) {
                key.append(string);
            }
            web = web.substring(0, web.length() - 6);
            chapter.setPages(pages);
            chapter.setExtra("1|" + cid + "|" + web + "|" + key.toString());
        } else {
            data = getFirstMatch("\\['(.+?)'\\]", Util.getInstance().unpack(data), "Error on chapter initialization (0)");
            chapter.setExtra("0|" + data.replaceAll("','", "|"));
            chapter.setPages(data.split("','").length);
        }
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(
                        context.getString(R.string.flt_include_tags),
                        buildTranslatedStringArray(fltGenre), ServerFilter.FilterType.MULTI),
                new ServerFilter(
                        context.getString(R.string.flt_status),
                        buildTranslatedStringArray(fltStatus), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_order),
                        buildTranslatedStringArray(fltOrder), ServerFilter.FilterType.SINGLE)
        };
    }

    private Navigator getNavigatorWithNeededHeader() {
        if (!cookieInit) {
            generateNeededCookie();
        }
        Navigator nav = getNavigatorAndFlushParameters();
        return nav;
    }
}
