package com.cyrenaica.cyrenaicaserver.server.adaptars;

public class ServerAttachedDevicesListData {

    int DEVICE_ID;
    String DEVICE_ATTACHE_ID;
    String DEVICE_TYPE;
    String DEVICE_NAME;
    String DEVICE_MODEL;
    String DEVICE_AP_IP;
    String DEVICE_AP_MAC;
    String DEVICE_NET_IP;
    String DEVICE_STA_MAC;


    String DEVICE_WIFI_CONNECTION_STATUS;
    String DEVICE_ESPNOW_CONNECTION_STATUS;


    public void setDeviceId(int id) {
        this.DEVICE_ID = id;
    }

    public int getDeviceId() {
        return DEVICE_ID;
    }

    public void setAttacheId(String attache_id) {
        this.DEVICE_ATTACHE_ID = attache_id;
    }

    public String getAttacheId() {
        return DEVICE_ATTACHE_ID;
    }

    public void setDeviceType(String type) {
        this.DEVICE_TYPE = type;
    }

    public String getDeviceType() {
        return DEVICE_TYPE;
    }

    public void setDeviceName(String name) {
        this.DEVICE_NAME = name;
    }

    public String getDeviceName() {
        return DEVICE_NAME;
    }

    public void setDeviceModel(String model) {
        this.DEVICE_MODEL = model;
    }

    public String getDeviceModel() {
        return DEVICE_MODEL;
    }

    public void setDeviceApIp(String ap_ip) {
        this.DEVICE_AP_IP = ap_ip;
    }

    public String getDeviceApIp() {
        return DEVICE_AP_IP;
    }

    public void setDeviceApMac(String ap_mac) {
        this.DEVICE_AP_MAC = ap_mac;
    }

    public String getDeviceApMac() {
        return DEVICE_AP_MAC;
    }

    public void setDeviceNetIp(String net_ip) {this.DEVICE_NET_IP = net_ip;}

    public String getDeviceNetIp() {
        return DEVICE_NET_IP;
    }

    public void setDeviceStaMac(String sta_mac) {this.DEVICE_STA_MAC = sta_mac;}

    public String getDeviceStaMac() {
        return DEVICE_STA_MAC;
    }

    public void setDeviceWifiConnectionStatus(String wifi_connection_status) {this.DEVICE_WIFI_CONNECTION_STATUS = wifi_connection_status;}

    public String getDeviceWifiConnectionStatus() {
        return DEVICE_WIFI_CONNECTION_STATUS;
    }

    public void setDeviceEspnowConnectionStatus(String espnow_connection_status) {this.DEVICE_ESPNOW_CONNECTION_STATUS = espnow_connection_status;}

    public String getDeviceEspnowConnectionStatus() {
        return DEVICE_ESPNOW_CONNECTION_STATUS;
    }



}
