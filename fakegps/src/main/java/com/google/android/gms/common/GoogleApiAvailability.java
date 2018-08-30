package com.google.android.gms.common;

import android.content.Context;

public class GoogleApiAvailability {
    public static  GoogleApiAvailability getInstance(){
        return  new GoogleApiAvailability();
    }

    public int isGooglePlayServicesAvailable(Context context){
        return 0;
    }


}
