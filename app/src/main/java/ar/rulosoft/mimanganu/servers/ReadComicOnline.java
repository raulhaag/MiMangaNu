package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;

class ReadComicOnline extends ServerBase {

    private static final String PATTERN_CHAPTER =
            "<td>[\\s]*<a[\\s]*href=\"(/Comic/[^\"]+)\"[^>]*>([^\"]+)</a>[\\s]*</td>";
    private static final String PATTERN_SEARCH =
            "href=\"(/Comic/.*?)\">([^<]+)</a>[^<]+<p>[^<]+<span class=\"info\"";
    //public static String IP = "31.192.104.134";
    private static String HOST = "readcomiconline.to";
    private static String[] genre = new String[]{
            "All", "Action", "Adventure", "Anthology", "Anthropomorphic", "Biography", "Children", "Comedy",
            "Crime", "Drama", "Family", "Fantasy", "Fighting", "Graphic Novels", "Historical", "Horror",
            "Leading Ladies", "LGBTQ", "Literature", "Manga", "Martial Arts", "Mature", "Military",
            "Movies & TV", "Mystery", "Mythology", "Personal", "Political", "Post-Apocalyptic",
            "Psychological", "Pulp", "Robots", "Romance", "School Life", "Sci-Fi", "Slice of Life",
            "Spy", "Superhero", "Supernatural", "Suspense", "Thriller", "Vampires", "Video Games", "War",
            "Western", "Zombies"
    };
    private static String[] genreV = new String[]{
            "/ComicList", "/Genre/Action", "/Genre/Adventure", "/Genre/Anthology", "/Genre/Anthropomorphic", "/Genre/Biography", "/Genre/Children", "/Genre/Comedy",
            "/Genre/Crime", "/Genre/Drama", "/Genre/Family", "/Genre/Fantasy", "/Genre/Fighting", "/Genre/Graphic-Novels", "/Genre/Historical", "/Genre/Horror",
            "/Genre/Leading-Ladies", "/Genre/LGBTQ", "/Genre/Literature", "/Genre/Manga", "/Genre/Martial-Arts", "/Genre/Mature", "/Genre/Military",
            "/Genre/Movies-TV", "/Genre/Mystery", "/Genre/Mythology", "/Genre/Personal", "/Genre/Political", "/Genre/Post-Apocalyptic",
            "/Genre/Psychological", "/Genre/Pulp", "/Genre/Robots", "/Genre/Romance", "/Genre/School-Life", "/Genre/Sci-Fi", "/Genre/Slice-of-Life",
            "/Genre/Spy", "/Genre/Superhero", "/Genre/Supernatural", "/Genre/Suspense", "/Genre/Thriller", "/Genre/Vampires", "/Genre/Video-Games", "/Genre/War",
            "/Genre/Western", "/Genre/Zombies"
    };

    private static String[] order = {"Popularity", "Latest Update", "New Manga", "a-z"};
    private static String[] orderV = new String[]{"/MostPopular", "/LatestUpdate", "/Newest", ""};

    private static String[] state = new String[]{
            "Any", "Ongoing", "Completed"
    };
    private static String[] stateV = new String[]{
            "", "Ongoing", "Completed"
    };

    ReadComicOnline(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.readcomiconline);
        this.setServerName("ReadComicOnline");
        setServerID(ServerBase.READCOMICONLINE);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addPost("keyword", term);
        String source = nav.post("http://" + HOST + "/Search/Comic");
        ArrayList<Manga> searchList;
        Pattern p = Pattern.compile(PATTERN_SEARCH);
        Matcher m = p.matcher(source);
        if (m.find()) {
            searchList = new ArrayList<>();
            boolean status = getFirstMatchDefault("Status:</span>&nbsp;([\\S]+)", m.group(), "Ongoing").length() == 9;
            searchList.add(new Manga(READCOMICONLINE, m.group(2), m.group(1), status));
        } else {
            searchList = getMangasSource(source);
        }
        return searchList;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 ||
                forceReload) loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        String source = getNavigatorAndFlushParameters().get("http://" + HOST + manga.getPath());

        // Summary
        manga.setSynopsis(Util.getInstance().fromHtml(getFirstMatchDefault(
                "<span " + "class=\"info\">Summary:</span>(.+?)</div>", source,
                defaultSynopsis)).toString());

        // Cover Image
        //Log.d("RCO", "m.gI: " + manga.getImages());
        if (manga.getImages() == null || manga.getImages().isEmpty()) {
            String coverImage = getFirstMatchDefault("src=\"(http[s]?://readcomiconline.to/Uploads/[^\"]+?|http[s]?://\\d+.bp.blogspot.com/[^\"]+?)\"", source, "");
            //Log.d("RCO", "cI: " + coverImage);
            if (!coverImage.isEmpty()) {
                manga.setImages(coverImage);
            }
        }

        // Author
        String artist = getFirstMatchDefault("Artist:.+?\">(.+?)</a>", source, "");
        String writer = getFirstMatchDefault("Writer:.+?\">(.+?)</a>", source, "");
        if (artist.equals(writer))
            manga.setAuthor(artist);
        else
            manga.setAuthor(artist + ", " + writer);

        // Genre
        manga.setGenre((Util.getInstance().fromHtml(getFirstMatchDefault("Genres:(.+?)</p>", source, "")).toString().replaceAll("^\\s+", "").trim()));

        manga.setFinished(getFirstMatchDefault("Status:</span>&nbsp;([\\S]+)", source, "Ongoing").length() == 9);

        // Chapter
        Pattern p = Pattern.compile(PATTERN_CHAPTER);
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(2).replace(" Read Online", ""), matcher.group(1)));
        }
        manga.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return chapter.getPath();
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        if (chapter.getExtra() == null || chapter.getExtra().length() < 2) {
            String source = getNavigatorAndFlushParameters().post("http://" + HOST + chapter.getPath());
            Pattern p = Pattern.compile("lstImages.push\\(\"(.+?)\"");
            Matcher m = p.matcher(source);
            String images = "";
            while (m.find()) {
                images = images + "|" + m.group(1);
            }
            chapter.setExtra(images);
        }
        return chapter.getExtra().split("\\|")[page];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        int pages = 0;
        if (chapter.getExtra() == null || chapter.getExtra().length() < 2) {
            String source = getNavigatorAndFlushParameters().get("http://" + HOST + chapter.getPath().replaceAll("[^!-z]+", ""), "http://" + HOST + chapter.getPath());
            Pattern p = Pattern.compile("lstImages.push\\(\"(.+?)\"");
            Matcher m = p.matcher(source);
            String images = "";
            while (m.find()) {
                pages++;
                images = images + "|" + m.group(1);
            }
            chapter.setExtra(images);
        }
        chapter.setPages(pages);
    }

    private ArrayList<Manga> getMangasSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("src=\"([^\"]+)\" style=\"float.+?href=\"(.+?)\">(.+?)<");
        Matcher m = p.matcher(source);
        while (m.find()) {
            /*Log.d("RCO", "1: " + m.group(1));
            Log.d("RCO", "2: " + m.group(2));
            Log.d("RCO", "3: " + m.group(3));*/
            Manga manga = new Manga(READCOMICONLINE, m.group(3), m.group(2), false);
            manga.setImages(m.group(1));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{new ServerFilter("Genre", genre, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Order", order, ServerFilter.FilterType.SINGLE)};
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        /*if (pageNumber > 1) {
            return new ArrayList<>();
        } else {
            Navigator nav = getNavigatorAndFlushParameters();
            nav.addPost("comicName", "");
            if (filters[0].length == 0) {
                for (int i = 1; i < genre.length; i++) {
                    nav.addPost("genres", "0");
                }
            } else {
                for (int i = 1; i < genre.length; i++) {
                    if (contains(filters[0], i)) {

                        nav.addPost("genres", "1");
                    } else {
                        nav.addPost("genres", "0");
                    }
                }
            }
            nav.addPost("status", stateV[filters[1][0]]);
            String source = nav.post("http://" + HOST + "/AdvanceSearch");
            return getMangasSource(source);
        }*/
        return getMangasFiltered(filters[0][0], filters[1][0], pageNumber);
    }

    public ArrayList<Manga> getMangasFiltered(int category, int order, int pageNumber) throws Exception {
        String web = genreV[category] + orderV[order];
        if (pageNumber > 1) {
            web = web + "?page=" + pageNumber;
        }
        //Log.d("RCO", "web: "+"http://" + HOST + web);
        String source = getNavigatorAndFlushParameters().get("http://" + HOST + web);
        return getMangasSource(source);
    }

    @Override
    public boolean hasList() {
        return false;
    }

}
