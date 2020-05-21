package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navigator;

/**
 * Created by xtj-9182 on 09.05.2017.
 */
/* Je suis Charlie */
class MangaKawaii extends ServerBase {
    private static final String HOST = "https://www.mangakawaii.com";

    MangaKawaii(Context context) {
        super(context);
        setFlag(R.drawable.flag_fr);
        setIcon(R.drawable.mangakawaii);
        setServerName("MangaKawaii");
        setServerID(MANGAKAWAII);
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
    public boolean hasSearch() {
        return true;
    }

    @Override
    public ArrayList<Manga> search(String search) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        String data = nav.get(HOST + "/recherche?query=" + URLEncoder.encode(search, "UTF-8"));
        JSONArray array = new JSONObject(data).getJSONArray("suggestions");
        ArrayList<Manga> mangas = new ArrayList<>();
        for (int i = 0, n = array.length(); i < n; i++) {
            mangas.add(new Manga(getServerID(), array.getJSONObject(i).getString("value"),
                    "/manga/" + array.getJSONObject(i).getString("data"),
                    false));
        }
        return mangas;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public synchronized void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String source = getNavigatorAndFlushParameters().get(HOST + manga.getPath());

            // Cover
            if (manga.getImages() == null || manga.getImages().isEmpty()) {
                String img = getFirstMatchDefault("itemprop=\"image\" content = \"([^\"]+)", source, "");
                manga.setImages(img);
            }

            // Summary
            manga.setSynopsis(getFirstMatchDefault("desc__content\">(.+?)<br></div>", source, context.getString(R.string.nodisponible)));

            // Status
            manga.setFinished(!getFirstMatchDefault("Statut[^=]+[^>]+>([^<]+)", source, "").contains("En Cours"));

            // Author
            manga.setAuthor(getFirstMatchDefault("liste-manga/author/[^\"]+\">([^<]+)<", source, context.getString(R.string.nodisponible)));

            // Genre
            manga.setGenre(TextUtils.join(",", getAllMatch("liste-manga/category/[^\"]+\">([^<]+)</a>", source)));

            // Chapters
            Pattern p = Pattern.compile("<a href=\"(/manga/[^\"]+)[\\s\\S]+?svg>\\s+([^\\n]+)", Pattern.DOTALL);
            Matcher matcher = p.matcher(source);
            ArrayList<String> tmpChapterList = new ArrayList<>();
            while (matcher.find()) {
                if (!tmpChapterList.contains(matcher.group(1))) {
                    manga.addChapterFirst(new Chapter(matcher.group(2), matcher.group(1)));
                    tmpChapterList.add(matcher.group(1));
                }
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        assert chapter.getExtra() != null;
        return chapter.getExtra().split("\\|")[page];
    }

    @Override
    public synchronized void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getExtra() == null || chapter.getExtra().length() == 0) {
            getNavigatorAndFlushParameters().get(HOST);
            String source = getNavigatorAndFlushParameters().get(HOST + chapter.getPath() + "/1");
            ArrayList<String> images = getAllMatch("\"data-src\", \"\\s*([^\"]+)\\s+", source);
            if (images.isEmpty()) {
                throw new Exception(context.getString(R.string.server_failed_loading_page_count));
            }
            chapter.setExtra(TextUtils.join("|", images));
            chapter.setPages(images.size());
        }
    }

    @Override
    public synchronized ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = HOST + "/filterLists?page=" + pageNumber + "&cat=&alpha=&sortBy=name&asc=true&author=";
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addHeader("Referer", "https://www.mangakawaii.com/liste-manga");
        nav.addHeader("X-Requested-With", "XMLHttpRequest");
        String source = nav.get(web);
        Pattern pattern = Pattern.compile("(\\/manga\\/[^\"]+)\"[\\s]*?data-background-image=\"([^\"]+)\"[\\s\\S]+?-item__name\">([^<]+)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            mangas.add(new Manga(getServerID(), matcher.group(3), matcher.group(1), matcher.group(2)));
        }
        return mangas;
    }

    @Override
    public boolean needRefererForImages() {
        return false;
    }
}
