package com.cyrenaica.cyrenaicaserver.tools.mjpeg;

import android.content.ContextWrapper;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.SessionState;
import com.cyrenaica.cyrenaicaserver.R;
import com.cyrenaica.cyrenaicaserver.tools.Tools;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;


@UnstableApi
public class MjpegView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "MJPEG_VIEW";
    Context mContext;

    public final static int POSITION_UPPER_LEFT  = 9;
    public final static int POSITION_UPPER_RIGHT = 3;
    public final static int POSITION_LOWER_LEFT  = 12;
    public final static int POSITION_LOWER_RIGHT = 6;

    public final static int SIZE_SELF       = 0;
    public final static int SIZE_STANDARD   = 1;
    public final static int SIZE_BEST_FIT   = 4;
    public final static int SIZE_FULLSCREEN = 8;

    private MjpegViewThread thread;
    private MjpegInputCamera.MjpegInputStream mIn = null;
    private boolean showFps = false;
    private boolean showName = false;
    private boolean startRecord = false;
    private boolean showTimeStamp = false;
    private boolean showRecordTime = false;
    private boolean showPlayTime = false;


    private boolean mRun = false;
    private boolean surfaceDone = false;

    private int overlayWidth;
    private int overlayHeight;

    private String DEVICE_NAME; // for DEVICE NAME
    private Paint nameOverlayPaint; // for DEVICE NAME
    private int nameBackgroundColor; // for DEVICE NAME
    private int nameTextColor; // for DEVICE NAME
    private int nameTextSize; // for DEVICE NAME
    public float nameScaleX; // for DEVICE NAME
    private int nameOvlPos; // for DEVICE NAME
    public int namePosMarginX; // for DEVICE NAME
    public int namePosMarginY; // for DEVICE NAME


    private String TIME_STAMP; // for TIME STAMP
    private Paint timeStampOverlayPaint; // for TIME STAMP
    private int timeStampBackgroundColor; // for TIME STAMP
    private int timeStampTextColor; // for TIME STAMP
    private int timeStampTextSize; // for TIME STAMP
    public float timeStampScaleX; // for TIME STAMP
    private int timeStampOvlPos; // for TIME STAMP
    public int timeStampPosMarginX; // for TIME STAMP
    public int timeStampPosMarginY; // for TIME STAMP

    private String PLAY_TIME; // for PLAY TIME
    private Paint playTimeOverlayPaint; // for PLAY TIME
    private int playTimeBackgroundColor; // for PLAY TIME
    private int playTimeTextColor; // for PLAY TIME
    private int playTimeTextSize; // for PLAY TIME
    public float playTimeScaleX; // for PLAY TIME
    private int playTimeOvlPos; // for PLAY TIME
    public int playTimePosMarginX; // for PLAY TIME
    public int playTimePosMarginY; // for PLAY TIME
    Double playTime = 0.0;
    Timer playTimeTimer;
    TimerTask playTimeTimerTask;

    boolean playTimTimerStarted = false;


    private Paint overlayPaint; // for FPS
    private int overlayTextColor; // for FPS
    private int overlayBackgroundColor; // for FPS
    private int ovlPos; // for FPS

    private int dispWidth;
    private int dispHeight;
    private int displayMode;
    public MediaRecorder mediaVideoRecorde = new MediaRecorder();
    SurfaceHolder holder;
    VideoRecording vr;
    VideoEncoder vn;
    FFmpegSession session;
    long sessionId;
    SessionState state;
    ReturnCode returnCode;

    public class MjpegViewThread extends Thread {

        private final SurfaceHolder mSurfaceHolder;
        private int frameCounter = 0;
        private long start;
        private long timeStampStart;


        private Bitmap ovl;
        private Bitmap nameOvl;
        private Bitmap timeStampOvl;
        private Bitmap playTimeOvl;

        boolean recordStarted;

        public MjpegViewThread(SurfaceHolder surfaceHolder, Context context) {
            mSurfaceHolder = surfaceHolder;
        }

        private Rect destRect(int bmw, int bmh) {

            int tempx;
            int tempy;

            if (displayMode == MjpegView.SIZE_SELF){

                float bmasp = (float) bmw / (float) bmh;
                bmw = dispWidth;
                bmh = (int) (dispWidth / bmasp);
                tempx = 0;
                tempy = 0;
                // Log.d(TAG, "[SIZE_STANDARD] - X: " + tempx + " - Y: " + tempy + " - W: " + bmw + " - H: " + bmh);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }

            if (displayMode == MjpegView.SIZE_STANDARD) {
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
               // Log.d(TAG, "[SIZE_STANDARD] - X: " + tempx + " - Y: " + tempy + " - W: " + bmw + " - H: " + bmh);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }

            if (displayMode == MjpegView.SIZE_BEST_FIT) {

                float bmasp = (float) bmw / (float) bmh;

                bmw = dispWidth;
                bmh = (int) (dispWidth / bmasp);

                if (bmh > dispHeight) {
                    bmh = dispHeight;
                    bmw = (int) (dispHeight * bmasp);
                }

                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = 0; //(dispHeight / 2) - (bmh / 2);

                return new Rect(tempx, tempy, (bmw + tempx), (bmh + tempy));
            }

            if (displayMode == MjpegView.SIZE_FULLSCREEN) return new Rect(0, 0, dispWidth, dispHeight);
            return null;
        }

        public void setSurfaceSize(int width, int height) {
            synchronized(mSurfaceHolder) {
                dispWidth = width;
                dispHeight = height;
            }
        }

        private Bitmap makeNameOverlay(Paint p, String text) {

            Rect b      = new Rect();
            p.getTextBounds(text, 0, text.length(), b);
            int bwidth  = b.width() + 4;
            int bheight = b.height() + 4;
            Bitmap bm   = Bitmap.createBitmap(bwidth, bheight, Bitmap.Config.ARGB_8888);
            Canvas c    = new Canvas(bm);

            p.setColor(nameBackgroundColor);
            //c.drawRect(0, 0, bwidth, bheight, p);
            p.setColor(nameTextColor);
            p.setTextSize(nameTextSize);
            p.setTextScaleX(nameScaleX);
            c.drawText(text, -b.left + 1, ((float) bheight / 2) - ((p.ascent()+p.descent()) / 2) + 1, p);
            return bm;
        }

        private Bitmap makeTimeStampOverlay(Paint p, String text) {
            Rect b = new Rect();
            p.getTextBounds(text, 0, text.length(), b);
            int bwidth  = b.width() + 4;
            int bheight = b.height() + 4;
            Bitmap bm   = Bitmap.createBitmap(bwidth, bheight, Bitmap.Config.ARGB_8888);
            Canvas c    = new Canvas(bm);
            p.setColor(timeStampBackgroundColor);
            //c.drawRect(0, 0, bwidth, bheight, p);
            p.setColor(timeStampTextColor);
            p.setTextSize(timeStampTextSize);
            p.setTextScaleX(timeStampScaleX);
            c.drawText(text, -b.left + 1, ((float) bheight / 2) - ((p.ascent()+p.descent()) / 2) + 1, p);
            return bm;
        }

        private Bitmap makePlayTimeOverlay(Paint p, String text) {
            Rect b = new Rect();
            p.getTextBounds(text, 0, text.length(), b);
            int bwidth  = b.width() + 4;
            int bheight = b.height() + 4;
            Bitmap bm   = Bitmap.createBitmap(bwidth, bheight, Bitmap.Config.ARGB_8888);
            Canvas c    = new Canvas(bm);
            p.setColor(playTimeBackgroundColor);
            c.drawRect(0, 0, bwidth, bheight, p);
            p.setColor(playTimeTextColor);
            p.setTextSize(playTimeTextSize);
            p.setTextScaleX(playTimeScaleX);
            c.drawText(text, -b.left + 1, ((float) bheight / 2) - ((p.ascent()+p.descent()) / 2) + 1, p);
            return bm;
        }

        private Bitmap makeFpsOverlay(Paint p, String text) {
            Rect b = new Rect();
            p.getTextBounds(text, 0, text.length(), b);
            int bwidth  = b.width()+2;
            int bheight = b.height()+2;
            Bitmap bm = Bitmap.createBitmap(bwidth, bheight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bm);
            p.setColor(overlayBackgroundColor);
            c.drawRect(0, 0, bwidth, bheight, p);
            p.setColor(overlayTextColor);
            c.drawText(text, -b.left+1, (bheight/2)-((p.ascent()+p.descent())/2)+1, p);
            return bm;
        }

        public void run() {

            start = System.currentTimeMillis();
            timeStampStart =  System.currentTimeMillis();

            PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);
            Bitmap bm;
            int width;
            int height;
            Rect destRect;
            Canvas c   = null;
            Paint p    = new Paint();
            String fps = "";

            //String path = getVideoSaveInternalStoragePath();

            while (mRun) {

                if(surfaceDone) {

                    try {

                        c = mSurfaceHolder.lockCanvas(null);

                        synchronized (mSurfaceHolder) {

                            try {

                                bm = mIn.readMjpegFrame();

                                destRect = destRect(bm.getWidth(), bm.getHeight());

                                assert c != null;
                                c.drawColor(Color.BLACK);

                                assert destRect != null;
                                c.drawBitmap(bm, null, destRect, p);

                                if(showFps) {

                                    p.setXfermode(mode);

                                    if(ovl != null) {
                                        height = ((ovlPos & 1) == 1) ? destRect.top : destRect.bottom - ovl.getHeight();
                                        width  = ((ovlPos & 8) == 8) ? destRect.left : destRect.right - ovl.getWidth();

                                        c.drawBitmap(ovl, width, height, null);
                                    }

                                    p.setXfermode(null);

                                    frameCounter++;

                                    if((System.currentTimeMillis() - start) >= 1000) {
                                        fps = String.valueOf(frameCounter)+"fps";
                                        frameCounter = 0;
                                        start = System.currentTimeMillis();
                                        ovl = makeFpsOverlay(overlayPaint, fps);
                                    }
                                }

                                if(showName){

                                    if(nameOvl != null) {
                                        height = ((nameOvlPos & 1) == 1) ? destRect.top : destRect.bottom - nameOvl.getHeight();
                                        width  = ((nameOvlPos & 8) == 8) ? destRect.left : destRect.right - nameOvl.getWidth();

                                        width  = width + namePosMarginX;
                                        height = height + namePosMarginX;

                                        c.drawBitmap(nameOvl, width, height, null);
                                    }

                                    nameOvl = makeNameOverlay(nameOverlayPaint, DEVICE_NAME);
                                }

                                if(showTimeStamp){

                                    if(timeStampOvl != null) {

                                        height = ((timeStampOvlPos & 1) == 1) ? destRect.top : destRect.bottom - timeStampOvl.getHeight();
                                        width  = ((timeStampOvlPos & 8) == 8) ? destRect.left : destRect.right - timeStampOvl.getWidth();

                                        width  = width + timeStampPosMarginX;
                                        height = height + timeStampPosMarginY;

                                        c.drawBitmap(timeStampOvl, width, height, null);
                                    }

                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault(Locale.Category.FORMAT));

                                    if((System.currentTimeMillis() - timeStampStart) >= 1000) {
                                        TIME_STAMP = dateFormat.format(new Date()); // Find today's date;
                                        timeStampStart = System.currentTimeMillis();
                                        timeStampOvl = makeTimeStampOverlay(timeStampOverlayPaint, TIME_STAMP);
                                    }
                                }

                                if(showPlayTime){

                                    if(playTimeOvl != null) {

                                        height = ((playTimeOvlPos & 1) == 1) ? destRect.top : destRect.bottom - playTimeOvl.getHeight();
                                        width  = ((playTimeOvlPos & 8) == 8) ? destRect.left : destRect.right - playTimeOvl.getWidth();

                                        width  = width + playTimePosMarginX;
                                        height = height + playTimePosMarginY;

                                        c.drawBitmap(playTimeOvl, width, height, null);
                                    }

                                    playTimeOvl = makePlayTimeOverlay(playTimeOverlayPaint, PLAY_TIME);
                                }

                                if(startRecord){
                                   // vr.convertImageToVideo(bm, bm.getWidth(), bm.getHeight());
                                    //String comm = "-f mjpeg -r 15 -i 'http://192.168.1.208/480x320.mjpeg' -codec copy -map 0 -f segment -segment_time 900 -segment_atclocktime 1 " + getVideoSaveInternalStoragePath();
                                    //String comm = "-f mjpeg -i http://192.168.1.208/480x320.mjpeg -c:v libx264 -preset veryslow -crf 18 " + path;
                                    //String comm = "-f mjpeg -i http://192.168.1.208/480x320.mjpeg -codec -c:v copy -map 0 -f segment -strftime 1 -segment_time 300 -segment_format mp4 -reset_timestamps 1 " + path;
                                    //String comm = "-f mjpeg -i http://192.168.1.208/480x320.mjpeg -an -vcodec mjpeg " + path;
                                    //String comm = "-f mjpeg -i http://192.168.1.208/480x320.mjpeg -movflags +frag_keyframe+separate_moof+omit_tfhd_offset+empty_moov -c:v mjpeg -q:v 3 -an " + path;

                                    //Log.d(TAG, "FFMPEG_COMMEND: " + comm);

                                    //if(!recordStarted){
                                       // session = FFmpegKit.execute(comm);
                                        // Unique session id created for this execution
                                       // sessionId = session.getSessionId();

                                       // recordStarted = true;
                                    //}


                                    // State of the execution. Shows whether it is still running or completed
                                    //state = session.getState();

                                    // Return code for completed sessions. Will be null if session is still running or ends with a failure
                                    //returnCode = session.getReturnCode();

                                }
                            }
                            catch (IOException e) {}
                        }
                    }
                    finally
                    {
                        if (c != null)
                            mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }

    public void playVideo(Context context, VideoView v){
        String SAVE_PATH = Tools.getStringPref(context, "CURRENT_REC_VID");
        Log.e(TAG, "PLAY PATH -------- " + SAVE_PATH);

        Uri video = Uri.parse(SAVE_PATH);
        v.setVideoURI(video);

        MediaController mc = new MediaController(context);
        v.setMediaController(mc);

        mc.setAnchorView(v);
        //mc.setMediaPlayer(v);
        v.start();
    }

    public String getVideoSaveInternalStoragePath() {

        String tempPath = "";

        try {

            ContextWrapper cw = new ContextWrapper(mContext);
            File directory = cw.getDir("cyrenaicaVid", Context.MODE_PRIVATE);

            tempPath = directory.getPath();
            String SAVE_PATH = tempPath + "/CAM_" + System.currentTimeMillis() + ".mp4";
            Tools.setStringPref(mContext, "CURRENT_REC_VID", SAVE_PATH);
            Log.d(TAG, "SAVE_PATH: " + SAVE_PATH);

            return SAVE_PATH;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getHlsPath(String name) {

        String tempPath = "";

        try {

            ContextWrapper cw = new ContextWrapper(mContext);
            File directory = cw.getDir("cyrenaicaVid", Context.MODE_PRIVATE);

            tempPath = directory.getPath();
            String SAVE_PATH = tempPath + "/CAM_" + System.currentTimeMillis() + ".mp4";
            Tools.setStringPref(mContext, "CURRENT_REC_VID", SAVE_PATH);
            Log.d(TAG, "SAVE_PATH: " + SAVE_PATH);

            return SAVE_PATH;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    private void init(Context context) {

        mContext = context;

        holder = getHolder();
        holder.setFixedSize(getWidth(), 800);
        holder.addCallback(this);

        Log.d(TAG, "INIT: " +  " --------------[X] -> ");


        thread = new MjpegViewThread(holder, mContext);
        setFocusable(true);

        /* Fps init */
        overlayPaint           = new Paint();
        overlayPaint.setTextAlign(Paint.Align.LEFT);
        overlayPaint.setTextSize(20);
        overlayPaint.setTypeface(Typeface.DEFAULT);
        overlayTextColor       = Color.WHITE;
        overlayBackgroundColor = Color.BLACK;
        ovlPos                 = MjpegView.POSITION_LOWER_RIGHT;

        /* Device name init */
        nameOverlayPaint       = new Paint();
        nameOverlayPaint.setTextAlign(Paint.Align.LEFT);
        nameTextColor          = Color.WHITE;
        nameTextSize           = 30;
        nameScaleX             = 2.8F;
        nameOverlayPaint.setTextSize(nameTextSize);
        nameOverlayPaint.setTextScaleX(nameScaleX);
        nameOverlayPaint.setTypeface(Typeface.DEFAULT);
        nameBackgroundColor    = Color.BLACK;
        nameOvlPos             = MjpegView.POSITION_UPPER_LEFT;
        namePosMarginX         = 10;
        namePosMarginY         = 10;

        /* Time stamp init */
        TIME_STAMP                  = "";
        timeStampOverlayPaint       = new Paint();
        timeStampOverlayPaint.setTextAlign(Paint.Align.LEFT);
        timeStampTextColor          = Color.WHITE;
        timeStampTextSize           = 20;
        timeStampScaleX             = 2.2F;
        timeStampOverlayPaint.setTextSize(nameTextSize);
        timeStampOverlayPaint.setTextScaleX(nameScaleX);
        timeStampOverlayPaint.setTypeface(Typeface.DEFAULT);
        timeStampBackgroundColor    = Color.BLACK;
        timeStampOvlPos             = MjpegView.POSITION_UPPER_RIGHT;
        timeStampPosMarginX         = -10;
        timeStampPosMarginY         = 10;

        /* Play time init */
        PLAY_TIME                  = "";
        playTimeOverlayPaint       = new Paint();
        playTimeOverlayPaint.setTextAlign(Paint.Align.LEFT);
        playTimeTextColor          = Color.WHITE;
        playTimeTextSize           = 20;
        playTimeScaleX             = 2.2F;
        playTimeOverlayPaint.setTextSize(nameTextSize);
        playTimeOverlayPaint.setTextScaleX(nameScaleX);
        playTimeOverlayPaint.setTypeface(Typeface.DEFAULT);
        playTimeBackgroundColor    = Color.BLACK;
        playTimeOvlPos             = MjpegView.POSITION_LOWER_LEFT;
        playTimePosMarginX         = 10;
        playTimePosMarginY         = -10;


        displayMode            = MjpegView.SIZE_SELF;
        dispWidth              = getWidth();
        dispHeight             = getHeight();

    }

    public void startPlayback() {

        if(mIn != null) {
            mRun = true;

            if(thread != null && !thread.isAlive()){
                Log.d(TAG, "START: " +  " --------------[X] -> ");
                thread.start();
            }
        }
    }

    public void stopPlayback(FrameLayout view) {
        mRun          = false;
        boolean retry = true;

        while(retry) {

            try {

                stopPlayTimeTimerTask();
                view.setBackgroundColor(getResources().getColor(R.color.black));
                if(thread != null){
                    thread.join();
                }

                thread = null;
                view.removeView(this);

                //view.setForeground();
                retry = false;
            }
            catch (InterruptedException e) {
                Log.e(TAG, "Failed while trying stop player", e);
            }
        }
    }

    public void endRecord() {
        FFmpegKit.cancel(sessionId);
    }

    public void startRecording(){
        vr.startingRecording();
    }

    public void stopRecording(){
        vr.stopRecording();
    }

    public MjpegView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void surfaceChanged(@NonNull SurfaceHolder holder, int f, int w, int h) { if(thread != null){thread.setSurfaceSize(w, h);} }

    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        surfaceDone = false;
    }

    public MjpegView(Context context) {
        super(context);
        init(context);
    }

    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        surfaceDone = true;
    }

    @OptIn(markerClass = UnstableApi.class)
    public void setSource(MjpegInputCamera.MjpegInputStream source) {

        if(source != null){
            Log.d(TAG, "SET SOURCE: " +  " --------------[X] -> ");
        }

        mIn = source;
        startPlayback();
    }

    public void setDisplayMode(int s) {
        displayMode = s;
    }

    /* -----------------------------------------  DEVICE NAME  ---------------------------------------------------*/
    public void showName(boolean show) {
        showName = show;
    }

    public void setDeviceName(String n) {
        DEVICE_NAME = n;
    }

    public void setNameXYMargin(int x, int y){
        namePosMarginX = x;
        namePosMarginY = y;
    }

    public void setNameXMargin(int x){
        namePosMarginX = x;
    }

    public void setNameYMargin(int y){
        namePosMarginY = y;
    }

    public void setNameTextSize(int size){
        nameTextSize = size;
    }

    public void setNameScaleX(int scale){
        nameScaleX = scale;
    }

    public void setNameTextColor(int color /* Ex: Color.WHITE */){
        nameTextColor = color;
    }

    public void setNameBackgroundColor(int c) {
        nameBackgroundColor = c;
    }

    /* -----------------------------------------  TIME STAMP  ---------------------------------------------------*/

    public void setTimeStamp(String t) {
        TIME_STAMP = t;
    }
    public void setShowTimeStamp(boolean show){
        showTimeStamp = show;
    }

    public void setTimeStampXYMargin(int x, int y){
        timeStampPosMarginX = x;
        timeStampPosMarginY = y;
    }

    public void setTimeStampXMargin(int x){
        timeStampPosMarginX = x;
    }

    public void setTimeStampYMargin(int y){
        timeStampPosMarginY = y;
    }

    public void setTimeStampTextSize(int size){
        timeStampTextSize = size;
    }

    public void setTimeStampScaleX(int scale){
        timeStampScaleX = scale;
    }

    public void setTimeStampTextColor(int color /* Ex: Color.WHITE */){
        timeStampTextColor = color;
    }

    public void setTimeStampBackgroundColor(int c) {
        timeStampBackgroundColor = c;
    }

    /* -----------------------------------------  RECORD TIME  ---------------------------------------------------*/
    public void setShowRecordTime(boolean show){
        showRecordTime = show;
    }

    public void setStartRecording(boolean start){
        startRecord = start;
    }

    /* -----------------------------------------  PLAY TIME  ---------------------------------------------------*/
    public void setShowPlayTime(boolean show){
        showPlayTime = show;
    }

    public void setPlayTimeXYMargin(int x, int y){
        playTimePosMarginX = x;
        playTimePosMarginY = y;
    }

    public void setPlayTimeXMargin(int x){
        playTimePosMarginX = x;
    }

    public void setPlayTimeYMargin(int y){
        playTimePosMarginY = y;
    }

    public void setPlayTimeTextSize(int size){
        playTimeTextSize = size;
    }

    public void setPlayTimeScaleX(int scale){
        playTimeScaleX = scale;
    }

    public void setPlayTimeTextColor(int color /* Ex: Color.WHITE */){
        playTimeTextColor = color;
    }

    public void setPlayTimeBackgroundColor(int c) {
        playTimeBackgroundColor = c;
    }

    public void startPlayTimeTimer() {

        playTimeTimer = new Timer();

        playTimeTimer.schedule(new TimerTask() {

            @Override
            public void run() {

                Handler handler = new Handler(Looper.getMainLooper()) {

                    public void handleMessage(Message msg) {


                        playTime++;
                        PLAY_TIME = getTimerText();

                    }
                };

                handler.sendEmptyMessage(1);
            }

        }, 100, 1000);

    }

    public void stopPlayTimeTimerTask() {

        if ( playTimeTimer != null ) {
            playTimeTimer.cancel();
            playTimeTimer = null;
        }
    }

    private String getTimerText() {

        int rounded = (int) Math.round(playTime);

        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = ((rounded % 86400) / 3600);

        return formatTime(seconds, minutes, hours);
    }

    private String formatTime(int seconds, int minutes, int hours) {
        return String.format( Locale.getDefault(Locale.Category.FORMAT), "%02d",hours) + " : " + String.format( Locale.getDefault(Locale.Category.FORMAT), "%02d",minutes) + " : " + String.format( Locale.getDefault(Locale.Category.FORMAT), "%02d",seconds);
    }

    /* -----------------------------------------  FPS  ---------------------------------------------------*/
    public void showFps(boolean b) {
        showFps = b;
    }

    public void setOverlayWidthHeight(int w, int h) {
        overlayWidth = w;
        overlayHeight = h;
    }

    public void setOverlayPaint(Paint p) {
        overlayPaint = p;
    }

    public void setOverlayTextColor(int c) {
        overlayTextColor = c;
    }

    public void setOverlayBackgroundColor(int c) {
        overlayBackgroundColor = c;
    }

    public void setOverlayPosition(int p) {
        ovlPos = p;
    }


}
