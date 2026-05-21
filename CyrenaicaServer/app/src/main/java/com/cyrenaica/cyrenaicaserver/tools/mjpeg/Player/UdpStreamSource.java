package com.cyrenaica.cyrenaicaserver.tools.mjpeg.Player;

import static androidx.media3.common.util.Assertions.checkNotNull;

import static java.lang.Math.min;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.BaseDataSource;
import androidx.media3.datasource.DataSourceException;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.UdpDataSource;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

@UnstableApi
public final class UdpStreamSource extends BaseDataSource {

    public static final class UdpDataSourceException extends DataSourceException {

        /**
         * Creates a {@code UdpDataSourceException}.
         *
         * @param cause The error cause.
         * @param errorCode Reason of the error, should be one of the {@code ERROR_CODE_IO_*} in {@link
         *     PlaybackException.ErrorCode}.
         */
        public UdpDataSourceException(Throwable cause, @PlaybackException.ErrorCode int errorCode) {
            super(cause, errorCode);
        }
    }

    public static final int DEFAULT_MAX_PACKET_SIZE = 2000;

    /** The default socket timeout, in milliseconds. */
    public static final int DEFAULT_SOCKET_TIMEOUT_MILLIS = 8 * 1000;

    public static final int UDP_PORT_UNSET = -1;

    private final int socketTimeoutMillis;
    private final byte[] packetBuffer;
    private final DatagramPacket packet;

    @Nullable
    private Uri uri;
    @Nullable private DatagramSocket socket;
    @Nullable private MulticastSocket multicastSocket;
    @Nullable private InetAddress address;
    private boolean opened;

    private int packetRemaining;

    public UdpStreamSource() {
        this(DEFAULT_MAX_PACKET_SIZE);
    }

    public UdpStreamSource(int maxPacketSize) {
        this(maxPacketSize, DEFAULT_SOCKET_TIMEOUT_MILLIS);
    }

    public UdpStreamSource(int maxPacketSize, int socketTimeoutMillis) {
        super(/* isNetwork= */ true);
        this.socketTimeoutMillis = socketTimeoutMillis;
        packetBuffer = new byte[maxPacketSize];
        packet = new DatagramPacket(packetBuffer, 0, maxPacketSize);
    }

    @Override
    public long open(@NonNull DataSpec dataSpec) throws UdpDataSource.UdpDataSourceException {

        uri         = dataSpec.uri;
        String host = checkNotNull(uri.getHost());
        int port    = uri.getPort();

        transferInitializing(dataSpec);

        try {

            address                         = InetAddress.getByName(host);
            InetSocketAddress socketAddress = new InetSocketAddress(address, port);

            if (address.isMulticastAddress()) {
                multicastSocket = new MulticastSocket(socketAddress);
                multicastSocket.joinGroup(address);
                socket = multicastSocket;
            }
            else
            {
                socket = new DatagramSocket(socketAddress);
            }

            socket.setSoTimeout(socketTimeoutMillis);
        }
        catch (SecurityException e) {
            throw new UdpDataSource.UdpDataSourceException(e, PlaybackException.ERROR_CODE_IO_NO_PERMISSION);
        }
        catch (IOException e) {
            throw new UdpDataSource.UdpDataSourceException(e, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED);
        }

        opened = true;

        transferStarted(dataSpec);

        return C.LENGTH_UNSET;
    }

    @Override
    public int read(@NonNull byte[] buffer, int offset, int length) throws IOException {

        if (length == 0) {
            return 0;
        }

        if (packetRemaining == 0) {

            // We've read all of the data from the current packet. Get another.
            try {
                checkNotNull(socket).receive(packet);
            }
            catch (SocketTimeoutException e) {
                throw new UdpDataSource.UdpDataSourceException(e, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT);
            }
            catch (IOException e) {
                throw new UdpDataSource.UdpDataSourceException(e, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED);
            }

            packetRemaining = packet.getLength();

            bytesTransferred(packetRemaining);
        }

        int packetOffset = packet.getLength() - packetRemaining;
        int bytesToRead  = min(packetRemaining, length);

        System.arraycopy(packetBuffer, packetOffset, buffer, offset, bytesToRead);
        packetRemaining -= bytesToRead;

        return bytesToRead;
    }

    @Nullable
    @Override
    public Uri getUri() {
        return uri;
    }

    @Override
    public void close() {
        uri = null;

        if (multicastSocket != null) {

            try {
                multicastSocket.leaveGroup(checkNotNull(address));
            }
            catch (IOException e) {
                // Do nothing.
            }

            multicastSocket = null;
        }

        if (socket != null) {
            socket.close();
            socket = null;
        }

        address         = null;
        packetRemaining = 0;

        if (opened) {
            opened = false;
            transferEnded();
        }
    }

    public int getLocalPort() {

        if (socket == null) {
            return UDP_PORT_UNSET;
        }

        return socket.getLocalPort();
    }
}
