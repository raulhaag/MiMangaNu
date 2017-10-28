package ar.rulosoft.mimanganu;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.DeadServer;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.navegadores.Navigator;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Raul on 09/01/2017.
 */

@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestServersInstrumentation {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    private Random rand;
    private ServerBase serverBase;

    @Before
    public void setupTest() throws Exception {
        rand = new Random();
    }

    @Ignore("FromFolder cannot be tested - yet")
    @SuppressWarnings("unused")
    public void test_FROMFOLDER() throws Exception {
        testServer(ServerBase.getServer(ServerBase.FROMFOLDER, null));
    }

    @Test
    public void test_RAWSENMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.RAWSENMANGA, null));
    }

    @Test
    public void test_MANGAPANDA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGAPANDA, null));
    }

    @Test
    public void test_ESMANGAHERE() throws Exception {
        testServer(ServerBase.getServer(ServerBase.ESMANGAHERE, null));
    }

    @Test
    public void test_MANGAHERE() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGAHERE, null));
    }

    @Test
    public void test_MANGAFOX() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGAFOX, null));
    }

    @Test
    public void test_SUBMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.SUBMANGA, null));
    }

    @Test
    public void test_ESMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.ESMANGA, null));
    }

    @Test
    public void test_HEAVENMANGACOM() throws Exception {
        testServer(ServerBase.getServer(ServerBase.HEAVENMANGACOM, null));
    }

    @Test
    public void test_STARKANACOM() throws Exception {
        testServer(ServerBase.getServer(ServerBase.STARKANACOM, null));
    }

    @Test
    public void test_ESNINEMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.ESNINEMANGA, null));
    }

    @Test
    public void test_LECTUREENLIGNE() throws Exception {
        testServer(ServerBase.getServer(ServerBase.LECTUREENLIGNE, null));
    }

    @Test
    public void test_KISSMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.KISSMANGA, null));
    }

    @Test
    public void test_ITNINEMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.ITNINEMANGA, null));
    }

    @Test
    public void test_TUSMANGAS() throws Exception {
        testServer(ServerBase.getServer(ServerBase.TUSMANGAS, null));
    }

    @Test
    public void test_MANGAREADER() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGAREADER, null));
    }

    @Test
    public void test_DENINEMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.DENINEMANGA, null));
    }

    @Test
    public void test_RUNINEMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.RUNINEMANGA, null));
    }

    @Test
    public void test_MANGATUBE() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGATUBE, null));
    }

    @Test
    public void test_MANGAEDENIT() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGAEDENIT, null));
    }

    @Test
    public void test_MYMANGAIO() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MYMANGAIO, null));
    }

    @Test
    public void test_TUMANGAONLINE() throws Exception {
        testServer(ServerBase.getServer(ServerBase.TUMANGAONLINE, null));
    }

    @Test
    public void test_NINEMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.NINEMANGA, null));
    }

    @Test
    public void test_MANGAEDEN() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGAEDEN, null));
    }

    @Test
    public void test_LEOMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.LEOMANGA, null));
    }

    @Ignore("Batoto does not work without login credentials")
    @SuppressWarnings("unused")
    public void test_BATOTO() throws Exception {
        testServer(ServerBase.getServer(ServerBase.BATOTO, null));
    }

    @Ignore("Batoto(ES) does not work without login credentials")
    @SuppressWarnings("unused")
    public void test_BATOTOES() throws Exception {
        testServer(ServerBase.getServer(ServerBase.BATOTOES, null));
    }

    @Test
    public void test_JAPSCAN() throws Exception {
        testServer(ServerBase.getServer(ServerBase.JAPSCAN, null));
    }

    @Test
    public void test_READMANGATODAY() throws Exception {
        testServer(ServerBase.getServer(ServerBase.READMANGATODAY, null));
    }

    @Test
    public void test_TAADD() throws Exception {
        testServer(ServerBase.getServer(ServerBase.TAADD, null));
    }

    @Test
    public void test_MANGASTREAM() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGASTREAM, null));
    }

    @Test
    public void test_MANGAKAWAII() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGAKAWAII, null));
    }

    @Test
    public void test_KUMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.KUMANGA, null));
    }

    @Test
    public void test_MANGAPEDIA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGAPEDIA, null));
    }

    @Test
    public void test_MANGATOWN() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGATOWN, null));
    }

    @Test
    public void test_READCOMICONLINE() throws Exception {
        testServer(ServerBase.getServer(ServerBase.READCOMICONLINE, null));
    }

    @Test
    public void test_READCOMICSTV() throws Exception {
        testServer(ServerBase.getServer(ServerBase.READCOMICSTV, null));
    }

    @Test
    public void test_GOGOCOMIC() throws Exception {
        testServer(ServerBase.getServer(ServerBase.GOGOCOMIC, null));
    }

    @Test
    public void test_VIEWCOMIC() throws Exception {
        testServer(ServerBase.getServer(ServerBase.VIEWCOMIC, null));
    }

    private void testServer(ServerBase serverBase) throws Exception {
        String test_context = String.format(Locale.getDefault(), "[SRV] %s (id=%d)", serverBase.getServerName(), serverBase.getServerID());
        assertNotNull(test_context, serverBase);

        if(serverBase instanceof DeadServer) {
            // dead servers cannot be tested
            return;
        }

        // recreate the serverBase object with a valid context
        serverBase = ServerBase.getServer(
                serverBase.getServerID(),
                mActivityRule.getActivity().getApplicationContext()
        );
        this.serverBase = serverBase;

        if (serverBase.hasFilteredNavigation()) {
            ArrayList<Manga> mangas = serverBase.getMangasFiltered(serverBase.getBasicFilter(), 1);
            testManga(mangas);
        }

        if (serverBase.hasList()) {
            ArrayList<Manga> mangas = serverBase.getMangas();
            testManga(mangas);
        }

        if (serverBase.hasSearch()) {
            ArrayList<Manga> mangas = serverBase.search("world");
            testManga(mangas);
        }
    }

    private void testManga(ArrayList<Manga> mangas) throws Exception {
        assertNotNull(mangas);
        assertFalse(mangas.isEmpty());

        // some servers list Manga without chapters - so try to fetch up to 'retries' times
        Manga manga;
        int retries = 5;
        do {
            manga = mangas.get(rand.nextInt(mangas.size()));
            assertNotNull(manga);

            testLoadManga(manga);
            serverBase.loadChapters(manga, false);
        } while (manga.getChapters().isEmpty() && retries-- > 0);
        assertFalse(manga.getChapters().isEmpty());

        Chapter chapter = manga.getChapter(rand.nextInt(manga.getChapters().size()));
        assertNotNull(chapter);
        testInitAndGetImage(chapter);
    }

    private void testLoadManga(Manga manga) throws Exception {
        String test_context = String.format(Locale.getDefault(), "[MNG] %s (%s)", manga.getTitle(), manga.getPath());

        serverBase.loadMangaInformation(manga, false);
        assertNotNull(test_context, manga.getImages());
        // manga.getImages() might be empty

        assertNotNull(test_context, manga.getSynopsis());
        assertFalse(test_context, manga.getSynopsis().isEmpty());

        assertNotNull(test_context, manga.getAuthor());
        assertFalse(test_context, manga.getAuthor().isEmpty());

        assertNotNull(test_context, manga.getGenre());
        assertFalse(test_context, manga.getGenre().isEmpty());
    }

    private void testInitAndGetImage(Chapter chapter) throws Exception {
        String test_context = String.format(Locale.getDefault(), "[CHP] %s (%s)", chapter.getTitle(), chapter.getPath());

        String image, dimage;
        serverBase.chapterInit(chapter);
        assertFalse(test_context, chapter.getPages() == 0);

        {
            // check random image link
            image = serverBase.getImageFrom(chapter, rand.nextInt(chapter.getPages()) + 1);
            assertNotNull(test_context, image);
            assertFalse(test_context, image.isEmpty());

            dimage = Navigator.getInstance().getAndReturnResponseCodeOnFailure(image);
            assertTrue(test_context + ", " + dimage + ": " + image, dimage.length() > 5);
        }

        {
            // additional checking of the last page (to verify array indexing)
            image = serverBase.getImageFrom(chapter, chapter.getPages());
            assertNotNull(test_context, image);
            assertFalse(test_context, image.isEmpty());

            dimage = Navigator.getInstance().getAndReturnResponseCodeOnFailure(image);
            assertTrue(test_context + ", " + dimage + ": " + image, dimage.length() > 5);
        }
    }
}
