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
 * Created by Raul on 04/12/2015.
 */
class MyMangaIo extends ServerBase {

    private static final String HOST = "http://www.mymanga.io/";

    private static final int[] fltDemographic = {
            R.string.flt_tag_josei,
            R.string.flt_tag_seinen,
            R.string.flt_tag_shoujo,
            R.string.flt_tag_shounen,
    };

    private static final String[] valDemographic = {
            "&genre%5B4%5D=1",
            "&genre%5B2%5D=1",
            "&genre%5B3%5D=1",
            "&genre%5B1%5D=1",
    };

    private static final int[] fltType = {
            R.string.flt_tag_doujinshi,
            R.string.flt_tag_magazine,
            R.string.flt_tag_manfra,
            R.string.flt_tag_manga,
            R.string.flt_tag_manhua,
            R.string.flt_tag_manhwa,
    };
    private static final String[] valType = {
            "&type%5B3%5D=1",
            "&type%5B5%5D=1",
            "&type%5B6%5D=1",
            "&type%5B1%5D=1",
            "&type%5B4%5D=1",
            "&type%5B2%5D=1",
    };

    private static final int[] fltStatus = {
            R.string.flt_status_abandoned, //Abandonné
            R.string.flt_status_ongoing, //En cours
            R.string.flt_status_one_shot, //One shot
            R.string.flt_status_completed, //Terminé
    };
    private static final String[] valStatus = {
            "&statut%5B4%5D=1",
            "&statut%5B3%5D=1",
            "&statut%5B1%5D=1",
            "&statut%5B2%5D=1",
    };

    private static final int[] fltGenre = {
            R.string.flt_tag_action, //Action
            R.string.flt_tag_adult, //Adulte
            R.string.flt_tag_martial_arts, //Arts martiaux
            R.string.flt_tag_adventure, //Aventure
            R.string.flt_tag_comedy, //Comédie
            R.string.flt_tag_drama, //Drame
            R.string.flt_tag_ecchi, //Ecchi
            R.string.flt_tag_fantasy, //Fantaisie
            R.string.flt_tag_harem, //Harem
            R.string.flt_tag_historical, //Historique
            R.string.flt_tag_horror, //Horreur
            R.string.flt_tag_lolicon, //Lolicon
            R.string.flt_tag_mature, //Mature
            R.string.flt_tag_mecha, //Mecha
            R.string.flt_tag_mystery, //Mystère
            R.string.flt_tag_perverted, //Pervers
            R.string.flt_tag_psychological, //Psychologique
            R.string.flt_tag_romance, //Romance
            R.string.flt_tag_sci_fi, //Science fiction
            R.string.flt_tag_shotacon, //Shotacon
            R.string.flt_tag_sports, //Sports
            R.string.flt_tag_supernatural, //Surnaturel
            R.string.flt_tag_tragedy, //Tragédie
            R.string.flt_tag_slice_of_life, //Tranche de vie
            R.string.flt_tag_travel, //Travelo
            R.string.flt_tag_school_life, //Vie scolaire
            R.string.flt_tag_yaoi, //Yaoi
            R.string.flt_tag_yuri, //Yuri
    };
    private static final String[] valGenre = {
            "&subgenre%5B4%5D=1",
            "&subgenre%5B5%5D=1",
            "&subgenre%5B23%5D=1",
            "&subgenre%5B3%5D=1",
            "&subgenre%5B10%5D=1",
            "&subgenre%5B12%5D=1",
            "&subgenre%5B2%5D=1",
            "&subgenre%5B20%5D=1",
            "&subgenre%5B25%5D=1",
            "&subgenre%5B19%5D=1",
            "&subgenre%5B6%5D=1",
            "&subgenre%5B30%5D=1",
            "&subgenre%5B26%5D=1",
            "&subgenre%5B15%5D=1",
            "&subgenre%5B22%5D=1",
            "&subgenre%5B8%5D=1",
            "&subgenre%5B13%5D=1",
            "&subgenre%5B1%5D=1",
            "&subgenre%5B21%5D=1",
            "&subgenre%5B29%5D=1",
            "&subgenre%5B17%5D=1",
            "&subgenre%5B9%5D=1",
            "&subgenre%5B18%5D=1",
            "&subgenre%5B16%5D=1",
            "&subgenre%5B27%5D=1",
            "&subgenre%5B11%5D=1",
            "&subgenre%5B28%5D=1",
            "&subgenre%5B24%5D=1",
    };

    MyMangaIo(Context context) {
        super(context);
        setFlag(R.drawable.flag_fr);
        setIcon(R.drawable.mymanga);
        setServerName("MyMangaIo");
        setServerID(MYMANGAIO);
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return getMangasFiltered(getBasicFilter(), 0);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        loadChapters(manga, forceReload);
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String data = getNavigatorWithNeededHeader().get(manga.getPath());
            // Cover
            manga.setImages(HOST + getFirstMatchDefault("<img src=\"(images/mangas_thumb/.+?)\"", data, ""));
            // Summary
            manga.setSynopsis(getFirstMatchDefault("Synopsis</h1>\\s+<p><[^>]+>(.+?)</p>", data, context.getString(R.string.nodisponible)));
            // Status
            manga.setFinished(!data.contains("en cours</a>"));
            // Author
            manga.setAuthor(getFirstMatchDefault("Auteur\\s*:\\s*(.+?)</tr>", data, context.getString(R.string.nodisponible)));
            // Genre
            manga.setGenre(getFirstMatchDefault("Sous-Genres\\s*:\\s*(.+?)</tr>", data, context.getString(R.string.nodisponible)));
            // Chapter
            Pattern p = Pattern.compile("href=\"([^\"]+)\"[^>]+title=\"Li.+?<span class=\"chapter\">(.+?)<", Pattern.DOTALL);
            Matcher m = p.matcher(data);
            while (m.find()) {
                manga.addChapterFirst(new Chapter(m.group(2).trim(), m.group(1)));
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String data = getNavigatorWithNeededHeader().get(chapter.getPath() + page);
        return getFirstMatch(
                "<img src=\"(http[^\"]+)\"", data,
                context.getString(R.string.server_failed_loading_image));
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if(chapter.getPages() == 0) {
            String source = getNavigatorWithNeededHeader().get(chapter.getPath());
            String pages = getFirstMatch(
                    "<span>sur (\\d+)</span>", source,
                    context.getString(R.string.server_failed_loading_page_count));
            chapter.setPages(Integer.parseInt(pages));
        }
    }

    private ArrayList<Manga> getMangasFromSource(String source) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("<a href=\"(mangas/[^\"]+?)\">(.+?)<", Pattern.DOTALL);
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(getServerID(), Util.getInstance().fromHtml(m.group(2)).toString(), HOST + m.group(1), false);
            manga.setImages(HOST + "/images/mangas_thumb/" + getFirstMatchDefault("mangas/(.+?)/", manga.getPath(), "") + ".jpg");
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String web = HOST + "/search?name=" + URLEncoder.encode(term, "UTF-8");
        String data = getNavigatorWithNeededHeader().get(web);
        return getMangasFromSource(data);
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = HOST + "/search?name=&author=&illustrator=&parution_span=0&parution=";
        for (int i = 0; i < filters[0].length; i++) {
            web = web + valDemographic[filters[0][i]];
        }
        for (int i = 0; i < filters[1].length; i++) {
            web = web + valStatus[filters[1][i]];
        }
        for (int i = 0; i < filters[2].length; i++) {
            web = web + valType[filters[2][i]];
        }
        for (int i = 0; i < filters[3].length; i++) {
            web = web + valGenre[filters[3][i]];
        }
        Navigator nav = getNavigatorWithNeededHeader();
        web = web + "&chapter_span=0&chapter_count=&last_update=&like_span=0&like=&dislike_span=0&dislike=&search=Rechercher";
        String source = nav.get(web);
        return getMangasFromSource(source);
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(
                        context.getString(R.string.flt_demographic),
                        buildTranslatedStringArray(fltDemographic), ServerFilter.FilterType.MULTI),
                new ServerFilter(
                        context.getString(R.string.flt_status),
                        buildTranslatedStringArray(fltStatus), ServerFilter.FilterType.MULTI),
                new ServerFilter(
                        context.getString(R.string.flt_type),
                        buildTranslatedStringArray(fltType), ServerFilter.FilterType.MULTI),
                new ServerFilter(
                        context.getString(R.string.flt_genre),
                        buildTranslatedStringArray(fltGenre), ServerFilter.FilterType.MULTI)
        };
    }

    @Override
    public boolean needRefererForImages() {
        return false;
    }

    private Navigator getNavigatorWithNeededHeader() throws Exception {
        Navigator nav = Navigator.getInstance();
        nav.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        return nav;
    }
}
