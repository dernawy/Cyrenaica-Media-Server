package com.cyrenaica.cyrenaicaserver.tools.mjpeg;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.camera2.CaptureRequest;
import android.media.CamcorderProfile;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.MediaController;
import android.widget.VideoView;

import com.cyrenaica.cyrenaicaserver.tools.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

public class VideoRecording {

    private static final String TAG = "VIDEO_RECORDER";

    Context pContext;
    SurfaceHolder mRecordFromHolder;
    SurfaceHolder mPreviewToHolder;

    private String DEVICE_NAME;

    private boolean sub_preview;

    public MediaRecorder mediaVideoRecorde = new MediaRecorder();
    private boolean record_audio;
    private String VIDEO_SOURCE;
    private String AUDIO_SOURCE;

    private boolean save_file_internal;
    private String DIR_PATH; // without start (/)
    private String FILE_NAME; // without name point (.)
    private String FILE_EXT; // without ext point (.)
    private int FILE_MAX_SIZE_MB;

    private String SAVE_PATH;

    private static int MAX_DURATION;
    private int VIDEO_SIZE_W;
    private int VIDEO_SIZE_H;

    private CaptureRequest.Builder mPreviewBuilder;
    MediaMuxer muxer;
    MediaCodec codec;
    boolean muxerStarted = false;
    boolean codecStarted = false;
    int videoTrackIndex = -1;

    public VideoRecording() {

    }

    public String getPath(){
        return SAVE_PATH;
    }

    public VideoRecording(Context context, SurfaceHolder fromHolder, boolean saveFileInternal){
        pContext           = context;
        DEVICE_NAME        = "CAM";
        mRecordFromHolder  = fromHolder;
        save_file_internal = saveFileInternal;
        sub_preview        = false;
        VIDEO_SOURCE       = "SURFACE";
        AUDIO_SOURCE       = "DEFAULT";
        VIDEO_SIZE_W       = 1080;
        VIDEO_SIZE_H       = 720;
        MAX_DURATION       = 100000; // 10 seconds by default
        FILE_NAME          = DEVICE_NAME + "_" + System.currentTimeMillis();
        FILE_EXT           = "mp4";
        FILE_MAX_SIZE_MB   = 50000000; // 50MB
    }

    public VideoRecording(Context context, SurfaceHolder fromHolder, SurfaceHolder toHolder, boolean saveFileInternal){
        pContext           = context;
        DEVICE_NAME        = "CAM";
        mRecordFromHolder  = fromHolder;
        mPreviewToHolder   = toHolder;
        save_file_internal = saveFileInternal;
        sub_preview        = false;
        VIDEO_SOURCE       = "SURFACE";
        AUDIO_SOURCE       = "DEFAULT";
        VIDEO_SIZE_W       = 1080;
        VIDEO_SIZE_H       = 720;
        MAX_DURATION       = 100000; // 10 seconds by default
        FILE_NAME          = DEVICE_NAME + "_" + System.currentTimeMillis();
        FILE_EXT           = "mp4";
        FILE_MAX_SIZE_MB   = 50000000; // 50MB
    }

    private static final String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;
    private static final int TIMEOUT_USEC = 10000;
    private static final int frameRate = 15;
    private static final int bitrate = 2000000;
    private static final int keyFrameInternal = 10;

    public void convertImageToVideo(Bitmap frame, int width, int height) {

        int nbFrames = (int)(MAX_DURATION * (float)frameRate) + 1;




        try {

            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);

            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, keyFrameInternal);

            codec = MediaCodec.createEncoderByType(MIME_TYPE);

            try {
                codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            }
            catch(Exception ce) {
                Log.e(TAG, "Encoder: " + ce.getMessage());
            }

            muxer = new MediaMuxer(Objects.requireNonNull(getVideoSaveInternalStoragePath()), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            Surface surface = codec.createInputSurface();

            codec.start();
            codecStarted = true;

            ByteBuffer[] outputBuffers = codec.getOutputBuffers();
            int outputBufferIndex;
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean outputDone = false;
            int nbEncoded = 0;

            //Bitmap frame = getBitmapFromImage(filePath, width, height);
            Canvas canvas = surface.lockCanvas(new Rect(0,0, width, height));

            canvas.drawBitmap(frame, 0, 0, new Paint());
            surface.unlockCanvasAndPost(canvas);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

            while (!outputDone) {

                canvas = surface.lockCanvas(null);
                canvas.drawBitmap(frame, 0, 0, paint);
                surface.unlockCanvasAndPost(canvas);
                outputBufferIndex = codec.dequeueOutputBuffer(info, TIMEOUT_USEC);

                if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    Log.d(TAG, "no output from encoder available");
                }
                else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not expected for an encoder
                    outputBuffers = codec.getOutputBuffers();
                    Log.d(TAG, "encoder output buffers changed");
                }
                else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if(muxerStarted)
                        throwException("format changed twice");
                    MediaFormat newFormat = codec.getOutputFormat();
                    videoTrackIndex = muxer.addTrack(newFormat);
                    muxer.start();
                    muxerStarted = true;
                }
                else if (outputBufferIndex < 0) {
                    throwException("unexpected result from encoder.dequeueOutputBuffer: " + outputBufferIndex);
                }
                else { // encoderStatus >= 0

                    ByteBuffer encodedData = outputBuffers[outputBufferIndex];

                    if (encodedData == null) {
                        throwException("encoderOutputBuffer " + outputBufferIndex + " was null");
                    }

                    if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        // The codec config data was pulled out and fed to the muxer when we got
                        // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                        Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                        info.size = 0;
                    }

                    if (info.size != 0) {

                        if (!muxerStarted)
                            throwException("muxer hasn't started");

                        info.presentationTimeUs = computePresentationTime(nbEncoded);

                        if(videoTrackIndex == -1)
                            throwException("video track not set yet");

                        //Log.e(TAG, "TIME: " + String.valueOf(info.presentationTimeUs) + "OFFSET: " + info.offset);

                        // adjust the ByteBuffer values to match BufferInfo (not needed?)
                        encodedData.position(info.offset);
                        encodedData.limit(info.offset + info.size);

                        muxer.writeSampleData(videoTrackIndex, encodedData, info);

                        nbEncoded++;

                        if(nbEncoded == nbFrames)
                            outputDone = true;
                    }

                    // It's usually necessary to adjust the ByteBuffer values to match BufferInfo.
                    codec.releaseOutputBuffer(outputBufferIndex, false);
                }
            }

            if (codec != null) {
                if (codecStarted) {codec.stop();}
                codec.release();
            }
            if (muxer != null) {
                if (muxerStarted) {muxer.stop();}
                muxer.release();
            }

        } catch (Exception e) {
            Log.e(TAG, "Encoding exception: " + e.toString());
        }
    }

    private static long computePresentationTime(int frameIndex) {
        final long ONE_BILLION = 1000000000;
        return frameIndex * ONE_BILLION / frameRate;
    }


    public void playVid(Context context, VideoView v){

        SAVE_PATH = "data/user/0/com.cyrenaica.cyrenaicaserver/app_cyrenaicaVid/CAM_1741939067438.mp4"; //Tools.getStringPref(pContext, "CURRENT_REC_VID");
        Log.e(TAG, "PATH--------" + SAVE_PATH);
        MediaController mc = new MediaController(context);
        mc.setAnchorView(v);
        mc.setMediaPlayer(v);
        Uri video = Uri.parse(SAVE_PATH);
        v.setMediaController(mc);
        v.setVideoURI(video);
        v.start();
    }
    private static void throwException(String exp) {
        throw new RuntimeException(exp);
    }

    public void initRecording() {

        releaseMediaRecorder();

        mediaVideoRecorde = new MediaRecorder();  // Works well

        if(VIDEO_SOURCE.equals("CAMERA")){
            mediaVideoRecorde.setVideoSource(MediaRecorder.VideoSource.CAMERA); // Must not be called after serFormat()
        }

        if(VIDEO_SOURCE.equals("SURFACE")){

            if(record_audio){

                if(AUDIO_SOURCE.equals("DEFAULT")){
                    mediaVideoRecorde.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                }

                if(AUDIO_SOURCE.equals("MIC")){
                    mediaVideoRecorde.setAudioSource(MediaRecorder.AudioSource.MIC);
                }
            }

            Surface recorderSurface = mRecordFromHolder.getSurface();
            Surface recorderSurface1 = MediaCodec.createPersistentInputSurface();

            mediaVideoRecorde.setInputSurface(recorderSurface);
            mediaVideoRecorde.setVideoSource(MediaRecorder.VideoSource.SURFACE); // Must not be called after setOutputFormat()
            mediaVideoRecorde.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaVideoRecorde.getSurface();
        }

        mediaVideoRecorde.setMaxDuration(MAX_DURATION); // In ms
        mediaVideoRecorde.setMaxFileSize(FILE_MAX_SIZE_MB); // Set max file size 50M

        if(record_audio) {
            mediaVideoRecorde.setAudioEncodingBitRate(8000);
        }

        mediaVideoRecorde.setVideoFrameRate(30);
        mediaVideoRecorde.setVideoSize(VIDEO_SIZE_W, VIDEO_SIZE_H);

        mediaVideoRecorde.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //mediaVideoRecorde.setVideoEncodingBitRate(3000000);

        if(record_audio) {
            mediaVideoRecorde.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        }

        //mediaVideoRecorde.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        if(save_file_internal){
            mediaVideoRecorde.setOutputFile("/" + getVideoSaveInternalStoragePath());
        }

        try {
            mediaVideoRecorde.prepare();
        }
        catch (IllegalStateException | IOException e) {
            // This is thrown if the previous calls are not called with the
            // proper order
            e.printStackTrace();
        }

        //mediaVideoRecorde.start();
    }

    public void initRecording(SurfaceHolder inHolder, SurfaceHolder outHolder) {

        mediaVideoRecorde = new MediaRecorder();  // Works well

        mediaVideoRecorde.setMaxDuration(MAX_DURATION); // In ms
        //mediaVideoRecorde.setVideoFrameRate(24);
        mediaVideoRecorde.setVideoSize(VIDEO_SIZE_W, VIDEO_SIZE_H);
        mediaVideoRecorde.setVideoEncodingBitRate(3000000);

        if(record_audio) {
            mediaVideoRecorde.setAudioEncodingBitRate(8000);
        }

        if(VIDEO_SOURCE.equals("CAMERA")){
            mediaVideoRecorde.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        }

        if(VIDEO_SOURCE.equals("SURFACE")){

            mediaVideoRecorde.getSurface();
            mediaVideoRecorde.setPreviewDisplay(inHolder.getSurface());
            mediaVideoRecorde.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        }

        if(record_audio){

            if(AUDIO_SOURCE.equals("DEFAULT")){
                mediaVideoRecorde.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            }

            if(AUDIO_SOURCE.equals("MIC")){
                mediaVideoRecorde.setAudioSource(MediaRecorder.AudioSource.MIC);
            }
        }

        mediaVideoRecorde.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        if(sub_preview){
            mediaVideoRecorde.setPreviewDisplay(outHolder.getSurface());
        }
        mediaVideoRecorde.setOutputFile("/" + DIR_PATH + FILE_NAME + "." + FILE_EXT);

        try {
            mediaVideoRecorde.prepare();
        }
        catch (IllegalStateException | IOException e) {
            // This is thrown if the previous calls are not called with the
            // proper order
            e.printStackTrace();
        }

        mediaVideoRecorde.start();
    }

    public void startingRecording(){

        initRecording();

        mediaVideoRecorde.getSurface();

        mediaVideoRecorde.start();

    }

    private void releaseMediaRecorder(){

        if (mediaVideoRecorde != null) {
            mediaVideoRecorde.reset();   // clear recorder configuration
            mediaVideoRecorde.release(); // release the recorder object
            mediaVideoRecorde = null;
        }
    }

    public void stopRecording() {

        releaseMediaRecorder();
        mediaVideoRecorde.stop();
    }

    public void setStartRecording(boolean start){

    }

    public void setDeviceName(String name){
        DEVICE_NAME = name;
    }

    public void setRecordAudio(boolean audio){
        record_audio = audio;
    }

    public void setVideoSource(String video_source){
        VIDEO_SOURCE = video_source;
    }

    public void setAudioSource(String audio_source){
        AUDIO_SOURCE = audio_source;
    }

    public void setVideoSizeWH(int w, int h){
        VIDEO_SIZE_W = w;
        VIDEO_SIZE_H = h;
    }

    public void setVideoMaxDuration(int d){
        MAX_DURATION = d;
    }

    public void setDirPath(String path){
        DIR_PATH = path;
    }

    public void setFileName(String name){
        FILE_NAME = name;
    }

    public void setFileExt(String ext){
        FILE_EXT = ext;
    }

    public void setVideoFileMaxSize(int max_mb){
        FILE_MAX_SIZE_MB = max_mb;
    }

    private String getVideoSaveInternalStoragePath() {

        String tempPath = "";

        try {

            ContextWrapper cw = new ContextWrapper(pContext);
            File directory = cw.getDir("cyrenaicaVid", Context.MODE_PRIVATE);

            tempPath = directory.getPath();
            SAVE_PATH = tempPath + "/" + FILE_NAME + "." + FILE_EXT;
            Tools.setStringPref(pContext, "CURRENT_REC_VID", SAVE_PATH);
            Log.d(TAG, "SAVE_PATH: " + SAVE_PATH);

            return SAVE_PATH;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void loadVideoFromInternalStorage(String filePath){

        Uri uri = Uri.parse(Environment.getExternalStorageDirectory()+filePath);
       // myVideoView.setVideoURI(uri);

    }
}
