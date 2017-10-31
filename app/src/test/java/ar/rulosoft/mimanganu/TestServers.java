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

// TODO fix KeyStore usage in Navigator with "AndroidCAStore"

// need to specify SDK version for Robolectric (see targetSdkVersion in app.gradle)
@Config(manifest = "../app/src/main/AndroidManifest.xml", sdk = 25)
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
        new TestServersCommon(ServerBase.getServer(ServerBase.FROMFOLDER, context));
    }

    @Test
    public void test_RAWSENMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.RAWSENMANGA, context));
    }

    @Test
    public void test_MANGAPANDA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAPANDA, context));
    }

    @Test
    public void test_ESMANGAHERE() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.ESMANGAHERE, context));
    }

    @Test
    public void test_MANGAHERE() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAHERE, context));
    }

    @Test
    public void test_MANGAFOX() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAFOX, context));
    }

    @Test
    public void test_SUBMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.SUBMANGA, context));
    }

    @Test
    public void test_ESMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.ESMANGA, context));
    }

    @Test
    public void test_HEAVENMANGACOM() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.HEAVENMANGACOM, context));
    }

    @Test
    public void test_STARKANACOM() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.STARKANACOM, context));
    }

    @Test
    public void test_ESNINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.ESNINEMANGA, context));
    }

    @Test
    public void test_LECTUREENLIGNE() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.LECTUREENLIGNE, context));
    }

    @Ignore("Cannot be tested on host due to Duktape usage (needs JNI) - use instrumented tests")
    public void test_KISSMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.KISSMANGA, context));
    }

    @Test
    public void test_ITNINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.ITNINEMANGA, context));
    }

    @Test
    public void test_TUSMANGAS() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.TUSMANGAS, context));
    }

    @Test
    public void test_MANGAREADER() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAREADER, context));
    }

    @Test
    public void test_DENINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.DENINEMANGA, context));
    }

    @Test
    public void test_RUNINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.RUNINEMANGA, context));
    }

    @Test
    public void test_MANGATUBE() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGATUBE, context));
    }

    @Test
    public void test_MANGAEDENIT() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAEDENIT, context));
    }

    @Test
    public void test_MYMANGAIO() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MYMANGAIO, context));
    }

    @Test
    public void test_TUMANGAONLINE() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.TUMANGAONLINE, context));
    }

    @Test
    public void test_NINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.NINEMANGA, context));
    }

    @Test
    public void test_MANGAEDEN() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAEDEN, context));
    }

    @Test
    public void test_LEOMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.LEOMANGA, context));
    }

    @Ignore("Batoto does not work without login credentials")
    public void test_BATOTO() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.BATOTO, context));
    }

    @Ignore("Batoto(ES) does not work without login credentials")
    public void test_BATOTOES() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.BATOTOES, context));
    }

    @Test
    public void test_JAPSCAN() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.JAPSCAN, context));
    }

    @Test
    public void test_READMANGATODAY() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.READMANGATODAY, context));
    }

    @Test
    public void test_TAADD() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.TAADD, context));
    }

    @Test
    public void test_MANGASTREAM() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGASTREAM, context));
    }

    @Ignore("Cannot be tested on host due to Duktape usage (needs JNI) - use instrumented tests")
    public void test_MANGAKAWAII() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAKAWAII, context));
    }

    @Test
    public void test_KUMANGA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.KUMANGA, context));
    }

    @Ignore("Cannot be tested on host due to Duktape usage (needs JNI) - use instrumented tests")
    public void test_MANGAPEDIA() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGAPEDIA, context));
    }

    @Test
    public void test_MANGATOWN() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.MANGATOWN, context));
    }

    @Ignore("Cannot be tested on host due to Duktape usage (needs JNI) - use instrumented tests")
    public void test_READCOMICONLINE() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.READCOMICONLINE, context));
    }

    @Test
    public void test_READCOMICSTV() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.READCOMICSTV, context));
    }

    @Test
    public void test_GOGOCOMIC() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.GOGOCOMIC, context));
    }

    @Test
    public void test_VIEWCOMIC() throws Exception {
        new TestServersCommon(ServerBase.getServer(ServerBase.VIEWCOMIC, context));
    }
}
