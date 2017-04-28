package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;

/**
 * Created by Raul on 28/04/2017.
 */

public class DeadServer extends ServerBase {
    public DeadServer(Context context) {
        super(context);
        setFlag(R.drawable.rip);
        setIcon(R.drawable.rip);
        setServerName("DEAD SERVER");
    }

    public static String getServerName(Manga m) {
        // before remove deprecated add the correct id/name here
        HashMap<Integer, String> deathServers = new HashMap<>();
        deathServers.put(LECTUREENLIGNE, "LectureEnLigne");
        deathServers.put(ESMANGA, "EsManga");
        deathServers.put(GOGOCOMIC, "GoGoComic");
        deathServers.put(MANGATUBE, "Manga-tube");
        deathServers.put(READCOMICSTV, "ReadComicsTV");
        deathServers.put(STARKANACOM, "Starkana");
        deathServers.put(TUSMANGAS, "TusMangasOnline");
        return deathServers.get(m.getServerId());
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
    public String getPagesNumber(Chapter chapter, int page) {
        return null;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        return null;
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        throw new Exception(context.getString(R.string.server_dead_message));
    }

    @Override
    public boolean hasList() {
        return false;
    }
}
