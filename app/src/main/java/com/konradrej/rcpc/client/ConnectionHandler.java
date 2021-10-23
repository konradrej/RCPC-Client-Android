package com.konradrej.rcpc.client;

import android.util.Log;

import com.konradrej.rcpc.core.network.Message;
import com.konradrej.rcpc.core.network.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Handles communication to server.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 1.2
 * @since 1.0
 */
public class ConnectionHandler {
    private static final String TAG = "ConnectionHandler";
    private static ConnectionHandler singleInstance = null;
    private final Set<onNetworkEventListener> onNetworkEventListeners = new HashSet<>();
    private SocketHandler socketHandler = null;
    private KeyStore keyStore;
    private KeyStore trustStore;

    private ConnectionHandler() {

    }

    /**
     * Creates an instance of singleton ConnectionHandler
     * if it does not exist yet and returns it.
     *
     * @return singleton instance of ConnectionHandler
     * @since 1.0
     */
    public static ConnectionHandler getInstance() {
        if (singleInstance == null) {
            singleInstance = new ConnectionHandler();
        }

        return singleInstance;
    }

    /**
     * Adds message to messageQueue to be sent to server.
     *
     * @param message message to send
     * @since 1.0
     */
    public synchronized void sendMessage(Message message) {
        socketHandler.messageQueue.add(message);
    }

    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public void setTruststore(KeyStore trustStore) {
        this.trustStore = trustStore;
    }

    /**
     * Disconnects previous connection and connects to given ip address.
     *
     * @param ip                   ip to connect to
     * @param networkEventListener network event callback
     * @since 1.0
     */
    public void connectToServer(String ip, onNetworkEventListener networkEventListener) {
        if (socketHandler != null) {
            socketHandler.disconnect = true;
        }

        socketHandler = new SocketHandler();
        socketHandler.ip = ip;
        socketHandler.keyStore = keyStore;
        socketHandler.trustStore = trustStore;

        addCallback(networkEventListener);

        new Thread(socketHandler).start();
    }

    /**
     * Disconnects connection to server.
     *
     * @since 1.0
     */
    public void disconnect() {
        socketHandler.disconnect = true;
    }

    /**
     * Get IP.
     *
     * @return last set IP
     * @since 1.0
     */
    public String getIP() {
        return socketHandler.ip;
    }

    /**
     * Adds onNetworkEventListener callback.
     *
     * @param onNetworkEventListener instance of onNetworkEventListener to add
     * @since 1.0
     */
    public void addCallback(onNetworkEventListener onNetworkEventListener) {
        this.onNetworkEventListeners.add(onNetworkEventListener);
    }

    /**
     * Removes onNetworkEventListener callback.
     *
     * @param onNetworkEventListener instance of onNetworkEventListener to remove
     * @since 1.0
     */
    public void removeCallback(onNetworkEventListener onNetworkEventListener) {
        this.onNetworkEventListeners.remove(onNetworkEventListener);
    }

    private void notifyListener(NetworkEvent event) {
        for (onNetworkEventListener onNetworkEventListener : onNetworkEventListeners) {
            switch (event) {
                case CONNECT:
                    onNetworkEventListener.onConnect();
                    break;
                case REFUSED:
                    onNetworkEventListener.onRefused();
                    break;
                case DISCONNECT:
                    onNetworkEventListener.onDisconnect();
                    break;
                case TIMEOUT:
                    onNetworkEventListener.onConnectTimeout();
                    break;
            }
        }
    }

    private void notifyListener(NetworkEvent event, Exception errorException) {
        for (onNetworkEventListener onNetworkEventListener : onNetworkEventListeners) {
            switch (event) {
                case ERROR:
                    onNetworkEventListener.onError(errorException);
                    break;
            }
        }
    }

    private enum NetworkEvent {
        CONNECT,
        DISCONNECT,
        TIMEOUT,
        ERROR,
        REFUSED
    }

    /**
     * Callback interface for network events.
     *
     * @since 1.0
     */
    public interface onNetworkEventListener {
        void onConnect();

        void onRefused();

        void onDisconnect();

        void onConnectTimeout();

        void onError(Exception e);
    }

    private class SocketHandler implements Runnable {
        private final Deque<Message> messageQueue = new ArrayDeque<>();
        private boolean disconnect = false;
        private String ip = null;
        private KeyStore keyStore;
        private KeyStore trustStore;

        @Override
        public void run() {
            try {
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keyStore, "UX6eCJ2%zue".toCharArray());

                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(trustStore);

                SSLContext ssl_ctx = SSLContext.getInstance("TLSv1");
                ssl_ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

                SSLSocketFactory sslSocketFactory = ssl_ctx.getSocketFactory();

                try (SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket()) {
                    // Connect socket and optimize its settings
                    SocketAddress socketAddress = new InetSocketAddress(ip, 666);
                    socket.connect(socketAddress, 5000);
                    socket.setTcpNoDelay(true);
                    socket.setPerformancePreferences(0, 2, 1);
                    socket.setKeepAlive(true);

                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                    Message message = (Message) in.readObject();
                    if (message.getMessageType() == MessageType.INFO_USER_ACCEPTED_CONNECTION) {
                        notifyListener(NetworkEvent.CONNECT);

                        while (!disconnect) {
                            if (!messageQueue.isEmpty()) {
                                out.writeObject(messageQueue.removeFirst());
                            }
                        }

                        Message outMessage = new Message(MessageType.INFO_USER_CLOSED_CONNECTION);

                        out.writeObject(outMessage);
                        out.flush();
                    } else if (message.getMessageType() == MessageType.INFO_USER_CLOSED_CONNECTION) {
                        notifyListener(NetworkEvent.REFUSED);
                    } else {
                        notifyListener(NetworkEvent.ERROR, new Exception("Invalid message type, server or client is probably outdated."));
                    }
                } catch (SocketTimeoutException e) {
                    notifyListener(NetworkEvent.TIMEOUT);
                } catch (IOException | ClassNotFoundException e) {
                    notifyListener(NetworkEvent.ERROR, e);
                } finally {
                    disconnect = false;
                    notifyListener(NetworkEvent.DISCONNECT);
                }
            } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | UnrecoverableKeyException e) {
                Log.d(TAG, "Error: " + e.getLocalizedMessage());
            }
        }
    }
}
