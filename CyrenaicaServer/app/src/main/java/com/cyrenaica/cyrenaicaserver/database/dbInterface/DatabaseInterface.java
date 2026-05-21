package com.cyrenaica.cyrenaicaserver.database.dbInterface;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.cyrenaicabox.AttachNodeActivity;
import com.cyrenaica.cyrenaicaserver.R;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.Encryption;
import com.cyrenaica.cyrenaicaserver.database.databaseManager.ServerSQLHelper;
import com.cyrenaica.cyrenaicaserver.language.LocalHelper;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;

public class DatabaseInterface extends AppCompatActivity {

    private static final String TAG = "DatabaseInterface";

   // private static ServerSQLHelper dbHelper;
   // Encryption encrp;

    TextView database_interface_title;
    TextView database_info_title;
    TextView database_name;
    TextView database_name_value;
    TextView tables_count;
    TextView tables_count_value;

    TextView create_table;
    TextView delete_table;
    TextView delete_device;

    TextView create_delete_table;


    //JSON Array
    private JSONArray devices_json;

    LinearLayout operations_inputs_layout;
    com.google.android.material.textfield.TextInputLayout create_delete_input_layout;
    com.google.android.material.textfield.TextInputEditText create_delete_input_field;

    LinearLayout delete_device_inputs_layout;
    LinearLayout tables_spinner_layout;
    TextView tables_spinner_label;
    Spinner tables_spinner;

    LinearLayout devices_spinner_layout;
    TextView devices_spinner_label;
    Spinner devices_spinner;
    private ArrayList<String> devices_list;

    com.google.android.material.textfield.TextInputLayout device_table_input_layout;
    com.google.android.material.textfield.TextInputEditText device_table_input_field;
    Button devices_to_delete_button;

    String TABLE_NAME;
    String DEVICE_NAME;
    private String[] device_array;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_interface);

        SQLiteDatabase.loadLibs(this);
        ServerSQLHelper.dbHelper = ServerSQLHelper.getInstance(getApplicationContext());
        ServerSQLHelper.encrp = new Encryption(getApplicationContext());

        //Initializing the ArrayList
        devices_list                = new ArrayList<String>();

        database_interface_title    = findViewById(R.id.id_database_interface_title);
        database_info_title         = findViewById(R.id.id_database_info_title);
        database_name               = findViewById(R.id.id_database_name_label);
        database_name_value         = findViewById(R.id.id_database_name_value);
        tables_count                = findViewById(R.id.id_database_tables_count_label);
        tables_count_value          = findViewById(R.id.id_database_tables_count_value);
        create_table                = findViewById(R.id.id_create_table_button);
        delete_table                = findViewById(R.id.id_delete_table_button);
        operations_inputs_layout    = findViewById(R.id.id_operations_inputs_layout);
        create_delete_table         = findViewById(R.id.id_create_delete_table_button);
        create_delete_input_layout  = findViewById(R.id.id_create_delete_input_layout);
        create_delete_input_field   = findViewById(R.id.id_create_delete_input_field);

        delete_device               = findViewById(R.id.id_delete_device_button);

        delete_device_inputs_layout = findViewById(R.id.id_delete_device_inputs_layout);

        tables_spinner_layout       = findViewById(R.id.id_db_interface_tables_spinner_layout);
        tables_spinner_label        = findViewById(R.id.id_db_interface_tables_spinner_label);
        tables_spinner              = findViewById(R.id.id_tables_spinner);

        devices_spinner_layout      = findViewById(R.id.id_db_interface_devices_spinner_layout);
        devices_spinner_label       = findViewById(R.id.id_db_interface_devices_spinner_label);
        devices_spinner             = findViewById(R.id.id_camera_record_device_name_font_list_spinner);

        devices_to_delete_button    = findViewById(R.id.id_search_for_devices_to_delete_button);

        //database_name_value.setText(ServerSQLHelper.getDbName());
        tables_count_value.setText(String.valueOf(ServerSQLHelper.dbHelper.getTablesCount()));

        //Make this field Capitals letters
        create_delete_input_field.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        operations_inputs_layout.setVisibility(View.GONE);

        if (LocalHelper.getLanguage(DatabaseInterface.this).equalsIgnoreCase("ar")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                database_interface_title.setTypeface(getResources().getFont(R.font.cairo));
                database_info_title.setTypeface(getResources().getFont(R.font.cairo));
                database_name.setTypeface(getResources().getFont(R.font.cairo));
                tables_count.setTypeface(getResources().getFont(R.font.cairo));
                create_table.setTypeface(getResources().getFont(R.font.cairo));
                delete_table.setTypeface(getResources().getFont(R.font.cairo));

                create_delete_input_layout.setTypeface(getResources().getFont(R.font.cairo));
                create_delete_table.setTypeface(getResources().getFont(R.font.cairo));

            }
        }

        String prefs_cipher   = ServerSQLHelper.encrp.getDbCipher();
        String prefs_password = ServerSQLHelper.encrp.getDbPassword();

        // Set the password cipher
        ServerSQLHelper.dbHelper.setPasswordCipher(prefs_cipher);

        // Set the database password
        ServerSQLHelper.dbHelper.setPassword(prefs_password);

        if(!ServerSQLHelper.dbHelper.dbExist()){
            Intent intent = new Intent(DatabaseInterface.this, com.cyrenaica.cyrenaicaserver.database.databaseManager.DatabaseSetup.class);
            intent.putExtra("Activity", "DatabaseInterface");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        create_table.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {


                operations_inputs_layout.setVisibility(View.VISIBLE);
                create_delete_input_layout.setHint("Enter the name of table to be created");
                create_delete_table.setText(R.string.database_interface_database_create_table_create_button);

                create_delete_table.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        hideKeyboardFrom();

                        TABLE_NAME = Objects.requireNonNull(create_delete_input_field.getText()).toString();

                        if(ServerSQLHelper.dbHelper.createDatabaseInterfaceTable(TABLE_NAME)){
                            create_delete_input_field.setText("");
                            operations_inputs_layout.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "The table [" + TABLE_NAME + "] created successfully.", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Error while creating the table [" + TABLE_NAME + "].", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        delete_table.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                operations_inputs_layout.setVisibility(View.VISIBLE);
                create_delete_input_layout.setHint("Enter the name of table to be deleted");
                create_delete_table.setText(R.string.database_interface_database_delete_table_delete_button);

                create_delete_table.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        hideKeyboardFrom();

                        TABLE_NAME = Objects.requireNonNull(create_delete_input_field.getText()).toString();

                        if(ServerSQLHelper.dbHelper.deleteDatabaseInterfaceTable(TABLE_NAME)){
                            create_delete_input_field.setText("");
                            operations_inputs_layout.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "The table [" + TABLE_NAME + "] deleted successfully from database.", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Error while deleting the table [" + TABLE_NAME + "] from database.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        delete_device.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                delete_device_inputs_layout.setVisibility(View.VISIBLE);

            }
        });

        ArrayAdapter<String> tables_adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.database_interface_spinner_custom_dropdown_item, ServerSQLHelper.dbHelper.TABLES_ARRAY);
        tables_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tables_spinner.setAdapter(tables_adapter);

        tables_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(i > 0){

                    TABLE_NAME = adapterView.getItemAtPosition(i).toString();

                    try {

                        device_array = ServerSQLHelper.dbHelper.getDevicesForDelete(ServerSQLHelper.dbHelper.getDevicesNameArray(TABLE_NAME));
                        devices_spinner_layout.setVisibility(View.VISIBLE);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }

                    ArrayAdapter<String> devices_adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.database_interface_spinner_custom_dropdown_item, device_array);
                    devices_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    devices_spinner.setAdapter(devices_adapter);

                    for(int x = 0; x < device_array.length; x++){
                        Log.i(TAG, "List array Length [" + device_array[x] + "].");
                    }


                    Toast.makeText(getApplicationContext(), "Table [" + TABLE_NAME + "].", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "List array Length [" + device_array.length + "].");
                    Log.i(TAG, "Popup Length [" + l + "].");
                }
                else
                {
                    devices_spinner_layout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        devices_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(i > 0) {

                    DEVICE_NAME = adapterView.getItemAtPosition(i).toString();
                    devices_to_delete_button.setVisibility(View.VISIBLE);


                    devices_to_delete_button.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            String server_name = ServerSQLHelper.dbHelper.getServerNameFromDeviceTable(TABLE_NAME, DEVICE_NAME);

                            if(server_name.equals("DEVICE_NOT_FOUND")){
                                server_name         = "DEVICE_NOT_FOUND";
                            }

                            if(server_name.equals("SERVER_NOT_FOUND")){
                                server_name         = "SERVER_NOT_FOUND";
                            }

                            if(!server_name.equals("SERVER_NOT_FOUND") && !server_name.equals("DEVICE_NOT_FOUND")) {

                                String delete_device_query = ServerSQLHelper.dbHelper.deleteDatabaseInterfaceDevice(server_name, TABLE_NAME, DEVICE_NAME);

                                if (delete_device_query.equals("SUCCESS_DEVICE_DELETED")) {
                                    Toast.makeText(getApplicationContext(), "The device [" + DEVICE_NAME + "] deleted successfully.", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Log.i(TAG, "Error while deleting device [" + DEVICE_NAME + "] --> Delete Results [" + delete_device_query + "].");
                                }
                            }


                        }
                    });

                    Toast.makeText(getApplicationContext(), "Table [" + DEVICE_NAME + "].", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    devices_to_delete_button.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }

    private void getDevicesData(){

    }

    public void hideKeyboardFrom() {
        if (this.getCurrentFocus() != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}