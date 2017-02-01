package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.preference.PreferenceManager;
import android.text.Html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.navegadores.Navigator;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Raul on 13/01/2017.
 */

public class BatoTo extends ServerBase {

    private static String[] genre = new String[]{
            "4-Koma", "Action", "Adventure", "Award Winning", "Comedy", "Cooking", "Doujinshi", "Drama",
            "Ecchi", "Fantasy", "Gender Bender", "Harem", "Historical", "Horror", "Josei", "Martial Arts",
            "Mecha", "Medical", "Music", "Mystery", "Oneshot", "Psychological", "Romance", "School Life",
            "Sci-fi", "Seinen", "Shoujo", "Shoujo Ai", "Shounen", "Shounen Ai", "Slice of Life", "Smut",
            "Sports", "Supernatural", "Tragedy", "Webtoon", "Yaoi", "Yuri", "[no chapters]"
    };
    private static String[] genreV = new String[]{
            "40", "1", "2", "39", "3", "41", "9", "10",
            "12", "13", "15", "17", "20", "22", "34", "27",
            "30", "42", "37", "4", "38", "5", "6", "7",
            "8", "32", "35", "16", "33", "19", "21", "23",
            "25", "26", "28", "36", "29", "31", "44"
    };

    public BatoTo(Context context) {
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
            web = web + "&" + genres;
        }

        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigatorAndFlushParameters().get(web);
        Pattern p = Pattern.compile("<a href=\"([^\"]+)\">[^>]+(book_open|book).+?>(.+?)<");
        Matcher m = p.matcher(data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            while (m.find()) {
                mangas.add(new Manga(getServerID(), Html.fromHtml(m.group(3), Html.FROM_HTML_MODE_LEGACY).toString(), m.group(1), m.group(2).length() == 4));
            }
        } else {
            while (m.find()) {
                mangas.add(new Manga(getServerID(), Html.fromHtml(m.group(3)).toString(), m.group(1), m.group(2).length() == 4));
            }
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{new ServerFilter("Genre", genre, ServerFilter.FilterType.MULTI)};
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        return null;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().size() == 0 || forceReload) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String user = prefs.getString("username_" + getServerName(), "");
            String password = prefs.getString("dwp_" + getServerName(), "");
            String data = getNavigatorAndFlushParameters().get(manga.getPath(), new BatotoLoginIntercetor(user, password));
            manga.setSynopsis(getFirstMatchDefault("Description:</td>\\s+<td>(.*?)</td>", data, defaultSynopsis));
            manga.setImages(getFirstMatchDefault("(http://img\\.bato\\.to/forums/uploads.+?)\"", data, ""));
            manga.setAuthor(getFirstMatch("search\\?artist_name=.+?>([^<]+)", data, "n/a"));
            manga.setGenre(getFirstMatch("Genres:</td>\\s+<td>([\\s\\S]+?)<img[^>]+?alt=.edit", data, "").replaceAll("<.*?>", "").replaceAll(",[\\s]*", ",").trim());
            manga.setFinished(!getFirstMatchDefault("Status:<\\/td>\\s+<td>([^<]+)", data, "").contains("Ongoing"));
            ArrayList<Chapter> chapters = new ArrayList<>();
            Pattern pattern = Pattern.compile("<a href=\"([^\"]+)\" title=\"[^\"]+\">.+?>([^<]+).+?title=\"(.+?)\".+?<a[^>]+>([^<]+)");
            data = getFirstMatchDefault("ipb_table chapters_list\"([\\s\\S]+?)</table", data, "");
            Matcher matcher = pattern.matcher(data);
            while (matcher.find()) {
                chapters.add(new Chapter("(" + matcher.group(3) + ") " + matcher.group(2) + " [" + matcher.group(4) + "]", matcher.group(1)));
            }
            manga.setChapters(chapters);
        }
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return null;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String data = getNavigatorAndFlushParameters().get(chapter.getExtra() + page, "http://bato.to/reader");
        return getFirstMatch("img id=\"comic_page\"[^>]+src=\"([^\"]+)", data, "Error getting images");
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        chapter.setExtra("http://bato.to/areader?id=" + chapter.getPath().split("#")[1] + "&p=");
        String data = getNavigatorAndFlushParameters().get(chapter.getExtra() + "1", "http://bato.to/reader");
        String pages = getFirstMatch("page\\s+(\\d+)</option>\\s+</select>", data, "Can't init the chapter");
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
        String data = nav.get("https://bato.to/forums/index.php?app=core&amp;module=global&amp;section=login");
        HashMap<String, String> params = Navigator.getFormParamsFromSource(data);
        nav = getNavigatorAndFlushParameters();
        nav.addPost("auth_key", params.get("auth_key"));
        nav.addPost("ips_password", password);
        nav.addPost("ips_username", user);
        nav.addPost("referer", "https://bato.to/forums/");
        nav.addPost("rememberMe", "1");
        nav.post("https://bato.to/forums/index.php?app=core&module=global&section=login&do=process");
        data = getNavigatorAndFlushParameters().get("https://bato.to/myfollows/");
        return data.contains("-" + user);
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

    public class BatotoLoginIntercetor implements Interceptor {
        String user;
        String passWord;

        public BatotoLoginIntercetor(String user, String passWord) {
            this.user = user;
            this.passWord = passWord;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            if (response.code() != 200)
                return response;
            Request request = response.request();
            String content = response.body().string();
            String domain = request.url().toString();
            MediaType contentType = response.body().contentType();
            if (content.contains("-" + user)) {//check if user is log in (need to find a better way to do it)
                ResponseBody body = ResponseBody.create(contentType, content);//needed just because body only can be consumed once
                return response.newBuilder().body(body).build();
            } else {
                try {
                    HashMap<String, String> params = Navigator.getFormParamsFromSource(content);
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("auth_key", params.get("auth_key"))
                            .addFormDataPart("ips_password", passWord)
                            .addFormDataPart("ips_username", user)
                            .addFormDataPart("referer", "https://bato.to/forums/")
                            .addFormDataPart("rememberMe", "1")
                            .build();
                    Request request0 = new Request.Builder().url("https://bato.to/forums/index.php?app=core&module=global&section=login&do=process")
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
