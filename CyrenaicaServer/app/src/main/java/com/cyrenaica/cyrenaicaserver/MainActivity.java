package com.cyrenaica.cyrenaicaserver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.DatabaseSetup;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.Encryption;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.ServerSQLHelper;
import com.cyrenaica.cyrenaicaserver.language.LocalHelper;
import com.cyrenaica.cyrenaicaserver.mdns.mDnsService;
import com.cyrenaica.cyrenaicaserver.nodes.AddNewDevice;
import com.cyrenaica.cyrenaicaserver.server.AddNewServer;
import com.cyrenaica.cyrenaicaserver.server.ViewAllServers;
import com.cyrenaica.cyrenaicaserver.tools.ConnectionReceiver;
import com.cyrenaica.cyrenaicaserver.tools.Connectivity;
import com.cyrenaica.cyrenaicaserver.tools.RequestSingleton;
import com.cyrenaica.cyrenaicaserver.tools.Tools;
import com.cyrenaica.cyrenaicaserver.tools.mjpeg.MjpegHome;
import com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player.SurfacePlayer;
import com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player.facerec.LocalCameraFaceDetectionActivity;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN";

    ConnectionReceiver SocketsReceiver = new ConnectionReceiver();
    Intent SocService;
    private String server_address;

    FrameLayout upper_message_frame;
    FrameLayout servers_and_devices_info_frame;
    LinearLayout control_button_layout;
    FrameLayout bottom_message_frame;
    TextView setup_message;
    TextView upper_message_text;
    TextView servers_and_devices_info_label;
    TextView servers_label;
    TextView server_count;
    TextView devices_label;
    TextView devices_count;
    TextView bottom_message_text;
    Button add_new_server_button;
    Button add_new_device_button;
    Button view_installed_servers_button;
    Button view_installed_devices_button;

    String _SERVER_IP;
    String _URL;
    mDnsService DNS;

    Button btn;
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.d(TAG, "onCreate");

        /* init Toolbar */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        upper_message_frame            = findViewById(R.id.id_upper_msg_frame);
        control_button_layout          = findViewById(R.id.id_control_button_layout);
        bottom_message_frame           = findViewById(R.id.id_bottom_msg_frame);
        setup_message                  = findViewById(R.id.id_setup_message);
        upper_message_text             = findViewById(R.id.id_main_upper_message_text);
        servers_and_devices_info_frame = findViewById(R.id.id_servers_and_devices_info_frame);
        servers_and_devices_info_label = findViewById(R.id.id_servers_and_devices_info_label);
        servers_label                  = findViewById(R.id.id_servers_label);
        server_count                   = findViewById(R.id.id_server_count);
        devices_label                  = findViewById(R.id.id_devices_label);
        devices_count                  = findViewById(R.id.id_devices_count);
        bottom_message_text            = findViewById(R.id.id_main_bottom_message_text);
        add_new_server_button          = findViewById(R.id.id_add_new_server);
        add_new_device_button          = findViewById(R.id.id_add_new_device);
        view_installed_servers_button  = findViewById(R.id.id_view_installed_servers);
        view_installed_devices_button  = findViewById(R.id.id_view_installed_devices);

        if (LocalHelper.getLanguage(MainActivity.this).equalsIgnoreCase("ar")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                upper_message_text.setTypeface(getResources().getFont(R.font.cairo));
                setup_message.setTypeface(getResources().getFont(R.font.changa));
                servers_and_devices_info_label.setTypeface(getResources().getFont(R.font.changa));
                servers_label.setTypeface(getResources().getFont(R.font.changa));
                devices_label.setTypeface(getResources().getFont(R.font.changa));
                bottom_message_text.setTypeface(getResources().getFont(R.font.changa));
                add_new_server_button.setTypeface(getResources().getFont(R.font.changa));
                add_new_device_button.setTypeface(getResources().getFont(R.font.cairo));
                view_installed_servers_button.setTypeface(getResources().getFont(R.font.changa));
                view_installed_devices_button.setTypeface(getResources().getFont(R.font.cairo));

            }

        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                upper_message_text.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
                setup_message.setTypeface(getResources().getFont(R.font.robotoslab));
                servers_and_devices_info_label.setTypeface(getResources().getFont(R.font.robotoslab));
                servers_label.setTypeface(getResources().getFont(R.font.robotoslab));
                devices_label.setTypeface(getResources().getFont(R.font.robotoslab));
                bottom_message_text.setTypeface(getResources().getFont(R.font.robotoslab));
                add_new_server_button.setTypeface(getResources().getFont(R.font.robotoslab));
                add_new_device_button.setTypeface(getResources().getFont(R.font.robotoslab));
                view_installed_servers_button.setTypeface(getResources().getFont(R.font.robotoslab));
                view_installed_devices_button.setTypeface(getResources().getFont(R.font.robotoslab));
            }
        }

        /* To indicate the connectivity of network all communications to server must be after this method */
        Connectivity.appConnectionOnscreenHandle(getApplicationContext());

        //DNS = new mDnsService(this);
        //DNS.initializeNsd("MainPage", "_http._tcp.");

        SQLiteDatabase.loadLibs(this);
        ServerSQLHelper.dbHelper = ServerSQLHelper.getInstance(getApplicationContext());
        ServerSQLHelper.encrp    = new Encryption(getApplicationContext());

        String db_cipher   = ServerSQLHelper.encrp.getDbCipher();
        String db_password = ServerSQLHelper.encrp.getDbPassword();

        // Set the password cipher
        ServerSQLHelper.dbHelper.setPasswordCipher(db_cipher);

        // Set the database password
        ServerSQLHelper.dbHelper.setPassword(db_password);

        if(!Tools.IS_SETUP_OK) {

            if (Tools.SETUP_CURRENT_STEP == 0) {

                // - SETUP DATABASE
                if(!ServerSQLHelper.dbHelper.dbExist() || !ServerSQLHelper.encrp.searchDbPassword()){

                    Log.i(TAG, "GO TO DatabaseSetup to create");

                    Intent intent = new Intent(MainActivity.this, DatabaseSetup.class);
                    intent.putExtra("Activity", "MainActivity");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    return;
                }

                /* Check if server db table exist */
                if(!ServerSQLHelper.DbHelper(getApplicationContext()).tableExist(ServerSQLHelper.SERVER_TABLE_NAME)) {/* If server db table not exist */

                    Log.i(TAG, "Creating SERVER database Table");

                    /* Create the server db table */
                    ServerSQLHelper.DbHelper(getApplicationContext()).createTable(ServerSQLHelper.SERVER_TABLE_NAME);

                    /* Check if server db table created */
                    if(ServerSQLHelper.DbHelper(getApplicationContext()).tableExist(ServerSQLHelper.SERVER_TABLE_NAME)) {/* If server db table created */

                        Log.i(TAG, "SERVER database Table created");

                        /* Here we will go to server home page to add new devices or manage the presents devices */

                        /*Intent intent = new Intent(MainActivity.this, ServerHomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);*/

                    }
                    else
                    {
                        Log.i(TAG, "Can not create SERVER table"); /* ERROR */
                    }
                }
                else/* If server db table is exist */
                {

                    /* Check if server db table is not empty */
                    if(!ServerSQLHelper.DbHelper(getApplicationContext()).tableEmpty(ServerSQLHelper.SERVER_TABLE_NAME)){/* If server db table is not empty */

                        Log.i(TAG, "SERVER table not empty");

                        /*Intent intent = new Intent(MainActivity.this, ServerHomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);*/
                    }
                    else/* If server db table is empty we will go to Application home page to add new server */
                    {
                        Log.i(TAG, "SERVER table empty");

                        servers_and_devices_info_frame.setVisibility(View.GONE);
                        upper_message_text.setVisibility(View.GONE);
                        bottom_message_frame.setVisibility(View.GONE);
                        bottom_message_text.setVisibility(View.GONE);
                        add_new_device_button.setVisibility(View.GONE);
                        view_installed_servers_button.setVisibility(View.GONE);
                        view_installed_devices_button.setVisibility(View.GONE);
                        add_new_server_button.setVisibility(View.VISIBLE);
                        setup_message.setVisibility(View.VISIBLE);

                        setup_message.setText(R.string.main_setup_not_completed_message);

                        add_new_server_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent intent = new Intent(MainActivity.this, AddNewServer.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }
            }
        }
        else /* The server setup was made we will show the options list */
        {

            // - CHECK DATABASE SETUP
            if(!ServerSQLHelper.dbHelper.dbExist() || !ServerSQLHelper.encrp.searchDbPassword()){

                Log.i(TAG, "GO TO DatabaseSetup to create");

                Intent intent = new Intent(MainActivity.this, DatabaseSetup.class);
                intent.putExtra("Activity", "MainActivity");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                return;
            }

            /* Check if server db table exist */
            if(!ServerSQLHelper.DbHelper(getApplicationContext()).tableExist(ServerSQLHelper.SERVER_TABLE_NAME)) {/* If server db table not exist */
                Toast.makeText(getApplicationContext(), "Servers table did not exist in database!", Toast.LENGTH_SHORT).show();
                bottom_message_frame.setVisibility(View.VISIBLE);
                bottom_message_text.setVisibility(View.VISIBLE);
                bottom_message_text.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.error)));
                bottom_message_text.setText(R.string.database_server_table_not_exist_reinstall);
                return;
            }

            /* Check if server settings db table exist */
            if(!ServerSQLHelper.DbHelper(getApplicationContext()).tableExist(ServerSQLHelper.SERVER_SETTINGS_TABLE_NAME)) {/* If server settings db table not exist */
                Toast.makeText(getApplicationContext(), "Servers settings table did not exist in database!", Toast.LENGTH_SHORT).show();
                bottom_message_frame.setVisibility(View.VISIBLE);
                bottom_message_text.setVisibility(View.VISIBLE);
                bottom_message_text.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.error)));
                bottom_message_text.setText(R.string.database_server_settings_table_not_exist_reinstall);
                return;
            }

            int _servers_count = 0;

            if(!ServerSQLHelper.DbHelper(getApplicationContext()).tableEmpty(ServerSQLHelper.SERVER_TABLE_NAME)){/* If server db table not empty */

                _servers_count = ServerSQLHelper.dbHelper.getServersCount();

            }

            int _total_devices     = 0;
            int _cameras_count     = 0;
            int _controllers_count = 0;
            int _detectors_count   = 0;

            /* If cameras db table not exist */
            if(ServerSQLHelper.DbHelper(getApplicationContext()).tableExist(ServerSQLHelper.CAMERAS_TABLE_NAME)) {

                /* Check if cameras db table empty */
                if (!ServerSQLHelper.DbHelper(getApplicationContext()).tableEmpty(ServerSQLHelper.CAMERAS_TABLE_NAME)) {

                    _cameras_count = ServerSQLHelper.dbHelper.getCamerasCount();
                }
            }

            /* If controllers db table not exist */
            if(ServerSQLHelper.DbHelper(getApplicationContext()).tableExist(ServerSQLHelper.CONTROLLERS_TABLE_NAME)) {

                /* Check if controllers db table exist */
                if (!ServerSQLHelper.DbHelper(getApplicationContext()).tableEmpty(ServerSQLHelper.CONTROLLERS_TABLE_NAME)) {
                    _controllers_count = ServerSQLHelper.dbHelper.getControllersCount();
                }
            }

            /* If detectors db table not exist */
            if(ServerSQLHelper.DbHelper(getApplicationContext()).tableExist(ServerSQLHelper.DETECTORS_TABLE_NAME)) {

                /* Check if detectors db table exist */
                if (!ServerSQLHelper.DbHelper(getApplicationContext()).tableEmpty(ServerSQLHelper.DETECTORS_TABLE_NAME)) {
                    _detectors_count = ServerSQLHelper.dbHelper.getDetectorsCount();
                }
            }

            _total_devices = _cameras_count + _controllers_count + _detectors_count;

            setup_message.setVisibility(View.GONE);
            upper_message_text.setVisibility(View.VISIBLE);
            servers_and_devices_info_frame.setVisibility(View.VISIBLE);

            add_new_server_button.setVisibility(View.VISIBLE);
            add_new_device_button.setVisibility(View.VISIBLE);
            view_installed_servers_button.setVisibility(View.VISIBLE);
            view_installed_devices_button.setVisibility(View.VISIBLE);

            bottom_message_frame.setVisibility(View.GONE);
            bottom_message_text.setVisibility(View.GONE);

            server_count.setText(String.valueOf(_servers_count));
            devices_count.setText(String.valueOf(_total_devices));

            /* Add new server click listener */
            add_new_server_button.setOnClickListener(new View.OnClickListener() {
                @OptIn(markerClass = UnstableApi.class)
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(MainActivity.this, MjpegHome.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    //finish(); /* If disabled we can use back to go back to the last page */

                }
            });

            /* Add new device click listener */
            add_new_device_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(MainActivity.this, AddNewDevice.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    //finish(); /* If disabled we can use back to go back to the last page */

                }
            });

            /* View installed servers click listener */
            view_installed_servers_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(MainActivity.this, ViewAllServers.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //finish(); /* If disabled we can use back to go back to the last page */

                }
            });

            /* View installed devices click listener */
            view_installed_devices_button.setOnClickListener(new View.OnClickListener() {
                @OptIn(markerClass = UnstableApi.class)
                @Override
                public void onClick(View v) {

                    /*_SERVER_IP = "192.168.1.97";

                    _URL = "http://" + _SERVER_IP + "/server_home";

                    Log.w(TAG, "SENDING REQUEST TO: " + _URL);

                    connectToServer(getApplicationContext(), _URL, "SERVER_TEST");*/

                    /*WifiConfiguration wifiConfig = new WifiConfiguration();
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    int netId = wifiManager.addNetwork(wifiConfig);

                    wifiManager.removeNetwork(netId);
                    wifiManager.saveConfiguration();

                    if (netId != -1) {
                        wifiManager.enableNetwork(netId, false);
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                        WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
                        builder.setSsid("CYRENAICA_SERVER");
                        builder.setWpa2Passphrase("QNS5QCKVK1J7");

                        WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();

                        NetworkRequest.Builder networkRequestBuilder = new NetworkRequest.Builder();

                        networkRequestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
                        networkRequestBuilder.setNetworkSpecifier(wifiNetworkSpecifier);
                        networkRequestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED);

                        NetworkRequest nr = networkRequestBuilder.build();

                        final ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                        final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {

                            @Override
                            public void onAvailable(@NonNull Network network) {
                                super.onAvailable(network);

                                cm.bindProcessToNetwork(network);

                                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);

                                if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

                                    if(cm.isDefaultNetworkActive()) {

                                        cm.reportNetworkConnectivity(network, false);

                                        _SERVER_IP = "192.168.1.97";

                                        _URL = "http://" + _SERVER_IP + "/server_home";

                                        Log.w(TAG, "SENDING REQUEST TO: " + _URL);

                                        connectToServer(getApplicationContext(), _URL, "SERVER_TEST");
                                    }
                                }
                            }
                        };

                        cm.requestNetwork(nr, networkCallback);
                    }*/



                    //Intent intent = new Intent(MainActivity.this, DatabaseInterface.class);
                    //Intent intent = new Intent(MainActivity.this, ViewAllDevices.class);
                    //Intent intent = new Intent(MainActivity.this, CameraHomeActivity.class);
                    Intent intent = new Intent(MainActivity.this, MjpegHome.class);
                    //Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                    //Intent intent = new Intent(MainActivity.this, SurfacePlayer.class);
                    //Intent intent = new Intent(MainActivity.this, LocalCameraFaceDetectionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    //finish(); /* If disabled we can use back to go back to the last page */

                }
            });

        }
    }

    /** When the activity enters the Started state, the system invokes this callback. The onStart() call makes the activity visible to the user,
        as the app prepares for the activity to enter the foreground and become interactive.
    */
    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        /*SocService = new Intent(getApplicationContext(), SocketsService.class);
        SocketsService.startSockets(SocService, getApplicationContext(), SocketsReceiver, "192.168.1.97", "80", "production_ws");
        SocketsService.registerSockets(getApplicationContext(), SocketsReceiver, "SOCKETS_CONNECTION_SERVICE");

        if (DNS != null) {
            DNS.discoverServices();
        }*/
    }

    /** When the activity enters the Resumed state, it comes to the foreground, and then the system invokes the onResume() callback */
    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");

        /*SocketsService.registerSockets(getApplicationContext(), SocketsReceiver, "SOCKETS_CONNECTION_SERVICE");

        if (DNS != null) {
            DNS.discoverServices();
        }*/
    }

    /** The system calls this method as the first indication that the user is leaving your activity
        (though it does not always mean the activity is being destroyed);
        it indicates that the activity is no longer in the foreground (though it may still be visible if the user is in multi-window mode).
    */
    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, "onPause");

        /*if (DNS != null) {
            DNS.stopDiscovery();
        }*/

        /* Will unregister Receiver and stop sockets */
        //SocketsService.stopSockets(SocService, getApplicationContext(), SocketsReceiver, false);
    }

    /** When your activity is no longer visible to the user, it has entered the Stopped state, and the system invokes the onStop() callback. */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");

        /*if (DNS != null) {
            DNS.stopDiscovery();
        }*/

        /* Will unregister Receiver and stop sockets */
        //SocketsService.stopSockets(SocService, getApplicationContext(), SocketsReceiver, true);
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

    private void connectToServer(final Context context, String jsonUrl, String question){

        Map<String, String> postParam = new HashMap<String, String>();

        postParam.put("QUESTION", question);
        postParam.put("SECURITY", "GET_SECURITY");
        postParam.put("APP_ID", Tools.APP_ID);

        RetryPolicy mRetryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, jsonUrl, new JSONObject(postParam), response -> {

            Log.d(TAG, "WIFI SERVER TEST RESPONSE: " + response.toString());

            if(!response.toString().isEmpty()) {

                try {

                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONObject main_response = new JSONObject(jsonObject.getString("RESPONSE"));
                    String device_status = main_response.getString("STATUS");
                    String setup_ok = main_response.getString("SETUP_OK");
                    String id = main_response.getString("APP_ID");

                    if(device_status.equals("SERVER_TEST_OK")){
                        Log.w(TAG, "Server Responded with: SETUP_OK [" + setup_ok + "] and APP_ID [" + id + "]");
                    }
                    else
                    {
                        Log.e(TAG, "Server Response ERROR");
                    }

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },

        error -> System.out.println("Get device setup Error: " + error.getMessage())) {
            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
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
}