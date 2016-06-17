package ar.rulosoft.mimanganu.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import ar.rulosoft.mimanganu.MainActivity;

/**
 * Created by jtx on 11.06.2016.
 */
public class WifiStateChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiStateChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            int tmp = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN);
            checkChangedWifiState(tmp);
        } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo.DetailedState state =
                    ((NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)).getDetailedState();
            checkChangedNetworkState(state, context);
        }
    }

    public void checkChangedWifiState(int state) {
        if (state == WifiManager.WIFI_STATE_ENABLING) {
            //Log.d(TAG, "WIFI_STATE_ENABLING");
        } else if (state == WifiManager.WIFI_STATE_ENABLED) {
            Log.d(TAG, "WIFI_STATE_ENABLED");
            MainActivity.isConnectedToWifi = true;
        } else if (state == WifiManager.WIFI_STATE_DISABLING) {
            //Log.d(TAG, "WIFI_STATE_DISABLING");
        } else if (state == WifiManager.WIFI_STATE_DISABLED) {
            Log.d(TAG, "WIFI_STATE_DISABLED");
            MainActivity.isConnectedToWifi = false;
        }
    }

    private void checkChangedNetworkState(NetworkInfo.DetailedState state, Context context) {
        if (state == NetworkInfo.DetailedState.SCANNING) {
            //Log.d(TAG, "SCANNING");
        } else if (state == NetworkInfo.DetailedState.CONNECTING) {
            //Log.d(TAG, "CONNECTING");
        } else if (state == NetworkInfo.DetailedState.CONNECTED) {
            Log.d(TAG, "CONNECTED");
            //Util.getInstance().toast(context, "CONNECTED", 0);
            MainActivity.isConnectedToWifi = true;
        } else if (state == NetworkInfo.DetailedState.DISCONNECTING) {
            Log.d(TAG, "DISCONNECTING");
            MainActivity.isConnectedToWifi = false;
        } else if (state == NetworkInfo.DetailedState.DISCONNECTED) {
            Log.d(TAG, "DISCONNECTED");
            //Util.getInstance().toast(context, "DISCONNECTED", 0);
            MainActivity.isConnectedToWifi = false;
        } else if (state == NetworkInfo.DetailedState.FAILED) {
            Log.d(TAG, "FAILED");
        }
    }

}
