package ar.rulosoft.navegadores;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.utils.RequestWebViewUserAction;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class CFInterceptor implements Interceptor {
    private static Pattern PATTERN_HOST = Pattern.compile("([^.]+\\..{1,3}$)");
    private static CFInterceptor instance;
    private String cookies = "";
    private WebView wv;

    private CFInterceptor() {
    }

    public static CFInterceptor getInstance() {
        if (instance == null) instance = new CFInterceptor();
        return instance;
    }

    static String removeSubDomains(String url) {
        Matcher m = PATTERN_HOST.matcher(url);
        if (m.find()) {
            return m.group(1);

        }
        return url;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        if ((response.code() == 503 || response.code() == 403) && response.headers().get("Server").contains("cloudflare")) {
            return resolveOverCF(chain);
        }
        return response;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private synchronized Response resolveOverCF(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        if ((response.code() == 503 || response.code() == 403) && response.headers().get("Server").contains("cloudflare")) {

            cookies = "";
            final Request request = response.request();
            String content = response.body().string();
            response.body().close();
            response.close();
            int counter = 15;
            Handler h = new Handler(Looper.getMainLooper());
            if (!content.contains("__cf_chl_captcha_tk__")) {
                h.post(() -> {
                            wv = new WebView(Navigator.getInstance().getContext());
                            wv.getSettings().setJavaScriptEnabled(true);
                            wv.getSettings().setUserAgentString(Navigator.USER_AGENT);
                            List<Cookie> actCookies = Navigator.getCookieJar().loadForRequest(request.url());
                            boolean fcfc = false;
                            for (Cookie c : actCookies) {
                                if (c.name().contains("__cfduid")) {
                                    fcfc = true;
                                }
                                CookieManager.getInstance().setCookie(request.url().host(), c.name() + "=" + c.value());
                            }
                            if (!fcfc) {
                                CookieManager.getInstance().setCookie(request.url().host(), "__cfduid=");
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                CookieManager.getInstance().flush();
                            }
                            wv.setWebViewClient(new WebViewClient() {
                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    if (url.contains("__cf_chl_jschl_tk__")) {
                                        addWebViewCookiesToNavigator(url);
                                        view.destroy();
                                    }
                                }
                            });
                            wv.loadDataWithBaseURL(request.url().toString(), content, null, null, null);
                        }
                );
            } else {
                if (RequestWebViewUserAction.isRequestAvailable()) {
                    RequestWebViewUserAction.makeRequest(request.url().toString(), null, content);
                    addWebViewCookiesToNavigator(request.url().toString());
                    counter = 0;
                } else {
                    return response;
                }
            }
            while (counter > 0 && cookies.length() == 0) { //await for wv cookie
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                counter--;
            }
            if (counter == 0) {
                h.post(() -> {
                    try {
                        wv.destroy();
                    } catch (NullPointerException ignore) {
                    }
                });
                wv = null;
            }
            Request request1 = new Request.Builder()
                    .url(request.url())
                    .headers(request.headers())
                    .build();

            return chain.proceed(request1);
        } else {
            return response;
        }
    }

    public void addWebViewCookiesToNavigator(String url) {
        cookies = CookieManager.getInstance().getCookie(url);
        String[] cks = cookies.split(";");
        ArrayList<Cookie> cookieArrayList = new ArrayList<>();
        HttpUrl url1 = HttpUrl.parse(url);
        url1 = HttpUrl.parse((url1.isHttps() ? "https://" : "http://") + url1.host());
        for (String c : cks) {
            cookieArrayList.add(Cookie.parse(url1, c + "; Max-Age=36000000; domain=" + removeSubDomains(url1.host())));
        }
        Navigator.getInstance().getHttpClient().cookieJar().saveFromResponse(url1, cookieArrayList);
    }
}