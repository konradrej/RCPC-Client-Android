package com.konradrej.rcpc.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.konradrej.rcpc.R;
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
import java.security.cert.CertificateException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

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
 * @version 1.7
 * @since 1.0
 */
public class ConnectionHandler {
    private static final String TAG = "ConnectionHandler";
    private static ConnectionHandler singleInstance = null;
    private final Set<onNetworkEventListener> onNetworkEventListeners = new HashSet<>();
    private final Set<onNetworkMessageListener> onNetworkMessageListeners = new HashSet<>();
    private SocketHandler socketHandler = null;
    private Context context;
    private SharedPreferences sharedPreferences = null;

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

    /**
     * Sets context to given value.
     *
     * @param context the given value
     * @since 1.0
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Sets shared preferences to given value.
     *
     * @param sharedPreferences the given value
     * @since 1.0
     */
    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
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
        socketHandler.context = context;

        addNetworkEventCallback(networkEventListener);

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
    public void addNetworkEventCallback(onNetworkEventListener onNetworkEventListener) {
        this.onNetworkEventListeners.add(onNetworkEventListener);
    }

    /**
     * Adds onNetworkMessageListener callback.
     *
     * @param onNetworkMessageListener instance of onNetworkMessageListener to add
     * @since 1.0
     */
    public void addNetworkMessageCallback(onNetworkMessageListener onNetworkMessageListener) {
        this.onNetworkMessageListeners.add(onNetworkMessageListener);
    }

    /**
     * Removes onNetworkEventListener callback.
     *
     * @param onNetworkEventListener instance of onNetworkEventListener to remove
     * @since 1.0
     */
    public void removeNetworkEventCallback(onNetworkEventListener onNetworkEventListener) {
        this.onNetworkEventListeners.remove(onNetworkEventListener);
    }

    /**
     * Removes onNetworkMessageListener callback.
     *
     * @param onNetworkMessageListener instance of onNetworkMessageListener to remove
     * @since 1.0
     */
    public void removeNetworkMessageCallback(onNetworkMessageListener onNetworkMessageListener) {
        this.onNetworkMessageListeners.remove(onNetworkMessageListener);
    }

    private void notifyEventListeners(NetworkEvent event) {
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

    private void notifyEventListeners(NetworkEvent event, Exception errorException) {
        for (onNetworkEventListener onNetworkEventListener : onNetworkEventListeners) {
            switch (event) {
                case ERROR:
                    onNetworkEventListener.onError(errorException);
                    break;
            }
        }
    }

    private void notifyMessageListeners(Message message) {
        for (onNetworkMessageListener onNetworkMessageListener : onNetworkMessageListeners) {
            onNetworkMessageListener.onReceivedMessage(message);
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

    /**
     * Callback interface for network messages.
     *
     * @since 1.0
     */
    public interface onNetworkMessageListener {
        void onReceivedMessage(Message message);
    }

    private class SocketHandler implements Runnable {
        private final Deque<Message> messageQueue = new ArrayDeque<>();
        private boolean disconnect = false;
        private String ip = null;
        private Context context;

        @Override
        public void run() {
            try {
                SSLContext sslContext = getSSLContext();
                SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                try (SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket()) {
                    // Connect socket and optimize its settings
                    SocketAddress socketAddress = new InetSocketAddress(ip, 666);
                    socket.connect(socketAddress, 5000);
                    socket.setTcpNoDelay(true);
                    socket.setPerformancePreferences(0, 2, 1);
                    socket.setKeepAlive(true);

                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                    out.writeObject(getGuidMessage());

                    Message message = (Message) in.readObject();
                    if (message.getMessageType() == MessageType.INFO_USER_ACCEPTED_CONNECTION) {
                        notifyEventListeners(NetworkEvent.CONNECT);

                        new Thread(() -> {
                            try {
                                while (!socket.isInputShutdown()) {
                                    Message receivedMessage = (Message) in.readObject();
                                    notifyMessageListeners(receivedMessage);

                                    switch (receivedMessage.getMessageType()) {
                                        case ACTION_GET_UUID:
                                            messageQueue.add(getGuidMessage());
                                            break;
                                        case INFO_USER_CLOSED_CONNECTION:
                                            notifyEventListeners(NetworkEvent.DISCONNECT);
                                            break;
                                        default:
                                            Log.e(TAG, "Message type not implemented: " + receivedMessage.getMessageType());
                                    }
                                }
                            } catch (IOException | ClassNotFoundException e) {
                                if (!disconnect) {
                                    notifyEventListeners(NetworkEvent.ERROR, e);
                                }
                            }
                        }).start();

                        while (!disconnect) {
                            if (!messageQueue.isEmpty()) {
                                out.writeObject(messageQueue.removeFirst());
                            }
                        }

                        Message outMessage = new Message(MessageType.INFO_USER_CLOSED_CONNECTION);

                        out.writeObject(outMessage);
                        out.flush();
                    } else if (message.getMessageType() == MessageType.INFO_USER_CLOSED_CONNECTION) {
                        notifyEventListeners(NetworkEvent.REFUSED);
                    } else {
                        notifyEventListeners(NetworkEvent.ERROR, new Exception("Invalid message type, server or client is probably outdated."));
                    }
                } catch (SocketTimeoutException e) {
                    notifyEventListeners(NetworkEvent.TIMEOUT);
                } catch (IOException | ClassNotFoundException e) {
                    notifyEventListeners(NetworkEvent.ERROR, e);
                } finally {
                    disconnect = false;
                    notifyEventListeners(NetworkEvent.DISCONNECT);
                }
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "Error: " + e.getLocalizedMessage());
            }
        }

        private SSLContext getSSLContext() throws NoSuchAlgorithmException {
            Properties properties = new Properties();
            try {
                properties.load(context.getResources().openRawResource(R.raw.ssl));

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                try {
                    String keystorePassword = properties.getProperty("keystore.password");

                    KeyStore keyStore = KeyStore.getInstance("PKCS12");
                    keyStore.load(context.getResources().openRawResource(R.raw.keystore), keystorePassword.toCharArray());

                    keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
                } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
                    Log.e(TAG, "Could not load keystore. Error: " + e.getLocalizedMessage());
                } catch (UnrecoverableKeyException e) {
                    e.printStackTrace();
                }

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                try {
                    String truststorePassword = properties.getProperty("truststore.password");

                    KeyStore trustStore = KeyStore.getInstance("PKCS12");
                    trustStore.load(context.getResources().openRawResource(R.raw.truststore), truststorePassword.toCharArray());

                    trustManagerFactory.init(trustStore);
                } catch (KeyStoreException | CertificateException e) {
                    Log.e(TAG, "Could not load truststore. Error: " + e.getLocalizedMessage());
                }

                SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
                sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

                return sslContext;
            } catch (IOException e) {
                Log.e(TAG, "Could not load SSL properties file. Error: " + e.getLocalizedMessage());
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                Log.e(TAG, "Keystore/truststore related error. Error: " + e.getLocalizedMessage());
            }

            return SSLContext.getDefault();
        }

        private Message getGuidMessage() {
            String guid;
            String guidKey = "app_instance_guid";

            if (sharedPreferences.contains(guidKey)) {
                guid = sharedPreferences.getString(guidKey, "");
            } else {
                guid = UUID.randomUUID().toString();
                sharedPreferences.edit().putString(guidKey, guid).apply();
            }

            return new Message(MessageType.INFO_UUID, guid);
        }
    }
}
