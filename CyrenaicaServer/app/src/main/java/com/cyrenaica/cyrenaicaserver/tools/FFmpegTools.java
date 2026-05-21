package com.cyrenaica.cyrenaicaserver.tools;

import static org.bytedeco.javacpp.Loader.getCacheDir;

import android.content.Context;
import android.util.Log;

import com.cyrenaica.cyrenaicaserver.tools.customs.FfmpegColorAdapter;
import com.cyrenaica.cyrenaicaserver.tools.customs.FfmpegColors;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FFmpegTools {



    private static final String TAG = "FFMPEG_TOOLS";

    static String DEVICE_NAME;
    static String FORMATED_DEVICE_NAME;
    static String CAMERA_SOURCE_VIDEO_W = "480";
    static String CAMERA_SOURCE_VIDEO_H = "320";
    static String VIDEO_OUTPUT_W;
    static String VIDEO_OUTPUT_H;
    static String CAMERA_IP;
    static String CAMERA_URL;

    static String [] FONTS_LIST;
    static public String [] VIDEO_SOURCE_RESOLUTIONS = {"Select", "240x240", "320x240","400x296", "480x320", "640x480", "800x600", "1024x768", "1280x720", "1280x1024", "1600x1200"};


    static String LOGO_SAVED_PATH;
    static String LOGO_SAVED_POSITION;
    static String LOGO_PREPARE_COMMAND;
    static String LOGO_W;
    static String LOGO_H;

    static String LOGO_COMMAND;

    static String LIVE_LOGO_PATH;
    static String LIVE_LOGO_W;
    static String LIVE_LOGO_H;

    static String DEVICE_NAME_FONT;
    static String DEVICE_NAME_FONT_PATH;
    static String DEVICE_NAME_TEXT_SIZE;
    static String DEVICE_NAME_TEXT_COLOR;
    static String DATE_TIME_FONT;
    static String DATE_TIME_FONT_PATH;
    static String DATE_TIME_TEXT_SIZE;
    static String DATE_TIME_TEXT_COLOR;
    static String DEVICE_NAME_COMMAND;
    static String DATE_TIME_COMMAND;
    static String FREE_TEXT_COMMAND;
    static String TEXT_JOINT_COMMAND;
    static String TEXT_PREPARE_COMMAND;
    static String FILTER_COMPLEX_COMMAND;
    static String HLS_CODEC_COMMAND;
    static String MP4_CODEC_COMMAND;
    static String HLS_MAP_COMMAND;
    static String MP4_MAP_COMMAND;
    static String MP4_LAST_RECORDER_VIDEO_PATH;

    public static String FFMPEG_COMMAND;

    public static void setLastRecordedVideoPath(String path){
        MP4_LAST_RECORDER_VIDEO_PATH =  path;
    }

    public static String getLastRecordedVideoPath(){
        return MP4_LAST_RECORDER_VIDEO_PATH;
    }

    public static void setDeviceName(String name){
        DEVICE_NAME =  name;
    }

    public static String getDeviceName(){
        return DEVICE_NAME;
    }

    public static void setFormatedDeviceName(String name){
        FORMATED_DEVICE_NAME = "'" + name + "'";
    }

    public static String getFormatedDeviceName(){
        return FORMATED_DEVICE_NAME;
    }

    public static void setCameraSourceVideoW(String w){
        CAMERA_SOURCE_VIDEO_W = w;
    }

    public static String getCameraSourceVideoW(){
        return CAMERA_SOURCE_VIDEO_W;
    }

    public static void setCameraSourceVideoH(String h){
        CAMERA_SOURCE_VIDEO_H = h;
    }

    public static String getCameraSourceVideoH(){
        return CAMERA_SOURCE_VIDEO_H;
    }

    public static void setVideoOutputW(String w){
        VIDEO_OUTPUT_W = w;
    }

    public static String getVideoOutputW(){
        return VIDEO_OUTPUT_W;
    }

    public static void setVideoOutputH(String h){
        VIDEO_OUTPUT_H = h;
    }

    public static String getVideoOutputH(){
        return VIDEO_OUTPUT_H;
    }

    public static void setCameraIp(String ip){
        CAMERA_IP = ip;
    }

    public static String getCameraIp(){
        return CAMERA_IP;
    }

    public static void setCameraUrl(){

        CAMERA_URL = "http://";
        CAMERA_URL += CAMERA_IP;
        CAMERA_URL += "/";
        CAMERA_URL += CAMERA_SOURCE_VIDEO_W;
        CAMERA_URL += "x";
        CAMERA_URL += CAMERA_SOURCE_VIDEO_H;
        CAMERA_URL += ".mjpeg";

        Log.d(TAG, "CAMERA URL:       " + CAMERA_URL);
    }

    public static String getCameraUrl(){
        return CAMERA_URL;
    }

    /* To get font's file name */
    public static void setDeviceNameFont(String font){
        DEVICE_NAME_FONT = font;
    }

    public static String getDeviceNameFont(){
        return DEVICE_NAME_FONT;
    }

    /* To set font's file full path [with file name]*/
    public static void setDeviceNameFontPath(String path){
        DEVICE_NAME_FONT_PATH = path;
    }

    /* To get font's file full path [with file name]*/
    public static String getDeviceNameFontPath(){
        return DEVICE_NAME_FONT_PATH;
    }

    public static void setDeviceNameFontSize(String size){
        DEVICE_NAME_TEXT_SIZE = size;
    }

    public static String getDeviceNameFontSize(){
        return DEVICE_NAME_TEXT_SIZE;
    }

    public static void setDeviceNameFontColor(String color){
        DEVICE_NAME_TEXT_COLOR = color;
    }

    public static String getDeviceNameFontColor(){
        return DEVICE_NAME_TEXT_COLOR;
    }

    /* To set font's file full path [with file name]*/
    public static void setDateTimeFontPath(String path){
        DATE_TIME_FONT_PATH = path;
    }

    /* To get font's file full path [with file name]*/
    public static String getDateTimeFontPath(){
        return DATE_TIME_FONT_PATH;
    }
    public static void setDateTimeFont(String font){
        DATE_TIME_FONT = font;
    }

    public static String getDateTimeFont(){
        return DATE_TIME_FONT;
    }

    public static void setDateTimeFontSize(String size){
        DATE_TIME_TEXT_SIZE = size;
    }

    public static String getDateTimeFontSize(){
        return DATE_TIME_TEXT_SIZE;
    }

    public static void setDateTimeFontColor(String color){
        DATE_TIME_TEXT_COLOR = color;
    }

    public static String getDateTimeFontColor(){
        return DATE_TIME_TEXT_COLOR;
    }

    public static void setLogoSavedPath(String path){
        LOGO_SAVED_PATH = path;
    }

    public static String getLogoSavedPath(){
        return LOGO_SAVED_PATH;
    }

    public static void setLogoSavedPosition(String position){
        LOGO_SAVED_POSITION = position;
    }

    public static String getLogoSavedPosition(){
        return LOGO_SAVED_POSITION;
    }

    /* TOOLS */
    public static void setFontsList(String []list){
        FONTS_LIST = list;
    }

    public static String [] getFontsList(){
        return FONTS_LIST;
    }

    public static String getTimeStamp(String format){

        SimpleDateFormat dateFormat = null;

        if(format.equals("D")){
            dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault(Locale.Category.FORMAT));
        }

        if(format.equals("T")){
            dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault(Locale.Category.FORMAT));
        }

        if(format.equals("DT")){
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault(Locale.Category.FORMAT));
        }

        if(format.equals("TD")){
            dateFormat = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd", Locale.getDefault(Locale.Category.FORMAT));
        }

        assert dateFormat != null;
        return dateFormat.format(new Date()); // Find today's date;
    }

    public static void parseVideoResolution(String resolution){
        CAMERA_SOURCE_VIDEO_W = resolution.substring(0, resolution.indexOf("x"));
        CAMERA_SOURCE_VIDEO_H = resolution.substring(resolution.indexOf("x") + 1);

        Log.d(TAG, "RESOLUTION: W [" + CAMERA_SOURCE_VIDEO_W + "] - H [" + CAMERA_SOURCE_VIDEO_H + "] LENGTH [" + resolution.length() + "] | parseVideoResolution()");

    }

    public static void getTextColorSpinner(Context context, Field[] fields, FfmpegColorAdapter adapter, FfmpegColors colorListData, ArrayList<FfmpegColors> list){

        for(int i = 0; i < fields.length; i++) {

            String colorName = fields[i].getName();

            if(colorName.startsWith("ffmpeg")){

                String name = colorName.substring(colorName.indexOf("_") + 1).toUpperCase();

                int colorId = 0;

                try {
                    colorId = fields[i].getInt(null);
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                int color = context.getResources().getColor(colorId);
                String s_color = Integer.toHexString(color);

                colorListData = new FfmpegColors();

                colorListData.setName(name);
                colorListData.setColor(color);
                colorListData.setCode(s_color);

                list.add(colorListData);
                adapter.notifyDataSetChanged();

                Log.i("COLORS", colorName + " => " + colorId + " => " + color + " => " + s_color);
            }
        }
    }

    public void setDeviseNameCommand(String x, String y, String formatedDeviceName){

        DEVICE_NAME_COMMAND =  "drawtext=fontsize=" + DEVICE_NAME_TEXT_SIZE + ":fontcolor=" + DEVICE_NAME_TEXT_COLOR + ":x=" + x + ":y=" + y + ":fontfile=" + "'" + DEVICE_NAME_FONT_PATH  + "'" + ":text=" + formatedDeviceName;
    }

    public static void setLiveLogoPath(String path){
        LIVE_LOGO_PATH = path;
    }

    public static String getLiveLogoPath(){
        return LIVE_LOGO_PATH;
    }

    public static void setLogoW(String w){
        LOGO_W = w;
    }

    public static String getLogoW(){
        return LOGO_W;
    }

    public static void setLogoH(String h){
        LOGO_H = h;
    }

    public static String getLogoH(){
        return LOGO_H;
    }

    public static void setLiveLogoW(String w){
        LIVE_LOGO_W = w;
    }

    public static String getLiveLogoW(){
        return LIVE_LOGO_W;
    }

    public static void setLiveLogoH(String h){
        LIVE_LOGO_H = h;
    }

    public static String getLiveLogoH(){
        return LIVE_LOGO_H;
    }

    public static void watchRecordedVideo(String url){

    }


    public static void setFFmpegCommand(String formatedDeviceName, String formatedDateTime, boolean showLogo, boolean showDeviceName, boolean showDateTime, boolean showFreeText){

        String device_name_x  = "10";
        String device_name_y  = "10";
        String date_time_x    = "10";
        String date_time_y    = "40";
        String logo_x         = "W-w-10";
        String logo_y         = "10";
        String logo_overlay_x = "W-w-10";
        String logo_overlay_y = "10";
        String live_logo_x = "W-w-10";
        String live_logo_y = "H-h-10";

        FFMPEG_COMMAND = " -f mjpeg -re -i ";
        FFMPEG_COMMAND += CAMERA_URL;

        if(showDeviceName){
            DEVICE_NAME_COMMAND =  "drawtext=fontsize=" + DEVICE_NAME_TEXT_SIZE + ":fontcolor=" + DEVICE_NAME_TEXT_COLOR + ":x=" + device_name_x + ":y=" + device_name_y + ":fontfile=" + "'" + DEVICE_NAME_FONT_PATH  + "'" + ":text=" + formatedDeviceName;
        }

        if(showDateTime){
            DATE_TIME_COMMAND =  "drawtext=fontsize=" + DATE_TIME_TEXT_SIZE + ":fontcolor=" + DATE_TIME_TEXT_COLOR + ":x=" + date_time_x + ":y=" + date_time_y + ":fontfile=" + "'" + DATE_TIME_FONT_PATH  + "'" + ":text=" + "'" + formatedDateTime + "'";
        }

        if(showLogo){

            if(LOGO_SAVED_POSITION != null) {

                if (LOGO_SAVED_POSITION.equals("UP RIGHT")) {

                    LOGO_PREPARE_COMMAND = "[1]scale=" + LOGO_W + ":" + LOGO_H + "[scl],[2]scale=" + LIVE_LOGO_W + ":" + LIVE_LOGO_H + "[lscl],[0:v][scl]overlay=" + logo_overlay_x + ":" + logo_overlay_y + "[ovl];";

                    if(showDeviceName && showDateTime){
                        TEXT_JOINT_COMMAND = "[ovl]" + DEVICE_NAME_COMMAND + "," + DATE_TIME_COMMAND + "[txt];[txt]split=2[s0][s1];[s0]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[s0ovl];[s0ovl][lscl]overlay=" + live_logo_x + ":" + live_logo_y + "[v0];[s1]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[v1]";
                        LOGO_COMMAND         = LOGO_PREPARE_COMMAND + TEXT_JOINT_COMMAND;
                        FILTER_COMPLEX_COMMAND = " -filter_complex '" + LOGO_COMMAND + "'";
                    }

                    if(showDeviceName && !showDateTime){
                        TEXT_JOINT_COMMAND = "[ovl]" + DEVICE_NAME_COMMAND + "[txt];[txt]split=2[s0][s1];[s0]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[s0ovl];[s0ovl][lscl]overlay=" + live_logo_x + ":" + live_logo_y + "[v0];[s1]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[v1]";
                        LOGO_COMMAND         = LOGO_PREPARE_COMMAND + TEXT_JOINT_COMMAND;
                        FILTER_COMPLEX_COMMAND = " -filter_complex '" + LOGO_COMMAND + "'";
                    }

                    if(!showDeviceName && showDateTime){
                        TEXT_JOINT_COMMAND = "[ovl]" + DATE_TIME_COMMAND + "[txt];[txt]split=2[s0][s1];[s0]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[s0ovl];[s0ovl][lscl]overlay=" + live_logo_x + ":" + live_logo_y + "[v0];[s1]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[v1]";
                        LOGO_COMMAND         = LOGO_PREPARE_COMMAND + TEXT_JOINT_COMMAND;
                        FILTER_COMPLEX_COMMAND = " -filter_complex '" + LOGO_COMMAND + "'";
                    }

                    if(!showDeviceName && !showDateTime){
                        LOGO_COMMAND         = LOGO_PREPARE_COMMAND + "[ovl]split=2[s0][s1];[s0]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[s0ovl];[s0ovl][lscl]overlay=" + live_logo_x + ":" + live_logo_y + "[v0];[s1]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[v1]";
                        FILTER_COMPLEX_COMMAND = " -filter_complex '" + LOGO_COMMAND + "'";
                    }
                }

                if (LOGO_SAVED_POSITION.equals("DOWN RIGHT")) {

                    logo_overlay_x = "10";
                    logo_overlay_y = "H-h-10";

                    LOGO_PREPARE_COMMAND = "[1]scale=" + LOGO_W + ":" + LOGO_H + "[scl],[2]scale=" + LIVE_LOGO_W + ":" + LIVE_LOGO_H + "[lscl],[0:v][scl]overlay=" + logo_overlay_x + ":" + logo_overlay_y + "[ovl];";

                    if(showDeviceName && showDateTime){
                        TEXT_JOINT_COMMAND = "[ovl]" + DEVICE_NAME_COMMAND + "," + DATE_TIME_COMMAND + "[txt];[txt]split=2[s0][s1];[s0]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[s0ovl];[s0ovl][lscl]overlay=" + live_logo_x + ":" + live_logo_y + "[v0];[s1]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[v1]";
                        LOGO_COMMAND         = LOGO_PREPARE_COMMAND + TEXT_JOINT_COMMAND;
                        FILTER_COMPLEX_COMMAND = " -filter_complex '" + LOGO_COMMAND + "'";
                    }

                    if(showDeviceName && !showDateTime){
                        TEXT_JOINT_COMMAND = "[ovl]" + DEVICE_NAME_COMMAND + "[txt];[txt]split=2[s0][s1];[s0]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[s0ovl];[s0ovl][lscl]overlay=" + live_logo_x + ":" + live_logo_y + "[v0];[s1]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[v1]";
                        LOGO_COMMAND         = LOGO_PREPARE_COMMAND + TEXT_JOINT_COMMAND;
                        FILTER_COMPLEX_COMMAND = " -filter_complex '" + LOGO_COMMAND + "'";
                    }

                    if(!showDeviceName && showDateTime){
                        TEXT_JOINT_COMMAND = "[ovl]" + DATE_TIME_COMMAND + "[txt];[txt]split=2[s0][s1];[s0]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[s0ovl];[s0ovl][lscl]overlay=" + live_logo_x + ":" + live_logo_y + "[v0];[s1]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[v1]";
                        LOGO_COMMAND         = LOGO_PREPARE_COMMAND + TEXT_JOINT_COMMAND;
                        FILTER_COMPLEX_COMMAND = " -filter_complex '" + LOGO_COMMAND + "'";
                    }

                    if(!showDeviceName && !showDateTime){
                        LOGO_COMMAND         = LOGO_PREPARE_COMMAND + "[ovl]split=2[s0][s1];[s0]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[s0ovl];[s0ovl][lscl]overlay=" + live_logo_x + ":" + live_logo_y + "[v0];[s1]scale=1920x1080:flags=lanczos[v1]";
                        FILTER_COMPLEX_COMMAND = " -filter_complex '" + LOGO_COMMAND + "'";
                    }
                }
            }

            FFMPEG_COMMAND += " -i " + LOGO_SAVED_PATH + " -i " + LIVE_LOGO_PATH + FILTER_COMPLEX_COMMAND;
        }
        else
        {
            TEXT_PREPARE_COMMAND = "[1]scale=" + LIVE_LOGO_W + ":" + LIVE_LOGO_H + "[lscl],[0:v]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[scl];";

            if(showDeviceName && showDateTime){
                TEXT_JOINT_COMMAND = TEXT_PREPARE_COMMAND + "[scl]"  + DEVICE_NAME_COMMAND + "," + DATE_TIME_COMMAND + "[txt];[txt]split=2[s0][s1];[s0]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[s0ovl];[s0ovl][lscl]overlay=" + live_logo_x + ":" + live_logo_y + "[v0];[s1]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[v1]";
                FILTER_COMPLEX_COMMAND = " -filter_complex '" + TEXT_JOINT_COMMAND + "'";
            }

            if (showDeviceName && !showDateTime) {
                TEXT_JOINT_COMMAND = TEXT_PREPARE_COMMAND + "[scl]"  + DEVICE_NAME_COMMAND + "[txt];[txt]split=2[s0][s1];[s0]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[s0ovl];[s0ovl][lscl]overlay=" + live_logo_x + ":" + live_logo_y + "[v0];[s1]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[v1]";
                FILTER_COMPLEX_COMMAND = " -filter_complex '" + TEXT_JOINT_COMMAND + "'";
            }

            if (!showDeviceName && showDateTime) {
                TEXT_JOINT_COMMAND = TEXT_PREPARE_COMMAND + "[scl]" + DATE_TIME_COMMAND + "[txt];[txt]split=2[s0][s1];[s0]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[s0ovl];[s0ovl][lscl]overlay=" + live_logo_x + ":" + live_logo_y + "[v0];[s1]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[v1]";
                FILTER_COMPLEX_COMMAND = " -filter_complex '" + TEXT_JOINT_COMMAND + "'";
            }

            if (!showDeviceName && !showDateTime) {
                FILTER_COMPLEX_COMMAND = " -filter_complex '" + TEXT_PREPARE_COMMAND + "[scl]split=2[s0][s1];[s0]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[s0ovl];[s0ovl][lscl]overlay=" + live_logo_x + ":" + live_logo_y + "[v0];[s1]scale=" + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + ":flags=lanczos[v1]" + "'";
            }

            FFMPEG_COMMAND += " -i " + LIVE_LOGO_PATH + FILTER_COMPLEX_COMMAND;
        }

        HLS_CODEC_COMMAND = " -vcodec libx264 -r 10 -s " + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + " -preset ultrafast -tune zerolatency -b:v 10000K -pix_fmt yuv420p -flags +cgop -g 5 -sc_threshold 0";
        MP4_CODEC_COMMAND = " -vcodec libx264 -r 10 -s " + VIDEO_OUTPUT_W + "x" + VIDEO_OUTPUT_H + " -preset ultrafast -b:v 20000K -pix_fmt yuv420p -flags +cgop -g 30";
        HLS_MAP_COMMAND   = " -f hls -hls_time 1 -hls_playlist_type event -hls_flags independent_segments -hls_flags program_date_time -hls_list_size 0 -map \"" + "[v0]" + "\"";
        MP4_MAP_COMMAND   = " -movflags +frag_keyframe+separate_moof+omit_tfhd_offset+empty_moov" + MP4_CODEC_COMMAND + " -map \"" + "[v1]" + "\"";

        String hls_path       = FilesTools.getHlsPath();
        String mp4_path       = FilesTools.getRecordMp4SavePath();
        String time_stamp     = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault(Locale.Category.FORMAT)).format(new Date());
        String m3u8_file_name =  hls_path + DEVICE_NAME.toLowerCase() + "-live.m3u8";
        String mp4_file_name  =  mp4_path + DEVICE_NAME.toLowerCase() + "_" + time_stamp + "-live_record.mp4";

        setLastRecordedVideoPath(mp4_file_name);

        FFMPEG_COMMAND += HLS_CODEC_COMMAND + HLS_MAP_COMMAND + " " + m3u8_file_name + MP4_MAP_COMMAND + " -y " + mp4_file_name;
    }

    public static String getFFmpegCommand(){

        return FFMPEG_COMMAND;
    }

}
