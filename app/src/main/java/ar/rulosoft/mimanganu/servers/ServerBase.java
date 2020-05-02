package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
 * <p>
 * Any Manga service shall be based off this class. Please follow this guideline when providing a new
 * server:
 * <p>
 * <ul>
 * <li>Subclass the new server from <code>ServerBase</code> and implement it
 * <li>Add a new unique identifier to <code>ServerBase</code>
 * <li>Add the new server to <code>getServer</code>
 * <li>Add the new server to <code>getServers</code>
 * </ul>
 * <p>
 * Hints:
 * <ul>
 * <li>optimize your regular expressions to avoid backtracking
 * <li>use the provided functionality from <code>ServerBase</code> as often as possible
 * <li>use <code>URLEncoder.encode</code>before passing search terms to the server
 * </ul>
 * <p>
 * Test the server implementation for the following use cases:
 * <p>
 * <ul>
 * <li>TC: Open the server list by clicking the '+' inside the circle.
 * <li>EX: The server is listed by its name and the correct icon and language flag are shown.
 * </ul>
 * <p>
 * <ul>
 * <li>TC: Select the server from the list.
 * <li>EX: The Manga grid is populated (showing covers and names). If the server supports filtering,
 * the filter button is visible, if no filtering is possible, the button is hidden.
 * </ul>
 * <p>
 * <ul>
 * <li>TC: Scroll the list.
 * <li>EX: New entries on the grid become visible. Verify that items are continuously fetched as you
 * scroll.
 * </ul>
 * <p>
 * <ul>
 * <li>TC: Click the magnifying glass icon to search for a Manga. Search for 'world'.
 * <li>EX: A non-empty list of search results is presented, all containing the search term. Special
 * characters are displayed properly (e.g. apostrophes).
 * </ul>
 * <p>
 * <ul>
 * <li>TC: Select a Manga from the list of search results.
 * <li>EX: All supported fields contain content as expected (cover, author, genre, status, number of
 * chapters, summary).
 * </ul>
 * <p>
 * Click the back button twice to return to the list.
 * <p>
 * <ul>
 * <li>TC: If the server supports listing, click the list icon.
 * <li>EX: A non-empty list of manga is presented.
 * </ul>
 * <p>
 * <ul>
 * <li>TC: Select a Manga from the list.
 * <li>EX: All supported fields contain content as expected (cover, author, genre, status, number of
 * chapters, summary).
 * </ul>
 * <p>
 * Click the back button to return to the list.
 * <p>
 * <ul>
 * <li>TC: Click the magnifying glass icon to search for a Manga. Search for 'world'.
 * <li>EX: A non-empty list of search results is presented, all containing the search term. Special
 * characters are displayed properly (e.g. apostrophes).
 * </ul>
 * <p>
 * <ul>
 * <li>TC: Select a Manga from the list of search results.
 * <li>EX: All supported fields contain content as expected (cover, author, genre, status, number of
 * chapters, summary).
 * </ul>
 * <p>
 * Click the back button to return to the filtered list.
 * <p>
 * <ul>
 * <li>TC: Open the filter view and change the default selection.
 * <li>EX: The filtered view displays Manga matching the filter criteria.
 * </ul>
 * <p>
 * <ul>
 * <li>TC: Select a Manga from the list of search results.
 * <li>EX: All supported fields contain content as expected (cover, author, genre, status, number of
 * chapters, summary).
 * </ul>
 * <p>
 * <ul>
 * <li>TC: Add the Manga to the database.
 * <li>EX: No error message is displayed.
 * </ul>
 * <p>
 * <ul>
 * <li>TC: Return to the home screen and open the Manga which was just added.
 * <li>EX: All supported fields contain content as expected (cover, author, genre, summary). The
 * list of chapters is non-empty.
 * </ul>
 * <p>
 * <ul>
 * <li>TC: Pull the chapter list down to initiate a Manga update.
 * <li>EX: All supported fields contain content as expected (cover, author, genre, summary). The
 * list of chapters is non-empty.
 * </ul>
 * <p>
 * <ul>
 * <li>TC: Open a chapter.
 * <li>EX: The first page is loaded and displayed.
 * </ul>
 * <p>
 * <ul>
 * <li>TC: Scroll through the chapter until the end and scroll further to load the next chapter.
 * <li>EX: The first page of the next chapter is loaded and displayed. The order is as expected.
 * </ul>
 * <p>
 * <ul>
 * <li>TC: Return to the home screen and remove the Manga.
 * <li>EX: The Manga is no longer visible on the home screen.
 * </ul>
 * <p>
 * If all of steps work properly - congratulations, you made it. (o.O)/v
 */
public abstract class ServerBase {
    public final int VERSION = 1;

    public static final int FROMFOLDER = 1001;
    public static final int RAWSENMANGA = 21;
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
    public static final int TUMANGAONLINE = 22;
    public static final int NINEMANGA = 23;
    public static final int MANGAEDEN = 24;
    public static final int LEOMANGA = 25;
    public static final int BATOTO = 26;
    public static final int BATOTOES = 27;
    public static final int JAPSCAN = 28;
    public static final int READMANGATODAY = 29;
    public static final int TAADD = 30;
    public static final int MANGASTREAM = 31;
    public static final int MANGAKAWAII = 32;
    public static final int KUMANGA = 33;
    public static final int MANGAPEDIA = 34;
    public static final int MANGATOWN = 35;
    public static final int READMANGAME = 36;
    public static final int DESUME = 37;
    public static final int MANGARAWONLINE = 38;
    public static final int MINTMANGA = 39;
    public static final int MANGAAE = 40;
    public static final int NEUMANGATV = 41;
    public static final int MANGASHIRONET = 42;
    public static final int FANFOXNET = 43;
    public static final int INMNAGA = 44;
	public static final int MANGANELO = 45;
    public static final int FRNINEMANGA = 46;
    public static final int READCOMICONLINE = 1000;
    public static final int READCOMICSTV = 1002;
    public static final int GOGOCOMIC = 1003;
    public static final int VIEWCOMIC = 1004;
    public static final int VERCOMICSCOM = 1005;

    Context context;
    private String serverName;
    private int icon;
    private int flag;
    private int serverID;
    private static HashMap<String, ServerBase> serversInstances = new HashMap<>();


    /**
     * Construct a new ServerBase object.
     *
     * @param context the context for this object
     */
    public ServerBase(Context context) {
        this.context = context;
    }

    public static ServerBase getServer(int id, Context context) {
        if (!serversInstances.containsKey("" + id)) {
            serversInstances.put("" + id, getNewServer(id, context));
        }
        return serversInstances.get("" + id);
    }

    /**
     * Get a new ServerBase object via the given identifier.
     * If the passed identifier is not known, a DeadServer instance is returned. This mechanism can
     * also be used for servers which went out of commission (like EsMangaHere).
     *
     * @param id      the identifier of the server
     * @param context the context for this object
     * @return a <code>ServerBase</code> or a <code>DeadServer</code> object
     */
    private static ServerBase getNewServer(int id, Context context) {
        ServerBase serverBase;
        switch (id) {
            case MANGAPANDA:
                serverBase = new MangaPanda(context);
                break;
            case MANGAHERE:
                serverBase = new MangaHere(context);
                break;
            case FANFOXNET:
                serverBase = new FanFoxNet(context);
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
            case MANGATOWN:
                serverBase = new MangaTown(context);
                break;
            case READMANGAME:
                serverBase = new ReadMangaMe(context);
                break;
            case DESUME:
                serverBase = new DesuMe(context);
                break;
            case MINTMANGA:
                serverBase = new MintManga(context);
                break;
            case FROMFOLDER:
                serverBase = new FromFolder(context);
                break;
            case MANGAAE:
                serverBase = new MangaAE(context);
                break;
            case NEUMANGATV:
                serverBase = new NeuMangaTv(context);
                break;
            case INMNAGA:
                serverBase = new InManga(context);
                break;
            case MANGANELO:
                serverBase = new MangaNelo(context);
                break;
            case FRNINEMANGA:
                serverBase = new FrNineManga(context);
                break;
            case VERCOMICSCOM:
                serverBase = new VerComicsCom(context);
                break;
            case MANGASHIRONET:
                serverBase = new MangaShiroNet(context);
                break;
            default:
                serverBase = new DeadServer(context);
                break;
        }
        return serverBase;
    }

    /**
     * Return a clean Navigator instance with old POST parameters flushed.
     *
     * @return the Navigator object
     */
    public static Navigator getNavigatorAndFlushParameters() {
        Navigator.getInstance().flushParameter();
        return Navigator.getInstance();
    }

    /**
     * Returns the first regular expression match on a string, or throws an Exception.
     * If the pattern is found in the source string, the first match group is returned. In case no
     * match can be done, an Exception is raised with the passed errorMsg string as payload.
     *
     * @param patron   the regular expression pattern to match
     * @param source   the string to check the pattern for
     * @param errorMsg the descriptive error message string for the Exception raised if not match
     *                 could be found
     * @return the first match group
     */
    public static String getFirstMatch(String patron, String source, String errorMsg) throws Exception {
        Pattern p = Pattern.compile(patron, Pattern.DOTALL);
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group(1);
        } else {
            throw new Exception(errorMsg);
        }
    }

    /**
     * Returns a list of registered servers.
     * Returns an array of all possible server objects (except DeadServer of course) with a given
     * context.
     *
     * @param context the context for this object
     * @return an array containing ServerBase instances - for each registered server
     */
    public static ServerBase[] getServers(Context context) {
        return (new ServerBase[]{
                new TuMangaOnline(context),
                new HeavenManga(context),
                new EsNineManga(context),
                new Kumanga(context),
                new InManga(context),
                new MangaPanda(context),
                new MangaReader(context),
                new MangaHere(context),
                new FanFoxNet(context),
                new KissManga(context),
                new MangaEden(context),
                new MangaStream(context),
                new Taadd(context),
                new NineManga(context),
                new ReadMangaToday(context),
                new MangaTown(context),
				new MangaNelo(context),
                new RuNineManga(context),
                new DesuMe(context),
                new MintManga(context),
                new ReadMangaMe(context),
                new JapScan(context),
                new MangaKawaii(context),
                new FrNineManga(context),
                new ItNineManga(context),
                new MangaEdenIt(context),
                new DeNineManga(context),
                new RawSenManga(context),
                new MangaAE(context),
                new NeuMangaTv(context),
                new MangaShiroNet(context),
                new VerComicsCom(context),
                new ReadComicOnline(context),
                new ViewComic(context),
                new FromFolder(context),
        });
    }

    /**
     * Returns the list of Manga found on the server.
     * <p>
     * This function shall only be called if <code>hasList</code> returns <code>true</code>.
     *
     * @return an ArrayList of Manga objects or <code>null</code> if unsupported.
     * @throws Exception if an error occurred. The exception is thrown back to UI, so its message
     *                   should be a translatable string.
     */
    @Nullable
    public ArrayList<Manga> getMangas() throws Exception { //make default null just a couple of server need to implement it
        return null;
    }

    /**
     * Returns a list of Manga filtered by a given search term.
     * <p>
     * This function shall only be called if <code>hasSearch</code> returns <code>true</code>.
     *
     * @param term a term to search for
     * @return an ArrayList of Manga objects or <code>null</code> if unsupported.
     * @throws Exception if an error occurred. The exception is thrown back to UI, so its message
     *                   should be a translatable string.
     */
    public abstract ArrayList<Manga> search(String term) throws Exception;

    /**
     * Load all available chapters for a given Manga.
     *
     * @param manga       the Manga to find chapters for
     * @param forceReload force new retrieval of chapter information
     * @throws Exception if an error occurred. The exception is thrown back to UI, so its message
     *                   should be a translatable string.
     * @see Chapter
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
     * @throws Exception if an error occurred. The exception is thrown back to UI, so its message
     *                   should be a translatable string.
     * @see Manga
     * @see Chapter
     */
    public abstract void loadMangaInformation(Manga manga, boolean forceReload) throws Exception;

    /**
     * Returns the URL for the image on a given Chapter page.
     * It must be ensured, that the <code>Chapter</code> object has been initialised - this
     * means, that at least the number of pages is set properly. This is ensured by calling
     * <code>chapterInit</code> before loading any images from a given chapter.
     * <p>
     * If the <code>page</code> index lies outside the valid range, an exception is raised.
     * Therefore the calling function must ensure, that the requested page is within the interval
     * [1 .. <code>chapter.getPages()</code>] (boundaries included).
     * <p>
     * Be aware, that the page numbers start at 1 - not at 0. This is mostly relevant if the page
     * number is used as an array index.
     *
     * @param chapter a Chapter object to get the page image URL for
     * @param page    the page number starting at page 1 (NOT zero based)
     * @return the URL to the image on the given page of the Chapter
     * @throws Exception if an error occurred. The exception is thrown back to UI, so its message
     *                   should be a translatable string.
     */
    public abstract String getImageFrom(Chapter chapter, int page) throws Exception;

    /**
     * Initialise the <code>Chapter</code> information, like the number of pages.
     * <p>
     * For servers listing all images on one page, the image information can be extracted in this
     * function and stored in the <code>Chapter</code> object via its <code>setExtra</code>
     * function.
     * <p>
     * Also it has to be ensured, that <code>chapterInit</code> is called before
     * <code>getImageFrom</code> in order to have one time information already fetched and to avoid
     * double work. The latter has to be ensured in this function by checking if the number of
     * pages stored in the <code>Chapter</code> object is bigger than zero.
     *
     * @param chapter the <code>Chapter</code> object to do the initialisation for
     * @throws Exception if an error occurred. The exception is thrown back to UI, so its message
     *                   should be a translatable string.
     */
    public abstract void chapterInit(Chapter chapter) throws Exception;

    /**
     * Returns a list of Manga filtered by the given filter set.
     * There might be more than one result page, so pageNumber is used to get a certain result page.
     * <p>
     * The filter parameter contains the current selection. The first index is given by the order
     * of filters returned by <code>getServerFilters()</code>. The second index indicates the
     * current selection for the criteria given in the first index.
     * <p>
     * So if the ordering of the filter criteria is changed, make sure to reflect the change in this
     * function as well.
     *
     * @param filters    the filter set to use
     * @param pageNumber the result page number for a given filter
     * @return a list of Manga matching the filter criteria
     * @throws Exception if an error occurred. The exception is thrown back to UI, so its message
     *                   should be a translatable string.
     */
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        return new ArrayList<>();
    }

    /**
     * Returns information if the server provides a Manga listing.
     * If <code>true</code> is returned, getMangas() must be implemented properly.
     *
     * @return <code>true</code> if the server provides a list of Manga, <code>false</code>
     * otherwise
     */
    public abstract boolean hasList();

    /**
     * Searches for new chapters for a given Manga.
     * Loads information from the database and fetches the current state from the server. Afterwards
     * the server information is compared to the local information to check if new chapters are
     * available.
     * <p>
     * A fast check can be triggered to reduce load and compare time by checking only the last 20
     * chapters for differences.
     * <p>
     * All detected changes are also stored in the database.
     *
     * @param id      the Manga id to check new chapters for
     * @param context the Context object to use for checking
     * @param fast    <code>true</code> to perform a fast check (first 20 chapters only)
     * @return the count of new chapters found
     * @throws Exception if an error occurred. The exception is thrown back to UI, so its message
     *                   should be a translatable string.
     */
    public int searchForNewChapters(int id, Context context, boolean fast) {
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
                manga.getAuthor() != null && manga.getAuthor().length() > 2) {
            mangaDb.setAuthor(manga.getAuthor());
            changes = true;
        }

        if ((mangaDb.getImages() != null && !mangaDb.getImages().equals(manga.getImages()) &&
                manga.getImages() != null && manga.getImages().length() > 2) ||
                mangaDb.getImages() == null && manga.getImages() != null) {
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
            if (mangaDb.getLastUpdate() == null) {
                mangaDb.setLastUpdate(lastUpdate);
                changes = true;
            } else if (!mangaDb.getLastUpdate().equals(lastUpdate)) {
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
     * Returns the server path name.
     *
     * @return the server path name
     */
    public String getPath() {
        return serverName;
    }

    /**
     * Returns a list of matches for a given pattern and string.
     *
     * @param patron the pattern to search for
     * @param source the string to search
     * @return a list of matches (may be empty)
     */
    ArrayList<String> getAllMatch(String patron, String source) {
        Pattern p = Pattern.compile(patron, Pattern.DOTALL);
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
     * @param patron   the pattern to search for
     * @param source   the string to search
     * @param mDefault the default string to return in case no match was found
     * @return the first match or the value defined by mDefault
     */
    public String getFirstMatchDefault(String patron, String source, String mDefault) {
        Pattern p = Pattern.compile(patron, Pattern.DOTALL);
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group(1);
        } else {
            return mDefault;
        }
    }

    /**
     * Returns the last match for a given pattern and string or a default text.
     *
     * @param patron   the pattern to search for
     * @param source   the string to search
     * @param mDefault the default string to return in case no match was found
     * @return the first match or the value defined by mDefault
     */

    public String getLastMatchDefault(String patron, String source, String mDefault) {
        Pattern p = Pattern.compile(patron, Pattern.DOTALL);
        Matcher m = p.matcher(source);
        String result = mDefault;
        while (m.find()) {
            result = m.group(1);
        }
        return result;
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
     * If <code>true</code> is returned, <code>getMangasFiltered</code> must be implemented properly.
     *
     * @return <code>true</code> if filtered navigation is offered
     */
    public boolean hasFilteredNavigation() {
        return true;
    }

    /**
     * Returns information if the server offers a search functionality.
     * If <code>true</code> is returned, <code>search</code> must be implemented properly.
     *
     * @return <code>true</code> if search feature is offered in the filtered view
     */
    public boolean hasSearch() {
        return true;
    }

    /**
     * Returns the type of filtered list display supported.
     * <p>
     * Determines the type of filtered list displayed. Either Manga can be presented as a nice grid
     * with covers (<code>FilteredType.VISUAL</code>) or only as a plain textual list
     * (<code>FilteredType.TEXT</code>).
     *
     * @return either <code>FilteredType.VISUAL</code> or <code>FilteredType.TEXT</code>
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
     * @param user   the user to log in
     * @param passwd the password to use for logging in
     * @return <code>true</code> if the login succeeded, <code>false</code> otherwise
     */
    @SuppressWarnings("unused")
    public boolean testLogin(String user, String passwd) throws Exception {
        return false;
    }

    /**
     * Build an array of translated strings from an array of resource identifiers.
     *
     * @param resId an array containing the resource identifiers
     * @return an array of translated strings
     */
    String[] buildTranslatedStringArray(int[] resId) {
        String[] result = new String[resId.length];
        for (int i = 0; i < resId.length; i++) {
            result[i] = context.getString(resId[i]);
        }
        return result;
    }

    /**
     * An enumeration for the type of filtering supported.
     */
    public enum FilteredType {
        VISUAL, TEXT
    }

    private static class CreateGroupByMangaNotificationsTask extends AsyncTask<Void, Integer, Integer> {
        private ArrayList<Chapter> simpleList = new ArrayList<>();
        private final WeakReference<Context> weakReferenceContext;
        private Manga manga;
        private String largeContentText = "";

        CreateGroupByMangaNotificationsTask(ArrayList<Chapter> simpleList, Manga manga, Context context) {
            this.simpleList.addAll(simpleList);
            this.weakReferenceContext = new WeakReference<>(context);
            this.manga = manga;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if (!simpleList.isEmpty() && weakReferenceContext.get() != null && !isCancelled()) {
                int simpleListSize = simpleList.size();
                int x = 10;
                if (simpleListSize <= 10)
                    x = simpleListSize - 1;
                int n = 0;
                for (int i = simpleListSize - 1; i > -1; i--) {
                    StringBuilder stringBuilder = new StringBuilder();
                    if (simpleListSize > 10 && n == x) { // last element if 10+ chapters
                        int tmp = simpleListSize - x;
                        Context context = weakReferenceContext.get();
                        if (context != null)
                            stringBuilder.append(context.getResources().getQuantityString(R.plurals.more_chapters_not_displayed_here, tmp, tmp));
                        else
                            this.cancel(true);
                    } else if (simpleListSize <= 10 && n == x) { // last element if <= 10 chapters
                        stringBuilder.append(simpleList.get(i).getTitle());
                    } else { // every element that isn't the last element
                        if (simpleListSize > 10) { // shorten titles if > 10 chapters
                            String title = simpleList.get(i).getTitle();
                            if (title.length() > 38)
                                title = title.substring(0, Math.min(title.length(), 35)) + "...";
                            stringBuilder.append(title).append("\n");
                        } else {
                            stringBuilder.append(simpleList.get(i).getTitle()).append("\n");
                        }
                    }
                    largeContentText = stringBuilder.toString();
                    n++;
                    // we can only display 11 lines of text
                    if (n == 11)
                        break;
                }

                Context context = weakReferenceContext.get();
                if (context != null) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("manga_id", simpleList.get(0).getMangaID());
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    Util.getInstance().createNotification(context, false, (int) System.currentTimeMillis(), intent, context.getResources().getQuantityString(R.plurals.new_chapter, simpleListSize, simpleListSize, manga.getTitle()), largeContentText);
                } else
                    this.cancel(true);
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
        }
    }

    public int getServerVersion() {
        return VERSION;
    }

    public boolean updateServerVersion() {
        return false;
    }
}