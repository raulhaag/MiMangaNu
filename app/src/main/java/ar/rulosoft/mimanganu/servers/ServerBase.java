package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
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
    public static final int ESMANGAHERE = 3;
    public static final int RAWSENMANGA = 21;
    static final int MANGAPANDA = 1;
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
    static final int TUMANGAONLINE = 22;
    static final int NINEMANGA = 23;
    static final int MANGAEDEN = 24;
    static final int LEOMANGA = 25;
    static final int BATOTO = 26;
    static final int BATOTOES = 27;
    static final int JAPSCAN = 28;
    static final int READMANGATODAY = 29;
    static final int TAADD = 30;
    static final int MANGASTREAM = 31;
    static final int MANGAKAWAII = 32;


    static final int READCOMICONLINE = 1000;
    static final int READCOMICSTV = 1002;
    static final int GOGOCOMIC = 1003;
    static final int VIEWCOMIC = 1004;
    public boolean hasMore = true;
    protected String defaultSynopsis = "N/A";
    Context context;
    private String serverName;
    private int icon;
    private int flag;
    private int serverID;

    public ServerBase(Context context) {
        this.context = context;
    }

    public static ServerBase getServer(int id, Context context) {
        //before remove deprecated add info on DeadServer
        ServerBase serverBase;
        switch (id) {
            case MANGAPANDA:
                serverBase = new MangaPanda(context);
                break;
            case ESMANGAHERE:
                serverBase = new EsMangaHere(context);
                break;
            case MANGAHERE:
                serverBase = new MangaHere(context);
                break;
            case MANGAFOX:
                serverBase = new MangaFox(context);
                break;
            case SUBMANGA:
                serverBase = new SubManga(context);
                break;
            case HEAVENMANGACOM:
                serverBase = new HeavenManga(context);
                break;
            case MANGAREADER:
                serverBase = new MangaReader(context);
                break;
            case ESNINEMANGA:
                serverBase = new EsNineManga(context);
                break;
            case KISSMANGA:
                serverBase = new KissManga(context);
                break;
            case ITNINEMANGA:
                serverBase = new ItNineManga(context);
                break;
            case TUMANGAONLINE:
                serverBase = new TuMangaOnline(context);
                break;
            case DENINEMANGA:
                serverBase = new DeNineManga(context);
                break;
            case RUNINEMANGA:
                serverBase = new RuNineManga(context);
                break;
            case MANGAEDENIT:
                serverBase = new MangaEdenIt(context);
                break;
            case MYMANGAIO:
                serverBase = new MyMangaIo(context);
                break;
            case RAWSENMANGA:
                serverBase = new RawSenManga(context);
                break;
            case MANGAEDEN:
                serverBase = new MangaEden(context);
                break;
            case NINEMANGA:
                serverBase = new NineManga(context);
                break;
            case TAADD:
                serverBase = new Taadd(context);
                break;
            case MANGASTREAM:
                serverBase = new MangaStream(context);
                break;
            case READCOMICONLINE:
                serverBase = new ReadComicOnline(context);
                break;
            case LEOMANGA:
                serverBase = new LeoManga(context);
                break;
            case BATOTO:
                serverBase = new BatoTo(context);
                break;
            case BATOTOES:
                serverBase = new BatoToEs(context);
                break;
            case JAPSCAN:
                serverBase = new JapScan(context);
                break;
            case MANGAKAWAII:
                serverBase = new MangaKawaii(context);
                break;
            case READMANGATODAY:
                serverBase = new ReadMangaToday(context);
                break;
            case VIEWCOMIC:
                serverBase = new ViewComic(context);
                break;
            case FROMFOLDER:
                serverBase = new FromFolder(context);
                break;
            default:
                serverBase = new DeadServer(context);
                break;
        }
        return serverBase;
    }

    public static Navigator getNavigatorAndFlushParameters() {
        Navigator.navigator.flushParameter();//remove old post parameters
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

    public static ServerBase[] getServers(Context context) {
        return (new ServerBase[]{
                new TuMangaOnline(context),
                new BatoToEs(context),
                new HeavenManga(context),
                new SubManga(context),
                new EsNineManga(context),
                new EsMangaHere(context),
                new LeoManga(context),
                new MangaPanda(context),
                new MangaReader(context),
                new MangaHere(context),
                new MangaFox(context),
                new KissManga(context),
                new MangaEden(context),
                new MangaStream(context),
                new Taadd(context),
                new NineManga(context),
                new ReadMangaToday(context),
                new RuNineManga(context),
                new MyMangaIo(context),
                new JapScan(context),
                new MangaKawaii(context),
                new ItNineManga(context),
                new MangaEdenIt(context),
                new DeNineManga(context),
                new RawSenManga(context),
                new BatoTo(context),
                new ReadComicOnline(context),
                new ViewComic(context),
                new FromFolder(context)
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
        try {
            this.loadMangaInformation(manga, true);
            this.loadChapters(manga, false);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        if (fast && manga.getChapters().size() > 21) {
            manga.getChapters().subList(0, manga.getChapters().size() - 20).clear();
        }
        manga.getChapters().removeAll(mangaDb.getChapters());
        ArrayList<Chapter> simpleList = manga.getChapters();
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
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
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

    public ArrayList<String> getAllMatch(String patron, String source) throws Exception {
        Pattern p = Pattern.compile(patron);
        Matcher m = p.matcher(source);
        ArrayList<String> matches = new ArrayList<>();
        while (m.find()) {
            matches.add(m.group(1));
        }
        return matches;
    }

    public String getFirstMatchDefault(String patron, String source, String mDefault) {
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

    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{};
    }

    public int[][] getBasicFilter() {
        ServerFilter[] filters = getServerFilters();
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

    public boolean needLogin() {
        return false;
    }

    public boolean hasCredentials() {
        return true;
    }

    public boolean testLogin(String user, String passwd) throws Exception {
        return false;
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