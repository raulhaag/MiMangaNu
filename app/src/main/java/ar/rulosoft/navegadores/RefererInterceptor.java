package ar.rulosoft.navegadores;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class RefererInterceptor implements Interceptor {

    private String referer;

    public RefererInterceptor(String referer) {
        this.referer = referer;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response;
        try {
            //try to set referer, if it contains special characters this will fail
            response = chain.proceed(chain.request().newBuilder()
                    .header("Referer", referer)
                    .build());
            //Log.d("RefererIn", "ref: " + referer);
        } catch (IllegalArgumentException e) {
            //referer contained special characters so set no referer
            response = chain.proceed(chain.request().newBuilder()
                    .header("Referer", "")
                    .build());
            //Log.d("RefererIn", "ref: " + "");
        }
        return response;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }
}
