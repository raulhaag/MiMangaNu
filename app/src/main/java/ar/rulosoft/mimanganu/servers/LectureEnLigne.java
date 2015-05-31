package ar.rulosoft.mimanganu.servers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public class LectureEnLigne extends ServerBase {

    public LectureEnLigne() {
        setServerID(LECTUREENLIGNE);
        setIcon(R.drawable.lectureenligne);
        this.setServerName("LectureEnLigne");
        setBandera(R.drawable.flag_fr);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String source = new Navegador().get("http://www.lecture-en-ligne.com/");
        Pattern p = Pattern.compile("<option value=\"([^\"]+)\">(.+?)</option>");
        Matcher m = p.matcher(source);
        while (m.find()) {
            mangas.add(new Manga(LECTUREENLIGNE, m.group(2), m.group(1), false));
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> getBusqueda(String termino) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void cargarCapitulos(Manga manga, boolean reinicia) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 || reinicia)
            cargarPortada(manga, reinicia);
    }

    @Override
    public void cargarPortada(Manga manga, boolean reinicia) throws Exception {

        String data = new Navegador().get((manga.getPath()));// :</p><p>(.+?)</p>

        manga.setSinopsis(getFirstMacthDefault("</p>[\\s]+<p>(.+?)</p>", data, "Sans synopsis"));
        manga.setImages("http://www.lecture-en-ligne.com/" + getFirstMacthDefault("<img src=\"([^\"]+)\" alt=\"[^\"]+\" class=\"imagemanga\"", data, ""));

        //autor
        manga.setAutor(getFirstMacthDefault("Auteur :.+?d>(.+?)<",data,""));

        // capitulos
        ArrayList<Chapter> chapters = new ArrayList<>();
        Pattern p = Pattern.compile("<td class=\"td\">(.+?)</td>[^\\.]+\\.\\./\\.\\.(.+?)\"");
        Matcher ma = p.matcher(data);
        while (ma.find()) {
            chapters.add(0, new Chapter(ma.group(1), "http://www.lecture-en-ligne.com/" + ma.group(2)));
        }
        manga.setChapters(chapters);
    }

    @Override
    public String getPagina(Chapter c, int pagina) {
        return c.getPath().replaceAll("\\d+\\.h", pagina + ".h");
    }

    @Override
    public String getImagen(Chapter c, int pagina) throws Exception {
        String data = new Navegador().get(this.getPagina(c, pagina));
        return getFirstMacth("<img id='image' src='(.+?)'", data, "Error: no se pudo obtener el enlace a la imagen");
    }

    @Override
    public void iniciarCapitulo(Chapter c) throws Exception {
        String data = new Navegador().get(c.getPath());
        String paginas = getFirstMacth("<select class=\"pages\">.+?(\\d+)</option>[\\s]*</select>", data, "Error: no se pudo obtener el numero de paginas");
        c.setPaginas(Integer.parseInt(paginas));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categoria, int ordentipo, int pagina) throws Exception {
        return null;
    }

    @Override
    public String[] getCategorias() {
        return null;
    }

    @Override
    public String[] getOrdenes() {
        return null;
    }

    @Override
    public boolean tieneListado() {
        return true;
    }

    @Override
    public boolean tieneNavegacionVisual() {
        return false;
    }

}
