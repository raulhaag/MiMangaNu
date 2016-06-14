package ar.rulosoft.mimanganu.Exceptions;

import android.content.Context;

import ar.rulosoft.mimanganu.R;

/**
 * Created by Raul on 14/06/2016.
 */
public class NoWifiException extends Exception {
    public NoWifiException(Context mContext) {
        super(mContext.getString(R.string.no_wifi_connection));
    }
}
