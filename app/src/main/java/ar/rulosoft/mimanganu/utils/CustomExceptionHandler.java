package ar.rulosoft.mimanganu.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

// taken from https://stackoverflow.com/questions/601503/how-do-i-obtain-crash-data-from-my-android-application
// rrainn / Prags
// only minimal changes


public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler defaultUEH;

    private String localPath;


    /*
     * if any of the parameters is null, the respective functionality
     * will not be used
     */
    public CustomExceptionHandler(String localPath) {
        this.localPath = localPath;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    public static void Attach(Context ctx) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            String dir = prefs.getString("directorio", Environment.getExternalStorageDirectory().getAbsolutePath()) + "/MiMangaNu/logs/";
            if (!new File(dir).exists()) {
                new File(dir).mkdir();
            }
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(dir));
            }
        } catch (Exception e) {
            Log.e("MiMangaNu", "can't start exception handler");
        }
    }

    public void uncaughtException(Thread t, Throwable e) {
        String timestamp = (DateFormat.format("dd-MM-yyyy--hh-mm-ss", new java.util.Date()).toString());
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        String filename = timestamp + ".txt";

        if (localPath != null) {
            writeToFile(stacktrace, filename);
        }
        defaultUEH.uncaughtException(t, e);
    }

    private void writeToFile(String stacktrace, String filename) {
        try {
            BufferedWriter bos = new BufferedWriter(new FileWriter(localPath + "/" + filename));
            bos.write(stacktrace);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}