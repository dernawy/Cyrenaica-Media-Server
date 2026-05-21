package com.cyrenaica.cyrenaicaserver.nodes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
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
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cyrenaica.cyrenaicaserver.MainActivity;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.Encryption;
import com.cyrenaica.cyrenaicaserver.server.ServerHomeActivity;
import com.cyrenaica.cyrenaicaserver.tools.ConnectionReceiver;
import com.cyrenaica.cyrenaicaserver.tools.RequestSingleton;
import com.cyrenaica.cyrenaicaserver.tools.SocketsService;
import com.cyrenaica.cyrenaicaserver.tools.Tools;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cyrenaica.cyrenaicaserver.R;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.ServerSQLHelper;
import com.cyrenaica.cyrenaicaserver.language.LocalHelper;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NewDeviceSetup extends AppCompatActivity {

    private static final String TAG = "NEW_DEVICE_SETUP";
    private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1;

    TextView page_label;
    TextView devices_count_first_paragraph;
    TextView devices_count;
    TextView devices_count_last_paragraph;
    TextView new_device_scan_message;
    FrameLayout wifi_scan_result_frameLayout;
    TextView new_device_wifi_scan_ap_name;
    ImageView scan_net_secured_icon;
    TextView net_secure_description;
    ImageView wifi_level_icon;
    TextView wifi_level_description;
    ImageView wifi_mac_icon;
    TextView wifi_mac_description;
    TextView scan_error_message;
    Button scan_button;

    Spinner choose_device_model_spinner;
    FrameLayout choose_server_frame;
    Spinner choose_server_spinner;
    TextView choose_model_title;
    Button start_button;
    Button cancel_button;
    Button delete_device_button;
    Button reset_device_button;
    TextView device_model_error_message;

    LinearLayout control_layout;
    LinearLayout choose_device_model_layout;
    LinearLayout device_options_layout;
    LinearLayout wifi_scan_layout;
    FrameLayout wifi_scan_results_frameLayout;
    LinearLayout security_check_layout;
    LinearLayout prefs_status_message_layout;
    com.google.android.material.textfield.TextInputLayout options_device_name_layout;
    com.google.android.material.textfield.TextInputEditText options_device_name_input;
    com.google.android.material.textfield.TextInputLayout apSsidLayout;
    com.google.android.material.textfield.TextInputEditText apSsidInput;
    com.google.android.material.textfield.TextInputLayout apPassLayout;
    com.google.android.material.textfield.TextInputEditText apPassInput;
    TextView prefs_status_message_view;
    Button security_check_button;
    TextView device_security_check_label;
    TextView device_options_error; // error view in id_add_device_options_layout
    TextView scan_message;
    TextView apView;
    TextView netSecurityDesc;
    ImageView netSecurityIcon;
    TextView WifiLevelLabel;
    ImageView WifiLevelIcon;
    TextView macAddressLabel;
    ImageView macAddressIcon;
    TextView scan_error_msg;

    String _DEVICE = "";
    String _MODEL  = "";
    String _SSID = "";
    String _NAME = "";
    String _ATTACH_SERVER_NAME = "";
    String URL;
    String DEVICE_IP;
    String _SECURITY = "";
    String _model  = "";
    String _device_name = "";
    String _attach_server_name = "";
    final String[] choose_model_cameras_array     = { "Select", "AI THINKER" };
    final String[] choose_server_array     = { "Select" };
    String device_name_zero_error                 = "The DEVICE NAME field is empty, please choose a name between [3 and 8] Latin characters";
    String device_name_min_error                  = "The DEVICE NAME field is less than the min name length, please choose a name between [3 and 8] Latin characters";
    String device_name_max_error                  = "The DEVICE NAME field is more than the max name length, please choose a name between [3 and 8] Latin characters";

    String RESET_DEVICE_RESULT_STRING;
    String TEMP_RESET_PASSWORD;

    WifiManager wifiManager;

    int signalLevel;
    int level;
    String bssid;
    String conn_encryption;
    int conn_wifiL;
    int conn_channelW;
    List<ScanResult> results;
    int size = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_device_setup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.d(TAG, "onCreate");

        String _device_type = getIntent().getStringExtra("device_type");
        setDevice(_device_type);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;

        page_label                    = findViewById(R.id.id_add_device_page_label);
        choose_device_model_layout    = findViewById(R.id.id_add_device_choose_device_model_layout);
        choose_model_title            = findViewById(R.id.id_add_device_choose_model_title);
        choose_device_model_spinner   = findViewById(R.id.id_add_device_choose_model_spinner);
        choose_server_frame           = findViewById(R.id.id_choose_server_frame);
        choose_server_spinner         = findViewById(R.id.id_choose_server_spinner);
        device_model_error_message    = findViewById(R.id.id_add_device_choose_device_model_error_message);
        device_options_layout         = findViewById(R.id.id_add_device_options_layout);
        options_device_name_layout    = findViewById(R.id.id_new_device_option_device_name_layout);
        options_device_name_input     = findViewById(R.id.id_new_device_option_device_name_input);
        device_options_error          = findViewById(R.id.id_add_device_options_error);
        wifi_scan_layout              = findViewById(R.id.id_add_device_wifi_scan_layout);
        wifi_scan_results_frameLayout = findViewById(R.id.id_add_device_scan_result_frameLayout);
        apView                        = findViewById(R.id.id_add_device_wifi_scan_ap);
        scan_net_secured_icon         = findViewById(R.id.id_scan_net_secured_icon);
        net_secure_description        = findViewById(R.id.id_net_secur_description);
        wifi_level_icon               = findViewById(R.id.id_wifi_level_icon);
        wifi_level_description        = findViewById(R.id.id_wifi_level_label);
        wifi_mac_icon                 = findViewById(R.id.id_wifi_mac_icon);
        macAddressLabel               = findViewById(R.id.id_wifi_mac_label);
        scan_button                   = findViewById(R.id.id_add_device_scan_button);
        scan_error_msg                = findViewById(R.id.id_add_device_scan_error_msg);
        security_check_layout         = findViewById(R.id.id_add_device_security_check_layout);
        device_security_check_label   = findViewById(R.id.id_add_device_security_check_section_label);
        apSsidLayout                  = findViewById(R.id.id_add_device_security_check_ap_ssid_field_layout);
        apSsidInput                   = findViewById(R.id.id_add_device_security_check_ap_ssid_input_field);
        apPassLayout                  = findViewById(R.id.id_add_device_security_check_ap_pass_field_layout);
        apPassInput                   = findViewById(R.id.id_add_device_security_check_ap_pass_input_field);
        prefs_status_message_layout   = findViewById(R.id.id_add_device_save_device_prefs_status_message_layout);
        prefs_status_message_view     = findViewById(R.id.id_add_device_save_device_prefs_status_message_view);
        security_check_button         = findViewById(R.id.id_add_device_security_check_button);
        control_layout                = findViewById(R.id.id_add_device_control_buttons_layout);
        start_button                  = findViewById(R.id.id_add_device_start_button);
        delete_device_button          = findViewById(R.id.id_add_device_delete_device_button);
        reset_device_button           = findViewById(R.id.id_add_device_reset_device_button);
        cancel_button                 = findViewById(R.id.id_add_device_cancel_button);

        if (LocalHelper.getLanguage(NewDeviceSetup.this).equalsIgnoreCase("ar")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                choose_model_cameras_array[0]   = "اختيار";

                page_label.setTypeface(getResources().getFont(R.font.changa));
                choose_model_title.setTypeface(getResources().getFont(R.font.cairo));
                device_model_error_message.setTypeface(getResources().getFont(R.font.cairo));
                device_options_error.setTypeface(getResources().getFont(R.font.cairo));
                net_secure_description.setTypeface(getResources().getFont(R.font.cairo));
                wifi_level_description.setTypeface(getResources().getFont(R.font.cairo));
                macAddressLabel.setTypeface(getResources().getFont(R.font.cairo));
                scan_button.setTypeface(getResources().getFont(R.font.cairo));
                scan_error_msg.setTypeface(getResources().getFont(R.font.cairo));
                device_security_check_label.setTypeface(getResources().getFont(R.font.cairo));
                prefs_status_message_view.setTypeface(getResources().getFont(R.font.cairo));
                security_check_button.setTypeface(getResources().getFont(R.font.cairo));
                start_button.setTypeface(getResources().getFont(R.font.cairo));
                delete_device_button.setTypeface(getResources().getFont(R.font.cairo));
                reset_device_button.setTypeface(getResources().getFont(R.font.cairo));
                cancel_button.setTypeface(getResources().getFont(R.font.cairo));
            }
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                page_label.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
                //devices_count_first_paragraph.setTypeface(getResources().getFont(R.font.robotoslab));
                //devices_count_last_paragraph.setTypeface(getResources().getFont(R.font.robotoslab));
                //new_device_scan_message.setTypeface(getResources().getFont(R.font.robotoslab));
                scan_button.setTypeface(getResources().getFont(R.font.robotoslab));
            }
        }

        int servers_length = ServerSQLHelper.dbHelper.getAllServerForSpinner().length;

        if(servers_length == 0){
            choose_model_title.setVisibility(View.GONE);
            device_model_error_message.setVisibility(View.VISIBLE);
            device_model_error_message.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.error)));
            device_model_error_message.setText(R.string.server_add_device_no_servers_found_message);
            return;
        }

        ArrayAdapter<String> choose_server_adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.add_new_device_choose_device_model_spinner, ServerSQLHelper.dbHelper.getAllServerForSpinner());
        choose_server_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        choose_server_spinner.setAdapter(choose_server_adapter);

        ArrayAdapter<String> choose_device_adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.add_new_device_choose_device_model_spinner, choose_model_cameras_array);
        choose_device_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        choose_device_model_spinner.setAdapter(choose_device_adapter);

        choose_device_model_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(getDevice().equals("CAMERA")) {

                    if(i > 0){

                        _model = adapterView.getItemAtPosition(i).toString();
                        setDeviceModel(_model);
                        choose_server_frame.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        _model = "";
                        _attach_server_name = "";
                        choose_server_frame.setVisibility(View.GONE);
                        control_layout.setVisibility(View.GONE);
                        device_model_error_message.setText("");
                        device_model_error_message.setVisibility(View.GONE);
                    }

                    Log.d(TAG, "DEVICE MODEL: " + getDeviceModel());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        choose_server_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(i > 0){

                    _attach_server_name = adapterView.getItemAtPosition(i).toString();
                    setAttachServerName(_attach_server_name);

                    control_layout.setVisibility(View.VISIBLE);
                    start_button.setVisibility(View.VISIBLE);
                    cancel_button.setVisibility(View.VISIBLE);
                    device_model_error_message.setText("");

                }
                else
                {
                    _attach_server_name = "";
                    control_layout.setVisibility(View.GONE);
                    device_model_error_message.setText("");
                    device_model_error_message.setVisibility(View.GONE);
                }

                Log.d(TAG, "ATTACHE SERVER NAME: " + getAttachServerName());

                String server_name = getAttachServerName();
                int device_count = ServerSQLHelper.dbHelper.getServerDevicesCount(server_name); // Get current devices count in server table

                Log.d(TAG, "ATTACHE SERVER NAME: " + getAttachServerName() + " Count: " + device_count);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if(getDevice().equals("CAMERA")) {

            if (!ServerSQLHelper.DbHelper(getApplicationContext()).tableExist(ServerSQLHelper.CAMERAS_TABLE_NAME)) {

                ServerSQLHelper.DbHelper(getApplicationContext()).createTable(ServerSQLHelper.CAMERAS_TABLE_NAME);

                Log.d(TAG, "CREATE TABLE: " + ServerSQLHelper.CAMERAS_TABLE_NAME);

                if (!ServerSQLHelper.DbHelper(getApplicationContext()).tableExist(ServerSQLHelper.CAMERAS_SETTINGS_TABLE_NAME)) {
                    ServerSQLHelper.DbHelper(getApplicationContext()).createTable(ServerSQLHelper.CAMERAS_SETTINGS_TABLE_NAME);
                    Log.d(TAG, "CREATE TABLE: " + ServerSQLHelper.CAMERAS_SETTINGS_TABLE_NAME);
                }
            }
        }

        if(getDevice().equals("CONTROLLER")) {

            if (!ServerSQLHelper.DbHelper(getApplicationContext()).tableExist(ServerSQLHelper.CONTROLLERS_TABLE_NAME)) {

                ServerSQLHelper.DbHelper(getApplicationContext()).createTable(ServerSQLHelper.CONTROLLERS_TABLE_NAME);

                if (!ServerSQLHelper.DbHelper(getApplicationContext()).tableExist(ServerSQLHelper.CONTROLLERS_SETTINGS_TABLE_NAME)) {
                    ServerSQLHelper.DbHelper(getApplicationContext()).createTable(ServerSQLHelper.CONTROLLERS_SETTINGS_TABLE_NAME);
                }
            }
        }

        if(getDevice().equals("DETECTOR")) {

            if (!ServerSQLHelper.DbHelper(getApplicationContext()).tableExist(ServerSQLHelper.DETECTORS_TABLE_NAME)) {

                ServerSQLHelper.DbHelper(getApplicationContext()).createTable(ServerSQLHelper.DETECTORS_TABLE_NAME);

                if (!ServerSQLHelper.DbHelper(getApplicationContext()).tableExist(ServerSQLHelper.DETECTORS_SETTINGS_TABLE_NAME)) {
                    ServerSQLHelper.DbHelper(getApplicationContext()).createTable(ServerSQLHelper.DETECTORS_SETTINGS_TABLE_NAME);
                }
            }
        }

        start_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if(getDeviceModel().equals("Select") || getDeviceModel().equals("اختيار")){
                    choose_model_title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                    device_model_error_message.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                    device_model_error_message.setVisibility(View.VISIBLE);
                    device_model_error_message.setText(R.string.server_add_device_choose_device_model_error_message_text);
                    return;
                }

                device_model_error_message.setVisibility(View.GONE);
                choose_device_model_layout.setVisibility(View.GONE);
                device_options_layout.setVisibility(View.VISIBLE);

                start_button.setText(R.string.button_next); // Change START button text to NEXT

                options_device_name_input.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

                // START button now named NEXT
                start_button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // check the chosen name
                        if(Tools.validateDeviceName(options_device_name_input, 3, 8).equals("ZERO_ERROR")){
                            options_device_name_input.setError(device_name_zero_error);
                            options_device_name_input.setHintTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                        }
                        else if(Tools.validateDeviceName(options_device_name_input, 3, 8).equals("MIN_ERROR")){
                            options_device_name_input.setError(device_name_min_error);
                            options_device_name_input.setHintTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                        }
                        else if(Tools.validateDeviceName(options_device_name_input, 3, 8).equals("MAX_ERROR")){
                            options_device_name_input.setError(device_name_max_error);
                            options_device_name_input.setHintTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                        }
                        else
                        {
                            _device_name   = Objects.requireNonNull(options_device_name_input.getText()).toString().toUpperCase();
                            String _model  = getDeviceModel();
                            String _device = getDevice();
                            String _ssid   = getDeviceSsid();

                            /*
                              Check if the device name in database, here the device SSID is empty because we did not yet scan the wifi for device SSID
                              if duplicated will try to delete device from database but before we get the password from database for maybe resetting the device
                            */
                            String deviceDuplicated = ServerSQLHelper.dbHelper.isDeviceDuplicated(_device, _device_name, _ssid);

                            Log.i(TAG, "DUPLICATION -> " + deviceDuplicated);

                            /*
                              If chosen name is duplicated in database,
                              at this moment we don't know if we add
                              a new device or installed device
                            */
                            if(deviceDuplicated.equals("NAME_DUPLICATED")){

                                int update_setup_ok    = -1;
                                String updated_ap_ssid = "EMPTY";
                                String updated_ap_pass = "EMPTY";
                                String updated_net_ip  = "EMPTY";

                                // if the device found in database
                                if(ServerSQLHelper.dbHelper.getDeviceSetupOk(_device, _device_name) != -1){
                                    // IF DEVICE DUPLICATED we get device setup_ok before delete it
                                    update_setup_ok    = ServerSQLHelper.dbHelper.getDeviceSetupOk(_device, _device_name);
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(), "Setup not finished correctly SETUP_OK = -1", Toast.LENGTH_SHORT).show();
                                }

                                // if the device found in database
                                if(!ServerSQLHelper.dbHelper.getDeviceApSsid(_device, _device_name).equals("NO_DEVICE_FOUND")){
                                    // IF DEVICE DUPLICATED we get device ap ssid before delete it
                                    updated_ap_ssid = ServerSQLHelper.dbHelper.getDeviceApSsid(_device, _device_name);
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(), "Setup not finished correctly SSID = EMPTY or NOT SET", Toast.LENGTH_SHORT).show();
                                }

                                // if the device found in database
                                if(!ServerSQLHelper.dbHelper.getDeviceApPassword(_device, _device_name).equals("NO_DEVICE_FOUND")){
                                    // IF DEVICE DUPLICATED we get device ap password before delete it
                                    updated_ap_pass = ServerSQLHelper.dbHelper.getDeviceApPassword(_device, _device_name);
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(), "Setup not finished correctly PASSWORD = EMPTY or NOT SET", Toast.LENGTH_SHORT).show();
                                }

                                // if the device found in database
                                if(!ServerSQLHelper.dbHelper.getDeviceNetIp(_device, _device_name).equals("NO_DEVICE_FOUND")) {

                                    // IF DEVICE DUPLICATED we get device ap ip before delete it
                                    updated_net_ip = ServerSQLHelper.dbHelper.getDeviceNetIp(_device, _device_name);
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(), "Setup not finished correctly NET_IP = EMPTY or NOT SET", Toast.LENGTH_SHORT).show();
                                }

                                Log.d(TAG, "GET RESET DATA: SETUP_OK [" + update_setup_ok + "] - SSID [" + updated_ap_ssid + "] - PASSWORD [" + updated_ap_pass + "] - IP [" + updated_net_ip + "]");

                                // set this password as temp password in preferences TEMP_RESET_PASSWORD
                                Tools.setStringPref(getApplicationContext(), "TEMP_RESET_PASSWORD", updated_ap_pass);

                                String _msg = "";

                                if (LocalHelper.getLanguage(NewDeviceSetup.this).equalsIgnoreCase("ar")) {
                                    _msg = "الجهاز من توع [" + _device_name + "] والاسم [" + _device + "] يتضارب مع جهاز آخر في قاعدة البيانات. الرجاء اختيار إسم آخر";
                                }
                                else if(LocalHelper.getLanguage(NewDeviceSetup.this).equalsIgnoreCase("en")){
                                    _msg = "The [" + _device + "] name [" + _device_name + "] is (DUPLICATED) in database, please choose other name or delete the device present in database to be able adding this device";
                                }
                                else // FR
                                {
                                    _msg = "The [" + _device + "] name [" + _device_name + "] is (DUPLICATED) in database, please choose other name or delete the device present in database to be able adding this device";
                                }

                                Log.e(TAG, _msg);

                                // change helper text to error
                                options_device_name_layout.setHelperText(_msg);
                                // change helper text color to error
                                options_device_name_layout.setHelperTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.error)));
                                // Show options error
                                device_options_error.setVisibility(View.VISIBLE);
                                // change options error text to error
                                device_options_error.setText(R.string.server_add_device_security_check_save_device_duplicated_name_error);
                                // Hide reset button to reset device
                                reset_device_button.setVisibility(View.GONE);
                                // Show control buttons layout
                                control_layout.setVisibility(View.VISIBLE);
                                // Show delete button
                                delete_device_button.setVisibility(View.VISIBLE);
                                // Show cancel button to to cancel add new device process and go to MainActivity
                                cancel_button.setVisibility(View.VISIBLE);
                                // Show delete button
                                start_button.setVisibility(View.VISIBLE);
                                // rename start button to Change Name
                                start_button.setText(R.string.button_change_name);

                                // DELETE button, will delete device from database
                                delete_device_button.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View view) {

                                        //Show Loading Dialog to indicate deleting process
                                        Tools.LoadingDialog(
                                                NewDeviceSetup.this,
                                                NewDeviceSetup.this,
                                                R.string.server_add_device_delete_device_loading_dialog_label,
                                                R.string.server_add_device_delete_device_loading_dialog_msg,
                                                R.string.server_add_device_delete_device_loading_dialog_text
                                        );

                                        String del_table = "";
                                        String _attach_server_name = getAttachServerName();

                                        if(_device.equals("CAMERA")){
                                            del_table = ServerSQLHelper.CAMERAS_TABLE_NAME;
                                        }

                                        if(_device.equals("CONTROLLER")){
                                            del_table = ServerSQLHelper.CONTROLLERS_TABLE_NAME;
                                        }

                                        if(_device.equals("DETECTOR")){
                                            del_table = ServerSQLHelper.DETECTORS_TABLE_NAME;
                                        }

                                        if(ServerSQLHelper.dbHelper.deleteDatabaseInterfaceDevice(_attach_server_name, del_table, _device_name).equals("SUCCESS_DEVICE_DELETED")){

                                            //Hide deleting Loading Dialog
                                            Tools.CloseLoadingDialog();

                                            String _msg = "";

                                            if (LocalHelper.getLanguage(NewDeviceSetup.this).equalsIgnoreCase("ar")) {
                                                _msg = "الجهاز من توع [" + _device_name + "] والاسم [" + _device + "] يتضارب مع جهاز آخر في قاعدة البيانات. الرجاء اختيار إسم آخر";
                                            }
                                            else if(LocalHelper.getLanguage(NewDeviceSetup.this).equalsIgnoreCase("en")){
                                                _msg = "The [" + _device + "] name [" + _device_name + "] (DELETED) successfully from database, you can choose the same name ["+ _device_name +"] for your device";
                                            }
                                            else // FR
                                            {
                                                _msg = "The [" + _device + "] name [" + _device_name + "] (DELETED) successfully from database, you can choose the same name ["+ _device_name +"] for your device";
                                            }

                                            // change helper text to success
                                            options_device_name_layout.setHelperText(_msg);
                                            // change helper text color to success
                                            options_device_name_layout.setHelperTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.success)));
                                            //Show options error
                                            device_options_error.setVisibility(View.VISIBLE);
                                            // change options error text color to success
                                            device_options_error.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.success)));
                                            // Set message
                                            device_options_error.setText(R.string.server_add_device_delete_device_success_message);
                                            //Hide delete button
                                            delete_device_button.setVisibility(View.GONE);
                                            //Show start button
                                            start_button.setVisibility(View.VISIBLE);
                                            //Rename start button to Set Name
                                            start_button.setText(R.string.button_set_name);
                                            // empty name field
                                            options_device_name_input.setText("");

                                            // cancel button still hidden
                                        }
                                        else
                                        {
                                            //Hide Loading Dialog
                                            Tools.CloseLoadingDialog();
                                            device_options_error.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.error))); // change helper text color to error
                                            device_options_error.setText(R.string.server_add_device_delete_device_error_message);

                                            control_layout.setVisibility(View.VISIBLE);
                                            start_button.setVisibility(View.GONE);
                                            reset_device_button.setVisibility(View.GONE);
                                            cancel_button.setVisibility(View.GONE);
                                            delete_device_button.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });

                                cancel_button.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(NewDeviceSetup.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                });

                                return;
                            }

                            setDeviceName(_device_name);

                            choose_device_model_layout.setVisibility(View.GONE);
                            device_options_layout.setVisibility(View.GONE);
                            control_layout.setVisibility(View.GONE);
                            start_button.setVisibility(View.GONE);
                            reset_device_button.setVisibility(View.GONE);
                            delete_device_button.setVisibility(View.GONE);
                            cancel_button.setVisibility(View.GONE);

                            wifi_scan_layout.setVisibility(View.VISIBLE);

                            scan_button.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    scanWifiNetworks(getApplicationContext());
                                }
                            });
                        }

                        Log.i(TAG, "DEVICE:         " + getDevice());
                        Log.i(TAG, "DEVICE MODEL:   " + getDeviceModel());
                        Log.i(TAG, "DEVICE NAME:    " + getDeviceName());
                    }
                });

                cancel_button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(NewDeviceSetup.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
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

    private void setAttachServerName(String server_name){
        _ATTACH_SERVER_NAME = server_name;
    }

    private String getAttachServerName(){
        return _ATTACH_SERVER_NAME;
    }
    private void setDevice(String device){
        _DEVICE = device;
    }

    private String getDevice(){
        return _DEVICE;
    }

    private void setDeviceModel(String model){
        _MODEL = model;
    }

    private String getDeviceModel(){
        return _MODEL;
    }

    private void setDeviceName(String name){
        _NAME = name;
    }

    private String getDeviceName(){
        return _NAME;
    }

    private void setDeviceSsid(String ssid){_SSID = ssid;}

    private String getDeviceSsid(){
        return _SSID;
    }

    public void resetDevice(final Context context, String jsonUrl, String question, String security, String where){

        //Show Loading Dialog to indicate resetting process
        Tools.LoadingDialog(
                NewDeviceSetup.this,
                NewDeviceSetup.this,
                R.string.server_add_device_reset_device_loading_dialog_label,
                R.string.server_add_device_reset_device_loading_dialog_msg,
                R.string.server_add_device_reset_device_loading_dialog_text
        );

        Map<String, String> postParam = new HashMap<String, String>();

        postParam.put("QUESTION", question);
        postParam.put("SECURITY", security);
        postParam.put("WHERE", where);

        RetryPolicy mRetryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, jsonUrl, new JSONObject(postParam), response -> {

            System.out.println("DEVICE SETUP RESET DATA RESPONSE: " + response.toString());

            if(!response.toString().isEmpty()) {

                try {

                    JSONObject jsonObject    = new JSONObject(response.toString());
                    JSONObject main_response = new JSONObject(jsonObject.getString("RESPONSE"));
                    String device_status     = main_response.getString("STATUS");

                    if(device_status.equals("RESET_DEVICE_OK")){

                        RESET_DEVICE_RESULT_STRING = device_status;

                        if(where.equals("ALL_RESET")){
                            Tools.CloseLoadingDialog();
                            deviceAllReset(RESET_DEVICE_RESULT_STRING);
                        }

                        // We found the this device was setup before or there is an other error on device
                        if(where.equals("RESET_SETUP_OK")){
                            deviceSetupOkReset(RESET_DEVICE_RESULT_STRING);
                        }

                        if(where.equals("APP_ID_KO")){

                        }

                        if(where.equals("NAME_DUPLICATED")){

                            deviceNameDuplicatedReset(RESET_DEVICE_RESULT_STRING);
                        }

                    }

                    if(device_status.equals("RESET_DEVICE_KO")){

                        RESET_DEVICE_RESULT_STRING = "RESET_DEVICE_KO";


                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },

                error -> System.out.println("Get reset device Error: " + error.getMessage())) {
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

    private void deviceAllReset(String resetStatus){

        if(resetStatus.equals("RESET_DEVICE_OK")){

            setAttachServerName("NEW");
            setDevice("NEW");
            setDeviceModel("NEW");
            setDeviceName("NEW");
            setDeviceSsid("NEW");

            // Rename start button text to Start
            start_button.setText(R.string.button_start);
            // Hide Wifi scan layout
            wifi_scan_layout.setVisibility(View.GONE);
            // Hide device option layout
            device_options_layout.setVisibility(View.GONE);
            // Show choose device model layout
            choose_device_model_layout.setVisibility(View.VISIBLE);

            device_model_error_message.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.success)));
            device_model_error_message.setText(R.string.server_add_device_reset_device_success_message);
            device_model_error_message.setVisibility(View.VISIBLE);

            // Show control buttons layout
            control_layout.setVisibility(View.VISIBLE);

            // Show start buttons layout
            start_button.setVisibility(View.VISIBLE); // Hide start button after device reset

            // Hide cancel button
            cancel_button.setVisibility(View.GONE);

            delete_device_button.setVisibility(View.VISIBLE); // Show delete button after device reset

            reset_device_button.setVisibility(View.GONE);// Hide reset button after device reset
        }
    }

    private void deviceSetupOkReset(String resetStatus){

        if(resetStatus.equals("RESET_DEVICE_OK")){

            scan_error_msg.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.success)));
            scan_error_msg.setText(R.string.server_add_device_reset_device_success_message);

            if(getDevice().equals("CAMERA")){
                DEVICE_IP = Tools.DEFAULT_CAMERAS_AP_IP;
            }

            if(getDevice().equals("DETECTOR")){
                DEVICE_IP = Tools.DEFAULT_DETECTORS_AP_IP;
            }

            if(getDevice().equals("CONTROLLER")){
                DEVICE_IP = Tools.DEFAULT_CONTROLLERS_AP_IP;
            }

            URL       = "http://" + DEVICE_IP + "/device_control";

            Log.d(TAG, "GO TO: " + URL);

            restartDeviceNetwork(getApplicationContext(), URL, "RESTART_NETWORK");
        }
        else
        {

            scan_error_msg.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.error)));
            scan_error_msg.setText(R.string.server_add_device_reset_device_config_error_message);

            reset_device_button.setVisibility(View.VISIBLE);
        }

    }

    private void deviceNameDuplicatedReset(String resetStatus){

        if(resetStatus.equals("RESET_DEVICE_OK")){
            device_options_error.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.success)));
            device_options_error.setText(R.string.server_add_device_reset_device_success_message);
        }
        else
        {
            device_options_error.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.error)));
            device_options_error.setText(R.string.server_add_device_reset_device_config_error_message); // we can not reset device from application so we ask user to reset his device manually

            reset_device_button.setVisibility(View.VISIBLE);
            reset_device_button.setText(R.string.button_restart); // we change button text to RESTART to restart installation


            reset_device_button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                }
            });
        }
    }

    public void restartDeviceNetwork(final Context context, String jsonUrl, String question){

        Map<String, String> postParam = new HashMap<String, String>();

        postParam.put("QUESTION", question);

        RetryPolicy mRetryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, jsonUrl, new JSONObject(postParam), response -> {

            System.out.println("RESTART DEVICE NETWORK DATA RESPONSE: " + response.toString());

            if(!response.toString().isEmpty()) {

                try {

                    JSONObject jsonObject    = new JSONObject(response.toString());
                    JSONObject main_response = new JSONObject(jsonObject.getString("RESPONSE"));
                    String device_status     = main_response.getString("STATUS");

                    if(device_status.equals("RESTART_NETWORK_OK")){

                        reset_device_button.setText(R.string.button_continue);  // change to CONTINUE

                        reset_device_button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) { // On click on CONTINUE
                                scan_error_msg.setText("");
                                scan_error_msg.setVisibility(View.GONE);
                                control_layout.setVisibility(View.GONE);
                                start_button.setVisibility(View.GONE);
                                reset_device_button.setVisibility(View.GONE);
                                delete_device_button.setVisibility(View.GONE);
                                cancel_button.setVisibility(View.GONE);
                                apView.setText(R.string.server_add_device_ap_view_after_reset_message);
                                scan_button.setText(R.string.button_rescan);
                            }
                        });
                    }

                    if(device_status.equals("RESTART_NETWORK_KO")){



                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },

                error -> System.out.println("Get restart device network Error: " + error.getMessage())) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case MY_PERMISSIONS_ACCESS_COARSE_LOCATION:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(getApplicationContext(), "permission granted", Toast.LENGTH_SHORT).show();

                    apView.setText(R.string.scanning_points_text);

                    scanWifiNetworks(getApplicationContext());
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "permission not granted", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }
    }

    private void scanWifiNetworks(Context context) {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(setupAddDeviceWifiReceiver, intentFilter);

        wifiManager.startScan();

        Toast.makeText(getApplicationContext(), "Scanning....", Toast.LENGTH_SHORT).show();
    }

    final BroadcastReceiver setupAddDeviceWifiReceiver = new BroadcastReceiver() {

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.w(TAG, "BroadcastReceiver / onReceive");

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            results = wifiManager.getScanResults();
            size = results.size();
            context.unregisterReceiver(this);

            apView.setText("");

            boolean cyrenaica_devices = false;

            String DEVICE = getDevice();
            String MODEL  = getDeviceModel();
            String NAME   = getDeviceName();
            String SSID   = "";

            try {

                for(int i = 0; i < size; i++){

                    ScanResult res = results.get(i);
                    SSID            = res.SSID;

                    System.out.println("SSID: " + SSID);

                    if (SSID.contains("CYRENAICA")) {

                        cyrenaica_devices = true;

                        if(DEVICE.equals("CAMERA")){

                            if (SSID.contains("CAMERA")) {

                                if (res.capabilities.toUpperCase().contains("WEP")) {
                                    conn_encryption = "WEP";
                                }
                                else if (res.capabilities.toUpperCase().contains("WPA")) {
                                    conn_encryption = "WPA";
                                }
                                else {
                                    conn_encryption = "OPEN";
                                }

                                conn_wifiL    = res.level;
                                conn_channelW = res.channelWidth;

                                signalLevel   = WifiManager.calculateSignalLevel(conn_wifiL, 4);
                                level         = res.level;
                                bssid         = res.BSSID;

                                break;
                            }
                            else
                            {
                                //This device is CAMERA but SSID not contain CAMERA string
                            }
                        }
                        else if(DEVICE.equals("CONTROLLER")){

                            if (SSID.contains("CONTROLLER")) {

                                if (res.capabilities.toUpperCase().contains("WEP")) {
                                    conn_encryption = "WEP";
                                }
                                else if (res.capabilities.toUpperCase().contains("WPA")) {
                                    conn_encryption = "WPA";
                                }
                                else {
                                    conn_encryption = "OPEN";
                                }

                                conn_wifiL    = res.level;
                                conn_channelW = res.channelWidth;

                                signalLevel   = WifiManager.calculateSignalLevel(conn_wifiL, 4);
                                level         = res.level;
                                bssid         = res.BSSID;

                                break;
                            }
                            else
                            {
                                //This device is CONTROLLER but SSID not contain CONTROLLER string
                            }
                        }
                        else if(DEVICE.equals("DETECTOR")){

                            if (SSID.contains("DETECTOR")) {

                                if (res.capabilities.toUpperCase().contains("WEP")) {
                                    conn_encryption = "WEP";
                                }
                                else if (res.capabilities.toUpperCase().contains("WPA")) {
                                    conn_encryption = "WPA";
                                }
                                else {
                                    conn_encryption = "OPEN";
                                }

                                conn_wifiL    = res.level;
                                conn_channelW = res.channelWidth;

                                signalLevel   = WifiManager.calculateSignalLevel(conn_wifiL, 4);
                                level         = res.level;
                                bssid         = res.BSSID;

                                break;
                            }
                            else
                            {
                                //This device is DETECTOR but SSID not contain DETECTOR string
                            }
                        }
                        else
                        {
                            //This device is cyrenaica but not supported yet
                        }
                    }
                    else
                    {
                        cyrenaica_devices = false;
                    }
                }

                // Search this device if it in database, no connection to device yet
                cyrenaicaDevicesSearch(cyrenaica_devices, DEVICE, MODEL, NAME, SSID, conn_encryption, signalLevel, level, bssid);

            }
            catch (Exception e) {
                Log.w("WifScanner", "Exception: " + e);
            }
        }
    };

    /* We search in database, if  */
    public void cyrenaicaDevicesSearch(boolean cyrenaica_devices, String device, String model, String name, String ssid, String encryption, int signal_level, int level, String bssid){

        Log.i(TAG, "cyrenaicaDevicesSearch: ");

        Log.i(TAG, "cyrenaica_devices: " + cyrenaica_devices);
        Log.i(TAG, "device:            " + device);
        Log.i(TAG, "model:             " + model);
        Log.i(TAG, "name:              " + name);
        Log.i(TAG, "ssid:              " + ssid);

        if(cyrenaica_devices){

            setDeviceSsid(ssid);

            String deviceDuplicated = ServerSQLHelper.dbHelper.isDeviceDuplicated(device, name, ssid);

            Log.d("DEVICE_SEARCH_METHOD", "DUPLICATION: " + deviceDuplicated);

            // Check if device SSID is Duplicated in database, we chosen NAME = name but name not Duplicated
            if(deviceDuplicated.equals("SSID_DUPLICATED")){

                // Get this DUPLICATED device ap password for reset
                String _duplicated_device_ap_password = ServerSQLHelper.dbHelper.getDeviceApPassword(device, name);
                // Get this DUPLICATED device ap ip for reset
                String _duplicated_device_ap_ip      = ServerSQLHelper.dbHelper.getDeviceApIp(device, name);

                // show error message
                Log.w(TAG, "The [" + device + "] you trying to add with SSID [" + ssid + "] and NAME [" + name + "] is (DUPLICATED) in database");

                String _msg ="The [" + device + "] you trying to add with SSID [" + ssid + "] and NAME [" + name + "] is (DUPLICATED) in database";

                 // Set error message to field
                options_device_name_layout.setHelperText(_msg);
                options_device_name_layout.setHelperTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.error)));
                device_options_error.setText(R.string.server_add_device_security_check_save_device_duplicated_name_ssid_error);

                // Show control buttons layout
                control_layout.setVisibility(View.VISIBLE);
                // Show reset button to reset on device
                reset_device_button.setVisibility(View.VISIBLE);
                // Rename reset button to Reset Device
                reset_device_button.setText(R.string.button_reset_device);
                // Show start button
                start_button.setVisibility(View.VISIBLE);
                // Rename start button to Change Name
                start_button.setText(R.string.button_change_name);
                // Show cancel button
                cancel_button.setVisibility(View.VISIBLE);

                start_button.setOnClickListener(new View.OnClickListener() { // On click on Change Name

                    @Override
                    public void onClick(View view) {
                        //TODO: Change Name

                        setAttachServerName("NEW");
                        setDevice("NEW");
                        setDeviceModel("NEW");
                        setDeviceName("NEW");
                        setDeviceSsid("NEW");

                        // Rename start button text to Start
                        start_button.setText(R.string.button_start);
                        // Hide Wifi scan layout
                        wifi_scan_layout.setVisibility(View.GONE);
                        // Hide device option layout
                        device_options_layout.setVisibility(View.GONE);
                        // Show choose device model layout
                        choose_device_model_layout.setVisibility(View.VISIBLE);

                    }
                });

                reset_device_button.setOnClickListener(new View.OnClickListener() { // On click on Reset Device

                    @Override
                    public void onClick(View view) {
                        //TODO: Reset Device

                        Tools.hideSoftKeyboard(NewDeviceSetup.this);

                        WifiConfiguration wifiConfig = new WifiConfiguration();
                        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                        int netId = wifiManager.addNetwork(wifiConfig);

                        wifiManager.removeNetwork(netId);
                        wifiManager.saveConfiguration();

                        if (netId != -1) {
                            wifiManager.enableNetwork(netId, false);
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                            WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
                            builder.setSsid(ssid);
                            builder.setWpa2Passphrase(_duplicated_device_ap_password);

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

                                            DEVICE_IP = _duplicated_device_ap_ip;

                                            _SECURITY = Tools.APP_ID;

                                            URL = "http://" + DEVICE_IP + "/device_control";

                                            Log.w(TAG, "SENDING REQUEST TO: " + URL);
                                            Log.i(TAG, "QUESTION: " + "RESET_DEVICE");

                                            resetDevice(getApplicationContext(), URL, "RESET_DEVICE", _SECURITY, "ALL_RESET");
                                        }
                                    }
                                }
                            };

                            cm.requestNetwork(nr, networkCallback);
                        }
                    }
                });

                cancel_button.setOnClickListener(new View.OnClickListener() { // On click on Cancel Device

                    @Override
                    public void onClick(View view) {
                        //TODO: Reset Device

                        setAttachServerName("NEW");
                        setDevice("NEW");
                        setDeviceModel("NEW");
                        setDeviceName("NEW");
                        setDeviceSsid("NEW");

                        Intent intent = new Intent(NewDeviceSetup.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });

                return;
            }

            apView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.orange));
            apView.setAlpha((float) 1);
            apView.setText(ssid);

            net_secure_description.setText(String.valueOf(encryption));
            wifi_level_description.setText(String.valueOf(level));
            macAddressLabel.setText(String.valueOf(bssid));

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
            else
            {
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
            // Not cyrenaica device
            apView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.orange));
            apView.setText(R.string.server_add_device_no_cyrenaica_devices_nearby_message);
            return;
        }

        apView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDevicePasswordDialog(v);
            }
        });

        Log.d(TAG, "SSID: " + ssid + " - ENCRYPTION: " + encryption + " - LEVEL: " + level + " - SIGNAL: " + signal_level + " - MAC: " + bssid);

    }

    public void setDevicePasswordDialog(View view) {

        Log.i(TAG, "setDevicePasswordDialog: ");

        Tools.CURRENT_WIFI_SSID = (String) apView.getText();

        if(Tools.CURRENT_WIFI_SSID.contains("CYRENAICA")) {

            final AlertDialog.Builder alert                                             = new AlertDialog.Builder(NewDeviceSetup.this);
            View mView                                                                  = getLayoutInflater().inflate(R.layout.wifi_connect_dialog, null);

            final TextView connectDialog_label                                          = mView.findViewById(R.id.id_dialog_label);
            final com.google.android.material.textfield.TextInputLayout passwordLayout  = mView.findViewById(R.id.id_router_info_dialog_input_layout);
            final com.google.android.material.textfield.TextInputEditText password      = mView.findViewById(R.id.id_dialog_input_field);
            final String dialogPassword                                                 = password.getText().toString();
            final TextView dialog_default_message                                       = mView.findViewById(R.id.id_dialog_default_message);
            final TextView validationMsg                                                = mView.findViewById(R.id.passwordValidationMsg);
            final TextView btn_cancel                                                   = mView.findViewById(R.id.id_btn_cancel);
            final TextView btn_okay                                                     = mView.findViewById(R.id.id_btn_retry);

            if (LocalHelper.getLanguage(getApplicationContext()).equalsIgnoreCase("ar")) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

            String _device_type = getDevice();

            if(_device_type.equals("CAMERA")){

                _SECURITY = Tools.DEFAULT_CAMERA_APP_ID;

                connectDialog_label.setText(R.string.server_add_device_camera_wifi_connect_dialog_title);
                dialog_default_message.setText(R.string.server_add_device_wifi_camera_setup_password_field_alert);
            }

            if(_device_type.equals("CONTROLLER")){

                _SECURITY = Tools.DEFAULT_CONTROLLER_APP_ID;

                connectDialog_label.setText(R.string.server_add_device_controller_wifi_connect_dialog_title);
                dialog_default_message.setText(R.string.server_add_device_wifi_controller_setup_password_field_alert);
            }

            if(_device_type.equals("DETECTOR")){

                _SECURITY = Tools.DEFAULT_DETECTOR_APP_ID;

                connectDialog_label.setText(R.string.server_add_device_detector_wifi_connect_dialog_title);
                dialog_default_message.setText(R.string.server_add_device_wifi_detector_setup_password_field_alert);
            }

            btn_okay.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (!Tools.isPasswordValid(password.getText().toString())) {

                        password.setError("Please enter the password, current is: " + password.getText().toString());
                        return;
                    }

                    Tools.hideSoftKeyboard(NewDeviceSetup.this);

                    WifiConfiguration wifiConfig = new WifiConfiguration();
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    int netId = wifiManager.addNetwork(wifiConfig);

                    wifiManager.removeNetwork(netId);
                    wifiManager.saveConfiguration();

                    if (netId != -1) {
                        wifiManager.enableNetwork(netId, false);
                    }

                    TEMP_RESET_PASSWORD = Tools.getStringPref(getApplicationContext(), "TEMP_RESET_PASSWORD");

                    if(Objects.equals(password.getText().toString(), Tools.DEFAULT_WIFI_PASSWORD) || Objects.equals(password.getText().toString(), TEMP_RESET_PASSWORD)){

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

                        WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
                        builder.setSsid(Tools.CURRENT_WIFI_SSID);
                        builder.setWpa2Passphrase(password.getText().toString());

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

                                Tools.LIVE_WIFI_STATUS = "AVAILABLE";

                                Log.d("ON_AVAILABLE", "Available Network" + network.toString());

                                Log.d(TAG, "CONNECTED TO: SSID [" + Tools.CURRENT_WIFI_SSID + "] PASS [" + password.getText().toString() + "]");

                                cm.bindProcessToNetwork(network);

                                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);

                                if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

                                    alertDialog.dismiss();

                                    if(cm.isDefaultNetworkActive()){

                                        cm.reportNetworkConnectivity(network, false);

                                        Tools.setStringPref(getApplicationContext(), "CURRENT_WIFI_SSID", Tools.CURRENT_WIFI_SSID);

                                        Log.w(TAG, "GO TO checkDeviceSetup");

                                        DEVICE_IP = Tools.getConnectedApIp(getApplicationContext());

                                        URL = "http://" + DEVICE_IP + "/setup_needed";

                                        Log.w(TAG, "SENDING REQUEST TO: " + URL);
                                        Log.i(TAG, "QUESTION: " + "IS_SETUP_NEEDED");

                                        checkDeviceSetup(getApplicationContext(), URL, _device_type, _SECURITY, "IS_SETUP_NEEDED");
                                    }
                                }
                            }

                            @Override
                            public void onLosing(@NonNull Network network, int maxMsToLive) {
                                super.onLosing(network, maxMsToLive);
                                Log.d("ON_LOSING", "Losing Network");
                            }

                            @Override
                            public void onLost(@NonNull Network network) {
                                super.onLost(network);
                                Log.d("ON_LOST", "Network Lost");

                            }

                            @Override
                            public void onUnavailable() {
                                super.onUnavailable();
                                Log.d("ON_UNAVAILABLE", "Network is Unavailable");

                            }

                            @Override
                            public void onCapabilitiesChanged(@NonNull Network network, NetworkCapabilities networkCapabilities) {
                                WifiInfo wifiInfo = (WifiInfo) networkCapabilities.getTransportInfo();
                                int down_stream   = networkCapabilities.getLinkDownstreamBandwidthKbps();
                                int up_stream     = networkCapabilities.getLinkUpstreamBandwidthKbps();
                                Log.d("ON_CAPABILITIES_CHANGED", "DWN STREAM [" + down_stream + "] - UP STREAM [" + up_stream + "]");
                            }

                            @Override
                            public void onLinkPropertiesChanged(@NonNull Network network, LinkProperties linkProperties){
                                List<RouteInfo> mm;
                                List<InetAddress> _dns;
                                List<LinkAddress> _ip;

                                _dns = linkProperties.getDnsServers();
                                _ip = linkProperties.getLinkAddresses();
                                Log.d("ON_PROPERTIES_CHANGED", "PROPS DNS [" + _dns + "] - PROPS IP [" + _ip + "]");

                            }

                            @Override
                            public void onBlockedStatusChanged(@NonNull Network network, boolean blocked){
                                Log.d(TAG, "ON_BLOCKED -> BLOCKED [" + blocked + "] ");
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

    public void checkDeviceSetup(final Context context, String jsonUrl, String type, String security, String question) {

        Map<String, String> postParam = new HashMap<String, String>();

        postParam.put("QUESTION", question);
        postParam.put("SECURITY", "GET_SECURITY");
        postParam.put("APP_ID", security);

        RetryPolicy mRetryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, jsonUrl, new JSONObject(postParam), response -> {

            System.out.println("DEVICE SETUP CHECK DATA RESPONSE: " + response.toString());

            if(!response.toString().isEmpty()) {

                try {

                    JSONObject jsonObject    = new JSONObject(response.toString());
                    JSONObject main_response = new JSONObject(jsonObject.getString("RESPONSE"));
                    String device_status     = main_response.getString("STATUS");
                    String default_app_id    = "";
                    String setup_ok          = "";
                    boolean _setup_ok        = false;

                    if(device_status.equals("SETUP_OK_CHECK")){

                        setup_ok = main_response.getString("SETUP_OK");
                        _setup_ok = Tools.convertEspBoolean(setup_ok);

                        Log.i(TAG, "SETUP_OK: " + _setup_ok);

                        if(_setup_ok){

                            if(type.equals("CAMERA")){
                                _SECURITY = Tools.DEFAULT_CAMERA_APP_ID;
                            }

                            if(type.equals("CONTROLLER")){
                                _SECURITY = Tools.DEFAULT_CONTROLLER_APP_ID;
                            }

                            if(type.equals("DETECTOR")){
                                _SECURITY = Tools.DEFAULT_DETECTOR_APP_ID;
                            }

                            // device setup made / if others checks

                            device_options_layout.setVisibility(View.GONE);
                            control_layout.setVisibility(View.VISIBLE);
                            reset_device_button.setVisibility(View.VISIBLE);
                            cancel_button.setVisibility(View.VISIBLE);
                            scan_error_msg.setVisibility(View.VISIBLE);

                            scan_error_msg.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.orange));
                            scan_error_msg.setText(R.string.server_add_device_check_device_setup_ok_to_reset_error);

                            reset_device_button.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) { // On click on RESET DEVICE

                                    DEVICE_IP = Tools.getConnectedApIp(getApplicationContext());

                                    URL       = "http://" + DEVICE_IP + "/device_control";

                                    Log.i(TAG, "SENDING REQUEST TO: [" + URL + "] WITH QUESTION [RESET_DEVICE] WHERE [RESET_SETUP_OK]");

                                    resetDevice(getApplicationContext(), URL, "RESET_DEVICE", _SECURITY, "RESET_SETUP_OK");

                                }
                            });

                            cancel_button.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(NewDeviceSetup.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                        else
                        {
                            default_app_id = main_response.getString("DEFAULT_APP_ID");

                            if(default_app_id.isEmpty() || default_app_id.equals("EMPTY")) {

                                System.out.println("DEFAULT_APP_ID RESPONSE: " + default_app_id);

                            }
                            else
                            {
                                String QUESTION = "";

                                // check the default app_id coming from device if it is in defaults app ids in IDS array, if true check for accesspoint
                                if (Tools.checkDefaultAppId(default_app_id)) {

                                    Tools.setStringPref(getApplicationContext(), "DEFAULT_APP_ID", default_app_id);

                                    DEVICE_IP = Tools.getConnectedApIp(getApplicationContext());

                                    if(getDevice().equals("CAMERA")){
                                        QUESTION = "SETUP_CAMERA_ACCESSPOINT";
                                        _SECURITY = Tools.DEFAULT_CAMERA_APP_ID;
                                    }

                                    if(getDevice().equals("CONTROLLER")){
                                        QUESTION = "SETUP_CONTROLLER_ACCESSPOINT";
                                        _SECURITY = Tools.DEFAULT_CONTROLLER_APP_ID;
                                    }

                                    if(getDevice().equals("DETECTOR")){
                                        QUESTION = "SETUP_DETECTOR_ACCESSPOINT";
                                        _SECURITY = Tools.DEFAULT_DETECTOR_APP_ID;
                                    }

                                    Log.i(TAG, "checkAccessPointSetup");

                                    URL = "http://" + DEVICE_IP + "/server_network";

                                    Log.i(TAG, "GO TO: " + URL);
                                    Log.i(TAG, "QUESTION: " + QUESTION);

                                    checkAccessPointSetup(getApplicationContext(), URL, QUESTION);
                                }
                                else
                                {
                                    // Here we found other APP_ID so the setup was made but maybe interrupted
                                    // so we will trying to reset the device and restart the setup process
                                    // the device is connected to application on device's AP so we can send reset request
                                    System.out.println("DEFAULT_APP_ID NOT CORRECT ");

                                    if(getDevice().equals("CAMERA")){
                                        _SECURITY = Tools.DEFAULT_CAMERA_APP_ID;
                                    }

                                    if(getDevice().equals("CONTROLLER")){
                                        _SECURITY = Tools.DEFAULT_CONTROLLER_APP_ID;
                                    }

                                    if(getDevice().equals("DETECTOR")){
                                        _SECURITY = Tools.DEFAULT_DETECTOR_APP_ID;
                                    }

                                    choose_device_model_layout.setVisibility(View.GONE);
                                    choose_model_title.setVisibility(View.GONE);
                                    choose_device_model_spinner.setVisibility(View.GONE);
                                    device_model_error_message.setText("");
                                    device_model_error_message.setVisibility(View.GONE);

                                    device_options_layout.setVisibility(View.GONE);
                                    device_options_error.setVisibility(View.GONE);
                                    device_options_error.setText("");

                                    start_button.setVisibility(View.GONE);
                                    delete_device_button.setVisibility(View.GONE);
                                    wifi_scan_results_frameLayout.setVisibility(View.GONE);
                                    security_check_layout.setVisibility(View.GONE);

                                    reset_device_button.setVisibility(View.VISIBLE);

                                    scan_error_msg.setVisibility(View.VISIBLE);
                                    scan_error_msg.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                                    scan_error_msg.setText(R.string.server_add_device_check_default_id_not_equal_error);

                                    control_layout.setVisibility(View.VISIBLE);

                                    _DEVICE    = "";
                                    _MODEL     = "";
                                    _NAME      = "";
                                    DEVICE_IP  = "";

                                    // the device is connected to application we try to get its IP - DEVICE_IP = AP_IP
                                    DEVICE_IP = Tools.getConnectedApIp(getApplicationContext());

                                    URL       = "http://" + DEVICE_IP + "/device_control";
                                    Log.d(TAG, "SEND RESET REQUEST TO: " + URL);

                                    reset_device_button.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View view) {

                                            resetDevice(getApplicationContext(), URL, "RESET_DEVICE", _SECURITY, "APP_ID_KO");

                                        }
                                    });

                                    return;
                                }
                            }
                        }
                    }

                    if(device_status.equals("SECURITY_ERROR")){

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

    public void checkAccessPointSetup(final Context context, String jsonUrl, String question) {

        Log.i(TAG, "checkAccessPointSetup");

        Map<String, String> postParam = new HashMap<String, String>();

        String _def_app_id     = Tools.getStringPref(getApplicationContext(), "DEFAULT_APP_ID");
        String _ssid           = Tools.getStringPref(getApplicationContext(), "CURRENT_WIFI_SSID");

        _ssid += "_";
        _ssid += getDeviceName();

        String _pass           = Tools.getStringPref(getApplicationContext(), "CURRENT_WIFI_PASS");

        postParam.put("QUESTION", question);
        postParam.put("QUESTION_TYPE", "ACCESSPOINT");
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
                    String server_message    = main_response.getString("MESSAGE");

                    System.out.println("server_status: " + server_status);

                    String _current_device, _name, _model, _ap_ssid, _ap_pass, _ap_ip, _ap_mac, _ap_hostname, _server_ssid, _server_pass, _server_ip, _net_ip, _sta_mac, _freq, _heap, _spiffs, message;
                    int _setup_ok, _ap_max_clients, _ap_channel, _ap_ssid_hidden;

                    if(getDevice().equals("CAMERA")){

                        Log.i(TAG, "Camera server status: ["+ server_status + "] Camera server message: [" + server_message + "]");

                        if(server_status.equals("CAMERA_SETUP_APP_ID_KO")){

                            wifi_scan_results_frameLayout.setVisibility(View.GONE);
                            scan_message.setVisibility(View.GONE);
                            device_model_error_message.setVisibility(View.VISIBLE);
                            device_model_error_message.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                            device_model_error_message.setText(R.string.wifi_setup_security_check_ko);

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
                            if (server_status.equals("CAMERA_SETUP_ACCESS_POINT_OK")) {

                                String _device = getDevice();
                                String _attach_server_name = getAttachServerName();
                                String _router_ssid = ServerSQLHelper.dbHelper.getRouterSSid(_attach_server_name), _router_pass = ServerSQLHelper.dbHelper.getRouterPassword(_attach_server_name);

                                Log.i(TAG, "CAMERA_SETUP_ACCESS_POINT_OK");

                                _setup_ok = 0; // we will change it later to 1 to indicate setup is ok

                                message        = main_response.getString("MESSAGE");

                                //TODO: Before this step at the device setup, Must sync the device AP_IP after connect it to server to get its IP

                                // This id is the device id on the device
                                int ID = ServerSQLHelper.dbHelper.getLastId(ServerSQLHelper.CAMERAS_TABLE_NAME); // = 0

                                int SERVER_ID = ServerSQLHelper.dbHelper.getServerId(_attach_server_name);

                                Log.i(TAG, "CAMERA LAST ID [" + ID + "]");

                                int _ID;

                                if(ID == 0){
                                    _ID = ID;
                                }
                                else
                                {
                                    _ID = ID + 1;
                                }

                                Log.i(TAG, "NEW CAMERA ID [" + _ID + "]");

                                _name           = getDeviceName();
                                _model          = getDeviceModel();
                                _ap_ssid        = main_response.getString("AP_SSID");
                                _ap_pass        = main_response.getString("AP_PASS");
                                _ap_ip          = main_response.getString("AP_IP");
                                _ap_mac         = main_response.getString("AP_MAC");
                                _ap_hostname    = "EMPTY";
                                _ap_max_clients = 1;
                                _ap_ssid_hidden = 0;
                                _ap_channel     = 1;
                                _server_ssid    = "EMPTY";
                                _server_pass    = "EMPTY";
                                _server_ip      = "EMPTY";
                                _net_ip         = "EMPTY";
                                _sta_mac        = "EMPTY";
                                _freq           = "EMPTY";
                                _heap           = "EMPTY";
                                _spiffs         = "EMPTY";

                                String camera_insert_status = ServerSQLHelper.dbHelper.CameraInsert(_ID, SERVER_ID, _setup_ok, _device, _name, _model, _ap_ssid, _ap_pass, _ap_ip, _ap_mac, _ap_hostname, _ap_max_clients, _ap_ssid_hidden, _ap_channel, _server_ssid, _server_pass, _server_ip, _net_ip, _sta_mac, _freq, _heap, _spiffs);

                                if(camera_insert_status.equals("INSERT_OK")){

                                    String device = getDevice();

                                    int cam_key_id = ServerSQLHelper.dbHelper.getDeviceId(device, _name);

                                    String camera_setting_insert_status = ServerSQLHelper.dbHelper.CameraSettingsInsert(cam_key_id, _ID, _name, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, "EMPTY", "EMPTY", _router_ssid, _router_pass, 0, 0, "480x320" /* {480x320} */, 0, 1);

                                    if(camera_setting_insert_status.equals("INSERT_OK")) {

                                        choose_device_model_layout.setVisibility(View.GONE);
                                        device_options_layout.setVisibility(View.GONE);
                                        wifi_scan_layout.setVisibility(View.GONE);
                                        control_layout.setVisibility(View.GONE);

                                        security_check_layout.setVisibility(View.VISIBLE);

                                        Objects.requireNonNull(apSsidLayout.getEditText()).setText(_ap_ssid);

                                        // Check if device still in default password mode
                                        if (_ap_pass.equals(Tools.DEFAULT_WIFI_PASSWORD)) {

                                            Objects.requireNonNull(apPassLayout.getEditText()).setText(_ap_pass);

                                            String hintText = getResources().getString(R.string.wifi_setup_change_default_password_field_hint);
                                            String helperText = getResources().getString(R.string.wifi_setup_default_password_field_helper);

                                            apPassLayout.setHint(hintText);
                                            apPassLayout.setHelperText(helperText);
                                        }
                                        else
                                        {
                                            Objects.requireNonNull(apPassLayout.getEditText()).setText(_ap_pass);

                                            String hintText = getResources().getString(R.string.wifi_setup_current_password_field_hint);

                                            apPassLayout.setHint(hintText);
                                        }

                                        // Click to save changes of default password
                                        security_check_button.setOnClickListener(new View.OnClickListener() {

                                            @Override
                                            public void onClick(View view) {

                                                //TODO: Add loading popup
                                                Tools.LoadingDialog(
                                                        NewDeviceSetup.this,
                                                        NewDeviceSetup.this,
                                                        R.string.server_add_device_save_password_loading_dialog_label,
                                                        R.string.server_add_device_save_password_loading_dialog_msg,
                                                        R.string.server_add_device_save_password_loading_dialog_text
                                                );

                                                // send save device
                                                String updated_ap_pass = Objects.requireNonNull(apPassInput.getText()).toString();

                                                if (updated_ap_pass.equals(Tools.DEFAULT_WIFI_PASSWORD)) {

                                                    Tools.CloseLoadingDialog();

                                                    Objects.requireNonNull(apPassLayout.getEditText()).setText(updated_ap_pass);

                                                    String hintText = getResources().getString(R.string.wifi_setup_change_default_password_field_hint);
                                                    String helperText = getResources().getString(R.string.wifi_setup_default_password_field_helper);

                                                    apPassLayout.setHint(hintText);
                                                    apPassLayout.setHelperText(helperText);

                                                    return;
                                                }

                                                if (ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_AP_PASS, updated_ap_pass, ServerSQLHelper.CAMERA_NAME, _name)) {
                                                    device_security_check_label.setText(R.string.server_add_device_security_check_password_changed_successfully_label);
                                                    device_security_check_label.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.success));
                                                    Tools.CloseLoadingDialog();
                                                }
                                                else
                                                {
                                                    device_security_check_label.setText(R.string.server_add_device_security_check_password_change_error_label);
                                                    device_security_check_label.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                                                    Tools.CloseLoadingDialog();
                                                }

                                                Tools.LoadingDialog(
                                                        NewDeviceSetup.this,
                                                        NewDeviceSetup.this,
                                                        R.string.server_add_device_save_device_loading_dialog_label,
                                                        R.string.server_add_device_save_device_loading_dialog_msg,
                                                        R.string.server_add_device_save_device_loading_dialog_text
                                                );

                                                int ID = ServerSQLHelper.dbHelper.getCameraAttachId(_name);

                                                /* This function check if the new device setup was made or not */
                                                DEVICE_IP = Tools.getConnectedApIp(getApplicationContext());

                                                URL = "http://" + DEVICE_IP + "/server_network";

                                                Log.i(TAG, "saveDevice()");

                                                Log.i(TAG, "GO TO: " + URL);
                                                Log.i(TAG, "QUESTION: " + "SETUP_DEVICE_PREFS");

                                                String _device = getDevice();

                                                saveDevice(getApplicationContext(), URL, "SETUP_DEVICE_PREFS", _device, ID, _name, _model, updated_ap_pass);
                                            }
                                        });
                                    }
                                }

                                if(camera_insert_status.equals("DUPLICATED")){  // Name = duplicates && SSID = duplicated

                                    prefs_status_message_layout.setVisibility(View.VISIBLE);
                                    prefs_status_message_view.setText(R.string.server_add_device_security_check_save_device_duplicated_error);
                                    security_check_button.setText(R.string.button_reinstall);

                                    security_check_button.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View view) {

                                            //TODO: delete device from database, reset device configuration

                                            String del_table = "";

                                            if(_device.equals("CAMERA")){
                                                del_table = ServerSQLHelper.CAMERAS_TABLE_NAME;
                                            }

                                            if(_device.equals("CONTROLLER")){
                                                del_table = ServerSQLHelper.CONTROLLERS_TABLE_NAME;
                                            }

                                            if(_device.equals("DETECTOR")){
                                                del_table = ServerSQLHelper.DETECTORS_TABLE_NAME;
                                            }

                                            if(ServerSQLHelper.dbHelper.deleteDatabaseInterfaceDevice(_attach_server_name, del_table, _device_name).equals("SUCCESS_DEVICE_DELETED")){

                                            }
                                        }
                                    });
                                }

                                if(camera_insert_status.equals("NAME_DUPLICATED")){   // Only Name = duplicates

                                }

                                if(camera_insert_status.equals("SSID_DUPLICATED")){   // Only SSID = duplicates

                                }
                            }
                            else /* if the router setup is not made yet will send user to  SecurityCheckActivity activity so the user have the possibility to modify the router again */
                            {
                                if (server_status.equals("CAMERA_SETUP_AP_SSID_KO")) {

                                    /*Intent intent = new Intent(DeviceSetup.this, SecurityCheckActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    requireActivity().finish();*/
                                }

                                if (server_status.equals("CAMERA_SETUP_AP_PASS_KO")) {

                                }
                            }
                        }
                    }

                    if(getDevice().equals("CONTROLLER")){
                        Log.i(TAG, "Controller server status: ["+ server_status + "] Controller server message: [" + server_message + "]");
                    }

                    if(getDevice().equals("DETECTOR")){
                        Log.i(TAG, "Detector server status: ["+ server_status + "] Detector server message: [" + server_message + "]");
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },

        error -> System.out.println("Get accesspoint setup Error: " + error.getMessage())) {
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

    public void saveDevice(final Context context, String jsonUrl, String question, String device, int id, String name, String model, String apPass) {

        Map<String, String> postParam = new HashMap<String, String>();

        String d_id                = String.valueOf(id);
        String _attach_server_name = getAttachServerName();
        String serv_ap_ip          = ServerSQLHelper.dbHelper.getServerApIp(_attach_server_name), _server_ssid = ServerSQLHelper.dbHelper.getServerSsid(_attach_server_name), _server_pass = ServerSQLHelper.dbHelper.getServerPass(_attach_server_name);
        String _router_ssid        = ServerSQLHelper.dbHelper.getRouterSSid(_attach_server_name), _router_pass = ServerSQLHelper.dbHelper.getRouterPassword(_attach_server_name);

        postParam.put("APP_ID", Tools.APP_ID);
        postParam.put("QUESTION", question);
        postParam.put("QUESTION_TYPE", "PREFS");
        postParam.put("DEVICE_ID", d_id);
        postParam.put("DEVICE_NAME", name);
        postParam.put("DEVICE_MODEL", model);
        postParam.put("AP_PASS", apPass);
        postParam.put("SERVER_NAME", _attach_server_name);
        postParam.put("SERVER_SSID", _server_ssid);
        postParam.put("SERVER_PASS", _server_pass);
        postParam.put("SERVER_IP", serv_ap_ip);
        postParam.put("ROUTER_SSID", _router_ssid);
        postParam.put("ROUTER_PASS", _router_pass);

        RetryPolicy mRetryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, jsonUrl, new JSONObject(postParam), response -> {

            System.out.println("SAVE DEVICE DATA RESPONSE: " + response.toString());

            if(!response.toString().isEmpty()) {

                try {

                    JSONObject jsonObject    = new JSONObject(response.toString());
                    JSONObject main_response = new JSONObject(jsonObject.getString("RESPONSE"));
                    String server_status     = main_response.getString("STATUS");

                    if(device.equals("CAMERA")) {

                        if (server_status.equals("CAMERA_SETUP_APP_ID_KO")) {

                            prefs_status_message_layout.setVisibility(View.GONE);
                            prefs_status_message_view.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                            prefs_status_message_view.setText(R.string.wifi_setup_security_check_ko);

                            security_check_button.setText(R.string.terminate_button);

                            security_check_button.setOnClickListener(v -> {

                                finish();
                                System.exit(0);
                            });
                        }
                        else
                        {

                            Tools.setStringPref(context, "APP_ID", Tools.APP_ID);

                            int setup_ok;

                            int _ap_max_clients, _ap_channel, _ap_ssid_hidden;
                            String _id, _name, _model, _ap_ssid, _ap_pass, _ap_ip, _ap_mac, _ap_hostname, _server_name, _serv_ssid, _serv_pass, _rout_ssid, _rout_pass, _server_ip, _net_ip, _sta_mac, _freq, _heap, _spiffs, _start_cam, _espnow, _http, _sockets, _wifiscan, _cam_web, _to_server, _to_app, s_startup, _remote, _w_user, _w_pass, _img_q;
                            String _setup_ok, max_clients, channel, ssid_hidden;

                            if (server_status.equals("CAMERA_SETUP_OK")) {

                                prefs_status_message_layout.setVisibility(View.GONE);
                                apSsidLayout.setVisibility(View.GONE);
                                apPassLayout.setVisibility(View.GONE);

                                _setup_ok          = main_response.getString("SETUP_OK"); // String from server
                                setup_ok           = Integer.parseInt(_setup_ok);               // Boolean to Database

                                _id                = main_response.getString("ID");
                                _name              = main_response.getString("NAME");
                                _model             = main_response.getString("MODEL");
                                _ap_ssid           = main_response.getString("AP_SSID");
                                _ap_pass           = main_response.getString("AP_PASS");
                                _ap_ip             = main_response.getString("AP_IP");
                                _ap_mac            = main_response.getString("AP_MAC");
                                _ap_hostname       = main_response.getString("AP_HOSTNAME");
                                _server_name       = main_response.getString("SERVER_NAME");
                                _serv_ssid       = main_response.getString("SERVER_SSID");
                                _serv_pass       = main_response.getString("SERVER_PASS");
                                _rout_ssid       = main_response.getString("ROUTER_SSID");
                                _rout_pass       = main_response.getString("ROUTER_PASS");
                                _server_ip         = main_response.getString("SERVER_IP");
                                _net_ip            = main_response.getString("NET_IP");
                                _sta_mac           = main_response.getString("STA_MAC");
                                _freq              = main_response.getString("FREQ");
                                _heap              = main_response.getString("HEAP");
                                _spiffs            = main_response.getString("SPIFFS");

                                max_clients        = main_response.getString("AP_MAX_CLIENTS"); // String from server
                                ssid_hidden        = main_response.getString("AP_SSID_HIDDEN"); // String from server
                                channel            = main_response.getString("AP_CHANNEL");     // String from server

                                /* CAMERA SETTING FROM DEVICE */
                                _start_cam         = main_response.getString("START_CAM");
                                _espnow            = main_response.getString("ESPNOW_CONTROL");
                                _http              = main_response.getString("HTTP_CONTROL");
                                _sockets           = main_response.getString("SOCKETS_CONTROL");
                                _wifiscan          = main_response.getString("ACTIVATE_WIFI_SCAN");
                                _cam_web           = main_response.getString("START_CAM_WEB");
                                _to_server         = main_response.getString("SEND_TO_CYRENAICA_SERVER");
                                _to_app            = main_response.getString("SEND_TO_APPLICATION");
                                s_startup          = main_response.getString("START_SERVICES_ON_STARTUP");
                                _remote            = main_response.getString("REMOTE_ACCESS");
                                _w_user            = main_response.getString("WEB_USERNAME");
                                _w_pass            = main_response.getString("WEB_PASSWORD");

                                _ap_max_clients    = Integer.parseInt(max_clients); // Integer to Database
                                _ap_ssid_hidden    = Integer.parseInt(ssid_hidden); // Integer to Database
                                _ap_channel        = Integer.parseInt(channel);     // Integer to Database

                                if(setup_ok > 0) {

                                    Log.d(TAG, "UPDATE [" + ServerSQLHelper.CAMERAS_TABLE_NAME + "] table started");

                                    ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_SETUP_OK, setup_ok, ServerSQLHelper.CAMERA_NAME, _name);
                                    ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_AP_SSID, _ap_ssid, ServerSQLHelper.CAMERA_NAME, _name);
                                    ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_AP_PASS, _ap_pass, ServerSQLHelper.CAMERA_NAME, _name);
                                    ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_AP_IP, _ap_ip, ServerSQLHelper.CAMERA_NAME, _name);
                                    ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_AP_MAC, _ap_mac, ServerSQLHelper.CAMERA_NAME, _name);
                                    ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_AP_HOSTNAME, _ap_hostname, ServerSQLHelper.CAMERA_NAME, _name);

                                    ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_AP_MAX_CLIENTS, _ap_max_clients, ServerSQLHelper.CAMERA_NAME, _name);
                                    ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_AP_SSID_HIDDEN, _ap_ssid_hidden, ServerSQLHelper.CAMERA_NAME, _name);
                                    ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_AP_CHANNEL, _ap_channel, ServerSQLHelper.CAMERA_NAME, _name);

                                    ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_SERVER_SSID, _serv_ssid, ServerSQLHelper.CAMERA_NAME, _name);
                                    ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_SERVER_PASS, _serv_pass, ServerSQLHelper.CAMERA_NAME, _name);
                                    ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_SERVER_IP, _server_ip, ServerSQLHelper.CAMERA_NAME, _name);
                                    ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_NET_IP, _net_ip, ServerSQLHelper.CAMERA_NAME, _name);
                                    ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_STA_MAC, _sta_mac, ServerSQLHelper.CAMERA_NAME, _name);
                                    ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_FREQ, _freq, ServerSQLHelper.CAMERA_NAME, _name);
                                    ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_HEAP, _heap, ServerSQLHelper.CAMERA_NAME, _name);

                                    if(ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.CAMERAS_TABLE_NAME, ServerSQLHelper.CAMERA_SPIFFS, _spiffs, ServerSQLHelper.CAMERA_NAME, _name)){
                                        Log.d(TAG, "UPDATE [" + ServerSQLHelper.CAMERAS_TABLE_NAME + "] table ended");

                                        Log.d(TAG, "UPDATE [" + ServerSQLHelper.CAMERAS_SETTINGS_TABLE_NAME + "] table started");
                                        /* CAMERA SETTING DATABASE UPDATE */
                                        ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.CAMERAS_SETTINGS_TABLE_NAME, ServerSQLHelper.CAMERA_START_CAM, Integer.parseInt(_start_cam), ServerSQLHelper.CAM_NAME, _name);
                                        ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.CAMERAS_SETTINGS_TABLE_NAME, ServerSQLHelper.CAMERA_ESPNOW_CONTROL, Integer.parseInt(_espnow), ServerSQLHelper.CAM_NAME, _name);
                                        ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.CAMERAS_SETTINGS_TABLE_NAME, ServerSQLHelper.CAMERA_HTTP_CONTROL, Integer.parseInt(_http), ServerSQLHelper.CAM_NAME, _name);
                                        ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.CAMERAS_SETTINGS_TABLE_NAME, ServerSQLHelper.CAMERA_SOCKETS_CONTROL, Integer.parseInt(_sockets), ServerSQLHelper.CAM_NAME, _name);
                                        ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.CAMERAS_SETTINGS_TABLE_NAME, ServerSQLHelper.CAMERA_ACTIVATE_WIFISCAN, Integer.parseInt(_wifiscan), ServerSQLHelper.CAM_NAME, _name);
                                        ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.CAMERAS_SETTINGS_TABLE_NAME, ServerSQLHelper.CAMERA_START_CAM_WEB, Integer.parseInt(_cam_web), ServerSQLHelper.CAM_NAME, _name);
                                        ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.CAMERAS_SETTINGS_TABLE_NAME, ServerSQLHelper.CAMERA_SEND_TO_SERVER, Integer.parseInt(_to_server), ServerSQLHelper.CAM_NAME, _name);
                                        ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.CAMERAS_SETTINGS_TABLE_NAME, ServerSQLHelper.CAMERA_SEND_TO_APP, Integer.parseInt(_to_app), ServerSQLHelper.CAM_NAME, _name);
                                        ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.CAMERAS_SETTINGS_TABLE_NAME, ServerSQLHelper.CAMERA_SERVICES_ON_STARTUP, Integer.parseInt(s_startup), ServerSQLHelper.CAM_NAME, _name);
                                        ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.CAMERAS_SETTINGS_TABLE_NAME, ServerSQLHelper.CAMERA_REMOTE_ACCESS, Integer.parseInt(_remote), ServerSQLHelper.CAM_NAME, _name);

                                        if(ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.CAMERAS_SETTINGS_TABLE_NAME, ServerSQLHelper.CAMERA_WEB_USERNAME, _w_user, ServerSQLHelper.CAM_NAME, _name)){
                                            Log.d(TAG, "UPDATE [" + ServerSQLHelper.CAMERA_WEB_USERNAME + "] = [" + _w_user + "] OK");
                                        }
                                        else
                                        {
                                            Log.d(TAG, "UPDATE [" + ServerSQLHelper.CAMERA_WEB_USERNAME + "] = [" + _w_user + "] KO");
                                        }

                                        if(ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.CAMERAS_SETTINGS_TABLE_NAME, ServerSQLHelper.CAMERA_WEB_PASSWORD, _w_pass, ServerSQLHelper.CAM_NAME, _name)){
                                            Log.d(TAG, "UPDATE [" + ServerSQLHelper.CAMERA_WEB_PASSWORD + "] = [" + _w_pass + "] OK");
                                        }
                                        else
                                        {
                                            Log.d(TAG, "UPDATE [" + ServerSQLHelper.CAMERA_WEB_PASSWORD  + "] = [" + _w_pass + "] KO");
                                        }

                                    }


                                    int device_count = ServerSQLHelper.dbHelper.getServerDevicesCount(_server_name); // Get current devices count in server table
                                    int _count = device_count + 1;

                                    if(ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_NODES_COUNT, _count, ServerSQLHelper.SERVER_NAME, _server_name)) { // Update the devices count in server table

                                        Log.d(TAG, "SERVER DEVICE COUNT: Updated successfully, now is: " + _count);

                                        Tools.CloseLoadingDialog();

                                        device_security_check_label.setText(R.string.server_add_device_security_check_password_changed_successfully_label);

                                        security_check_button.setText(R.string.terminate_button);

                                        security_check_button.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                URL = "http://" + DEVICE_IP + "/server_network";

                                                /* Save device to server */
                                                //addDeviceToServer(final Context context, String jsonUrl, String id, String name, String type, String ap_ip, String sta_ip, String ap_mac, String sta_mac);

                                                Intent intent = new Intent(context, ServerHomeActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                context.startActivity(intent);
                                            }
                                        });
                                    }
                                    else
                                    {

                                    }


                                }
                                else
                                {
                                    System.out.println("ADD DEVICE SETUP NOT OK: " + setup_ok);
                                }
                            }
                            else
                            {
                                if (server_status.equals("CAMERA_SETUP_AP_PASS_KO")) {

                                    apSsidLayout.setVisibility(View.GONE);
                                    apPassLayout.setVisibility(View.GONE);
                                    prefs_status_message_layout.setVisibility(View.VISIBLE);
                                    prefs_status_message_view.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                                    prefs_status_message_view.setText(R.string.wifi_setup_ap_pass_error);

                                    security_check_button.setText(R.string.retry_button);

                                    security_check_button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            System.out.println("CAMERA ERROR MESSAGE");
                                        }
                                    });
                                }

                                if (server_status.equals("CAMERA_SETUP_SERVER_SSID_KO")) {

                                    apSsidLayout.setVisibility(View.GONE);
                                    apPassLayout.setVisibility(View.GONE);
                                    prefs_status_message_layout.setVisibility(View.VISIBLE);
                                    prefs_status_message_view.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                                    prefs_status_message_view.setText(R.string.wifi_setup_router_ssid_error);

                                    security_check_button.setText(R.string.retry_button);

                                    security_check_button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            System.out.println("CAMERA ERROR MESSAGE");
                                        }
                                    });
                                }

                                if (server_status.equals("CAMERA_SETUP_SERVER_PASS_KO")) {

                                    apSsidLayout.setVisibility(View.GONE);
                                    apPassLayout.setVisibility(View.GONE);
                                    prefs_status_message_layout.setVisibility(View.VISIBLE);
                                    prefs_status_message_view.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                                    prefs_status_message_view.setText(R.string.wifi_setup_router_pass_error);

                                    security_check_button.setText(R.string.retry_button);

                                    security_check_button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            System.out.println("CAMERA ERROR MESSAGE");
                                        }
                                    });
                                }

                                //TODO: finish the error message for the rest of elements
                            }
                        }
                    }

                    if(device.equals("CONTROLLER")) {

                    }

                    if(device.equals("DETECTOR")) {

                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                error -> System.out.println("Save Access Point Error: " + error.getMessage())) {
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

    public void addDeviceToServer(final Context context, String jsonUrl, String id, String name, String type, String ap_ip, String sta_ip, String ap_mac, String sta_mac) {

        Map<String, String> params = new HashMap<>();
        params.put("QUESTION", "SAVE_DEVICE");
        params.put("D_ID", id);
        params.put("D_NAME", name);
        params.put("D_TYPE", type);
        params.put("D_AP_IP", ap_ip);
        params.put("D_STA_IP", sta_ip);
        params.put("D_AP_MAC", ap_mac);
        params.put("D_STA_MAC", sta_mac);

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
                error -> System.out.println("Save Access Point Error: " + error.getMessage())) {
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