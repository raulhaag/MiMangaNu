package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.navegadores.Navigator;

/**
 * Created by Raúl on 04/01/2018.
 */

public class ReadMangaMe extends ServerBase {

    private String HOST = "https://readmanga.me";

    private static int[] genres = new int[]{
            R.string.flt_tag_all, //ap
            R.string.flt_tag_color,
            R.string.flt_tag_webtoon, //"Веб",
            R.string.flt_status_abandoned,// "Выпуск приостановлен",
            R.string.flt_tag_4_koma, //"Ёнкома",
            R.string.flt_tag_comic, //"Комикс западный",
            R.string.flt_tag_manhwa, //"Манхва",
            R.string.flt_tag_manhua, //"Маньхуа",
            R.string.flt_tag_light_novel, //"Ранобэ",
            R.string.flt_tag_collection,//"Сборник",
            R.string.flt_tag_art, //"арт",
            R.string.flt_tag_action, //"боевик",
            R.string.flt_tag_martial_arts, //"боевые искусства",
            R.string.flt_tag_vampire, //"вампиры",
            R.string.flt_tag_harem, //"гарем",
            R.string.flt_tag_gender_bender, // "гендерная интрига",
            R.string.flt_tag_super_hero, //"героическое фэнтези",
            R.string.flt_tag_detective, //"детектив",
            R.string.flt_tag_josei,//"дзёсэй",
            R.string.flt_tag_doujinshi, //"додзинси",
            R.string.flt_tag_drama, //"драма",
            R.string.flt_tag_game, //"игра",
            R.string.flt_tag_historical, //"история",
            R.string.flt_tag_cyberpunk, //"киберпанк",
            R.string.flt_tag_kodomo, //"кодомо",
            R.string.flt_tag_comedy, //"комедия",
            R.string.flt_tag_maho_shoujo, //"махо-сёдзё",
            R.string.flt_tag_mecha, //"меха",
            R.string.flt_tag_mystery, //"мистика",
            R.string.flt_tag_sci_fi, //"научная фантастика",
            R.string.flt_tag_daily_life, //"повседневность",
            R.string.flt_tag_post_apocalyptic, //"постапокалиптика",
            R.string.flt_tag_adventure, //"приключения",
            R.string.flt_tag_psychological, //"психология",
            R.string.flt_tag_romance, //"романтика",
            R.string.flt_tag_samurai, //"самурайский боевик",
            R.string.flt_tag_supernatural, //"сверхъестественное",
            R.string.flt_tag_shoujo, //"сёдзё",
            R.string.flt_tag_shoujo_ai, //"сёдзё-ай",
            R.string.flt_tag_shounen, //"сёнэн",
            R.string.flt_tag_shounen_ai, //"сёнэн-ай",
            R.string.flt_tag_sports, //"спорт",
            R.string.flt_tag_seinen, //"сэйнэн",
            R.string.flt_tag_tragedy, //"трагедия",
            R.string.flt_tag_thriller, //"триллер",
            R.string.flt_tag_horror, //"ужасы",
            R.string.flt_tag_fantastic,
            R.string.flt_tag_fantasy, //"фэнтези",
            R.string.flt_tag_school_life, //"школа",
            R.string.flt_tag_ecchi, //"этти",
            R.string.flt_tag_yuri,//"юри"
    };
    private static String[] genresV = new String[]{
            "/list", "/list/tag/color", "/list/tag/web", "/list/tag/stopped", "/list/tag/yonkoma",
            "/list/tag/comix", "/list/tag/manhwa", "/list/tag/manhua", "/list/tag/light_novel",
            "/list/tag/sbornik", "/list/genre/art", "/list/genre/action", "/list/genre/martial_arts",
            "/list/genre/vampires", "/list/genre/harem", "/list/genre/gender_intriga",
            "/list/genre/heroic_fantasy", "/list/genre/detective", "/list/genre/josei",
            "/list/genre/doujinshi", "/list/genre/drama", "/list/genre/game",
            "/list/genre/historical", "/list/genre/cyberpunk", "/list/genre/codomo",
            "/list/genre/comedy", "/list/genre/maho_shoujo", "/list/genre/mecha",
            "/list/genre/mystery", "/list/genre/sci_fi", "/list/genre/natural",
            "/list/genre/postapocalypse", "/list/genre/adventure", "/list/genre/psychological",
            "/list/genre/romance", "/list/genre/samurai", "/list/genre/supernatural",
            "/list/genre/shoujo", "/list/genre/shoujo_ai", "/list/genre/shounen",
            "/list/genre/shounen_ai", "/list/genre/sports", "/list/genre/seinen",
            "/list/genre/tragedy", "/list/genre/thriller", "/list/genre/horror",
            "/list/genre/fantastic", "/list/genre/fantasy", "/list/genre/school",
            "/list/genre/ecchi", "/list/genre/yuri"
    };

    private static int[] sort = new int[]{
            R.string.flt_order_rating,//"По популярности"
            R.string.flt_order_votes,
            R.string.flt_order_created,
            R.string.flt_order_last_update //"По дате обновления",
    };

    private static String[] sortV = new String[]{
            "sortType=rate", "sortType=votes", "sortType=created", "sortType=updated"
    };

    /**
     * Construct a new ServerBase object.
     *
     * @param context the context for this object
     */
    ReadMangaMe(Context context) {
        super(context);
        setFlag(R.drawable.flag_ru);
        setIcon(R.drawable.readmangame);
        setServerName("ReadMangaMe");
        setServerID(READMANGAME);
    }

    @Nullable
    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addPost("q", term);
        nav.addHeader("Referer", HOST);
        String data = nav.post(HOST + "/search");
        return getMangasFromSource(data);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String source = getNavigatorAndFlushParameters().get(HOST + manga.getPath());

            // Cover
            manga.setImages(getFirstMatchDefault("<img class=.+?src=\"(http[^\"]+)\"", source, ""));

            // Summary
            String summary = getFirstMatchDefault("<div class=\"manga-description\" itemprop=\"description\">(.+?)<div class=\"clearfix\"></div>", source, context.getString(R.string.nodisponible));
            manga.setSynopsis(summary);

            // summary can be empty after cleaning, so check again here
            assert (manga.getSynopsis() != null);
            if (manga.getSynopsis().isEmpty()) {
                manga.setSynopsis(context.getString(R.string.nodisponible));
            }

            // Status
            manga.setFinished(getFirstMatchDefault("<b>Перевод:</b>([^<]+)", source, "").contains("завершен"));

            // Author (can be multi-part)
            ArrayList<String> authors = getAllMatch(
                    "elem_(author|screenwriter|illustrator).+?person-link\">(.+?)<", source);

            if (authors.isEmpty()) {
                manga.setAuthor(context.getString(R.string.nodisponible));
            } else {
                manga.setAuthor(TextUtils.join(", ", authors));
            }

            // Genre
            ArrayList<String> genres = getAllMatch(
                    "elem_genre.+?element-link\">(.+?)<", source);

            if (genres.isEmpty()) {
                manga.setGenre(context.getString(R.string.nodisponible));
            } else {
                manga.setGenre(TextUtils.join(", ", genres));
            }

            // Chapters
            Pattern p = Pattern.compile("<td class=\"[\\s\\S]+?\">\\s+<a href=\"([^\"]+)\"[^>]+?>\\s+([\\s\\S]+?)<", Pattern.DOTALL);
            Matcher matcher = p.matcher(source);
            while (matcher.find()) {
                manga.addChapterFirst(new Chapter(matcher.group(2).replaceAll("\\s+", " ").trim(), matcher.group(1)));
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        assert (chapter.getExtra() != null);
        return chapter.getExtra().split("\\|")[page];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data = getNavigatorAndFlushParameters().get(HOST + chapter.getPath() + "?mtr=1");
        data = getFirstMatch("rm_h.init\\(([\\s\\S]+?)\\)", data, context.getString(R.string.server_failed_loading_page_count));

        Pattern pattern = Pattern.compile("\\[['|\"](.+?)['|\"],['|\"](.+?)['|\"],['|\"](.+?)['|\"]");
        Matcher m = pattern.matcher(data);

        StringBuilder sb = new StringBuilder();
        int pages = 0;
        while (m.find()) {
            sb.append("|").append(m.group(1)).append(m.group(3));
            pages++;
        }
        if (pages > 0) {
            chapter.setExtra(sb.toString());
            chapter.setPages(pages);
        } else {
            throw new Exception(context.getString(R.string.server_failed_loading_page_count));
        }
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = HOST + genresV[filters[0][0]] + "?" + sortV[filters[1][0]];
        if (pageNumber > 1) {
            web = web + "&offset=" + (pageNumber * 70) + "&max=70";
        }
        String data = getNavigatorAndFlushParameters().get(web);
        return getMangasFromSource(data);
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern pattern = Pattern.compile("sm-6 \">[\\s\\S]+?href=\"(.+?)\"[\\s\\S]+?original='(.+?)' title='(.+?)'", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            mangas.add(new Manga(getServerID(), matcher.group(3), matcher.group(1), matcher.group(2)));
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(context.getString(R.string.flt_category) + " / " + context.getString(R.string.flt_genre), buildTranslatedStringArray(genres), ServerFilter.FilterType.SINGLE),//0
                new ServerFilter(context.getString(R.string.flt_order), buildTranslatedStringArray(sort), ServerFilter.FilterType.SINGLE),//1 "Сортировать"
        };
    }

    @Override
    public boolean hasList() {
        return false;
    }

    void setHost(String host) {
        HOST = host;
    }
}
