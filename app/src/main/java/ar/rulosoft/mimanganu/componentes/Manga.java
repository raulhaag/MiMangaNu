package ar.rulosoft.mimanganu.componentes;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.utils.HtmlUnescape;

public class Manga {
    private int id;
    private int serverId;
    private int news;
    private int lastIndex;
    private int readingDirection = -1;
    private int readerType;//0, default, 1 paged, 2 continuous

    private String title;
    private String synopsis = "";
    private String images = "";
    private String path;
    private String author = "";
    private String genre = "";

    private boolean finished;
    private ArrayList<Chapter> chapters = new ArrayList<>();
    private float scrollSensitive;

    public Manga(int serverId, String title, String path, boolean finished) {
        super();
        this.serverId = serverId;
        this.title = title;
        this.path = path;
        this.author = "";
        this.genre = "";
        this.finished = finished;
    }

    public Manga(int serverId, int id, String title, String synopsis, String images, String path, String author, boolean finished, float scrollSentive, int readingDirection, int lastIndex, int news) {
        this.serverId = serverId;
        this.id = id;
        this.title = title;
        this.synopsis = synopsis;
        this.images = images;
        this.path = path;
        this.author = author;
        this.finished = finished;
        this.scrollSensitive = scrollSentive;
        this.readingDirection = readingDirection;
        this.lastIndex = lastIndex;
        this.news = news;
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

    public void setSynopsis(String synopsis) {
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

    public float getScrollSensitive() {
        return scrollSensitive;
    }

    public void setScrollSensitive(float scrollSensitive) {
        this.scrollSensitive = scrollSensitive;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getReaderType(){
        return readerType;
    }

    public void setReaderType(int readerType) {
        this.readerType = readerType;
    }
}
