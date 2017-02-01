package ar.rulosoft.mimanganu;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Raul on 09/01/2017.
 */

@RunWith(value = Parameterized.class)
@LargeTest
public class TestServers {


    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Parameterized.Parameter
    public ServerBase serverBase;

    private Manga manga;
    private Chapter chapter;

    @Parameterized.Parameters(name = "{index}: ServerTest - {0}")
    public static Object[] data() {
        return ServerBase.getServers(InstrumentationRegistry.getContext());
    }

    @Test
    public void testServer() throws Exception {
        if (serverBase.getServerID() != ServerBase.FROMFOLDER) {
            if (serverBase.hasFilteredNavigation()) {
                testGetMangas();
            }
            if (serverBase.hasList()) {
                testGetMangas2();
            }
            testLoadManga();
            if (serverBase.getServerID() == ServerBase.ESMANGAHERE) {
                Thread.sleep(5000);//to avoid the server kick
            }
            testInitAndGetImage();
        }
    }

    public void testGetMangas() throws Exception {
        ArrayList<Manga> mangas;
        mangas = serverBase.getMangasFiltered(serverBase.getBasicFilter(), 1);
        if (!mangas.isEmpty()) {
            manga = mangas.get((mangas.size() - 1) / 2);
        }
        assertTrue(!mangas.isEmpty());
    }

    public void testGetMangas2() throws Exception {
        ArrayList<Manga> mangas;
        mangas = serverBase.getMangas();
        if (!mangas.isEmpty()) {
            if (serverBase.getServerID() == ServerBase.RAWSENMANGA) {
                manga = mangas.get(0);
            } else {
                manga = mangas.get((mangas.size() - 1) / 2);
            }
        }
        assertTrue(!mangas.isEmpty());
    }

    public void testLoadManga() throws Exception {
        if (manga != null) {
            serverBase.loadMangaInformation(manga, true);
            serverBase.loadChapters(manga, true);
            assertTrue(!manga.getChapters().isEmpty());
            chapter = manga.getChapter((manga.getChapters().size() - 1) / 2);
        } else {
            assertTrue(false);
        }
    }

    public void testInitAndGetImage() throws Exception {
        if (chapter != null) {
            serverBase.chapterInit(chapter);
            String image = serverBase.getImageFrom(chapter, 1);
            assertTrue(chapter.getPages() > 0);
            assertTrue(!image.isEmpty());
        } else {
            assertTrue(false);
        }
    }
}
