package com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player;

import static androidx.media3.common.util.Assertions.checkArgument;
import static androidx.media3.common.util.Assertions.checkNotNull;
import static androidx.media3.common.util.Assertions.checkState;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.StreamKey;
import androidx.media3.common.util.BundleCollectionUtil;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@UnstableApi
public final class StreamItem {

    /**
     * Creates a {@link StreamItem} for the given URI.
     *
     * @param uri The URI.
     * @return An {@link StreamItem} for the given URI.
     */
    @OptIn(markerClass = UnstableApi.class)
    public static StreamItem fromUri(String uri) {
        return new StreamItem.Builder().setUri(uri).build();
    }

    /**
     * Creates a {@link StreamItem} for the given {@link Uri URI}.
     *
     * @param uri The {@link Uri uri}.
     * @return An {@link StreamItem} for the given URI.
     */
    @OptIn(markerClass = UnstableApi.class)
    public static StreamItem fromUri(Uri uri) {
        return new StreamItem.Builder().setUri(uri).build();
    }

    /** A builder for {@link StreamItem} instances. */
    @UnstableApi
    public static final class Builder {

        @Nullable
        private String mediaId;
        @Nullable private Uri uri;
        private long imageDurationMs;
        private List<StreamKey> streamKeys;
        @Nullable private String customCacheKey;

        /** Creates a builder. */
        public Builder() {

            streamKeys = Collections.emptyList();
            imageDurationMs = C.TIME_UNSET;
        }

        private Builder(StreamItem streamItem) {

            this();

            mediaId = streamItem.mediaId;

            @Nullable StreamItem.LocalConfiguration localConfiguration = streamItem.localConfiguration;
            if (localConfiguration != null) {
                customCacheKey  = localConfiguration.customCacheKey;
                uri             = localConfiguration.uri;
                streamKeys      = localConfiguration.streamKeys;
                imageDurationMs = localConfiguration.imageDurationMs;
            }
        }

        /**
         * Sets the optional media ID which identifies the media item.
         *
         * <p>By default {@link #DEFAULT_MEDIA_ID} is used.
         */
        public StreamItem.Builder setMediaId(String mediaId) {
            this.mediaId = checkNotNull(mediaId);
            return this;
        }

        /**
         * Sets the optional URI.
         *
         * <p>If {@code uri} is null or unset then no {@link StreamItem.LocalConfiguration} object is created
         * during {@link #build()} and no other {@code Builder} methods that would populate {@link
         * StreamItem#localConfiguration} should be called.
         */
        public StreamItem.Builder setUri(@Nullable String uri) {
            return setUri(uri == null ? null : Uri.parse(uri));
        }

        /**
         * Sets the optional URI.
         *
         * <p>If {@code uri} is null or unset then no {@link StreamItem.LocalConfiguration} object is created
         * during {@link #build()} and no other {@code Builder} methods that would populate {@link
         * StreamItem#localConfiguration} should be called.
         */
        public StreamItem.Builder setUri(@Nullable Uri uri) {
            this.uri = uri;
            return this;
        }

        public StreamItem.Builder setImageDurationMs(long imageDurationMs) {
            checkArgument(imageDurationMs > 0 || imageDurationMs == C.TIME_UNSET);
            this.imageDurationMs = imageDurationMs;
            return this;
        }

        /** Returns a new {@link MediaItem} instance with the current builder values. */
        public StreamItem build() {

            @Nullable StreamItem.LocalConfiguration localConfiguration = null;
            @Nullable Uri uri                                          = this.uri;

            if (uri != null) {
                localConfiguration = new StreamItem.LocalConfiguration(uri, streamKeys, customCacheKey, imageDurationMs);
            }

            return new StreamItem(mediaId != null ? mediaId : DEFAULT_MEDIA_ID, localConfiguration);
        }
    }

    public static final class LocalConfiguration {

        /** The {@link Uri}. */
        public final Uri uri;

        /** Optional stream keys by which the manifest is filtered. */
        @UnstableApi public final List<StreamKey> streamKeys;

        /** Optional custom cache key (only used for progressive streams). */
        @UnstableApi @Nullable public final String customCacheKey;

        /** Duration for image assets in milliseconds. */
        @UnstableApi public final long imageDurationMs;

        private LocalConfiguration(Uri uri, List<StreamKey> streamKeys, @Nullable String customCacheKey, long imageDurationMs) {

            this.uri = uri;
            this.streamKeys = streamKeys;
            this.customCacheKey = customCacheKey;
            this.imageDurationMs = imageDurationMs;
        }

        @Override
        public boolean equals(@Nullable Object obj) {

            if (this == obj) {
                return true;
            }

            if (!(obj instanceof StreamItem.LocalConfiguration)) {
                return false;
            }

            StreamItem.LocalConfiguration other = (StreamItem.LocalConfiguration) obj;

            return uri.equals(other.uri) && streamKeys.equals(other.streamKeys) && Objects.equals(customCacheKey, other.customCacheKey) && imageDurationMs == other.imageDurationMs;
        }

        @Override
        public int hashCode() {
            int result = uri.hashCode();
            result = 31 * result + streamKeys.hashCode();
            result = 31 * result + (customCacheKey == null ? 0 : customCacheKey.hashCode());
            result = (int) (31L * result + imageDurationMs);
            return result;
        }

        private static final String FIELD_URI = Util.intToStringMaxRadix(0);
        private static final String FIELD_STREAM_KEYS = Util.intToStringMaxRadix(4);
        private static final String FIELD_CUSTOM_CACHE_KEY = Util.intToStringMaxRadix(5);
        private static final String FIELD_IMAGE_DURATION_MS = Util.intToStringMaxRadix(7);

        /**
         * Returns a {@link Bundle} representing the information stored in this object.
         */
        @UnstableApi
        public Bundle toBundle() {

            Bundle bundle = new Bundle();
            bundle.putParcelable(FIELD_URI, uri);

            if (!streamKeys.isEmpty()) {
                bundle.putParcelableArrayList(FIELD_STREAM_KEYS, BundleCollectionUtil.toBundleArrayList(streamKeys, StreamKey::toBundle));
            }

            if (customCacheKey != null) {
                bundle.putString(FIELD_CUSTOM_CACHE_KEY, customCacheKey);
            }

            if (imageDurationMs != C.TIME_UNSET) {
                bundle.putLong(FIELD_IMAGE_DURATION_MS, imageDurationMs);
            }

            return bundle;
        }

        /** Restores a {@code LocalConfiguration} from a {@link Bundle}. */
        @UnstableApi
        public static StreamItem.LocalConfiguration fromBundle(Bundle bundle) {

            @Nullable List<Bundle> streamKeysBundles = bundle.getParcelableArrayList(FIELD_STREAM_KEYS);

            List<StreamKey> streamKeys = streamKeysBundles == null ? ImmutableList.of() : BundleCollectionUtil.fromBundleList(StreamKey::fromBundle, streamKeysBundles);

            long imageDurationMs = bundle.getLong(FIELD_IMAGE_DURATION_MS, C.TIME_UNSET);

            return new StreamItem.LocalConfiguration(checkNotNull(bundle.getParcelable(FIELD_URI)), streamKeys, bundle.getString(FIELD_CUSTOM_CACHE_KEY), imageDurationMs);
        }
    }

    /**
     * The default media ID that is used if the media ID is not explicitly set by {@link
     * StreamItem.Builder#setMediaId(String)}.
     */
    public static final String DEFAULT_MEDIA_ID = "";

    /** Empty {@link StreamItem}. */
    public static final StreamItem EMPTY = new StreamItem.Builder().build();

    /** Identifies the media item. */
    public final String mediaId;

    /**
     * Optional configuration for local playback. May be {@code null} if shared over process
     * boundaries.
     */
    @Nullable public final LocalConfiguration localConfiguration;

    /** Returns a {@link StreamItem.Builder} initialized with the values of this instance. */
    public StreamItem.Builder buildUpon() {
        return new StreamItem.Builder(this);
    }

    private StreamItem(String mediaId, @Nullable LocalConfiguration localConfiguration) {
        this.mediaId            = mediaId;
        this.localConfiguration = localConfiguration;
    }

}
