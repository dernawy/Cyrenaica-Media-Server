package com.cyrenaica.cyrenaicaserver.nodes.Cameras;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.preference.PreferenceManager;

import com.cyrenaica.cyrenaicaserver.R;
import com.cyrenaica.cyrenaicaserver.nodes.Cameras.Settings.CameraRecordSettingActivity;
import com.cyrenaica.cyrenaicaserver.nodes.Cameras.Settings.CameraWatchSettingActivity;
import com.cyrenaica.cyrenaicaserver.tools.Tools;
import com.cyrenaica.cyrenaicaserver.tools.mjpeg.MjpegInputCamera;
import com.cyrenaica.cyrenaicaserver.tools.mjpeg.MjpegView;
import com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player.CameraStream;
import com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player.MjpegFaceDetection;

import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

@UnstableApi
public class CameraHomeActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "CAMERA_WATCH";

    static ActionBar actionBar;
    SharedPreferences sharedPref;
    private int DEVICE_ID;
    private String DEVICE_NAME;
    private String DEVICE_IP;
    private String WIFI_STATUS;
    private String ESPNOW_STATUS;

    /* PREFERENCES */
    Boolean httpConnectionPref;
    Boolean udpConnectionPref;
    int connectionPort;
    Boolean activateFaceDetection;
    int faceScaleWidth;
    int faceScaleHeight;
    double faceScaleFactor;
    int faceRecognizeMinNeighbors;
    int faceRecognizeFlags;
    Boolean showOnscreenText;
    Boolean localOnscreenShowDeviceName = true;
    Boolean localOnscreenShowTimeStamp = true;
    Boolean localOnscreenShowPlayTime = true;

    Boolean videoPlayByFfmpeg;
    Boolean videoPlayByMjpeg;

    Boolean cameraVideoRecordActivated;
    Boolean start_detection_now = false;

    /* VARIABLES */
    private String _connection_string;
    private String _connection_protocol;
    private String _connection_video_resolution;
    private static boolean isStreamRunning;

    private MjpegView cameraMjpegView;
    FrameLayout camera_mjpeg_frame;
    ImageView record_button;
    ImageView play_button;
    ImageView pause_button;
    ImageView stop_button;
    ImageView activate_face_detection_button;

    FrameLayout face_detection_toolbox_frame; // to show hide face detection toolbox
    ImageView detected_face_image;
    Button remove_image_btn;
    Button recognize_actions_btn;
    Button save_face_btn;


    private Thread streamThread;
    CameraStream cameraStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera_home);
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

        isStreamRunning = false;

        Tools.hideSoftKeyboard(CameraHomeActivity.this); // Hide keyboard

        /* init Toolbar */
        Toolbar toolbar = findViewById(R.id.camera_home_toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Camera of " + DEVICE_NAME);
            //actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /* init openCV Module */
        OpenCVLoader.initLocal();

        camera_mjpeg_frame             = findViewById(R.id.id_camera_video_view_frame);
        record_button                  = findViewById(R.id.id_video_record_button);
        play_button                    = findViewById(R.id.id_video_play_button);
        pause_button                   = findViewById(R.id.id_video_pause_button);
        stop_button                    = findViewById(R.id.id_video_stop_button);
        activate_face_detection_button = findViewById(R.id.id_face_recognize_button);
        face_detection_toolbox_frame   = findViewById(R.id.id_face_detection_toolbox_frame);
        detected_face_image            = findViewById(R.id.id_detected_face_image);
        remove_image_btn               = findViewById(R.id.id_remove_image_btn);
        recognize_actions_btn          = findViewById(R.id.id_recognize_actions_btn);
        save_face_btn                  = findViewById(R.id.id_save_face_btn);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        httpConnectionPref         = sharedPref.getBoolean(CameraWatchSettingActivity.KEY_PREF_HTTP_CHOICE, true);
        udpConnectionPref          = sharedPref.getBoolean(CameraWatchSettingActivity.KEY_PREF_UDP_CHOICE, false);
        connectionPort             = Integer.parseInt(Objects.requireNonNull(sharedPref.getString(CameraWatchSettingActivity.KEY_PREF_CONNECTION_PORT_EDITTEXT, "80")));
        activateFaceDetection      = sharedPref.getBoolean(CameraWatchSettingActivity.KEY_PREF_FACE_DETECTION_SWITCH, false);
        faceScaleWidth             = Integer.parseInt(Objects.requireNonNull(sharedPref.getString(CameraWatchSettingActivity.KEY_PREF_FACE_SCALE_WIDTH, "112")));
        faceScaleHeight            = Integer.parseInt(Objects.requireNonNull(sharedPref.getString(CameraWatchSettingActivity.KEY_PREF_FACE_SCALE_HEIGHT, "112")));
        faceScaleFactor            = Double.parseDouble(Objects.requireNonNull(sharedPref.getString(CameraWatchSettingActivity.KEY_PREF_FACE_SCALE_FACTOR, "1.2")));
        //faceRecognizeMinNeighbors  = Integer.parseInt(Objects.requireNonNull(sharedPref.getString(CameraWatchSettingActivity.KEY_PREF_FACE_MIN_NEIGHBORS, "2")));
        //faceRecognizeFlags         = Integer.parseInt(Objects.requireNonNull(sharedPref.getString(CameraWatchSettingActivity.KEY_PREF_FACE_RECOGNIZE_FLAGS, "0")));

        showOnscreenText           = sharedPref.getBoolean(CameraWatchSettingActivity.KEY_PREF_ONSCREEN_TEXT_SWITCH, false);

        videoPlayByFfmpeg          = sharedPref.getBoolean(CameraWatchSettingActivity.KEY_PREF_VIDEO_PLAY_BY_FFMPEG, false);
        videoPlayByMjpeg           = sharedPref.getBoolean(CameraWatchSettingActivity.KEY_PREF_VIDEO_PLAY_BY_MJPEG, true);

        cameraVideoRecordActivated = sharedPref.getBoolean(CameraRecordSettingActivity.KEY_PREF_ACTIVATE_VIDEO_RECORD, false);

        /* If camera video record option is true, show record button in controls bar */
        if(cameraVideoRecordActivated){
            record_button.setVisibility(View.VISIBLE);
        }

        /* If face detection option is true, show face detection button in controls bar */
        if(activateFaceDetection){
            activate_face_detection_button.setVisibility(View.VISIBLE);
            start_detection_now = false; // Flag indicate if detection started or no, this will be changed on runtime when click the detection icon in controls bar
        }
        else
        {
            face_detection_toolbox_frame.setVisibility(View.GONE); // hide face detection toolbox
        }

        /* Activate dis-activate face detection action */
        activate_face_detection_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!start_detection_now){
                    start_detection_now = true;
                    activate_face_detection_button.setImageResource(R.drawable.ic_recognize_green_24);
                    face_detection_toolbox_frame.setVisibility(View.VISIBLE); // show face detection toolbox

                    new MjpegInputCamera.InputStreamConfig.Builder().setEnableFaceDetection(start_detection_now).build();
                }
                else
                {
                    start_detection_now = false;
                    activate_face_detection_button.setImageResource(R.drawable.ic_recognize_white_24);
                    face_detection_toolbox_frame.setVisibility(View.GONE); // hide face detection toolbox
                    new MjpegInputCamera.InputStreamConfig.Builder().setEnableFaceDetection(start_detection_now).build();
                }
            }
        });

        record_button.setOnClickListener(v -> {

        });

        play_button.setOnClickListener(v -> {


            if(videoPlayByMjpeg){
                cameraMjpegView = new MjpegView(this);
                camera_mjpeg_frame.addView(cameraMjpegView);

                if(httpConnectionPref){
                    _connection_protocol = "http://";
                }

                if(udpConnectionPref){
                    _connection_protocol = "udp://";
                }

                _connection_video_resolution = "/480x320.mjpeg";

                _connection_string = _connection_protocol;
                _connection_string += DEVICE_IP;
                _connection_string += _connection_video_resolution;


                Uri cam_uri = Uri.parse(_connection_string);
                String path = getApplicationContext().getFilesDir().getPath() + "/data";

                cameraStream = new CameraStream.Builder()
                        .setStreamConfiguration(new CameraStream.StreamConfig.Builder().setUri(cam_uri).useEnableFaceDetection(false).build()) // if here false and in setInputStreamConfiguration() is true then will be true
                        .setInputStreamConfiguration(new MjpegInputCamera.InputStreamConfig.Builder().setEnableFaceDetection(start_detection_now).setResolutionW("480").setResolutionH("320").setConnectionPort(connectionPort).build())
                        .setFaceDetectionConfiguration(new CameraStream.FaceDetection.Builder().setContext(CameraHomeActivity.this).setActivity(this).setModelPath(path).setModelDirection("FRONTAL_FACE").setModelType("HAAR").setModelName("DEF").build())
                        .setFaceConfiguration(new MjpegFaceDetection.Config.Builder().setFaceScaleW(faceScaleWidth).setFaceScaleH(faceScaleHeight).setScaleFactor(faceScaleFactor).setMinNeighbors(faceRecognizeMinNeighbors).setFlags(faceRecognizeFlags).setImagesFrameLayout(face_detection_toolbox_frame).setImageView(detected_face_image).setRemoveImageButton(remove_image_btn).setFaceActionsButton(recognize_actions_btn).setSaveFaceButton(save_face_btn).build()) //.setName("AKRAM").setNamePositionFree(false).setNameFont()
                        .build();

                playVideo("MJPEG");
            }

            if(videoPlayByFfmpeg){
                playVideo("FFMPEG");
            }

        });

        pause_button.setOnClickListener(v -> {

        });

        stop_button.setOnClickListener(v -> {

            /*try {
                MjpegInputCamera.MjpegInputStream.stopSockets();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }*/

            stopStream();
        });

    }

    /* The system calls this method as the first indication that the user is leaving your activity
            (though it does not always mean the activity is being destroyed);
            it indicates that the activity is no longer in the foreground (though it may still be visible if the user is in multi-window mode).
        */
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    /* When the activity enters the Started state, the system invokes this callback. The onStart() call makes the activity visible to the user,
        as the app prepares for the activity to enter the foreground and become interactive.
    */
    @Override
    protected void onStart() {
        super.onStart();
        actionBar.setTitle("Camera of " + DEVICE_NAME);
        Log.d(TAG, "onStart");

        httpConnectionPref         = sharedPref.getBoolean(CameraWatchSettingActivity.KEY_PREF_HTTP_CHOICE, true);
        udpConnectionPref          = sharedPref.getBoolean(CameraWatchSettingActivity.KEY_PREF_UDP_CHOICE, false);
        connectionPort             = Integer.parseInt(Objects.requireNonNull(sharedPref.getString(CameraWatchSettingActivity.KEY_PREF_CONNECTION_PORT_EDITTEXT, "80")));
        activateFaceDetection      = sharedPref.getBoolean(CameraWatchSettingActivity.KEY_PREF_FACE_DETECTION_SWITCH, false);
        faceScaleWidth             = Integer.parseInt(Objects.requireNonNull(sharedPref.getString(CameraWatchSettingActivity.KEY_PREF_FACE_SCALE_WIDTH, "112")));
        faceScaleHeight            = Integer.parseInt(Objects.requireNonNull(sharedPref.getString(CameraWatchSettingActivity.KEY_PREF_FACE_SCALE_HEIGHT, "112")));
        faceScaleFactor            = Float.parseFloat(Objects.requireNonNull(sharedPref.getString(CameraWatchSettingActivity.KEY_PREF_FACE_SCALE_FACTOR, "1.2")));
        //faceRecognizeMinNeighbors  = Integer.parseInt(Objects.requireNonNull(sharedPref.getString(CameraWatchSettingActivity.KEY_PREF_FACE_MIN_NEIGHBORS, "2")));
        //faceRecognizeFlags         = Integer.parseInt(Objects.requireNonNull(sharedPref.getString(CameraWatchSettingActivity.KEY_PREF_FACE_RECOGNIZE_FLAGS, "0")));

        showOnscreenText           = sharedPref.getBoolean(CameraWatchSettingActivity.KEY_PREF_ONSCREEN_TEXT_SWITCH, false);

        videoPlayByFfmpeg          = sharedPref.getBoolean(CameraWatchSettingActivity.KEY_PREF_VIDEO_PLAY_BY_FFMPEG, false);
        videoPlayByMjpeg           = sharedPref.getBoolean(CameraWatchSettingActivity.KEY_PREF_VIDEO_PLAY_BY_MJPEG, true);

        cameraVideoRecordActivated = sharedPref.getBoolean(CameraRecordSettingActivity.KEY_PREF_ACTIVATE_VIDEO_RECORD, false);

        if(cameraVideoRecordActivated){
            record_button.setVisibility(View.VISIBLE);
        }
        else
        {
            record_button.setVisibility(View.GONE);
        }

        if(activateFaceDetection){
            activate_face_detection_button.setVisibility(View.VISIBLE);
        }
        else
        {
            activate_face_detection_button.setVisibility(View.GONE);
        }
    }

    /* When the activity enters the Resumed state, it comes to the foreground, and then the system invokes the onResume() callback */
    @Override
    protected void onResume() {
        super.onResume();
        actionBar.setTitle("Camera of " + DEVICE_NAME);
        Log.d(TAG, "onResume");

        httpConnectionPref         = sharedPref.getBoolean(CameraWatchSettingActivity.KEY_PREF_HTTP_CHOICE, true);
        udpConnectionPref          = sharedPref.getBoolean(CameraWatchSettingActivity.KEY_PREF_UDP_CHOICE, false);
        connectionPort             = Integer.parseInt(Objects.requireNonNull(sharedPref.getString(CameraWatchSettingActivity.KEY_PREF_CONNECTION_PORT_EDITTEXT, "80")));
        activateFaceDetection      = sharedPref.getBoolean(CameraWatchSettingActivity.KEY_PREF_FACE_DETECTION_SWITCH, false);
        faceScaleWidth             = Integer.parseInt(Objects.requireNonNull(sharedPref.getString(CameraWatchSettingActivity.KEY_PREF_FACE_SCALE_WIDTH, "112")));
        faceScaleHeight            = Integer.parseInt(Objects.requireNonNull(sharedPref.getString(CameraWatchSettingActivity.KEY_PREF_FACE_SCALE_HEIGHT, "112")));
        faceScaleFactor            = Float.parseFloat(Objects.requireNonNull(sharedPref.getString(CameraWatchSettingActivity.KEY_PREF_FACE_SCALE_FACTOR, "1.2")));
        //faceRecognizeMinNeighbors  = Integer.parseInt(Objects.requireNonNull(sharedPref.getString(CameraWatchSettingActivity.KEY_PREF_FACE_MIN_NEIGHBORS, "2")));
        //faceRecognizeFlags         = Integer.parseInt(Objects.requireNonNull(sharedPref.getString(CameraWatchSettingActivity.KEY_PREF_FACE_RECOGNIZE_FLAGS, "0")));

        showOnscreenText           = sharedPref.getBoolean(CameraWatchSettingActivity.KEY_PREF_ONSCREEN_TEXT_SWITCH, false);

        videoPlayByFfmpeg          = sharedPref.getBoolean(CameraWatchSettingActivity.KEY_PREF_VIDEO_PLAY_BY_FFMPEG, false);
        videoPlayByMjpeg           = sharedPref.getBoolean(CameraWatchSettingActivity.KEY_PREF_VIDEO_PLAY_BY_MJPEG, true);

        cameraVideoRecordActivated = sharedPref.getBoolean(CameraRecordSettingActivity.KEY_PREF_ACTIVATE_VIDEO_RECORD, false);

        if(cameraVideoRecordActivated){
            record_button.setVisibility(View.VISIBLE);
        }
        else
        {
            record_button.setVisibility(View.GONE);
        }

        if(activateFaceDetection){
            activate_face_detection_button.setVisibility(View.VISIBLE);
        }
        else
        {
            activate_face_detection_button.setVisibility(View.GONE);
        }
    }

    /* When your activity is no longer visible to the user, it has entered the Stopped state, and the system invokes the onStop() callback. */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.camera_watch_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.camera_watch_settings){
            stopStream();
            Intent intent = new Intent(CameraHomeActivity.this, CameraWatchSettingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }

        if(id == R.id.camera_record_settings){
            stopStream();
            Intent intent = new Intent(CameraHomeActivity.this, CameraRecordSettingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        if(id == R.id.camera_details){

        }


        return true;

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {

        Log.i(TAG, "Preference key: " + key);

        if (key.equals(CameraRecordSettingActivity.KEY_PREF_ACTIVATE_VIDEO_RECORD)) {

            cameraVideoRecordActivated = sharedPreferences.getBoolean(key, false);

            if(cameraVideoRecordActivated){
                record_button.setVisibility(View.VISIBLE);
            }
            else
            {
                record_button.setVisibility(View.GONE);
            }

            Log.i(TAG, "Preference value was updated to: " + sharedPreferences.getBoolean(key, false));
        }

        if (key.equals(CameraWatchSettingActivity.KEY_PREF_FACE_DETECTION_SWITCH)) {

            activateFaceDetection = sharedPreferences.getBoolean(key, false);

            if(activateFaceDetection){
                activate_face_detection_button.setVisibility(View.VISIBLE);
            }
            else
            {
                activate_face_detection_button.setVisibility(View.GONE);
            }

            Log.i(TAG, "Preference value was updated to: " + sharedPreferences.getBoolean(key, false));
        }
    }

    private void playVideo(String playType){
        Log.d(TAG, "Play Type: " + playType);

        if(playType.equals("MJPEG")){

            final Runnable streamTask = () -> {

                try {

                    cameraMjpegView.setSource(MjpegInputCamera.MjpegInputStream.read(cameraStream.getUri()));
                    cameraMjpegView.setDisplayMode(MjpegView.SIZE_BEST_FIT);

                    if(showOnscreenText){

                        if(localOnscreenShowDeviceName){
                            cameraMjpegView.showName(true);
                            cameraMjpegView.setDeviceName(DEVICE_NAME);
                        }

                        if(localOnscreenShowTimeStamp){
                            cameraMjpegView.setShowTimeStamp(true);
                            //TODO: Implementation of time stamp format
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault(Locale.Category.FORMAT));
                            String time                 = dateFormat.format(new Date()); // Find today's date;
                            cameraMjpegView.setTimeStamp(time);
                        }

                        if(localOnscreenShowPlayTime){
                            cameraMjpegView.setShowPlayTime(true);
                        }
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };

            this.streamThread = new Thread(streamTask);
            startStream();
            if(localOnscreenShowPlayTime) {
                cameraMjpegView.startPlayTimeTimer();
            }

            isStreamRunning = true;
        }

        if(playType.equals("FFMPEG")){

        }

    }

    private void startStream(){
        this.streamThread.start();
    }

    private void stopStream(){

        if(isStreamRunning) {

            cameraMjpegView.stopPlayback(camera_mjpeg_frame);

            try {
                MjpegInputCamera.MjpegInputStream.stopSockets();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

            if(this.streamThread.isAlive()){

                try {
                    this.streamThread.join();
                    this.streamThread = null;
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            isStreamRunning = false;
        }
    }

}