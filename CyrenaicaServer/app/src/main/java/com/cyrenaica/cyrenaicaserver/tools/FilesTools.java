package com.cyrenaica.cyrenaicaserver.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.arthenica.ffmpegkit.FFmpegKitConfig;
import com.arthenica.smartexception.java.Exceptions;
import com.cyrenaica.cyrenaicaserver.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;


public class FilesTools {

    private static final String TAG = "FILES_TOOLS";

    /* CREATE FOLDER VARIABLES */
    /* To create a folder on the internal storage */
    public static int IN = 0;
    /* To create a folder on the external storage */
    public static int EX = 1;

    static String DEVICE_NAME;

    public static int SD_FOLDER_TYPE_IMAGES = 0;
    public static int SD_FOLDER_TYPE_LOGOS  = 1;
    public static int SD_FOLDER_TYPE_VIDEOS = 2;
    public static int SD_FOLDER_TYPE_DATA   = 3;

    public static String SD_FOLDER_TYPE_IMAGES_PATH      = "IMAGES";
    public static String SD_FOLDER_TYPE_LOGOS_PATH       = "LOGOS";
    public static String SD_FOLDER_TYPE_VIDEOS_PATH      = "VIDEOS";
    public static String SD_FOLDER_TYPE_VIDEOS_SAVE_PATH = "VIDEOS_SAVE";
    public static String SD_FOLDER_TYPE_HLS_PATH         = "hls";
    public static String SD_FOLDER_TYPE_DATA_PATH        = "DATA";

    public static String IN_FOLDER_TYPE_IMAGES_PATH = SD_FOLDER_TYPE_IMAGES_PATH;
    public static String IN_FOLDER_TYPE_VIDEOS_PATH = SD_FOLDER_TYPE_VIDEOS_PATH;
    public static String IN_FOLDER_TYPE_DATA_PATH   = SD_FOLDER_TYPE_DATA_PATH;

    public static String FONTS_FILES_PATH;

    public static String SD_PATH;

    static Context mContext;


    public static void setContext(Context context){
        mContext = context;
        SD_PATH  = "/storage/sdcard1/Android/data/" + mContext.getPackageName() + "/files";
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    public static long formatSizeLong(long size) {

        String suffix = null;

        if (size >= 1024) {

            size /= 1024;

            if (size >= 1024) {
                size /= 1024;

                if (size >= 1024) {
                    size /= 1024;

                    if (size >= 1024) {
                        size /= 1024;
                    }
                }
            }
        }


        return size;
    }

    public static String formatSize(long size) {

        String suffix = null;

        if (size >= 1024) {

            suffix = " KB";
            size /= 1024;

            if (size >= 1024) {

                suffix = " MB";
                size /= 1024;

                if (size >= 1024) {

                    suffix = " GB";
                    size /= 1024;

                    if (size >= 1024) {

                        suffix = " TB";
                        size /= 1024;
                    }
                }
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;

        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    public static boolean isSDAvailable(){

        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean isSDWritable(){

        String state = Environment.getExternalStorageState();

        return !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static boolean isSDParentDirExist(String folder_name){
        String temp_path = SD_PATH + "/" + folder_name;
        File path = new File(temp_path);
        return path.exists();
    }

    public static int isSDDirExist(String folder_name, int type){

        if(type == SD_FOLDER_TYPE_IMAGES){

            String images_path = SD_PATH + "/IMAGES";
            String folder_path = images_path + "/" + folder_name;

            File images_folder = new File(images_path);

            if(images_folder.exists()){

                File folder = new File(folder_path);

                if(folder.exists()){

                    return 0;
                }
                else
                {
                    return -1;
                }
            }
            else
            {
                return -2;
            }
        }

        if(type == SD_FOLDER_TYPE_LOGOS){

            String images_path = SD_PATH + "/IMAGES";
            String folder_path = images_path + "/" + folder_name;

            File images_folder = new File(images_path);

            if(images_folder.exists()){

                File folder = new File(folder_path);

                if(folder.exists()){

                    return 0;
                }
                else
                {
                    return -1;
                }
            }
            else
            {
                return -2;
            }
        }

        if(type == SD_FOLDER_TYPE_VIDEOS){

            String images_path = SD_PATH + "/VIDEOS";
            String folder_path = images_path + "/" + folder_name;

            File images_folder = new File(images_path);

            if(images_folder.exists()) {

                File folder = new File(folder_path);

                if (folder.exists()) {

                    return 0;

                }
                else
                {
                    return -1;
                }

            }
            else
            {
                return -2;
            }
        }

        if(type == SD_FOLDER_TYPE_DATA){

            String images_path = SD_PATH + "/VIDEOS";
            String folder_path = images_path + "/" + folder_name;

            File images_folder = new File(images_path);

            if(images_folder.exists()) {

                File folder = new File(folder_path);

                if (folder.exists()) {

                    return 0;

                }
                else
                {
                    return -1;
                }

            }
            else
            {
                return -2;
            }
        }

        return -3;
    }

    /* This function to search in application (data) dir */
    public static boolean isAppDataDirExist(Context context, String folder){

        //String data_dir_path = context.getDataDir().getPath();
        String data_dir_path = context.getDir(folder, Context.MODE_PRIVATE).getPath();

        File _folder = new File(data_dir_path);

        return _folder.exists();
    }

    /* To get any APP folders [IMAGES, VIDEOS, DATA] in data */
    public static boolean isAppInternalDirExist(String folder){

        String data_dir_path = mContext.getDir(folder, Context.MODE_PRIVATE).getPath();

        File _folder = new File(data_dir_path);

        return _folder.exists();
    }

    public static String getExternalSaveLogosPath(String device_name){
        return SD_PATH + "/" + SD_FOLDER_TYPE_IMAGES_PATH + "/" + device_name;
    }

    /* To get any APP folders [IMAGES/SUB, VIDEOS/SUB, DATA/SUB] in data */
    public static boolean isAppInternalSubDirExist(String folder, int type){

        String dir_path = "";

        if(type == SD_FOLDER_TYPE_IMAGES){

            dir_path += SD_FOLDER_TYPE_IMAGES_PATH;
            dir_path += "/";
            dir_path += folder;
        }

        if(type == SD_FOLDER_TYPE_VIDEOS){

            dir_path += SD_FOLDER_TYPE_VIDEOS_PATH;
            dir_path += "/";
            dir_path += folder;
        }

        if(type == SD_FOLDER_TYPE_DATA){

            dir_path += SD_FOLDER_TYPE_DATA_PATH;
            dir_path += "/";
            dir_path += folder;
        }

        String data_dir_path = mContext.getDir(dir_path, Context.MODE_PRIVATE).getPath();

        File _folder = new File(data_dir_path);

        return _folder.exists();
    }

    public static String getExternalMemoryPath(){

        File storage = new File("/storage");
        String external_storage_path = "";

        if (storage.exists()) {

            File[] files = storage.listFiles();

            assert files != null;

            for (File file : files) {

                if (Environment.isExternalStorageRemovable(file)) {
                    // storage is removable
                    external_storage_path = file.getAbsolutePath();

                    return external_storage_path;
                }
            }
        }

        return "ERROR";
    }

    public static long getTotalExternalMemorySize() {

        //StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        //String fileName = Environment.getExternalStorageDirectory().toString();
        //Log.d(TAG, "fileName Path: " + fileName);

        File storage = new File("/storage");
        String external_storage_path = "";
        long size = -1;

        if (storage.exists()) {

            File[] files = storage.listFiles();

            assert files != null;

            for (File file : files) {

                if (Environment.isExternalStorageRemovable(file)) {
                    // storage is removable
                    external_storage_path = file.getAbsolutePath();

                    if (!external_storage_path.isEmpty()) {

                        File external_storage = new File(external_storage_path);
                        if (external_storage.exists()) {
                            size = formatSizeLong(file.getTotalSpace());
                        }

                        return size;
                    }
                }
            }
        }

        return -1;
    }

    public static long getAvailableExternalMemorySize() {

        if (isSDAvailable()) {

            File storage = new File("/storage");
            String external_storage_path = "";
            long size = -1;

            if (storage.exists()) {

                File[] files = storage.listFiles();

                assert files != null;

                for (File file : files) {

                    if (Environment.isExternalStorageRemovable(file)) {
                        // storage is removable
                        external_storage_path = file.getAbsolutePath();

                        if (!external_storage_path.isEmpty()) {

                            File external_storage = new File(external_storage_path);
                            if (external_storage.exists()) {
                                size = formatSizeLong(file.getFreeSpace());
                            }

                            return size;
                        }
                    }
                }
            }

            return -1;
        }

        return -2;
    }

    public static long getTotalInternalMemorySize() {
        File path        = Environment.getExternalStorageDirectory();
        return formatSizeLong(path.getTotalSpace());
    }

    public static long getAvailableInternalMemorySize() {
        File path            = Environment.getExternalStorageDirectory();
        return formatSizeLong(path.getFreeSpace());
    }

    public static String getTotalFormatedExternalMemorySize() {

        //StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        //String fileName = Environment.getExternalStorageDirectory().toString();
        //Log.d(TAG, "fileName Path: " + fileName);

        File storage = new File("/storage");
        String external_storage_path = "";
        String size = "";

        if (storage.exists()) {

            File[] files = storage.listFiles();

            assert files != null;

            for (File file : files) {

                if (Environment.isExternalStorageRemovable(file)) {
                    // storage is removable
                    external_storage_path = file.getAbsolutePath();

                    if (!external_storage_path.isEmpty()) {

                        File external_storage = new File(external_storage_path);
                        if (external_storage.exists()) {
                            size = formatSize(file.getTotalSpace());
                        }

                        return size;
                    }
                }
            }
        }

        return "";
    }

    public static String getAvailableFormatedExternalMemorySize() {

        if (isSDAvailable()) {
            File storage = new File("/storage");
            String external_storage_path = "";
            String size = "";

            if (storage.exists()) {

                File[] files = storage.listFiles();

                assert files != null;

                for (File file : files) {

                    if (Environment.isExternalStorageRemovable(file)) {
                        // storage is removable
                        external_storage_path = file.getAbsolutePath();

                        if (!external_storage_path.isEmpty()) {

                            File external_storage = new File(external_storage_path);
                            if (external_storage.exists()) {
                                size = formatSize(file.getFreeSpace());
                            }

                            return size;
                        }
                    }
                }
            }

            return "";
        }
        else
        {
            return "ERROR";
        }
    }

    public static String getTotalFormatedInternalMemorySize() {
        File path        = Environment.getExternalStorageDirectory();
        return formatSize(path.getTotalSpace());
    }

    public static String getAvailableFormatedInternalMemorySize() {
        File path            = Environment.getExternalStorageDirectory();
        return formatSize(path.getFreeSpace());
    }

    public static void listExistingSDDrives() {

        String sdpath,sd1path,usbdiskpath,sd0path;
        String path = "";

        if(new File("/storage/extSdCard/").exists()) {
            sdpath="/storage/extSdCard/";
            path = sdpath;
            Log.i("Sd Card ext Path",sdpath);
        }

        if(new File("/storage/sdcard0/").exists()) {
            sd0path="/storage/sdcard0/";
            path = sd0path;
            Log.i("Sd Card0 Path",sd0path);
        }

        if(new File("/storage/sdcard1/").exists()) {
            sd1path="/storage/sdcard1/";
            path = sd1path;
            Log.i("Sd Card1 Path",sd1path);
        }

        if(new File("/storage/usbcard1/").exists()) {
            usbdiskpath="/storage/usbcard1/";
            path = usbdiskpath;
            Log.i("USB Path",usbdiskpath);
        }

    }

    /* This method will create a folder in [/data/user/0/com.cyrenaica.cyrenaicaserver/] if where = [IN] and in [/storage/sdcard1/] if where = [EX] */
    public static boolean createFolder(String folder_name, int where /* IN=internal - EX=external */){

        if(where == IN ){

            String new_dir = mContext.getDataDir().getPath() + "/" + folder_name;
            File dir = new File(new_dir);

            if(!dir.exists()) {

                if (dir.mkdir()) {
                    Log.d(TAG, "Directory created");
                    return true;
                }
            }

            Log.d(TAG, "Directory is not created");
            return true;
        }

        if(where == EX) {

            File path = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);;
            //File file = new File("/storage/sdcard1/Android/data/"+context.getOpPackageName(), folder_name);
            File file = new File(SD_PATH, folder_name);

            assert path != null;
            Log.d(TAG, "Path: " + path.toString());
            Log.d(TAG, "File: " + file.getPath());

            String state;
            state = Environment.getExternalStorageState();

            if ((Environment.MEDIA_MOUNTED).equals(state)) {

                if (!file.exists()) {

                    if(!file.mkdirs()){
                        Toast.makeText(mContext, "Error while creating folder [" + folder_name + "]", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error while creating folder [" + folder_name + "]");
                        return false;
                    }

                    Toast.makeText(mContext, "The folder [" + folder_name + "] created successfully", Toast.LENGTH_LONG).show();
                    return true;
                }
                else
                {
                    Log.d(TAG, "Folder [" + folder_name + "] already exist in this path [" + SD_PATH + "]");
                    return false;
                }
            }
            else
            {
                Toast.makeText(mContext, "The SD not mounted", Toast.LENGTH_LONG).show();
                Log.e(TAG, "The SD card not mounted");
                return false;
            }
        }

        return false;
    }

    public static void deleteHlsContent(){

        String hls_folder_path = SD_PATH + "/" + SD_FOLDER_TYPE_VIDEOS_PATH + "/" + DEVICE_NAME + "/" + SD_FOLDER_TYPE_HLS_PATH;

        File hls_dir = new File(hls_folder_path);

        if (hls_dir.exists()) {

            if (hls_dir.isDirectory()) {

                String[] children = hls_dir.list();

                if(Objects.requireNonNull(children).length > 0){

                    Log.d(TAG, "There (is/are) [" + children.length + "] file(s) in [" + hls_folder_path + "]");

                    for (int i = 0; i < Objects.requireNonNull(children).length; i++) {
                        new File(hls_dir, children[i]).delete();
                    }
                }
                else
                {
                    Log.d(TAG, "There (is/are) [" + children.length + "] file(s) in [" + hls_folder_path + "]");
                }
            }
        }
    }

    public static void setDeviceName(String name){
        DEVICE_NAME =  name;
    }

    public static String getDeviceName(){
        return DEVICE_NAME;
    }
    public static String getFontsPath(){
        //return FONTS_FILES_PATH = "file:///android_asset/fonts/";
        return FONTS_FILES_PATH = "file://android_asset/public/assets/";
    }

    public static void rawResourceToFile(Resources resources, final int resourceId, final File file) throws IOException {

        final InputStream inputStream = resources.openRawResource(resourceId);

        if (file.exists()) {
            file.delete();
        }

        final FileOutputStream outputStream = new FileOutputStream(file);

        try {

            final byte[] buffer = new byte[1024];

            int readSize;

            while ((readSize = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, readSize);
            }

        }
        catch (final IOException e) {
            Log.e(TAG, String.format("Saving raw resource failed.%s", Exceptions.getStackTraceString(e)));
        }
        finally {
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }
    }

    public static void registerApplicationFonts() throws IOException {

        final File cacheDirectory = mContext.getCacheDir();
        final File fontDirectory  = new File(cacheDirectory, "fonts");

        boolean fontDirectoryCreated = fontDirectory.mkdirs();
        if (!fontDirectoryCreated) {
            android.util.Log.i(TAG, String.format("Failed to create font directory: %s.", fontDirectory.getAbsolutePath()));
        }

        rawResourceToFile(mContext.getResources(), R.raw.impact, new File(fontDirectory, "impact.ttf"));
        rawResourceToFile(mContext.getResources(), R.raw.georgia, new File(fontDirectory, "georgia.ttf"));
        rawResourceToFile(mContext.getResources(), R.raw.georgiab, new File(fontDirectory, "georgiab.ttf"));
        rawResourceToFile(mContext.getResources(), R.raw.verdana, new File(fontDirectory, "verdana.ttf"));
        rawResourceToFile(mContext.getResources(), R.raw.verdanab, new File(fontDirectory, "verdanab.ttf"));

        final HashMap<String, String> fontNameMapping = new HashMap<>();

        fontNameMapping.put("Impact", "Impact");
        fontNameMapping.put("georgia", "Georgia");
        fontNameMapping.put("georgiab", "Georgia B");
        fontNameMapping.put("verdana", "Verdana");
        fontNameMapping.put("verdanab", "Verdana B");

        FFmpegKitConfig.setFontDirectoryList(mContext, Arrays.asList(fontDirectory.getAbsolutePath(), "/system/fonts"), fontNameMapping);

        FFmpegKitConfig.setEnvironmentVariable("FFREPORT", String.format("file=%s", new File(cacheDirectory.getAbsolutePath(), "ffreport.txt").getAbsolutePath()));


    }

    /* With end slash [/] */
    public static String getHlsPath(){
        return SD_PATH + "/" + SD_FOLDER_TYPE_VIDEOS_PATH + "/" + DEVICE_NAME + "/" + SD_FOLDER_TYPE_HLS_PATH + "/";
    }

    /* With end slash [/] */
    public static String getRecordMp4SavePath(){
        return SD_PATH + "/" + SD_FOLDER_TYPE_VIDEOS_PATH + "/" + DEVICE_NAME + "/" + SD_FOLDER_TYPE_VIDEOS_SAVE_PATH + "/";
    }

    public static String[] getAllFontsFileInAssets(Context context) throws IOException {

        AssetManager am = context.getAssets();
        String [] list  = am.list("fonts");

        int count = 0;
        assert list != null;
        String [] fonts = new String[list.length];
        for (String file : list) {
            fonts[count] = file;
            count++;
        }

        return fonts;
    }

    public static String[] getAllFontsNames(){

        String [] list        = {"Georgia", "Georgia Bold", "Impact", "Verdana", "Verdana Bold"};
        int count             = 0;
        String [] fonts_names = new String[list.length];

        for (String name : list) {
            fonts_names[count] = name;
            count++;
        }

        return fonts_names;
    }

    public static String[] getAllFontsNamesSmalls(){

        String [] list        = {"georgia", "georgiab", "impact", "verdana", "verdanab"};
        int count             = 0;
        String [] fonts_names = new String[list.length];

        for (String name : list) {
            fonts_names[count] = name;
            count++;
        }

        return fonts_names;
    }

}
