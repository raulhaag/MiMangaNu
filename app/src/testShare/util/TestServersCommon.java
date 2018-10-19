package util;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import java.util.Stack;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.DeadServer;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.navegadores.Navigator;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestServersCommon {

    private ServerBase serverBase;
    private boolean hostBasedTests;
    private Context context;

    private Random rand;
    private Stack<String> messages;

    /**
     * Create a common test case to be used for host based unit tests or tests using the
     * instrumentation API (i.e. device based testing).
     *
     * @param serverId       the id to get the proper <code>ServerBase</code> instance to test
     * @param hostBasedTests <code>true</code> if tests are to be run on the host, <code>false</code>
     *                       otherwise
     * @param context        the <code>Context</code> to use during testing
     * @throws Exception if something goes wrong
     */
    public TestServersCommon(int serverId, boolean hostBasedTests, Context context) throws Exception {
        this.rand = new Random();
        this.messages = new Stack<>();

        this.serverBase = ServerBase.getServer(serverId, context);
        this.hostBasedTests = hostBasedTests;
        this.context = context;

        logMessage(String.format(Locale.getDefault(), "Testing: %s (id=%d)", serverBase.getServerName(), serverBase.getServerID()));

        if (serverBase instanceof DeadServer) {
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
            } catch (Exception e) {
                fail(getContext(e.getMessage()));
            }

            if (manga.getChapters().isEmpty()) {
                logMessage("[WRN] no chapters found - will try another Manga.");
            }
        } while (manga.getChapters().isEmpty() && retries-- > 0);
        assertFalse(getContext(), manga.getChapters().isEmpty());

        ArrayList<Chapter> chapters = manga.getChapters();
        Chapter.Comparators.setManga_title(manga.getTitle());
        Collections.sort(chapters, Chapter.Comparators.NUMBERS_ASC);
        assertEquals(getContext("chapters not sorted ascending"), manga.getChapters(), chapters);

        Chapter chapter = manga.getChapter(rand.nextInt(manga.getChapters().size()));
        assertNotNull(getContext(), chapter);
        testInitChapter(chapter);
    }

    private void testLoadManga(Manga manga) {
        logMessage(String.format(Locale.getDefault(), "[MNG] %s (%s)", manga.getTitle(), manga.getPath()));

        assertNotNull(getContext(), manga.getTitle());
        assertFalse(getContext(), manga.getTitle().isEmpty());
        assertEquals(getContext(), manga.getTitle(), manga.getTitle().trim());

        assertNotNull(getContext(), manga.getPath());
        assertFalse(getContext(), manga.getPath().isEmpty());
        assertTrue(getContext(), (manga.getPath().split("//", -1).length - 1) <= 1);

        try {
            serverBase.loadMangaInformation(manga, false);
        } catch (Exception e) {
            fail(getContext(e.getMessage()));
        }
        assertNotNull(getContext(), manga.getImages());
        if (manga.getImages().isEmpty()) {
            System.err.println("[WRN] no cover image set");
        }

        assertNotNull(getContext(), manga.getSynopsis());
        assertFalse(getContext(), manga.getSynopsis().isEmpty());
        assertEquals(getContext(), manga.getSynopsis(), manga.getSynopsis().trim());
        if (manga.getSynopsis().equals(context.getString(R.string.nodisponible))) {
            System.err.println("[WRN] default value for 'summary'");
        }

        assertNotNull(getContext(), manga.getAuthor());
        assertFalse(getContext(), manga.getAuthor().isEmpty());
        assertEquals(getContext(), manga.getAuthor(), manga.getAuthor().trim().replaceAll("\\s+", " "));
        if (manga.getAuthor().equals(context.getString(R.string.nodisponible))) {
            System.err.println("[WRN] default value for 'author'");
        }

        assertNotNull(getContext(), manga.getGenre());
        assertFalse(getContext(), manga.getGenre().isEmpty());
        assertEquals(getContext(), manga.getGenre(), manga.getGenre().trim().replaceAll("\\s+", " "));
        assertEquals(getContext(), manga.getGenre(), manga.getGenre().replaceAll(",+", ","));
        assertFalse(getContext(), manga.getGenre().startsWith(","));
        assertFalse(getContext(), manga.getGenre().endsWith(","));
        if (manga.getGenre().equals(context.getString(R.string.nodisponible))) {
            System.err.println("[WRN] default value for 'genre'");
        }
    }

    private void testInitChapter(Chapter chapter) throws Exception {
        logMessage(String.format(Locale.getDefault(), "[CHP] %s (%s)", chapter.getTitle(), chapter.getPath()));

        assertNotNull(getContext(), chapter.getTitle());
        assertFalse(getContext(), chapter.getTitle().isEmpty());

        assertNotNull(getContext(), chapter.getPath());
        assertFalse(getContext(), chapter.getPath().isEmpty());
        assertTrue(getContext(), (chapter.getPath().split("//", -1).length - 1) <= 1);

        if(chapter.getPath().startsWith("http")) {
            System.err.println("[WRN] relative chapter paths shall be used if possible");
        }

        try {
            serverBase.chapterInit(chapter);
        } catch (Exception e) {
            fail(getContext(e.getMessage()));
        }
        assertTrue(getContext(), chapter.getPages() > 1);

        // check random image link
        String url = null;
        int numimg = 0;
        try {
            numimg = rand.nextInt(chapter.getPages()) + 1;
            url = serverBase.getImageFrom(chapter, numimg);
        } catch (Exception e) {
            fail(getContext("[NUM] " + numimg + " - " + e.getMessage()));
        }
        testLoadImage(url);

        // additional checking of the last page (to verify array indexing)
        url = null;
        try {
            numimg = chapter.getPages();
            url = serverBase.getImageFrom(chapter, numimg);
        } catch (Exception e) {
            fail(getContext("[NUM] " + numimg + " - " + e.getMessage()));
        }
        testLoadImage(url);
    }

    private void testLoadImage(String image) throws Exception {
        String parts[] = image.split("\\|");
        logMessage(String.format(Locale.getDefault(), "[IMG] %s", parts[0]));
        assertNotNull(getContext(), image);
        assertFalse(getContext(), image.isEmpty());
        if (parts.length == 1)
            assertTrue(getContext(), (image.split("//", -1).length - 1) <= 1);
        else
            assertTrue(getContext(), (image.split("//", -1).length - 1) <= 2);

        // additional test for NineManga servers to detect broken hotlinking
        assertFalse(
                getContext("NineManga: hotlink detection - server delivers bogus image link"),
                image.equals("http://es.taadd.com/files/img/57ead05682694a7c026f99ad14abb8c1.jpg")
        );

        // check if the image loaded has at least 1kB (assume proper content)
        Navigator nav = Navigator.getInstance();
        if (parts.length > 1) {
            nav.addHeader("Referer", parts[1]);
        }
        image = nav.getAndReturnResponseCodeOnFailure(parts[0]);
        assertTrue(getContext("[CONTENT] " + image), image.length() > 1024);
    }

    /**
     * Log a message.
     * <p>
     * For host based tests, the message will directly be written on <code>System.out</code>. Device
     * based tests do not have this pipe connected, so the message will be stored for now.
     *
     * @param msg the message to log
     */
    private void logMessage(String msg) {
        if (hostBasedTests) {
            // direct output
            System.out.println(msg);
            System.out.flush();
        } else {
            // store
            messages.push(msg);
        }
    }

    /**
     * Create a context message to be used in assertions as message.
     * <p>
     * Returns nothing for host-based tests as <code>System.out</code> is working, for device based
     * tests it will dump the collected logs as context before the exception stack trace.
     * <p>
     * Use this like <code>assertXXX(getContext(), condition);</code>.
     *
     * @return a string representing the log up to now
     */
    private String getContext() {
        if (hostBasedTests) {
            return "\n";
        } else {
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
