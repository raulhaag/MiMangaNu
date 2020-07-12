package ar.rulosoft.mimanganu;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.navegadores.Navigator;

/**
 * Created by Raul on 01/04/2017.
 */
@LargeTest
public class japScanDicMaker {

    private static String[] pages = {"https://www.japscan.co/lecture-en-ligne/tales-of-demons-and-gods/265/",
            "https://www.japscan.co/lecture-en-ligne/the-promised-neverland/170/",
            "https://www.japscan.co/lecture-en-ligne/2001-night-stories/volume-1/",
            "https://www.japscan.co/lecture-en-ligne/dr-stone/142/"};
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    WebView webView;

    @Test
    public void testImages() throws Exception {
        MainActivity ma = mActivityRule.getActivity();
        Context ctx = ma.getApplicationContext();
        Navigator nav = Navigator.getInstance();
        String baseData = nav.get(pages[0]);
        String inSources = "";
        Pattern p = Pattern.compile("<div id=\"image\" data-src=\"(https:\\/\\/c.japscan.co\\/(.+?)\\.jpg)\"");
        Matcher m = p.matcher(baseData);

        while (m.find()) {
            //    System.out.println(m.group(1));
            inSources = inSources + m.group(2);
        }
        inSources = "<div id=\"image\" data-src=\"https://c.japscan.co/" + inSources + "data-next-link=\"/manga/abc/\" >";
        // baseData = baseData.replaceAll("<div id=\"image\" data-src=\"(.+?)>", inSources);
        final String cd = baseData;
        ma.runOnUiThread(() -> {
            webView = new WebView(ctx);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setBlockNetworkImage(false);
            webView.getSettings().setUserAgentString(Navigator.USER_AGENT);
            webView.setWebViewClient(new WebViewClient() {

                @Override
                public void onLoadResource(WebView view, String url) {
                    if (url.contains(".jpg")) {
                        Log.e("jpg", url);
                    }
                    super.onLoadResource(view, url);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                }
            });
            // webView.loadUrl(pages[1]);
            webView.loadDataWithBaseURL(pages[0], cd, null, null, null);
        });
        // Log.e("ins", inSources);
        for (int i = 0; i < 50; i++) {
            Log.i("" + i, "Secconds");
            Thread.sleep(1000);
        }

    }
}
