package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.text.TextUtils;

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
import okhttp3.MultipartBody;

/**
 * Created by Raúl on 15/07/2017.
 */
class Mangapedia extends ServerBase {

    private static final String HOST = "http://mangapedia.fr/";

    private static final int[] fltDemographic = {
            R.string.flt_tag_shounen,
            R.string.flt_tag_seinen,
            R.string.flt_tag_shounen_ai,
            R.string.flt_tag_josei,
            R.string.flt_tag_shoujo,
            R.string.flt_tag_shoujo_ai,
            R.string.flt_tag_yaoi,
            R.string.flt_tag_yuri,
    };
    private static final String[] valDemographic = {
            "typeID[1]",
            "typeID[2]",
            "typeID[3]",
            "typeID[4]",
            "typeID[5]",
            "typeID[6]",
            "typeID[7]",
            "typeID[8]",
    };

    private static final int[] fltGenre = {
            R.string.flt_tag_action, //Action
            R.string.flt_tag_adventure, //Aventure
            R.string.flt_tag_comedy, //Comédie
            R.string.flt_tag_drama, //Drame
            R.string.flt_tag_fantasy, //Fantasie
            R.string.flt_tag_martial_arts, //Arts Martiaux
            R.string.flt_tag_mystery, //Mystères
            R.string.flt_tag_supernatural, //Surnaturel
            R.string.flt_tag_adult, //Adulte
            R.string.flt_tag_doujinshi, //Doujinshi
            R.string.flt_tag_ecchi, //Ecchi
            R.string.flt_tag_harem, //Harem
            R.string.flt_tag_historical, //Historique
            R.string.flt_tag_horror, //Horreur
            R.string.flt_tag_mature, //Mature
            R.string.flt_tag_mecha, //Mecha
            R.string.flt_tag_one_shot, //One Shot
            R.string.flt_tag_psychological, //Psychologie
            R.string.flt_tag_romance, //Romance
            R.string.flt_tag_school_life, //School Life
            R.string.flt_tag_sci_fi, //Sci-Fi
            R.string.flt_tag_slice_of_life, //Tranche de vie
            R.string.flt_tag_sports, //Sports
            R.string.flt_tag_tragedy, //Tragédie
    };
    private static final String[] valGenre = {
            "categoryID[1]",
            "categoryID[2]",
            "categoryID[3]",
            "categoryID[4]",
            "categoryID[5]",
            "categoryID[6]",
            "categoryID[7]",
            "categoryID[8]",
            "categoryID[9]",
            "categoryID[10]",
            "categoryID[11]",
            "categoryID[12]",
            "categoryID[13]",
            "categoryID[14]",
            "categoryID[15]",
            "categoryID[16]",
            "categoryID[17]",
            "categoryID[18]",
            "categoryID[19]",
            "categoryID[20]",
            "categoryID[21]",
            "categoryID[22]",
            "categoryID[23]",
            "categoryID[24]",
    };

    private static final int[] fltType = {
            R.string.flt_tag_manga, //Manga
            R.string.flt_tag_manhwa, //Manhwa
            R.string.flt_tag_manhua, //Manhua
            R.string.flt_tag_webcomic, //Webcomics
    };
    private static final String[] valType = {
            "typeBookID[1]",
            "typeBookID[2]",
            "typeBookID[4]",
            "typeBookID[5]"
    };

    private static final int[] fltSortBy = {
            R.string.flt_order_default, //Default
            R.string.flt_order_title, //Titre
            R.string.flt_order_author, //Auteur
            R.string.flt_order_artist, //Illustrateur
            R.string.flt_order_views, //Vues
            R.string.flt_order_last_update, //Derniere MAJ
    };
    private static final String[] valSortBy = {
            "0",
            "1",
            "2",
            "3",
            "4",
            "5"
    };

    private static final int[] fltSortOrder = {
            R.string.flt_order_default, //Default
            R.string.flt_order_ascending, //Croissant
            R.string.flt_order_descending, //Décroissant
    };
    private static final String[] valSortOrder = {
            "0",
            "1",
            "2"
    };

    Mangapedia(Context context) {
        super(context);
        setFlag(R.drawable.flag_fr);
        setIcon(R.drawable.mangapedia);
        setServerName("Mangapedia");
        setServerID(MANGAPEDIA);
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
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = HOST + "/project_code/script/moreMangas.php";
        if (pageNumber == 1) {
            web = HOST + "/project_code/script/search.php";
        }

        Navigator nav = getNavigatorAndFlushParameters();
        String boundary = Navigator.getNewBoundary();
        nav.addHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
        nav.addHeader("X-Requested-With", "XMLHttpRequest");
        MultipartBody.Builder mBodyBuilder = new MultipartBody.Builder(boundary).setType(MultipartBody.FORM);
        mBodyBuilder.addFormDataPart("artist", "");
        mBodyBuilder.addFormDataPart("searchType", "advance");
        mBodyBuilder.addFormDataPart("pageNumber", "" + pageNumber);
        mBodyBuilder.addFormDataPart("searchTerm", "");
        mBodyBuilder.addFormDataPart("searchByLetter", "");
        for (int i = 0; i < valDemographic.length; i++) {
            if (Util.getInstance().contains(filters[0], i)) {
                mBodyBuilder.addFormDataPart(valDemographic[i], "1");
            } else {
                mBodyBuilder.addFormDataPart(valDemographic[i], "0");
            }
        }
        for (int i = 0; i < valGenre.length; i++) {
            if (Util.getInstance().contains(filters[1], i)) {
                mBodyBuilder.addFormDataPart(valGenre[i], "1");
            } else {
                mBodyBuilder.addFormDataPart(valGenre[i], "0");
            }
        }
        for (int i = 0; i < valType.length; i++) {
            if (Util.getInstance().contains(filters[2], i)) {
                mBodyBuilder.addFormDataPart(valType[i], "1");
            } else {
                mBodyBuilder.addFormDataPart(valType[i], "0");
            }
        }
        mBodyBuilder.addFormDataPart("sortBy", valSortBy[filters[3][0]]);
        mBodyBuilder.addFormDataPart("sortOrder", valSortOrder[filters[4][0]]);

        return getMangasString(nav.post(web, mBodyBuilder.build()));
    }

    private ArrayList<Manga> getMangasString(String data) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("<a href=\"([^\"]+)\".+?src=\"([^\"]+)\"\\s*/>.+?>(.+?)<");
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(3), m.group(1), false);
            manga.setImages(m.group(2).replaceAll("-thumb", "") + "|" + HOST + "/mangas");
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(
                        context.getString(R.string.flt_demographic),
                        buildTranslatedStringArray(fltDemographic), ServerFilter.FilterType.MULTI),
                new ServerFilter(
                        context.getString(R.string.flt_genre),
                        buildTranslatedStringArray(fltGenre), ServerFilter.FilterType.MULTI),
                new ServerFilter(
                        context.getString(R.string.flt_type),
                        buildTranslatedStringArray(fltType), ServerFilter.FilterType.MULTI),
                new ServerFilter(
                        context.getString(R.string.flt_order_by),
                        buildTranslatedStringArray(fltSortBy), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_order),
                        buildTranslatedStringArray(fltSortOrder), ServerFilter.FilterType.SINGLE)};
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        return getMangasString(nav.get(HOST + "/mangas/" + URLEncoder.encode(term, "UTF-8").replaceAll(" ", "%25")));
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String data = getNavigatorAndFlushParameters().get(manga.getPath());
            // Cover
            if(manga.getImages() == null) {
                manga.setImages(
                        getFirstMatchDefault("<div id=\"mangaImage\">\\s*<img src=\"([^\"]+)\"", data, "")
                        + "|" + HOST + "/mangas/"
                );
            }
            // Author
            manga.setAuthor(getFirstMatchDefault("Auteur : (.+?)</div>", data, context.getString(R.string.nodisponible)));
            // Genre
            manga.setGenre(getFirstMatchDefault("Sous-genres : (.+?)<\\/div>", data, context.getString(R.string.nodisponible)));
            // Summary
            manga.setSynopsis(getFirstMatchDefault("Synopsis : (.+?)<\\/div>", data, context.getString(R.string.nodisponible)));
            // Status
            manga.setFinished(!getFirstMatchDefault("Statut : (.+?)<\\/div>", data, "en cours").contains("en cours"));
            // Chapters
            Pattern pattern = Pattern.compile("<a href=\"(http[s]*://mangapedia.fr/lel[^\"]+).+?\"nameChapter\">(.+?)<", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(data);
            while (matcher.find()) {
                manga.addChapterFirst(new Chapter(matcher.group(2), matcher.group(1)));
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
        if(chapter.getExtra() == null) {
            String data = getNavigatorAndFlushParameters().get(chapter.getPath());
            ArrayList<String> images = getAllMatch("['|\"](http[s]*://mangapedia.fr/project_code/script/image.php\\?path=.+?)['|\"]", data);

            if (images.isEmpty()) {
                throw new Exception(context.getString(R.string.server_failed_loading_page_count));
            }
            chapter.setExtra(TextUtils.join("|", images));
            chapter.setPages(images.size());
        }
    }

    @Override
    public boolean needRefererForImages() {
        return true;
    }
}
