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
        if (url.toString().matches(".+?senmanga.com/viewer.+") || url.toString().matches(".+?gardenmanage.com/.+")) {
            return new ArrayList<>();
        } else if (url.toString().contains("neumanga.tv")) {
            List<Cookie> oc = super.loadForRequest(url);
            oc.add(Cookie.parse(url, "age_confirmed=1"));
            return oc;
        } else {
            return super.loadForRequest(url);
        }
    }
}
