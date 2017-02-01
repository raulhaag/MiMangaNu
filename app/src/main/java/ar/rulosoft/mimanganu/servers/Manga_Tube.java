package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;

/**
 * Created by Raul on 02/12/2015.
 */
public class Manga_Tube extends ServerBase {

    public static String[] sort = new String[]{
            "dem Alphabet", "Beliebtheit", "Aufrufen", "Erstveröffentlichung", "Bewertung"
    };
    public static String[] sortV = new String[]{
            "alphabetic", "popularity", "hits", "date", "rating"
    };

    public static String[] order = new String[]{
            "↓", "↑"
    };
    public static String[] orderV = new String[]{
            "asc", "desc"
    };


    private static String[] genre = new String[]{
            "Alle", "0-9",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "W", "X", "Y", "Z"
    };
    private static String[] genreV = new String[]{
            "", "#",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "W", "X", "Y", "Z"
    };
    int[][] no_more_pages;

    public Manga_Tube(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_de);
        this.setIcon(R.drawable.mangatube);
        this.setServerName("Manga-tube");
        setServerID(ServerBase.MANGATUBE);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addPost("action", "search_query");
        nav.addPost("parameter[query]", URLEncoder.encode(term, "UTF-8"));
        JSONArray jsonArray = new JSONObject(nav.post("https://manga-tube.me/ajax")).getJSONArray("suggestions");
        ArrayList<Manga> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                Manga m = new Manga(MANGATUBE, object.getString("value"), "https://manga-tube.me/series/" + object.getString("manga_slug"), false);
                result.add(m);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 || forceReload)
            loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        String source = nav.post(manga.getPath());
        // Front
        manga.setImages(getFirstMatchDefault("data-original=\"(.+?)\"", source, ""));
        // Summary
        String summary = getFirstMatchDefault("<h4>Beschreibung</h4>\\s+<p>(.+?)</p>",
                source, defaultSynopsis).replaceAll("<.+?>", "");
        manga.setSynopsis(Util.getInstance().fromHtml(summary.replaceFirst("Zusammenfassung:", "")).toString());
        //Author
        manga.setAuthor(Util.getInstance().fromHtml(getFirstMatchDefault("<b>Autor:<\\/b>&nbsp;(.+?)<\\/li>", source, "N/A")).toString().replaceAll("\\s+", "").trim());
        //Finished
        manga.setFinished(getFirstMatchDefault("<b>Status \\(Scanlation\\):</b>(.+?)</li>", source, "laufend").contains("abgeschlossen"));
        //Genres
        manga.setGenre((Util.getInstance().fromHtml(getFirstMatchDefault("<li><b>Genre:</b>&nbsp;(.+?)</ul>", source, "").replaceAll("</a></li>", "</a></li>,")).toString().replaceAll("\\s+", "").replaceAll(",", ", ").trim()));
        // Chapter
        Pattern p = Pattern.compile(
                "(manga-tube.me[^\"]+?read[^\"]+?)\".+?\">.+?<b>(.+?)</b>");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(2), "http://" + matcher.group(1)));
        }
        manga.setChapters(chapters);
    }


    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return chapter.getPath() + "page/" + page;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String[] d1 = chapter.getExtra().split("\\|");
        return d1[page];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        String source = nav.get(chapter.getPath(), chapter.getPath());
        String img_path = getFirstMatch("img_path: '(.+?)'", source, "can't initialize the chapter");
        source = getFirstMatch("pages:\\s\\[(.+?)\\]", source, "can't initialize the chapter 2");
        ArrayList<String> pages = getAllMatch("\"file_name\":\"(.+?)\"", source);
        chapter.setPages(pages.size());
        String images = "";
        for (String d : pages) {
            images = images + "|" + img_path + d;
        }
        chapter.setExtra(images);
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filter, int pageNumber) throws Exception {
        if (no_more_pages != filter) {
            Navigator nav = getNavigatorAndFlushParameters();
            nav.addPost("action", "load_series_list_entries");
            nav.addPost("parameter[letter]", genreV[filter[0][0]]);
            nav.addPost("parameter[order]", orderV[filter[2][0]]);
            nav.addPost("parameter[page]", "" + pageNumber);
            nav.addPost("parameter[sortby]", sortV[filter[1][0]]);
            JSONObject object = new JSONObject(nav.post("https://manga-tube.me/ajax"));
            try {
                return getMangasFromJson(object.getJSONObject("success"));
            } catch (Exception e) {
                no_more_pages = filter;
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }

    public ArrayList<Manga> getMangasFromJson(JSONObject jSO) throws Exception {
        ArrayList<Manga> result = new ArrayList<>();
        Iterator<?> keys = jSO.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (jSO.get(key) instanceof JSONObject) {
                JSONObject manObj = jSO.getJSONObject(key);
                Manga m = new Manga(MANGATUBE, manObj.getString("manga_title"), "https://manga-tube.me/series/" + manObj.getString("manga_slug"), false);
                m.setImages(manObj.getJSONArray("covers").getJSONObject(0).getString("img_name"));
                result.add(m);
            }
        }
        return result;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{new ServerFilter("Index", genre, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Sortiert nach", sort, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Bestellen", order, ServerFilter.FilterType.SINGLE)};
    }

    @Override
    public boolean hasList() {
        return false;
    }


}
