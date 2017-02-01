package ar.rulosoft.navegadores;

import android.content.Context;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author Raul, nulldev, xtj-9182
 */
public class Navigator {
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0";
    public static int connectionTimeout = 10;
    public static int writeTimeout = 10;
    public static int readTimeout = 30;
    public static Navigator navigator;
    private static ClearableCookieJar cookieJar;
    private OkHttpClient httpClient;
    private ArrayList<Parameter> parameters = new ArrayList<>();
    private ArrayList<Parameter> headers = new ArrayList<>();

    public Navigator(Context context) throws Exception {
        if (httpClient == null) {
            cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
            httpClient = new OkHttpClientConnectionChecker.Builder()
                    //.addInterceptor(new UserAgentInterceptor(USER_AGENT))
                    .addInterceptor(new CFInterceptor())
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .cookieJar(cookieJar)
                    .build();
        }
    }

    public static HashMap<String, String> getFormParamsFromSource(String inSource) throws Exception {
        HashMap<String, String> ParametrosForm = new HashMap<>();
        Pattern p = Pattern.compile("<[F|f]orm([\\s|\\S]+?)</[F|f]orm>");
        Matcher m = p.matcher(inSource);
        while (m.find()) {
            Pattern p1 = Pattern.compile("<[I|i]nput type=[^ ]* name=['|\"]([^\"']*)['|\"] value=['|\"]([^'\"]*)['|\"]");
            Matcher m1 = p1.matcher(m.group());
            while (m1.find()) {
                ParametrosForm.put(m1.group(1), m1.group(2));
            }
        }
        return ParametrosForm;
    }

    public static ClearableCookieJar getCookieJar() {
        return cookieJar;
    }

    public String get(String web) throws Exception {
        return this.get(web, connectionTimeout, writeTimeout, readTimeout);
    }

    public String get(String web, int connectionTimeout, int writeTimeout, int readTimeout) throws Exception {
        // copy will share the connection pool with httpclient
        // NEVER create new okhttp clients that aren't sharing the same connection pool
        // see: https://github.com/square/okhttp/issues/2636
        OkHttpClient copy = httpClient.newBuilder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();

        Response response = copy.newCall(new Request.Builder().url(web).headers(getHeaders()).build()).execute();
        if (response.isSuccessful()) {
            return formatResponseBody(response.body());
        } else {
            response.body().close();
            return "";
        }
    }

    public String get(String web, String referer, Interceptor interceptor) throws Exception {
        OkHttpClient copy = httpClient.newBuilder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .addNetworkInterceptor(interceptor)
                .build();
        addHeader("Referer", referer);
        Response response = copy.newCall(new Request.Builder().url(web).headers(getHeaders()).build()).execute();
        if (response.isSuccessful()) {
            return formatResponseBody(response.body());
        } else {
            response.body().close();
            return "";
        }
    }

    public String get(String web, Interceptor interceptor) throws Exception {
        OkHttpClient copy = httpClient.newBuilder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .addNetworkInterceptor(interceptor)
                .build();
        addHeader("Content-Encoding", "deflate");
        addHeader("Accept-Encoding", "deflate");
        Response response = copy.newCall(new Request.Builder().url(web).headers(getHeaders()).build()).execute();
        if (response.isSuccessful()) {
            return formatResponseBody(response.body());
        } else {
            response.body().close();
            return "";
        }
    }

    public String get(String web, String referer) throws Exception {
        OkHttpClient copy = httpClient.newBuilder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();
        addHeader("Referer", referer);
        Response response = copy.newCall(new Request.Builder().url(web).headers(getHeaders()).build()).execute();

        if (response.isSuccessful()) {
            return formatResponseBody(response.body());
        } else {
            response.body().close();
            return "";
        }
    }

    @Deprecated
    public String get(String web, int timeout) throws Exception {
        OkHttpClient copy = httpClient.newBuilder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .build();

        Response response = copy.newCall(new Request.Builder().url(web).build()).execute();

        if (response.isSuccessful()) {
            return formatResponseBody(response.body());
        } else {
            response.body().close();
            return "";
        }
    }

    public String get(String ip, String path, String host) throws Exception {
        OkHttpClient copy = httpClient.newBuilder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();
        addHeader("Host", host);

        Request request = new Request.Builder()
                .url("http://" + ip + path)
                .headers(getHeaders())
                .build();
        Response response = copy.newCall(request).execute();

        if (response.isSuccessful()) {
            return formatResponseBody(response.body());
        } else {
            response.body().close();
            return "";
        }
    }

    public String post(String web) throws Exception {
        OkHttpClient copy = httpClient.newBuilder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(web)
                .headers(getHeaders())
                .method("POST", getPostParams())
                .build();
        Response response = copy.newCall(request).execute();

        if (response.isSuccessful()) {
            return formatResponseBody(response.body());
        } else {
            response.body().close();
            return "";
        }
    }

    public String post(String web, RequestBody formParams) throws Exception {
        OkHttpClient copy = httpClient.newBuilder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(web)
                .headers(getHeaders())
                .method("POST", formParams)
                .build();
        Response response = copy.newCall(request).execute();

        if (response.isSuccessful()) {
            return formatResponseBody(response.body());
        } else {
            response.body().close();
            return "";
        }
    }

    public String post(String ip, String path, String host) throws Exception {
        OkHttpClient copy = httpClient.newBuilder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();
        addHeader("Host", host);
        Request request = new Request.Builder()
                .url("http://" + ip + path)
                .headers(getHeaders())
                .method("POST", getPostParams())
                .build();
        Response response = copy.newCall(request).execute();

        if (response.isSuccessful()) {
            return formatResponseBody(response.body());
        } else {
            response.body().close();
            return "";
        }
    }

    public String formatResponseBody(ResponseBody responseBody) throws IOException {
        return responseBody.string().replaceAll("(\n|\r)", "");
    }

    private RequestBody getPostParams() throws Exception {
        FormBody.Builder builder = new FormBody.Builder();
        for (Parameter p : parameters) {
            builder.add(p.getKey(), p.getValue());
        }
        return builder.build();
    }

    public void addPost(String key, String value) {
        parameters.add(new Parameter(key, value));
    }

    public HashMap<String, String> getFormParams(String url) throws Exception {
        String source = this.get(url);
        HashMap<String, String> ParametrosForm = new HashMap<>();
        Pattern p = Pattern.compile("<[F|f]orm([\\s|\\S]+?)</[F|f]orm>");
        Matcher m = p.matcher(source);
        while (m.find()) {
            Pattern p1 = Pattern.compile("<[I|i]nput type=[^ ]* name=['|\"]([^\"']*)['|\"] value=['|\"]([^'\"]*)['|\"]");
            Matcher m1 = p1.matcher(m.group());
            while (m1.find()) {
                ParametrosForm.put(m1.group(1), m1.group(2));
            }
        }
        return ParametrosForm;
    }

    public Headers getHeaders() {
        Headers.Builder builder = new Headers.Builder();
        builder.add("User-Agent", USER_AGENT);//this is used all the time
        for (Parameter p : headers) {
            builder.add(p.getKey(), p.getValue());
        }
        headers.clear();//and those are volatile
        return builder.build();
    }

    public void addHeader(String key, String value) {
        headers.add(new Parameter(key, value));
    }


    public void flushParameter() {
        parameters = new ArrayList<>();
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public void dropAllCalls() {
        httpClient.dispatcher().cancelAll();
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

