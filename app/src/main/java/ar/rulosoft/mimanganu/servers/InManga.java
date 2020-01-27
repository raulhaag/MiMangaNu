package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.navegadores.Navigator;

public class InManga extends ServerBase {

    public static final String HOST = "https://inmanga.com";

    String[] genreV = new String[]{"-1", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44",
            "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61",
            "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78",
            "79", "80", "81", "82", "83", "84"};
    String[] genre = new String[]{"Todos", "Aventura", "Shounen", "Suspenso", "Misterio", "Acción", "Fantasía",
            "Gore", "Sobrenatural", "Romance", "Drama", "Artes Marciales", "Ciencia Ficción", "Thriller",
            "Comedia", "Mecha", "Supernatural", "Tragedia", "Adulto", "Harem", "Yuri", "Seinen", "Horror",
            "Webtoon", "Apocalíptico", "Boys Love", "Ciberpunk", "Crimen", "Demonios", "Deporte", "Ecchi",
            "Extranjero", "Familia", "Fantasia", "Gender Bender", "Girls Love", "Guerra", "Historia",
            "Magia", "Militar", "Musica", "Parodia", "Policiaco", "Psicológico", "Realidad",
            "Realidad Virtual", "Recuentos de la vida", "Reencarnación", "Samurái", "Superpoderes",
            "Supervivencia", "Vampiros", "VidaEscolar"};

    String[] sort = new String[]{"Nombre", "Relevacia", "Vistos", "Recién agregados", "Recién actualizados", "Más capítulos", "Menos capítulos"};
    String[] sortV = new String[]{"5", "2", "1", "4", "3", "6", "7"};

    String[] state = new String[]{"Todos", "En progreso", "Finalizados"};
    String[] stateV = new String[]{"0", "1", "2"};


    /**
     * Construct a new ServerBase object.
     *
     * @param context the context for this object
     */
    public InManga(Context context) {
        super(context);
        setFlag(R.drawable.flag_es);
        setIcon(R.drawable.inmanga);
        setServerName("InManga");
        setServerID(INMNAGA);
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addPost("filter[generes][]", "-1");
        nav.addPost("filter[queryString]", URLEncoder.encode(term, "UTF-8"));
        nav.addPost("filter[skip]", "0");
        nav.addPost("filter[take]", "100");
        nav.addPost("filter[sortby]", "2");
        nav.addPost("filter[broadcastStatus]", "0");
        nav.addPost("filter[onlyFavorites]", "false");
        nav.addPost("d", "");
        String data = nav.post(HOST + "/manga/getMangasConsultResult");
        return getMangasFromSource(data);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            Navigator nav = getNavigatorAndFlushParameters();
            String data = nav.get(HOST + manga.getPath());
            String img = getFirstMatchDefault("src=\"(\\/thumbnails\\/.+?)\"", data, "");
            img = img.length() > 0 ? HOST + img : img;
            manga.setImages(img);
            manga.setFinished(getFirstMatchDefault("<span class=\"label.+?pull-right\">(.+?)<", data, "En emisi").contains("En emisi"));
            manga.setSynopsis(getFirstMatchDefault("<div class=\"panel-body\">([\\s\\S]+?)<", data, context.getString(R.string.nodisponible)));
            manga.setAuthor(context.getString(R.string.nodisponible));
            manga.setGenre(context.getString(R.string.nodisponible));
            JSONObject jO = new JSONObject(nav.get(HOST + "/chapter/getall?mangaIdentification=" + manga.getPath().split("/")[4]));
            JSONArray ca = new JSONObject(jO.getString("data")).getJSONArray("result");
            List<JSONObject> jsonValues = new ArrayList<JSONObject>();
            for (int i = 0; i < ca.length(); i++) {
                jsonValues.add(ca.getJSONObject(i));
            }
            Collections.sort(jsonValues, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject a, JSONObject b) {
                    double valA = 0;
                    double valB = 0;

                    try {
                        valA = a.getDouble("Number");
                        valB = b.getDouble("Number");
                    } catch (JSONException e) {
                        //do something
                    }
                    return Double.compare(valA, valB);
                }
            });
            for (JSONObject o : jsonValues) {
                manga.addChapterLast(new Chapter("Capítulo " + o.getString("Number"), o.getString("Identification")));
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String[] d1 = chapter.getExtra().split("\\|");
        return d1[0] + page + "/" + d1[page];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data = getNavigatorAndFlushParameters().get(HOST + "/chapter/chapterIndexControls?identification=" + chapter.getPath());
        data = getFirstMatch("PagesSourceChange\\(this\\)\">([\\s\\S]+?)<\\/select>", data, context.getString(R.string.error));
        ArrayList<String> img = getAllMatch("<option value=\"([^\"]+)", data);
        String manga = getFirstMatchDefault("\"([^\"]+)\" id=\"FriendlyMangaName\"", data, "mangaName");
        String cn = getFirstMatchDefault("\"([^\"]+)\" id=\"ChapterNumber\"", data, "1");
        chapter.setExtra(HOST + "/images/manga/" + manga + "/chapter/" + cn + "/page/|" + TextUtils.join("|", img));
        chapter.setPages(img.size());
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        int take = 25;
        Navigator nav = getNavigatorAndFlushParameters();
        int skip = take * (pageNumber - 1);
        nav.addPost("filter[generes][]", genreV[filters[0][0]]);
        nav.addPost("filter[queryString]", "");
        nav.addPost("filter[skip]", "" + skip);
        nav.addPost("filter[take]", "" + take);
        nav.addPost("filter[sortby]", sortV[filters[1][0]]);
        nav.addPost("filter[broadcastStatus]", stateV[filters[1][0]]);
        nav.addPost("filter[onlyFavorites]", "false");
        nav.addPost("d", "");
        String data = nav.post(HOST + "/manga/getMangasConsultResult");
        return getMangasFromSource(data);
    }

    public ArrayList<Manga> getMangasFromSource(String data) {
        Pattern p = Pattern.compile("<a href=\"([^\"]+)[\\s\\S]+?(<h4.+?<\\/h4>)[\\s\\S]+?src=\"([^\"]+)");
        Matcher m = p.matcher(data);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(2).replaceAll("<.+?>", ""), m.group(1), HOST + m.group(3)));
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(
                        context.getString(R.string.flt_genre),
                        genre, ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_order),
                        sort, ServerFilter.FilterType.SINGLE),

                new ServerFilter(
                        context.getString(R.string.flt_status),
                        state, ServerFilter.FilterType.SINGLE),
        };
    }

}
