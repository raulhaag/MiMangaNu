package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;

/**
 * Created by Raul on 03/02/2017.
 */

class BatoToEs extends BatoTo {
    BatoToEs(Context context) {
        super(context);
        setFlag(R.drawable.flag_es);
        setIcon(R.drawable.batoto);
        setServerName("BatoTo(ES)");
        setServerID(BATOTOES);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String user = prefs.getString("username_" + getServerName(), "");
            String password = prefs.getString("dwp_" + getServerName(), "");
            String data = getNavigatorAndFlushParameters().get(manga.getPath(), new BatotoLoginInterceptor(user, password));
            // Summary
            manga.setSynopsis(getFirstMatchDefault("Description:</td>\\s+<td>(.*?)</td>", data, context.getString(R.string.nodisponible)));
            // Cover
            manga.setImages(getFirstMatchDefault("(http://img\\.bato\\.to/forums/uploads.+?)\"", data, ""));
            // Author
            manga.setAuthor(getFirstMatchDefault("search\\?artist_name=.+?>([^<]+)", data, context.getString(R.string.nodisponible)));
            // Genre
            manga.setGenre(getFirstMatchDefault("Genres:</td>\\s+<td>([\\s\\S]+?)<img[^>]+?alt=.edit", data, context.getString(R.string.nodisponible)));
            // Status
            manga.setFinished(!getFirstMatchDefault("Status:<\\/td>\\s+<td>([^<]+)", data, "").contains("Ongoing"));
            // Chapters
            Pattern pattern = Pattern.compile("<a href=\"([^\"]+)\" title=\"[^\"]+\">.+?>([^<]+).+?title=\"(.+?)\".+?<a[^>]+>([^<]+)", Pattern.DOTALL);
            data = getFirstMatchDefault("ipb_table chapters_list\"([\\s\\S]+?)</table", data, "");
            Matcher matcher = pattern.matcher(data);
            String lang = "Spanish";
            while (matcher.find()) {
                if (matcher.group(3).contains(lang)) {
                    manga.addChapterFirst(new Chapter("(" + matcher.group(3) + ") " + matcher.group(2) + " [" + matcher.group(4) + "]", matcher.group(1)));
                }
            }
        }
    }
}
