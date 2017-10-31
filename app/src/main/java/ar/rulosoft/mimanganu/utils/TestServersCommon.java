package ar.rulosoft.mimanganu.utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Stack;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.DeadServer;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.navegadores.Navigator;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class TestServersCommon {

    private ServerBase serverBase;
    private boolean hostBasedTests;

    private Random rand;
    private Stack<String> messages;

    /**
     * Create a common test case to be used for host based unit tests or tests using the
     * instrumentation API (i.e. device based testing).
     *
     * @param serverBase     the <code>ServerBase</code> instance to test
     * @param hostBasedTests <code>true</code> if tests are to be run on the host, <code>false</code>
     *                       otherwise
     * @throws Exception     if something goes wrong
     */
    public TestServersCommon(ServerBase serverBase, boolean hostBasedTests) throws Exception {
        this.rand = new Random();
        this.messages = new Stack<>();

        logMessage(String.format(Locale.getDefault(), "Testing: %s (id=%d)", serverBase.getServerName(), serverBase.getServerID()));

        this.serverBase = serverBase;
        this.hostBasedTests = hostBasedTests;

        if(serverBase instanceof DeadServer) {
            logMessage("[INFO] server is an instance of DeadServer - skipping.");
            return;
        }

        if (serverBase.hasFilteredNavigation()) {
            logMessage("(filtered navigation)");

            ArrayList<Manga> mangas = serverBase.getMangasFiltered(serverBase.getBasicFilter(), 1);
            testManga(mangas);
        }

        if (serverBase.hasList()) {
            logMessage("(list)");

            ArrayList<Manga> mangas = serverBase.getMangas();
            testManga(mangas);
        }

        if (serverBase.hasSearch()) {
            logMessage("(search)");

            ArrayList<Manga> mangas = serverBase.search("world");
            testManga(mangas);
        }

        logMessage("Testing: [SUCCESS]\n");
    }

    private void testManga(ArrayList<Manga> mangas) throws Exception {
        assertNotNull(getContext(), mangas);
        assertFalse(getContext(), mangas.isEmpty());

        // some servers list Manga without chapters - so try to fetch up to 'retries' times
        Manga manga;
        int retries = 5;
        do {
            manga = mangas.get(rand.nextInt(mangas.size()));
            assertNotNull(getContext(), manga);

            testLoadManga(manga);
            serverBase.loadChapters(manga, false);

            if(manga.getChapters().isEmpty()) {
                logMessage("[WRN] no chapters found - will try another Manga.");
            }
        } while (manga.getChapters().isEmpty() && retries-- > 0);
        assertFalse(getContext(), manga.getChapters().isEmpty());

        Chapter chapter = manga.getChapter(rand.nextInt(manga.getChapters().size()));
        assertNotNull(getContext(), chapter);
        testInitChapter(chapter);
    }

    private void testLoadManga(Manga manga) throws Exception {
        logMessage(String.format(Locale.getDefault(), "[MNG] %s (%s)", manga.getTitle(), manga.getPath()));

        serverBase.loadMangaInformation(manga, false);
        assertNotNull(getContext(), manga.getImages());
        // manga.getImages() might be empty

        assertNotNull(getContext(), manga.getSynopsis());
        assertFalse(getContext(), manga.getSynopsis().isEmpty());

        assertNotNull(getContext(), manga.getAuthor());
        assertFalse(getContext(), manga.getAuthor().isEmpty());

        assertNotNull(getContext(), manga.getGenre());
        assertFalse(getContext(), manga.getGenre().isEmpty());
    }

    private void testInitChapter(Chapter chapter) throws Exception {
        logMessage(String.format(Locale.getDefault(), "[CHP] %s (%s)", chapter.getTitle(), chapter.getPath()));

        serverBase.chapterInit(chapter);
        assertFalse(getContext(), chapter.getPages() == 0);

        // check random image link
        testLoadImage(serverBase.getImageFrom(chapter, rand.nextInt(chapter.getPages()) + 1));

        // additional checking of the last page (to verify array indexing)
        testLoadImage(serverBase.getImageFrom(chapter, chapter.getPages()));
    }

    private void testLoadImage(String image) throws Exception {
        logMessage(String.format(Locale.getDefault(), "[IMG] %s", image));

        assertNotNull(getContext(), image);
        assertFalse(getContext(), image.isEmpty());

        // additional test for NineManga servers to detect broken hotlinking
        assertFalse(
                getContext("NineManga: hotlink detection - server delivers bogus image link"),
                image.equals("http://es.taadd.com/files/img/57ead05682694a7c026f99ad14abb8c1.jpg")
        );

        // check if the image loaded has at least 1kB (assume proper content)
        String content = Navigator.getInstance().getAndReturnResponseCodeOnFailure(image);
        assertTrue(getContext(image), content.length() > 1024);
    }

    /**
     * Log a message.
     *
     * For host based tests, the message will directly be written on <code>System.out</code>. Device
     * based tests do not have this pipe connected, so the message will be stored for now.
     *
     * @param msg the message to log
     */
    private void logMessage(String msg) {
        if(hostBasedTests) {
            // direct output
            System.out.println(msg);
        }
        else {
            // store
            messages.push(msg);
        }
    }

    /**
     * Create a context message to be used in assertions as message.
     *
     * Returns nothing for host-based tests as <code>System.out</code> is working, for device based
     * tests it will dump the collected logs as context before the exception stack trace.
     *
     * Use this like <code>assertXXX(getContext(), condition);</code>.
     *
     * @return a string representing the log up to now
     */
    private String getContext() {
        if(hostBasedTests) {
            return "\n";
        }
        else {
            return "\n" + TextUtils.join("\n", messages) + "\n-----------------\n";
        }
    }

    /**
     * Create a context message to be used in assertions as message plus a last message.
     *
     * @param msg the last message to output after the collected log
     * @return a string representing the log up to now
     */
    private String getContext(String msg) {
        return getContext() + msg;
    }
}
