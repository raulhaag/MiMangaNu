package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class MangaDex extends ServerBase {

    private final String HOST = "https://mangadex.org";

    private final String[] content = new String[]{
            "4-Koma", "Action", "Adventure", "Award Winning", "Comedy", "Cooking", "Doujinshi", "Drama",
            "Ecchi", "Fantasy", "Gyaru", "Harem", "Historical", "Horror", "Martial Arts", "Mecha",
            "Medical", "Music", "Mystery", "Oneshot", "Psychological", "Romance", "School Life", "Sci-Fi",
            "Shoujo Ai", "Shounen Ai", "Slice of Life", "Smut", "Sports", "Supernatural", "Tragedy",
            "Long Strip", "Yaoi", "Yuri", "Video Games", "Isekai", "Adaptation", "Anthology", "Web Comic",
            "Full Color", "User Created", "Official Colored", "Fan Colored", "Gore", "Sexual Violence",
            "Crime", "Magical Girls", "Philosophical", "Superhero", "Thriller", "Wuxia", "Aliens",
            "Animals", "Crossdressing", "Demons", "Delinquents", "Genderswap", "Ghosts", "Monster Girls",
            "Loli", "Magic", "Military", "Monsters", "Ninja", "Office Workers", "Police",
            "Post-Apocalyptic", "Reincarnation", "Reverse Harem", "Samurai", "Shota", "Survival",
            "Time Travel", "Vampires", "Traditional Games", "Virtual Reality", "Zombies", "Incest"
    };


    /**
     * Construct a new ServerBase object.
     *
     * @param context the context for this object
     */
    public MangaDex(Context context) {
        super(context);
        setFlag(R.drawable.noimage);
        setIcon(R.drawable.noimage);
        setServerName("MangaDex");
        setServerID(MANGADEX);
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String tags = "";
        for (int i = 0; i < filters[0].length; i++) {
            if (filters[0][i] == -1) {
                tags = tags + "%2C" + "-" + (i + 1);
            }
        }
        for (int i = 0; i < filters[0].length; i++) {
            if (filters[0][i] == 1) {
                tags = tags + "%2C" + (i + 1);
            }
        }
        if (!tags.isEmpty()) {
            tags = tags.substring(3);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String user = prefs.getString("username_" + getServerName(), "");
        String password = prefs.getString("dwp_" + getServerName(), "");
        String data = getNavigatorAndFlushParameters()
                .get(HOST + "/search?tag_mode_exc=any&p=" + pageNumber + "&tag_mode_inc=all&tags=" +
                        tags, new MangaDexLoginInterceptor(user, password));
        return getMangasFromSource(data);
    }

    private ArrayList<Manga> getMangasFromSource(String src) {
        ArrayList<Manga> result = new ArrayList<>();
        Pattern p = Pattern.compile("\"(\\/title[^\"]+)\"><img.+?src=\"([^\"]+)[\\s\\S]+?text-truncate\" title=\"([^\"]+)");
        Matcher m = p.matcher(src);
        while (m.find()) {
            result.add(new Manga(getServerID(), m.group(3), m.group(1), HOST + m.group(2)));
        }
        return result;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String user = prefs.getString("username_" + getServerName(), "");
        String password = prefs.getString("dwp_" + getServerName(), "");
        String data = getNavigatorAndFlushParameters()
                .get(HOST + "/search?tag_mode_exc=any&tag_mode_inc=all&tags=&title=" +
                                URLEncoder.encode(term, "UTF-8"),
                        new MangaDexLoginInterceptor(user, password));
        return getMangasFromSource(data);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String data = getNavigatorAndFlushParameters()
                    .get(HOST + "/api/manga/" + manga.getPath().split("/")[2]);
            JSONObject full = new JSONObject(data);
            setMangaInfoFromJson(manga, full.getJSONObject("manga"));
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Set<String> lang = prefs.getStringSet(getServerName() + "_langs", null);
            HashMap<String, String> dictionary = new HashMap<>();
            ArrayList<String> orderedIds = new ArrayList<>();// just because dictionary ids are not ordered as needed
            JSONObject chapters = full.getJSONObject("chapter");
            Iterator<String> ids = chapters.keys();
            boolean filterLangs = false;
            if (lang != null && !lang.isEmpty()) {
                filterLangs = true;
            }
            String id;
            JSONObject cChapter;
            while (ids.hasNext()) {
                id = ids.next();
                cChapter = chapters.getJSONObject(id);
                String lc = cChapter.getString("lang_code");
                if (filterLangs && !lang.contains(lc)) {
                    continue;
                }
                String cn = cChapter.getString("chapter");
                String gn = cChapter.getString("group_name");
                String title = cChapter.getString("title");

                if (dictionary.containsKey(cn)) {
                    dictionary.put(cn,
                            dictionary.get(cn) + "|" + id + "::" + gn + "::" + lc + "::" + title);
                } else {
                    orderedIds.add(cn);
                    dictionary.put(cn, "0" + "|" + id + "::" + gn + "::" + lc + "::" + title);
                }
            }
            String cs = context.getString(R.string.chapter) + " ";
            for (String key : orderedIds) {
                manga.addChapterLast(new Chapter(cs + key, dictionary.get(key) + "|" + key));
            }
        }
    }

    private void setMangaInfoFromJson(Manga manga, JSONObject mangaO) throws JSONException {
        manga.setSynopsis(mangaO.getString("description")
                .replace("[", "<")
                .replace("]", ">"));
        manga.setAuthor(mangaO.getString("author"));
        manga.setFinished(mangaO.getString("status").equals("2"));
        JSONArray genres = mangaO.getJSONArray("genres");
        /*String gText = "";
        for (int i = 0; i < genres.length(); i++) {
           // gText = gText + ", " + content[genres.getInt(i) + 1];
        }*/
        manga.setGenre("");
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        return chapter.getExtra().split("\\|")[page];
    }

    //selected|id1:scan1:lang1:nom1|id2:scan2:lang2:nom2|.....|chapter
    // \d+|
    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String[] paths = chapter.getPath().split("\\|");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String behavior = prefs.getString(getServerName() + "_behavior", "0");
        int idx = 1;
        if (behavior.equals("1")) {
            idx = Integer.parseInt(paths[0]);
        }
        String cid = paths[idx].split("::")[0];
        JSONObject data = new JSONObject(getNavigatorAndFlushParameters().get(HOST + "/api/chapter/" + cid));
        String hash = data.getString("hash");
        String server = data.getString("server");
        JSONArray images = data.getJSONArray("page_array");
        chapter.setPages(images.length());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < images.length(); i++) {
            sb.append("|").append(server).append("/").append(hash).append("/").append(images.getString(i));
        }
        chapter.setExtra(sb.toString());
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public boolean hasFilteredNavigation() {
        return true;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(context.getString(R.string.flt_genre), content, ServerFilter.FilterType.MULTI_STATES)
        };
    }

    @Override
    public Preference[] getCustomPreferences() {
        Preference[] preferences = new Preference[2];
        MultiSelectListPreference multiSelectPref = new MultiSelectListPreference(context);
        multiSelectPref.setKey(getServerName() + "_langs");
        multiSelectPref.setTitle(getServerName() + " langauges filter.");
        multiSelectPref.setEntries(new String[]{"Arabic", "Bengali", "Bulgarian", "Burmese", "Catalan",
                "Chinese (Simp)", "Chinese (Trad)", "Czech", "Danish", "Dutch", "English", "Filipino",
                "Finnish", "French", "German", "Greek", "Hebrew", "Hindi", "Hungarian", "Indonesian",
                "Italian", "Japanese", "Korean", "Lithuanian", "Malay", "Mongolian", "Other", "Persian",
                "Polish", "Portuguese (Br)", "Portuguese (Pt)", "Romanian", "Russian", "Serbo-Croatian",
                "Spanish (Es)", "Spanish (LATAM)", "Swedish", "Thai", "Turkish", "Ukrainian",
                "Vietnamese"});
        multiSelectPref.setEntryValues(new String[]{"sa", "bd", "bg", "mm", "ct", "cn", "hk", "cz", "dk",
                "nl", "gb", "ph", "fi", "fr", "de", "gr", "il", "in", "hu", "id", "it", "jp", "kr", "lt", "my",
                "mn", " ", "ir", "pl", "br", "pt", "ro", "ru", "rs", "es", "mx", "se", "th", "tr", "ua", "vn"});
        preferences[0] = multiSelectPref;
        ListPreference multiScanBehavior = new ListPreference(context);
        multiScanBehavior.setKey(getServerName() + "_behavior");
        multiScanBehavior.setTitle(getServerName() + " multi scanlators behavior");
        multiScanBehavior.setEntries(new String[]{"Always select first.", "Let me decide."});
        multiScanBehavior.setEntryValues(new String[]{"0", "1"});
        preferences[1] = multiScanBehavior;
        return preferences;
    }

    //just log in server to store the cookie if all ok
    @Override
    public boolean testLogin(String user, String password) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addPost("login_username", user);
        nav.addPost("login_password", password);
        nav.addPost("two_factor", "");
        nav.addPost("remember_me", "1");
        nav.addHeader("Referer", HOST + "/login");
        String data = nav.post(HOST + "/ajax/actions.ajax.php?function=login&nojs=1");
        return data.contains(user + "</span>");
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

    @Override
    public boolean supportMultiScans() {
        return true;
    }

    @Override
    public int searchForNewChapters(int id, Context context, boolean fast) {
        Manga mangaDb = Database.getFullManga(context, id);
        ArrayList<Chapter> dbChapters = mangaDb.getChapters();
        Manga manga = new Manga(mangaDb.getServerId(), mangaDb.getTitle(), mangaDb.getPath(), false);
        try {
            String data = getNavigatorAndFlushParameters()
                    .get(HOST + "/api/manga/" + manga.getPath().split("/")[2]);
            JSONObject full = new JSONObject(data);
            setMangaInfoFromJson(manga, full.getJSONObject("manga"));
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Set<String> lang = prefs.getStringSet(getServerName() + "_langs", null);
            JSONObject chaptersO = full.getJSONObject("chapter");
            int nc = 0;
            HashMap<String, String> chapters = new HashMap();
            HashMap<String, Chapter> chaptersFull = new HashMap<>();
            Set<String> scid = new HashSet<>();
            //create a set of added chapters and a map
            for (int i = 0, length = dbChapters.size(); i < length; i++) {
                String[] scans = dbChapters.get(i).getPath().split("\\|");
                String cid = scans[scans.length - 1];
                chapters.put(cid, mangaDb.getChapter(i).getPath().replace("::" + cid, ""));
                chaptersFull.put(cid, mangaDb.getChapter(i));
                for (int j = 1; j < scans.length - 1; j++) {
                    scid.add(scans[j].split("::")[0]);
                }
            }

            Iterator<String> ids = chaptersO.keys();
            boolean filterLangs = false;
            if (lang != null && !lang.isEmpty()) {
                filterLangs = true;
            }
            String idc;
            JSONObject cChapter;
            int newC = 0;
            Set<String> changes = new HashSet<>();
            while (ids.hasNext()) {
                idc = ids.next();
                cChapter = chaptersO.getJSONObject(idc);
                String lc = cChapter.getString("lang_code");
                if (filterLangs && !lang.contains(lc)) {
                    continue;
                }
                String cn = cChapter.getString("chapter");
                String gn = cChapter.getString("group_name");
                String title = cChapter.getString("title");
                if (chapters.containsKey(cn)) {
                    if (!chapters.get(cn).contains(idc + "::")) {
                        chapters.put(cn,
                                chapters.get(cn) + "|" + idc + "::" + gn + "::" + lc + "::" + title);
                        changes.add(cn);
                    }
                } else {
                    chapters.put(cn, "0" + "|" + idc + "::" + gn + "::" + lc + "::" + title);
                    changes.add(cn);
                    newC++;
                }
            }
            String cs = context.getString(R.string.chapter) + " ";
            for (String sid : changes) {
                if (chaptersFull.containsKey(sid)) {
                    Chapter tup = chaptersFull.get(sid);
                    tup.setPath(chapters.get(sid).replace("|" + sid, "") + "|" + sid);
                    Database.updateChapter(context, tup);
                } else {
                    Database.addChapter(context, new Chapter(cs + sid, chapters.get(sid) + "|" + sid), mangaDb.getId());
                }
            }
            return newC;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public class MangaDexLoginInterceptor implements Interceptor {
        String user;
        String password;

        public MangaDexLoginInterceptor(String user, String password) {
            this.user = user;
            this.password = Util.xorDecode(password, getServerName());
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            if (response.code() != 200)
                return response;
            Request request = response.request();
            String domain = request.url().toString();
            String content = Util.extractContent(response);
            if (!content.contains("Certain features disabled for guests during DDoS mitigation")) {//check for reclaims
                return response;
            } else {
                try {
                    if (user.isEmpty() && password.isEmpty()) {
                        Util.getInstance().toast(context, "Certain features disabled for guests during DDoS mitigation");
                        return null;
                    } else {
                        FormBody.Builder builder = new FormBody.Builder();
                        builder.add("login_username", user);
                        builder.add("login_password", password);
                        builder.add("two_factor", "");
                        builder.add("remember_me", "1");

                        Request request0 = new Request.Builder().url(HOST + "/ajax/actions.ajax.php?function=login&nojs=1")
                                .method("POST", builder.build())
                                .addHeader("User-Agent", Navigator.USER_AGENT)
                                .addHeader("Referer", HOST + "/login")
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
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }

    @Override
    public boolean needRefererForImages() {
        return false;
    }
}
