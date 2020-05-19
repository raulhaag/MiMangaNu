package ar.rulosoft.navegadores;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CFInterceptor implements Interceptor {
    private String cookies = "";
    private WebView wv;

    public static String getFirstMatch(Pattern p, String source) {
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        if ((response.code() == 503 || response.code() == 403) && response.headers().get("Server").contains("cloudflare")) {
            return resolveOverCF(chain, response);
        }
        return response;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private synchronized Response resolveOverCF(Chain chain, Response response) throws IOException {
        Request request = response.request();
        String content = response.body().string();
        response.body().close();
        Handler h = new Handler(Looper.getMainLooper());
        h.post(() -> {
                    wv = new WebView(Navigator.getInstance().getContext());
                    wv.getSettings().setJavaScriptEnabled(true);
                    wv.getSettings().setUserAgentString(Navigator.USER_AGENT);
                    wv.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            if (url.contains("__cf_chl_jschl_tk__=")) {
                                cookies = CookieManager.getInstance().getCookie(url);
                                String[] cks = cookies.split(";");
                                ArrayList<Cookie> cookieArrayList = new ArrayList<>();
                                HttpUrl url1 = HttpUrl.parse(url);
                                url1 = HttpUrl.parse((url1.isHttps() ? "https://" : "http://") + url1.host());
                                for (String c : cks) {
                                    cookieArrayList.add(Cookie.parse(url1, c));
                                }
                                Navigator.getInstance().getHttpClient().cookieJar().saveFromResponse(url1, cookieArrayList);
                                view.destroy();
                            }
                        }
                    });
                    wv.loadDataWithBaseURL(request.url().toString(), content, null, null, null);
                }
        );
        int counter = 20;
        while (counter > 0 && cookies.length() == 0) { //await for wv cookie
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            counter--;
        }
        if (counter == 0) {
            h.post(() -> wv.destroy());
            wv = null;
        }
        Request request1 = new Request.Builder()
                .url(request.url())
                .addHeader("User-Agent", Navigator.USER_AGENT)
                .addHeader("Accept-Language", "es-AR,es;q=0.8,en-US;q=0.5,en;q=0.3")
                .addHeader("Connection", "keep-alive")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .build();

        return chain.proceed(request1);
    }
}