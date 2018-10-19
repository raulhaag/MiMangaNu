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
        String data = nav.get("https://www.mangakawaii.com/recherche?query=" + URLEncoder.encode(search, "UTF-8"));
        JSONArray array = new JSONObject(data).getJSONArray("suggestions");
        ArrayList<Manga> mangas = new ArrayList<>();
        for(int i = 0, n = array.length(); i < n; i++){
            mangas.add(new Manga(getServerID(), array.getJSONObject(i).getString("value"),
                    "https://www.mangakawaii.com/manga/" + array.getJSONObject(i).getString("data"),
                    false));
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
            String source = getNavigatorAndFlushParameters().get(manga.getPath());

            // Cover
            if (manga.getImages() == null || manga.getImages().isEmpty()) {
                String img = getFirstMatchDefault("src='(https://www.mangakawaii.com/uploads/manga/[^\"]+?)'", source, "");
                manga.setImages(img);
            }

            // Summary
            manga.setSynopsis(getFirstMatchDefault("id=\"synopsis\">[\\s]*<p>(.+?)</p>", source, context.getString(R.string.nodisponible)));

            // Status
            manga.setFinished(!getFirstMatchDefault("Statut(.+?)</span>", source, "").contains("En Cours"));

            // Author
            manga.setAuthor(getFirstMatchDefault("author/.+?\">(.+?)</a>", source, context.getString(R.string.nodisponible)));

            // Genre
            manga.setGenre(getFirstMatchDefault("Genres</span>(.+?)</span>", source, context.getString(R.string.nodisponible)));

            // Chapters
            Pattern p = Pattern.compile("href=\"(https://www\\.mangakawaii\\.com/manga/[^\"]+?)\">([^\"]+?)</a>", Pattern.DOTALL);
            Matcher matcher = p.matcher(source);
            ArrayList<String> tmpChapterList = new ArrayList<>();
            while (matcher.find()) {
                if(!tmpChapterList.contains(matcher.group(1))) {
                    manga.addChapterFirst(new Chapter(matcher.group(2), matcher.group(1)));
                    tmpChapterList.add(matcher.group(1));
                }
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
        if (chapter.getExtra() == null) {
            String source = getNavigatorAndFlushParameters().get(chapter.getPath());
            String tmpImages = getFirstMatchDefault("<div id=\"all\"(.+?)</div>", source, "");
            ArrayList<String> images = getAllMatch("data-src='.(https://www\\.mangakawaii\\.com/uploads/[^\"]+?).'", tmpImages);

            if(images.isEmpty()) {
                throw new Exception(context.getString(R.string.server_failed_loading_page_count));
            }
            chapter.setExtra(TextUtils.join("|", images));
            chapter.setPages(images.size());
        }
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        // TODO implement filtering

        String web = HOST + "/liste-mangas?page=" + pageNumber;
        String source = getNavigatorAndFlushParameters().get(web);

        Pattern pattern = Pattern.compile("<div class=\"media-left\">(.+?)</div>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {

            mangas.add(new Manga(getServerID(),
                    getFirstMatch("alt='([^']+)", matcher.group(1), context.getString(R.string.server_failed_locate_manga_name)),
                    getFirstMatch("href=\"([^\"]+)", matcher.group(1), context.getString(R.string.server_failed_locate_manga_url)),
                    getFirstMatchDefault("src='([^\']+)", matcher.group(1), "")
            ));
        }
        return mangas;
    }
}
