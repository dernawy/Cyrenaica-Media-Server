package com.cyrenaica.cyrenaicaserver.server;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cyrenaica.cyrenaicaserver.R;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.DatabaseSetup;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.Encryption;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.ServerSQLHelper;
import com.cyrenaica.cyrenaicaserver.language.LocalHelper;
import com.cyrenaica.cyrenaicaserver.mdns.mDnsService;
import com.cyrenaica.cyrenaicaserver.tools.ConnectionReceiver;
import com.cyrenaica.cyrenaicaserver.tools.RequestSingleton;
import com.cyrenaica.cyrenaicaserver.tools.Tools;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SecurityCheckActivity extends AppCompatActivity {

    private static final String TAG = "SecurityCheckActivity";

    Intent SecurityCheckSocketsService;
    ConnectionReceiver SocketsReceiver = new ConnectionReceiver();

    TextView pageLabel;

    String security_step_text;

    TextView NetworkConnectStatusLabel;
    TextView SocketsStatusLabel;
    static TextView NetworkConnectStatus;
    static TextView SocketsStatus;

    com.google.android.material.textfield.TextInputLayout apSsidLayout;
    com.google.android.material.textfield.TextInputEditText apSsidInput;
    com.google.android.material.textfield.TextInputLayout apPassLayout;
    com.google.android.material.textfield.TextInputEditText apPassInput;

    com.google.android.material.textfield.TextInputLayout routerSsidLayout;
    com.google.android.material.textfield.TextInputEditText routerSsidInput;
    com.google.android.material.textfield.TextInputLayout routerPassLayout;
    com.google.android.material.textfield.TextInputEditText routerPassInput;

    static LinearLayout fieldsLayout;
    static TextView endMessage;

    static Button saveBtn;
    static Button moveBtn;

    static Intent intent;
    mDnsService _DNS;

    String ssid_type;
    String ap_ssid;
    String ap_pass;
    boolean router_setup;
    String router_ssid;
    String router_pass;
    boolean remote_access;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_check);

        Tools.IS_SETUP_OK        = Tools.getFirstTimeIntro(getApplicationContext());
        Tools.SETUP_CURRENT_STEP = Tools.getSetupCurrentStep(getApplicationContext());

        Log.i(TAG, "onCreate");

        Tools.hideSoftKeyboard(SecurityCheckActivity.this);

        //_DNS = new mDnsService(this);
        //_DNS.initializeNsd("ServerSetup", "_http._tcp.");

        if(!ServerSQLHelper.DbHelper(getApplicationContext()).tableExist(ServerSQLHelper.SERVER_TABLE_NAME)) {
            Log.i(TAG, "Creating SERVER database Table");
            ServerSQLHelper.DbHelper(getApplicationContext()).createTable(ServerSQLHelper.SERVER_TABLE_NAME);
        }

        pageLabel                  = findViewById(R.id.id_security_page_title);

        NetworkConnectStatusLabel  = findViewById(R.id.id_network_connection_status_label);
        SocketsStatusLabel         = findViewById(R.id.id_sockets_status_label);
        NetworkConnectStatus       = findViewById(R.id.id_network_connection_status);
        SocketsStatus              = findViewById(R.id.id_sockets_status);
        apSsidLayout               = findViewById(R.id.id_ap_ssid_field_layout);
        apSsidInput                = findViewById(R.id.id_ap_ssid_input_field);
        apPassLayout               = findViewById(R.id.id_ap_pass_field_layout);
        apPassInput                = findViewById(R.id.id_ap_pass_input_field);
        routerSsidLayout           = findViewById(R.id.id_router_ssid_field_layout);
        routerSsidInput            = findViewById(R.id.id_router_ssid_input_field);
        routerPassLayout           = findViewById(R.id.id_router_pass_field_layout);
        routerPassInput            = findViewById(R.id.id_router_pass_input_field);
        fieldsLayout               = findViewById(R.id.id_security_page_fields_layout);
        endMessage                 = findViewById(R.id.id_wifi_setup_end_message);
        saveBtn                    = findViewById(R.id.id_security_page_tests_save_btn);
        moveBtn                    = findViewById(R.id.id_move_button);

        remote_access              = Tools.getBoolPref(getApplicationContext(), "REMOTE_ACCESS");
        router_setup               = Tools.getBoolPref(getApplicationContext(), "ROUTER_SETUP");
        ap_ssid                    = Tools.getStringPref(getApplicationContext(), "CURRENT_WIFI_SSID");
        ap_pass                    = Tools.getStringPref(getApplicationContext(), "CURRENT_WIFI_PASS");
        router_ssid                = Tools.getStringPref(getApplicationContext(), "CURRENT_ROUTER_SSID");
        router_pass                = Tools.getStringPref(getApplicationContext(), "CURRENT_ROUTER_PASS");

        if (LocalHelper.getLanguage(SecurityCheckActivity.this).equalsIgnoreCase("ar")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                pageLabel.setTypeface(getResources().getFont(R.font.cairo));

                NetworkConnectStatusLabel.setTypeface(getResources().getFont(R.font.cairo));
                SocketsStatusLabel.setTypeface(getResources().getFont(R.font.cairo));
                NetworkConnectStatus.setTypeface(getResources().getFont(R.font.cairo));
                SocketsStatus.setTypeface(getResources().getFont(R.font.cairo));

                apSsidLayout.setTypeface(getResources().getFont(R.font.cairo));
                apPassLayout.setTypeface(getResources().getFont(R.font.cairo));
                routerSsidLayout.setTypeface(getResources().getFont(R.font.cairo));
                routerPassLayout.setTypeface(getResources().getFont(R.font.cairo));
                saveBtn.setTypeface(getResources().getFont(R.font.cairo));
                moveBtn.setTypeface(getResources().getFont(R.font.cairo));
            }
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                pageLabel.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
            }
        }

        Objects.requireNonNull(apSsidLayout.getEditText()).setText(ap_ssid);

        if(ap_pass.equals(Tools.DEFAULT_WIFI_PASSWORD)){

            Objects.requireNonNull(apPassLayout.getEditText()).setText(ap_pass);

            String hintText   = getResources().getString(R.string.wifi_setup_change_default_password_field_hint);
            String helperText = getResources().getString(R.string.wifi_setup_default_password_field_helper);

            apPassLayout.setHint(hintText);
            apPassLayout.setHelperText(helperText);
        }
        else
        {
            Objects.requireNonNull(apPassLayout.getEditText()).setText(ap_pass);

            String hintText   = getResources().getString(R.string.wifi_setup_current_password_field_hint);

            apPassLayout.setHint(hintText);
        }

        Objects.requireNonNull(routerSsidLayout.getEditText()).setText("SFR_F2A3");
        Objects.requireNonNull(routerPassLayout.getEditText()).setText("3aifl6rv9l8ihgefzli8");

        //Objects.requireNonNull(routerSsidLayout.getEditText()).setText(router_ssid);
        //Objects.requireNonNull(routerPassLayout.getEditText()).setText(router_pass);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Tools.hideSoftKeyboard(SecurityCheckActivity.this);

                /* check if fields are empties before sending save request */
                String AP_PASS     = "";
                String ROUTER_SSID = "";
                String ROUTER_PASS = "";

                if (Tools.validateWifiPassword(apPassInput, 8, 64).equals("ZERO_ERROR")) {
                    apPassLayout.setError(getApplicationContext().getResources().getText(R.string.wifi_setup_pass_zero_error));
                    return;
                }
                else {
                    apPassLayout.setError("");
                }

                if (Tools.validateWifiPassword(apPassInput, 8, 64).equals("MIN_ERROR")) {
                    apPassLayout.setError(getApplicationContext().getResources().getText(R.string.wifi_setup_pass_min_error));
                    return;
                }
                else {
                    apPassLayout.setError("");
                }

                if (Tools.validateWifiPassword(apPassInput, 8, 64).equals("MAX_ERROR")) {
                    apPassLayout.setError(getApplicationContext().getResources().getText(R.string.wifi_setup_pass_max_error));
                    return;
                }
                else {
                    apPassLayout.setError("");
                }

                if (Objects.equals(apPassInput.getText().toString(), Tools.DEFAULT_WIFI_PASSWORD)) {
                    apPassLayout.setError(getApplicationContext().getResources().getText(R.string.wifi_setup_pass_equal_to_default_error));
                    return;
                }
                else {
                    apPassLayout.setError("");
                }

                if (Tools.validateWifiSsid(routerSsidInput, 8, 40).equals("ZERO_ERROR")) {
                    routerSsidLayout.setError(getApplicationContext().getResources().getText(R.string.wifi_setup_ssid_zero_error));
                    return;
                }
                else {
                    routerSsidLayout.setError("");
                }

                if (Tools.validateWifiSsid(routerSsidInput, 8, 40).equals("MIN_ERROR")) {
                    routerSsidLayout.setError(getApplicationContext().getResources().getText(R.string.wifi_setup_ssid_min_error));
                    return;
                }
                else {
                    routerSsidLayout.setError("");
                }

                if (Tools.validateWifiSsid(routerSsidInput, 8, 40).equals("MAX_ERROR")) {
                    routerSsidLayout.setError(getApplicationContext().getResources().getText(R.string.wifi_setup_ssid_max_error));
                    return;
                }
                else {
                    routerSsidLayout.setError("");
                }

                if (Tools.validateWifiPassword(routerPassInput, 8, 64).equals("ZERO_ERROR")) {
                    routerPassLayout.setError(getApplicationContext().getResources().getText(R.string.wifi_setup_pass_zero_error));
                    return;
                }
                else {
                    routerPassLayout.setError("");
                }

                if (Tools.validateWifiPassword(routerPassInput, 8, 64).equals("MIN_ERROR")) {
                    routerPassLayout.setError(getApplicationContext().getResources().getText(R.string.wifi_setup_pass_min_error));
                    return;
                }
                else {
                    routerPassLayout.setError("");
                }

                if (Tools.validateWifiPassword(routerPassInput, 8, 64).equals("MAX_ERROR")) {
                    routerPassLayout.setError(getApplicationContext().getResources().getText(R.string.wifi_setup_pass_max_error));
                    return;
                }
                else {
                    routerPassLayout.setError("");
                }

                AP_PASS     = Objects.requireNonNull(apPassInput.getText()).toString();
                ROUTER_SSID = Objects.requireNonNull(routerSsidInput.getText()).toString();
                ROUTER_PASS = Objects.requireNonNull(routerPassInput.getText()).toString();

                String url = "http://" + Tools.DEFAULT_AP_STATIC_IP + "/server_network";
                saveRouter(getApplicationContext(), url, "SETUP_SERVER_ROUTER", AP_PASS, ROUTER_SSID, ROUTER_PASS);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        //SecurityCheckSocketsService = new Intent(this, SocketBackgroundService.class);
        //SocketBackgroundService.startSockets(SecurityCheckSocketsService, getApplicationContext(), SocketsReceiver, Tools.DEFAULT_AP_STATIC_IP, "80", "setup_ws");
        //if (_DNS != null) {
            //_DNS.discoverServices();
        //}
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        //SocketBackgroundService.stopSockets(SecurityCheckSocketsService, getApplicationContext(), SocketsReceiver);
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

    public static void securityCheckGetConnectionStatus(Context context, boolean wifi, boolean sockets, boolean securityCheck, boolean internet, String soc_message) {

        if(wifi){
            if(NetworkConnectStatus != null) {
                NetworkConnectStatus.setText(R.string.connected_text);
                NetworkConnectStatus.setTextColor(ContextCompat.getColor(context, R.color.success));
            }
        }
        else
        {
            if(NetworkConnectStatus != null) {
                NetworkConnectStatus.setText(R.string.not_connected_text);
                NetworkConnectStatus.setTextColor(ContextCompat.getColor(context, R.color.error));

                SocketsStatus.setText(R.string.not_connected_text);
                SocketsStatus.setTextColor(ContextCompat.getColor(context, R.color.error));
            }
        }

        if(sockets){

            if(SocketsStatus != null) {

                if(securityCheck) {

                    fieldsLayout.setVisibility(View.VISIBLE);
                    endMessage.setVisibility(View.GONE);
                    saveBtn.setVisibility(View.VISIBLE);
                    SocketsStatus.setText(R.string.connected_text);
                    SocketsStatus.setTextColor(ContextCompat.getColor(context, R.color.success));
                }
                else
                {
                    if(soc_message == null || soc_message.equals("APP_SECURITY_KEY_NOT_SAME")) {

                        fieldsLayout.setVisibility(View.GONE);
                        endMessage.setVisibility(View.VISIBLE);
                        saveBtn.setVisibility(View.GONE);
                        SocketsStatus.setText(R.string.not_auth_text);
                        SocketsStatus.setTextColor(ContextCompat.getColor(context, R.color.error));
                        endMessage.setText(R.string.wifi_setup_security_check_ko);
                        endMessage.setTextColor(ContextCompat.getColor(context, R.color.error));
                        moveBtn.setText(R.string.terminate_button);

                        moveBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                /*intent = new Intent(context, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);*/
                            }
                        });
                    }
                }
            }
        }
        else
        {
            if(SocketsStatus != null) {

                if(!securityCheck) {

                    fieldsLayout.setVisibility(View.GONE);
                    endMessage.setVisibility(View.VISIBLE);
                    saveBtn.setVisibility(View.GONE);
                    SocketsStatus.setText(R.string.not_auth_text);
                    SocketsStatus.setTextColor(ContextCompat.getColor(context, R.color.error));
                    endMessage.setText(R.string.wifi_setup_security_check_ko);
                    endMessage.setTextColor(ContextCompat.getColor(context, R.color.error));
                    moveBtn.setText(R.string.terminate_button);

                    moveBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            /*intent = new Intent(context, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);*/
                        }
                    });
                }
                else
                {
                    SocketsStatus.setText(R.string.not_connected_text);
                    SocketsStatus.setTextColor(ContextCompat.getColor(context, R.color.error));
                }


            }
        }
    }

    public void saveRouter(final Context context, String jsonUrl, String question, String apPass, String routerSsid, String routerPass) {

        Map<String, String> postParam = new HashMap<String, String>();

        postParam.put("APP_ID", Tools.APP_ID);
        postParam.put("QUESTION", question);
        postParam.put("AP_PASS", apPass);
        postParam.put("ROUTER_SSID", routerSsid);
        postParam.put("ROUTER_PASS", routerPass);

        RetryPolicy mRetryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, jsonUrl, new JSONObject(postParam), response -> {

            System.out.println("WIFI ROUTER SETUP DATA RESPONSE: " + response.toString());

            if(!response.toString().isEmpty()) {

                try {

                    JSONObject jsonObject    = new JSONObject(response.toString());
                    JSONObject main_response = new JSONObject(jsonObject.getString("RESPONSE"));
                    String server_status     = main_response.getString("STATUS");

                    if(server_status.equals("SERVER_SETUP_APP_ID_KO")){

                        fieldsLayout.setVisibility(View.GONE);
                        endMessage.setVisibility(View.VISIBLE);
                        endMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                        endMessage.setText(R.string.wifi_setup_security_check_ko);

                        saveBtn.setText(R.string.terminate_button);

                        saveBtn.setOnClickListener(v -> {

                            finish();
                            System.exit(0);
                        });
                    }
                    else
                    {

                        Tools.setStringPref(context, "APP_ID", Tools.APP_ID);

                        if(server_status.equals("SERVER_SETUP_OK")) {

                            int _setup_ok, _id, _router_setup_ok, _use_sta_static, _use_ap_static, _ap_ssid_hidden, _router_setup, _remote_access, _ap_max_clients, _ap_channel;
                            String _name, _model, _ap_ssid, _ap_pass, _sta_static_ip, _local_ip, _gateway_ip, _netmask_ip, _dns_ip, _remote_ip, _remote_domain, _sta_host, _sta_mac, _ap_mac, _ap_static_ip, _ap_ip, _ap_gateway_ip, _ap_netmask_ip, _ap_host, _freq, _heap, _spiffs, _router_ssid, _router_pass;

                            _setup_ok          = 0;
                            _id                = main_response.getInt("ID");
                            _name              = main_response.getString("NAME");
                            _model             = main_response.getString("MODEL");
                            _ap_ssid           = main_response.getString("AP_SSID");
                            _ap_pass           = main_response.getString("AP_PASS");
                            _use_sta_static    = 0; //main_response.getString("USE_AP_STATIC_IP");
                            _sta_static_ip     = "EMPTY"; //main_response.getString("STA_STATIC_IP");
                            _local_ip          = "EMPTY";
                            _gateway_ip        = "EMPTY";
                            _netmask_ip        = "EMPTY";
                            _dns_ip            = "EMPTY";
                            _remote_access     = Integer.parseInt(main_response.getString("REMOTE_ACCESS"));
                            _remote_ip         = "EMPTY";
                            _remote_domain     = "EMPTY";
                            _sta_host          = "EMPTY";
                            _sta_mac           = "EMPTY";
                            _ap_mac            = "EMPTY";
                            _use_ap_static     = 0; //main_response.getString("USE_AP_STATIC_IP");
                            _ap_static_ip      = "EMPTY"; //main_response.getString("AP_STATIC_IP");
                            _ap_ip             = "EMPTY";
                            _ap_gateway_ip     = "EMPTY";
                            _ap_netmask_ip     = "EMPTY";
                            _ap_host           = "EMPTY";
                            _ap_max_clients    = 0;
                            _ap_channel        = 0;
                            _ap_ssid_hidden    = 0;
                            _freq              = main_response.getString("FREQ");
                            _heap              = main_response.getString("HEAP");
                            _spiffs            = main_response.getString("SPIFFS");
                            _router_setup      = Integer.parseInt(main_response.getString("ROUTER_SETUP"));


                            _router_ssid = main_response.getString("ROUTER_SSID");
                            _router_pass = main_response.getString("ROUTER_PASS");

                            String response_message = main_response.getString("MESSAGE");

                            String insert_status = ServerSQLHelper.dbHelper.ServerInsert(
                                _id,
                                _setup_ok,
                                _name,
                                _model,
                                _ap_ssid,
                                _ap_pass,
                                _use_sta_static,
                                _sta_static_ip,
                                _local_ip,
                                _gateway_ip,
                                _netmask_ip,
                                _dns_ip,
                                _remote_access,
                                _remote_ip,
                                _remote_domain,
                                _sta_host,
                                _sta_mac,
                                _ap_mac,
                                _use_ap_static,
                                _ap_static_ip,
                                _ap_ip,
                                _ap_gateway_ip,
                                _ap_netmask_ip,
                                _ap_host,
                                _ap_max_clients,
                                _ap_channel,
                                _ap_ssid_hidden,
                                _freq,
                                _heap,
                                _spiffs,
                                _router_setup,
                                _router_ssid,
                                _router_pass
                            );

                            if(insert_status.equals("INSERT_OK")) {

                                /* TODO: add database implementation */

                                fieldsLayout.setVisibility(View.GONE);
                                endMessage.setVisibility(View.VISIBLE);
                                moveBtn.setVisibility(View.GONE);
                                endMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.success));
                                endMessage.setText(R.string.wifi_setup_end_success);

                                saveBtn.setText(R.string.button_next);

                                saveBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Tools.setSetupCurrentStep(getApplicationContext(), 3);

                                        intent = new Intent(context, NewServerSettings.class);
                                        intent.putExtra("SSID_TYPE", ssid_type);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);

                                    }
                                });
                            }
                            else if(insert_status.equals("DUPLICATED")) {

                                fieldsLayout.setVisibility(View.GONE);
                                endMessage.setVisibility(View.VISIBLE);
                                moveBtn.setVisibility(View.GONE);
                                endMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                                endMessage.setText(R.string.setup_server_insert_error);

                                saveBtn.setText(R.string.terminate_button);

                                saveBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Tools.setSetupCurrentStep(getApplicationContext(), 3);

                                        intent = new Intent(context, NewServerSettings.class);
                                        intent.putExtra("SSID_TYPE", ssid_type);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                    }
                                });

                            }
                        }
                        else
                        {
                            if (server_status.equals("SERVER_SETUP_AP_PASS_KO")) {

                                fieldsLayout.setVisibility(View.GONE);
                                endMessage.setVisibility(View.VISIBLE);
                                moveBtn.setVisibility(View.GONE);
                                endMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                                endMessage.setText(R.string.wifi_setup_ap_pass_error);

                                saveBtn.setText(R.string.retry_button);

                                saveBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        System.out.println("ERROR MESSAGE");
                                    }
                                });
                            }

                            if(server_status.equals("SERVER_SETUP_ROUTER_SSID_KO")){

                                fieldsLayout.setVisibility(View.GONE);
                                endMessage.setVisibility(View.VISIBLE);
                                moveBtn.setVisibility(View.GONE);
                                endMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                                endMessage.setText(R.string.wifi_setup_router_ssid_error);

                                saveBtn.setText(R.string.retry_button);

                                saveBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        System.out.println("ERROR MESSAGE");
                                    }
                                });
                            }

                            if(server_status.equals("SERVER_SETUP_ROUTER_PASS_KO")){

                                fieldsLayout.setVisibility(View.GONE);
                                endMessage.setVisibility(View.VISIBLE);
                                moveBtn.setVisibility(View.GONE);
                                endMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                                endMessage.setText(R.string.wifi_setup_router_pass_error);

                                saveBtn.setText(R.string.retry_button);

                                saveBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        System.out.println("ERROR MESSAGE");
                                    }
                                });
                            }
                        }
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
}