package ar.rulosoft.mimanganu;

import android.content.Context;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.navegadores.Navigator;
import util.TestServersCommon;

@Config(
        shadows = {ShadowNavigator.class}
)
@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestServers {

    private Context context;

    @Before
    public void setupTest() throws Exception {
        context = RuntimeEnvironment.application.getApplicationContext();
        Navigator.initialiseInstance(context);
    }

    @Ignore("FromFolder cannot be tested - yet")
    public void test_FROMFOLDER() throws Exception {
        new TestServersCommon(ServerBase.FROMFOLDER, true, context);
    }


    @Test
    public void test_MANGASHIRONET() throws Exception {
        new TestServersCommon(ServerBase.MANGASHIRONET, true, context);
    }

    @Test
    public void test_VERCOMICSCOM() throws Exception {
        new TestServersCommon(ServerBase.VERCOMICSCOM, true, context);
    }

    @Test
    public void test_RAWSENMANGA() throws Exception {
        new TestServersCommon(ServerBase.RAWSENMANGA, true, context);
    }

    @Test
    public void test_MANGAPANDA() throws Exception {
        new TestServersCommon(ServerBase.MANGAPANDA, true, context);
    }

    @Test
    public void test_ESMANGAHERE() throws Exception {
        new TestServersCommon(ServerBase.ESMANGAHERE, true, context);
    }

    @Test
    public void test_MANGAHERE() throws Exception {
        new TestServersCommon(ServerBase.MANGAHERE, true, context);
    }

    @Test
    public void test_MANGAFOX() throws Exception {
        new TestServersCommon(ServerBase.MANGAFOX, true, context);
    }

    @Test
    public void test_SUBMANGA() throws Exception {
        new TestServersCommon(ServerBase.SUBMANGA, true, context);
    }

    @Test
    public void test_ESMANGA() throws Exception {
        new TestServersCommon(ServerBase.ESMANGA, true, context);
    }

    @Test
    public void test_HEAVENMANGACOM() throws Exception {
        new TestServersCommon(ServerBase.HEAVENMANGACOM, true, context);
    }

    @Test
    public void test_STARKANACOM() throws Exception {
        new TestServersCommon(ServerBase.STARKANACOM, true, context);
    }

    @Test
    public void test_ESNINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.ESNINEMANGA, true, context);
    }

    @Test
    public void test_LECTUREENLIGNE() throws Exception {
        new TestServersCommon(ServerBase.LECTUREENLIGNE, true, context);
    }

    @Ignore("Cannot be tested on host due to Duktape usage (needs JNI) - use instrumented tests")
    public void test_KISSMANGA() throws Exception {
        new TestServersCommon(ServerBase.KISSMANGA, true, context);
    }

    @Test
    public void test_ITNINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.ITNINEMANGA, true, context);
    }

    @Test
    public void test_TUSMANGAS() throws Exception {
        new TestServersCommon(ServerBase.TUSMANGAS, true, context);
    }

    @Test
    public void test_MANGAREADER() throws Exception {
        new TestServersCommon(ServerBase.MANGAREADER, true, context);
    }

    @Test
    public void test_DENINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.DENINEMANGA, true, context);
    }

    @Test
    public void test_RUNINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.RUNINEMANGA, true, context);
    }

    @Test
    public void test_MANGATUBE() throws Exception {
        new TestServersCommon(ServerBase.MANGATUBE, true, context);
    }

    @Test
    public void test_MANGAEDENIT() throws Exception {
        new TestServersCommon(ServerBase.MANGAEDENIT, true, context);
    }

    @Test
    public void test_MYMANGAIO() throws Exception {
        new TestServersCommon(ServerBase.MYMANGAIO, true, context);
    }

    @Ignore("Cannot be tested on host due to Duktape usage (needs JNI) - use instrumented tests")
    public void test_TUMANGAONLINE() throws Exception {
        new TestServersCommon(ServerBase.TUMANGAONLINE, true, context);
    }

    @Test
    public void test_NINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.NINEMANGA, true, context);
    }

    @Test
    public void test_MANGAEDEN() throws Exception {
        new TestServersCommon(ServerBase.MANGAEDEN, true, context);
    }

    @Test
    public void test_LEOMANGA() throws Exception {
        new TestServersCommon(ServerBase.LEOMANGA, true, context);
    }

    @Ignore("Batoto does not work without login credentials")
    public void test_BATOTO() throws Exception {
        new TestServersCommon(ServerBase.BATOTO, true, context);
    }

    @Ignore("Batoto(ES) does not work without login credentials")
    public void test_BATOTOES() throws Exception {
        new TestServersCommon(ServerBase.BATOTOES, true, context);
    }

    @Test
    public void test_JAPSCAN() throws Exception {
        new TestServersCommon(ServerBase.JAPSCAN, true, context);
    }

    @Ignore("Cannot be tested on host due to Duktape usage (needs JNI) - use instrumented tests")
    public void test_READMANGATODAY() throws Exception {
        new TestServersCommon(ServerBase.READMANGATODAY, true, context);
    }

    @Test
    public void test_TAADD() throws Exception {
        new TestServersCommon(ServerBase.TAADD, true, context);
    }

    @Test
    public void test_MANGASTREAM() throws Exception {
        new TestServersCommon(ServerBase.MANGASTREAM, true, context);
    }

    @Ignore("Cannot be tested on host due to Duktape usage (needs JNI) - use instrumented tests")
    public void test_MANGAKAWAII() throws Exception {
        new TestServersCommon(ServerBase.MANGAKAWAII, true, context);
    }

    @Test
    public void test_KUMANGA() throws Exception {
        new TestServersCommon(ServerBase.KUMANGA, true, context);
    }

    @Ignore("Cannot be tested on host due to Duktape usage (needs JNI) - use instrumented tests")
    public void test_MANGAPEDIA() throws Exception {
        new TestServersCommon(ServerBase.MANGAPEDIA, true, context);
    }

    @Test
    public void test_MANGATOWN() throws Exception {
        new TestServersCommon(ServerBase.MANGATOWN, true, context);
    }

    @Test
    public void test_READMANGAME() throws Exception {
        new TestServersCommon(ServerBase.READMANGAME, true, context);
    }

    @Test
    public void test_DESUME() throws Exception {
        new TestServersCommon(ServerBase.DESUME, true, context);
    }

    @Test
    public void test_MANGARAWONLINE() throws Exception {
        new TestServersCommon(ServerBase.MANGARAWONLINE, true, context);
    }

    @Test
    public void test_MINTMANGA() throws Exception {
        new TestServersCommon(ServerBase.MINTMANGA, true, context);
    }

    @Test
    public void test_MANGAAE() throws Exception {
        new TestServersCommon(ServerBase.MANGAAE, true, context);
    }

    @Test
    public void test_NEUMANGATV() throws Exception {
        new TestServersCommon(ServerBase.NEUMANGATV, true, context);
    }

    @Ignore("Cannot be tested on host due to Duktape usage (needs JNI) - use instrumented tests")
    public void test_READCOMICONLINE() throws Exception {
        new TestServersCommon(ServerBase.READCOMICONLINE, true, context);
    }

    @Test
    public void test_READCOMICSTV() throws Exception {
        new TestServersCommon(ServerBase.READCOMICSTV, true, context);
    }

    @Test
    public void test_GOGOCOMIC() throws Exception {
        new TestServersCommon(ServerBase.GOGOCOMIC, true, context);
    }

    @Test
    public void test_VIEWCOMIC() throws Exception {
        new TestServersCommon(ServerBase.VIEWCOMIC, true, context);
    }

}
