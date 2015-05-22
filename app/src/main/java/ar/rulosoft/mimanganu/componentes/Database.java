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
    public static final String COL_NOMBRE = "nombre";
    public static final String COL_PATH = "path";
    public static final String COL_IMAGE = "imagen";
    public static final String COL_SINOPSIS = "sinopsis";
    public static final String COL_LAST_REAS = "ultima";
    public static final String COL_ID = "id";
    public static final String COL_LAST_INDEX = "last_index";// indice listview
    public static final String COL_NUEVOS = "nuevos";// hay nuevos?
    public static final String COL_BUSCAR = "burcar";// buscar updates
    public static final String COL_AUTOR = "autor";
    public static final String TABLE_CAPITULOS = "capitulos";
    // lectura
    public static final String COL_CAP_ID_MANGA = "manga_id";
    public static final String COL_CAP_NOMBRE = "nombre";
    public static final String COL_CAP_PATH = "path";
    public static final String COL_CAP_PAGINAS = "paginas";
    public static final String COL_CAP_PAG_LEIDAS = "leidas";
    public static final String COL_CAP_ESTADO = "estado";
    public static final String COL_CAP_DESCARGADO = "descargado";
    public static final String COL_CAP_ID = "id";
    private static final String COL_SENTIDO = "orden_lectura";// sentido de
    private static final String DATABASE_NAME = "mangas.db";
    private static final int DATABASE_VERSION = 8;

    // Database creation sql statement
    private static final String DATABASE_MANGA_CREATE = "create table " + TABLE_MANGA + "(" + COL_ID + " integer primary key autoincrement, " + COL_NOMBRE
            + " text not null," + COL_PATH + " text not null UNIQUE, " + COL_IMAGE + " text," + COL_SINOPSIS + " text," + COL_SERVER_ID + "," + COL_LAST_REAS
            + " int," + COL_NUEVOS + " int DEFAULT 0," + COL_LAST_INDEX + " int DEFAULT 0, " + COL_BUSCAR + " int DEFAULT 0, " + COL_SENTIDO
            + " int not null DEFAULT -1, " + COL_AUTOR + " TEXT NOT NULL DEFAULT 'N/A');";

    private static final String DATABASE_CAPITULOS_CREATE = "create table " + TABLE_CAPITULOS + "(" + COL_CAP_ID + " integer primary key autoincrement, "
            + COL_CAP_NOMBRE + " text not null," + COL_CAP_PATH + " text not null UNIQUE, " + COL_CAP_PAGINAS + " int," + COL_CAP_ID_MANGA + " int,"
            + COL_CAP_ESTADO + " int DEFAULT 0," + COL_CAP_PAG_LEIDAS + " int DEFAULT 1, " + COL_CAP_DESCARGADO + " int DEFAULT 0);";

    private static SQLiteDatabase localDB;
    Context context;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public static SQLiteDatabase getDatabase(Context c) {
        if (!(localDB != null) || !localDB.isOpen()) {
            localDB = new Database(c).getReadableDatabase();
        }
        return localDB;
    }

    public static int addManga(Context c, Manga m) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NOMBRE, m.titulo);
        cv.put(COL_PATH, m.path);
        cv.put(COL_IMAGE, m.images);
        cv.put(COL_SINOPSIS, m.sinopsis);
        cv.put(COL_SERVER_ID, m.serverId);
        cv.put(COL_AUTOR, m.getAutor());
        cv.put(COL_LAST_REAS, System.currentTimeMillis());

        if (m.finalizado)
            cv.put(COL_BUSCAR, 1);
        else
            cv.put(COL_BUSCAR, 0);

        int mid = (int) getDatabase(c).insert(TABLE_MANGA, null, cv);
        return mid;
    }

    public static void updateManga(Context context, Manga manga) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NOMBRE, manga.titulo);
        cv.put(COL_PATH, manga.path);
        cv.put(COL_IMAGE, manga.images);
        cv.put(COL_SINOPSIS, manga.sinopsis);
        cv.put(COL_SERVER_ID, manga.serverId);
        cv.put(COL_AUTOR, manga.getAutor());
        cv.put(COL_LAST_REAS, System.currentTimeMillis());

        if (manga.finalizado)
            cv.put(COL_BUSCAR, 1);
        else
            cv.put(COL_BUSCAR, 0);

        getDatabase(context).update(TABLE_MANGA, cv, COL_ID + "=" + manga.getId(), null);
    }

    public static void updateMangaNotime(Context context, Manga manga) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NOMBRE, manga.titulo);
        cv.put(COL_PATH, manga.path);
        cv.put(COL_IMAGE, manga.images);
        cv.put(COL_SINOPSIS, manga.sinopsis);
        cv.put(COL_SERVER_ID, manga.serverId);
        cv.put(COL_AUTOR, manga.getAutor());

        if (manga.finalizado)
            cv.put(COL_BUSCAR, 1);
        else
            cv.put(COL_BUSCAR, 0);

        getDatabase(context).update(TABLE_MANGA, cv, COL_ID + "=" + manga.getId(), null);
    }


    public static void updateMangaLeido(Context c, int mid) {
        ContentValues cv = new ContentValues();
        cv.put(COL_LAST_REAS, System.currentTimeMillis());
        getDatabase(c).update(TABLE_MANGA, cv, COL_ID + "=" + mid, null);
    }

    public static void setUpgrable(Context c, int mangaid, boolean buscar) {
        ContentValues cv = new ContentValues();
        if (buscar)
            cv.put(COL_BUSCAR, 1);
        else
            cv.put(COL_BUSCAR, 0);
        getDatabase(c).update(TABLE_MANGA, cv, COL_ID + "=" + mangaid, null);
    }

    public static void updateMangaLastIndex(Context c, int mid, int idx) {
        ContentValues cv = new ContentValues();
        cv.put(COL_LAST_INDEX, idx);
        getDatabase(c).update(TABLE_MANGA, cv, COL_ID + "=" + mid, null);
    }

    public static void updateMangaNuevos(Context c, Manga m, int nuevos) {
        int actual = 0;
        if (nuevos > -99) {
            Cursor cursor = getDatabase(c).query(TABLE_MANGA, new String[]{COL_NUEVOS}, COL_ID + " = " + m.id, null, null, null, null);
            if (cursor.moveToFirst()) {
                actual = cursor.getInt(cursor.getColumnIndex(COL_NUEVOS));
                actual += nuevos;
            }
            cursor.close();
        }
        ContentValues cv = new ContentValues();
        if (!(actual > 0))
            cv.put(COL_NUEVOS, 0);
        else
            cv.put(COL_NUEVOS, actual);
        getDatabase(c).update(TABLE_MANGA, cv, COL_ID + "=" + m.getId(), null);
    }

    public static void addCapitulo(Context c, Capitulo cap, int mangaId) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_ID_MANGA, mangaId);
        cv.put(COL_CAP_NOMBRE, cap.getTitulo());
        cv.put(COL_CAP_PATH, cap.path);
        cv.put(COL_CAP_PAGINAS, cap.getPaginas());
        cv.put(COL_CAP_ESTADO, cap.getEstadoLectura());
        cv.put(COL_CAP_PAG_LEIDAS, cap.getPagLeidas());
        getDatabase(c).insert(TABLE_CAPITULOS, null, cv);
    }

    public static void updateCapitulo(Context context, Capitulo cap) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_NOMBRE, cap.getTitulo());
        cv.put(COL_CAP_PATH, cap.path);
        cv.put(COL_CAP_PAGINAS, cap.getPaginas());
        cv.put(COL_CAP_ESTADO, cap.getEstadoLectura());
        cv.put(COL_CAP_PAG_LEIDAS, cap.getPagLeidas());
        getDatabase(context).update(TABLE_CAPITULOS, cv, COL_CAP_ID + " = " + cap.id, null);
    }

    public static ArrayList<Manga> getMangasForUpdates(Context c) {
        return getMangasCondotion(c, COL_BUSCAR + "= 0");
    }

    public static ArrayList<Manga> getMangas(Context c) {
        return getMangasCondotion(c, null);
    }

    public static ArrayList<Manga> getMangasCondotion(Context c, String condition) {
        Cursor cursor = getDatabase(c).query(
                TABLE_MANGA,
                new String[]{COL_ID, COL_NOMBRE, COL_PATH, COL_IMAGE, COL_SINOPSIS, COL_LAST_REAS, COL_SERVER_ID, COL_NUEVOS, COL_BUSCAR, COL_LAST_INDEX,
                        COL_SENTIDO, COL_AUTOR}, condition, null, null, null, COL_LAST_REAS + " DESC");
        return getMangasFromCursor(cursor);
    }

    public static ArrayList<Manga> getMangasFromCursor(Cursor cursor) {
        ArrayList<Manga> mangas = new ArrayList<Manga>();
        if (cursor.moveToFirst()) {
            int colId = cursor.getColumnIndex(COL_ID);
            int colServerId = cursor.getColumnIndex(COL_SERVER_ID);
            int colTitulo = cursor.getColumnIndex(COL_NOMBRE);
            int colSinopsis = cursor.getColumnIndex(COL_SINOPSIS);
            int colImagen = cursor.getColumnIndex(COL_IMAGE);
            int colWeb = cursor.getColumnIndex(COL_PATH);
            int conNuevos = cursor.getColumnIndex(COL_NUEVOS);
            int colBuscar = cursor.getColumnIndex(COL_BUSCAR);
            int colLastIdx = cursor.getColumnIndex(COL_LAST_INDEX);
            int colSentido = cursor.getColumnIndex(COL_SENTIDO);
            int colAutor = cursor.getColumnIndex(COL_AUTOR);

            do {
                Manga m = new Manga(cursor.getInt(colServerId), cursor.getString(colTitulo), cursor.getString(colWeb), false);
                m.setSinopsis(cursor.getString(colSinopsis));
                m.setImages(cursor.getString(colImagen));
                m.setId(cursor.getInt(colId));
                m.setNuevos(cursor.getInt(conNuevos));
                m.setFinalizado(cursor.getInt(colBuscar) > 0);
                m.setLastIndex(cursor.getInt(colLastIdx));
                m.setSentidoLectura(cursor.getInt(colSentido));
                m.setAutor(cursor.getString(colAutor));
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
            m.setCapitulos(getCapitulos(c, mangaID));
            manga = m;
        } catch (Exception e) {

        }
        return manga;
    }

    public static Manga getFullManga(Context c, int mangaID, boolean asc) {
        Manga manga = null;
        try {
            Manga m = getMangasCondotion(c, COL_ID + "=" + mangaID).get(0);
            m.setCapitulos(getCapitulos(c, mangaID, "1", asc));
            manga = m;
        } catch (Exception e) {

        }
        return manga;
    }

    public static ArrayList<Capitulo> getCapitulos(Context c, int MangaId) {
        return getCapitulos(c, MangaId, "1");
    }

    public static ArrayList<Capitulo> getCapitulos(Context c, int MangaId, String condicion) {
        return getCapitulos(c, MangaId, condicion, false);
    }

    public static ArrayList<Capitulo> getCapitulos(Context c, int MangaId, String condicion, boolean asc) {
        ArrayList<Capitulo> capitulos = new ArrayList<Capitulo>();
        String orden = " DESC";
        if (asc)
            orden = " ASC";
        Cursor cursor = getDatabase(c).query(
                TABLE_CAPITULOS,
                new String[]{COL_CAP_ID, COL_CAP_ID_MANGA, COL_CAP_NOMBRE, COL_CAP_PATH, COL_CAP_PAGINAS, COL_CAP_PAG_LEIDAS, COL_CAP_ESTADO,
                        COL_CAP_DESCARGADO}, COL_CAP_ID_MANGA + "=" + MangaId + " AND " + condicion, null, null, null, COL_CAP_ID + orden);
        if (cursor.moveToFirst()) {
            int colId = cursor.getColumnIndex(COL_CAP_ID);
            int colTitulo = cursor.getColumnIndex(COL_CAP_NOMBRE);
            int colPaginas = cursor.getColumnIndex(COL_CAP_PAGINAS);
            int colWeb = cursor.getColumnIndex(COL_CAP_PATH);
            int colLeidas = cursor.getColumnIndex(COL_CAP_PAG_LEIDAS);
            int colEstado = cursor.getColumnIndex(COL_CAP_ESTADO);
            int colDescargado = cursor.getColumnIndex(COL_CAP_DESCARGADO);
            do {
                Capitulo cap = new Capitulo(cursor.getString(colTitulo), cursor.getString(colWeb));
                cap.setPaginas(cursor.getInt(colPaginas));
                cap.setId(cursor.getInt(colId));
                cap.setMangaID(MangaId);
                cap.setEstadoLectura(cursor.getInt(colEstado));
                cap.setPagLeidas(cursor.getInt(colLeidas));
                cap.setDescargado((cursor.getInt(colDescargado) == 1));
                capitulos.add(cap);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return capitulos;
    }

    public static Capitulo getCapitulo(Context c, int capId) {
        Capitulo cap = null;
        Cursor cursor = getDatabase(c).query(
                TABLE_CAPITULOS,
                new String[]{COL_CAP_ID, COL_CAP_ID_MANGA, COL_CAP_NOMBRE, COL_CAP_PATH, COL_CAP_PAGINAS, COL_CAP_PAG_LEIDAS, COL_CAP_ESTADO,
                        COL_CAP_DESCARGADO}, COL_CAP_ID + "=" + capId, null, null, null, null);
        if (cursor.moveToFirst()) {
            int colId = cursor.getColumnIndex(COL_CAP_ID);
            int colMID = cursor.getColumnIndex(COL_CAP_ID_MANGA);
            int colTitulo = cursor.getColumnIndex(COL_CAP_NOMBRE);
            int colPaginas = cursor.getColumnIndex(COL_CAP_PAGINAS);
            int colWeb = cursor.getColumnIndex(COL_CAP_PATH);
            int colLeidas = cursor.getColumnIndex(COL_CAP_PAG_LEIDAS);
            int colEstado = cursor.getColumnIndex(COL_CAP_ESTADO);
            int colDescargado = cursor.getColumnIndex(COL_CAP_DESCARGADO);

            cap = new Capitulo(cursor.getString(colTitulo), cursor.getString(colWeb));
            cap.setPaginas(cursor.getInt(colPaginas));
            cap.setId(cursor.getInt(colId));
            cap.setMangaID(cursor.getInt(colMID));
            cap.setEstadoLectura(cursor.getInt(colEstado));
            cap.setPagLeidas(cursor.getInt(colLeidas));
            cap.setDescargado((cursor.getInt(colDescargado) == 1));
        }
        cursor.close();
        return cap;
    }

    public static void UpdateCapituloDescargado(Context c, int cid, int estado) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_DESCARGADO, estado);
        getDatabase(c).update(TABLE_CAPITULOS, cv, COL_CAP_ID + "=" + Integer.toString(cid), null);
    }

    public static void updateCapituloConDescarga(Context context, Capitulo cap) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_NOMBRE, cap.getTitulo());
        cv.put(COL_CAP_PATH, cap.path);
        cv.put(COL_CAP_PAGINAS, cap.getPaginas());
        cv.put(COL_CAP_ESTADO, cap.getEstadoLectura());
        cv.put(COL_CAP_PAG_LEIDAS, cap.getPagLeidas());
        cv.put(COL_CAP_DESCARGADO, cap.isDescargado() ? 1 : 0);
        getDatabase(context).update(TABLE_CAPITULOS, cv, COL_CAP_ID + " = " + cap.id, null);

    }

    public static void UpdateCapituloPagina(Context c, int cid, int pagina) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_PAG_LEIDAS, pagina);
        getDatabase(c).update(TABLE_CAPITULOS, cv, COL_CAP_ID + "=" + Integer.toString(cid), null);
    }

    public static void BorrarManga(Context c, int mid) {
        getDatabase(c).delete(TABLE_MANGA, COL_ID + " = " + mid, null);
        getDatabase(c).delete(TABLE_CAPITULOS, COL_CAP_ID_MANGA + "=" + mid, null);
    }

    public static void borrarCapitulo(Context context, Capitulo capitulo) {
        getDatabase(context).delete(TABLE_CAPITULOS, COL_CAP_ID + "=" + capitulo.id, null);
    }

    public static Manga getManga(Context context, int mangaID) {
        Manga manga = null;
        Cursor cursor = getDatabase(context).query(TABLE_MANGA,
                new String[]{COL_ID, COL_NOMBRE, COL_PATH, COL_IMAGE, COL_SINOPSIS, COL_LAST_REAS, COL_SERVER_ID, COL_NUEVOS, COL_LAST_INDEX, COL_SENTIDO, COL_AUTOR},
                COL_ID + "=" + mangaID, null, null, null, COL_LAST_REAS + " DESC");
        if (cursor.moveToFirst()) {
            int colId = cursor.getColumnIndex(COL_ID);
            int colServerId = cursor.getColumnIndex(COL_SERVER_ID);
            int colTitulo = cursor.getColumnIndex(COL_NOMBRE);
            int colSinopsis = cursor.getColumnIndex(COL_SINOPSIS);
            int colImagen = cursor.getColumnIndex(COL_IMAGE);
            int colWeb = cursor.getColumnIndex(COL_PATH);
            int conNuevos = cursor.getColumnIndex(COL_NUEVOS);
            int colLastIndex = cursor.getColumnIndex(COL_LAST_INDEX);
            int colSentido = cursor.getColumnIndex(COL_SENTIDO);
            int conAutor = cursor.getColumnIndex(COL_AUTOR);

            Manga m = new Manga(cursor.getInt(colServerId), cursor.getString(colTitulo), cursor.getString(colWeb), false);
            m.setSinopsis(cursor.getString(colSinopsis));
            m.setImages(cursor.getString(colImagen));
            m.setId(cursor.getInt(colId));
            m.setNuevos(cursor.getInt(conNuevos));
            m.setLastIndex(cursor.getInt(colLastIndex));
            m.setSentidoLectura(cursor.getInt(colSentido));
            m.setAutor(cursor.getString(conAutor));

            manga = m;
        }
        cursor.close();
        return manga;
    }

    public static void marcarTodoComoLeido(Context c, int mangaId) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_ESTADO, Capitulo.LEIDO);
        getDatabase(c).update(TABLE_CAPITULOS, cv, COL_CAP_ID_MANGA + " = " + mangaId, null);
    }

    public static void marcarComoLeido(Context c, int capId) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_ESTADO, Capitulo.LEIDO);
        getDatabase(c).update(TABLE_CAPITULOS, cv, COL_CAP_ID + " = " + capId, null);
    }

    public static void marcarTodoComoNoLeido(Context c, int mangaId) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAP_ESTADO, Capitulo.SIN_LEER);
        getDatabase(c).update(TABLE_CAPITULOS, cv, COL_CAP_ID_MANGA + " = " + mangaId, null);
    }

    public static void removerCapitulosHuerfanos(Context c) {
        getDatabase(c).delete(TABLE_CAPITULOS, COL_CAP_ID_MANGA + "= -1", null);
    }

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
        db.execSQL("ALTER TABLE " + TABLE_MANGA + " ADD COLUMN " + COL_AUTOR + " TEXT NOT NULL DEFAULT 'N/A';");
    }
}
