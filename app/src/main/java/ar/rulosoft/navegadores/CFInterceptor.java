package ar.rulosoft.navegadores;

import android.util.Log;

import com.squareup.duktape.Duktape;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Resolver based on habrahabr.ru/post/258101/.
 */
public class CFInterceptor implements Interceptor {

    private final static Pattern OPERATION_PATTERN = Pattern.compile("setTimeout\\(function\\(\\)\\{\\s+(var .,.,.,.[\\s\\S]+?a\\.value = .+?)\r?\n", Pattern.DOTALL);
    private final static Pattern PASS_PATTERN = Pattern.compile("name=\"pass\" value=\"(.+?)\"", Pattern.DOTALL);
    private final static Pattern CHALLENGE_PATTERN = Pattern.compile("name=\"jschl_vc\" value=\"(\\w+)\"", Pattern.DOTALL);

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

    public Response resolveOverCF(Chain chain, Response response) throws IOException {
        Request request = response.request();
        String domain = request.url().host().trim();
        String content = response.body().string();
        response.body().close();
        String rawOperation = getFirstMatch(OPERATION_PATTERN, content);
        String challenge = getFirstMatch(CHALLENGE_PATTERN, content);
        String challengePass = getFirstMatch(PASS_PATTERN, content);
        if (rawOperation == null || challengePass == null || challenge == null) {
            Log.e("CFI", "couldn't resolve over cloudflare");
            return response; // returning null here is not a good idea since it could stop a download ~xtj-9182
        }

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

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String url = new HttpUrl.Builder().scheme(request.isHttps() ? "https" : "http").host(domain)
                .addPathSegments("cdn-cgi/l/chk_jschl")
                .addEncodedQueryParameter("jschl_vc", challenge)
                .addEncodedQueryParameter("pass", challengePass)
                .addEncodedQueryParameter("jschl_answer", result)
                .build().toString();

        Request request1 = new Request.Builder()
                .url(url)
                .header("User-Agent", Navigator.USER_AGENT)
                .header("Referer", request.url().toString())
                .header("Accept-Language", "en, eu")
                .header("Connection", "keep-alive")
                .header("Accept", "text/html,application/xhtml+xml,application/xml")
                .build();
        return chain.proceed(request1);//generate the cookie;
    }
}
