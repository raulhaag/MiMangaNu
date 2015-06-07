package ar.rulosoft.mimanganu.componentes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class Database extends SQLiteOpenHelper {

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
    public static final String COL_SEARCH = "burcar";// buscar updates
    public static final String COL_AUTHOR = "autor";
    public static final String TABLE_CHAPTERS = "capitulos";
    // lectura
    public static final String COL_CAP_ID_MANGA = "manga_id";
    public static final String COL_CAP_NAME = "nombre";
    public static final String COL_CAP_PATH = "path";
    public static final String COL_CAP_PAGES = "paginas";
    public static final String COL_CAP_PAG_READ = "leidas";
    public static final String COL_CAP_STATE = "estado";
    // TODO: Translate
    public static final String COL_CAP_DESCARGADO = "descargado";
    public static final String COL_CAP_ID = "id";
    // TODO: Translate
    private static final String COL_SENTIDO = "orden_lectura";// sentido de
    private static final String DATABASE_NAME = "mangas.db";
    private static final int DATABASE_VERSION = 8;

    // Database creation sql statement
    private static final String DATABASE_MANGA_CREATE = "create table " + TABLE_MANGA + "(" + COL_ID + " integer primary key autoincrement, " + COL_NAME
            + " text not null," + COL_PATH + " text not null UNIQUE, " + COL_IMAGE + " text," + COL_SYNOPSIS + " text," + COL_SERVER_ID + "," + COL_LAST_READ
            + " int," + COL_NEW + " int DEFAULT 0," + COL_LAST_INDEX + " int DEFAULT 0, " + COL_SEARCH + " int DEFAULT 0, " + COL_SENTIDO
            + " int not null DEFAULT -1, " + COL_AUTHOR + " TEXT NOT NULL DEFAULT 'N/A');";

    private static final String DATABASE_CAPITULOS_CREATE = "create table " + TABLE_CHAPTERS + "(" + COL_CAP_ID + " integer primary key autoincrement, "
            + COL_CAP_NAME + " text not null," + COL_CAP_PATH + " text not null UNIQUE, " + COL_CAP_PAGES + " int," + COL_CAP_ID_MANGA + " int,"
            + COL_CAP_STATE + " int DEFAULT 0," + COL_CAP_PAG_READ + " int DEFAULT 1, " + COL_CAP_DESCARGADO + " int DEFAULT 0);";

    private static SQLiteDatabase localDB;
    Context context;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public static SQLiteDatabase getDatabase(Context c) {
        if ((localDB == null) || !localDB.isOpen()) {
            localDB = new Database(c).getReadableDatabase();
        }
        return localDB;
    }

    public static int addManga(Context c, Manga m) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, m.title);
        cv.put(COL_PATH, m.path);
        cv.put(COL_IMAGE, m.images);
        cv.put(COL_SYNOPSIS, m.synopsis);
        cv.put(COL_SERVER_ID, m.serverId);
        cv.put(COL_AUTHOR, m.getAuthor());
        cv.put(COL_LAST_READ, System.currentTimeMillis());

        if (m.finalizado)
            cv.put(COL_SEARCH, 1);
        else
            cv.put(COL_SEARCH, 0);

        return (int) getDatabase(c).insert(TABLE_MANGA, null, cv);
    }

    public static void updateManga(Context context, Manga manga) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, manga.title);
        cv.put(COL_PATH, manga.path);
        cv.put(COL_IMAGE, manga.images);
        cv.put(COL_SYNOPSIS, manga.synopsis);
        cv.put(COL_SERVER_ID, manga.serverId);
        cv.put(COL_AUTHOR, manga.getAuthor());
        cv.put(COL_LAST_READ, System.currentTimeMillis());

        if (manga.finalizado)
            cv.put(COL_SEARCH, 1);
        else
            cv.put(COL_SEARCH, 0);

        getDatabase(context).update(TABLE_MANGA, cv, COL_ID + "=" + manga.getId(), null);
    }

    public static void updateMangaNotime(Context context, Manga manga) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, manga.title);
        cv.put(COL_PATH, manga.path);
        cv.put(COL_IMAGE, manga.images);
        cv.put(COL_SYNOPSIS, manga.synopsis);
        cv.put(COL_SERVER_ID, manga.serverId);
        cv.put(COL_AUTHOR, manga.getAuthor());

        if (manga.finalizado)
            cv.put(COL_SEARCH, 1);
        else
            cv.put(COL_SEARCH, 0);

        getDatabase(context).update(TABLE_MANGA, cv, COL_ID + "=" + manga.getId(), null);
    }


    public static void updateMangaRead(Context c, int mid) {
        ContentValues cv = new ContentValues();
        cv.put(COL_LAST_READ, System.currentTimeMillis());
        getDatabase(c).update(TABLE_MANGA, cv, COL_ID + "=" + mid, null);
    }

    // TODO: Typo? What's that supposed to mean?
    public static void setUpgrable(Context c, int mangaid, boolean buscar) {
        ContentValues cv = new ContentValues();
        if (buscar)
            cv.put(COL_SEARCH, 1);
        else
            cv.put(COL_SEARCH, 0);
        getDatabase(c).update(TABLE_MANGA, cv, COL_ID + "=" + mangaid, null);
    }

    public static void updateMangaLastIndex(Context c, int mid, int idx) {
        ContentValues cv = new ContentValues();
        cv.put(COL_LAST_INDEX, idx);
        getDatabase(c).update(TABLE_MANGA, cv, COL_ID + "=" + mid, null);
    }

    public static void updateNewMangas(Context c, Manga m, int nuevos) {
        int actual = 0;
        if (nuevos > -99) {
            Cursor cursor = getDatabase(c).query(TABLE_MANGA, new String[]{COL_NEW}, COL_ID + " = " + m.id, null, null, null, null);
            if (cursor.moveToFirst()) {
                actual = cursor.getInt(cursor.getColumnIndex(COL_NEW));
                actual += nuevos;
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

    public static void addChapter(Context c, Chapter cap, int mangaId) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_ID_MANGA, mangaId);
        cv.put(COL_CAP_NAME, cap.getTitle());
        cv.put(COL_CAP_PATH, cap.path);
        cv.put(COL_CAP_PAGES, cap.getPaginas());
        cv.put(COL_CAP_STATE, cap.getReadStatus());
        cv.put(COL_CAP_PAG_READ, cap.getPagesRead());
        getDatabase(c).insert(TABLE_CHAPTERS, null, cv);
    }

    public static void updateChapter(Context context, Chapter cap) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_NAME, cap.getTitle());
        cv.put(COL_CAP_PATH, cap.path);
        cv.put(COL_CAP_PAGES, cap.getPaginas());
        cv.put(COL_CAP_STATE, cap.getReadStatus());
        cv.put(COL_CAP_PAG_READ, cap.getPagesRead());
        getDatabase(context).update(TABLE_CHAPTERS, cv, COL_CAP_ID + " = " + cap.id, null);
    }

    public static ArrayList<Manga> getMangasForUpdates(Context c) {
        return getMangasCondotion(c, COL_SEARCH + "= 0");
    }

    public static ArrayList<Manga> getMangas(Context c) {
        return getMangasCondotion(c, null);
    }

    // TODO: Needs translation. Condotion? Condition? Not sure, so I don't touch it.
    public static ArrayList<Manga> getMangasCondotion(Context c, String condition) {
        Cursor cursor = getDatabase(c).query(
                TABLE_MANGA,
                new String[]{COL_ID, COL_NAME, COL_PATH, COL_IMAGE, COL_SYNOPSIS, COL_LAST_READ, COL_SERVER_ID, COL_NEW, COL_SEARCH, COL_LAST_INDEX,
                        COL_SENTIDO, COL_AUTHOR}, condition, null, null, null, COL_LAST_READ + " DESC");
        return getMangasFromCursor(cursor);
    }

    public static ArrayList<Manga> getMangasFromCursor(Cursor cursor) {
        ArrayList<Manga> mangas = new ArrayList<>();
        if (cursor.moveToFirst()) {
            int colId = cursor.getColumnIndex(COL_ID);
            int colServerId = cursor.getColumnIndex(COL_SERVER_ID);
            int colTitulo = cursor.getColumnIndex(COL_NAME);
            int colSinopsis = cursor.getColumnIndex(COL_SYNOPSIS);
            int colImagen = cursor.getColumnIndex(COL_IMAGE);
            int colWeb = cursor.getColumnIndex(COL_PATH);
            int conNuevos = cursor.getColumnIndex(COL_NEW);
            int colBuscar = cursor.getColumnIndex(COL_SEARCH);
            int colLastIdx = cursor.getColumnIndex(COL_LAST_INDEX);
            int colSentido = cursor.getColumnIndex(COL_SENTIDO);
            int colAutor = cursor.getColumnIndex(COL_AUTHOR);

            do {
                Manga m = new Manga(cursor.getInt(colServerId), cursor.getString(colTitulo), cursor.getString(colWeb), false);
                m.setSinopsis(cursor.getString(colSinopsis));
                m.setImages(cursor.getString(colImagen));
                m.setId(cursor.getInt(colId));
                m.setNuevos(cursor.getInt(conNuevos));
                m.setFinalizado(cursor.getInt(colBuscar) > 0);
                m.setLastIndex(cursor.getInt(colLastIdx));
                m.setReadingDirection(cursor.getInt(colSentido));
                m.setAuthor(cursor.getString(colAutor));
                mangas.add(m);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return mangas;
    }

    public static Manga getFullManga(Context c, int mangaID) {
        Manga manga = null;
        try {
            Manga m = getMangasCondotion(c, COL_ID + "=" + mangaID).get(0);
            m.setChapters(getChapters(c, mangaID));
            manga = m;
        } catch (Exception e) {

        }
        return manga;
    }

    public static Manga getFullManga(Context c, int mangaID, boolean asc) {
        Manga manga = null;
        try {
            Manga m = getMangasCondotion(c, COL_ID + "=" + mangaID).get(0);
            m.setChapters(getChapters(c, mangaID, "1", asc));
            manga = m;
        } catch (Exception e) {

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
        String orden = " DESC";
        if (asc)
            orden = " ASC";
        Cursor cursor = getDatabase(c).query(
                TABLE_CHAPTERS,
                new String[]{COL_CAP_ID, COL_CAP_ID_MANGA, COL_CAP_NAME, COL_CAP_PATH, COL_CAP_PAGES, COL_CAP_PAG_READ, COL_CAP_STATE,
                        COL_CAP_DESCARGADO}, COL_CAP_ID_MANGA + "=" + MangaId + " AND " + condicion, null, null, null, COL_CAP_ID + orden);
        if (cursor.moveToFirst()) {
            int colId = cursor.getColumnIndex(COL_CAP_ID);
            int colTitulo = cursor.getColumnIndex(COL_CAP_NAME);
            int colPaginas = cursor.getColumnIndex(COL_CAP_PAGES);
            int colWeb = cursor.getColumnIndex(COL_CAP_PATH);
            int colLeidas = cursor.getColumnIndex(COL_CAP_PAG_READ);
            int colEstado = cursor.getColumnIndex(COL_CAP_STATE);
            int colDescargado = cursor.getColumnIndex(COL_CAP_DESCARGADO);
            do {
                Chapter cap = new Chapter(cursor.getString(colTitulo), cursor.getString(colWeb));
                cap.setPaginas(cursor.getInt(colPaginas));
                cap.setId(cursor.getInt(colId));
                cap.setMangaID(MangaId);
                cap.setReadStatus(cursor.getInt(colEstado));
                cap.setPagesRead(cursor.getInt(colLeidas));
                cap.setDownloaded((cursor.getInt(colDescargado) == 1));
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
                new String[]{COL_CAP_ID, COL_CAP_ID_MANGA, COL_CAP_NAME, COL_CAP_PATH, COL_CAP_PAGES, COL_CAP_PAG_READ, COL_CAP_STATE,
                        COL_CAP_DESCARGADO}, COL_CAP_ID + "=" + capId, null, null, null, null);
        if (cursor.moveToFirst()) {
            int colId = cursor.getColumnIndex(COL_CAP_ID);
            int colMID = cursor.getColumnIndex(COL_CAP_ID_MANGA);
            int colTitulo = cursor.getColumnIndex(COL_CAP_NAME);
            int colPaginas = cursor.getColumnIndex(COL_CAP_PAGES);
            int colWeb = cursor.getColumnIndex(COL_CAP_PATH);
            int colLeidas = cursor.getColumnIndex(COL_CAP_PAG_READ);
            int colEstado = cursor.getColumnIndex(COL_CAP_STATE);
            int colDescargado = cursor.getColumnIndex(COL_CAP_DESCARGADO);

            cap = new Chapter(cursor.getString(colTitulo), cursor.getString(colWeb));
            cap.setPaginas(cursor.getInt(colPaginas));
            cap.setId(cursor.getInt(colId));
            cap.setMangaID(cursor.getInt(colMID));
            cap.setReadStatus(cursor.getInt(colEstado));
            cap.setPagesRead(cursor.getInt(colLeidas));
            cap.setDownloaded((cursor.getInt(colDescargado) == 1));
        }
        cursor.close();
        return cap;
    }

    // TODO: Needs translation.
    public static void UpdateCapituloDescargado(Context c, int cid, int estado) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_DESCARGADO, estado);
        getDatabase(c).update(TABLE_CHAPTERS, cv, COL_CAP_ID + "=" + Integer.toString(cid), null);
    }

    // TODO: Needs translation
    public static void updateCapituloConDescarga(Context context, Chapter cap) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_NAME, cap.getTitle());
        cv.put(COL_CAP_PATH, cap.path);
        cv.put(COL_CAP_PAGES, cap.getPaginas());
        cv.put(COL_CAP_STATE, cap.getReadStatus());
        cv.put(COL_CAP_PAG_READ, cap.getPagesRead());
        cv.put(COL_CAP_DESCARGADO, cap.isDownloaded() ? 1 : 0);
        getDatabase(context).update(TABLE_CHAPTERS, cv, COL_CAP_ID + " = " + cap.id, null);

    }

    public static void UpdateChapterPage(Context c, int cid, int pagina) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_PAG_READ, pagina);
        getDatabase(c).update(TABLE_CHAPTERS, cv, COL_CAP_ID + "=" + Integer.toString(cid), null);
    }

    public static void deleteManga(Context c, int mid) {
        getDatabase(c).delete(TABLE_MANGA, COL_ID + " = " + mid, null);
        getDatabase(c).delete(TABLE_CHAPTERS, COL_CAP_ID_MANGA + "=" + mid, null);
    }

    public static void deleteChapter(Context context, Chapter chapter) {
        getDatabase(context).delete(TABLE_CHAPTERS, COL_CAP_ID + "=" + chapter.id, null);
    }

    public static Manga getManga(Context context, int mangaID) {
        Manga manga = null;
        Cursor cursor = getDatabase(context).query(TABLE_MANGA,
                new String[]{COL_ID, COL_NAME, COL_PATH, COL_IMAGE, COL_SYNOPSIS, COL_LAST_READ, COL_SERVER_ID, COL_NEW, COL_LAST_INDEX, COL_SENTIDO, COL_AUTHOR},
                COL_ID + "=" + mangaID, null, null, null, COL_LAST_READ + " DESC");
        if (cursor.moveToFirst()) {
            int colId = cursor.getColumnIndex(COL_ID);
            int colServerId = cursor.getColumnIndex(COL_SERVER_ID);
            int colTitulo = cursor.getColumnIndex(COL_NAME);
            int colSinopsis = cursor.getColumnIndex(COL_SYNOPSIS);
            int colImagen = cursor.getColumnIndex(COL_IMAGE);
            int colWeb = cursor.getColumnIndex(COL_PATH);
            int conNuevos = cursor.getColumnIndex(COL_NEW);
            int colLastIndex = cursor.getColumnIndex(COL_LAST_INDEX);
            int colSentido = cursor.getColumnIndex(COL_SENTIDO);
            int conAutor = cursor.getColumnIndex(COL_AUTHOR);

            Manga m = new Manga(cursor.getInt(colServerId), cursor.getString(colTitulo), cursor.getString(colWeb), false);
            m.setSinopsis(cursor.getString(colSinopsis));
            m.setImages(cursor.getString(colImagen));
            m.setId(cursor.getInt(colId));
            m.setNuevos(cursor.getInt(conNuevos));
            m.setLastIndex(cursor.getInt(colLastIndex));
            m.setReadingDirection(cursor.getInt(colSentido));
            m.setAuthor(cursor.getString(conAutor));

            manga = m;
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

    // TODO: Needs translation
    public static void removerCapitulosHuerfanos(Context c) {
        getDatabase(c).delete(TABLE_CHAPTERS, COL_CAP_ID_MANGA + "= -1", null);
    }

    // TODO: Needs translation
    public static void updadeSentidoLectura(Context c, int ordinal, int mid) {
        ContentValues cv = new ContentValues();
        cv.put(COL_SENTIDO, ordinal);
        getDatabase(c).update(TABLE_MANGA, cv, COL_ID + "=" + mid, null);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_MANGA_CREATE);
        db.execSQL(DATABASE_CAPITULOS_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        localDB = db;
        db.execSQL("ALTER TABLE " + TABLE_MANGA + " ADD COLUMN " + COL_AUTHOR + " TEXT NOT NULL DEFAULT 'N/A';");
    }
}
