package ar.rulosoft.mimanganu.utils;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;

/*
    Base from Yakiv Mospan
    from http://stackoverflow.com/questions/32242384/getallnetworkinfo-is-deprecated-how-to-use-getallnetworks-to-check-network
 */

public class NetworkUtilsAndReciever extends BroadcastReceiver {

    public static int state = -1; //-1 not checked or changed, 0 no connection wifi, 1 no connection general, 2 connect
    public static boolean ONLY_WIFI;

    public static boolean isConnected(@NonNull Context context) throws Exception {
        boolean result;
        switch (state) {
            case -1:
                if (ONLY_WIFI) {
                    result = isWifiConnected(context);
                    if (result) {
                        state = 2;
                    } else {
                        state = 0;
                    }
                    return result;
                } else {
                    result = _isConnected(context);
                    if (result) {
                        state = 2;
                    } else {
                        state = 1;
                    }
                    return result;
                }
            case 0:
                throw new Exception("No WIFI connection");
            case 1:
                throw new Exception("No internet connection");
            case 2:
                return true;
            default:
                return true;
        }
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
        state = -1;
    }
}