package ar.rulosoft.navegadores;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryInterceptor implements Interceptor {


    public RetryInterceptor() {
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        int retry = Navigator.connectionRetry;
        Request request = chain.request();
        Response response = chain.proceed(request);
        int tryCount = 0;
        while (tryCount <= retry &&
                (response == null || (!response.isSuccessful() && !response.isRedirect())) &&
                !request.url().host().contains("github") && response.code() != 404) { // don't retry app updates and 404 errors
            String url = request.url().toString();
            Request newRequest = request.newBuilder().url(url).build();
            tryCount++;
            try {
                Log.w("Retry ", "retry (" + (response != null ? response.code() : "null") + ")");
                Thread.sleep(1000 * tryCount);
                if (response != null && (response.code() == 429 || response.code() == 400)) {
                    Thread.sleep(30000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            response = chain.proceed(newRequest);
        }
        return response;
    }
}
