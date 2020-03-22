package ar.rulosoft.mimanganu.servers;

import android.content.Context;
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
 * Created by xtj-9182 on 23.04.2017.
 */
class MangaStream extends ServerBase {
    private static final String HOST = "http://mangastream.cc";
    private static ArrayList<Manga> tmpManga = new ArrayList<>();
    private boolean coldStart = true;
    private static final String PATTERN_CHAPTER = "<li class=\"wp-manga-chapter\\s*\">[\\s\\S]+?href=\"([^\"]+)\">([^<]+)";
    private static final String PATTERN_MANGA = "href=\"([^\"]+?\\/manga\\/[^\"]+)\" title=\"([^\"]+)[\\s\\S]+?src=\"([^\"]+)";
    private static final String PATTERN_IMAGE = "src=\"(//[^/]+/cdn/manga/[^\"]+)";

    private static final String[] genresValues = {"", "action-manga", "adventure-manga",
            "comedy-manga", "comics-online", "completed-manga", "drama-manga", "ecchi-manga",
            "fantasy-manga", "gender-bender-manga", "harem-manga", "historical", "horror-manga",
            "josei", "manga", "manhua", "manhwa-manga", "martial-arts-manga", "mature-manga", "mystery",
            "psychological-manga", "reincarnation-manga", "reverse-harem", "romance-manga",
            "read-school-life-manga", "sci-fi", "seinen-manga", "shotacon", "shoujo-manga",
            "shoujo-ai", "shounen-manga", "shounen-ai", "slice-of-life", "smut-manga", "soft-yaoi",
            "soft-yuri", "sports-manga", "supernatural", "tragedy", "webtoons", "yaoi-manga",
            "yuri-manga"};

    private static final String[] genresLabels = {"All", "Action", "Adventure", "Comedy", "Comics", "Completed",
            "Drama", "Ecchi", "Fantasy", "Gender", "Harem", "Historical", "Horror", "Josei", "Manga",
            "Manhua", "Manhwa", "Martial", "Mature", "Mystery", "Psychological", "Reincarnation",
            "Reverse", "Romance", "School", "Sci-fi", "Seinen", "Shotacon", "Shoujo", "Shoujo", "Shounen",
            "Shounen", "Slice", "Smut", "Soft", "Soft", "Sports", "Supernatural", "Tragedy", "Webtoon",
            "Yaoi", "Yuri"};

    private static final String[] orderValues = {"?m_orderby=latest", "?m_orderby=alphabet",
            "?m_orderby=rating", "?m_orderby=trending", "?m_orderby=views", "?m_orderby=new-manga"};

    private static final String[] orderLabels = {"Latest", "A-Z", "Rating", "Trending", "Most", "New"};

    MangaStream(Context context) {
        super(context);
        setFlag(R.drawable.flag_en);
        setIcon(R.drawable.mangastream);
        setServerName("MangaStream");
        setServerID(MANGASTREAM);
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
    public ArrayList<Manga> search(String search) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addPost("action", "madara_load_more");
        nav.addPost("page", "0");
        nav.addPost("template", "madara-core/content/content-search");
        nav.addPost("vars[s]", search);
        nav.addPost("vars[paged]", "1");
        nav.addPost("vars[template]", "search");
        nav.addPost("vars[meta_query][0][relation]", "AND");
        nav.addPost("vars[meta_query][relation]", "OR");
        nav.addPost("vars[post_type]", "wp-manga");
        nav.addPost("vars[post_status]", "publish");
        nav.addPost("vars[manga_archives_item_layout]", "default");
        //  nav.addPost("template", "madara-core/content/content-archive");
        //   nav.addHeader("Referer","https://www.mangastream.cc/");
        return getMangasFromSource(nav.post("https://www.mangastream.cc/wp-admin/admin-ajax.php"));
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String source = getNavigatorAndFlushParameters().get(manga.getPath());

            manga.setImages(getFirstMatchDefault("<div class=\"summary_image\">[\\s\\S]+?src=\"([^\"]+)\"", source, ""));

            // no Summary
            manga.setSynopsis(context.getString(R.string.nodisponible));

            // no Status
            manga.setFinished(!source.contains("OnGoing\t</div>"));

            // no Authors
            manga.setAuthor(getFirstMatch("manga-author[^\"]+\" rel=\"tag\">([^<]+)",
                    source, context.getString(R.string.nodisponible)));

            // no Genres
            manga.setGenre(TextUtils.join(", ", getAllMatch("manga-genre[^\"]+\" rel=\"tag\">([^<]+)", source)));

            // Chapters
            Pattern p = Pattern.compile(PATTERN_CHAPTER, Pattern.DOTALL);
            Matcher matcher = p.matcher(source);
            while (matcher.find()) {
                manga.addChapterFirst(new Chapter(matcher.group(2).trim(), matcher.group(1)));
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        if (chapter.getExtra() == null || chapter.getExtra().isEmpty()) {//to older versions
            chapter.setPages(0);
            chapterInit(chapter);
        }
        return chapter.getExtra().split("\\|")[page];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getPages() == 0) {
            String source = getNavigatorAndFlushParameters().get(chapter.getPath());
            ArrayList<String> imgs = getAllMatch("\"image-[^\"]+\" src=\"([^\"]+)", source);
            if (source.contains("been removed from the website.") || imgs.isEmpty()) {
                throw new Exception("Licenced, removed chapter or outdated plugin.");
            }
            chapter.setExtra("|" + TextUtils.join("|", imgs));
            chapter.setPages(imgs.size());
        }
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern pattern = Pattern.compile(PATTERN_MANGA, Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            mangas.add(new Manga(getServerID(), matcher.group(2), matcher.group(1), matcher.group(3)));
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addPost("action", "madara_load_more");
        nav.addPost("page", "" + pageNumber);
        nav.addPost("template", "madara-core/content/content-archive");
        nav.addPost("vars[wp-manga-genre]", genresValues[filters[0][0]]);
        nav.addPost("vars[error]", "");
        nav.addPost("vars[m]", "");
        nav.addPost("vars[p]", "0");
        nav.addPost("vars[post_parent]", "");
        nav.addPost("vars[subpost]", "");
        nav.addPost("vars[subpost_id]", "");
        nav.addPost("vars[attachment]", "");
        nav.addPost("vars[attachment_id]", "0");
        nav.addPost("vars[name]", "");
        nav.addPost("vars[pagename]", "");
        nav.addPost("vars[page_id]", "0");
        nav.addPost("vars[second]", "");
        nav.addPost("vars[minute]", "");
        nav.addPost("vars[hour]", "");
        nav.addPost("vars[day]", "0");
        nav.addPost("vars[monthnum]", "0");
        nav.addPost("vars[year]", "0");
        nav.addPost("vars[w]", "0");
        nav.addPost("vars[category_name]", "");
        nav.addPost("vars[tag]", "");
        nav.addPost("vars[cat]", "");
        nav.addPost("vars[tag_id]", "");
        nav.addPost("vars[author]", "");
        nav.addPost("vars[author_name]", "");
        nav.addPost("vars[feed]", "");
        nav.addPost("vars[tb]", "");
        nav.addPost("vars[paged]", "1");
        nav.addPost("vars[meta_key]", "_latest_update");
        nav.addPost("vars[meta_value]", "");
        nav.addPost("vars[preview]", "");
        nav.addPost("vars[s]", "");
        nav.addPost("vars[sentence]", "");
        nav.addPost("vars[title]", "");
        nav.addPost("vars[fields]", "");
        nav.addPost("vars[menu_order]", "");
        nav.addPost("vars[embed]", "");
        nav.addPost("vars[ignore_sticky_posts]", "false");
        nav.addPost("vars[suppress_filters]", "false");
        nav.addPost("vars[cache_results]", "true");
        nav.addPost("vars[update_post_term_cache]", "true");
        nav.addPost("vars[lazy_load_term_meta]", "true");
        nav.addPost("vars[update_post_meta_cache]", "true");
        nav.addPost("vars[post_type]", "wp-manga");
        nav.addPost("vars[posts_per_page]", "10");
        nav.addPost("vars[nopaging]", "false");
        nav.addPost("vars[comments_per_page]", "50");
        nav.addPost("vars[no_found_rows]", "false");
        nav.addPost("vars[taxonomy]", "wp-manga-genre");
        nav.addPost("vars[term]", "action-manga");
        nav.addPost("vars[order]", "desc");
        nav.addPost("vars[orderby]", "meta_value_num");
        nav.addPost("vars[template]", "archive");
        nav.addPost("vars[sidebar]", "right");
        nav.addPost("vars[post_status]", "publish");
        nav.addPost("vars[meta_query][relation]", "OR");
        nav.addHeader("Referer", "https://www.mangastream.cc/");
        return getMangasFromSource(nav.post("https://www.mangastream.cc/wp-admin/admin-ajax.php"));
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(context.getString(R.string.genre), genresLabels, ServerFilter.FilterType.SINGLE),
                new ServerFilter(context.getString(R.string.flt_order), genresLabels, ServerFilter.FilterType.SINGLE),
        };
    }
}
