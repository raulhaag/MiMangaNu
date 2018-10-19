package ar.rulosoft.mimanganu.servers;

import android.content.Context;
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
import ar.rulosoft.navegadores.Navigator;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by Raúl on 23/06/2017.
 * pd: no te enojes koneko
 */
class Kumanga extends ServerBase {

    private static final String HOST = "http://www.kumanga.com";

    private static final int[] fltGenre = {
            R.string.flt_tag_action, //Acción
            R.string.flt_tag_martial_arts, //Artes marciales
            R.string.flt_tag_automobiles, //Automóviles
            R.string.flt_tag_adventure, //Aventura
            R.string.flt_tag_sci_fi, //Ciencia Ficción
            R.string.flt_tag_comedy, //Comedia
            R.string.flt_tag_daemons, //Demonios
            R.string.flt_tag_sports, //Deportes
            R.string.flt_tag_doujinshi, //Doujinshi
            R.string.flt_tag_drama, //Drama
            R.string.flt_tag_ecchi, //Ecchi
            R.string.flt_tag_outer_space, //Espacio exterior
            R.string.flt_tag_fantasy, //Fantasía
            R.string.flt_tag_gender_bender, //Gender bender
            R.string.flt_tag_gore, //Gore
            R.string.flt_tag_harem, //Harem
            R.string.flt_tag_hentai, //Hentai
            R.string.flt_tag_historical, //Histórico
            R.string.flt_tag_horror, //Horror
            R.string.flt_tag_josei, //Josei
            R.string.flt_tag_game, //Juegos
            R.string.flt_tag_madness, //Locura
            R.string.flt_tag_magic, //Magia
            R.string.flt_tag_mecha, //Mecha
            R.string.flt_tag_military, //Militar
            R.string.flt_tag_mystery, //Misterio
            R.string.flt_tag_music, //Música
            R.string.flt_tag_kodomo, //Niños
            R.string.flt_tag_parody, //Parodia
            R.string.flt_tag_police, //Policía
            R.string.flt_tag_psychological, //Psicológico
            R.string.flt_tag_slice_of_life, //Recuentos de la vida
            R.string.flt_tag_romance, //Romance
            R.string.flt_tag_samurai, //Samurai
            R.string.flt_tag_seinen, //Seinen
            R.string.flt_tag_shoujo, //Shoujo
            R.string.flt_tag_shoujo_ai, //Shoujo Ai
            R.string.flt_tag_shounen, //Shounen
            R.string.flt_tag_shounen_ai, //Shounen Ai
            R.string.flt_tag_supernatural, //Sobrenatural
            R.string.flt_tag_super_powers, //Súperpoderes
            R.string.flt_tag_suspense, //Suspenso
            R.string.flt_tag_terror, //Terror
            R.string.flt_tag_tragedy, //Tragedia
            R.string.flt_tag_vampire, //Vampiros
            R.string.flt_tag_school_life, //Vida escolar
            R.string.flt_tag_yaoi, //Yaoi
            R.string.flt_tag_yuri, //Yuri
    };

    private static final String[] valGenre = {
            "&category_filter%5B1%5D=1",
            "&category_filter%5B2%5D=2",
            "&category_filter%5B3%5D=3",
            "&category_filter%5B4%5D=4",
            "&category_filter%5B5%5D=5",
            "&category_filter%5B6%5D=6",
            "&category_filter%5B7%5D=7",
            "&category_filter%5B8%5D=8",
            "&category_filter%5B9%5D=9",
            "&category_filter%5B10%5D=10",
            "&category_filter%5B11%5D=11",
            "&category_filter%5B12%5D=12",
            "&category_filter%5B13%5D=13",
            "&category_filter%5B14%5D=14",
            "&category_filter%5B46%5D=46",
            "&category_filter%5B15%5D=15",
            "&category_filter%5B16%5D=16",
            "&category_filter%5B17%5D=17",
            "&category_filter%5B18%5D=18",
            "&category_filter%5B19%5D=19",
            "&category_filter%5B20%5D=20",
            "&category_filter%5B21%5D=21",
            "&category_filter%5B22%5D=22",
            "&category_filter%5B23%5D=23",
            "&category_filter%5B24%5D=24",
            "&category_filter%5B25%5D=25",
            "&category_filter%5B26%5D=26",
            "&category_filter%5B27%5D=27",
            "&category_filter%5B28%5D=28",
            "&category_filter%5B29%5D=29",
            "&category_filter%5B30%5D=30",
            "&category_filter%5B31%5D=31",
            "&category_filter%5B32%5D=32",
            "&category_filter%5B33%5D=33",
            "&category_filter%5B34%5D=34",
            "&category_filter%5B35%5D=35",
            "&category_filter%5B36%5D=36",
            "&category_filter%5B37%5D=37",
            "&category_filter%5B38%5D=38",
            "&category_filter%5B39%5D=39",
            "&category_filter%5B41%5D=41",
            "&category_filter%5B40%5D=40",
            "&category_filter%5B47%5D=47",
            "&category_filter%5B48%5D=48",
            "&category_filter%5B42%5D=42",
            "&category_filter%5B43%5D=43",
            "&category_filter%5B44%5D=44",
            "&category_filter%5B45%5D=45",
    };

    private static final int[] fltType = {
            R.string.flt_tag_manga,
            R.string.flt_tag_manhwa,
            R.string.flt_tag_manhua,
    };
    private static final String[] valType = {
            "&type_filter%5B1%5D=1",
            "&type_filter%5B2%5D=2",
            "&type_filter%5B3%5D=3",
    };

    private static final int[] fltStatus = {
            R.string.flt_status_ongoing,
            R.string.flt_status_completed,
            R.string.flt_status_suspended,
    };
    private static final String[] valStatus = {
            "&status_filter%5B1%5D=1",
            "&status_filter%5B2%5D=2",
            "&status_filter%5B3%5D=3",
    };

    Kumanga(Context context) {
        super(context);
        setFlag(R.drawable.flag_es);
        setIcon(R.drawable.kumanga);
        setServerName("Kumanga");
        setServerID(KUMANGA);
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
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();

        StringBuilder sb = new StringBuilder("contentType=manga&page=");
        sb.append(pageNumber);
        sb.append("&perPage=30&retrieveCategories=true&retrieveAuthors=true");

        for (int i = 0; i < filters[0].length; i++) {
            sb.append(valGenre[filters[0][i]]);
        }
        for (int i = 0; i < filters[1].length; i++) {
            sb.append(valType[filters[1][i]]);
        }
        for (int i = 0; i < filters[2].length; i++) {
            sb.append(valStatus[filters[2][i]]);
        }

        nav.addHeader("Accept-Language", "es-AR,es;q=0.8,en-US;q=0.5,en;q=0.3");
        nav.addHeader("Accept-Encoding", "deflate");
        nav.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        nav.addHeader("X-Requested-With", "XMLHttpRequest");
        RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"),
                sb.toString());
        String data = nav.post(HOST + "/backend/ajax/searchengine.php", body);
        return getMangasFromJson(new JSONObject(data));
    }

    private ArrayList<Manga> getMangasFromJson(JSONObject json) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        JSONArray jsonArray = json.getJSONArray("contents");
        for (int i = 0, j = jsonArray.length(); i < j; i++) {
            JSONObject object = (JSONObject) jsonArray.get(i);
            mangas.add(new Manga(getServerID(), object.getString("name"),
                    HOST + "/manga/" + object.getInt("id") + "/",
                    HOST + "/kumathumb.php?src=" + object.getInt("id")
            ));
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        String web = "contentType=manga&page=1&perPage=30&keywords=";
        web += URLEncoder.encode(term, "UTF-8");
        web += "&retrieveCategories=true&retrieveAuthors=true";

        nav.addHeader("Accept-Language", "es-AR,es;q=0.8,en-US;q=0.5,en;q=0.3");
        nav.addHeader("Accept-Encoding", "deflate");
        nav.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        nav.addHeader("X-Requested-With", "XMLHttpRequest");
        RequestBody body = RequestBody
                .create(MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"), web);
        String data = nav.post(HOST + "/backend/ajax/searchengine.php", body);
        return getMangasFromJson(new JSONObject(data));
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String data = getNavigatorAndFlushParameters().get(manga.getPath());
            // Cover
            manga.setImages(getFirstMatchDefault("</div>\\s*<img src=\"([^\"]+)\"", data, ""));

            // Author
            manga.setAuthor(getFirstMatchDefault("<p><b>Autor: </b>(.+?)</p>", data,
                    context.getString(R.string.nodisponible)));
            // Genre
            manga.setGenre(getFirstMatchDefault("Géneros:(.+?)</div>", data,
                    context.getString(R.string.nodisponible)).replaceAll("</a>\\s*<a", "</a>, <a"));

            // Summary
            manga.setSynopsis(getFirstMatchDefault("id=\"tab1\"><p>(.+?)</p>", data,
                    context.getString(R.string.nodisponible)));
            // Status
            manga.setFinished(!getFirstMatchDefault("<p><b>Estado: </b>(.+?)</p>", data, "Activo")
                    .contains("Activo"));
            // Chapters
            int pages = (Integer.parseInt(getFirstMatchDefault("php_pagination\\([^,]+,[^,]+,[^,]+,[^,]+,([^,]+),.+?\\)", data, "10")) / 10) + 1;
            Pattern pattern = Pattern.compile("<h4 class=\"title\">\\s*<a href=\"(manga[^\"]+).+?>(.+?)</i>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(data);
            ArrayList<Chapter> temp = new ArrayList<>();
            while (matcher.find()) {
                temp.add(0, new Chapter(matcher.group(2), HOST + "/" + matcher.group(1)));
            }
            try {
                data = getAllMatch("leer\\/'\\+this.value\">([\\s\\S]+?)<\\/select>",
                        getNavigatorAndFlushParameters().get(temp.get(0).getPath().replace("/c/", "/leer/"))).get(1);
                pattern = Pattern.compile("value=\"([^\"]+)\"[ selected]*>([^<]+)", Pattern.DOTALL);
                matcher = pattern.matcher(data);
                while (matcher.find()) {
                    manga.addChapterLast(new Chapter(matcher.group(2), HOST + "/manga/leer/" + matcher.group(1)));
                }
                return;
            }catch (Exception e){
                pattern = Pattern.compile("<h4 class=\"title\">\\s*<a href=\"(manga[^\"]+).+?>(.+?)</i>", Pattern.DOTALL);
                for (int i = 2; i <= pages; i++) {
                    data = getNavigatorAndFlushParameters().get(manga.getPath() + "/p/" + i);
                    matcher = pattern.matcher(data);
                    while (matcher.find()) {
                        temp.add(0,new Chapter(matcher.group(2), HOST + "/" + matcher.group(1)));
                    }
                }
            }
            manga.setChapters(temp);
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        assert chapter.getExtra() != null;
        if (chapter.getExtra().contains("|")) {
            return chapter.getExtra().split("\\|")[page - 1];
        } else {
            return chapter.getExtra().replaceAll("\\{.+?\\}", "" + page);
        }
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getPages() == 0) {
            String data = getNavigatorAndFlushParameters().get(chapter.getPath().replace("/c/", "/leer/"));
            String pages = getFirstMatch(
                    "<select class=\"pageselector.+?>(\\d+)</option>[\\s]*</select>", data,
                    context.getString(R.string.server_failed_loading_page_count));
            chapter.setExtra(getFirstMatch(
                    "'pageFormat':'(.+?)'", data,
                    context.getString(R.string.server_failed_loading_chapter)));
            String pURLs = getFirstMatchDefault("var pUrl=\\[(.+?)\\]", data, "");
            if (pURLs.contains("\"npage\":\"1\"")) {
                chapter.setExtra(TextUtils.join("|", getAllMatch("\"imgURL\":\"([^\"]+)", pURLs.replaceAll("\\\\", ""))));
            }
            chapter.setPages(Integer.parseInt(pages));
        }
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(
                        context.getString(R.string.flt_genre),
                        buildTranslatedStringArray(fltGenre), ServerFilter.FilterType.MULTI),
                new ServerFilter(
                        context.getString(R.string.flt_type),
                        buildTranslatedStringArray(fltType), ServerFilter.FilterType.MULTI),
                new ServerFilter(
                        context.getString(R.string.flt_status),
                        buildTranslatedStringArray(fltStatus), ServerFilter.FilterType.MULTI)};
    }
}
