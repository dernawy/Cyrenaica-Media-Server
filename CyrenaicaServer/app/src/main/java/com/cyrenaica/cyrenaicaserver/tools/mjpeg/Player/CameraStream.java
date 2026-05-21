package com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player;

import static androidx.media3.common.util.Assertions.checkArgument;
import static androidx.media3.common.util.Assertions.checkNotNull;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.SpannableString;
import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.UnstableApi;
import com.cyrenaica.cyrenaicaserver.tools.mjpeg.MjpegInputCamera;
import org.opencv.core.Mat;
import java.io.IOException;
import android.util.Log;

/**
 * Creates a {@link CameraStream} class object to start streaming, recording and face detecting.
 *
 * <p>Uses a {@link SpannableString} to store the text and support advanced per-character text
 * styling.
 */
@UnstableApi
public final class CameraStream {

    private static final String CAMERA_STREAM_CLASS_TAG = "CAMERA_STREAM_CLASS";

    public static Uri class_uri;

    public static int class_port;
    public static String class_host;
    public static int class_current_packet_size;
    public static int class_current_connection_timeout;
    public static boolean class_use_default_http_port;
    public static boolean class_enable_face_detection;
    public static String class_face_detection_model_path;
    public static String class_face_detection_model_type;
    public static String class_face_detection_model_direction;
    public static String class_face_detection_model_name;

    @UnstableApi
    public static final class Builder {

        private static final String CAMERA_STREAM_CLASS_BUILDER_TAG = "CAMERA_STREAM_BUILDER";

        private Uri builder_uri;
        private String builder_host;
        private int builder_port;
        private boolean builder_use_default_http_port;
        private int builder_current_packet_size;
        private int builder_current_connection_timeout;
        private boolean builder_enable_face_detection;
        private String builder_face_detection_model_path;
        private String builder_face_detection_model_type;
        private String builder_face_detection_model_direction;
        private String builder_face_detection_model_name;

        private StreamConfig.Builder streamConfiguration;
        private FaceDetection.Builder faceDetectionConfiguration;

        private MjpegInputCamera.InputStreamConfig.Builder input_config_builder;
        private MjpegFaceDetection.Config.Builder face_detection_config;

        /** Creates a builder. */
        public Builder() {
            streamConfiguration           = new StreamConfig.Builder();
            faceDetectionConfiguration    = new FaceDetection.Builder();
            input_config_builder          = new MjpegInputCamera.InputStreamConfig.Builder();
            face_detection_config         = new MjpegFaceDetection.Config.Builder();
        }

        private Builder(CameraStream stream) {

            this();

            StreamConfig localConfiguration = stream.Configuration;
            FaceDetection faceConfiguration = stream.FaceDetection;

            if(localConfiguration != null){
                this.builder_uri                            = localConfiguration.config_uri;
                this.builder_host                           = localConfiguration.config_host;
                this.builder_port                           = localConfiguration.config_port;
                this.builder_use_default_http_port          = localConfiguration.config_use_default_http_port;
                this.builder_current_packet_size            = localConfiguration.config_current_packet_size;
                this.builder_current_connection_timeout     = localConfiguration.config_current_connection_timeout;
                this.builder_enable_face_detection          = localConfiguration.config_enable_face_detection;
            }

            if(faceConfiguration != null) {
                this.builder_face_detection_model_path      = faceConfiguration.config_face_detection_model_path;
                this.builder_face_detection_model_type      = faceConfiguration.config_face_detection_model_type;
                this.builder_face_detection_model_direction = faceConfiguration.config_face_detection_model_direction;
                this.builder_face_detection_model_name      = faceConfiguration.config_face_detection_model_name;
            }


           // Log.e(CAMERA_STREAM_CLASS_BUILDER_TAG, "BUILDER CONSTRUCT ENABLED: " + this.builder_enable_face_detection);
        }

        public Builder setStreamConfiguration(StreamConfig streamConfiguration) {
            this.streamConfiguration = streamConfiguration.buildUpon();
            return this;
        }

        public Builder setFaceDetectionConfiguration(FaceDetection faceConfiguration) {
            this.faceDetectionConfiguration = faceConfiguration.buildUpon();
            return this;
        }

        public Builder setInputStreamConfiguration(MjpegInputCamera.InputStreamConfig inputStreamConfiguration) {
            this.input_config_builder = inputStreamConfiguration.buildUpon();
            return this;
        }

        public Builder setFaceConfiguration(MjpegFaceDetection.Config faceDetectConfiguration) {
            this.face_detection_config = faceDetectConfiguration.buildUpon();
            return this;
        }

        public CameraStream build() {

            StreamConfig localConfiguration                = new StreamConfig(streamConfiguration);
            FaceDetection localFaceDetection               = new FaceDetection(faceDetectionConfiguration);
            MjpegInputCamera.InputStreamConfig inputConfig = new MjpegInputCamera.InputStreamConfig(input_config_builder);
            MjpegFaceDetection.Config detectionConfig      = new MjpegFaceDetection.Config(face_detection_config);

            return new CameraStream(localConfiguration, inputConfig, localFaceDetection, detectionConfig);
        }
    }

    public static final class StreamConfig {

        private static final String STREAM_CONFIG_CLASS_TAG = "CAMERA_STREAM_CONFIG_CLASS";

        public final Uri config_uri;
        public final String config_host;
        public final int config_port;
        public final int config_default_connection_timeout = 5000;
        public final int config_default_packet_size = (1024 * 3); //3MB
        public final int config_max_packet_size = 5000; //5MB
        public final boolean config_use_default_http_port;
        public final int config_current_packet_size;
        public final int config_current_connection_timeout;
        public final boolean config_enable_face_detection;
        public final String config_face_detection_model_path;
        public final String config_face_detection_model_type;
        public final String config_face_detection_model_direction;
        public final String config_face_detection_model_name;

        @Nullable FaceDetection config_localFaceDetection;

        public static final class Builder {

            private static final String CLASS_BUILDER_TAG = "CAMERA_STREAM_CLASS_BUILDER";

            private Uri builder_uri;
            private String builder_host;
            private int builder_port;
            private final int builder_default_connection_timeout = 5000;
            private final int builder_default_packet_size = (1024 * 3); //3MB
            private int builder_max_packet_size = 5000; //5MB
            private boolean builder_use_default_http_port;
            private int builder_current_packet_size;
            private int builder_current_connection_timeout;
            private boolean builder_enable_face_detection;
            private String builder_face_detection_model_path;
            private String builder_face_detection_model_type;
            private String builder_face_detection_model_direction;
            private String builder_face_detection_model_name;

            @Nullable FaceDetection builder_localFaceDetection;

            public Builder() {

            }

            private Builder(StreamConfig config) {
                this.builder_uri                            = config.config_uri;
                this.builder_host                           = config.config_host;
                this.builder_port                           = config.config_port;
                this.builder_use_default_http_port          = config.config_use_default_http_port;
                this.builder_current_packet_size            = config.config_current_packet_size;
                this.builder_current_connection_timeout     = config.config_current_connection_timeout;
                this.builder_enable_face_detection          = config.config_enable_face_detection;
                this.builder_face_detection_model_path      = config.config_face_detection_model_path;
                this.builder_face_detection_model_type      = config.config_face_detection_model_type;
                this.builder_face_detection_model_direction = config.config_face_detection_model_direction;
                this.builder_face_detection_model_name      = config.config_face_detection_model_name;

               // Log.e(CLASS_BUILDER_TAG, "CONSTRUCT ENABLED: " + this.builder_enable_face_detection);
            }

            public Builder setUri(Uri uri) {
                this.builder_uri = checkNotNull(uri);
                return this;
            }

            public Builder setPort(int port){
                checkArgument(port > 0);
                this.builder_port = port;
                return this;
            }

            public Builder useDefaultHttpPort(boolean use){
                this.builder_use_default_http_port = use;
                return this;
            }

            public Builder setPacketSize(int size){
                checkArgument(size > 0);
                this.builder_current_packet_size = size;
                return this;
            }

            public Builder setConnectionTimeout(int timeout){
                checkArgument(timeout > 0);
                this.builder_current_connection_timeout = timeout;
                return this;
            }

            public Builder useEnableFaceDetection(boolean enable){
                this.builder_enable_face_detection = enable;
                //Log.e(CLASS_BUILDER_TAG, "METHOD ENABLED: " + this.builder_enable_face_detection);
                return this;
            }

            public Builder setModelPath(String path){
                this.builder_face_detection_model_path = checkNotNull(path);
                return this;
            }

            public Builder setModelType(String type){
                this.builder_face_detection_model_type = checkNotNull(type);
                return this;
            }

            public Builder setModelDirection(String direction){
                this.builder_face_detection_model_direction = checkNotNull(direction);
                return this;
            }

            public Builder setModelName(String name){
                this.builder_face_detection_model_name = checkNotNull(name);
                return this;
            }

            public int getCurrentPacketSize() {
                return builder_current_packet_size;
            }

            public void setCurrentPacketSize(int builder_current_packet_size) {
                this.builder_current_packet_size = builder_current_packet_size;
            }

            public int getCurrentConnectionTimeout() {
                return builder_current_connection_timeout;
            }

            public void setCurrentConnectionTimeout(int builder_current_connection_timeout) {
                this.builder_current_connection_timeout = builder_current_connection_timeout;
            }

            public int getMaxPacketSize() {
                return builder_max_packet_size;
            }

            public void setMaxPacketSize(int builder_max_packet_size) {
                this.builder_max_packet_size = builder_max_packet_size;
            }

            public StreamConfig build() {
                return new StreamConfig(this);
            }

            public static final StreamConfig UNSET = new StreamConfig.Builder().build();
        }

        /**
         * To use when the HTTP port is [80] and without set timeout
         * @param builder The media url
         *  Connection [port], [host] will be set internally if [HTTP] connection,
         * [port], [host], [timeout] and [packet_size] will be set internally
         */
        private StreamConfig(Builder builder){

            this.config_uri                             = builder.builder_uri;
            this.config_port                            = builder.builder_port;
            this.config_host                            = builder.builder_host;
            this.config_current_packet_size             = builder.builder_default_packet_size;
            this.config_current_connection_timeout      = builder.builder_default_connection_timeout;
            this.config_use_default_http_port           = builder.builder_use_default_http_port;
            this.config_enable_face_detection           = builder.builder_enable_face_detection;
            this.config_face_detection_model_path       = builder.builder_face_detection_model_path;
            this.config_face_detection_model_type       = builder.builder_face_detection_model_type;
            this.config_face_detection_model_direction  = builder.builder_face_detection_model_direction;
            this.config_face_detection_model_name       = builder.builder_face_detection_model_name;
            this.config_localFaceDetection              = builder.builder_localFaceDetection;

           // Log.e(STREAM_CONFIG_CLASS_TAG, "CONSTRUCT ENABLED: " + this.config_enable_face_detection);
        }

        public Builder buildUpon() {
            return new Builder(this);
        }
    }

    public static final class FaceDetection extends MjpegFaceDetection {

        public final Context config_face_detection_context;
        public final Activity config_face_detection_activity;
        public final String config_face_detection_model_path;
        public final String config_face_detection_model_type;
        public final String config_face_detection_model_direction;
        public final String config_face_detection_model_name;
        public final MjpegFaceDetection.Config config_face_detection_config;

        @UnstableApi
        public static final class Builder {

            private Context builder_face_detection_context;
            private Activity builder_face_detection_activity;
            private String builder_face_detection_model_path;
            private String builder_face_detection_model_type;
            private String builder_face_detection_model_direction;
            private String builder_face_detection_model_name;
            private MjpegFaceDetection.Config builder_face_detection_config;

            /** Creates a builder. */
            public Builder() {
                builder_face_detection_model_path      = "";
                builder_face_detection_model_type      = "";
                builder_face_detection_model_direction = "";
                builder_face_detection_model_name      = "";
            }

            private Builder(FaceDetection config) {

                this();

                this.builder_face_detection_model_path      = config.config_face_detection_model_path;
                this.builder_face_detection_model_type      = config.config_face_detection_model_type;
                this.builder_face_detection_model_direction = config.config_face_detection_model_direction;
                this.builder_face_detection_model_name      = config.config_face_detection_model_name;
                this.builder_face_detection_config          = config.config_face_detection_config;
                this.builder_face_detection_context         = config.config_face_detection_context;
                this.builder_face_detection_activity        = config.config_face_detection_activity;
            }

            public Builder setContext(Context context){
                this.builder_face_detection_context = context;
                return this;
            }

            public Builder setActivity(Activity activity){
                this.builder_face_detection_activity = activity;
                return this;
            }

            public Builder setModelPath(String path){
                this.builder_face_detection_model_path = checkNotNull(path);
                return this;
            }

            public Builder setModelType(String type){
                this.builder_face_detection_model_type = checkNotNull(type);
                return this;
            }

            public Builder setModelDirection(String direction){
                this.builder_face_detection_model_direction = checkNotNull(direction);
                return this;
            }

            public Builder setModelName(String name){
                this.builder_face_detection_model_name = checkNotNull(name);
                return this;
            }

            public Builder setFaceDetectionConfig(MjpegFaceDetection.Config config){
                this.builder_face_detection_config = checkNotNull(config);
                return this;
            }

            public FaceDetection build() {
                return new FaceDetection(this);
            }
        }

        private FaceDetection(Builder builder){
            super(builder.builder_face_detection_context, builder.builder_face_detection_activity, builder.builder_face_detection_config, builder.builder_face_detection_model_path, builder.builder_face_detection_model_type, builder.builder_face_detection_model_direction, builder.builder_face_detection_model_name);
            config_face_detection_model_path      = builder.builder_face_detection_model_path;
            config_face_detection_model_type      = builder.builder_face_detection_model_type;
            config_face_detection_model_direction = builder.builder_face_detection_model_direction;
            config_face_detection_model_name      = builder.builder_face_detection_model_name;
            config_face_detection_config          = builder.builder_face_detection_config;
            config_face_detection_context         = builder.builder_face_detection_context;
            config_face_detection_activity        = builder.builder_face_detection_activity;
        }

        public Builder buildUpon() {
            return new Builder(this);
        }
    }

    public final StreamConfig Configuration;
    public final MjpegInputCamera.InputStreamConfig inputStreamConfig;
    public final FaceDetection FaceDetection;
    public final MjpegFaceDetection.Config FaceConfig;

    private CameraStream(StreamConfig configuration, MjpegInputCamera.InputStreamConfig inputConfig , FaceDetection faceDetection, MjpegFaceDetection.Config faceConfig) {
        this.Configuration     = configuration;
        this.FaceDetection     = faceDetection;
        this.FaceConfig        = faceConfig;
        this.inputStreamConfig = inputConfig;
    }

    public CameraStream.Builder buildUpon() {
        return new CameraStream.Builder(this);
    }

    public Uri getUri(){
        return Configuration.config_uri;
    }

    public boolean isFaceDetectionEnabled(){
        return Configuration.config_enable_face_detection;
    }

}