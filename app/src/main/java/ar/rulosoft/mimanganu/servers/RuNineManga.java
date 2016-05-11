package ar.rulosoft.mimanganu.servers;

import android.text.Html;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;

/**
 * Created by Raul on 29/11/2015.
 */
public class RuNineManga extends ServerBase {
    public static String HOST = "http://ru.ninemanga.com";
    private static String[] genre = new String[]{
            "арт","боевик","боевыеискусства","вампиры","гарем","гендернаяинтрига","героическоефэнтези","детектив","дзёсэй","додзинси","драма","игра","история","кодомо","комедия","махо-сёдзё","меха","мистика","научнаяфантастика","повседневность","постапокалиптика","приключения","психология","романтика","самурайскийбоевик","сверхъестественное","сёдзё","сёдзё-ай","сёнэн","сёнэн-ай","спорт","сэйнэн","трагедия","триллер","ужасы","фантастика","фэнтези","школа","этти","юри"
    };
    private static String[] genreV = new String[]{
            "/category/%D0%B0%D1%80%D1%82_.html","/category/%D0%B1%D0%BE%D0%B5%D0%B2%D0%B8%D0%BA_.html","/category/%D0%B1%D0%BE%D0%B5%D0%B2%D1%8B%D0%B5+%D0%B8%D1%81%D0%BA%D1%83%D1%81%D1%81%D1%82%D0%B2%D0%B0_.html"
            ,"/category/%D0%B2%D0%B0%D0%BC%D0%BF%D0%B8%D1%80%D1%8B_.html","/category/%D0%B3%D0%B0%D1%80%D0%B5%D0%BC_.html","/category/%D0%B3%D0%B5%D0%BD%D0%B4%D0%B5%D1%80%D0%BD%D0%B0%D1%8F+%D0%B8%D0%BD%D1%82%D1%80%D0%B8%D0%B3%D0%B0_.html"
            ,"/category/%D0%B3%D0%B5%D1%80%D0%BE%D0%B8%D1%87%D0%B5%D1%81%D0%BA%D0%BE%D0%B5+%D1%84%D1%8D%D0%BD%D1%82%D0%B5%D0%B7%D0%B8_.html","/category/%D0%B4%D0%B5%D1%82%D0%B5%D0%BA%D1%82%D0%B8%D0%B2_.html"
            ,"/category/%D0%B4%D0%B7%D1%91%D1%81%D1%8D%D0%B9_.html","/category/%D0%B4%D0%BE%D0%B4%D0%B7%D0%B8%D0%BD%D1%81%D0%B8_.html","/category/%D0%B4%D1%80%D0%B0%D0%BC%D0%B0_.html"
            ,"/category/%D0%B8%D0%B3%D1%80%D0%B0_.html","/category/%D0%B8%D1%81%D1%82%D0%BE%D1%80%D0%B8%D1%8F_.html","/category/%D0%BA%D0%BE%D0%B4%D0%BE%D0%BC%D0%BE_.html","/category/%D0%BA%D0%BE%D0%BC%D0%B5%D0%B4%D0%B8%D1%8F_.html"
            ,"/category/%D0%BC%D0%B0%D1%85%D0%BE-%D1%81%D1%91%D0%B4%D0%B7%D1%91_.html","/category/%D0%BC%D0%B5%D1%85%D0%B0_.html","/category/%D0%BC%D0%B8%D1%81%D1%82%D0%B8%D0%BA%D0%B0_.html"
            ,"/category/%D0%BD%D0%B0%D1%83%D1%87%D0%BD%D0%B0%D1%8F+%D1%84%D0%B0%D0%BD%D1%82%D0%B0%D1%81%D1%82%D0%B8%D0%BA%D0%B0_.html","/category/%D0%BF%D0%BE%D0%B2%D1%81%D0%B5%D0%B4%D0%BD%D0%B5%D0%B2%D0%BD%D0%BE%D1%81%D1%82%D1%8C_.html"
            ,"/category/%D0%BF%D0%BE%D1%81%D1%82%D0%B0%D0%BF%D0%BE%D0%BA%D0%B0%D0%BB%D0%B8%D0%BF%D1%82%D0%B8%D0%BA%D0%B0_.html","/category/%D0%BF%D1%80%D0%B8%D0%BA%D0%BB%D1%8E%D1%87%D0%B5%D0%BD%D0%B8%D1%8F_.html"
            ,"/category/%D0%BF%D1%81%D0%B8%D1%85%D0%BE%D0%BB%D0%BE%D0%B3%D0%B8%D1%8F_.html","/category/%D1%80%D0%BE%D0%BC%D0%B0%D0%BD%D1%82%D0%B8%D0%BA%D0%B0_.html","/category/%D1%81%D0%B0%D0%BC%D1%83%D1%80%D0%B0%D0%B9%D1%81%D0%BA%D0%B8%D0%B9+%D0%B1%D0%BE%D0%B5%D0%B2%D0%B8%D0%BA_.html"
            ,"/category/%D1%81%D0%B2%D0%B5%D1%80%D1%85%D1%8A%D0%B5%D1%81%D1%82%D0%B5%D1%81%D1%82%D0%B2%D0%B5%D0%BD%D0%BD%D0%BE%D0%B5_.html","/category/%D1%81%D1%91%D0%B4%D0%B7%D1%91_.html","/category/%D1%81%D1%91%D0%B4%D0%B7%D1%91-%D0%B0%D0%B9_.html"
            ,"/category/%D1%81%D1%91%D0%BD%D1%8D%D0%BD_.html","/category/%D1%81%D1%91%D0%BD%D1%8D%D0%BD-%D0%B0%D0%B9_.html","/category/%D1%81%D0%BF%D0%BE%D1%80%D1%82_.html","/category/%D1%81%D1%8D%D0%B9%D0%BD%D1%8D%D0%BD_.html"
            ,"/category/%D1%82%D1%80%D0%B0%D0%B3%D0%B5%D0%B4%D0%B8%D1%8F_.html","/category/%D1%82%D1%80%D0%B8%D0%BB%D0%BB%D0%B5%D1%80_.html","/category/%D1%83%D0%B6%D0%B0%D1%81%D1%8B_.html","/category/%D1%84%D0%B0%D0%BD%D1%82%D0%B0%D1%81%D1%82%D0%B8%D0%BA%D0%B0_.html"
            ,"/category/%D1%84%D1%8D%D0%BD%D1%82%D0%B5%D0%B7%D0%B8_.html","/category/%D1%88%D0%BA%D0%BE%D0%BB%D0%B0_.html","/category/%D1%8D%D1%82%D1%82%D0%B8_.html","/category/%D1%8E%D1%80%D0%B8_.html"
    };

    public RuNineManga() {
        this.setFlag(R.drawable.flag_ru);
        this.setIcon(R.drawable.ninemanga);
        this.setServerName("RuNineManga");
        setServerID(ServerBase.RUNINEMANGA);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String source = getNavWithHeader().get(
                HOST + "/search/?wd=" + URLEncoder.encode(term, "UTF-8"));
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("bookname\" href=\"(/manga/[^\"]+)\">(.+?)<");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(RUNINEMANGA, m.group(2), HOST + m.group(1), false);
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 || forceReload)
            loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        String source = getNavWithHeader().get(manga.getPath() + "?waring=1");
        // Front
        manga.setImages(getFirstMatchDefault("<img itemprop=\"image\".+?src=\"(.+?)\"", source, ""));
        // Summary
        String summary = getFirstMatchDefault("<p itemprop=\"description\">(.+?)</p>",
                source, "нет синопсис").replaceAll("<.+?>", "");
        manga.setSynopsis(Html.fromHtml(summary.replaceFirst("резюме:", "")).toString());
        // Status
        manga.setFinished(false);//not supported by server
        // Author
        manga.setAuthor(getFirstMatchDefault("itemprop=\"author\".+?>(.+?)<", source, ""));
        //Genre
        manga.setGenre((Html.fromHtml(getFirstMatchDefault("<li itemprop=\"genre\".+?</b>(.+?)</li>", source, "").replace("a><a", "a>, <a") + ".").toString().trim()));
        // Chapter
        Pattern p = Pattern.compile(
                "<a class=\"chapter_list_a\" href=\"(/chapter.+?)\" title=\"(.+?)\">(.+?)</a>");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(3), HOST + matcher.group(1)));
        }
        manga.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return chapter.getPath().replace(".html", "-" + page + ".html");
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        if (chapter.getExtra() == null)
            setExtra(chapter);
        String[] images = chapter.getExtra().split("\\|");
        return images[page];
    }

    private void setExtra(Chapter c) throws Exception {
        String source = getNavWithHeader().get(
                c.getPath().replace(".html", "-" + c.getPages() + "-1.html"));
        Pattern p = Pattern.compile("<img class=\"manga_pic.+?src=\"([^\"]+)");
        Matcher m = p.matcher(source);
        String images = "";
        while (m.find()) {
            images = images + "|" + m.group(1);
        }
        c.setExtra(images);
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String source = getNavWithHeader().get(chapter.getPath());
        String nop = getFirstMatch(
                "\\d+/(\\d+)</option>[\\s]*</select>", source,
                "Не удалось получить количество страниц");
        chapter.setPages(Integer.parseInt(nop));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int category, int order, int pageNumber) throws Exception {
        String source = getNavWithHeader().get(
                HOST + genreV[category].replace("_", "_" + pageNumber));
        return getMangasFromSource(source);
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile(
                "<a href=\"(/manga/[^\"]+)\"><img src=\"(.+?)\".+?alt=\"([^\"]+)\"");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(RUNINEMANGA, m.group(3), HOST + m.group(1), false);
            manga.setImages(m.group(2));
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
        return new String[]{"жанр"};
    }

    @Override
    public boolean hasList() {
        return false;
    }
}
