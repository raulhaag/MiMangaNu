package ar.rulosoft.mimanganu;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import org.junit.Rule;
import org.junit.Test;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.EsNineManga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.navegadores.Navigator;

/**
 * Created by Raul on 01/04/2017.
 */
@LargeTest
public class testNine {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    private Manga manga;
    private Chapter chapter;

    @Test
    public void testImages() throws Exception {
        Chapter c = new Chapter("ss", "http://es.ninemanga.com/chapter/Shokugeki%20no%20Soma/559993.html");
        ServerBase s = new EsNineManga(InstrumentationRegistry.getContext());
        Navigator nav = Navigator.navigator;
        nav.addHeader("Accept-Language", "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3");
        nav.addHeader("Referer", c.getPath());
        String r = Navigator.navigator.get("http://es.ninemanga.com/show_ads/google/");
        s.chapterInit(c);
        String st = s.getImageFrom(c, 1);
        Log.i("Aaaaaaaaaaaa", st);

    }

}
