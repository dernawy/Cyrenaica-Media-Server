package com.cyrenaica.cyrenaicaserver.nodes.Cameras;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cyrenaica.cyrenaicaserver.R;
import com.cyrenaica.cyrenaicaserver.language.LocalHelper;

import com.cyrenaica.cyrenaicaserver.server.ServerHomeActivity;
import com.cyrenaica.cyrenaicaserver.tools.FilesTools;
import com.cyrenaica.cyrenaicaserver.tools.Tools;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class CameraHome1Activity extends AppCompatActivity {

    private static final String TAG = "CAMERA_HOME";

    private int DEVICE_ID;
    private String DEVICE_NAME;
    private String DEVICE_IP;
    private String WIFI_STATUS;
    private String ESPNOW_STATUS;

    TextView page_label;
    TextView device_name_label;
    TextView device_name_value;
    TextView device_ip_label;
    TextView device_ip_value;
    TextView wifi_status_label;
    TextView wifi_status_value;
    TextView espnow_status_label;
    TextView espnow_status_value;
    TextView choose_action_label;
    TextView record_button_label;
    TextView watch_button_label;
    ImageView record_button_image;
    ImageView watch_button_image;
    Button device_details_button;

    FrameLayout device_info_frame;     // This hold the device info, will be hidden when click on one of buttons
    LinearLayout action_choose_layout; // This hold the watch button and record button, will be hidden when click on one of buttons

    boolean show_on_screen_info;

    boolean show_logo;
    boolean show_device_name;
    boolean show_date_time;
    boolean use_face_detect;
    boolean use_face_recognise;
    boolean use_record_on_app;
    String file_name;
    boolean use_mp4_file;
    boolean use_mov_file;
    boolean use_as_origenal;
    boolean use_hd;
    boolean use_4k;
    int video_max_length;

    boolean less_than256gb;
    boolean sd_mounted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera_home1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* GET DEVICE INFO FROM Intent */
        DEVICE_ID      = getIntent().getIntExtra("id", -1);
        DEVICE_NAME    = getIntent().getStringExtra("name");
        DEVICE_IP      = getIntent().getStringExtra("ip");
        WIFI_STATUS    = getIntent().getStringExtra("wifi_status");
        ESPNOW_STATUS  = getIntent().getStringExtra("espnow_status");

        page_label            = findViewById(R.id.id_camera_home_page_label);
        device_name_label     = findViewById(R.id.id_camera_home_device_name_label);
        device_name_value     = findViewById(R.id.id_camera_home_device_name_value);
        device_ip_label       = findViewById(R.id.id_camera_home_device_ip_label);
        device_ip_value       = findViewById(R.id.id_camera_home_device_ip_value);
        wifi_status_label     = findViewById(R.id.id_camera_home_wifi_status_label);
        wifi_status_value     = findViewById(R.id.id_camera_home_wifi_status_value);
        espnow_status_label   = findViewById(R.id.id_camera_home_espnow_status_label);
        espnow_status_value   = findViewById(R.id.id_camera_home_espnow_status_value);
        choose_action_label   = findViewById(R.id.id_camera_home_choose_action_label);
        record_button_label   = findViewById(R.id.id_camera_home_record_btn_label);
        watch_button_label    = findViewById(R.id.id_camera_home_watch_btn_label);

        record_button_image   = findViewById(R.id.id_camera_home_record_btn_image);
        watch_button_image    = findViewById(R.id.id_camera_home_watch_btn_image);
        device_details_button = findViewById(R.id.id_camera_home_details_btn);
        device_info_frame     = findViewById(R.id.id_camera_home_device_info_frame);     // To be hidden when click on RECORD Icon or on WATCH Icon
        action_choose_layout  = findViewById(R.id.id_camera_home_action_choose_layout);  // To be hidden when click on RECORD Icon or on WATCH Icon
        /* END OF CAMERA ACTIONS INTERFACE */

        /* SET FONT FOR TextView & Button */
        if (LocalHelper.getLanguage(getApplicationContext()).equalsIgnoreCase("ar")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                page_label.setTypeface(getResources().getFont(R.font.changa));
                device_name_label.setTypeface(getResources().getFont(R.font.cairo));
                device_ip_label.setTypeface(getResources().getFont(R.font.cairo));
                wifi_status_label.setTypeface(getResources().getFont(R.font.cairo));
                espnow_status_label.setTypeface(getResources().getFont(R.font.cairo));
                choose_action_label.setTypeface(getResources().getFont(R.font.changa));
                record_button_label.setTypeface(getResources().getFont(R.font.changa));
                watch_button_label.setTypeface(getResources().getFont(R.font.changa));
                device_details_button.setTypeface(getResources().getFont(R.font.changa));
            }
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                page_label.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
                choose_action_label.setTypeface(getResources().getFont(R.font.bayon));


            }
        }

        /* SET DEVICE INFO ON PAGE LOAD */
        device_name_value.setText(DEVICE_NAME);
        device_ip_value.setText(DEVICE_IP);
        wifi_status_value.setText(WIFI_STATUS);
        espnow_status_value.setText(ESPNOW_STATUS);

        if(WIFI_STATUS.equals("CONNECTED") && ESPNOW_STATUS.equals("CONNECTED")){

            DisableEnableImage(record_button_image, true);
            DisableEnableImage(watch_button_image, true);
        }
        else if(WIFI_STATUS.equals("CONNECTED") && ESPNOW_STATUS.equals("DISCONNECTED")){
            DisableEnableImage(record_button_image, true);
            DisableEnableImage(watch_button_image, true);
        }
        else if(WIFI_STATUS.equals("DISCONNECTED") && ESPNOW_STATUS.equals("CONNECTED")){
            DisableEnableImage(record_button_image, false);
            DisableEnableImage(watch_button_image, false);
        }
        else // WIFI_STATUS.equals("DISCONNECTED") && ESPNOW_STATUS.equals("DISCONNECTED")
        {
            DisableEnableImage(record_button_image, false);
            DisableEnableImage(watch_button_image, false);
        }

        /* FIRED WHEN CLICK ON RECORD ICON */
        record_button_image.setOnLongClickListener(v -> {

            if(WIFI_STATUS.equals("CONNECTED")) {

                showLongClickDialog("RECORD", DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Long Clicked on record icon", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Device not connected ", Toast.LENGTH_SHORT).show();
            }

            return true;
        });

        /* FIRED WHEN CLICK ON WATCH ICON */
        watch_button_image.setOnClickListener(v -> {

            if(WIFI_STATUS.equals("CONNECTED")) {
                showLongClickDialog("WATCH", DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Long Clicked on watch icon", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Device not connected ", Toast.LENGTH_SHORT).show();
            }
        });

        /* FIRED WHEN CLICK ON Details Button */
        device_details_button.setOnClickListener(v -> {

        });
    }

    private void DisableEnableImage(ImageView img, boolean enable){

        if(enable){
            img.setImageAlpha(255);
            img.setClickable(true);
            img.setLongClickable(true);
        }
        else
        {
            img.setImageAlpha(60);
            img.setClickable(false);
            img.setLongClickable(false);
        }
    }

    private void showLongClickDialog(String action, String device_name){

        AlertDialog.Builder d = new AlertDialog.Builder(CameraHome1Activity.this);
        View mView            = getLayoutInflater().inflate(R.layout.dialog_camera_home_record_watch_setting, null);

        TextView device_name_label         = mView.findViewById(R.id.id_injected_name_value);
        TextView action_type_label         = mView.findViewById(R.id.injected_action_value);
        TextView last_label                = mView.findViewById(R.id.id_settings_label);
        TextView header_message            = mView.findViewById(R.id.id_setting_header_message);

        /* START OF VIDEO RECORD SECTION */
        FrameLayout record_interface_frame                                                 = mView.findViewById(R.id.id_recording_interface_frame);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch on_screen_info_switch        = mView.findViewById(R.id.id_on_screen_info_switch);
        TextView show_on_screen_info_helper                                                = mView.findViewById(R.id.id_show_on_screen_info_helper);

        LinearLayout name_date_time_layout                                                 = mView.findViewById(R.id.id_name_date_time_layout); // Visible when on_screen_info_switch is true
        RadioButton add_device_name_to_motion                                              = mView.findViewById(R.id.id_add_device_name_to_motion);
        RadioButton add_date_time_to_motion                                                = mView.findViewById(R.id.id_add_date_time_to_motion);

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch show_logo_switch             = mView.findViewById(R.id.id_show_logo_switch);
        TextView show_logo_helper                                                          = mView.findViewById(R.id.id_show_logo_helper);


        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch activate_face_detect         = mView.findViewById(R.id.id_activate_face_detection);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch activate_face_recognise      = mView.findViewById(R.id.id_activate_face_recognise);
        TextView face_detect_recognise_helper                                              = mView.findViewById(R.id.id_face_detect_recognise_helper);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch record_stream_to_application = mView.findViewById(R.id.id_record_stream_to_application);
        TextView record_stream_to_application_helper                                       = mView.findViewById(R.id.id_record_stream_to_application_helper);
        FrameLayout record_on_sd_options_frame                                             = mView.findViewById(R.id.id_record_on_sd_options_frame); // Visible when record_stream_to_application Switch is false
        TextView record_on_sd_section_label                                                = mView.findViewById(R.id.id_record_on_sd_section_label);
        TextInputLayout file_name_layout                                                   = mView.findViewById(R.id.id_file_name_layout);
        TextInputEditText file_name_input                                                  = mView.findViewById(R.id.id_file_name_input);
        TextView file_type_section_label                                                   = mView.findViewById(R.id.id_choose_record_file_type_section_label);
        RadioButton file_type_mp4_radio                                                    = mView.findViewById(R.id.id_file_type_mp4_radio);
        RadioButton file_type_mov_radio                                                    = mView.findViewById(R.id.id_file_type_mov_radio);
        FrameLayout video_type_mp4_resul                                                   = mView.findViewById(R.id.id_video_type_mp4_resul); // Visible when file_type_mp4_radio Switch is true
        TextView mp4_choose_resul_label                                                    = mView.findViewById(R.id.id_file_type_mp4_choose_resul_label);
        RadioButton file_quality_as_origenal_radio                                         = mView.findViewById(R.id.id_file_quality_as_origenal_radio);
        RadioButton file_quality_hd_radio                                                  = mView.findViewById(R.id.id_file_quality_hd_radio);
        RadioButton file_quality_4k_radio                                                  = mView.findViewById(R.id.id_file_quality_4k_radio);
        TextView mp4_choose_resul_helper                                                   = mView.findViewById(R.id.id_file_type_mp4_choose_resul_helper);
        SeekBar vide_length                                                                = mView.findViewById(R.id.id_video_max_time_length_bar);
        TextView video_max_length_number_place_holder                                      = mView.findViewById(R.id.id_video_max_length_number_place_holder);

        /* END OF VIDEO RECORD SECTION */

        /* START OF VIDEO WATCH SECTION */
        FrameLayout watch_interface_frame                                                  = mView.findViewById(R.id.id_watching_interface_frame);

        /* END OF VIDEO WATCH SECTION */

        Button save_button   = mView.findViewById(R.id.id_save_btn);
        Button reset_button  = mView.findViewById(R.id.id_reset_btn);
        Button cancel_button = mView.findViewById(R.id.id_cancel_btn);

        /* SET FONT FOR TextView & Button */
        if (LocalHelper.getLanguage(getApplicationContext()).equalsIgnoreCase("ar")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                device_name_label.setTypeface(getResources().getFont(R.font.changa));
                action_type_label.setTypeface(getResources().getFont(R.font.changa));
                last_label.setTypeface(getResources().getFont(R.font.changa));

                header_message.setTypeface(getResources().getFont(R.font.cairo));
                show_on_screen_info_helper.setTypeface(getResources().getFont(R.font.cairo));
                record_stream_to_application_helper.setTypeface(getResources().getFont(R.font.cairo));
                record_on_sd_section_label.setTypeface(getResources().getFont(R.font.cairo));
                file_name_layout.setTypeface(getResources().getFont(R.font.cairo));
                file_type_section_label.setTypeface(getResources().getFont(R.font.cairo));
                mp4_choose_resul_label.setTypeface(getResources().getFont(R.font.cairo));
                mp4_choose_resul_helper.setTypeface(getResources().getFont(R.font.cairo));

                save_button.setTypeface(getResources().getFont(R.font.changa));
                reset_button.setTypeface(getResources().getFont(R.font.changa));
                cancel_button.setTypeface(getResources().getFont(R.font.changa));
            }
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                device_name_label.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
                action_type_label.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
                last_label.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
            }
        }

        d.setView(mView);
        final AlertDialog alertDialog = d.create();
        alertDialog.setCanceledOnTouchOutside(false);

        cancel_button.setOnClickListener(cancel -> {

            show_on_screen_info = false;
            on_screen_info_switch.setChecked(false);
            name_date_time_layout.setVisibility(View.GONE); // Show name date time layout
            show_device_name    = false;
            add_device_name_to_motion.setChecked(false);
            show_date_time      = false;
            add_date_time_to_motion.setChecked(false);
            use_face_detect     = false;
            activate_face_detect.setChecked(false);
            use_face_recognise  = false;
            activate_face_recognise.setChecked(false);
            use_record_on_app   = false;
            record_stream_to_application_helper.setVisibility(View.GONE);
            use_mp4_file        = false;
            file_type_mp4_radio.setChecked(false);                // Empty mp4 radio
            video_type_mp4_resul.setVisibility(View.GONE);
            use_mov_file        = false;
            file_type_mov_radio.setChecked(false);                // Empty mov radio
            use_as_origenal     = false;
            file_quality_as_origenal_radio.setChecked(false);     // Empty as origenal radio
            use_hd              = false;
            file_quality_hd_radio.setChecked(false);              // Empty HD radio
            use_4k              = false;
            file_quality_4k_radio.setChecked(false);              // Empty 4K radio
            file_name_input.setText(""); // Empty name filed
            video_max_length = 3;

            alertDialog.dismiss();
        });

        reset_button.setOnClickListener(reset -> {

            show_on_screen_info = false;
            on_screen_info_switch.setChecked(false);
            name_date_time_layout.setVisibility(View.GONE); // Show name date time layout
            show_device_name    = false;
            add_device_name_to_motion.setChecked(false);
            show_date_time      = false;
            add_date_time_to_motion.setChecked(false);
            use_face_detect     = false;
            activate_face_detect.setChecked(false);
            use_face_recognise  = false;
            activate_face_recognise.setChecked(false);
            record_stream_to_application.setChecked(false);
            record_stream_to_application_helper.setVisibility(View.GONE);
            record_on_sd_options_frame.setVisibility(View.VISIBLE);  // Show record on SD section
            use_mp4_file        = false;
            file_type_mp4_radio.setChecked(false);                // Empty mp4 radio
            video_type_mp4_resul.setVisibility(View.GONE);
            use_mov_file        = false;
            file_type_mov_radio.setChecked(false);                // Empty mov radio
            use_as_origenal     = false;
            file_quality_as_origenal_radio.setChecked(false);     // Empty as origenal radio
            use_hd              = false;
            file_quality_hd_radio.setChecked(false);              // Empty HD radio
            use_4k              = false;
            file_quality_4k_radio.setChecked(false);              // Empty 4K radio
            file_name_input.setText(""); // Empty name filed
            video_max_length = 3;
            vide_length.setProgress(video_max_length);

        });

        device_name_label.setText(device_name);

        if(action.equals("RECORD")){

            watch_interface_frame.setVisibility(View.GONE);     // Hide watch section
            record_interface_frame.setVisibility(View.VISIBLE); // Show record section

            action_type_label.setText(R.string.word_record_first_cap_small);

            if(on_screen_info_switch.isChecked()){
                show_on_screen_info = false;
                add_device_name_to_motion.setChecked(false);       // Empty name radio
                add_date_time_to_motion.setChecked(false);         // Empty date time radio
                name_date_time_layout.setVisibility(View.GONE); // Show name date time layout
            }
            else
            {
                show_on_screen_info = false;
                add_device_name_to_motion.setChecked(false);       // Empty name radio
                add_date_time_to_motion.setChecked(false);         // Empty date time radio
                name_date_time_layout.setVisibility(View.GONE);    // Hide name date time layout
            }

            if(show_logo_switch.isChecked()){
                show_logo = false;
            }
            else
            {
                show_logo = false;
            }

            if(record_stream_to_application.isChecked()){
                record_stream_to_application_helper.setVisibility(View.GONE);
                record_on_sd_options_frame.setVisibility(View.GONE);     // Hide record on SD section
                use_record_on_app = false;
            }
            else
            {
                record_stream_to_application_helper.setVisibility(View.GONE);
                record_on_sd_options_frame.setVisibility(View.VISIBLE);  // Show record on SD section
                use_record_on_app = false;
            }

            if(activate_face_detect.isChecked()){
                use_face_detect = false;
            }
            else
            {
                use_face_detect = false;
            }

            if(activate_face_recognise.isChecked()){
                use_face_recognise = false;
            }
            else
            {
                use_face_recognise = false;
            }

            video_max_length = vide_length.getProgress();

            on_screen_info_switch.setOnClickListener(s -> {

                if(on_screen_info_switch.isChecked()){

                    show_on_screen_info = true;
                    show_device_name    = false;
                    show_date_time      = false;
                    add_device_name_to_motion.setChecked(false);       // Empty name radio
                    add_date_time_to_motion.setChecked(false);         // Empty date time radio
                    name_date_time_layout.setVisibility(View.VISIBLE); // Show name date time layout

                }
                else
                {
                    show_on_screen_info = false;
                    show_device_name    = false;
                    show_date_time      = false;
                    add_device_name_to_motion.setChecked(false);       // Empty name radio
                    add_date_time_to_motion.setChecked(false);         // Empty date time radio
                    name_date_time_layout.setVisibility(View.GONE);    // Hide name date time layout
                }
            });

            add_device_name_to_motion.setOnClickListener(r -> {
                show_device_name = true;
            });

            add_date_time_to_motion.setOnClickListener(r -> {
                show_date_time = true;
            });

            show_logo_switch.setOnClickListener(s -> {

                if(show_logo_switch.isChecked()){
                    show_logo = true;
                }
                else
                {
                    show_logo = false;
                }
            });

            activate_face_detect.setOnClickListener(s -> {

                if(activate_face_detect.isChecked()){
                    use_face_detect = true;
                }
                else
                {
                    use_face_detect = false;
                }
            });

            activate_face_recognise.setOnClickListener(s -> {

                if(activate_face_recognise.isChecked()){
                    use_face_recognise = true;
                }
                else
                {
                    use_face_recognise = false;
                }
            });

            record_stream_to_application.setOnClickListener( s -> {

                if(record_stream_to_application.isChecked()){

                    if(FilesTools.getAvailableInternalMemorySize() < 256){

                        less_than256gb = true;

                        record_stream_to_application_helper.setVisibility(View.VISIBLE);

                        Log.e(TAG, "MEMORY less than 256");

                        String error = "Your phone internal memory has no enough space to record a video, you can not record a video on phone with internal memory less than [256GB]";

                        if(FilesTools.isSDAvailable()){

                            sd_mounted = true;
                            String  sd_total_size = FilesTools.getTotalFormatedExternalMemorySize();
                            String sd_free_size   = FilesTools.getAvailableFormatedExternalMemorySize();

                            error += ", the application detected that there is a SD card with capacity of [" + sd_total_size + "] and [" + sd_free_size + "] of free space, please disable this option to use the SD card to record your video";
                        }
                        else
                        {
                            sd_mounted = false;
                            error += " and your phone has no SD card mounted, please insert SD card or you can not record videos on this phone device";
                        }

                        record_stream_to_application_helper.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                        record_stream_to_application_helper.setText(error);
                    }
                    else
                    {
                        less_than256gb    = false;

                        use_record_on_app = true;
                        use_mp4_file      = false;
                        use_mov_file      = false;
                        use_as_origenal   = false;
                        use_hd            = false;
                        use_4k            = false;
                        file_name_input.setText(""); // Empty name filed
                        record_stream_to_application_helper.setVisibility(View.VISIBLE);
                        record_on_sd_options_frame.setVisibility(View.GONE);  // Hide record on SD section
                        file_type_mp4_radio.setChecked(false);                // Empty mp4 radio
                        file_type_mov_radio.setChecked(false);                // Empty mov radio
                        file_quality_as_origenal_radio.setChecked(false);     // Empty as origenal radio
                        file_quality_hd_radio.setChecked(false);              // Empty HD radio
                        file_quality_4k_radio.setChecked(false);              // Empty 4K radio
                    }
                }
                else
                {
                    if(less_than256gb){
                        use_record_on_app = false;
                    }

                    if(!sd_mounted) {

                        record_interface_frame.setVisibility(View.GONE);
                        reset_button.setVisibility(View.GONE);
                        save_button.setText(R.string.button_watch_first_cap_small);

                        header_message.setText(R.string.internal_memory_less_than_256gb_end_dialog);

                        /* Save button now is [Watch] */
                        save_button.setOnClickListener(watch -> {

                            alertDialog.dismiss();

                            Intent intent = new Intent(CameraHome1Activity.this, CameraHomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        });

                        cancel_button.setOnClickListener(watch -> {

                            alertDialog.dismiss();

                            Intent intent = new Intent(CameraHome1Activity.this, ServerHomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                            finish();

                        });
                    }
                    else
                    {
                        use_record_on_app = false;
                        use_mp4_file      = false;
                        use_mov_file      = false;
                        use_as_origenal   = false;
                        use_hd            = false;
                        use_4k            = false;

                        file_name_input.setText(""); // Empty name filed
                        record_stream_to_application_helper.setVisibility(View.GONE);
                        record_on_sd_options_frame.setVisibility(View.VISIBLE);  // Show record on SD section
                        file_type_mp4_radio.setChecked(false);                   // Empty mp4 radio
                        file_type_mov_radio.setChecked(false);                   // Empty mov radio
                        file_quality_as_origenal_radio.setChecked(false);        // Empty as origenal radio
                        file_quality_as_origenal_radio.setChecked(false);        // Empty as origenal radio
                        file_quality_hd_radio.setChecked(false);                 // Empty HD radio
                        file_quality_4k_radio.setChecked(false);                 // Empty 4K radio
                    }
                }
            });

            file_type_mp4_radio.setOnClickListener( r -> {

                use_mp4_file = true;
                use_mov_file = false;
                file_type_mov_radio.setChecked(false);
                video_type_mp4_resul.setVisibility(View.VISIBLE);
                file_quality_as_origenal_radio.setChecked(false); // Empty as origenal radio
                file_quality_hd_radio.setChecked(false);          // Empty HD radio
                file_quality_4k_radio.setChecked(false);          // Empty 4K radio
            });

            file_type_mov_radio.setOnClickListener( r -> {

                use_mov_file = true;
                use_mp4_file = false;
                file_type_mp4_radio.setChecked(false);
                video_type_mp4_resul.setVisibility(View.GONE);
                file_quality_as_origenal_radio.setChecked(false); // Empty as origenal radio
                file_quality_hd_radio.setChecked(false);          // Empty HD radio
                file_quality_4k_radio.setChecked(false);          // Empty 4K radio
            });

            file_quality_as_origenal_radio.setOnClickListener( r -> {
                use_as_origenal = true;
                use_hd          = false;
                file_quality_hd_radio.setChecked(false);
                use_4k          = false;
                file_quality_4k_radio.setChecked(false);
            });

            file_quality_hd_radio.setOnClickListener( r -> {
                use_as_origenal = false;
                file_quality_as_origenal_radio.setChecked(false);
                use_hd          = true;
                use_4k          = false;
                file_quality_4k_radio.setChecked(false);
            });

            file_quality_4k_radio.setOnClickListener( r -> {
                use_as_origenal = false;
                file_quality_as_origenal_radio.setChecked(false);
                use_hd          = false;
                file_quality_hd_radio.setChecked(false);
                use_4k          = true;
            });

            vide_length.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    video_max_length = progress;
                    video_max_length_number_place_holder.setText(String.valueOf(video_max_length));

                    Log.d(TAG, "Video Max Length:      " +  video_max_length);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            save_button.setOnClickListener(save -> {

                if (show_on_screen_info) {

                    if (!show_device_name && !show_date_time) {
                        show_on_screen_info_helper.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                        show_on_screen_info_helper.setText(R.string.no_name_no_date_time_chosen);
                        return;
                    }
                }

                if (use_record_on_app) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault(Locale.Category.FORMAT));
                    file_name    = device_name + "_" + dateFormat.format(new Date()); // Find today's date
                    use_mov_file = true;
                    use_mp4_file = false;
                    use_hd = true;
                }
                else
                {
                    if(FilesTools.isSDAvailable()) {

                        file_name = Objects.requireNonNull(file_name_input.getText()).toString();

                        if (file_name.isEmpty()) {

                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault(Locale.Category.FORMAT));
                            file_name = device_name + "_" + dateFormat.format(new Date()); // Find today's date
                        }
                        else
                        {
                            String validate_name = Tools.validateDeviceName(file_name_input, 6, 10);

                            if (validate_name.equals("MIN_ERROR")) {
                                file_name_layout.setError(getApplicationContext().getResources().getText(R.string.file_name_min_error));
                                return;
                            }

                            if (validate_name.equals("MAX_ERROR")) {
                                file_name_layout.setError(getApplicationContext().getResources().getText(R.string.file_name_max_error));
                                return;
                            }
                        }
                    }
                    else
                    {


                        return;
                    }
                }

                Log.d(TAG, "Device Name:        " + device_name);
                Log.d(TAG, "Show Logo:          " + show_logo);
                Log.d(TAG, "Action:             " + action);
                Log.d(TAG, "On screen:          " + show_on_screen_info);
                Log.d(TAG, "Show Name:          " + show_device_name);
                Log.d(TAG, "Show Date Time:     " + show_date_time);
                Log.d(TAG, "Use face detect:    " + use_face_detect);
                Log.d(TAG, "Use face recognise: " + use_face_recognise);
                Log.d(TAG, "Record in app:      " + use_record_on_app);
                Log.d(TAG, "File name:          " + file_name);
                Log.d(TAG, "Use MP4:            " + use_mp4_file);
                Log.d(TAG, "Use MOV:            " + use_mov_file);
                Log.d(TAG, "As origenal:        " + use_as_origenal);
                Log.d(TAG, "Use HD:             " + use_hd);
                Log.d(TAG, "Use 4K:             " + use_4k);
                Log.d(TAG, "Max length:         " + video_max_length);

                alertDialog.dismiss();

                Intent intent = new Intent(CameraHome1Activity.this, CameraRecordActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                intent.putExtra("device_id", DEVICE_ID);
                intent.putExtra("device_name", device_name);
                intent.putExtra("action", action);
                intent.putExtra("wifi_status", WIFI_STATUS);
                intent.putExtra("espnow_status", ESPNOW_STATUS);
                intent.putExtra("device_ip", DEVICE_IP);
                intent.putExtra("show_on_screen_info", show_on_screen_info);
                intent.putExtra("show_device_name", show_device_name);
                intent.putExtra("show_date_time", show_date_time);
                intent.putExtra("show_logo", show_logo);
                intent.putExtra("use_face_detect", use_face_detect);
                intent.putExtra("use_face_recognise", use_face_recognise);
                intent.putExtra("use_record_on_app", use_record_on_app);
                intent.putExtra("file_name", file_name);
                intent.putExtra("use_mp4_file", use_mp4_file);
                intent.putExtra("use_mov_file", use_mov_file);
                intent.putExtra("use_as_origenal", use_as_origenal);
                intent.putExtra("use_hd", use_hd);
                intent.putExtra("use_4k", use_4k);
                intent.putExtra("video_max_length", video_max_length);

                startActivity(intent);
                finish();
            });
        }

        if(action.equals("WATCH")){

            record_interface_frame.setVisibility(View.GONE);     // Hide record section
            watch_interface_frame.setVisibility(View.VISIBLE);   // Show watch section
            action_type_label.setText(R.string.word_watch_first_cap_small);

            save_button.setOnClickListener(save -> {


                Intent intent = new Intent(CameraHome1Activity.this, CameraRecordActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        }

        if(!alertDialog.isShowing()){
            alertDialog.show();
        }
    }
}