package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.support.annotation.NonNull;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.HtmlUnescape;
import ar.rulosoft.mimanganu.utils.Util;

/**
 * Created by Raul on 03/02/2016.
 */
class RawSenManga extends ServerBase {

    private static final String HOST = "http://raw.senmanga.com/";

    private static final int[] fltGenre = {
            R.string.flt_tag_all,
            R.string.flt_tag_action,
            R.string.flt_tag_adult,
            R.string.flt_tag_adventure,
            R.string.flt_tag_comedy,
            R.string.flt_tag_cooking,
            R.string.flt_tag_drama,
            R.string.flt_tag_ecchi,
            R.string.flt_tag_fantasy,
            R.string.flt_tag_gender_bender,
            R.string.flt_tag_harem,
            R.string.flt_tag_historical,
            R.string.flt_tag_horror,
            R.string.flt_tag_josei,
            R.string.flt_tag_light_novel,
            R.string.flt_tag_martial_arts,
            R.string.flt_tag_mature,
            R.string.flt_tag_music,
            R.string.flt_tag_mystery,
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
            R.string.flt_tag_supernatural,
            R.string.flt_tag_tragedy,
            R.string.flt_tag_webtoon,
            R.string.flt_tag_yuri
    };
    private static final String[] valGenre = {
            "Manga/",
            "directory/category/Action/",
            "directory/category/Adult/",
            "directory/category/Adventure/",
            "directory/category/Comedy/",
            "directory/category/Cooking/",
            "directory/category/Drama/",
            "directory/category/Ecchi/",
            "directory/category/Fantasy/",
            "directory/category/Gender-Bender/",
            "directory/category/Harem/",
            "directory/category/Historical/",
            "directory/category/Horror/",
            "directory/category/Josei/",
            "directory/category/Light_Novel/",
            "directory/category/Martial_Arts/",
            "directory/category/Mature/",
            "directory/category/Music/",
            "directory/category/Mystery/",
            "directory/category/Psychological/",
            "directory/category/Romance/",
            "directory/category/School_Life/",
            "directory/category/Sci-Fi/",
            "directory/category/Seinen/",
            "directory/category/Shoujo/",
            "directory/category/Shoujo-Ai/",
            "directory/category/Shounen/",
            "directory/category/Shounen-Ai/",
            "directory/category/Slice_of_Life/",
            "directory/category/Smut/",
            "directory/category/Sports/",
            "directory/category/Supernatural/",
            "directory/category/Tragedy/",
            "directory/category/Webtoons/",
            "directory/category/Yuri/"
    };
    private static final int[] fltOrder = {
            R.string.flt_order_views,
            R.string.flt_order_rating,
            R.string.flt_order_alpha,
    };
    private static final String[] valOrder = {
            "Manga/?order=popular",
            "Manga/?order=rating",
            "Manga/?order=title"
    };

    RawSenManga(Context context) {
        super(context);
        setFlag(R.drawable.flag_raw);
        setIcon(R.drawable.senmanga);
        setServerName("SenManga");
        setServerID(RAWSENMANGA);
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigatorAndFlushParameters().get(HOST + "Manga/?order=text-version");
        Pattern p = Pattern.compile("\\d</td><td><a href=\"([^\"]+)\"\\s*>([^<]+)", Pattern.DOTALL);
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(getServerID(), HtmlUnescape.Unescape(m.group(2)), HOST + m.group(1), false);
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String web = HOST + "Search.php?q=" + URLEncoder.encode(term, "UTF-8");
        String data = getNavigatorAndFlushParameters().get(web);
        Pattern p = Pattern.compile("<div class='search-results'>.+?<a href='(.+?)' title='(.+?)'", Pattern.DOTALL);
        Matcher m = p.matcher(data);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()) {
            Manga manga = new Manga(getServerID(), HtmlUnescape.Unescape(m.group(2)), HOST + m.group(1), false);
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String data = getNavigatorAndFlushParameters().get(manga.getPath());
            String data2 = getFirstMatchDefault("<div class=\"series_desc\">(.+?)<\\/div>", data, "");
            manga.setSynopsis(Util.getInstance().fromHtml(getFirstMatchDefault("<div itemprop=\"description\">(.+?)<", data2, defaultSynopsis)).toString());
            manga.setImages(HOST + getFirstMatchDefault("image\" src=\"(.+?)\"", data, ""));
            manga.setAuthor(Util.getInstance().fromHtml(getFirstMatchDefault("Author:<\\/strong> <span class='desc'>(.+?)<\\/span>", data2, "N/A")).toString());
            manga.setGenre(Util.getInstance().fromHtml(getFirstMatchDefault("in:<\\/strong><\\/p> (.+?)<\\/p>", data2, "N/A")).toString().trim());
            manga.setFinished(data2.contains("Complete"));
            Pattern p = Pattern.compile("<td><a href=\"(/.+?)\" title=\"(.+?)\"", Pattern.DOTALL);
            Matcher m = p.matcher(data);
            while (m.find()) {
                Chapter mc = new Chapter(HtmlUnescape.Unescape(m.group(2).trim()), HOST + m.group(1));
                mc.addChapterFirst(manga);
            }
        }
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        loadChapters(manga, forceReload);
    }


    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return null;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        if (chapter.getExtra() == null) {
            String data = getNavigatorAndFlushParameters().get(chapter.getPath());
            chapter.setExtra(getFirstMatchDefault("<img src=\".(vi.+?/)[^/]+?\"", data, "Error: failed to locate page image link"));
        }
        return HOST + chapter.getExtra() + page;
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data = getNavigatorAndFlushParameters().get(chapter.getPath());
        String number = getFirstMatchDefault("</select> of (\\d+)", data, "Error: failed to get the number of pages");
        chapter.setPages(Integer.parseInt(number));
        chapter.setExtra(getFirstMatchDefault("<img src=\".(vi.+?/)[^/]+?\"", data, "Error: failed to locate page image link"));
    }

    @NonNull
    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern p = Pattern.compile("<div class=\"cover\"><a href=\"/(.+?)\" title=\"(.+?)\"><img src=\"/(.+?)\"", Pattern.DOTALL);
        Matcher m = p.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()) {
            Manga manga = new Manga(getServerID(), HtmlUnescape.Unescape(m.group(2)), HOST + m.group(1), false);
            manga.setImages(HOST + m.group(3));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
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
        String web = HOST + valGenre[filters[0][0]] + "?page=" + pageNumber;
        if (fltGenre[filters[0][0]] == R.string.flt_tag_all) {
            web = HOST + valOrder[filters[1][0]] + "&page=" + pageNumber;
        }
        String source = getNavigatorAndFlushParameters().get(web);
        return getMangasFromSource(source);
    }
}
