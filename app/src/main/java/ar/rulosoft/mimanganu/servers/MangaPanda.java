package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;

class MangaPanda extends ServerBase {
    private String HOST = "http://www.mangapanda.com";

    private static final String PATTERN_SERIE =
            "<li><a href=\"([^\"]+)\">([^<]+)";
    private static final String PATTERN_SUB =
            "<div class=\"series_col\">([\\s\\S]+?)<div id=\"adfooter\">";
    private static final String PATTERN_FRAG_CHAPTER =
            "<div id=\"chapterlist\">([\\s\\S]+?)</table>";
    private static final String PATTERN_CHAPTER =
            "<a href=\"([^\"]+)\">([^\"]+?)</a>.:([^\"]+?)</td>";
    private static final String PATTERN_CHAPTER_WEB =
            "/[-|\\d]+/([^/]+)/chapter-(\\d+).html";

    private static int[] fltGenre = {
            R.string.flt_tag_action,
            R.string.flt_tag_adventure,
            R.string.flt_tag_comedy,
            R.string.flt_tag_daemons,
            R.string.flt_tag_drama,
            R.string.flt_tag_ecchi,
            R.string.flt_tag_fantasy,
            R.string.flt_tag_gender_bender,
            R.string.flt_tag_harem,
            R.string.flt_tag_historical,
            R.string.flt_tag_horror,
            R.string.flt_tag_josei,
            R.string.flt_tag_magic,
            R.string.flt_tag_martial_arts,
            R.string.flt_tag_mature,
            R.string.flt_tag_mecha,
            R.string.flt_tag_military,
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
            R.string.flt_tag_smut,
            R.string.flt_tag_sports,
            R.string.flt_tag_super_powers,
            R.string.flt_tag_supernatural,
            R.string.flt_tag_tragedy,
            R.string.flt_tag_vampire,
            R.string.flt_tag_yaoi,
            R.string.flt_tag_yuri
    };

    private static final int[] fltType = {
            R.string.flt_tag_all,
            R.string.flt_tag_manga,
            R.string.flt_tag_manhwa,
    };
    private static final String[] valType = {
            "&rd=0",
            "&rd=2",
            "&rd=1"
    };

    private static final int[] fltStatus = {
            R.string.flt_status_all,
            R.string.flt_status_ongoing,
            R.string.flt_status_completed
    };
    private static final String[] valStatus = {
            "&status=",
            "&status=1",
            "&status=2"
    };

    private static final int[] fltOrder = {
            R.string.flt_order_views,
            R.string.flt_order_alpha,
            R.string.flt_order_similar
    };
    private static final String[] valOrder = {
            "&order=2",
            "&order=1",
            "&order="
    };

    MangaPanda(Context context) {
        super(context);
        setFlag(R.drawable.flag_en);
        setIcon(R.drawable.mangapanda_icon);
        setServerName("mangapanda");
        setServerID(MANGAPANDA);
    }

    void setHost(String host) {
        HOST = host;
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigatorAndFlushParameters().get(HOST + "/alphabetical");
        Pattern p = Pattern.compile(PATTERN_SUB, Pattern.DOTALL);
        Matcher m = p.matcher(data);
        if (m.find()) {
            String b = m.group(1);
            Pattern p1 = Pattern.compile(PATTERN_SERIE, Pattern.DOTALL);
            Matcher m1 = p1.matcher(b);
            while (m1.find()) {
                mangas.add(new Manga(this.getServerID(), m1.group(2), HOST + m1.group(1), false));
            }
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigatorAndFlushParameters().get(HOST + "/actions/search/?q=" + term + "&limit=100");
        Pattern p = Pattern.compile("(.+?)\\|.+?\\|(/.+?)\\|\\d+", Pattern.DOTALL);
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(1), HOST + m.group(2), false));
        }
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
            String data = getNavigatorAndFlushParameters().get(manga.getPath());
            Pattern p = Pattern.compile(PATTERN_FRAG_CHAPTER, Pattern.DOTALL);
            Matcher m = p.matcher(data);
            if (m.find()) {
                manga.getChapters().clear();
                Pattern p1 = Pattern.compile(PATTERN_CHAPTER, Pattern.DOTALL);
                Matcher m1 = p1.matcher(m.group(1));
                while (m1.find()) {
                    String web = m1.group(1);
                    if (web.matches(PATTERN_CHAPTER_WEB)) {
                        Pattern p2 = Pattern.compile(PATTERN_CHAPTER_WEB, Pattern.DOTALL);
                        Matcher m2 = p2.matcher(web);
                        if (m2.find()) web = m2.group(1) + "/" + m2.group(2);
                    }
                    String chName = m1.group(2);
                    if (!m1.group(3).trim().isEmpty()) {
                        chName += " :" + m1.group(3);
                    }
                    manga.addChapterLast(new Chapter(chName, HOST + web));
                }
            }
            // Summary
            manga.setSynopsis(getFirstMatchDefault("<p>(.+)</p>", data, context.getString(R.string.nodisponible)));
            // Cover
            manga.setImages(getFirstMatchDefault("mangaimg\"><img src=\"([^\"]+)", data, ""));
            // Status
            manga.setFinished(data.contains("<td>Completed</td>"));
            // Genre
            manga.setGenre(getFirstMatchDefault("Genre:</td>[^<]*<td>(.+?)</td>", data, context.getString(R.string.nodisponible)).replace("a> <a", "a>, <a"));
            assert manga.getGenre() != null;
            if (manga.getGenre().isEmpty()) {
                manga.setGenre(context.getString(R.string.nodisponible));
            }
            // Author
            manga.setAuthor(getFirstMatchDefault("Author:</td>[^<]*<td>([^<]+)", data, context.getString(R.string.nodisponible)));
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String data = getNavigatorAndFlushParameters().get(chapter.getPath() + "/" + page);
        return getFirstMatch(
                "src=\"([^\"]+?.(jpg|gif|jpeg|png|bmp))", data,
                context.getString(R.string.server_failed_loading_image));
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getPages() == 0) {
            String data = getNavigatorAndFlushParameters().get(chapter.getPath());
            String pages = getFirstMatch(
                    "of (\\d+)</div>", data,
                    context.getString(R.string.server_failed_loading_page_count));
            chapter.setPages(Integer.parseInt(pages));
        }
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(
                        context.getString(R.string.flt_genre),
                        buildTranslatedStringArray(fltGenre), ServerFilter.FilterType.MULTI_STATES),
                new ServerFilter(
                        context.getString(R.string.flt_type),
                        buildTranslatedStringArray(fltType), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_status),
                        buildTranslatedStringArray(fltStatus), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_order),
                        buildTranslatedStringArray(fltOrder), ServerFilter.FilterType.SINGLE),
        };
    }

    ///search/?w=&rd=0&status=0&order=0&genre=1000010000000000000000000000000000000&p=0

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String gens = "";
        for (int i = 0; i < filters[0].length; i++) {
            if (filters[0][i] == -1) {
                gens = gens + "2";
            } else {
                gens = gens + filters[0][i];
            }
        }
        ArrayList<Manga> mangas = new ArrayList<>();
        String web = HOST + "/search/?w=" + valType[filters[1][0]] + valStatus[filters[2][0]] + valOrder[filters[3][0]] + "&genre=" + gens + "&p=" + ((pageNumber - 1) * 30);
        String data = getNavigatorAndFlushParameters().get(web);
        Pattern p = Pattern.compile("(https:[^']+/cover/[^']+).+?<h3><a href=\"([^\"]+)\">([^<]+)", Pattern.DOTALL);
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(3), HOST + m.group(2), m.group(1).replace("r0", "l0")));
        }
        return mangas;
    }
}
