package ar.rulosoft.navegadores;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.URLEncoder;

public class RefererInterceptor implements Interceptor {

    private String referer;

    public RefererInterceptor(String referer) {
        this.referer = referer;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(chain.request().newBuilder()
                .header("Referer", referer) //url encodes don't work with well on referers
                .build());
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }


}
