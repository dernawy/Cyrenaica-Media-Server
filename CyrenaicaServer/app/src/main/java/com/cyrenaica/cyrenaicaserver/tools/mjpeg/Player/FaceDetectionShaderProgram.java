package com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player;

import static androidx.media3.common.util.Assertions.checkArgument;

import android.content.Context;
import android.opengl.GLES20;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.VideoFrameProcessingException;
import androidx.media3.common.util.GlProgram;
import androidx.media3.common.util.GlUtil;
import androidx.media3.common.util.Size;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.effect.BaseGlShaderProgram;
import androidx.media3.effect.ColorLut;
import androidx.media3.effect.GlEffect;

import java.io.IOException;

/** Applies a {@link ColorLut} to each frame in the fragment shader. */
/* package */ @UnstableApi
final class FaceDetectionShaderProgram extends BaseGlShaderProgram {
    private static final String VERTEX_SHADER_PATH = "shaders/vertex_shader_transformation_es3.glsl";
    private static final String FRAGMENT_SHADER_PATH = "shaders/fragment_shader_transformation_external_yuv_es3.glsl";

    private final GlProgram glProgram;
    private final MyEffect colorLut;

    /**
     * Creates a new instance.
     *
     * @param context The {@link Context}.
     * @param colorLut The {@link ColorLut} to apply to each frame in order.
     * @param useHdr Whether input textures come from an HDR source. If {@code true}, colors will be
     *     in linear RGB BT.2020. If {@code false}, colors will be in linear RGB BT.709.
     * @throws VideoFrameProcessingException If a problem occurs while reading shader files.
     */
    @OptIn(markerClass = UnstableApi.class)
    public FaceDetectionShaderProgram(Context context, MyEffect colorLut, boolean useHdr) throws VideoFrameProcessingException {
        super(/* useHighPrecisionColorComponents= */ useHdr, /* texturePoolCapacity= */ 1);
        // TODO(b/246315245): Add HDR support.
        checkArgument(!useHdr, "ColorLutShaderProgram does not support HDR colors.");
        this.colorLut = colorLut;

        try {
            glProgram = new GlProgram(context, VERTEX_SHADER_PATH, FRAGMENT_SHADER_PATH);
        } catch (IOException | GlUtil.GlException e) {
            throw new VideoFrameProcessingException(e);
        }

        // Draw the frame on the entire normalized device coordinate space, from -1 to 1, for x and y.
        glProgram.setBufferAttribute("aFramePosition", GlUtil.getNormalizedCoordinateBounds(), GlUtil.HOMOGENEOUS_COORDINATE_VECTOR_SIZE);

        float[] identityMatrix = GlUtil.create4x4IdentityMatrix();
        glProgram.setFloatsUniform("uTransformationMatrix", identityMatrix);
        glProgram.setFloatsUniform("uTexTransformationMatrix", identityMatrix);
    }

    @NonNull
    @Override
    public Size configure(int inputWidth, int inputHeight) {
        return new Size(inputWidth, inputHeight);
    }

    @Override
    public void drawFrame(int inputTexId, long presentationTimeUs) throws VideoFrameProcessingException {
        try {
            glProgram.use();
            glProgram.setSamplerTexIdUniform("uTexSampler", inputTexId, /* texUnitIndex= */ 0);
            glProgram.setSamplerTexIdUniform("uColorLut", colorLut.getLutTextureId(presentationTimeUs), /* texUnitIndex= */ 1);
            glProgram.setFloatUniform("uColorLutLength", colorLut.getLength(presentationTimeUs));
            glProgram.bindAttributesAndUniforms();

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, /* first= */ 0, /* count= */ 4);
        }
        catch (GlUtil.GlException e) {
            throw new VideoFrameProcessingException(e);
        }
    }

    @Override
    public void release() throws VideoFrameProcessingException {
        super.release();
        try {
            colorLut.release();
            glProgram.delete();
        } catch (GlUtil.GlException e) {
            throw new VideoFrameProcessingException(e);
        }
    }
}