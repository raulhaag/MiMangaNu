package ar.rulosoft.mimanganu.componentes;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.fedorvlasov.lazylist.FileCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.utils.Util;

public class Database extends SQLiteOpenHelper {

    public static final String COL_NAME = "nombre";
    public static final String COL_LAST_READ = "ultima";
    public static final String COL_ID = "id";
    public static final String COL_IS_FINISHED = "burcar";// buscar updates / isFinished
    public static final String COL_AUTHOR = "autor";
    public static final String COL_CAP_STATE = "estado";
    public static final String COL_CAP_DOWNLOADED = "descargado";
    // Table for entire manga
    public static final String COL_SERVER_ID = "server_id";
    private static final String TABLE_MANGA = "manga";
    private static final String COL_PATH = "path";
    private static final String COL_IMAGE = "imagen";
    private static final String COL_SYNOPSIS = "sinopsis";
    private static final String COL_LAST_INDEX = "last_index";// indice listview
    private static final String COL_NEW = "nuevos";// hay nuevos?
    private static final String COL_SCROLL_SENSITIVE = "scroll_s";
    private static final String COL_GENRE = "genres";
    private static final String COL_READ_ORDER = "orden_lectura";// sentido de
    private static final String COL_READER = "reader";
    private static final String COL_LAST_UPDATE = "last_update";
    public static final String COL_LAST_UPDATE_LONG = "last_uodate_long";
    public static final String COL_VAULT = "vault";

    // Table for each chapter
    public static final String TABLE_CHAPTERS = "capitulos";
    private static final String COL_CAP_ID_MANGA = "manga_id";
    private static final String COL_CAP_NAME = "nombre";
    private static final String COL_CAP_PATH = "path";
    public static final String COL_CAP_PAGES = "paginas";
    public static final String COL_CAP_PAG_READ = "leidas";
    public static final String COL_CAP_ID = "id";
    private static final String COL_CAP_EXTRA = "extra";
    // Database creation sql statement
    private static final String DATABASE_MANGA_CREATE = "create table " +
            TABLE_MANGA + "(" +
            COL_ID + " integer primary key autoincrement, " +
            COL_NAME + " text not null," +
            COL_PATH + " text not null, " +
            COL_IMAGE + " text," +
            COL_SYNOPSIS + " text," +
            COL_SERVER_ID + "," +
            COL_LAST_READ + " int," +
            COL_NEW + " int DEFAULT 0," +
            COL_LAST_INDEX + " int DEFAULT 0, " +
            COL_IS_FINISHED + " int DEFAULT 0, " +
            COL_READ_ORDER + " int not null DEFAULT -1, " +
            COL_AUTHOR + " TEXT NOT NULL DEFAULT 'N/A'," +
            COL_SCROLL_SENSITIVE + " NUMERICAL DEFAULT -1.1," +
            COL_READER + " INTEGER DEFAULT 0," +
            COL_GENRE + " TEXT NOT NULL DEFAULT 'N/A'," +
            COL_LAST_UPDATE + " TEXT DEFAULT 'N/A', " +
            COL_LAST_UPDATE_LONG + " INT DEFAULT 0, " +
            COL_VAULT + " TEXT DEFAULT '', " +
            "UNIQUE (" + COL_SERVER_ID + ", " + COL_PATH + "));";
    private static final String DATABASE_CHAPTERS_CREATE = "create table " +
            TABLE_CHAPTERS + "(" +
            COL_CAP_ID + " integer primary key autoincrement, " +
            COL_CAP_NAME + " text not null," +
            COL_CAP_PATH + " text not null, " +
            COL_CAP_PAGES + " int," +
            COL_CAP_ID_MANGA + " int," +
            COL_CAP_STATE + " int DEFAULT 0," +
            COL_CAP_PAG_READ + " int DEFAULT 1, " +
            COL_CAP_DOWNLOADED + " int DEFAULT 0, " +
            COL_CAP_EXTRA + " text, " +
            "UNIQUE (" + COL_CAP_ID_MANGA + ", " + COL_CAP_PATH + "));";
    public static boolean is_in_update_process = false;
    // name and path of database
    private static String database_name;
    private static String database_path;
    private static int database_version = 31;
    private static SQLiteDatabase localDB;
    Context context;

    // make private, should be single instance
    private Database(Context context) {
        super(context, database_path + database_name, null, database_version);
        this.context = context;
    }

    public static SQLiteDatabase getDatabase(Context context) {
        // Setup path and database name
        if (database_path == null || database_path.length() == 0) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            database_path = (prefs.getString("directorio", Environment.getExternalStorageDirectory().getAbsolutePath()) + "/MiMangaNu/") + "dbs/";
            database_name = "mangas.db";
        }
        if (!new File(database_path).exists()) {
            if (!new File(database_path).mkdirs()) {
                Log.e("Database", "failed to create database directory");
            }
        }
        if ((localDB == null) || !localDB.isOpen()) {
            try {
                localDB = new Database(context).getWritableDatabase(); // Now it's writable! I think.
            } catch (SQLiteDatabaseCorruptException sqldce) {
                Log.e("Database", "SQLiteDatabaseCorruptException", sqldce);
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_while_trying_to_open_db));
            } catch (SQLiteCantOpenDatabaseException sqlcode) {
                Log.e("Database", "SQLiteCantOpenDatabaseException", sqlcode);
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_while_trying_to_open_db));
            } catch (SQLException sqle) {
                Log.e("Database", "SQLException", sqle);
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_while_trying_to_open_db));
            } catch (Exception e) {
                Log.e("Database", "Exception", e);
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_while_trying_to_open_db));
            }
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
        cv.put(COL_IS_FINISHED, manga.isFinished() ? 1 : 0);
        cv.put(COL_READER, manga.getReaderType());
        cv.put(COL_LAST_UPDATE, manga.getLastUpdate());
        cv.put(COL_VAULT, manga.getVault());
        if (setTime)
            cv.put(COL_LAST_READ, System.currentTimeMillis());
        return cv;
    }

    public static int addManga(Context context, Manga manga) {
        int tmp = -1;
        try {
            SQLiteDatabase database = getDatabase(context);
            if (!database.isReadOnly())
                tmp = (int) database.insertOrThrow(TABLE_MANGA, null, setMangaCV(manga, true));
            else {
                Log.e("Database", "(addManga) " + context.getResources().getString(R.string.error_database_is_read_only));
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_database_is_read_only));
            }
        } catch (Exception e) {
            Log.e("Database", Log.getStackTraceString(e));
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
        try {
            SQLiteDatabase database = getDatabase(context);
            if (!database.isReadOnly())
                database.update(TABLE_MANGA, setMangaCV(manga, setTime), COL_ID + "=" + manga.getId(), null);
            else {
                Log.e("Database", "(updateManga) " + context.getResources().getString(R.string.error_database_is_read_only));
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_database_is_read_only));
            }
        } catch (SQLiteFullException sqlfe) {
            Log.e("Database", "SQLiteFullException", sqlfe);
            outputMangaDebugInformation(manga);
            Util.getInstance().toast(context, context.getString(R.string.error_while_updating_chapter_or_manga_in_db, manga.getTitle()));
        } catch (SQLiteDiskIOException sqldioe) {
            Log.e("Database", "SQLiteDiskIOException", sqldioe);
            outputMangaDebugInformation(manga);
            Util.getInstance().toast(context, context.getString(R.string.error_while_updating_chapter_or_manga_in_db, manga.getTitle()));
        } catch (Exception e) {
            Log.e("Database", "Exception", e);
            outputMangaDebugInformation(manga);
            Util.getInstance().toast(context, context.getString(R.string.error_while_updating_chapter_or_manga_in_db, manga.getTitle()));
        }
    }

    public static void updateMangaRead(Context context, int mid) {
        ContentValues cv = new ContentValues();
        cv.put(COL_LAST_READ, System.currentTimeMillis());
        cv.put(COL_NEW, 0);
        try {
            SQLiteDatabase database = getDatabase(context);
            if (!database.isReadOnly())
                database.update(TABLE_MANGA, cv, COL_ID + "=" + mid, null);
            else {
                Log.e("Database", "(updateMangaRead) " + context.getResources().getString(R.string.error_database_is_read_only));
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_database_is_read_only));
            }
        } catch (SQLiteFullException sqlfe) {
            Log.e("Database", "SQLiteFullException", sqlfe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (SQLiteDiskIOException sqldioe) {
            Log.e("Database", "SQLiteDiskIOException", sqldioe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (Exception e) {
            Log.e("Database", "Exception", e);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        }
    }

    public static void setUpgradable(Context context, int mangaId, boolean search) {
        ContentValues cv = new ContentValues();
        cv.put(COL_IS_FINISHED, search ? 1 : 0);
        try {
            SQLiteDatabase database = getDatabase(context);
            if (!database.isReadOnly())
                database.update(TABLE_MANGA, cv, COL_ID + "=" + mangaId, null);
            else {
                Log.e("Database", "(setUpgradable) " + context.getResources().getString(R.string.error_database_is_read_only));
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_database_is_read_only));
            }
        } catch (SQLiteFullException sqlfe) {
            Log.e("Database", "SQLiteFullException", sqlfe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (SQLiteDiskIOException sqldioe) {
            Log.e("Database", "SQLiteDiskIOException", sqldioe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (Exception e) {
            Log.e("Database", "Exception", e);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        }
    }

    public static void updateMangaLastIndex(Context context, int mid, int idx) {
        ContentValues cv = new ContentValues();
        cv.put(COL_LAST_INDEX, idx);
        try {
            SQLiteDatabase database = getDatabase(context);
            if (!database.isReadOnly())
                database.update(TABLE_MANGA, cv, COL_ID + "=" + mid, null);
            else {
                Log.e("Database", "(updateMangaLastIndex) " + context.getResources().getString(R.string.error_database_is_read_only));
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_database_is_read_only));
            }
        } catch (SQLiteFullException sqlfe) {
            Log.e("Database", "SQLiteFullException", sqlfe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (SQLiteDiskIOException sqldioe) {
            Log.e("Database", "SQLiteDiskIOException", sqldioe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (Exception e) {
            Log.e("Database", "Exception", e);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        }
    }

    public static void updateMangaScrollSensitive(Context context, int mid, float nScroll) {
        ContentValues cv = new ContentValues();
        cv.put(COL_SCROLL_SENSITIVE, nScroll);
        try {
            SQLiteDatabase database = getDatabase(context);
            if (!database.isReadOnly())
                database.update(TABLE_MANGA, cv, COL_ID + "=" + mid, null);
            else {
                Log.e("Database", "(updateMangaScrollSensitive) " + context.getResources().getString(R.string.error_database_is_read_only));
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_database_is_read_only));
            }
        } catch (SQLiteFullException sqlfe) {
            Log.e("Database", "SQLiteFullException", sqlfe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (SQLiteDiskIOException sqldioe) {
            Log.e("Database", "SQLiteDiskIOException", sqldioe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (Exception e) {
            Log.e("Database", "Exception", e);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        }
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

        if (actual <= 0) {
            cv.put(COL_NEW, 0);
        } else {
            cv.put(COL_NEW, actual);
            cv.put(COL_LAST_UPDATE_LONG, (System.currentTimeMillis() / 1000));
        }
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
            SQLiteDatabase database = getDatabase(context);
            if (!database.isReadOnly())
                database.insertOrThrow(TABLE_CHAPTERS, null, contentValues);
            else {
                Log.e("Database", "(addChapterLast) " + context.getResources().getString(R.string.error_database_is_read_only));
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_database_is_read_only));
            }
        } catch (SQLiteConstraintException sqlce) {
            // remove orphaned chapters and try again
            try {
                removeOrphanedChapters(context);
                getDatabase(context).insertOrThrow(TABLE_CHAPTERS, null, contentValues);
            } catch (Exception e) {
                Log.e("Database", "Exception", e);
                outputChapterDebugInformation(chapter, mangaId);
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_while_adding_chapter_or_manga_to_db, chapter.getTitle()));
            }
        } catch (Exception e) {
            Log.e("Database", "Exception", e);
            outputChapterDebugInformation(chapter, mangaId);
            Util.getInstance().toast(context, context.getResources().getString(R.string.error_while_adding_chapter_or_manga_to_db, chapter.getTitle()));
        }
    }

    public static void updateChapter(Context context, Chapter chapter) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_NAME, chapter.getTitle());
        cv.put(COL_CAP_PATH, chapter.getPath());
        cv.put(COL_CAP_PAGES, chapter.getPages());
        cv.put(COL_CAP_STATE, chapter.getReadStatus());
        cv.put(COL_CAP_PAG_READ, chapter.getPagesRead());
        cv.put(COL_CAP_EXTRA, chapter.getExtra());
        try {
            SQLiteDatabase database = getDatabase(context);
            if (!database.isReadOnly())
                database.update(TABLE_CHAPTERS, cv, COL_CAP_ID + " = " + chapter.getId(), null);
            else {
                Log.e("Database", "(updateChapter) " + context.getResources().getString(R.string.error_database_is_read_only));
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_database_is_read_only));
            }
        } catch (SQLiteFullException sqlfe) {
            Log.e("Database", "SQLiteFullException", sqlfe);
            outputChapterDebugInformation(chapter);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (SQLiteDiskIOException sqldioe) {
            Log.e("Database", "SQLiteDiskIOException", sqldioe);
            outputChapterDebugInformation(chapter);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (Exception e) {
            Log.e("Database", "Exception", e);
            outputChapterDebugInformation(chapter);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        }
    }

    private static void outputMangaDebugInformation(Manga manga) {
        Log.i("Database", "Title: " + manga.getTitle());
        Log.i("Database", "ID: " + manga.getId());
        Log.i("Database", "Path: " + manga.getPath());
        Log.i("Database", "Server ID: " + manga.getServerId());
    }

    private static void outputChapterDebugInformation(Chapter chapter, int mangaId) {
        Log.i("Database", "Manga_ID: " + mangaId);
        Log.i("Database", "Title: " + chapter.getTitle());
        Log.i("Database", "Path: " + chapter.getPath());
        Log.i("Database", "Pages: " + chapter.getPages());
        Log.i("Database", "Pages Read: " + chapter.getPagesRead());
        Log.i("Database", "Read Status: " + chapter.getReadStatus());
        Log.i("Database", "isDownloaded: " + chapter.isDownloaded());
        //Log.i("Database", "Extra: " + chapter.getExtra());
    }

    private static void outputChapterDebugInformation(Chapter chapter) {
        Log.i("Database", "Title: " + chapter.getTitle());
        Log.i("Database", "Path: " + chapter.getPath());
        Log.i("Database", "Pages: " + chapter.getPages());
        Log.i("Database", "Pages Read: " + chapter.getPagesRead());
        Log.i("Database", "Read Status: " + chapter.getReadStatus());
        Log.i("Database", "isDownloaded: " + chapter.isDownloaded());
        //Log.i("Database", "Extra: " + chapter.getExtra());
    }

    public static ArrayList<Manga> getFromFolderMangas(Context context) {
        return getMangasCondition(context, COL_SERVER_ID + "= 1001", null, false);
    }

    public static ArrayList<Manga> getMangasForUpdates(Context context) {
        return getMangasCondition(context, COL_IS_FINISHED + "= 0 AND " + COL_VAULT + " = ''", null, false);
    }

    public static ArrayList<Manga> getMangas(Context context, String sortBy, boolean asc) {
        return getMangasCondition(context, COL_VAULT + " = ''", sortBy, asc);
    }

    public static ArrayList<Manga> getMangasVault(Context context, String vault, String sortBy, boolean asc) {
        return getMangasCondition(context, COL_VAULT + " = '" + vault + "'", sortBy, asc);
    }

    public static ArrayList<Manga> getMangasCondition(
            Context context, String condition, String sortBy, boolean asc) {
        if (sortBy == null) sortBy = COL_LAST_READ;
        Cursor cursor = null;
        if (getDatabase(context) != null) {
            cursor = getDatabase(context).query(
                    TABLE_MANGA,
                    new String[]{
                            COL_ID, COL_NAME, COL_PATH, COL_IMAGE, COL_SYNOPSIS,
                            COL_LAST_READ, COL_SERVER_ID, COL_NEW, COL_IS_FINISHED, COL_LAST_INDEX,
                            COL_READ_ORDER, COL_AUTHOR, COL_SCROLL_SENSITIVE, COL_GENRE, COL_READER,
                            COL_LAST_UPDATE, COL_VAULT
                    },
                    condition, null, null, null, sortBy + (asc ? " ASC" : " DESC"));
        }
        return getMangasFromCursor(cursor);
    }

    private static ArrayList<Manga> getMangasFromCursor(Cursor cursor) {
        ArrayList<Manga> mangas = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int colId = cursor.getColumnIndex(COL_ID);
                int colServerId = cursor.getColumnIndex(COL_SERVER_ID);
                int colTitle = cursor.getColumnIndex(COL_NAME);
                int colSynopsis = cursor.getColumnIndex(COL_SYNOPSIS);
                int colImage = cursor.getColumnIndex(COL_IMAGE);
                int colWeb = cursor.getColumnIndex(COL_PATH);
                int colNew = cursor.getColumnIndex(COL_NEW);
                int colIsFinished = cursor.getColumnIndex(COL_IS_FINISHED);
                int colLastIdx = cursor.getColumnIndex(COL_LAST_INDEX);
                int colOrder = cursor.getColumnIndex(COL_READ_ORDER);
                int colAuthor = cursor.getColumnIndex(COL_AUTHOR);
                int colScroll = cursor.getColumnIndex(COL_SCROLL_SENSITIVE);
                int colGenre = cursor.getColumnIndex(COL_GENRE);
                int colReader = cursor.getColumnIndex(COL_READER);
                int colLastUpdate = cursor.getColumnIndex(COL_LAST_UPDATE);
                int colVault = cursor.getColumnIndex(COL_VAULT);

                do {
                    Manga manga = new Manga(cursor.getInt(colServerId),
                            cursor.getString(colTitle), cursor.getString(colWeb), false);
                    manga.setSynopsis(cursor.getString(colSynopsis));
                    manga.setImages(cursor.getString(colImage));
                    manga.setId(cursor.getInt(colId));
                    manga.setNews(cursor.getInt(colNew));
                    manga.setFinished(cursor.getInt(colIsFinished) > 0);
                    manga.setLastIndex(cursor.getInt(colLastIdx));
                    manga.setReadingDirection(cursor.getInt(colOrder));
                    manga.setAuthor(cursor.getString(colAuthor));
                    manga.setScrollSensitive(cursor.getFloat(colScroll));
                    manga.setGenre(cursor.getString(colGenre));
                    manga.setReaderType(cursor.getInt(colReader));
                    manga.setLastUpdate(cursor.getString(colLastUpdate));
                    manga.setVault(cursor.getString(colVault));
                    mangas.add(manga);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return mangas;
    }

    public static Manga getFullManga(Context context, int mangaID) {
        return getFullManga(context, mangaID, false);
    }

    private static Manga getFullManga(Context context, int mangaID, boolean asc) {
        Manga manga = null;
        try {
            manga = getMangasCondition(context, COL_ID + "=" + mangaID, null, false).get(0);
            manga.setChapters(getChapters(context, mangaID, "1", asc));
        } catch (Exception ignore) {
            Log.d("Database", "Exception", ignore);
        }
        return manga;
    }

    public static ArrayList<Chapter> getChapters(Context c, int MangaId) {
        return getChapters(c, MangaId, "1");
    }

    public static ArrayList<Chapter> getChapters(Context c, int MangaId, String condition) {
        return getChapters(c, MangaId, condition, false);
    }

    public static ArrayList<Chapter> getChapters(Context c, int MangaId, String condition, boolean asc) {
        Cursor cursor = getDatabase(c).query(
                TABLE_CHAPTERS,
                new String[]{
                        COL_CAP_ID, COL_CAP_ID_MANGA, COL_CAP_NAME, COL_CAP_PATH, COL_CAP_EXTRA,
                        COL_CAP_PAGES, COL_CAP_PAG_READ, COL_CAP_STATE, COL_CAP_DOWNLOADED
                }, COL_CAP_ID_MANGA + "=" + MangaId + " AND " + condition,
                null, null, null, COL_CAP_ID + (asc ? " ASC" : " DESC")
        );
        return getChapterFromCursor(cursor);
    }

    public static ArrayList<Chapter> getChapterFromCursor(Cursor cursor) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        if (cursor.moveToFirst()) {
            int colId = cursor.getColumnIndex(COL_CAP_ID);
            int colTitle = cursor.getColumnIndex(COL_CAP_NAME);
            int colPages = cursor.getColumnIndex(COL_CAP_PAGES);
            int colWeb = cursor.getColumnIndex(COL_CAP_PATH);
            int colPageRead = cursor.getColumnIndex(COL_CAP_PAG_READ);
            int colState = cursor.getColumnIndex(COL_CAP_STATE);
            int colDownloaded = cursor.getColumnIndex(COL_CAP_DOWNLOADED);
            int colExtra = cursor.getColumnIndex(COL_CAP_EXTRA);
            int colMID = cursor.getColumnIndex(COL_CAP_ID_MANGA);
            do {
                Chapter cap = new Chapter(cursor.getString(colTitle), cursor.getString(colWeb));
                cap.setPages(cursor.getInt(colPages));
                cap.setId(cursor.getInt(colId));
                cap.setMangaID(cursor.getInt(colMID));
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

    public static void updateChapterDownloaded(Context context, int cid, int state) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_DOWNLOADED, state);
        try {
            SQLiteDatabase database = getDatabase(context);
            if (!database.isReadOnly())
                database.update(TABLE_CHAPTERS, cv, COL_CAP_ID + "=" + cid, null);
            else {
                Log.e("Database", "(updateChapterDownloaded) " + context.getResources().getString(R.string.error_database_is_read_only));
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_database_is_read_only));
            }
        } catch (SQLiteFullException sqlfe) {
            Log.e("Database", "SQLiteFullException", sqlfe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (SQLiteDiskIOException sqldioe) {
            Log.e("Database", "SQLiteDiskIOException", sqldioe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (Exception e) {
            Log.e("Database", "Exception", e);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        }
    }

    static void updateChapterPlusDownload(Context context, Chapter cap) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_NAME, cap.getTitle());
        cv.put(COL_CAP_PATH, cap.getPath());
        cv.put(COL_CAP_PAGES, cap.getPages());
        cv.put(COL_CAP_STATE, cap.getReadStatus());
        cv.put(COL_CAP_PAG_READ, cap.getPagesRead());
        cv.put(COL_CAP_DOWNLOADED, cap.isDownloaded() ? 1 : 0);
        try {
            SQLiteDatabase database = getDatabase(context);
            if (!database.isReadOnly())
                database.update(TABLE_CHAPTERS, cv, COL_CAP_ID + " = " + cap.getId(), null);
            else {
                Log.e("Database", "(updateChapterPlusDownload) " + context.getResources().getString(R.string.error_database_is_read_only));
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_database_is_read_only));
            }
        } catch (SQLiteFullException sqlfe) {
            Log.e("Database", "SQLiteFullException", sqlfe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (SQLiteDiskIOException sqldioe) {
            Log.e("Database", "SQLiteDiskIOException", sqldioe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (Exception e) {
            Log.e("Database", "Exception", e);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        }
    }

    public static void updateChapterPage(Context context, int cid, int pages) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_PAG_READ, pages);
        try {
            SQLiteDatabase database = getDatabase(context);
            if (!database.isReadOnly())
                database.update(TABLE_CHAPTERS, cv, COL_CAP_ID + "=" + cid, null);
            else {
                Log.e("Database", "(updateChapterPage) " + context.getResources().getString(R.string.error_database_is_read_only));
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_database_is_read_only));
            }
        } catch (SQLiteFullException sqlfe) {
            Log.e("Database", "SQLiteFullException", sqlfe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (SQLiteDiskIOException sqldioe) {
            Log.e("Database", "SQLiteDiskIOException", sqldioe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (Exception e) {
            Log.e("Database", "Exception", e);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        }
    }

    public static void deleteManga(Context context, int mid) {
        SQLiteDatabase database = getDatabase(context);
        if (!database.isReadOnly()) {
            database.delete(TABLE_MANGA, COL_ID + " = " + mid, null);
            database.delete(TABLE_CHAPTERS, COL_CAP_ID_MANGA + "=" + mid, null);
        } else {
            Log.e("Database", "(deleteManga) " + context.getResources().getString(R.string.error_database_is_read_only));
            Util.getInstance().toast(context, context.getResources().getString(R.string.error_database_is_read_only));
        }
    }

    static void deleteChapter(Context context, Chapter chapter) {
        SQLiteDatabase database = getDatabase(context);
        if (!database.isReadOnly()) {
            database.delete(TABLE_CHAPTERS, COL_CAP_ID + "=" + chapter.getId(), null);
        } else {
            Log.e("Database", "(deleteChapter) " + context.getResources().getString(R.string.error_database_is_read_only));
            Util.getInstance().toast(context, context.getResources().getString(R.string.error_database_is_read_only));
        }
    }

    public static Manga getManga(Context context, int mangaID) {
        Manga manga = null;
        Cursor cursor = getDatabase(context).query(TABLE_MANGA,
                new String[]{
                        COL_ID, COL_NAME, COL_PATH, COL_IMAGE, COL_SYNOPSIS,
                        COL_LAST_READ, COL_SERVER_ID, COL_NEW, COL_LAST_INDEX,
                        COL_READ_ORDER, COL_AUTHOR, COL_GENRE, COL_READER, COL_IS_FINISHED,
                        COL_LAST_UPDATE, COL_VAULT
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
            int colIsFinished = cursor.getColumnIndex(COL_IS_FINISHED);
            int colLastUpdate = cursor.getColumnIndex(COL_LAST_UPDATE);
            int colVault = cursor.getColumnIndex(COL_VAULT);

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
            manga.setFinished(cursor.getInt(colIsFinished) > 0);
            manga.setLastUpdate(cursor.getString(colLastUpdate));
            manga.setVault(cursor.getString(colVault));
        }
        cursor.close();
        return manga;
    }

    static void markChapter(Context context, int capId, boolean read) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_STATE, read ? Chapter.READ : Chapter.UNREAD);
        try {
            SQLiteDatabase database = getDatabase(context);
            if (!database.isReadOnly()) {
                database.update(TABLE_CHAPTERS, cv, COL_CAP_ID + " = " + capId, null);
            } else {
                Log.e("Database", "(markChapter) " + context.getResources().getString(R.string.error_database_is_read_only));
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_database_is_read_only));
            }
        } catch (SQLiteFullException sqlfe) {
            Log.e("Database", "SQLiteFullException", sqlfe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (SQLiteDiskIOException sqldioe) {
            Log.e("Database", "SQLiteDiskIOException", sqldioe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (Exception e) {
            Log.e("Database", "Exception", e);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        }
    }

    public static void markAllChapters(Context context, int mangaId, boolean read) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_STATE, read ? Chapter.READ : Chapter.UNREAD);
        try {
            SQLiteDatabase database = getDatabase(context);
            if (!database.isReadOnly()) {
                database.update(TABLE_CHAPTERS, cv, COL_CAP_ID_MANGA + " = " + mangaId, null);
            } else {
                Log.e("Database", "(markAllChapters) " + context.getResources().getString(R.string.error_database_is_read_only));
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_database_is_read_only));
            }
        } catch (SQLiteFullException sqlfe) {
            Log.e("Database", "SQLiteFullException", sqlfe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (SQLiteDiskIOException sqldioe) {
            Log.e("Database", "SQLiteDiskIOException", sqldioe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (Exception e) {
            Log.e("Database", "Exception", e);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        }
    }

    public static void setNotDownloadedAllChapter(Context context) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_DOWNLOADED, 0);
        try {
            SQLiteDatabase database = getDatabase(context);
            if (!database.isReadOnly()) {
                database.update(TABLE_CHAPTERS, cv, null, null);
            } else {
                Log.e("Database", "(markChapter) " + context.getResources().getString(R.string.error_database_is_read_only));
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_database_is_read_only));
            }
        } catch (SQLiteFullException sqlfe) {
            Log.e("Database", "SQLiteFullException", sqlfe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (SQLiteDiskIOException sqldioe) {
            Log.e("Database", "SQLiteDiskIOException", sqldioe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (Exception e) {
            Log.e("Database", "Exception", e);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        }
    }

    private static void removeOrphanedChapters(Context c) {
        getDatabase(c).delete(TABLE_CHAPTERS, COL_CAP_ID_MANGA + "= -1", null);
    }

    public static void updateReadOrder(Context context, int ordinal, int mid) {
        ContentValues cv = new ContentValues();
        cv.put(COL_READ_ORDER, ordinal);
        try {
            SQLiteDatabase database = getDatabase(context);
            if (!database.isReadOnly()) {
                database.update(TABLE_MANGA, cv, COL_ID + "=" + mid, null);
            } else {
                Log.e("Database", "(updateReadOrder) " + context.getResources().getString(R.string.error_database_is_read_only));
                Util.getInstance().toast(context, context.getResources().getString(R.string.error_database_is_read_only));
            }
        } catch (SQLiteFullException sqlfe) {
            Log.e("Database", "SQLiteFullException", sqlfe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (SQLiteDiskIOException sqldioe) {
            Log.e("Database", "SQLiteDiskIOException", sqldioe);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        } catch (Exception e) {
            Log.e("Database", "Exception", e);
            Util.getInstance().toast(context, context.getString(R.string.error_while_trying_to_update_db));
        }
    }

    public static void vacuumDatabase(Context context) {
        SQLiteDatabase database = getDatabase(context);
        try {
            database.execSQL("VACUUM");
        } catch (Exception e) {
            Log.e("Database", "Exception", e);
        }
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
        // create a backup of the old database in case the upgrade should fail for some reason
        // if a backup of the old version already exists, do not overwrite it
        try {
            is_in_update_process = true;
            if (newVersion != 30) {//TODO REMOVE ON FUTURES VERSIONS
                try {
                    String backup = db.getPath() + "." + oldVersion + ".bak";
                    if (!new File(backup).exists()) {
                        Util.getInstance().copyFile(new File(db.getPath()), new File(backup));
                    }
                } catch (IOException e) {
                    Log.e("Database backup error", "Exception", e);
                }
            }

            if (oldVersion < 10) {
                db.execSQL("ALTER TABLE " + TABLE_MANGA + " ADD COLUMN " + COL_SCROLL_SENSITIVE + " NUMERICAL DEFAULT -1.1");
            }
            if (oldVersion < 11) {
                db.execSQL("ALTER TABLE " + TABLE_MANGA + " ADD COLUMN " + COL_GENRE + " TEXT NOT NULL DEFAULT 'N/A'");
            }
            if (oldVersion < 12) {
                db.execSQL("ALTER TABLE " + TABLE_MANGA + " ADD COLUMN " + COL_READER + " INTEGER DEFAULT 0");
            }
            if (oldVersion < 13) {
                db.execSQL("ALTER TABLE " + TABLE_CHAPTERS + " ADD COLUMN " + COL_CAP_EXTRA + " TEXT");
            }
            if (oldVersion < 14) {
                db.execSQL("ALTER TABLE " + TABLE_MANGA + " ADD COLUMN " + COL_LAST_UPDATE + " TEXT DEFAULT 'N/A'");
            }
            if (oldVersion < 15) {
                db.execSQL("DELETE FROM " + TABLE_CHAPTERS + " WHERE " + COL_CAP_PATH + " LIKE '%hitmanga.eu%'");
            }
            if (oldVersion < 16) {
                String query = "UPDATE " + TABLE_MANGA + " SET " + COL_PATH +
                        " = REPLACE(" + COL_PATH + ", 'www.readmanga.today', 'www.readmng.com') WHERE " +
                        COL_SERVER_ID + "=29";
                db.execSQL(query);
                query = "UPDATE " + TABLE_CHAPTERS + " SET " + COL_CAP_PATH +
                        " = REPLACE(" + COL_CAP_PATH + ", 'www.readmanga.today', 'www.readmng.com') WHERE 1";
                db.execSQL(query);
                query = "UPDATE " + TABLE_MANGA + " SET " + COL_PATH +
                        " = REPLACE(" + COL_PATH + ", 'mangapedia.fr', 'mangapedia.eu') WHERE " +
                        COL_SERVER_ID + "=34";
                db.execSQL(query);
                query = "UPDATE " + TABLE_CHAPTERS + " SET " + COL_CAP_PATH +
                        " = REPLACE(" + COL_CAP_PATH + ", 'mangapedia.fr', 'mangapedia.eu') WHERE 1";
                db.execSQL(query);
            }
            if (oldVersion < 17) {
                String query = "UPDATE " + TABLE_MANGA + " SET " + COL_PATH +
                        " = REPLACE(" + COL_PATH + ", 'mangapedia.eu', 'mangapedia.fr') WHERE " +
                        COL_SERVER_ID + "=34";
                db.execSQL(query);
                query = "UPDATE " + TABLE_CHAPTERS + " SET " + COL_CAP_PATH +
                        " = REPLACE(" + COL_CAP_PATH + ", 'mangapedia.eu', 'mangapedia.fr') WHERE 1";
                db.execSQL(query);
            }
            if (oldVersion < 18) {
                db.execSQL("CREATE TABLE temp_chapters AS SELECT * FROM " + TABLE_CHAPTERS + ";");
                db.execSQL("DROP TABLE " + TABLE_CHAPTERS + ";");
                db.execSQL(DATABASE_CHAPTERS_CREATE);
                db.execSQL("INSERT INTO capitulos (id, nombre, path, paginas, manga_id, estado, leidas, descargado, extra) " +
                        "SELECT id, nombre, path, paginas, manga_id, estado, leidas, descargado, extra FROM temp_chapters; ");
                db.execSQL("DROP TABLE temp_chapters;");
            }
            if (oldVersion < 19) {
                String query = "DELETE FROM capitulos WHERE id IN (SELECT id FROM capitulos WHERE path LIKE '//www.mangahere.co%' OR path LIKE 'http://www.mangahere.cc%');";
                db.execSQL(query);
                query = "UPDATE " + TABLE_MANGA + " SET " + COL_PATH +
                        " = REPLACE(" + COL_PATH + ", 'https://www.mangahere.co', 'http://www.mangahere.cc') WHERE " +
                        COL_SERVER_ID + "=4";
                db.execSQL(query);
                query = "UPDATE " + TABLE_MANGA + " SET " + COL_PATH +
                        " = REPLACE(" + COL_PATH + ", 'http://www.mangahere.co', 'http://www.mangahere.cc') WHERE " +
                        COL_SERVER_ID + "=4";
                db.execSQL(query);
                query = "UPDATE " + TABLE_CHAPTERS + " SET " + COL_CAP_PATH +
                        " = REPLACE(" + COL_CAP_PATH + ", 'http://www.mangahere.co', 'http://www.mangahere.cc') WHERE 1";
                db.execSQL(query);
                query = "UPDATE " + TABLE_CHAPTERS + " SET " + COL_CAP_PATH +
                        " = REPLACE(" + COL_CAP_PATH + ", 'https://www.mangahere.co', 'http://www.mangahere.cc') WHERE 1";
                db.execSQL(query);
            }
            if (oldVersion < 20) {
                String query = "UPDATE " + TABLE_CHAPTERS + " SET " + COL_CAP_PATH +
                        " = REPLACE(" + COL_CAP_PATH + ", 'http://www.mangahere.cc', '') WHERE 1";
                db.execSQL(query);
                query = "UPDATE " + TABLE_CHAPTERS + " SET " + COL_CAP_PATH +
                        " = REPLACE(" + COL_CAP_PATH + ", '//www.mangahere.cc', '') WHERE 1";
                db.execSQL(query);
            }
            if (oldVersion < 21) {
                db.execSQL("CREATE TABLE temp_manga AS SELECT * FROM manga;");
                db.execSQL("DROP TABLE " + TABLE_MANGA + ";");
                db.execSQL(DATABASE_MANGA_CREATE);
                db.execSQL("INSERT INTO manga(id, nombre, path, imagen, sinopsis, server_id, ultima, nuevos, last_index, burcar, orden_lectura, autor, scroll_s, reader, genres, last_update) " +
                        "SELECT id, nombre, path, imagen, sinopsis, server_id, ultima, nuevos, last_index, burcar, orden_lectura, autor, scroll_s, reader, genres, last_update FROM temp_manga;");
                db.execSQL("DROP TABLE temp_manga");
                String query = "UPDATE manga SET path = REPLACE(path,'http://www.mangahere.cc', '') WHERE server_id=4;";
                db.execSQL(query);
            }
            if (oldVersion < 22) {
                String query = "UPDATE " + TABLE_MANGA + " SET " + COL_PATH +
                        " = REPLACE(" + COL_PATH + ", 'bato.to', 'vatoto.com') WHERE 1";
                db.execSQL(query);
                query = "UPDATE " + TABLE_CHAPTERS + " SET " + COL_CAP_PATH +
                        " = REPLACE(" + COL_CAP_PATH + ", 'bato.to', 'vatoto.com') WHERE 1";
                db.execSQL(query);
            }
            if (oldVersion < 23) {
                //http://de.ninemanga.com
                String query = "UPDATE " + TABLE_MANGA + " SET " + COL_PATH +
                        " = REPLACE(" + COL_PATH + ", 'http://de.ninemanga.com', '') WHERE 1";
                db.execSQL(query);
                query = "UPDATE " + TABLE_CHAPTERS + " SET " + COL_CAP_PATH +
                        " = REPLACE(" + COL_CAP_PATH + ", 'http://de.ninemanga.com', '') WHERE 1";
                db.execSQL(query);
                //http://ru.ninemanga.com
                query = "UPDATE " + TABLE_MANGA + " SET " + COL_PATH +
                        " = REPLACE(" + COL_PATH + ", 'http://ru.ninemanga.com', '') WHERE 1";
                db.execSQL(query);
                query = "UPDATE " + TABLE_CHAPTERS + " SET " + COL_CAP_PATH +
                        " = REPLACE(" + COL_CAP_PATH + ", 'http://ru.ninemanga.com', '') WHERE 1";
                db.execSQL(query);
                //http://es.ninemanga.com
                query = "UPDATE " + TABLE_MANGA + " SET " + COL_PATH +
                        " = REPLACE(" + COL_PATH + ", 'http://es.ninemanga.com', '') WHERE 1";
                db.execSQL(query);
                query = "UPDATE " + TABLE_CHAPTERS + " SET " + COL_CAP_PATH +
                        " = REPLACE(" + COL_CAP_PATH + ", 'http://es.ninemanga.com', '') WHERE 1";
                db.execSQL(query);
                //http://it.ninemanga.com
                query = "UPDATE " + TABLE_MANGA + " SET " + COL_PATH +
                        " = REPLACE(" + COL_PATH + ", 'http://it.ninemanga.com', '') WHERE 1";
                db.execSQL(query);
                query = "UPDATE " + TABLE_CHAPTERS + " SET " + COL_CAP_PATH +
                        " = REPLACE(" + COL_CAP_PATH + ", 'http://it.ninemanga.com', '') WHERE 1";
                db.execSQL(query);
                //http://ninemanga.com
                query = "UPDATE " + TABLE_MANGA + " SET " + COL_PATH +
                        " = REPLACE(" + COL_PATH + ", 'http://ninemanga.com', '') WHERE 1";
                db.execSQL(query);
                query = "UPDATE " + TABLE_CHAPTERS + " SET " + COL_CAP_PATH +
                        " = REPLACE(" + COL_CAP_PATH + ", 'http://ninemanga.com', '') WHERE 1";
                db.execSQL(query);
            }

            if (oldVersion < 24) {
                String query = "UPDATE " + TABLE_MANGA + " SET " + COL_PATH +
                        " = REPLACE(" + COL_PATH + ", 'japscan.com', 'japscan.cc') WHERE 1";
                db.execSQL(query);
                query = "UPDATE " + TABLE_CHAPTERS + " SET " + COL_CAP_PATH +
                        " = REPLACE(" + COL_CAP_PATH + ", 'japscan.com', 'japscan.cc') WHERE 1";
                db.execSQL(query);

            }
            if (oldVersion < 26) {
                String query = "UPDATE " + TABLE_MANGA + " SET " + COL_PATH + " = REPLACE(" + COL_PATH + ", 'http://www.japscan.cc/mangas/', '/manga/') WHERE 1";
                db.execSQL(query);
                query = "UPDATE " + TABLE_CHAPTERS + " SET " + COL_CAP_PATH + " = REPLACE(" + COL_CAP_PATH + ", 'http://www.japscan.cc', '') WHERE 1";
                db.execSQL(query);

            }
            if (oldVersion < 27) {
                String query = "UPDATE " + TABLE_MANGA + " SET " + COL_PATH + " = REPLACE(" + COL_PATH + ", 'https://www.mangakawaii.com', '') WHERE 1";
                db.execSQL(query);
                query = "UPDATE " + TABLE_CHAPTERS + " SET " + COL_CAP_PATH + " = REPLACE(" + COL_CAP_PATH + ", 'https://www.mangakawaii.com', '') WHERE 1";
                db.execSQL(query);
                query = "UPDATE " + TABLE_MANGA + " SET " + COL_PATH + " = REPLACE(" + COL_PATH + ", 'http://www.mangakawaii.com', '') WHERE 1";
                db.execSQL(query);
                query = "UPDATE " + TABLE_CHAPTERS + " SET " + COL_CAP_PATH + " = REPLACE(" + COL_CAP_PATH + ", 'http://www.mangakawaii.com', '') WHERE 1";
                db.execSQL(query);

            }
            if (oldVersion < 28) {
                db.execSQL("ALTER TABLE " + TABLE_MANGA + " ADD COLUMN " + COL_LAST_UPDATE_LONG + " INT DEFAULT 0");
                db.execSQL("UPDATE " + TABLE_MANGA + " SET " + COL_LAST_UPDATE_LONG + " = strftime('%s', substr(last_update, 7, 4) || \"-\" || substr(last_update, 4, 2) || \"-\" || substr(last_update, 1, 2) || \" 00:00:00\") WHERE last_update LIKE \"%.%\";");

            }


            if (oldVersion < 29) {
                db.execSQL("ALTER TABLE " + TABLE_MANGA + " ADD COLUMN " + COL_VAULT + " TEXT DEFAULT ''");
            }

            if (oldVersion < 30) {
                db.execSQL("UPDATE " + TABLE_MANGA + " SET " + COL_VAULT + " = '' WHERE 1;");//reset vaults
            }

            if (oldVersion < 31) {
                db.execSQL("UPDATE capitulos set path = replace(SUBSTR(rtrim(path, replace(path, '-', '')),0, length(rtrim(path, replace(path, '-', '')))), \"heavenmanga.com\", \"heavenmanga.com/manga\") || '/' || replace(replace(path, rtrim(path, replace(path, '-', '')), ''),\".html\",\"\") where path like'%heavenmanga.com%'");
            }

            //db.execSQL("SELECT * FROM errorneousTable where 'inexistenteField'='gveMeAException'");/*/
        } catch (Exception e) {
            // on update error try to restore last version
            Log.e("Database update error", "Exception", e);
            try {
                String backup = db.getPath() + "." + oldVersion + ".bak";
                if (new File(backup).exists()) {
                    if (new File(db.getPath()).delete()) {
                        File journal = new File(db.getPath() + "-journal");
                        if (journal.exists()) {
                            journal.delete();
                        }
                        Util.getInstance().copyFile(new File(backup), new File(db.getPath()));
                    }
                }
            } catch (IOException e1) {
                Log.e("Database restore error", "Exception", e1);
            }
            //ACRA.getErrorReporter().handleException(e);
        } finally {
            is_in_update_process = false;
        }
    }


    private void copyDbToSd(Context c) {
        File dbFile = c.getDatabasePath("mangas.db");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        String ruta = sp.getString("directorio",
                Environment.getExternalStorageDirectory().getAbsolutePath()) + "/MiMangaNu/";
        ruta += "dbs/";
        File exportDir = new File(ruta, "");
        if (!exportDir.exists()) {
            if (!exportDir.mkdirs()) {
                Log.e("Database", "failed to create dbs directory");
            }
        }
        File file = new File(exportDir, dbFile.getName());
        try {
            if (file.createNewFile()) {
                InputStream is = new FileInputStream(dbFile);
                FileCache.writeFile(is, file);
                is.close();
            } else {
                Log.e("Database", "failed to store DB");
            }
        } catch (IOException e) {
            Toast.makeText(c, "Error: ", Toast.LENGTH_LONG).show();
        }
    }

    private boolean doesTableExist(SQLiteDatabase db, String tableName) {
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
