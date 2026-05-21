package com.cyrenaica.cyrenaicaserver.tools.mjpeg;

import static androidx.media3.common.util.Assertions.checkNotNull;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.Properties;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.UnstableApi;

import com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player.CameraStream;
import com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player.MjpegFaceDetection;

@UnstableApi
public class MjpegInputCamera {

    private static final byte[] SOI_MARKER            = {(byte) 0xFF, (byte) 0xD8};
    private static final byte[] EOF_MARKER            = {(byte) 0xFF, (byte) 0xD9};
    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private final static int HEADER_MAX_LENGTH        = 100;
    private final static int FRAME_MAX_LENGTH         = 40000 + HEADER_MAX_LENGTH;
    private static int mContentLength                 = -1;

    public static Uri main_class_uri;
    public static String main_class_resolution_w;
    public static String main_class_resolution_h;
    public static String main_class_stream_extension; //".mjpeg" by default
    public static String main_class_connection_method; //"GET" by default
    public static int main_class_connection_port; //80 by default
    public static String main_class_http_version; //"HTTP/2" by default
    public static boolean main_class_enable_face_detection;
    public static boolean main_class_start_detection_now;


    @UnstableApi
    public static final class Builder {

        private Uri main_builder_uri;
        private String main_builder_resolution_w;
        private String main_builder_resolution_h;
        private String main_builder_stream_extension;
        private String main_builder_connection_method;
        private int main_builder_connection_port;
        private String main_builder_http_version;
        private boolean main_builder_enable_face_detection;

        private InputStreamConfig.Builder inputConfig;

        public Builder(){
            inputConfig = new InputStreamConfig.Builder();
        }

        public Builder(MjpegInputCamera inputCamera){
            this();

            InputStreamConfig config = input_stream_config;

            if(config != null) {
                this.main_builder_uri                   = config.input_class_uri;
                this.main_builder_resolution_w          = config.input_class_resolution_w;
                this.main_builder_resolution_h          = config.input_class_resolution_h;
                this.main_builder_stream_extension      = config.input_class_stream_extension;
                this.main_builder_http_version          = config.input_class_http_version;
                this.main_builder_connection_method     = config.input_class_connection_method;
                this.main_builder_connection_port       = config.input_class_connection_port;
                this.main_builder_enable_face_detection = config.input_class_enable_face_detection;
            }
        }

        public Builder setInputStreamConfiguration(InputStreamConfig inputStreamConfiguration) {
            this.inputConfig = inputStreamConfiguration.buildUpon();
            return this;
        }

        public MjpegInputCamera build() {
            InputStreamConfig newConfig = new InputStreamConfig(inputConfig);
            return new MjpegInputCamera(newConfig);
        }

    }

    public static final class InputStreamConfig {

        public final Uri input_class_uri;
        public final String input_class_resolution_w;
        public final String input_class_resolution_h;
        public final String input_class_stream_extension;
        public final String input_class_connection_method;
        public final int input_class_connection_port;
        public final String input_class_http_version;
        public boolean input_class_enable_face_detection = false;
        public boolean input_class_start_detection_now = false;

        @UnstableApi
        public static final class Builder {

            private Uri input_builder_uri;
            private String input_builder_resolution_w = "480";
            private String input_builder_resolution_h = "320";
            private String input_builder_stream_extension = "mjpeg";
            private String input_builder_connection_method = "GET";
            private int input_builder_connection_port = 80;
            private String input_builder_http_version = "HTTP/2";
            private boolean input_builder_enable_face_detection;
            private boolean input_builder_start_detection_now;

            /**
             * Creates a builder.
             */
            public Builder() {

            }

            private Builder(InputStreamConfig inputCamera) {

                this();

                this.input_builder_uri                   = inputCamera.input_class_uri;
                this.input_builder_resolution_w          = inputCamera.input_class_resolution_w;
                this.input_builder_resolution_h          = inputCamera.input_class_resolution_h;
                this.input_builder_stream_extension      = inputCamera.input_class_stream_extension;
                this.input_builder_http_version          = inputCamera.input_class_http_version;
                this.input_builder_connection_method     = inputCamera.input_class_connection_method;
                this.input_builder_connection_port       = inputCamera.input_class_connection_port;
                this.input_builder_enable_face_detection = inputCamera.input_class_enable_face_detection;
                this.input_builder_start_detection_now   = inputCamera.input_class_start_detection_now;
            }

            public Builder setUri(Uri uri) {
                this.input_builder_uri = checkNotNull(uri);
                return this;
            }

            public Builder setResolutionW(String w) {
                this.input_builder_resolution_w = checkNotNull(w);
                return this;
            }

            public Builder setResolutionH(String h) {
                this.input_builder_resolution_h = checkNotNull(h);
                return this;
            }

            public Builder setStreamExtension(String extension) {
                this.input_builder_stream_extension = checkNotNull(extension);
                return this;
            }

            public Builder setConnectionMethod(String method) {
                this.input_builder_connection_method = checkNotNull(method);
                return this;
            }

            public Builder setConnectionPort(int port) {
                this.input_builder_connection_port = port;
                return this;
            }

            public Builder setHttpVersion(String version) {
                this.input_builder_http_version = checkNotNull(version);
                return this;
            }

            public Builder setEnableFaceDetection(boolean enable) {
                this.input_builder_enable_face_detection = enable;
                return this;
            }

            public Builder setStartDetectionNow(boolean start) {
                this.input_builder_start_detection_now = start;
                return this;
            }

            public InputStreamConfig build() {
                return new InputStreamConfig(this);
            }
        }

        @OptIn(markerClass = UnstableApi.class)
        public InputStreamConfig(Builder builder) {
            this.input_class_uri                   = builder.input_builder_uri;
            this.input_class_resolution_w          = builder.input_builder_resolution_w;
            this.input_class_resolution_h          = builder.input_builder_resolution_h;
            this.input_class_stream_extension      = builder.input_builder_stream_extension;
            this.input_class_connection_method     = builder.input_builder_connection_method;
            this.input_class_connection_port       = builder.input_builder_connection_port;
            this.input_class_http_version          = builder.input_builder_http_version;
            this.input_class_enable_face_detection = builder.input_builder_enable_face_detection;
            this.input_class_start_detection_now   = builder.input_builder_start_detection_now;

            main_class_uri                         = this.input_class_uri;
            main_class_resolution_w                = this.input_class_resolution_w;
            main_class_resolution_h                = this.input_class_resolution_h;
            main_class_stream_extension            = this.input_class_stream_extension;
            main_class_connection_method           = this.input_class_connection_method;
            main_class_connection_port             = this.input_class_connection_port;
            main_class_http_version                = this.input_class_http_version;
            main_class_enable_face_detection       = this.input_class_enable_face_detection;
            main_class_start_detection_now         = this.input_class_start_detection_now;
        }

        @OptIn(markerClass = UnstableApi.class)
        public Builder buildUpon() {
            return new Builder(this);
        }
    }

    public static class MjpegInputStream extends DataInputStream {
        private static final String TAG = "MJPEG_INPUT_STREAM";

        byte[] header = null;
        byte[] frameData = null;
        int headerLen = -1;
        int headerLenPrev = -1;
        int skip = 1;
        int count = 0;

        static String scheme;
        static String hostname;
        static int port;
        static Socket socket;
        private static final int BUFFER_SIZE = 1024 * 5; // 5MB

        public MjpegInputStream(InputStream in) {
            super(new BufferedInputStream(in, FRAME_MAX_LENGTH));
        }

        public static MjpegInputStream udpStreamRead() throws IOException {
            return null;
        }

        public static MjpegInputStream httpRead(String url) throws IOException {

            try {
                URL http_url = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) http_url.openConnection();
                return new MjpegInputStream(urlConnection.getInputStream());
            } catch (Exception e) {
                Log.e(TAG, "Error while http connection", e);
            }

            return null;
        }

        @OptIn(markerClass = UnstableApi.class)
        public static MjpegInputStream read(Uri uri) throws IOException {

            hostname = checkNotNull(uri).getHost();
            scheme   = checkNotNull(uri).getScheme();
            port     = main_class_connection_port; // [80] by default

            PrintWriter output_request    = null;
            InputStream input_byte_stream = null;

            String resolution;

            if (checkNotNull(scheme).equals("http")) {

                socket                = new Socket(hostname, port);
                output_request        = new PrintWriter(socket.getOutputStream());
                input_byte_stream     = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input_byte_stream));

                resolution = main_class_resolution_w + "x" + main_class_resolution_h + "." + main_class_stream_extension; // 640x480.mjpeg ** 480x320.mjpeg = length: 7157

                output_request.write(
                    String.format(
                        main_class_connection_method + " /" + resolution + " " + main_class_http_version + "\r\n"
                        + "Host: " + hostname + "\r\n"
                        + "Connection: keep-alive\r\n"
                        + "\r\n"
                    )
                );

                output_request.flush();
            }

            return new MjpegInputStream(input_byte_stream);
        }

        private int getEndOfSeqeunce(DataInputStream in, byte[] sequence) throws IOException {
            int seqIndex = 0;
            byte c;
            for (int i = 0; i < FRAME_MAX_LENGTH; i++) {
                c = (byte) in.readUnsignedByte();
                if (c == sequence[seqIndex]) {
                    seqIndex++;
                    if (seqIndex == sequence.length) return i + 1;
                } else seqIndex = 0;
            }
            return -1;
        }

        private int getStartOfSequence(DataInputStream in, byte[] sequence) throws IOException {
            int end = getEndOfSeqeunce(in, sequence);
            return (end < 0) ? (-1) : (end - sequence.length);
        }

        private int getEndOfSeqeunceSimplified(DataInputStream in, byte[] sequence) throws IOException {
            int startPos = mContentLength / 2;
            int endPos = 3 * mContentLength / 2;

            skipBytes(headerLen + startPos);

            int seqIndex = 0;
            byte c;
            for (int i = 0; i < endPos - startPos; i++) {
                c = (byte) in.readUnsignedByte();
                if (c == sequence[seqIndex]) {
                    seqIndex++;
                    if (seqIndex == sequence.length) {

                        return headerLen + startPos + i + 1;
                    }
                } else seqIndex = 0;
            }


            return -1;
        }

        private int parseContentLength(byte[] headerBytes) throws IOException, NumberFormatException {
            ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
            Properties props = new Properties();
            props.load(headerIn);
            return Integer.parseInt(props.getProperty(HEADER_CONTENT_LENGTH));
        }

        public Bitmap readMjpegFrame() throws IOException {

            mark(FRAME_MAX_LENGTH);

            int headerLen = getStartOfSequence(this, SOI_MARKER);

            reset();

            byte[] header = new byte[headerLen];

            readFully(header);

            try {
                mContentLength = parseContentLength(header);
            }
            catch (NumberFormatException nfe) {
                mContentLength = getEndOfSeqeunce(this, EOF_MARKER);
            }

            reset();

            byte[] frameData = new byte[mContentLength];

            skipBytes(headerLen);
            readFully(frameData);

            Bitmap bmp = BitmapFactory.decodeStream(new ByteArrayInputStream(frameData));

            //Log.d(TAG, "HEADERS: " + " ---- [LENGTH] -> " + headerLen + " DATA: " + " ---- [LENGTH] -> " + frameData.length + " FRAME: " +  " ---- [WIDTH] -> " + bmp.getWidth() + " FRAME: " + " ---- [HEIGHT] -> " + bmp.getHeight() + " FACE DETECTION " + " ---- [ENABLED] -> " + main_class_enable_face_detection);
            //Bitmap _bb =null;
            if (main_class_enable_face_detection) {
                //Log.d(TAG, "DETECTION -> RUNNING ...");
                //Mat face_mat = Imgcodecs.imdecode(new MatOfByte(frameData), Imgcodecs.IMREAD_UNCHANGED);
                //_bb = MjpegFaceDetection.recognizeFaces(face_mat, bmp);
                //Utils.matToBitmap(face_mat, bmp);
                return MjpegFaceDetection.faceDetection(frameData);
            }

            return bmp;
        }

        public int readMjpegFrame(Bitmap bmp) throws IOException {

            mark(FRAME_MAX_LENGTH);

            int headerLen;

            try {
                headerLen = getStartOfSequence(this, SOI_MARKER);
            }
            catch (IOException e) {
                Log.d(TAG, "IOException in betting headerLen.");
                reset();
                return -1;
            }

            reset();

            if (header == null || headerLen != headerLenPrev) {
                header = new byte[headerLen];
                Log.d(TAG, "header renewed " + headerLenPrev + " -> " + headerLen);
            }

            headerLenPrev = headerLen;

            readFully(header);

            int ContentLengthNew = -1;

            try {
                ContentLengthNew = parseContentLength(header);
            }
            catch (NumberFormatException nfe) {

                ContentLengthNew = getEndOfSeqeunceSimplified(this, EOF_MARKER);

                if (ContentLengthNew < 0) {
                    Log.d(TAG, "Worst case for finding EOF_MARKER");
                    reset();
                    ContentLengthNew = getEndOfSeqeunce(this, EOF_MARKER);
                }

            }
            catch (IllegalArgumentException e) {

                Log.d(TAG, "IllegalArgumentException in parseContentLength");
                ContentLengthNew = getEndOfSeqeunceSimplified(this, EOF_MARKER);

                if (ContentLengthNew < 0) {
                    Log.d(TAG, "Worst case for finding EOF_MARKER");
                    reset();
                    ContentLengthNew = getEndOfSeqeunce(this, EOF_MARKER);
                }

            }
            catch (IOException e) {
                Log.d(TAG, "IOException in parseContentLength");
                reset();
                return -1;
            }

            mContentLength = ContentLengthNew;

            reset();

            if (frameData == null) {
                frameData = new byte[FRAME_MAX_LENGTH];
                Log.d(TAG, "frameData newed cl=" + FRAME_MAX_LENGTH);
            }

            if (mContentLength + HEADER_MAX_LENGTH > FRAME_MAX_LENGTH) {
                frameData = new byte[mContentLength + HEADER_MAX_LENGTH];
                Log.d(TAG, "frameData renewed cl=" + (mContentLength + HEADER_MAX_LENGTH));
            }

            skipBytes(headerLen);

            readFully(frameData, 0, mContentLength);

            if (count++ % skip == 0) {
                return -1; //pixeltobmp(frameData, mContentLength, bmp);
            }
            else
            {
                return 0;
            }
        }

        public void setSkip(int s) {
            skip = s;
        }

        public static void stopSockets() throws IOException {

            socket.close();
        }
    }

    public static InputStreamConfig input_stream_config;

    private MjpegInputCamera(InputStreamConfig inputStreamConfig){

        input_stream_config     = inputStreamConfig;

    }

    public Uri getUri(){
        return input_stream_config.input_class_uri;
    }

    public static String getResolutionW(){
        return main_class_resolution_w;
    }

    public static String getResolutionH(){
        return main_class_resolution_h;
    }

    public static String getStreamExtension(){
        return main_class_stream_extension;
    }

    public static String getConnectionMethod(){
        return main_class_connection_method;
    }

    public static int getConnectionPort(){
        return main_class_connection_port;
    }

    public static String getHttpVersion(){
        return main_class_http_version;
    }

    public static boolean isFaceDetectionEnabled(){
        return main_class_enable_face_detection;
    }


}
