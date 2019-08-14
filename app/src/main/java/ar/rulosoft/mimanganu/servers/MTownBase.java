package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.text.TextUtils;

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
import ar.rulosoft.navegadores.Navigator;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

public abstract class MTownBase extends ServerBase {

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

    /**
     * Construct a new ServerBase object.
     *
     * @param context the context for this object
     */
    MTownBase(Context context) {
        super(context);
    }

    public abstract String getHost();

    public abstract String getDomain();

    public abstract int[] getFilter();

    public abstract boolean getCookieInit();

    public abstract void setCookieInit(boolean state);

    private void generateNeededCookie() {
        HttpUrl url = HttpUrl.parse(getHost());
        assert (url != null);
        Navigator.getCookieJar().saveFromResponse(url, Collections.singletonList(
                Cookie.parse(url, "isAdult=1; Domain=" + getDomain()))
        );
        setCookieInit(true);
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        StringBuilder web = new StringBuilder(getHost());
        web.append("/search?title=&genres=");
        StringBuilder nogen = new StringBuilder("&nogenres=");
        for (int i = 0; i < filters[0].length; i++) {
            if (filters[0][i] == -1) {
                nogen.append(i + 1);
                nogen.append("%2C");
            } else if (filters[0][i] == 1) {
                web.append(i + 1);
                web.append("%2C");
            }
        }
        web.append(nogen);
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
                .get(getHost() + "/search?title=&genres=&st=0&sort=&stype=1&name_method=cw&name=" +
                        URLEncoder.encode(term, "UTF-8") +
                        "&author_method=cw&author=&artist_method=cw&artist=&type=&rating_method=eq&rating=&released_method=eq&released="));
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {

            String data = getNavigatorWithNeededHeader().get(getHost() + manga.getPath());
            manga.setImages(getFirstMatchDefault("over-img\" src=\"([^\"]+)", data, ""));

            String author = getFirstMatchDefault("say\">Author:(.+?)</p>", data, "");
            if (author.trim().isEmpty()) {
                author = context.getString(R.string.nodisponible);
            }
            manga.setAuthor(author);

            String genre = getFirstMatchDefault("tag-list\">(.+?)</p>", data, "");
            genre = TextUtils.join(", ", getAllMatch("\">([^<]+)</a>", genre));
            if (genre.trim().isEmpty()) {
                genre = context.getString(R.string.nodisponible);
            }
            manga.setGenre(genre);

            String synopsis = getFirstMatchDefault("right-content\">([^<]+)<", data, "");
            if (synopsis.trim().isEmpty()) {
                synopsis = context.getString(R.string.nodisponible);
            }
            manga.setSynopsis(synopsis);

            manga.setFinished(getFirstMatchDefault("title-tip\">([^<]+)", data, "").contains("Complete"));
            // Chapter
            Pattern p = Pattern.compile("(li|none')> <a href=\"([^\"]+)\".+?title3\">(.+?)</p", Pattern.DOTALL);
            Matcher m = p.matcher(data);
            while (m.find()) {
                manga.addChapterFirst(new Chapter(m.group(3), m.group(2)));
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        assert (chapter.getExtra() != null);
        String[] vars = chapter.getExtra().split("\\|");
        if (vars[0].equals("1")) {
            Navigator nav = getNavigatorWithNeededHeader();
            nav.addHeader("Referer", getHost() + chapter.getPath());
            String data = nav.get(vars[2] + "/chapterfun.ashx?cid=" + vars[1] + "&page=" + page + "&key=" + (page != 1 ? vars[3] : ""));
            data = Util.getInstance().unpack(data);
            String dir = getFirstMatch("\"([^\"]+)\";", data, "Error getting image(0)");
            String image = getFirstMatch("\\[\"([^\"]+)\"", data, "Error getting image(1)");
            if (!dir.startsWith("http"))
                dir = "https:" + dir;
            return dir + image;
        } else {
            return "https:" + vars[page];
        }
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String web = getHost() + chapter.getPath();
        String data = getNavigatorWithNeededHeader().get(web);
        int pages = Integer.parseInt(getLastMatchDefault("data-page=\"(\\d+)\">(\\d+)", data, "-1"));
        if (pages != -1) {
            cim1(data, web, chapter, pages);
        } else {
            String data2 = getFirstMatchDefault("\\['(.+?)'\\]", Util.getInstance().unpack(data), "");
            if (data2.equals("")) {
                cim1(data, web, chapter, 1);
                return;
            }
            chapter.setExtra("0|" + data2.replaceAll("','", "|"));
            chapter.setPages(data2.split("','").length);
        }
    }

    private void cim1(String data, String web, Chapter chapter, int pages) throws Exception {
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
                        buildTranslatedStringArray(getFilter()), ServerFilter.FilterType.MULTI_STATES),
                new ServerFilter(
                        context.getString(R.string.flt_status),
                        buildTranslatedStringArray(fltStatus), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_order),
                        buildTranslatedStringArray(fltOrder), ServerFilter.FilterType.SINGLE)
        };
    }

    private Navigator getNavigatorWithNeededHeader() {
        if (!getCookieInit()) {
            generateNeededCookie();
        }
        return getNavigatorAndFlushParameters();
    }

}
