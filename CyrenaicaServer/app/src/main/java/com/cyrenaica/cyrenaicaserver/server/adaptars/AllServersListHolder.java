package com.cyrenaica.cyrenaicaserver.server.adaptars;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.cyrenaica.cyrenaicaserver.R;
import com.cyrenaica.cyrenaicaserver.language.LocalHelper;
import com.cyrenaica.cyrenaicaserver.nodes.ViewAllDevices;
import com.cyrenaica.cyrenaicaserver.server.ServerHomeActivity;
import com.cyrenaica.cyrenaicaserver.server.ServerSettings;
import com.cyrenaica.cyrenaicaserver.tools.ConnectionReceiver;
import com.cyrenaica.cyrenaicaserver.tools.Connectivity;
import com.cyrenaica.cyrenaicaserver.tools.SocketsService;

public class AllServersListHolder extends LinearLayout{

    private static final String TAG = "ALL_SERVERS_LIST_HOLDER";
    static Context mContext;
    AllServersListData mItems;

    static ImageView server_presence_icon;
    static TextView connection_indication_text;
    static TextView connection_secured_text;
    static TextView sub_title;
    CardView card_view;
    LinearLayout hidden_view;
    ImageView sub_expand_icon;
    TextView sub_connection_type_label;
    static TextView sub_connection_type;
    TextView sub_ip_address_label;
    static TextView sub_ip_address;
    TextView sub_mac_address_label;
    static TextView sub_mac_address;
    TextView server_name;
    TextView server_model_label;
    TextView server_model_value;
    TextView server_spiffs_label;
    TextView server_spiffs_value;
    TextView server_heap_label;
    TextView server_heap_value;
    TextView server_freq_label;
    TextView server_freq_value;
    TextView server_devices_count_f_paragraph;
    TextView server_devices_count_digits;
    TextView server_devices_count_l_paragraph;
    Button server_details_button;
    Button server_settings_button;
    Button server_view_devices_button;

    Intent AllServersSocketsService;
    static ConnectionReceiver SocketsReceiver = new ConnectionReceiver();

    public View server_name_sub_inflated;

    private static boolean IS_SOCKETS_CONNECTED;
    public static String SOCKETS_CONNECT_RESPONSE;
    String _connection_ip;
    public static boolean soc_setup_ok;

    public AllServersListHolder(Context context) {
        super(context);
        mContext = context;
        setup();
    }

    public AllServersListHolder(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setup();
    }

    public AllServersListHolder(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setup();
    }

    public AllServersListHolder(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        setup();
    }

    private void setup() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.all_servers_custom_list_item, this);
    }

    public void setAllServersView(AllServersListData items) {

        mItems = items;

        int server_id       = mItems.getServerId();
        String _server_name = mItems.getServerName();
        String _server_ssid = mItems.getServerApSsid();
        String _server_pass = mItems.getServerApPass();

        int is_static_ip = mItems.getServerStaUseStaticIp();

        // If the server use the (use_sta_static_ip = 1) we put the STA static ip
        if(is_static_ip == 1){
            _connection_ip = mItems.getServerStaStaticIp();

        }
        else// If no we put the STA local ip
        {
            _connection_ip = mItems.getServerLocalIp();
        }

        SocketsService.registerSockets(mContext, SocketsReceiver, "SERVERS_SOCKETS_CONNECTED", TAG);

        AllServersSocketsService = new Intent(mContext, SocketsService.class);
        SocketsService.startSockets(AllServersSocketsService, mContext, SocketsReceiver, _connection_ip, "80", "production_ws", "SERVER", TAG);

        server_presence_icon               = findViewById(R.id.id_server_home_presence_status_icon);
        connection_indication_text         = findViewById(R.id.id_server_connection_indication_text);
        connection_secured_text            = findViewById(R.id.id_is_connection_secured_text);
        server_name                        = findViewById(R.id.id_server_name_text);
        server_model_label                 = findViewById(R.id.id_server_model_label);
        server_model_value                 = findViewById(R.id.id_server_model_value);
        server_spiffs_label                = findViewById(R.id.id_server_spiffs_label);
        server_spiffs_value                = findViewById(R.id.id_server_spiffs_value);
        server_heap_label                  = findViewById(R.id.id_server_heap_label);
        server_heap_value                  = findViewById(R.id.id_server_heap_value);
        server_freq_label                  = findViewById(R.id.id_server_frequancey_label);
        server_freq_value                  = findViewById(R.id.id_server_frequancey_value);

        server_devices_count_f_paragraph   = findViewById(R.id.id_server_devices_count_first_paragraph);
        server_devices_count_digits        = findViewById(R.id.id_server_devices_count_digits);
        server_devices_count_l_paragraph   = findViewById(R.id.id_server_devices_count_last_paragraph);
        server_details_button              = findViewById(R.id.id_server_detailes_button);
        server_settings_button             = findViewById(R.id.id_server_settings_button);
        server_view_devices_button         = findViewById(R.id.id_server_devices_button);

        /* Server name sub expanded view */
        ViewStub name_sub_stub     = findViewById(R.id.id_server_name_sub_stub_view);
        server_name_sub_inflated   = name_sub_stub.inflate();

        sub_title                  = server_name_sub_inflated.findViewById(R.id.id_network_status_label);
        card_view                  = server_name_sub_inflated.findViewById(R.id.id_base_cardview);
        hidden_view                = server_name_sub_inflated.findViewById(R.id.is_hidden_layout);
        sub_expand_icon            = server_name_sub_inflated.findViewById(R.id.id_network_status_expand_arrow);
        sub_connection_type_label  = server_name_sub_inflated.findViewById(R.id.id_connection_type_label);
        sub_connection_type        = server_name_sub_inflated.findViewById(R.id.id_connection_type_value);
        sub_ip_address_label       = server_name_sub_inflated.findViewById(R.id.id_ip_address_label);
        sub_ip_address             = server_name_sub_inflated.findViewById(R.id.id_ip_address_value);
        sub_mac_address_label      = server_name_sub_inflated.findViewById(R.id.id_mac_address_label);
        sub_mac_address            = server_name_sub_inflated.findViewById(R.id.id_mac_address_value);

        if (LocalHelper.getLanguage(mContext).equalsIgnoreCase("ar")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                server_name.setTypeface(getResources().getFont(R.font.changa));
                server_model_label.setTypeface(getResources().getFont(R.font.cairo));
                server_spiffs_label.setTypeface(getResources().getFont(R.font.cairo));
                server_heap_label.setTypeface(getResources().getFont(R.font.cairo));
                server_freq_label.setTypeface(getResources().getFont(R.font.cairo));
                server_devices_count_f_paragraph.setTypeface(getResources().getFont(R.font.cairo));
                server_devices_count_l_paragraph.setTypeface(getResources().getFont(R.font.cairo));
                server_details_button.setTypeface(getResources().getFont(R.font.cairo));
                server_settings_button.setTypeface(getResources().getFont(R.font.cairo));
                server_view_devices_button.setTypeface(getResources().getFont(R.font.cairo));
            }
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                server_name.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
                server_model_label.setTypeface(getResources().getFont(R.font.robotoslab));
                server_spiffs_label.setTypeface(getResources().getFont(R.font.robotoslab));
                server_heap_label.setTypeface(getResources().getFont(R.font.robotoslab));
                server_freq_label.setTypeface(getResources().getFont(R.font.robotoslab));
                server_devices_count_f_paragraph.setTypeface(getResources().getFont(R.font.robotoslab));
                server_devices_count_l_paragraph.setTypeface(getResources().getFont(R.font.robotoslab));
                server_details_button.setTypeface(getResources().getFont(R.font.robotoslab));
                server_settings_button.setTypeface(getResources().getFont(R.font.robotoslab));
                server_view_devices_button.setTypeface(getResources().getFont(R.font.robotoslab));
            }
        }

        server_name.setText(_server_name);

        // If the server use the (use_sta_static_ip = 1) we put the STA static ip
        if(is_static_ip == 1){
            sub_ip_address.setText(mItems.getServerStaStaticIp());
        }
        else// If no we put the STA local ip
        {
            sub_ip_address.setText(mItems.getServerLocalIp());
        }

        sub_mac_address.setText(mItems.getServerStaMac());

        sub_expand_icon.setOnClickListener(view -> {

            // If the CardView is already expanded, set its visibility
            // to gone and change the expand less icon to expand more.
            if (hidden_view.getVisibility() == View.GONE) {
                // The transition of the hiddenView is carried out by the TransitionManager class.
                // Here we use an object of the AutoTransition Class to create a default transition
                TransitionManager.beginDelayedTransition(card_view, new AutoTransition());
                hidden_view.setVisibility(View.VISIBLE);
                sub_expand_icon.setImageResource(R.drawable.ic_expand_less_orange_24);
            }

            // If the CardView is not expanded, set its visibility to
            // visible and change the expand more icon to expand less.
            else
            {
                TransitionManager.beginDelayedTransition(card_view, new AutoTransition());
                hidden_view.setVisibility(View.GONE);
                sub_expand_icon.setImageResource(R.drawable.ic_expand_more_orange_24);
            }

        });

        server_model_value.setText(mItems.getServerModel());
        server_spiffs_value.setText(mItems.getServerSpiffs());
        server_heap_value.setText(mItems.getServerHeap());
        server_freq_value.setText(mItems.getServerFreq());
        server_devices_count_digits.setText(String.valueOf(mItems.getServerDevicesCount()));

        server_details_button.setOnClickListener(view -> {

            Intent intent = new Intent(getContext(), ServerHomeActivity.class);
            intent.putExtra("server_id",server_id);
            intent.putExtra("server_name",_server_name);
            intent.putExtra("server_ip",_connection_ip);
            intent.putExtra("server_ssid",_server_ssid);
            intent.putExtra("server_pass",_server_pass);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);

        });

        server_settings_button.setOnClickListener(view -> {

            Intent intent = new Intent(getContext(), ServerSettings.class);
            intent.putExtra("server_id", server_id);
            intent.putExtra("server_name", _server_name);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);

        });

        server_view_devices_button.setOnClickListener(view -> {

            Intent intent = new Intent(getContext(), ViewAllDevices.class);
            intent.putExtra("server_id", server_id);
            intent.putExtra("server_name", _server_name);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);

        });

    }

    public static boolean getServerSocketsConnected(){
        return IS_SOCKETS_CONNECTED;
    }

    public static String getServerSocketsConnectResponse(){
        return SOCKETS_CONNECT_RESPONSE;
    }

    public static void IfSocketsConnected(boolean sockets_connected){

        IS_SOCKETS_CONNECTED = sockets_connected;

        if(IS_SOCKETS_CONNECTED){

            server_presence_icon.setImageResource(R.drawable.online_prediction_24);
            connection_secured_text.setVisibility(View.VISIBLE);

            connection_indication_text.setText(R.string.word_connected_first_cap_small);
            connection_indication_text.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.success)));

            sub_connection_type.setText(Connectivity.getConnectionType());
            sub_title.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.success)));
        }
        else
        {
            server_presence_icon.setImageResource(R.drawable.offline_prediction_24);

            sub_title.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.error)));
            connection_secured_text.setVisibility(View.GONE);

            connection_indication_text.setText(R.string.word_disconnected_first_cap_small);
            connection_indication_text.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.error)));

            sub_connection_type.setText(R.string.word_disconnected_first_cap_small);
            sub_connection_type.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.error)));

            sub_ip_address.setText(R.string.word_disconnected_first_cap_small);
            sub_ip_address.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.error)));

            sub_mac_address.setText(R.string.word_disconnected_first_cap_small);
            sub_mac_address.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.error)));
        }
    }

    public static void IfConnectionSecured(String connection_secured){

        SOCKETS_CONNECT_RESPONSE = connection_secured;

        if(IS_SOCKETS_CONNECTED){

            if(connection_secured.equals("SOCKETS_CONNECTED_OK")){
                connection_secured_text.setVisibility(View.VISIBLE);
                connection_secured_text.setText(R.string.word_secured_first_cap_small);
                connection_secured_text.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.success)));
            }
            else
            {
                connection_secured_text.setVisibility(View.VISIBLE);
                connection_secured_text.setText(R.string.word_unsecured_first_cap_small);
                connection_secured_text.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.warning)));
            }
        }
        else
        {
            connection_secured_text.setVisibility(View.GONE);
        }
    }


}
