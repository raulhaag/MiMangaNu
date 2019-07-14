package ar.rulosoft.navegadores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class VolatileCookieJar implements CookieJar {
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

    public boolean contain(HttpUrl url, String key) {
        return contain(loadForRequest(url), key);
    }

    public boolean contain(List<Cookie> cookies, String key) {
        for (Cookie c : cookies) {
            if (c.name().equals(key)) {
                return true;
            }
        }
        return false;
    }

    private void removeCookie(List<Cookie> cookies, String key) {
        for (Cookie c : cookies) {
            if (c.name().equals(key)) {
                cookies.remove(c);
                return;
            }
        }
    }

    public String getValue(HttpUrl url, String key) {
        List<Cookie> cookies = loadForRequest(url);
        for (Cookie c : cookies) {
            if (c.name().equals(key)) {
                return c.value();
            }
        }
        return "";
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        List<Cookie> oldCookies = loadForRequest(url);
        for (Cookie c : cookies) {
            if (c.expiresAt() > 0) {
                if (contain(oldCookies, c.name())) {
                    removeCookie(oldCookies, c.name());
                }
                oldCookies.add(c);
            }
        }
        cookieStore.put(url.host(), oldCookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = cookieStore.get(url.host());
        return cookies != null ? cookies : new ArrayList<Cookie>();
    }
}
