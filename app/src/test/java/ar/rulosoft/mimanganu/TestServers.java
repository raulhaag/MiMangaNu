package ar.rulosoft.mimanganu;

import android.content.Context;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Random;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.DeadServer;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.navegadores.Navigator;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

// TODO fix missing Duktape due to NDK code
// TODO fix KeyStore issue via shadowing

// need to specify SDK version for Roboelectric (see targetSdkVersion in app.gradle)
@Config(manifest = "../app/src/main/AndroidManifest.xml", sdk = 25)
@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestServers {

    private Random rand;
    private Context context;
    private ServerBase serverBase;

    @Before
    public void setupTest() throws Exception {
        rand = new Random();
        context = RuntimeEnvironment.application.getApplicationContext();

        Navigator.initialiseInstance(context);
    }

    @Ignore("FromFolder cannot be tested - yet")
    public void test_FROMFOLDER() throws Exception {
        testServer(ServerBase.getServer(ServerBase.FROMFOLDER, context));
    }

    @Test
    public void test_RAWSENMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.RAWSENMANGA, context));
    }

    @Test
    public void test_MANGAPANDA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGAPANDA, context));
    }

    @Test
    public void test_ESMANGAHERE() throws Exception {
        testServer(ServerBase.getServer(ServerBase.ESMANGAHERE, context));
    }

    @Test
    public void test_MANGAHERE() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGAHERE, context));
    }

    @Test
    public void test_MANGAFOX() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGAFOX, context));
    }

    @Test
    public void test_SUBMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.SUBMANGA, context));
    }

    @Test
    public void test_ESMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.ESMANGA, context));
    }

    @Test
    public void test_HEAVENMANGACOM() throws Exception {
        testServer(ServerBase.getServer(ServerBase.HEAVENMANGACOM, context));
    }

    @Test
    public void test_STARKANACOM() throws Exception {
        testServer(ServerBase.getServer(ServerBase.STARKANACOM, context));
    }

    @Test
    public void test_ESNINEMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.ESNINEMANGA, context));
    }

    @Test
    public void test_LECTUREENLIGNE() throws Exception {
        testServer(ServerBase.getServer(ServerBase.LECTUREENLIGNE, context));
    }

    @Test
    public void test_KISSMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.KISSMANGA, context));
    }

    @Test
    public void test_ITNINEMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.ITNINEMANGA, context));
    }

    @Test
    public void test_TUSMANGAS() throws Exception {
        testServer(ServerBase.getServer(ServerBase.TUSMANGAS, context));
    }

    @Test
    public void test_MANGAREADER() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGAREADER, context));
    }

    @Test
    public void test_DENINEMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.DENINEMANGA, context));
    }

    @Test
    public void test_RUNINEMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.RUNINEMANGA, context));
    }

    @Test
    public void test_MANGATUBE() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGATUBE, context));
    }

    @Test
    public void test_MANGAEDENIT() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGAEDENIT, context));
    }

    @Test
    public void test_MYMANGAIO() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MYMANGAIO, context));
    }

    @Test
    public void test_TUMANGAONLINE() throws Exception {
        testServer(ServerBase.getServer(ServerBase.TUMANGAONLINE, context));
    }

    @Test
    public void test_NINEMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.NINEMANGA, context));
    }

    @Test
    public void test_MANGAEDEN() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGAEDEN, context));
    }

    @Test
    public void test_LEOMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.LEOMANGA, context));
    }

    @Ignore("Batoto does not work without login credentials")
    public void test_BATOTO() throws Exception {
        testServer(ServerBase.getServer(ServerBase.BATOTO, context));
    }

    @Ignore("Batoto(ES) does not work without login credentials")
    public void test_BATOTOES() throws Exception {
        testServer(ServerBase.getServer(ServerBase.BATOTOES, context));
    }

    @Test
    public void test_JAPSCAN() throws Exception {
        testServer(ServerBase.getServer(ServerBase.JAPSCAN, context));
    }

    @Test
    public void test_READMANGATODAY() throws Exception {
        testServer(ServerBase.getServer(ServerBase.READMANGATODAY, context));
    }

    @Test
    public void test_TAADD() throws Exception {
        testServer(ServerBase.getServer(ServerBase.TAADD, context));
    }

    @Test
    public void test_MANGASTREAM() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGASTREAM, context));
    }

    @Test
    public void test_MANGAKAWAII() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGAKAWAII, context));
    }

    @Test
    public void test_KUMANGA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.KUMANGA, context));
    }

    @Test
    public void test_MANGAPEDIA() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGAPEDIA, context));
    }

    @Test
    public void test_MANGATOWN() throws Exception {
        testServer(ServerBase.getServer(ServerBase.MANGATOWN, context));
    }

    @Test
    public void test_READCOMICONLINE() throws Exception {
        testServer(ServerBase.getServer(ServerBase.READCOMICONLINE, context));
    }

    @Test
    public void test_READCOMICSTV() throws Exception {
        testServer(ServerBase.getServer(ServerBase.READCOMICSTV, context));
    }

    @Test
    public void test_GOGOCOMIC() throws Exception {
        testServer(ServerBase.getServer(ServerBase.GOGOCOMIC, context));
    }

    @Test
    public void test_VIEWCOMIC() throws Exception {
        testServer(ServerBase.getServer(ServerBase.VIEWCOMIC, context));
    }

    public void testServer(ServerBase serverBase) throws Exception {
        System.out.printf("Testing: %s (id=%d)\n", serverBase.getServerName(), serverBase.getServerID());

        if(serverBase instanceof DeadServer) {
            System.out.println("[INFO] server is an instance of DeadServer - skipping.");
            return;
        }

        this.serverBase = serverBase;
        assertNotNull(context);

        if (serverBase.hasFilteredNavigation()) {
            System.out.println("(filtered navigation)");

            ArrayList<Manga> mangas = serverBase.getMangasFiltered(serverBase.getBasicFilter(), 1);
            testManga(mangas);
        }

        if (serverBase.hasList()) {
            System.out.println("(list)");

            ArrayList<Manga> mangas = serverBase.getMangas();
            testManga(mangas);
        }

        if (serverBase.hasSearch()) {
            System.out.println("(search)");

            ArrayList<Manga> mangas = serverBase.search("world");
            testManga(mangas);
        }

        System.out.printf("Testing: [SUCCESS]\n");
    }

    public void testManga(ArrayList<Manga> mangas) throws Exception {
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

            if(manga.getChapters().isEmpty()) {
                System.out.println("[WRN] no chapters found - will try another Manga.");
            }
        } while (manga.getChapters().isEmpty() && retries-- > 0);
        assertFalse(manga.getChapters().isEmpty());

        Chapter chapter = manga.getChapter(rand.nextInt(manga.getChapters().size()));
        assertNotNull(chapter);
        testInitAndGetImage(chapter);
    }

    public void testLoadManga(Manga manga) throws Exception {
        System.out.printf("[MNG] %s (%s)\n", manga.getTitle(), manga.getPath());

        serverBase.loadMangaInformation(manga, false);
        assertNotNull(manga.getImages());
        // manga.getImages() might be empty

        assertNotNull(manga.getSynopsis());
        assertFalse(manga.getSynopsis().isEmpty());

        assertNotNull(manga.getAuthor());
        assertFalse(manga.getAuthor().isEmpty());

        assertNotNull(manga.getGenre());
        assertFalse(manga.getGenre().isEmpty());
    }

    public void testInitAndGetImage(Chapter chapter) throws Exception {
        System.out.printf("[CHP] %s (%s)\n", chapter.getTitle(), chapter.getPath());

        String image, dimage;
        serverBase.chapterInit(chapter);
        assertFalse(chapter.getPages() == 0);

        {
            // check random image link
            image = serverBase.getImageFrom(chapter, rand.nextInt(chapter.getPages()) + 1);
            assertNotNull(image);
            assertFalse(image.isEmpty());

            System.out.printf("[IMG] load %s\n", image);
            dimage = Navigator.getInstance().getAndReturnResponseCodeOnFailure(image);
            assertTrue(dimage, dimage.length() > 5);
        }

        {
            // additional checking of the last page (to verify array indexing)
            image = serverBase.getImageFrom(chapter, chapter.getPages());
            assertNotNull(image);
            assertFalse(image.isEmpty());

            System.out.printf("[IMG] load %s\n", image);
            dimage = Navigator.getInstance().getAndReturnResponseCodeOnFailure(image);
            assertTrue(dimage, dimage.length() > 5);
        }
    }
}
