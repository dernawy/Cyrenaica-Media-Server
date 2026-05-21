package com.cyrenaica.cyrenaicaserver.database.databaseManager;



import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

//import com.cyrenaica.cyrenaicaserver.DeviceSetup;
import com.cyrenaica.cyrenaicaserver.MainActivity;
//import com.cyrenaica.cyrenaicaserver.NewServerSettings;
import com.cyrenaica.cyrenaicaserver.R;
//import com.cyrenaica.cyrenaicaserver.RouterCheckActivity;
//import com.cyrenaica.cyrenaicaserver.SecurityCheckActivity;
import com.cyrenaica.cyrenaicaserver.server.AddNewServer;
import com.cyrenaica.cyrenaicaserver.server.NewServerSettings;
import com.cyrenaica.cyrenaicaserver.server.SecurityCheckActivity;
import com.cyrenaica.cyrenaicaserver.tools.Tools;
import com.cyrenaica.cyrenaicaserver.database.dbInterface.DatabaseInterface;
import com.cyrenaica.cyrenaicaserver.language.LocalHelper;

import net.sqlcipher.database.SQLiteDatabase;
import java.util.Objects;

public class DatabaseSetup extends AppCompatActivity {

    private static final String TAG = "DatabaseSetup";

    ScrollView scroll_layout;
    LinearLayout setup_end_layout;

    TextView page_title;
    TextView page_intro;

    com.google.android.material.textfield.TextInputLayout db_cipher_layout;
    com.google.android.material.textfield.TextInputEditText db_cipher_input;

    com.google.android.material.textfield.TextInputLayout db_password_layout;
    com.google.android.material.textfield.TextInputEditText db_password_input;

    TextView page_label;
    TextView end_message;
    Button db_setup_save_btn;
    Button end_btn;


    //ServerSQLHelper dbSetupHelper;
    //Encryption encrp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_setup);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        String _activity    = getIntent().getStringExtra("Activity");

        scroll_layout       = findViewById(R.id.id_db_setup_scroll_container);
        setup_end_layout    = findViewById(R.id.id_db_setup_end_layout);

        page_label          = findViewById(R.id.id_db_setup_page_title);
        //page_intro        = findViewById(R.id.id_db_setup_page_intro);

        db_cipher_layout    = findViewById(R.id.id_db_cipher_layout);
        db_cipher_input     = findViewById(R.id.id_db_cipher_input);

        db_password_layout  = findViewById(R.id.id_db_password_layout);
        db_password_input   = findViewById(R.id.id_db_password_input);

        db_setup_save_btn   = findViewById(R.id.id_db_setup_save_btn);
        end_btn             = findViewById(R.id.id_db_setup_end_btn);

        end_message         = findViewById(R.id.id_db_setup_end_message);

        setup_end_layout.setVisibility(View.GONE);

        //SQLiteDatabase.loadLibs(this);
        //ServerSQLHelper.dbHelper = ServerSQLHelper.getInstance(getApplicationContext());

        //ServerSQLHelper.encrp = new Encryption(getApplicationContext());

        db_password_input.setText("palestin2023");
        db_cipher_input.setText("derna@libya123456789");

        if (LocalHelper.getLanguage(DatabaseSetup.this).equalsIgnoreCase("ar")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                page_label.setTypeface(getResources().getFont(R.font.cairo));
                db_cipher_layout.setTypeface(getResources().getFont(R.font.changa));
                db_password_layout.setTypeface(getResources().getFont(R.font.changa));
                db_setup_save_btn.setTypeface(getResources().getFont(R.font.changa));
                end_btn.setTypeface(getResources().getFont(R.font.changa));
                end_message.setTypeface(getResources().getFont(R.font.changa));
            }
        }
        else
        {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                page_label.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
            }

        }

        Log.i(TAG, "COME FROM:" + _activity);

        db_setup_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!ServerSQLHelper.dbHelper.dbExist()) {

                    // Save encrypted Cipher & password in prefs
                    ServerSQLHelper.encrp.saveDbPassword(Objects.requireNonNull(db_password_input.getText()).toString());
                    ServerSQLHelper.encrp.saveDbCipher(Objects.requireNonNull(db_cipher_input.getText()).toString());

                    //Hide keyboard
                    Tools.hideSoftKeyboard(DatabaseSetup.this);

                    // After saving the password to prefs we check if it is saved correctly
                    if(ServerSQLHelper.encrp.searchDbPassword()){

                        String prefs_cipher   = ServerSQLHelper.encrp.getDbCipher();
                        String prefs_password = ServerSQLHelper.encrp.getDbPassword();

                        // Set the password cipher
                        ServerSQLHelper.dbHelper.setPasswordCipher(prefs_cipher);

                        // Set the database password
                        ServerSQLHelper.dbHelper.setPassword(prefs_password);

                        // Check if SERVER table was created or not
                        if(ServerSQLHelper.DbHelper(getApplicationContext()).tableExist(ServerSQLHelper.SERVER_TABLE_NAME)){

                            scroll_layout.setVisibility(View.GONE);
                            setup_end_layout.setVisibility(View.VISIBLE);

                            end_message.setText(R.string.database_setup_end_msg_success);
                            end_btn.setText(R.string.terminate_button);

                            end_btn.setOnClickListener(new View.OnClickListener() {
                                 @Override
                                 public void onClick(View v) {

                                     Log.i(TAG, "GO TO:" + _activity);

                                     if(_activity.equals("MainActivity")){
                                         Intent intent = new Intent(DatabaseSetup.this, MainActivity.class);
                                         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                         startActivity(intent);
                                         finish();
                                     }

                                     if(_activity.equals("AddNewServer")){
                                         Intent intent = new Intent(DatabaseSetup.this, AddNewServer.class);
                                         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                         startActivity(intent);
                                         finish();
                                     }

                                     if(_activity.equals("DatabaseInterface")){
                                         Intent intent = new Intent(DatabaseSetup.this, DatabaseInterface.class);
                                         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                         startActivity(intent);
                                         finish();
                                     }

                                     if(_activity.equals("SecurityCheckActivity")){
                                         Intent intent = new Intent(DatabaseSetup.this, SecurityCheckActivity.class);
                                         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                         startActivity(intent);
                                         finish();
                                     }

                                     if(_activity.equals("NewServerSettings")){
                                         Intent intent = new Intent(DatabaseSetup.this, NewServerSettings.class);
                                         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                         startActivity(intent);
                                         finish();
                                     }

                                 }
                            });
                        }
                        else
                        {
                            scroll_layout.setVisibility(View.GONE);
                            setup_end_layout.setVisibility(View.VISIBLE);

                            end_message.setText(R.string.database_setup_end_msg_error);
                            end_btn.setText(R.string.retry_button);

                            end_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(DatabaseSetup.this, DatabaseSetup.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    }
                }
                else
                {
                    end_message.setText("Database exist");
                }
            }
        });
    }
}

