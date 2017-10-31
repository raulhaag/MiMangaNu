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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

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
            try {
                serverBase.loadChapters(manga, false);
            }
            catch (Exception e) {
                fail(getContext(e.getMessage()));
            }

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
        assertNotNull(getContext(), manga.getTitle());
        assertFalse(getContext(), manga.getTitle().isEmpty());
        assertEquals(getContext(), manga.getTitle(), manga.getTitle().trim());

        assertNotNull(getContext(), manga.getPath());
        assertFalse(getContext(), manga.getPath().isEmpty());

        logMessage(String.format(Locale.getDefault(), "[MNG] %s (%s)", manga.getTitle(), manga.getPath()));

        try {
            serverBase.loadMangaInformation(manga, false);
        }
        catch (Exception e) {
            fail(getContext(e.getMessage()));
        }
        assertNotNull(getContext(), manga.getImages());
        // manga.getImages() might be empty

        assertNotNull(getContext(), manga.getSynopsis());
        assertFalse(getContext(), manga.getSynopsis().isEmpty());
        assertEquals(getContext(), manga.getSynopsis(), manga.getSynopsis().trim());

        assertNotNull(getContext(), manga.getAuthor());
        assertFalse(getContext(), manga.getAuthor().isEmpty());
        assertEquals(getContext(), manga.getAuthor(), manga.getAuthor().trim().replaceAll("\\s+", " "));

        assertNotNull(getContext(), manga.getGenre());
        assertFalse(getContext(), manga.getGenre().isEmpty());
        assertEquals(getContext(), manga.getGenre(), manga.getGenre().trim().replaceAll("\\s+", " "));
        assertEquals(getContext(), manga.getGenre(), manga.getGenre().replaceAll(",+", ","));
        assertFalse(getContext(), manga.getGenre().startsWith(","));
        assertFalse(getContext(), manga.getGenre().endsWith(","));
    }

    private void testInitChapter(Chapter chapter) throws Exception {
        logMessage(String.format(Locale.getDefault(), "[CHP] %s (%s)", chapter.getTitle(), chapter.getPath()));

        try {
            serverBase.chapterInit(chapter);
        }
        catch (Exception e) {
            fail(getContext(e.getMessage()));
        }
        assertFalse(getContext(), chapter.getPages() == 0);

        // check random image link
        String url = null;
        int numimg = 0;
        try {
            numimg = rand.nextInt(chapter.getPages()) + 1;
            url = serverBase.getImageFrom(chapter, numimg);
        }
        catch (Exception e) {
            fail(getContext("[NUM] " + numimg + " - " + e.getMessage()));
        }
        testLoadImage(url);

        // additional checking of the last page (to verify array indexing)
        url = null;
        try {
            numimg = chapter.getPages();
            url = serverBase.getImageFrom(chapter, numimg);
        }
        catch (Exception e) {
            fail(getContext("[NUM] " + numimg + " - " + e.getMessage()));
        }
        testLoadImage(url);
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
        image = Navigator.getInstance().getAndReturnResponseCodeOnFailure(image);
        assertTrue(getContext("[CONTENT] " + image), image.length() > 1024);
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
            return "\n" + TextUtils.join("\n", messages) + "\n";
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
