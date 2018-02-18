package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;

/**
 * Created by Raúl on 18/02/2018.
 */

public class MangaRawOnline extends ServerBase {

    private static final String HOST = "http://mangaraw.online";
    private static final int[] fltOrder = {
            R.string.flt_order_views,
            R.string.flt_order_title
    };
    private static final String[] orderV = {
            "&sortBy=views",
            "&sortBy=name"
    };
    private static final int[] fltOrderDir = {
            R.string.flt_order_descending,
            R.string.flt_order_ascending
    };
    private static final String[] orderDirV = {
            "&asc=false",
            "&asc=true"
    };
    private static int last_page = 1000;
    protected int[] fltGenre = {
            R.string.flt_tag_all,
            R.string.flt_tag_action,
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
            R.string.flt_tag_shoujo_ai,
            R.string.flt_tag_shounen,
            R.string.flt_tag_shounen_ai,
            R.string.flt_tag_slice_of_life,
            R.string.flt_tag_sports,
            R.string.flt_tag_supernatural,
            R.string.flt_tag_tragedy,
            R.string.flt_tag_yaoi,
            R.string.flt_tag_yuri,
            R.string.flt_tag_adult
    };

    public MangaRawOnline(Context context) {
        super(context);
        setFlag(R.drawable.flag_raw);
        setIcon(R.drawable.noimage);
        setServerName("MangaRawOnline");
        setServerID(MANGARAWONLINE);
    }

    @Nullable
    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String data = getNavigatorAndFlushParameters().get("http://mangaraw.online/search?query=" + URLEncoder.encode(term, "UTF-8"));
        JSONArray results = new JSONObject(data).getJSONArray("suggestions");
        ArrayList<Manga> mangas = new ArrayList<>();
        for(int i = 0; i < results.length(); i++){
            JSONObject result = results.getJSONObject(i);
            mangas.add(new Manga(getServerID(), result.getString("value"), "/manga/" + result.getString("data"), false));
        }
        return mangas;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {

            String data = getNavigatorAndFlushParameters().get(HOST + manga.getPath());

            manga.setSynopsis(getFirstMatchDefault("<h5><strong>Summary</strong></h5>\\s*<p>(.+?)</p>", data, context.getString(R.string.nodisponible)));

            manga.setAuthor(getFirstMatchDefault("<dt>Author\\(s\\)</dt>\\s+<dd>([\\s\\S]+?)</dd>", data, context.getString(R.string.nodisponible)).replaceAll(" <a href=\".+?\">(.+?)</a>", "$1,"));

            if (data.contains("<span class=\"label label-success\">Ongoing</span>")) {
                manga.setFinished(false);
            } else {
                manga.setFinished(true);
            }

            manga.setGenre(getFirstMatchDefault("<dt>Categories</dt>\\s+<dd>([\\s\\S]+?)</dd>", data, context.getString(R.string.nodisponible)).replaceAll(" <a href=\".+?\">(.+?)</a>", "$1"));

            manga.setImages(getFirstMatchDefault("class=\"img-responsive\" src='(.+?)'", data, ""));
            // chapter
            Pattern p = Pattern.compile("<h5 class=\"chapter-title-rtl\">\\s*<a href=\"https?://mangaraw.online(.+?)\">(.+?)<", Pattern.DOTALL);
            Matcher m = p.matcher(data);
            while (m.find()) {
                manga.addChapterFirst(new Chapter(m.group(2), m.group(1)));
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        return chapter.getExtra().split("\\|")[page];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data = getNavigatorAndFlushParameters().get(HOST + chapter.getPath());
        ArrayList<String> images = getAllMatch("data-src='\\s*(.+?)\\s*'", data);
        if(images.size() > 0) {
            chapter.setExtra("|" + TextUtils.join("|", images));
            chapter.setPages(images.size());
        }
    }

    @Override
    public boolean hasList() {
        return false;
    }


    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        // http://mangaraw.online/filterList?page=1&cat=3&alpha=&sortBy=name&asc=true&author=&tag=
        if(pageNumber == 1)
            last_page = 10;
        ArrayList<Manga> mangas = new ArrayList<>();
        if(last_page > pageNumber) {
            String cat = "&cat=";
            if (filters[0][0] != 0) {
                cat = cat + filters[0][0];
            }
            String url = HOST + "/filterList?page=" + (pageNumber) + cat + "&alpha=" + orderV[filters[1][0]] + orderDirV[filters[2][0]];
            String data = getNavigatorAndFlushParameters().get(url);

            last_page = Integer.parseInt(getFirstMatchDefault(">(\\d+)<\\/a><\\/li><li>[^>]+?page[^>]+?rel", data, "" + (pageNumber + 1)));
            String name;
            Pattern p = Pattern.compile("src=['|\"](.+?)['|\"] alt=['|\"](.*?)['|\"][\\s\\S]+?(/manga/.+?)['|\"]", Pattern.DOTALL);
            Matcher m = p.matcher(data);
            while (m.find()) {
                if(m.group(2).isEmpty()){
                    name = m.group(3).substring(m.group(3).lastIndexOf("/") + 1).replaceAll("-"," ").replace(".html","");
                }else{
                    name = m.group(2);
                }
                Manga manga = new Manga(getServerID(), name, m.group(3), false);
                if(!m.group(1).equals("http://mangaraw.online/uploads/no-image.png"))
                    manga.setImages(m.group(1));
                else {
                    String iPath = m.group(3).substring(m.group(3).lastIndexOf("/") + 1).replace(".html","");
                    manga.setImages(HOST + "/uploads/manga/" + iPath + "/cover/cover_250x350.jpg");
                }
                mangas.add(manga);
            }
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(
                        context.getString(R.string.flt_genre),
                        buildTranslatedStringArray(fltGenre), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_order_by),
                        buildTranslatedStringArray(fltOrder), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_order),
                        buildTranslatedStringArray(fltOrderDir), ServerFilter.FilterType.SINGLE)
        };
    }
}
