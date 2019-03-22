package ar.rulosoft.navegadores;

import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class CFInterceptor implements Interceptor {

    boolean cfl = false;
    private WebView webView;
    private String cfurl;

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
        if (response.code() == 503 && response.headers().get("Server").contains("cloudflare")) {
            return resolveOverCF(chain, response);
        }
        return response;
    }

    public synchronized Response resolveOverCF(Chain chain, Response response) throws IOException {
        final Request request = response.request();
        cfl = false;
        Navigator.getInstance().getMlHandler().post(new Runnable() {
            @Override
            public void run() {
                webView = new WebView(Navigator.getInstance().getContext());
                webView.setWebViewClient(new WebViewClient() {
                    boolean cs = false;

                    @Override
                    public void onLoadResource(WebView view, String url) {
                        if (cs) {
                        }
                        if (url.contains("cdn-cgi/l/chk_jschl")) { //challenged send;
                            view.stopLoading();
                            cfl = true;
                            cfurl = url;
                        }
                    }
                });
                CookieManager.getInstance().removeAllCookies(null);
                CookieManager.getInstance().flush();
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setUserAgentString(Navigator.USER_AGENT);
                webView.getSettings().setLoadsImagesAutomatically(false);
                webView.getSettings().setBlockNetworkLoads(false);
                webView.loadUrl(request.url().toString());
            }
        });

        int toCounter = 0;
        while (!cfl && toCounter < 20) {
            try {
                toCounter++;
                Thread.sleep(1000);
                if (cfl) break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Navigator.getInstance().getMlHandler().post(new Runnable() {
            @Override
            public void run() {
                webView.destroy();
            }
        });

        if (toCounter >= 20) {
            Navigator.getInstance().getMlHandler().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Navigator.getInstance().getContext(), "Timeout", Toast.LENGTH_LONG).show();
                }
            });
            return null;
        }
        Request request1 = new Request.Builder()
                .url(cfurl)
                .header("User-Agent", Navigator.USER_AGENT)
                .header("Referer", request.url().toString())
                .header("Accept-Language", "en, eu")
                .header("Connection", "keep-alive")
                .header("Accept", "text/html,application/xhtml+xml,application/xml")
                .build();

        return chain.proceed(request1);//.addHeader("Cookie", lastCookie).build());
    }
}
