package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by Raúl on 23/06/2017.
 * <p>
 * pd: no te enojes koneko
 */

public class Kumanga extends ServerBase {

    public static String HOST = "http://www.kumanga.com/";

    private static final String[] genre = {
            "Acción", "Artes marciales", "Automóviles", "Aventura", "Ciencia Ficción",
            "Comedia", "Demonios", "Deportes", "Doujinshi", "Drama", "Ecchi", "Espacio exterior",
            "Fantasía", "Gender bender", "Gore", "Harem", "Hentai", "Histórico", "Horror", "Josei",
            "Juegos", "Locura", "Magia", "Mecha", "Militar", "Misterio", "Música", "Niños",
            "Parodia", "Policía", "Psicológico", "Recuentos de la vida", "Romance", "Samurai",
            "Seinen", "Shoujo", "Shoujo Ai", "Shounen", "Shounen Ai", "Sobrenatural",
            "Súperpoderes", "Suspenso", "Terror", "Tragedia", "Vampiros", "Vida escolar", "Yaoi",
            "Yuri"
    };

    private static final String[] genreV = {
            "&category_filter%5B1%5D=1", "&category_filter%5B2%5D=2", "&category_filter%5B3%5D=3",
            "&category_filter%5B4%5D=4", "&category_filter%5B5%5D=5", "&category_filter%5B6%5D=6",
            "&category_filter%5B7%5D=7", "&category_filter%5B8%5D=8", "&category_filter%5B9%5D=9",
            "&category_filter%5B10%5D=10", "&category_filter%5B11%5D=11",
            "&category_filter%5B12%5D=12", "&category_filter%5B13%5D=13",
            "&category_filter%5B14%5D=14", "&category_filter%5B46%5D=46",
            "&category_filter%5B15%5D=15", "&category_filter%5B16%5D=16",
            "&category_filter%5B17%5D=17", "&category_filter%5B18%5D=18",
            "&category_filter%5B19%5D=19", "&category_filter%5B20%5D=20",
            "&category_filter%5B21%5D=21", "&category_filter%5B22%5D=22",
            "&category_filter%5B23%5D=23", "&category_filter%5B24%5D=24",
            "&category_filter%5B25%5D=25", "&category_filter%5B26%5D=26",
            "&category_filter%5B27%5D=27", "&category_filter%5B28%5D=28",
            "&category_filter%5B29%5D=29", "&category_filter%5B30%5D=30",
            "&category_filter%5B31%5D=31", "&category_filter%5B32%5D=32",
            "&category_filter%5B33%5D=33", "&category_filter%5B34%5D=34",
            "&category_filter%5B35%5D=35", "&category_filter%5B36%5D=36",
            "&category_filter%5B37%5D=37", "&category_filter%5B38%5D=38",
            "&category_filter%5B39%5D=39", "&category_filter%5B41%5D=41",
            "&category_filter%5B40%5D=40", "&category_filter%5B47%5D=47",
            "&category_filter%5B48%5D=48", "&category_filter%5B42%5D=42",
            "&category_filter%5B43%5D=43", "&category_filter%5B44%5D=44",
            "&category_filter%5B45%5D=45"
    };

    private static final String[] type = {"Manga", "Manhwa", "Manhua"};

    private static final String[] typeV = {
            "&type_filter%5B1%5D=1", "&type_filter%5B2%5D=2", "&type_filter%5B3%5D=3",
    };

    private static final String[] state = {"Activo", "Finalizado", "Inconcluso"};

    private static final String[] stateV = {
            "&status_filter%5B1%5D=1", "&status_filter%5B2%5D=2", "&status_filter%5B3%5D=3",
    };

    public Kumanga(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_es);
        this.setIcon(R.drawable.kumanga);
        this.setServerName("Kumanga");
        setServerID(ServerBase.KUMANGA);
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
            sb.append(genreV[filters[0][i]]);
        }
        for (int i = 0; i < filters[1].length; i++) {
            sb.append(typeV[filters[1][i]]);
        }
        for (int i = 0; i < filters[2].length; i++) {
            sb.append(stateV[filters[2][i]]);
        }

        nav.addHeader("Accept-Language", "es-AR,es;q=0.8,en-US;q=0.5,en;q=0.3");
        nav.addHeader("Accept-Encoding", "deflate");
        nav.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        nav.addHeader("X-Requested-With", "XMLHttpRequest");
        RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"),
                sb.toString());
        String data = nav.post("http://www.kumanga.com/backend/ajax/searchengine.php",
                body);
        return getMangasFromJson(new JSONObject(data));
    }

    private ArrayList<Manga> getMangasFromJson(JSONObject json) {
        ArrayList<Manga> mangas = new ArrayList<>();
        try {
            JSONArray jsonArray = json.getJSONArray("contents");
            for (int i = 0, j = jsonArray.length(); i < j; i++) {
                JSONObject object = (JSONObject) jsonArray.get(i);
                Manga m = new Manga(KUMANGA, object.getString("name"),
                        "http://www.kumanga.com/manga/" + object.getInt("id") + "/", false);
                m.setImages("http://www.kumanga.com/kumathumb.php?src=" + object.getInt("id"));
                mangas.add(m);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        StringBuilder sb = new StringBuilder("contentType=manga&page=1&perPage=30&keywords=");
        sb.append(term);
        sb.append("&retrieveCategories=true&retrieveAuthors=true");

        nav.addHeader("Accept-Language", "es-AR,es;q=0.8,en-US;q=0.5,en;q=0.3");
        nav.addHeader("Accept-Encoding", "deflate");
        nav.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        nav.addHeader("X-Requested-With", "XMLHttpRequest");
        RequestBody body = RequestBody
                .create(MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"),
                sb.toString());
        String data = nav.post("http://www.kumanga.com/backend/ajax/searchengine.php",
                body);
        return getMangasFromJson(new JSONObject(data));    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga,forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().size() == 0 || forceReload) {
            String data = getNavigatorAndFlushParameters().get(manga.getPath());
            //Autor
            manga.setAuthor(Util.getInstance().fromHtml(
                    getFirstMatchDefault("<p><b>Autor: </b>(.+?)</p>", data,
                            context.getString(R.string.nodisponible))).toString());
            //Generos
            manga.setGenre(Util.getInstance().fromHtml(
                    getFirstMatchDefault("Géneros:(.+?)</div>", data,
                            context.getString(R.string.nodisponible))).toString());

            //Sinopsis
            manga.setSynopsis(Util.getInstance().fromHtml(
                    getFirstMatchDefault("id=\"tab1\"><p>(.+?)</p>", data,
                            context.getString(R.string.nodisponible))).toString());
            //Estado
            manga.setFinished(!getFirstMatchDefault("<p><b>Estado: </b>(.+?)</p>", data, "Activo")
                    .contains("Activo"));
            //Capítulos
            manga.getChapters().clear();
            Pattern pattern = Pattern.compile("<td><a href=\"(manga[^\"]+).+?>(.+?)</i>");
            Matcher matcher = pattern.matcher(data);
            while (matcher.find()) {
                manga.addChapter(new Chapter(matcher.group(2).trim().replaceAll("<.*?>", ""),
                        HOST + matcher.group(1)));
            }
        }
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return null;
    }

    // http://img.kumanga.com/manga/{chapterid}/{pnumber}.jpg
    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        return "http://img.kumanga.com/manga/" + chapter.getExtra() + "/" + page + ".jpg";
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data = getNavigatorAndFlushParameters().get(chapter.getPath().replace("/c/", "/leer/"));
        chapter.setPages(Integer.parseInt(getFirstMatchDefault("<select class=\"pageselector.+?>(\\d+)</option>[\\s]*</select>", data, "0")));
        chapter.setExtra(getFirstMatch("/c/(.+)", chapter.getPath(), "Error al iniciar cápitulo(imagenes)."));
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{new ServerFilter("Géneros", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Tipo", type, ServerFilter.FilterType.MULTI),
                new ServerFilter("Estado", state, ServerFilter.FilterType.MULTI)};
    }

    @Override
    public boolean hasList() {
        return false;
    }
}
