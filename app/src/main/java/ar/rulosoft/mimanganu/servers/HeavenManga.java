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

class HeavenManga extends ServerBase {

    private static String[] generos = new String[]{
            "Todo", "Accion", "Adulto", "Aventura", "Artes Marciales",
            "Acontesimientos de la Vida", "Bakunyuu", "Sci-fi",
            "Comic", "Combate", "Comedia", "Cooking", "Cotidiano", "Colegialas",
            "Critica social", "Ciencia ficcion", "Cambio de genero", "Cosas de la Vida",
            "Drama", "Deporte", "Doujinshi", "Delincuentes", "Ecchi", "Escolar",
            "Erotico", "Escuela", "Estilo de Vida", "Fantasia", "Fragmentos de la Vida",
            "Gore", "Gender Bender", "Humor", "Harem", "Haren", "Hentai", "Horror",
            "Historico", "Josei", "Loli", "Light", "Lucha Libre", "Manga", "Mecha",
            "Magia", "Maduro", "Manhwa", "Manwha", "Mature", "Misterio", "Mutantes",
            "Novela", "Orgia", "OneShot", "OneShots", "Psicologico", "Romance",
            "Recuentos de la vida", "Smut", "Shojo", "Shonen", "Seinen", "Shoujo",
            "Shounen", "Suspenso", "School Life", "Sobrenatural", "SuperHeroes",
            "Supernatural", "Slice of Life", "Super Poderes", "Terror", "Torneo",
            "Tragedia", "Transexual", "Vida", "Vampiros", "Violencia", "Vida Pasada",
            "Vida Cotidiana", "Vida de Escuela", "Webtoon", "Webtoons", "Yuri"
    };
    private static String[] generosV = new String[]{
            "/top", "/genero/accion.html", "/genero/adulto.html", "/genero/aventura.html",
            "/genero/artes+marciales.html", "/genero/acontesimientos+de+la+vida.html",
            "/genero/bakunyuu.html", "/genero/sci-fi.html", "/genero/comic.html",
            "/genero/combate.html", "/genero/comedia.html", "/genero/cooking.html",
            "/genero/cotidiano.html", "/genero/colegialas.html", "/genero/critica+social.html",
            "/genero/ciencia+ficcion.html", "/genero/cambio+de+genero.html",
            "/genero/cosas+de+la+vida.html", "/genero/drama.html", "/genero/deporte.html",
            "/genero/doujinshi.html", "/genero/delincuentes.html", "/genero/ecchi.html",
            "/genero/escolar.html", "/genero/erotico.html", "/genero/escuela.html",
            "/genero/estilo+de+vida.html", "/genero/fantasia.html",
            "/genero/fragmentos+de+la+vida.html", "/genero/gore.html",
            "/genero/gender+bender.html", "/genero/humor.html", "/genero/harem.html",
            "/genero/haren.html", "/genero/hentai.html", "/genero/horror.html",
            "/genero/historico.html", "/genero/josei.html", "/genero/loli.html",
            "/genero/light.html", "/genero/lucha+libre.html", "/genero/manga.html",
            "/genero/mecha.html", "/genero/magia.html", "/genero/maduro.html",
            "/genero/manhwa.html", "/genero/manwha.html", "/genero/mature.html",
            "/genero/misterio.html", "/genero/mutantes.html", "/genero/novela.html",
            "/genero/orgia.html", "/genero/oneshot.html", "/genero/oneshots.html",
            "/genero/psicologico.html", "/genero/romance.html",
            "/genero/recuentos+de+la+vida.html", "/genero/smut.html", "/genero/shojo.html",
            "/genero/shonen.html", "/genero/seinen.html", "/genero/shoujo.html",
            "/genero/shounen.html", "/genero/suspenso.html", "/genero/school+life.html",
            "/genero/sobrenatural.html", "/genero/superheroes.html", "/genero/supernatural.html",
            "/genero/slice+of+life.html", "/genero/ssuper+poderes.html",
            "/genero/terror.html", "/genero/torneo.html", "/genero/tragedia.html",
            "/genero/transexual.html", "/genero/vida.html", "/genero/vampiros.html",
            "/genero/violencia.html", "/genero/vida+pasada.html", "/genero/vida+cotidiana.html",
            "/genero/vida+de+escuela.html", "/genero/webtoon.html",
            "/genero/webtoons.html", "/genero/yuri.html"
    };

    HeavenManga(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_es);
        this.setIcon(R.drawable.heavenmanga);
        this.setServerName("HeavenManga");
        setServerID(ServerBase.HEAVENMANGACOM);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        String source = getNavigatorAndFlushParameters().get("https://heavenmanga.com/");
        source = getFirstMatch("<span>Lista Completa(.+)", source, "Error al obtener la lista");
        Pattern p = Pattern.compile("<li class=\"rpwe-clearfix\"><a href=\"(.+?)\" title=\"(.+?)\"", Pattern.DOTALL);
        Matcher m = p.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()) {
            mangas.add(new Manga(HEAVENMANGACOM, m.group(2), m.group(1), true));
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String source = getNavigatorAndFlushParameters().get("https://heavenmanga.com/buscar?query=" + URLEncoder.encode(term, "UTF-8"));
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("<h4><a href=\"([^\"]+)\">([^<]+)<.a><.h4>", Pattern.DOTALL);
        Matcher matcher = p.matcher(source);
        while (matcher.find()) {
            mangas.add(new Manga(HEAVENMANGACOM, matcher.group(2), matcher.group(1), false));
        }
        return mangas;    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 || forceReload)
            loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        String source = getNavigatorAndFlushParameters().get(manga.getPath());
        // portada
        manga.setImages(getFirstMatchDefault("data-src='([^']+)", source, ""));

        // sinopsis
        manga.setSynopsis(getFirstMatchDefault("summary__content\">\\s*<p>([\\s\\S]+?)<\\/p>", source, context.getString(R.string.nodisponible)));

        // estado no soportado

        // autor no soportado
        manga.setAuthor(context.getString(R.string.nodisponible));

        // genero
        manga.setGenre(getFirstMatchDefault("genres-content\">([\\s\\S]+?)<\\/div>", source, context.getString(R.string.nodisponible)));

        // capitulos
        Pattern p = Pattern.compile("c_title[\\s\\S]+?<\\/i>([^<]+)[\\s\\S]+?window.location='([^']+)", Pattern.DOTALL);
        Matcher matcher = p.matcher(source);
        while (matcher.find()) {
            manga.addChapterFirst(new Chapter(matcher.group(1), matcher.group(2)));
        }
        if (manga.getChapters().isEmpty()) {
            throw new Exception(context.getString(R.string.server_failed_loading_chapter));
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        return chapter.getExtra().split("\\|")[(page - 1)];
    }

    @Override
    public boolean needRefererForImages() {
        return true;
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getPages() == 0) {
            if (chapter.getExtra() == null || chapter.getExtra().isEmpty()) {
                String source = getNavigatorAndFlushParameters().get(chapter.getPath());
                String web = getFirstMatch(
                        "id=\"leer\".+?href=\"([^\"]+)", source,
                        context.getString(R.string.server_failed_loading_chapter));
                chapter.setExtra(web);
            }
            String source = getNavigatorAndFlushParameters().get(chapter.getExtra());
            ArrayList<String> images = getAllMatch("\"imgURL\":\"([^\"]+)", source);
            if(images.get(0).contains("data:image/jpg;base64")){
                throw new Exception("Base 64 Image, proceso parado para prevenir la corrupción de la base de datos.");
            }
            chapter.setExtra(TextUtils.join("|", images));
            chapter.setPages(images.size());

        }
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("<div class=\"manga-name\">([^<]+)[\\s\\S]+?href=\"([^\"]+)[\\s\\S]+?<img src='([^']+)", Pattern.DOTALL);
        Matcher matcher = p.matcher(source);
        while (matcher.find()) {
            mangas.add(new Manga(HEAVENMANGACOM, matcher.group(1), matcher.group(2), matcher.group(3)));
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] categories, int pageNumber) throws Exception {
        ArrayList<Manga> mangas = null;
        String web = "";
        if (categories[0][0] == 0 ) {
            web = "/top?orderby=alphabet&page=" + pageNumber;
        } else {
            web = generosV[categories[0][0]] + "?orderby=alphabet&page=" + pageNumber;
        }
        if (web.length() > 2) {
            String source = getNavigatorAndFlushParameters().get("https://heavenmanga.com" + web);
            mangas = getMangasFromSource(source);
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{new ServerFilter("Generos", generos, ServerFilter.FilterType.SINGLE)};
    }

    @Override
    public boolean hasList() {
        return false;
    }

}