package ar.rulosoft.mimanganu.componentes;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;

import ar.rulosoft.mimanganu.utils.HtmlUnescape;
import ar.rulosoft.mimanganu.utils.Util;

public class Manga {
    private int id;
    private int serverId;
    private int news;
    private int lastIndex;
    private int readingDirection = -1;
    private int readerType; //0, default, 1 paged, 2 continuous

    private String title;
    private String synopsis;
    private String images;
    private String path;
    private String author;
    private String genre;
    private String lastUpdate;
    private boolean finished;
    private String vault = "";

    private ArrayList<Chapter> chapters = new ArrayList<>();
    private float scrollSensitive;

    public Manga(int serverId, String title, String path, boolean finished) {
        super();
        setServerId(serverId);
        setTitle(title);
        setPath(path);
        setFinished(finished);
    }

    public Manga(int serverId, String title, String path, String images) {
        super();
        setServerId(serverId);
        setTitle(title);
        setPath(path);
        setImages(images);
    }


    public Manga(int serverId, int id, String title, String synopsis, String images, String path, String author, boolean finished, float scrollSensitive, int readingDirection, int lastIndex, int news, String vault) {
        setServerId(serverId);
        setId(id);
        setTitle(title);
        setSynopsis(synopsis);
        setImages(images);
        setPath(path);
        setAuthor(author);
        setFinished(finished);
        setScrollSensitive(scrollSensitive);
        setReadingDirection(readingDirection);
        setLastIndex(lastIndex);
        setNews(news);
        setVault(vault);
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

    @SuppressWarnings("WeakerAccess")
    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = HtmlUnescape.Unescape(Util.getInstance().fromHtml(title).toString().trim());
    }

    @Nullable
    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = HtmlUnescape.Unescape(Util.getInstance().fromHtml(synopsis).toString().trim());
    }

    @Nullable
    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    @Nullable
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Nullable
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = HtmlUnescape.Unescape(Util.getInstance().fromHtml(author).toString().trim())
                .replaceAll("\\s+", " ")
                .replaceAll("\\s,", ",");
    }

    @Nullable
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

    public void setChapters(ArrayList<Chapter> chapters) {
        this.chapters = chapters;
    }

    public int getNews() {
        return news;
    }

    @SuppressWarnings("WeakerAccess")
    public void setNews(int news) {
        this.news = news;
    }

    public int getLastIndex() {
        return lastIndex;
    }

    @SuppressWarnings("WeakerAccess")
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

    @SuppressWarnings("WeakerAccess")
    public void setScrollSensitive(float scrollSensitive) {
        this.scrollSensitive = scrollSensitive;
    }

    @Nullable
    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = HtmlUnescape.Unescape(Util.getInstance().fromHtml(genre).toString().trim())
                .replaceAll("\\s+", " ")
                .replaceAll("\\s,", ",");
    }

    public int getReaderType() {
        return readerType;
    }

    public void setReaderType(int readerType) {
        this.readerType = readerType;
    }

    @Nullable
    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void addChapterFirst(Chapter c) {
        chapters.add(0, c);
    }

    public void addChapterLast(Chapter c) {
        chapters.add(c);
    }

    @Override
    public String toString() {
        return this.title;
    }

    public String getVault() {
        return vault;
    }

    public void setVault(String vault) {
        this.vault = vault;
    }

    public static class Comparators {
        public static Comparator<Manga> TITLE_ASC = new Comparator<Manga>() {
            @Override
            public int compare(Manga m0, Manga m1) {
                return m0.getTitle().compareTo(m1.getTitle());
            }
        };
        public static Comparator<Manga> TITLE_DESC = new Comparator<Manga>() {
            @Override
            public int compare(Manga m0, Manga m1) {
                return m1.getTitle().compareTo(m0.getTitle());
            }
        };
    }

}
