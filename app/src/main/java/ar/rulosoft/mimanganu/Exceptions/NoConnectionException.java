package ar.rulosoft.mimanganu.Exceptions;

import android.content.Context;

import ar.rulosoft.mimanganu.R;

/**
 * Created by Raul on 14/06/2016.
 */
public class NoConnectionException extends Exception {
    public NoConnectionException(Context mContext) {
        super(mContext.getString(R.string.no_internet_connection));
    }
}
