package ar.rulosoft.mimanganu;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import org.junit.Rule;
import org.junit.Test;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
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
        Navigator nav = Navigator.navigator;
        //nav.addHeader("Referer", "");
        //nav.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:51.0) Gecko/20100101 Firefox/51.0");
        nav.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        nav.addHeader("Accept-Language", "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3");
        //nav.addHeader("Accept-Encoding", "deflate");
        //nav.addHeader("Cookie", "counter=YToyOntzOjU6ImFib3V0IjtpOjE0OTUzNzY1MDY7czoyNzoic2hpbmlnYW1pc2FtYXRvNG5pbm5va2Fub2pvIjtpOjE0OTUzNzY3NTY7fQ%3D%3D; PHPSESSID=ouk65c56jdhghsn7porasee3o3; _ga=GA1.2.2020475924.1495373987; _gid=GA1.2.1567975044.1495376588; newsMM=1; cookieconsent_dismissed=yes");
        //nav.addHeader("DNT", "1");
        //nav.addHeader("Connection", "keep-alive");
        //nav.addHeader("Upgrade-Insecure-Requests", "1");
        String r = nav.get("http://www.mymanga.io/mangas/shinigamisamato4ninnokanojo/");
        Log.i("Aaaaaaaaaaaa", r);
    }

}
