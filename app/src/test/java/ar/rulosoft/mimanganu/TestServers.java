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
import ar.rulosoft.mimanganu.utils.TestServersCommon;

@Config(
        manifest = "../app/src/main/AndroidManifest.xml",
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
        new TestServersCommon(ServerBase.getServer(ServerBase.FROMFOLDER, context), true);
    }

    @Test
    public void test_RAWSENMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.RAWSENMANGA, context), true);
    }

    @Test
    public void test_MANGAPANDA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAPANDA, context), true);
    }

    @Test
    public void test_ESMANGAHERE() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.ESMANGAHERE, context), true);
    }

    @Test
    public void test_MANGAHERE() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAHERE, context), true);
    }

    @Test
    public void test_MANGAFOX() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAFOX, context), true);
    }

    @Test
    public void test_SUBMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.SUBMANGA, context), true);
    }

    @Test
    public void test_ESMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.ESMANGA, context), true);
    }

    @Test
    public void test_HEAVENMANGACOM() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.HEAVENMANGACOM, context), true);
    }

    @Test
    public void test_STARKANACOM() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.STARKANACOM, context), true);
    }

    @Test
    public void test_ESNINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.ESNINEMANGA, context), true);
    }

    @Test
    public void test_LECTUREENLIGNE() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.LECTUREENLIGNE, context), true);
    }

    @Ignore("Cannot be tested on host due to Duktape usage (needs JNI) - use instrumented tests")
    public void test_KISSMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.KISSMANGA, context), true);
    }

    @Test
    public void test_ITNINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.ITNINEMANGA, context), true);
    }

    @Test
    public void test_TUSMANGAS() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.TUSMANGAS, context), true);
    }

    @Test
    public void test_MANGAREADER() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAREADER, context), true);
    }

    @Test
    public void test_DENINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.DENINEMANGA, context), true);
    }

    @Test
    public void test_RUNINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.RUNINEMANGA, context), true);
    }

    @Test
    public void test_MANGATUBE() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGATUBE, context), true);
    }

    @Test
    public void test_MANGAEDENIT() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAEDENIT, context), true);
    }

    @Test
    public void test_MYMANGAIO() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MYMANGAIO, context), true);
    }

    @Test
    public void test_TUMANGAONLINE() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.TUMANGAONLINE, context), true);
    }

    @Test
    public void test_NINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.NINEMANGA, context), true);
    }

    @Test
    public void test_MANGAEDEN() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAEDEN, context), true);
    }

    @Test
    public void test_LEOMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.LEOMANGA, context), true);
    }

    @Ignore("Batoto does not work without login credentials")
    public void test_BATOTO() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.BATOTO, context), true);
    }

    @Ignore("Batoto(ES) does not work without login credentials")
    public void test_BATOTOES() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.BATOTOES, context), true);
    }

    @Test
    public void test_JAPSCAN() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.JAPSCAN, context), true);
    }

    @Test
    public void test_READMANGATODAY() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.READMANGATODAY, context), true);
    }

    @Test
    public void test_TAADD() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.TAADD, context), true);
    }

    @Test
    public void test_MANGASTREAM() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGASTREAM, context), true);
    }

    @Ignore("Cannot be tested on host due to Duktape usage (needs JNI) - use instrumented tests")
    public void test_MANGAKAWAII() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAKAWAII, context), true);
    }

    @Test
    public void test_KUMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.KUMANGA, context), true);
    }

    @Ignore("Cannot be tested on host due to Duktape usage (needs JNI) - use instrumented tests")
    public void test_MANGAPEDIA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAPEDIA, context), true);
    }

    @Test
    public void test_MANGATOWN() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGATOWN, context), true);
    }

    @Ignore("Cannot be tested on host due to Duktape usage (needs JNI) - use instrumented tests")
    public void test_READCOMICONLINE() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.READCOMICONLINE, context), true);
    }

    @Test
    public void test_READCOMICSTV() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.READCOMICSTV, context), true);
    }

    @Test
    public void test_GOGOCOMIC() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.GOGOCOMIC, context), true);
    }

    @Test
    public void test_VIEWCOMIC() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.VIEWCOMIC, context), true);
    }
}
