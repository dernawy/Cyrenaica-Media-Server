package com.cyrenaica.cyrenaicaserver.nodes.Cameras.Settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.Preference.OnPreferenceChangeListener;
import com.cyrenaica.cyrenaicaserver.R;

public class CameraWatchSettingActivity extends AppCompatActivity {

    private static final String TAG = "CAMERA_WATCH_PREFERENCES";

    static ActionBar actionBar;

    static SharedPreferences sharedPref;
    static SharedPreferences.Editor editor;

    public static final String KEY_PREF_HTTP_CHOICE               = "cam_watch_network_http";
    public static final String KEY_PREF_UDP_CHOICE                = "cam_watch_network_udp";
    public static final String KEY_PREF_FACE_DETECTION_SWITCH     = "activate_face_detection_switch";
    public static final String KEY_PREF_ONSCREEN_TEXT_SWITCH      = "show_onscreen_text_switch";
    public static final String KEY_PREF_CONNECTION_PORT_EDITTEXT  = "cam_watch_connection_port";


    public static final String KEY_PREF_FACE_SCALE_WIDTH      = "face_recognize_face_scale_width";
    public static final String KEY_PREF_FACE_SCALE_HEIGHT     = "face_recognize_face_scale_height";
    public static final String KEY_PREF_FACE_SCALE_FACTOR     = "face_recognize_face_scale_factor";
    public static final String KEY_PREF_FACE_MIN_NEIGHBORS    = "face_recognize_face_min_neighbors";
    public static final String KEY_PREF_FACE_RECOGNIZE_FLAGS  = "face_recognize_recognize_flags";

    public static final String KEY_PREF_VIDEO_PLAY_BY_FFMPEG = "cam_watch_video_play_by_ffmpeg_choice";
    public static final String KEY_PREF_VIDEO_PLAY_BY_MJPEG  = "cam_watch_video_play_by_mjpeg_choice";

    static CheckBoxPreference network_use_http_choice;
    static CheckBoxPreference network_use_udp_choice;
    static SwitchPreferenceCompat activate_face_detection_switch;
    static SwitchPreferenceCompat show_onscreen_text_switch;
    static EditTextPreference connection_port;
    static CheckBoxPreference video_play_by_ffmpeg;
    static CheckBoxPreference video_play_by_mjpeg;

    static EditTextPreference face_scale_width;
    static EditTextPreference face_scale_height;
    static EditTextPreference face_scale_factor;
    static EditTextPreference face_min_neighbors;
    static EditTextPreference face_recognize_flags;

    static boolean useHttp;
    static boolean useUdp;
    static String connectionPort;
    static boolean activateFaceDetection;
    static boolean showOnscreenText;

    static boolean videoPlayByFfmpeg;
    static boolean videoPlayByMjpeg;

    static String faceScaleWidth;
    static String faceScaleHeight;
    static String faceScaleFactor;
    static String faceMinNeighbors;
    static String faceRecognizeFlags;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_watch_settings_activity);

        /* init Toolbar */
        Toolbar toolbar = findViewById(R.id.camera_watch_toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.settings, new CameraWatchPreferencesFragment()).commit();
        }

        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Camera Watch Settings");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
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
        actionBar.setTitle("Camera Watch Settings");
        Log.d(TAG, "onStart");
    }

    /* When the activity enters the Resumed state, it comes to the foreground, and then the system invokes the onResume() callback */
    @Override
    protected void onResume() {
        super.onResume();
        actionBar.setTitle("Camera Watch Settings");
        Log.d(TAG, "onResume");
    }

    /* When your activity is no longer visible to the user, it has entered the Stopped state, and the system invokes the onStop() callback. */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public static class CameraWatchPreferencesFragment extends PreferenceFragmentCompat {

        public CameraWatchPreferencesFragment(){

        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.camera_watch_preferences, rootKey);
            actionBar.setTitle("Camera Watch Settings");

            network_use_http_choice        = findPreference(KEY_PREF_HTTP_CHOICE);
            network_use_udp_choice         = findPreference(KEY_PREF_UDP_CHOICE);
            activate_face_detection_switch = findPreference(KEY_PREF_FACE_DETECTION_SWITCH);
            show_onscreen_text_switch      = findPreference(KEY_PREF_ONSCREEN_TEXT_SWITCH);
            connection_port                = findPreference(KEY_PREF_CONNECTION_PORT_EDITTEXT);

            network_use_http_choice.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(@NonNull androidx.preference.Preference preference, Object newValue) {

                    useHttp = (boolean) newValue;

                    editor = sharedPref.edit();
                    editor.putBoolean(KEY_PREF_HTTP_CHOICE, useHttp);
                    editor.apply();

                    if(useHttp){
                        network_use_udp_choice.setChecked(false);
                    }

                    return true;
                }
            });

            network_use_udp_choice.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(@NonNull androidx.preference.Preference preference, Object newValue) {

                    useUdp = (boolean) newValue;

                    editor = sharedPref.edit();
                    editor.putBoolean(KEY_PREF_UDP_CHOICE, useUdp);
                    editor.apply();

                    if(useUdp){
                        network_use_http_choice.setChecked(false);
                    }


                    return true;
                }
            });

            connection_port.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(@NonNull androidx.preference.Preference preference, Object newValue) {

                    connectionPort = (String) newValue;

                    editor = sharedPref.edit();
                    editor.putString(KEY_PREF_CONNECTION_PORT_EDITTEXT, connectionPort);
                    editor.apply();

                    connection_port.setSummary("Port used " + connectionPort);
                    return true;
                }
            });

            activate_face_detection_switch.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(@NonNull androidx.preference.Preference preference, Object newValue) {

                    activateFaceDetection = (boolean) newValue;

                    editor = sharedPref.edit();
                    editor.putBoolean(KEY_PREF_FACE_DETECTION_SWITCH, activateFaceDetection);
                    editor.apply();

                    return true;
                }
            });

            show_onscreen_text_switch.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(@NonNull androidx.preference.Preference preference, Object newValue) {

                    showOnscreenText = (boolean) newValue;

                    editor = sharedPref.edit();
                    editor.putBoolean(KEY_PREF_ONSCREEN_TEXT_SWITCH, showOnscreenText);
                    editor.apply();

                    return true;
                }
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
        public void onStart() {
            super.onStart();
            actionBar.setTitle("Camera Watch Settings");
            Log.d(TAG, "onStart");
        }

        /* When the activity enters the Resumed state, it comes to the foreground, and then the system invokes the onResume() callback */
        @Override
        public void onResume() {
            super.onResume();
            actionBar.setTitle("Camera Watch Settings");
            Log.d(TAG, "onResume");
        }

        /* When your activity is no longer visible to the user, it has entered the Stopped state, and the system invokes the onStop() callback. */
        @Override
        public void onStop() {
            super.onStop();
            Log.d(TAG, "onStop");
        }

    }

    public static class CameraWatchFaceRecognizePreferencesFragment extends PreferenceFragmentCompat {

        private static final String RECOGNIZE_TAG = "CAMERA_WATCH_FACE_RECOGNIZE_PREFERENCES";

        public CameraWatchFaceRecognizePreferencesFragment(){

        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.camera_watch_face_recognize_prefernces, rootKey);
            actionBar.setTitle("Face Recognize Settings");

            face_scale_width     = findPreference(KEY_PREF_FACE_SCALE_WIDTH);
            face_scale_height    = findPreference(KEY_PREF_FACE_SCALE_HEIGHT);
            face_scale_factor    = findPreference(KEY_PREF_FACE_SCALE_FACTOR);
            face_min_neighbors   = findPreference(KEY_PREF_FACE_MIN_NEIGHBORS);
            face_recognize_flags = findPreference(KEY_PREF_FACE_RECOGNIZE_FLAGS);

            face_scale_width.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(@NonNull androidx.preference.Preference preference, Object newValue) {

                    faceScaleWidth = (String) newValue;

                    editor = sharedPref.edit();
                    editor.putString(KEY_PREF_FACE_SCALE_WIDTH, faceScaleWidth);
                    editor.apply();

                    face_scale_width.setSummary("Current scale width " + faceScaleWidth);
                    return true;
                }

            });

            face_scale_height.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(@NonNull androidx.preference.Preference preference, Object newValue) {

                    faceScaleHeight = (String) newValue;

                    editor = sharedPref.edit();
                    editor.putString(KEY_PREF_FACE_SCALE_HEIGHT, faceScaleHeight);
                    editor.apply();

                    face_scale_height.setSummary("Current scale height " + faceScaleHeight);
                    return true;
                }

            });

            face_scale_factor.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(@NonNull androidx.preference.Preference preference, Object newValue) {

                    faceScaleFactor = (String) newValue;

                    editor = sharedPref.edit();
                    editor.putString(KEY_PREF_FACE_SCALE_FACTOR, faceScaleFactor);
                    editor.apply();

                    face_scale_factor.setSummary("Current scale factor " + faceScaleFactor);
                    return true;
                }

            });

            face_min_neighbors.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(@NonNull androidx.preference.Preference preference, Object newValue) {

                    faceMinNeighbors = (String) newValue;

                    editor = sharedPref.edit();
                    editor.putString(KEY_PREF_FACE_MIN_NEIGHBORS, faceMinNeighbors);
                    editor.apply();

                    face_scale_factor.setSummary("Current face min neighbors " + faceMinNeighbors);
                    return true;
                }

            });

            face_recognize_flags.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(@NonNull androidx.preference.Preference preference, Object newValue) {

                    faceRecognizeFlags = (String) newValue;

                    editor = sharedPref.edit();
                    editor.putString(KEY_PREF_FACE_RECOGNIZE_FLAGS, faceRecognizeFlags);
                    editor.apply();

                    face_scale_factor.setSummary("Current face recognize flags " + faceRecognizeFlags);
                    return true;
                }

            });

        }

        /* The system calls this method as the first indication that the user is leaving your activity
               (though it does not always mean the activity is being destroyed);
               it indicates that the activity is no longer in the foreground (though it may still be visible if the user is in multi-window mode).
           */
        @Override
        public void onPause() {
            super.onPause();
            Log.d(RECOGNIZE_TAG, "onPause");
        }

        /* When the activity enters the Started state, the system invokes this callback. The onStart() call makes the activity visible to the user,
            as the app prepares for the activity to enter the foreground and become interactive.
        */
        @Override
        public void onStart() {
            super.onStart();
            actionBar.setTitle("Face Recognize Settings");
            Log.d(RECOGNIZE_TAG, "onStart");
        }

        /* When the activity enters the Resumed state, it comes to the foreground, and then the system invokes the onResume() callback */
        @Override
        public void onResume() {
            super.onResume();
            actionBar.setTitle("Face Recognize Settings");
            Log.d(RECOGNIZE_TAG, "onResume");
        }

        /* When your activity is no longer visible to the user, it has entered the Stopped state, and the system invokes the onStop() callback. */
        @Override
        public void onStop() {
            super.onStop();
            Log.d(RECOGNIZE_TAG, "onStop");
        }
    }

    public static class CameraWatchVideoPlayOptionsPreferencesFragment extends PreferenceFragmentCompat {

        public CameraWatchVideoPlayOptionsPreferencesFragment(){

        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.camera_watch_video_play_prefernces, rootKey);
            actionBar.setTitle("Camera Video Play Settings");

            video_play_by_ffmpeg        = findPreference(KEY_PREF_VIDEO_PLAY_BY_FFMPEG);
            video_play_by_mjpeg         = findPreference(KEY_PREF_VIDEO_PLAY_BY_MJPEG);

            video_play_by_ffmpeg.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(@NonNull androidx.preference.Preference preference, Object newValue) {

                    videoPlayByFfmpeg = (boolean) newValue;

                    editor = sharedPref.edit();
                    editor.putBoolean(KEY_PREF_VIDEO_PLAY_BY_FFMPEG, videoPlayByFfmpeg);
                    editor.apply();

                    return true;
                }
            });

            video_play_by_mjpeg.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(@NonNull androidx.preference.Preference preference, Object newValue) {

                    videoPlayByMjpeg = (boolean) newValue;

                    editor = sharedPref.edit();
                    editor.putBoolean(KEY_PREF_VIDEO_PLAY_BY_MJPEG, videoPlayByMjpeg);
                    editor.apply();

                    return true;
                }
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
        public void onStart() {
            super.onStart();
            actionBar.setTitle("Camera Video Play Settings");
            Log.d(TAG, "onStart");
        }

        /* When the activity enters the Resumed state, it comes to the foreground, and then the system invokes the onResume() callback */
        @Override
        public void onResume() {
            super.onResume();
            actionBar.setTitle("Camera Video Play Settings");
            Log.d(TAG, "onResume");
        }

        /* When your activity is no longer visible to the user, it has entered the Stopped state, and the system invokes the onStop() callback. */
        @Override
        public void onStop() {
            super.onStop();
            Log.d(TAG, "onStop");
        }

    }
}