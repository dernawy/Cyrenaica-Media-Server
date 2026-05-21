package com.cyrenaica.cyrenaicaserver.tools;


import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.cyrenaica.cyrenaicaserver.R;

public class Connectivity extends Activity {

    private static final String TAG = "TOOLS";

    private final int MY_PERMISSIONS_ACCESS_READ_PHONE_STATE = 1;
    public static boolean SCREEN_CONNECTED_FLAG;
    public static boolean SCREEN_REMOTE_FLAG;
    public static String SCREEN_CONNECTED_MSG;
    public static String SCREEN_CONNECTION_TYPE;
    public static String SCREEN_NETWORK_TYPE;
    public static String SCREEN_APP_ACCESS_TYPE_MSG;

    /**
     * Get the network info
     * @param context
     * @return
     */
    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    /**
     * Check if there is any connectivity to a Wifi network
     * @param context
     * @return
     */
    public static boolean isConnectedWifi(Context context) {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    /**
     * Check if there is any connectivity to a mobile network
     * @param context
     * @return
     */
    public static boolean isConnectedMobile(Context context) {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Check if there is fast connectivity
     * @param context
     * @return
     */
    public static boolean isConnectedFast(Context context) {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected() && Connectivity.isConnectionFast(info.getType(), info.getSubtype()));
    }

    /**
     * Check if the connection is fast
     * @param type
     * @param subType
     * @return
     */
    public static boolean isConnectionFast(int type, int subType) {

        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
                /*
                 * Above API level 7, make sure to set android:targetSdkVersion
                 * to appropriate level to use these
                 */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public static String getNetworkClass(Context context) {

        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if (ContextCompat.checkSelfPermission(context, permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.

            int networkType = mTelephonyManager.getNetworkType();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return "4G";
                case TelephonyManager.NETWORK_TYPE_NR:
                    return "5G";
                case TelephonyManager.NETWORK_TYPE_GSM:
                case TelephonyManager.NETWORK_TYPE_IWLAN:
                case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return "Unknown";
            }
        }

        return "error";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_READ_PHONE_STATE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(Connectivity.this, "permission granted", Toast.LENGTH_SHORT).show();
                    //Code here
                }
                else
                {
                    Toast.makeText(Connectivity.this, "permission not granted", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }
    }

    public static void appConnectionOnscreenHandle(Context context){

        if(Connectivity.isConnected(context)){

            SCREEN_CONNECTED_FLAG = true;
            SCREEN_CONNECTED_MSG  = "Connected";

            if(Connectivity.isConnectedWifi(context)){

                SCREEN_CONNECTION_TYPE     = "Wifi";
                SCREEN_NETWORK_TYPE        = "2.4Ghz";
                //SCREEN_REMOTE_FLAG         = false;

                int dd = (R.string.dashboard_connectivity_network_access_1_text);
                SCREEN_APP_ACCESS_TYPE_MSG = String.valueOf(dd);

                //Log.i(TAG,"URL-LOC: "+local_domain);

            }

            if(Connectivity.isConnectedMobile(context)){

                SCREEN_CONNECTION_TYPE     = "Mobile";
                SCREEN_APP_ACCESS_TYPE_MSG = "Remote access to server";
                SCREEN_REMOTE_FLAG         = true;
                SCREEN_NETWORK_TYPE        = Connectivity.getNetworkClass(context);
                //Log.i(TAG,"URL-REMO: "+ remote_domain);
            }
        }
        else
        {
            SCREEN_CONNECTED_FLAG      = false;
            SCREEN_CONNECTION_TYPE     = "N/A";
            SCREEN_NETWORK_TYPE        = "N/A";
            SCREEN_APP_ACCESS_TYPE_MSG = "N/A";
            SCREEN_CONNECTED_MSG       = "Disconnected";
        }

        //CURRENT_DOMAIN = getStringPref(context, "CURRENT_DOMAIN_NAME");

        Log.i(TAG,"CONNECTION STATUS [" + SCREEN_CONNECTED_MSG + "] TYPE [" + SCREEN_CONNECTION_TYPE + "]");
    }

    public static boolean isScreenConnectedFlag(){
        return SCREEN_CONNECTED_FLAG;
    }

    public static String getConnectionType(){
        return SCREEN_CONNECTION_TYPE;
    }

    public static String getNetworkType(){
        return SCREEN_NETWORK_TYPE;
    }

    public static String getConnectionIndicationText(){
        return SCREEN_CONNECTED_MSG;
    }


}
