package ar.rulosoft.mimanganu;

import android.content.Context;
import android.util.Log;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.squareup.duktape.Duktape;

import org.junit.Rule;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;

/**
 * Created by Raul on 01/04/2017.
 */
@LargeTest
public class testCF {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    String content = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
            "<head>\n" +
            "<title>Please wait 5 seconds...</title>\n" +
            "<script type=\"text/javascript\">\n" +
            "  //<![CDATA[\n" +
            "  (function(){\n" +
            "    var a = function() {try{return !!window.addEventListener} catch(e) {return !1} },\n" +
            "    b = function(b, c) {a() ? document.addEventListener(\"DOMContentLoaded\", b, c) : document.attachEvent(\"onreadystatechange\", b)};\n" +
            "    b(function(){\n" +
            "      var a = document.getElementById('cf-content');a.style.display = 'block';\n" +
            "      setTimeout(function(){\n" +
            "        var s,t,o,p,b,r,e,a,k,i,n,g,f, ZTcQxyg={\"CbqAfZNNrNXm\":+((!+[]+!![]+!![]+!![]+!![]+!![]+!![]+!![]+[])+(!+[]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![])+(+[])+(!+[]+!![]+!![]))/+((+!![]+[])+(!+[]+!![]+!![]+!![]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![]+!![])+(+[])+(!+[]+!![]+!![])+(+[]))};\n" +
            "        t = document.createElement('div');\n" +
            "        t.innerHTML=\"<a href='/'>x</a>\";\n" +
            "        t = t.firstChild.href;r = t.match(/https?:\\/\\//)[0];\n" +
            "        t = t.substr(r.length); t = t.substr(0,t.length-1);\n" +
            "        a = document.getElementById('jschl-answer');\n" +
            "        f = document.getElementById('challenge-form');\n" +
            "        ;ZTcQxyg.CbqAfZNNrNXm+=+((!+[]+!![]+!![]+!![]+!![]+!![]+!![]+!![]+!![]+[])+(!+[]+!![]+!![]+!![])+(+!![])+(+[])+(!+[]+!![]+!![]+!![]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![]+!![]+!![]+!![])+(+!![]))/+((+!![]+[])+(!+[]+!![]+!![]+!![]+!![]+!![]+!![]+!![]+!![])+(+[])+(!+[]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![]+!![]+!![]+!![]+!![])+(+!![])+(!+[]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![]));ZTcQxyg.CbqAfZNNrNXm+=+((!+[]+!![]+!![]+!![]+!![]+!![]+!![]+!![]+[])+(!+[]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![])+(+[])+(!+[]+!![]+!![]))/+((!+[]+!![]+!![]+!![]+!![]+!![]+[])+(!+[]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![]+!![])+(!+[]+!![]+!![]+!![]+!![]+!![])+(+[])+(!+[]+!![]+!![])+(!+[]+!![]+!![]+!![])+(!+[]+!![]+!![])+(!+[]+!![]+!![]+!![]));a.value = +ZTcQxyg.CbqAfZNNrNXm.toFixed(10) + t.length; '; 121'\n" +
            "        f.action += location.hash;\n" +
            "        f.submit();\n" +
            "      }, 4000);\n" +
            "    }, false);\n" +
            "  })();\n" +
            "  //]]>\n" +
            "</script>\n" +
            "\n" +
            "</head>\n" +
            "<body>\n" +
            "<div>\n" +
            "<div style=\"position: relative; height: 130px;\">\n" +
            "<div style=\"max-width: 635px; margin: 0 auto; z-index: 1000; text-align: center;\n" +
            "                position: relative; margin-top: 150px; width: 100%\">\n" +
            "<img id=\"imgLogo\" alt=\"jadopado\" style=\"display: inline-block;\" src=\"data:image/gif;charset=binary;base64,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\"><br />\n" +
            "</div>\n" +
            "</div>\n" +
            "<div>\n" +
            "<div style=\"text-align: center\">\n" +
            "Please wait 5 seconds...<br />Make sure to enable cookies and javascript.<br />This site does not work with \"Mini browsers\" (e.g. UC mini, Opera mini...)\n" +
            "</div>\n" +
            "<div style=\"margin: 0 auto; visibility: hidden\">\n" +
            "<div class=\"cf-browser-verification cf-im-under-attack\">\n" +
            "  <noscript><h1 data-translate=\"turn_on_js\" style=\"color:#bd2426;\">Please turn JavaScript on and reload the page.</h1></noscript>\n" +
            "  <div id=\"cf-content\" style=\"display:none\">\n" +
            "    \n" +
            "    <div>\n" +
            "      <div class=\"bubbles\"></div>\n" +
            "      <div class=\"bubbles\"></div>\n" +
            "      <div class=\"bubbles\"></div>\n" +
            "    </div>\n" +
            "    <h1><span data-translate=\"checking_browser\">Checking your browser before accessing</span> kissmanga.com.</h1>\n" +
            "    \n" +
            "    <p data-translate=\"process_is_automatic\">This process is automatic. Your browser will redirect to your requested content shortly.</p>\n" +
            "    <p data-translate=\"allow_5_secs\">Please allow up to 5 seconds&hellip;</p>\n" +
            "  </div>\n" +
            "   \n" +
            "  <form id=\"challenge-form\" action=\"/cdn-cgi/l/chk_jschl\" method=\"get\">\n" +
            "    <input type=\"hidden\" name=\"jschl_vc\" value=\"fa90f5c225517302ea18090bff5a006c\"/>\n" +
            "    <input type=\"hidden\" name=\"pass\" value=\"1526482604.807-NxFVk11wwk\"/>\n" +
            "    <input type=\"hidden\" id=\"jschl-answer\" name=\"jschl_answer\"/>\n" +
            "  </form>\n" +
            "</div>\n" +
            "\n" +
            "</div>\n" +
            "</div>\n" +
            "<div style=\"padding: 20px;\">\n" +
            "</div>\n" +
            "</div>\n" +
            "</body>\n" +
            "</html>\n";
    private Context context;

    public static String getFirstMatch(Pattern p, String source) {
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    @Test
    public void testCFOr() throws Exception {

        final Pattern OPERATION_PATTERN = Pattern.compile("setTimeout\\(function\\(\\)\\{\\s+(var .,.,.,.[\\s\\S]+?a\\.value = .+?)\r?\n", Pattern.DOTALL);
        final Pattern PASS_PATTERN = Pattern.compile("name=\"pass\" value=\"(.+?)\"", Pattern.DOTALL);
        final Pattern CHALLENGE_PATTERN = Pattern.compile("name=\"jschl_vc\" value=\"(\\w+)\"", Pattern.DOTALL);
        String domain = "kissmanga.com";
        String rawOperation = getFirstMatch(OPERATION_PATTERN, content);
        String challenge = getFirstMatch(CHALLENGE_PATTERN, content);
        String challengePass = getFirstMatch(PASS_PATTERN, content);

        String operation = rawOperation.replaceAll("a\\.value = (.+ \\+ t\\.length).+", "$1").replaceAll("\\s{3,}[a-z](?: = |\\.).+", "");
        String js = operation.replaceAll("t.length", "" + domain.length()).replaceAll("\n", "");
        Duktape duktape = Duktape.create();
        String result = "";
        try {
            result = duktape.evaluate(js).toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            duktape.close();
        }


        String answer = result + domain.length();

    }

    @Test
    public void testCF() throws Exception {
        final Pattern OPERATION_PATTERN = Pattern.compile("setTimeout\\(function\\(\\)\\{\\s+(var (?:\\w,)+f.+?\\r?\\n[\\s\\S]+?a\\.value =.+?)\\r?\\n", Pattern.DOTALL);
        final Pattern PASS_PATTERN = Pattern.compile("name=\"pass\" value=\"(.+?)\"", Pattern.DOTALL);
        final Pattern CHALLENGE_PATTERN = Pattern.compile("name=\"jschl_vc\" value=\"([^\"]+)\"", Pattern.DOTALL);

        String domain = "kissmanga.com";
        String rawOperation = getFirstMatch(OPERATION_PATTERN, content);
        String challenge = getFirstMatch(CHALLENGE_PATTERN, content);
        String challengePass = getFirstMatch(PASS_PATTERN, content);

        if (rawOperation == null || challengePass == null || challenge == null) {
            Log.e("CFI", "couldn't resolve over cloudflare");
            return; // returning null here is not a good idea since it could stop a download ~xtj-9182
        }

        String js = rawOperation.replaceAll("a\\.value = (.+ \\+ t\\.length).+", "$1")
                .replaceAll("\\s{3,}[a-z](?: = |\\.).+", "")
                .replaceAll("t.length", "" + domain.length())
                .replaceAll("\n", "");
        Duktape duktape = Duktape.create();
        String result = "";
        try {
            result = duktape.evaluate(js).toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            duktape.close();
        }

        String url = new HttpUrl.Builder().scheme("http").host(domain)
                .addPathSegments("cdn-cgi/l/chk_jschl")
                .addEncodedQueryParameter("jschl_vc", challenge)
                .addEncodedQueryParameter("pass", challengePass)
                .addEncodedQueryParameter("jschl_answer", result)
                .build().toString();


    }

}
