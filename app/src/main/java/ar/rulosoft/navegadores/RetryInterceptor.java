package ar.rulosoft.navegadores;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryInterceptor implements Interceptor {


    public RetryInterceptor() {}

    @Override
    public Response intercept(Chain chain) throws IOException {
        int retry = Navigator.connectionRetry;
        Request request = chain.request();
        Response response = chain.proceed(request);
        int tryCount = 0;
        while (tryCount <= retry && (response == null || !response.isSuccessful())) {
            String url = request.url().toString();
            Request newRequest = request.newBuilder().url(url).build();
            tryCount++;
            try {
                Thread.sleep(500 * tryCount);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            response = chain.proceed(newRequest);
        }
        return response;
    }
}
