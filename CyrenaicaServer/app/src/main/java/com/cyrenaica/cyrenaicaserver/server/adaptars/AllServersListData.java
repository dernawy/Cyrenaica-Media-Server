package com.cyrenaica.cyrenaicaserver.server.adaptars;

public class AllServersListData {

    int SERVER_ID;

    int SERVER_SETUP_OK;
    String SERVER_NAME;
    String SERVER_MODEL;
    String SERVER_SSID;
    String SERVER_PASS;
    int SERVER_USE_STA_STATIC_IP;
    String SERVER_LOCAL_IP;
    String SERVER_STA_STATIC_IP;
    int SERVER_REMOTE_ACCESS;
    String SERVER_REMOTE_IP;
    String SERVER_REMOTE_DOMAIN;
    String SERVER_GATEWAY_IP;
    String SERVER_NETMASK_IP;
    String SERVER_DNS_IP;
    String SERVER_STA_HOST;
    String SERVER_STA_MAC;

    int SERVER_USE_AP_STATIC_IP;
    String SERVER_AP_IP;
    String SERVER_AP_STATIC_IP;
    String SERVER_AP_MAC;
    String SERVER_AP_GATEWAY_IP;
    String SERVER_AP_NETMASK_IP;
    String SERVER_AP_HOST;
    int SERVER_AP_MAX_CLIENTS;
    int SERVER_AP_CHANNEL;
    int SERVER_SSID_HIDDEN;
    String SERVER_SPIFFS;
    String SERVER_HEAP;
    String SERVER_FREQ;
    int ROUTER_SETUP;
    String ROUTER_SSID;
    String ROUTER_PASS;



    int DEVICES_COUNT;

    public void setServerId (int id) {
        this.SERVER_ID = id;
    }

    public int getServerId() {
        return SERVER_ID;
    }

    public void setServerSetupOK (int setup_ok) {
        this.SERVER_SETUP_OK = setup_ok;
    }

    public int getServerSetupOK() {
        return SERVER_SETUP_OK;
    }

    public void setServerName (String name) {
        this.SERVER_NAME = name;
    }

    public String getServerName() {
        return SERVER_NAME;
    }

    public void setServerModel (String model) {
        this.SERVER_MODEL = model;
    }

    public String getServerModel() {
        return SERVER_MODEL;
    }

    public void setServerApSsid (String ssid) {
        this.SERVER_SSID = ssid;
    }

    public String getServerApSsid() {
        return SERVER_SSID;
    }

    public void setServerApPass (String pass) {
        this.SERVER_PASS = pass;
    }

    public String getServerApPass() {
        return SERVER_PASS;
    }

    public void setServerStaUseStaticIp (int use_sta_static_ip) {this.SERVER_USE_STA_STATIC_IP = use_sta_static_ip;}

    public int getServerStaUseStaticIp() {
        return SERVER_USE_STA_STATIC_IP;
    }
    /** For STA */
    public void setServerLocalIp (String local_ip) {
        this.SERVER_LOCAL_IP = local_ip;
    }
    /** For STA */
    public String getServerLocalIp() {
        return SERVER_LOCAL_IP;
    }
    /** For STA */
    public void setServerStaStaticIp (String sta_static_ip) {this.SERVER_STA_STATIC_IP = sta_static_ip;}
    /** For STA */
    public String getServerStaStaticIp() {
        return SERVER_STA_STATIC_IP;
    }

    public void setServerRemoteAccess (int remote_access) {this.SERVER_REMOTE_ACCESS = remote_access;}

    public int getServerRemoteAccess() {
        return SERVER_REMOTE_ACCESS;
    }

    public void setServerRemoteIp (String remote_ip) {this.SERVER_REMOTE_IP = remote_ip;}

    public String getServerRemoteIp() {
        return SERVER_REMOTE_IP;
    }

    public void setServerRemoteDomain (String remote_domain) {this.SERVER_REMOTE_DOMAIN = remote_domain;}

    public String getServerDomain() {
        return SERVER_REMOTE_DOMAIN;
    }

    /** For STA */
    public void setServerGateway (String gateway_ip) {this.SERVER_GATEWAY_IP = gateway_ip;}
    /** For STA */
    public String getServerGateway() {
        return SERVER_GATEWAY_IP;
    }
    /** For STA */
    public void setServerNetmask (String netmask_ip) {this.SERVER_NETMASK_IP = netmask_ip;}
    /** For STA */
    public String getServerNetmask() {
        return SERVER_NETMASK_IP;
    }
    /** For STA */
    public void setServerDns (String dns_ip) {this.SERVER_DNS_IP = dns_ip;}
    /** For STA */
    public String getServerDns() {
        return SERVER_DNS_IP;
    }
    /** For STA */
    public void setServerStaHost (String sta_host) {this.SERVER_STA_HOST = sta_host;}
    /** For STA */
    public String getServerStaHost() {
        return SERVER_STA_HOST;
    }
    /** For STA */
    public void setServerStaMac (String sta_mac) {this.SERVER_STA_MAC = sta_mac;}
    /** For STA */
    public String getServerStaMac() {
        return SERVER_STA_MAC;
    }
    /** For AP */
    public void setServerApUseStaticIp (int use_sta_static_ip) {this.SERVER_USE_AP_STATIC_IP = use_sta_static_ip;}
    /** For AP */
    public int getServerApUseStaticIp() {
        return SERVER_USE_AP_STATIC_IP;
    }
    /** For AP */
    public void setServerApIp (String ap_ip) {this.SERVER_AP_IP = ap_ip;}
    /** For AP */
    public String getServerApIp() {
        return SERVER_AP_IP;
    }
    /** For AP */
    public void setServerApStaticIp (String ap_static_ip) {this.SERVER_AP_STATIC_IP = ap_static_ip;}
    /** For AP */
    public String getServerApStaticIp() {
        return SERVER_AP_STATIC_IP;
    }
    /** For AP */
    public void setServerApMac (String ap_mac) {this.SERVER_AP_MAC = ap_mac;}
    /** For AP */
    public String getServerApMac() {
        return SERVER_AP_MAC;
    }
    /** For AP */
    public void setServerApGateway (String ap_gateway_ip) {this.SERVER_AP_GATEWAY_IP = ap_gateway_ip;}
    /** For AP */
    public String getServerApGateway() {
        return SERVER_AP_GATEWAY_IP;
    }
    /** For AP */
    public void setServerApNetmask (String ap_netmask_ip) {this.SERVER_AP_NETMASK_IP = ap_netmask_ip;}
    /** For AP */
    public String getServerApNetmask() {
        return SERVER_AP_NETMASK_IP;
    }
    /** For AP */
    public void setServerApHost (String ap_host) {this.SERVER_AP_HOST = ap_host;}
    /** For AP */
    public String getServerApHost() {
        return SERVER_AP_HOST;
    }
    /** For AP */
    public void setServerApMaxClients (int max_clients) {this.SERVER_AP_MAX_CLIENTS = max_clients;}
    /** For AP */
    public int getServerApMaxClients() {
        return SERVER_AP_MAX_CLIENTS;
    }
    /** For AP */
    public void setServerApChannel (int channel) {this.SERVER_AP_CHANNEL = channel;}
    /** For AP */
    public int getServerApChannel() {
        return SERVER_AP_CHANNEL;
    }
    /** For AP */
    public void setServerApSsidHidden (int hidden) {this.SERVER_SSID_HIDDEN = hidden;}
    /** For AP */
    public int getServerSsidHidden() {
        return SERVER_SSID_HIDDEN;
    }
    /** For AP */
    public void setServerRouterSetupOk (int ok) {this.ROUTER_SETUP = ok;}
    /** For AP */
    public int getServerRouterSetupOk() {
        return ROUTER_SETUP;
    }

    /** For AP */
    public void setServerRouterSsid (String router_ssid) {this.ROUTER_SSID = router_ssid;}
    /** For AP */
    public String getServerRouterSsid() {
        return ROUTER_SSID;
    }
    /** For AP */
    public void setServerRouterPass (String router_pass) {this.ROUTER_PASS = router_pass;}
    /** For AP */
    public String getServerRouterPass() {
        return ROUTER_PASS;
    }


    public void setServerSpiffs (String spiffs) {
        this.SERVER_SPIFFS = spiffs;
    }

    public String getServerSpiffs() {
        return SERVER_SPIFFS;
    }

    public void setServerHeap (String heap) {
        this.SERVER_HEAP = heap;
    }

    public String getServerHeap() {
        return SERVER_HEAP;
    }

    public void setServerFreq (String freq) {
        this.SERVER_FREQ = freq;
    }

    public String getServerFreq() {
        return SERVER_FREQ;
    }

    public void setServerDevicesCount (int count) {
        this.DEVICES_COUNT = count;
    }

    public int getServerDevicesCount() {
        return DEVICES_COUNT;
    }
}
