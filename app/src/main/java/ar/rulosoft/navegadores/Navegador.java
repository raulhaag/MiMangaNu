package ar.rulosoft.navegadores;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Raul, nulldev, xtj-9182
 */
public class Navegador {
    public static int TIME_OUT = 5000;
    OkHttpClient httpClient;
    UserAgentInterceptor userAgentInterceptor;
    private HashMap<String, String> parametros = new HashMap<>();

    public Navegador() throws Exception {
        httpClient = new OkHttpClientConnectionChecker();
        userAgentInterceptor = new UserAgentInterceptor("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:42.0) Gecko/20100101 Firefox/42.0");
        httpClient.networkInterceptors().add(userAgentInterceptor);
    }

    public String get(String web) throws Exception {
        return this.get(web, TIME_OUT);
    }

    public String get(String web, int timeOut) throws Exception {
        httpClient.setConnectTimeout(timeOut, TimeUnit.SECONDS);
        httpClient.setReadTimeout(timeOut, TimeUnit.SECONDS);

        Response response = httpClient.newCall(new Request.Builder().url(web).build()).execute();

        if (response.isSuccessful()) {
            return formatResponseBody(response.body());
        } else {
            response.body().close();
            return "";
        }
    }

    public String get(String ip, String path, String host) throws Exception {
        httpClient.setConnectTimeout(TIME_OUT, TimeUnit.SECONDS);
        httpClient.setReadTimeout(TIME_OUT, TimeUnit.SECONDS);

        Request request = new Request.Builder()
                .url("http://" + ip + path)
                .addHeader("Host", host)
                .build();
        Response response = httpClient.newCall(request).execute();

        if (response.isSuccessful()) {
            return formatResponseBody(response.body());
        } else {
            response.body().close();
            return "";
        }
    }

    public String post(String web) throws Exception {
        httpClient.setConnectTimeout(TIME_OUT, TimeUnit.SECONDS);
        httpClient.setReadTimeout(TIME_OUT, TimeUnit.SECONDS);

        Request request = new Request.Builder()
                .url(web)
                .method("POST", getPostParams())
                .build();
        Response response = httpClient.newCall(request).execute();

        if (response.isSuccessful()) {
            return formatResponseBody(response.body());
        } else {
            response.body().close();
            return "";
        }
    }

    public String post(String ip, String path, String host) throws Exception {
        httpClient.setConnectTimeout(TIME_OUT, TimeUnit.SECONDS);
        httpClient.setReadTimeout(TIME_OUT, TimeUnit.SECONDS);
        Request request = new Request.Builder()
                .url("http://" + ip + path)
                .addHeader("Host", host)
                .method("POST", getPostParams())
                .build();
        Response response = httpClient.newCall(request).execute();

        if (response.isSuccessful()) {
            return formatResponseBody(response.body());
        } else {
            response.body().close();
            return "";
        }
    }

    public String formatResponseBody(ResponseBody body) throws IOException {
        return body.string().replaceAll("(\n|\r)","");
    }

    public RequestBody getPostParams() throws Exception {
        FormEncodingBuilder builder = new FormEncodingBuilder();
        for (Map.Entry<String, String> entry : parametros.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    public void addPost(String key, String value) {
        parametros.put(key, value);
    }

    public HashMap<String, String> getFormParams(String url) throws Exception {
        String source = this.get(url);
        HashMap<String, String> ParametrosForm = new HashMap<>();
        Pattern p = Pattern.compile("<[F|f]orm[^$]*</[F|f]orm>");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Pattern p1 = Pattern.compile("<[I|i]nput type=[^ ]* name=\"([^\"]*)\" value=\"([^\"]*)\">");
            Matcher m1 = p1.matcher(m.group());
            while (m1.find()) {
                ParametrosForm.put(m1.group(1), m1.group(2));
            }
        }
        return ParametrosForm;
    }

    public HashMap<String, String> getFormParamsFromSource(String inSource) throws Exception {
        HashMap<String, String> ParametrosForm = new HashMap<>();
        Pattern p = Pattern.compile("<[F|f]orm[^$]*</[F|f]orm>");
        Matcher m = p.matcher(inSource);
        while (m.find()) {
            Pattern p1 = Pattern.compile("<[I|i]nput type=[^ ]* name=\"([^\"]*)\" value=\"([^\"]*)\">");
            Matcher m1 = p1.matcher(m.group());
            while (m1.find()) {
                ParametrosForm.put(m1.group(1), m1.group(2));
            }
        }
        return ParametrosForm;
    }

    public boolean isOnline(String web) throws Exception {
        return httpClient.newCall(new Request.Builder().url(web).build()).execute().isSuccessful();
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }
}

/**
 * Adds user agent to any client the interceptor is attached to.
 */
class UserAgentInterceptor implements Interceptor {

    private String userAgent;

    public UserAgentInterceptor(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(chain.request().newBuilder()
                .header("User-Agent", userAgent)
                .build());
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}

