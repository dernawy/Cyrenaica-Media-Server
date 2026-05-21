package com.cyrenaica.cyrenaicaserver.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.cyrenaica.cyrenaicaserver.server.ServerHomeActivity;
import com.cyrenaica.cyrenaicaserver.server.adaptars.AllServersListHolder;
import com.cyrenaica.cyrenaicaserver.server.adaptars.ServerAttachedDevicesListHolder;

import java.util.Objects;


public class ConnectionReceiver extends BroadcastReceiver {

    private static final String TAG = "CONNECTION_RECEIVER";

    public static boolean isWifiConnected;
    public static boolean isSocketsConnected;
    public static boolean isSecurityCheck;
    public static boolean serverConnected;
    public static boolean isPinged;
    public static String soc_response;
    public static boolean soc_setup_ok;
    public static String soc_message;
    public static String network_type;

    private static String SOCKETS_CONNECT_RESPONSE;

    @Override
    public void onReceive(Context context, Intent intent) {

        final PendingResult pendingResult = goAsync();

        try {

            StringBuilder sb = new StringBuilder();
            sb.append("Action: ").append(intent.getAction()).append("\n");
            sb.append("URI: ").append(intent.toUri(Intent.URI_INTENT_SCHEME)).append("\n");

            String log = sb.toString();
            //Log.d(TAG, log);

            if (Objects.equals(intent.getAction(), "STATE_CHANGE")) {
                Log.d(TAG, "STATE_CHANGE");
            }

            if (Objects.equals(intent.getAction(), "CONNECTIVITY_CHANGE")) {
                Log.d(TAG, "CONNECTIVITY_CHANGE");
            }

            if (Objects.equals(intent.getAction(), "SOCKETS_CONNECTION_SERVICE")) {

                isPinged = false;

                ConnectivityManager cm    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                if(activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI){
                    network_type = "WIFI";
                }
                else if(activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE){
                    network_type = "MOBILE";
                }

                /* init network & sockets connection flags */
                isWifiConnected           = activeNetwork != null && activeNetwork.isConnected();
                isSocketsConnected        = intent.getBooleanExtra("isConnected", false); // is connection between server and app was made for the first time and connected successfully
                isSecurityCheck           = intent.getBooleanExtra("isSecurityCheck", false); // is the request contain the app id check
                serverConnected           = intent.getBooleanExtra("serverConnected", false); // is server connected with sending message every 5 seconds in timer SocketBackgroundService.startInternetConnectionCheckTimer()
                isPinged                  = intent.getBooleanExtra("isPinged", false); // is ping sent to server and got pong
                soc_message               = intent.getStringExtra("soc_message");

                AllServersListHolder.IfSocketsConnected(isSocketsConnected);
                ServerHomeActivity.IfSocketsConnected(isSocketsConnected);


                Log.d(TAG, "isSocketsConnected: " + isSocketsConnected);
            }
            else if(Objects.equals(intent.getAction(), "SERVERS_SOCKETS_CONNECTED")){
                soc_setup_ok              = intent.getBooleanExtra("SETUP_OK", false); // is connection between server and app was made for the first time and connected successfully
                soc_response              = intent.getStringExtra("RESPONSE");

                AllServersListHolder.IfConnectionSecured(soc_response);
                ServerHomeActivity.IfConnectionSecured(soc_response);

                Log.d(TAG, "CONNECTIVITY: " + soc_response);

            }
            else if(Objects.equals(intent.getAction(), "DEVICES_SOCKETS_CONNECTED")){

                soc_setup_ok              = intent.getBooleanExtra("SETUP_OK", false); // is connection between server and app was made for the first time and connected successfully
                soc_response              = intent.getStringExtra("RESPONSE");


            }
            else
            {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                isWifiConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                if (isWifiConnected) {

                    try {
                        Toast.makeText(context, "Network is connected", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(context, "Network is changed or reconnected", Toast.LENGTH_LONG).show();
                }
            }


        }

        finally {
            // Must call finish() so the BroadcastReceiver can be recycled

            pendingResult.finish();
        }
    }

    public static boolean getIsWifiConnected(){
        return isWifiConnected;
    }

    public static boolean getServerConnected(){
        return serverConnected;
    }

    public static boolean getIsConnected(){
        return isSocketsConnected;
    }

    public static void setSocketsResponse(String response){
        SOCKETS_CONNECT_RESPONSE = response;
    }
    @NonNull
    public static String getSocketsResponse(){
        return SOCKETS_CONNECT_RESPONSE;
    }


}
