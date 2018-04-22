package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;

class MangaHere extends ServerBase {
    private static final String HOST = "http://www.mangahere.cc";

    private static final int[] fltType = {
            R.string.flt_tag_all,
            R.string.flt_tag_manga,
            R.string.flt_tag_manhwa,
    };
    private static final String[] valType = {
            "",
            "rl",
            "lr",
    };

    private static final int[] fltGenre = {
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
            R.string.flt_tag_yuri
    };
    private static final String[] valGenre = {
            "Action",
            "Adventure",
            "Comedy",
            "Doujinshi",
            "Drama",
            "Ecchi",
            "Fantasy",
            "Gender Bender",
            "Harem",
            "Historical",
            "Horror",
            "Josei",
            "Martial Arts",
            "Mature",
            "Mecha",
            "Mystery",
            "One Shot",
            "Psychological",
            "Romance",
            "School Life",
            "Sci-fi",
            "Seinen",
            "Shoujo",
            "Shoujo Ai",
            "Shounen",
            "Shounen Ai",
            "Slice of Life",
            "Sports",
            "Supernatural",
            "Tragedy",
            "Yaoi",
            "Yuri",
    };

    private static final int[] fltStatus = {
            R.string.flt_status_all,
            R.string.flt_status_ongoing,
            R.string.flt_status_completed,
    };
    private static final String[] valStatus = {
            "",
            "1",
            "0",
    };

    private static final String PATTERN_SERIE =
            "<li><a class=\"manga_info\" rel=\"([^\"]*)\" href=\"([^\"]*)\"><span>[^<]*</span>([^<]*)</a></li>";
    private static final String PATTERN_COVER =
            "<img src=\"(.+?cover.+?)\"";
    private static final String PATTERN_SUMMARY =
            "<p id=\"show\" style=\"display:none;\">(.+?)&nbsp;<a";
    private static final String PATTERN_AUTHOR =
            "<li><label>Author\\(s\\):</label>(.+?)</li>";
    private static final String PATTERN_FINISHED =
            "</label>Completed</li>";
    private static final String PATTERN_GENRE =
            "<li><label>Genre\\(s\\):</label>(.+?)</li>";
    private static final String PATTERN_IMAGE =
            "src=\"([^\"]+?/manga/.+?.(jpg|gif|jpeg|png|bmp).*?)\"";
    private static final String PATTERN_MANGA =
            "<img src=\"(.+?)\".+?alt=\"(.+?)\".+?<a href=\"(.+?)\"";
    private static final String PATTERN_MANGA_SEARCHED =
            "<dt>\\s+<a href=\"[^\"]+(/manga[^\"]+).+?>(.+?)<";

    MangaHere(Context context) {
        super(context);
        setFlag(R.drawable.flag_en);
        setIcon(R.drawable.mangahere_icon);
        setServerName("MangaHere");
        setServerID(MANGAHERE);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigatorAndFlushParameters().get(HOST + "/mangalist/");
        Pattern p = Pattern.compile(PATTERN_SERIE, Pattern.DOTALL);
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(1), Util.getInstance().getFilePath(m.group(2)), false));
        }
        return mangas;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String data = getNavigatorAndFlushParameters().get(HOST + manga.getPath());
            // Front
            manga.setImages(getFirstMatchDefault(PATTERN_COVER, data, context.getString(R.string.nodisponible)));
            // Summary
            manga.setSynopsis(getFirstMatchDefault(PATTERN_SUMMARY, data, context.getString(R.string.nodisponible)));
            // Status
            manga.setFinished(data.contains(PATTERN_FINISHED));
            // Author
            manga.setAuthor(getFirstMatchDefault(PATTERN_AUTHOR, data, context.getString(R.string.nodisponible)));
            assert manga.getAuthor() != null;
            if(manga.getAuthor().equals("Unknown")) {
                manga.setAuthor(context.getString(R.string.nodisponible));
            }
            // Genre
            manga.setGenre(getFirstMatchDefault(PATTERN_GENRE, data, context.getString(R.string.nodisponible)));
            assert manga.getGenre() != null;
            if(manga.getGenre().equals("None")) {
                manga.setGenre(context.getString(R.string.nodisponible));
            }
            // Chapter
            Pattern p = Pattern.compile("<li>\\s*<span class=\"left\">\\s*<a class=\"color_0077\"\\s+href=\"([^\"]+)\"\\s*>([^<]+)</a>\\s*<span\\s+class=\"mr6\">([^<]*)", Pattern.DOTALL);
            Matcher m = p.matcher(data);
            while (m.find()) {
                manga.addChapterFirst(new Chapter(m.group(2).trim() + " " + m.group(3).trim(), Util.getInstance().getFilePath(m.group(1))));
            }
        }
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        loadChapters(manga, forceReload);
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        assert chapter.getExtra() != null;
        String web = "http:" + chapter.getExtra().split("\\|")[page - 1];
        String data = getNavigatorAndFlushParameters().get(web);
        return getFirstMatch(
                PATTERN_IMAGE, data,
                context.getString(R.string.server_failed_loading_image));
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if((chapter.getPages() == 0) || (chapter.getExtra() == null)) {
            String path = HOST + chapter.getPath();
            String data = getNavigatorAndFlushParameters().get(path);
            String page_selection = getFirstMatchDefault(
                    "<select class=\"wid60\"[^>]+>(.+?)</select>", data, ""
            );
            if(page_selection.isEmpty()) {
                // if the Manga was licensed, page selector is not present
                if(data.contains("has been licensed. It's not available in MangaHere.")) {
                    throw new Exception(context.getString(R.string.server_manga_is_licensed));
                }
                else {
                    throw new Exception(context.getString(R.string.server_failed_loading_page_count));
                }
            }

            // only match numeric page indices, this automatically skips the 'featured' ad page
            ArrayList<String> page_links = getAllMatch("<option value=\"([^\"]+)\"[^>]*>\\d+</option>", page_selection);
            if (page_links.isEmpty()) {
                throw new Exception(context.getString(R.string.server_failed_loading_page_count));
            }

            chapter.setExtra(TextUtils.join("|", page_links));
            chapter.setPages(page_links.size());
        }
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[] {
                new ServerFilter(
                        context.getString(R.string.flt_type),
                        buildTranslatedStringArray(fltType), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_genre),
                        buildTranslatedStringArray(fltGenre), ServerFilter.FilterType.MULTI),
                new ServerFilter(
                        context.getString(R.string.flt_status),
                        buildTranslatedStringArray(fltStatus), ServerFilter.FilterType.SINGLE),
        };
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();

        if(filters[0][0] == 0 && filters[1].length <= 1 && filters[2][0] == 0) {
            // perform simple directory listing (faster)
            String web;
            if(filters[1].length == 0) {
                web = HOST + "/directory/" + pageNumber + ".htm";
            } else {
                web = HOST + "/" + valGenre[filters[1][0]].toLowerCase().replace(" ", "_") + "/" + pageNumber + ".htm";
            }

            String source = getNavigatorAndFlushParameters().get(web);
            Pattern p = Pattern.compile(PATTERN_MANGA, Pattern.DOTALL);
            Matcher m = p.matcher(source);
            while (m.find()) {
                Manga manga = new Manga(getServerID(), m.group(2), Util.getInstance().getFilePath(m.group(3)), false);
                manga.setImages(m.group(1));
                mangas.add(manga);
            }
        }
        else {
            // perform complex filtering
            StringBuilder sb = new StringBuilder();

            sb.append(HOST).append("/search.php?");
            sb.append("direction=").append(valType[filters[0][0]]).append("&");
            sb.append("name_method=cw&name=&author_method=cw&author=&artist_method=cw&artist=&");
            for (int i = 0; i < valGenre.length; i++) {
                sb.append("genres[").append(valGenre[i]).append("]=");
                boolean hasGenre = false;
                for (int j : filters[1]) {
                    if (j == i) {
                        hasGenre = true;
                        break;
                    }
                }
                sb.append(hasGenre ? "1&" : "0&");
            }
            sb.append("released_method=eq&released=&");
            sb.append("is_completed=").append(valStatus[filters[2][0]]).append("&");
            sb.append("advopts=1");
            if (pageNumber > 1) {
                sb.append("&page=").append(pageNumber);
            }

            String web = sb.toString();
            String source = getNavigatorAndFlushParameters().get(web);
            // MangaHere delivers the last page again for page numbers greater than the last page number
            // detect this case here and deliver an empty array in that case
            int lastPage = Integer.parseInt(getFirstMatchDefault("<a href=\"javascript:void\\(0\\);\" class=\"hover\">(\\d+)</a>", source, "0"));
            if (pageNumber <= lastPage) {
                Pattern p = Pattern.compile("href=\"([^\"]+)\" class=\"manga_info name_one\" rel=\"([^\"]+)", Pattern.DOTALL);
                Matcher m = p.matcher(source);
                while (m.find()) {
                    Manga manga = new Manga(getServerID(), m.group(2), Util.getInstance().getFilePath(m.group(1)), false);

                    // extract the cover URL from the popup via AJAX
                    Navigator nav = getNavigatorAndFlushParameters();
                    nav.addHeader("X-Requested-With", "XMLHttpRequest");
                    nav.addPost("name", m.group(2));
                    String data = nav.post(HOST + "/ajax/series.php");
                    try {
                        manga.setImages(new JSONArray(data).getString(1));
                    } catch (JSONException e) {
                        manga.setImages("");
                    }

                    mangas.add(manga);
                }
            }
        }

        return mangas;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String web = HOST + "/search.php?name=" + term.replace(" ", "+");
        String data = getNavigatorAndFlushParameters().get(web);

        if(data.contains("Sorry you have just searched, please try 5 seconds later.")) {
            TimeUnit.MILLISECONDS.sleep(5500);
            data = getNavigatorAndFlushParameters().get(web);
        }

        Pattern p = Pattern.compile(PATTERN_MANGA_SEARCHED, Pattern.DOTALL);
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(2), m.group(1), false));
        }
        return mangas;
    }
}
