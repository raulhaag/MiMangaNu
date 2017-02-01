package ar.rulosoft.mimanganu.servers;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import ar.rulosoft.mimanganu.MainActivity;
import ar.rulosoft.navegadores.OkHttpClientConnectionChecker;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Raul on 31/01/2017.
 */


@RunWith(AndroidJUnit4.class)
@LargeTest
public class EsNineMangaTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void inet() throws Exception {
        PersistentCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(),
                new SharedPrefsCookiePersistor(InstrumentationRegistry.getContext()));
        OkHttpClient httpClient = new OkHttpClientConnectionChecker.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .build();
        Response response = httpClient.newCall(new Request.Builder().url("http://ninemanga.com/")
                .build()).execute();
        String responseStr = response.body().string();
        assertTrue(responseStr.length() > 0);
    }


}