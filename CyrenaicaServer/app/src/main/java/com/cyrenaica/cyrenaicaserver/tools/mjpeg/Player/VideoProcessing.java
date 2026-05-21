package com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player;

import android.content.Context;
import android.media.MediaCodec;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;
import androidx.media3.common.Effect;
import androidx.media3.common.MediaItem;
import androidx.media3.common.VideoFrameProcessingException;
import androidx.media3.common.util.UnstableApi;

import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.UdpDataSource;
import androidx.media3.effect.OverlayEffect;
import androidx.media3.effect.RgbFilter;
import androidx.media3.effect.TextOverlay;
import androidx.media3.effect.TextureOverlay;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.video.VideoSink;
import androidx.media3.transformer.EditedMediaItem;
import androidx.media3.ui.PlayerView;

import com.cyrenaica.cyrenaicaserver.R;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@UnstableApi
public class VideoProcessing {
    private static final String TAG = "VIDEO_PROCESSING";

    MediaCodec mc;
    Context procContext;
    ExoPlayer player;
    PlayerView mPlayerView;
    Uri mUri;
    MediaItem mediaItem;
    EditedMediaItem editMediaItem;
    DataSource.Factory sourceFactory;
    public int STREAM_LIVE = 0;
    public int STREAM_FILE = 1;
    private int streamType;
    private int liveStreamType;
    public int LIVE_STREAM_HTTP = 0;
    public int LIVE_STREAM_UDP  = 1;
    int udpPacketSize;
    int udpSockTimeoutMills;

    VideoSink mSink;

    public VideoProcessing(Context context, PlayerView pView) {
        this.procContext         = context;
        this.mPlayerView         = pView;
        this.streamType          = 0;
        this.liveStreamType      = 1;
        this.udpPacketSize       = 3000;
        this.udpSockTimeoutMills = 5000;
    }

    public void setSourceUri(Uri uri){
        this.mUri = uri;
    }

    public void setStreamType(int type){
        this.streamType = type;
    }

    public void setUdpPacketSize(int size){
        this.udpPacketSize = size;
    }

    public void setUdpSockTimeoutMills(int ms){
        this.udpPacketSize = ms;
    }
    private List<Effect> effects = new ArrayList<Effect>();
    public void initPlayer() throws VideoFrameProcessingException {

        player    = new ExoPlayer.Builder(this.procContext).build();
        mediaItem = new MediaItem.Builder().setUri(this.mUri).build();


        if(streamType == STREAM_LIVE){

            if(liveStreamType == LIVE_STREAM_HTTP){

            }

            if(liveStreamType == LIVE_STREAM_UDP){

                sourceFactory                         = () -> new UdpDataSource(udpPacketSize, udpSockTimeoutMills);
                ProgressiveMediaSource udpMediaSource = new ProgressiveMediaSource.Factory(sourceFactory).createMediaSource(mediaItem);
                player.setMediaSource(udpMediaSource);

            }

        }

        mPlayerView.setPlayer(player);
        player.setPlayWhenReady(true);
        player.setVideoEffects(effects);
        player.prepare();
    }

    public ExoPlayer getPlayer(){
        return this.player;
    }

    public void Play(){

        if(player.isPlaying()){
            player.pause();
        }
        else
        {
            player.play();
        }
    }

    public void Stop(){
        if(player.isPlaying()){
            player.stop();
        }
    }

    public void Release(){
        if(player.isPlaying()){
            player.stop();
        }

        player.release();
    }

    public void setEffects(List<Effect> eff){
        effects = eff;
    }



}
