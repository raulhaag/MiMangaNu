package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import java.io.IOException;
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
import ar.rulosoft.navegadores.VolatileCookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jtx on 07.05.2016.
 */
class MangaEden extends ServerBase {

    protected static final String HOST = "https://www.mangaeden.com";

    protected int[] fltGenre = {
            R.string.flt_tag_action,
            R.string.flt_tag_adult,
            R.string.flt_tag_adventure,
            R.string.flt_tag_comedy,
            R.string.flt_tag_doujinshi,
            R.string.flt_tag_drama,
            R.string.flt_tag_ecchi,
            R.string.flt_tag_fantasy,
            R.string.flt_tag_gender_bender,
            R.string.flt_tag_harem,
            R.string.flt_tag_historical,
            R.string.flt_tag_horror,
            R.string.flt_tag_josei,
            R.string.flt_tag_martial_arts,
            R.string.flt_tag_mature,
            R.string.flt_tag_mecha,
            R.string.flt_tag_mystery,
            R.string.flt_tag_one_shot,
            R.string.flt_tag_psychological,
            R.string.flt_tag_romance,
            R.string.flt_tag_school_life,
            R.string.flt_tag_sci_fi,
            R.string.flt_tag_seinen,
            R.string.flt_tag_shoujo,
            R.string.flt_tag_shounen,
            R.string.flt_tag_slice_of_life,
            R.string.flt_tag_smut,
            R.string.flt_tag_sports,
            R.string.flt_tag_supernatural,
            R.string.flt_tag_tragedy,
            R.string.flt_tag_webtoon,
            R.string.flt_tag_yaoi,
            R.string.flt_tag_yuri
    };
    protected String[] valGenre = {
            "4e70e91bc092255ef70016f8",
            "4e70e92fc092255ef7001b94",
            "4e70e918c092255ef700168e",
            "4e70e918c092255ef7001675",
            "4e70e928c092255ef7001a0a",
            "4e70e918c092255ef7001693",
            "4e70e91ec092255ef700175e",
            "4e70e918c092255ef7001676",
            "4e70e921c092255ef700184b",
            "4e70e91fc092255ef7001783",
            "4e70e91ac092255ef70016d8",
            "4e70e919c092255ef70016a8",
            "4e70e920c092255ef70017de",
            "4e70e923c092255ef70018d0",
            "4e70e91bc092255ef7001705",
            "4e70e922c092255ef7001877",
            "4e70e918c092255ef7001681",
            "4e70e91dc092255ef7001747",
            "4e70e919c092255ef70016a9",
            "4e70e918c092255ef7001677",
            "4e70e918c092255ef7001688",
            "4e70e91bc092255ef7001706",
            "4e70e918c092255ef700168b",
            "4e70e918c092255ef7001667",
            "4e70e918c092255ef700166f",
            "4e70e918c092255ef700167e",
            "4e70e922c092255ef700185a",
            "4e70e91dc092255ef700172e",
            "4e70e918c092255ef700166a",
            "4e70e918c092255ef7001672",
            "4e70ea70c092255ef7006d9c",
            "4e70e91ac092255ef70016e5",
            "4e70e92ac092255ef7001a57"
    };

    private static final int[] fltType = {
            R.string.flt_tag_manga,
            R.string.flt_tag_manhwa,
            R.string.flt_tag_manhua,
            R.string.flt_tag_comic,
            R.string.flt_tag_doujinshi
    };
    private static final String[] valType = {
            "&type=0",
            "&type=1",
            "&type=2",
            "&type=3",
            "&type=4"
    };

    private static final int[] fltStatus = {
            R.string.flt_status_ongoing,
            R.string.flt_status_completed,
            R.string.flt_status_suspended
    };
    private static final String[] valStatus = {
            "&status=1",
            "&status=2",
            "&status=0"
    };

    private static final int[] fltOrder = {
            R.string.flt_order_views,
            R.string.flt_order_last_update,
            R.string.flt_order_title,
            R.string.flt_order_chapters
    };
    private static final String[] orderV = {
            "", // same as "&order=1",
            "&order=3",
            "&order=-0",
            "&order=2"
    };

    private String lang_2l;
    private String lang_3l;

    MangaEden(Context context) {
        super(context);
        setFlag(R.drawable.flag_en);
        setIcon(R.drawable.mangaeden);
        setServerName("MangaEden");
        setServerID(MANGAEDEN);
        setLanguage("en", "eng");
    }

    @SuppressWarnings("WeakerAccess")
    protected void setLanguage(String l2l, String l3l) {
        lang_2l = l2l;
        lang_3l = l3l;
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String source = getNavigatorAndFlushParameters().get(HOST + "/" + lang_2l + "/" + lang_2l + "-directory/?title=" + URLEncoder.encode(term, "UTF-8") + "&author=&artist=&releasedType=0&released=");
        ArrayList<Manga> mangas = new ArrayList<>(getMangasFromSource(source));
        Collections.sort(mangas, Manga.Comparators.TITLE_ASC);
        return mangas;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String source = getNavigatorAndFlushParameters().get(manga.getPath());
            // Front
            String image = getFirstMatchDefault("<div class=\"mangaImage2\"><img src=\"(.+?)\"", source, "");
            if (image.length() > 2) {
                image = "http:" + image;
            }
            manga.setImages(image);
            // Summary
            manga.setSynopsis(getFirstMatchDefault("mangaDescription\">(.+?)</h", source, context.getString(R.string.nodisponible)));
            // Status
            manga.setFinished(getFirstMatchDefault("Status</h(.+?)<h", source, "").contains("Completed"));
            // Author
            manga.setAuthor(getFirstMatchDefault("Author</h4>(.+?)<h4>", source, context.getString(R.string.nodisponible)));
            // Genres
            manga.setGenre(getFirstMatchDefault("Genres</h4>(.+?)<h4>", source, context.getString(R.string.nodisponible)));
            // Chapters
            Pattern pattern = Pattern.compile("<tr.+?href=\"(/en/en-manga/.+?)\".+?>(.+?)</a", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(source);
            while (matcher.find()) {
                manga.addChapterFirst(new Chapter(matcher.group(2).replaceAll("Chapter", " Ch "), HOST + matcher.group(1)));
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        assert chapter.getExtra() != null;
        return "http:" + chapter.getExtra().split("\\|")[page - 1];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getPages() == 0) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String user = prefs.getString("username_" + getServerName(), "");
            String password = Util.xorDecode(prefs.getString("dwp_" + getServerName(), ""), getServerName());
            String source = getNavigatorAndFlushParameters().get(chapter.getPath(), new MangaEdenLoginInterceptor(user, password, chapter.getPath()));
            ArrayList<String> images = getAllMatch("fs\":\\s*\"(.+?)\"", source);

            if (images.isEmpty()) {
                // if the Manga was licensed, no pages are returned as well
                getFirstMatch(
                        "We are sorry but this manga has been licensed in your country",
                        source,
                        context.getString(R.string.server_failed_loading_chapter));
                throw new Exception(context.getString(R.string.server_manga_is_licensed));
            }
            chapter.setExtra(TextUtils.join("|", images));
            chapter.setPages(images.size());
        }
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern pattern = Pattern.compile("<tr>[^<]*<td>[^<]*<a href=\"/" + lang_2l + "/" + lang_2l + "-manga/(.+?)\" class=\"(.+?)\">(.+?)</a>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            Manga manga = new Manga(getServerID(), matcher.group(3), HOST + "/" + lang_2l + "/" + lang_2l + "-manga/" + matcher.group(1), matcher.group(2).contains("close"));
            mangas.add(manga);
        }
        return mangas;
    }

    private ArrayList<Manga> getMangasFromFrontpage(String source) {
        String newSource = "";
        try {
            newSource = getFirstMatchDefault("<ul id=\"news\"(.+?)</ul>", source, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Pattern pattern = Pattern.compile("<img src=\"(//cdn\\.mangaeden\\.com/mangasimg/.+?)\".+?<div class=\"hottestInfo\">[\\s]*<a href=\"(/" + lang_2l + "/" + lang_2l + "-manga/[^\"<>]+?)\" class=.+?\">(.+?)</a>", Pattern.DOTALL);
        Matcher matcher;
        if (newSource.isEmpty()) {
            matcher = pattern.matcher(source);
        } else {
            matcher = pattern.matcher(newSource);
        }
        ArrayList<Manga> mangas = new ArrayList<>();
        int i = 0;
        while (matcher.find()) {
            i++;
            Manga manga = new Manga(getServerID(), matcher.group(3), HOST + matcher.group(2), false);
            manga.setImages("http:" + matcher.group(1));
            mangas.add(manga);
            if (newSource.isEmpty()) {
                if (i == 60) {
                    break;
                }
            }
        }
        return mangas;
    }

    //http://www.mangaeden.com/en/en-directory/?status=2&author=&title=&releasedType=0&released=&artist=&type=0&categoriesInc=4e70e91bc092255ef70016f8&categoriesInc=4e70e91ec092255ef700175e&page=2

    // when filtering is active, the result will have no images. this can't be helped.
    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = HOST + "/" + lang_2l + "/" + lang_2l + "-directory/?author=&title=&artist=";
        String noFilters = web;
        for (int i = 0; i < filters[2].length; i++) {
            web = web + valStatus[filters[2][i]];
        }
        for (int i = 0; i < filters[1].length; i++) {
            if (filters[1][i] == 1) {
                web = web + "&categoriesInc=" + valGenre[i];
            } else if (filters[1][i] == -1) {
                web = web + "&categoriesExcl=" + valGenre[i];
            }
        }

        for (int i = 0; i < filters[0].length; i++) {
            web = web + valType[filters[0][i]];
        }
        if (orderV[filters[3][0]].equals("") && web.equals(noFilters)) {
            // no filters are active - simply fetch the front page
            web = HOST + "/" + lang_3l + "/";
            String source = getNavigatorAndFlushParameters().get(web);
            return getMangasFromFrontpage(source);
        } else {
            // filtering is active, get results from the list
            web = web + orderV[filters[3][0]] + "&page=" + pageNumber;
            String source = getNavigatorAndFlushParameters().get(web);
            return getMangasFromSource(source);
        }
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(context.getString(R.string.flt_type), buildTranslatedStringArray(fltType), ServerFilter.FilterType.MULTI),
                new ServerFilter(context.getString(R.string.flt_genre), buildTranslatedStringArray(fltGenre), ServerFilter.FilterType.MULTI_STATES),
                //new ServerFilter(context.getString(R.string.flt_exclude_tags), buildTranslatedStringArray(fltGenre), ServerFilter.FilterType.MULTI),
                new ServerFilter(context.getString(R.string.flt_status), buildTranslatedStringArray(fltStatus), ServerFilter.FilterType.MULTI),
                new ServerFilter(context.getString(R.string.flt_order), buildTranslatedStringArray(fltOrder), ServerFilter.FilterType.SINGLE)
        };
    }


    @Override
    public boolean testLogin(String user, String password) throws Exception {
        VolatileCookieJar cj = new VolatileCookieJar();
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addPost("username", user);
        nav.addPost("password", password);
        nav.addPost("t", "" + (System.currentTimeMillis()));
        nav.post(HOST + "/ajax/login/", cj);
        HttpUrl url = HttpUrl.parse(HOST);
        return (cj.contain(url, "remember_token") && !cj.getValue(url, "remember_token").isEmpty());
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

    public class MangaEdenLoginInterceptor implements Interceptor {
        private final String page;
        String user;
        String password;

        public MangaEdenLoginInterceptor(String user, String password, String page) {
            this.user = user;
            this.password = password;
            this.page = page;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            if (!response.request().url().toString().contains("/login/")) {//when is not logged in it redirect to a page of description of chapter
                return response;
            } else {
                if (user.trim().isEmpty()) {
                    Util.getInstance().toast(context, "For read this chapter you need to login (add it in preferences)");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return response;
                }
                try {
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("username", user)
                            .addFormDataPart("password", password)
                            .addFormDataPart("t", "" + System.currentTimeMillis())
                            .build();

                    Request request0 = new Request.Builder().url(HOST + "/ajax/login/")
                            .method("POST", requestBody)
                            .header("User-Agent", Navigator.USER_AGENT)
                            .build();
                    response.body().close();
                    response = chain.proceed(request0);//generate the cookie

                    Request request1 = new Request.Builder()
                            .url(page)
                            .header("User-Agent", Navigator.USER_AGENT)
                            .build();
                    response.body().close();
                    return chain.proceed(request1);

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }
}

