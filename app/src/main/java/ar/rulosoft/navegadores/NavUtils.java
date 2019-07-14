package ar.rulosoft.navegadores;

import java.io.IOException;

import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.RealResponseBody;
import okio.GzipSource;
import okio.Okio;
import okio.Source;

public class NavUtils {
    /*
        get a copy of response with a independent body without affecting the original body also
        unzip if necessary
     */
    public static Response getResponseCopy(Response response) throws IOException {
        ResponseBody copy = response.peekBody(Long.MAX_VALUE);
        String contentEncoding = response.headers().get("Content-Encoding");
        if (contentEncoding != null && contentEncoding.equals("gzip")) {

            Source source = new GzipSource(copy.source());
            Response responseCopy = response.newBuilder().headers(response.headers().newBuilder().build())
                    .body(new RealResponseBody(response.body().contentType().toString(),
                            response.body().contentLength(), Okio.buffer(source)))
                    .build();
            return responseCopy;
        } else {
            Response responseCopy = response.newBuilder().headers(response.headers().newBuilder().build())
                    .body(copy)
                    .build();
            return responseCopy;
        }

    }
}
