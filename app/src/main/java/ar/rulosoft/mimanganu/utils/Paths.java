package ar.rulosoft.mimanganu.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import androidx.preference.PreferenceManager;

import java.util.Arrays;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.DeadServer;
import ar.rulosoft.mimanganu.servers.ServerBase;

/**
 * Created by Raul on 28/04/2017.
 */

public class Paths {

    private final static int[] illegalChars = {
            34, 60, 62, 124,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
            21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
            58, 42, 63, 92, 47
    };

    static {
        Arrays.sort(illegalChars);
    }

    public static String generateBasePath(ServerBase serverBase, Manga manga, Chapter chapter, Context context) {
        String serverName = serverBase.getPath();
        if (serverBase instanceof DeadServer) {
            serverName = DeadServer.getServerName(manga);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String dir = prefs.getString("directorio", Environment.getExternalStorageDirectory().getAbsolutePath());
        return dir + "/MiMangaNu/" + cleanFileName(serverName) + "/" +
                cleanFileName(manga.getTitle()).trim() + "/" + cleanFileName(chapter.getTitle()).trim();
    }

    public static String generateBasePath(ServerBase serverBase, Manga manga, Context context) {
        String serverName = serverBase.getPath();
        if (serverBase instanceof DeadServer) {
            serverName = DeadServer.getServerName(manga);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String dir = prefs.getString("directorio",
                Environment.getExternalStorageDirectory().getAbsolutePath());
        return dir + "/MiMangaNu/" + cleanFileName(serverName).trim() + "/" +
                cleanFileName(manga.getTitle()).trim();
    }

    public static String generateBasePath(ServerBase serverBase, Context context) {
        String serverName = serverBase.getPath();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String dir = prefs.getString("directorio",
                Environment.getExternalStorageDirectory().getAbsolutePath());
        return dir + "/MiMangaNu/" + cleanFileName(serverName).trim() + "/";
    }


    public static String generateBasePath(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String dir = prefs.getString("directorio",
                Environment.getExternalStorageDirectory().getAbsolutePath());
        return dir + "/MiMangaNu/";
    }

    private static String cleanFileName(String badFileName) {
        StringBuilder cleanName = new StringBuilder();
        for (int i = 0; i < badFileName.length(); i++) {
            int c = badFileName.charAt(i);
            if (Arrays.binarySearch(illegalChars, c) < 0) {
                cleanName.append((char) c);
            }
        }
        return cleanName.toString();
    }
}
