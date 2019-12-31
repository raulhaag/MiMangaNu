package ar.rulosoft.mimanganu.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by jtx on 23.08.2016.
 */
public class NotificationDeleteIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Util.n > 0)
            Util.n--;
        //Util.getInstance().toast(context, "n: " + Util.n, 1);
        Log.i("NDIR", "n: " + Util.n);
    }
}
