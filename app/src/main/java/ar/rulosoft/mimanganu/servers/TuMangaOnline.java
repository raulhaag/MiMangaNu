package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.text.TextUtils;

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

/**
 * Created by Raul on 05/04/2016.
 */
class TuMangaOnline extends ServerBase {

    public static String[] type = new String[]{
            "Todos", "Manga", "Manhua", "Manhwa", "Novela", "One Shot", "Dounjinshi", "Oel"
    };
    private static String[] genres = new String[]{
            "Acción", "Aventura", "Comedia", "Drama", "Recuentos de la vida", "Ecchi", "Fantasia",
            "Magia", "Supernatural", "Horror", "Misterio", "Psicológico", "Romance",
            "Ciencia Ficción", "Thriller", "Deporte", "Girls Love", "Boys Love", "Harem", "Mecha",
            "Supervivencia", "Reencarnación", "Gore", "Apocalíptico", "Tragedia", "Vida Escolar",
            "Historia", "Militar", "Policiaco", "Crimen", "Superpoderes", "Vampiros",
            "Artes Marciales", "Samurái", "Género Bender", "Realidad Virtual", "Ciberpunk",
            "Musica", "Parodia", "Animación", "Demonios", "Familia", "Extranjero", "Niños",
            "Realidad", "Telenovela", "Guerra", "Oeste"
    };
    private static String[] genresValues = new String[]{
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16",
            "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
            "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44",
            "45", "46", "47", "48"
    };
    private static String[] demografia = {
            "Todos", "Seinen", "Shoujo", "Shounen", "Josei", "Kodomo"
    };
    private static String[] demografiaV = {
            "", "Seinen", "Shoujo", "Shounen", "Josei", "Kodomo"
    };
    private static String[] estado = {
            "Todos", "Activo", "Abandonado", "Finalizado", "Pausado"
    };
    private static String[] estadoV = {
            "", "publishing", "cancelled", "ended", "on_hold"
    };
    private static String[] typeV = new String[]{
            "", "manga", "manhua", "manhwa", "novel", "one_shot", "doujinshi", "oel"
    };

    private static String[] sortBy = new String[]{
            "Me gusta", "Alfabetico", "Puntuación", "Creación", "Fecha de Esterno",
    };

    private static String[] sortByValues = new String[]{
            "likes_count", "alphabetically", "score", "creation", "release_date"
    };

    private static String[] sortOrder = new String[]{
            "Descendiente", "Ascendiente"
    };

    private static String[] sortOrderValues = new String[]{
            "desc", "asc"
    };

    private static int lastPage = 10000;

    TuMangaOnline(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_es);
        this.setIcon(R.drawable.tumangaonline_icon);
        this.setServerName("TuMangaOnline");
        setServerID(ServerBase.TUMANGAONLINE);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        JSONObject jsonObject = new JSONObject(getNavWithNeededHeaders().get("http://www.tumangaonline.me/api/v1/mangas?categorias=%5B%5D&generos=%5B%5D&itemsPerPage=20&nameSearch=" + URLEncoder.encode(term, "UTF-8") + "&page=1&puntuacion=0&searchBy=nombre&sortDir=asc&sortedBy=nombre"));
        return null;//TODO getMangasJsonArray(jsonObject.getJSONArray("data"));
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        String data = getNavWithNeededHeaders().get(
                String.format("https://tumangaonline.me/library/manga/%s/%s", manga.getPath(),
                        URLEncoder.encode(manga.getTitle(), "UTF-8")));

        manga.setSynopsis(getFirstMatchDefault("<p class=\"element-description\">(.+?)</p>", data, context.getString(R.string.nodisponible)));
        manga.setGenre(TextUtils.join(", ", getAllMatch("genders\\[\\]=\\d+\">(.+)<", data)));
        manga.setAuthor(getFirstMatchDefault(">(.+?)</h5>\\n<p class=\"card-text\">Autor", data, context.getString(R.string.nodisponible)));

        Pattern pattern = Pattern.compile("<div class=\"col-10 text-truncate\">([\\s\\S]+?)</div>[\\s\\S]+?goto/(.+?)\"");
        Matcher matcher = pattern.matcher(data);

        while (matcher.find()) {
            manga.addChapterFirst(new Chapter(matcher.group(1).replaceAll("<[\\s\\S]+?>", ""), "_" + matcher.group(2) + "_"));
        }
    }


    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String[] d1 = chapter.getExtra().split("\\|");
        return d1[page];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if(chapter.getPages() == 0) {
            String data = getNavWithNeededHeaders().get("https://tumangaonline.me/goto/" + chapter.getPath().split("_")[1]);
            ArrayList<String> imgs = getAllMatch("src=\"(https://img.+?)\"", data);
            chapter.setPages(imgs.size());
            chapter.setExtra("|" + TextUtils.join("|", imgs));
        }
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public boolean needRefererForImages() {
        return true;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter("Tipo", type, ServerFilter.FilterType.SINGLE),//0
                new ServerFilter("Demografia", demografia, ServerFilter.FilterType.SINGLE),//1
                new ServerFilter("Generos", genres, ServerFilter.FilterType.MULTI),//2
                new ServerFilter("Estado", estado, ServerFilter.FilterType.SINGLE),//3
                new ServerFilter("Ordenado por", sortBy, ServerFilter.FilterType.SINGLE), //4
                new ServerFilter("En dirección", sortOrder, ServerFilter.FilterType.SINGLE) //5
        };
    }

    // ?order_item=     0
    // &order_dir=      1
    // &title=&filter_by=title
    // &type=           2
    // &demography=     3
    // &status=         4
    // &webcomic=&yonkoma=&amateur=
    // &genders%5B%5D=1&genders%5B%5D=3&genders%5B%5D=6

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {

        String web = "https://tumangaonline.me/library?order_item=%s&order_dir=%s&title=&filter_by=title&type=%s&demography=%s&status=%s&webcomic=&yonkoma=&amateur=";


        if (pageNumber == 1)
            lastPage = 10000;
        if (pageNumber <= lastPage) {
            String gens = "";
            for (int i = 0; i < filters[2].length; i++) {
                gens = gens + "&genders%5B%5D=" + genresValues[filters[2][i]];
            }

            web = String.format(web, sortByValues[filters[4][0]], sortOrderValues[filters[5][0]],
                    typeV[filters[0][0]], demografiaV[filters[1][0]], estadoV[filters[3][0]]) + gens;
            String data = getNavWithNeededHeaders().get(web);
            return getMangasLibrary(data);
        } else {
            return new ArrayList<>();
        }
    }

    private ArrayList<Manga> getMangasLibrary(String data) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern pattern = Pattern.compile("https:\\/\\/tumangaonline.me\\/library\\/\\w+\\/(\\d+)\\/[\\s\\S]+?background-image: url\\('(.+?)'\\)[\\s\\S]+?title=\"(.+)\"");
        Matcher m = pattern.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(3), m.group(1), false);
            manga.setImages(m.group(2));
            mangas.add(manga);
        }
        return mangas;
    }

    private Navigator getNavWithNeededHeaders() {
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addHeader("Cache-mode", "no-cache");
        nav.addHeader("Referer", "https://tumangaonline.me/library/manga/");
        return nav;
    }
}
