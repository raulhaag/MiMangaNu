package ar.rulosoft.mimanganu.componentes;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.fedorvlasov.lazylist.FileCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.utils.Util;

public class Database extends SQLiteOpenHelper {

    // Table for entire manga
    public static final String TABLE_MANGA = "manga";
    public static final String COL_SERVER_ID = "server_id";
    public static final String COL_NAME = "nombre";
    public static final String COL_PATH = "path";
    public static final String COL_IMAGE = "imagen";
    public static final String COL_SYNOPSIS = "sinopsis";
    public static final String COL_LAST_READ = "ultima";
    public static final String COL_ID = "id";
    public static final String COL_LAST_INDEX = "last_index";// indice listview
    public static final String COL_NEW = "nuevos";// hay nuevos?
    public static final String COL_SEARCH = "burcar";// buscar updates / isFinished
    public static final String COL_AUTHOR = "autor";
    public static final String COL_SCROLL_SENSITIVE = "scroll_s";
    public static final String COL_GENRE = "genres";
    private static final String COL_READ_ORDER = "orden_lectura";// sentido de
    private static final String COL_READER = "reader";
    // Table for each chapter
    public static final String TABLE_CHAPTERS = "capitulos";
    public static final String COL_CAP_ID_MANGA = "manga_id";
    public static final String COL_CAP_NAME = "nombre";
    public static final String COL_CAP_PATH = "path";
    public static final String COL_CAP_PAGES = "paginas";
    public static final String COL_CAP_PAG_READ = "leidas";
    public static final String COL_CAP_STATE = "estado";
    public static final String COL_CAP_DOWNLOADED = "descargado";
    public static final String COL_CAP_ID = "id";
    public static final String COL_CAP_EXTRA = "extra";
    // Database creation sql statement
    private static final String DATABASE_MANGA_CREATE = "create table " +
            TABLE_MANGA + "(" +
            COL_ID + " integer primary key autoincrement, " +
            COL_NAME + " text not null," +
            COL_PATH + " text not null UNIQUE, " +
            COL_IMAGE + " text," +
            COL_SYNOPSIS + " text," +
            COL_SERVER_ID + "," +
            COL_LAST_READ + " int," +
            COL_NEW + " int DEFAULT 0," +
            COL_LAST_INDEX + " int DEFAULT 0, " +
            COL_SEARCH + " int DEFAULT 0, " +
            COL_READ_ORDER + " int not null DEFAULT -1, " +
            COL_AUTHOR + " TEXT NOT NULL DEFAULT 'N/A'," +
            COL_SCROLL_SENSITIVE + " NUMERICAL DEFAULT -1.1," +
            COL_READER + " INTEGER DEFAULT 0," +
            COL_GENRE + " TEXT NOT NULL DEFAULT 'N/A');";
    private static final String DATABASE_CHAPTERS_CREATE = "create table " +
            TABLE_CHAPTERS + "(" +
            COL_CAP_ID + " integer primary key autoincrement, " +
            COL_CAP_NAME + " text not null," +
            COL_CAP_PATH + " text not null UNIQUE, " +
            COL_CAP_PAGES + " int," +
            COL_CAP_ID_MANGA + " int," +
            COL_CAP_STATE + " int DEFAULT 0," +
            COL_CAP_PAG_READ + " int DEFAULT 1, " +
            COL_CAP_DOWNLOADED + " int DEFAULT 0, "+
            COL_CAP_EXTRA + " text);";
    // name and path of database
    private static String database_name;
    private static String database_path;
    private static int database_version = 13;
    private static SQLiteDatabase localDB;
    Context context;

    // make private, should be single instance
    private Database(Context context) {
        super(context, database_path + database_name, null, database_version);
        this.context = context;
    }

    public static SQLiteDatabase getDatabase(Context c) {
        // Setup path and database name
        if (database_path == null || database_path.length() == 0) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
            database_path = (prefs.getString("directorio",
                    Environment.getExternalStorageDirectory().getAbsolutePath()) + "/MiMangaNu/") + "dbs/";
            database_name = "mangas.db";
        }
        if (!new File(database_path).exists())
            new File(database_path).mkdirs();
        if ((localDB == null) || !localDB.isOpen()) {
            localDB = new Database(c).getReadableDatabase();
        }
        return localDB;
    }

    private static ContentValues setMangaCV(Manga manga, boolean setTime) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, manga.getTitle());
        cv.put(COL_PATH, manga.getPath());
        cv.put(COL_IMAGE, manga.getImages());
        cv.put(COL_SYNOPSIS, manga.getSynopsis());
        cv.put(COL_SERVER_ID, manga.getServerId());
        cv.put(COL_AUTHOR, manga.getAuthor());
        cv.put(COL_GENRE, manga.getGenre());
        cv.put(COL_SEARCH, manga.isFinished() ? 1 : 0);
        cv.put(COL_READER, manga.getReaderType());
        if (setTime)
            cv.put(COL_LAST_READ, System.currentTimeMillis());
        return cv;
    }

    public static int addManga(Context context, Manga manga) {
        int tmp = -1;
        try {
            tmp = (int) getDatabase(context).insertOrThrow(TABLE_MANGA, null, setMangaCV(manga, true));
        } catch (Exception e){
            e.printStackTrace();
            Util.getInstance().toast(context, context.getResources().getString(R.string.error_while_adding_chapter_or_manga_to_db, manga.getTitle()));
        }
        return tmp;
    }

    /**
     * For information,
     * <p/>
     * setTime = false is "updateMangaNo
     * Time"
     * setTime = true is "updateManga"
     */
    public static void updateManga(Context context, Manga manga, boolean setTime) {
        getDatabase(context).update(TABLE_MANGA,
                setMangaCV(manga, setTime), COL_ID + "=" + manga.getId(), null);
    }

    public static void updateMangaRead(Context c, int mid) {
        ContentValues cv = new ContentValues();
        cv.put(COL_LAST_READ, System.currentTimeMillis());
        cv.put(COL_NEW, 0);
        getDatabase(c).update(TABLE_MANGA, cv, COL_ID + "=" + mid, null);
    }

    public static void setUpgradable(Context c, int mangaid, boolean buscar) {
        ContentValues cv = new ContentValues();
        cv.put(COL_SEARCH, buscar ? 1 : 0);
        getDatabase(c).update(TABLE_MANGA, cv, COL_ID + "=" + mangaid, null);
    }

    public static void updateMangaLastIndex(Context c, int mid, int idx) {
        ContentValues cv = new ContentValues();
        cv.put(COL_LAST_INDEX, idx);
        getDatabase(c).update(TABLE_MANGA, cv, COL_ID + "=" + mid, null);
    }

    public static void updateMangaScrollSensitive(Context c, int mid, float nScroll) {
        ContentValues cv = new ContentValues();
        cv.put(COL_SCROLL_SENSITIVE, nScroll);
        getDatabase(c).update(TABLE_MANGA, cv, COL_ID + "=" + mid, null);
    }


    public static void updateNewMangas(Context c, Manga m, int news) {
        int actual = 0;
        if (news > -99) {
            Cursor cursor = getDatabase(c).query(TABLE_MANGA,
                    new String[]{COL_NEW}, COL_ID + " = " + m.getId(), null, null, null, null);
            if (cursor.moveToFirst()) {
                actual = cursor.getInt(cursor.getColumnIndex(COL_NEW));
                actual += news;
            }
            cursor.close();
        }
        ContentValues cv = new ContentValues();
        if (!(actual > 0))
            cv.put(COL_NEW, 0);
        else
            cv.put(COL_NEW, actual);
        getDatabase(c).update(TABLE_MANGA, cv, COL_ID + "=" + m.getId(), null);
    }

    public static void addChapter(Context context, Chapter chapter, int mangaId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_CAP_ID_MANGA, mangaId);
        contentValues.put(COL_CAP_NAME, chapter.getTitle());
        contentValues.put(COL_CAP_PATH, chapter.getPath());
        contentValues.put(COL_CAP_PAGES, chapter.getPages());
        contentValues.put(COL_CAP_STATE, chapter.getReadStatus());
        contentValues.put(COL_CAP_PAG_READ, chapter.getPagesRead());
        contentValues.put(COL_CAP_DOWNLOADED, chapter.isDownloaded());
        contentValues.put(COL_CAP_EXTRA, chapter.getExtra());
        try {
            getDatabase(context).insertOrThrow(TABLE_CHAPTERS, null, contentValues);
        } catch (SQLiteConstraintException sqlce) {
            try {
                removeOrphanedChapters(context);
                getDatabase(context).insertOrThrow(TABLE_CHAPTERS, null, contentValues);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Database", "Manga_ID: " + mangaId);
                Log.d("Database", "Title: " + chapter.getTitle());
                Log.d("Database", "Path: " + chapter.getPath());
                Log.d("Database", "Pages: " + chapter.getPages());
                Log.d("Database", "Pages Read: " + chapter.getPagesRead());
                Log.d("Database", "Read Status: " + chapter.getReadStatus());
                Log.d("Database", "isDownloaded: " + chapter.isDownloaded());
                Log.d("Database", "Extra: " + chapter.getExtra());
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_while_adding_chapter_or_manga_to_db, chapter.getTitle()));
            }
        }
    }

    public static void updateChapter(Context context, Chapter chapter) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_NAME, chapter.getTitle());
        cv.put(COL_CAP_PATH, chapter.getPath());
        cv.put(COL_CAP_PAGES, chapter.getPages());
        cv.put(COL_CAP_STATE, chapter.getReadStatus());
        cv.put(COL_CAP_PAG_READ, chapter.getPagesRead());
        cv.put(COL_CAP_EXTRA,chapter.getExtra());
        try {
            getDatabase(context).update(TABLE_CHAPTERS, cv, COL_CAP_ID + " = " + chapter.getId(), null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Database", "Title: " + chapter.getTitle());
            Log.d("Database", "Path: " + chapter.getPath());
            Log.d("Database", "Pages: " + chapter.getPages());
            Log.d("Database", "Pages Read: " + chapter.getPagesRead());
            Log.d("Database", "Read Status: " + chapter.getReadStatus());
            Log.d("Database", "isDownloaded: " + chapter.isDownloaded());
            Log.d("Database", "Extra: " + chapter.getExtra());
            Util.getInstance().toast(context, context.getResources().getString(R.string.error_while_updating_chapter_or_manga_in_db, chapter.getTitle()));
        }
    }

    public static ArrayList<Manga> getMangasForUpdates(Context context) {
        return getMangasCondition(context, COL_SEARCH + "= 0", null, false);
    }

    public static ArrayList<Manga> getMangas(Context context, String sortBy, boolean asc) {
        return getMangasCondition(context, null, sortBy, asc);
    }

    public static ArrayList<Manga> getMangasCondition(
            Context context, String condition, String sortBy, boolean asc) {
        if (sortBy == null) sortBy = COL_LAST_READ;
        Cursor cursor = getDatabase(context).query(
                TABLE_MANGA,
                new String[]{
                        COL_ID, COL_NAME, COL_PATH, COL_IMAGE, COL_SYNOPSIS,
                        COL_LAST_READ, COL_SERVER_ID, COL_NEW, COL_SEARCH, COL_LAST_INDEX,
                        COL_READ_ORDER, COL_AUTHOR, COL_SCROLL_SENSITIVE, COL_GENRE, COL_READER
                },
                condition, null, null, null, sortBy + (asc ? " ASC" : " DESC"));
        return getMangasFromCursor(cursor);
    }

    public static ArrayList<Manga> getMangasFromCursor(Cursor cursor) {
        ArrayList<Manga> mangas = new ArrayList<>();
        if (cursor.moveToFirst()) {
            int colId = cursor.getColumnIndex(COL_ID);
            int colServerId = cursor.getColumnIndex(COL_SERVER_ID);
            int colTitle = cursor.getColumnIndex(COL_NAME);
            int colSynopsis = cursor.getColumnIndex(COL_SYNOPSIS);
            int colImage = cursor.getColumnIndex(COL_IMAGE);
            int colWeb = cursor.getColumnIndex(COL_PATH);
            int colNew = cursor.getColumnIndex(COL_NEW);
            int colSearch = cursor.getColumnIndex(COL_SEARCH);
            int colLastIdx = cursor.getColumnIndex(COL_LAST_INDEX);
            int colOrder = cursor.getColumnIndex(COL_READ_ORDER);
            int colAuthor = cursor.getColumnIndex(COL_AUTHOR);
            int colScroll = cursor.getColumnIndex(COL_SCROLL_SENSITIVE);
            int colGenre = cursor.getColumnIndex(COL_GENRE);
            int colReader = cursor.getColumnIndex(COL_READER);

            do {
                Manga manga = new Manga(cursor.getInt(colServerId),
                        cursor.getString(colTitle), cursor.getString(colWeb), false);
                manga.setSynopsis(cursor.getString(colSynopsis));
                manga.setImages(cursor.getString(colImage));
                manga.setId(cursor.getInt(colId));
                manga.setNews(cursor.getInt(colNew));
                manga.setFinished(cursor.getInt(colSearch) > 0);
                manga.setLastIndex(cursor.getInt(colLastIdx));
                manga.setReadingDirection(cursor.getInt(colOrder));
                manga.setAuthor(cursor.getString(colAuthor));
                manga.setScrollSensitive(cursor.getFloat(colScroll));
                manga.setGenre(cursor.getString(colGenre));
                manga.setReaderType(cursor.getInt(colReader));
                mangas.add(manga);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return mangas;
    }

    public static Manga getFullManga(Context context, int mangaID) {
        return getFullManga(context, mangaID, false);
    }

    public static Manga getFullManga(Context context, int mangaID, boolean asc) {
        Manga manga = null;
        try {
            manga = getMangasCondition(context, COL_ID + "=" + mangaID, null, false).get(0);
            manga.setChapters(getChapters(context, mangaID, "1", asc));
        } catch (Exception ignore) {
            // ignore this
        }
        return manga;
    }

    public static ArrayList<Chapter> getChapters(Context c, int MangaId) {
        return getChapters(c, MangaId, "1");
    }

    public static ArrayList<Chapter> getChapters(Context c, int MangaId, String condicion) {
        return getChapters(c, MangaId, condicion, false);
    }

    public static ArrayList<Chapter> getChapters(Context c, int MangaId, String condicion, boolean asc) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        Cursor cursor = getDatabase(c).query(
                TABLE_CHAPTERS,
                new String[]{
                        COL_CAP_ID, COL_CAP_ID_MANGA, COL_CAP_NAME, COL_CAP_PATH, COL_CAP_EXTRA,
                        COL_CAP_PAGES, COL_CAP_PAG_READ, COL_CAP_STATE, COL_CAP_DOWNLOADED
                }, COL_CAP_ID_MANGA + "=" + MangaId + " AND " + condicion,
                null, null, null, COL_CAP_ID + (asc ? " ASC" : " DESC")
        );
        if (cursor.moveToFirst()) {
            int colId = cursor.getColumnIndex(COL_CAP_ID);
            int colTitle = cursor.getColumnIndex(COL_CAP_NAME);
            int colPages = cursor.getColumnIndex(COL_CAP_PAGES);
            int colWeb = cursor.getColumnIndex(COL_CAP_PATH);
            int colPageRead = cursor.getColumnIndex(COL_CAP_PAG_READ);
            int colState = cursor.getColumnIndex(COL_CAP_STATE);
            int colDownloaded = cursor.getColumnIndex(COL_CAP_DOWNLOADED);
            int colExtra = cursor.getColumnIndex(COL_CAP_EXTRA);
            do {
                Chapter cap = new Chapter(cursor.getString(colTitle), cursor.getString(colWeb));
                cap.setPages(cursor.getInt(colPages));
                cap.setId(cursor.getInt(colId));
                cap.setMangaID(MangaId);
                cap.setReadStatus(cursor.getInt(colState));
                cap.setPagesRead(cursor.getInt(colPageRead));
                cap.setDownloaded((cursor.getInt(colDownloaded) == 1));
                cap.setExtra(cursor.getString(colExtra));
                chapters.add(cap);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return chapters;
    }

    public static Chapter getChapter(Context c, int capId) {
        Chapter cap = null;
        Cursor cursor = getDatabase(c).query(
                TABLE_CHAPTERS,
                new String[]{
                        COL_CAP_ID, COL_CAP_ID_MANGA, COL_CAP_NAME, COL_CAP_PATH, COL_CAP_EXTRA,
                        COL_CAP_PAGES, COL_CAP_PAG_READ, COL_CAP_STATE, COL_CAP_DOWNLOADED
                }, COL_CAP_ID + "=" + capId, null, null, null, null);
        if (cursor.moveToFirst()) {
            int colId = cursor.getColumnIndex(COL_CAP_ID);
            int colMID = cursor.getColumnIndex(COL_CAP_ID_MANGA);
            int colTitle = cursor.getColumnIndex(COL_CAP_NAME);
            int colPages = cursor.getColumnIndex(COL_CAP_PAGES);
            int colWeb = cursor.getColumnIndex(COL_CAP_PATH);
            int colPageRead = cursor.getColumnIndex(COL_CAP_PAG_READ);
            int colState = cursor.getColumnIndex(COL_CAP_STATE);
            int colDownloaded = cursor.getColumnIndex(COL_CAP_DOWNLOADED);
            int colExtra = cursor.getColumnIndex(COL_CAP_EXTRA);

            cap = new Chapter(cursor.getString(colTitle), cursor.getString(colWeb));
            cap.setPages(cursor.getInt(colPages));
            cap.setId(cursor.getInt(colId));
            cap.setMangaID(cursor.getInt(colMID));
            cap.setReadStatus(cursor.getInt(colState));
            cap.setPagesRead(cursor.getInt(colPageRead));
            cap.setDownloaded((cursor.getInt(colDownloaded) == 1));
            cap.setExtra(cursor.getString(colExtra));
        }
        cursor.close();
        return cap;
    }

    public static void updateChapterDownloaded(Context c, int cid, int state) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_DOWNLOADED, state);
        getDatabase(c).update(TABLE_CHAPTERS, cv, COL_CAP_ID + "=" + Integer.toString(cid), null);
    }

    public static void updateChapterPlusDownload(Context context, Chapter cap) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_NAME, cap.getTitle());
        cv.put(COL_CAP_PATH, cap.getPath());
        cv.put(COL_CAP_PAGES, cap.getPages());
        cv.put(COL_CAP_STATE, cap.getReadStatus());
        cv.put(COL_CAP_PAG_READ, cap.getPagesRead());
        cv.put(COL_CAP_DOWNLOADED, cap.isDownloaded() ? 1 : 0);
        getDatabase(context).update(TABLE_CHAPTERS, cv, COL_CAP_ID + " = " + cap.getId(), null);

    }

    public static void updateChapterPage(Context c, int cid, int pages) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_PAG_READ, pages);
        getDatabase(c).update(TABLE_CHAPTERS, cv, COL_CAP_ID + "=" + Integer.toString(cid), null);
    }

    public static void deleteManga(Context c, int mid) {
        getDatabase(c).delete(TABLE_MANGA, COL_ID + " = " + mid, null);
        getDatabase(c).delete(TABLE_CHAPTERS, COL_CAP_ID_MANGA + "=" + mid, null);
    }

    public static void deleteChapter(Context context, Chapter chapter) {
        getDatabase(context).delete(TABLE_CHAPTERS, COL_CAP_ID + "=" + chapter.getId(), null);
    }

    public static Manga getManga(Context context, int mangaID) {
        Manga manga = null;
        Cursor cursor = getDatabase(context).query(TABLE_MANGA,
                new String[]{
                        COL_ID, COL_NAME, COL_PATH, COL_IMAGE, COL_SYNOPSIS,
                        COL_LAST_READ, COL_SERVER_ID, COL_NEW, COL_LAST_INDEX,
                        COL_READ_ORDER, COL_AUTHOR, COL_GENRE, COL_READER, COL_SEARCH
                },
                COL_ID + "=" + mangaID, null, null, null, COL_LAST_READ + " DESC");
        if (cursor.moveToFirst()) {
            int colId = cursor.getColumnIndex(COL_ID);
            int colServerId = cursor.getColumnIndex(COL_SERVER_ID);
            int colTitle = cursor.getColumnIndex(COL_NAME);
            int colSummary = cursor.getColumnIndex(COL_SYNOPSIS);
            int colImages = cursor.getColumnIndex(COL_IMAGE);
            int colWeb = cursor.getColumnIndex(COL_PATH);
            int colNew = cursor.getColumnIndex(COL_NEW);
            int colLastIndex = cursor.getColumnIndex(COL_LAST_INDEX);
            int colReadSense = cursor.getColumnIndex(COL_READ_ORDER);
            int colAuthor = cursor.getColumnIndex(COL_AUTHOR);
            int colGenre = cursor.getColumnIndex(COL_GENRE);
            int colReader = cursor.getColumnIndex(COL_READER);
            int colSearchAKAisFinished = cursor.getColumnIndex(COL_SEARCH);

            manga = new Manga(cursor.getInt(colServerId),
                    cursor.getString(colTitle), cursor.getString(colWeb), false);
            manga.setSynopsis(cursor.getString(colSummary));
            manga.setImages(cursor.getString(colImages));
            manga.setId(cursor.getInt(colId));
            manga.setNews(cursor.getInt(colNew));
            manga.setLastIndex(cursor.getInt(colLastIndex));
            manga.setReadingDirection(cursor.getInt(colReadSense));
            manga.setAuthor(cursor.getString(colAuthor));
            manga.setGenre(cursor.getString(colGenre));
            manga.setReaderType(cursor.getInt(colReader));
            manga.setFinished(cursor.getInt(colSearchAKAisFinished) > 0);
        }
        cursor.close();
        return manga;
    }

    public static void markChapter(Context c, int capId, boolean read) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_STATE, read ? Chapter.READ : Chapter.UNREAD);
        getDatabase(c).update(TABLE_CHAPTERS, cv, COL_CAP_ID + " = " + capId, null);
    }

    public static void markAllChapters(Context c, int mangaId, boolean read) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_STATE, read ? Chapter.READ : Chapter.UNREAD);
        getDatabase(c).update(TABLE_CHAPTERS, cv, COL_CAP_ID_MANGA + " = " + mangaId, null);
    }

    public static void removeOrphanedChapters(Context c) {
        getDatabase(c).delete(TABLE_CHAPTERS, COL_CAP_ID_MANGA + "= -1", null);
    }

    public static void updateReadOrder(Context c, int ordinal, int mid) {
        ContentValues cv = new ContentValues();
        cv.put(COL_READ_ORDER, ordinal);
        getDatabase(c).update(TABLE_MANGA, cv, COL_ID + "=" + mid, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (context.getDatabasePath("mangas.db").exists() && !(doesTableExist(db, TABLE_MANGA))) {
            //move to new path
            copyDbToSd(context);
            db.close();
            Util.getInstance().restartApp(context);
        } else {
            db.execSQL(DATABASE_MANGA_CREATE);
            db.execSQL(DATABASE_CHAPTERS_CREATE);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < 10){
            db.execSQL("ALTER TABLE " + TABLE_MANGA +" ADD COLUMN " + COL_SCROLL_SENSITIVE + " NUMERICAL DEFAULT -1.1");
        }
        if (oldVersion < 11) {
            db.execSQL("ALTER TABLE " + TABLE_MANGA + " ADD COLUMN " + COL_GENRE + " TEXT NOT NULL DEFAULT 'N/A'");
        }
        if(oldVersion < 12){
            db.execSQL("ALTER TABLE " + TABLE_MANGA + " ADD COLUMN " + COL_READER + " INTEGER DEFAULT 0");
        }
        if(oldVersion < 13){
            db.execSQL("ALTER TABLE " + TABLE_CHAPTERS + " ADD COLUMN " + COL_CAP_EXTRA + " TEXT");
        }
    }

    public void copyDbToSd(Context c) {
        File dbFile = c.getDatabasePath("mangas.db");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        String ruta = sp.getString("directorio",
                Environment.getExternalStorageDirectory().getAbsolutePath()) + "/MiMangaNu/";
        ruta += "dbs/";
        File exportDir = new File(ruta, "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File file = new File(exportDir, dbFile.getName());
        try {
            file.createNewFile();
            InputStream is = new FileInputStream(dbFile);
            FileCache.writeFile(is, file);
            is.close();
        } catch (IOException e) {
            Toast.makeText(c, "Error: ", Toast.LENGTH_LONG).show();
        }
    }

    public boolean doesTableExist(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery(
                "select DISTINCT tbl_name from sqlite_master where tbl_name = '" +
                        tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }
}
