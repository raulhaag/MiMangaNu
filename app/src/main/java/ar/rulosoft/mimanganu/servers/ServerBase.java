package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.MainActivity;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;

public abstract class ServerBase {

    public static final int FROMFOLDER = 1001;
    static final int MANGAPANDA = 1;
    static final int ESMANGAHERE = 3;
    static final int MANGAHERE = 4;
    static final int MANGAFOX = 5;
    static final int SUBMANGA = 6;
    static final int ESMANGA = 7;
    static final int HEAVENMANGACOM = 8;
    static final int STARKANACOM = 9;
    static final int ESNINEMANGA = 10;
    static final int LECTUREENLIGNE = 11;
    static final int KISSMANGA = 12;
    static final int ITNINEMANGA = 13;
    static final int TUSMANGAS = 14;
    static final int MANGAREADER = 15;
    static final int DENINEMANGA = 16;
    static final int RUNINEMANGA = 17;
    static final int MANGATUBE = 18;
    static final int MANGAEDENIT = 19;
    static final int MYMANGAIO = 20;
    static final int RAWSENMANGA = 21;
    static final int TUMANGAONLINE = 22;
    static final int NINEMANGA = 23;
    static final int MANGAEDEN = 24;
    static final int LEOMANGA = 25;

    static final int READCOMICONLINE = 1000;
    public boolean hasMore = true;
    protected String defaultSynopsis = "N/A";
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
            case LEOMANGA:
                serverBase = new LeoManga();
                break;
            case FROMFOLDER:
                serverBase = new FromFolder();
                break;
            default:
                break;
        }
        return serverBase;
    }

    public static Navigator getNavigatorAndFlushParameters() {
        Navigator.navigator.flushParameter(); // remove old post parameters
        return Navigator.navigator;
    }

    public static String getFirstMatch(String patron, String source, String errorMsj) throws Exception {
        Pattern p = Pattern.compile(patron);
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group(1);
        }
        throw new Exception(errorMsj);
    }

    public static ServerBase[] getServers() {
        return (new ServerBase[]{
                new TuMangaOnline(),
                new HeavenManga(),
                new SubManga(),
                new EsNineManga(),
                new EsMangaHere(),
                new LeoManga(),
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
                new ReadComicOnline(),
                new FromFolder()
        });
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
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        return new ArrayList<>();
    }

    public abstract boolean hasList();

    public int searchForNewChapters(int id, Context context, boolean fast) throws Exception {
        int returnValue;
        Manga mangaDb = Database.getFullManga(context, id);
        Manga manga = new Manga(mangaDb.getServerId(), mangaDb.getTitle(), mangaDb.getPath(), false);
        manga.setId(mangaDb.getId());
        this.loadMangaInformation(manga, true);
        this.loadChapters(manga, false);
        ArrayList<Chapter> simpleList = new ArrayList<>();
        if (fast && manga.getChapters().size() > 20) {
            int chapters = manga.getChapters().size();
            List<Chapter> f20 = manga.getChapters().subList(chapters - 20, chapters);
            for (Chapter chapter : f20) {
                boolean add = true;
                for (Chapter chapterDB : mangaDb.getChapters()) {
                    if (chapter.getPath().equals(chapterDB.getPath())) {
                        add = false;
                        break;
                    }
                }
                if (add)
                    simpleList.add(chapter);
            }
        } else {
            for (Chapter chapter : manga.getChapters()) {
                boolean add = true;
                for (Chapter chapterDB : mangaDb.getChapters()) {
                    if (chapter.getPath().equals(chapterDB.getPath())) {
                        add = false;
                        break;
                    }
                }
                if (add)
                    simpleList.add(chapter);
            }
        }
        for (Chapter chapter : simpleList) {
            chapter.setMangaID(mangaDb.getId());
            chapter.setReadStatus(Chapter.NEW);
            Database.addChapter(context, chapter, mangaDb.getId());
        }

        if (simpleList.size() > 0) {
            Database.updateMangaRead(context, mangaDb.getId());
            Database.updateNewMangas(context, mangaDb, simpleList.size());
        }

        if (!simpleList.isEmpty())
            new CreateGroupByMangaNotificationsTask(simpleList, manga, context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        returnValue = simpleList.size();

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
                manga.getSynopsis().length() > 3) {
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

        if (!simpleList.isEmpty()) {
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            Date date = new Date();
            String lastUpdate = dateFormat.format(date);
            if (!mangaDb.getLastUpdate().equals(lastUpdate)) {
                mangaDb.setLastUpdate(lastUpdate);
                changes = true;
            }
        }

        if (changes)
            Database.updateManga(context, mangaDb, false);

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

    public ArrayList<String> getAllMatches(String pattern, String source) throws Exception {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(source);
        ArrayList<String> tmpMatches = new ArrayList<>();
        while (m.find()) {
            tmpMatches.add(m.group(1));
        }
        return tmpMatches;
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

    public FilteredType getFilteredType() {
        return FilteredType.VISUAL;
    }

    public ServerFilter[] getServerFilters(Context context) {
        return new ServerFilter[]{};
    }

    public int[][] getBasicFilter(Context context) {
        ServerFilter[] filters = getServerFilters(context);
        int[][] result = new int[filters.length][];
        for (int i = 0; i < filters.length; i++) {
            if (filters[i].getFilterType() == ServerFilter.FilterType.SINGLE) {
                result[i] = new int[1];
                result[i][0] = 0;
            } else {
                result[i] = new int[0];
            }
        }
        return result;
    }

    public boolean contains(int[] array, int value) {
        for (int i : array) {
            if (i == value) {
                return true;
            }
        }
        return false;
    }

    public enum FilteredType {VISUAL, TEXT}

    class CreateGroupByMangaNotificationsTask extends AsyncTask<Void, Integer, Integer> {
        private ArrayList<Chapter> simpleList = new ArrayList<>();
        private Context context;
        private Manga manga;
        private String largeContentText = "";

        CreateGroupByMangaNotificationsTask(ArrayList<Chapter> simpleList, Manga manga, Context context) {
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
            if (!simpleList.isEmpty() && context != null) {
                int simpleListSize = simpleList.size();
                int x = 10;
                if (simpleListSize <= 10)
                    x = simpleListSize - 1;
                int n = 0;
                for (int i = simpleListSize - 1; i > -1; i--) {
                    if (simpleListSize > 10 && n == x) { // last element if 10+ chapters
                        int tmp = simpleListSize - x;
                        if (tmp == 1)
                            largeContentText = largeContentText + tmp + " " + context.getString(R.string.one_more_chapter_not_displayed_here);
                        else
                            largeContentText = largeContentText + tmp + " " + context.getString(R.string.x_more_chapters_not_displayed_here);
                    } else if (simpleListSize <= 10 && n == x) { // last element if <= 10 chapters
                        largeContentText = largeContentText + simpleList.get(i).getTitle();
                    } else { // every element that isn't the last element
                        if (simpleListSize > 10) { // shorten titles if > 10 chapters
                            String title = simpleList.get(i).getTitle();
                            if (title.length() > 38)
                                title = title.substring(0, Math.min(title.length(), 35)) + "...";
                            largeContentText = largeContentText + title + "\n";
                        } else {
                            largeContentText = largeContentText + simpleList.get(i).getTitle() + "\n";
                        }
                    }
                    n++;
                    // we can only display 11 lines of text
                    if (n == 11)
                        break;
                }

                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("manga_id", simpleList.get(0).getMangaID());
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                if (simpleListSize > 1) {
                    Util.getInstance().createNotification(context, false, (int) System.currentTimeMillis(), intent, simpleListSize + " " + context.getResources().getString(R.string.new_chapters, manga.getTitle()), largeContentText);
                } else {
                    Util.getInstance().createNotification(context, false, (int) System.currentTimeMillis(), intent, simpleListSize + " " + context.getResources().getString(R.string.new_chapter, manga.getTitle()), largeContentText);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
        }
    }

}