package com.konradrej.rcpc.client.Network;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Handles connection to server and setting up ConnectionHandler.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 2.0
 * @since 2.0
 */
public class NetworkHandler {
    private static final String TAG = NetworkHandler.class.getName();

    private final NetworkEventNotifier networkEventNotifier = new NetworkEventNotifier();

    private ConnectionHandler connectionHandler = null;
    private String ip;

    /**
     * Attempts to connect to given IP and setups ConnectionHandler if successful.
     *
     * @param ip ip address to connect to
     * @since 2.0
     */
    public void connectTo(String ip) {
        this.ip = ip;

        new Thread(() -> {
            SSLContext sslContext = SSLContextFactory.getPreconfiguredInstance();
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            try (SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket()) {
                sslSocket.setTcpNoDelay(true);
                sslSocket.setPerformancePreferences(0, 2, 1);
                sslSocket.setKeepAlive(true);

                SocketAddress socketAddress = new InetSocketAddress(ip, 666);
                sslSocket.connect(socketAddress, 5000);

                connectionHandler = new ConnectionHandler(sslSocket, networkEventNotifier);
                connectionHandler.run();
            } catch (SocketTimeoutException e) {
                Log.d(TAG, "Connection timed out.");
                networkEventNotifier.notifyNetworkEventListeners(NetworkEvent.TIMEOUT);
            } catch (IOException e) {
                Log.e(TAG, "A network error occurred, connection closed.", e);
                networkEventNotifier.notifyNetworkEventListeners(NetworkEvent.ERROR, e);
            }
        }).start();
    }

    /**
     * Gets connected ip.
     *
     * @return connection ip
     * @since 2.0
     */
    public String getIP() {
        return ip;
    }

    /**
     * Gets ConnectionHandler instance.
     *
     * @return ConnectionHandler instance
     * @since 2.0
     */
    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    /**
     * Add NetworkEventListener to NetworkEventNotifier.
     *
     * @param networkEventListener NetworkEventListener instance
     * @since 2.0
     */
    public void addNetworkEventListener(INetworkEventListener networkEventListener) {
        networkEventNotifier.addNetworkEventListener(networkEventListener);
    }

    /**
     * Remove NetworkEventListener from NetworkEventNotifier.
     *
     * @param networkEventListener NetworkEventListener instance
     * @since 2.0
     */
    public void removeNetworkEventListener(INetworkEventListener networkEventListener) {
        networkEventNotifier.removeNetworkEventListener(networkEventListener);
    }
}
