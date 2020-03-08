package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.util.Log;

import com.squareup.duktape.Duktape;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navigator;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class JSServerHelper {

    public static IterationInterface ii = initII();
    public PluginInterface pi;
    private Context ctx;


    public JSServerHelper(Context context, String script) {
        Duktape duktape = Duktape.create();
        duktape.set("nav", JSServerHelper.IterationInterface.class, JSServerHelper.ii);//TODO nav to ii
        duktape.evaluate(script);
        pi = duktape.get("Server", PluginInterface.class);
        ctx = context;
    }

    private static IterationInterface initII() {
        return new IterationInterface() {
            @Override
            public String getRedirectWeb(String web, String headers) {
                try {
                    Navigator nav = Navigator.getInstance();
                    nav.flushParameter();
                    if (!headers.trim().isEmpty()) {
                        String[] headers_ = headers.trim().split("\\|");
                        for (int i = 0; i < headers_.length; i = i + 2) {
                            nav.addHeader(headers_[i], headers_[i + 1]);
                        }
                    }
                    return nav.getRedirectWeb(web);
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            public String get(String web, String headers) {
                try {
                    Navigator nav = Navigator.getInstance();
                    nav.flushParameter();
                    if (!headers.trim().isEmpty()) {
                        String[] headers_ = headers.trim().split("\\|");
                        for (int i = 0; i < headers_.length; i = i + 2) {
                            nav.addHeader(headers_[i], headers_[i + 1]);
                        }
                    }
                    return nav.get(web);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "error" + e.getMessage();
                }
            }

            @Override
            public String post(String web, String headers, String params) {
                try {
                    Navigator nav = Navigator.getInstance();
                    nav.flushParameter();
                    if (!headers.trim().isEmpty()) {
                        String[] headers_ = headers.trim().split("\\|");
                        for (int i = 0; i < headers_.length; i = i + 2) {
                            nav.addHeader(headers_[i], headers_[i + 1]);
                        }
                    }
                    if (!params.trim().isEmpty()) {
                        String[] post_ = params.trim().split("\\|");
                        for (int i = 0; i < post_.length; i = i + 2) {
                            nav.addPost(post_[i], post_[i + 1]);
                        }
                    }
                    return nav.post(web);
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            public String postM(String web, String headers, String params) {
                Navigator nav = Navigator.getInstance();
                nav.flushParameter();
                if (!headers.trim().isEmpty()) {
                    String[] headers_ = headers.trim().split("\\|");
                    for (int i = 0; i < headers_.length; i = i + 2) {
                        nav.addHeader(headers_[i], headers_[i + 1]);
                    }
                }
                RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"), params);
                try {
                    return nav.post(web, body);
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            public void srCookie(String url, String val) {
                HttpUrl url1 = HttpUrl.parse(url);
                Cookie cookie = Cookie.parse(url1, val);
                Navigator.getInstance().getHttpClient().cookieJar().saveFromResponse(url1, Arrays.asList(cookie));
            }

            @Override
            public void log(String id, String val) {
                Log.d("MMN_" + id, val);
            }
        };
    }

    public ArrayList<Manga> getMangasFiltered(int[][] filters, int page) {
        try {
            return jsonArrayToMangasArrayList(new JSONArray(pi.getMangasFiltered(filters, page)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public ArrayList<Manga> search(String term) {
        try {
            return jsonArrayToMangasArrayList(new JSONArray(pi.search(URLEncoder.encode(term, "UTF-8"))));
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void loadMangaInformation(Manga manga) {
        try {
            String in = pi.loadMangaInformation(manga.getTitle(), manga.getPath());
            Manga mn = jsonToManga(new JSONObject(in));
            manga.setSynopsis(mn.getSynopsis());
            manga.setFinished(mn.isFinished());
            manga.setImages(mn.getImages());
            manga.setAuthor(mn.getAuthor());
            manga.setGenre(mn.getGenre());
            manga.setChapters(mn.getChapters());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private ArrayList<Manga> jsonArrayToMangasArrayList(JSONArray array) {
        ArrayList<Manga> mangas = new ArrayList<>();
        try {
            for (int i = 0; i < array.length(); i++) {
                mangas.add(jsonToManga(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mangas;
    }

    private Manga jsonToManga(JSONObject jsm) throws JSONException {
        String na = ctx.getString(R.string.nodisponible);
        Manga manga = new Manga(Integer.parseInt(pi.getServerId()), jsm.getString("title"), jsm.getString("path"), jsm.getString("image"));
        if (jsm.has("genres")) manga.setGenre(jsm.getString("genres"));
        else manga.setGenre(na);
        if (jsm.has("author")) manga.setAuthor(jsm.getString("author"));
        else manga.setAuthor(na);
        if (jsm.has("synopsis")) manga.setSynopsis(jsm.getString("synopsis"));
        else manga.setSynopsis(na);
        if (jsm.has("finished")) manga.setFinished(jsm.getBoolean("finished"));
        else manga.setFinished(false);
        if (jsm.has("chapters")) {
            JSONArray chapters = jsm.getJSONArray("chapters");
            for (int i = 0; i < chapters.length(); i++) {
                JSONObject jsc = chapters.getJSONObject(i);
                manga.addChapterLast(new Chapter(jsc.getString("title"), jsc.getString("path")));
            }
        }
        return manga;
    }

    interface IterationInterface {
        String getRedirectWeb(String web, String headers);

        String get(String web, String headers);

        String post(String web, String headers, String params);

        String postM(String web, String headers, String params);

        void srCookie(String url, String val);

        void log(String id, String val);
    }

    interface PluginInterface {
        String getServerId();

        String search(String term);

        String getMangasFiltered(int[][] filters, int pageNumber);

        String loadMangaInformation(String title, String path);

        String chapterInit(String data, String mw);

    }

}
