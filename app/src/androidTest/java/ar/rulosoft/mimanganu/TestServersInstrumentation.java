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
import ar.rulosoft.navegadores.Navigator;
import util.TestServersCommon;

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
        new TestServersCommon(ServerBase.FROMFOLDER, false, context);
    }

    @Test
    public void test_MANGASHIRONET() throws Exception {
        new TestServersCommon(ServerBase.MANGASHIRONET, false, context);
    }

    @Test
    public void test_VERCOMICSCOM() throws Exception {
        new TestServersCommon(ServerBase.VERCOMICSCOM, false, context);
    }

    @Test
    public void test_RAWSENMANGA() throws Exception {
        new TestServersCommon(ServerBase.RAWSENMANGA, false, context);
    }

    @Test
    public void test_MANGAPANDA() throws Exception {
        new TestServersCommon(ServerBase.MANGAPANDA, false, context);
    }

    @Test
    public void test_ESMANGAHERE() throws Exception {
        new TestServersCommon(ServerBase.ESMANGAHERE, false, context);
    }

    @Test
    public void test_MANGAHERE() throws Exception {
        new TestServersCommon(ServerBase.MANGAHERE, false, context);
    }

    @Test
    public void test_MANGAFOX() throws Exception {
        new TestServersCommon(ServerBase.MANGAFOX, false, context);
    }

    @Test
    public void test_SUBMANGA() throws Exception {
        new TestServersCommon(ServerBase.SUBMANGA, false, context);
    }

    @Test
    public void test_ESMANGA() throws Exception {
        new TestServersCommon(ServerBase.ESMANGA, false, context);
    }

    @Test
    public void test_HEAVENMANGACOM() throws Exception {
        new TestServersCommon(ServerBase.HEAVENMANGACOM, false, context);
    }

    @Test
    public void test_STARKANACOM() throws Exception {
        new TestServersCommon(ServerBase.STARKANACOM, false, context);
    }

    @Test
    public void test_ESNINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.ESNINEMANGA, false, context);
    }

    @Test
    public void test_LECTUREENLIGNE() throws Exception {
        new TestServersCommon(ServerBase.LECTUREENLIGNE, false, context);
    }

    @Test
    public void test_KISSMANGA() throws Exception {
        new TestServersCommon(ServerBase.KISSMANGA, false, context);
    }

    @Test
    public void test_ITNINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.ITNINEMANGA, false, context);
    }

    @Test
    public void test_TUSMANGAS() throws Exception {
        new TestServersCommon(ServerBase.TUSMANGAS, false, context);
    }

    @Test
    public void test_MANGAREADER() throws Exception {
        new TestServersCommon(ServerBase.MANGAREADER, false, context);
    }

    @Test
    public void test_DENINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.DENINEMANGA, false, context);
    }

    @Test
    public void test_RUNINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.RUNINEMANGA, false, context);
    }

    @Test
    public void test_MANGATUBE() throws Exception {
        new TestServersCommon(ServerBase.MANGATUBE, false, context);
    }

    @Test
    public void test_MANGAEDENIT() throws Exception {
        new TestServersCommon(ServerBase.MANGAEDENIT, false, context);
    }

    @Test
    public void test_MYMANGAIO() throws Exception {
        new TestServersCommon(ServerBase.MYMANGAIO, false, context);
    }

    @Test
    public void test_TUMANGAONLINE() throws Exception {
        new TestServersCommon(ServerBase.TUMANGAONLINE, false, context);
    }

    @Test
    public void test_NINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.NINEMANGA, false, context);
    }

    @Test
    public void test_MANGAEDEN() throws Exception {
        new TestServersCommon(ServerBase.MANGAEDEN, false, context);
    }

    @Test
    public void test_LEOMANGA() throws Exception {
        new TestServersCommon(ServerBase.LEOMANGA, false, context);
    }

    @Ignore("Batoto does not work without login credentials")
    @SuppressWarnings("unused")
    public void test_BATOTO() throws Exception {
        new TestServersCommon(ServerBase.BATOTO, false, context);
    }

    @Ignore("Batoto(ES) does not work without login credentials")
    @SuppressWarnings("unused")
    public void test_BATOTOES() throws Exception {
        new TestServersCommon(ServerBase.BATOTOES, false, context);
    }

    @Test
    public void test_JAPSCAN() throws Exception {
        new TestServersCommon(ServerBase.JAPSCAN, false, context);
    }

    @Test
    public void test_READMANGATODAY() throws Exception {
        new TestServersCommon(ServerBase.READMANGATODAY, false, context);
    }

    @Test
    public void test_TAADD() throws Exception {
        new TestServersCommon(ServerBase.TAADD, false, context);
    }

    @Test
    public void test_MANGASTREAM() throws Exception {
        new TestServersCommon(ServerBase.MANGASTREAM, false, context);
    }

    @Test
    public void test_MANGAKAWAII() throws Exception {
        new TestServersCommon(ServerBase.MANGAKAWAII, false, context);
    }

    @Test
    public void test_KUMANGA() throws Exception {
        new TestServersCommon(ServerBase.KUMANGA, false, context);
    }

    @Test
    public void test_MANGAPEDIA() throws Exception {
        new TestServersCommon(ServerBase.MANGAPEDIA, false, context);
    }

    @Test
    public void test_MANGATOWN() throws Exception {
        new TestServersCommon(ServerBase.MANGATOWN, false, context);
    }

    @Test
    public void test_READMANGAME() throws Exception {
        new TestServersCommon(ServerBase.READMANGAME, false, context);
    }

    @Test
    public void test_DESUME() throws Exception {
        new TestServersCommon(ServerBase.DESUME, false, context);
    }

    @Test
    public void test_MANGARAWONLINE() throws Exception {
        new TestServersCommon(ServerBase.MANGARAWONLINE, false, context);
    }

    @Test
    public void test_MINTMANGA() throws Exception {
        new TestServersCommon(ServerBase.MINTMANGA, false, context);
    }

    @Test
    public void test_MANGAAE() throws Exception {
        new TestServersCommon(ServerBase.MANGAAE, false, context);
    }

    @Test
    public void test_NEUMANGATV() throws Exception {
        new TestServersCommon(ServerBase.NEUMANGATV, false, context);
    }

    @Test
    public void test_READCOMICONLINE() throws Exception {
        new TestServersCommon(ServerBase.READCOMICONLINE, false, context);
    }

    @Test
    public void test_READCOMICSTV() throws Exception {
        new TestServersCommon(ServerBase.READCOMICSTV, false, context);
    }

    @Test
    public void test_GOGOCOMIC() throws Exception {
        new TestServersCommon(ServerBase.GOGOCOMIC, false, context);
    }

    @Test
    public void test_VIEWCOMIC() throws Exception {
        new TestServersCommon(ServerBase.VIEWCOMIC, false, context);
    }

}
