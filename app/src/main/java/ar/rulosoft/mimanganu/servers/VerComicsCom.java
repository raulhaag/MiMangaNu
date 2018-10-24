package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;

public class VerComicsCom extends ServerBase {

    private static final String HOST = "http://vercomics.com/";

    /**
     * Construct a new ServerBase object.
     *
     * @param context the context for this object
     */
    public VerComicsCom(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_es);
        this.setIcon(R.drawable.vercomicscom);
        this.setServerName("VerComicsCom");
        setServerID(ServerBase.VERCOMICSCOM);
    }

    @Nullable
    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String data = getNavigatorAndFlushParameters().get(HOST + "category/300/");
        JSONObject list = new JSONObject(getFirstMatch("js_array =(\\{[\\s\\S]+?\\})\\;\\s*</sc", data, context.getString(R.string.error)));
        int last = list.length();
        for (int i = 0; i < last; i++) {
            try {
                JSONObject o = (JSONObject) list.get("" + i);
                Manga m = new Manga(getServerID(), i + "|\t\t" + o.get("name").toString(), o.get("taxonomy").toString() + "/" +
                        o.get("slug").toString() + "/", false);
                mangas.add(m);
            } catch (Exception e) {
                last++;
                if (last > 1000) {//something go wrong better end it
                    break;
                }
            }
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        return null;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        String data = getNavigatorAndFlushParameters().get(HOST + manga.getPath());
        int pages = Integer.parseInt(getFirstMatchDefault("/page/(\\d+)/\">[^ ]+?<a class=\"nextpostslink\"", data, "1"));
        if (pages == 1) {
            pages = Integer.parseInt(getFirstMatchDefault("/(\\d+)/\">Última", data, "1"));
        }
        Pattern pattern = Pattern.compile("<a href=\"http://vercomics.com/(.+?)\" rel=\"bookmark\" title=\"(.+?)\"");
        Matcher matcher = pattern.matcher(data);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(new Chapter(matcher.group(2), matcher.group(1)));
        }
        for (int i = 2; i <= pages; i++) {
            //  Util.getInstance().toast(context, "Cargando " + i + " de " + pages + " paginas", Toast.LENGTH_SHORT);
            data = getNavigatorAndFlushParameters().get(HOST + manga.getPath() + "/page/" + i + "/");
            matcher = pattern.matcher(data);
            while (matcher.find()) {
                chapters.add(new Chapter(matcher.group(2), matcher.group(1)));
            }
        }
        for (Chapter c : chapters) {
            manga.addChapterFirst(c);
        }
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        String data = getNavigatorAndFlushParameters().get(HOST + manga.getPath());
        manga.setImages(getFirstMatchDefault("\"imgcover\" src=\"(.+?)\"", data, null));
        manga.setAuthor(context.getString(R.string.nodisponible));
        manga.setSynopsis(context.getString(R.string.nodisponible));
        manga.setGenre(context.getString(R.string.nodisponible));
        JSONObject list = new JSONObject(getFirstMatch("js_array =(\\{[\\s\\S]+?\\})\\;\\s*</sc", data, context.getString(R.string.error)));
        JSONObject o = (JSONObject) list.get(manga.getTitle().split("\\|")[0]);
        manga.setTitle(o.get("name").toString());
        manga.setSynopsis(o.get("description").toString());
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        return chapter.getExtra().split("\\|")[page];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data = getNavigatorAndFlushParameters().get(HOST + chapter.getPath());
        String page = getFirstMatchDefault("data-url=\"(.+?)\"", data, "notfound");
        if (page.equals("notfound")) {
            page = getFirstMatchDefault("src=\"([^\\s]+issu[^\\s]+)\"", data, "notfound");
        }
        String username = "";
        String docname = "";
        if (page.contains("embed")) {
            String iframe = getNavigatorAndFlushParameters().get("https://e.issuu.com/config/" + page.substring(page.lastIndexOf("/") + 1) + ".json");
            username = iframe.split("ownerUsername\":")[1].split(",")[0].split("\"")[1];
            docname = iframe.split("documentURI\":")[1].split(",")[0].split("\"")[1];
        } else if (page.contains("/docs/")) {
            username = page.split("/")[3];
            docname = page.split("/")[5];
        } else {
            return;
        }
        String query_url = "http://api.issuu.com/query?action=issuu.document.get_anonymous&format=json&documentUsername=" + username + "&name=" + docname + "&jsonCallback=C&_1341928054865=";

        String images = "";
        String js = getNavigatorAndFlushParameters().get(query_url);
        int pageCount = Integer.parseInt("0" + js.split("pageCount\":")[1].split(",")[0].trim());
        String hash = js.split("documentId\":")[1].split(",")[0].split("\"")[1];
        for (int i = 1; i <= pageCount; i++) {
            images = images + "|" + "http://image.issuu.com/" + hash + "/jpg/page_" + i + ".jpg";
        }

        chapter.setPages(pageCount);
        chapter.setExtra(images);
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public boolean hasFilteredNavigation() {
        return false;
    }
}
