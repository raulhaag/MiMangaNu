package ar.rulosoft.navegadores;

import android.util.Log;

import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

public class RefererInterceptor implements Interceptor {

    private String reference;

    public RefererInterceptor(String reference) {
        this.reference = reference;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response;
        try {
            //try to set referer, if it contains special characters this will fail
            response = chain.proceed(chain.request().newBuilder()
                    .header("Referer", reference)
                    .build());
            //Log.d("RefererIn: ", "ref: " + reference);
        } catch (IllegalArgumentException e) {
            //referer contained special characters so set no referer
            response = chain.proceed(chain.request().newBuilder()
                    .header("Referer", "")
                    .build());
            //Log.d("RefererIn: ", "ref: " + "");
        }
        return response;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
