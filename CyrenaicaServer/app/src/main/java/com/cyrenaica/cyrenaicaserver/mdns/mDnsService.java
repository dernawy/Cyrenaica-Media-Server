package com.cyrenaica.cyrenaicaserver.mdns;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.util.Map;
import java.util.Objects;

public class mDnsService {

    Context mContext;
    NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.RegistrationListener mRegistrationListener;
    public static final String TAG = "MDNS_SERVICE";

    NsdServiceInfo mService;

    public static final String HTTP_SERVICE_TYPE = "_http._tcp.";
    public static final String HTTP_SERVICE_NAME = "HttpService";

    public String mServiceName = "ServerSetup"; // must be 6 characters or more

    public String DISCOVER_SERVICE_TYPE;
    public String DISCOVER_SERVICE_NAME;

    public String SERVICE_NAME = "";
    public String SERVICE_TYPE = "";
    public String SERVICE_HOST = "";
    public int SERVICE_PORT = 0;

    String device;
    String mode;
    String name;
    String board;
    String fsize;
    String host;
    String soc_path;
    String port;
    String ap_name;

    public mDnsService(Context context) {
        mContext    = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void initializeNsd(String name, String type) {
        DISCOVER_SERVICE_TYPE = type;
        DISCOVER_SERVICE_NAME = name;
    }

    public void initializeDiscoveryListener() {

        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started: "+ regType);
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {



                if (!service.getServiceType().equals(DISCOVER_SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                }
                else if (service.getServiceName().equals(DISCOVER_SERVICE_NAME)) {

                    if (mNsdManager != null) {
                        initializeResolveListener();
                        mNsdManager.resolveService(service, mResolveListener);

                        Log.d(TAG, "Service Found: NAME -> [" + service.getServiceName() + "] TYPE -> [" + service.getServiceType() + "]");

                    }
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost: " + service);
                if (mService == service) {
                    mService = null;
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            }
        };
    }

    public void initializeResolveListener() {
        Log.v(TAG, "Service Resolve started");

        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {

                Log.e(TAG, "Resolve Succeeded. ");

                if (serviceInfo.getServiceType().equals(DISCOVER_SERVICE_NAME)) {

                }

                if (serviceInfo.getServiceName().equals(DISCOVER_SERVICE_NAME)) {

                    SERVICE_NAME = serviceInfo.getServiceName();
                    SERVICE_TYPE = serviceInfo.getServiceType();
                    SERVICE_HOST = serviceInfo.getHost().toString();
                    SERVICE_PORT = serviceInfo.getPort();

                    device    = getRecord(serviceInfo, "device");
                    mode      = getRecord(serviceInfo, "mode");
                    name      = getRecord(serviceInfo, "name");
                    board     = getRecord(serviceInfo, "board");
                    fsize     = getRecord(serviceInfo, "fsize");
                    host      = getRecord(serviceInfo, "host");
                    soc_path  = getRecord(serviceInfo, "soc_path");
                    port      = getRecord(serviceInfo, "port");
                    ap_name   = getRecord(serviceInfo, "ap_name");

                    Log.d(TAG, "[SERVICE-INFO]    - Service name [" + SERVICE_NAME + "] - Service type [" + SERVICE_TYPE + "] - Service host [" + SERVICE_HOST + "] - Service port [" + SERVICE_PORT + "]");

                    Log.d(TAG, "[SERVICE-DETAILS] - Device Type [" + device + "] - Mode [" + mode + "] - Name [" + name + "] - Flash [" + fsize + "] - Host [" + host + "] - Sockets Path [" + soc_path + "] - Port [" + port + "] - Ap Name [" + ap_name + "]");

                    return;
                }

                mService = serviceInfo;
            }
        };
    }

    public void initializeRegistrationListener() {

        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                mServiceName = NsdServiceInfo.getServiceName();
                Log.d(TAG, "Service registered: " + mServiceName);
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                Log.d(TAG, "Service registration failed: " + arg1);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                Log.d(TAG, "Service unregistered: " + arg0.getServiceName());
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d(TAG, "Service registration failed: " + errorCode);
            }
        };
    }

    public void registerService(int port) {
        tearDown();  // Cancel any previous registration request
        initializeRegistrationListener();
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(HTTP_SERVICE_NAME);
        serviceInfo.setServiceType(HTTP_SERVICE_TYPE);
        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    public void discoverServices() {
        stopDiscovery();  // Cancel any existing discovery request
        initializeDiscoveryListener();
        mNsdManager.discoverServices(DISCOVER_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void stopDiscovery() {

        if (mDiscoveryListener != null) {

            try {
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            }
            finally {
            }

            mDiscoveryListener = null;
        }
    }

    public NsdServiceInfo getChosenServiceInfo() {
        return mService;
    }

    public void tearDown() {
        if (mRegistrationListener != null) {
            try {
                mNsdManager.unregisterService(mRegistrationListener);
            }
            finally {
            }
            mRegistrationListener = null;
        }
    }

    public String getRecord(NsdServiceInfo serviceInfo, String key) {
        Map<String, byte[]> attr = serviceInfo.getAttributes();
        char c = 0;
        String sc = "";

        for (int i = 0; i < Objects.requireNonNull(attr.get(key)).length; i++) {
            c = (char) Objects.requireNonNull(attr.get(key))[i];
            sc += c;
        }
        //System.out.println("MDNS MESSAGE: " + sc);
        return sc;

    }
}