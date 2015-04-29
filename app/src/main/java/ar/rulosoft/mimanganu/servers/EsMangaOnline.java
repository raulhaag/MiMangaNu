package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.text.Html;
import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public class EsMangaOnline extends ServerBase {

    public static final String[] generos = {"Accion", "Comedia", "Deportes", "Drama", "Ecchi", "Escolar", "Gender Bender", "Harem", "Horror", "Josei",
            "Mecha", "Psicologico", "Shonen", "Shojo", "Seinen", "Yuri"};
    private static final String PATTERN_SERIE = "<a class=\"lst mng_det_pop\" href=\"([^\"]*)\" title=\"([^\"]*)\" rel=\"\\d*\">";
    private static final String PATRON_IMAGEN = "src=\"([^\"]+?.\\.jpg|gif|jpeg|png|bmp)";
    private static final String PATRON_SERIE_VISUAL = "	<div class=\"img_wrp\"><a href=\"(.+?)\" title=\"(.+?)\".+?<img src=\"(.+?)\"";
    private static final String SEGMENTO = "([\\s\\S]+?)<div class=\"wpm_wgt mng_wek\">";
    public static String[] orden = {"Más Popular", "A - Z", "Z - A", "Mejor Calificados", "Ultimos Actualizados", "Ultimos Agregados"};
    public static String[] ordenM = {"most-popular/", "", "name-za/", "top-rating/", "last-updated/", "last-added/"};

    public EsMangaOnline() {
        this.setBandera(R.drawable.flag_esp);
        this.setIcon(R.drawable.esmangaonline_icon);
        this.setServerName("EsMangaOnline");
        setServerID(ServerBase.ESMANGAONLINE);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<Manga>();
        String data = new Navegador().get("http://esmangaonline.com/lista-de-mangas/");
        Pattern p = Pattern.compile(PATTERN_SERIE);
        Matcher m = p.matcher(data);
        while (m.find()) {
            mangas.add(new Manga(this.getServerID(), m.group(2), m.group(1), false));
        }
        return mangas;
    }

    @Override
    public void cargarCapitulos(Manga manga) throws Exception {
        int pasos = 1;
        String data;
        Navegador nav = new Navegador();
        data = nav.get(manga.getPath() + "/");
        Pattern p = Pattern.compile("<li><a href=\"[^\"]*/(\\d*)/\">Ultimo</a></li>");
        Matcher m = p.matcher(data);
        if (m.find()) {
            pasos = Integer.parseInt(m.group(1));
        }

        cargarCapitulosData(data, manga.getCapitulos());

        for (int i = 2; i <= pasos; i++) {
            data = nav.get(manga.getPath() + "chapter-list/" + i + "/");
            cargarCapitulosData(data, manga.getCapitulos());
        }
    }

    private void cargarCapitulosData(String data, ArrayList<Capitulo> caps) {
        Pattern p = Pattern.compile("<a class=\"lst\" href=\"([^\"]*)\" title=\"([^\"]*)\">[^<]*<b");
        Matcher m = p.matcher(data);
        while (m.find()) {
            caps.add(0, (new Capitulo(m.group(2), m.group(1))));
        }
    }

    @Override
    public void cargarPortada(Manga manga) throws Exception {
        String data = new Navegador().get(manga.getPath());
        //portada
        manga.setSinopsis(Html.fromHtml(getFirstMacthDefault("<div class=\"det\">.+?<p>(.+?)</br></br>Algún", data, "Sin Sinopsis")).toString());
        manga.setImages(getFirstMacthDefault("<img class=\"cvr\" src=\"([^\"]*)\" alt=\"[^\"]*\"/>", data, ""));
    }

    @Override
    public String getPagina(Capitulo c, int pagina) {
        if (pagina > c.getPaginas()) {
            pagina = 1;
        }
        return c.getPath() + pagina + "/";

    }

    @Override
    public String getImagen(Capitulo c, int pagina) throws Exception {
        String data;
        data = new Navegador().get(this.getPagina(c, pagina));
        return getFirstMacth(PATRON_IMAGEN, data, "Error: no se pudo obtener el enlace a la imagen");
    }

    @Override
    public void iniciarCapitulo(Capitulo c) throws Exception {
        String data;
        data = new Navegador().get(c.getPath());
        Pattern p = Pattern.compile("(\\d*)</option></select></li>");
        Matcher m = p.matcher(data);
        if (m.find() && m.find()) {
            c.setPaginas(Integer.parseInt(m.group(1)));
        }
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categoria, int ordentipo, int pagina) throws Exception {
        hayMas = false;
        ArrayList<Manga> mangas = new ArrayList<Manga>();
        String web = "http://esmangaonline.com/lista-de-manga/categoria/" + generos[categoria] + "/" + ordenM[ordentipo];
        String data = new Navegador().get(web);
        if (data.length() < 100) {
            hayMas = true;
        } else {
            data = getFirstMacth(SEGMENTO, data, "Error en segmento");
            Pattern p = Pattern.compile(PATRON_SERIE_VISUAL);
            Matcher m = p.matcher(data);
            while (m.find()) {
                Manga manga = new Manga(getServerID(), m.group(2), m.group(1), false);
                manga.setImages(m.group(3));
                mangas.add(manga);
            }
        }
        return mangas;
    }

    @Override
    public String[] getCategorias() {
        return generos;
    }

    @Override
    public String[] getOrdenes() {
        return orden;
    }

    @Override
    public int buscarNuevosCapitulos(Manga manga, Context context) throws Exception {
        int returnValue = 0;
        Manga mangaDb = Database.getFullManga(context, manga.getId());
        this.cargarCapitulos(manga);
        int diff = manga.getCapitulos().size() - mangaDb.getCapitulos().size();
        if (diff > 0) {
            int diffEncontradas = 0;
            for (int j = 0; j < manga.getCapitulos().size(); j++) {
                boolean flag = true;
                for (int k = 0; k < mangaDb.getCapitulos().size(); k++) {
                    if (manga.getCapitulos().get(j).getPath().contentEquals(mangaDb.getCapitulos().get(k).getPath())) {
                        mangaDb.getCapitulos().remove(k);
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    manga.getCapitulo(j).setEstadoLectura(Capitulo.NUEVO);
                    Database.addCapitulo(context, manga.getCapitulo(j), manga.getId());
                    Log.e("Nuevo Manga", manga.getCapitulo(j).getTitulo());
                    diffEncontradas++;
                    if (diffEncontradas == diff) {
                        Database.updateMangaNuevos(context, mangaDb, diff);
                        returnValue = diff;
                        break;
                    }
                }
            }
        } else if (diff < 0) {

        }
        return returnValue;
    }

    @Override
    public boolean tieneListado() {
        return true;
    }

    @Override
    public ArrayList<Manga> getBusqueda(String termino) throws Exception {
        throw new Exception("No soportado aún");
        //return null;
    }

}
