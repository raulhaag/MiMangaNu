package ar.rulosoft.navegadores;

import android.util.Log;

import com.squareup.duktape.Duktape;

import java.io.IOException;
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

    private final static Pattern OPERATION_PATTERN = Pattern.compile("setTimeout\\(function\\(\\)\\{\\s+(var .,.,.,.[\\s\\S]+?a\\.value = .+?)\r?\n");
    private final static Pattern PASS_PATTERN = Pattern.compile("name=\"pass\" value=\"(.+?)\"");
    private final static Pattern CHALLENGE_PATTERN = Pattern.compile("name=\"jschl_vc\" value=\"(\\w+)\"");

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
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Request request = response.request();
        String content = response.body().string();
        String domain = request.url().host().trim();
        String rawOperation = getFirstMatch(OPERATION_PATTERN, content);
        String challenge = getFirstMatch(CHALLENGE_PATTERN, content);
        String challengePass = getFirstMatch(PASS_PATTERN, content);

        if (rawOperation == null || challengePass == null || challenge == null) {
            Log.e("CFI", "couldn't resolve over cloudflare");
            return response; // returning null here is not a good idea since it could stop a download ~xtj-9182
        }

        String operation = rawOperation.replaceAll("a\\.value =(.+?) \\+ .+?;.*", "$1").replaceAll("\\s{3,}[a-z](?: = |\\.).+", "");
        String js = operation.replace("\n", "");
        Duktape duktape = Duktape.create();
        int result = 0;
        try {
            String res = (String) duktape.evaluate(js + ".toString()");
            result = Integer.parseInt(res);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            duktape.close();
        }

        String answer = String.valueOf(result + domain.length());

        String url = new HttpUrl.Builder().scheme("http").host(domain)
                .addPathSegment("cdn-cgi").addPathSegment("l").addPathSegment("chk_jschl")
                .addEncodedQueryParameter("jschl_vc", challenge)
                .addEncodedQueryParameter("pass", challengePass)
                .addEncodedQueryParameter("jschl_answer", answer)
                .build().toString();

        Request request1 = new Request.Builder()
                .url(url)
                .header("User-Agent", Navigator.USER_AGENT)
                .header("Referer", request.url().toString())
                .build();

        response.body().close();
        response = chain.proceed(request1);//generate the cookie
        response.body().close();
        response = chain.proceed(request.newBuilder().build());
        return response;
    }
}
