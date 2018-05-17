package ar.rulosoft.mimanganu;

import android.content.Context;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navigator;
import okhttp3.Request;
import okio.Buffer;

/**
 * Created by Raul on 01/04/2017.
 */
@LargeTest
public class testNine {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    private Context context;
    private Manga manga;
    private Chapter chapter;

    private static String bodyToString(final Request request){
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    @Test
    public void testImages() throws Exception {
        Navigator nav = Navigator.getInstance();
        nav.addHeader("Accept-Language", "es-AR,es;q=0.8,en-US;q=0.5,en;q=0.3");
        nav.addHeader("Accept-Encoding", "deflate");
        nav.addHeader("Accept","application/json, text/plain, */*");
        nav.addHeader("Accept-Encoding","gzip, deflate, br");
        nav.addHeader("Accept-Language","es-AR,es;q=0.8,en-US;q=0.5,en;q=0.3");
        nav.addHeader("Cache-mode","no-cache");
        //nav.addHeader("Connection","keep-alive");
        nav.addHeader("Cookie","__cfduid=d7025d17b59e189d890c2cc05b9feb5c31505483555; tmoSession=eyJpdiI6ImZXZUlPZjVWOGNKS2VCRkVcL1I3XC9iZz09IiwidmFsdWUiOiJ0MEhzdXF0XC93YkFvSkJlUnVvZ3hnN011WWtGUzdEdnJrY1ZOakhnTVRBTGJQZk91NmphTTFaNDhhbUozUnZnRmJndGtBdkNHZlFsTTFZT2RvSWlBSGc9PSIsIm1hYyI6ImMxODZmYmRkNTJiZTQ4YjUwZGQ0MTBkYTRlOWExNmU1NjM3YWFkMTA2MGJjNGJhMzMwOGY3NjY0YmFhMDIyMmIifQ%3D%3D");
        //nav.addHeader("Host","www.tumangaonline.com");
        nav.addHeader("Referer","https://www.tumangaonline.com/biblioteca");
        //nav.addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:54.0) Gecko/20100101 Firefox/54.0");
        //nav.addHeader("X-Requested-With","XMLHttpRequest");
        Log.e("respuesta", nav.get("https://www.tumangaonline.com/api/v1/mangas/2968/capitulos?page=2&tomo=-1"));

    }

}
