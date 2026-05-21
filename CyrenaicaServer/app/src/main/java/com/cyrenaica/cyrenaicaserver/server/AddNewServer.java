package com.cyrenaica.cyrenaicaserver.server;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.RouteInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cyrenaica.cyrenaicaserver.R;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.DatabaseSetup;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.Encryption;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.ServerSQLHelper;
import com.cyrenaica.cyrenaicaserver.language.LocalHelper;
import com.cyrenaica.cyrenaicaserver.tools.RequestSingleton;
import com.cyrenaica.cyrenaicaserver.tools.Tools;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AddNewServer extends AppCompatActivity {

    private static final String TAG = "ADD_NEW_SERVER";

    private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1;

    TextView page_label;
    TextView servers_count_first_paragraph;
    TextView servers_count;
    TextView servers_count_last_paragraph;
    TextView new_server_scan_message;
    FrameLayout wifi_scan_result_frameLayout;
    TextView new_server_wifi_scan_ap_name;
    ImageView scan_net_secured_icon;
    TextView net_secure_description;
    ImageView wifi_level_icon;
    TextView wifi_level_description;
    ImageView wifi_mac_icon;
    TextView wifi_mac_description;
    TextView scan_error_message;
    Button scan_button;
    LinearLayout wifi_gps_enable_control_button_layout;
    TextView wifi_or_gps_disabled_message;
    Button enable_wifi_button;
    Button enable_gps_button;

    WifiManager wifiManager;

    List<ScanResult> results;
    int size = 0;

    int signalLevel;
    int level;
    String bssid;
    String conn_ssid;
    String conn_encryption;
    int conn_wifiL;
    int conn_channelW;
    private LocationSettingsRequest.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_new_server);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.d(TAG, "onCreate");

        if(!ServerSQLHelper.dbHelper.dbExist() || !ServerSQLHelper.encrp.searchDbPassword()){

            Log.i(TAG, "GO TO DatabaseSetup to create");

            Intent intent = new Intent(AddNewServer.this, DatabaseSetup.class);
            intent.putExtra("Activity", "AddNewServer");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            AutofillManager afm = getApplicationContext().getSystemService(AutofillManager.class);
            afm.cancel();
        }

        page_label                            = findViewById(R.id.id_add_new_server_page_label);
        servers_count_first_paragraph         = findViewById(R.id.id_servers_count_first_paragraph);
        servers_count                         = findViewById(R.id.id_servers_count);
        servers_count_last_paragraph          = findViewById(R.id.id_servers_count_last_paragraph);
        new_server_scan_message               = findViewById(R.id.id_add_new_server_scan_message);
        wifi_scan_result_frameLayout          = findViewById(R.id.id_wifi_scan_result_frameLayout);
        new_server_wifi_scan_ap_name          = findViewById(R.id.add_new_server_wifi_scan_ap_name);
        scan_net_secured_icon                 = findViewById(R.id.id_scan_net_secured_icon);
        net_secure_description                = findViewById(R.id.id_net_secure_description);
        wifi_level_icon                       = findViewById(R.id.id_wifi_level_icon);
        wifi_level_description                = findViewById(R.id.id_wifi_level_description);
        wifi_mac_icon                         = findViewById(R.id.id_wifi_mac_icon);
        wifi_mac_description                  = findViewById(R.id.id_wifi_mac_description);
        scan_error_message                    = findViewById(R.id.id_scan_error_message);
        scan_button                           = findViewById(R.id.id_scan_button);
        wifi_gps_enable_control_button_layout = findViewById(R.id.id_wifi_gps_enable_control_button_layout);
        wifi_or_gps_disabled_message          = findViewById(R.id.id_wifi_or_gps_disabled_message);
        enable_wifi_button                    = findViewById(R.id.id_enable_wifi_button);
        enable_gps_button                     = findViewById(R.id.id_enable_gps_button);

        int servers_count_in_db = ServerSQLHelper.DbHelper(getApplicationContext()).getServersCount();

        /* Get servers count in server table */
        servers_count.setText(String.valueOf(servers_count_in_db));

        if (LocalHelper.getLanguage(AddNewServer.this).equalsIgnoreCase("ar")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                page_label.setTypeface(getResources().getFont(R.font.cairo));
                servers_count_first_paragraph.setTypeface(getResources().getFont(R.font.changa));
                servers_count_last_paragraph.setTypeface(getResources().getFont(R.font.changa));
                new_server_scan_message.setTypeface(getResources().getFont(R.font.changa));
                scan_button.setTypeface(getResources().getFont(R.font.cairo));
                wifi_or_gps_disabled_message.setTypeface(getResources().getFont(R.font.changa));
                enable_wifi_button.setTypeface(getResources().getFont(R.font.cairo));
                enable_gps_button.setTypeface(getResources().getFont(R.font.cairo));
            }

        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                page_label.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
                new_server_scan_message.setTypeface(getResources().getFont(R.font.robotoslab));
                wifi_or_gps_disabled_message.setTypeface(getResources().getFont(R.font.robotoslab));
            }
        }

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // if Gps is disabled
        if(!Tools.isLocationEnabled(this)) {

            wifi_gps_enable_control_button_layout.setVisibility(View.VISIBLE);
            wifi_or_gps_disabled_message.setVisibility(View.GONE);
            enable_gps_button.setVisibility(View.VISIBLE);
            wifi_or_gps_disabled_message.setText(R.string.gps_disabled_message);

            // if your phone do not have GooglePlay service
            if (!Tools.checkPlayServices(this, this)) {

                enable_gps_button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        // Enable GPS without GooglePlay service
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                        // if GPS is enabled
                        if(Tools.isLocationEnabled(getApplicationContext())) {

                            enable_gps_button.setVisibility(View.GONE);
                            wifi_or_gps_disabled_message.setText("");
                            wifi_or_gps_disabled_message.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "GPS enabled successfully", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Failed enabling GPS service", Toast.LENGTH_LONG).show();

                        }
                    }
                });
            }
            else
            {
                enable_gps_button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        wifi_gps_enable_control_button_layout.setVisibility(View.VISIBLE);
                        enable_wifi_button.setVisibility(View.GONE);
                        enable_gps_button.setVisibility(View.VISIBLE);
                        wifi_or_gps_disabled_message.setText(R.string.gps_disabled_message);

                        LocationRequest request = new LocationRequest().setFastestInterval(1500).setInterval(3000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                        builder = new LocationSettingsRequest.Builder().addAllLocationRequests(Collections.singleton(request));

                        Task<LocationSettingsResponse> results = LocationServices.getSettingsClient(AddNewServer.this).checkLocationSettings(builder.build());

                        results.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                                try {
                                    task.getResult(ApiException.class);
                                }
                                catch (ApiException e) {

                                    switch (e.getStatusCode()) {

                                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                                            try {
                                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                                resolvableApiException.startResolutionForResult(AddNewServer.this, Tools.REQUEST_CHECK_CODE);

                                                enable_gps_button.setVisibility(View.GONE);
                                                wifi_or_gps_disabled_message.setText("");
                                                wifi_or_gps_disabled_message.setVisibility(View.GONE);
                                            }
                                            catch (IntentSender.SendIntentException sendIntentException) {
                                                sendIntentException.printStackTrace();
                                            }

                                        break;

                                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE: {
                                            break;
                                        }
                                    }
                                }
                            }
                        });

                        if(Tools.isLocationEnabled(getApplicationContext())) {

                            enable_gps_button.setVisibility(View.GONE);
                            wifi_or_gps_disabled_message.setText("");
                            wifi_or_gps_disabled_message.setVisibility(View.GONE);

                            Toast.makeText(getApplicationContext(), "GPS enabled successfully", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Failed enabling GPS service", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }

        if (!wifiManager.isWifiEnabled()) {

            wifi_gps_enable_control_button_layout.setVisibility(View.VISIBLE);
            enable_gps_button.setVisibility(View.GONE);
            enable_wifi_button.setVisibility(View.VISIBLE);
            wifi_or_gps_disabled_message.setText(R.string.wifi_disabled_message);

            enable_wifi_button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                        Intent panelIntent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
                        startActivityForResult(panelIntent, 0);
                    }
                    else
                    {
                        wifiManager.setWifiEnabled(true);
                    }

                    if (wifiManager.isWifiEnabled()) {

                        enable_wifi_button.setVisibility(View.GONE);
                        enable_gps_button.setVisibility(View.GONE);
                        wifi_or_gps_disabled_message.setText("");
                        wifi_or_gps_disabled_message.setVisibility(View.GONE);
                        wifi_gps_enable_control_button_layout.setVisibility(View.GONE);

                        Toast.makeText(getApplicationContext(), "WIFI enabled successfully", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Failed enabling WIFI", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        scan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ActivityCompat.checkSelfPermission(AddNewServer.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddNewServer.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);

                }
                else if (ActivityCompat.checkSelfPermission(AddNewServer.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddNewServer.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
                }
                else
                {
                    new_server_wifi_scan_ap_name.setText(R.string.scanning_points_text);
                    scanWifiNetworks(getApplicationContext());
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    final BroadcastReceiver setupNewDeviceWifiReceiver = new BroadcastReceiver() {

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.w(TAG, "BroadcastReceiver / onReceive");

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            results = wifiManager.getScanResults();
            size = results.size();
            context.unregisterReceiver(this);

            new_server_wifi_scan_ap_name.setText("");
            int len = 0;

            boolean cyrenaica_devices = false;
            boolean server_device     = false;

            try {

                String router_ssid = Tools.getStringPref(getApplicationContext(), "SETUP_ROUTER_WIFI_SSID");

                for(int i = 0; i < size; i++){

                    ScanResult res = results.get(i);

                    System.out.println("SSID: " + res.SSID);

                    if (res.SSID.contains("CYRENAICA")) {

                        cyrenaica_devices = true;

                        if (res.SSID.contains("SERVER")) {

                            server_device = true;

                            Tools.CURRENT_WIFI_SSID = res.SSID;

                            if (res.capabilities.toUpperCase().contains("WEP")) {
                                conn_encryption = "WEP";
                            }
                            else if (res.capabilities.toUpperCase().contains("WPA")) {
                                conn_encryption = "WPA";
                            }
                            else {
                                conn_encryption = "OPEN";
                            }

                            conn_channelW = res.channelWidth;

                            signalLevel = WifiManager.calculateSignalLevel(conn_wifiL, 4);
                            level = res.level;
                            bssid = res.BSSID;

                            break;
                        }
                        else
                        {
                            server_device = false;
                        }
                    }
                    else
                    {
                        cyrenaica_devices = false;
                    }
                }

                cyrenaicaDevicesSearch(cyrenaica_devices, server_device, Tools.CURRENT_WIFI_SSID, conn_encryption, signalLevel, level, bssid);

            }
            catch (Exception e) {
                Log.w("WifScanner", "Exception: " + e);
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case MY_PERMISSIONS_ACCESS_COARSE_LOCATION:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(AddNewServer.this, "permission granted", Toast.LENGTH_SHORT).show();

                    new_server_wifi_scan_ap_name.setText(R.string.scanning_points_text);

                    scanWifiNetworks(this);
                }
                else
                {
                    Toast.makeText(AddNewServer.this, "permission not granted", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }
    }

    private void scanWifiNetworks(Context context) {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(setupNewDeviceWifiReceiver, intentFilter);

        wifiManager.startScan();

        Toast.makeText(this, "Scanning....", Toast.LENGTH_SHORT).show();
    }

    public void cyrenaicaDevicesSearch(boolean cyrenaica_devices, boolean is_server, String ssid, String encryption, int signal_level, int level, String bssid){

        Log.i(TAG, "cyrenaicaDevicesSearch: ");

        Log.i(TAG, "cyrenaica_devices: " + cyrenaica_devices);
        Log.i(TAG, "is_server: " + is_server);

        if(cyrenaica_devices){

            if(is_server) {

                wifi_scan_result_frameLayout.setVisibility(View.VISIBLE);

                new_server_wifi_scan_ap_name.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.orange));
                new_server_wifi_scan_ap_name.setAlpha((float) 1);
                new_server_wifi_scan_ap_name.setText(ssid);
                net_secure_description.setText(String.valueOf(encryption));

                wifi_level_description.setText(String.valueOf(level));
                wifi_mac_description.setText(String.valueOf(bssid));

                if (encryption.equals("OPEN")) {
                    if (signal_level == 0) {
                        scan_net_secured_icon.setImageResource(R.drawable.ic_network_wifi_1_bar_white_24dp);
                    }
                    else if (signal_level == 1) {
                        scan_net_secured_icon.setImageResource(R.drawable.ic_network_wifi_2_bar_white_24dp);
                    }
                    else if (signal_level == 2) {
                        scan_net_secured_icon.setImageResource(R.drawable.ic_network_wifi_3_bar_white_24dp);
                    }
                    else if (signal_level == 3) {
                        scan_net_secured_icon.setImageResource(R.drawable.ic_signal_wifi_4_bar_white_24dp);
                    }
                }
                else {
                    if (signal_level == 0) {
                        scan_net_secured_icon.setImageResource(R.drawable.ic_signal_wifi_1_bar_lock_white_24dp);
                    }
                    else if (signal_level == 1) {
                        scan_net_secured_icon.setImageResource(R.drawable.ic_signal_wifi_2_bar_lock_white_24dp);
                    }
                    else if (signal_level == 2) {
                        scan_net_secured_icon.setImageResource(R.drawable.ic_signal_wifi_3_bar_lock_white_24dp);
                    }
                    else if (signal_level == 3) {
                        scan_net_secured_icon.setImageResource(R.drawable.ic_signal_wifi_4_bar_lock_white_24dp);
                    }
                }
            }
            else
            {
                // Not server device
            }
        }
        else
        {
            // Not cyrenaica device
        }

        System.out.println("SSID: " + ssid + " - ENCRYPTION: " + encryption + " - LEVEL: " + level + " - SIGNAL: " + signal_level + " - MAC: " + bssid);

    }

    public void btn_showMessage(View view) {

        Log.i(TAG, "btn_showMessage: ");

        Tools.CURRENT_WIFI_SSID = (String) new_server_wifi_scan_ap_name.getText();

        if(Tools.CURRENT_WIFI_SSID.contains("CYRENAICA")) {

            final AlertDialog.Builder alert                                             = new AlertDialog.Builder(AddNewServer.this);
            View mView                                                                  = getLayoutInflater().inflate(R.layout.wifi_connect_dialog, null);

            final TextView connectDialog_label                                          = mView.findViewById(R.id.id_dialog_label);
            final com.google.android.material.textfield.TextInputLayout passwordLayout  = mView.findViewById(R.id.id_router_info_dialog_input_layout);
            final com.google.android.material.textfield.TextInputEditText password      = mView.findViewById(R.id.id_dialog_input_field);
            final String dialogPassword                                                 = password.getText().toString();
            final TextView dialog_default_message                                       = mView.findViewById(R.id.id_dialog_default_message);
            final TextView validationMsg                                                = mView.findViewById(R.id.passwordValidationMsg);
            final TextView btn_cancel                                                   = mView.findViewById(R.id.id_btn_cancel);
            final TextView btn_okay                                                     = mView.findViewById(R.id.id_btn_retry);

            password.setText("QNS5QCKVK1J6");

            if (LocalHelper.getLanguage(AddNewServer.this).equalsIgnoreCase("ar")) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    connectDialog_label.setTypeface(getResources().getFont(R.font.changa));
                    passwordLayout.setTypeface(getResources().getFont(R.font.cairo));
                    dialog_default_message.setTypeface(getResources().getFont(R.font.cairo));
                    validationMsg.setTypeface(getResources().getFont(R.font.cairo));
                    btn_cancel.setTypeface(getResources().getFont(R.font.cairo));
                    btn_okay.setTypeface(getResources().getFont(R.font.cairo));
                }
            }
            else
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    connectDialog_label.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
                }
            }

            alert.setView(mView);

            final AlertDialog alertDialog = alert.create();

            alertDialog.setCanceledOnTouchOutside(false);

            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

            btn_okay.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (!Tools.isPasswordValid(password.getText().toString())) {

                        password.setError("Please enter the password, current is: " + password.getText().toString());
                        return;
                    }

                    WifiConfiguration wifiConfig = new WifiConfiguration();
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    int netId = wifiManager.addNetwork(wifiConfig);

                    if (netId != -1) {
                        wifiManager.enableNetwork(netId, true);
                    }

                    if(Objects.equals(password.getText().toString(), Tools.DEFAULT_WIFI_PASSWORD)){

                        Tools.setStringPref(getApplicationContext(), "CURRENT_WIFI_PASS", password.getText().toString());
                        Toast.makeText(getApplicationContext(), "Server Credentials Saved Successfully", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        if(dialogPassword.isEmpty()){
                            validationMsg.setText("");
                        }

                        validationMsg.setVisibility(View.VISIBLE);

                        validationMsg.setText(R.string.conn_to_server_dialog_pass_validation_msg);
                        Toast.makeText(getApplicationContext(), "invalid credential", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                        System.out.println("SSID: " + Tools.CURRENT_WIFI_SSID + " PASS: " + password.getText().toString());

                        WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
                        builder.setSsid(Tools.CURRENT_WIFI_SSID);
                        builder.setWpa2Passphrase(password.getText().toString());

                        WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();

                        NetworkRequest.Builder networkRequestBuilder1 = new NetworkRequest.Builder();

                        networkRequestBuilder1.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
                        networkRequestBuilder1.setNetworkSpecifier(wifiNetworkSpecifier);
                        networkRequestBuilder1.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED);

                        NetworkRequest nr = networkRequestBuilder1.build();
                        final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                        final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {

                            @Override
                            public void onAvailable(@NonNull Network network) {
                                super.onAvailable(network);

                                Tools.LIVE_WIFI_STATUS = "AVAILABLE";

                                Log.d("ON_AVAILABLE", "Available Network" + network.toString());

                                cm.bindProcessToNetwork(network);

                                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);

                                if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

                                    alertDialog.dismiss();

                                    if(cm.isDefaultNetworkActive()){

                                        cm.reportNetworkConnectivity(network, isActivityTransitionRunning());

                                        //Toast.makeText(getApplicationContext(), "CURRENT_WIFI_SSID: " + Tools.CURRENT_WIFI_SSID, Toast.LENGTH_SHORT).show();
                                        System.out.println("CURRENT_WIFI_SSID: " + Tools.CURRENT_WIFI_SSID + " CURRENT_WIFI_SSID_TYPE: " + Tools.CURRENT_WIFI_SSID_TYPE);

                                        /* This function check if the new server setup was made or not */
                                        Tools.setStringPref(getApplicationContext(), "CURRENT_WIFI_SSID", Tools.CURRENT_WIFI_SSID);
                                        Tools.setStringPref(getApplicationContext(), "CURRENT_WIFI_SSID_TYPE", Tools.CURRENT_WIFI_SSID_TYPE);

                                        Tools.hideSoftKeyboard(AddNewServer.this);

                                        Log.w(TAG, "GO TO checkDeviceSetup");

                                        String url = "http://" + Tools.DEFAULT_AP_STATIC_IP + "/setup_needed";
                                        checkDeviceSetup(getApplicationContext(), url, "IS_SETUP_NEEDED");
                                    }
                                }
                            }

                            @Override
                            public void onLosing(@NonNull Network network, int maxMsToLive) {
                                super.onLosing(network, maxMsToLive);
                                Tools.LIVE_WIFI_STATUS = "LOSING CONNECTION";
                                Log.d("ON_LOSING", "Losing Network "+network);
                            }

                            @Override
                            public void onLost(@NonNull Network network) {
                                super.onLost(network);
                                Tools.LIVE_WIFI_STATUS = "CONNECTION LOST";
                                Log.d("ON_LOST", "We lost Network "+network);
                            }

                            @Override
                            public void onUnavailable() {
                                super.onUnavailable();
                                Tools.LIVE_WIFI_STATUS = "CONNECTION UNAVAILABLE";
                                Log.d("ON_UNAVAILABLE", "Network is Unavailable");
                            }

                            @Override
                            public void onCapabilitiesChanged(@NonNull Network network, NetworkCapabilities networkCapabilities) {
                                WifiInfo wifiInfo = (WifiInfo) networkCapabilities.getTransportInfo();
                                int down_stream   = networkCapabilities.getLinkDownstreamBandwidthKbps();
                                int up_stream     = networkCapabilities.getLinkUpstreamBandwidthKbps();
                            }

                            @Override
                            public void onLinkPropertiesChanged(@NonNull Network network, LinkProperties linkProperties){
                                List<RouteInfo> mm;
                                List<InetAddress> aa;
                                List<LinkAddress> bb;

                                aa = linkProperties.getDnsServers();
                                bb = linkProperties.getLinkAddresses();
                                System.out.println("PROPS DNS: " + aa);
                                System.out.println("PROPS IP:  " + bb);
                            }

                            @Override
                            public void onBlockedStatusChanged(@NonNull Network network, boolean blocked){

                            }
                        };

                        cm.requestNetwork(nr, networkCallback);
                    }
                    else
                    {

                    }
                }
            });

            alertDialog.show();
        }
    }

    public void checkDeviceSetup(final Context context, String jsonUrl, String question) {

        Map<String, String> postParam = new HashMap<String, String>();

        postParam.put("QUESTION", question);
        postParam.put("SECURITY", "GET_SECURITY");

        RetryPolicy mRetryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, jsonUrl, new JSONObject(postParam), response -> {

            System.out.println("DEVICE SETUP CHECK DATA RESPONSE: " + response.toString());

            if(!response.toString().isEmpty()) {

                try {

                    JSONObject jsonObject    = new JSONObject(response.toString());
                    JSONObject main_response = new JSONObject(jsonObject.getString("RESPONSE"));
                    String server_status     = main_response.getString("STATUS");
                    String default_app_id    = "";
                    String setup_ok          = "";
                    boolean _setup_ok        = false;

                    if(server_status.equals("SETUP_OK_CHECK")){

                        setup_ok = main_response.getString("SETUP_OK");
                        _setup_ok = Tools.convertEspBoolean(setup_ok);

                        Log.i(TAG, "SETUP_OK: " + _setup_ok);

                        if(_setup_ok){

                            Tools.setBoolPref(getApplicationContext(),"", _setup_ok);
                            // device setup made / if others checks
                        }
                        else
                        {
                            default_app_id = main_response.getString("DEFAULT_APP_ID");

                            if(default_app_id.isEmpty() || default_app_id.equals("EMPTY")) {

                                System.out.println("DEFAULT_APP_ID RESPONSE: " + default_app_id);

                            }
                            else
                            {

                                if (Tools.checkDefaultAppId(default_app_id)) {

                                    Log.i(TAG, "GO TO checkAccessPointSetup");

                                    Tools.setStringPref(getApplicationContext(), "DEFAULT_APP_ID", default_app_id);
                                    /* This function check if the new server setup was made or not */
                                    String url = "http://" + Tools.DEFAULT_AP_STATIC_IP + "/server_network";
                                    checkAccessPointSetup(getApplicationContext(), url, "SETUP_SERVER_ACCESSPOINT");

                                }
                                else
                                {
                                    System.out.println("DEFAULT_APP_ID NOT CORRECT ");
                                }
                            }
                        }

                    }

                    if(server_status.equals("SECURITY_ERROR")){

                    }

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },

                error -> System.out.println("Get router setup Error: " + error.getMessage())) {
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

    public void checkAccessPointSetup(final Context context, String jsonUrl, String question) {

        Log.i(TAG, "checkAccessPointSetup");

        Map<String, String> postParam = new HashMap<String, String>();

        String _def_app_id     = Tools.getStringPref(getApplicationContext(), "DEFAULT_APP_ID");
        String _ssid           = Tools.getStringPref(getApplicationContext(), "CURRENT_WIFI_SSID");
        String _pass           = Tools.getStringPref(getApplicationContext(), "CURRENT_WIFI_PASS");

        postParam.put("QUESTION", question);
        postParam.put("DEFAULT_APP_ID", _def_app_id);
        postParam.put("APP_ID", Tools.APP_ID);
        postParam.put("AP_SSID", _ssid);
        postParam.put("AP_PASS", _pass);

        RetryPolicy mRetryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, jsonUrl, new JSONObject(postParam), response -> {

            System.out.println("ACCESS POINT SETUP CHECK DATA RESPONSE: " + response.toString());

            if(!response.toString().isEmpty()) {

                try {

                    JSONObject jsonObject    = new JSONObject(response.toString());
                    JSONObject main_response = new JSONObject(jsonObject.getString("RESPONSE"));
                    String server_status     = main_response.getString("STATUS");

                    System.out.println("server_status: " + server_status);

                    String type, ap_ssid, ap_pass, remote_access, router_setup, router_ssid, router_pass;
                    boolean _router_setup, _r_access;

                    if(server_status.equals("SERVER_SETUP_APP_ID_KO")){

                        System.out.println("server: " + server_status);

                        wifi_scan_result_frameLayout.setVisibility(View.GONE);
                        new_server_scan_message.setVisibility(View.GONE);
                        scan_error_message.setVisibility(View.VISIBLE);
                        scan_error_message.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                        scan_error_message.setText(R.string.wifi_setup_security_check_ko);

                        scan_button.setText(R.string.terminate_button);

                        scan_button.setOnClickListener(v -> {

                            // go to mainActivity

                            finish();
                            System.exit(0);
                        });
                    }
                    else
                    {
                        /* If accesspoint setup was made successfully */
                        if (server_status.equals("SERVER_SETUP_ACCESS_POINT_OK")) {

                            Log.i(TAG, "SERVER_SETUP_ACCESS_POINT_OK");

                            remote_access = main_response.getString("REMOTE_ACCESS");
                            router_setup  = main_response.getString("ROUTER_SETUP");
                            _r_access     = Tools.convertEspBoolean(remote_access);
                            _router_setup = Tools.convertEspBoolean(router_setup);


                            ap_ssid = main_response.getString("AP_SSID");
                            ap_pass = main_response.getString("AP_PASS");
                            router_ssid = main_response.getString("ROUTER_SSID");
                            router_pass = main_response.getString("ROUTER_PASS");

                            Tools.setStringPref(getApplicationContext(), "CURRENT_WIFI_SSID", ap_ssid);
                            Tools.setStringPref(getApplicationContext(), "CURRENT_WIFI_PASS", ap_pass);
                            Tools.setBoolPref(getApplicationContext(), "REMOTE_ACCESS", _r_access);
                            Tools.setBoolPref(getApplicationContext(), "ROUTER_SETUP", _router_setup);
                            Tools.setStringPref(getApplicationContext(), "CURRENT_ROUTER_SSID", router_ssid);
                            Tools.setStringPref(getApplicationContext(), "CURRENT_ROUTER_PASS", router_pass);

                            Log.i(TAG, "GO TO SecurityCheckActivity");

                            Tools.setSetupCurrentStep(getApplicationContext(), 2);

                            // Because the accesspoint still with the default (password) we will change it in the next step
                            Intent intent = new Intent(AddNewServer.this, SecurityCheckActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                        else /* if the router setup is not made yet will send user to  SecurityCheckActivity activity so the user have the possibility to modify the router again */
                        {
                            if (server_status.equals("SERVER_SETUP_ACCESS_POINT_SSID_KO")) {

                                Intent intent = new Intent(AddNewServer.this, SecurityCheckActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }

                            if (server_status.equals("SERVER_SETUP_ACCESS_POINT_PASS_KO")) {

                            }
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },

            error -> System.out.println("Get router setup Error: " + error.getMessage())) {
            /*
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