package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;

/**
 * Created by Raul on 29/11/2015.
 */
class RuNineManga extends ServerBase {
    public static String HOST = "http://ru.ninemanga.com";
    private static String[] genre = new String[]{
            "арт", "боевик", "боевыеискусства", "вампиры", "гарем",
            "гендернаяинтрига", "героическоефэнтези", "детектив", "дзёсэй", "додзинси",
            "драма", "игра", "история", "кодомо", "комедия",
            "махо-сёдзё", "меха", "мистика", "научнаяфантастика", "повседневность",
            "постапокалиптика", "приключения", "психология", "романтика", "самурайскийбоевик",
            "сверхъестественное", "сёдзё", "сёдзё-ай", "сёнэн", "сёнэн-ай",
            "спорт", "сэйнэн", "трагедия", "триллер", "ужасы",
            "фантастика", "фэнтези", "школа", "этти", "юри"
    };
    private static String[] genreV = new String[]{
            "90", "53", "58", "85", "73",
            "81", "68", "72", "64", "62",
            "51", "76", "75", "89", "57",
            "88", "84", "71", "79", "65",
            "87", "59", "54", "61", "82",
            "55", "67", "78", "52", "63",
            "69", "74", "70", "83", "86",
            "77", "56", "66", "60", "80"
    };

    private static String[] orderV = {"/list/Hot-Book/", "/list/New-Update/", "/category/", "/list/New-Book/"};
    private static String[] order = {"Топ манга", "Последние", "Каталог манги", "новая книга"};
    private static String[] complete = new String[]{"или", "Да", "Нет"};
    private static String[] completeV = new String[]{"either", "yes", "no"};

    RuNineManga(Context context) {
        super(context);
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
        String source = getNavigatorWithNeededHeader().get(
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
        String source = getNavigatorWithNeededHeader().get(manga.getPath() + "?waring=1");
        // Front
        manga.setImages(getFirstMatchDefault("<img itemprop=\"image\".+?src=\"(.+?)\"", source, ""));
        // Summary
        String summary = getFirstMatchDefault("<p itemprop=\"description\">(.+?)</p>",
                source, defaultSynopsis).replaceAll("<.+?>", "");
        manga.setSynopsis(Util.getInstance().fromHtml(summary.replaceFirst("резюме:", "")).toString());
        // Status
        manga.setFinished(false);//not supported by server
        // Author
        manga.setAuthor(getFirstMatchDefault("itemprop=\"author\".+?>(.+?)<", source, ""));
        // Genre
        manga.setGenre((Util.getInstance().fromHtml(getFirstMatchDefault("<li itemprop=\"genre\".+?</b>(.+?)</li>", source, "").replace("a><a", "a>, <a") + ".").toString().trim()));
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

    private void setExtra(Chapter chapter) throws Exception {
        Navigator nav = getNavigatorWithNeededHeader();
        nav.addHeader("Referer", chapter.getPath());
        nav.get(HOST + "/show_ads/google/");
        String source = nav.get(chapter.getPath().replace(".html", "-" + chapter.getPages() + "-1.html"));
        Pattern p = Pattern.compile("src=\"(http://www\\.mangarussia\\.com/comics/[^\"]+?)\"");
        Matcher m = p.matcher(source);
        String images = "";
        while (m.find()) {
            images = images + "|" + m.group(1);
        }
        chapter.setExtra(images);
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String source = getNavigatorWithNeededHeader().get(chapter.getPath());
        String nop = getFirstMatch("\\d+/(\\d+)</option>[\\s]*</select>", source, "Не удалось получить количество страниц");
        chapter.setPages(Integer.parseInt(nop));
    }

    @Deprecated
    private ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("<a href=\"(/manga/[^\"]+)\"><img src=\"(.+?)\".+?alt=\"([^\"]+)\"");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(RUNINEMANGA, m.group(3), HOST + m.group(1), false);
            manga.setImages(m.group(2));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter("Included Genre(s)", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Excluded Genre(s)", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Completed Series", complete, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Order", order, ServerFilter.FilterType.SINGLE)
        };
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String includedGenres = "";
        if (filters[0].length > 0) {
            for (int i = 0; i < filters[0].length; i++) {
                includedGenres = includedGenres + genreV[filters[0][i]] + "%2C"; // comma
            }
        }
        String excludedGenres = "";
        if (filters[1].length > 0) {
            for (int i = 0; i < filters[1].length; i++) {
                excludedGenres = excludedGenres + genreV[filters[1][i]] + "%2C"; // comma
            }
        }
        String web;
        if (filters[0].length < 1 && filters[1].length < 1)
            web = HOST + orderV[filters[3][0]];
        else
            web = "http://ru.ninemanga.com/search/?name_sel=contain&wd=&author_sel=contain&author=&artist_sel=contain&artist=&category_id=" + includedGenres + "&out_category_id=" + excludedGenres + "&completed_series=" + completeV[filters[2][0]] + "&type=high&page=" + pageNumber + ".html";
        //Log.d("NM","web: "+web);
        String source = getNavigatorWithNeededHeader().get(web);
        // regex to generate genre ids: <li id="cate_.+?" cate_id="(.+?)" cur="none" class="cate_list"><label><a class="sub_clk cirmark">(.+?)</a></label></li>
        Pattern pattern = Pattern.compile("<dl class=\"bookinfo\">.+?href=\"(.+?)\"><img src=\"(.+?)\".+?\">(.+?)<");
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            /*Log.d("NM","(2): "+matcher.group(2));
            Log.d("NM","(1): "+matcher.group(1));*/
            Manga m = new Manga(getServerID(), Util.getInstance().fromHtml(matcher.group(3)).toString(), HOST + matcher.group(1), false);
            m.setImages(matcher.group(2));
            mangas.add(m);
        }
        return mangas;
    }

    public Navigator getNavigatorWithNeededHeader() throws Exception {
        Navigator nav = new Navigator(context);
        nav.addHeader("Accept-Language", "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3");
        return nav;
    }

    @Override
    public boolean hasList() {
        return false;
    }
}
