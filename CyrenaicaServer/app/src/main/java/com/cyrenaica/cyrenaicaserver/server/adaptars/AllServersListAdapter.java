package com.cyrenaica.cyrenaicaserver.server.adaptars;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public class AllServersListAdapter extends BaseAdapter {

    public ArrayList<AllServersListData> AllServersList;
    Context context;

    public AllServersListAdapter(Context context, ArrayList<AllServersListData> serversList) {
        this.AllServersList = serversList;
        this.context  = context;

    }

    @Override
    public int getCount() {
        return AllServersList.size();
    }

    @Override
    public Object getItem(int position) {
        return AllServersList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        AllServersListHolder view = (AllServersListHolder) convertView;

        if (view == null) {
            view = new AllServersListHolder(context);
        }

        AllServersListData data = (AllServersListData) getItem(position);

        view.setAllServersView(data);

        return view;
    }
}
