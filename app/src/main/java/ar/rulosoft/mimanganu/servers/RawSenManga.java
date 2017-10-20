package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;

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
            "directory/",
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
            "directory/popular/",
            "directory/rating/",
            "directory/"
    };

    RawSenManga(Context context) {
        super(context);
        setFlag(R.drawable.flag_raw);
        setIcon(R.drawable.senmanga);
        setServerName("SenManga");
        setServerID(RAWSENMANGA);
    }

    @Nullable
    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String web = HOST + "search/" + URLEncoder.encode(term, "UTF-8");
        String data = getNavigatorAndFlushParameters().get(web);
        return getMangasFromSource(data);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String data = getNavigatorAndFlushParameters().get(manga.getPath());
            // summary
            manga.setSynopsis(Util.getInstance().fromHtml(
                    getFirstMatchDefault("description\" content=\"(.+?)\">", data,
                            context.getString(R.string.nodisponible)).replace("</a>", "</a>,"))
                    .toString().trim());
            // cover
            manga.setImages(getFirstMatchDefault("itemprop=\"image\" src=\"(.+?)\"", data, ""));
            //  author
            manga.setAuthor(Util.getInstance().fromHtml(
                    getFirstMatchDefault("<li><b>Author<\\/b>:(.+?)<\\/li>", data,
                            context.getString(R.string.nodisponible)).replace("</a>", "</a>,"))
                    .toString().trim());
            // genre
            manga.setGenre(Util.getInstance().fromHtml(
                    getFirstMatchDefault("<li><b>Categories<\\/b>:(.+?)<\\/li>", data,
                            context.getString(R.string.nodisponible)).replace("</a>", "</a>,"))
                    .toString().trim());
            // status
            manga.setFinished(getFirstMatchDefault("<li><b>Status<\\/b>:(.+?)<\\/li>", data, " ").contains("Complete"));
            // chapters
            Pattern p = Pattern.compile("<div class=\"title\"><a href=\"([^\"]+)\" title=\"([^\"]+)", Pattern.DOTALL);
            Matcher m = p.matcher(data);
            while (m.find()) {
                Chapter mc;
                if (m.group(1).endsWith("/1"))
                    mc = new Chapter(m.group(2), m.group(1).substring(0,m.group(1).lastIndexOf("/")));
                else
                    mc = new Chapter(m.group(2), m.group(1));
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
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addHeader("Referer", chapter.getPath());
        String data = nav.get(chapter.getPath() + "/" + page);
        return getFirstMatch("img src=\"(https?://raw.senmanga.com/viewer[^\"]+)", data, "\"Error: failed to locate page image link\"");
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data = getNavigatorAndFlushParameters().get(chapter.getPath());
        String number = getFirstMatchDefault("</select> of (\\d+)", data, "Error: failed to get the number of pages");
        chapter.setPages(Integer.parseInt(number));
    }

    @NonNull
    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern p = Pattern.compile("cover\" src=\"([^\"]+).+?alt=\"([^\"]+).+?[\\s\\S]+?href=\"([^\"]+)", Pattern.DOTALL);
        Matcher m = p.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(2), m.group(3), false);
            manga.setImages(m.group(1).replace("/thumb/50x50/", "/covers/"));
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
        String web = HOST + valGenre[filters[0][0]] + "page/" + pageNumber;
        if (fltGenre[filters[0][0]] == R.string.flt_tag_all) {
            web = HOST + valOrder[filters[1][0]] + "page/" + pageNumber;
        }
        String source = getNavigatorAndFlushParameters().get(web);
        return getMangasFromSource(source);
    }
}
