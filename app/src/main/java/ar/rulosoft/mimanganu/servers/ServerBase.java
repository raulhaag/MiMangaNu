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

/**
 * The base class for all online Manga servers supported by this application.
 */
public abstract class ServerBase {

    public static final int FROMFOLDER = 1001;
    public static final int RAWSENMANGA = 21;
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
    static final int KUMANGA = 33;
    static final int MANGAPEDIA = 34;
    static final int MANGATOWN = 35;

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

	/**
	 * Construct a new ServerBase object.
	 *
	 * @param context the context for this object
	 */
    public ServerBase(Context context) {
        this.context = context;
    }

	/**
	 * Get a new ServerBase object via the given identifier.
	 * If the passed identifier is not known, a DeadServer instance is returned. This mechanism can
	 * also be used for servers which wen out of commission (like EsMangaHere).
	 *
	 * @param  id      the identifier of the server
	 * @param  context the context for this object
	 * @return         a ServerBase object or a DeadServer object
	 */
    public static ServerBase getServer(int id, Context context) {
        ServerBase serverBase;
        switch (id) {
            case MANGAPANDA:
                serverBase = new MangaPanda(context);
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
            case KUMANGA:
                serverBase = new Kumanga(context);
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
            case MANGAPEDIA:
                serverBase = new Mangapedia(context);
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
            case MANGATOWN:
                serverBase = new MangaTown(context);
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

	/**
	 * Return a clean Navigator instance with old parameters flushed.
	 *
	 * @return the Navigator object
	 */
    public static Navigator getNavigatorAndFlushParameters() {
        Navigator.navigator.flushParameter();//remove old post parameters
        return Navigator.navigator;
    }

	/**
	 * Returns the first regular expression match on a string, or throws an Exception.
	 * If the pattern is found in the source string, the first match group is returned. In case no
	 * match can be done, an Exception is raised with the passed errorMsj string as payload.
	 *
	 * @param patron   the regular expression pattern to match
	 * @param source   the string to check the pattern for
	 * @param errorMsj the descriptive error message string for the Exception raised if not match
	 *                 could be found
	 * @return         the first match group
	 */
    public static String getFirstMatch(String patron, String source, String errorMsj) throws Exception {
        Pattern p = Pattern.compile(patron);
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group(1);
        }
        throw new Exception(errorMsj);
    }

	/**
	 * Returns a list of registered servers.
	 * Returns an array of all possible server objects (except DeadServer of course) with a given
	 * context.
	 *
	 * @param context the context for this object
	 * @return        an array containing ServerBase instances - for each registered server
	 */
    public static ServerBase[] getServers(Context context) {
        return (new ServerBase[]{
                new TuMangaOnline(context),
                new BatoToEs(context),
                new HeavenManga(context),
                new SubManga(context),
                new EsNineManga(context),
                new LeoManga(context),
                new Kumanga(context),
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
                new Mangapedia(context),
                new MangaKawaii(context),
                new ItNineManga(context),
                new MangaEdenIt(context),
                new DeNineManga(context),
                new RawSenManga(context),
                new BatoTo(context),
                new ReadComicOnline(context),
                new ViewComic(context),
                new MangaTown(context),
                new FromFolder(context)
        });
    }

    /**
	 * Returns the list of Manga found on the server.
	 *
	 * @return           an ArrayList of Manga objects
	 * @throws Exception if an error occurred
	 */
    public abstract ArrayList<Manga> getMangas() throws Exception;
	/**
	 * Returns a list of Manga filtered by a given search term.
	 *
	 * @param term       a term to search for
	 * @return           an ArrayList of Manga objects
	 * @throws Exception if an error occurred
	 */
    public abstract ArrayList<Manga> search(String term) throws Exception;

    /**
	 * Load all available chapters for a given Manga.
	 *
	 * @param manga       the Manga to find chapters for
	 * @param forceReload force new retrieval of chapter information
	 * @throws Exception  if an error occurred
	 * @see               Chapter
	 */
    public abstract void loadChapters(Manga manga, boolean forceReload) throws Exception;
	/**
	 * Load available information for a given Manga.
	 * Load and add information to a given Manga, like:
	 * <ul>
	 * <li>cover image
	 * <li>summary
	 * <li>ongoing/finished
	 * <li>genre
	 * <li>chapters
	 * </ul>
	 *
	 * @param manga       the Manga to find information for
	 * @param forceReload force new retrieval of Manga information
	 * @throws Exception  if an error occurred
	 * @see               Manga
	 * @see               Chapter
	 */
    public abstract void loadMangaInformation(Manga manga, boolean forceReload) throws Exception;

    /**
	 * Returns the URL for the given page in a Chapter.
	 * Some sanitiy checking should be done in the override function, like non-negativity and that
	 * page lies within the available page numbers of the given Chapter.
	 *
	 * @param chapter a Chapter object to get the page URL for
	 * @param page    the page number
	 * @return        the URL to the given page of the Chapter
	 */
    public abstract String getPagesNumber(Chapter chapter, int page);

	/**
	 * Returns the URL for the image on a given Chapter page.
	 * Some sanitiy checking should be done in the override function, like non-negativity and that
	 * page lies within the available page numbers of the given Chapter.
	 *
	 * @param chapter    a Chapter object to get the page image URL for
	 * @param page       the page number
	 * @return           the URL to the image on the given page of the Chapter
	 * @throws Exception if an error occurred
	 */
    public abstract String getImageFrom(Chapter chapter, int page) throws Exception;

	/**
	 * Initialise the Chapter information, basically the number of pages.
	 *
	 * @param chapter    the Chapter object to do the initialisation for
	 * @throws Exception if an error occurred
	 */
    public abstract void chapterInit(Chapter chapter) throws Exception;

    /**
	 * Returns a list of Manga filtered by the given filter set.
	 * There might be more than one result page, so pageNumber is used to get a certain result page.
	 * If more information is available, the hasMore variable shall be set to <code>true</code> to
	 * indicate this condition to the caller in order to fetch the next page.
	 *
	 * @param filters    the filter set to use
	 * @param pageNumber the result page number for a given filter
	 * @return           a list of Manga matching the filter criteria
	 * @throws Exception if an error occurred
	 */
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        return new ArrayList<>();
    }

	/**
	 * Returns information if the server provides a Manga listing.
     * If <code>true</code> is returned, getMangas() must be implemented properly.
	 *
	 * @return <code>true</code> if the server provides a list of Manga, <code>false</code>
	 *         otherwise
	 */
    public abstract boolean hasList();

	/**
	 * Searches for new chapters for a given Manga.
	 * Loads information from the database and fetches the current state from the server. Afterwards
	 * the server information is compared to the local information to check if new chapters are
	 * available.
	 *
	 * A fast check can be triggered to reduce load and compare time by checking only the last 20
	 * chapters for differences.
	 *
	 * All detected changes are also stored in the database.
	 *
	 * @param id         the Manga id to check new chapters for
	 * @param context    the Context object to use for checking
	 * @param fast       <code>true</code> to perform a fast check (first 20 chapters only)
	 * @return           the count of new chapters found
	 * @throws Exception if an error occurred
	 */
    public int searchForNewChapters(int id, Context context, boolean fast) throws Exception {
        int returnValue;
        Manga mangaDb = Database.getFullManga(context, id);
        Manga manga = new Manga(mangaDb.getServerId(), mangaDb.getTitle(), mangaDb.getPath(), false);
        manga.setId(mangaDb.getId());
        try {
            this.loadMangaInformation(manga, true);
            this.loadChapters(manga, false);
        } catch (Exception e) {
            //to many messages are annoying
            //Util.getInstance().toast(context, context.getResources().getString(R.string.update_search_failed, mangaDb.getTitle(), getServerName()));
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

	/**
	 * Returns the server icon resource identifier.
	 *
	 * @return the server icon resource identifier
	 */
    public int getIcon() {
        return icon;
    }
	/**
	 * Sets the server icon resource identifier.
	 *
	 * @param icon the server icon resource identifier to set
	 */
    public void setIcon(int icon) {
        this.icon = icon;
    }

	/**
	 * Returns the flag icon resource identifier.
	 *
	 * @return the flag icon resource identifier
	 */
    public int getFlag() {
        return flag;
    }
	/**
	 * Sets the flag icon resource identifier.
	 *
	 * @param flag the flag icon resource identifier to set
	 */
    public void setFlag(int flag) {
        this.flag = flag;
    }

	/**
	 * Returns the server resource identifier.
	 *
	 * @return the server resource identifier
	 */
    public int getServerID() {
        return serverID;
    }
	/**
	 * Sets the server identifier.
	 *
	 * @param serverID the server identifier to set
	 */
    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

	/**
	 * Returns the server name.
	 *
	 * @return the server name
	 */
    public String getServerName() {
        return serverName;
    }
	/**
	 * Sets the server name.
	 *
	 * @param serverName the server name to set
	 */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

	/**
	 * Returns a list of matches for a given pattern and string.
	 *
	 * @param patron     the pattern to search for
	 * @param source     the string to search
	 * @return           a list of matches (may be empty)
	 * @throws Exception if an error occurred
	 */
    public ArrayList<String> getAllMatch(String patron, String source) throws Exception {
        Pattern p = Pattern.compile(patron);
        Matcher m = p.matcher(source);
        ArrayList<String> matches = new ArrayList<>();
        while (m.find()) {
            matches.add(m.group(1));
        }
        return matches;
    }

	/**
	 * Returns the first match for a given pattern and string or a default text.
	 *
	 * @param patron     the pattern to search for
	 * @param source     the string to search
	 * @param mDefault   the default string to return in case no match was found
	 * @return           the first match or the value defined by mDefault
	 * @throws Exception if an error occurred
	 */
    public String getFirstMatchDefault(String patron, String source, String mDefault) {
        Pattern p = Pattern.compile(patron);
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group(1);
        } else {
            return mDefault;
        }
    }

	/**
	 * Returns information if a referrer is needed for image loading.
	 *
	 * @return <code>true</code> if a referrer is needed
	 */
    public boolean needRefererForImages() {
        return true;
    }

	/**
	 * Returns information if the server offers filtered navigation.
	 * If <code>true</code> is returned, getMangasFiltered() must be implemented properly.
     *
	 * @return <code>true</code> if filtered navigation is offered
	 */
    public boolean hasFilteredNavigation() {
        return true;
    }

	/**
	 * Returns the type of filtering supported.
	 *
	 * @return either VISUAL or TEXT
	 */
    public FilteredType getFilteredType() {
        return FilteredType.VISUAL;
    }

	/**
	 * Returns the supported server filters for this server.
	 *
	 * @return a list of ServerFilter supported by this server
	 */
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{};
    }

	/**
	 * Returns the most basic filter set for this server.
     *
     * @return the basic filter for this server
	 */
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

	/**
	 * Returns information the server needs a login.
	 *
	 * @return <code>true</code> if a login is needed
	 */
    public boolean needLogin() {
        return false;
    }

	/**
	 * Returns information if credentials are present.
	 * Should return <code>true</code> to disable querying credentials.
	 *
	 * @return <code>true</code> if credentials are present
	 */
    public boolean hasCredentials() {
        return true;
    }

	/**
	 * Tests if the given login data is working.
	 *
	 * @param user the user to log in
	 * @param passwd the password to use for logging in
	 * @return <code>true</code> if the login succeeded, <code>false</code> otherwise
	 * @throws Exception if an error occurred
	 */
    public boolean testLogin(String user, String passwd) throws Exception {
        return false;
    }

	/**
	 * An enumeration for the type of filtering supported.
	 */
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
                        largeContentText += context.getResources().getQuantityString(R.plurals.more_chapters_not_displayed_here, tmp, tmp);
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

                Util.getInstance().createNotification(context, false, (int) System.currentTimeMillis(), intent, context.getResources().getQuantityString(R.plurals.new_chapter, simpleListSize, simpleListSize, manga.getTitle()), largeContentText);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
        }
    }
}
