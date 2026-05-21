package com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.VideoFrameProcessingException;
import androidx.media3.common.util.GlUtil;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.effect.GlEffect;
import androidx.media3.effect.GlShaderProgram;

@UnstableApi
public interface MyEffect extends GlEffect {

    /**
     * Returns the OpenGL texture ID of the LUT to apply to the pixels of the frame with the given
     * timestamp.
     */
    int getLutTextureId(long presentationTimeUs);

    /**
     * Returns the length N of the 3D N x N x N LUT cube with the given timestamp.
     */
    int getLength(long presentationTimeUs);

    /**
     * Releases the OpenGL texture of the LUT.
     */
    void release() throws GlUtil.GlException;

    @NonNull
    @OptIn(markerClass = UnstableApi.class)
    @Override
    default GlShaderProgram toGlShaderProgram(@NonNull Context context, boolean useHdr) throws VideoFrameProcessingException {
        return new FaceDetectionShaderProgram(context, /* colorLut= */ this, useHdr);
    }
}
