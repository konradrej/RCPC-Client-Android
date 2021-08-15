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

public class SocketHandler implements Runnable {

    private static SocketHandler singleInstance = null;
    private final Set<onNetworkEventListener> onNetworkEventListeners = new HashSet<>();

    private final Deque<String> messageQueue = new ArrayDeque<>();
    private boolean disconnect = false;
    private String ip = null;

    private SocketHandler() {
    }

    public void disconnect() {
        disconnect = true;
    }

    public synchronized void sendMessage(String message) {
        messageQueue.add(message);
    }

    public static SocketHandler getInstance() {
        if (singleInstance == null) {
            singleInstance = new SocketHandler();
        }

        return singleInstance;
    }

    public void setIP(String ip) {
        this.ip = ip;
    }

    public void addCallback(onNetworkEventListener onNetworkEventListener) {
        this.onNetworkEventListeners.add(onNetworkEventListener);
    }

    public String getIP() {
        return ip;
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        SocketAddress socketAddress = new InetSocketAddress(ip, 666);

        try {
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

            out.flush();
            socket.close();
            disconnect = false;

            notifyListener(NetworkEvent.DISCONNECT, null);
        } catch (SocketTimeoutException e) {
            notifyListener(NetworkEvent.TIMEOUT, null);
        } catch (IOException e) {
            notifyListener(NetworkEvent.ERROR, e);
        }
    }

    public void notifyListener(NetworkEvent event, IOException errorException) {
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

    public interface onNetworkEventListener {
        void onConnect();

        void onDisconnect();

        void onConnectTimeout();

        void onError(IOException e);
    }
}
