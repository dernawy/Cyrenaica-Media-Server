package com.cyrenaica.cyrenaicaserver.server;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.cyrenaica.cyrenaicaserver.R;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.ServerSQLHelper;
import com.cyrenaica.cyrenaicaserver.language.LocalHelper;
import com.cyrenaica.cyrenaicaserver.mdns.mDnsService;
import com.cyrenaica.cyrenaicaserver.server.adaptars.AllServersListAdapter;
import com.cyrenaica.cyrenaicaserver.server.adaptars.AllServersListData;
import com.cyrenaica.cyrenaicaserver.tools.ConnectionReceiver;
import com.cyrenaica.cyrenaicaserver.tools.Connectivity;
import com.cyrenaica.cyrenaicaserver.tools.Tools;
import java.util.ArrayList;

public class ViewAllServers extends AppCompatActivity {

    private static final String TAG = "VIEW_ALL_SERVERS";

    TextView page_label;
    ListView all_servers_list_view;

    static ArrayList<AllServersListData> _servers_list = new ArrayList<AllServersListData>();
    static AllServersListData serversLog;

    static AllServersListAdapter all_servers_adapter;

    Intent AllServersSocketsService;
    ConnectionReceiver SocketsReceiver = new ConnectionReceiver();

    IntentFilter socket_conn_filter;

    mDnsService DNS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_all_servers);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* init Toolbar */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.d(TAG, "onCreate");

        Tools.hideSoftKeyboard(ViewAllServers.this); // Hide keyboard

        /* To indicate the connectivity of network all communications to server must be after this method */
        Connectivity.appConnectionOnscreenHandle(getApplicationContext());

        //DNS = new mDnsService(this);
        //DNS.initializeNsd("ViewAllServers", "_http._tcp.");

        page_label            = findViewById(R.id.id_page_label);
        all_servers_list_view = findViewById(R.id.id_all_servers_list_view);

        if (LocalHelper.getLanguage(getApplicationContext()).equalsIgnoreCase("ar")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                page_label.setTypeface(getResources().getFont(R.font.changa));
                //server_model_label.setTypeface(getResources().getFont(R.font.cairo));

            }
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                page_label.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
                //server_model_label.setTypeface(getResources().getFont(R.font.robotoslab));
            }
        }

        _servers_list.clear();
        all_servers_adapter = new AllServersListAdapter(ViewAllServers.this, _servers_list);
        all_servers_list_view.setAdapter(all_servers_adapter);

        ServerSQLHelper.DbHelper(getApplicationContext()).getAllServersForList(_servers_list, all_servers_adapter);

    }

    /** When the activity enters the Started state, the system invokes this callback. The onStart() call makes the activity visible to the user,
     * as the app prepares for the activity to enter the foreground and become interactive.
     */
    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        /*AllServersSocketsService = new Intent(getApplicationContext(), SocketsService.class);
        SocketsService.startSockets(AllServersSocketsService, getApplicationContext(), SocketsReceiver, "192.168.1.97", "80", "production_ws");*/
        //SocketsService.registerSockets(getApplicationContext(), SocketsReceiver, "SOCKETS_CONNECTED");

        /*if (DNS != null) {
            DNS.discoverServices();
        }*/
    }

    /** When the activity enters the Resumed state, it comes to the foreground,
     * and then the system invokes the onResume() callback
     */
    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");

       // SocketsService.registerSockets(getApplicationContext(), SocketsReceiver, "SOCKETS_CONNECTION_SERVICE");

        /*if (DNS != null) {
            DNS.discoverServices();
        }*/
    }

    /** The system calls this method as the first indication that the user is leaving your activity (though it does not always mean the activity is being destroyed);
        it indicates that the activity is no longer in the foreground (though it may still be visible if the user is in multi-window mode).
     */
    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, "onPause");

        /*if (DNS != null) {
            DNS.stopDiscovery();
        }*/

        /* Will unregister Receiver and stop sockets */
        //SocketsService.stopSockets(AllServersSocketsService, getApplicationContext(), SocketsReceiver, false);
    }

    /** When your activity is no longer visible to the user, it has entered the Stopped state, and the system invokes the onStop() callback. */
    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop");

        /*if (DNS != null) {
            DNS.stopDiscovery();
        }*/

        /* Will unregister Receiver and stop sockets */
       // SocketsService.stopSockets(AllServersSocketsService, getApplicationContext(), SocketsReceiver, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.add_new_server){

        }

        if(id == R.id.add_new_device){

        }

        if(id == R.id.view_installed_servers){

        }

        if(id == R.id.view_installed_devices){

        }

        if(id == R.id.settings){

        }

        return true;

    }
}