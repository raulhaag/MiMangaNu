package ar.rulosoft.mimanganu.utils.autotest;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.FromFolder;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.utils.autotest.adapters.TestServerAdapter;
import ar.rulosoft.navegadores.Navigator;

public class RunAutoTest extends Activity {

    boolean running = false;
    boolean finished = true;
    Button btn_start, btn_stop;
    TextView logText;
    ScrollView scrollView;
    StringBuilder log = new StringBuilder();
    ServerBase[] servers;
    private Random rand;
    private TestServerAdapter serverRecAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_auto_test);
        btn_start = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);
        logText = findViewById(R.id.logtext);
        scrollView = findViewById(R.id.scrollview);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                running = false;
            }
        });
        RecyclerView server_list = findViewById(R.id.server_list);
        server_list.setLayoutManager(new LinearLayoutManager(RunAutoTest.this));
        servers = ServerBase.getServers(RunAutoTest.this);
        serverRecAdapter = new TestServerAdapter(servers);
        server_list.setAdapter(serverRecAdapter);
        serverRecAdapter.setOnServerClickListener(new TestServerAdapter.OnServerClickListener() {
            @Override
            public void onServerClick(final ServerBase server) {

            }
        });

    }

    private void moveScroll() {
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.smoothScrollTo(0, logText.getBottom());
            }
        });
    }

    private void start() {
        if (finished) {
            finished = false;
            running = true;
            log = new StringBuilder();
            new RunTest().execute();
        } else {
            Toast.makeText(RunAutoTest.this, "Already running task", Toast.LENGTH_LONG).show();
        }
    }

    private void log(String s) {
        log.append(s).append("<br>");
        RunAutoTest.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logText.setText(Html.fromHtml(log.toString() + "<br><br>"));
                moveScroll();
            }
        });
    }

    private void logOK(String s) {
        log.append("<font color='#00EE00'>").append(s).append("</font><br>");
        RunAutoTest.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logText.setText(Html.fromHtml(log.toString() + "<br><br>"));
                moveScroll();
            }
        });
    }

    private void logError(String s) {
        log.append("<font color='#EE0000'>").append(s).append("</font><br>");
        RunAutoTest.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logText.setText(Html.fromHtml(log.toString() + "<br><br>"));
                moveScroll();
            }
        });
    }

    private void logWarning(String s) {
        log.append("<font color='#00FFFF'>").append(s).append("</font><br>");
        RunAutoTest.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logText.setText(Html.fromHtml(log.toString() + "<br><br>"));
                moveScroll();
            }
        });
    }

    private class RunTest extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            rand = new Random();
            try {
                Navigator.initialiseInstance(RunAutoTest.this);
            } catch (Exception e) {
                logError("[ERROR] error on initialization.");
                return null;
            }
            for (ServerBase sb : servers) {
                if (running) {
                    if (!(sb instanceof FromFolder)) {
                        log(String.format(Locale.getDefault(), "Testing: %s (id=%d)", sb.getServerName(), sb.getServerID()));
                        if (sb.hasFilteredNavigation()) {
                            log("(filtered navigation)");
                            ArrayList<Manga> mangas = null;
                            try {
                                mangas = sb.getMangasFiltered(sb.getBasicFilter(), 1);
                                testManga(mangas, sb);
                            } catch (Exception e) {
                                logError("[Error] Error in filtered navigation: " + e.getMessage());
                            }
                        }
                        if (sb.hasList()) {
                            log("(list)");
                            try {
                                ArrayList<Manga> mangas = sb.getMangas();
                                testManga(mangas, sb);
                            } catch (Exception e) {
                                logError("[Error] Error in list navigation: " + e.getMessage());
                            }
                        }

                        if (sb.hasSearch()) {
                            log("(search)");
                            try {
                                ArrayList<Manga> mangas = sb.search("world");
                                testManga(mangas, sb);
                            } catch (Exception e) {
                                logError("[Error] Error on search: " + e.getMessage());
                            }

                        }
                        log("-----------------------------------------------");
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            finished = true;
        }

        private void testManga(ArrayList<Manga> mangas, ServerBase sb) throws Exception {
            if (mangas == null || mangas.isEmpty()) {
                logError("[Error] Manga list empty.");
                return;
            }

            // some servers list Manga without chapters - so try to fetch up to 'retries' times
            Manga manga;
            int retries = 5;
            do {
                manga = mangas.get(rand.nextInt(mangas.size()));
                if (manga == null) {
                    logError("[Error] Manga is null.");
                    continue;
                }

                testLoadManga(manga, sb);
                try {
                    sb.loadChapters(manga, false);
                } catch (Exception e) {
                    logError("[Error] " + e.getMessage());
                    continue;
                }

                if (manga.getChapters().isEmpty()) {
                    logWarning("[WRN] no chapters found - will try another Manga.");
                }
            } while (manga.getChapters().isEmpty() && retries-- > 0);

            if (manga.getChapters().isEmpty()) {
                logError("[ERROR] no chapters found.");
            }

            ArrayList<Chapter> chapters = manga.getChapters();
            Chapter.Comparators.setManga_title(manga.getTitle());
            Collections.sort(chapters, Chapter.Comparators.NUMBERS_ASC);
            if (!manga.getChapters().equals(chapters)) {
                logWarning("[WRN] chapters not sorted ascending.");
            }

            Chapter chapter = manga.getChapter(rand.nextInt(manga.getChapters().size()));
            if (chapter == null) {
                logError("[ERROR] null chapter.");
                return;
            }
            testInitChapter(chapter, sb);
        }

        private void testLoadManga(Manga manga, ServerBase sb) {
            log(String.format(Locale.getDefault(), "[MNG] %s (%s)", manga.getTitle(), manga.getPath()));

            if (manga.getTitle() == null || manga.getTitle().trim().isEmpty()) {
                logError("[ERROR] Error on manga title");
            }

            if (manga.getPath() == null || manga.getTitle().trim().isEmpty() || (manga.getPath().split("//", -1).length - 1) > 1) {
                logError("[ERROR] Error on manga path");
            }

            // assertTrue(getContext(), (manga.getPath().split("//", -1).length - 1) <= 1);

            try {
                sb.loadMangaInformation(manga, false);
            } catch (Exception e) {
                logError(e.getMessage());
            }
            if (manga.getImages() == null || manga.getImages().isEmpty()) {
                logWarning("[WRN] no cover image set.");
            }

            if (manga.getSynopsis() == null || manga.getSynopsis().trim().isEmpty()) {
                logError("[WRN] no synopsis.");
            }
            if (manga.getSynopsis().equals(getString(R.string.nodisponible))) {
                logWarning("[WRN] default value for 'summary'");
            }

            if (manga.getAuthor() == null || manga.getAuthor().trim().isEmpty()) {
                logError("[WRN] no author.");
            }

            if (manga.getAuthor().equals(getString(R.string.nodisponible))) {
                logWarning("[WRN] default value for 'author'");
            }

            if (manga.getGenre() == null || manga.getGenre().trim().isEmpty()) {
                logError("[WRN] no genre.");
            }

            if (manga.getGenre().equals(getString(R.string.nodisponible))) {
                logWarning("[WRN] default value for 'genre'");
            }
        }

        private void testInitChapter(Chapter chapter, ServerBase sb) throws Exception {
            log(String.format(Locale.getDefault(), "[CHP] %s (%s)", chapter.getTitle(), chapter.getPath()));

            if (chapter.getTitle() == null || chapter.getTitle().trim().isEmpty()) {
                logError("[ERROR] no chapter title");
            }

            if (chapter.getTitle() == null || chapter.getPath().trim().isEmpty() || (chapter.getPath().split("//", -1).length - 1) > 1) {
                logError("[ERROR] no path " + chapter.getPath());
                return;
            }

            if (chapter.getPath().toLowerCase().startsWith("http")) {
                logWarning("[WRN] relative chapter paths shall be used if possible");
            }

            try {
                sb.chapterInit(chapter);
            } catch (Exception e) {
                logError("[ERROR] on chapterInit: " + e.getMessage());
            }
            if (chapter.getPages() < 1) {
                logError("[ERROR] on chapterInit: 0 Pages");
                return;
            }
            if (chapter.getPages() == 1) {
                System.err.println("[WRN] the chapter consists only of one page - possible, but suspicious.");
            }

            // check random image link
            String url = null;
            int numimg = 0;
            try {
                numimg = rand.nextInt(chapter.getPages()) + 1;
                url = sb.getImageFrom(chapter, numimg);
            } catch (Exception e) {
                logError("[NUM] " + numimg + " - " + e.getMessage());
            }
            testLoadImage(url);

            // additional checking of the last page (to verify array indexing)
            url = null;
            try {
                numimg = chapter.getPages();
                url = sb.getImageFrom(chapter, numimg);
            } catch (Exception e) {
                logError("[NUM] " + numimg + " - " + e.getMessage());
            }
            testLoadImage(url);
        }

        private void testLoadImage(String image) throws Exception {
            String[] parts = image.split("\\|");
            log(String.format(Locale.getDefault(), "[IMG] %s", parts[0]));
            if (image == null || image.isEmpty()) {
                logError("[ERROR] Image in null or empty ");
            }

            // additional test for NineManga servers to detect broken hotlinking
            if (image.equals("http://es.taadd.com/files/img/57ead05682694a7c026f99ad14abb8c1.jpg")) {
                logError("[Error] NineManga giving spam image");
                return;
            }
            // check if the image loaded has at least 1kB (assume proper content)
            Navigator nav = Navigator.getInstance();
            if (parts.length > 1) {
                nav.addHeader("Referer", parts[1]);
            }
            image = nav.getAndReturnResponseCodeOnFailure(parts[0]);
            if (image.length() < 1024) {
                logError("[CONTENT] " + image);
            }
        }
    }
}
