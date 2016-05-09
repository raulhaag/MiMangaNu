package ar.rulosoft.mimanganu.servers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public class LectureEnLigne extends ServerBase {

    public static String HOST = "http://www.lecture-en-ligne.com/";

    public LectureEnLigne() {
        setServerID(LECTUREENLIGNE);
        setIcon(R.drawable.lectureenligne_icon);
        this.setServerName("LectureEnLigne");
        setFlag(R.drawable.flag_fr);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String source = new Navegador().get(HOST);
        Pattern p = Pattern.compile("<option value=\"([^\"]+)\">(.+?)</option>");
        Matcher m = p.matcher(source);
        while (m.find()) {
            mangas.add(new Manga(LECTUREENLIGNE, m.group(2), HOST + m.group(1), false));
        }
        return mangas;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 || forceReload)
            loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {

        String data = new Navegador().get((manga.getPath()));// :</p><p>(.+?)</p>

        manga.setSynopsis(getFirstMatchDefault("</p>[\\s]+<p>(.+?)</p>", data, "Sans synopsis"));
        manga.setImages(getFirstMatchDefault("<img src=\"([^\"]+)\" alt=\"[^\"]+\" class=\"imagemanga\"", data, ""));

        //autor
        manga.setAuthor(getFirstMatchDefault("Auteur :.+?d>(.+?)<", data, ""));

        //genre
        manga.setGenre(getFirstMatchDefault("<tr><th>Genres :</th><td>(.+?)</td>", data, ""));

        // capitulos
        ArrayList<Chapter> chapters = new ArrayList<>();
        Pattern p = Pattern.compile("<td class=\"td\">(.+?)</td>[\\s\\S]+?<a href=\"(.+?)\"");
        Matcher ma = p.matcher(data);
        while (ma.find()) {
            chapters.add(0, new Chapter(ma.group(1), HOST + ma.group(2)));
        }
        manga.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return chapter.getPath().replaceAll("\\d+\\.h", page + ".h");
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String data = new Navegador().get(this.getPagesNumber(chapter, page));
        return getFirstMatch("<img id='image' src='(.+?)'", data, "Error: no se pudo obtener el enlace a la imagen");
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data = new Navegador().get(chapter.getPath());
        String paginas = getFirstMatch("<select class=\"pages\">.+?(\\d+)</option>[\\s]*</select>", data, "Error: no se pudo obtener el numero de paginas");
        chapter.setPages(Integer.parseInt(paginas));
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception {
        return null;
    }

    @Override
    public String[] getCategories() {
        return null;
    }

    @Override
    public String[] getOrders() {
        return null;
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public boolean hasFilteredNavigation() {
        return false;
    }

}
