package com.cyrenaica.cyrenaicaserver.tools;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.WIFI_SERVICE;

import static androidx.compose.ui.semantics.SemanticsPropertiesKt.dismiss;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.RouteInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.location.LocationManagerCompat;

import com.cyrenaica.cyrenaicaserver.R;
import com.cyrenaica.cyrenaicaserver.language.LocalHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.File;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Tools {

    private static final String TAG = "TOOLS";

    public static String DEFAULT_TFT_BOX_APP_ID     = "DEFV1-TFTI7-BOXR3-ESP2"; // for tft box devices
    public static String DEFAULT_CAMERA_APP_ID      = "DEFV1-CAMI0-THINK-ESP2"; // for cameras devices
    public static String DEFAULT_CONTROLLER_APP_ID  = "DEFV1-TFTI7-BOXR3-ESP2"; // for controllers devices
    public static String DEFAULT_DETECTOR_APP_ID    = "DEFV1-TFTI7-BOXR3-ESP2"; // for detectors devices
    public static String ALL_DEVICES_DEFAULT_APP_ID[] = {DEFAULT_TFT_BOX_APP_ID, DEFAULT_CAMERA_APP_ID, DEFAULT_CONTROLLER_APP_ID, DEFAULT_DETECTOR_APP_ID};

    public static String DEVICES_TYPES_ARRAY[] = {"CAMERA", "CONTROLLER", "DETECTOR"};

    public static boolean IS_SETUP_OK                = true;
    public static int SETUP_CURRENT_STEP             = 0;

    public static int REQUEST_CHECK_CODE = 1972;

    public static String APP_ID                    = "Q1MLO-PK9LI-E3K8J-SX5JI";
    public static String DEFAULT_AP_STATIC_IP      = "10.10.10.10";
    public static String DEFAULT_AP_IP             = "192.168.4.1";
    public static String DEFAULT_CAMERAS_AP_IP     = "11.11.11.11"; // cameras static ip if activated
    public static String DEFAULT_DETECTORS_AP_IP   = "12.12.12.12";
    public static String DEFAULT_CONTROLLERS_AP_IP = "13.13.13.13";

    public static String LIVE_WIFI_STATUS          = "";
    public static String CURRENT_WIFI_SSID         = "";
    public static String CURRENT_WIFI_SSID_TYPE    = "";
    public static String DEFAULT_WIFI_PASSWORD     = "QNS5QCKVK1J6";
    static AlertDialog loadDialog;
    static boolean _connected_to_ap = false; // variable used in connectToAP() method


    public static void setFirstTimeIntro(Context context, boolean setup_ok) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("IS_SETUP_OK", setup_ok);
        editor.apply();
    }

    public static boolean getFirstTimeIntro(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("IS_SETUP_OK", false);
    }

    public static void setSetupCurrentStep(Context context, int step) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("SETUP_CURRENT_STEP", step);
        editor.apply();
    }

    public static int getSetupCurrentStep(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("SETUP_CURRENT_STEP", 0);
    }

    public static void setStringPref(Context context, String prefKey, String prefValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(prefKey, prefValue);
        editor.apply();
    }

    public static String getStringPref(Context context, String prefKey) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(prefKey, null);
    }

    public static void setBoolPref(Context context, String prefKey, boolean prefValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(prefKey, prefValue);
        editor.apply();
    }

    public static Boolean getBoolPref(Context context, String prefKey) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(prefKey, false);
    }

    /* TOOLS */
    public static void hideSoftKeyboard(Activity activity) {

        if (activity.getCurrentFocus() != null) {

            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

            if (inputMethodManager.isAcceptingText()) {
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public static boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return LocationManagerCompat.isLocationEnabled(locationManager);
    }

    public static boolean checkPlayServices(Context context, Activity activity) {

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);

        if (resultCode != ConnectionResult.SUCCESS) {

            if (apiAvailability.isUserResolvableError(resultCode)) {
                //apiAvailability.getErrorDialog(activity, resultCode, REQUEST_CHECK_COD.show();
            }
            else
            {
                Log.i(TAG, "This device is not supported.");
                activity.finish();
            }

            return false;
        }

        return true;
    }

    public static boolean checkDefaultAppId(String id){

        int length = ALL_DEVICES_DEFAULT_APP_ID.length;

        for(int i = 0; i < length; i++){

            if(ALL_DEVICES_DEFAULT_APP_ID[i].equals(id)){

                Log.w(TAG, "DEVICE SETUP DEFAULT ID OK");

                return true;
            }
        }

        Log.e(TAG, "DEVICE SETUP DEFAULT ID KO");

        return false;
    }

    public static boolean isPasswordValid(String password) {

        if(password.isEmpty() || password.length() > 64){
            return false;
        }

        return password.length() >= 8;
    }

    public static String validateWifiPassword(EditText etText, int min, int max){

        if(etText.getText().length() == 0){
            return "ZERO_ERROR";
        }

        if(etText.getText().length() < min){
            return "MIN_ERROR";
        }

        if(etText.getText().length() > max){
            return "MAX_ERROR";
        }

        return "PASSWORD_OK";
    }

    public static String validateWifiSsid(EditText etText, int min, int max){

        if(etText.getText().length() == 0){
            return "ZERO_ERROR";
        }

        if(etText.getText().length() < min){
            return "MIN_ERROR";
        }

        if(etText.getText().length() > max){
            return "MAX_ERROR";
        }

        return "SSID_OK";
    }

    public static String validateDeviceName(EditText etText, int min, int max) {

        if (etText.getText().length() == 0) {
            return "ZERO_ERROR";
        }

        if (etText.getText().length() < min) {
            return "MIN_ERROR";
        }

        if (etText.getText().length() > max) {
            return "MAX_ERROR";
        }

        return "DEVICE_NAME_OK";
    }

    public static boolean numbersInputIsEmpty(EditText etText){

        if(etText.getText().length() == 0){
            return true;
        }

        return false;
    }

    public static boolean convertEspBoolean(String arg){

        if(arg.equals("1")){
            return Boolean.valueOf("true");
        }

        return Boolean.valueOf("false");
    }

    @SuppressLint("DefaultLocale")
    public static String getConnectedApIp(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int ip = wifiManager.getDhcpInfo().serverAddress;
        return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff)); //11.11.11.11*/
    }

    public static void LoadingDialog(Context context, Activity activity, int title, int message, int text){

        final AlertDialog.Builder loading_dialog_builder  = new AlertDialog.Builder(context);
        View mView                                        = activity.getLayoutInflater().inflate(R.layout.loading_dialog_view, null);

        final TextView _title   = mView.findViewById(R.id.id_title);
        final TextView _message = mView.findViewById(R.id.id_loading_msg);
        final TextView _text    = mView.findViewById(R.id.id_loading_text);

        _title.setText(title);
        _message.setText(message);
        _text.setText(text);

        loading_dialog_builder.setView(mView);

        loadDialog = loading_dialog_builder.create();

        loadDialog.setCanceledOnTouchOutside(false);

        loadDialog.show();

    }
    public static void CloseLoadingDialog(){
        loadDialog.dismiss();
    }

    public static void errorDialog(Context context, int label_string, int error_string, int ok_btn_string, int cancel_btn_string, Class<?> restartPage){


        final AlertDialog.Builder errorAlert = new AlertDialog.Builder(context);
        LayoutInflater inflater              = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE );
        View ErrorView                       = inflater.inflate(R.layout.new_server_settings_error_dialog, null );

        final TextView dialog_label          = (TextView) ErrorView.findViewById(R.id.id_dialog_label);
        final TextView message               = (TextView) ErrorView.findViewById(R.id.id_new_server_settings_dialog_error_msg);
        final TextView btn_retry             = (TextView) ErrorView.findViewById(R.id.id_btn_retry);
        final TextView btn_restart           = (TextView) ErrorView.findViewById(R.id.id_btn_cancel);

        if (LocalHelper.getLanguage(context).equalsIgnoreCase("ar")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dialog_label.setTypeface(context.getResources().getFont(R.font.cairo));
                message.setTypeface(context.getResources().getFont(R.font.cairo));
                btn_retry.setTypeface(context.getResources().getFont(R.font.cairo));
                btn_restart.setTypeface(context.getResources().getFont(R.font.cairo));
            }
        }

        message.setTextColor(context.getResources().getColor(R.color.error));

        dialog_label.setText(label_string);
        message.setText(error_string);
        btn_retry.setText(ok_btn_string);
        btn_restart.setText(cancel_btn_string);

        errorAlert.setView(ErrorView);

        final AlertDialog errorDialog = errorAlert.create();

        errorDialog.setCanceledOnTouchOutside(false);

        btn_restart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                errorDialog.dismiss();
                Intent intent = new Intent(context, restartPage);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);


            }
        });

        btn_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                errorDialog.dismiss();
            }
        });

        errorDialog.show();
    }

    public static boolean connectToApToReset(Context context, String ssid, String password){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
            builder.setSsid(ssid);
            builder.setWpa2Passphrase(password);

            WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();

            NetworkRequest.Builder networkRequestBuilder1 = new NetworkRequest.Builder();

            networkRequestBuilder1.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
            networkRequestBuilder1.setNetworkSpecifier(wifiNetworkSpecifier);
            networkRequestBuilder1.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED);

            NetworkRequest nr = networkRequestBuilder1.build();

            final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {

                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    Log.d("ON_AVAILABLE", "Network Available");

                    cm.bindProcessToNetwork(network);

                    NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                    if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

                        if(cm.isDefaultNetworkActive()){
                            cm.reportNetworkConnectivity(network, false);
                            _connected_to_ap = true;
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
                    _connected_to_ap = false;
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    Log.d("ON_UNAVAILABLE", "Network is Unavailable");
                    _connected_to_ap = false;
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

        return _connected_to_ap;

    }

    /*  FILE & STORAGE */
    public static String formatSize(long size) {

        String suffix = null;

        if (size >= 1024) {

            suffix = " KB";
            size /= 1024;

            if (size >= 1024) {

                suffix = " MB";
                size /= 1024;

                if (size >= 1024) {

                    suffix = " GB";
                    size /= 1024;

                    if (size >= 1024) {

                        suffix = " TB";
                        size /= 1024;
                    }
                }
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;

        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }


}
