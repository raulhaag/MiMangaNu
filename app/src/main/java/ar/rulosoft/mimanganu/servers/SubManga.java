package ar.rulosoft.mimanganu.servers;

import android.text.Html;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public class SubManga extends ServerBase {

    public SubManga() {
        setServerID(SUBMANGA);
        setIcon(R.drawable.submanga_icon);
        this.setServerName("SubManga");
        setBandera(R.drawable.flag_esp);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        // <td><a href="(http://submanga.com/.+?)".+?</b>(.+?)<
        ArrayList<Manga> mangas = new ArrayList<Manga>();
        Navegador nav = new Navegador();
        String source = nav.get("http://submanga.com/series/n");
        Pattern p = Pattern.compile("<td><a href=\"(http://submanga.com/.+?)\".+?</b>(.+?)<");
        Matcher m = p.matcher(source);
        while (m.find()) {
            String name = m.group(2);
            if (name.indexOf("¡") == -1 && name.indexOf("¿") == -1 && name.indexOf("ñ") == -1 && name.indexOf("Ñ") == -1) {
                mangas.add(new Manga(SUBMANGA, name, m.group(1), false));
            }
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
        if (manga.getCapitulos().size() == 0||reinicia) {
            Pattern p;
            Matcher m;
            String data = new Navegador().get((manga.getPath() + "/completa"));
            p = Pattern.compile("<tr><td><a href=\"(http://submanga.com/.+?)\">(.+?)</td>");
            m = p.matcher(data);

            while (m.find()) {
                String web = "http://submanga.com/c/" + m.group(1).substring(m.group(1).lastIndexOf("/"));
                Capitulo mc = new Capitulo(Html.fromHtml(m.group(2)).toString(), web);
                manga.addCapituloFirst(mc);
            }
        }
    }

    @Override
    public void cargarPortada(Manga manga, boolean reinicia) throws Exception {
        Pattern p;
        Matcher m;
        String data = new Navegador().get((manga.getPath()));

        p = Pattern.compile("</h1><img src=\"(http://.+?)\"/><br />(.+?)</div>");
        m = p.matcher(data);

        if (m.find()) {
            manga.setImages(m.group(1));
            manga.setSinopsis(Html.fromHtml(m.group(2)).toString());
        } else {
            manga.setSinopsis("Sin sinopsis.");
        }
    }

    @Override
    public String getPagina(Capitulo c, int pagina) {
        return c.getPath() + "/" + pagina;
    }

    @Override
    public String getImagen(Capitulo c, int pagina) throws Exception {
        //if (c.getExtra() == null || c.getExtra().length() < 2) {
        String data;
        data = new Navegador().get(this.getPagina(c, pagina));
        return getFirstMacthDefault("<img src=\"(http://.+?)\"", data, null);
        //} else {
        //	return (c.getExtra().split("|")[pagina]);
        //}
    }

    @Override
    public void iniciarCapitulo(Capitulo c) throws Exception {
        String pagina = "";
        int i = 1;
        String extra = "";
        while ((pagina = getImagen(c, i)) != null) {
            extra = extra + "|" + pagina;
            i++;
        }
        c.setExtra(extra);
        i--;
        c.setPaginas(i);
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categoria, int ordentipo, int pagina) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getCategorias() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getOrdenes() {
        // TODO Auto-generated method stub
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
