package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.navegadores.Navigator;

public class DesuMe extends ServerBase {
    private static final String HOST = "http://desu.me";

    // kinds=
    private static final int[] fltType = {
            R.string.flt_tag_manga, // Манга
            R.string.flt_tag_manhwa, // Манхва
            R.string.flt_tag_manhua, // Маньхуа
            R.string.flt_tag_one_shot, // Ваншот
            R.string.flt_tag_comic, // Комикс
    };
    private static final String[] valType = {
            "manga",
            "manhwa",
            "manhua",
            "one_shot",
            "comics",
    };

    // genres=
    private static final int[] fltGenre = {
            R.string.flt_tag_madness, // Безумие
            R.string.flt_tag_martial_arts, // Боевые искусства
            R.string.flt_tag_vampire, // Вампиры
            R.string.flt_tag_military, // Военное
            R.string.flt_tag_harem, // Гарем
            R.string.flt_tag_daemons, // Демоны
            R.string.flt_tag_mystery, // Детектив
            R.string.flt_tag_kodomo, // Детское
            R.string.flt_tag_josei, // Дзёсей
            R.string.flt_tag_doujinshi, // Додзинси
            R.string.flt_tag_drama, // Драма
            R.string.flt_tag_game, // Игры
            R.string.flt_tag_historical, // Исторический
            R.string.flt_tag_comedy, // Комедия
            R.string.flt_tag_outer_space, // Космос
            R.string.flt_tag_magic, // Магия
            R.string.flt_tag_automobiles, // Машины
            R.string.flt_tag_mecha, // Меха
            R.string.flt_tag_music, // Музыка
            R.string.flt_tag_parody, // Пародия
            R.string.flt_tag_slice_of_life, // Повседневность
            R.string.flt_tag_police, // Полиция
            R.string.flt_tag_adventure, // Приключения
            R.string.flt_tag_psychological, // Психологическое
            R.string.flt_tag_romance, // Романтика
            R.string.flt_tag_samurai, // Самураи
            R.string.flt_tag_supernatural, // Сверхъестественное
            R.string.flt_tag_shoujo, // Сёдзе
            R.string.flt_tag_shoujo_ai, // Сёдзе Ай
            R.string.flt_tag_seinen, // Сейнен
            R.string.flt_tag_shounen, // Сёнен
            R.string.flt_tag_shounen_ai, // Сёнен Ай
            R.string.flt_tag_gender_bender, // Смена пола
            R.string.flt_tag_sports, // Спорт
            R.string.flt_tag_super_powers, // Супер сила
            R.string.flt_tag_thriller, // Триллер
            R.string.flt_tag_horror, // Ужасы
            R.string.flt_tag_sci_fi, // Фантастика
            R.string.flt_tag_fantasy, // Фэнтези
            R.string.flt_tag_hentai, // Хентай
            R.string.flt_tag_school_life, // Школа
            R.string.flt_tag_action, // Экшен
            R.string.flt_tag_ecchi, // Этти
            R.string.flt_tag_yuri, // Юри
            R.string.flt_tag_yaoi, // Яой
    };
    private static final String[] valGenre = {
            "Dementia",
            "Martial Arts",
            "Vampire",
            "Military",
            "Harem",
            "Demons",
            "Mystery",
            "Kids",
            "Josei",
            "Doujinshi",
            "Drama",
            "Game",
            "Historical",
            "Comedy",
            "Space",
            "Magic",
            "Cars",
            "Mecha",
            "Music",
            "Parody",
            "Slice of Life",
            "Police",
            "Adventure",
            "Psychological",
            "Romance",
            "Samurai",
            "Supernatural",
            "Shoujo",
            "Shoujo Ai",
            "Seinen",
            "Shounen",
            "Shounen Ai",
            "Gender Bender",
            "Sports",
            "Super Power",
            "Thriller",
            "Horror",
            "Sci-Fi",
            "Fantasy",
            "Hentai",
            "School",
            "Action",
            "Ecchi",
            "Yuri",
            "Yaoi",
    };

    // order_by=
    private static int[] fltOrder = {
            R.string.flt_order_last_update,
            R.string.flt_order_alpha,
            R.string.flt_order_views,
    };
    private static String[] valOrder = {
            "",
            "name",
            "popular",
    };

    DesuMe(Context context) {
        super(context);
        setFlag(R.drawable.flag_ru);
        setIcon(R.drawable.desume);
        setServerName("DesuMe");
        setServerID(DESUME);
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
    public boolean hasFilteredNavigation() {
        return true;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(
                        context.getString(R.string.flt_type),
                        buildTranslatedStringArray(fltType), ServerFilter.FilterType.MULTI),
                new ServerFilter(
                        context.getString(R.string.flt_genre),
                        buildTranslatedStringArray(fltGenre), ServerFilter.FilterType.MULTI),
                new ServerFilter(
                        context.getString(R.string.flt_order),
                        buildTranslatedStringArray(fltOrder), ServerFilter.FilterType.SINGLE)
        };
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        StringBuilder sb = new StringBuilder();

        sb.append(HOST + "/manga/?");

        if (filters[0].length > 0) {
            sb.append("kinds=");
            for (int i = 0; i < filters[0].length; i++) {
                sb.append(valType[filters[0][i]]).append(",");
            }
            sb.setLength(sb.length() - 1);
        }

        if (filters[1].length > 0) {
            sb.append("&genres=");
            for (int i = 0; i < filters[1].length; i++) {
                sb.append(valGenre[filters[1][i]]).append(",");
            }
            sb.setLength(sb.length() - 1);
        }

        if (filters[2][0] != 0) {
            sb.append("&order_by=").append(valOrder[filters[2][0]]);
        }

        if (pageNumber > 1) {
            sb.append("&page=").append(pageNumber);
        }

        String source = getNavigatorAndFlushParameters().get(sb.toString());
        Pattern pattern = Pattern.compile("memberListItem\">\\s*<a href=\"(manga/[^\"]+).+?url\\('([^']+).+?title=[^>]+>([^<]+)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            mangas.add(new Manga(getServerID(), matcher.group(3), "/" + matcher.group(1), HOST + matcher.group(2)));
        }
        return mangas;
    }

    @Override
    public boolean hasSearch() {
        return true;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addHeader("x-requested-with", "XMLHttpRequest");
        nav.addPost("q", term);
        nav.addPost("_xfResponseType", "json");
        String data = nav.post(HOST + "/manga/find");

        try {
            JSONObject result = new JSONObject(data).getJSONObject("results");
            Iterator<String> it = result.keys();
            while (it.hasNext()) {
                String title = it.next();
                String id = getFirstMatch(
                        "/(\\d+)\\.jpg",
                        result.getJSONObject(title).getString("avatar"),
                        context.getString(R.string.server_failed_locate_manga_url));
                String web = "/manga/" + title.replace(" ", "-").toLowerCase() + "." + id + "/";
                mangas.add(new Manga(getServerID(), title, web, false));
            }
        } catch (JSONException e) {
            // do nothing
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

            // cover image
            manga.setImages(HOST + getFirstMatchDefault("src=\"(/data/manga/covers/[^\"]+)", data, ""));
            // summary
            manga.setSynopsis(getFirstMatchDefault("class=\"prgrph\">([^<]+)", data,
                    context.getString(R.string.nodisponible)));
            // ongoing or completed
            manga.setFinished(!data.contains("b-anime_status_tag ongoing"));
            // author - n/a
            manga.setAuthor(context.getString(R.string.nodisponible));
            // genre
            manga.setGenre(getFirstMatchDefault("(<a class=\"tag Tooltip\".+?)</ul>", data, context.getString(R.string.nodisponible)).replace("</li> <li>", ", "));
            // chapter
            Pattern p = Pattern.compile("<h4><a href=\"(/manga/[^\"]+).+?title=\"([^\"]+)", Pattern.DOTALL);
            Matcher m = p.matcher(data);
            while (m.find()) {
                // remove 'volume' to ease sorting after chapters
                String title = m.group(2).replaceFirst("^Том \\d+.\\s+", "");
                manga.addChapterFirst(new Chapter(title, m.group(1)));
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        assert chapter.getExtra() != null;
        String img = chapter.getExtra().split("\\|")[page - 1];
        return (img.startsWith("//") ? "http:" + img : img);
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getPages() == 0) {
            String data = getNavigatorAndFlushParameters().get(HOST + chapter.getPath());

            String imageDir = getFirstMatch("\\s*dir:\\s*\"([^\"]+)", data, context.getString(R.string.server_failed_loading_chapter));
            imageDir = imageDir.replace("\\", "");
            String images = getFirstMatch("\\s*images:\\s*\\[\\[(.+?)\\]\\]", data, context.getString(R.string.server_failed_loading_chapter));
            ArrayList<String> pages = getAllMatch("\"([^\"]+)\"", images);

            if (pages.size() > 0) {
                StringBuilder extra = new StringBuilder();
                for (String p : pages) {
                    extra.append(imageDir).append(p).append("|");
                }
                extra.setLength(extra.length() - 1);

                chapter.setPages(pages.size());
                chapter.setExtra(extra.toString());
            } else {
                throw new Exception(context.getString(R.string.server_failed_loading_page_count));
            }
        }
    }
}
