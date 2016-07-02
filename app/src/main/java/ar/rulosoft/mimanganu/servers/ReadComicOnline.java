package ar.rulosoft.mimanganu.servers;

import android.text.Html;

import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;
import ar.rulosoft.navegadores.RefererInterceptor;

public class ReadComicOnline extends ServerBase {

    private static final String PATTERN_CHAPTER =
            "<td>[\\s]*<a[\\s]*href=\"(/Comic/[^\"]+)\"[^>]*>([^\"]+)</a>[\\s]*</td>";
    private static final String PATTERN_SEARCH =
            "href=\"(/Comic/.*?)\">([^<]+)</a>[^<]+<p>[^<]+<span class=\"info\"";
    public static String IP = "31.192.104.134";
    private static String HOST = "readcomiconline.to";
    private static String[] genre = new String[]{
            "All", "Action", "Adventure", "Anthology", "Anthropomorphic", "Biography", "Comedy", "Crime", "Drama", "Family", "Fantasy", "Fighting", "Graphic Novels", "Historical", "Horror", "Leading Ladies", "Literature", "Manga", "Martial Arts", "Mature", "Military", "Movies & TV", "Mystery", "Mythology", "Political", "Post-Apocalyptic", "Psychological", "Pulp", "Robots", "Romance", "Sci-Fi", "Spy", "Superhero", "Supernatural", "Suspense", "Thriller", "Vampires", "Video Games", "War", "Western", "Zombies"
    };
    private static String[] genreV = new String[]{
            "/ComicList", "/Genre/Action", "/Genre/Adventure", "/Genre/Anthology", "/Genre/Anthropomorphic", "/Genre/Biography", "/Genre/Comedy", "/Genre/Crime", "/Genre/Drama", "/Genre/Family", "/Genre/Fantasy", "/Genre/Fighting", "/Genre/Graphic-Novels", "/Genre/Historical", "/Genre/Horror", "/Genre/Leading-Ladies", "/Genre/Literature", "/Genre/Manga", "/Genre/Martial-Arts", "/Genre/Mature", "/Genre/Military", "/Genre/Movies-TV", "/Genre/Mystery", "/Genre/Mythology", "/Genre/Political", "/Genre/Post-Apocalyptic", "/Genre/Psychological", "/Genre/Pulp", "/Genre/Robots", "/Genre/Romance", "/Genre/Sci-Fi", "/Genre/Spy", "/Genre/Superhero", "/Genre/Supernatural", "/Genre/Suspense", "/Genre/Thriller", "/Genre/Vampires", "/Genre/Video-Games", "/Genre/War", "/Genre/Western", "/Genre/Zombies"
    };
    private static String[] order = new String[]{
            "/MostPopular", "/LatestUpdate", "/Newest", ""
    };

    public ReadComicOnline() {
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

        Navegador nav = getNavWithHeader();

        nav.addPost("keyword", term);
        String source = nav.post(IP, "/Search/Comic", HOST);

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
        String source = getNavWithHeader().get(IP, manga.getPath(), HOST);

        // Summary
        manga.setSynopsis(Html.fromHtml(getFirstMatchDefault(
                "<span " + "class=\"info\">Summary:</span>(.+?)</div>", source,
                "Without" + " synopsis.")).toString());
        // Title
        String pictures = getFirstMatchDefault(
                "rel=\"image_src\" href=\"(.+?)" + "\"", source, null);
        if (pictures != null) {
            if (pictures.contains(HOST)) {
                manga.setImages("http://" + IP + pictures.replace("http://" + HOST, "") + "|" + HOST);
            } else {
                manga.setImages(pictures);
            }
        }

        // Author
        manga.setAuthor(getFirstMatchDefault("href=\"/AuthorArtist/.+?>(.+?)<", source, ""));

        //genre
        manga.setGenre((Html.fromHtml(getFirstMatchDefault("Genres:(.+?)</p>", source, "")).toString().replaceAll("^\\s+", "").trim()));

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

            String source = getNavWithHeader().post(IP, chapter.getPath(), HOST);

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
            Navegador nav = new Navegador();
            OkHttpClient client = nav.getHttpClient();
            client.networkInterceptors().add(new RefererInterceptor("http://" + HOST + chapter.getPath()));
            String source = nav.get(IP, chapter.getPath().replaceAll("[^!-z]+", ""), HOST);
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

    @Override
    public ArrayList<Manga> getMangasFiltered(int category, int order, int pageNumber) throws Exception {
        String web = genreV[category] + ReadComicOnline.order[order];
        if (pageNumber > 1) {
            web = web + "?page=" + pageNumber;
        }
        String source = getNavWithHeader().post(IP, web, HOST);
        return getMangasSource(source);
    }

    private ArrayList<Manga> getMangasSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("src=\"([^\"]+)\" style=\"float.+?href=\"(.+?)\">(.+?)<");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(READCOMICONLINE, m.group(3), m.group(2), false);
            if (m.group(1).contains(HOST)) {
                manga.setImages("http://" + IP + m.group(1).replace("http://" + HOST, "") + "|" + HOST);
            } else {
                manga.setImages(m.group(1));
            }
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public String[] getCategories() {
        return genre;
    }

    @Override
    public String[] getOrders() {
        return new String[]{"Popularity", "Latest Update", "New Comic", "a-z"};
    }

    @Override
    public boolean hasList() {
        return false;
    }

}
