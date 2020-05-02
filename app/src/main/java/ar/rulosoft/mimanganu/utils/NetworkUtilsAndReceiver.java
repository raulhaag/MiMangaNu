package ar.rulosoft.mimanganu.utils;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import ar.rulosoft.mimanganu.Exceptions.NoConnectionException;
import ar.rulosoft.mimanganu.Exceptions.NoWifiException;
import ar.rulosoft.mimanganu.MainActivity;
import ar.rulosoft.mimanganu.MainFragment;
import ar.rulosoft.mimanganu.services.AutomaticUpdateTask;

/*
    Base from Yakiv Mospan
    from http://stackoverflow.com/questions/32242384/getallnetworkinfo-is-deprecated-how-to-use-getallnetworks-to-check-network
 */

public class NetworkUtilsAndReceiver extends BroadcastReceiver {

    public static ConnectionStatus connectionStatus = ConnectionStatus.UNCHECKED; //-1 not checked or changed, 0 no connection wifi, 1 no connection general, 2 connect
    public static boolean ONLY_WIFI;

    public static boolean isConnectedNonDestructive(@NonNull Context context) {
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(context);
        if (!pm.getBoolean("disable_internet_detection", false)) {
            boolean result;
            switch (connectionStatus) {
                case UNCHECKED:
                    if (ONLY_WIFI) {
                        result = isWifiConnected(context);
                        if (result) {
                            connectionStatus = ConnectionStatus.CONNECTED;
                        } else {
                            connectionStatus = ConnectionStatus.NO_WIFI_CONNECTED;
                        }
                        return result;
                    } else {
                        result = _isConnected(context);
                        if (result) {
                            connectionStatus = ConnectionStatus.CONNECTED;
                        } else {
                            connectionStatus = ConnectionStatus.NO_INET_CONNECTED;
                        }
                        return result;
                    }
                case NO_WIFI_CONNECTED:
                    return false;
                case NO_INET_CONNECTED:
                    return false;
                case CONNECTED:
                    return true;
                default:
                    return true;
            }
        } else {
            return true;
        }
    }

    public static boolean isConnected(@NonNull Context context) throws Exception {
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(context);
        if (!pm.getBoolean("disable_internet_detection", false)) {
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
        } else {
            return true;
        }
    }

    public static ConnectionStatus getConnectionStatus(@NonNull Context context) {
        return getConnectionStatus(context, ONLY_WIFI);
    }

    public static ConnectionStatus getConnectionStatus(@NonNull Context context, boolean only_wifi) {
        boolean result;
        if (connectionStatus == ConnectionStatus.UNCHECKED) {
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
        try {
            Network[] networks = connMgr.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = connMgr.getNetworkInfo(mNetwork);
                if (networkInfo != null && networkInfo.getType() == type && networkInfo.isConnected()) {
                    return true;
                }
            }
        } catch (Exception e) {
            //ignore return false
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(context);
        connectionStatus = ConnectionStatus.UNCHECKED;

        if (!pm.getBoolean("disable_internet_detection", false)) {
            if (isWifiConnected(context) || isMobileConnected(context)) {
                Log.d("NUAR", "onRec Connected");
                MainActivity.isConnected = true;
            } else {
                Log.d("NUAR", "onRec Disconnected");
                MainActivity.isConnected = false;
            }
        } else {
            MainActivity.isConnected = true;
        }

        try {
            if (isConnected(context)) {
                long last_check = pm.getLong(MainFragment.LAST_CHECK, 0);
                long current_time = System.currentTimeMillis();
                long interval = Long.parseLong(pm.getString("update_interval", "0"));
                if (interval > 0) {
                    if (interval < current_time - last_check) {
                        new AutomaticUpdateTask(context, null, pm, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum ConnectionStatus {UNCHECKED, NO_INET_CONNECTED, NO_WIFI_CONNECTED, CONNECTED}

}