package com.cyrenaica.cyrenaicaserver.server.adaptars;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.cyrenaica.cyrenaicaserver.R;
import com.cyrenaica.cyrenaicaserver.language.LocalHelper;
import com.cyrenaica.cyrenaicaserver.server.ServerHomeActivity;

public class ServerAttachedDevicesListHolder extends LinearLayout{

    private static final String TAG = "ATTACHED_DEVICES_HOLDER";
    Context mContext;
    ServerAttachedDevicesListData mItems;

    TextView device_name_label;
    TextView device_name_value;
    TextView device_id_label;
    TextView device_id_value;
    TextView device_model_label;
    TextView device_model_value;
    TextView device_type_label;
    TextView device_type_value;
    TextView device_wifi_connection_status_label;
    TextView device_wifi_connection_status_value;
    TextView device_espnow_connection_status_label;
    TextView device_espnow_connection_status_value;


    public ServerAttachedDevicesListHolder(Context context) {
        super(context);
        mContext = context;
        setup();
    }

    public ServerAttachedDevicesListHolder(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setup();
    }

    public ServerAttachedDevicesListHolder(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setup();
    }

    public ServerAttachedDevicesListHolder(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        setup();
    }

    private void setup() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.servers_attached_devices_custom_list_item, this);
    }

    public void setServerAttachedDevicesView(ServerAttachedDevicesListData items) {

        mItems = items;
        boolean is_server_sockets_connected = ServerHomeActivity.getServerSocketsConnected();
        String server_sockets_response      = ServerHomeActivity.getServerSocketsConnectResponse();

        device_name_label                        = findViewById(R.id.id_device_name_label);
        device_name_value                        = findViewById(R.id.id_device_name_value);
        device_id_label                          = findViewById(R.id.id_device_id_label);
        device_id_value                          = findViewById(R.id.id_device_id_value);
        device_model_label                       = findViewById(R.id.id_device_model_label);
        device_model_value                       = findViewById(R.id.id_device_model_value);
        device_type_label                        = findViewById(R.id.id_device_type_label);
        device_type_value                        = findViewById(R.id.id_device_type_value);
        device_wifi_connection_status_label      = findViewById(R.id.id_device_wifi_connection_status_label);
        device_wifi_connection_status_value      = findViewById(R.id.id_device_wifi_connection_status_value);
        device_espnow_connection_status_label    = findViewById(R.id.id_device_espnow_connection_status_label);
        device_espnow_connection_status_value    = findViewById(R.id.id_device_espnow_connection_status_value);

        if (LocalHelper.getLanguage(mContext).equalsIgnoreCase("ar")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                device_name_label.setTypeface(getResources().getFont(R.font.changa));
                device_id_label.setTypeface(getResources().getFont(R.font.changa));
                device_model_label.setTypeface(getResources().getFont(R.font.changa));
                device_type_label.setTypeface(getResources().getFont(R.font.changa));
            }
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                /*device_name_label.setTypeface(getResources().getFont(R.font.bayon));
                device_id_label.setTypeface(getResources().getFont(R.font.bayon));
                device_model_label.setTypeface(getResources().getFont(R.font.bayon));
                device_type_label.setTypeface(getResources().getFont(R.font.bayon));*/

            }
        }

        int device_id                    = mItems.getDeviceId();
        String _device_attache_id        = mItems.getAttacheId();
        String _device_name              = mItems.getDeviceName();
        String _device_type              = mItems.getDeviceType();
        String _device_model             = mItems.getDeviceModel();
        String _ap_ip                    = mItems.getDeviceApIp();
        String _net_ip                   = mItems.getDeviceNetIp();
        String _wifi_connection_status   = mItems.getDeviceWifiConnectionStatus();
        String _espnow_connection_status = mItems.getDeviceEspnowConnectionStatus();

        device_id_value.setText(String.valueOf(device_id));
        device_name_value.setText(_device_name);
        device_type_value.setText(_device_type);
        device_model_value.setText(_device_model);

        device_wifi_connection_status_value.setText(_wifi_connection_status);
        device_espnow_connection_status_value.setText(_espnow_connection_status);

        if(_wifi_connection_status.equals("DISCONNECTED")){
            device_wifi_connection_status_value.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.error)));
        }
        else
        {
            device_wifi_connection_status_value.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.success)));
        }

        if(_espnow_connection_status.equals("DISCONNECTED")){
            device_espnow_connection_status_value.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.error)));
        }
        else
        {
            device_espnow_connection_status_value.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.success)));
        }
    }
}
