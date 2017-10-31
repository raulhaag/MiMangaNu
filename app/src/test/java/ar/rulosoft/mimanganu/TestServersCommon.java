package ar.rulosoft.mimanganu;

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

class TestServersCommon {

    private ServerBase serverBase;
    private Random rand;

    TestServersCommon(ServerBase serverBase) throws Exception {
        System.out.printf("Testing: %s (id=%d)\n", serverBase.getServerName(), serverBase.getServerID());

        this.serverBase = serverBase;
        this.rand = new Random();

        if(serverBase instanceof DeadServer) {
            System.out.println("[INFO] server is an instance of DeadServer - skipping.");
            return;
        }

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

            if(manga.getChapters().isEmpty()) {
                System.out.println("[WRN] no chapters found - will try another Manga.");
            }
        } while (manga.getChapters().isEmpty() && retries-- > 0);
        assertFalse(manga.getChapters().isEmpty());

        Chapter chapter = manga.getChapter(rand.nextInt(manga.getChapters().size()));
        assertNotNull(chapter);
        testInitChapter(chapter);
    }

    private void testLoadManga(Manga manga) throws Exception {
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

    private void testInitChapter(Chapter chapter) throws Exception {
        System.out.printf("[CHP] %s (%s)\n", chapter.getTitle(), chapter.getPath());

        serverBase.chapterInit(chapter);
        assertFalse(chapter.getPages() == 0);

        // check random image link
        testLoadImage(serverBase.getImageFrom(chapter, rand.nextInt(chapter.getPages()) + 1));

        // additional checking of the last page (to verify array indexing)
        testLoadImage(serverBase.getImageFrom(chapter, chapter.getPages()));
    }

    private void testLoadImage(String image) throws Exception {
        System.out.printf("[IMG] %s\n", image);

        assertNotNull(image);
        assertFalse(image.isEmpty());

        // additional test for NineManga servers to detect broken hotlinking
        assertFalse(
                "NineManga: hotlink detection - server delivers bogus image link",
                image.equals("http://es.taadd.com/files/img/57ead05682694a7c026f99ad14abb8c1.jpg")
        );

        // check if the image loaded has at least 1kB (assume proper content)
        image = Navigator.getInstance().getAndReturnResponseCodeOnFailure(image);
        assertTrue(image, image.length() > 1024);
    }
}
