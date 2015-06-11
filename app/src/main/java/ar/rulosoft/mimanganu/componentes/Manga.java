package ar.rulosoft.mimanganu.componentes;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.utils.HtmlUnescape;

public class Manga {
    int id, serverId, news, lastIndex, readingDirection = -1;
    String title, synopsis, images, path, author;
    ArrayList<Chapter> chapters = new ArrayList<>();

    boolean finished;

    public Manga(int serverId, String title, String path, boolean finished) {
        super();
        this.serverId = serverId;
        this.title = title;
        this.path = path;
        this.author = "";
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSinopsis(String synopsis) {
        this.synopsis = HtmlUnescape.Unescape(synopsis);
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return this.title;
    }

    public void addChapter(Chapter c) {
        chapters.add(c);
    }

    public Chapter getChapter(int index) {
        if (chapters.size() > index && index >= 0) {
            return chapters.get(index);
        } else {
            return null;
        }
    }

    public ArrayList<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(ArrayList<Chapter> caps) {
        chapters = caps;
    }

    public void clearChapters() {
        chapters.clear();
    }


    public int getNews() {
        return news;
    }

    public void setNews(int news) {
        this.news = news;
    }

    public int getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(int lastIndex) {
        this.lastIndex = lastIndex;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getReadingDirection() {
        return readingDirection;
    }

    public void setReadingDirection(int readingDirection) {
        this.readingDirection = readingDirection;
    }

}
