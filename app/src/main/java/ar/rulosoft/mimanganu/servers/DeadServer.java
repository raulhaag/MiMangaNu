package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.util.SparseArray;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;

/**
 * Created by Raul on 28/04/2017.
 */

public class DeadServer extends ServerBase {
    static final SparseArray<String> deadServers = deadServersGen();

    public DeadServer(Context context) {
        super(context);
        setFlag(R.drawable.rip);
        setIcon(R.drawable.rip);
        setServerName("DEAD SERVER");
    }

    public static SparseArray<String> deadServersGen() {
        // before removing deprecated Servers add the correct id/name here
        SparseArray<String> deadServers = new SparseArray<>();
        deadServers.put(LECTUREENLIGNE, "LectureEnLigne");
        deadServers.put(ESMANGA, "EsManga");
        deadServers.put(GOGOCOMIC, "GoGoComic");
        deadServers.put(MANGATUBE, "Manga-tube");
        deadServers.put(READCOMICSTV, "ReadComicsTV");
        deadServers.put(STARKANACOM, "Starkana");
        deadServers.put(TUSMANGAS, "TusMangasOnline");
        deadServers.put(ESMANGAHERE, "EsMangaHere");
        deadServers.put(SUBMANGA, "SubManga");
        deadServers.put(BATOTO, "BatoTo");
        deadServers.put(BATOTOES, "BatoTo(ES)");
        deadServers.put(MANGAPEDIA, "Mangapedia");
        deadServers.put(LEOMANGA, "LeoManga");
        deadServers.put(MANGAFOX, "MangaFox");
        deadServers.put(MANGARAWONLINE, "MangaRawOnline");
        deadServers.put(MYMANGAIO, "MyMangaIo");
        return deadServers;
    }

    public static String getServerName(Manga m) {
        return deadServers.get(m.getServerId());
    }

    public static SparseArray<String> getServersName() {
        return deadServers;
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        throw new Exception(context.getString(R.string.server_dead_message));
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        throw new Exception(context.getString(R.string.server_dead_message));
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        throw new Exception(context.getString(R.string.server_dead_message));
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        throw new Exception(context.getString(R.string.server_dead_message));
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        throw new Exception(context.getString(R.string.server_dead_message));
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        throw new Exception(context.getString(R.string.server_dead_message));
    }

    @Override
    public int searchForNewChapters(int id, Context context, boolean fast) {
        return 0; //just return 0 found and don't throw exceptions
    }

    @Override
    public boolean hasList() {
        return false;
    }
}
