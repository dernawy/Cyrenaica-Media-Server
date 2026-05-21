package com.cyrenaica.cyrenaicaserver.database.databaseManager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;
import android.widget.ListAdapter;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import com.cyrenaica.cyrenaicaserver.language.LocalHelper;
import com.cyrenaica.cyrenaicaserver.server.adaptars.AllServersListAdapter;
import com.cyrenaica.cyrenaicaserver.server.adaptars.AllServersListData;
import com.cyrenaica.cyrenaicaserver.server.adaptars.ServerAttachedDevicesListAdapter;
import com.cyrenaica.cyrenaicaserver.server.adaptars.ServerAttachedDevicesListData;
import com.scottyab.aescrypt.AESCrypt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerSQLHelper extends SQLiteOpenHelper {

    public static ServerSQLHelper dbHelper;
    public static Encryption encrp;

    static String TAG = "ServerSQLHelper";

    private static AllServersListData servList;
    private static ServerAttachedDevicesListData attachedDevicesList;

    final String[] interface_device_to_delete_array = new String[20];

    // database version
    static final int DB_VERSION = 1;

    // Database Information
    static final String DB_NAME = "CYRENAICA_CENTER.DB";

    public String DB_PASSWORD = "";
    protected String DB_ENCRYPTED_PASSWORD = "";
    public String DB_PASSWORD_CIPHER = "";


    // Server Table Name
    public static final String SERVER_TABLE_NAME           = "SERVER";
    public static final String SERVER_SETTINGS_TABLE_NAME  ="SERVER_SETTINGS";

    // Nodes Table Name
    public static final String MASTER_TABLE_NAME           = "sqlite_master";
    public static final String KYE_TABLE_NAME              = "sqlite_sequence";

    public static final String CAMERAS_TABLE_NAME              = "CAMERAS";
    public static final String CAMERAS_SETTINGS_TABLE_NAME     = "CAMERAS_SETTINGS";

    public static final String CAMERAS_FACE_DETECTION_TABLE_NAME = "CAMERAS_FACE_DETECTION";

    public static final String CONTROLLERS_TABLE_NAME          = "CONTROLLERS";
    public static final String CONTROLLERS_SETTINGS_TABLE_NAME = "CONTROLLERS_SETTINGS";
    public static final String DETECTORS_TABLE_NAME            = "DETECTORS";
    public static final String DETECTORS_SETTINGS_TABLE_NAME   = "DETECTORS_SETTINGS";
    public static final String LIGHTS_TABLE_NAME               = "LIGHTS";
    public static final String LIGHTS_SETTINGS_TABLE_NAME      = "LIGHTS_SETTINGS";

    public String TABLES_ARRAY[] = {"Select", CAMERAS_TABLE_NAME, CAMERAS_SETTINGS_TABLE_NAME, CONTROLLERS_TABLE_NAME, CONTROLLERS_SETTINGS_TABLE_NAME, DETECTORS_TABLE_NAME, DETECTORS_SETTINGS_TABLE_NAME};

    /** SERVER_ID - ID = 0 */
    public static final String SERVER_ID                   = "id";
    /** SERVER_SETUP_OK - ID = 1 */
    public static final String SERVER_SETUP_OK             = "server_setup_ok";
    /** SERVER_DEVICE_ID - ID = 2 */
    public static final String SERVER_DEVICE_ID            = "server_id";
    /** SERVER_NAME - ID = 3 */
    public static final String SERVER_NAME                 = "name";
    /** SERVER_MODEL - ID = 4 */
    public static final String SERVER_MODEL                = "model";
    /** SERVER_SSID - ID = 5 */
    public static final String SERVER_SSID                 = "ssid";
    /** SERVER_PASS - ID = 6 */
    public static final String SERVER_PASS                 = "password";
    /** SERVER_USE_STA_STATIC_IP - ID = 7 */
    public static final String SERVER_USE_STA_STATIC_IP    = "use_sta_static_ip";
    /** SERVER_STA_STATIC_IP - ID = 8 */
    public static final String SERVER_STA_STATIC_IP        = "sta_static_ip"; // The STA IP when connected to Router when use_sta_static_ip is true
    /** SERVER_LOCAL_IP - ID = 9 */
    public static final String SERVER_LOCAL_IP             = "local_ip"; // The STA IP when connected to Router // Static 192.168.1.100
    /** SERVER_GATEWAY_IP - ID = 10 */
    public static final String SERVER_GATEWAY_IP           = "gateway_ip";
    /** SERVER_NETMASK_IP - ID = 11 */
    public static final String SERVER_NETMASK_IP           = "netmask_ip";
    /** SERVER_DNS_IP - ID = 12 */
    public static final String SERVER_DNS_IP               = "dns_ip";
    /** SERVER_REMOTE_ACCESS - ID = 13 */
    public static final String SERVER_REMOTE_ACCESS        = "remote_access";
    /** SERVER_REMOTE_IP - ID = 14 */
    public static final String SERVER_REMOTE_IP            = "remote_ip"; // The ROUTER public IP
    /** SERVER_REMOTE_DOMAIN - ID = 15 */
    public static final String SERVER_REMOTE_DOMAIN        = "remote_domain";

    /** SERVER_STA_HOST - ID = 16 */
    public static final String SERVER_STA_HOST             = "sta_host";
    /** SERVER_STA_MAC - ID = 17 */
    public static final String SERVER_STA_MAC              = "sta_mac";
    /** SERVER_AP_MAC - ID = 18 */
    public static final String SERVER_AP_MAC               = "ap_mac";
    /** SERVER_USE_AP_STATIC_IP - ID = 19 */
    public static final String SERVER_USE_AP_STATIC_IP     = "use_ap_static_ip";
    /** SERVER_AP_STATIC_IP - ID = 20 */
    public static final String SERVER_AP_STATIC_IP         = "ap_static_ip"; // The STA IP when connected to Router when use_sta_static_ip is true
    /** SERVER_AP_IP - ID = 21 */
    public static final String SERVER_AP_IP                = "ap_ip";
    /** SERVER_AP_GATEWAY_IP - ID = 22 */
    public static final String SERVER_AP_GATEWAY_IP        = "ap_gateway_ip";
    /** SERVER_AP_NETMASK_IP - ID = 23 */
    public static final String SERVER_AP_NETMASK_IP        = "ap_netmask_ip";
    /** SERVER_AP_HOST - ID = 24 */
    public static final String SERVER_AP_HOST              = "ap_host";
    /** SERVER_AP_MAX_CLIENTS - ID = 25 */
    public static final String SERVER_AP_MAX_CLIENTS       = "ap_max_clients";
    /** SERVER_AP_CHANNEL - ID = 26 */
    public static final String SERVER_AP_CHANNEL           = "ap_channel";
    /** SERVER_SSID_HIDDEN - ID = 27 */
    public static final String SERVER_SSID_HIDDEN          = "ap_ssid_hidden";
    /** SERVER_CPU_FREQ - ID = 28 */
    public static final String SERVER_CPU_FREQ             = "freq";
    /** SERVER_HEAP - ID = 29 */
    public static final String SERVER_HEAP                 = "heap";
    /** SERVER_SPIFFS - ID = 30 */
    public static final String SERVER_SPIFFS               = "spiffs";
    /** ROUTER_SETUP - ID = 31 */
    public static final String ROUTER_SETUP                = "router_setup";
    /** ROUTER_SSID - ID = 32 */
    public static final String ROUTER_SSID                 = "router_ssid";
    /** ROUTER_PASS - ID = 33 */
    public static final String ROUTER_PASS                 = "router_pass";
    /** SERVER_NODES_COUNT - ID = 34 */
    public static final String SERVER_NODES_COUNT          = "nodes_count";


    // Creating server table query
    private static final String CREATE_SERVER_TABLE = "CREATE TABLE IF NOT EXISTS " + SERVER_TABLE_NAME + "(" + // 0, 1, 2, 7, 13, 19, 31, 34
    /* 0 */    SERVER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
    /* 1 */    SERVER_SETUP_OK +  " INTEGER NOT NULL, " +
    /* 2 */    SERVER_DEVICE_ID + " INTEGER NOT NULL, " +
    /* 3 */    SERVER_NAME + " TEXT NOT NULL, " +
    /* 4 */    SERVER_MODEL + " TEXT NOT NULL, " +
    /* 5 */    SERVER_SSID + " TEXT NOT NULL, " +
    /* 6 */    SERVER_PASS + " TEXT NOT NULL, " +
    /* 7 */    SERVER_USE_STA_STATIC_IP + " INTEGER NOT NULL, " +
    /* 8 */    SERVER_STA_STATIC_IP + " TEXT NOT NULL, " +
    /* 9 */    SERVER_LOCAL_IP + " TEXT NOT NULL, " +
    /* 10 */   SERVER_GATEWAY_IP + " TEXT NOT NULL, " +
    /* 11 */   SERVER_NETMASK_IP + " TEXT NOT NULL, " +
    /* 12 */   SERVER_DNS_IP + " TEXT NOT NULL, " +
    /* 13 */   SERVER_REMOTE_ACCESS + " INTEGER NOT NULL, " +
    /* 14 */   SERVER_REMOTE_IP + " TEXT NOT NULL, " +
    /* 15 */   SERVER_REMOTE_DOMAIN + " TEXT NOT NULL, " +
    /* 16 */   SERVER_STA_HOST + " TEXT NOT NULL, " +
    /* 17 */   SERVER_STA_MAC + " TEXT NOT NULL, " +
    /* 18 */   SERVER_AP_MAC + " TEXT NOT NULL, " +
    /* 19 */   SERVER_USE_AP_STATIC_IP + " INTEGER NOT NULL, " +
    /* 20 */   SERVER_AP_STATIC_IP + " TEXT NOT NULL, " +
    /* 21 */   SERVER_AP_IP + " TEXT NOT NULL, " +
    /* 22 */   SERVER_AP_GATEWAY_IP  + " TEXT NOT NULL, " +
    /* 23 */   SERVER_AP_NETMASK_IP  + " TEXT NOT NULL, " +
    /* 24 */   SERVER_AP_HOST + " TEXT NOT NULL, " +
    /* 25 */   SERVER_AP_MAX_CLIENTS + " INTEGER NOT NULL, " +
    /* 26 */   SERVER_AP_CHANNEL + " INTEGER NOT NULL, " +
    /* 27 */   SERVER_SSID_HIDDEN + " INTEGER NOT NULL, " +
    /* 28 */   SERVER_CPU_FREQ + " TEXT NOT NULL, " +
    /* 29 */   SERVER_HEAP + " TEXT NOT NULL, " +
    /* 30 */   SERVER_SPIFFS + " TEXT NOT NULL, " +
    /* 31 */   ROUTER_SETUP + " INTEGER NOT NULL, " +
    /* 32 */   ROUTER_SSID + " TEXT NOT NULL, " +
    /* 33 */   ROUTER_PASS + " TEXT NOT NULL, " +
    /* 34 */   SERVER_NODES_COUNT + " INTEGER NOT NULL);";

    // Server Settings Table columns
    public static final String SER_S_ID                = "serv_settings_id";
    public static final String SERV_ID                 = "serv_s_id";
    public static final String SERVER_S_ID             = "server_s_id";
    public static final String SERVER_PORT             = "server_port";
    public static final String SERVER_SOCKETS_DIR      = "server_sockets_dir";
    public static final String SERVER_TEMP_VALUE       = "server_temp_value";
    public static final String SERVER_PRESS_VALUE      = "server_press_value";
    public static final String SERVER_MOVEMENT_VALUE   = "server_movement_value";
    public static final String SERVER_GYRO_X_VALUE     = "server_gyro_x_value";
    public static final String SERVER_GYRO_Y_VALUE     = "server_gyro_y_value";
    public static final String SERVER_GYRO_Z_VALUE     = "server_gyro_z_value";

    // Creating lights settings table query
    private static final String CREATE_SERVER_SETTINGS_TABLE = "CREATE TABLE IF NOT EXISTS " + SERVER_SETTINGS_TABLE_NAME + "(" +
        SER_S_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        SERV_ID + " INTEGER NOT NULL, " +
        SERVER_S_ID + " INTEGER NOT NULL, " +
        SERVER_TEMP_VALUE  + " INTEGER NOT NULL, " +
        SERVER_PORT  + " TEXT NOT NULL, " +
        SERVER_SOCKETS_DIR  + " TEXT NOT NULL, " +
        SERVER_PRESS_VALUE + " INTEGER NOT NULL, " +
        SERVER_MOVEMENT_VALUE + " INTEGER NOT NULL, " +
        SERVER_GYRO_X_VALUE + " INTEGER NOT NULL, " +
        SERVER_GYRO_Y_VALUE + " INTEGER NOT NULL, " +
        SERVER_GYRO_Z_VALUE + " INTEGER NOT NULL, " +
        "CONSTRAINT server_id_link_fk FOREIGN KEY ('" + SER_S_ID + "') REFERENCES '" + SERVER_TABLE_NAME + "'('"+ SERVER_ID + "') ON DELETE CASCADE ON UPDATE CASCADE);";

    /***************************************************************************************************/

    // Cameras Table columns
    public static final String CAMERA_ID                   = "camera_id";
    public static final String CAM_ID                      = "cam_id";
    public static final String CAM_SERVER_ID               = "cam_server_id";
    public static final String CAMERA_SETUP_OK             = "cam_setup_ok";
    public static final String CAMERA_DEVICE_TYPE          = "cam_device_type";
    public static final String CAMERA_NAME                 = "cam_name";
    public static final String CAMERA_MODEL                = "cam_model";
    public static final String CAMERA_AP_SSID              = "cam_ap_ssid";
    public static final String CAMERA_AP_PASS              = "cam_ap_pass";
    public static final String CAMERA_AP_IP                = "cam_ap_ip"; // This is the IP of AP when created and started on device // if CAMERA_STATIC_IP_ON in camera settings table is true and device in AP mode this = AP STATIC IP from device
    public static final String CAMERA_AP_MAC               = "cam_ap_mac";
    public static final String CAMERA_AP_HOSTNAME          = "cam_ap_hostname";
    public static final String CAMERA_AP_MAX_CLIENTS       = "cam_ap_max_clients";
    public static final String CAMERA_AP_SSID_HIDDEN       = "cam_ap_ssid_hidden";
    public static final String CAMERA_AP_CHANNEL           = "cam_ap_channel";
    public static final String CAMERA_SERVER_SSID          = "cam_server_ssid";
    public static final String CAMERA_SERVER_PASS          = "cam_server_pass";
    public static final String CAMERA_SERVER_IP            = "cam_server_ip"; // This is th IP of CYRENAICA SERVER
    public static final String CAMERA_NET_IP               = "cam_net_ip"; // This is the IP of STA when connect to server or to router, // if CAMERA_STATIC_IP_ON in camera settings table is true and device in STA mode this  = STA STATIC IP from device
    public static final String CAMERA_STA_MAC              = "cam_sta_mac";
    public static final String CAMERA_FREQ                 = "cam_freq";
    public static final String CAMERA_HEAP                 = "cam_heap";
    public static final String CAMERA_SPIFFS               = "cam_spiffs";


    // Creating cameras table query
    private static final String CREATE_CAMERAS_TABLE = "CREATE TABLE IF NOT EXISTS " + CAMERAS_TABLE_NAME + "(" +
       /* 0 */    CAM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
       /* 1 */    CAMERA_ID + " INTEGER NOT NULL, " +
       /* 2 */    CAM_SERVER_ID + " INTEGER NOT NULL, " +
       /* 3 */    CAMERA_SETUP_OK + " INTEGER NOT NULL, " +
       /* 4 */    CAMERA_DEVICE_TYPE + " TEXT NOT NULL, " +
       /* 5 */    CAMERA_NAME + " TEXT NOT NULL, " +
       /* 6 */    CAMERA_MODEL + " TEXT NOT NULL, " +
       /* 7 */    CAMERA_AP_SSID + " TEXT NOT NULL, " +
       /* 8 */    CAMERA_AP_PASS + " TEXT NOT NULL, " +
       /* 9 */    CAMERA_AP_IP + " TEXT NOT NULL, " +
       /* 10 */   CAMERA_AP_MAC + " TEXT NOT NULL, " +
       /* 11 */   CAMERA_AP_HOSTNAME + " TEXT NOT NULL, " +
       /* 12 */   CAMERA_AP_MAX_CLIENTS + " INTEGER NOT NULL, " +
       /* 13 */   CAMERA_AP_SSID_HIDDEN + " INTEGER NOT NULL, " +
       /* 14 */   CAMERA_AP_CHANNEL + " INTEGER NOT NULL, " +
       /* 15 */   CAMERA_SERVER_SSID + " TEXT NOT NULL, " +
       /* 16 */   CAMERA_SERVER_PASS + " TEXT NOT NULL, " +
       /* 17 */   CAMERA_SERVER_IP + " TEXT NOT NULL, " +
       /* 18 */   CAMERA_NET_IP + " TEXT NOT NULL, " +
       /* 19 */   CAMERA_STA_MAC + " TEXT NOT NULL, " +
       /* 20 */   CAMERA_FREQ + " TEXT NOT NULL, " +
       /* 21 */   CAMERA_HEAP + " TEXT NOT NULL, " +
       /* 22 */   CAMERA_SPIFFS + " TEXT NOT NULL, " +
       "CONSTRAINT camera_id_link_fk FOREIGN KEY ('" + CAM_SERVER_ID + "') REFERENCES '" + SERVER_TABLE_NAME + "'('"+ SERVER_ID + "') ON DELETE CASCADE ON UPDATE CASCADE);";

    // Cameras Settings Table columns
    public static final String CAM_S_ID                    = "cam_settings_id";          // CAM SETTING ID (KEY)
    public static final String CAMS_ID                     = "cams_id";                  // CAM ID FROM CAMERA TABLE (KEY)
    public static final String CAMERA_S_ID                 = "camera_s_id";              // CAM ID FROM DEVICE
    public static final String CAM_NAME                    = "camera_name";              // CAM NAME from CAMERA table
    public static final String CAMERA_START_CAM            = "camera_start_cam";
    public static final String CAMERA_ESPNOW_CONTROL       = "camera_espnow_control";
    public static final String CAMERA_HTTP_CONTROL         = "camera_http_control";
    public static final String CAMERA_SOCKETS_CONTROL      = "camera_sockets_control";
    public static final String CAMERA_ACTIVATE_WIFISCAN    = "camera_activate_wifiscan";
    public static final String CAMERA_START_CAM_WEB        = "camera_start_cam_web";
    public static final String CAMERA_SEND_TO_SERVER       = "camera_send_to_server";
    public static final String CAMERA_SEND_TO_APP          = "camera_send_to_app";
    public static final String CAMERA_SERVICES_ON_STARTUP  = "camera_service_on_startup";
    public static final String CAMERA_REMOTE_ACCESS        = "camera_remote_access";
    public static final String CAMERA_WEB_USERNAME         = "camera_web_user";
    public static final String CAMERA_WEB_PASSWORD         = "camera_web_pass";
    public static final String CAMERA_ROUTER_SSID          = "camera_router_ssid";
    public static final String CAMERA_ROUTER_PASS          = "camera_router_pass";
    public static final String CAMERA_FACE_DETECT          = "camera_f_detect";
    public static final String CAMERA_FACE_RECOGNISE       = "camera_f_recognise";
    public static final String CAMERA_IMAGE_SIZE           = "camera_image_size";
    public static final String CAMERA_IMAGE_QUALITY        = "camera_image_quality";
    public static final String CAMERA_ON_OFF               = "camera_on_off";

    // Creating lights settings table query
    private static final String CREATE_CAMERAS_SETTINGS_TABLE = "CREATE TABLE IF NOT EXISTS " + CAMERAS_SETTINGS_TABLE_NAME + "(" +
        CAM_S_ID                   + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        CAMS_ID                    + " INTEGER NOT NULL, " +
        CAMERA_S_ID                + " INTEGER NOT NULL, " +
        CAM_NAME                   + " TEXT NOT NULL, " +
        CAMERA_START_CAM           + " INTEGER NOT NULL, " +
        CAMERA_ESPNOW_CONTROL      + " INTEGER NOT NULL, " +
        CAMERA_HTTP_CONTROL        + " INTEGER NOT NULL, " +
        CAMERA_SOCKETS_CONTROL     + " INTEGER NOT NULL, " +
        CAMERA_ACTIVATE_WIFISCAN   + " INTEGER NOT NULL, " +
        CAMERA_START_CAM_WEB       + " INTEGER NOT NULL, " +
        CAMERA_SEND_TO_SERVER      + " INTEGER NOT NULL, " +
        CAMERA_SEND_TO_APP         + " INTEGER NOT NULL, " +
        CAMERA_SERVICES_ON_STARTUP + " INTEGER NOT NULL, " +
        CAMERA_REMOTE_ACCESS       + " INTEGER NOT NULL, " +
        CAMERA_WEB_USERNAME        + " TEXT NOT NULL, " +
        CAMERA_WEB_PASSWORD        + " TEXT NOT NULL, " +
        CAMERA_ROUTER_SSID         + " TEXT NOT NULL, " +
        CAMERA_ROUTER_PASS         + " TEXT NOT NULL, " +
        CAMERA_FACE_DETECT         + " INTEGER NOT NULL, " +
        CAMERA_FACE_RECOGNISE      + " INTEGER NOT NULL, " +
        CAMERA_IMAGE_SIZE          + " TEXT NOT NULL, " +
        CAMERA_IMAGE_QUALITY       + " INTEGER NOT NULL, " +
        CAMERA_ON_OFF              + " INTEGER NOT NULL, " +
        "CONSTRAINT camera_setting_id_link_fk FOREIGN KEY ('" + CAMS_ID + "') REFERENCES '" + CAMERAS_TABLE_NAME + "'('"+ CAM_ID + "') ON DELETE CASCADE ON UPDATE CASCADE);";

    /***************************************************************************************************/

    public static final String F_DETECTION_ID                    = "id";                 // FACE DETECTION ID (KEY)
    public static final String F_DETECTION_CAM_ID                = "cam_id";             // CAM ID FROM CAMERA TABLE (KEY) [ EVERY ROW IN CAMERA TABLE CONTAIN THE SERVER ID AS (KEY)]
    public static final String F_DETECTION_P_ID                  = "person_id";          // DETECTED PERSON ID
    public static final String F_DETECTION_P_NAME                = "person_name";        // DETECTED PERSON NAME
    public static final String F_DETECTION_P_EMBEEDINGS          = "person_embeedings";  // DETECTED PERSON EMBEEDINGS (ARRAY[ARRAY])
    public static final String F_DETECTION_P_AGE                 = "person_age";         // DETECTED PERSON AGE
    public static final String F_DETECTION_P_ADDRESS             = "person_address";     // DETECTED PERSON ADDRESS
    public static final String F_DETECTION_P_EXTRA_TEXT          = "person_extra_text";  // DETECTED PERSON EXTRA (TEXT)

    private static final String CREATE_CAMERA_F_DETECTION_TABLE = "CREATE TABLE IF NOT EXISTS " + CAMERAS_FACE_DETECTION_TABLE_NAME + "(" +
            F_DETECTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            F_DETECTION_CAM_ID + " INTEGER NOT NULL, " +
            F_DETECTION_P_ID + " TEXT NOT NULL, " +
            F_DETECTION_P_NAME + " TEXT NOT NULL, " +
            F_DETECTION_P_EMBEEDINGS + " TEXT NOT NULL, " +
            F_DETECTION_P_ADDRESS + " TEXT NOT NULL, " +
            F_DETECTION_P_EXTRA_TEXT + " TEXT NOT NULL, " +
            "CONSTRAINT camera_f_detection_id_link_fk FOREIGN KEY ('" + F_DETECTION_CAM_ID + "') REFERENCES '" + CAMERAS_TABLE_NAME + "'('"+ CAM_ID + "') ON DELETE CASCADE ON UPDATE CASCADE);";



    /***************************************************************************************************/

    // Controllers Table columns
    public static final String CONTROLLER_ID               = "controller_id";
    public static final String C_ID                        = "c_id";
    public static final String C_SERVER_ID                 = "c_server_id";
    public static final String CONTROLLER_SETUP_OK         = "c_setup_ok";
    public static final String CONTROLLER_DEVICE_TYPE      = "c_device_type";
    public static final String CONTROLLER_NAME             = "c_name";
    public static final String CONTROLLER_MODEL            = "c_model";
    public static final String CONTROLLER_AP_SSID          = "c_ap_ssid";
    public static final String CONTROLLER_AP_PASS          = "c_ap_pass";
    public static final String CONTROLLER_AP_IP            = "c_ap_ip";
    public static final String CONTROLLER_AP_MAC           = "c_ap_mac";
    public static final String CONTROLLER_AP_HOSTNAME      = "c_ap_hostname";
    public static final String CONTROLLER_AP_MAX_CLIENTS   = "c_ap_max_clients";
    public static final String CONTROLLER_AP_SSID_HIDDEN   = "c_ap_ssid_hidden";
    public static final String CONTROLLER_AP_CHANNEL       = "c_ap_channel";
    public static final String CONTROLLER_SERVER_SSID      = "c_server_ssid";
    public static final String CONTROLLER_SERVER_PASS      = "c_server_pass";
    public static final String CONTROLLER_SERVER_IP        = "c_server_ip"; // This is th IP of CYRENAICA SERVER
    public static final String CONTROLLER_NET_IP           = "c_net_ip"; // This is the IP of STA when connect to server or to router, // if CONTROLLER_STATIC_IP_ON in controller settings table is true and device in STA mode this  = STA STATIC IP from device
    public static final String CONTROLLER_STA_MAC          = "c_sta_mac";
    public static final String CONTROLLER_FREQ             = "c_freq";
    public static final String CONTROLLER_HEAP             = "c_heap";
    public static final String CONTROLLER_SPIFFS           = "c_spiffs";


    // Creating controllers table query
    private static final String CREATE_CONTROLLERS_TABLE = "CREATE TABLE IF NOT EXISTS " + CONTROLLERS_TABLE_NAME + "(" +
        /* 0 */    C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        /* 1 */    CONTROLLER_ID + " INTEGER NOT NULL, " +
        /* 2 */    C_SERVER_ID + " INTEGER NOT NULL, " +
        /* 3 */    CONTROLLER_SETUP_OK + " INTEGER NOT NULL, " +
        /* 4 */    CONTROLLER_DEVICE_TYPE + " TEXT NOT NULL, " +
        /* 5 */    CONTROLLER_NAME + " TEXT NOT NULL, " +
        /* 6 */    CONTROLLER_MODEL + " TEXT NOT NULL, " +
        /* 7 */    CONTROLLER_AP_SSID + " TEXT NOT NULL, " +
        /* 8 */    CONTROLLER_AP_PASS + " TEXT NOT NULL, " +
        /* 9 */    CONTROLLER_AP_IP + " TEXT NOT NULL, " +
        /* 10 */   CONTROLLER_AP_MAC + " TEXT NOT NULL, " +
        /* 11 */   CONTROLLER_AP_HOSTNAME + " TEXT NOT NULL, " +
        /* 12 */   CONTROLLER_AP_MAX_CLIENTS + " INTEGER NOT NULL, " +
        /* 13 */   CONTROLLER_AP_SSID_HIDDEN + " INTEGER NOT NULL, " +
        /* 14 */   CONTROLLER_AP_CHANNEL + " INTEGER NOT NULL, " +
        /* 15 */   CONTROLLER_SERVER_SSID + " TEXT NOT NULL, " +
        /* 16 */   CONTROLLER_SERVER_PASS + " TEXT NOT NULL, " +
        /* 17 */   CONTROLLER_SERVER_IP + " TEXT NOT NULL, " +
        /* 18 */   CONTROLLER_NET_IP + " TEXT NOT NULL, " +
        /* 19 */   CONTROLLER_STA_MAC + " TEXT NOT NULL, " +
        /* 20 */   CONTROLLER_FREQ + " TEXT NOT NULL, " +
        /* 21 */   CONTROLLER_HEAP + " TEXT NOT NULL, " +
        /* 22 */   CONTROLLER_SPIFFS + " TEXT NOT NULL, " +
        "CONSTRAINT controller_id_link_fk FOREIGN KEY ('" + C_SERVER_ID + "') REFERENCES '" + SERVER_TABLE_NAME + "'('"+ SERVER_ID + "') ON DELETE CASCADE ON UPDATE CASCADE);";

    // Controllers Settings Table columns
    public static final String C_S_ID                   = "con_settings_id";
    public static final String CON_ID                   = "con_s_id";
    public static final String CONTROLLER_S_ID          = "controller_s_id";
    public static final String CONTROLLER_FIRST_VALUE   = "controller_first_value";
    public static final String CONTROLLER_SECOND_VALUE  = "controller_second_value";
    public static final String CONTROLLER_THIRD_VALUE   = "controller_third_value";
    public static final String CONTROLLER_FOURTH_VALUE  = "controller_fourth_value";
    public static final String CONTROLLER_FIFTH_VALUE   = "controller_fifth_value";
    public static final String CONTROLLER_ON_OFF        = "controller_on_off";

    // Creating controller settings table query
    private static final String CREATE_CONTROLLERS_SETTINGS_TABLE = "CREATE TABLE IF NOT EXISTS " + CONTROLLERS_SETTINGS_TABLE_NAME + "(" +
        C_S_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        CON_ID + " INTEGER NOT NULL, " +
        CONTROLLER_S_ID + " INTEGER NOT NULL, " +
        CONTROLLER_FIRST_VALUE  + " INTEGER NOT NULL, " +
        CONTROLLER_SECOND_VALUE + " INTEGER NOT NULL, " +
        CONTROLLER_THIRD_VALUE + " INTEGER NOT NULL, " +
        CONTROLLER_FOURTH_VALUE + " INTEGER NOT NULL, " +
        CONTROLLER_FIFTH_VALUE + " INTEGER NOT NULL, " +
        CONTROLLER_ON_OFF + " INTEGER NOT NULL, " +
        "CONSTRAINT controller_setting_id_link_fk FOREIGN KEY ('" + CON_ID + "') REFERENCES '" + CONTROLLERS_TABLE_NAME + "'('"+ C_ID + "') ON DELETE CASCADE ON UPDATE CASCADE);";

    /***************************************************************************************************/

    // Detectors Table columns
    public static final String DETECTOR_ID               = "detector_id";
    public static final String D_ID                      = "d_id";
    public static final String D_SERVER_ID               = "d_server_id";
    public static final String DETECTOR_SETUP_OK         = "d_setup_ok";
    public static final String DETECTOR_DEVICE_TYPE      = "d_device_type";
    public static final String DETECTOR_NAME             = "d_name";
    public static final String DETECTOR_MODEL            = "d_model";
    public static final String DETECTOR_AP_SSID          = "d_ap_ssid";
    public static final String DETECTOR_AP_PASS          = "d_ap_pass";
    public static final String DETECTOR_AP_IP            = "d_ap_ip";
    public static final String DETECTOR_AP_MAC           = "d_ap_mac";
    public static final String DETECTOR_AP_HOSTNAME      = "d_ap_hostname";
    public static final String DETECTOR_AP_MAX_CLIENTS   = "d_ap_max_clients";
    public static final String DETECTOR_AP_SSID_HIDDEN   = "d_ap_ssid_hidden";
    public static final String DETECTOR_AP_CHANNEL       = "d_ap_channel";
    public static final String DETECTOR_SERVER_SSID      = "d_server_ssid";
    public static final String DETECTOR_SERVER_PASS      = "d_server_pass";
    public static final String DETECTOR_SERVER_IP        = "d_server_ip"; // This is th IP of CYRENAICA SERVER
    public static final String DETECTOR_NET_IP           = "d_net_ip"; // This is the IP of STA when connect to server or to router, // if CONTROLLER_STATIC_IP_ON in controller settings table is true and device in STA mode this  = STA STATIC IP from device
    public static final String DETECTOR_STA_MAC          = "d_sta_mac";
    public static final String DETECTOR_FREQ             = "d_freq";
    public static final String DETECTOR_HEAP             = "d_heap";
    public static final String DETECTOR_SPIFFS           = "d_spiffs";

    // Creating detectors table query
    private static final String CREATE_DETECTORS_TABLE = "CREATE TABLE IF NOT EXISTS " + DETECTORS_TABLE_NAME + "(" +
        /* 0 */    D_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        /* 1 */    DETECTOR_ID + " INTEGER NOT NULL, " +
        /* 2 */    D_SERVER_ID + " INTEGER NOT NULL, " +
        /* 3 */    DETECTOR_SETUP_OK + " INTEGER NOT NULL, " +
        /* 4 */    DETECTOR_DEVICE_TYPE + " TEXT NOT NULL, " +
        /* 5 */    DETECTOR_NAME + " TEXT NOT NULL, " +
        /* 6 */    DETECTOR_MODEL + " TEXT NOT NULL, " +
        /* 7 */    DETECTOR_AP_SSID + " TEXT NOT NULL, " +
        /* 8 */    DETECTOR_AP_PASS + " TEXT NOT NULL, " +
        /* 9 */    DETECTOR_AP_IP + " TEXT NOT NULL, " +
        /* 10 */   DETECTOR_AP_MAC + " TEXT NOT NULL, " +
        /* 11 */   DETECTOR_AP_HOSTNAME + " TEXT NOT NULL, " +
        /* 12 */   DETECTOR_AP_MAX_CLIENTS + " INTEGER NOT NULL, " +
        /* 13 */   DETECTOR_AP_SSID_HIDDEN + " INTEGER NOT NULL, " +
        /* 14 */   DETECTOR_AP_CHANNEL + " INTEGER NOT NULL, " +
        /* 15 */   DETECTOR_SERVER_SSID + " TEXT NOT NULL, " +
        /* 16 */   DETECTOR_SERVER_PASS + " TEXT NOT NULL, " +
        /* 17 */   DETECTOR_SERVER_IP + " TEXT NOT NULL, " +
        /* 18 */   DETECTOR_NET_IP + " TEXT NOT NULL, " +
        /* 19 */   DETECTOR_STA_MAC + " TEXT NOT NULL, " +
        /* 20 */   DETECTOR_FREQ + " TEXT NOT NULL, " +
        /* 21 */   DETECTOR_HEAP + " TEXT NOT NULL, " +
        /* 22 */   DETECTOR_SPIFFS + " TEXT NOT NULL, " +
        "CONSTRAINT detector_id_link_fk FOREIGN KEY ('" + D_SERVER_ID + "') REFERENCES '" + SERVER_TABLE_NAME + "'('"+ SERVER_ID + "') ON DELETE CASCADE ON UPDATE CASCADE);";

    // Detectors Settings Table columns
    public static final String D_S_ID                    = "d_settings_id";
    public static final String DET_ID                    = "d_s_id";
    public static final String DETECTOR_S_ID             = "detector_s_id";
    public static final String DETECTOR_GAS_VALUE        = "detector_gas_value";
    public static final String DETECTOR_TEMP_VALUE       = "detector_temp_value";
    public static final String DETECTOR_PRESS_VALUE      = "detector_press_value";
    public static final String DETECTOR_MOVEMENT_VALUE   = "detector_movement_value";
    public static final String DETECTOR_GYRO_X_VALUE     = "detector_gyro_x_value";
    public static final String DETECTOR_GYRO_Y_VALUE     = "detector_gyro_y_value";
    public static final String DETECTOR_GYRO_Z_VALUE     = "detector_gyro_y_value";

    // Creating lights settings table query
    private static final String CREATE_DETECTORS_SETTINGS_TABLE = "CREATE TABLE IF NOT EXISTS " + DETECTORS_SETTINGS_TABLE_NAME + "(" +
        D_S_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        DET_ID + " INTEGER NOT NULL, " +
        DETECTOR_S_ID + " INTEGER NOT NULL, " +
        DETECTOR_GAS_VALUE  + " INTEGER NOT NULL, " +
        DETECTOR_TEMP_VALUE + " INTEGER NOT NULL, " +
        DETECTOR_PRESS_VALUE + " INTEGER NOT NULL, " +
        DETECTOR_MOVEMENT_VALUE + " INTEGER NOT NULL, " +
        DETECTOR_GYRO_X_VALUE + " INTEGER NOT NULL, " +
        DETECTOR_GYRO_Y_VALUE + " INTEGER NOT NULL, " +
        DETECTOR_GYRO_Z_VALUE + " INTEGER NOT NULL, " +
        "CONSTRAINT detector_setting_id_link_fk FOREIGN KEY ('" + DET_ID + "') REFERENCES '" + DETECTORS_TABLE_NAME + "'('"+ D_ID + "') ON DELETE CASCADE ON UPDATE CASCADE);";

    /***************************************************************************************************/

    static ServerSQLHelper instance;
    SQLiteDatabase sqDatabase;
    Context context;

    public ServerSQLHelper(Context c) {

        super(c, DB_NAME, null, DB_VERSION);
        context = c;

    }

    static public synchronized ServerSQLHelper getInstance(Context context){

        if(instance == null) {
            instance = new ServerSQLHelper(context);
        }

        Log.i(TAG, "getInstance()");

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        Log.i(TAG, "Database onCreate()");

        sqLiteDatabase.execSQL(CREATE_SERVER_TABLE);
    }

    @Override
    public void onOpen(SQLiteDatabase sqLiteDatabase) {
        super.onOpen(sqLiteDatabase);
        Log.i(TAG, "Database onOpen()");
        sqLiteDatabase.execSQL("PRAGMA foreign_keys = ON;");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        onCreate(sqLiteDatabase);
    }

    public static ServerSQLHelper DbHelper(Context context){

        SQLiteDatabase.loadLibs(context);

        return getInstance(context);
    }

    public static String getDbName(){
        return DB_NAME;
    }

    public int getTablesCount(){

        int TABLES_COUNT = 0;

        SQLiteDatabase database = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){

            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String tables_count_query = "SELECT count(*) FROM sqlite_master as tables WHERE TYPE='table'";

        Cursor cursor = database.rawQuery(tables_count_query, null);

        if (cursor.getCount() > 0) {

            TABLES_COUNT = cursor.getCount();

            System.out.println("Tables Count:  " + TABLES_COUNT);

            cursor.close();
            database.close();
        }
        else
        {
            cursor.close();
            database.close();
        }

        return TABLES_COUNT;
    }

    public void setPassword(String password){
        DB_PASSWORD = password;
    }

    public void setPasswordCipher(String text){
        DB_PASSWORD_CIPHER = text;
    }

    public String encryptDbPassword(){

        if(DB_PASSWORD.isEmpty()){
            return "PASSWORD_EMPTY";
        }

        String cipher = "Palestine Arabia";

        if(DB_PASSWORD_CIPHER.isEmpty()){
            DB_PASSWORD_CIPHER = cipher;
        }

        try { //@!Qmi4Grvx%
            DB_ENCRYPTED_PASSWORD = AESCrypt.encrypt(DB_PASSWORD, DB_PASSWORD_CIPHER);
            Log.i("DB ENCRYPTED PASSWORD ", DB_ENCRYPTED_PASSWORD);

            return DB_ENCRYPTED_PASSWORD;
        }
        catch (GeneralSecurityException e){
            //handle error
            System.out.println("encryptDbPassword() -> Error:  " + e.toString());
        }

        return "+";
    }

    public SQLiteDatabase database(){
        return instance.getWritableDatabase(encryptDbPassword());
    }

    public boolean dbExist() {
        return context.getDatabasePath(DB_NAME).exists();
    }

    public boolean tableExist(String table) {

        SQLiteDatabase database = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String table_exist_query = "SELECT name FROM " + MASTER_TABLE_NAME + " WHERE name='" + table + "'";

        Cursor cursor = database.rawQuery(table_exist_query, null);

        if (cursor.getCount() > 0) {

            cursor.moveToFirst();

            cursor.close();
            database.close();
            return true;
        }

        cursor.close();
        database.close();
        return false;
    }

    public boolean tableEmpty(String table) {

        SQLiteDatabase database = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String table_exist_query = "SELECT * FROM '" + table + "'";

        Cursor cursor = database.rawQuery(table_exist_query, null);

        if (cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            cursor.close();
            database.close();
            return false;
        }

        cursor.close();
        database.close();
        return true;
    }
    
    public int getServersCount(){

        SQLiteDatabase database = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String servers_count_query = "SELECT * FROM " + SERVER_TABLE_NAME + ";";

        Cursor cursor = database.rawQuery(servers_count_query, null);

        if (cursor.getCount() > 0) {
            return cursor.getCount();
        }

        return -1;
    }

    public boolean deviceExist(String table, String field_name, String name) {

        SQLiteDatabase database = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String device_exist_query = "SELECT " + field_name + " FROM " + table + " WHERE " + field_name + "='" + name + "';";

        Cursor cursor = database.rawQuery(device_exist_query, null);

        if (cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            cursor.close();
            database.close();
            return true;
        }

        cursor.close();
        database.close();
        return false;
    }

    public int getLastId(String table){

        SQLiteDatabase database = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String last_id_query = "SELECT * FROM '" + table + "';";

        Cursor cursor = database.rawQuery(last_id_query, null);

        int ID = cursor.getCount();

        System.out.println("Get last ID, there is/are :  [" + ID + "] row(s) in this table");

        if (ID > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            cursor.close();
            database.close();
            return ID;
        }
        
        cursor.close();
        database.close();
        return 0;
    }

    public int getCameraAttachId(String name){

        SQLiteDatabase database = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String device_id_query = "SELECT * FROM " + CAMERAS_TABLE_NAME + " WHERE cam_name='" + name + "';";
        Cursor cursor = database.rawQuery(device_id_query, null);

        int COUNT = cursor.getCount();

        if (COUNT > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            int ID = cursor.getInt(1);

            cursor.close();
            database.close();

            return ID;
        }

        cursor.close();
        database.close();

        return -1;
    }

    public int updateForeignKey(String table, String name){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + table + " WHERE name ='" + name + "'";
        Cursor cursor              = database.rawQuery( query, null);

        int ID;

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            System.out.println("ID:   "+ cursor.getString(0) + "| NAME: "+ cursor.getString(3) + "| MODEL: "+ cursor.getString(4));

            ID = cursor.getInt(0);

            cursor.close();
            database.close();

            return ID;
        }

        cursor.close();
        database.close();

        return -1;

    }

    /* Possible tables name: SERVER, SERVER_SETTINGS, CAMERAS, CAMERAS_SETTINGS, CAMERA_DETECTION, CONTROLLERS, CONTROLLERS_SETTINGS, DETECTORS, DETECTORS_SETTINGS */
    public void createTable(String table){

        Log.d(TAG, "CREATE TABLE: " + table);

        switch (table) {
            case "SERVER":
                database().execSQL(CREATE_SERVER_TABLE);
            break;
            case "SERVER_SETTINGS":
                database().execSQL(CREATE_SERVER_SETTINGS_TABLE);
                break;
            case "CAMERAS":
                database().execSQL(CREATE_CAMERAS_TABLE);
            break;
            case "CAMERAS_SETTINGS":
                database().execSQL(CREATE_CAMERAS_SETTINGS_TABLE);
                break;
            case "CAMERA_DETECTION":
                database().execSQL(CREATE_CAMERA_F_DETECTION_TABLE);
                break;
            case "CONTROLLERS":
                database().execSQL(CREATE_CONTROLLERS_TABLE);
            break;
            case "CONTROLLERS_SETTINGS":
                database().execSQL(CREATE_CONTROLLERS_SETTINGS_TABLE);
            break;
            case "DETECTORS":
                database().execSQL(CREATE_DETECTORS_TABLE);
            break;
            case "DETECTORS_SETTINGS":
                database().execSQL(CREATE_DETECTORS_SETTINGS_TABLE);
            break;

        }
    }

    /********************************************************************* SERVER ******************************************************************/

    public String ServerInsert(
        int id,
        int setup_ok,
        String name,
        String model,
        String ssid,
        String pass,
        int use_sta_static_ip,
        String sta_static_ip,
        String local_ip,
        String gateway_ip,
        String netmask_ip,
        String dns_ip,
        int remote_access,
        String remote_ip,
        String remote_domain,
        String sta_host,
        String sta_mac,
        String ap_mac,
        int use_ap_static_ip,
        String ap_static_ip,
        String ap_ip,
        String ap_gateway_ip,
        String ap_netmask_ip,
        String ap_host,
        int ap_max_clients,
        int ap_channel,
        int ap_ssid_hidden,
        String freq,
        String heap,
        String spiffs,
        int router_setup,
        String router_ssid,
        String router_pass
    ) {
        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(database.isOpen()){
            database.close();
            database    = instance.getWritableDatabase(encryptDbPassword());
        }
        else
        {
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        if(findServer(name, id)){
            return "DUPLICATED";
        }

        ContentValues contentValue = new ContentValues();

        contentValue.put(SERVER_DEVICE_ID, id);
        contentValue.put(SERVER_SETUP_OK, setup_ok);
        contentValue.put(SERVER_NAME, name);
        contentValue.put(SERVER_MODEL, model);
        contentValue.put(SERVER_SSID, ssid);
        contentValue.put(SERVER_PASS, pass);
        contentValue.put(SERVER_USE_STA_STATIC_IP, use_sta_static_ip);
        contentValue.put(SERVER_STA_STATIC_IP, sta_static_ip);
        contentValue.put(SERVER_LOCAL_IP, local_ip);
        contentValue.put(SERVER_GATEWAY_IP, gateway_ip);
        contentValue.put(SERVER_NETMASK_IP, netmask_ip);
        contentValue.put(SERVER_DNS_IP, dns_ip);
        contentValue.put(SERVER_REMOTE_ACCESS, remote_access);
        contentValue.put(SERVER_REMOTE_IP, remote_ip);
        contentValue.put(SERVER_REMOTE_DOMAIN, remote_domain);
        contentValue.put(SERVER_STA_HOST, sta_host);
        contentValue.put(SERVER_STA_MAC, sta_mac);
        contentValue.put(SERVER_AP_MAC, ap_mac);
        contentValue.put(SERVER_USE_AP_STATIC_IP, use_ap_static_ip);
        contentValue.put(SERVER_AP_STATIC_IP, ap_static_ip);
        contentValue.put(SERVER_AP_IP, ap_ip);
        contentValue.put(SERVER_AP_GATEWAY_IP, ap_gateway_ip);
        contentValue.put(SERVER_AP_NETMASK_IP, ap_netmask_ip);
        contentValue.put(SERVER_AP_HOST, ap_host);
        contentValue.put(SERVER_AP_MAX_CLIENTS, ap_max_clients);
        contentValue.put(SERVER_AP_CHANNEL, ap_channel);
        contentValue.put(SERVER_SSID_HIDDEN, ap_ssid_hidden);
        contentValue.put(SERVER_CPU_FREQ, freq);
        contentValue.put(SERVER_HEAP, heap);
        contentValue.put(SERVER_SPIFFS, spiffs);
        contentValue.put(ROUTER_SETUP, router_setup);
        contentValue.put(ROUTER_SSID, router_ssid);
        contentValue.put(ROUTER_PASS, router_pass);
        contentValue.put(SERVER_NODES_COUNT, 0);

        if(!database.isOpen()) {
            database    = instance.getWritableDatabase(encryptDbPassword());
            database.insert(SERVER_TABLE_NAME, null, contentValue);
            database.close();
        }
        else
        {
            Log.i(TAG, "Database OPENED");

        }

        return "INSERT_OK";
    }

    public int getServerId(String name){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME + " WHERE " + SERVER_NAME + "='" + name + "';";
        Cursor cursor              = database.rawQuery( query, null);

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            int server_id = cursor.getInt(0);
            Log.d(TAG, "getServerId() -> Search for server [" + name + "] id [ " + server_id + " ]");

            cursor.close();
            database.close();
            return server_id;
        }

        cursor.close();
        database.close();
        return -1;

    }

    public void ServerSettingsInsert(int id, String name, int temp, int press, int movement, int gyro_x, int gyro_y, int gyro_z) {

        int _id = updateForeignKey(SERVER_TABLE_NAME, name);

        ContentValues contentValue = new ContentValues();

        if(tableEmpty(SERVER_SETTINGS_TABLE_NAME)){
            contentValue.put(SER_S_ID, 0);
        }

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        contentValue.put(SERV_ID, _id);
        contentValue.put(SERVER_S_ID, id);
        contentValue.put(SERVER_TEMP_VALUE, temp);
        contentValue.put(SERVER_PRESS_VALUE, press);
        contentValue.put(SERVER_MOVEMENT_VALUE, movement);
        contentValue.put(SERVER_GYRO_X_VALUE, gyro_x);
        contentValue.put(SERVER_GYRO_Y_VALUE, gyro_y);
        contentValue.put(SERVER_GYRO_Z_VALUE, gyro_z);

        database.insert(SERVER_SETTINGS_TABLE_NAME, null, contentValue);
        database.close();

    }

    public String[] getAllServerForSpinner(){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME;
        Cursor cursor              = database.rawQuery( query, null);

        String[] names_array = new String[cursor.getCount() + 1];

        if(cursor.getCount() > 0) {

            if (LocalHelper.getLanguage(context).equalsIgnoreCase("ar")) {
                names_array[0] = "اختيار";
            }
            else
            {
                names_array[0] = "Select";
            }

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            int i = 1;

            do {

                String sever_name = cursor.getString(3); // Server name

                names_array[i] = sever_name;

                if(i < cursor.getCount()) {
                    i = i + 1;
                }
            }
            while(cursor.moveToNext());

            cursor.close();
            database().close();
        }

        return names_array;
    }

    public String getServerNameFromDeviceTable(String table, String device_name){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        String name_field = "";

        if(table.equals(CAMERAS_TABLE_NAME)){
            name_field = CAMERA_NAME;
        }

        if(table.equals(DETECTORS_TABLE_NAME)){
            name_field = DETECTOR_NAME;
        }

        if(table.equals(CONTROLLERS_TABLE_NAME)){
            name_field = CONTROLLER_NAME;
        }

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + table + " WHERE " + name_field + "='" + device_name + "';";
        Cursor cursor              = database.rawQuery( query, null);

        int device_server_id = 0;

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            device_server_id = cursor.getInt(2); // The server_id is on index (2) on all devices table

        }
        else
        {
            return "DEVICE_NOT_FOUND";
        }

        String server_query               = "SELECT * FROM " + SERVER_TABLE_NAME + " WHERE " + SERVER_ID + "='" + device_server_id + "';";
        Cursor server_cursor              = database.rawQuery( server_query, null);

        if(server_cursor.getCount() > 0) {

            if (!server_cursor.moveToFirst())
                server_cursor.moveToFirst();

            // Server name
            return server_cursor.getString(3);

        }

        return "SERVER_NOT_FOUND";

    }

    public boolean getServerSetupOk(){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME;
        Cursor cursor              = database.rawQuery( query, null);

        if(cursor.getCount() > 0) {

            int sever_setup = cursor.getInt(1);
            Log.d(TAG, "getServerSetupOk() -> Search for [ " + sever_setup + " ]");

            if(sever_setup == 1){
                cursor.close();
                database.close();

                return true;
            }
        }

        cursor.close();
        database.close();
        return false;
    }

    public String getServerName(){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME;
        Cursor cursor              = database.rawQuery( query, null);

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            String server_name = cursor.getString(3);
            Log.d(TAG, "getServerName() -> Search for [ " + server_name + " ]");

            cursor.close();
            database.close();
            return server_name;
        }

        cursor.close();
        database.close();
        return "ERROR";
    }

    public String getServerModel(){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME;
        Cursor cursor              = database.rawQuery( query, null);

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            String server_model = cursor.getString(4);
            Log.d(TAG, "getServerName() -> Search for [ " + server_model + " ]");

            cursor.close();
            database.close();
            return server_model;
        }

        cursor.close();
        database.close();
        return "ERROR";
    }

    public String getServerApIp(String server_name){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME + " WHERE " + SERVER_NAME + "='" + server_name + "';";
        Cursor cursor              = database.rawQuery( query, null);
        String ap_ip = "";

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            int use_static_ip = cursor.getInt(7);

            if(use_static_ip > 0){
                ap_ip = cursor.getString(20);
            }
            else
            {
                ap_ip = cursor.getString(21);
            }

            Log.d(TAG, "getServerApIp() -> Search server [" + server_name + "] for IP [" + ap_ip + " ]");

            cursor.close();
            database.close();
            return ap_ip;
        }

        cursor.close();
        database.close();
        return "ERROR";
    }

    public String getServerStaIp(){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME;
        Cursor cursor              = database.rawQuery( query, null);

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            String sta_ip = cursor.getString(9);
            Log.d(TAG, "getServerStaIp() -> Search for [ " + sta_ip + " ]");

            cursor.close();
            database.close();
            return sta_ip;
        }

        cursor.close();
        database.close();
        return "ERROR";
    }

    public String getServerSsid(String server_name){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME + " WHERE " + SERVER_NAME + "='" + server_name + "';";
        Cursor cursor              = database.rawQuery( query, null);

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            String server_ssid = cursor.getString(5);
            Log.d(TAG, "getServerSsid() -> Search [" + server_name + "] for [ " + server_ssid + " ]");

            cursor.close();
            database.close();
            return server_ssid;
        }

        cursor.close();
        database.close();
        return "ERROR";
    }

    public String getServerPass(String server_name){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME + " WHERE " + SERVER_NAME + "='" + server_name + "';";
        Cursor cursor              = database.rawQuery( query, null);

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            String server_pass = cursor.getString(6);
            Log.d(TAG, "getServerPass() -> Search [" + server_name + "] for [ " + server_pass + " ]");

            cursor.close();
            database.close();
            return server_pass;
        }

        cursor.close();
        database.close();
        return "ERROR";
    }

    public boolean getRouterSetupOk(){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME;
        Cursor cursor              = database.rawQuery( query, null);

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            int router_setup = cursor.getInt(31);
            Log.d(TAG, "getRouterSetupOk() -> Search for [ " + router_setup + " ]");

            if(router_setup == 1){
                cursor.close();
                database.close();

                return true;
            }
        }

        cursor.close();
        database.close();
        return false;
    }

    public String getRouterSSid(String server_name){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME + " WHERE " + SERVER_NAME + "='" + server_name + "';";
        Cursor cursor              = database.rawQuery( query, null);

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            String router_ssid = cursor.getString(32);
            Log.d(TAG, "getRouterSSid() -> Search for [ " + router_ssid + " ]");

            cursor.close();
            database.close();
            return router_ssid;
        }

        cursor.close();
        database.close();
        return "ERROR";
    }

    public String getRouterPassword(String server_name){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME + " WHERE " + SERVER_NAME + "='" + server_name + "';";
        Cursor cursor              = database.rawQuery( query, null);

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            String router_pass = cursor.getString(33);
            Log.d(TAG, "getRouterPassword() -> Search for [ " + router_pass + " ]");

            cursor.close();
            database.close();
            return router_pass;
        }

        cursor.close();
        database.close();
        return "ERROR";
    }

    public boolean getRemoteAccess(){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME;
        Cursor cursor              = database.rawQuery( query, null);

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            int remote_access = cursor.getInt(13);

            if(remote_access == 1) {
                cursor.close();
                database.close();
                Log.d(TAG, "getRemoteAccess() -> Search for [ " + remote_access + " ]");
                return true;
            }
        }

        cursor.close();
        database.close();
        return false;
    }

    public String getPublicDomain(){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME;
        Cursor cursor              = database.rawQuery( query, null);

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            String public_domain = cursor.getString(15);
            Log.d(TAG, "getPublicDomain() -> Search for [ " + public_domain + " ]");

            cursor.close();
            database.close();
            return public_domain;
        }

        cursor.close();
        database.close();
        return "ERROR";

    }

    public String getPublicIp(){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME;
        Cursor cursor              = database.rawQuery( query, null);

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            String public_ip = cursor.getString(14);
            Log.d(TAG, "getPublicIp() -> Search for [ " + public_ip + " ]");

            cursor.close();
            database.close();
            return public_ip;
        }

        cursor.close();
        database.close();
        return "ERROR";

    }

    public boolean getUseStaStaticIp(){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME;
        Cursor cursor              = database.rawQuery( query, null);

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            int sta_use_static_ip = cursor.getInt(7);

            if(sta_use_static_ip == 1){

                cursor.close();
                database.close();

                Log.d(TAG, "getUseStaStaticIp() -> Search for [ " + sta_use_static_ip + " ]");

                return true;
            }
        }

        cursor.close();
        database.close();
        return false;
    }

    public String getStaStaticIp(){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME;
        Cursor cursor              = database.rawQuery( query, null);

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            String sta_static_ip = cursor.getString(8);
            Log.d(TAG, "getStaStaticIp() -> Search for [ " + sta_static_ip + " ]");

            cursor.close();
            database.close();
            return sta_static_ip;
        }

        cursor.close();
        database.close();
        return "ERROR";

    }

    public boolean getUseApStaticIp(){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME;
        Cursor cursor              = database.rawQuery( query, null);

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            int ap_use_static_ip = cursor.getInt(19);

            if(ap_use_static_ip == 1){

                cursor.close();
                database.close();

                Log.d(TAG, "getUseApStaticIp() -> Search for [ " + ap_use_static_ip + " ]");

                return true;
            }
        }

        cursor.close();
        database.close();
        return false;
    }

    public String getApStaticIp(){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME;
        Cursor cursor              = database.rawQuery( query, null);

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            String ap_static_ip = cursor.getString(20);
            System.out.println("getApStaticIp() -> Search for [ " + ap_static_ip + " ]");

            cursor.close();
            database.close();
            return ap_static_ip;
        }

        cursor.close();
        database.close();
        return "ERROR";

    }

    public int getServerDevicesCount(String server_name){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query          = "SELECT * FROM " + SERVER_TABLE_NAME + " WHERE " + SERVER_NAME + "='" + server_name  +"';";

        Cursor cursor         = database.rawQuery( query, null);

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            int devices_count = cursor.getInt(34);
            Log.d(TAG, "getServerDevicesCount() -> Search for devices count in [" + server_name + "] table, we found [" + devices_count + "] device(s)");

            cursor.close();
            database.close();
            return devices_count;
        }

        cursor.close();
        database.close();
        return -1;
    }

    public boolean findServer(String name, int id){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME + " WHERE " + SERVER_NAME + "='"+ name +"' AND " + SERVER_ID + "='" + id + "';";
        Cursor cursor              = database.rawQuery( query, null);

        StringBuffer buffer = new StringBuffer();

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            buffer.append(cursor.getString(2));
            cursor.close();
            database.close();
            return true;
        }

        cursor.close();
        database.close();
        return false;
    }

    /* All server data in array, we must use database.close() after using this method because we don not close it inside this method */
    public String[] getServerData(String name, int id){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + SERVER_TABLE_NAME + " WHERE " + SERVER_NAME + "='"+ name +"' AND " + SERVER_ID + "='" + id + "';";
        Cursor cursor              = database.rawQuery( query, null);

        String[] server_data_array = new String[35];

        if(cursor.getCount() > 0) {

            Log.d(TAG, "SERVER ELEMENTS COUNT: " + cursor.getCount());

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            for(int i = 0; i < server_data_array.length; i++){// 0, 1, 2, 7, 13, 19, 31, 34 int

                if(i == 0 || i == 1 || i == 2 || i == 7 || i == 13 || i == 19 || i == 31 || i == 34){
                    server_data_array[i] = String.valueOf(cursor.getInt(i));
                }
                else
                {
                    server_data_array[i] = cursor.getString(i);
                }
            }
        }

        cursor.close();
        //database.close(); // We did not close database because we will call ServerAttachedDevicesData() method directly
        return server_data_array;
    }

    public ArrayList<AllServersListData> getAllServersForList(final ArrayList<AllServersListData> serversList, final AllServersListAdapter serversAdapter) {

        int SERVER_ID                = -1; // id = 0
        int SETUP_OK                 = -1; // id = 1
        String SERVER_NAME           = ""; // id = 3
        String SERVER_MODEL          = ""; // id = 4
        String SERVER_SSID           = ""; // id = 5
        String SERVER_PASS           = ""; // id = 6
        int SERVER_USE_STA_STATIC_IP = -1; // id = 7
        String SERVER_STA_STATIC_IP  = ""; // id = 8
        String SERVER_LOCAL_IP       = ""; // id = 9
        String SERVER_AP_STATIC_IP   = ""; // id = 20
        int SERVER_ROUTER_SETUP      = -1; // id = 31
        String SERVER_ROUTER_SSID    = ""; // id = 32
        String SERVER_ROUTER_PASS    = ""; // id = 33
        int SERVER_REMOTE_ACCESS     = -1; // id = 13
        String SERVER_REMOTE_IP      = ""; // id = 14
        String SERVER_REMOTE_DOMAIN  = ""; // id = 15
        String SERVER_SPIFFS         = ""; // id = 30
        String SERVER_HEAP           = ""; // id = 29
        String SERVER_FREQ           = ""; // id = 28
        int DEVICES_COUNT            = -1; // id = 34

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }


        String all_servers_query = "SELECT * FROM " + SERVER_TABLE_NAME;
        Cursor all_servers_cursors = database.rawQuery(all_servers_query, null);

        if(all_servers_cursors.getCount() > 0) {

            if (!all_servers_cursors.moveToFirst())
                all_servers_cursors.moveToFirst();

            do {
                SERVER_ID                = all_servers_cursors.getInt(0);
                SETUP_OK                 = all_servers_cursors.getInt(1);
                SERVER_NAME              = all_servers_cursors.getString(3);
                SERVER_MODEL             = all_servers_cursors.getString(4);
                SERVER_SSID              = all_servers_cursors.getString(5);
                SERVER_PASS              = all_servers_cursors.getString(6);
                SERVER_USE_STA_STATIC_IP = all_servers_cursors.getInt(7);
                SERVER_STA_STATIC_IP     = all_servers_cursors.getString(8);
                SERVER_LOCAL_IP          = all_servers_cursors.getString(9);
                SERVER_AP_STATIC_IP      = all_servers_cursors.getString(20);
                SERVER_ROUTER_SETUP      = all_servers_cursors.getInt(31);
                SERVER_ROUTER_SSID       = all_servers_cursors.getString(32);
                SERVER_ROUTER_PASS       = all_servers_cursors.getString(33);
                SERVER_REMOTE_ACCESS     = all_servers_cursors.getInt(13);
                SERVER_REMOTE_IP         = all_servers_cursors.getString(14);
                SERVER_REMOTE_DOMAIN     = all_servers_cursors.getString(15);
                SERVER_SPIFFS            = all_servers_cursors.getString(30);
                SERVER_HEAP              = all_servers_cursors.getString(29);
                SERVER_FREQ              = all_servers_cursors.getString(28);
                DEVICES_COUNT            = all_servers_cursors.getInt(34);

                servList = new AllServersListData();

                servList.setServerId(SERVER_ID);
                servList.setServerSetupOK(SETUP_OK);
                servList.setServerName(SERVER_NAME);
                servList.setServerModel(SERVER_MODEL);
                servList.setServerApSsid(SERVER_SSID);
                servList.setServerApPass(SERVER_PASS);
                servList.setServerStaUseStaticIp(SERVER_USE_STA_STATIC_IP);
                servList.setServerStaStaticIp(SERVER_STA_STATIC_IP);
                servList.setServerLocalIp(SERVER_LOCAL_IP);
                servList.setServerApStaticIp(SERVER_AP_STATIC_IP);
                servList.setServerRouterSetupOk(SERVER_ROUTER_SETUP);
                servList.setServerRouterSsid(SERVER_ROUTER_SSID);
                servList.setServerRouterPass(SERVER_ROUTER_PASS);
                servList.setServerRemoteAccess(SERVER_REMOTE_ACCESS);
                servList.setServerRemoteIp(SERVER_REMOTE_IP);
                servList.setServerRemoteDomain(SERVER_REMOTE_DOMAIN);
                servList.setServerSpiffs(SERVER_SPIFFS);
                servList.setServerHeap(SERVER_HEAP);
                servList.setServerFreq(SERVER_FREQ);
                servList.setServerDevicesCount(DEVICES_COUNT);

                serversList.add(servList);
                serversAdapter.notifyDataSetChanged();
            }

            while (all_servers_cursors.moveToNext());

            all_servers_cursors.close();
            database().close();

        }

        return serversList;

    }

    public JSONObject ServerAttachedDevicesData(int server_id){

        String _camera_device_type            = "EMPTY";
        String _controller_device_type        = "EMPTY";
        String _detector_device_type          = "EMPTY";

        String cameras_query                  = "EMPTY";
        String controllers_query              = "EMPTY";
        String detectors_query                = "EMPTY";

        JSONObject err_obj                    = null;
        JSONObject devices_assembly_obj       = null;
        JSONObject camera_devices_obj         = null;
        JSONObject controller_devices_obj     = null;
        JSONObject detector_devices_obj       = null;
        JSONObject final_object               = new JSONObject();
        JSONObject multi_devices_array_object = new JSONObject();

        JSONArray devicesJsonArray            = null;
        JSONArray camerasJsonArray            = null;
        JSONArray controllersJsonArray        = null;
        JSONArray detectorsJsonArray          = null;

        if(tableExist(CAMERAS_TABLE_NAME)){
            cameras_query          = "SELECT * FROM " + CAMERAS_TABLE_NAME     + " WHERE " + CAM_SERVER_ID + "='" + server_id + "';";
        }
        else
        {
            cameras_query = "NOT_EXIST";
        }

        if(tableExist(CONTROLLERS_TABLE_NAME)){
            controllers_query      = "SELECT * FROM " + CONTROLLERS_TABLE_NAME + " WHERE " + C_SERVER_ID   + "='" + server_id + "';";
        }
        else
        {
            controllers_query = "NOT_EXIST";
        }

        if(tableExist(DETECTORS_TABLE_NAME)){
            detectors_query        = "SELECT * FROM " + DETECTORS_TABLE_NAME   + " WHERE " + D_SERVER_ID   + "='" + server_id + "';";
        }
        else
        {
            detectors_query = "NOT_EXIST";
        }

        if(cameras_query.equals("NOT_EXIST") || controllers_query.equals("NOT_EXIST") || detectors_query.equals("NOT_EXIST")){

            if(cameras_query.equals("NOT_EXIST")){

                err_obj            = new JSONObject();
                camera_devices_obj = new JSONObject();

                try {
                    err_obj.put("0", "TABLE_NOT_EXIST");
                    camera_devices_obj.put("CAMERA", err_obj); // {"CAMERA": {"0": "TABLE_NOT_EXIST"}
                }
                catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if(controllers_query.equals("NOT_EXIST")){

                err_obj                = new JSONObject();
                controller_devices_obj = new JSONObject();

                try {
                    err_obj.put("0", "TABLE_NOT_EXIST");
                    controller_devices_obj.put("CONTROLLER", err_obj); // {"CONTROLLER": {"0": "TABLE_NOT_EXIST"}
                }
                catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if(detectors_query.equals("NOT_EXIST")){

                err_obj              = new JSONObject();
                detector_devices_obj = new JSONObject();

                try {
                    err_obj.put("0", "TABLE_NOT_EXIST");
                    detector_devices_obj.put("DETECTOR", err_obj);  // {"DETECTOR": {"0": "TABLE_NOT_EXIST"}
                }
                catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        if(!cameras_query.equals("NOT_EXIST")) {

            Cursor cameras_cursors       = database.rawQuery(cameras_query, null);

            if (cameras_cursors.getCount() > 0) {

                if (!cameras_cursors.moveToFirst())
                    cameras_cursors.moveToFirst();

                int device_count  = cameras_cursors.getCount();
                int device_cols   = cameras_cursors.getColumnCount();

                camera_devices_obj = new JSONObject();

                for (int a = 0; a < device_count; a++) {// devices count in Cursor

                    camerasJsonArray   = new JSONArray();

                    for (int i = 0; i < device_cols; i++) {// 0, 1, 2, 3, 12, 13 int

                        if (i == 0 || i == 1 || i == 2 || i == 3 || i == 12 || i == 13) {
                            camerasJsonArray.put(String.valueOf(cameras_cursors.getInt(i)));
                        }
                        else
                        {
                            camerasJsonArray.put(cameras_cursors.getString(i)); // [id, name, model, ap_ssid ... etc]
                        }
                    }

                    cameras_cursors.moveToNext();

                    try {
                        _camera_device_type = camerasJsonArray.getString(4);
                        multi_devices_array_object.put(String.valueOf(a), camerasJsonArray); // {"0": [id, name, model, ap_ssid ... etc], "1": [id, name, model, ap_ssid ... etc], "2": [id, name, model, ap_ssid ... etc] ... etc}
                    }
                    catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    camerasJsonArray = null;
                }

                try {
                    camera_devices_obj.put(_camera_device_type, multi_devices_array_object); // {"CAMERA": {"0": [id, name, model, ap_ssid ... etc], "1": [id, name, model, ap_ssid ... etc], "2": [id, name, model, ap_ssid ... etc] ... etc}}
                }
                catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            cameras_cursors.close();
        }

        if(!controllers_query.equals("NOT_EXIST")) {

            Cursor controllers_cursors   = database.rawQuery(controllers_query, null);

            if (controllers_cursors.getCount() > 0) {

                if (!controllers_cursors.moveToFirst())
                    controllers_cursors.moveToFirst();

                int device_count  = controllers_cursors.getCount();
                int device_cols   = controllers_cursors.getColumnCount();

                controller_devices_obj = new JSONObject();

                for (int a = 0; a < device_count; a++) {// devices count in Cursor

                    controllersJsonArray   = new JSONArray();

                    for (int i = 0; i < device_cols; i++) {// 0, 1, 2, 3, 12, 13 int

                        if (i == 0 || i == 1 || i == 2 || i == 3 || i == 12 || i == 13) {
                            controllersJsonArray.put(String.valueOf(controllers_cursors.getInt(i)));
                        }
                        else
                        {
                            controllersJsonArray.put(controllers_cursors.getString(i));
                        }

                    }

                    controllers_cursors.moveToNext();

                    try {

                        _controller_device_type = controllersJsonArray.getString(4);
                        multi_devices_array_object.put(String.valueOf(a), controllersJsonArray); // {"0": [id, name, model, ap_ssid ... etc], "1": [id, name, model, ap_ssid ... etc], "2": [id, name, model, ap_ssid ... etc] ... etc}

                    }
                    catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    controllersJsonArray = null;
                }

                try {
                    controller_devices_obj.put(_controller_device_type, multi_devices_array_object);  // {"CONTROLLER": {"0": [id, name, model, ap_ssid ... etc], "1": [id, name, model, ap_ssid ... etc], "2": [id, name, model, ap_ssid ... etc] ... etc}}
                }
                catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            controllers_cursors.close();
        }

        if(!detectors_query.equals("NOT_EXIST")) {

            Cursor detectors_cursors     = database.rawQuery(detectors_query, null);

            if (detectors_cursors.getCount() > 0) {

                if (!detectors_cursors.moveToFirst())
                    detectors_cursors.moveToFirst();

                int device_count = detectors_cursors.getCount();
                int device_cols   = detectors_cursors.getColumnCount();

                detector_devices_obj = new JSONObject();

                for (int a = 0; a < device_count; a++) {// devices count in Cursor

                    detectorsJsonArray   = new JSONArray();

                    for (int i = 0; i < device_cols; i++) {// 0, 1, 2, 3, 12, 13 int

                        if (i == 0 || i == 1 || i == 2 || i == 3 || i == 12 || i == 13) {
                            detectorsJsonArray.put(String.valueOf(detectors_cursors.getInt(i)));
                        }
                        else
                        {
                            detectorsJsonArray.put(detectors_cursors.getString(i));
                        }
                    }

                    detectors_cursors.moveToNext();

                    try {
                        _detector_device_type = detectorsJsonArray.getString(4);
                        multi_devices_array_object.put(String.valueOf(a), detectorsJsonArray); // {"0": [id, name, model, ap_ssid ... etc], "1": [id, name, model, ap_ssid ... etc], "2": [id, name, model, ap_ssid ... etc] ... etc}
                    }
                    catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    detectorsJsonArray = null;
                }

                try {
                    detector_devices_obj.put(_detector_device_type, multi_devices_array_object); // {"DETECTOR": {"0": [id, name, model, ap_ssid ... etc], "1": [id, name, model, ap_ssid ... etc], "2": [id, name, model, ap_ssid ... etc] ... etc}}
                }
                catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            detectors_cursors.close();
        }

        devices_assembly_obj = new JSONObject();

        try {
            devices_assembly_obj.accumulate("DEVICES", camera_devices_obj);    //{"DEVICES": {"CAMERA": {"0": [id, name, model, ap_ssid ... etc], "1": [id, name, model, ap_ssid ... etc], "2": [id, name, model, ap_ssid ... etc] ... etc}}} OR {"DEVICES": {"CAMERA": {"0": "TABLE_NOT_EXIST"}}
            devices_assembly_obj.accumulate("DEVICES", controller_devices_obj);//{"DEVICES": {"CONTROLLER": {"0": [id, name, model, ap_ssid ... etc], "1": [id, name, model, ap_ssid ... etc], "2": [id, name, model, ap_ssid ... etc] ... etc}}} OR {"DEVICES": {"CONTROLLER": {"0": "TABLE_NOT_EXIST"}}
            devices_assembly_obj.accumulate("DEVICES", detector_devices_obj);  //{"DEVICES": {"DETECTOR": {"0": [id, name, model, ap_ssid ... etc], "1": [id, name, model, ap_ssid ... etc], "2": [id, name, model, ap_ssid ... etc] ... etc}}} OR {"DEVICES": {"DETECTOR": {"0": "TABLE_NOT_EXIST"}}

            devicesJsonArray = devices_assembly_obj.getJSONArray("DEVICES");
            final_object.put("DEVICES", devicesJsonArray.getJSONObject(0));
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        database.close();
        return final_object;
    }

    public ArrayList<ServerAttachedDevicesListData> serversAttachedDevicesList(int server_id, final ArrayList<ServerAttachedDevicesListData> devicesList, final ServerAttachedDevicesListAdapter serversAdapter) {

        int DEVICE_ID         = -1; // id = 0
        String DEVICE_ATTACHE_ID = ""; // id = 1
        String DEVICE_TYPE    = ""; // id = 4
        String DEVICE_NAME    = ""; // id = 5
        String DEVICE_MODEL   = ""; // id = 6
        String DEVICE_AP_IP   = ""; // id = 9
        String DEVICE_AP_MAC  = ""; // id = 10
        String DEVICE_NET_IP  = ""; // id = 18
        String DEVICE_STA_MAC = ""; // id = 19

        String DEVICE_WIFI_CONN_STATUS   = ""; // id = without id (is not in database but it is in list, by default it is [DISCONNECTED]
        String DEVICE_ESPNOW_CONN_STATUS = ""; // id = without id (is not in database but it is in list, by default it is [DISCONNECTED]

        JSONObject all_devices_object = ServerAttachedDevicesData(server_id);

        JSONObject main_object    = null; // To get all devices in MAIN object
        JSONObject devices_object = null; // To get all devices in MAIN object
        JSONArray device_array    = null;

        try {
            main_object = new JSONObject();
            main_object = all_devices_object.getJSONObject("DEVICES");

            boolean camera_not_found     = main_object.isNull("CAMERA"); // false when there ara one camera or more / true if no cameras
            boolean controller_not_found = main_object.isNull("CONTROLLER");   // false when there ara one controller or more / true if no controllers
            boolean detector_not_found   = main_object.isNull("DETECTOR");     // false when there ara one detector or more / true if no detectors

            int main_length            = main_object.length();  // CAMERA, CONTROLLER, DETECTOR ... etc

            if (!camera_not_found) {

                devices_object = main_object.getJSONObject("CAMERA"); // For all devices type CAMERA in {"DEVICES":{"CAMERA"}}

                int devices_length = devices_object.length();  // "0", "1", "2" ... etc
                Log.d(TAG, "DEVICES LENGTH " + devices_length);

                int a = 0;

                do {

                    device_array = devices_object.getJSONArray(String.valueOf(a));

                    DEVICE_ID                 = Integer.parseInt(device_array.getString(0));
                    DEVICE_ATTACHE_ID         = device_array.getString(1);
                    DEVICE_TYPE               = device_array.getString(4);
                    DEVICE_NAME               = device_array.getString(5);
                    DEVICE_MODEL              = device_array.getString(6);
                    DEVICE_AP_IP              = device_array.getString(9);
                    DEVICE_AP_MAC             = device_array.getString(10);
                    DEVICE_NET_IP             = device_array.getString(18);
                    DEVICE_STA_MAC            = device_array.getString(19);
                    DEVICE_WIFI_CONN_STATUS   = "DISCONNECTED"; // without id (is not in database but it is in list, by default it is [DISCONNECTED]
                    DEVICE_ESPNOW_CONN_STATUS = "DISCONNECTED"; // without id (is not in database but it is in list, by default it is [DISCONNECTED]

                    Log.d(TAG, "DEVICE LIST - ID [" + DEVICE_ID + "] - ATTACH ID [" + DEVICE_ATTACHE_ID + "] - TYPE [" + DEVICE_TYPE + "] - NAME [" + DEVICE_NAME + "] - MODEL [" + DEVICE_MODEL + "] IP [" + DEVICE_AP_IP + "] - AP MAC [" + DEVICE_AP_MAC + "] - STA MAC [" + DEVICE_STA_MAC + "]");

                    attachedDevicesList = new ServerAttachedDevicesListData();

                    attachedDevicesList.setDeviceId(DEVICE_ID);
                    attachedDevicesList.setAttacheId(DEVICE_ATTACHE_ID);
                    attachedDevicesList.setDeviceType(DEVICE_TYPE);
                    attachedDevicesList.setDeviceName(DEVICE_NAME);
                    attachedDevicesList.setDeviceModel(DEVICE_MODEL);
                    attachedDevicesList.setDeviceApIp(DEVICE_AP_IP);
                    attachedDevicesList.setDeviceNetIp(DEVICE_NET_IP);
                    attachedDevicesList.setDeviceStaMac(DEVICE_STA_MAC);

                    attachedDevicesList.setDeviceWifiConnectionStatus(DEVICE_WIFI_CONN_STATUS);
                    attachedDevicesList.setDeviceEspnowConnectionStatus(DEVICE_ESPNOW_CONN_STATUS);

                    devicesList.add(attachedDevicesList);
                    serversAdapter.notifyDataSetChanged();

                    a++;
                    devices_length--;
                }while(devices_length > 0);

                /*for (int a = 0; a < devices_length; a++) {

                    device_array = devices_object.getJSONArray(String.valueOf(a));

                    DEVICE_ID     = Integer.parseInt(device_array.getString(0));
                    DEVICE_TYPE   = device_array.getString(4);
                    DEVICE_NAME   = device_array.getString(5);
                    DEVICE_MODEL  = device_array.getString(6);
                    DEVICE_AP_IP  = device_array.getString(9);
                    DEVICE_NET_IP = device_array.getString(18);

                    Log.d(TAG, "DEVICE LIST - ID [" + DEVICE_ID + "] - TYPE [" + DEVICE_TYPE + "] - NAME [" + DEVICE_NAME + "] - MODEL [" + DEVICE_MODEL + "] IP [" + DEVICE_AP_IP + "]");

                    attachedDevicesList = new ServerAttachedDevicesListData();

                    attachedDevicesList.setDeviceId(DEVICE_ID);
                    attachedDevicesList.setDeviceType(DEVICE_TYPE);
                    attachedDevicesList.setDeviceName(DEVICE_NAME);
                    attachedDevicesList.setDeviceModel(DEVICE_MODEL);
                    attachedDevicesList.setDeviceApIp(DEVICE_AP_IP);
                    attachedDevicesList.setDeviceNetIp(DEVICE_NET_IP);

                    devicesList.add(attachedDevicesList);
                    serversAdapter.notifyDataSetChanged();
                    return devicesList;
                }*/
            }
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }


        return devicesList;
    }

    /********************************************************************* CAMERA ******************************************************************/

    public String CameraInsert(
        int id,
        int server_id,
        int setup_ok,
        String type,
        String name,
        String model,
        String ap_ssid,
        String ap_pass,
        String ap_ip,
        String ap_mac,
        String ap_host,
        int ap_max_clients,
        int ap_ssid_hidden,
        int ap_channel,
        String server_ssid,
        String server_pass,
        String server_ip,
        String net_ip,
        String sta_mac,
        String freq,
        String heap,
        String spiffs
    ){
        System.out.println("INSERT DEVICE: " + setup_ok);
        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        ContentValues contentValue = new ContentValues();

        contentValue.put(CAMERA_ID, id); // DEVICE ID from device
        contentValue.put(CAM_SERVER_ID, server_id); // SERVER ID this device attached to
        contentValue.put(CAMERA_SETUP_OK, setup_ok);
        contentValue.put(CAMERA_DEVICE_TYPE, type);
        contentValue.put(CAMERA_NAME, name);
        contentValue.put(CAMERA_MODEL, model);
        contentValue.put(CAMERA_AP_SSID, ap_ssid);
        contentValue.put(CAMERA_AP_PASS, ap_pass);
        contentValue.put(CAMERA_AP_IP, ap_ip);
        contentValue.put(CAMERA_AP_MAC, ap_mac);
        contentValue.put(CAMERA_AP_HOSTNAME, ap_host);
        contentValue.put(CAMERA_AP_MAX_CLIENTS, ap_max_clients);
        contentValue.put(CAMERA_AP_SSID_HIDDEN, ap_ssid_hidden);
        contentValue.put(CAMERA_AP_CHANNEL, ap_channel);
        contentValue.put(CAMERA_SERVER_SSID, server_ssid);
        contentValue.put(CAMERA_SERVER_PASS, server_pass);
        contentValue.put(CAMERA_SERVER_IP, server_ip);
        contentValue.put(CAMERA_NET_IP, net_ip);
        contentValue.put(CAMERA_STA_MAC, sta_mac);
        contentValue.put(CAMERA_FREQ, freq);
        contentValue.put(CAMERA_HEAP, heap);
        contentValue.put(CAMERA_SPIFFS, spiffs);
        
        database.insert(CAMERAS_TABLE_NAME, null, contentValue);
        database.close();

        return "INSERT_OK";
    }

    public String CameraSettingsInsert(int id, int device_id, String name, int start_cam, int espnow, int http, int sockets, int wifiscan, int web, int to_server, int to_app, int s_startup, int remote, String w_user, String w_pass, String r_ssid, String r_pass, int face_d, int face_r, String img_size /* {480x320} */, int img_q, int cam_on){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        ContentValues contentValue = new ContentValues();

        contentValue.put(CAMS_ID, id);                           // CAMERA ID from camera table
        contentValue.put(CAMERA_S_ID, device_id);                // CAMERA DEVICE ID from camera table (device id)
        contentValue.put(CAM_NAME, name);                        // CAMERA NAME from camera table (CAMERA_NAME)
        contentValue.put(CAMERA_START_CAM, start_cam);           // START_CAM on device (true by default)
        contentValue.put(CAMERA_ESPNOW_CONTROL, espnow);         // ESPNOW CONTROL on device (false by default)
        contentValue.put(CAMERA_HTTP_CONTROL, http);             // HTTP CONTROL on device (false by default)
        contentValue.put(CAMERA_SOCKETS_CONTROL, sockets);       // SOCKETS CONTROL on device (true by default)
        contentValue.put(CAMERA_ACTIVATE_WIFISCAN, wifiscan);    // ACTIVATE WIFISCAN on device (true by default)
        contentValue.put(CAMERA_START_CAM_WEB, web);             // ACTIVATE access camera by web on device (true by default)
        contentValue.put(CAMERA_SEND_TO_SERVER, to_server);      // ACTIVATE SEND TO SERVER on device (false by default)
        contentValue.put(CAMERA_SEND_TO_APP, to_app);            // ACTIVATE SEND TO APPLICATION on device (true by default)
        contentValue.put(CAMERA_SERVICES_ON_STARTUP, s_startup); // ACTIVATE MDNS SERVICE ON STARTUP on device (true by default)
        contentValue.put(CAMERA_REMOTE_ACCESS, remote);          // ACTIVATE REMOTE ACCESS on device (false by default)
        contentValue.put(CAMERA_WEB_USERNAME, w_user);           // ADD WEB USERNAME to login to camera web if START_CAM_WEB ACTIVATED on device (cyrenaica by default)
        contentValue.put(CAMERA_WEB_PASSWORD, w_pass);           // ADD WEB PASSWORD to login to camera web if START_CAM_WEB ACTIVATED on device (cyrenaica by default)
        contentValue.put(CAMERA_ROUTER_SSID, r_ssid);            // ADD ROUTER USERNAME to login to camera web if REMOTE_ACCESS ACTIVATED on device
        contentValue.put(CAMERA_ROUTER_PASS, r_pass);            // ADD ROUTER PASSWORD to use camera if REMOTE_ACCESS ACTIVATED on device
        contentValue.put(CAMERA_FACE_DETECT, face_d);            // ACTIVATE FACE DETECTION on APPLICATION with the camera (false by default)
        contentValue.put(CAMERA_FACE_RECOGNISE, face_r);         // ACTIVATE FACE REORGANISATION on APPLICATION with the camera (false by default)
        contentValue.put(CAMERA_IMAGE_SIZE, img_size);           // SET IMAGE SIZE when use camera for face detection (480x320 by default)
        contentValue.put(CAMERA_IMAGE_QUALITY, img_q);           // SET IMAGE QUALITY when use camera for face detection
        contentValue.put(CAMERA_ON_OFF, cam_on);                 // SET ON_OFF on APPLICATION (on by default)

        database.insert(CAMERAS_SETTINGS_TABLE_NAME, null, contentValue);
        database.close();

        return "INSERT_OK";

    }

    public int getDeviceId(String device, String name){

        String table      = "";
        String name_field = "";

        SQLiteDatabase database = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        if(device.equals("CAMERA")){
            table      = CAMERAS_TABLE_NAME;
            name_field = CAMERA_NAME;
        }

        if(device.equals("CONTROLLER")){
            table      = CONTROLLERS_TABLE_NAME;
            name_field = CONTROLLER_NAME;
        }

        if(device.equals("DETECTOR")){
            table      = DETECTORS_TABLE_NAME;
            name_field = DETECTOR_NAME;
        }

        String device_id_query               = "SELECT * FROM " + table + " WHERE " + name_field + "='"+ name + "';";

        Log.d(TAG, "getDeviceId() -> device_id_query -> " + device_id_query);

        Cursor cursor              = database.rawQuery(device_id_query, null);

        if (cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            String device_name  = cursor.getString(5);
            int device_id       = cursor.getInt(0);

            Log.d(TAG, "getDeviceId() -> Device found -> [" + device_name + "] with ID [" + device_id + "]");

            cursor.close();
            database.close();

            return device_id;

        }

        cursor.close();
        database.close();
        return -1;

    }

    public int getCamerasCount(){

        SQLiteDatabase database = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String servers_count_query = "SELECT * FROM " + CAMERAS_TABLE_NAME + ";";

        Cursor cursor = database.rawQuery(servers_count_query, null);

        return cursor.getCount();
    }

    public String getDeviceName(String device, String name){

        String table      = "";
        String name_field = "";

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        if(device.equals("CAMERA")){
            table      = CAMERAS_TABLE_NAME;
            name_field = CAMERA_NAME;
        }

        if(device.equals("CONTROLLER")){
            table      = CONTROLLERS_TABLE_NAME;
            name_field = CONTROLLER_NAME;
        }

        if(device.equals("DETECTOR")){
            table      = DETECTORS_TABLE_NAME;
            name_field = DETECTOR_NAME;
        }

        String device_name_query               = "SELECT * FROM " + table + " WHERE " + name_field + "='"+ name + "';";
        Log.w(TAG, "getDeviceName() -> device_name_query -> " + device_name_query);
        Cursor cursor              = database.rawQuery( device_name_query, null);

        if (cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            String device_name = cursor.getString(5);

            Log.w(TAG, "getDeviceName() -> Device found -> " + device_name);

            cursor.close();
            database.close();

            return device_name;
        }

        cursor.close();
        database.close();
        return "NO_DEVICE_FOUND";
    }

    public String getDeviceApPassword(String device, String name){

        String table      = "";
        String name_field = "";

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        if(device.equals("CAMERA")){
            table      = CAMERAS_TABLE_NAME;
            name_field = CAMERA_NAME;
        }

        if(device.equals("CONTROLLER")){
            table      = CONTROLLERS_TABLE_NAME;
            name_field = CONTROLLER_NAME;
        }

        if(device.equals("DETECTOR")){
            table      = DETECTORS_TABLE_NAME;
            name_field = DETECTOR_NAME;
        }

        String device_ap_password_query               = "SELECT * FROM " + table + " WHERE " + name_field + "='"+ name + "';";

        Log.w(TAG, "getDeviceApPassword() -> device_ap_password_query -> " + device_ap_password_query);

        Cursor cursor              = database.rawQuery(device_ap_password_query, null);

        if (cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            String device_name        = cursor.getString(5);
            String device_ap_password = cursor.getString(8);

            Log.w(TAG, "Device found -> [" + device_name + "] with AP password [" + device_ap_password + "]");

            cursor.close();
            database.close();

            return device_ap_password;

        }

        cursor.close();
        database.close();
        return "NO_DEVICE_FOUND";
    }

    public String getDeviceApSsid(String device, String name){

        String table      = "";
        String name_field = "";

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        if(device.equals("CAMERA")){
            table      = CAMERAS_TABLE_NAME;
            name_field = CAMERA_NAME;
        }

        if(device.equals("CONTROLLER")){
            table      = CONTROLLERS_TABLE_NAME;
            name_field = CONTROLLER_NAME;
        }

        if(device.equals("DETECTOR")){
            table      = DETECTORS_TABLE_NAME;
            name_field = DETECTOR_NAME;
        }

        String device_ap_ip_ssid               = "SELECT * FROM " + table + " WHERE " + name_field + "='"+ name + "';";

        Log.d(TAG, "getDeviceApSsid() -> device_ap_ip_query -> " + device_ap_ip_ssid);

        Cursor cursor              = database.rawQuery(device_ap_ip_ssid, null);

        if (cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            String device_name  = cursor.getString(5);
            String device_ap_ssid = cursor.getString(7);

            Log.d(TAG, "getDeviceApSsid() -> Device found - NAME [" + device_name + "] with AP SSID [" + device_ap_ssid + "]");

            cursor.close();
            database.close();

            return device_ap_ssid;

        }

        cursor.close();
        database.close();
        return "NO_DEVICE_FOUND";
    }

    public String getDeviceApIp(String device, String name){

        String table      = "";
        String name_field = "";

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        if(device.equals("CAMERA")){
            table      = CAMERAS_TABLE_NAME;
            name_field = CAMERA_NAME;
        }

        if(device.equals("CONTROLLER")){
            table      = CONTROLLERS_TABLE_NAME;
            name_field = CONTROLLER_NAME;
        }

        if(device.equals("DETECTOR")){
            table      = DETECTORS_TABLE_NAME;
            name_field = DETECTOR_NAME;
        }

        String device_ap_ip_query               = "SELECT * FROM " + table + " WHERE " + name_field + "='"+ name + "';";

        Log.d(TAG, "getDeviceApIp() -> device_ap_ip_query -> " + device_ap_ip_query);

        Cursor cursor              = database.rawQuery(device_ap_ip_query, null);

        if (cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            String device_name  = cursor.getString(5);
            String device_ap_ip = cursor.getString(9);

            Log.d(TAG, "getDeviceApIp() -> Device found -> [" + device_name + "] with AP ip [" + device_ap_ip + "]");

            cursor.close();
            database.close();

            return device_ap_ip;

        }

        cursor.close();
        database.close();
        return "NO_DEVICE_FOUND";
    }

    public String getDeviceNetIp(String device, String name){

        String table      = "";
        String name_field = "";

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        if(device.equals("CAMERA")){
            table      = CAMERAS_TABLE_NAME;
            name_field = CAMERA_NAME;
        }

        if(device.equals("CONTROLLER")){
            table      = CONTROLLERS_TABLE_NAME;
            name_field = CONTROLLER_NAME;
        }

        if(device.equals("DETECTOR")){
            table      = DETECTORS_TABLE_NAME;
            name_field = DETECTOR_NAME;
        }

        String device_net_ip_query               = "SELECT * FROM " + table + " WHERE " + name_field + "='"+ name + "';";

        Log.d(TAG, "getDeviceNetIp() -> device_ap_ip_query -> " + device_net_ip_query);

        Cursor cursor              = database.rawQuery(device_net_ip_query, null);

        if (cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            String device_name  = cursor.getString(5);
            String device_net_ip = cursor.getString(18);

            Log.d(TAG, "getDeviceNetIp() -> Device found -> [" + device_name + "] with NET ip [" + device_net_ip + "]");

            cursor.close();
            database.close();

            return device_net_ip;

        }

        cursor.close();
        database.close();
        return "NO_DEVICE_FOUND";
    }

    public int getDeviceSetupOk(String device, String name){

        Log.d(TAG, "getDeviceApIp()");

        String table      = "";
        String name_field = "";

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        if(device.equals("CAMERA")){
            table      = CAMERAS_TABLE_NAME;
            name_field = CAMERA_NAME;
        }

        if(device.equals("CONTROLLER")){
            table      = CONTROLLERS_TABLE_NAME;
            name_field = CONTROLLER_NAME;
        }

        if(device.equals("DETECTOR")){
            table      = DETECTORS_TABLE_NAME;
            name_field = DETECTOR_NAME;
        }

        String device_setup_ok_query               = "SELECT * FROM " + table + " WHERE " + name_field + "='"+ name + "';";

        Log.d(TAG, "getDeviceSetupOk() -> device_ap_ip_query -> " + device_setup_ok_query);

        Cursor cursor              = database.rawQuery(device_setup_ok_query, null);

        if (cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            String device_name  = cursor.getString(5);
            int device_setup_ok = cursor.getInt(3);

            Log.d(TAG, "Device found -> [" + device_name + "] with AP ip [" + device_setup_ok + "]");

            cursor.close();
            database.close();

            return device_setup_ok;

        }

        cursor.close();
        database.close();
        return -1;
    }

    public String isDeviceDuplicated(String device, String name, String ssid){

        String table      = "";
        String name_field = "";
        String ssid_field = "";

        if(device.equals("CAMERA")){
            table      = CAMERAS_TABLE_NAME;
            name_field = CAMERA_NAME;
            ssid_field = CAMERA_AP_SSID;
        }

        if(device.equals("CONTROLLER")){
            table      = CONTROLLERS_TABLE_NAME;
            name_field = CONTROLLER_NAME;
            ssid_field = CONTROLLER_AP_SSID;
        }

        if(device.equals("DETECTOR")){
            table      = DETECTORS_TABLE_NAME;
            name_field = DETECTOR_NAME;
            ssid_field = DETECTOR_AP_SSID;
        }

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String duplicated_query = "";

        if(ssid.isEmpty()){
            Log.w(TAG, "isDeviceDuplicated() - Check if device ssid is empty -> [Yes]");
            duplicated_query               = "SELECT * FROM " + table + " WHERE " + name_field + "='" + name + "';";
        }
        else
        {
            Log.d(TAG, "isDeviceDuplicated() - Check if device ssid is empty -> [No]");
            duplicated_query               = "SELECT * FROM " + table + " WHERE " + name_field + "='" + name + "' AND " + ssid_field + "='" + ssid + "';";
        }

        Log.d(TAG, "isDeviceDuplicated() - duplicated_query -> " + duplicated_query);

        Cursor cursor              = database.rawQuery( duplicated_query, null);

        String device_ssid = "EMPTY";

        if (cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            /* Name id = 3 - Model id = 4 - SSID id = 6 */

            String device_name = cursor.getString(5);
            String device_model = cursor.getString(6);

            // If ssid is not empty
            if (!ssid.isEmpty()) {

                device_ssid = cursor.getString(7);

                if (device_name.equals(name) && device_ssid.equals(ssid)) {
                    cursor.close();
                    database.close();
                    return "DUPLICATED";
                }

                if (device_name.equals(name)) {
                    cursor.close();
                    database.close();
                    return "NAME_DUPLICATED";
                }

                if (device_ssid.equals(ssid)) {
                    cursor.close();
                    database.close();
                    return "SSID_DUPLICATED";
                }

            }
            else // If ssid is empty
            {
                if (device_name.equals(name)) {
                    cursor.close();
                    database.close();
                    return "NAME_DUPLICATED";
                }
            }

            Log.d(TAG, "isDeviceDuplicated() - Check if device [DUPLICATED] - NAME [" + device_name + "] - MODEL [" + device_model + "] - SSID [" + device_ssid + "]");
        }

        cursor.close();
        database.close();

        return "DEVICE_OK";
    }

    public String isSsidDuplicated(String device, String name, String ssid){

        String table      = "";
        String name_field = "";
        String ssid_field = "";

        if(device.equals("CAMERA")){
            table      = CAMERAS_TABLE_NAME;
            name_field = CAMERA_NAME;
            ssid_field = CAMERA_AP_SSID;
        }

        if(device.equals("CONTROLLER")){
            table      = CONTROLLERS_TABLE_NAME;
            name_field = CONTROLLER_NAME;
            ssid_field = CONTROLLER_AP_SSID;
        }

        if(device.equals("DETECTOR")){
            table      = DETECTORS_TABLE_NAME;
            name_field = DETECTOR_NAME;
            ssid_field = DETECTOR_AP_SSID;
        }

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String duplicated_query = "";

        duplicated_query               = "SELECT * FROM " + table + " WHERE " + name_field + "='" + name + "' AND " + ssid_field + "='" + ssid + "';";

        Log.w(TAG, "duplicated_query -> " + duplicated_query);

        Cursor cursor              = database.rawQuery( duplicated_query, null);

        if (cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            /* Name id = 3 - Model id = 4 - SSID id = 6 */

            String device_name  = cursor.getString(5);
            String device_model = cursor.getString(6);
            String device_ssid  = cursor.getString(7);

            if(device_ssid.equals(ssid)){

                if(device_name.equals(name)){
                    cursor.close();
                    database.close();
                    return "SSID_NAME_DUPLICATED";
                }

                cursor.close();
                database.close();
                return "SSID_DUPLICATED";
            }
        }

        cursor.close();
        database.close();

        return "DEVICE_OK";
    }

    public boolean findStringRecord(String column_name /* column name in db */, String table /* Name of table */, String value /* Value of column */){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String query               = "SELECT * FROM " + table + " WHERE " + column_name + "='"+ value + "';";
        Cursor cursor              = database.rawQuery( query, null);

        StringBuffer buffer = new StringBuffer();

        if(cursor.getCount() > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            buffer.append(cursor.getString(2));
            cursor.close();
            database.close();
            return true;
        }

        cursor.close();
        database.close();
        return false;
    }

    public boolean updateStringRecord(final String table, final String column, String value, String field, String f_value){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){

            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        ContentValues values = new ContentValues();

        values.put(column, value);

        if(database.update(table, values, "\"" + field + "\"=?", new String[]{f_value}) > 0){
            database.close();
            return true;
        }

        database.close();
        return false;
    }

    public boolean updateIntRecord(final String table, final String column, int value, String field, String f_value){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){

            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(column, value);
        if(database.update(table, contentValues, "\"" + field + "\"=?", new String[]{f_value} ) > 0){
            database.close();
            return true;
        }

        database.close();
        return false;
    }

    public void updateBoolRecord(final String table, final String column, boolean value, String field, String f_value){

        SQLiteDatabase database    = instance.getWritableDatabase(encryptDbPassword());

        if(database.isOpen()){
            database.close();
            database    = instance.getWritableDatabase(encryptDbPassword());
        }
        else
        {
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(column, value);
        database.update(table, contentValues, "\"" + field + "\"=?", new String[]{f_value} );

    }

    public int getControllersCount(){

        SQLiteDatabase database = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String servers_count_query = "SELECT * FROM " + CONTROLLERS_TABLE_NAME + ";";

        Cursor cursor = database.rawQuery(servers_count_query, null);

        return cursor.getCount();
    }

    public int getDetectorsCount(){

        SQLiteDatabase database = instance.getWritableDatabase(encryptDbPassword());

        if(!database.isOpen()){
            database    = instance.getWritableDatabase(encryptDbPassword());
        }

        String servers_count_query = "SELECT * FROM " + DETECTORS_TABLE_NAME + ";";

        Cursor cursor = database.rawQuery(servers_count_query, null);

        return cursor.getCount();
    }

    /************************************** DATABASE INTERFACE ************************/

    public boolean createDatabaseInterfaceTable(String table){ /* If table created will return true else return false */

        if(tableExist(table)){
            Log.w(TAG, "Table already exist");
            return false;
        }
        else
        {

            switch (table) {
                case "SERVER":
                    database().execSQL(CREATE_SERVER_TABLE);
                    break;
                case "SERVER_SETTINGS":
                    database().execSQL(CREATE_SERVER_SETTINGS_TABLE);
                    break;
                case "CAMERAS":
                    database().execSQL(CREATE_CAMERAS_TABLE);
                    break;
                case "CAMERAS_SETTINGS":
                    database().execSQL(CREATE_CAMERAS_SETTINGS_TABLE);
                    break;
                case "CONTROLLERS":
                    database().execSQL(CREATE_CONTROLLERS_TABLE);
                    break;
                case "CONTROLLERS_SETTINGS":
                    database().execSQL(CREATE_CONTROLLERS_SETTINGS_TABLE);
                    break;
                case "DETECTORS":
                    database().execSQL(CREATE_DETECTORS_TABLE);
                    break;
                case "DETECTORS_SETTINGS":
                    database().execSQL(CREATE_DETECTORS_SETTINGS_TABLE);
                    break;
            }

            if (tableExist(table)) {
                //System.out.println("Table created");
                return true;
            }

            return false;
        }
    }

    public boolean deleteDatabaseInterfaceTable(String table){ /* If table deleted will return true else return false */

        if(tableExist(table)) {

            SQLiteDatabase database = instance.getWritableDatabase(encryptDbPassword());

            if(!database.isOpen()){
                database    = instance.getWritableDatabase(encryptDbPassword());
            }

            String drop_table_query = "DROP TABLE IF EXISTS '" + table + "'";

            System.out.println(drop_table_query);

            database.execSQL(drop_table_query);
            database.close();

            if (tableExist(table)) {
                //System.out.println("Table still exist");
                return false;
            }
            else
            {
                //System.out.println("Table deleted");
                return true;
            }
        }
        else
        {
            //System.out.println("Table did not exist");
            return false;
        }
    }

    public JSONObject devicesNamesToJson(String table) throws JSONException {

        SQLiteDatabase database = instance.getWritableDatabase(encryptDbPassword());

        if (!database.isOpen()) {
            database = instance.getWritableDatabase(encryptDbPassword());
        }

        String devices_query = "SELECT * FROM " + table;

        Cursor cursor              = database.rawQuery(devices_query, null);
        int devices_count = cursor.getCount();
        int column_count = cursor.getColumnCount();

        JSONObject final_object = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        if(devices_count > 0) {

            if (!cursor.moveToFirst())
                cursor.moveToFirst();

            Log.w(TAG, "devices count [" + devices_count + "] device(s)");

            JSONObject obj = null;

            for (int i = 0; i < devices_count; i++) {

                Log.w(TAG, "devices name [" + cursor.getString(5) + "] device(s)");

                obj = new JSONObject();

                try {

                    obj.put("name", cursor.getString(5));

                }
                catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                jsonArray.put(obj);
                Log.w(TAG, "jsonArray " + jsonArray.toString());
            }

            cursor.close();
            database.close();
        }

        final_object.put("devices", jsonArray);
        Log.w(TAG, "final_object " + final_object.toString());

        return final_object;
    }

    public JSONArray getDevicesNameArray(String table) throws JSONException {

        JSONObject j = devicesNamesToJson(table);

        Log.w(TAG, "getDevicesNameArray " + j.toString());

        return j.getJSONArray("devices");
    }

    public String[] getDevicesForDelete(JSONArray json){

        Log.w(TAG, "getDevicesForDelete " + json.toString());
        Log.w(TAG, "json.length() " + json.length());
        interface_device_to_delete_array[0] = "Select";

        //Traversing through all the items in the json array
        for(int i = 0; i < json.length(); i++){

            try {

                //Getting json object
                JSONObject obj = json.getJSONObject(i);
                Log.w(TAG, "getString " + obj.getString("name"));
                //Adding the name of the student to array list

                interface_device_to_delete_array[i+1] = obj.getString("name");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return interface_device_to_delete_array;

        //Setting adapter to show the items in the spinner
        //spinner.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, students));
    }

    public String deleteDatabaseInterfaceDevice(String server_name, String table, String device_name){

        String field_name = "";

        if(tableExist(table)) {

            SQLiteDatabase database = instance.getWritableDatabase(encryptDbPassword());

            if(table.equals(CAMERAS_TABLE_NAME)){
                field_name = CAMERA_NAME;
            }

            if(table.equals(DETECTORS_TABLE_NAME)){
                field_name = DETECTOR_NAME;
            }

            if(table.equals(CONTROLLERS_TABLE_NAME)){
                field_name = CONTROLLER_NAME;
            }

            if(deviceExist(table, field_name, device_name)){

                if (!database.isOpen()) {
                    database = instance.getWritableDatabase(encryptDbPassword());
                }

                String drop_device_query = "DELETE FROM " + table + " WHERE " + field_name + "='" + device_name + "';";

                database.execSQL(drop_device_query);

                database.close();

                if(deviceExist(table, field_name, device_name)){

                    return "ERROR_DEVICE_STILL_PRESENT";
                }
                else
                {

                    int devices_count = getServerDevicesCount(server_name);

                    if(devices_count > 0){
                        devices_count = (devices_count - 1);
                    }

                    updateIntRecord(SERVER_TABLE_NAME, SERVER_NODES_COUNT, devices_count, SERVER_NAME, server_name);

                }
            }
            else
            {
                return "ERROR_DEVICE_NOT_FOUND";
            }
        }
        else
        {
            return "ERROR_TABLE_NOT_FOUND";
        }

        return "SUCCESS_DEVICE_DELETED";
    }

}
