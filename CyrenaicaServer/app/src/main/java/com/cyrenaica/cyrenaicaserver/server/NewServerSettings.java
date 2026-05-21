package com.cyrenaica.cyrenaicaserver.server;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cyrenaica.cyrenaicaserver.R;
import com.cyrenaica.cyrenaicaserver.tools.ConnectionReceiver;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.DatabaseSetup;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.ServerSQLHelper;
import com.cyrenaica.cyrenaicaserver.language.LocalHelper;

import com.cyrenaica.cyrenaicaserver.tools.IpValidation;
import com.cyrenaica.cyrenaicaserver.tools.RequestSingleton;
import com.cyrenaica.cyrenaicaserver.tools.Tools;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NewServerSettings extends AppCompatActivity {

    private static final String TAG = "NewServerSettings";
    Intent ServerSettingsSocketsService;
    ConnectionReceiver SocketsReceiver = new ConnectionReceiver();

    TextView new_server_setup_page_label;

    TextView ap_settings_label;
    TextView sta_settings_label;
    Button new_server_setup_modify_btn;

    com.google.android.material.textfield.TextInputLayout apChannelLayout;
    com.google.android.material.textfield.TextInputEditText apChannelInput;
    com.google.android.material.textfield.TextInputLayout apMaxClientsLayout;
    com.google.android.material.textfield.TextInputEditText apMaxClientsInput;
    Switch ap_use_static_ip;
    com.google.android.material.textfield.TextInputLayout apStaticIpLayout;
    com.google.android.material.textfield.TextInputEditText apStaticIpInput;
    com.google.android.material.textfield.TextInputLayout staStaticIpLayout;
    Switch sta_use_static_ip;
    com.google.android.material.textfield.TextInputEditText staStaticIpInput;
    com.google.android.material.textfield.TextInputLayout staGatewayLayout;
    com.google.android.material.textfield.TextInputEditText staGatewayInput;

    LinearLayout fieldContainer;
    TextView errorMessage;

    ScrollView settings_container;

    static boolean is_ap_static_ip;
    static boolean is_sta_static_ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_server_settings);

        Log.i(TAG, "onCreate");

        Tools.IS_SETUP_OK        = Tools.getFirstTimeIntro(getApplicationContext());
        Tools.SETUP_CURRENT_STEP = Tools.getSetupCurrentStep(getApplicationContext());

        if(!ServerSQLHelper.dbHelper.dbExist() || !ServerSQLHelper.encrp.searchDbPassword()){
            Intent intent = new Intent(NewServerSettings.this, DatabaseSetup.class);
            intent.putExtra("Activity", "NewServerSettings");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            //finish();
            return;
        }

        if (!ServerSQLHelper.DbHelper(getApplicationContext()).tableExist(ServerSQLHelper.SERVER_TABLE_NAME)) {
            Log.i(TAG, "Creating SERVER database Table");
            ServerSQLHelper.DbHelper(getApplicationContext()).createTable(ServerSQLHelper.SERVER_TABLE_NAME);

            if (!ServerSQLHelper.DbHelper(getApplicationContext()).tableExist(ServerSQLHelper.SERVER_SETTINGS_TABLE_NAME)) {
                Log.i(TAG, "Creating SERVER database Table");
                ServerSQLHelper.DbHelper(getApplicationContext()).createTable(ServerSQLHelper.SERVER_SETTINGS_TABLE_NAME);
            }
        }

        apChannelLayout             = findViewById(R.id.id_file_name_layout);
        apMaxClientsLayout          = findViewById(R.id.id_new_server_settings_ap_max_clients_layout);
        apStaticIpLayout            = findViewById(R.id.id_new_server_settings_ap_static_ip_layout);
        staStaticIpLayout           = findViewById(R.id.id_new_server_settings_sta_static_ip_layout);
        staGatewayLayout            = findViewById(R.id.id_new_server_settings_sta_gateway_layout);
        apChannelInput              = findViewById(R.id.id_file_name_input);
        apMaxClientsInput           = findViewById(R.id.id_new_server_settings_ap_max_clients_input);
        ap_use_static_ip            = findViewById(R.id.id_new_server_settings_ap_use_static_ip_switch);
        apStaticIpInput             = findViewById(R.id.id_new_server_settings_ap_static_ip_input);
        sta_use_static_ip           = findViewById(R.id.id_new_server_settings_sta_use_static_ip_switch);
        staStaticIpInput            = findViewById(R.id.id_new_server_settings_sta_static_ip_input);
        staGatewayInput             = findViewById(R.id.id_new_server_settings_sta_gateway_input);
        new_server_setup_page_label = findViewById(R.id.id_new_server_settings_page_title);
        ap_settings_label           = findViewById(R.id.id_new_server_settings_ap_setting_label);
        sta_settings_label          = findViewById(R.id.id_new_server_settings_sta_setting_label);
        new_server_setup_modify_btn = findViewById(R.id.id_new_server_settings_modify_btn);
        settings_container          = findViewById(R.id.id_new_server_settings_scroll_container);
        fieldContainer              = findViewById(R.id.id_new_server_settings_field_container);
        errorMessage                = findViewById(R.id.id_new_server_settings_error_msg);

        if (LocalHelper.getLanguage(NewServerSettings.this).equalsIgnoreCase("ar")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                new_server_setup_page_label.setTypeface(getResources().getFont(R.font.cairo));
                ap_settings_label.setTypeface(getResources().getFont(R.font.cairo));
                sta_settings_label.setTypeface(getResources().getFont(R.font.cairo));
                new_server_setup_modify_btn.setTypeface(getResources().getFont(R.font.cairo));
                apChannelLayout.setTypeface(getResources().getFont(R.font.cairo));

                apMaxClientsLayout.setTypeface(getResources().getFont(R.font.cairo));
                apStaticIpLayout.setTypeface(getResources().getFont(R.font.cairo));
                staStaticIpLayout.setTypeface(getResources().getFont(R.font.cairo));
                staGatewayLayout.setTypeface(getResources().getFont(R.font.cairo));
            }
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                new_server_setup_page_label.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
            }
        }

        String get_prefs_url = "http://" + Tools.DEFAULT_AP_STATIC_IP + "/server_network";
        getNewServerPreferences(getApplicationContext(), get_prefs_url, "SAVE_NEW_SERVER_PREFERENCES"); // to get default data from server device

        ap_use_static_ip.setOnClickListener(v -> {

            if(!ap_use_static_ip.isChecked()){
                apStaticIpLayout.setVisibility(View.GONE);
                apStaticIpInput.setVisibility(View.GONE);

                is_ap_static_ip = false;
            }
            else
            {
                apStaticIpLayout.setVisibility(View.VISIBLE);
                apStaticIpInput.setVisibility(View.VISIBLE);

                is_ap_static_ip = true;
            }

        });

        sta_use_static_ip.setOnClickListener(v -> {

            if(!sta_use_static_ip.isChecked()){
                staStaticIpLayout.setVisibility(View.GONE);
                staStaticIpInput.setVisibility(View.GONE);

                staGatewayLayout.setVisibility(View.GONE);
                staGatewayInput.setVisibility(View.GONE);

                is_sta_static_ip = false;
            }
            else
            {
                staStaticIpLayout.setVisibility(View.VISIBLE);
                staStaticIpInput.setVisibility(View.VISIBLE);

                staGatewayLayout.setVisibility(View.VISIBLE);
                staGatewayInput.setVisibility(View.VISIBLE);

                is_sta_static_ip = true;
            }
        });

        new_server_setup_modify_btn.setOnClickListener(v -> {

            Tools.hideSoftKeyboard(NewServerSettings.this);

            String apChannel     = Objects.requireNonNull(apChannelInput.getText()).toString();
            String apMaxClients  = Objects.requireNonNull(apMaxClientsInput.getText()).toString();
            String apStaticIp    = Objects.requireNonNull(apStaticIpInput.getText()).toString();
            String staStaticIp   = Objects.requireNonNull(staStaticIpInput.getText()).toString();
            String gateway       = Objects.requireNonNull(staGatewayInput.getText()).toString();

            /* Ap channel field (zero) validation */
            if(Tools.validateWifiSsid(apChannelInput, 0, 12).equals("ZERO_ERROR")){
                apChannelLayout.setError(getApplicationContext().getResources().getText(R.string.new_server_settings_ap_channel_input_zero_error));
                return;
            }
            else
            {
                apChannelLayout.setError("");
            }

            /* Ap channel field (min - max) validation */
            apChannelInput.addTextChangedListener(new TextWatcher() {

                int number = -1;

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    number = Integer.parseInt(apChannelInput.getText().toString());

                    if(number < 0){
                        apChannelLayout.setError(getApplicationContext().getResources().getText(R.string.new_server_settings_ap_channel_input_min_max_error));
                    }
                    else
                    {
                        apChannelLayout.setError("");
                    }

                    if(number > 12){
                        apChannelLayout.setError(getApplicationContext().getResources().getText(R.string.new_server_settings_ap_channel_input_min_max_error));
                    }
                    else
                    {
                        apChannelLayout.setError("");
                    }
                }
            });

            /* Ap channel field (zero) validation */
            if(Tools.validateWifiSsid(apMaxClientsInput, 0, 12).equals("ZERO_ERROR")){
                apMaxClientsLayout.setError(getApplicationContext().getResources().getText(R.string.new_server_settings_ap_channel_input_zero_error));
                return;
            }
            else
            {
                apMaxClientsLayout.setError("");
            }

            /* Ap channel field (min - max) validation */
            apMaxClientsInput.addTextChangedListener(new TextWatcher() {

                int number = -1;

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    number = Integer.parseInt(apMaxClientsInput.getText().toString());

                    if(number < 0){
                        apMaxClientsLayout.setError(getApplicationContext().getResources().getText(R.string.new_server_settings_ap_max_clients_input_min_max_error));
                    }
                    else
                    {
                        apMaxClientsLayout.setError("");
                    }

                    if(number > 12){
                        apMaxClientsLayout.setError(getApplicationContext().getResources().getText(R.string.new_server_settings_ap_max_clients_input_min_max_error));
                    }
                    else
                    {
                        apMaxClientsLayout.setError("");
                    }
                }
            });

            if(is_ap_static_ip){

                if(Tools.numbersInputIsEmpty(apStaticIpInput)){
                    apStaticIpLayout.setError(getApplicationContext().getResources().getText(R.string.new_server_settings_ap_channel_input_zero_error));
                }

                if(!IpValidation.isValid(Objects.requireNonNull(apStaticIpInput.getText()).toString())){
                    apStaticIpLayout.setError(getApplicationContext().getResources().getText(R.string.new_server_settings_ip_input_validation_error));
                }
            }

            if(is_sta_static_ip) {

                if (Tools.numbersInputIsEmpty(apStaticIpInput)) {
                    staStaticIpLayout.setError(getApplicationContext().getResources().getText(R.string.new_server_settings_ap_channel_input_zero_error));
                }

                if (!IpValidation.isValid(Objects.requireNonNull(staStaticIpInput.getText()).toString())) {
                    staStaticIpLayout.setError(getApplicationContext().getResources().getText(R.string.new_server_settings_ip_input_validation_error));
                }

                if (Tools.numbersInputIsEmpty(staGatewayInput)) {
                    staGatewayLayout.setError(getApplicationContext().getResources().getText(R.string.new_server_settings_ap_channel_input_zero_error));
                }

                if (!IpValidation.isValid(Objects.requireNonNull(staGatewayInput.getText()).toString())) {
                    staGatewayLayout.setError(getApplicationContext().getResources().getText(R.string.new_server_settings_ip_input_validation_error));
                }
            }

            String url = "http://" + Tools.DEFAULT_AP_STATIC_IP + "/server_network";
            saveNewServerSettings(getApplicationContext(), url, apChannel, apMaxClients, is_ap_static_ip, apStaticIp, is_sta_static_ip, staStaticIp, gateway);

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

        Log.d(TAG, "onStop");
        super.onStop();
    }

    public void getNewServerPreferences(final Context context, String jsonUrl, String question) {
        Map<String, String> postParam = new HashMap<>();

        postParam.put("APP_ID", Tools.getStringPref(context, "APP_ID"));
        postParam.put("QUESTION", question);
        postParam.put("STEP", "GET_PREFS");

        RetryPolicy mRetryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, jsonUrl, new JSONObject(postParam), response -> {

            System.out.println("NEW SERVER GET PREFS DATA RESPONSE: " + response.toString());

            if(!response.toString().isEmpty()) {

                try {

                    JSONObject jsonObject    = new JSONObject(response.toString());
                    JSONObject main_response = new JSONObject(jsonObject.getString("RESPONSE"));
                    String server_status     = main_response.getString("STATUS");

                    if (server_status.equals("NEW_SERVER_PREFS_APP_ID_KO")) {

                        fieldContainer.setVisibility(View.GONE);
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                        errorMessage.setText(R.string.wifi_setup_security_check_ko);

                        new_server_setup_modify_btn.setText(R.string.terminate_button);

                        new_server_setup_modify_btn.setOnClickListener(v -> {

                            finish();
                            System.exit(0);
                        });
                    }
                    else
                    {

                        if (server_status.equals("NEW_SERVER_GET_PREFS_OK")) {
                            String _ap_static_ip     = main_response.getString("AP_STATIC_IP");
                            String _ap_channel       = main_response.getString("AP_CHANNEL");
                            String _ap_max_clients   = main_response.getString("AP_MAX_CLIENTS");

                            String _sta_static_ip     = main_response.getString("STA_STATIC_IP");
                            String _sta_gateway_ip    = main_response.getString("STA_GATEWAY");

                            apStaticIpInput.setText("10.10.10.10");


                            //apStaticIpInput.setText(_ap_static_ip);
                            apChannelInput.setText(_ap_channel);
                            apMaxClientsInput.setText(_ap_max_clients);

                            staStaticIpInput.setText("192.168.1.97");
                            //staStaticIpInput.setText(_sta_static_ip);
                            staGatewayInput.setText(_sta_gateway_ip);
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
            error -> System.out.println("New Server Settings Error: " + error.getMessage())) {
            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
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

    public void saveNewServerSettings(final Context context, String jsonUrl, String apChannel, String apMaxClients, boolean ap_is_static_ip, String apStaticIp, boolean sta_is_static_ip, String staStaticIp, String gateWay) {

        Map<String, String> postParam = new HashMap<>();

        String language = LocalHelper.getLanguage(getApplicationContext());
        Calendar cal    = Calendar.getInstance();
        TimeZone tz     = cal.getTimeZone();

        String is_ap_static = "";
        String is_sta_static = "";

        if(ap_is_static_ip){
            is_ap_static = "1";
        }
        else
        {
            is_ap_static = "0";
        }

        if(sta_is_static_ip){
            is_sta_static = "1";
        }
        else
        {
            is_sta_static = "0";
        }

        postParam.put("APP_ID", Tools.getStringPref(context, "APP_ID"));
        postParam.put("QUESTION", "SAVE_NEW_SERVER_PREFERENCES");
        postParam.put("STEP", "SAVE_PREFS");
        postParam.put("AP_CHANNEL", apChannel);
        postParam.put("AP_MAX_CLIENTS", apMaxClients);
        postParam.put("AP_USE_STATIC_IP", is_ap_static);
        postParam.put("AP_STATIC_IP", apStaticIp);
        postParam.put("STA_USE_STATIC_IP", is_sta_static);
        postParam.put("STA_STATIC_IP", staStaticIp);
        postParam.put("STA_GATEWAY_IP", gateWay);
        postParam.put("REMOTE_DOMAIN", "EMPTY");
        postParam.put("LANGUAGE", language);
        postParam.put("TIMEZONE_ID", tz.getID());
        postParam.put("TIMEZONE_OFFSET", String.valueOf(tz.getRawOffset()));
        postParam.put("TIMEZONE_DTS_SAVING", String.valueOf(tz.getDSTSavings()));

        RetryPolicy mRetryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, jsonUrl, new JSONObject(postParam), response -> {

            System.out.println("NEW SERVER SETTINGS DATA RESPONSE: " + response.toString());

            if(!response.toString().isEmpty()) {

                try {

                    JSONObject jsonObject    = new JSONObject(response.toString());
                    JSONObject main_response = new JSONObject(jsonObject.getString("RESPONSE"));
                    String server_status     = main_response.getString("STATUS");

                    if (server_status.equals("SERVER_SETUP_APP_ID_KO")) {

                        fieldContainer.setVisibility(View.GONE);
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                        errorMessage.setText(R.string.wifi_setup_security_check_ko);

                        new_server_setup_modify_btn.setText(R.string.terminate_button);

                        new_server_setup_modify_btn.setOnClickListener(v -> {

                            finish();
                            System.exit(0);
                        });
                    }
                    else
                    {

                        if (server_status.equals("NEW_SERVER_SAVE_PREFS_OK")) {

                            int _setup_ok, _use_ap_static_ip, _use_sta_static_ip, _router_setup_ok, _remote_access, _ap_max_clients, _ap_channel;
                            String _name, _ap_static_ip, _ap_ip, _ap_gateway, ap_netmask, ap_host, ap_mac, _sta_static_ip, _sta_mac, _local_ip, _gateway_ip, _netmask_ip, _dns_ip, _sta_host, _remote_domain, _remote_ip;

                            int _use_sta_static, _use_ap_static;
                            _setup_ok          = Integer.parseInt(main_response.getString("SETUP_OK"));

                            _name              = main_response.getString("NAME");
                            _use_ap_static_ip  = Integer.parseInt(main_response.getString("USE_AP_STATIC_IP"));
                            _ap_static_ip      = main_response.getString("AP_STATIC_IP");
                            _ap_ip             = main_response.getString("AP_IP");
                            _ap_gateway        = main_response.getString("AP_GATEWAY_IP");
                            ap_netmask         = main_response.getString("AP_NETMASK_IP");
                            ap_host            = main_response.getString("AP_HOSTNAME");
                            ap_mac             = main_response.getString("AP_MAC");
                            _ap_max_clients    = Integer.parseInt(main_response.getString("AP_MAX_CLIENTS"));
                            _ap_channel        = Integer.parseInt(main_response.getString("AP_CHANNEL"));
                            _use_sta_static_ip = Integer.parseInt(main_response.getString("USE_STA_STATIC_IP"));
                            _sta_static_ip     = main_response.getString("STA_STATIC_IP");
                            _sta_mac           = main_response.getString("STA_MAC");
                            _local_ip          = main_response.getString("NET_IP");
                            _gateway_ip        = main_response.getString("STA_GATEWAY_IP");
                            _netmask_ip        = main_response.getString("STA_NETMASK_IP");
                            _dns_ip            = main_response.getString("STA_DNS_1");
                            _sta_host          = main_response.getString("STA_HOSTNAME");
                            _router_setup_ok   = Integer.parseInt(main_response.getString("ROUTER_SETUP"));
                            _remote_access     = Integer.parseInt(main_response.getString("REMOTE_ACCESS"));
                            _remote_domain     = main_response.getString("REMOTE_DOMAIN");
                            _remote_ip         = main_response.getString("PUBLIC_IP");


                            if(_setup_ok == 1) {

                                ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_SETUP_OK, _setup_ok, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_USE_STA_STATIC_IP, _use_sta_static_ip, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_STA_STATIC_IP, _sta_static_ip, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_STA_MAC, _sta_mac, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_AP_IP, _ap_ip, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_AP_GATEWAY_IP, _ap_gateway, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_AP_NETMASK_IP, ap_netmask, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_AP_HOST, ap_host, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_AP_MAC, ap_mac, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_AP_MAX_CLIENTS, _ap_max_clients, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_AP_CHANNEL, _ap_channel, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_USE_AP_STATIC_IP, _use_ap_static_ip, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_AP_STATIC_IP, _ap_static_ip, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_LOCAL_IP, _local_ip, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_GATEWAY_IP, _gateway_ip, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_NETMASK_IP, _netmask_ip, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_DNS_IP, _dns_ip, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_STA_HOST, _sta_host, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.ROUTER_SETUP, _router_setup_ok, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateIntRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_REMOTE_ACCESS, _remote_access, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_REMOTE_DOMAIN, _remote_domain, ServerSQLHelper.SERVER_NAME, _name);
                                ServerSQLHelper.dbHelper.updateStringRecord(ServerSQLHelper.SERVER_TABLE_NAME, ServerSQLHelper.SERVER_REMOTE_IP, _remote_ip, ServerSQLHelper.SERVER_NAME, _name);

                                //TODO: Add server settings from server

                                Tools.setSetupCurrentStep(getApplicationContext(), 4);

                                Tools.setFirstTimeIntro(getApplicationContext(), true);

                                Intent intent = new Intent(NewServerSettings.this, ViewAllServers.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                        else
                        {
                            if (server_status.equals("SERVER_ACCESS_POINT_LOCAL_IP_KO")) {
                                new_server_setup_modify_btn.setText(R.string.retry_button);
                                Tools.errorDialog(NewServerSettings.this, R.string.new_server_settings_error_dialog_label, R.string.new_server_settings_ap_static_ip_error, R.string.retry_button, R.string.button_restart, AddNewServer.class);
                            }
                            else if (server_status.equals("SERVER_STATION_POINT_NET_IP_KO")) {
                                new_server_setup_modify_btn.setText(R.string.retry_button);
                                Tools.errorDialog(NewServerSettings.this, R.string.new_server_settings_error_dialog_label, R.string.new_server_settings_sta_static_ip_error, R.string.retry_button, R.string.button_restart, AddNewServer.class);
                            }
                            else if (server_status.equals("SERVER_STATION_GATEWAY_IP_KO")) {
                                new_server_setup_modify_btn.setText(R.string.retry_button);
                                Tools.errorDialog(NewServerSettings.this, R.string.new_server_settings_error_dialog_label, R.string.new_server_settings_ap_channel_error, R.string.retry_button, R.string.button_restart, AddNewServer.class);
                            }
                            else if (server_status.equals("SERVER_ACCESS_POINT_CHANNEL_KO")) {
                                new_server_setup_modify_btn.setText(R.string.retry_button);
                                Tools.errorDialog(NewServerSettings.this, R.string.new_server_settings_error_dialog_label, R.string.new_server_settings_ap_channel_error, R.string.retry_button, R.string.button_restart, AddNewServer.class);
                            }
                            else if (server_status.equals("SERVER_ACCESS_POINT_MAX_CLIENT_KO")) {
                                new_server_setup_modify_btn.setText(R.string.retry_button);
                                Tools.errorDialog(NewServerSettings.this, R.string.new_server_settings_error_dialog_label, R.string.new_server_settings_ap_max_clients_error, R.string.retry_button, R.string.button_restart, AddNewServer.class);
                            }
                            else
                            {
                                fieldContainer.setVisibility(View.GONE);
                                settings_container.setVisibility(View.GONE);

                                Intent intent = new Intent(NewServerSettings.this, ServerHomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
        error -> System.out.println("New Server Settings Error: " + error.getMessage())) {
            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
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
}