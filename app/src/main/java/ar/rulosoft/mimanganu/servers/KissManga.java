package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import com.squareup.duktape.Duktape;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.RequestWebViewUserAction;
import ar.rulosoft.navegadores.Navigator;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.Response;

class KissManga extends ServerBase {
    private static final String HOST = "https://kissmanga.com";

    private static final String PATTERN_CHAPTER =
            "<td>\\s*<a\\s*href=\"(/Manga/[^\"]+)\"\\s*title=\"[^\"]+\">([^<]+)</a>\\s*</td>";
    private static final String PATTERN_MANGA =
            "<td title='[\\s]*<img.+?src=\"(https?://.+?.(?:jpe?g|png)).+?href=\"(/.anga/[^\"]+).+?>([^<]+)";

    private static final int[] fltGenre = {
            R.string.flt_tag_4_koma,
            R.string.flt_tag_action,
            R.string.flt_tag_adult,
            R.string.flt_tag_adventure,
            R.string.flt_tag_comedy,
            R.string.flt_tag_comic,
            R.string.flt_tag_cooking,
            R.string.flt_tag_doujinshi,
            R.string.flt_tag_drama,
            R.string.flt_tag_ecchi,
            R.string.flt_tag_fantasy,
            R.string.flt_tag_gender_bender,
            R.string.flt_tag_harem,
            R.string.flt_tag_historical,
            R.string.flt_tag_horror,
            R.string.flt_tag_josei,
            R.string.flt_tag_lolicon,
            R.string.flt_tag_manga,
            R.string.flt_tag_manhua,
            R.string.flt_tag_manhwa,
            R.string.flt_tag_martial_arts,
            R.string.flt_tag_mature,
            R.string.flt_tag_mecha,
            R.string.flt_tag_medical,
            R.string.flt_tag_music,
            R.string.flt_tag_mystery,
            R.string.flt_tag_one_shot,
            R.string.flt_tag_psychological,
            R.string.flt_tag_romance,
            R.string.flt_tag_school_life,
            R.string.flt_tag_sci_fi,
            R.string.flt_tag_seinen,
            R.string.flt_tag_shotacon,
            R.string.flt_tag_shoujo,
            R.string.flt_tag_shoujo_ai,
            R.string.flt_tag_shounen,
            R.string.flt_tag_shounen_ai,
            R.string.flt_tag_smut,
            R.string.flt_tag_sports,
            R.string.flt_tag_supernatural,
            R.string.flt_tag_webtoon,
            R.string.flt_tag_yaoi,
            R.string.flt_tag_yuri
    };

    private static final int[] fltStatus = {
            R.string.flt_status_all,
            R.string.flt_status_ongoing,
            R.string.flt_status_completed,
    };
    private static final String[] valStatus = {
            "",
            "Ongoing",
            "Completed",
    };

    private static final int[] fltOrder = {
            R.string.flt_order_rating,
            R.string.flt_order_last_update,
            R.string.flt_order_newest,
            R.string.flt_order_alpha
    };
    private static final String[] valOrder = {
            "/MostPopular",
            "/LatestUpdate",
            "/Newest",
            ""
    };

    KissManga(Context context) {
        super(context);
        setFlag(R.drawable.flag_en);
        setIcon(R.drawable.kissmanga_icon);
        setServerName("KissManga");
        setServerID(KISSMANGA);
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        // make use of AdvanceSearch, more data is then needed
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addHeader("Cookie", "vns_doujinshi=1; ");
        nav.addPost("keyword", term);

        String source = nav.post(HOST + "/Search/Manga");
        return getMangasSource(source);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public synchronized void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String source = getNavigatorAndFlushParameters().get(HOST + manga.getPath());

            // Summary
            manga.setSynopsis(
                    getFirstMatchDefault("<span " + "class=\"info\">Summary:</span>(.+?)</div>", source,
                            context.getString(R.string.nodisponible)));

            // Cover Image
            manga.setImages(
                    getFirstMatchDefault("rel=\"image_src\" href=\"(.+?)" + "\"", source, ""));

            // Author
            manga.setAuthor(getFirstMatchDefault("Author:</span>&nbsp;(.+?)</p>", source, context.getString(R.string.nodisponible)));

            // Genre
            manga.setGenre(getFirstMatchDefault("Genres:</span>&nbsp;(.+?)</p>", source, context.getString(R.string.nodisponible)));

            // Status
            manga.setFinished(source.contains("Status:</span>&nbsp;Completed"));

            // Chapter
            Pattern p = Pattern.compile(PATTERN_CHAPTER, Pattern.DOTALL);
            Matcher matcher = p.matcher(source);
            while (matcher.find()) {
                manga.addChapterFirst(new Chapter(matcher.group(2).replace(" Read Online", ""), matcher.group(1)));
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        assert chapter.getExtra() != null;
        return chapter.getExtra().split("\\|")[page];
    }

    @Override
    public synchronized void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getPages() == 0) {
            int pages = 0;
            Response response = Navigator.getInstance().getResponse(HOST + chapter.getPath());
            String source = response.body().string();

            if (source.contains("class=\"g-recaptcha\"")) {
                throw new Exception(context.getString(R.string.server_uses_captcha));
                //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(HOST + chapter.getPath()));
                //context.startActivity(browserIntent);
            }
            if (source.contains("this captcha")) {
                if (RequestWebViewUserAction.isRequestAvailable()) {
                    List<Cookie> cookies = Navigator.getCookieJar().loadForRequest(HttpUrl.parse(HOST + chapter.getPath()));
                    StringBuilder sb = new StringBuilder();
                    for (Cookie cookie : cookies) {
                        sb.append(cookie.name()).append("=").append(cookie.value()).append(";");
                    }
                    RequestWebViewUserAction.makeRequest(response.request().url().toString(), null,
                            source.replaceAll("\"\\/\\/ads.+?\"", "")
                                    .replaceAll("<.script>\\s<.div>\\s(<[\\s\\S]+?)<div id=\"footer\">", "")
                    );
                    chapterInit(chapter);
                    return;
                } else {
                    throw new Exception("User action required but unavailable");
                }
            }

            String ca = getNavigatorAndFlushParameters().get(HOST + "/Scripts/ca.js");
            String lo = getNavigatorAndFlushParameters().get(HOST + "/Scripts/lo.js");
            try {
                Duktape duktape = Duktape.create();
                duktape.evaluate(ca);
                duktape.evaluate(lo);
                Pattern p = Pattern.compile("javascript\">(.+?)<", Pattern.DOTALL);
                Matcher m = p.matcher(source);
                while (m.find()) {
                    if (m.group(1).contains("CryptoJS")) {
                        duktape.evaluate(m.group(1));
                    }
                }

                p = Pattern.compile("lstOLA.push\\((wrap.+?\\))\\)", Pattern.DOTALL);
                m = p.matcher(source);
                StringBuilder sb = new StringBuilder();
                String image;
                while (m.find()) {
                    pages++;
                    image = (String) duktape.evaluate(m.group(1) + ".toString()");
                    sb.append("|").append(image);
                }
                chapter.setExtra(sb.toString());
            } catch (Exception ignored) {
            }
            chapter.setPages(pages);
        }
    }

    private ArrayList<Manga> getMangasSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile(PATTERN_MANGA, Pattern.DOTALL);
        Matcher m = p.matcher(source);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(3), m.group(2), m.group(1)));
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(
                        context.getString(R.string.flt_genre),
                        buildTranslatedStringArray(fltGenre), ServerFilter.FilterType.MULTI_STATES),
                new ServerFilter(
                        context.getString(R.string.flt_status),
                        buildTranslatedStringArray(fltStatus), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_order) + " (" + context.getString(R.string.flt_hint_order_unfiltered_only) + ")",
                        buildTranslatedStringArray(fltOrder), ServerFilter.FilterType.SINGLE),
        };
    }

    @Override
    public synchronized ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        String source;

        // do not hide Doujinshi in result
        nav.addHeader("Cookie", "vns_doujinshi=1; ");

        // no filtering is active - use MangaList (much faster as it is smaller)
        boolean hasGenreFilter = false;
        for (int i = 0; i < filters[0].length; i++) {
            if (filters[0][i] != 0) {
                hasGenreFilter = true;
                break;
            }
        }
        if (filters[1][0] == 0 && !hasGenreFilter) {
            String web = HOST + "/MangaList" + valOrder[filters[2][0]] + "?page=" + pageNumber;
            source = nav.get(web);
        }
        // filtering is active, use advanced search (slow, as the whole result set is returned)
        else {
            if (pageNumber > 1) {
                // there is only one result page for the advanced search
                return new ArrayList<>();
            } else {

                nav.addPost("mangaName", "");
                nav.addPost("authorArtist", "");
                for (int i = 0; i < fltGenre.length; i++) {
                    int result = 0;
                    if (filters[0][i] == 1) {
                        result = 1;
                    } else if (filters[0][i] == -1) {
                        result = 2;
                    }
                    nav.addPost("genres", Integer.toString(result));
                }
                nav.addPost("status", valStatus[filters[1][0]]);

                source = nav.post(HOST + "/AdvanceSearch");
            }
        }

        return getMangasSource(source);
    }

    @Override
    public boolean needRefererForImages() {
        return false;
    }
}
