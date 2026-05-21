package com.cyrenaica.cyrenaicaserver.server;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cyrenaica.cyrenaicaserver.R;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.Encryption;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.ServerSQLHelper;
import com.cyrenaica.cyrenaicaserver.language.LocalHelper;
import com.cyrenaica.cyrenaicaserver.mdns.mDnsService;
import com.cyrenaica.cyrenaicaserver.nodes.Cameras.CameraHomeActivity;
import com.cyrenaica.cyrenaicaserver.server.adaptars.ServerAttachedDevicesListAdapter;
import com.cyrenaica.cyrenaicaserver.server.adaptars.ServerAttachedDevicesListData;
import com.cyrenaica.cyrenaicaserver.tools.ConnectionReceiver;
import com.cyrenaica.cyrenaicaserver.tools.Connectivity;
import com.cyrenaica.cyrenaicaserver.tools.RequestSingleton;
import com.cyrenaica.cyrenaicaserver.tools.SocketsService;
import com.cyrenaica.cyrenaicaserver.tools.Tools;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ServerHomeActivity extends AppCompatActivity {

    private static final String TAG = "SERVER_HOME";
    // This is the server home page when click on DETAILS button in servers list in ViewAllServers() class
    // this will show frame with server info on upper and all attached devices list

    int _SERVER_ID; // variable to get the clicked server ID on the ViewAllServers() list. (Intent.getIntExtra("server_id", -1))
    String _SERVER_NAME; // variable to get the clicked server NAME on the ViewAllServers() list. (Intent.getIntExtra("server_name"))
    String _SERVER_IP; // variable to get the clicked server IP on the ViewAllServers() list. (Intent.getIntExtra("server_ip"))
    String _SERVER_SSID; // variable to get the clicked server SSID on the ViewAllServers() list. (Intent.getIntExtra("server_ssid"))
    String _SERVER_PASS; // variable to get the clicked server PASSWORD on the ViewAllServers() list. (Intent.getIntExtra("server_pass"))

    TextView page_label;
    TextView id_label;
    TextView id_value;
    ImageView wifi_status_icon;
    ImageView presence_status_icon;
    TextView name_label;
    TextView name_value;
    ProgressBar temp_progress_bar;
    TextView temp_value;
    TextView temp_unit;
    TextView temp_label_text;
    ProgressBar press_progress_bar;
    TextView press_value;
    TextView press_unit;
    TextView press_label_text;

    ArrayList<ServerAttachedDevicesListData> _attached_devices_list = new ArrayList<>();

    TextView attached_devices_list_view_label;
    TextView no_devices_message;
    ListView attached_devices_list_view;
    ServerAttachedDevicesListAdapter attached_devices_adapter;

    Intent HomeSocketsService;
    ConnectionReceiver SocketsReceiver = new ConnectionReceiver();

    private static String SOCKETS_CONNECT_RESPONSE;

    private static boolean IS_SOCKETS_CONNECTED;
    mDnsService DNS;

    private Thread streamThread;

    Timer timer;

    public ServerHomeActivity() {
        super(R.layout.activity_server_home);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_server_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* init Toolbar */
        Toolbar toolbar = findViewById(R.id.all_servers_toolbar);
        setSupportActionBar(toolbar);

        Log.d(TAG, "onCreate");

        // Get server ID
        _SERVER_ID    = getIntent().getIntExtra("server_id", -1);
        _SERVER_NAME  = getIntent().getStringExtra("server_name");
        _SERVER_IP    = getIntent().getStringExtra("server_ip");
        _SERVER_SSID  = getIntent().getStringExtra("server_ssid");
        _SERVER_PASS  = getIntent().getStringExtra("server_pass");

        Log.d(TAG, "Server ID [" + _SERVER_ID + "] And Server NAME [" + _SERVER_NAME + "]");

        Tools.hideSoftKeyboard(ServerHomeActivity.this); // Hide keyboard

        /* To indicate the connectivity of network all communications to server must be after this method */
        Connectivity.appConnectionOnscreenHandle(getApplicationContext());

        DNS = new mDnsService(this);
        DNS.initializeNsd("CyrenaicaServer", "_http._tcp."); // CyrenaicaServer on cyrenaica server ESP32

        /* init openCV Module */
        //OpenCVLoader.initLocal();

        SQLiteDatabase.loadLibs(this);
        ServerSQLHelper.dbHelper = ServerSQLHelper.getInstance(getApplicationContext());
        ServerSQLHelper.encrp    = new Encryption(getApplicationContext());

        String db_cipher   = ServerSQLHelper.encrp.getDbCipher();
        String db_password = ServerSQLHelper.encrp.getDbPassword();

        // Set the password cipher
        ServerSQLHelper.dbHelper.setPasswordCipher(db_cipher);

        // Set the database password
        ServerSQLHelper.dbHelper.setPassword(db_password);

        page_label                       = findViewById(R.id.id_server_home_page_label);
        id_label                         = findViewById(R.id.id_server_home_id_label);
        id_value                         = findViewById(R.id.id_server_home_id_value);
        wifi_status_icon                 = findViewById(R.id.id_server_home_server_wifi_status_icon);
        presence_status_icon             = findViewById(R.id.id_server_home_presence_status_icon);

        name_label                       = findViewById(R.id.id_server_home_name_label);
        name_value                       = findViewById(R.id.id_server_home_name_value);
        temp_progress_bar                = findViewById(R.id.id_temp_progress_bar);
        temp_value                       = findViewById(R.id.id_temp_value);
        temp_unit                        = findViewById(R.id.id_temp_unit);
        temp_label_text                  = findViewById(R.id.id_temp_label_text);
        press_progress_bar               = findViewById(R.id.id_press_progress_bar);
        press_value                      = findViewById(R.id.id_press_value);
        press_unit                       = findViewById(R.id.id_press_unit);
        press_label_text                 = findViewById(R.id.id_press_label_text);
        attached_devices_list_view_label = findViewById(R.id.id_attached_devices_list_label);
        no_devices_message               = findViewById(R.id.id_attached_devices_list_no_devices_message);
        attached_devices_list_view       = findViewById(R.id.id_attached_devices_list_view);

        if (LocalHelper.getLanguage(getApplicationContext()).equalsIgnoreCase("ar")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                page_label.setTypeface(getResources().getFont(R.font.changa));
                id_label.setTypeface(getResources().getFont(R.font.cairo));
                name_label.setTypeface(getResources().getFont(R.font.cairo));
                name_value.setTypeface(getResources().getFont(R.font.cairo));
                temp_label_text.setTypeface(getResources().getFont(R.font.cairo));
                press_label_text.setTypeface(getResources().getFont(R.font.cairo));
                attached_devices_list_view_label.setTypeface(getResources().getFont(R.font.changa));
            }
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                page_label.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
                id_label.setTypeface(getResources().getFont(R.font.bayon));
                name_label.setTypeface(getResources().getFont(R.font.bayon));
                name_value.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
                temp_label_text.setTypeface(getResources().getFont(R.font.bayon));
                press_label_text.setTypeface(getResources().getFont(R.font.bayon));
                attached_devices_list_view_label.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));

            }
        }

        String[] server = ServerSQLHelper.dbHelper.getServerData(_SERVER_NAME, _SERVER_ID);

        boolean is_server_sockets_connected = getServerSocketsConnected();
        String server_sockets_response      = getServerSocketsConnectResponse();

        if(is_server_sockets_connected){

            if(server_sockets_response.equals("SOCKETS_CONNECTED_OK")){
                presence_status_icon.setImageResource(R.drawable.online_prediction_24);
            }
        }

        _attached_devices_list.clear();

        attached_devices_adapter = new ServerAttachedDevicesListAdapter(ServerHomeActivity.this, _attached_devices_list);
        attached_devices_list_view.setAdapter(attached_devices_adapter);

        id_value.setText(server[0]);
        name_value.setText(server[3]);
        //name_value.setText(server[3]);

        ServerSQLHelper.DbHelper(getApplicationContext()).serversAttachedDevicesList(_SERVER_ID, _attached_devices_list, attached_devices_adapter);

        String URL = "http://" + _SERVER_IP + "/server_operations";
        updateServerTempPress(getApplicationContext(), temp_progress_bar, temp_value, press_progress_bar, press_value, URL);

        JSONArray devices_array;
        JSONObject devices_object;
        JSONObject devices_type;

        JSONObject all_devices_object = ServerSQLHelper.dbHelper.ServerAttachedDevicesData(_SERVER_ID);

        Log.e(TAG, "DATA----- " + all_devices_object.toString());

        String _device_id   = "";
        String _device_type = "";
        String _device_name = "";

        try {

            // In this block of code we use (DEVICE ATTACHE ID FROM DATABASE) so if device's IDs on server devices.json file and from database not the same this code will not work

            int _devices_types_array_length = Tools.DEVICES_TYPES_ARRAY.length;

            devices_object = all_devices_object.getJSONObject("DEVICES");

            for(int a = 0; a < _devices_types_array_length; a++){

                if(devices_object.has(Tools.DEVICES_TYPES_ARRAY[a])) {

                    devices_type = devices_object.getJSONObject(Tools.DEVICES_TYPES_ARRAY[a]);

                    int _devices_length = devices_type.length();

                    for (int b = 0; b < _devices_length; b++) {

                        devices_array = devices_type.getJSONArray(String.valueOf(b));

                        int device_array_length = devices_array.length();

                        for (int c = 0; c < device_array_length; c++) {

                            if (c == 1) { // device attache id
                                _device_id = String.valueOf(devices_array.getInt(c));
                            }

                            if (c == 4) {
                                _device_type = devices_array.getString(c);
                            }

                            if (c == 5) { // Device name
                                _device_name = devices_array.getString(c);
                            }
                        }

                        URL = "http://" + _SERVER_IP + "/server_operations";
                        updateDeviceConnectionStatus(getApplicationContext(), _attached_devices_list, attached_devices_adapter, URL, _device_id, _device_type, _device_name);
                    }
                }
            }
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }

        startTimer(getApplicationContext());

        wifi_status_icon.setOnClickListener( v -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                Log.d(TAG, "SSID: " + _SERVER_SSID);

                WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
                builder.setSsid(_SERVER_SSID);
                builder.setWpa2Passphrase(_SERVER_PASS);

                WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();

                NetworkRequest.Builder networkRequestBuilder1 = new NetworkRequest.Builder();

                networkRequestBuilder1.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
                networkRequestBuilder1.setNetworkSpecifier(wifiNetworkSpecifier);
                networkRequestBuilder1.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED);

                NetworkRequest nr = networkRequestBuilder1.build();

                final ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {

                    @Override
                    public void onAvailable(@NonNull Network network) {
                        super.onAvailable(network);
                        Log.d("ON_AVAILABLE", "Network Available");

                        cm.bindProcessToNetwork(network);

                        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);

                        boolean _connected_to_ap = false;

                        if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

                            if(cm.isDefaultNetworkActive()){
                                cm.reportNetworkConnectivity(network, false);
                                _connected_to_ap = true;






                                Toast.makeText(getApplicationContext(), "Connected to Server: " + _SERVER_SSID, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                };

                cm.requestNetwork(nr, networkCallback);

                SocketsService.registerSockets(getApplicationContext(), SocketsReceiver, "SERVERS_SOCKETS_CONNECTED", TAG);
                HomeSocketsService = new Intent(this, SocketsService.class);
                SocketsService.startSockets(HomeSocketsService, getApplicationContext(), SocketsReceiver, "10.10.10.10", "80", "production_ws", "SERVER", TAG);
            }

        });

        attached_devices_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                ServerAttachedDevicesListData device_data = _attached_devices_list.get(position);

                Intent intent = new Intent(ServerHomeActivity.this, CameraHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("id" ,device_data.getDeviceId());
                intent.putExtra("name" ,device_data.getDeviceName());
                intent.putExtra("ip" , device_data.getDeviceNetIp());
                intent.putExtra("wifi_status" , device_data.getDeviceWifiConnectionStatus());
                intent.putExtra("espnow_status" , device_data.getDeviceEspnowConnectionStatus());
                startActivity(intent);
                //finish();
            }
        });
    }

    /** When the activity enters the Started state, the system invokes this callback. The onStart() call makes the activity visible to the user,
     * as the app prepares for the activity to enter the foreground and become interactive.
     */
    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        /*AllServersSocketsService = new Intent(getApplicationContext(), SocketsService.class);
        SocketsService.startSockets(AllServersSocketsService, getApplicationContext(), SocketsReceiver, "192.168.1.97", "80", "production_ws");*/
        //SocketsService.registerSockets(getApplicationContext(), SocketsReceiver, "SOCKETS_CONNECTED");

        if (DNS != null) {
            DNS.discoverServices();
        }
    }

    /** When the activity enters the Resumed state, it comes to the foreground,
     * and then the system invokes the onResume() callback
     */
    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");

        if ( timer == null ) {
            startTimer(getApplicationContext());
        }

        // SocketsService.registerSockets(getApplicationContext(), SocketsReceiver, "SOCKETS_CONNECTION_SERVICE");

        if (DNS != null) {
            DNS.discoverServices();
        }
    }

    /** The system calls this method as the first indication that the user is leaving your activity (though it does not always mean the activity is being destroyed);
     it indicates that the activity is no longer in the foreground (though it may still be visible if the user is in multi-window mode).
     */
    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, "onPause");

        stopTimerTask();

        if (DNS != null) {
            DNS.stopDiscovery();
        }

        /* Will unregister Receiver and stop sockets */
        //SocketsService.stopSockets(AllServersSocketsService, getApplicationContext(), SocketsReceiver, false);
    }

    /** When your activity is no longer visible to the user, it has entered the Stopped state, and the system invokes the onStop() callback. */
    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop");

        stopTimerTask();

        if (DNS != null) {
            DNS.stopDiscovery();
        }

        /* Will unregister Receiver and stop sockets */
        // SocketsService.stopSockets(AllServersSocketsService, getApplicationContext(), SocketsReceiver, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.add_new_server){

        }

        if(id == R.id.add_new_device){

        }

        if(id == R.id.view_installed_servers){

        }

        if(id == R.id.view_installed_devices){

        }

        if(id == R.id.settings){

        }

        return true;

    }

    static public boolean getServerSocketsConnected(){
        return IS_SOCKETS_CONNECTED;
    }

    static public String getServerSocketsConnectResponse(){
        return SOCKETS_CONNECT_RESPONSE;
    }

    static public void IfSocketsConnected(boolean sockets_connected){

        IS_SOCKETS_CONNECTED = sockets_connected;

        if(IS_SOCKETS_CONNECTED){


        }
        else
        {

        }
    }

    static public void IfConnectionSecured(String connection_secured){

        SOCKETS_CONNECT_RESPONSE = connection_secured;

        if(IS_SOCKETS_CONNECTED){

            if(connection_secured.equals("SOCKETS_CONNECTED_OK")){
                //presence_status_icon.setImageResource(R.drawable.online_prediction_24);
            }
            else
            {
                //presence_status_icon.setImageResource(R.drawable.offline_prediction_24);
            }
        }
        else
        {

        }
    }

    public void updateServerTempPress(final Context context, ProgressBar temp_progress, TextView tValue, ProgressBar press_progress, TextView pValue, String jsonUrl){

        Map<String, String> postParam = new HashMap<>();

        postParam.put("QUESTION", "GET_SERVER_TEMP_PRESS");

        RetryPolicy mRetryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, jsonUrl, new JSONObject(postParam), response -> {

            //Log.d(TAG, "UPDATE SERVER TEMP & PRESS DATA RESPONSE: " + response.toString());

            if(!response.toString().isEmpty()) {

                try {

                    JSONObject jsonObject   = new JSONObject(response.getString("RESPONSE"));
                    String temp             = jsonObject.getString("TEMP");
                    String press            = jsonObject.getString("PRESS");
                    /*String AX_x             = jsonObject.getString("AX_X");
                    String AX_y             = jsonObject.getString("AX_Y");
                    String AX_z             = jsonObject.getString("AX_Z");
                    String GY_x             = jsonObject.getString("GY_X");
                    String GY_y             = jsonObject.getString("GY_Y");
                    String GY_z             = jsonObject.getString("GY_Z");*/

                    temp_progress.setProgress(Integer.parseInt(temp));
                    tValue.setText(temp);
                    press_progress.setProgress(Integer.parseInt(press));
                    pValue.setText(press);

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },

                error -> Log.d(TAG, "Update Server Temp and Press Error: " + error.getMessage())) {
            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Allow", "POST");
                headers.put("Connection","keep-alive");
                headers.put("Accept", "*/*");
                headers.put("Accept-Encoding", "gzip, deflate, br");

                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(mRetryPolicy);

        RequestSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public void updateDeviceConnectionStatus(final Context context, final ArrayList<ServerAttachedDevicesListData> list, ServerAttachedDevicesListAdapter adapter, String jsonUrl, String id, String type, String name){

        Map<String, String> postParam = new HashMap<>();

        // In this method we use (DEVICE ATTACHE ID FROM DATABASE) so if device's IDs on server devices.json file and from database not the same this code will not work

        postParam.put("QUESTION", "GET_DEVICES_CONNECTION_STATUS");
        postParam.put("D_ID", id);
        postParam.put("D_TYPE", type);

        RetryPolicy mRetryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, jsonUrl, new JSONObject(postParam), response -> {

            Log.d(TAG, "UPDATE DEVICE CONNECTION STATUS DATA RESPONSE: " + response.toString());

            if(!response.toString().isEmpty()) {

                try {

                    JSONObject jsonObject       = new JSONObject(response.getString("RESPONSE"));
                    JSONObject  status          = new JSONObject(jsonObject.getString("STATUS"));
                    String sta_ip              = status.getString("STA_IP");
                    String wifi_status          = status.getString("WIFI");
                    String espnow_status        = status.getString("ESPNOW");

                    if(type.equals("CAMERA")){
                        ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_NET_IP, sta_ip, ServerSQLHelper.CAMERA_NAME, name);
                    }

                    if(type.equals("CONTROLLER")){
                        ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.CONTROLLERS_TABLE_NAME, ServerSQLHelper.CONTROLLER_NET_IP, sta_ip, ServerSQLHelper.CONTROLLER_NAME, name);
                    }

                    if(type.equals("DETECTOR")){
                        ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.DETECTORS_TABLE_NAME, ServerSQLHelper.DETECTOR_NET_IP, sta_ip, ServerSQLHelper.DETECTOR_NAME, name);
                    }

                    if (!list.isEmpty()) {

                        Log.d(TAG, "IP [" + sta_ip + "] - ID [" + id + "] - WIFI [" + wifi_status + "] ESPNOW [" + espnow_status + "]");

                        list.get(Integer.parseInt(id)).setDeviceWifiConnectionStatus(wifi_status);
                        list.get(Integer.parseInt(id)).setDeviceEspnowConnectionStatus(espnow_status);
                        adapter.notifyDataSetChanged();

                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        },

        error -> Log.d(TAG, "Update Device Connection Status Error: " + error.getMessage())) {
            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Allow", "POST");
                headers.put("Connection","keep-alive");
                headers.put("Accept", "*/*");
                headers.put("Accept-Encoding", "gzip, deflate, br");

                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(mRetryPolicy);

        RequestSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public void saveDevices(final Context context, String jsonUrl, String id, String name, String type, String ip) {

        Map<String, String> params = new HashMap<>();
        params.put("QUESTION", "GET_CONNECTED_STATUS");
        params.put("D_ID", id);
        params.put("D_TYPE", type);


        RetryPolicy mRetryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, jsonUrl, new JSONObject(params), response -> {

            Log.d(TAG, "SAVE DEVICE DATA RESPONSE: " + response.toString());

            if(!response.toString().isEmpty()) {

                try {

                    JSONObject jsonObject    = new JSONObject(response.toString());
                    JSONObject main_response  = new JSONObject(jsonObject.getString("RESPONSE"));
                    //String ip_response          = soc_response.getString("RESPONSE");
                    String device_status     = main_response.getString("STATUS");

                    Log.d(TAG, "DEVICE STATUS [" + device_status + "]");
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        },
                error -> Log.d(TAG, "Save Access Point Error: " + error.getMessage())) {
            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Allow", "POST");
                headers.put("Connection","keep-alive");
                headers.put("Accept", "*/*");
                headers.put("Accept-Encoding", "gzip, deflate, br");

                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(mRetryPolicy);

        RequestSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);

    }

    public void startTimer(final Context context) {

        System.out.println("List Updated");

        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                Handler handler = new Handler(Looper.getMainLooper()) {
                    public void handleMessage(Message msg) {

                        //Log.d(TAG, "TEMP MESSAGE" + msg.toString());

                        String SENSORS_URL = "http://" + _SERVER_IP + "/server_operations";
                        updateServerTempPress(context, temp_progress_bar, temp_value, press_progress_bar, press_value, SENSORS_URL);
                    }
                };

                handler.sendEmptyMessage(1);

            }

        },100, 1000);
    }

    public void stopTimerTask() {
        if ( timer != null ) {
            timer.cancel();
            timer = null;
        }
    }
}

