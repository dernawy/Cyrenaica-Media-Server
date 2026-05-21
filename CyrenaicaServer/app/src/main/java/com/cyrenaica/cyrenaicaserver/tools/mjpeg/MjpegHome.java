package com.cyrenaica.cyrenaicaserver.tools.mjpeg;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.VideoFrameProcessingException;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.video.VideoSink;
import androidx.media3.ui.PlayerView;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.SessionState;
import com.cyrenaica.cyrenaicaserver.R;
import com.cyrenaica.cyrenaicaserver.tools.RequestSingleton;
import com.cyrenaica.cyrenaicaserver.tools.http_server.HTTPServer;
import com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player.CameraStream;
import com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player.MjpegFaceDetection;
import com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player.VideoProcessing;
import com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player.facerec.SimilarityClassifier;

import org.json.JSONException;
import org.json.JSONObject;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@UnstableApi
public class MjpegHome  extends  AppCompatActivity {

    private static final String TAG = "MJPEG_HOME";
    private static boolean isStreamRunning;

    private MjpegView mv;
    private static final int MENU_QUIT = 1;

    private Thread streamThread;
    private Thread udpThread;

    Core opencv;
    URL url;
    String hostname;
    Socket socket;

    TextView camera_page_title;
    FrameLayout video_frame;
    ImageView record_button;
    ImageView play_button;
    ImageView pause_button;
    ImageView stop_button;

    FrameLayout images_frame;
    ImageView face_image;
    Button face_actions_btn;
    Button add_face_btn;
    Button cancel_save_face_btn;


    FFmpegSession session;
    long sessionId;
    SessionState state;
    ReturnCode returnCode;

    VideoRecording VR = new VideoRecording();
    MediaController mc;
    ExoPlayer player;
    PlayerView p_View;

    String d_name;
    File hls_directory;
    protected static final Handler handler = new Handler(Looper.getMainLooper());

    private static String modelPath;
    ServerSocket httpServerSocket;
    String msgLog = "";
    HTTPServer hs = null;

    VideoProcessing vp;
    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_QUIT, 0, "Quit");
        return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_QUIT:
                finish();
                return true;
        }
        return false;
    }

    @OptIn(markerClass = UnstableApi.class)
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);
        setContentView(R.layout.activity_camera_home3);

        Log.d(TAG, "onCreate");

        camera_page_title    = findViewById(R.id.id_camera_page_label);
        video_frame          = findViewById(R.id.id_video_view_frame);

        record_button        = findViewById(R.id.id_video_record_button);
        play_button          = findViewById(R.id.id_video_play_button);
        pause_button         = findViewById(R.id.id_video_pause_button);
        stop_button          = findViewById(R.id.id_video_stop_button);

        images_frame         = findViewById(R.id.id_images_frame);
        face_image           = findViewById(R.id.id_detected_face_img);
        face_actions_btn     = findViewById(R.id.id_face_detection_action_button);
        add_face_btn         = findViewById(R.id.id_add_face_button);
        cancel_save_face_btn = findViewById(R.id.id_cancel_face_save_btn);

        isStreamRunning = false;

        /* init openCV Module */
        OpenCVLoader.initLocal();

        String segments_file_name =  getApplicationContext().getDir("cyrenaicaVid", Context.MODE_PRIVATE).getPath() + "/hls/" + d_name + "/live-%03d.ts";
        String m3u8_file_name =  getApplicationContext().getDir("cyrenaicaVid", Context.MODE_PRIVATE).getPath() + "/hls/" + d_name + "/" + d_name + "-live.m3u8";
        String mp4_file_name =  getApplicationContext().getDir("cyrenaicaVid", Context.MODE_PRIVATE).getPath() + "/hls/" + "output-live.mp4";


        mv = new MjpegView(this);
        video_frame.addView(mv);

        Uri cam_uri = Uri.parse("http://192.168.1.208/480x320.mjpeg");
        String path = getApplicationContext().getFilesDir().getPath() + "/data";
        CameraStream cameraStream = new CameraStream.Builder()
                .setStreamConfiguration(new CameraStream.StreamConfig.Builder().setUri(cam_uri).useEnableFaceDetection(false).build()) // if here false and in setInputStreamConfiguration() is true then will be true
                .setInputStreamConfiguration(new MjpegInputCamera.InputStreamConfig.Builder().setEnableFaceDetection(true).setResolutionW("480").setResolutionH("320").setConnectionPort(80).build())
                .setFaceDetectionConfiguration(new CameraStream.FaceDetection.Builder().setContext(MjpegHome.this).setActivity(this).setModelPath(path).setModelDirection("FRONTAL_FACE").setModelType("HAAR").setModelName("DEF").build())
                .setFaceConfiguration(new MjpegFaceDetection.Config.Builder().setFaceScaleW(112).setFaceScaleH(112).setScaleFactor(1.2).setMinNeighbors(2).setFlags(0).setImagesFrameLayout(images_frame).setImageView(face_image).setFaceActionsButton(face_actions_btn).setSaveFaceButton(add_face_btn).build()) //.setName("AKRAM").setNamePositionFree(false).setNameFont()
        .build();


        play_button.setOnClickListener(v -> {

            mv = new MjpegView(this);
            video_frame.addView(mv);

            Log.d(TAG, "R WIDTH: [" + MjpegInputCamera.getResolutionW() + "] --- R HEIGHT: [" + MjpegInputCamera.getResolutionH() + "] --- EXTENSION: [" + MjpegInputCamera.getStreamExtension() + "] --- METHOD: [" + MjpegInputCamera.getConnectionMethod() + "] --- HTTP V: [" + MjpegInputCamera.getHttpVersion() + "] --- PORT: [" + MjpegInputCamera.getConnectionPort() + "]");

            final Runnable streamTask = () -> {

                try {

                    mv.setSource(MjpegInputCamera.MjpegInputStream.read(cameraStream.getUri()));

                    mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
                    //mv.showFps(true);
                    mv.showName(true);
                    mv.setDeviceName("BEDROOM");

                    mv.setShowTimeStamp(true);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault(Locale.Category.FORMAT));
                    String time                 = dateFormat.format(new Date()); // Find today's date;
                    //String time = getCurrentTimeStamp();
                    mv.setTimeStamp(time);

                    //mv.setShowPlayTime(true);

                    Log.d(TAG, "AKRAM");

                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };

            this.streamThread = new Thread(streamTask);

            startStream();

            //mv.startPlayTimeTimer();

        });

        record_button.setOnClickListener(v -> {

            // mv.setStartRecording(true);
            // OK BUT SPEEDY
            //String comm = "-y -f mjpeg -i http://192.168.1.208/480x320.mjpeg -movflags +frag_keyframe+separate_moof+omit_tfhd_offset+empty_moov -r 60 -s 1280x720 -vf mpdecimate,setpts=N/60/TB -vcodec libx264 -preset ultrafast -tune zerolatency -b:v 5505K -pix_fmt yuv420p " + ffmpeg_path;

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
            String _name = "BEDROOM";
            String font_file = getApplicationContext().getFilesDir().getPath() + "/font/impact.ttf";
            String name_text_comm = "drawtext=fontsize=24:x=10:y=10" + ":fontfile=" + "'" + font_file  + "'" + ":text=" + name_text;
            String name1_text_comm = "drawtext=fontsize=24:x=10:y=50" + ":fontfile=" + "'" + font_file  + "'" + ":text=" + "'" + "28-03-2025" + "'";
            //OK FOR VideoView
            //String segments_file_name =  getApplicationContext().getDir("cyrenaicaVid", Context.MODE_PRIVATE).getPath() + "/hls/" + _name + "/live-%03d.ts";
            //String m3u8_file_name =  getApplicationContext().getDir("cyrenaicaVid", Context.MODE_PRIVATE).getPath() + "/hls/" + _name + "/" + _name + "-live.m3u8";
            //String mp4_file_name =  getApplicationContext().getDir("cyrenaicaVid", Context.MODE_PRIVATE).getPath() + "/hls/" + "output-live.mp4";
            String map_v = "v:0";
            String map = "-var_stream_map \"" + map_v + "\"";
            String logo_overlay_x = "W-w-10";
            String logo_overlay_y = "10";
            String live_logo_x = "W-w-10";
            String live_logo_y = "H-h-10";
            //String  comm = "-y -f mjpeg -i http://192.168.1.208/480x320.mjpeg -movflags +frag_keyframe+separate_moof+omit_tfhd_offset+empty_moov -r 60 -s 1280x720 -vf \"" + name_text_comm + "\"" + " -vcodec libx264 -preset ultrafast -tune zerolatency -b:v 5505K -pix_fmt yuv420p " + ffmpeg_path;
            //String  comm = "-y -f mjpeg -i http://192.168.1.208/480x320.mjpeg -movflags +frag_keyframe+separate_moof+omit_tfhd_offset+empty_moov -r 60 -s 1280x720 -vf \"" + name_text_comm + "," + name1_text_comm + "\"" + " -vcodec libx264 -preset ultrafast -tune zerolatency -b:v 5505K -pix_fmt yuv420p -flags +cgop -g 60 " + map + " -hls_time 2 -hls_list_size 10 -hls_delete_threshold 10 -hls_flags delete_segments -hls_segment_filename " + segments_file_name + " " + m3u8_file_name;
            //String  comm = "-f mjpeg -i http://192.168.1.208/480x320.mjpeg -movflags +frag_keyframe+separate_moof+omit_tfhd_offset+empty_moov -vf \"" + name_text_comm + "," + name1_text_comm + "\"" + " -vcodec libx264 -r 60 -s 1280x720 -preset ultrafast -tune zerolatency -b:v 5505K -pix_fmt yuv420p -flags +cgop -g 60 " + map + " -hls_time 2 -hls_list_size 10 -hls_delete_threshold 10 -hls_flags delete_segments -hls_segment_filename -y " + segments_file_name + " " + m3u8_file_name;
            //String  comm = "-f mjpeg -i http://192.168.1.208/480x320.mjpeg -movflags +frag_keyframe+separate_moof+omit_tfhd_offset+empty_moov -vcodec libx264 -r 60 -s 1280x720 -preset ultrafast -tune zerolatency -b:v 5505K -pix_fmt yuv420p -flags +cgop -g 60 -q:v 2 -filter_complex [0]scale=size=hd1080[scl];[scl]" + name_text_comm + "," + name1_text_comm + "[v_1]; -map \"" +"[v_1]" + "\"" + " " + mp4_file_name;


            //String mp4_map        = " -map \"" + "[v0]" + "\"" + " -map \"" + "[v1]" + "\"" + " -y -segment_list_flags live -hls_time 2 -hls_list_size 10 -hls_delete_threshold 10 -hls_flags delete_segments -hls_segment_filename " + segments_file_name;
            /*
            String hls_output     = " -y " + m3u8_file_name;*/
            /* String  comm = "-f mjpeg -i http://192.168.1.208/800x600.mjpeg  -vcodec libx264 -r 10 -s 1920x1080 -preset ultrafast -b:v 20000K -pix_fmt yuv420p -flags +cgop -g 60 -filter_complex " +
                    "'" +
                    "[0]" + name_text_comm + "[mp4_name];" + "[mp4_name]" + name1_text_comm + ", scale=1920x1080:flags=neighbor[mp4_txt];" +

                    "[hls_name]" + name_text_comm + "[hls_name]" + ", scale=1920x1080:flags=neighbor[hls_dtime];" +
                    "'" +
                    " -map \"" + "[mp4_dtime]" + "\"" + " -y " + mp4_file_name + " -map \"" + "[hls_dtime]" + "\"" + " -y -hls_time 2 -hls_list_size 10 -hls_delete_threshold 10 -hls_flags delete_segments -hls_segment_filename "+ segments_file_name + " " + m3u8_file_name;*/


            String  cam_input     = "-f mjpeg -re -i http://192.168.1.208/480x320.mjpeg";
            String logo_path      = "/storage/sdcard1/Android/data/com.cyrenaica.cyrenaicaserver/files/IMAGES/KITCHEN/LOGOS/logo.png";
            String live_logo_path = getApplicationContext().getDir("cyrenaicaVid", Context.MODE_PRIVATE).getPath() + "/hls/ic_live-stream.png";
            String  logo_input    = " -i " + logo_path;
            String  live_logo_input = " -i " + live_logo_path;

            String  mof_flags     = " -movflags +frag_keyframe+separate_moof+omit_tfhd_offset+empty_moov";
            String  v_codec       = " -vcodec libx264 -r 10 -s 1920x1080 -preset ultrafast -b:v 20000K -pix_fmt yuv420p -flags +cgop -g 10 -sc_threshold 0";
            String  mp4_codec     = " -vcodec libx264 -r 10 -s 1920x1080 -preset ultrafast -b:v 20000K -pix_fmt yuv420p -flags +cgop -g 30";
            /////////String filter_complex = " -filter_complex " + "'" + "[1]scale=40:40[scl],[2]scale=100:80[lscl],[0:v][scl]overlay=" + logo_overlay_x + ":" + logo_overlay_y + "[ovl];[ovl]" + name_text_comm + "," + name1_text_comm + "[txt];[txt]split=3[s0][s1][s2];[s0]scale=1920x1080:flags=lanczos[s0ovl];[s0ovl][lscl]overlay=" + live_logo_x + ":" + live_logo_y + "[v0];[s1]scale=1920x1080:flags=lanczos[v1];[s2]scale=1920x1080:flags=lanczos[v2]'";

            String filter_complex = " -filter_complex " + "'" + "[1]scale=40:40[scl],[2]scale=100:80[lscl],[0:v][scl]overlay=" + logo_overlay_x + ":" + logo_overlay_y + "[ovl];[ovl]" + name_text_comm + "," + name1_text_comm + "[txt];[txt]split=3[s0][s1];[s0]scale=1920x1080:flags=lanczos[s0ovl];[s0ovl][lscl]overlay=" + live_logo_x + ":" + live_logo_y + "[v0];[s1]scale=1920x1080:flags=lanczos[v1]'";


            //String filter_complex = " -filter_complex " + "'[1]scale=40:40[scl],[0:v][scl]overlay=" + logo_overlay_x + ":" + logo_overlay_y + "[ovl];[ovl]" + name_text_comm + "," + name1_text_comm + "[vid];[vid]scale=1920x1080:flags=neighbor[v0];'";
            String hls_map        = " -f hls -hls_time 2 -hls_playlist_type event -hls_flags independent_segments -hls_segment_type mpegts -http_persistent 1 -hls_allow_cache 1 -hls_list_size 0 -map \"" + "[v0]" + "\"" + " ";
            String server_map     = " -map \"" + "[v1]" + "\"";

            String mp4_map        = " -movflags +frag_keyframe+separate_moof+omit_tfhd_offset+empty_moov" + mp4_codec + " -map \"" + "[v0]" + "\"";
            String mp4_output     = " -y " + mp4_file_name;


            //String  COMMAND = cam_input + logo_input + mof_flags + v_codec + filter_complex + mp4_map + " -f tee " + mp4_output + "|[f=nut]-ffplay";

            //String  COMMAND = cam_input + logo_input + mof_flags + v_codec + filter_complex + mp4_map + mp4_output; // OK FOR mp4record
            //String  COMMAND = cam_input + logo_input + live_logo_input + filter_complex + v_codec + hls_map + m3u8_file_name + mp4_map + " " + mp4_output + " " + server_map + " -f mpegts udp://127.0.0.1:4711/live/video";
            String  COMMAND = cam_input + logo_input + live_logo_input + filter_complex + mp4_map + " " + mp4_output + " " + server_map + " -f mpegts udp://127.0.0.1:8000/source";


            Log.d(TAG, "COMMAND: " + COMMAND);


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
            }, log -> {

                // CALLED WHEN SESSION PRINTS LOGS
                Log.d(TAG, "FFmpeg Log: "+ log);

            }, statistics -> {

                // CALLED WHEN SESSION GENERATES STATISTICS
                Log.d(TAG, "FFmpeg statistics: "+ statistics.getBitrate());

            });
        });

        pause_button.setOnClickListener(v -> {

            if(MjpegInputCamera.isFaceDetectionEnabled()){
                new CameraStream.Builder().setInputStreamConfiguration(new MjpegInputCamera.InputStreamConfig.Builder().setEnableFaceDetection(false).build()).build();
            }
            else
            {
                new CameraStream.Builder().setInputStreamConfiguration(new MjpegInputCamera.InputStreamConfig.Builder().setEnableFaceDetection(true).build()).build();
            }


            //if(mv != null){
               // mv.stopPlayback();
           // }


           /* try {
               // MjpegInputStream.stopSockets();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }*/
           // mv = new MjpegView(this);
            //playVideo(getApplicationContext());
            //vp.Play();

        });

        stop_button.setOnClickListener(v -> {

            //FFmpegKit.cancel();

            try {
                    MjpegInputCamera.MjpegInputStream.stopSockets();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

            stopStream();
        });
    }

    private static void addUIAction(final Runnable runnable) {
        handler.post(runnable);
    }

    /* The system calls this method as the first indication that the user is leaving your activity
        (though it does not always mean the activity is being destroyed);
        it indicates that the activity is no longer in the foreground (though it may still be visible if the user is in multi-window mode).
    */
    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "onPause");

       mv.stopPlayback(video_frame);
    }

    /* When the activity enters the Started state, the system invokes this callback. The onStart() call makes the activity visible to the user,
        as the app prepares for the activity to enter the foreground and become interactive.
    */
    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

    }

    /* When the activity enters the Resumed state, it comes to the foreground, and then the system invokes the onResume() callback */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

    }

    /* When your activity is no longer visible to the user, it has entered the Stopped state, and the system invokes the onStop() callback. */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");

        stopStream();
    }


    public void HttpClient() throws UnknownHostException, IOException, InterruptedException {
        url = new URL("http://10.10.10.11");
        hostname = url.getHost();

        int port = 80;

        System.out.println(url.getUserInfo());


        socket = new Socket(hostname, port);
        PrintWriter output = new PrintWriter(socket.getOutputStream());
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        String x = "480x320.mjpeg";

        output.write(
            String.format(
                "GET /" + x + " HTTP/2\r\n"
                + "Host: " + hostname + "\r\n"
                + "Connection: keep-alive\r\n"
                + "\r\n"
            )
        );

        output.flush();

        String line;

        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        Thread.sleep(200);

    }

	public void resetDevice(final Context context, String jsonUrl, String question, String where){

        Map<String, String> postParam = new HashMap<String, String>();

        postParam.put("QUESTION", question);
        postParam.put("CONTROL", "RESET_DEVICE");

        RetryPolicy mRetryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, jsonUrl, new JSONObject(postParam), response -> {

            System.out.println("DEVICE SETUP CHECK DATA RESPONSE: " + response.toString());

            if(!response.toString().isEmpty()) {

                try {

                    JSONObject jsonObject    = new JSONObject(response.toString());
                    JSONObject main_response = new JSONObject(jsonObject.getString("RESPONSE"));
                    String device_status     = main_response.getString("STATUS");

                    if(device_status.equals("RESET_DEVICE_OK")){

                       


                    }

                    if(device_status.equals("RESET_DEVICE_KO")){

                        


                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },

        error -> System.out.println()) {
            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Allow", "POST");
                headers.put("Connection","keep-alive");
                headers.put("Accept", "*/*");
                headers.put("Accept-Encoding", "gzip, deflate, br");

                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(mRetryPolicy);

        RequestSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    private void startStream(){
        this.streamThread.start();
        //this.udpThread.start();
    }

    private void stopStream(){

        if(isStreamRunning) {
            mv.stopPlayback(video_frame);
            isStreamRunning = false;
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    public void playVideo(Context context) throws VideoSink.VideoSinkException, IOException, VideoFrameProcessingException {

        /*Uri udp_url = Uri.parse("udp://localhost:8000/source");
        vp.setSourceUri(udp_url);

        vp.initPlayer();



        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(context, udp_url, null);

        player = new ExoPlayer.Builder(getApplicationContext()).build();

        p_View.setPlayer(player);
        player.setPlayWhenReady(true);

        StreamItem mediaItem = new StreamItem.Builder().setUri(udp_url).build();
        UdpStreamSource udpS = new UdpStreamSource(3000, 50000);


        MediaItem mediaItem = new MediaItem.Builder().setUri(udp_url).build();
        DataSource.Factory udp_factory = () -> new UdpDataSource(3000, 50000);
        ProgressiveMediaSource udpMediaSource = new ProgressiveMediaSource.Factory(udp_factory).createMediaSource(mediaItem);
        player.setMediaSource(udpMediaSource);*/


        // Prepare the player.
        player.prepare();
        // Start the playback.
        player.play();
        //player.seekToDefaultPosition();



        player.addListener(

            new Player.Listener() {

                @Override
                public void onEvents(@NonNull Player player, @NonNull Player.Events events) {

                    if (events.contains(Player.EVENT_TRACKS_CHANGED)) {
                        // If no video or image track: show shutter, hide image view.
                        // Otherwise: do nothing to wait for first frame or image.
                        Log.d(TAG, "EVENT: EVENT_TRACKS_CHANGED");
                    }

                    if (events.contains(Player.EVENT_RENDERED_FIRST_FRAME)) {
                        // Hide shutter, hide image view.
                        Log.d(TAG, "EVENT: EVENT_RENDERED_FIRST_FRAME");

                    }
                }



                @Override
                public void onIsPlayingChanged(boolean isPlaying) {

                    if (isPlaying) {

                        Log.d(TAG, "EVENT: isPlaying");

                    }
                    else
                    {
                        // Not playing because playback is paused, ended, suppressed, or the player
                        // is buffering, stopped or failed. Check player.getPlayWhenReady,
                        // player.getPlaybackState, player.getPlaybackSuppressionReason and
                        // player.getPlaybackError for details.
                    }
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    @Nullable Throwable cause = error.getCause();

                }
            }


        );

        /*lastBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);

        Mat in_mat = new Mat();

        Utils.bitmapToMat(lastBitmap, in_mat);

        Mat out_mat = Imgcodecs.imdecode(in_mat, Imgcodecs.IMREAD_UNCHANGED);

        recognizeFaces(out_mat);

        Utils.matToBitmap(out_mat, lastBitmap);

        Canvas canvas = new Canvas(checkNotNull(lastBitmap));
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        return checkNotNull(lastBitmap);*/


    }

    private void convertToMp4(String in_path, String out_path, SessionState s, ReturnCode r){

        String command = "-f hls -i " + in_path + " -codec:v libx264 -preset ultrafast " +  out_path;

        Log.d(TAG, String.format("CONVERT - ENCODING state %s and rc %s", s, ReturnCode.isCancel(r)));


                FFmpegKit.executeAsync(command, new FFmpegSessionCompleteCallback() {

                    @Override
                    public void apply(FFmpegSession session) {
                        SessionState st = session.getState();
                        ReturnCode ret = session.getReturnCode();
                        sessionId = session.getSessionId();

                        // CALLED WHEN SESSION IS EXECUTED


                        Log.d(TAG, String.format("FFmpeg process exited with state %s and rc %s.%s", state, returnCode, session.getFailStackTrace()));
                    }
                }, log -> {

                    // CALLED WHEN SESSION PRINTS LOGS
                    Log.d(TAG, "FFmpeg Log: " + log);

                }, statistics -> {

                    // CALLED WHEN SESSION GENERATES STATISTICS
                    Log.d(TAG, "FFmpeg statistics: " + statistics.getBitrate());
                });

    }

    public static String getCurrentTimeStamp(){
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault(Locale.Category.FORMAT));
            //String currentDateTime = dateFormat.format(new Date()); // Find today's date

            return dateFormat.format(new Date()); // Find today's date;
        }
        catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: " + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }
    private class HttpServerThread extends Thread {

        static final int HttpServerPORT = 8888;

        @Override
        public void run() {
            Socket socket = null;

            try {
                httpServerSocket = new ServerSocket(HttpServerPORT);


                while (true) {
                    socket = httpServerSocket.accept();

                    HttpResponseThread httpResponseThread = new HttpResponseThread(socket, "SERVER OK");
                    httpResponseThread.start();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    private class HttpResponseThread extends Thread {

        Socket socket;
        String h1;

        HttpResponseThread(Socket socket, String msg){
            this.socket = socket;
            h1 = msg;
        }

        @Override
        public void run() {
            BufferedReader is;
            PrintWriter os;
            String request;


            try {
                is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                request = is.readLine();

                os = new PrintWriter(socket.getOutputStream(), true);

                String response =
                        "<html><head></head>" +
                                "<body>" +
                                "<h1>" + h1 + "</h1>" +
                                "</body></html>";

                os.print("HTTP/1.0 200" + "\r\n");
                os.print("Content type: text/html" + "\r\n");
                os.print("Content length: " + response.length() + "\r\n");
                os.print("\r\n");
                os.print(response + "\r\n");
                os.flush();
                socket.close();


                msgLog += "Request of " + request
                        + " from " + socket.getInetAddress().toString() + "\n";
                MjpegHome.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        Log.w(TAG, msgLog);
                    }
                });

            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return;
        }
    }

    public static void CvVideo(String url) {

        VideoCapture camera = new VideoCapture();
        camera.open(url);
        Mat frame = new Mat();

        for (;;) {
            camera.read(frame); //reads captured frame into the Mat image

        }
    }
}
