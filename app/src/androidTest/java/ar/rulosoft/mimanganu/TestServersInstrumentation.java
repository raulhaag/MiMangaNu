package ar.rulosoft.mimanganu;

import android.content.Context;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.utils.TestServersCommon;
import ar.rulosoft.navegadores.Navigator;

/**
 * Created by Raul on 09/01/2017.
 */

@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestServersInstrumentation {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    private Context context;

    @Before
    public void setupTest() throws Exception {
        context = mActivityRule.getActivity().getApplicationContext();
        Navigator.initialiseInstance(context);
    }

    @Ignore("FromFolder cannot be tested - yet")
    @SuppressWarnings("unused")
    public void test_FROMFOLDER() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.FROMFOLDER, context), false);
    }

    @Test
    public void test_RAWSENMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.RAWSENMANGA, context), false);
    }

    @Test
    public void test_MANGAPANDA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAPANDA, context), false);
    }

    @Test
    public void test_ESMANGAHERE() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.ESMANGAHERE, context), false);
    }

    @Test
    public void test_MANGAHERE() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAHERE, context), false);
    }

    @Test
    public void test_MANGAFOX() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAFOX, context), false);
    }

    @Test
    public void test_SUBMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.SUBMANGA, context), false);
    }

    @Test
    public void test_ESMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.ESMANGA, context), false);
    }

    @Test
    public void test_HEAVENMANGACOM() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.HEAVENMANGACOM, context), false);
    }

    @Test
    public void test_STARKANACOM() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.STARKANACOM, context), false);
    }

    @Test
    public void test_ESNINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.ESNINEMANGA, context), false);
    }

    @Test
    public void test_LECTUREENLIGNE() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.LECTUREENLIGNE, context), false);
    }

    @Test
    public void test_KISSMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.KISSMANGA, context), false);
    }

    @Test
    public void test_ITNINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.ITNINEMANGA, context), false);
    }

    @Test
    public void test_TUSMANGAS() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.TUSMANGAS, context), false);
    }

    @Test
    public void test_MANGAREADER() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAREADER, context), false);
    }

    @Test
    public void test_DENINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.DENINEMANGA, context), false);
    }

    @Test
    public void test_RUNINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.RUNINEMANGA, context), false);
    }

    @Test
    public void test_MANGATUBE() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGATUBE, context), false);
    }

    @Test
    public void test_MANGAEDENIT() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAEDENIT, context), false);
    }

    @Test
    public void test_MYMANGAIO() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MYMANGAIO, context), false);
    }

    @Test
    public void test_TUMANGAONLINE() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.TUMANGAONLINE, context), false);
    }

    @Test
    public void test_NINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.NINEMANGA, context), false);
    }

    @Test
    public void test_MANGAEDEN() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAEDEN, context), false);
    }

    @Test
    public void test_LEOMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.LEOMANGA, context), false);
    }

    @Ignore("Batoto does not work without login credentials")
    @SuppressWarnings("unused")
    public void test_BATOTO() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.BATOTO, context), false);
    }

    @Ignore("Batoto(ES) does not work without login credentials")
    @SuppressWarnings("unused")
    public void test_BATOTOES() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.BATOTOES, context), false);
    }

    @Test
    public void test_JAPSCAN() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.JAPSCAN, context), false);
    }

    @Test
    public void test_READMANGATODAY() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.READMANGATODAY, context), false);
    }

    @Test
    public void test_TAADD() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.TAADD, context), false);
    }

    @Test
    public void test_MANGASTREAM() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGASTREAM, context), false);
    }

    @Test
    public void test_MANGAKAWAII() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAKAWAII, context), false);
    }

    @Test
    public void test_KUMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.KUMANGA, context), false);
    }

    @Test
    public void test_MANGAPEDIA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAPEDIA, context), false);
    }

    @Test
    public void test_MANGATOWN() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGATOWN, context), false);
    }

    @Test
    public void test_READCOMICONLINE() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.READCOMICONLINE, context), false);
    }

    @Test
    public void test_READCOMICSTV() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.READCOMICSTV, context), false);
    }

    @Test
    public void test_GOGOCOMIC() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.GOGOCOMIC, context), false);
    }

    @Test
    public void test_VIEWCOMIC() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.VIEWCOMIC, context), false);
    }
}
