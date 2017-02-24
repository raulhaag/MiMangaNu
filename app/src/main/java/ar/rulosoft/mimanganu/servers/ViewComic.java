package ar.rulosoft.mimanganu.servers;

import android.content.Context;
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
    private static String HOST0 = "http://viewcomic.com";
    private static String HOST1 = "http://view-comic.com";
    private static String[] domain = new String[]{
            "http://view-comic.com/", "http://viewcomic.com/"
    };
    private static boolean onHost0;

    ViewComic(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.viewcomic);
        this.setServerName("ViewComic");
        setServerID(ServerBase.VIEWCOMIC);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String search) throws Exception {
        String web;
        if (onHost0) {
            web = "http://viewcomic.com/?s=" + URLEncoder.encode(search, "UTF-8");
        } else {
            web = "http://view-comic.com/?s=" + URLEncoder.encode(search, "UTF-8");
        }
        String source = getNavigatorAndFlushParameters().get(web);
        return getMangasFromSource(source);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 || forceReload)
            loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        String source = getNavigatorAndFlushParameters().get(manga.getPath());

        // Cover Img
        //Log.d("VC", "m.gI0: " + manga.getImages());
        if (manga.getImages() == null || manga.getImages().isEmpty())
            manga.setImages(getFirstMatchDefault("src=\"(http[s]?://\\d+\\.bp\\.blogspot\\.com/.+?)\"", source, ""));

        // Summary
        // ViewComic lists no summary ...

        // Status
        // ViewComic lists no status ...

        // Author
        // ViewComic lists no authors ...

        // Genre
        // ViewComic lists no genres ...

        // Chapters
        //<select id(.+?)</select>
        String newSource = getFirstMatchDefault("<select id(.+?)</select>", source, "");
        Pattern p = Pattern.compile("<option  value=\"(.+?)\">(.+?)</div>|<option selected value=\"(.+?)\">(.+?)</div>");
        Matcher matcher;
        if(newSource.isEmpty())
            matcher = p.matcher(source);
        else
            matcher = p.matcher(newSource);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            /*Log.d("VC", "1: " + matcher.group(1));
            Log.d("VC", "2: " + matcher.group(2));
            Log.d("VC", "3: " + matcher.group(3));
            Log.d("VC", "4: " + matcher.group(4));*/
            if(matcher.group(1) != null && matcher.group(2) != null)
                chapters.add(0, new Chapter(Util.getInstance().fromHtml(matcher.group(2).replaceAll("…", "").replaceAll("\\.","").trim()).toString().replaceAll("…", ""), matcher.group(1)));
            else
                chapters.add(0, new Chapter(Util.getInstance().fromHtml(matcher.group(4).replaceAll("…", "").replaceAll("\\.","").replaceAll("Reading", "").trim()).toString().replaceAll("…", ""), matcher.group(3)));
        }
        manga.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return null;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        if (chapter.getExtra() == null || chapter.getExtra().length() < 2) {
            setExtra(chapter);
        }
        return chapter.getExtra().split("\\|")[page];
    }

    private int setExtra(Chapter chapter) throws Exception {
        String source = getNavigatorAndFlushParameters().get(chapter.getPath());
        String images = "";
        Pattern pattern = Pattern.compile("src=\"(http[s]?://\\d+\\.bp\\.blogspot\\.com/.+?)\"");
        Matcher matcher = pattern.matcher(source);
        int i = 0;
        while (matcher.find()) {
            i++;
            //Log.d("VC", "(1_0): " + matcher.group(1));
            images = images + "|" + matcher.group(1);
        }

        if (i == 0) {
            Pattern pattern1 = Pattern.compile("src=\"(//\\d+\\.bp\\.blogspot\\.com/.+?)\"");
            Matcher matcher1 = pattern1.matcher(source);
            while (matcher1.find()) {
                i++;
                //Log.d("VC", "(1_1): " + "https:" + matcher1.group(1));
                images = images + "|" + "https:" + matcher1.group(1);
            }
        }
        chapter.setExtra(images);
        return i;
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getExtra() == null || chapter.getExtra().length() < 2) {
            chapter.setPages(setExtra(chapter));
        } else
            chapter.setPages(0);
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter("Domain", domain, ServerFilter.FilterType.SINGLE)
        };
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = "";
        String host0web = "";
        String host1web = "";
        if (domain[filters[0][0]].equals("http://viewcomic.com/")) {
            onHost0 = true;
            web = HOST0 + "/page/" + pageNumber + "/";
            host0web = web;
            host1web = HOST1 + "/page/" + pageNumber + "/";
        } else if (domain[filters[0][0]].equals("http://view-comic.com/")) {
            onHost0 = false;
            web = HOST1 + "/page/" + pageNumber + "/";
            host1web = web;
            host0web = HOST0 + "/page/" + pageNumber + "/";
        }
        //Log.d("VC", "web: " + web);
        String source = getNavigatorAndFlushParameters().getAndReturnResponseCodeOnFailure(web);
        if (source.equals("404")) {
            if (onHost0) {
                Log.e("VC", "viewcomic is down :(. Redirecting to view-comic");
                Util.getInstance().toast(context, "viewcomic is down :(. Redirecting to view-comic");
                source = getNavigatorAndFlushParameters().getAndReturnResponseCodeOnFailure(host1web);
            } else {
                Log.e("VC", "view-comic is down :(. Redirecting to viewcomic");
                Util.getInstance().toast(context, "view-comic is down :(. Redirecting to viewcomic");
                source = getNavigatorAndFlushParameters().getAndReturnResponseCodeOnFailure(host0web);
            }
            if (source.equals("404")) {
                Log.e("VC", "viewcomic and view-comic are down :(");
                Util.getInstance().toast(context, "viewcomic and view-comic are down :(");
            }
        }
        return getMangasFromSource(source);
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        //<div id=(.+?)</div>
        Pattern pattern = Pattern.compile("src=\"(http[s]?://\\d+\\.bp\\.blogspot\\.com/.+?)\".+?<a class=\"front-link\" href=\"(.+?)\">(.+?)</a>");
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            /*Log.d("VC", "(1): " + matcher.group(1));
            Log.d("VC", "(2): " + matcher.group(2));
            Log.d("VC", "(3): " + matcher.group(3));*/
            Manga manga = new Manga(getServerID(), Util.getInstance().fromHtml(matcher.group(3).replaceAll("…", "").replaceAll("\\.", "").trim()).toString().replaceAll("…", ""), matcher.group(2), false);
            manga.setImages(matcher.group(1));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public boolean hasList() {
        return false;
    }
}