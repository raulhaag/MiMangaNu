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
            "", "/genero/accion.html", "/genero/adulto.html", "/genero/aventura.html",
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
    private static String[] paginas = new String[]{
            "0-9.html", "a.html", "b.html", "c.html", "d.html", "e.html",
            "f.html", "g.html", "h.html", "i.html", "j.html", "k.html", "l.html", "m.html",
            "n.html", "o.html", "p.html", "q.html", "r.html", "s.html", "t.html", "u.html",
            "v.html", "w.html", "x.html", "y.html", "z.html"
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
        String source = getNavigatorAndFlushParameters().get("http://heavenmanga.com/");
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
        String source = getNavigatorAndFlushParameters().get("http://heavenmanga.com/buscar/" + URLEncoder.encode(term, "UTF-8") + ".html");
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
        // portada
        manga.setImages(getFirstMatchDefault("<meta property=\"og:image\" content=\"(.+?)\"", source, ""));

        // sinopsis
        manga.setSynopsis(getFirstMatchDefault("<div class=\"sinopsis\">(.+?)<div", source, context.getString(R.string.nodisponible)));

        // estado no soportado

        // autor no soportado
        manga.setAuthor(context.getString(R.string.nodisponible));

        // genero
        manga.setGenre(getFirstMatchDefault("nero\\(s\\) :(.+?)</div>", source, context.getString(R.string.nodisponible)));

        // capitulos
        Pattern p = Pattern.compile("<li><span class=\"capfec\">.+?><a href=\"(http://heavenmanga.com/.+?)\" title=\"(.+?)\"", Pattern.DOTALL);
        Matcher matcher = p.matcher(source);
        while (matcher.find()) {
            manga.addChapterFirst(new Chapter(matcher.group(2), matcher.group(1)));
        }
        if (manga.getChapters().isEmpty()) {
            throw new Exception(context.getString(R.string.server_failed_loading_chapter));
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        assert chapter.getExtra() != null;
        String web = getNavigatorAndFlushParameters().get(chapter.getExtra().substring(0, chapter.getExtra().lastIndexOf("/")) + "/" + page);
        String source = getFirstMatch("<center>([\\s\\S]+)<center>", web,
                context.getString(R.string.server_failed_loading_image));
        return getFirstMatch(
                "<img src=\"([^\"]+)", source,
                context.getString(R.string.server_failed_loading_image));
    }

    @Override
    public boolean needRefererForImages() {
        return false;
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if(chapter.getPages() == 0) {
            if (chapter.getExtra() == null) {
                String source = getNavigatorAndFlushParameters().get(chapter.getPath());
                String web = getFirstMatch(
                        "<a id=\"l\" href=\"(http://heavenmanga.com/.+?)\"><b>Leer</b>", source,
                        context.getString(R.string.server_failed_loading_chapter));
                chapter.setExtra(web);
            }
            String source = getNavigatorAndFlushParameters().get(chapter.getExtra());
            String nop = getFirstMatch(
                    "(\\d+)</option></select>", source,
                    context.getString(R.string.server_failed_loading_page_count));
            chapter.setPages(Integer.parseInt(nop));
        }
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("<article class=\"rel\"><a href=\"(http://heavenmanga.com/.+?)\"><header>(.+?)<.+?src=\"(.+?)\"", Pattern.DOTALL);
        Matcher matcher = p.matcher(source);
        while (matcher.find()) {
            mangas.add(new Manga(HEAVENMANGACOM, matcher.group(2), matcher.group(1), matcher.group(3)));
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] categorie, int pageNumber) throws Exception {
        int paginaLoc = pageNumber - 1;
        ArrayList<Manga> mangas = null;
        String web = "";
        if (categorie[0][0] == 0 && paginaLoc < paginas.length) {
            web = "/letra/" + paginas[paginaLoc];
        } else if (paginaLoc < 1) {
            web = generosV[categorie[0][0]];
        }
        if (web.length() > 2) {
            String source = getNavigatorAndFlushParameters().get("http://heavenmanga.com" + web);
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
        return true;
    }

}