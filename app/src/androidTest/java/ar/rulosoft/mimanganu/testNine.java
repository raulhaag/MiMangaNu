package ar.rulosoft.mimanganu;

import android.provider.Settings;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navigator;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.ByteString;

/**
 * Created by Raul on 01/04/2017.
 */
@LargeTest
public class testNine {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    private Manga manga;
    private Chapter chapter;

    private static String bodyToString(final Request request){
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    @Test
    public void testImages() throws Exception {
        Navigator nav = Navigator.navigator;
        nav.addHeader("Accept-Language", "es-AR,es;q=0.8,en-US;q=0.5,en;q=0.3");
        nav.addHeader("Accept-Encoding", "deflate");
        String boundary = Navigator.getNewBoundary();
        nav.addHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
        nav.addHeader("X-Requested-With", "XMLHttpRequest");
        MultipartBody.Builder mBodyBuilder = new MultipartBody.Builder(boundary).setType(MultipartBody.FORM);
        mBodyBuilder.addFormDataPart("artist","");
        mBodyBuilder.addFormDataPart("categoryID[1]","1");
        mBodyBuilder.addFormDataPart("sortBy","0");
        mBodyBuilder.addFormDataPart("sortOrder","0");
        mBodyBuilder.addFormDataPart("searchType","advance");
        mBodyBuilder.addFormDataPart("pageNumber","1");
        mBodyBuilder.addFormDataPart("searchTerm","");
        mBodyBuilder.addFormDataPart("searchByLetter","");
        for(int i = 0; i < 25; i++){
            mBodyBuilder.addFormDataPart("categoryID["+i+"]","0");
        }
        for(int i = 0; i < 10; i++){
            mBodyBuilder.addFormDataPart("typeID["+i+"]","0");
        }
        for(int i = 0; i < 6; i++){
            mBodyBuilder.addFormDataPart("typeBookID["+i+"]","0");
        }
/*
        Request request = new Request.Builder()
                .url("http://mangapedia.fr/project_code/script/search.php")
                .method("POST", mBodyBuilder.build())
                .build();
        Log.e("Body", bodyToString(request));//*/
        //RequestBody body = RequestBody
        //        .create(MediaType.parse("multipart/form-data; boundary"),mBodyBuilder.build());

        String data = nav.post("http://mangapedia.fr/project_code/script/search.php",
                mBodyBuilder.build());
        Log.e("Aaaaaaaaaaaa", data);
    }

}
