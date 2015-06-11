package ar.rulosoft.mimanganu.servers;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public class HeavenMangaCom extends ServerBase {

    static String[] generos = new String[]{"Todo", "Accion", "Adulto", "Aventura", "Artes Marciales", "Acontesimientos de la Vida", "Bakunyuu", "Sci-fi",
            "Comic", "Combate", "Comedia", "Cooking", "Cotidiano", "Colegialas", "Critica social", "Ciencia ficcion", "Cambio de genero", "Cosas de la Vida",
            "Drama", "Deporte", "Doujinshi", "Delincuentes", "Ecchi", "Escolar", "Erotico", "Escuela", "Estilo de Vida", "Fantasia", "Fragmentos de la Vida",
            "Gore", "Gender Bender", "Humor", "Harem", "Haren", "Hentai", "Horror", "Historico", "Josei", "Loli", "Light", "Lucha Libre", "Manga", "Mecha",
            "Magia", "Maduro", "Manhwa", "Manwha", "Mature", "Misterio", "Mutantes", "Novela", "Orgia", "OneShot", "OneShots", "Psicologico", "Romance",
            "Recuentos de la vida", "Smut", "Shojo", "Shonen", "Seinen", "Shoujo", "Shounen", "Suspenso", "School Life", "Sobrenatural", "SuperHeroes",
            "Supernatural", "Slice of Life", "Super Poderes", "Terror", "Torneo", "Tragedia", "Transexual", "Vida", "Vampiros", "Violencia", "Vida Pasada",
            "Vida Cotidiana", "Vida de Escuela", "Webtoon", "Webtoons", "Yuri"};
    static String[] generosV = new String[]{"", "/genero/accion.html", "/genero/adulto.html", "/genero/aventura.html", "/genero/artes+marciales.html",
            "/genero/acontesimientos+de+la+vida.html", "/genero/bakunyuu.html", "/genero/sci-fi.html", "/genero/comic.html", "/genero/combate.html",
            "/genero/comedia.html", "/genero/cooking.html", "/genero/cotidiano.html", "/genero/colegialas.html", "/genero/critica+social.html",
            "/genero/ciencia+ficcion.html", "/genero/cambio+de+genero.html", "/genero/cosas+de+la+vida.html", "/genero/drama.html", "/genero/deporte.html",
            "/genero/doujinshi.html", "/genero/delincuentes.html", "/genero/ecchi.html", "/genero/escolar.html", "/genero/erotico.html",
            "/genero/escuela.html", "/genero/estilo+de+vida.html", "/genero/fantasia.html", "/genero/fragmentos+de+la+vida.html", "/genero/gore.html",
            "/genero/gender+bender.html", "/genero/humor.html", "/genero/harem.html", "/genero/haren.html", "/genero/hentai.html", "/genero/horror.html",
            "/genero/historico.html", "/genero/josei.html", "/genero/loli.html", "/genero/light.html", "/genero/lucha+libre.html", "/genero/manga.html",
            "/genero/mecha.html", "/genero/magia.html", "/genero/maduro.html", "/genero/manhwa.html", "/genero/manwha.html", "/genero/mature.html",
            "/genero/misterio.html", "/genero/mutantes.html", "/genero/novela.html", "/genero/orgia.html", "/genero/oneshot.html", "/genero/oneshots.html",
            "/genero/psicologico.html", "/genero/romance.html", "/genero/recuentos+de+la+vida.html", "/genero/smut.html", "/genero/shojo.html",
            "/genero/shonen.html", "/genero/seinen.html", "/genero/shoujo.html", "/genero/shounen.html", "/genero/suspenso.html", "/genero/school+life.html",
            "/genero/sobrenatural.html", "/genero/superheroes.html", "/genero/supernatural.html", "/genero/slice+of+life.html", "/genero/ssuper+poderes.html",
            "/genero/terror.html", "/genero/torneo.html", "/genero/tragedia.html", "/genero/transexual.html", "/genero/vida.html", "/genero/vampiros.html",
            "/genero/violencia.html", "/genero/vida+pasada.html", "/genero/vida+cotidiana.html", "/genero/vida+de+escuela.html", "/genero/webtoon.html",
            "/genero/webtoons.html", "/genero/yuri.html"};
    static String[] paginas = new String[]{"/letra/0-9.html", "/letra/a.html", "/letra/b.html", "/letra/c.html", "/letra/d.html", "/letra/e.html",
            "/letra/f.html", "/letra/g.html", "/letra/h.html", "/letra/i.html", "/letra/j.html", "/letra/k.html", "/letra/l.html", "/letra/m.html",
            "/letra/n.html", "/letra/o.html", "/letra/p.html", "/letra/q.html", "/letra/r.html", "/letra/s.html", "/letra/t.html", "/letra/u.html",
            "/letra/v.html", "/letra/w.html", "/letra/x.html", "/letra/y.html", "/letra/z.html"};

    public HeavenMangaCom() {
        this.setFlag(R.drawable.flag_esp);
        this.setIcon(R.drawable.heavenmanga);
        this.setServerName("HeavenManga");
        setServerID(ServerBase.HEAVENMANGACOM);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        String source = new Navegador().get("http://heavenmanga.com/");
        source = getFirstMacth("<span>Lista Completa(.+)", source, "Error al obtener la lista");
        Pattern p = Pattern.compile("<li class=\"rpwe-clearfix\"><a href=\"(.+?)\" title=\"(.+?)\"");
        Matcher m = p.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (m.find()) {
            mangas.add(new Manga(HEAVENMANGACOM, m.group(2), m.group(1), true));
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String source = new Navegador().get("http://heavenmanga.com/buscar/" + URLEncoder.encode(term, "UTF-8") + ".html");
        return getMangasFromSource(source);
    }

    @Override
    public void loadChapters(Manga m, boolean forceReload) throws Exception {
        if (m.getChapters() == null || m.getChapters().size() == 0 || forceReload)
            loadMangaInformation(m, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga m, boolean forceReload) throws Exception {
        String source = new Navegador().get(m.getPath());
        // portada
        String portada = getFirstMacthDefault("style=\"position:absolute;\"><img src=\"(.+?)\"", source, "");
        m.setImages(portada);

        // sinopsis
        String sinopsis = getFirstMacthDefault("<div class=\"sinopsis\">(.+?)<div", source, "Sin sinopsis");
        m.setSinopsis(sinopsis.replaceAll("<.+?>", ""));

        //estado no soportado

        // capitulos
        Pattern p = Pattern.compile("<li><span class=\"capfec\">.+?><a href=\"(http://heavenmanga.com/.+?)\" title=\"(.+?)\"");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(2), matcher.group(1)));
        }
        if (chapters.size() > 0)
            m.setChapters(chapters);
        else
            throw new Exception("Error al cargar capitulos");


    }

    @Override
    public String getPagesNumber(Chapter c, int page) {
        if (c.getExtra() == null)
            try {
                setExtra(c);
            } catch (Exception e) {
                e.printStackTrace();
            }
        return c.getExtra().substring(0, c.getExtra().lastIndexOf("/") + 1) + page;
    }

    @Override
    public String getImageFrom(Chapter c, int page) throws Exception {
        String source = new Navegador().get(getPagesNumber(c, page));
        return getFirstMacth("src=\"([^\"]+)\" border=\"1\" id=\"p\">", source, "Error al obtener imagen");
    }

    @Override
    public void chapterInit(Chapter c) throws Exception {
        if (c.getExtra() == null)
            setExtra(c);
        String source = new Navegador().get(c.getExtra());
        String nop = getFirstMacth("(\\d+)</option></select>", source, "Error al cargar paginas");
        c.setPages(Integer.parseInt(nop));
    }

    private void setExtra(Chapter c) throws Exception {
        String source = new Navegador().get(c.getPath());
        String web = getFirstMacth("<a id=\"l\" href=\"(http://heavenmanga.com/.+?)\"><b>Leer</b>", source, "Error al obtener página");
        c.setExtra(web);
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception {
        int paginaLoc = pageNumber - 1;
        ArrayList<Manga> mangas = null;
        String web = "";
        if (categorie == 0 && paginaLoc < paginas.length) {
            web = paginas[paginaLoc];
        } else if (paginaLoc < 1) {
            web = generosV[categorie];
        }
        if (web.length() > 2) {
            String source = new Navegador().get("http://heavenmanga.com" + web);
            mangas = getMangasFromSource(source);
        }
        return mangas;
    }

    public ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("<article class=\"rel\"><a href=\"(http://heavenmanga.com/.+?)\"><header>(.+?)<.+?src=\"(.+?)\"");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Manga manga = new Manga(HEAVENMANGACOM, m.group(2), m.group(1), false);
            manga.setImages(m.group(3));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public String[] getCategories() {
        return generos;
    }

    @Override
    public String[] getOrders() {
        return new String[]{"a-z"};
    }

    @Override
    public boolean hasList() {
        return true;
    }

}
