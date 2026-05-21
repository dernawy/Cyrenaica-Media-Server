package com.cyrenaica.cyrenaicaserver.nodes.Cameras.Settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreferenceCompat;

import com.cyrenaica.cyrenaicaserver.R;

public class CameraRecordSettingActivity extends AppCompatActivity {

    private static final String TAG = "CAMERA_RECORD_PREFERENCES";
    static ActionBar actionBar;

    static SharedPreferences sharedPref;
    static SharedPreferences.Editor editor;

    public static final String KEY_PREF_ACTIVATE_VIDEO_RECORD      = "activate_video_record_switch";

    static SwitchPreferenceCompat activate_video_record_switch;



    static boolean activateVideoRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_record_settings_activity);

        /* init Toolbar */
        Toolbar toolbar = findViewById(R.id.camera_record_toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.settings, new CameraRecordSettingActivity.CameraRecordPreferencesFragment()).commit();
        }

        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Camera Record Settings");
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
        actionBar.setTitle("Camera Record Settings");
        Log.d(TAG, "onStart");
    }

    /* When the activity enters the Resumed state, it comes to the foreground, and then the system invokes the onResume() callback */
    @Override
    protected void onResume() {
        super.onResume();
        actionBar.setTitle("Camera Record Settings");
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

    public static class CameraRecordPreferencesFragment extends PreferenceFragmentCompat {

        public CameraRecordPreferencesFragment() {

        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.camera_record_preferences, rootKey);
            actionBar.setTitle("Camera Record Settings");

            activate_video_record_switch = findPreference(KEY_PREF_ACTIVATE_VIDEO_RECORD);


            activate_video_record_switch.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(@NonNull androidx.preference.Preference preference, Object newValue) {

                    activateVideoRecord = (Boolean) newValue;

                    editor = sharedPref.edit();
                    editor.putBoolean(KEY_PREF_ACTIVATE_VIDEO_RECORD, activateVideoRecord);
                    editor.apply();

                    activate_video_record_switch.setSummary(R.string.menu_camera_record_activate_video_record_on_summary);
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
            actionBar.setTitle("Camera Record Settings");
            Log.d(TAG, "onStart");
        }

        /* When the activity enters the Resumed state, it comes to the foreground, and then the system invokes the onResume() callback */
        @Override
        public void onResume() {
            super.onResume();
            actionBar.setTitle("Camera Record  Settings");
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
