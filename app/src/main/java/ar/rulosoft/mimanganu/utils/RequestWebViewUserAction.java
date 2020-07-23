package ar.rulosoft.mimanganu.utils;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.List;

import ar.rulosoft.mimanganu.MainActivity;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.navegadores.Navigator;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class RequestWebViewUserAction extends AppCompatActivity {

    public static final String WEB_TAG = "1_Web_";
    public static final String HEADERS_TAG = "2_Headers_";
    public static final String BODY_TAG = "2_body_";

    private static String lastCookie = "";
    private static MainActivity mainActivity;
    private static boolean running = false;

    public static void init(MainActivity ma) {
        mainActivity = ma;
    }

    public static boolean isRequestAvailable() {
        return mainActivity != null;
    }

    public static synchronized String makeRequest(String web, @Nullable String headers, @Nullable String body) {
        Intent intent = new Intent(mainActivity, RequestWebViewUserAction.class);
        Bundle bundle = new Bundle();
        bundle.putString(WEB_TAG, web);
        if (headers != null)
            bundle.putString(HEADERS_TAG, headers);
        if (headers != null)
            bundle.putString(HEADERS_TAG, headers);

        intent.putExtras(bundle);
        lastCookie = "";
        mainActivity.startActivity(intent);
        running = true;
        while (running) {
            if (lastCookie.length() > 0) {
                return lastCookie;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return lastCookie;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        String web = bundle.getString(WEB_TAG);
        String headers = null, body = null;
        if (bundle.containsKey(HEADERS_TAG)) {
            headers = bundle.getString(HEADERS_TAG);
        }
        if (bundle.containsKey(BODY_TAG)) {
            body = bundle.getString(BODY_TAG);
        }
        setContentView(R.layout.activity_request_webview_user_action);
        WebView wv = findViewById(R.id.webView);
        wv.setWebViewClient(new WebViewClient() {
                                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                    RequestWebViewUserAction.this.runOnUiThread(() -> {
                                        lastCookie = CookieManager.getInstance().getCookie(web);
                                        onBackPressed();
                                    });
                                    return true;
                                }

                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    if (url.contains("__cf_chl_captcha_tk__")) {
                                        lastCookie = CookieManager.getInstance().getCookie(web);
                                        onBackPressed();
                                    }
                                }
                            }
        );
        wv.getSettings().setUserAgentString(Navigator.USER_AGENT);
        wv.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        wv.getSettings().setDisplayZoomControls(true);
        CookieManager cm = CookieManager.getInstance();
        cm.removeAllCookie();
        HttpUrl url = HttpUrl.parse(web);
        List<Cookie> actCookies = Navigator.getCookieJar().loadForRequest(url);
        cm.setCookie(url.host(), "__cfduid=");
        for (Cookie c : actCookies) {
            cm.setCookie(url.host(), c.name() + "=" + c.value());
        }
        wv.getSettings().setJavaScriptEnabled(true);
        HashMap<String, String> headMap = new HashMap<>();
        headMap.put("Referer", web);

        if (body != null) {
            wv.loadDataWithBaseURL(web, body, null, null, null);
        } else {
            wv.loadUrl(web, headMap);
        }
    }

    @Override
    public void onBackPressed() {
        running = false;
        super.onBackPressed();
    }
}
