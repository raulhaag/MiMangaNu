package ar.rulosoft.mimanganu.componentes;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.utils.HtmlUnescape;

public class Manga {
    int id, serverId, nuevos, lastIndex, sentidoLectura = -1;
    String titulo, sinopsis, images, path, autor;
    ArrayList<Capitulo> capitulos = new ArrayList<Capitulo>();

    boolean finalizado;

    public Manga(int serverId, String titulo, String path, boolean finalizado) {
        super();
        this.serverId = serverId;
        this.titulo = titulo;
        this.path = path;
        this.autor = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = HtmlUnescape.Unescape(sinopsis);
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    @Override
    public String toString() {
        return this.titulo;
    }

    public void addCapitulo(Capitulo c) {
        capitulos.add(c);
    }

    public void addCapituloFirst(Capitulo c) {
        capitulos.add(0, c);
    }

    public Capitulo getCapitulo(int index) {
        if (capitulos.size() > index && index >= 0) {
            return capitulos.get(index);
        } else {
            return null;
        }
    }

    public ArrayList<Capitulo> getCapitulos() {
        return capitulos;
    }

    public void setCapitulos(ArrayList<Capitulo> caps) {
        capitulos = caps;
    }

    public void clearCapitulos() {
        capitulos.clear();
    }


    public int getNuevos() {
        return nuevos;
    }

    public void setNuevos(int nuevos) {
        this.nuevos = nuevos;
    }

    public int getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(int lastIndex) {
        this.lastIndex = lastIndex;
    }

    public boolean isFinalizado() {
        return finalizado;
    }

    public void setFinalizado(boolean finalizado) {
        this.finalizado = finalizado;
    }

    public int getSentidoLectura() {
        return sentidoLectura;
    }

    public void setSentidoLectura(int sentidoLectura) {
        this.sentidoLectura = sentidoLectura;
    }

}
