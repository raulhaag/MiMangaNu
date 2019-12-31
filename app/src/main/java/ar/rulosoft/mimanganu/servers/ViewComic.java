package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;

/**
 * Created by xtj-9182 on 18.02.2016.
 */
class ViewComic extends ServerBase {
    private static final String PATTERN_MANGA =
            "src=\"(https?://\\d+\\.bp\\.blogspot\\.com/[^\"]+)\".+?<a class=\"front-link\" href=\"([^\"]+)\">([^….<]+)";

    private static final String HOST0 = "http://view-comic.com";
    private static final String HOST1 = "http://viewcomic.com";
    private static final String[] domain = {
            HOST0, HOST1
    };
    private static boolean onHost0;

    ViewComic(Context context) {
        super(context);
        setFlag(R.drawable.flag_en);
        setIcon(R.drawable.viewcomic);
        setServerName("ViewComic");
        setServerID(VIEWCOMIC);
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
        String web, suffix;

        suffix = "/?s=" + URLEncoder.encode(search, "UTF-8");
        if (onHost0) {
            web = HOST0 + suffix;
        } else {
            web = HOST1 + suffix;
        }

        String source = getNavigatorAndFlushParameters().getAndReturnResponseCodeOnFailure(web);
        if (source.equals("404") || source.equals("500") || source.contains(".com temporarily close")) {
            if (onHost0) {
                Log.e("VC", "viewcomic is down :(. Redirecting to view-comic");
                Util.getInstance().toast(context, context.getString(R.string.viewcomic_host0_down_redirect));
                web = HOST1 + suffix;
            } else {
                Log.e("VC", "view-comic is down :(. Redirecting to viewcomic");
                Util.getInstance().toast(context, context.getString(R.string.viewcomic_host1_down_redirect));
                web = HOST0 + suffix;
            }
            // retry with switchover
            source = getNavigatorAndFlushParameters().getAndReturnResponseCodeOnFailure(web);
            if (source.equals("404") || source.equals("500") || source.contains(".com temporarily close")) {
                Log.e("VC", "viewcomic and view-comic are down :(");
                throw new Exception(context.getString(R.string.viewcomic_down));
            }
        }

        return getMangasFromSource(source);
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
            if (manga.getImages() == null || manga.getImages().isEmpty())
                manga.setImages(getFirstMatchDefault("src=\"(http[s]?://\\d+\\.bp\\.blogspot\\.com/.+?)\"", source, ""));

            // Summary
            manga.setSynopsis(context.getString(R.string.nodisponible));

            // Status
            // ViewComic lists no status ...

            // Author
            manga.setAuthor(context.getString(R.string.nodisponible));

            // Genre
            manga.setGenre(context.getString(R.string.nodisponible));

            // Chapters
            String newSource = getFirstMatchDefault("<select id(.+?)</select>", source, "");
            Pattern p = Pattern.compile("<option  value=\"(.+?)\">(.+?)</div>|<option selected value=\"(.+?)\">(.+?)</div>", Pattern.DOTALL);
            Matcher matcher;
            if (newSource.isEmpty()) {
                matcher = p.matcher(source);
            } else {
                matcher = p.matcher(newSource);
            }
            while (matcher.find()) {
                if (matcher.group(1) != null && matcher.group(2) != null) {
                    manga.addChapterFirst(new Chapter(matcher.group(2).replaceAll("[….\\s]*(Reading)?$", ""), matcher.group(1)));
                } else {
                    manga.addChapterFirst(new Chapter(matcher.group(4).replaceAll("[….\\s]*(Reading)?$", ""), matcher.group(3)));
                }
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        assert chapter.getExtra() != null;
        String url = chapter.getExtra().split("\\|")[page - 1];
        if (url.startsWith("//")) {
            url = "http:" + url;
        }
        return url;
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getPages() == 0) {
            String source = getNavigatorAndFlushParameters().get(chapter.getPath());
            ArrayList<String> images = getAllMatch("src=\"(http[s]?://\\d+\\.bp\\.blogspot\\.com/.+?)\"", source);
            if (images.isEmpty()) {
                images = getAllMatch("src=\"(//\\d+\\.bp\\.blogspot\\.com/.+?)\"", source);
            }

            if (images.isEmpty()) {
                throw new Exception(context.getString(R.string.server_failed_loading_image));
            }
            chapter.setExtra(TextUtils.join("|", images));
            chapter.setPages(images.size());
        }
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(
                        context.getString(R.string.flt_domain),
                        domain, ServerFilter.FilterType.SINGLE)
        };
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web;

        String host0web = HOST0 + "/page/" + pageNumber + "/";
        String host1web = HOST1 + "/page/" + pageNumber + "/";

        if (filters[0][0] == 0) {
            onHost0 = true;
            web = host0web;
        } else {
            onHost0 = false;
            web = host1web;
        }

        String source = getNavigatorAndFlushParameters().getAndReturnResponseCodeOnFailure(web);
        if (source.equals("404") || source.equals("500")) {
            if (onHost0) {
                Log.e("VC", "viewcomic is down :(. Redirecting to view-comic");
                Util.getInstance().toast(context, context.getString(R.string.viewcomic_host0_down_redirect));
                source = getNavigatorAndFlushParameters().getAndReturnResponseCodeOnFailure(host1web);
            } else {
                Log.e("VC", "view-comic is down :(. Redirecting to viewcomic");
                Util.getInstance().toast(context, context.getString(R.string.viewcomic_host1_down_redirect));
                source = getNavigatorAndFlushParameters().getAndReturnResponseCodeOnFailure(host0web);
            }
            if (source.equals("404") || source.equals("500")) {
                Log.e("VC", "viewcomic and view-comic are down :(");
                throw new Exception(context.getString(R.string.viewcomic_down));
            }
        }
        return getMangasFromSource(source);
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern pattern = Pattern.compile(PATTERN_MANGA, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            mangas.add(new Manga(getServerID(), matcher.group(3), matcher.group(2), matcher.group(1)));
        }
        return mangas;
    }
}
