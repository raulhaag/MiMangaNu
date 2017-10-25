package ar.rulosoft.mimanganu;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Random;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/**
 * Created by Raul on 09/01/2017.
 */

@RunWith(value = Parameterized.class)
@LargeTest
public class TestServers {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Parameterized.Parameter
    @SuppressWarnings("WeakerAccess")
    public ServerBase serverBase;

    @Parameterized.Parameters(name = "{index}: ServerTest - {0}")
    public static Object[] data() {
        // initialise the context for now to null, it will be reinstalled later
        return ServerBase.getServers(null);
    }

    private Manga manga;
    private Chapter chapter;
    private Random rand;

    @Before
    public void initTest() {
        rand = new Random();
    }

    @Test
    public void testServer() throws Exception {
        // not to be tested - yet
        if(serverBase.getServerID() == ServerBase.FROMFOLDER) {
            return;
        }

        // recreate the serverBase object with a valid context
        serverBase = ServerBase.getServer(
                serverBase.getServerID(),
                mActivityRule.getActivity().getApplicationContext()
        );

        if (serverBase.hasFilteredNavigation()) {
            ArrayList<Manga> mangas = serverBase.getMangasFiltered(serverBase.getBasicFilter(), 1);
            assertFalse(mangas.isEmpty());

            // pick a random Manga for testing
            manga = mangas.get(rand.nextInt(mangas.size()));
            assertNotNull(manga);

            testLoadManga();
            testInitAndGetImage();
        }

        if (serverBase.hasList()) {
            ArrayList<Manga> mangas = serverBase.getMangas();
            assertNotNull(mangas);
            assertFalse(mangas.isEmpty());

            // pick a random Manga for testing
            manga = mangas.get(rand.nextInt(mangas.size()));
            assertNotNull(manga);

            testLoadManga();
            testInitAndGetImage();
        }

        if (serverBase.hasSearch()) {
            ArrayList<Manga> mangas = serverBase.search("world");
            assertNotNull(mangas);
            assertFalse(mangas.isEmpty());

            // pick a random Manga for testing
            manga = mangas.get(rand.nextInt(mangas.size()));
            assertNotNull(manga);

            testLoadManga();
            testInitAndGetImage();
        }
    }

    public void testLoadManga() throws Exception {
        String context = "Manga '" + manga.getTitle() + "' (" + manga.getPath() + ")";

        try {
            serverBase.loadMangaInformation(manga, false);
        }
        catch (Exception e) {
            fail(e.getMessage() + ": " + context);
        }
        assertNotNull(context, manga.getImages());
        // manga.getImages() might be empty

        assertNotNull(context, manga.getSynopsis());
        assertFalse(context, manga.getSynopsis().isEmpty());

        assertNotNull(context, manga.getAuthor());
        assertFalse(context, manga.getAuthor().isEmpty());

        assertNotNull(context, manga.getGenre());
        assertFalse(context, manga.getGenre().isEmpty());

        try {
            serverBase.loadChapters(manga, false);
        }
        catch (Exception e) {
            fail(e.getMessage() + ": " + context);
        }
        switch (serverBase.getServerID()) {
            case ServerBase.RAWSENMANGA:
            case ServerBase.MANGAEDENIT:
                // these servers list also Manga without chapters - so do not test for emptiness
                break;
            default:
                assertFalse(context, manga.getChapters().isEmpty());
                break;
        }


        // pick a random chapter for testing
        try {
            chapter = manga.getChapter(rand.nextInt(manga.getChapters().size()));
        }
        catch (Exception e) {
            fail(e.getMessage() + ": " + context);
        }
        assertNotNull(context, chapter);
    }

    public void testInitAndGetImage() throws Exception {
        String image = null;
        String context = "Chapter '" + chapter.getTitle() + "' (" + chapter.getPath() + ")";

        try {
            serverBase.chapterInit(chapter);
        }
        catch (Exception e) {
            fail(e.getMessage() + ": " + context);
        }
        assertFalse(context, chapter.getPages() == 0);

        try {
            image = serverBase.getImageFrom(chapter, rand.nextInt(chapter.getPages()) + 1);
        }
        catch (Exception e) {
            fail(e.getMessage() + ": " + context);
        }
        assertNotNull(context, image);
        assertFalse(context, image.isEmpty());

        // additional checking of the last page (to verify array indexing)
        try {
            image = serverBase.getImageFrom(chapter, chapter.getPages());
        }
        catch (Exception e) {
            fail(e.getMessage() + ": " + context);
        }
        assertNotNull(context, image);
        assertFalse(context, image.isEmpty());
    }
}
