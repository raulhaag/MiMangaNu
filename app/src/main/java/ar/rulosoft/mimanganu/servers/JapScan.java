package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;

import static ar.rulosoft.mimanganu.utils.PostProcess.FLAG_PPL90;

/**
 * Created by xtj-9182 on 21.02.2017.
 */
class JapScan extends ServerBase {
    private static final String TAGWEBTOON = "[1webtoon1]";
    private static final String HOST = "https://www.japscan.co";
    private static HashMap<String, String> dicc = new HashMap<>();
    private static ArrayList<String> currentScripts = new ArrayList<>();

    private String[] letterFilter = new String[]{"All", "0-9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private String[] pageFilter = new String[]{"0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120"};

    JapScan(Context context) {
        super(context);
        setFlag(R.drawable.flag_fr);
        setIcon(R.drawable.japscan);
        setServerName("JapScan");
        setServerID(JAPSCAN);
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
    public ArrayList<Manga> search(String search) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addPost("search", URLEncoder.encode(search, "UTF-8"));
        nav.addHeader("Content-Type", "application/x-www-form-urlencoded");
        nav.addHeader("X-Requested-With", "XMLHttpRequest");
        nav.addHeader("Referer", HOST);
        String source = nav.post(HOST + "/live-search/");
        if (source.length() > 2) {
            JSONArray jsonArray = new JSONArray(source);
            JSONObject item;
            for (int i = 0; i < jsonArray.length(); i++) {
                item = (JSONObject) jsonArray.get(i);
                mangas.add(new Manga(getServerID(), item.getString("name"), item.getString("url"), false));
            }
        }
        return mangas;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public synchronized void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String source = getNavigatorAndFlushParameters().get(HOST + manga.getPath());

            // Cover Image
            // JapScan has no cover images ...
            manga.setImages(HOST + getFirstMatchDefault("<div class=\"m-2\">[\\s\\S]+?src=\"([^\"]+)", source, ""));

            // Summary
            manga.setSynopsis(getFirstMatchDefault("Synopsis:</div>[\\s\\S]+?<p[^>]+>([^<]+)", source, context.getString(R.string.nodisponible)));

            // Status
            manga.setFinished(getFirstMatchDefault("Statut:</span>([^<]+)", source, "").contains("Terminé"));

            // Author
            manga.setAuthor(getFirstMatchDefault("Auteur\\(s\\):</span>([^<]+)", source, context.getString(R.string.nodisponible)).trim());

            // Genres
            manga.setGenre(getFirstMatchDefault("Type\\(s\\):</span>([^<]+)", source, context.getString(R.string.nodisponible)));

            // Chapters
            Pattern pattern = Pattern.compile("<div class=\"chapters_list text-truncate\">[\\s\\S]+?href=\"([^\"]+)\">([^<]+)", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(source);
            while (matcher.find()) {
                manga.addChapterFirst(new Chapter(matcher.group(2), matcher.group(1)));
            }
        }
    }

    public String getImageFrom(Chapter chapter, int page) throws Exception {
        if (chapter.getExtra() != null && chapter.getExtra().contains(TAGWEBTOON)) {
            String[] parts = chapter.getExtra().split("\\|");
            return parts[page] + "|" + parts[0] + (parts[parts.length - 1].equals(FLAG_PPL90) ? FLAG_PPL90 : "");
        } else {
            String source = getNavigatorAndFlushParameters().get(HOST + chapter.getPath() + page + ".html");
            String extra = "";
            if (source.contains("iYFbYi_UibMqYb.js")) {
                extra = FLAG_PPL90;
            }
            String encoded = getFirstMatch("<div id=\"image\" data-src=\"(https://c.japscan.co/.+?)\"", source, "Error obtaining img");
            String cd = getFirstMatch("<script src=\"\\/zjs\\/(.+?)\\.", source, context.getString(R.string.error_downloading_image));
            generateDictionary(cd);
            String decoded = imageDecode(encoded);
            return decoded + "|" + HOST + chapter.getPath() + page + ".html" + extra;
        }
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern pattern = Pattern.compile("\"img-fluid\" src=\"([^\"]+)[\\s\\S]+?<a[^>]+?href=\"([^\"]+)\">([^<]+)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            Manga manga = new Manga(getServerID(), matcher.group(3), matcher.group(2), HOST + matcher.group(1));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public synchronized ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
       /* String extra = "";
        if (filters[0][0] != 0) {
            extra = letterFilter[filters[0][0]] + "/";
        }*/
        String source = getNavigatorAndFlushParameters().get(HOST + "/mangas/" + (pageNumber - 1 + (filters[0][0] * 10)));
        return getMangasFromSource(source);
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getPages() == 0) {
            String source = getNavigatorAndFlushParameters().get(HOST + chapter.getPath());
            String pages = getFirstMatch("Page (\\d+)</option>\\s*</select>", source,
                    context.getString(R.string.server_failed_loading_image));
            ArrayList<String> imgs = getAllMatch("<option[^<]+?data-img=\"([^\"]+)\"", source);
            String encoded = getFirstMatch("<div id=\"image\" data-src=\"(https://c.japscan.co/.+?)\"", source, "Error obtaining img");
            if (imgs.get(0).equals(encoded)) {
                String cd = getFirstMatch("<script src=\"\\/zjs\\/(.+?)\\.", source, context.getString(R.string.error_downloading_image));
                String extra = "";
                if (source.contains("iYFbYi_UibMqYb.js")) {
                    extra = "|" + FLAG_PPL90;
                }
                generateDictionary(cd);
                for (int i = 0; i < imgs.size(); i++) {
                    imgs.set(i, imageDecode(imgs.get(i)));
                }
                chapter.setExtra(HOST + chapter.getPath() + "|" + TextUtils.join("|", imgs) + "|" + TAGWEBTOON + extra);
            }
            chapter.setPages(Integer.parseInt(pages));
        }
    }

    @Override
    public boolean hasSearch() {
        return true;
    }

    @Override
    public boolean needRefererForImages() {
        return true;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(context.getString(R.string.flt_page), pageFilter, ServerFilter.FilterType.SINGLE),
                //  new ServerFilter(context.getString(R.string.flt_alpha), letterFilter, ServerFilter.FilterType.SINGLE),
        };
    }

    private String imageDecode(String fakeURL) {
        int point = fakeURL.indexOf("/", 9);
        String origin = fakeURL.substring(0, point);
        String path = fakeURL.substring(point, fakeURL.lastIndexOf("."));
        String ext = fakeURL.substring(fakeURL.lastIndexOf("."));

        StringBuilder sb = new StringBuilder(origin);
        for (String s : path.split("")) {
            if (!s.isEmpty()) {
                sb.append(dicc.get(s));
            }
        }
        sb.append(ext);
        return sb.toString();
    }


    private synchronized void generateDictionary(String newDicName) throws Exception {
        if (!currentScripts.contains(newDicName)) {
            Util.getInstance().toast(context, "Generating dictionary init");
            JSONObject object = new JSONObject(getNavigatorAndFlushParameters().get("https://raw.githubusercontent.com/raulhaag/MiMangaNu/master/js_plugin/28.json"));
            ArrayList<Integer> idxs = new ArrayList<>();
            ArrayList<String> values = new ArrayList<>();
            ArrayList<String> sources = new ArrayList<>();
            JSONArray array = object.getJSONArray("idxs");
            for (int i = 0; i < array.length(); i++) {
                idxs.add(array.getInt(i));
            }
            array = object.getJSONArray("values");
            for (int i = 0; i < array.length(); i++) {
                values.add(array.getString(i));
            }
            array = object.getJSONArray("pages");
            for (int i = 0; i < array.length(); i++) {
                sources.add(array.getString(i));
            }
            int cl = object.getInt("length");
            StringBuilder enc = new StringBuilder();
            dicc.clear();
            currentScripts.clear();
            for (String web : sources) {
                String data = getNavigatorAndFlushParameters().get(web);
                String id = getFirstMatch("<script src=\"\\/zjs\\/(.+?)\\.", data, context.getString(R.string.error_downloading_image));
                if(!currentScripts.contains(id)){
                    currentScripts.add(id);
                }
                enc.append(getFirstMatch("<div id=\"image\" data-src=\"https://c.japscan.co(/.+?)\\..{3,4}\"", data, "Error creating dictionary (Obtaining bases)"));
            }
            if(currentScripts.size() != 3){
                throw new Exception("Error creating dictionary (ids error, maybe hour change?)");
            }
            if (enc.length() == cl) {
                for (int i = 0; i < idxs.size(); i++) {
                    dicc.put("" + enc.charAt(idxs.get(i)), values.get(i));
                }
            } else {
                throw new Exception("Error creating dictionary (not valid length)");
            }
            Util.getInstance().toast(context, "Generating dictionary end");
        }
    }

}