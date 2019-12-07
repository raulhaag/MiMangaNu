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
import ar.rulosoft.navegadores.Navigator;

class ReadComicOnline extends ServerBase {

    private static final String HOST = "http://readcomiconline.to";

    private static final String PATTERN_CHAPTER =
            "<td>[\\s]*<a[\\s]*href=\"(/Comic/[^\"]+)\"[^>]*>([^\"]+)</a>[\\s]*</td>";
    private static final String PATTERN_SEARCH =
            "href=\"(/Comic/.*?)\">([^<]+)</a>[^<]+<p>[^<]+<span class=\"info\"";

    private static final int[] fltGenre = {
            R.string.flt_tag_all,
            R.string.flt_tag_action,
            R.string.flt_tag_adventure,
            R.string.flt_tag_anthology,
            R.string.flt_tag_anthropomorphic,
            R.string.flt_tag_biography,
            R.string.flt_tag_children,
            R.string.flt_tag_comedy,
            R.string.flt_tag_crime,
            R.string.flt_tag_drama,
            R.string.flt_tag_family,
            R.string.flt_tag_fantasy,
            R.string.flt_tag_fighting,
            R.string.flt_tag_graphic_novel,
            R.string.flt_tag_historical,
            R.string.flt_tag_horror,
            R.string.flt_tag_leading_ladies,
            R.string.flt_tag_lgbtq,
            R.string.flt_tag_literature,
            R.string.flt_tag_manga,
            R.string.flt_tag_martial_arts,
            R.string.flt_tag_mature,
            R.string.flt_tag_military,
            R.string.flt_tag_movies_and_tv,
            R.string.flt_tag_mystery,
            R.string.flt_tag_mythology,
            R.string.flt_tag_personal,
            R.string.flt_tag_political,
            R.string.flt_tag_post_apocalyptic,
            R.string.flt_tag_psychological,
            R.string.flt_tag_pulp,
            R.string.flt_tag_robots,
            R.string.flt_tag_romance,
            R.string.flt_tag_school_life,
            R.string.flt_tag_sci_fi,
            R.string.flt_tag_slice_of_life,
            R.string.flt_tag_spy,
            R.string.flt_tag_super_hero,
            R.string.flt_tag_supernatural,
            R.string.flt_tag_suspense,
            R.string.flt_tag_thriller,
            R.string.flt_tag_vampire,
            R.string.flt_tag_video_game,
            R.string.flt_tag_war,
            R.string.flt_tag_western,
            R.string.flt_tag_zombies,
    };
    private static final String[] valGenre = {
            "/ComicList",
            "/Genre/Action",
            "/Genre/Adventure",
            "/Genre/Anthology",
            "/Genre/Anthropomorphic",
            "/Genre/Biography",
            "/Genre/Children",
            "/Genre/Comedy",
            "/Genre/Crime",
            "/Genre/Drama",
            "/Genre/Family",
            "/Genre/Fantasy",
            "/Genre/Fighting",
            "/Genre/Graphic-Novels",
            "/Genre/Historical",
            "/Genre/Horror",
            "/Genre/Leading-Ladies",
            "/Genre/LGBTQ",
            "/Genre/Literature",
            "/Genre/Manga",
            "/Genre/Martial-Arts",
            "/Genre/Mature",
            "/Genre/Military",
            "/Genre/Movies-TV",
            "/Genre/Mystery",
            "/Genre/Mythology",
            "/Genre/Personal",
            "/Genre/Political",
            "/Genre/Post-Apocalyptic",
            "/Genre/Psychological",
            "/Genre/Pulp",
            "/Genre/Robots",
            "/Genre/Romance",
            "/Genre/School-Life",
            "/Genre/Sci-Fi",
            "/Genre/Slice-of-Life",
            "/Genre/Spy",
            "/Genre/Superhero",
            "/Genre/Supernatural",
            "/Genre/Suspense",
            "/Genre/Thriller",
            "/Genre/Vampires",
            "/Genre/Video-Games",
            "/Genre/War",
            "/Genre/Western",
            "/Genre/Zombies",
    };

    private static final int[] fltOrder = {
            R.string.flt_order_views,
            R.string.flt_order_last_update,
            R.string.flt_order_newest,
            R.string.flt_order_alpha,
    };
    private static final String[] valOrder = {
            "/MostPopular",
            "/LatestUpdate",
            "/Newest",
            "",
    };

    private static final int[] fltStatus = {
            R.string.flt_status_all,
            R.string.flt_status_ongoing,
            R.string.flt_status_completed,
    };
    private static final String[] valStatus = new String[]{
            "",
            "/Status/Ongoing",
            "/Status/Completed",
    };

    ReadComicOnline(Context context) {
        super(context);
        setFlag(R.drawable.flag_en);
        setIcon(R.drawable.readcomiconline);
        setServerName("ReadComicOnline");
        setServerID(READCOMICONLINE);
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
    public ArrayList<Manga> search(String term) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addPost("keyword", URLEncoder.encode(term, "UTF-8"));
        String source = nav.post(HOST + "/Search/Comic");
        ArrayList<Manga> searchList;
        Pattern p = Pattern.compile(PATTERN_SEARCH, Pattern.DOTALL);
        Matcher m = p.matcher(source);
        if (m.find()) {
            searchList = new ArrayList<>();
            searchList.add(new Manga(READCOMICONLINE, m.group(2), m.group(1), m.group().contains("Status:</span>&nbsp;Completed")));
        } else {
            searchList = getMangasSource(source);
        }
        return searchList;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String source = getNavigatorAndFlushParameters().get(HOST + manga.getPath());

            // Summary
            manga.setSynopsis(
                    getFirstMatchDefault("<span " + "class=\"info\">Summary:</span>(.+?)</div>", source,
                            context.getString(R.string.nodisponible)));

            // Cover Image
            if (manga.getImages() == null || manga.getImages().isEmpty()) {
                String coverImage = getFirstMatchDefault("src=\"(http[s]?://readcomiconline.to/Uploads/[^\"]+?|http[s]?://\\d+.bp.blogspot.com/[^\"]+?)\"", source, "");
                if (!coverImage.isEmpty()) {
                    manga.setImages(coverImage);
                }
            }

            // Author
            String artist = getFirstMatchDefault("Artist:.+?\">(.+?)</a>", source, context.getString(R.string.nodisponible));
            String writer = getFirstMatchDefault("Writer:.+?\">(.+?)</a>", source, context.getString(R.string.nodisponible));
            if (artist.equals(writer)) {
                manga.setAuthor(artist);
            } else {
                manga.setAuthor(artist + ", " + writer);
            }

            // Genre
            manga.setGenre(getFirstMatchDefault("Genres:</span>&nbsp;(.+?)</p>", source, context.getString(R.string.nodisponible)));

            // Status
            manga.setFinished(source.contains("Status:</span>&nbsp;Completed"));

            // Chapter
            Pattern p = Pattern.compile(PATTERN_CHAPTER, Pattern.DOTALL);
            Matcher matcher = p.matcher(source);
            while (matcher.find()) {
                manga.addChapterFirst(new Chapter(matcher.group(2).replace(" Read Online", ""), matcher.group(1)));
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        assert chapter.getExtra() != null;
        return chapter.getExtra().split("\\|")[page - 1];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getPages() == 0) {
            String source = getNavigatorAndFlushParameters().get(HOST + chapter.getPath());

            if (source.contains("class=\"g-recaptcha\"")) {
                throw new Exception(context.getString(R.string.server_uses_captcha));
            }

            ArrayList<String> images = getAllMatch("lstImages.push\\(\"([^\"]+)", source);

            if (images.isEmpty()) {
                throw new Exception(context.getString(R.string.server_failed_loading_page_count));
            }
            chapter.setExtra(TextUtils.join("|", images));
            chapter.setPages(images.size());
        }
    }

    private ArrayList<Manga> getMangasSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("src=\"([^\"]+)\" style=\"float.+?href=\"(.+?)\">(.+?)<", Pattern.DOTALL);
        Matcher m = p.matcher(source);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(3), m.group(2), m.group(1)));
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
                        context.getString(R.string.flt_status),
                        buildTranslatedStringArray(fltStatus), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_order),
                        buildTranslatedStringArray(fltOrder), ServerFilter.FilterType.SINGLE),
        };
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = HOST;
        // genre overrides status, as only one filter can be active at a time
        if (filters[0][0] != R.string.flt_tag_all) {
            web += valGenre[filters[0][0]] + valOrder[filters[2][0]];
        } else if (filters[1][0] != R.string.flt_status_all) {
            web += valStatus[filters[1][0]] + valOrder[filters[2][0]];
        }

        if (pageNumber > 1) {
            web += "?page=" + pageNumber;
        }
        String source = getNavigatorAndFlushParameters().get(web);
        return getMangasSource(source);
    }
}
