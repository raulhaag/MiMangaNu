package ar.rulosoft.navegadores;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

public class OldNavigator {
    public static String getWhitURLConnection(String web, ArrayList<Parameter> parameters, int connectionTimeout, int readTimeout) throws Exception {
        HttpURLConnection conn;
        URL url = new URL(web);
        conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(connectionTimeout * 1000);
        conn.setReadTimeout(readTimeout * 1000);
        parameters.add(new Parameter("Accept-Encoding", "gzip,deflate"));
        for (Parameter entry : parameters) {
            conn.setRequestProperty(entry.getKey(), entry.getValue());
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

        if (redirect) {
            String newUrl = conn.getHeaderField("Location");
            conn = (HttpURLConnection) new URL(newUrl).openConnection();
            for (Parameter entry : parameters) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        if (conn.getResponseCode() == 200) {
            BufferedReader buff = null;
            if ("gzip".equals(conn.getContentEncoding())) {
                buff = new BufferedReader(new InputStreamReader(new GZIPInputStream(conn.getInputStream())));
            } else {
                buff = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            }
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = buff.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            buff.close();
            conn.disconnect();
            return sb.toString();
        } else {
            return "";
        }
    }

    public static String getRedirectURLConnection(String web, ArrayList<Parameter> parameters) throws Exception {
        HttpURLConnection conn;
        String link = "";
        URL url = new URL(web);
        conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setInstanceFollowRedirects(false);
        for (Parameter entry : parameters) {
            conn.setRequestProperty(entry.getKey(), entry.getValue());
        }
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


    public static InputStream getStreamURLConnection(String web) {
        URL urlConnect = null;
        try {
            urlConnect = new URL(web);
            HttpURLConnection conn = (HttpURLConnection) urlConnect.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(true);
            return conn.getInputStream();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}