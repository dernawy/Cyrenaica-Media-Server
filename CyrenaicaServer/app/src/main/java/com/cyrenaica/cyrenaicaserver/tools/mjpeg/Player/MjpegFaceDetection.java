package com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player;



import static android.content.Context.MODE_PRIVATE;
import static androidx.core.app.PendingIntentCompat.getActivity;
import static org.opencv.core.Core.copyTo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.icu.text.ListFormatter;
import android.media.Image;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.cyrenaica.cyrenaicaserver.R;
import com.cyrenaica.cyrenaicaserver.tools.mjpeg.MjpegHome;
import com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player.facerec.LocalCameraFaceDetectionActivity;
import com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player.facerec.SimilarityClassifier;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.types.family.TType;
import org.tensorflow.types.TUint8;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MjpegFaceDetection {

    private static final String TAG = "MJPEG_FACE_DETECTION";

    static Context mContext;
    static Activity mActivity;

    static boolean start = true;
    private static int SELECT_PICTURE = 1;

    private static final CascadeClassifier face_cascade = new CascadeClassifier();
    private static String cascade_classifier_modelPath;
    private static String cascade_classifier_modelType;
    private static String cascade_classifier_modelDirection;
    private static String cascade_classifier_modelName;
    private static Mat GRY_MAT;
    private static MatOfRect DETECTED_FACES;
    static Bitmap STREAM_IN_BITMAP;

    private final Map<String, float[]> faceDb = new HashMap< >();

    private static Session session;

    public static double class_scale_factor;
    public static int class_min_neighbors;
    public static int class_flags;
    public static double class_face_resize_w;
    public static double class_face_resize_h;

    public static boolean class_face_name_position_free;
    public static String class_face_name;
    public static double class_face_name_pos_x;
    public static double class_face_name_pos_y;
    public static int class_face_name_font;
    public static double class_face_name_font_scale;
    public static Scalar class_face_name_font_color_scalar;
    public static int class_face_name_font_thickness;
    public static Scalar class_rect_color_scalar;
    public static int class_rect_thickness;
    public static Rect class_face_rectangle;
    public static FrameLayout class_images_frame;
    public static ImageView class_face_image;
    public static Button class_remove_image_btn;
    public static Button class_save_face_btn;
    public static Button class_face_actions_btn;

    static boolean developerMode = false;
    static Interpreter tfLite;
    String modelFile = "mobile_face_net.tflite"; //model name
    private static HashMap<String, SimilarityClassifier.Recognition> registered = new HashMap<>(); //saved Faces
    static float distance = 1.1f;
    static boolean isModelQuantized = false;
    static float[][] embeedings;
    static float IMAGE_MEAN = 128.0f;
    static float IMAGE_STD = 128.0f;
    static int OUTPUT_SIZE = 192; //Output size of model
    static int[] intValues;
    static int inputSize = 112;  //Input size for model
    static FaceDetector detector;


    public static final class Builder {

        private double main_builder_scale_factor;
        private int main_builder_min_neighbors;
        private int main_builder_flags;
        private double main_builder_face_resize_w;
        private double main_builder_face_resize_h;
        private boolean main_builder_face_name_position_free;
        private String main_builder_face_name;
        private double main_builder_face_name_pos_x;
        private double main_builder_face_name_pos_y;
        private int main_builder_face_name_font;
        private double main_builder_face_name_font_scale;
        private Scalar main_builder_face_name_font_color_scalar;
        private int main_builder_face_name_font_thickness;
        private Scalar main_builder_rect_color_scalar;
        private int main_builder_rect_thickness;
        private FrameLayout main_builder_images_frame;
        private ImageView main_builder_face_image;
        private Button main_builder_remove_image_btn;
        private Button main_builder_face_actions_btn;
        private Button main_builder_save_face_btn;

        Config.Builder config_builder;

        /** Creates a builder. */
        public Builder() {
            config_builder = new Config.Builder();
        }

        private Builder(MjpegFaceDetection mjpegFaceDetection) {
            this();
            Config new_config = mjpegFaceDetection.mainConfig;

            if(new_config != null) {
                this.main_builder_scale_factor                = new_config.config_scale_factor;
                this.main_builder_min_neighbors               = new_config.config_min_neighbors;
                this.main_builder_flags                       = new_config.config_flags;
                this.main_builder_face_resize_w               = new_config.config_face_resize_w;
                this.main_builder_face_resize_h               = new_config.config_face_resize_h;

                this.main_builder_face_name_position_free     = new_config.config_face_name_position_free;
                this.main_builder_face_name                   = new_config.config_face_name;
                this.main_builder_face_name_pos_x             = new_config.config_face_name_pos_x;
                this.main_builder_face_name_pos_y             = new_config.config_face_name_pos_y;
                this.main_builder_face_name_font              = new_config.config_face_name_font;
                this.main_builder_face_name_font_scale        = new_config.config_face_name_font_scale;
                this.main_builder_face_name_font_color_scalar = new_config.config_face_name_font_color_scalar;
                this.main_builder_face_name_font_thickness    = new_config.config_face_name_font_thickness;
                this.main_builder_rect_color_scalar           = new_config.config_rect_color_scalar;
                this.main_builder_rect_thickness              = new_config.config_rect_thickness;
                this.main_builder_images_frame                = new_config.config_images_frame;
                this.main_builder_face_image                  = new_config.config_face_image;
                this.main_builder_remove_image_btn            = new_config.config_remove_image_btn;
                this.main_builder_save_face_btn               = new_config.config_save_face_btn;
                this.main_builder_face_actions_btn            = new_config.config_face_actions_btn;
            }
        }

        public Builder setFaceConfiguration(Config faceConfiguration) {
            this.config_builder = faceConfiguration.buildUpon();
            return this;
        }

        public MjpegFaceDetection build() {
            Config config = new Config(config_builder);
            return new MjpegFaceDetection(config); //**//
        }
    }

    public static final class Config {

        public final double config_scale_factor;
        public final  int config_min_neighbors;
        public final  int config_flags;
        public final double config_face_resize_w;
        public final double config_face_resize_h;
        public final boolean config_face_name_position_free;
        public final String config_face_name;
        public final double config_face_name_pos_x;
        public final double config_face_name_pos_y;
        public final int config_face_name_font;
        public final double config_face_name_font_scale;
        public final Scalar config_face_name_font_color_scalar;
        public final int config_face_name_font_thickness;
        public final Scalar config_rect_color_scalar;
        public final int config_rect_thickness;
        public final FrameLayout config_images_frame;
        public final ImageView config_face_image;
        public final Button config_remove_image_btn;
        public final Button config_face_actions_btn;
        public final Button config_save_face_btn;

        public static final class Builder {

            private double builder_scale_factor                = 1.1;                          // [1.1] by default
            private int builder_min_neighbors                  = 3;                            // [3] by default
            private int builder_flags                          = 0;                            // [0] by default
            private double builder_face_resize_w               = 160;                          // [160] by default
            private double builder_face_resize_h               = 1.1;                          // [160] by default
            private boolean builder_face_name_position_free    = false;                        // [false] by default
            private String builder_face_name                   = "UNKNOWN";                    // [UNKNOWN] by default
            private double builder_face_name_pos_x             = 10;                           // [10] by default
            private double builder_face_name_pos_y             = 10;                           // [10] by default
            private int builder_face_name_font                 = Imgproc.FONT_HERSHEY_SIMPLEX; // [FONT_HERSHEY_SIMPLEX] by default
            private double builder_face_name_font_scale        = 0.5;                          // [0.5] by default
            private Scalar builder_face_name_font_color_scalar = new Scalar(0, 255, 0);        // [0, 255, 0] by default (Green)
            private int builder_face_name_font_thickness       = 2;                            // [2] by default
            private Scalar builder_rect_color_scalar           = new Scalar(0, 255, 0);        // [0, 255, 0] by default (Green)
            private int builder_rect_thickness                 = 2;                            // [2] by default
            private FrameLayout builder_images_frame;
            private ImageView builder_face_image;
            private Button builder_remove_image_btn;
            private Button builder_save_face_btn;
            private Button builder_face_actions_btn;

            /** Creates a builder. */
            public Builder() {

            }

            private Builder(Config config) {

                this();

                this.builder_scale_factor                = config.config_scale_factor;
                this.builder_min_neighbors               = config.config_min_neighbors;
                this.builder_flags                       = config.config_flags;
                this.builder_face_resize_w               = config.config_face_resize_w;
                this.builder_face_resize_h               = config.config_face_resize_h;

                this.builder_face_name_position_free     = config.config_face_name_position_free;
                this.builder_face_name                   = config.config_face_name;
                this.builder_face_name_pos_x             = config.config_face_name_pos_x;
                this.builder_face_name_pos_y             = config.config_face_name_pos_y;
                this.builder_face_name_font              = config.config_face_name_font;
                this.builder_face_name_font_scale        = config.config_face_name_font_scale;
                this.builder_face_name_font_color_scalar = config.config_face_name_font_color_scalar;
                this.builder_face_name_font_thickness    = config.config_face_name_font_thickness;
                this.builder_rect_color_scalar           = config.config_rect_color_scalar;
                this.builder_rect_thickness              = config.config_rect_thickness;
                this.builder_images_frame                = config.config_images_frame;
                this.builder_face_image                  = config.config_face_image;
                this.builder_remove_image_btn            = config.config_remove_image_btn;
                this.builder_save_face_btn               = config.config_save_face_btn;
                this.builder_face_actions_btn            = config.config_face_actions_btn;
            }

            public Builder setScaleFactor(double scaleFactor) {
                this.builder_scale_factor = scaleFactor;
                return this;
            }

            public Builder setMinNeighbors(int min) {
                this.builder_min_neighbors = min;
                return this;
            }

            public Builder setFlags(int flags) {
                this.builder_flags = flags;
                return this;
            }

            public Builder setFaceScaleW(double w) {
                this.builder_face_resize_w = w;
                return this;
            }

            public Builder setFaceScaleH(double h) {
                this.builder_face_resize_h = h;
                return this;
            }

            public Builder setNamePositionFree(boolean free) {
                this.builder_face_name_position_free = free;
                return this;
            }

            public Builder setName(String name) {
                this.builder_face_name = name;
                return this;
            }

            public Builder setNamePositionX(int name_x) {
                this.builder_face_name_pos_x = name_x;
                return this;
            }

            public Builder setNamePositionY(int name_y) {
                this.builder_face_name_pos_y = name_y;
                return this;
            }

            public Builder setNameFont(int font) {
                this.builder_face_name_font = font;
                return this;
            }

            public Builder setNameFontScale(int scale) {
                this.builder_face_name_font_scale = scale;
                return this;
            }

            public Builder setNameFontColorScalar(Scalar scalar) {
                this.builder_face_name_font_color_scalar = scalar;
                return this;
            }

            public Builder setNameFontThickness(int thickness) {
                this.builder_face_name_font_thickness = thickness;
                return this;
            }

            public Builder setRectangleColorScalar(Scalar scalar) {
                this.builder_rect_color_scalar = scalar;
                return this;
            }

            public Builder setRectangleThickness(int thickness) {
                this.builder_rect_thickness = thickness;
                return this;
            }

            public Builder setImagesFrameLayout(FrameLayout frame){
                this.builder_images_frame = frame;
                return this;
            }

            public Builder setImageView(ImageView view){
                this.builder_face_image = view;
                return this;
            }


            public Builder setRemoveImageButton(Button remove){
                this.builder_remove_image_btn = remove;
                return this;
            }

            public Builder setFaceActionsButton(Button action){
                this.builder_face_actions_btn = action;
               return this;
            }

            public Builder setSaveFaceButton(Button save){
                this.builder_save_face_btn = save;
                return this;
            }

            public Config build() {
                return new Config(this);
            }
        }

        public Config(Builder builder){
            this.config_scale_factor                = builder.builder_scale_factor;
            this.config_min_neighbors               = builder.builder_min_neighbors;
            this.config_flags                       = builder.builder_flags;
            this.config_face_resize_w               = builder.builder_face_resize_w;
            this.config_face_resize_h               = builder.builder_face_resize_h;

            this.config_face_name_position_free     = builder.builder_face_name_position_free;
            this.config_face_name                   = builder.builder_face_name;
            this.config_face_name_pos_x             = builder.builder_face_name_pos_x;
            this.config_face_name_pos_y             = builder.builder_face_name_pos_y;
            this.config_face_name_font              = builder.builder_face_name_font;
            this.config_face_name_font_scale        = builder.builder_face_name_font_scale;
            this.config_face_name_font_color_scalar = builder.builder_face_name_font_color_scalar;
            this.config_face_name_font_thickness    = builder.builder_face_name_font_thickness;
            this.config_rect_color_scalar           = builder.builder_rect_color_scalar;
            this.config_rect_thickness              = builder.builder_rect_thickness;
            this.config_images_frame                = builder.builder_images_frame;
            this.config_face_image                  = builder.builder_face_image;
            this.config_remove_image_btn            = builder.builder_remove_image_btn;
            this.config_save_face_btn               = builder.builder_save_face_btn;
            this.config_face_actions_btn            = builder.builder_face_actions_btn;

            class_scale_factor                      = this.config_scale_factor;
            class_min_neighbors                     = this.config_min_neighbors;
            class_flags                             = this.config_flags;
            class_face_resize_w                     = this.config_face_resize_w;
            class_face_resize_h                     = this.config_face_resize_h;

            class_face_name_position_free           = this.config_face_name_position_free;
            class_face_name                         = this.config_face_name;
            class_face_name_pos_x                   = this.config_face_name_pos_x;
            class_face_name_pos_y                   = this.config_face_name_pos_y;
            class_face_name_font                    = this.config_face_name_font;
            class_face_name_font_scale              = this.config_face_name_font_scale;
            class_face_name_font_color_scalar       = this.config_face_name_font_color_scalar;
            class_face_name_font_thickness          = this.config_face_name_font_thickness;
            class_rect_color_scalar                 = this.config_rect_color_scalar;
            class_rect_thickness                    = this.config_rect_thickness;
            class_images_frame                      = this.config_images_frame;
            class_face_image                        = this.config_face_image;
            class_remove_image_btn                  = this.config_remove_image_btn;
            class_save_face_btn                     = this.config_save_face_btn;
            class_face_actions_btn                  = this.config_face_actions_btn;

        }

        public Builder buildUpon() {
            return new Builder(this);
        }
    }

    /**
     * Main construct to init the face detection class object
     * @param modelPath CascadeClassifier main path
     * @param modelType The type of mode if [HAAR] or [LBPC]
     * @param modelDirection The model face's processing direction
     * @param modelName The model file name symbol [ALT2], [IMPROVED] ...etc
     */
    public MjpegFaceDetection(Context context, Activity activity, Config config, String modelPath, String modelType, String modelDirection, String modelName){

        mContext                          = context;
        mActivity                         = activity;
        cascade_classifier_modelPath      = modelPath;
        cascade_classifier_modelType      = modelType;
        cascade_classifier_modelDirection = modelDirection;
        cascade_classifier_modelName      = modelName;
        this.mainConfig                   = config;

        registered = readFromSP(); //Load saved faces from memory when app starts

        //Load model
        try {
            tfLite = new Interpreter(loadModelFile(activity, modelFile));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        //Initialize Face Detector
        FaceDetectorOptions highAccuracyOpts = new FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE).build();
        detector                             = FaceDetection.getClient(highAccuracyOpts);

        SharedPreferences sharedPref = mContext.getSharedPreferences("Distance", MODE_PRIVATE);
        distance = sharedPref.getFloat("distance",1.00f);

        if(cascade_classifier_modelType.equals("HAAR")){

            if(cascade_classifier_modelDirection.equals("FRONTAL_FACE")){

                if(cascade_classifier_modelName.equals("DEF")){
                    face_cascade.load(cascade_classifier_modelPath + "/haarcascades/haarcascade_frontalface_default.xml");
                }

                if(cascade_classifier_modelName.equals("ALT2")){
                    face_cascade.load(cascade_classifier_modelPath + "/haarcascades/haarcascade_frontalface_alt2.xml");
                }
            }
        }

        if(cascade_classifier_modelType.equals("LBPC")){

            if(cascade_classifier_modelDirection.equals("FRONTAL_FACE")){

                if(cascade_classifier_modelName.equals("IMPROVED")){
                    face_cascade.load(cascade_classifier_modelPath + "/lbpcascades/lbpcascade_frontalface_improved.xml");
                }
            }
        }

        GRY_MAT        = new Mat();
        DETECTED_FACES = new MatOfRect();
    }

    private static void insertToSP(HashMap<String, SimilarityClassifier.Recognition> jsonMap, int mode) {

        if(mode == 1)  //mode: 0:save all, 1:clear all, 2:update all
            jsonMap.clear();
        else if (mode == 0)
            jsonMap.putAll(readFromSP());

        String jsonString = new Gson().toJson(jsonMap);

        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : jsonMap.entrySet()) {

            System.out.println("Entry Input " + entry.getKey() + " " +  entry.getValue().getExtra());
        }

        SharedPreferences sharedPreferences = mContext.getSharedPreferences("HashMap", MODE_PRIVATE);
        SharedPreferences.Editor editor     = sharedPreferences.edit();

        editor.putString("map", jsonString);

        Log.d(TAG, "Input josn " + jsonString);
        editor.apply();
        Toast.makeText(mContext, "Recognitions Saved", Toast.LENGTH_SHORT).show();
    }

    private static HashMap<String, SimilarityClassifier.Recognition> readFromSP(){

        SharedPreferences sharedPreferences = mContext.getSharedPreferences("HashMap", MODE_PRIVATE);
        String defValue                     = new Gson().toJson(new HashMap<String, SimilarityClassifier.Recognition>());
        String json                         = sharedPreferences.getString("map", defValue);

        TypeToken<HashMap<String, SimilarityClassifier.Recognition>> token = new TypeToken<HashMap<String,SimilarityClassifier.Recognition>>() {};
        HashMap<String, SimilarityClassifier.Recognition> retrievedMap     = new Gson().fromJson(json, token.getType());

        float[][] output = new float[1][OUTPUT_SIZE];

        //During type conversion and save/load procedure,format changes(eg float converted to double).
        //So embeddings need to be extracted from it in required format(eg.double to float).
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : retrievedMap.entrySet()) {

            ArrayList arrayList = (ArrayList) entry.getValue().getExtra();
            arrayList           = (ArrayList) arrayList.get(0);

            for (int counter = 0; counter < arrayList.size(); counter++) {
                output[0][counter] = ((Double) arrayList.get(counter)).floatValue();
            }

            entry.getValue().setExtra(output);
        }

        Log.d(TAG, "OUTPUT" + retrievedMap /*Arrays.deepToString(output)*/);
        Toast.makeText(mContext, "Recognitions Loaded", Toast.LENGTH_SHORT).show();
        return retrievedMap;
    }

    private static void setFaceRectangleObject(Rect rect){
        class_face_rectangle = rect;
    }

    public static Rect getFaceRectangleObject(){
        return class_face_rectangle;
    }
    static Canvas c = null;
    static Paint p = new Paint();
    static Paint np = new Paint();

    public static Bitmap fireBaseFaceDetection(byte[] frameData){


        Bitmap byte_bmp  = BitmapFactory.decodeStream(new ByteArrayInputStream(frameData));
        Bitmap bitmap    = byte_bmp.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap scaled    = Bitmap.createScaledBitmap(byte_bmp, inputSize, inputSize, true);

        InputImage image = InputImage.fromBitmap(scaled,0);

        c = new Canvas(bitmap);

        mActivity.runOnUiThread(() -> {

            class_face_actions_btn.setOnClickListener(v -> {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Select Action:");

                // add a checkbox list
                String[] names = {"View Recognition List","Update Recognition List","Save Recognitions","Load Recognitions","Clear All Recognitions","Import Photo (Beta)","Hyperparameters","Developer Mode"};

                builder.setItems(names, (dialog, which) -> {

                    switch (which)
                    {
                        case 0:
                            displaynameListview(); //View Recognition List
                            break;
                        case 1:
                            updatenameListview(); //Update Recognition List
                            break;
                        case 2:
                            insertToSP(registered,0); //mode: 0:save all, 1:clear all, 2:update all //Save Recognitions
                            break;
                        case 3:
                            registered.putAll(readFromSP()); //Load Recognitions
                            break;
                        case 4:
                            clearnameList(); //Clear All Recognitions
                            break;
                        case 5:
                            loadphoto(); //Import Photo (Beta)
                            break;
                        case 6:
                            testHyperparameter(); //Hyperparameters
                            break;
                        case 7:
                            developerMode(); //Developer Mode
                            break;
                    }
                });

                builder.setPositiveButton("OK", (dialog, which) -> {

                });

                builder.setNegativeButton("Cancel", null);

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();

            });
        });

        detector.process(image).addOnSuccessListener(new OnSuccessListener<List<Face>>() {

            @Override
            public void onSuccess(List<Face> faces) {

                if(!faces.isEmpty()) {

                    Face face = faces.get(0); //Get first face from detected faces
                    //System.out.println(face);

                    //Get bounding box of face
                    RectF boundingBox = new RectF(face.getBoundingBox());


                    //if(start) {

                        p.setColor(Color.GREEN);
                        p.setStyle(Paint.Style.STROKE);
                        np.setTextSize(20);
                        np.setColor(Color.WHITE);

                        mActivity.runOnUiThread(() -> {

                            class_images_frame.setVisibility(ImageView.VISIBLE);

                            if(class_face_image.getDrawable() == null){
                                class_face_image.setImageBitmap(bitmap);
                            }

                            class_save_face_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //displaynameListview();
                                    addFace();
                                    class_face_image.setImageBitmap(null);
                                }
                            });

                        });

                        Log.d(TAG, "NAME: " + class_face_name);

                        class_face_name = recognizeImage(scaled);

                        if(class_face_name_position_free){
                            //Imgproc.putText(face, class_face_name, new Point(class_face_name_pos_x, class_face_name_pos_y), class_face_name_font, class_face_name_font_scale, class_face_name_font_color_scalar, class_face_name_font_thickness);
                        }
                        else
                        {

                            //Imgproc.putText(face_mat, class_face_name, new Point(rect.x, rect.y - 13), class_face_name_font, class_face_name_font_scale, class_face_name_font_color_scalar, class_face_name_font_thickness);
                            c.drawText(class_face_name, boundingBox.left, boundingBox.top - 10, np);


                            //Imgproc.putText(frame, "AKRAM", new Point(rect.x, rect.y - 10), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 2);
                        }

                        c.drawRect(boundingBox.left, boundingBox.top, (boundingBox.left + boundingBox.width()), (boundingBox.top + boundingBox.height()), p);

                    //}


                }
            }
        });

        return bitmap;
    }

    public static Bitmap faceDetection(byte[] frameData){

        Mat face_mat     = Imgcodecs.imdecode(new MatOfByte(frameData), Imgcodecs.IMREAD_UNCHANGED);

        Bitmap byte_bmp  = BitmapFactory.decodeStream(new ByteArrayInputStream(frameData));
        Bitmap bitmap    = byte_bmp.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap scaled    = Bitmap.createScaledBitmap(byte_bmp, inputSize, inputSize, true);

        p.setColor(Color.GREEN);
        p.setStyle(Paint.Style.STROKE);
        np.setTextSize(20);
        np.setColor(Color.WHITE);

        Imgproc.cvtColor(face_mat, GRY_MAT, Imgproc.COLOR_RGBA2GRAY);

        face_cascade.detectMultiScale(GRY_MAT, DETECTED_FACES, class_scale_factor, class_min_neighbors, class_flags);

        mActivity.runOnUiThread(() -> {

            class_remove_image_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    class_face_image.setImageBitmap(null);
                }
            });

            class_face_actions_btn.setOnClickListener(v -> {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Select Action:");

                // add a checkbox list
                String[] names= {"View Recognition List", "Update Recognition List", "Save Recognitions", "Load Recognitions", "Clear All Recognitions", "Import Photo (Beta)", "Hyperparameters", "Developer Mode"};

                builder.setItems(names, (dialog, which) -> {

                    switch (which)
                    {
                        case 0:
                            displaynameListview(); //View Recognition List
                            break;
                        case 1:
                            updatenameListview(); //Update Recognition List
                            break;
                        case 2:
                            insertToSP(registered,0); //mode: 0:save all, 1:clear all, 2:update all //Save Recognitions
                            break;
                        case 3:
                            registered.putAll(readFromSP()); //Load Recognitions
                            break;
                        case 4:
                            clearnameList(); //Clear All Recognitions
                            break;
                        case 5:
                            loadphoto(); //Import Photo (Beta)
                            break;
                        case 6:
                            testHyperparameter(); //Hyperparameters
                            break;
                        case 7:
                            developerMode(); //Developer Mode
                            break;
                    }
                });

                builder.setPositiveButton("OK", (dialog, which) -> {

                });

                builder.setNegativeButton("Cancel", null);

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();

            });
        });

        c = new Canvas(bitmap);
        float rect_x = 0;
        float rect_y = 0;
        float rect_w = 0;
        float rect_h = 0;

        if(!DETECTED_FACES.empty()) {

            for (Rect rect : DETECTED_FACES.toArray()) {

                rect_x = rect.x;
                rect_y = rect.y;
                rect_w = rect.width;
                rect_h = rect.height;

                if (class_face_name.equals("UNKNOWN")) {

                    class_face_name = recognizeImage(scaled);

                    Log.d(TAG, "NAME_1: " + class_face_name);

                    mActivity.runOnUiThread(() -> {

                        class_images_frame.setVisibility(ImageView.VISIBLE);

                        if (class_face_image.getDrawable() == null) {
                            class_face_image.setImageBitmap(bitmap);
                        }

                        class_save_face_btn.setOnClickListener(v -> {
                            class_images_frame.setVisibility(ImageView.GONE);
                            addFace();
                        });

                    });
                }
            }

            c.drawText(class_face_name, rect_x, rect_y - 10, np);

            c.drawRect(rect_x, rect_y, (rect_x + rect_w), (rect_y + rect_h), p);
        }

        return bitmap;

    }

    @SuppressLint("DefaultLocale")
    public static String recognizeImage(final Bitmap bitmap) {

        String face_name = "";

        //Create ByteBuffer to store normalized image
        //ByteBuffer imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);
        ByteBuffer imgData = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4);

        imgData.order(ByteOrder.nativeOrder());

        intValues = new int[inputSize * inputSize];

        //get pixel values from Bitmap to normalize
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();

        for (int i = 0; i < inputSize; ++i) {

            for (int j = 0; j < inputSize; ++j) {

                int pixelValue = intValues[i * inputSize + j];

                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                }
                else // Float model
                {
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);

                }
            }
        }

        //imgData is input to our model
        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();

        embeedings = new float[1][OUTPUT_SIZE]; //output of model will be stored in this variable

        outputMap.put(0, embeedings);

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap); //Run model

        float distance_local = Float.MAX_VALUE;
        String id            = "0";
        String label         = "?";

        //Compare new face with saved Faces.
        if (!registered.isEmpty()) {

            Log.d(TAG, "recognizeImage() -> embeedings -> " + Arrays.deepToString(embeedings));

            final List<Pair<String, Float>> nearest = findNearest(embeedings[0]); //Find 2 closest matching face

            Log.d(TAG, "recognizeImage() -> nearest -> " + nearest);
            Log.d(TAG, "recognizeImage() -> nearest.get(0) -> " + nearest.get(0));

            if (nearest.get(0) != null) {

                final String name = nearest.get(0).first; //get name and distance of closest matching face

                Log.d(TAG, "recognizeImage() -> name -> " + name);

                label = name;
                distance_local = nearest.get(0).second;

                Log.d(TAG, "recognizeImage() -> distance_local -> " + distance_local);

                if (developerMode) {

                    if(distance_local < distance) //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                        face_name = "Nearest: " + name + "\nDist: " + String.format("%.3f", distance_local) + "\n2nd Nearest: " + nearest.get(1).first + "\nDist: " + String.format("%.3f", nearest.get(1).second);
                    else
                        face_name = "Unknown " + "\nDist: " + String.format("%.3f", distance_local) + "\nNearest: " + name + "\nDist: " + String.format("%.3f", distance_local) + "\n2nd Nearest: " + nearest.get(1).first + "\nDist: " + String.format("%.3f",nearest.get(1).second);
                }
                else
                {
                    if(distance_local <= distance) //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                        face_name =  name;
                    else
                        face_name = "Unknown";
                }
            }
        }

        return face_name;
    }

    private static void addFace() {

        {

            start = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Enter Name");

            final TextView error_holder = new TextView(mContext);
            builder.setView(error_holder);

            // Set up the input
            final EditText input = new EditText(mContext);

            input.setInputType(InputType.TYPE_CLASS_TEXT );
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //Create and Initialize new object with Face embeddings and Name.
                    SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition("0", "", -1f);
                    result.setExtra(embeedings);



                    String new_name = input.getText().toString();

                    if(registered.get(new_name) == null){
                        registered.put(new_name, result);
                        Log.d(TAG, "RESULT: " + registered);
                        start = true;
                        //class_face_name = new_name;
                    }
                    else
                    {

                        start = false;
                        error_holder.setText("This name added before!");
                        return;
                    }
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    start = true;
                    dialog.cancel();
                }
            });

            builder.show();
        }
    }

    private static void displaynameListview() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        // System.out.println("Registered"+registered);
        if(registered.isEmpty())
            builder.setTitle("No Faces Added!!");
        else
            builder.setTitle("Recognitions:");

        // add a checkbox list
        String[] names         = new String[registered.size()];
        boolean[] checkedItems = new boolean[registered.size()];

        int i = 0;

        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : registered.entrySet()) {

            System.out.println("NAME: " + entry.getKey() + "\n");

            names[i]        = entry.getKey();
            checkedItems[i] = false;
            i = i + 1;
        }

        builder.setItems(names,null);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static void updatenameListview() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        if(registered.isEmpty()) {
            builder.setTitle("No Faces Added!!");
            builder.setPositiveButton("OK",null);
        }
        else
        {
            builder.setTitle("Select Recognition to delete:");

            // add a checkbox list
            String[] names         = new String[registered.size()];
            boolean[] checkedItems = new boolean[registered.size()];
            int i                  = 0;

            for (Map.Entry<String, SimilarityClassifier.Recognition> entry : registered.entrySet()) {

                names[i] = entry.getKey();
                checkedItems[i] = false;
                i = i + 1;

            }

            builder.setMultiChoiceItems(names, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    // user checked or unchecked a box
                    //Toast.makeText(MainActivity.this, names[which], Toast.LENGTH_SHORT).show();
                    checkedItems[which] = isChecked;

                }
            });

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // System.out.println("status:"+ Arrays.toString(checkedItems));
                    for(int i = 0; i < checkedItems.length; i++) {

                        //System.out.println("status:"+checkedItems[i]);
                        if(checkedItems[i]) {

                            //Toast.makeText(MainActivity.this, names[i], Toast.LENGTH_SHORT).show();
                            registered.remove(names[i]);
                        }
                    }

                    insertToSP(registered,2); //mode: 0:save all, 1:clear all, 2:update all
                    Toast.makeText(mContext, "Recognitions Updated", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", null);

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private static void clearnameList() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Do you want to delete all Recognitions?");

        builder.setPositiveButton("Delete All", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                registered.clear();
                Toast.makeText(mContext, "Recognitions Cleared", Toast.LENGTH_SHORT).show();
            }
        });

        insertToSP(registered,1);
        builder.setNegativeButton("Cancel",null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static void loadphoto() {

        start = false;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    private static void testHyperparameter() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Select Hyperparameter:");

        // add a checkbox list
        String[] names = {"Maximum Nearest Neighbour Distance"};

        builder.setItems(names, (dialog, which) -> {

            switch (which) {
                case 0:
                    //Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show();
                    hyperparameters();
                    break;
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static void hyperparameters() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Euclidean Distance");
        builder.setMessage("0.00 -> Perfect Match\n1.00 -> Default\nTurn On Developer Mode to find optimum value\n\nCurrent Value:");

        // Set up the input
        final EditText input = new EditText(mContext);

        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        SharedPreferences sharedPref =  mContext.getSharedPreferences("Distance",Context.MODE_PRIVATE);
        distance                     = sharedPref.getFloat("distance",1.00f);

        input.setText(String.valueOf(distance));

        // Set up the buttons
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(context, input.getText().toString(), Toast.LENGTH_SHORT).show();

                distance= Float.parseFloat(input.getText().toString());


                SharedPreferences sharedPref    =  mContext.getSharedPreferences("Distance",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                editor.putFloat("distance", distance);
                editor.apply();

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        builder.show();
    }

    private static void developerMode() {

        if (developerMode) {
            developerMode = false;
            Toast.makeText(mContext, "Developer Mode OFF", Toast.LENGTH_SHORT).show();
        }
        else
        {
            developerMode = true;
            Toast.makeText(mContext, "Developer Mode ON", Toast.LENGTH_SHORT).show();
        }
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {

        int width  = bm.getWidth();
        int height = bm.getHeight();

        float scaleWidth  = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();

        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();

        return resizedBitmap;
    }

    private String recognizeFace(float[] embedding) {

        String name = "Unknown";

        double minDistance = Double.MAX_VALUE;
        double threshold   = 0.7;

        for (Map.Entry<String, float[]> entry : faceDb.entrySet()) {
            float[] dbEmbedding = entry.getValue();
            double distance = calculateDistance(embedding, dbEmbedding);
            if (distance < minDistance) {
                minDistance = distance;
                name = entry.getKey();
            }
        }

        if (minDistance > threshold) {
            name = "Unknown";
        }
        return name;
    }

    private double calculateDistance(float[] embedding1, float[] embedding2) {
        double sum = 0.0;
        for (int i = 0; i < embedding1.length; i++) {
            sum += Math.pow(embedding1[i] - embedding2[i], 2);
        }
        return Math.sqrt(sum);
    }

    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {

        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream        = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel            = inputStream.getChannel();
        long startOffset                   = fileDescriptor.getStartOffset();
        long declaredLength                = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    //Compare Faces by distance between face embeddings
    private static List<Pair<String, Float>> findNearest(float[] emb) {

        List<Pair<String, Float>> neighbour_list = new ArrayList<Pair<String, Float>>();
        Pair<String, Float> ret                  = null; //to get closest match
        Pair<String, Float> prev_ret             = null; //to get second closest match

        // Get set Entry from registered
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : registered.entrySet()) {

            final String name      = entry.getKey(); // This key is person name
            final float[] knownEmb = ((float[][]) entry.getValue().getExtra())[0];

            float distance = 0;

            for (int i = 0; i < emb.length; i++) {

                float diff = emb[i] - knownEmb[i];
                distance  += diff * diff;
            }

            distance = (float) Math.sqrt(distance);

            if (ret == null || distance < ret.second) {
                prev_ret = ret;
                ret      = new Pair<>(name, distance);
            }
        }

        if(prev_ret == null) {
            prev_ret = ret;
        }

        neighbour_list.add(ret);
        neighbour_list.add(prev_ret);

        return neighbour_list;

    }

    private static Bitmap toBitmap(Image image) {

        byte[] nv21 = YUV_420_888toNV21(image);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new android.graphics.Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        //System.out.println("bytes"+ Arrays.toString(imageBytes));

        //System.out.println("FORMAT"+image.getFormat());

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private static byte[] YUV_420_888toNV21(Image image) {

        int width = image.getWidth();
        int height = image.getHeight();
        int ySize = width*height;
        int uvSize = width*height/4;

        byte[] nv21 = new byte[ySize + uvSize*2];

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer(); // Y
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer(); // U
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer(); // V

        int rowStride = image.getPlanes()[0].getRowStride();
        assert(image.getPlanes()[0].getPixelStride() == 1);

        int pos = 0;

        if (rowStride == width) { // likely
            yBuffer.get(nv21, 0, ySize);
            pos += ySize;
        }
        else
        {

            long yBufferPos = -rowStride; // not an actual position

            for (; pos<ySize; pos+=width) {
                yBufferPos += rowStride;
                yBuffer.position((int) yBufferPos);
                yBuffer.get(nv21, pos, width);
            }
        }

        rowStride = image.getPlanes()[2].getRowStride();
        int pixelStride = image.getPlanes()[2].getPixelStride();

        assert(rowStride == image.getPlanes()[1].getRowStride());
        assert(pixelStride == image.getPlanes()[1].getPixelStride());

        if (pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1)) {

            // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
            byte savePixel = vBuffer.get(1);

            try {

                vBuffer.put(1, (byte)~savePixel);

                if (uBuffer.get(0) == (byte)~savePixel) {
                    vBuffer.put(1, savePixel);
                    vBuffer.position(0);
                    uBuffer.position(0);
                    vBuffer.get(nv21, ySize, 1);
                    uBuffer.get(nv21, ySize + 1, uBuffer.remaining());

                    return nv21; // shortcut
                }
            }
            catch (ReadOnlyBufferException ex) {
                // unfortunately, we cannot check if vBuffer and uBuffer overlap
            }

            // unfortunately, the check failed. We must save U and V pixel by pixel
            vBuffer.put(1, savePixel);
        }

        // other optimizations could check if (pixelStride == 1) or (pixelStride == 2),
        // but performance gain would be less significant

        for (int row=0; row<height/2; row++) {

            for (int col=0; col<width/2; col++) {
                int vuPos = col*pixelStride + row*rowStride;
                nv21[pos++] = vBuffer.get(vuPos);
                nv21[pos++] = uBuffer.get(vuPos);
            }
        }

        return nv21;
    }

    static public float[] getFaceEmbedding(Mat face) {
        float[] embedding = null;
        try (Tensor tensor =  normalizeImage(face)) {
            Tensor output = session.runner().feed("input_1", tensor).fetch("Bottleneck_BatchNorm/batchnorm/add_1").run().get(0);

            //embedding = new float[(int) output.numBytes()[1]];
            //output.copyTo(embedding);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return embedding;
    }

    static private float[][] getEmbedding(Bitmap scaled){

        ByteBuffer imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);

        imgData.order(ByteOrder.nativeOrder());

        intValues = new int[inputSize * inputSize];

        //get pixel values from Bitmap to normalize
        scaled.getPixels(intValues, 0, scaled.getWidth(), 0, 0, scaled.getWidth(), scaled.getHeight());

        imgData.rewind();

        for (int i = 0; i < inputSize; ++i) {

            for (int j = 0; j < inputSize; ++j) {

                int pixelValue = intValues[i * inputSize + j];

                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                }
                else // Float model
                {
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);

                }
            }
        }

        //imgData is input to our model
        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();

        embeedings = new float[1][OUTPUT_SIZE]; //output of model will be stored in this variable

        outputMap.put(0, embeedings);

        return embeedings;

    }

    static private Tensor normalizeImage(Mat mat) {
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
        mat.convertTo(mat, CvType.CV_8UC3);
        Core.divide(mat, Scalar.all(255.0f), mat);

        //return Tensor.of(mat, mat.reshape(160, 160));
        return null;
    }

    public void loadFaceDb(String dbPath) {

        try {

            BufferedReader reader = new BufferedReader(new FileReader(dbPath));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                String name = values[0];
                String[] cor = values[1].split(" ");
                Stream<String> s = Arrays.stream(cor);

                Log.d(TAG, "STREAM ARRAY: " + s);
                //float[] embedding = s.toArray();
                //faceDb.put(name, embedding);
            }

            reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final Config mainConfig;

    private MjpegFaceDetection(Config mainConfig){
        this.mainConfig = mainConfig;
    }

    public static void setModelPath(String path){
        cascade_classifier_modelPath = path;
    }

    public static String getModelPath(){
        return cascade_classifier_modelPath;
    }

    public static void setModelType(String type){
        cascade_classifier_modelType = type;
    }

    public static String getModelType(){
        return cascade_classifier_modelType;
    }

    public static void setModelDirection(String direction){
        cascade_classifier_modelDirection = direction;
    }

    public static String getModelDirection(){
        return cascade_classifier_modelDirection;
    }

    public static void setModelName(String name){
        cascade_classifier_modelName = name;
    }

    public static String getModelName(){
        return cascade_classifier_modelName;
    }

    public static void setStreamInBitmap(Bitmap inputBitmap){
        STREAM_IN_BITMAP = inputBitmap;
    }

    public static double getScaleFactor(){
        return class_scale_factor;
    }

    public static int getMinNeighbors(){
        return class_min_neighbors;
    }

    public static int getFlags(){
        return class_flags;
    }


}
