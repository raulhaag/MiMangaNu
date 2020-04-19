package ar.rulosoft.mimanganu;

import android.content.Context;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

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

    @Test
    public void test_DENINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.DENINEMANGA, false, context);
    }

    @Test
    public void test_DESUME() throws Exception {
        new TestServersCommon(ServerBase.DESUME, false, context);
    }

    @Test
    public void test_ESNINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.ESNINEMANGA, false, context);
    }

    @Test
    public void test_FANFOXNET() throws Exception {
        new TestServersCommon(ServerBase.FANFOXNET, false, context);
    }

    @Ignore("FromFolder cannot be tested - yet")
    @SuppressWarnings("unused")
    public void test_FROMFOLDER() throws Exception {
        new TestServersCommon(ServerBase.FROMFOLDER, false, context);
    }

    @Test
    public void test_HEAVENMANGACOM() throws Exception {
        new TestServersCommon(ServerBase.HEAVENMANGACOM, false, context);
    }

    @Test
    public void test_ITNINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.ITNINEMANGA, false, context);
    }

    @Test
    public void test_JAPSCAN() throws Exception {
        new TestServersCommon(ServerBase.JAPSCAN, false, context);
    }

    @Test
    public void test_KISSMANGA() throws Exception {
        new TestServersCommon(ServerBase.KISSMANGA, false, context);
    }

    @Test
    public void test_KUMANGA() throws Exception {
        new TestServersCommon(ServerBase.KUMANGA, false, context);
    }

    @Test
    public void test_MANGAAE() throws Exception {
        new TestServersCommon(ServerBase.MANGAAE, false, context);
    }

    @Test
    public void test_MANGAEDEN() throws Exception {
        new TestServersCommon(ServerBase.MANGAEDEN, false, context);
    }

    @Test
    public void test_MANGAEDENIT() throws Exception {
        new TestServersCommon(ServerBase.MANGAEDENIT, false, context);
    }

    @Test
    public void test_MANGAHERE() throws Exception {
        new TestServersCommon(ServerBase.MANGAHERE, false, context);
    }

    @Test
    public void test_MANGAKAWAII() throws Exception {
        new TestServersCommon(ServerBase.MANGAKAWAII, false, context);
    }

    @Test
    public void test_MANGAPANDA() throws Exception {
        new TestServersCommon(ServerBase.MANGAPANDA, false, context);
    }

    @Test
    public void test_MANGAREADER() throws Exception {
        new TestServersCommon(ServerBase.MANGAREADER, false, context);
    }

    @Test
    public void test_MANGASHIRONET() throws Exception {
        new TestServersCommon(ServerBase.MANGASHIRONET, false, context);
    }

    @Test
    public void test_MANGASTREAM() throws Exception {
        new TestServersCommon(ServerBase.MANGASTREAM, false, context);
    }

    @Test
    public void test_MANGATOWN() throws Exception {
        new TestServersCommon(ServerBase.MANGATOWN, false, context);
    }

    @Test
    public void test_MINTMANGA() throws Exception {
        new TestServersCommon(ServerBase.MINTMANGA, false, context);
    }

    @Test
    public void test_NEUMANGATV() throws Exception {
        new TestServersCommon(ServerBase.NEUMANGATV, false, context);
    }

    @Test
    public void test_NINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.NINEMANGA, false, context);
    }

    @Test
    public void test_RAWSENMANGA() throws Exception {
        new TestServersCommon(ServerBase.RAWSENMANGA, false, context);
    }

    @Test
    public void test_READCOMICONLINE() throws Exception {
        new TestServersCommon(ServerBase.READCOMICONLINE, false, context);
    }

    @Test
    public void test_READMANGAME() throws Exception {
        new TestServersCommon(ServerBase.READMANGAME, false, context);
    }

    @Test
    public void test_READMANGATODAY() throws Exception {
        new TestServersCommon(ServerBase.READMANGATODAY, false, context);
    }

    @Test
    public void test_RUNINEMANGA() throws Exception {
        new TestServersCommon(ServerBase.RUNINEMANGA, false, context);
    }

    @Test
    public void test_TAADD() throws Exception {
        new TestServersCommon(ServerBase.TAADD, false, context);
    }

   /* @Test
    public void test_TUMANGAONLINE() throws Exception {
        new TestServersCommon(ServerBase.TUMANGAONLINE, false, context);
    }*/

    @Test
    public void test_VERCOMICSCOM() throws Exception {
        new TestServersCommon(ServerBase.VERCOMICSCOM, false, context);
    }

    @Test
    public void test_VIEWCOMIC() throws Exception {
        new TestServersCommon(ServerBase.VIEWCOMIC, false, context);
    }
}
