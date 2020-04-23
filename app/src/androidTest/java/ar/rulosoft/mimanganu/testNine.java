package ar.rulosoft.mimanganu;

import android.content.Context;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.JSServerHelper;
import ar.rulosoft.mimanganu.servers.TuMangaOnline;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;
import okhttp3.Request;
import okio.Buffer;

import static ar.rulosoft.mimanganu.servers.ServerBase.TUMANGAONLINE;
import static ar.rulosoft.mimanganu.servers.ServerBase.getFirstMatch;
import static ar.rulosoft.mimanganu.servers.ServerBase.getServer;

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
    private String script;
    private TuMangaOnline tmoServ = (TuMangaOnline) getServer(TUMANGAONLINE, context);
    private JSServerHelper scriptHelper;


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
        checkScript();
        scriptHelper.notifyAll();
    }

    private void checkScript() throws Exception {
        if (scriptHelper == null) {
            String d = " " + context.getString(R.string.factor_suffix).hashCode() + getServer(TUMANGAONLINE, context).getServerID() + TUMANGAONLINE;
            try {
                script = Util.xorDecode(tmoServ.getNavWithNeededHeaders().get("https://raw.githubusercontent.com/raulhaag/MiMangaNu/master/js_plugin/" + tmoServ.getServerID() + "_5.js"), d);
            } catch (Exception e) {
                script = tmoServ.getNavWithNeededHeaders().get("https://github.com/raulhaag/MiMangaNu/blob/master/js_plugin/" + tmoServ.getServerID() + "_5.js");
                script = Util.xorDecode(Util.getInstance().fromHtml(getFirstMatch("(<table class=\"highlight tab-size js-file-line-container\"[\\s\\S]+<\\/table>)", script, "error obteniendo script")).toString(), d);
            }
            if (!script.isEmpty())
                scriptHelper = new JSServerHelper(context, script);
        }

    }

}
