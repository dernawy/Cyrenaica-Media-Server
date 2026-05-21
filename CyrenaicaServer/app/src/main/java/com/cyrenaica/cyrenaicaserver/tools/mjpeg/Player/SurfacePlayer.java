package com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player;

import static androidx.media3.common.util.Assertions.checkNotNull;

import static org.opencv.videoio.Videoio.CAP_FFMPEG;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.C;
import androidx.media3.common.Effect;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.OnInputFrameProcessedListener;
import androidx.media3.common.Player;
import androidx.media3.common.SurfaceInfo;
import androidx.media3.common.VideoFrameProcessingException;
import androidx.media3.common.VideoFrameProcessor;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.GlUtil;
import androidx.media3.common.util.TimestampIterator;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.UdpDataSource;
import androidx.media3.effect.ByteBufferGlEffect;
import androidx.media3.effect.OverlayEffect;
import androidx.media3.effect.ColorLut;
import androidx.media3.effect.TextOverlay;
import androidx.media3.effect.TextureOverlay;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager;
import androidx.media3.exoplayer.drm.DrmSessionManager;
import androidx.media3.exoplayer.drm.FrameworkMediaDrm;
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.util.EventLogger;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.SessionState;
import com.cyrenaica.cyrenaicaserver.R;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@UnstableApi
public class SurfacePlayer extends AppCompatActivity {

    private static final String TAG = "SURFACE_PLAYER";

    @Nullable private PlayerView mPlayerView;
    private SurfaceView mSurfaceView;

    ExoPlayer exoPlayer;

    private boolean mShowStopLabel;

    Button playStopBtn;
    private boolean mPlayerReady = false;

    long sessionId;
    SessionState state;
    ReturnCode returnCode;

    VideoProcessing vp;
    private static final String DEFAULT_MEDIA_URI = "udp://127.0.0.1:8000/source";
    private static final String ACTION_VIEW = "androidx.media3.demo.gl.action.VIEW";
    private static final String EXTENSION_EXTRA = "extension";
    private static final String DRM_SCHEME_EXTRA = "drm_scheme";
    private static final String DRM_LICENSE_URL_EXTRA = "drm_license_url";
    @Nullable private VideoProcessingGLSurfaceView videoProcessingGLSurfaceView;

    @Nullable private ExoPlayer player;

    private List<Effect> effects = new ArrayList<Effect>();

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_surface_player);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* init openCV Module */
        OpenCVLoader.initLocal();

        mPlayerView  = findViewById(R.id.id_surface_player);
        //mSurfaceView = (SurfaceView)mPlayerView.getVideoSurfaceView();
        playStopBtn = findViewById(R.id.id_play_stop_btn);
        /*checkNotNull(mPlayerView);
        AspectRatioFrameLayout contentFrame = mPlayerView.findViewById(androidx.media3.ui.R.id.exo_content_frame);

        Context context = getApplicationContext();
        boolean requestSecureSurface = getIntent().hasExtra(DRM_SCHEME_EXTRA);
        if (requestSecureSurface && !GlUtil.isProtectedContentExtensionSupported(context)) {
            Toast.makeText(context, R.string.error_protected_content_extension_not_supported, Toast.LENGTH_LONG).show();
        }

        String path = getApplicationContext().getFilesDir().getPath() + "/data";

        VideoProcessingGLSurfaceView videoProcessingGLSurfaceView = new VideoProcessingGLSurfaceView(context, requestSecureSurface, new BitmapOverlayVideoProcessor(context, path));
        contentFrame.addView(videoProcessingGLSurfaceView);
        this.videoProcessingGLSurfaceView = videoProcessingGLSurfaceView;*/
        //contentFrame.draw();
        //contentFrame.onDrawForeground();

        /*TextOverlay overlay = new TextOverlay() {
            final SpannableString s = new SpannableString("hello");

            @NonNull
            @Override public SpannableString getText(long presentationTimeUs) { return s; }
        };

        effects.add(new OverlayEffect(new ImmutableList.Builder<TextureOverlay>().add(overlay).build()));*/
        /*********************************************************************************************************/




        //effects.add(new OverlayEffect(new ImmutableList.Builder<TextureOverlay>().add(faceOverlay).build()));

        /***********************************************************************************************************/





        /*assert mSurfaceView != null;
        playStopBtn = findViewById(R.id.id_play_stop_btn);
        vp = new VideoProcessing(getApplicationContext(), mPlayerView);
        //vp.setEffects(effects);
        vp.setEffects(effects);
        Uri udp_url = Uri.parse("udp://127.0.0.1:8000/source");
        vp.setSourceUri(udp_url);
        try {
            vp.initPlayer();
        }
        catch (VideoFrameProcessingException e) {
            throw new RuntimeException(e);
        }

        updateControls();
        mPlayerReady = true;*/

    }

    @Override
    public void onStart() {
        super.onStart();
        /*if (Util.SDK_INT > 23) {
            initializePlayer();
            if (mPlayerView != null) {
                mPlayerView.onResume();
            }
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        /*if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer();
            if (mPlayerView != null) {
                mPlayerView.onResume();
            }
        }*/
    }

    @Override
    public void onPause() {
        super.onPause();
        /*if (Util.SDK_INT <= 23) {
            if (mPlayerView != null) {
                mPlayerView.onPause();
            }
            releasePlayer();
        }*/
    }

    @Override
    public void onStop() {
        super.onStop();
        /*if (Util.SDK_INT > 23) {
            if (mPlayerView != null) {
                mPlayerView.onPause();
            }
            releasePlayer();
        }*/
    }

    private void initializePlayer() {

        Intent intent = getIntent();
        String action = intent.getAction();

        Uri uri = ACTION_VIEW.equals(action) ? Assertions.checkNotNull(intent.getData()) : Uri.parse(DEFAULT_MEDIA_URI);

        DrmSessionManager drmSessionManager;

        if (intent.hasExtra(DRM_SCHEME_EXTRA)) {
            String drmScheme                            = Assertions.checkNotNull(intent.getStringExtra(DRM_SCHEME_EXTRA));
            String drmLicenseUrl                        = Assertions.checkNotNull(intent.getStringExtra(DRM_LICENSE_URL_EXTRA));
            UUID drmSchemeUuid                          = Assertions.checkNotNull(Util.getDrmUuid(drmScheme));
            DataSource.Factory licenseDataSourceFactory = new DefaultHttpDataSource.Factory();
            HttpMediaDrmCallback drmCallback            = new HttpMediaDrmCallback(drmLicenseUrl, licenseDataSourceFactory);
            drmSessionManager                           = new DefaultDrmSessionManager.Builder().setUuidAndExoMediaDrmProvider(drmSchemeUuid, FrameworkMediaDrm.DEFAULT_PROVIDER).build(drmCallback);
        }
        else
        {
            drmSessionManager = DrmSessionManager.DRM_UNSUPPORTED;
        }

        String scheme = uri.getScheme();
        DataSource.Factory dataSourceFactory;

        assert scheme != null;
        if(scheme.equals("udp")){
            dataSourceFactory = () -> new UdpDataSource(3000, 50000);
        }
        else
        {
            dataSourceFactory = new DefaultDataSource.Factory(this);
        }

        MediaSource mediaSource;

        @Nullable String fileExtension = intent.getStringExtra(EXTENSION_EXTRA);

        @C.ContentType
        int type = TextUtils.isEmpty(fileExtension) ? Util.inferContentType(uri) : Util.inferContentTypeForExtension(fileExtension);

        if (type == C.CONTENT_TYPE_DASH) {

            mediaSource = new DashMediaSource.Factory(dataSourceFactory).setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager).createMediaSource(MediaItem.fromUri(uri));
        }
        else if (type == C.CONTENT_TYPE_OTHER) {
            mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager).createMediaSource(MediaItem.fromUri(uri));
            //mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
        }
        else
        {
            throw new IllegalStateException();
        }

        ExoPlayer player = new ExoPlayer.Builder(getApplicationContext()).build();
        //player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.setMediaSource(mediaSource);
        player.setPlayWhenReady(true);
        player.prepare();
        player.play();
        VideoProcessingGLSurfaceView videoProcessingGLSurfaceView = Assertions.checkNotNull(this.videoProcessingGLSurfaceView);
        videoProcessingGLSurfaceView.setPlayer(player);
        Assertions.checkNotNull(mPlayerView).setPlayer(player);
        player.addAnalyticsListener(new EventLogger());
        this.player = player;
    }

    private void releasePlayer() {
        Assertions.checkNotNull(mPlayerView).setPlayer(null);
        Assertions.checkNotNull(videoProcessingGLSurfaceView).setPlayer(null);
        if (player != null) {
            player.release();
            player = null;
        }
    }

    /**
     * Updates the on-screen controls to reflect the current state of the app.
     */
    private void updateControls() {

        if (mShowStopLabel) {
            playStopBtn.setText("Stop");
            record();
        }
        else
        {
            playStopBtn.setText("Play");
            stopRecord();
        }

    }

    /**
     * onClick handler for "play"/"stop" button. ->in Xml
     */
    public void clickPlayStop(@SuppressWarnings("unused") View unused){


        if(mShowStopLabel){
            mShowStopLabel = false;
        }
        else
        {
            mShowStopLabel = true;
        }
        updateControls();
        /*assert this.player != null;
        if(this.player.isPlaying()){
            stopRecord();
            this.player.stop();
            mShowStopLabel = false;
            updateControls();
        }
        else
        {
            record();
            this.player.play();

            mShowStopLabel = true;
            updateControls();
        }*/

        //stopRecord();
    }

    private void record(){

        String d_name = "BEDROOM";

        String mp4_file_name =  getApplicationContext().getDir("cyrenaicaVid", Context.MODE_PRIVATE).getPath() + "/hls/" + "output-live.mp4";
        File dir = new File(getApplicationContext().getDir("cyrenaicaVid", Context.MODE_PRIVATE).getPath() + "/hls/" + d_name);

        try{

            if(!dir.exists()){
                if(dir.mkdir()) {
                    System.out.println("Directory created");
                }
                else
                {
                    System.out.println("Directory is not created");
                }
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }

        String name_text = "'BEDROOM'";
        String font_file = getApplicationContext().getFilesDir().getPath() + "/font/impact.ttf";
        String name_text_comm = "drawtext=fontsize=24:x=10:y=10" + ":fontfile=" + "'" + font_file  + "'" + ":text=" + name_text;
        String name1_text_comm = "drawtext=fontsize=24:x=10:y=50" + ":fontfile=" + "'" + font_file  + "'" + ":text=" + "'" + "28-03-2025" + "'";
        String map_v = "v:0";

        String logo_overlay_x = "W-w-10";
        String logo_overlay_y = "10";
        String live_logo_x = "W-w-10";
        String live_logo_y = "H-h-10";

        String  cam_input     = "-f mjpeg -re -i http://192.168.1.208/480x320.mjpeg";
        String logo_path      = "/storage/sdcard1/Android/data/com.cyrenaica.cyrenaicaserver/files/IMAGES/KITCHEN/LOGOS/logo.png";
        String live_logo_path = getApplicationContext().getDir("cyrenaicaVid", Context.MODE_PRIVATE).getPath() + "/hls/ic_live-stream.png";
        String  logo_input    = " -i " + logo_path;
        String  live_logo_input = " -i " + live_logo_path;

        String  mp4_codec     = " -vcodec libx264 -r 10 -s 1920x1080 -preset ultrafast -b:v 20000K -pix_fmt yuv420p -flags +cgop -g 30";
        String filter_complex = " -filter_complex " + "'" + "[1]scale=40:40[scl],[2]scale=100:80[lscl],[0:v][scl]overlay=" + logo_overlay_x + ":" + logo_overlay_y + "[ovl];[ovl]" + name_text_comm + "," + name1_text_comm + "[txt];[txt]split=3[s0][s1];[s0]scale=1920x1080:flags=lanczos[s0ovl];[s0ovl][lscl]overlay=" + live_logo_x + ":" + live_logo_y + "[v0];[s1]scale=1920x1080:flags=lanczos[v1]'";

        String server_map     = " -map \"" + "[v1]" + "\"";


        String mp4_map        = " -movflags +frag_keyframe+separate_moof+omit_tfhd_offset+empty_moov" + mp4_codec + " -map \"" + "[v0]" + "\"";
        String mp4_output     = " -y " + mp4_file_name;

        String  COMMAND = cam_input + logo_input + live_logo_input + filter_complex + mp4_map + " " + mp4_output + " " + server_map + " -f mpegts udp://127.0.0.1:8000/source";


        FFmpegKit.executeAsync(COMMAND, new FFmpegSessionCompleteCallback() {

            @Override
            public void apply(FFmpegSession session) {
                state = session.getState();
                returnCode = session.getReturnCode();
                sessionId = session.getSessionId();

                if(ReturnCode.isSuccess(returnCode) || ReturnCode.isCancel(returnCode)) {

                    if (state.toString().equals("COMPLETED")) {

                        Log.d(TAG, String.format("FFmpeg process exited with state %s and rc %s.%s", state, returnCode, session.getFailStackTrace()));
                        // CALLED WHEN SESSION IS EXECUTED
                        //convertToMp4(m3u8_file_name, mp4_file_name, state, returnCode);
                    }
                }
            }
        },
        log -> {
            // CALLED WHEN SESSION PRINTS LOGS
            Log.d(TAG, "FFmpeg Log: "+ log.getMessage());
        },
        statistics -> {
            // CALLED WHEN SESSION GENERATES STATISTICS
            Log.d(TAG, "FFmpeg statistics: "+ statistics.getBitrate());
        });


    }

    private void stopRecord(){
        FFmpegKit.cancel();
    }
}