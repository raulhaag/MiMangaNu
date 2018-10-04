package ar.rulosoft.navegadores;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.android.gms.security.ProviderInstaller;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.utils.Util;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Raul, nulldev, xtj-9182
 */
public class Navigator {
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36";
    public static int connectionTimeout = 10;
    public static int writeTimeout = 10;
    public static int readTimeout = 30;
    public static int connectionRetry = 10;
    private static CookieJar cookieJar;
    private static Navigator instance;
    private OkHttpClient httpClient;
    private ArrayList<Parameter> parameters = new ArrayList<>();
    private ArrayList<Parameter> headers = new ArrayList<>();

    private Navigator(Context context) throws Exception {

        if (Util.getInstance().isGPServicesAvailable(context)) {
            ProviderInstaller.installIfNeeded(context); //dislike but needed on old devices to have access to new ssl ca/ etc
        }

        if (httpClient == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            writeTimeout = Integer.parseInt(prefs.getString("write_timeout", "10"));
            connectionRetry = Integer.parseInt(prefs.getString("connection_retry", "10"));
            readTimeout = Integer.parseInt(prefs.getString("read_timeout", "30"));
            connectionTimeout = Integer.parseInt(prefs.getString("connection_timeout", "10"));
            cookieJar = new CookieFilter(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
            initClient(new CookieFilter(new SetCookieCache(), new SharedPrefsCookiePersistor(context)), context);
        }
    }

    public static synchronized Navigator getInstance() {
        if (Navigator.instance == null) {
            throw new NullPointerException("Navigator has no instance with a valid context.");
        }
        return Navigator.instance;
    }

    public static synchronized Navigator initialiseInstance(Context context) throws Exception {
        Navigator.instance = new Navigator(context);
        return Navigator.instance;
    }

    public static HashMap<String, String> getFormParamsFromSource(String inSource) {
        HashMap<String, String> ParametrosForm = new HashMap<>();
        Pattern p = Pattern.compile("<[F|f]orm([\\s|\\S]+?)</[F|f]orm>", Pattern.DOTALL);
        Matcher m = p.matcher(inSource);
        while (m.find()) {
            Pattern p1 = Pattern.compile("<[I|i]nput type=[^ ]* name=['|\"]([^\"']*)['|\"] value=['|\"]([^'\"]*)['|\"]", Pattern.DOTALL);
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

    private void initClient(CookieJar cookieJar, Context context) throws KeyManagementException, NoSuchAlgorithmException {
        TrustManager[] trustManagers = getTrustManagers(context);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, null);
        SSLSocketFactory socketFactory = null;
        if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
            socketFactory = new Tls12SocketFactory(sslContext.getSocketFactory());
        } else {
            socketFactory = sslContext.getSocketFactory();
        }
        httpClient = new OkHttpClientConnectionChecker.Builder()
                .addInterceptor(new RetryInterceptor())// the interceptors list appear to be a lifo
                .addInterceptor(new CFInterceptor())
                .sslSocketFactory(socketFactory, (X509TrustManager) trustManagers[0])
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                //.dns(new MmNDNS())//
                .build();
        Navigator.cookieJar = cookieJar;
    }

    public static String getNewBoundary() {
        String boundary = "---------------------------";
        boundary += Math.floor(Math.random() * 32768);
        boundary += Math.floor(Math.random() * 32768);
        boundary += Math.floor(Math.random() * 32768);
        return boundary;
    }

    private static KeyStore getSystemCAKeyStore() throws
            KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {

        KeyStore keyStore = KeyStore.getInstance("AndroidCAStore");
        keyStore.load(null, null);
        return keyStore;
    }

    public void clearCookieJar(Context context) {
        cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
    }

    public synchronized String get(String web) throws Exception {
        return this.get(web, connectionTimeout, writeTimeout, readTimeout);
    }

    public synchronized String get(String web, int connectionTimeout, int writeTimeout, int readTimeout) throws Exception {
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
            return response.body().string();
        } else {
            Log.e("Nav", "response unsuccessful: " + response.code() + " " + response.message() + " web: " + web);
            response.body().close();
            return "";
        }
    }

    public String getRedirectWeb(String web) throws Exception {
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
            return response.request().url().toString();
        } else {
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
            return response.body().string();
        } else {
            String responseCode = "" + response.code();
            Log.e("Nav", "response unsuccessful: " + responseCode + " " + response.message() + " web: " + web);
            response.body().close();
            return responseCode;
        }
    }

    public synchronized String get(String web, String referer, Interceptor interceptor) throws Exception {
        OkHttpClient copy = httpClient.newBuilder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();
        addHeader("Referer", referer);
        Response response = copy.newCall(new Request.Builder().url(web).headers(getHeaders()).build()).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            Log.e("Nav", "response unsuccessful: " + response.code() + " " + response.message() + " web: " + web);
            response.body().close();
            return "";
        }
    }

    public synchronized String get(String web, Interceptor interceptor) throws Exception {
        OkHttpClient copy = httpClient.newBuilder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();
        Response response = copy.newCall(new Request.Builder().url(web).headers(getHeaders()).build()).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            Log.e("Nav", "response unsuccessful: " + response.code() + " " + response.message() + " web: " + web);
            response.body().close();
            return "";
        }
    }

    public synchronized String get(String web, String referer) throws Exception {
        OkHttpClient copy = httpClient.newBuilder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();
        addHeader("Referer", referer);
        Response response = copy.newCall(new Request.Builder().url(web).headers(getHeaders()).build()).execute();

        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            Log.e("Nav", "response unsuccessful: " + response.code() + " " + response.message() + " web: " + web);
            response.body().close();
            return "";
        }
    }

    public synchronized String post(String web) throws Exception {
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
            return response.body().string();
        } else {
            Log.e("Nav", "response unsuccessful: " + response.code() + " " + response.message() + " web: " + web);
            response.body().close();
            return "";
        }
    }

    public synchronized String post(String web, RequestBody formParams) throws Exception {
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
            return response.body().string();
        } else {
            Log.e("Nav", "response unsuccessful: " + response.code() + " " + response.message() + " web: " + web);
            response.body().close();
            return "";
        }
    }

    private RequestBody getPostParams() {
        FormBody.Builder builder = new FormBody.Builder();
        for (Parameter p : parameters) {
            builder.add(p.getKey(), p.getValue());
        }
        return builder.build();
    }

    public void addPost(String key, String value) {
        parameters.add(new Parameter(key, value));
    }

    private Headers getHeaders() {
        Headers.Builder builder = new Headers.Builder();
        builder.add("User-Agent", USER_AGENT)
                .add("Accept-Language", "en")
                .add("Connection", "keep-alive")
                .add("Accept", "text/html,application/xhtml+xml,application/xml");

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

    //Explained on https://developer.android.com/training/articles/security-ssl.html
    private TrustManager[] getTrustManagers(Context context) {
        try {
            //get system certs
            KeyStore keyStore = getSystemCAKeyStore();
            KeyStore keyStore_n = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore_n.load(null, null);
            Enumeration<String> aliases = keyStore.aliases();
            //creating a copy because original can't be modified
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) keyStore.getCertificate(alias);
                keyStore_n.setCertificateEntry(alias, cert);
            }
            //then add my key ;-P
            keyStore_n.setCertificateEntry("mangahereco", loadCertificateFromRaw(R.raw.mangahereco, context));
            keyStore_n.setCertificateEntry("mangafoxme", loadCertificateFromRaw(R.raw.mangafoxme, context));
            keyStore_n.setCertificateEntry("mangaherecoImages", loadCertificateFromRaw(R.raw.mangaherecoimages, context));
            keyStore_n.setCertificateEntry("mangatowncom", loadCertificateFromRaw(R.raw.mangatowncom, context));
            keyStore_n.setCertificateEntry("mangatownsecureimage", loadCertificateFromRaw(R.raw.mangatownsecureimages, context));
            keyStore_n.setCertificateEntry("tumangaonline", loadCertificateFromRaw(R.raw.tumangaonline, context));
            keyStore_n.setCertificateEntry("tmo_me", loadCertificateFromRaw(R.raw.tumangaonline_me, context));

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore_n);
            return tmf.getTrustManagers();
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Certificate loadCertificateFromRaw(int rawId, Context context) {
        Certificate certificate = null;
        InputStream certInput = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            certInput = new BufferedInputStream(context.getResources().openRawResource(rawId));
            certificate = cf.generateCertificate(certInput);
        } catch (CertificateException e) {
            Log.e("MMN Certificates", "Fail to load certificates");
        } finally {
            if (certInput != null) {
                try {
                    certInput.close();
                } catch (IOException e) {
                    //dion't mind
                }
            }
        }
        return certificate;
    }
}
