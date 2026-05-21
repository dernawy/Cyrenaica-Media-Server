package com.cyrenaica.cyrenaicaserver.nodes.Cameras;

import static androidx.media3.common.C.VIDEO_SCALING_MODE_SCALE_TO_FIT;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.FileDataSource;
import androidx.media3.exoplayer.ExoPlayer;

import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.PlayerView;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.SessionState;
import com.cyrenaica.cyrenaicaserver.MainActivity;
import com.cyrenaica.cyrenaicaserver.R;
import com.cyrenaica.cyrenaicaserver.language.LocalHelper;
import com.cyrenaica.cyrenaicaserver.tools.FFmpegTools;
import com.cyrenaica.cyrenaicaserver.tools.FilesTools;
import com.cyrenaica.cyrenaicaserver.tools.customs.FfmpegColorAdapter;
import com.cyrenaica.cyrenaicaserver.tools.customs.FfmpegColors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;


public class CameraRecordActivity extends AppCompatActivity {

    private int REQUEST_LOGO_CODE = 1000;

    private static final String TAG = "CAMERA_RECORD";

    FFmpegKitConfig ffmpegConfig;

    private FfmpegColorAdapter text_color_adapter;

    static ArrayList<FfmpegColors> _device_name_color_list = new ArrayList<FfmpegColors>();
    static ArrayList<FfmpegColors> _date_time_color_list = new ArrayList<FfmpegColors>();
    private static FfmpegColors deviceNameColorList;
    private static FfmpegColors dateTimeColorList;

    private int DEVICE_ID;
    private String DEVICE_NAME;
    private String DEVICE_IP;
    private String WIFI_STATUS;
    private String ESPNOW_STATUS;

    String action;
    boolean show_on_screen_info;
    boolean show_device_name;
    boolean show_date_time;
    boolean show_logo;
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

    /* ON SCREEN TEXT */
    String _font_file_name;
    String _font_size;
    String _font_color;
    String _text_x;
    String _text_y;

    TextView device_name;
    TextView date_time;
    PlayerView record_player;
    ExoPlayer player;
    FrameLayout record_control_frame;
    ImageView record_button;
    ImageView stop_button;
    FrameLayout advanced_settings_frame;

    TextView video_source_size_label;
    Spinner video_source_size_spinner;
    Button video_source_size_not_know_btn;
    FrameLayout resolution_message_frame;
    LinearLayout resolution_message_layout;
    TextView video_source_resolution_msg_label;
    TextView video_source_resolution_msg_value;


    TextView advanced_settings_label;
    ImageView sub_expand_icon;
    CardView base_cardview;
    LinearLayout hidden_view;
    TextView device_name_settings_label;
    FrameLayout device_name_settings_frame;
    TextView device_name_font_list_label;
    Spinner device_name_font_list_spinner;
    TextView device_name_font_size_label;
    Spinner device_name_font_size_spinner;
    Spinner device_name_text_color_spinner;

    View device_name_setting_divider;

    TextView date_time_settings_label;
    FrameLayout date_time_settings_frame;
    TextView date_time_font_list_label;
    Spinner date_time_font_list_spinner;
    TextView date_time_font_size_label;
    Spinner date_time_font_size_spinner;
    Spinner date_time_text_color_spinner;
    View date_time_setting_divider;

    TextView logo_settings_label;
    FrameLayout logo_settings_frame;
    TextView logo_label;
    ImageView load_logo_icon;
    TextView  logo_position_label;
    Spinner logo_position_spinner;



    Button save_settings;
    Button cancel_settings;

    FrameLayout files_control_frame;
    TextView files_control_message;
    Button watch_recorded_vid_btn;
    Button no_watch_recorded_vid_btn;


    String ffmpeg_device_name;
    String ffmpeg_time_stamp;
    String ffmpeg_video_source_resolution;
    String device_name_font;
    String device_name_font_path;
    String device_name_text_size;
    String device_name_text_color;
    String date_time_font;
    String date_time_font_path;
    String date_time_text_size;
    String date_time_text_color;


    String logo_position;
    String logo_file_name;
    String logo_saved_path; // to pas to ffmpeg command

    String video_file_type;

    boolean save_settings_ok_flag = false;

    long sessionId;
    boolean PLAY_STARTED;

    SessionState ffmpeg_state;
    ReturnCode ffmpeg_return_code;
    double ffmpeg_current_time;

    protected static final Handler handler = new Handler(Looper.getMainLooper());

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera_record);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* GET DEVICE INFO FROM Intent */
        DEVICE_ID      = getIntent().getIntExtra("device_id", -1);
        DEVICE_NAME    = getIntent().getStringExtra("device_name");
        DEVICE_IP      = getIntent().getStringExtra("device_ip");
        WIFI_STATUS    = getIntent().getStringExtra("wifi_status");
        ESPNOW_STATUS  = getIntent().getStringExtra("espnow_status");

        device_name                       = findViewById(R.id.id_camera_record_device_name);
        date_time                         = findViewById(R.id.id_camera_record_date_time);
        video_source_size_label           = findViewById(R.id.id_camera_record_video_source_size_label);
        video_source_size_spinner         = findViewById(R.id.id_camera_record_video_source_size_spinner);
        video_source_size_not_know_btn    = findViewById(R.id.id_camera_record_video_source_size_not_know_btn);
        resolution_message_frame          = findViewById(R.id.id_camera_record_resolution_message_frame);
        resolution_message_layout         = findViewById(R.id.id_camera_record_resolution_message_layout);
        video_source_resolution_msg_label = findViewById(R.id.id_camera_record_video_source_resolution_msg_label);
        video_source_resolution_msg_value = findViewById(R.id.id_camera_record_video_source_resolution_msg_value);

        record_control_frame              = findViewById(R.id.id_camera_record_control_frame);
        record_button                     = findViewById(R.id.id_video_record_button);
        stop_button                       = findViewById(R.id.id_video_stop_button);
        advanced_settings_frame           = findViewById(R.id.id_camera_record_advanced_settings_frame);
        advanced_settings_label           = findViewById(R.id.id_camera_record_advanced_settings_label);
        sub_expand_icon                   = findViewById(R.id.id_camera_record_advanced_settings_expand_arrow);
        base_cardview                     = findViewById(R.id.id_camera_record_base_cardview);
        hidden_view                       = findViewById(R.id.id_camera_record_is_hidden_layout);

        device_name_settings_label        = findViewById(R.id.id_camera_record_device_name_setting_label);
        device_name_settings_frame        = findViewById(R.id.id_camera_record_device_name_font_settings_frame);
        device_name_font_list_label       = findViewById(R.id.id_camera_record_device_name_font_list_label);
        device_name_font_list_spinner     = findViewById(R.id.id_camera_record_device_name_font_list_spinner);
        device_name_font_size_label       = findViewById(R.id.id_camera_record_device_name_font_size_label);
        device_name_font_size_spinner     = findViewById(R.id.id_camera_record_device_name_font_size_spinner);

        device_name_text_color_spinner    = findViewById(R.id.id_camera_record_device_name_font_color_spinner);

        device_name_setting_divider       = findViewById(R.id.id_device_name_setting_divider);

        date_time_settings_label          = findViewById(R.id.id_camera_record_date_and_time_setting_label);
        date_time_settings_frame          = findViewById(R.id.id_camera_record_date_and_time_font_settings_frame);
        date_time_font_list_label         = findViewById(R.id.id_camera_record_date_and_time_font_list_label);
        date_time_font_list_spinner       = findViewById(R.id.id_camera_record_date_and_time_font_list_spinner);
        date_time_font_size_label         = findViewById(R.id.id_camera_record_date_and_time_font_size_label);
        date_time_font_size_spinner       = findViewById(R.id.id_camera_record_date_and_time_font_size_spinner);

        date_time_text_color_spinner       = findViewById(R.id.id_camera_record_date_and_time_font_color_spinner);

        date_time_setting_divider         = findViewById(R.id.id_date_time_setting_divider);

        logo_settings_label               = findViewById(R.id.id_logo_settings_label);
        logo_settings_frame               = findViewById(R.id.id_camera_record_logo_settings_frame);
        logo_label                        = findViewById(R.id.id_camera_record_logo_label);
        load_logo_icon                    = findViewById(R.id.id_camera_record_load_logo_icon);
        logo_position_label               = findViewById(R.id.id_camera_record_logo_position_label);
        logo_position_spinner             = findViewById(R.id.id_camera_record_logo_position_spinner);

        record_player                     = findViewById(R.id.id_camera_record_player_view);
        save_settings                     = findViewById(R.id.id_camera_record_save_settings_save_btn);
        cancel_settings                   = findViewById(R.id.id_camera_record_save_settings_cancel_btn);

        files_control_frame               = findViewById(R.id.id_camera_record_files_control_frame);
        files_control_message             = findViewById(R.id.id_camera_record_files_control_message);
        watch_recorded_vid_btn            = findViewById(R.id.id_camera_record_watch_hls_btn);
        no_watch_recorded_vid_btn         = findViewById(R.id.id_camera_record_no_watch_hls_btn);

        if(WIFI_STATUS.isEmpty() || !WIFI_STATUS.equals("CONNECTED")){
            Log.d(TAG, "wifi error:        " + WIFI_STATUS);
            return;
        }

        assert ESPNOW_STATUS != null;
        if(ESPNOW_STATUS.isEmpty() || !ESPNOW_STATUS.equals("CONNECTED")){
            Log.d(TAG, "espnow error:        " + WIFI_STATUS);
        }

        action              = getIntent().getStringExtra("action");

        assert action != null;
        if(action.isEmpty() || !action.equals("RECORD")){
            Log.d(TAG, "action error:        " + action);
            return;
        }

        show_on_screen_info = getIntent().getBooleanExtra("show_on_screen_info", false);
        show_device_name    = getIntent().getBooleanExtra("show_device_name", false);
        show_date_time      = getIntent().getBooleanExtra("show_date_time", false);

        show_logo           = getIntent().getBooleanExtra("show_logo", false);

        use_face_detect     = getIntent().getBooleanExtra("use_face_detect", false);
        use_face_recognise  = getIntent().getBooleanExtra("show_on_screen_info", false);
        use_record_on_app   = getIntent().getBooleanExtra("use_record_on_app", false);
        file_name           = getIntent().getStringExtra("file_name");
        use_mp4_file        = getIntent().getBooleanExtra("use_mp4_file", false);
        use_mov_file        = getIntent().getBooleanExtra("use_mov_file", false);
        use_as_origenal     = getIntent().getBooleanExtra("use_as_origenal", false);
        use_hd              = getIntent().getBooleanExtra("use_hd", false);
        use_4k              = getIntent().getBooleanExtra("use_4k", false);
        video_max_length    = getIntent().getIntExtra("video_max_length", 3);

        /* This lines is important */
        FilesTools.setContext(getApplicationContext());

        device_name.setText(DEVICE_NAME);
        date_time.setText(FFmpegTools.getTimeStamp("D"));

        FilesTools.setDeviceName(DEVICE_NAME);
        FFmpegTools.setDeviceName(DEVICE_NAME);
        FFmpegTools.setCameraIp(DEVICE_IP);

        String live_logo_path = getApplicationContext().getDir("cyrenaicaVid", Context.MODE_PRIVATE).getPath() + "/hls/ic_live-stream.png";
        FFmpegTools.setLiveLogoPath(live_logo_path);

        /* Format the device name for ffmpeg | DEVICE_NAME */
        FFmpegTools.setFormatedDeviceName(DEVICE_NAME);

        try {
            FilesTools.registerApplicationFonts();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        /* Settings arrow */
        sub_expand_icon.setOnClickListener(view -> {

            // If the CardView is already expanded, set its visibility
            // to gone and change the expand less icon to expand more.
            if (hidden_view.getVisibility() == View.GONE) {

                // The transition of the hiddenView is carried out by the TransitionManager class.
                // Here we use an object of the AutoTransition Class to create a default transition
                TransitionManager.beginDelayedTransition(base_cardview, new AutoTransition());
                hidden_view.setVisibility(View.VISIBLE);
                sub_expand_icon.setImageResource(R.drawable.ic_expand_less_orange_24);
            }

            // If the CardView is not expanded, set its visibility to
            // visible and change the expand more icon to expand less.
            else
            {
                TransitionManager.beginDelayedTransition(base_cardview, new AutoTransition());
                hidden_view.setVisibility(View.GONE);
                sub_expand_icon.setImageResource(R.drawable.ic_expand_more_orange_24);
            }

        });

        /* Add camera source resolution spinner */
        ArrayAdapter<String> video_source_size = new ArrayAdapter<String>(getApplicationContext(), R.layout.camera_record_font_spinner_item, FFmpegTools.VIDEO_SOURCE_RESOLUTIONS);
        video_source_size.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        video_source_size_spinner.setAdapter(video_source_size);

        /* Camera video source size */
        video_source_size_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(i > 0){
                    ffmpeg_video_source_resolution = adapterView.getItemAtPosition(i).toString();
                    resolution_message_layout.setVisibility(View.VISIBLE);
                    video_source_resolution_msg_value.setText(ffmpeg_video_source_resolution);

                    FFmpegTools.parseVideoResolution(ffmpeg_video_source_resolution);
                    FFmpegTools.setCameraUrl(); // Prepare the camera link with source video size for ffmpeg command

                    Log.d(TAG, "FFMPEG COMMAND:       " + FFmpegTools.getFFmpegCommand());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /* Don't know button */
        video_source_size_not_know_btn.setOnClickListener(unknown -> {

            if (resolution_message_layout.getVisibility() == View.VISIBLE) {

                // The transition of the hiddenView is carried out by the TransitionManager class.
                // Here we use an object of the AutoTransition Class to create a default transition
                TransitionManager.beginDelayedTransition(resolution_message_frame, new AutoTransition());
                resolution_message_layout.setVisibility(View.GONE);
            }

            ffmpeg_video_source_resolution = "480x320";
            video_source_resolution_msg_value.setText(ffmpeg_video_source_resolution);
            resolution_message_layout.setVisibility(View.VISIBLE);

            FFmpegTools.setCameraSourceVideoW("480");
            FFmpegTools.setCameraSourceVideoH("320");

        });

        /* If user chosen to record video on phone memory */
        if(use_record_on_app){

            // prepare the disk to record
            FilesTools.verifyStoragePermissions(this);

            // - check the available space on internal disk
            if(FilesTools.getAvailableInternalMemorySize() >= 256){

                // - check if data/VIDEOS path exist or not - if false
                if(!FilesTools.isAppInternalDirExist(FilesTools.IN_FOLDER_TYPE_VIDEOS_PATH)){

                    // - Create path data/VIDEOS  - if false
                    if(!FilesTools.createFolder(FilesTools.SD_FOLDER_TYPE_VIDEOS_PATH, FilesTools.IN)) {
                        Log.d(TAG, "IN -  Error while creating the folder [" + FilesTools.IN_FOLDER_TYPE_VIDEOS_PATH + "]");
                        return;
                    }
                }

                // - check if data/VIDEOS/[DEVICE_NAME] path exist or not - if true
                if(!FilesTools.isAppInternalDirExist(FilesTools.IN_FOLDER_TYPE_VIDEOS_PATH + "/" + DEVICE_NAME)){

                    // - Create path data/VIDEOS/[DEVICE_NAME] - if false
                    if(!FilesTools.createFolder(FilesTools.IN_FOLDER_TYPE_VIDEOS_PATH + "/" + DEVICE_NAME, FilesTools.IN)){
                        Log.d(TAG, "IN -  Error while creating the folder [" + DEVICE_NAME + "]");
                        return;
                    }
                }
            }
            else
            {
                //TODO: show message on UI telling the user that the hard drive les than 256 GB
                return;
            }
        }

        if(show_on_screen_info){

            String[] fonts_names = FilesTools.getAllFontsNames();//fonts names list

            if(show_device_name){

                try {

                    String[] fonts_files = FilesTools.getAllFontsFileInAssets(getApplicationContext()); //fonts files list

                    FFmpegTools.setFontsList(fonts_files);

                    ArrayAdapter<String> font_adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.camera_record_font_spinner_item, fonts_names);
                    font_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    device_name_font_list_spinner.setAdapter(font_adapter);

                    device_name_font      = FilesTools.getAllFontsNamesSmalls()[0]; // fonts files -> if user did not click and select other font
                    device_name_font_path = FilesTools.getFontsPath() + device_name_font;

                    FFmpegTools.setDeviceNameFont(device_name_font);
                    FFmpegTools.setDeviceNameFontPath(device_name_font_path);

                    device_name_font_list_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {

                            device_name_font = FilesTools.getAllFontsNamesSmalls()[i]; // fonts names

                            FFmpegTools.setDeviceNameFontPath(device_name_font);

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String[] fonts_size = {"12", "14", "16", "18", "20", "22", "24", "26", "28", "30"};
                ArrayAdapter<String> font_size_adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.camera_record_font_spinner_item, fonts_size);
                font_size_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                device_name_font_size_spinner.setAdapter(font_size_adapter);

                device_name_text_size = fonts_size[0];
                FFmpegTools.setDeviceNameFontSize(device_name_text_size);

                device_name_font_size_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {
                        device_name_text_size = parent.getItemAtPosition(i).toString();

                        FFmpegTools.setDeviceNameFontSize(device_name_text_size);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                /* Device name text color spinner */
                _device_name_color_list.clear();
                text_color_adapter             = new FfmpegColorAdapter(CameraRecordActivity.this, _device_name_color_list);
                device_name_text_color_spinner = findViewById(R.id.id_camera_record_device_name_font_color_spinner);
                device_name_text_color_spinner.setAdapter(text_color_adapter);

                Field[] fields = null;

                try {
                    fields = Class.forName(getPackageName()+".R$color").getDeclaredFields();
                }
                catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                FFmpegTools.getTextColorSpinner(getApplicationContext(), fields, text_color_adapter, deviceNameColorList, _device_name_color_list);

                device_name_text_color = "#";
                device_name_text_color += _device_name_color_list.get(0).getCode();
                FFmpegTools.setDeviceNameFontColor(device_name_text_color);

                device_name_text_color_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {

                        device_name_text_color = "#";
                        device_name_text_color += _device_name_color_list.get(i).getCode();

                        FFmpegTools.setDeviceNameFontColor(device_name_text_color);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                device_name_settings_label.setVisibility(View.VISIBLE);
                device_name_settings_frame.setVisibility(View.VISIBLE);
                device_name_setting_divider.setVisibility(View.VISIBLE);

                ffmpeg_device_name = FFmpegTools.getFormatedDeviceName();
            }

            if(show_date_time){

                try {

                    String[] fonts_files = FilesTools.getAllFontsFileInAssets(getApplicationContext()); //fonts files list

                    FFmpegTools.setFontsList(fonts_files);

                    ArrayAdapter<String> font_adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.camera_record_font_spinner_item, fonts_names);
                    font_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    date_time_font_list_spinner.setAdapter(font_adapter);

                    date_time_font      = FilesTools.getAllFontsNamesSmalls()[0]; // fonts files -> if user did not click and select other font
                    date_time_font_path = FilesTools.getFontsPath() + date_time_font;

                    FFmpegTools.setDateTimeFont(date_time_font);
                    FFmpegTools.setDateTimeFontPath(date_time_font);

                    date_time_font_list_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {

                            date_time_font      = FilesTools.getAllFontsNamesSmalls()[i]; // fonts files

                            FFmpegTools.setDateTimeFontPath(date_time_font);

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String[] fonts_size = {"12", "14", "16", "18", "20", "22", "24", "26", "28", "30"};
                ArrayAdapter<String> font_size_adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.camera_record_font_spinner_item, fonts_size);
                font_size_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                date_time_font_size_spinner.setAdapter(font_size_adapter);

                date_time_text_size = fonts_size[0];
                FFmpegTools.setDateTimeFontSize(date_time_text_size);

                date_time_font_size_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {
                        date_time_text_size  = parent.getItemAtPosition(i).toString();

                        FFmpegTools.setDateTimeFontSize(date_time_text_size);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                /* Date time text color spinner */
                _date_time_color_list.clear();
                text_color_adapter           = new FfmpegColorAdapter(CameraRecordActivity.this, _date_time_color_list);
                date_time_text_color_spinner = findViewById(R.id.id_camera_record_date_and_time_font_color_spinner);
                date_time_text_color_spinner.setAdapter(text_color_adapter);

                Field[] fields = null;

                try {
                    fields = Class.forName(getPackageName()+".R$color").getDeclaredFields();
                }
                catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                FFmpegTools.getTextColorSpinner(getApplicationContext(), fields, text_color_adapter, dateTimeColorList, _date_time_color_list);

                date_time_text_color = "#";
                date_time_text_color += _date_time_color_list.get(0).getCode();
                FFmpegTools.setDateTimeFontColor(date_time_text_color);

                date_time_text_color_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {
                        date_time_text_color = "#";
                        date_time_text_color += _date_time_color_list.get(i).getCode();

                        FFmpegTools.setDateTimeFontColor(date_time_text_color);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                date_time_settings_label.setVisibility(View.VISIBLE);
                date_time_settings_frame.setVisibility(View.VISIBLE);
                date_time_setting_divider.setVisibility(View.VISIBLE);

                ffmpeg_time_stamp = FFmpegTools.getTimeStamp("D");
            }
        }

        if(show_logo){

            logo_file_name = "logo.png";

            load_logo_icon.setOnClickListener(logo -> {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");

                startActivityForResult(intent, REQUEST_LOGO_CODE);

            });

            String[] logo_position_list = {"UP RIGHT", "DOWN RIGHT"};
            ArrayAdapter<String> text_position_adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.camera_record_font_spinner_item, logo_position_list);
            text_position_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            logo_position_spinner.setAdapter(text_position_adapter);

            logo_position_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {
                    logo_position = parent.getItemAtPosition(i).toString();

                    FFmpegTools.setLogoSavedPosition(logo_position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            FFmpegTools.setLogoW("40");
            FFmpegTools.setLogoH("40");

            FFmpegTools.setLiveLogoW("100");
            FFmpegTools.setLiveLogoH("80");

            logo_settings_label.setVisibility(View.VISIBLE);
            logo_settings_frame.setVisibility(View.VISIBLE);
        }

        if(use_mp4_file){

            if(use_as_origenal){

                FFmpegTools.setVideoOutputW("480");
                FFmpegTools.setVideoOutputH("320");
            }

            if(use_hd){
                FFmpegTools.setVideoOutputW("1920");
                FFmpegTools.setVideoOutputH("1080");
            }

            if(use_4k){
                FFmpegTools.setVideoOutputW("3840");
                FFmpegTools.setVideoOutputH("2160");
            }

            video_file_type = ".mp4";
        }

        if(use_mov_file){
            FFmpegTools.setVideoOutputW("1280");
            FFmpegTools.setVideoOutputH("720");

            video_file_type = ".mov";
        }

        if(use_face_detect){

        }

        if(use_face_recognise){

        }

        if(!show_on_screen_info && !show_logo){
            advanced_settings_frame.setVisibility(View.GONE);
        }

        /* IMPLEMENTATION */

        FFmpegTools.setCameraUrl(); // Prepare the camera link with source video size for ffmpeg command

        /* get the formated device name for ffmpeg 'DEVICE_NAME' */
        String formated_device_name = FFmpegTools.getFormatedDeviceName();

        /* If user chosen to record video on phone memory */
        if(use_record_on_app){

        }
        else
        {
            if(!FilesTools.isSDParentDirExist(FilesTools.SD_FOLDER_TYPE_VIDEOS_PATH)){

                if(!FilesTools.createFolder(FilesTools.SD_FOLDER_TYPE_VIDEOS_PATH, FilesTools.EX)){
                    Log.d(TAG, "EX -  Error while creating the folder [" + FilesTools.SD_FOLDER_TYPE_VIDEOS_PATH + "]");
                }
            }

            if(FilesTools.isSDDirExist(DEVICE_NAME, FilesTools.SD_FOLDER_TYPE_VIDEOS) != 0){

                if(!FilesTools.createFolder(FilesTools.SD_FOLDER_TYPE_VIDEOS_PATH + "/" + DEVICE_NAME, FilesTools.EX)){
                    Log.d(TAG, "EX -  Error while creating the folder [" + DEVICE_NAME + "]");
                }
            }

            if(!FilesTools.isSDParentDirExist(FilesTools.SD_FOLDER_TYPE_IMAGES_PATH)){

                if(!FilesTools.createFolder(FilesTools.SD_FOLDER_TYPE_IMAGES_PATH, FilesTools.EX)){
                    Log.d(TAG, "EX -  Error while creating the folder [" + FilesTools.SD_FOLDER_TYPE_IMAGES_PATH + "]");
                }
            }

            if(FilesTools.isSDDirExist(DEVICE_NAME, FilesTools.SD_FOLDER_TYPE_IMAGES) != 0){

                if(!FilesTools.createFolder(FilesTools.SD_FOLDER_TYPE_IMAGES_PATH + "/" + DEVICE_NAME, FilesTools.EX)){
                    Log.d(TAG, "EX -  Error while creating the folder [" + DEVICE_NAME + "]");
                }
            }

            /* Create dir VIDEOS/DEVICE_NAME/[VIDEOS_SAVE] */
            if(FilesTools.isSDDirExist(DEVICE_NAME + "/"  + FilesTools.SD_FOLDER_TYPE_VIDEOS_SAVE_PATH , FilesTools.SD_FOLDER_TYPE_VIDEOS) != 0){

                if(!FilesTools.createFolder(FilesTools.SD_FOLDER_TYPE_VIDEOS_PATH + "/" + DEVICE_NAME + "/" + FilesTools.SD_FOLDER_TYPE_VIDEOS_SAVE_PATH, FilesTools.EX)){
                    Log.d(TAG, "EX -  Error while creating the folder [" + FilesTools.SD_FOLDER_TYPE_VIDEOS_SAVE_PATH + "]");
                }
            }

            /* Create dir VIDEOS/DEVICE_NAME/[hls] */
            if(FilesTools.isSDDirExist(DEVICE_NAME + "/"  + FilesTools.SD_FOLDER_TYPE_HLS_PATH , FilesTools.SD_FOLDER_TYPE_VIDEOS) != 0){

                if(!FilesTools.createFolder(FilesTools.SD_FOLDER_TYPE_VIDEOS_PATH + "/" + DEVICE_NAME + "/" + FilesTools.SD_FOLDER_TYPE_HLS_PATH, FilesTools.EX)){
                    Log.d(TAG, "EX -  Error while creating the folder [" + FilesTools.SD_FOLDER_TYPE_HLS_PATH + "]");
                }
            }

            FilesTools.deleteHlsContent();

            if(show_logo){

                if(FilesTools.isSDDirExist(DEVICE_NAME + "/" + FilesTools.SD_FOLDER_TYPE_LOGOS_PATH, FilesTools.SD_FOLDER_TYPE_IMAGES) != 0){

                    if(!FilesTools.createFolder(FilesTools.SD_FOLDER_TYPE_IMAGES_PATH + "/" + DEVICE_NAME + "/" + FilesTools.SD_FOLDER_TYPE_LOGOS_PATH, FilesTools.EX)){
                        Log.d(TAG, "EX -  Error while creating the folder [" + FilesTools.SD_FOLDER_TYPE_LOGOS_PATH + "]");
                    }
                }
            }
        }



        save_settings.setOnClickListener(save -> {

            /* Check camera source if set */
            if(FFmpegTools.getCameraSourceVideoW() == null || FFmpegTools.getCameraSourceVideoH() == null){
                showErrorDialog("SOURCE_RESOLUTION", "Camera source resolution error");
                save_settings_ok_flag = false;
                return;
            }

            /* Check device name and date time all settings if set */
            if(show_on_screen_info){

                if(show_device_name){

                    if(FFmpegTools.getDeviceNameFont() == null){

                        showErrorDialog("DEVICE_NAME_FONT", "Device name font error");
                        save_settings_ok_flag = false;
                        return;

                    }

                    if(FFmpegTools.getDeviceNameFontPath() == null){
                        showErrorDialog("DEVICE_NAME_FONT_PATH", "Device name font path error");
                        save_settings_ok_flag = false;
                        return;
                    }

                    if(FFmpegTools.getDeviceNameFontSize() == null){
                        showErrorDialog("DEVICE_NAME_FONT_SIZE", "Device name font size error");
                        save_settings_ok_flag = false;
                        return;
                    }

                    if(FFmpegTools.getDeviceNameFontColor() == null){
                        showErrorDialog("DEVICE_NAME_FONT_COLOR", "Device name font color error");
                        save_settings_ok_flag = false;
                        return;
                    }
                }

                if(show_date_time){

                    if(FFmpegTools.getDateTimeFont() == null){
                        showErrorDialog("DATE_TIME_FONT", "Date (and/or) Time font error");
                        save_settings_ok_flag = false;
                        return;
                    }

                    if(FFmpegTools.getDateTimeFontPath() == null){
                        showErrorDialog("DATE_TIME_FONT_PATH", "Date (and/or) Time font path error");
                        save_settings_ok_flag = false;
                        return;
                    }

                    if(FFmpegTools.getDateTimeFontSize() == null){
                        showErrorDialog("DATE_TIME_FONT_SIZE", "Date (and/or) Time font size error");
                        save_settings_ok_flag = false;
                        return;
                    }

                    if(FFmpegTools.getDateTimeFontColor() == null){
                        showErrorDialog("DATE_TIME_FONT_COLOR", "Date (and/or) Time font color error");
                        save_settings_ok_flag = false;
                        return;
                    }
                }
            }

            /* Check logo if chosen */
            if(show_logo){

                String tag = load_logo_icon.getTag().toString();

                if(tag.equals("NO")){ // User didn't chosen a logo - show error message or ask the user to disable the logo settings and continue

                    showErrorDialog("LOGO_ABSENT", "Logo absent error");
                    save_settings_ok_flag = false;
                    return;
                }

                if(logo_position == null){

                    showErrorDialog("LOGO_POSITION", "Logo position error");
                    save_settings_ok_flag = false;
                    return;
                }
            }

            FFmpegTools.setFFmpegCommand(FFmpegTools.getFormatedDeviceName(), ffmpeg_time_stamp, show_logo, show_device_name, show_date_time, false);
            Log.d(TAG, "FFMPEG COMMAND:       " + FFmpegTools.getFFmpegCommand());

            record_button.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_media_record_red_24));

            TransitionManager.beginDelayedTransition(base_cardview, new AutoTransition());
            hidden_view.setVisibility(View.GONE);
            sub_expand_icon.setImageResource(R.drawable.ic_expand_more_orange_24);

            FilesTools.deleteHlsContent();

            save_settings_ok_flag = true;

        });

        cancel_settings.setOnClickListener(cancel -> {
            FFmpegKit.cancel(sessionId);
        });

        record_button.setOnClickListener(record -> {

            if(save_settings_ok_flag){

                FilesTools.deleteHlsContent();

                FFmpegKit.executeAsync(FFmpegTools.FFMPEG_COMMAND, session -> {

                    ffmpeg_state       = session.getState();
                    ffmpeg_return_code = session.getReturnCode();
                    sessionId          = session.getSessionId();

                    addUIAction(new Runnable() {

                        @Override
                        public void run() {

                            if (ffmpeg_state.toString().equals("COMPLETED")) {
                                stopPlayer();

                                files_control_frame.setVisibility(View.VISIBLE);
                                files_control_message.setVisibility(View.VISIBLE);
                                watch_recorded_vid_btn.setVisibility(View.VISIBLE);
                                no_watch_recorded_vid_btn.setVisibility(View.VISIBLE);
                            }
                        }
                    });


                    // CALLED WHEN SESSION IS EXECUTED
                    //Log.d(TAG, String.format("FFmpeg process exited with state %s and rc %s.%s", state, returnCode, session.getFailStackTrace()));
                },
                        log -> {

                    // CALLED WHEN SESSION PRINTS LOGS
                    Log.d(TAG, "FFmpeg Log: "+ log);

                },
                    statistics -> {

                        ffmpeg_current_time = statistics.getTime();

                        if(!PLAY_STARTED){

                            addUIAction(new Runnable() {

                                @Override
                                public void run() {
                                    if (ffmpeg_current_time > 1500.0) {
                                        recordPlayVideo();
                                    }
                                }
                            });
                        }

                    // CALLED WHEN SESSION GENERATES STATISTICS
                    Log.d(TAG, "FFmpeg statistics: "+ ffmpeg_current_time);
                });
            }
        });

        stop_button.setOnClickListener(stop -> {

            FFmpegKit.cancel();


        });

        watch_recorded_vid_btn.setOnClickListener(watch -> {

            String url = FFmpegTools.getLastRecordedVideoPath();

            Intent intent = new Intent(CameraRecordActivity.this, CameraHomeActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("url", url);
            intent.putExtra("device_name", DEVICE_NAME);
            intent.putExtra("device_ip", DEVICE_IP);

            startActivity(intent);
            finish();
        });

        no_watch_recorded_vid_btn.setOnClickListener(no_watch -> {
            showExitDialog();
        });
    }

    /* When the activity enters the Started state, the system invokes this callback. The onStart() call makes the activity visible to the user,
        as the app prepares for the activity to enter the foreground and become interactive.
    */
    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "onPause");
        if(player != null) {
            player.pause();
        }
    }

    /* When the activity enters the Resumed state, it comes to the foreground, and then the system invokes the onResume() callback */
    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");

        if(player != null){
            player.play();
        }

    }

    /* When your activity is no longer visible to the user, it has entered the Stopped state, and the system invokes the onStop() callback. */
    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop");
        playerRelease();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
        playerRelease();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

            if(requestCode == REQUEST_LOGO_CODE){

                assert data     != null;
                Uri imageUri     = data.getData();
                assert imageUri != null;
                String imgPath   = imageUri.toString();
                load_logo_icon.setImageURI(imageUri);
                load_logo_icon.setTag("YES");

                BitmapDrawable bitmapDrawable     = (BitmapDrawable) load_logo_icon.getDrawable();
                Bitmap bitmap                     = bitmapDrawable.getBitmap();
                FileOutputStream outputStreamFile = null;

                String savePath                   = FilesTools.getExternalSaveLogosPath(DEVICE_NAME);
                File dir                          = new File(savePath + "/" + FilesTools.SD_FOLDER_TYPE_LOGOS_PATH);
                File outFile                      = new File(dir, logo_file_name);

                try {
                    outputStreamFile = new FileOutputStream(outFile);
                }
                catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStreamFile);

                try {

                    logo_saved_path = savePath + "/" + FilesTools.SD_FOLDER_TYPE_LOGOS_PATH + "/" + logo_file_name;
                    Log.d(TAG, "Logo Saved Path:        " + logo_saved_path);

                    FFmpegTools.setLogoSavedPath(logo_saved_path);

                    FFmpegTools.setFFmpegCommand(FFmpegTools.getFormatedDeviceName(), ffmpeg_time_stamp, show_logo, show_device_name, show_date_time, false);

                    Log.d(TAG, "FFMPEG COMMAND:       " + FFmpegTools.getFFmpegCommand());

                    outputStreamFile.flush();
                    outputStreamFile.close();
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Log.d(TAG, "IMAGE SAVED PATH: " + logo_saved_path);
            }
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupPlayer(){

        Log.d(TAG, "SETUP PLAYER");

        if(player == null){

            player = new ExoPlayer.Builder(getApplicationContext()).setMediaSourceFactory(new DefaultMediaSourceFactory(this).setLiveTargetOffsetMs(5000)).build();

            record_player.setShowNextButton(false);
            record_player.setShowPreviousButton(true);
            record_player.setShowFastForwardButton(false);
            record_player.setShowRewindButton(false);
            player.setVideoScalingMode(VIDEO_SCALING_MODE_SCALE_TO_FIT);

            player.setPlayWhenReady(true);
            record_player.setPlayer(player);
        }
    }
    @OptIn(markerClass = UnstableApi.class)
    public void recordPlayVideo(){

        setupPlayer();

        Log.e(TAG, "PLAY RECORD STARTED");

        PLAY_STARTED = true;
        String stream_file                    = FilesTools.getHlsPath() + DEVICE_NAME.toLowerCase() + "-live.m3u8";

        MediaItem mediaItem                   = new MediaItem.Builder().setUri(stream_file).setMimeType(MimeTypes.APPLICATION_M3U8).setLiveConfiguration(new MediaItem.LiveConfiguration.Builder().setMaxOffsetMs(100).setMinOffsetMs(1000).setMaxPlaybackSpeed(0.25f).build()).build();
        //MediaItem mediaItem = new MediaItem.Builder().setUri(stream_file).setMimeType(MimeTypes.APPLICATION_M3U8).build();

        DataSource.Factory fdataSourceFactory = new FileDataSource.Factory();

        //MediaSource hlsMediaSource            = new HlsMediaSource.Factory(fdataSourceFactory).setAllowChunklessPreparation(false).setMetadataType(METADATA_TYPE_ID3).createMediaSource(mediaItem);
        //player.setMediaSource(hlsMediaSource);
        player.setMediaItem(mediaItem);
        player.setPlaybackSpeed(0.25f);


        //player.addMediaSource(0, hlsMediaSource);

        // Prepare the player.
        player.prepare();
        //player.setRepeatMode(Player.REPEAT_MODE_ONE);

        // Start the playback.
        player.play();


    }

    private void stopPlayer(){

        Log.d(TAG, "STOP PLAYER");

        if(player != null){
            player.stop();
            player.seekToDefaultPosition();
            player.prepare();
            record_player.setPlayer(null);
            player = null;
            PLAY_STARTED = false;

        }
    }

    private void playerRelease(){

        Log.d(TAG, "RELEASE PLAYER");

        if(player != null){
            player.stop();
            player.release();
            player = null;
            PLAY_STARTED = false;

        }
    }

    private void showErrorDialog(String error, String errLabel){

        AlertDialog.Builder d = new AlertDialog.Builder(CameraRecordActivity.this);
        View mView            = getLayoutInflater().inflate(R.layout.dialog_camera_record_errors, null);

        TextView label        = mView.findViewById(R.id.id_camera_record_error_dialog_label);
        TextView message      = mView.findViewById(R.id.id_camera_record_error_dialog_message);
        TextView ok_btn       = mView.findViewById(R.id.id_camera_record_error_dialog_ok_btn);

        /* SET FONT FOR TextView & Button */
        if (LocalHelper.getLanguage(getApplicationContext()).equalsIgnoreCase("ar")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                label.setTypeface(getResources().getFont(R.font.changa));
                message.setTypeface(getResources().getFont(R.font.cairo));
                ok_btn.setTypeface(getResources().getFont(R.font.changa));
            }
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                label.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
                //message.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
                ok_btn.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
            }
        }

        d.setView(mView);
        final AlertDialog alertDialog = d.create();
        alertDialog.setCanceledOnTouchOutside(false);

        label.setText(errLabel);

        if(error.equals("SOURCE_RESOLUTION")){
            message.setText(R.string.save_record_setting_dialog_camera_source_resolution_error);
        }

        if(error.equals("DEVICE_NAME_FONT")){
            message.setText(R.string.save_record_setting_dialog_device_name_font_error);
        }

        if(error.equals("DEVICE_NAME_FONT_PATH")){
            message.setText(R.string.save_record_setting_dialog_device_name_font_path_error);
        }

        if(error.equals("DEVICE_NAME_FONT_SIZE")){
            message.setText(R.string.save_record_setting_dialog_device_name_font_size_error);
        }

        if(error.equals("DEVICE_NAME_FONT_COLOR")){
            message.setText(R.string.save_record_setting_dialog_device_name_font_color_error);
        }

        if(error.equals("DATE_TIME_FONT")){
            message.setText(R.string.save_record_setting_dialog_date_time_font_error);
        }

        if(error.equals("DATE_TIME_FONT_PATH")){
            message.setText(R.string.save_record_setting_dialog_date_time_font_path_error);
        }

        if(error.equals("DATE_TIME_FONT_SIZE")){
            message.setText(R.string.save_record_setting_dialog_date_time_font_size_error);
        }

        if(error.equals("DATE_TIME_FONT_COLOR")){
            message.setText(R.string.save_record_setting_dialog_date_time_font_color_error);
        }

        if(error.equals("LOGO_ABSENT")){
            message.setText(R.string.save_record_setting_dialog_logo_absent_error);
        }

        if(error.equals("LOGO_POSITION")){
            message.setText(R.string.save_record_setting_dialog_logo_position_error);
        }

        ok_btn.setOnClickListener(ok ->{
            alertDialog.dismiss();
        });

        if(!alertDialog.isShowing()){
            alertDialog.show();
        }


    }

    private void showExitDialog(){

        AlertDialog.Builder d = new AlertDialog.Builder(CameraRecordActivity.this);
        View mView            = getLayoutInflater().inflate(R.layout.dialog_exit_camera_recore_page, null);
        TextView label        = mView.findViewById(R.id.id_exit_camera_record_label);
        TextView message      = mView.findViewById(R.id.id_exit_camera_record_message);
        Button record_btn     = mView.findViewById(R.id.id_exit_camera_record_record_next_vid_btn);
        Button exit_btn       = mView.findViewById(R.id.id_exit_camera_record_exit_page_btn);

        if (LocalHelper.getLanguage(getApplicationContext()).equalsIgnoreCase("ar")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                label.setTypeface(getResources().getFont(R.font.changa));
                message.setTypeface(getResources().getFont(R.font.cairo));
                record_btn.setTypeface(getResources().getFont(R.font.changa));
                exit_btn.setTypeface(getResources().getFont(R.font.changa));
            }
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                label.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
                //message.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
                record_btn.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
                exit_btn.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
            }
        }

        d.setView(mView);
        final AlertDialog alertDialog = d.create();
        alertDialog.setCanceledOnTouchOutside(false);

        exit_btn.setOnClickListener(exit ->{

            alertDialog.dismiss();

            Intent intent = new Intent(CameraRecordActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        record_btn.setOnClickListener(record ->{
            alertDialog.dismiss();
            recreate();

        });

        if(!alertDialog.isShowing()){
            alertDialog.show();
        }
    }

    public static void addUIAction(final Runnable runnable) {
        handler.post(runnable);
    }
}