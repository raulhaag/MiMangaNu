package ar.rulosoft.navegadores;

import android.content.Context;
import android.util.Log;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.CookieJar;
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
    private static CookieJar cookieJar;
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

    public static CookieJar getCookieJar() {
        return cookieJar;
    }

    public void setCookieJar(CookieJar cookieJar) {
        httpClient = new OkHttpClientConnectionChecker.Builder()
                //.addInterceptor(new UserAgentInterceptor(USER_AGENT))
                .addInterceptor(new CFInterceptor())
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .build();
        Navigator.cookieJar = cookieJar;
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
            Log.e("Nav", "response unsuccessful: " + response.code() + " " + response.message() + " web: " + web);
            response.body().close();
            return "";
        }
    }

    public String getWithTimeout(String web) throws Exception {
        return this.getWithTimeout(web, "", connectionTimeout, writeTimeout, readTimeout);
    }

    public String getWithTimeout(String web, String referer) throws Exception {
        return this.getWithTimeout(web, referer, connectionTimeout, writeTimeout, readTimeout);
    }

    private String getWithTimeout(String web, String referer, int connectionTimeout, int writeTimeout, int readTimeout) throws Exception {
        // copy will share the connection pool with httpclient
        // NEVER create new okhttp clients that aren't sharing the same connection pool
        // see: https://github.com/square/okhttp/issues/2636
        OkHttpClient copy = httpClient.newBuilder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();
        if (!referer.isEmpty()) {
            addHeader("Referer", referer);
        }

        Response response = copy.newCall(new Request.Builder().url(web).headers(getHeaders()).build()).execute();
        int i = 0;
        int timeout = 250;
        while (!response.isSuccessful()) {
            Log.i("Nav", "source is empty, waiting for " + timeout + " ms before retrying ...");
            i++;
            Thread.sleep(timeout);
            response = copy.newCall(new Request.Builder().url(web).headers(getHeaders()).build()).execute();
            if (i < 3)
                timeout += 250;
            else
                timeout += 500;
            if (i == 5) {
                Log.i("Nav", "couldn't get a source from " + web + " :(");
                break;
            }
        }
        if (response.isSuccessful()) {
            if(timeout > 250)
                Log.i("Nav", "timeout of " + timeout + " ms worked got a source");
            return formatResponseBody(response.body());
        }
        else {
            Log.e("Nav", "response unsuccessful: " + response.code() + " " + response.message() + " web: " + web);
            response.body().close();
            return "";
        }
    }

    public InputStream getStream(String web) throws Exception {
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
            return response.body().byteStream();
        } else {
            Log.e("Nav", "response unsuccessful: " + response.code() + " " + response.message() + " web: " + web);
            response.body().close();
            throw new Exception("Can't get stream");
        }
    }

    public String getAndReturnResponseCodeOnFailure(String web) throws Exception {
        return this.getAndReturnResponseCodeOnFailure(web, connectionTimeout, writeTimeout, readTimeout);
    }

    private String getAndReturnResponseCodeOnFailure(String web, int connectionTimeout, int writeTimeout, int readTimeout) throws Exception {
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
            String responseCode = "" + response.code();
            Log.e("Nav", "response unsuccessful: " + responseCode + " " + response.message() + " web: " + web);
            response.body().close();
            return responseCode;
        }
    }

    public String get(String web, String referer, Interceptor interceptor) throws Exception {
        OkHttpClient copy = httpClient.newBuilder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();
        addHeader("Referer", referer);
        Response response = copy.newCall(new Request.Builder().url(web).headers(getHeaders()).build()).execute();
        if (response.isSuccessful()) {
            return formatResponseBody(response.body());
        } else {
            Log.e("Nav", "response unsuccessful: " + response.code() + " " + response.message() + " web: " + web);
            response.body().close();
            return "";
        }
    }

    public String get(String web, Interceptor interceptor) throws Exception {
        OkHttpClient copy = httpClient.newBuilder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();
        addHeader("Content-Encoding", "deflate");
        addHeader("Accept-Encoding", "deflate");
        Response response = copy.newCall(new Request.Builder().url(web).headers(getHeaders()).build()).execute();
        if (response.isSuccessful()) {
            return formatResponseBody(response.body());
        } else {
            Log.e("Nav", "response unsuccessful: " + response.code() + " " + response.message() + " web: " + web);
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
            Log.e("Nav", "response unsuccessful: " + response.code() + " " + response.message() + " web: " + web);
            response.body().close();
            return "";
        }
    }

    @Deprecated
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
            Log.e("Nav", "response unsuccessful: " + response.code() + " " + response.message());
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
            Log.e("Nav", "response unsuccessful: " + response.code() + " " + response.message() + " web: " + web);
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
            Log.e("Nav", "response unsuccessful: " + response.code() + " " + response.message() + " web: " + web);
            response.body().close();
            return "";
        }
    }

    @Deprecated
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
            Log.e("Nav", "response unsuccessful: " + response.code() + " " + response.message());
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

