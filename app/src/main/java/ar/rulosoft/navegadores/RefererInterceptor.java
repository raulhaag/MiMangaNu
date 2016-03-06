package ar.rulosoft.navegadores;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class RefererInterceptor implements Interceptor {

    private String reference;

    public RefererInterceptor(String reference) {
        this.reference = reference;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(chain.request().newBuilder()
                .header("Referer", reference)
                .build());
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
