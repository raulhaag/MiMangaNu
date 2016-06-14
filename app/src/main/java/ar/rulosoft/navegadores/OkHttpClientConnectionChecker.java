package ar.rulosoft.navegadores;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;

import ar.rulosoft.mimanganu.utils.NetworkUtilsAndReciever;

/**
 * Created by Raul on 14/06/2016.
 */
public class OkHttpClientConnectionChecker extends OkHttpClient {
    public OkHttpClientConnectionChecker(Context context)throws Exception{
        super();
        if(!NetworkUtilsAndReciever.isConnected(context)){
            if(NetworkUtilsAndReciever.ONLY_WIFI){
                throw new Exception("No WIFI connection");
            }else{
                throw new Exception("No internet connection");
            }
        }
    }
}
