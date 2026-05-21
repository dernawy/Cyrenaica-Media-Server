package com.cyrenaica.cyrenaicaserver;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.VideoView;
import com.cyrenaica.cyrenaicaserver.server.ServerHomeActivity;
import com.cyrenaica.cyrenaicaserver.tools.Tools;
import com.cyrenaica.cyrenaicaserver.language.LocalHelper;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends AppCompatActivity  implements MediaPlayer.OnCompletionListener {

    private static final String TAG = "SPLASH_ACTIVITY";

    TextView languageLabel;
    private Spinner languageSpinner;
    String actualLang;

    VideoView cyrenaica_intro_video;
    FrameLayout video_intro_frame;
    LinearLayout start_setup_controls_layout;
    TextView intro_first_time_text;
    Button intro_start_setup_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Log.i(TAG, "onCreate");

        Tools.IS_SETUP_OK           = Tools.getFirstTimeIntro(getApplicationContext());
        Tools.SETUP_CURRENT_STEP    = Tools.getSetupCurrentStep(getApplicationContext());

        languageLabel               = findViewById(R.id.main_lang_label);
        languageSpinner             = findViewById(R.id.id_main_activity_language_spinner);
        actualLang                  = LocalHelper.getLanguage(SplashActivity.this);
        video_intro_frame           = findViewById(R.id.id_video_intro_frame);
        cyrenaica_intro_video       = findViewById(R.id.id_cyrenaica_intro_video);
        start_setup_controls_layout = findViewById(R.id.id_start_setup_controls_layout);
        intro_first_time_text       = findViewById(R.id.id_intro_first_time_text);
        intro_start_setup_button    = findViewById(R.id.id_intro_start_setup_button);

        if(!Tools.IS_SETUP_OK){

            /* This one line method enough to start language */
            LocalHelper.init_language(getApplicationContext(), SplashActivity.this, languageSpinner, languageLabel, true);

            cyrenaica_intro_video.setOnCompletionListener(SplashActivity.this);

            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video_cyrenaica_intro);

            if(Tools.SETUP_CURRENT_STEP == 0) {

                if (LocalHelper.getLanguage(SplashActivity.this).equalsIgnoreCase("ar")) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        intro_start_setup_button.setTypeface(getResources().getFont(R.font.cairo));
                        intro_first_time_text.setTypeface(getResources().getFont(R.font.changa));
                    }

                }

                cyrenaica_intro_video.setVideoURI(uri);
                cyrenaica_intro_video.start();
            }
            else
            {

                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.putExtra("SETUP_STEP_MESSAGE", "As the last time your setup was interrupted, we saved your last setup step");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
        else
        {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        Log.i(TAG, "onCompletion");

        start_setup_controls_layout.setVisibility(View.VISIBLE);

        intro_start_setup_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.putExtra("SETUP_STEP_MESSAGE", "As the last time your setup was interrupted, we saved your last setup step");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "onStart");
    }

    @Override
    protected void onPause() {

        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onResume() {

        super.onResume();
        cyrenaica_intro_video.start();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onStop() {

        Log.i(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocalHelper.onAttach(base, "en"));
    }
}