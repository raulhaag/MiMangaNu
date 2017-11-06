package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.text.TextUtils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;

class MangaHere extends ServerBase {
    private static final String HOST = "https://www.mangahere.co";

    private static final int[] fltGenre = {
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
            R.string.flt_tag_slice_of_life,
            R.string.flt_tag_sports,
            R.string.flt_tag_supernatural,
            R.string.flt_tag_tragedy,
            R.string.flt_tag_yuri
    };
    private static final String[] valGenre = {
            "directory",
            "action",
            "adventure",
            "comedy",
            "doujinshi",
            "Drama",
            "ecchi",
            "fantasy",
            "gender_bender",
            "harem",
            "historical",
            "horror",
            "josei",
            "martial_arts",
            "mature",
            "mecha",
            "mystery",
            "one_shot",
            "psychological",
            "romance",
            "school_life",
            "sci-fi",
            "seinen",
            "shoujo",
            "shoujo Ai",
            "shounen",
            "slice_of_life",
            "sports",
            "supernatural",
            "tragedy",
            "yuri"
    };
    private static final String PATTERN_SERIE =
            "<li><a class=\"manga_info\" rel=\"([^\"]*)\" href=\"([^\"]*)\"><span>[^<]*</span>([^<]*)</a></li>";
    private static final String PATTERN_COVER =
            "<img src=\"(.+?cover.+?)\"";
    private static final String PATTERN_SUMMARY =
            "<p id=\"show\" style=\"display:none;\">(.+?)&nbsp;<a";
    private static final String PATTERN_CHAPTERS =
            "<li>[^<]*<span class=\"left\">[^<]*<a class=\"color_0077\" href=\"([^\"]*)\"[^>]*>([^<]*)</a>";
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
            "<dt>\\s+<a href=\"([^\"]+/manga[^\"]+).+?>(.+?)<";

    private static int[] fltOrder = {
            R.string.flt_order_views,
            R.string.flt_order_alpha,
            R.string.flt_order_rating,
            R.string.flt_order_last_update
    };
    private static String[] orderM = {
            "?views.za",
            "?name.az",
            "?rating.za",
            "?last_chapter_time.az"
    };

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
        String path;
        while (m.find()) {
            if(!m.group(2).startsWith("http")) {
                path = "https:" + m.group(2);
            }
            else {
                path = m.group(2);
            }
            mangas.add(new Manga(getServerID(), m.group(1), path, false));
        }
        return mangas;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String data = getNavigatorAndFlushParameters().get(manga.getPath());
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
            Pattern p = Pattern.compile(PATTERN_CHAPTERS, Pattern.DOTALL);
            Matcher m = p.matcher(data);
            while (m.find()) {
                manga.addChapterFirst(new Chapter(m.group(2), "http:" + m.group(1)));
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
            String data = getNavigatorAndFlushParameters().get(chapter.getPath());
            String page_selection = getFirstMatch(
                    "<select class=\"wid60\"[^>]+>(.+?)</select>", data,
                    context.getString(R.string.server_failed_loading_page_count)
            );

            // only match numeric page indices, this automatically skips the 'featured' ad page
            ArrayList<String> page_links = getAllMatch("<option value=\"([^\"]+)\"[^>]+>\\d+</option>", page_selection);
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
                        context.getString(R.string.flt_genre),
                        buildTranslatedStringArray(fltGenre), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_order),
                        buildTranslatedStringArray(fltOrder), ServerFilter.FilterType.SINGLE)
        };
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String web = HOST + "/" + valGenre[filters[0][0]] + "/" + pageNumber + ".htm" + orderM[filters[1][0]];
        String source = getNavigatorAndFlushParameters().get(web);
        Pattern p = Pattern.compile(PATTERN_MANGA, Pattern.DOTALL);
        Matcher m = p.matcher(source);
        String path;
        while (m.find()) {
            if(!m.group(3).contains("http")) {
                path = "http:" + m.group(3);
            }
            else {
                path = m.group(3);
            }
            Manga manga = new Manga(getServerID(), m.group(2), path, false);
            manga.setImages(m.group(1));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigatorAndFlushParameters().get(HOST + "/search.php?name=" + URLEncoder.encode(term, "UTF-8"));
        Pattern p = Pattern.compile(PATTERN_MANGA_SEARCHED, Pattern.DOTALL);
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(2), "http:" + m.group(1), false));
        }
        return mangas;
    }
}
