package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

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

    private static String HOST = "http://readmanga.me";

    private static String[] genres = new String[]{
            "Все", "В цвете", "Веб", "Выпуск приостановлен", "Ёнкома", "Комикс западный", "Манхва",
            "Маньхуа", "Ранобэ", "Сборник", "арт", "боевик", "боевые искусства", "вампиры", "гарем",
            "гендерная интрига", "героическое фэнтези", "детектив", "дзёсэй", "додзинси", "драма",
            "игра", "история", "киберпанк", "кодомо", "комедия", "махо-сёдзё", "меха", "мистика",
            "научная фантастика", "повседневность", "постапокалиптика", "приключения", "психология",
            "романтика", "самурайский боевик", "сверхъестественное", "сёдзё", "сёдзё-ай", "сёнэн",
            "сёнэн-ай", "спорт", "сэйнэн", "трагедия", "триллер", "ужасы", "фантастика", "фэнтези",
            "школа", "этти", "юри"
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

    private static String[] sort = new String[]{
            "По популярности","По рейтингу","Новинки","По дате обновления",
    };

    private static String[] sortV = new String[]{
            "sortType=rate","sortType=votes","sortType=created","sortType=updated"
    };

    /**
     * Construct a new ServerBase object.
     *
     * @param context the context for this object
     */
    public ReadMangaMe(Context context) {
        super(context);
        setFlag(R.drawable.flag_ru);
        setIcon(R.drawable.noimage);
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
            String summary = getFirstMatchDefault("<p><span style=\"text-align: start; text-indent: 0px;\">(.+?)</span></p>", source, context.getString(R.string.nodisponible));
            manga.setSynopsis(summary);
            // summary can be empty after cleaning, so check again here
            if(manga.getSynopsis().isEmpty()) {
                manga.setSynopsis(context.getString(R.string.nodisponible));
            }

            // Status
           // manga.setFinished(source.contains(""));

            // Author (can be multi-part)
            ArrayList<String> authors = getAllMatch(
                    "elem_author.+?person-link\">(.+?)<", source);

            if(authors.isEmpty()) {
                manga.setAuthor(context.getString(R.string.nodisponible));
            }
            else {
                manga.setAuthor(TextUtils.join(", ", authors));
            }

            // Genre
            ArrayList<String> genres = getAllMatch(
                    "elem_genre.+?element-link\">(.+?)<", source);

            if(genres.isEmpty()) {
                manga.setGenre(context.getString(R.string.nodisponible));
            }
            else {
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
        return null;
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data = getNavigatorAndFlushParameters().get(HOST + chapter.getPath());
        data = getFirstMatch("rm_h.init\\(([\\s\\S]+?)\\)", data, context.getString(R.string.server_failed_loading_page_count));
        Pattern pattern = Pattern.compile("\\[['|\"][\\s\\S]*?,['|\"](.+?)['|\"],['|\"](.+?)['|\"]");
        Matcher m = pattern.matcher(data);
        String images =  "";
        while (m.find()){
            images = images + "|" + m.group(1) + m.group(2);
        }
        int pages = images.split("\\|").length - 1;
        if(pages > 0){
            chapter.setExtra(images);
            chapter.setPages(pages);
        }else{
            throw new Exception(context.getString(R.string.server_failed_loading_page_count));
        }
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = HOST + genresV[filters[0][0]] + "?" + sortV[filters[1][0]];
        if(pageNumber > 1){
            web = web + "&offset=" + (pageNumber * 70) + "&max=70";
        }
        String data = getNavigatorAndFlushParameters().get(web);
        return getMangasFromSource(data);
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern pattern = Pattern.compile("sm-6 \">[^/]*href=\"([^\"]+)[^/]*=\"([^\"]+)\" title=\"([^\"]+)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            Manga manga = new Manga(getServerID(), matcher.group(3), matcher.group(1), false);
            manga.setImages(matcher.group(2));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter("Категории / Жанры", genres, ServerFilter.FilterType.SINGLE),//0
                new ServerFilter("Сортировать", sort, ServerFilter.FilterType.SINGLE),//1
        };
    }

    @Override
    public boolean hasList() {
        return false;
    }
}
