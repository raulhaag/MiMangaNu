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

/**
 * Created by Raul on 17/07/2016.
 */
class LeoManga extends ServerBase {

    private static final String HOST = "http://leomanga.com";

    private static final int[] fltDemographic = {
            R.string.flt_tag_all, //Todos
            R.string.flt_tag_shounen, //Shonen
            R.string.flt_tag_shoujo, //Shojo
            R.string.flt_tag_josei, //Josei
            R.string.flt_tag_seinen, //Seinen
            R.string.flt_tag_kodomo, //Kodomo
            R.string.flt_tag_yuri, //Yuri
    };
    private static final String[] valDemographic = {
            "",
            "&demografia=shonen",
            "&demografia=shojo",
            "&demografia=josei",
            "&demografia=seinen",
            "&demografia=kodomo",
            "&demografia=yuri"
    };

    private static final int[] fltStatus = {
            R.string.flt_status_all, //Todos
            R.string.flt_status_completed, //Finalizado
            R.string.flt_status_ongoing //En curso
    };
    private static final String[] valStatus = {
            "",
            "&estado=finalizado",
            "&estado=en-curso"
    };

    private static final int[] fltGenre = {
            R.string.flt_tag_action, //Acción
            R.string.flt_tag_martial_arts, //Artes Marciales
            R.string.flt_tag_adventure, //Aventura
            R.string.flt_tag_sci_fi, //Ciencia Ficción
            R.string.flt_tag_comedy, //Comedia
            R.string.flt_tag_sports, //Deporte
            R.string.flt_tag_doujinshi, //Doujinshi
            R.string.flt_tag_drama, //Drama
            R.string.flt_tag_ecchi, //Ecchi
            R.string.flt_tag_school_life, //Escolar
            R.string.flt_tag_fantasy, //Fantasía
            R.string.flt_tag_gender_bender, //Gender Bender
            R.string.flt_tag_gore, //Gore
            R.string.flt_tag_harem, //Harem
            R.string.flt_tag_historical, //Histórico
            R.string.flt_tag_horror, //Horror
            R.string.flt_tag_lolicon, //Lolicon
            R.string.flt_tag_magic, //Magia
            R.string.flt_tag_mecha, //Mecha
            R.string.flt_tag_mystery, //Misterio
            R.string.flt_tag_music, //Musical
            R.string.flt_tag_one_shot, //One-Shot
            R.string.flt_tag_parody, //Parodia
            R.string.flt_tag_police, //Policíaca
            R.string.flt_tag_psychological, //Psicológica
            R.string.flt_tag_romance, //Romance
            R.string.flt_tag_shoujo_ai, //Shojo Ai
            R.string.flt_tag_slice_of_life, //Slice of Life
            R.string.flt_tag_smut, //Smut
            R.string.flt_tag_supernatural, //Sobrenatural
            R.string.flt_tag_super_powers, //Superpoderes
            R.string.flt_tag_tragedy, //Tragedia
    };
    private static final String[] valGenre = {
            "accion",
            "artes-marciales",
            "aventura",
            "ciencia-ficcion",
            "comedia",
            "deporte",
            "doujinshi",
            "drama",
            "ecchi",
            "escolar",
            "fantasia",
            "gender-bender",
            "gore",
            "harem",
            "historico",
            "horror",
            "lolicon",
            "magia",
            "mecha",
            "misterio",
            "musical",
            "one-shot",
            "parodia",
            "policiaca",
            "psicologica",
            "romance",
            "shojo-ai",
            "slice-of-life",
            "smut",
            "sobrenatural",
            "superpoderes",
            "tragedia",
    };
    private static final int[] fltOrder = {
            R.string.flt_order_views, //Lecturas
            R.string.flt_order_alpha, //Alphabetico
            R.string.flt_order_rating, //Valoración
            R.string.flt_order_last_update, //Fecha
    };
    private static final String[] valOrder = {
            "",
            "&orden=alfabetico",
            "&orden=valoracion",
            "&orden=fecha",
    };
    private static final int[] fltType = {
            R.string.flt_tag_all,
            R.string.flt_tag_manga,
            R.string.flt_tag_manhwa,
            R.string.flt_tag_manhua,
    };
    private static final String[] valType = {
            "",
            "&estilo=manga",
            "&estilo=manhwa",
            "&estilo=manhua",
    };

    LeoManga(Context context) {
        super(context);
        setFlag(R.drawable.flag_es);
        setIcon(R.drawable.leomanga);
        setServerName("LeoManga");
        setServerID(LEOMANGA);
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
    public ArrayList<Manga> search(String term) throws Exception {
        String web = HOST + "/buscar?s=" + URLEncoder.encode(term, "UTF-8");
        String data = getNavigatorAndFlushParameters().get(web);
        Pattern pattern = Pattern.compile("<td onclick='window.location=\"(.+?)\"'>.+?<img src=\"(.+?)\"[^>]alt=\"(.+?)\"", Pattern.DOTALL);
        Matcher m = pattern.matcher(data);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(3), HOST + m.group(1), false));
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
            String data = getNavigatorAndFlushParameters().get(manga.getPath());

            // Summary
            manga.setSynopsis(getFirstMatchDefault("<p class=\"text-justify\">(.+?)</p>", data, context.getString(R.string.nodisponible)));

            // Cover
            String image = getFirstMatchDefault("<img data-original=\"(.+?)\"", data, "");
            if (image.length() > 4) {
                manga.setImages(HOST + image);
            } else {
                manga.setImages("");
            }

            // Author
            manga.setAuthor(getFirstMatchDefault("<a href=\"/autor.+?\">(.+?)<", data, context.getString(R.string.nodisponible)));

            // Genre
            manga.setGenre(getFirstMatchDefault("Géneros:.+?</div>(.+?)</div>", data, context.getString(R.string.nodisponible)));

            // Status
            manga.setFinished(getFirstMatchDefault("-state\">(.+?)</div>", data, "").contains("Finalizado"));

            // Chapters
            Pattern pattern = Pattern.compile("<li>[\\s]*<a href=\"(/manga/.+?)\">(.+?)</a>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(data);
            while (matcher.find()) {
                manga.addChapterFirst(new Chapter(matcher.group(2), HOST + matcher.group(1)));
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        assert chapter.getExtra() != null;
        return HOST + chapter.getExtra().split("\\|")[page - 1];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if(chapter.getPages() == 0) {
            String data = getNavigatorAndFlushParameters().get(chapter.getPath());
            String web = HOST + getFirstMatch("href=\"([^\"]+)\">Online", data, "Error: failed to get first indirection");
            data = getNavigatorAndFlushParameters().get(web);
            ArrayList<String> images = getAllMatch("class=\"cap-images\" src=\"(.+?)\"", data);

            if (images.isEmpty()) {
                throw new Exception(context.getString(R.string.server_failed_loading_chapter));
            }
            chapter.setExtra(TextUtils.join("|", images));
            chapter.setPages(images.size());
        }
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web;

        web = HOST + "/directorio-manga?pagina=" + pageNumber;
        web += valType[filters[0][0]];
        web += valDemographic[filters[1][0]];
        if (filters[2].length > 0) {
            String gen = "&genero=";
            for (int i = 0; i < filters[2].length; i++) {
                gen += valGenre[filters[2][i]] + "|";
            }
            web += gen.substring(0, gen.length() - 1);
        }
        web += valStatus[filters[3][0]];
        web += valOrder[filters[4][0]];

        String data = getNavigatorAndFlushParameters().get(web);
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("<a href=\"(/manga/.+?)\".+?src=\"(.+?)\" alt=\"(.+?)\"", Pattern.DOTALL);
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(3), HOST + m.group(1), false);
            manga.setImages(HOST + m.group(2).replace("thumb-", ""));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(
                        context.getString(R.string.flt_type),
                        buildTranslatedStringArray(fltType), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_demographic),
                        buildTranslatedStringArray(fltDemographic), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_genre),
                        buildTranslatedStringArray(fltGenre), ServerFilter.FilterType.MULTI),
                new ServerFilter(
                        context.getString(R.string.flt_status),
                        buildTranslatedStringArray(fltStatus), ServerFilter.FilterType.SINGLE),
                new ServerFilter(
                        context.getString(R.string.flt_order),
                        buildTranslatedStringArray(fltOrder), ServerFilter.FilterType.SINGLE)
        };
    }

    @Override
    public boolean needRefererForImages() {
        return false;
    }
}
