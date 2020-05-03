package ar.rulosoft.mimanganu.componentes;

import android.content.Context;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.Comparator;

import ar.rulosoft.mimanganu.servers.FromFolder;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.utils.HtmlUnescape;
import ar.rulosoft.mimanganu.utils.Paths;
import ar.rulosoft.mimanganu.utils.Util;

public class Chapter {

    public static final int NEW = -1;
    public static final int UNREAD = 0;
    public static final int READ = 1;
    public static final int READING = 2;

    private int id;
    private int pages;
    private int mangaID;
    private int pagesRead;
    private int readStatus = UNREAD;
    private String title;
    private String path;
    private String extra;
    private boolean downloaded;
    private float volatile_order = -1;

    public Chapter(String title, String path) {
        super();
        setTitle(title);
        setPath(path);
    }

    public int getMangaID() {
        return mangaID;
    }

    public void setMangaID(int mangaID) {
        this.mangaID = mangaID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = HtmlUnescape.Unescape(Util.getInstance().fromHtml(title).toString().trim());
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPagesRead() {
        return pagesRead;
    }

    public void setPagesRead(int pagesRead) {
        this.pagesRead = pagesRead;
    }

    public int getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(int readStatus) {
        this.readStatus = readStatus;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    @Nullable
    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public void delete(Context context, Manga manga, ServerBase s) {
        deleteImages(context, manga, s);
        Database.deleteChapter(context, this);
    }

    public void deleteImages(Context context) {
        Manga manga = Database.getManga(context, getMangaID());
        ServerBase s = ServerBase.getServer(manga.getServerId(), context);
        deleteImages(context, manga, s);
    }

    private void deleteImages(Context context, Manga manga, ServerBase serverBase) {
        String path;
        if (!(serverBase instanceof FromFolder)) {
            path = Paths.generateBasePath(serverBase, manga, this, context);
        } else {
            path = getPath();
        }
        File f = new File(path);
        if (f.exists()) {
            Util.getInstance().deleteRecursive(f);
        }
    }

    public void reset(Context context) {
        Manga manga = Database.getManga(context, getMangaID());
        ServerBase s = ServerBase.getServer(manga.getServerId(), context);
        reset(context, manga, s);
    }

    public void reset(Context context, Manga manga, ServerBase s) {
        String path = Paths.generateBasePath(s, manga, this, context);
        File f = new File(path);
        if (f.exists()) {
            Util.getInstance().deleteRecursive(f);
        }
        setReadStatus(0);
        setPages(0);
        setPagesRead(0);
        setDownloaded(false);
        Database.updateChapterPlusDownload(context, this);
    }

    public void freeSpace(Context context) {
        deleteImages(context);
        setDownloaded(false);
        Database.updateChapterPlusDownload(context, this);
    }

    public void freeSpace(Context context, Manga manga, ServerBase s) {
        deleteImages(context, manga, s);
        setDownloaded(false);
        Database.updateChapterPlusDownload(context, this);
    }

    public void markRead(Context context, boolean read) {
        Database.markChapter(context, getId(), read);
        setReadStatus(read ? Chapter.READ : Chapter.UNREAD);
        if (read) {
            setPagesRead(getPages());
            Database.updateChapterPlusDownload(context, this);
        } else {
            setPagesRead(0);
            Database.updateChapterPlusDownload(context, this);
        }
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Chapter) {
            //TODO: after a full migration to only path this two lines need to be changed
            String ours = Util.getInstance().getFilePath(getPath());
            String theirs = Util.getInstance().getFilePath(((Chapter) obj).getPath());
            return ours.equalsIgnoreCase(theirs);
        }
        return false;
    }

    public static class Comparators {
        private static final String FLOAT_PATTERN = "(\\d+([\\.,]\\d+)?)";
        private static final String STRING_END_PATTERN = "[^\\d]\\.";
        private static final String VOLUME_REMOVE_PATTERN = "[v|V][o|O][l|L]\\.?\\s*\\d+";
        public static Comparator<Chapter> TITLE_DESC = new Comparator<Chapter>() {
            @Override
            public int compare(Chapter c1, Chapter c2) {
                return c1.getTitle().compareTo(c2.getTitle());
            }
        };
        public static Comparator<Chapter> TITLE_ASC = new Comparator<Chapter>() {
            @Override
            public int compare(Chapter c1, Chapter c2) {
                return c2.getTitle().compareTo(c1.getTitle());
            }
        };
        public static Comparator<Chapter> DATABASE_ADDED_ASC = new Comparator<Chapter>() {
            @Override
            public int compare(Chapter c1, Chapter c2) {
                return (c1.getId() - c2.getId());
            }
        };
        public static Comparator<Chapter> DATABASE_ADDED_DESC = new Comparator<Chapter>() {
            @Override
            public int compare(Chapter c1, Chapter c2) {
                return (c2.getId() - c1.getId());
            }
        };

        private static String manga_title;
        public static Comparator<Chapter> NUMBERS_DESC = new Comparator<Chapter>() {
            @Override
            public int compare(Chapter c1, Chapter c2) {
                try {
                    return (int) Math.floor(getVolatileOrder(c2) - getVolatileOrder(c1));
                } catch (Exception e) {
                    return 0;
                }
            }
        };
        public static Comparator<Chapter> NUMBERS_ASC = new Comparator<Chapter>() {
            @Override
            public int compare(Chapter c1, Chapter c2) {
                try {
                    return (int) Math.floor(getVolatileOrder(c1) - getVolatileOrder(c2));
                } catch (Exception e) {
                    return 0;
                }
            }
        };

        public static void setManga_title(String title) {
            manga_title = title;
        }

        private static float getVolatileOrder(Chapter c) throws Exception {
            if (c.volatile_order == -1) {
                String str1 = c.getTitle().replace(manga_title, "");
                str1 = str1.replaceAll(VOLUME_REMOVE_PATTERN, " ");
                str1 = str1.replaceAll(STRING_END_PATTERN, " ");
                str1 = ServerBase.getFirstMatch(FLOAT_PATTERN, str1, "").replace(',', '.');
                c.volatile_order = Float.parseFloat(str1);
            }
            return c.volatile_order;
        }
    }
}
