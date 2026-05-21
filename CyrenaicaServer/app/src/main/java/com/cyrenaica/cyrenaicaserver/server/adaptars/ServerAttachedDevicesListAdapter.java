package com.cyrenaica.cyrenaicaserver.server.adaptars;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public class ServerAttachedDevicesListAdapter extends BaseAdapter {

    ArrayList<ServerAttachedDevicesListData> ServersAttachedDevicesList;
    Context context;

    public ServerAttachedDevicesListAdapter(Context context, ArrayList<ServerAttachedDevicesListData> serversList) {
        this.ServersAttachedDevicesList = serversList;
        this.context  = context;

    }

    @Override
    public int getCount() {
        return ServersAttachedDevicesList.size();
    }

    @Override
    public Object getItem(int position) {
        return ServersAttachedDevicesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ServerAttachedDevicesListHolder view = (ServerAttachedDevicesListHolder) convertView;

        if (view == null) {
            view = new ServerAttachedDevicesListHolder(context);
        }

        ServerAttachedDevicesListData data = (ServerAttachedDevicesListData) getItem(position);

        view.setServerAttachedDevicesView(data);

        return view;
    }
}
