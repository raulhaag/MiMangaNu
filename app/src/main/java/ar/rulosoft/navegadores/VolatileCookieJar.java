package ar.rulosoft.navegadores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class VolatileCookieJar implements CookieJar {
    private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();
    private final ArrayList<Cookie> cookies = new ArrayList<>(); // just because only for login

    public boolean contain(String key) {
        for (Cookie c : cookies) {
            if (c.name().equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cookieStore.put(url, cookies);
        for (Cookie c : cookies) {
            if (!this.cookies.contains(c)) {
                this.cookies.add(c);
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = cookieStore.get(url);
        return cookies != null ? cookies : new ArrayList<Cookie>();
    }
}
