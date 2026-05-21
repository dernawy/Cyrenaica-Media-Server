/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player;

import static androidx.media3.common.util.Assertions.checkNotNull;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.media3.common.C;
import androidx.media3.common.util.GlProgram;
import androidx.media3.common.util.GlUtil;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;

import org.opencv.android.Utils;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;
import javax.microedition.khronos.opengles.GL10;

/**
 * Video processor that demonstrates how to overlay a bitmap on video output using a GL shader. The
 * bitmap is drawn using an Android {@link Canvas}.
 */
/* package */ @UnstableApi
final class BitmapOverlayVideoProcessor implements VideoProcessingGLSurfaceView.VideoProcessor {

    private static final String TAG = "BitmapOverlayVP";
    private static final int OVERLAY_WIDTH = 1920;
    private static final int OVERLAY_HEIGHT = 1080;

    private final Context context;
    private final Paint paint;
    private final int[] textures;
    private final Bitmap overlayBitmap;
    private final Bitmap logoBitmap;
    private final Canvas overlayCanvas;

    private @NonNull GlProgram program;

    private float bitmapScaleX;
    private float bitmapScaleY;

    CascadeClassifier face_cascade = new CascadeClassifier();
    private String modelPath;

    Canvas canvas;

    int mWidth;
    int mHeight;

    public BitmapOverlayVideoProcessor(Context context, String path) {

        this.context = context.getApplicationContext();
        this.modelPath = path;
        paint = new Paint();
        paint.setTextSize(64);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        textures      = new int[1];
        overlayBitmap = Bitmap.createBitmap(OVERLAY_WIDTH, OVERLAY_HEIGHT, Bitmap.Config.ARGB_8888);
        overlayCanvas = new Canvas(overlayBitmap);
        this.mWidth = 1920;
        this.mHeight = 1080;

        try {
            logoBitmap = ((BitmapDrawable) context.getPackageManager().getApplicationIcon(context.getPackageName())).getBitmap();
        }
        catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException(e);
        }
        program = null;
    }

    @Override
    public void initialize() {

        try {
            program = new GlProgram(context,/* vertexShaderFilePath= */ "bitmap_overlay_video_processor_vertex.glsl",/* fragmentShaderFilePath= */ "bitmap_overlay_video_processor_fragment.glsl");
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
        catch (GlUtil.GlException e) {
            Log.e(TAG, "Failed to initialize the shader program", e);
            return;
        }

        program.setBufferAttribute("aFramePosition", GlUtil.getNormalizedCoordinateBounds(), GlUtil.HOMOGENEOUS_COORDINATE_VECTOR_SIZE);
        program.setBufferAttribute("aTexCoords", GlUtil.getTextureCoordinateBounds(), GlUtil.HOMOGENEOUS_COORDINATE_VECTOR_SIZE);

        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, /* level= */ 0, overlayBitmap, /* border= */ 0);
    }

    @Override
    public void setSurfaceSize(int width, int height) {
        this.mWidth = 1920;
        this.mHeight = 1080;
        bitmapScaleX = (float) width / OVERLAY_WIDTH;
        bitmapScaleY = (float) height / OVERLAY_HEIGHT;
    }

    @Override
    public void draw(Surface surface, int frameTexture, long frameTimestampUs, float[] transformMatrix) {
        // Draw to the canvas and store it in a texture.
        Log.e(TAG, "transformMatrix: "+ Arrays.toString(transformMatrix));


        //String text = String.format(Locale.US, "%.02f", frameTimestampUs / (float) C.MICROS_PER_SECOND);

        //overlayCanvas.drawBitmap(face_bitmap, /* left= */ 0, /* top= */ 0, paint);
        //overlayCanvas.drawBitmap(logoBitmap, /* left= */ 32, /* top= */ 32, paint);
        //overlayCanvas.drawText(text, /* x= */ 200, /* y= */ 130, paint);


       // GLES20.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
        //GLUtils.texSubImage2D(GL10.GL_TEXTURE_2D, /* level= */ 0, /* xoffset= */ 0, /* yoffset= */ 0, overlayBitmap);

        //try {
            //GlUtil.checkGlError();
        //}
       // catch (GlUtil.GlException e) {
            //Log.e(TAG, "Failed to populate the texture", e);
        //}

        // Run the shader program.
        //GlProgram program = checkNotNull(this.program);
       // program.setSamplerTexIdUniform("uTexSampler0", frameTexture, /* texUnitIndex= */ 0);
        //program.setSamplerTexIdUniform("uTexSampler1", textures[0], /* texUnitIndex= */ 1);
        //program.setFloatUniform("uScaleX", bitmapScaleX);
        //program.setFloatUniform("uScaleY", bitmapScaleY);
       // program.setFloatsUniform("uTexTransform", transformMatrix);

        //try {
            //program.bindAttributesAndUniforms();
        //}
        //catch (GlUtil.GlException e) {
            //Log.e(TAG, "Failed to update the shader program", e);
        //}

        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, /* first= */ 0, /* count= */ 4);

        //try {
            //GlUtil.checkGlError();
        //}
        //catch (GlUtil.GlException e) {
            //Log.e(TAG, "Failed to draw a frame", e);
        //}
    }
    @Override
    public void drawFrame(int inputTexId, long presentationTimeUs, float[] transformMatrix){

        ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(mWidth * mHeight * 4);
        Bitmap bitmap;
        int texId;
        int fboId;
        try {



            int[] boundFramebuffer = new int[1];
            GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, boundFramebuffer, /* offset= */ 0);

            GLES20.glReadPixels(/* x= */ 0,/* y= */ 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
            GlUtil.checkGlError();
            bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            overlayBitmap.copyPixelsFromBuffer(pixelBuffer);

            Mat in_mat = new Mat();
            Utils.bitmapToMat(overlayBitmap, in_mat);

            if(!in_mat.empty()){
                recognizeFaces(in_mat);
            }

            Utils.matToBitmap(in_mat, overlayBitmap);

            GLES20.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
            GLUtils.texSubImage2D(GL10.GL_TEXTURE_2D, /* level= */ 0, /* xoffset= */ 0, /* yoffset= */ 0, overlayBitmap);

            /*texId = GlUtil.createTexture(bitmap.getWidth(), bitmap.getHeight(), false);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
            GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
            GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
            GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,  0, overlayBitmap,  0);*/

            try {
                GlUtil.checkGlError();
            }
            catch (GlUtil.GlException e) {
                Log.e(TAG, "Failed to populate the texture", e);
            }

            GlProgram program = checkNotNull(this.program);
            program.setSamplerTexIdUniform("uTexSampler0", inputTexId, /* texUnitIndex= */ 0);
            program.setSamplerTexIdUniform("uTexSampler1", textures[0], /* texUnitIndex= */ 1);
            program.setFloatUniform("uScaleX", bitmapScaleX);
            program.setFloatUniform("uScaleY", bitmapScaleY);
            program.setFloatsUniform("uTexTransform", transformMatrix);

            try {
                program.bindAttributesAndUniforms();
            }
            catch (GlUtil.GlException e) {
                Log.e(TAG, "Failed to update the shader program", e);
            }

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, /* first= */ 0, /* count= */ 4);

            try {
                GlUtil.checkGlError();
            }
            catch (GlUtil.GlException e) {
                Log.e(TAG, "Failed to draw a frame", e);
            }


        }
        catch (GlUtil.GlException e) {
            Log.e(TAG, "Failed to extract a frame", e);
        }
    }

    public void recognizeFaces(Mat frame) {

        Log.i(TAG, "MAT W: " + frame.width());
        Log.i(TAG, "MAT H: " + frame.height());

        if(!frame.empty()) {

            //face_cascade.load(modelPath + "/haarcascades/haarcascade_frontalface_alt2.xml");
            face_cascade.load(modelPath + "/lbpcascades/lbpcascade_frontalface_improved.xml");


            Mat gray = new Mat();

            Imgproc.cvtColor(frame, gray, Imgproc.COLOR_RGBA2GRAY);

            MatOfRect faces = new MatOfRect();

            face_cascade.detectMultiScale(gray, faces, 1.1, 3, 0);

            for (Rect rect : faces.toArray()) {
                Mat face = new Mat(frame, rect);

                Imgproc.resize(face, face, new Size(160, 160));

                Log.d(TAG, "XY: " +  " --------------[X] -> " + rect.x + " --------- [Y] -> " + rect.y + " --------------[VW] -> " + frame.width() + " --------- [VH] -> " + frame.height() + " --------------[RW] -> " + rect.width + " --------- [RH] -> " + rect.height + " --------------[FW] -> " + face.width() + " --------- [FH] -> " + face.height());

                //float[] embedding = MjpegInputStream.getFaceEmbedding(face);
                //String name = recognizeFace(embedding);
                Imgproc.putText(frame, "AKRAM", new Point(rect.x, rect.y - 10), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 2);
                Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 2);

            }
        }

       // Utils.matToBitmap(frame, overlayBitmap);

    }

    @Override
    public void release() {
        if (program != null) {
            try {
                program.delete();
            } catch (GlUtil.GlException e) {
                Log.e(TAG, "Failed to delete the shader program", e);
            }
        }
    }
}
