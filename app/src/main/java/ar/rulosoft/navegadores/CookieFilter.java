package ar.rulosoft.navegadores;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.CookieCache;
import com.franmontiel.persistentcookiejar.persistence.CookiePersistor;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

/**
 * Created by Raúl on 26/11/2017.
 */

public class CookieFilter extends PersistentCookieJar {
    public CookieFilter(CookieCache cache, CookiePersistor persistor) {
        super(cache, persistor);
    }

    @Override
    public synchronized List<Cookie> loadForRequest(HttpUrl url) {
        if (url.toString().matches(".+?senmanga.com/viewer.+")) {
            return new ArrayList<>();
        } else {
            return super.loadForRequest(url);
        }
    }
}
