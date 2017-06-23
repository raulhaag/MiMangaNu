package ar.rulosoft.mimanganu.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ar.rulosoft.mimanganu.MainActivity;
import ar.rulosoft.mimanganu.MainFragment;
import ar.rulosoft.mimanganu.services.AutomaticUpdateTask;

/**
 * Created by jtx on 05.10.2016.
 */

public class CancelIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int mangaIdFromNotification = intent.getIntExtra("manga_id", -1);
        Log.i("CIR", "mangaID: " + mangaIdFromNotification);

        if (mangaIdFromNotification == -1) {
            MainActivity.isCancelled = true;
            Util.getInstance().cancelNotification(MainFragment.mNotifyID);
            Util.getInstance().cancelNotification(AutomaticUpdateTask.mNotifyID);
        }
    }
}
