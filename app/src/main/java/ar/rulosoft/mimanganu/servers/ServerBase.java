package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.MainActivity;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navegador;

public abstract class ServerBase {

    public enum FilteredType {VISUAL, TEXT}

    public static final int MANGAPANDA = 1;
    public static final int ESMANGAHERE = 3;
    public static final int MANGAHERE = 4;
    public static final int MANGAFOX = 5;
    public static final int SUBMANGA = 6;
    public static final int ESMANGA = 7;
    public static final int HEAVENMANGACOM = 8;
    public static final int STARKANACOM = 9;
    public static final int ESNINEMANGA = 10;
    public static final int LECTUREENLIGNE = 11;
    public static final int KISSMANGA = 12;
    public static final int ITNINEMANGA = 13;
    public static final int TUSMANGAS = 14;
    public static final int MANGAREADER = 15;
    public static final int DENINEMANGA = 16;
    public static final int RUNINEMANGA = 17;
    public static final int MANGATUBE = 18;
    public static final int MANGAEDENIT = 19;
    public static final int MYMANGAIO = 20;
    public static final int RAWSENMANGA = 21;
    public static final int TUMANGAONLINE = 22;
    public static final int NINEMANGA = 23;
    public static final int MANGAEDEN = 24;

    public static final int READCOMICONLINE = 1000;
    public static final int FROMFOLDER = 1001;

    public boolean hasMore = true;
    private String serverName;
    private int icon;
    private int flag;
    private int serverID;

    public static ServerBase getServer(int id) {
        ServerBase serverBase = null;
        switch (id) {
            case MANGAPANDA:
                serverBase = new MangaPanda();
                break;
            case ESMANGAHERE:
                serverBase = new EsMangaHere();
                break;
            case MANGAHERE:
                serverBase = new MangaHere();
                break;
            case MANGAFOX:
                serverBase = new MangaFox();
                break;
            case SUBMANGA:
                serverBase = new SubManga();
                break;
            case ESMANGA:
                serverBase = new EsManga();
                break;
            case HEAVENMANGACOM:
                serverBase = new HeavenManga();
                break;
            case MANGAREADER:
                serverBase = new MangaReader();
                break;
            case ESNINEMANGA:
                serverBase = new EsNineManga();
                break;
            case LECTUREENLIGNE:
                serverBase = new LectureEnLigne();
                break;
            case KISSMANGA:
                serverBase = new KissManga();
                break;
            case ITNINEMANGA:
                serverBase = new ItNineManga();
                break;
            case TUMANGAONLINE:
                serverBase = new TuMangaOnline();
                break;
            case TUSMANGAS:
                serverBase = new TusMangasOnlineCom();
                break;
            case STARKANACOM:
                serverBase = new StarkanaCom();
                break;
            case DENINEMANGA:
                serverBase = new DeNineManga();
                break;
            case RUNINEMANGA:
                serverBase = new RuNineManga();
                break;
            case MANGATUBE:
                serverBase = new Manga_Tube();
                break;
            case MANGAEDENIT:
                serverBase = new MangaEdenIt();
                break;
            case MYMANGAIO:
                serverBase = new MyMangaIo();
                break;
            case RAWSENMANGA:
                serverBase = new RawSenManga();
                break;
            case MANGAEDEN:
                serverBase = new MangaEden();
                break;
            case NINEMANGA:
                serverBase = new NineManga();
                break;
            case READCOMICONLINE:
                serverBase = new ReadComicOnline();
                break;
            case FROMFOLDER:
                serverBase = new FromFolder();
                break;
            default:
                break;
        }
        return serverBase;
    }

    // server
    public abstract ArrayList<Manga> getMangas() throws Exception;

    public abstract ArrayList<Manga> search(String term) throws Exception;

    // chapter
    public abstract void loadChapters(Manga manga, boolean forceReload) throws Exception;

    public abstract void loadMangaInformation(Manga manga, boolean forceReload) throws Exception;

    // manga
    public abstract String getPagesNumber(Chapter chapter, int page);

    public abstract String getImageFrom(Chapter chapter, int page) throws Exception;

    public abstract void chapterInit(Chapter chapter) throws Exception;

    // server visual
    public abstract ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception;

    public abstract String[] getCategories();

    public abstract String[] getOrders();

    public abstract boolean hasList();

    // public abstract boolean supportStatus();

    public int searchForNewChapters(int id, Context context) throws Exception {
        int returnValue = 0;
        Manga mangaDb = Database.getFullManga(context, id);
        Manga manga = new Manga(mangaDb.getServerId(), mangaDb.getTitle(), mangaDb.getPath(), false);
        manga.setId(mangaDb.getId());
        this.loadMangaInformation(manga, true);
        this.loadChapters(manga, false);
        int diff = manga.getChapters().size() - mangaDb.getChapters().size();
        if (diff > 0) {
            ArrayList<Chapter> simpleList = new ArrayList<>();
            if (manga.getChapters().size() < diff) {
                simpleList.addAll(manga.getChapters().subList(0, diff));
                simpleList.addAll(manga.getChapters().subList(
                        manga.getChapters().size() -
                                diff, manga.getChapters().size()));
                ArrayList<Chapter> simpleListC = new ArrayList<>();
                simpleListC.addAll(mangaDb.getChapters().subList(0, diff));
                simpleListC.addAll(mangaDb.getChapters().subList(
                        mangaDb.getChapters().size() -
                                diff, mangaDb.getChapters().size()));
                for (Chapter c : simpleListC) {
                    for (Chapter csl : simpleList) {
                        if (c.getPath().equalsIgnoreCase(csl.getPath())) {
                            simpleList.remove(csl);
                            break;
                        }
                    }
                }
            }
            if (simpleList.size() == 1) {
                Chapter c = simpleList.get(0);
                for (Chapter cap : manga.getChapters()) {
                    if (cap.getPath().equalsIgnoreCase(c.getPath())) {
                        simpleList.remove(0);
                        break;
                    }
                }
            }

            if (!(simpleList.size() >= diff)) {
                simpleList = new ArrayList<>();
                for (Chapter c : manga.getChapters()) {
                    boolean masUno = true;
                    for (Chapter csl : mangaDb.getChapters()) {
                        if (c.getPath().equalsIgnoreCase(csl.getPath())) {
                            mangaDb.getChapters().remove(csl);
                            masUno = false;
                            break;
                        }
                    }
                    if (masUno) {
                        simpleList.add(c);
                    }
                }
                // simpleList = manga.getChapters();
            }
            for (Chapter chapter : simpleList) {
                chapter.setMangaID(mangaDb.getId());
                chapter.setReadStatus(Chapter.NEW);
                Database.addChapter(context, chapter, mangaDb.getId());
            }

            if (simpleList.size() > 0) {
                Database.updateMangaRead(context, mangaDb.getId());
                Database.updateNewMangas(context, mangaDb, diff);
            }

            if(!simpleList.isEmpty())
                new CreateGroupByMangaNotificationsTask(simpleList, manga, context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            returnValue = simpleList.size();
        }

        boolean changes = false;
        if (!mangaDb.getAuthor().equals(manga.getAuthor()) &&
                manga.getAuthor().length() > 2) {
            mangaDb.setAuthor(manga.getAuthor());
            changes = true;
        }

        if (!mangaDb.getImages().equals(manga.getImages()) &&
                manga.getImages().length() > 2) {
            mangaDb.setImages(manga.getImages());
            changes = true;
        }

        if (!mangaDb.getSynopsis().equals(manga.getSynopsis()) &&
                manga.getSynopsis().length() > 2) {
            mangaDb.setSynopsis(manga.getSynopsis());
            changes = true;
        }

        if (!mangaDb.getGenre().equals(manga.getGenre()) &&
                manga.getGenre().length() > 2) {
            mangaDb.setGenre(manga.getGenre());
            changes = true;
        }
        if (mangaDb.isFinished() != manga.isFinished()) {
            mangaDb.setFinished(manga.isFinished());
            changes = true;
        }

        if (changes) Database.updateManga(context, mangaDb, false);

        return returnValue;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getServerID() {
        return serverID;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public static String getFirstMatch(String patron, String source, String errorMsj) throws Exception {
        Pattern p = Pattern.compile(patron);
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group(1);
        }
        throw new Exception(errorMsj);
    }

    public ArrayList<String> getAllMatch(String patron, String source) throws Exception {
        Pattern p = Pattern.compile(patron);
        Matcher m = p.matcher(source);
        ArrayList<String> matches = new ArrayList<>();
        if (m.find()) {
            matches.add(m.group(1));
        }
        return matches;
    }

    public String getFirstMatchDefault(String patron, String source, String mDefault) throws Exception {
        Pattern p = Pattern.compile(patron);
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group(1);
        } else {
            return mDefault;
        }
    }

    public boolean needRefererForImages() {
        return true;
    }

    public boolean hasFilteredNavigation() {
        return true;
    }

    Navegador getNavWithHeader() throws Exception {
        return new Navegador();
    }

    public FilteredType getFilteredType() {
        return FilteredType.VISUAL;
    }

    public static ServerBase[] getServers() {
        return (new ServerBase[]{
                new TuMangaOnline(),
                new HeavenManga(),
                new SubManga(),
                new EsNineManga(),
                new EsMangaHere(),
                new MangaPanda(),
                new MangaReader(),
                new MangaHere(),
                new MangaFox(),
                new KissManga(),
                new MangaEden(),
                new NineManga(),
                new RuNineManga(),
                new LectureEnLigne(),
                new MyMangaIo(),
                new ItNineManga(),
                new MangaEdenIt(),
                new DeNineManga(),
                new Manga_Tube(),
                new RawSenManga(),
               //new ReadComicOnline(),
                new FromFolder()
        });
    }

    /*public class CreateNotificationsTask extends AsyncTask<Void, Integer, Integer> {
        private ArrayList<Chapter> simpleList = new ArrayList<>();
        private Context context;
        private Manga manga;

        public CreateNotificationsTask(ArrayList<Chapter> simpleList, Manga manga, Context context) {
            this.simpleList.addAll(simpleList);
            this.context = context;
            this.manga = manga;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            for (int i = 0; i < simpleList.size(); i++) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("manga_id", simpleList.get(i).getMangaID());
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                Util.getInstance().createNotification(context, false, (int) System.currentTimeMillis(), intent, context.getResources().getString(R.string.new_chapter, manga.getTitle()), simpleList.get(i).getTitle());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
        }
    }*/

    public class CreateGroupByMangaNotificationsTask extends AsyncTask<Void, Integer, Integer> {
        private ArrayList<Chapter> simpleList = new ArrayList<>();
        private Context context;
        private Manga manga;
        private String LargeContentText = null;

        public CreateGroupByMangaNotificationsTask(ArrayList<Chapter> simpleList, Manga manga, Context context) {
            this.simpleList.addAll(simpleList);
            this.context = context;
            this.manga = manga;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            for (int i = simpleList.size() - 1; i > -1; i--) {
                if (LargeContentText == null)
                    if (i == 0)
                        LargeContentText = simpleList.get(i).getTitle();
                    else
                        LargeContentText = simpleList.get(i).getTitle() + "\n";
                else {
                    if (i == 0)
                        LargeContentText = LargeContentText + simpleList.get(i).getTitle();
                    else
                        LargeContentText = LargeContentText + simpleList.get(i).getTitle() + "\n";
                }
            }

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("manga_id", simpleList.get(0).getMangaID());
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            if (simpleList.size() > 1) {
                Util.getInstance().createNotification(context, false, (int) System.currentTimeMillis(), intent, simpleList.size() + " " + context.getResources().getString(R.string.new_chapters, manga.getTitle()), LargeContentText);
            } else {
                Util.getInstance().createNotification(context, false, (int) System.currentTimeMillis(), intent, simpleList.size() + " " + context.getResources().getString(R.string.new_chapter, manga.getTitle()), LargeContentText);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
        }
    }

}