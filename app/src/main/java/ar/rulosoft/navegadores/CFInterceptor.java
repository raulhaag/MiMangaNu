package ar.rulosoft.navegadores;

import android.util.Base64;
import android.util.Log;

import com.squareup.duktape.Duktape;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Resolver based on habrahabr.ru/post/258101/.
 * and https://github.com/VeNoMouS/cloudflare-scrape-js2py
 */
public class CFInterceptor implements Interceptor {

    private final static Pattern OPERATION_PATTERN = Pattern.compile("setTimeout\\(function\\(\\)\\{\\s+(var .,.,.,.[\\s\\S]+?a\\.value = .+?;)", Pattern.DOTALL);
    private final static Pattern PASS_PATTERN = Pattern.compile("name=\"pass\" value=\"(.+?)\"", Pattern.DOTALL);
    private final static Pattern CHALLENGE_PATTERN = Pattern.compile("name=\"jschl_vc\" value=\"(\\w+)\"", Pattern.DOTALL);
    private final static Pattern EXTRA_STRING_ADDED_PATTERN = Pattern.compile("<input type=\"hidden\" name=\"r\" value=\"([^\"]*)");
    private final static Pattern REPLACE_VALUE = Pattern.compile("visibility:hidden;\" id=\".+\">([^<]+)<");
    private final static Pattern FORM_ACTION = Pattern.compile("action=\"([^\"]+)");


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
        String r = URLEncoder.encode(getFirstMatch(EXTRA_STRING_ADDED_PATTERN, content), "UTF-8");
        String rv = getFirstMatch(REPLACE_VALUE, content);
        String formAction = getFirstMatch(FORM_ACTION, content);

        if (rv == null) {
            rv = "";
        }

        if (rawOperation == null || challengePass == null || challenge == null || formAction == null) {
            Log.e("CFI", "couldn't resolve over cloudflare");
            return response; // returning null here is not a good idea since it could stop a download ~xtj-9182
        }

        String js = rawOperation
                .replaceAll("e\\s*=\\s*function[\\s\\S]+?\\};", "")
                .replaceAll("atob", "Atob.atob")
                .replaceAll("e\\(\"(.+?)\"\\)", "Atob\\.atob(\"$1\")")
                .replaceAll("a\\.value = (.+ \\+ t\\.length.+?);.+", "$1")
                .replaceAll("\\s{3,}[a-z](?: = |\\.).+", "");
        Duktape duktape = Duktape.create();
        js = js.replaceAll("[\n\\']", "");
        // js = js.replace(";", ";\n"); only on debug for a easy read

        String newJs = String.format("var t = \"%s\";\n" +
                "            var g = String.fromCharCode;\n" +
                "            function italics (str) {{ return '<i>' + this + '</i>'; }};\n" +
                "            var document = {getElementById: function(p) { return {innerHTML:\"%s\"};}};\n" +
                "            %s", domain, rv, js);


        js = Jsonfxxx.getInstance().jsonfxxx(newJs);
        Atob atob = new Atob() {
            @Override
            public String atob(String str) {
                return new String(Base64.decode(str, Base64.DEFAULT));
            }
        };
        duktape.set("Atob", Atob.class, atob); //not sure if needed after replacement method
        js = js.replaceAll("a.value = (\\([^;]+;)", "$1").replaceAll(";\\s*;", ";");
        String result = "";
        try {
            result = duktape.evaluate(js).toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            duktape.close();
        }

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        RequestBody body = new FormBody.Builder()
                .add("r", r)
                .add("jschl_vc", challenge)
                .add("pass", challengePass)
                .add("jschl_answer", result)
                .build();

        String url = (request.isHttps() ? "https://" : "http://") + domain + formAction;

        Request request1 = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", Navigator.USER_AGENT)
                .addHeader("Accept-Language", "es-AR,es;q=0.8,en-US;q=0.5,en;q=0.3")
                .addHeader("Connection", "keep-alive")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("Referer", request.url().toString())
                .method("POST", body)
                .build();

        Response rps = chain.proceed(request1);
        return rps;//generate the cookie;
    }

    interface Atob {
        String atob(String str);
    }

    public static class Jsonfxxx {
        private static Jsonfxxx instance;
        Map<String, String> mapping = new HashMap<>();
        Map<String, String> simple = new HashMap<>();

        private Jsonfxxx() {
            mapping.put("a", "(false+\"\")[1]");
            mapping.put("b", "([][\"entries\"]()+\"\")[2]");
            mapping.put("c", "([][\"fill\"]+\"\")[3]");
            mapping.put("d", "(undefined+\"\")[2]");
            mapping.put("e", "(true+\"\")[3]");
            mapping.put("f", "(false+\"\")[0]");
            mapping.put("g", "(false+[0]+String)[20]");
            mapping.put("h", "(+(101))[\"to\"+String[\"name\"]](21)[1]");
            mapping.put("i", "([false]+undefined)[10]");
            mapping.put("j", "([][\"entries\"]()+\"\")[3]");
            mapping.put("k", "(+(20))[\"to\"+String[\"name\"]](21)");
            mapping.put("l", "(false+\"\")[2]");
            mapping.put("m", "(Number+\"\")[11]");
            mapping.put("n", "(undefined+\"\")[1]");
            mapping.put("o", "(true+[][\"fill\"])[10]");
            mapping.put("p", "(+(211))[\"to\"+String[\"name\"]](31)[1]");
            mapping.put("q", "(+(212))[\"to\"+String[\"name\"]](31)[1]");
            mapping.put("r", "(true+\"\")[1]");
            mapping.put("s", "(false+\"\")[3]");
            mapping.put("t", "(true+\"\")[0]");
            mapping.put("u", "(undefined+\"\")[0]");
            mapping.put("v", "(+(31))[\"to\"+String[\"name\"]](32)");
            mapping.put("w", "(+(32))[\"to\"+String[\"name\"]](33)");
            mapping.put("x", "(+(101))[\"to\"+String[\"name\"]](34)[1]");
            mapping.put("y", "(NaN+[Infinity])[10]");
            mapping.put("z", "(+(35))[\"to\"+String[\"name\"]](36)");
            mapping.put("A", "(+[]+Array)[10]");
            mapping.put("B", "(+[]+Boolean)[10]");
            mapping.put("C", "Function(\"return escape\")()((\"\")[\"italics\"]())[2]");
            mapping.put("D", "Function(\"return escape\")()([][\"fill\"])[\"slice\"](\"-1\")");
            mapping.put("E", "(RegExp+\"\")[12]");
            mapping.put("F", "(+[]+Function)[10]");
            mapping.put("G", "(false+Function(\"return Date\")()())[30]");
            mapping.put("I", "(Infinity+\"\")[0]");
            mapping.put("M", "(true+Function(\"return Date\")()())[30]");
            mapping.put("N", "(NaN+\"\")[0]");
            mapping.put("O", "(NaN+Function(\"return{}\")())[11]");
            mapping.put("R", "(+[]+RegExp)[10]");
            mapping.put("S", "(+[]+String)[10]");
            mapping.put("T", "(NaN+Function(\"return Date\")()())[30]");
            mapping.put("U", "(NaN+Function(\"return{}\")()[\"to\"+String[\"name\"]][\"call\"]())[11]");
            mapping.put(" ", "(NaN+[][\"fill\"])[11]");
            mapping.put("\"", "(\"\")[\"fontcolor\"]()[12]");
            mapping.put("%", "Function(\"return escape\")()([][\"fill\"])[21]");
            mapping.put("&", "(\"\")[\"link\"](0+\")[10]");
            mapping.put("(", "(undefined+[][\"fill\"])[22]");
            mapping.put(")", "([0]+false+[][\"fill\"])[20]");
            mapping.put("+", "(+(+!+[]+(!+[]+[])[!+[]+!+[]+!+[]]+[+!+[]]+[+[]]+[+[]])+[])[2]");
            mapping.put(",", "([][\"slice\"][\"call\"](false+\"\")+\"\")[1]");
            mapping.put("-", "(+(.+[0000000001])+\"\")[2]");
            mapping.put(".", "(+(+!+[]+[+!+[]]+(!![]+[])[!+[]+!+[]+!+[]]+[!+[]+!+[]]+[+[]])+[])[+!+[]]");
            mapping.put("/", "(false+[0])[\"italics\"]()[10]");
            mapping.put(":", "(RegExp()+\"\")[3]");
            mapping.put(";", "(\"\")[\"link\"](\")[14]");
            mapping.put("<", "(\"\")[\"italics\"]()[0]");
            mapping.put("=", "(\"\")[\"fontcolor\"]()[11]");
            mapping.put(">", "(\"\")[\"italics\"]()[2]");
            mapping.put("?", "(RegExp()+\"\")[2]");
            mapping.put("[", "([][\"entries\"]()+\"\")[0]");
            mapping.put("]", "([][\"entries\"]()+\"\")[22]");
            mapping.put("{", "(true+[][\"fill\"])[20]");
            mapping.put("}", "([][\"fill\"]+\"\")[\"slice\"](\"-1\")");

            simple.put("false", "![]");
            simple.put("true", "!![]");
            simple.put("undefined", "[][[]]");
            simple.put("NaN", "+[![]]");
            simple.put("infinity", "+(+!+[]+(!+[]+[])[!+[]+!+[]+!+[]]+[+!+[]]+[+[]]+[+[]]+[+[]])");
        }

        public static Jsonfxxx getInstance() {
            if (instance == null)
                instance = new Jsonfxxx();
            return instance;
        }

        String jsonfxxx(String js) {
            List<String> list = new ArrayList<String>(mapping.keySet());
            Collections.sort(list, Collections.reverseOrder());
            for (String key : list) {
                js = js.replace(mapping.get(key), "\"" + key + "\"");
            }
            list = new ArrayList<String>(simple.keySet());
            //  js = js.replaceAll("\"*([a..Z]*)\"*\\+\"*([a..Z]*)\"*", "$1$2");
         /*   Collections.sort(list, Collections.reverseOrder());
            for (String key : list) {
                js = js.replace(simple.get(key), key);
            }*/
            return js;
        }
    }


}