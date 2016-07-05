package ar.rulosoft.mimanganu.utils;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import ar.rulosoft.mimanganu.Exceptions.NoConnectionException;
import ar.rulosoft.mimanganu.Exceptions.NoWifiException;
import ar.rulosoft.mimanganu.MainActivity;
import ar.rulosoft.mimanganu.services.AlarmReceiver;

/*
    Base from Yakiv Mospan
    from http://stackoverflow.com/questions/32242384/getallnetworkinfo-is-deprecated-how-to-use-getallnetworks-to-check-network
 */

public class NetworkUtilsAndReciever extends BroadcastReceiver {

    public enum ConnectionStatus {UNCHECKED, NO_INET_CONNECTED, NO_WIFI_CONNECTED, CONNECTED}
    public static ConnectionStatus connectionStatus = ConnectionStatus.UNCHECKED; //-1 not checked or changed, 0 no connection wifi, 1 no connection general, 2 connect
    public static boolean ONLY_WIFI;

    public static boolean isConnected(@NonNull Context context) throws Exception {
        boolean result;
        switch (connectionStatus) {
            case UNCHECKED:
                if (ONLY_WIFI) {
                    result = isWifiConnected(context);
                    if (result) {
                        connectionStatus = ConnectionStatus.CONNECTED;
                    } else {
                        connectionStatus = ConnectionStatus.NO_WIFI_CONNECTED;
                        throw new NoWifiException(context);
                    }
                    return result;
                } else {
                    result = _isConnected(context);
                    if (result) {
                        connectionStatus = ConnectionStatus.CONNECTED;
                    } else {
                        connectionStatus = ConnectionStatus.NO_INET_CONNECTED;
                        throw new NoConnectionException(context);
                    }
                    return result;
                }
            case NO_WIFI_CONNECTED:
                throw new NoWifiException(context);
            case NO_INET_CONNECTED:
                throw new NoConnectionException(context);
            case CONNECTED:
                return true;
            default:
                return true;
        }
    }

    public static ConnectionStatus getConnectionStatus(@NonNull Context context){
            return getConnectionStatus(context,ONLY_WIFI);
    }

    public static ConnectionStatus getConnectionStatus(@NonNull Context context, boolean only_wifi){
        boolean result;
        if(connectionStatus == ConnectionStatus.UNCHECKED) {
            if (only_wifi) {
                result = isWifiConnected(context);
                if (result) {
                    connectionStatus = ConnectionStatus.CONNECTED;
                } else {
                    connectionStatus = ConnectionStatus.NO_WIFI_CONNECTED;
                }
            } else {
                result = _isConnected(context);
                if (result) {
                    connectionStatus = ConnectionStatus.CONNECTED;
                } else {
                    connectionStatus = ConnectionStatus.NO_INET_CONNECTED;
                }
            }
        }
        return connectionStatus;
    }

    public static void reset() {
        connectionStatus = ConnectionStatus.UNCHECKED;
    }

    public static boolean _isConnected(@NonNull Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean isWifiConnected(@NonNull Context context) {
        return isConnected(context, ConnectivityManager.TYPE_WIFI);
    }

    public static boolean isMobileConnected(@NonNull Context context) {
        return isConnected(context, ConnectivityManager.TYPE_MOBILE);
    }

    private static boolean isConnected(@NonNull Context context, int type) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(type);
            return networkInfo != null && networkInfo.isConnected();
        } else {
            return isConnected(connMgr, type);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static boolean isConnected(@NonNull ConnectivityManager connMgr, int type) {
        Network[] networks = connMgr.getAllNetworks();
        NetworkInfo networkInfo;
        for (Network mNetwork : networks) {
            networkInfo = connMgr.getNetworkInfo(mNetwork);
            if (networkInfo != null && networkInfo.getType() == type && networkInfo.isConnected()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        connectionStatus = ConnectionStatus.UNCHECKED;

        if (isWifiConnected(context) || isMobileConnected(context)) {
            Log.d("NUAR", "onRec Connected");
            MainActivity.isConnected = true;
        } else {
            Log.d("NUAR", "onRec Disconnected");
            MainActivity.isConnected = false;
        }

        try {
            if (isConnected(context)) {
                SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(context);
                long last_check = pm.getLong(AlarmReceiver.LAST_CHECK, 0);
                long current_time = System.currentTimeMillis();
                long interval = pm.getLong("update_interval", 0);
                if (interval > 0) {
                    if (interval < current_time - last_check) {
                        new AlarmReceiver().onReceive(context, intent);
                    }
                }
            }
        } catch (Exception ignore) {
        }
    }

}