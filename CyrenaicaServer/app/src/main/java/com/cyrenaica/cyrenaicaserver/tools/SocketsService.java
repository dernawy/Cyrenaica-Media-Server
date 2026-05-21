package com.cyrenaica.cyrenaicaserver.tools;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.cyrenaica.cyrenaicaserver.server.adaptars.AllServersListHolder;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import tech.gusavila92.websocketclient.WebSocketClient;

public class SocketsService extends  Service {

    static Timer timer ;
    private static final String TAG = "SocketsService";

    private static JSONObject jsonObject;
    JSONObject sockets;

    private static boolean isServiceRunning;

    private static Boolean isConnected = false;

    private static WebSocketClient client;

    private static String URL;

    private static String _domain;

    public static String _port;

    public static String _dir;

    public static String _type;

    Intent send_to_activity_intent;

    AllServersListHolder AllSHolder;

    private final IBinder socBinder = new ConnectSockets();

    public class ConnectSockets extends Binder {

        public SocketsService getService()  {
            return SocketsService.this;
        }
    }

    public SocketsService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");

        isServiceRunning = false;

        Thread backgroundThread = new Thread(socketsServiceTask);

        if(!isServiceRunning) {

            isServiceRunning = true;
            backgroundThread.start();
            Log.i(TAG, "SOCKET BACKGROUND SERVICE IS RUNNING: "+ backgroundThread.getState());
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG,"onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy");
        isServiceRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Log.i(TAG,"onStartCommand");
        return START_STICKY;
    }

    private final Runnable socketsServiceTask = new Runnable() {

        @Override
        public void run() {

            URI uri;
            URL = "ws://" + _domain + ":" + _port + "/" + _dir;

            Log.w(TAG, "URL: " + URL);

            try {
                // Connect to host
                uri = new URI(URL);
            }
            catch (URISyntaxException e) {
                e.printStackTrace();
                return;
            }

            client = new WebSocketClient(uri) {

                @Override
                public void onOpen() {

                    Log.w(TAG, "onOpen");

                    Map<String, String> params = new HashMap<>();
                    params.put("QUESTION", "OPEN_APP_SECURITY_CHECK");
                    sendMessage(params);

                    isConnected = true;
                    send_to_activity_intent = new Intent("SOCKETS_CONNECTION_SERVICE");
                    send_to_activity_intent.putExtra("isConnected", isConnected);
                    sendBroadcast(send_to_activity_intent);

                }

                @Override
                public void onTextReceived(String message) {

                    Log.w(TAG, "onTextReceived");
                    Log.d(TAG, "MESSAGE: " + message);

                    JSONObject response_object = null;
                    JSONObject main_object     = null;
                    boolean _setup_ok;
                    String _response;

                    try {
                        response_object = new JSONObject(message);
                        main_object     = response_object.getJSONObject("SOCKETS");

                        _setup_ok       = Boolean.parseBoolean(main_object.getString("SETUP_NEEDED"));
                        _response       = main_object.getString("RESPONSE");

                        if(_type.equals("SERVER")){
                            send_to_activity_intent = new Intent("SERVERS_SOCKETS_CONNECTED");
                        }

                        if(_type.equals("DEVICE")){
                            send_to_activity_intent = new Intent("DEVICES_SOCKETS_CONNECTED");
                        }

                        send_to_activity_intent.putExtra("SETUP_OK", _setup_ok);
                        send_to_activity_intent.putExtra("RESPONSE", _response);
                        sendBroadcast(send_to_activity_intent);
                    }
                    catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onBinaryReceived(byte[] data) {

                }

                @Override
                public void onPingReceived(byte[] data) {

                }

                @Override
                public void onPongReceived(byte[] data) {

                }

                @Override
                public void onException(Exception e) {

                }

                @Override
                public void onCloseReceived() {

                }
            };

            client.setConnectTimeout(5000);
            client.enableAutomaticReconnection(3000);
            client.connect();

        }
    };

    public static void startSockets(Intent serv, Context context, ConnectionReceiver receiver, String address, String port, String dir, String type /* SERVER or DEVICE */, String TAG){

        SocketsService.setDomain(address);
        SocketsService.setPort(port);
        SocketsService.setDir(dir);
        SocketsService.setType(type);
        context.startService(serv);

        Log.d(TAG, "ADDRESS [" + address + "] PORT [" + port + "] DIR [" + dir + "] TYPE [" + type + "] - startSockets()");

        IntentFilter filter = new IntentFilter("SOCKETS_CONNECTION_SERVICE");
        context.registerReceiver(receiver, filter);
        //ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED);
    }

    public static void UnregisterSockets(Context context, ConnectionReceiver receiver){
        context.unregisterReceiver(receiver);
    }

    public static void stopSockets(Intent serv, Context context, ConnectionReceiver receiver, boolean unregister){
        context.stopService(serv);

        if(unregister){
            context.unregisterReceiver(receiver);
            Log.d(TAG, "Unregister receiver");
        }
    }

    /**
     * Register sockets service */
    public static void registerSockets(Context context, ConnectionReceiver receiver, String action, String TAG){
        IntentFilter filter = new IntentFilter(action);
        context.registerReceiver(receiver, filter);
        Log.d(TAG, "Registering Sockets ACTION [" + action + "] - registerSockets()");
    }

    public static void setDomain(String domain){
        _domain = domain;
    }

    public static String getDomain(){
       return _domain;
    }

    public static String getAddress(){
        return URL;
    }

    public static void setPort(String port){
        _port = port;
    }

    public static void setDir(String dir){
        _dir = dir;
    }

    public static void setType(String type){
        _type = type;
    }

    public static void sendMessage(Map<String, String> postParam){
        JSONObject obj = new JSONObject(postParam);
        if(client != null) {
            client.send(obj.toString());
        }
    }
}
