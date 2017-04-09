package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;
import ar.rulosoft.navegadores.VolatileCookieJar;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Raul on 13/01/2017.
 */

class BatoTo extends ServerBase {

    private static String[] genre = new String[]{
            "4-Koma", "Action", "Adventure", "Award Winning", "Comedy", "Cooking", "Doujinshi", "Drama",
            "Ecchi", "Fantasy", "Gender Bender", "Harem", "Historical", "Horror", "Josei", "Martial Arts",
            "Mecha", "Medical", "Music", "Mystery", "Oneshot", "Psychological", "Romance", "School Life",
            "Sci-fi", "Seinen", "Shoujo", "Shoujo Ai", "Shounen", "Shounen Ai", "Slice of Life", "Smut",
            "Sports", "Supernatural", "Tragedy", "Webtoon", "Yaoi", "Yuri", "[no chapters]", "[chapters]"
    };
    private static String[] genreV = new String[]{
            "i40", "i1", "i2", "i39", "i3", "i41", "i9", "i10",
            "i12", "i13", "i15", "i17", "i20", "i22", "i34", "i27",
            "i30", "i42", "i37", "i4", "i38", "i5", "i6", "i7",
            "i8", "i32", "i35", "i16", "i33", "i19", "i21", "i23",
            "i25", "i26", "i28", "i36", "i29", "i31", "i44", "e44"
    };

    private static String[] completed = new String[]{"Any", "Completed", "Incomplete"};
    private static String[] completedV = new String[]{"", "&completed=c", "&completed=i"};


    private static String[] im = new String[]{"Yes", "No"};
    private static String[] imV = new String[]{"", "&mature=n"};


    private static String[] type = new String[]{"Any", "Manga(JP)", "Manhwa(Kr)", "Manhua(Cn)",
            "Artbook", "Other"};
    private static String[] typeV = new String[]{"", "&type=jp", "&type=kr", "&type=cn", "&type=ar",
            "&type=ot"};


    private static String[] orderBy = new String[]{"Title", "Author", "Artist", "Rating", "Views",
            "Last Update"};
    private static String[] orderByV = new String[]{"&order_cond=title", "&order_cond=author",
            "&order_cond=artist", "&order_cond=rating", "&order_cond=views", "&order_cond=update"};


    private static String[] orderDir = new String[]{"Ascending", "Descending"};
    private static String[] orderDirV = new String[]{"&order=asc", "&order=desc"};

    BatoTo(Context context) {
        super(context);
        this.setFlag(R.drawable.noimage);
        this.setIcon(R.drawable.batoto);
        this.setServerName("BatoTo");
        setServerID(ServerBase.BATOTO);
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = "http://bato.to/search_ajax?p=" + pageNumber;
        if (filters[0].length > 0) {
            String genres = "";
            for (int filter : filters[0]) {
                genres = genres + ";" + genreV[filter];
            }
            web = web + "&genres=" + genres + "&genre_cond=and";
        }
        web = web + completedV[filters[1][0]] + imV[filters[2][0]] + typeV[filters[3][0]]
                + orderByV[filters[4][0]] + orderDirV[filters[5][0]];
        String data = getNavigatorAndFlushParameters().get(web);
        return getMangasFromSource(data);
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("<a href=\"([^\"]+)\">[^>]+(book_open|book).+?>(.+?)<");
        Matcher m = p.matcher(source);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), Util.getInstance().fromHtml(m.group(3)).toString(), m.group(1), m.group(2).length() == 4));
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter("Genre", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Completed series", completed, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Include mature", im, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Type", type, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Oder by", orderBy, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Order direction", orderDir, ServerFilter.FilterType.SINGLE)
        };
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String web = "http://bato.to/search_ajax?name=" + URLEncoder.encode(term, "UTF-8") + "&name_cond=c&p=1";
        String data = getNavigatorAndFlushParameters().get(web);
        return getMangasFromSource(data);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        try {
            if (manga.getChapters().size() == 0 || forceReload) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                String user = prefs.getString("username_" + getServerName(), "");
                String password = prefs.getString("dwp_" + getServerName(), "");
                String data = getNavigatorAndFlushParameters().get(manga.getPath(), new BatotoLoginInterceptor(user, password));
                String synopsis = getFirstMatchDefault("Description:</td>\\s+<td>(.*?)</td>", data, defaultSynopsis);
                manga.setSynopsis(Util.getInstance().fromHtml(synopsis).toString());
                manga.setImages(getFirstMatchDefault("(http://img\\.bato\\.to/forums/uploads.+?)\"", data, ""));
                manga.setAuthor(getFirstMatchDefault("search\\?artist_name=.+?>([^<]+)", data, "n/a"));
                manga.setGenre(getFirstMatchDefault("Genres:</td>\\s+<td>([\\s\\S]+?)<img[^>]+?alt=.edit", data, "").replaceAll("<.*?>", "").replaceAll(",[\\s]*", ",").trim());
                manga.setFinished(!getFirstMatchDefault("Status:<\\/td>\\s+<td>([^<]+)", data, "").contains("Ongoing"));
                ArrayList<Chapter> chapters = new ArrayList<>();
                Pattern pattern = Pattern.compile("<a href=\"([^\"]+)\" title=\"[^\"]+\">.+?>([^<]+).+?title=\"(.+?)\".+?<a[^>]+>([^<]+)");
                data = getFirstMatchDefault("ipb_table chapters_list\"([\\s\\S]+?)</table", data, "");
                Matcher matcher = pattern.matcher(data);
                String lang_selection, lang = "";
                lang_selection = prefs.getString("batoto_lang_selection", "Automatic");
                if (lang_selection.equals("Automatic"))
                    lang = Locale.getDefault().getDisplayLanguage();
                else if (lang_selection.equals("Custom"))
                    lang = prefs.getString("batoto_custom_lang", "");
                else
                    lang = lang_selection;
                while (matcher.find()) {
                    if (!lang_selection.equals("All") && !lang.isEmpty()) {
                        if (matcher.group(3).contains(lang))
                            chapters.add(0, new Chapter("(" + matcher.group(3) + ") " + matcher.group(2) + " [" + matcher.group(4) + "]", matcher.group(1)));
                    } else if (lang_selection.equals("All"))
                        chapters.add(0, new Chapter("(" + matcher.group(3) + ") " + matcher.group(2) + " [" + matcher.group(4) + "]", matcher.group(1)));
                }
                manga.setChapters(chapters);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return null;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String data = getNavigatorAndFlushParameters().get(chapter.getExtra() + page, "http://bato.to/reader");
        return getFirstMatchDefault("img id=\"comic_page\"[^>]+src=\"([^\"]+)", data, "Error getting images");
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        chapter.setExtra("http://bato.to/areader?id=" + chapter.getPath().split("#")[1] + "&p=");
        String data = getNavigatorAndFlushParameters().get(chapter.getExtra() + "1", "http://bato.to/reader");
        String pages = getFirstMatchDefault("page\\s+(\\d+)</option>\\s+</select>", data, "Can't init the chapter");
        chapter.setPages(Integer.parseInt(pages));
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public FilteredType getFilteredType() {
        return FilteredType.TEXT;
    }

    //just log in batoto to store the cookie if all ok
    @Override
    public boolean testLogin(String user, String password) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        CookieJar ccj = nav.getHttpClient().cookieJar();
        VolatileCookieJar cj = new VolatileCookieJar();
        nav.setCookieJar(cj);
        String data = nav.get("https://bato.to/forums/index.php?app=core&amp;module=global&amp;section=login");
        HashMap<String, String> params = Navigator.getFormParamsFromSource(data);
        nav = getNavigatorAndFlushParameters();
        nav.addPost("auth_key", params.get("auth_key"));
        nav.addPost("ips_password", password);
        nav.addPost("ips_username", user);
        nav.addPost("referer", "https://bato.to/forums/");
        nav.addPost("rememberMe", "1");
        nav.post("https://bato.to/forums/index.php?app=core&module=global&section=login&do=process");
        List<Cookie> cookies = Navigator.getCookieJar().loadForRequest(HttpUrl.parse("https://bato.to"));
        nav.setCookieJar(ccj);
        return cj.contain("member_id");
    }

    @Override
    public boolean needLogin() {
        return true;
    }

    @Override
    public boolean hasCredentials() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String user = prefs.getString("username_" + getServerName(), "");
        String password = prefs.getString("dwp_" + getServerName(), "");
        return !(user.isEmpty() && password.isEmpty());
    }

    public class BatotoLoginInterceptor implements Interceptor {
        String user;
        String password;

        public BatotoLoginInterceptor(String user, String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            boolean needLogin = true;
            List<Cookie> cookies = Navigator.getCookieJar().loadForRequest(HttpUrl.parse("https://bato.to"));
            for (Cookie c : cookies) {
                if (c.name().contains("member_id")) {
                    needLogin = false;
                }
            }
            Response response = chain.proceed(chain.request());
            if (response.code() != 200)
                return response;
            if (!needLogin) {
                return response;
            } else {
                try {
                    Request request = response.request();
                    String content = response.body().string();
                    String domain = request.url().toString();
                    HashMap<String, String> params = Navigator.getFormParamsFromSource(content);
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("auth_key", params.get("auth_key"))
                            .addFormDataPart("ips_password", password)
                            .addFormDataPart("ips_username", user)
                            .addFormDataPart("referer", "https://bato.to/forums/")
                            .addFormDataPart("rememberMe", "1")
                            .build();
                    String loginWeb;
                    if (request.isHttps()) {
                        loginWeb = "https://bato.to/forums/index.php?app=core&module=global&section=login&do=process";
                    } else {
                        loginWeb = "http://bato.to/forums/index.php?app=core&module=global&section=login&do=process";
                    }
                    Request request0 = new Request.Builder().url(loginWeb)
                            .method("POST", requestBody)
                            .header("User-Agent", Navigator.USER_AGENT)
                            .build();
                    response.body().close();
                    response = chain.proceed(request0);//generate the cookie

                    Request request1 = new Request.Builder()
                            .url(domain)
                            .header("User-Agent", Navigator.USER_AGENT)
                            .build();
                    response.body().close();
                    response = chain.proceed(request1);
                    return response;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }
}
