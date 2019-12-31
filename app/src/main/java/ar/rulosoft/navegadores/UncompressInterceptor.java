package ar.rulosoft.navegadores;

import org.brotli.dec.BrotliInputStream;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.RealResponseBody;
import okio.GzipSource;
import okio.Okio;


public class UncompressInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        return unzip(response);
    }

    private Response unzip(final Response response) throws IOException {
        if (response.body() == null) {
            return response;
        }

        String contentEncoding = response.headers().get("Content-Encoding");
        if (contentEncoding != null && contentEncoding.equals("gzip")) {
            Long contentLength = response.body().contentLength();
            GzipSource responseBody = new GzipSource(response.body().source());
            Headers strippedHeaders = response.headers().newBuilder().build();
            return response.newBuilder().headers(strippedHeaders)
                    .body(new RealResponseBody(response.body().contentType().toString(), contentLength, Okio.buffer(responseBody)))
                    .build();
        } else if (contentEncoding != null && contentEncoding.equals("br")) {
            return response.newBuilder()
                    .removeHeader("Content-Encoding")
                    .body(ResponseBody.create(response.body().contentType(), -1,
                            Okio.buffer(Okio.source(new BrotliInputStream(response.body().source().inputStream())))))
                    .build();
        } else {
            return response;
        }
    }
}


