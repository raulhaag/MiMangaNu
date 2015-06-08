package ar.rulosoft.navegadores;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * @author Raul
 */
public class Navegador {

    HttpURLConnection conn;
    String modo = "GET";
    private HashMap<String, String> encabezados = new HashMap<String, String>();
    private HashMap<String, String> parametros = new HashMap<String, String>();
    private String rawPost = "";
    private String last_cookie = "";
    private String lastHeaders = "";

    public Navegador() {
    }


    public String get(String web) throws Exception {
        return this.get(web, 5000);
    }

    public String get(String web, int timeOut) throws Exception {
        URL url = new URL(web);
        conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(timeOut);
        conn.setReadTimeout(5000);
        addHeader("Accept-Encoding", "gzip,deflate");
        setHeaders();
        if (getLast_cookie() != null && getLast_cookie().length() > 2) {
            conn.setRequestProperty("Cookie", getLast_cookie());
        }
        // conn.connect();
        boolean redirect = false;
        // 3xx es redi
        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) {
                redirect = true;
            }
        }

        setLast_cookie(conn.getHeaderField("Set-Cookie"));
        setLastHeaders(conn.getHeaderFields().toString());

        if (redirect) {

            String newUrl = conn.getHeaderField("Location");

            conn = (HttpURLConnection) new URL(newUrl).openConnection();
            conn.setRequestProperty("Cookie", getLast_cookie());
            setHeaders();
        }

        if (conn.getResponseCode() == 200) {
            BufferedReader buff = getBufferedReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = buff.readLine()) != null) {
                sb.append(line);
            }
            buff.close();
            conn.disconnect();
            return sb.toString();
        } else {
            return "";
        }

    }

    public String get(String ip, String path, String host) throws Exception {
        URL url = new URL("http://" + ip + path);
        conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(5000);
        addHeader("Accept-Encoding", "gzip,deflate");
        addHeader("Host", host);
        setHeaders();
        if (getLast_cookie() != null && getLast_cookie().length() > 2) {
            conn.setRequestProperty("Cookie", getLast_cookie());
        }
        // conn.connect();
        boolean redirect = false;

        // 3xx es redi
        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) {
                redirect = true;
            }
        }

        setLast_cookie(conn.getHeaderField("Set-Cookie"));
        setLastHeaders(conn.getHeaderFields().toString());

        if (redirect) {

            String newUrl = conn.getHeaderField("Location");

            conn = (HttpURLConnection) new URL(newUrl).openConnection();
            conn.setRequestProperty("Cookie", getLast_cookie());
            setHeaders();

        }

        if (conn.getResponseCode() == 200) {
            BufferedReader buff = getBufferedReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = buff.readLine()) != null) {
                sb.append(line);
            }
            buff.close();
            conn.disconnect();
            return sb.toString();
        } else {
            return "";
        }
    }

    public String getRedirect(String web) throws Exception {

        String link = "";
        URL url = new URL(web);
        conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        setHeaders();
        // conn.connect();
        boolean redirect = false;

        // normally, 3xx is redirect
        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) {
                redirect = true;
            }
        }

        if (redirect) {
            link = conn.getHeaderField("Location");
        }

        return link;
    }

    public String post(String web) throws Exception {
        URL url = new URL(web);
        conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        setHeaders();
        if (getLast_cookie() != null && getLast_cookie().length() > 2) {
            conn.setRequestProperty("Cookie", getLast_cookie());
        }
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
        writer.write(getPostParams("UTF-8"));
        writer.close();

        setLast_cookie(conn.getHeaderField("Set-Cookie"));
        setLastHeaders(conn.getHeaderFields().toString());

        if (conn.getResponseCode() == 200) {
            BufferedReader buff = getBufferedReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = buff.readLine()) != null) {
                sb.append(line);
            }
            buff.close();
            conn.disconnect();
            return sb.toString();
        } else {
            return "";
        }
    }

    public String post(String ip, String path, String host) throws Exception {
        URL url = new URL("http://" + ip + path);
        conn = (HttpURLConnection) url.openConnection();
        conn.addRequestProperty("Host", host);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        setHeaders();
        if (getLast_cookie() != null && getLast_cookie().length() > 2) {
            conn.setRequestProperty("Cookie", getLast_cookie());
        }
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
        writer.write(getPostParams("UTF-8"));
        writer.close();

        setLast_cookie(conn.getHeaderField("Set-Cookie"));
        setLastHeaders(conn.getHeaderFields().toString());

        if (conn.getResponseCode() == 200) {
            BufferedReader buff = getBufferedReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = buff.readLine()) != null) {
                sb.append(line);
            }
            buff.close();
            conn.disconnect();
            return sb.toString();
        } else {
            return "";
        }
    }

    public void setHeaders() {
        for (Map.Entry<String, String> entry : encabezados.entrySet()) {
            conn.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    public String getPostParams(String encode) throws Exception {
        String query = "";
        if (rawPost != "")
            return rawPost;
        else {

            for (Map.Entry<String, String> entry : parametros.entrySet()) {
                if (query.equals("")) {
                    query = URLEncoder.encode(entry.getKey(), encode) + "=" + URLEncoder.encode(entry.getValue(), encode);
                } else {
                    query = query + "&" + URLEncoder.encode(entry.getKey(), encode) + "=" + URLEncoder.encode(entry.getValue(), encode);
                }
            }
            return query;
        }
    }

    public void addHeader(String key, String value) {
        encabezados.put(key, value);
    }

    public void setHeaders(HashMap<String, String> hd) {
        encabezados = hd;
    }

    public void addPost(String key, String value) {
        parametros.put(key, value);
    }

    public void addPosts(HashMap<String, String> posts) {
        parametros = posts;
    }

    private BufferedReader getBufferedReader() throws Exception {
        if ("gzip".equals(conn.getContentEncoding())) {
            return new BufferedReader(new InputStreamReader(new GZIPInputStream(conn.getInputStream())));
        } else {
            return new BufferedReader(new InputStreamReader(conn.getInputStream()));
        }
    }

    public HashMap<String, String> getFormParams(String url) throws Exception {
        String source = this.get(url);
        HashMap<String, String> ParametrosForm = new HashMap<String, String>();
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
        String source = inSource;
        HashMap<String, String> ParametrosForm = new HashMap<String, String>();
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

    public boolean isOnline(String web) throws Exception {
        boolean status = false;
        try {
            URL url = new URL(web);
            conn = (HttpURLConnection) url.openConnection();
            setHeaders();
            conn.connect();
            int code = conn.getResponseCode();
            if (code == 200)
                status = true;
            conn.disconnect();
        } catch (UnknownHostException e) {
            return false;
        }
        return status;
    }

    public String getLast_cookie() {
        return last_cookie;
    }

    public void setLast_cookie(String last_cookie) {
        this.last_cookie = last_cookie;
    }

    public String getRawPost() {
        return rawPost;
    }

    public void setRawPost(String rawPost) {
        this.rawPost = rawPost;
    }

    public String getLastHeaders() {
        return lastHeaders;
    }

    public void setLastHeaders(String lastHeaders) {
        this.lastHeaders = lastHeaders;
    }
}
