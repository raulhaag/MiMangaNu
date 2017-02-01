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

/**
 * Created by Raul on 17/07/2016.
 */
class LeoManga extends ServerBase {

    public static String HOST = "leomanga.com";

    private static String[] demografia = {
            "Todo", "Shonen", "Shojo", "Josei", "Seinen", "Kodomo", "Yuri"
    };

    private static String[] estado = {
            "Todo", "Finalizado", "En curso"
    };

    private static String[] estadoV = {
            "", "&estado=finalizado", "&estado=en-curso"
    };

    private static String[] demografiaV = {
            "", "&demografia=shonen", "&demografia=shojo", "&demografia=josei",
            "&demografia=seinen", "&demografia=kodomo", "&demografia=yuri"
    };

    private static String[] genres = {
            "Acción", "Artes Marciales", "Aventura", "Ciencia Ficción", "Comedia",
            "Deporte", "Doujinshi", "Drama", "Ecchi", "Escolar", "Fantasía", "Gender Bender", "Gore", "Harem", "Histórico", "Horror",
            "Lolicon", "Magia", "Mecha", "Misterio", "Musical", "One-Shot", "Parodia", "Policíaca", "Psicológica", "Romance", "Shojo Ai",
            "Slice of Life", "Smut", "Sobrenatural", "Superpoderes", "Tragedia"
    };
    private static String[] categoriasV = {
            "accion", "artes-marciales", "aventura", "ciencia-ficcion", "comedia", "deporte",
            "doujinshi", "drama", "ecchi", "escolar", "fantasia", "gender-bender", "gore", "harem",
            "historico", "horror", "lolicon", "magia", "mecha", "misterio", "musical", "one-shot",
            "parodia", "policiaca", "psicologica", "romance", "shojo-ai", "slice-of-life", "smut",
            "sobrenatural", "Superpoderes", "tragedia"
    };
    private static String[] orden = {
            "Lecturas", "Alfabetico", "Valoración", "Fecha"
    };
    private static String[] ordenM = {
            "", "orden=alfabetico", "orden=valoracion", "orden=fecha"
    };
    private static String[] estilo = {
            "Todos", "Manga", "Manhwa", "Manhua"
    };
    private static String[] estiloV = {
            "Todos", "Manga", "Manhwa", "Manhua"
    };

    LeoManga(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_es);
        this.setIcon(R.drawable.leomanga);
        this.setServerName("LeoManga");
        setServerID(ServerBase.LEOMANGA);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String web = "http://" + HOST + "/buscar?s=" + URLEncoder.encode(term, "UTF-8");
        String data = getNavigatorAndFlushParameters().get(web);
        Pattern pattern = Pattern.compile("<td onclick='window.location=\"(.+?)\"'>.+?<img src=\"(.+?)\"[^>]alt=\"(.+?)\"");
        Matcher m = pattern.matcher(data);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()) {
            mangas.add(new Manga(LEOMANGA, m.group(3), "http://" + HOST + m.group(1), false));
        }
        return mangas;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().size() == 0 || forceReload) {

            String data = getNavigatorAndFlushParameters().get(manga.getPath());
            manga.setSynopsis(getFirstMatchDefault("<p class=\"text-justify\">(.+?)</p>", data, defaultSynopsis));
            String image = getFirstMatchDefault("<img data-original=\"(.+?)\"", data, "");
            if (image.length() > 4) {
                manga.setImages("http://" + HOST + image);
            } else {
                manga.setImages("");
            }
            manga.setAuthor(getFirstMatch("<a href=\"/autor.+?\">(.+?)<", data, "n/a"));
            manga.setGenre(getFirstMatch("Géneros:.+?</div>(.+?)</div>", data, "").replaceAll("<.*?>", "").replaceAll(",[\\s]*", ",").trim());
            manga.setFinished(getFirstMatchDefault("curs-state\">(.+?)<", data, "").contains("Finalizado"));

            ArrayList<Chapter> chapters = new ArrayList<>();
            Pattern pattern = Pattern.compile("<li>[\\s]*<a href=\"(/manga/.+?)\">(.+?)</a>");
            Matcher matcher = pattern.matcher(data);
            while (matcher.find()) {
                chapters.add(0,new Chapter(matcher.group(2).replaceAll("<.+?>", "").trim(), "http://" + HOST + matcher.group(1)));
            }
            manga.setChapters(chapters);
        }
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return null;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        return chapter.getExtra().split("\\|")[page];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data = getNavigatorAndFlushParameters().get(chapter.getPath());
        String web = "http://" + HOST + getFirstMatch("href=\"([^\"]+)\">Online", data, "Error obteniendo paginas 1");
        data = getNavigatorAndFlushParameters().get(web);
        String sub = "http://" + HOST + getFirstMatch("id=\"read-chapter\" data-name=\"(.+?)\"", data, "Error obteniendo paginas 3");
        String[] pos = getFirstMatch("pos=\"(.+?)\"", data, "Error obteniendo paginas 4").split(";");
        chapter.setPages(pos.length);
        String images = "";
        for (String i : pos) {
            images = images + "|" + sub + i;
        }
        chapter.setExtra(images);
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = "http://" + HOST + "/directorio-manga?pagina=" + pageNumber;
        //demografia
        web = web + demografiaV[filters[0][0]];
        //genero
        String gen = "&genero=";
        for (int i = 0; i < filters[1].length; i++) {
            gen = gen + categoriasV[filters[1][i]] + "|";
        }
        if (gen.length() > 8) {
            gen = gen.substring(0, gen.length() - 1);
            web = web + gen;
        }

        //estado
        web = web + estadoV[filters[2][0]];
        //orden
        web = web + ordenM[filters[3][0]];
        String data = getNavigatorAndFlushParameters().get(web);
        return getMangasFromSource(data);
    }

    private ArrayList<Manga> getMangasFromSource(String data) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("<a href=\"(/manga/.+?)\".+?src=\"(.+?)\" alt=\"(.+?)\"");
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(LEOMANGA, m.group(3), "http://" + HOST + m.group(1), false);
            manga.setImages("http://" + HOST + m.group(2).replace("thumb-", ""));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{new ServerFilter("Demografia", demografia, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Genero", genres, ServerFilter.FilterType.MULTI),
                new ServerFilter("Estado", estado, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Orden", orden, ServerFilter.FilterType.SINGLE)
        };
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public boolean needRefererForImages() {
        return false;
    }
}
