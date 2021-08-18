package com.konradrej.rcpc;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles communication to server.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 1.0
 */
public class ConnectionHandler {
    private static ConnectionHandler singleInstance = null;
    private final Set<onNetworkEventListener> onNetworkEventListeners = new HashSet<>();
    private SocketHandler socketHandler = null;

    private ConnectionHandler() {
        
    }

    /**
     * Creates an instance of singleton ConnectionHandler
     * if it does not exist yet and returns it.
     *
     * @return singleton instance of ConnectionHandler
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
     */
    public synchronized void sendMessage(String message) {
        socketHandler.messageQueue.add(message);
    }

    /**
     * Disconnects previous connection and connects to given ip address.
     *
     * @param ip                   ip to connect to
     * @param networkEventListener network event callback
     */
    public void connectToServer(String ip, onNetworkEventListener networkEventListener) {
        if (socketHandler != null) {
            socketHandler.disconnect = true;
        }

        socketHandler = new SocketHandler();
        socketHandler.ip = ip;

        addCallback(networkEventListener);

        new Thread(socketHandler).start();
    }

    /**
     * Disconnects connection to server.
     */
    public void disconnect() {
        socketHandler.disconnect = true;
    }

    /**
     * Get IP.
     *
     * @return last set IP
     */
    public String getIP() {
        return socketHandler.ip;
    }

    /**
     * Adds onNetworkEventListener callback.
     *
     * @param onNetworkEventListener instance of onNetworkEventListener to add
     */
    public void addCallback(onNetworkEventListener onNetworkEventListener) {
        this.onNetworkEventListeners.add(onNetworkEventListener);
    }

    /**
     * Removes onNetworkEventListener callback.
     *
     * @param onNetworkEventListener instance of onNetworkEventListener to remove
     */
    public void removeCallback(onNetworkEventListener onNetworkEventListener) {
        this.onNetworkEventListeners.remove(onNetworkEventListener);
    }

    private void notifyListener(NetworkEvent event, IOException errorException) {
        for (onNetworkEventListener onNetworkEventListener : onNetworkEventListeners) {
            switch (event) {
                case CONNECT:
                    onNetworkEventListener.onConnect();
                    break;
                case DISCONNECT:
                    onNetworkEventListener.onDisconnect();
                    break;
                case TIMEOUT:
                    onNetworkEventListener.onConnectTimeout();
                    break;
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
        ERROR
    }

    /**
     * Callback interface for network events.
     */
    public interface onNetworkEventListener {
        void onConnect();

        void onDisconnect();

        void onConnectTimeout();

        void onError(IOException e);
    }

    private class SocketHandler implements Runnable {
        private final Deque<String> messageQueue = new ArrayDeque<>();
        private boolean disconnect = false;
        private String ip = null;

        @Override
        public void run() {
            try (Socket socket = new Socket()) {
                SocketAddress socketAddress = new InetSocketAddress(ip, 666);
                socket.connect(socketAddress, 5000);
                socket.setTcpNoDelay(true);
                socket.setPerformancePreferences(0, 2, 1);
                socket.setKeepAlive(true);

                notifyListener(NetworkEvent.CONNECT, null);

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                while (!disconnect) {
                    if (!messageQueue.isEmpty()) {
                        out.writeUTF(messageQueue.removeFirst());
                    }
                }
            } catch (SocketTimeoutException e) {
                notifyListener(NetworkEvent.TIMEOUT, null);
            } catch (IOException e) {
                notifyListener(NetworkEvent.ERROR, e);
            } finally {
                disconnect = false;
                notifyListener(NetworkEvent.DISCONNECT, null);
            }
        }
    }
}
