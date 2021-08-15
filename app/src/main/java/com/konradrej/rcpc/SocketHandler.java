package com.konradrej.rcpc;

import android.util.Log;

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

    public void removeCallback(onNetworkEventListener onNetworkEventListener) {
        this.onNetworkEventListeners.remove(onNetworkEventListener);
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

            for (onNetworkEventListener onNetworkEventListener : onNetworkEventListeners)
                onNetworkEventListener.onConnect();

            socket.setTcpNoDelay(true);
            socket.setPerformancePreferences(0, 2, 1);
            socket.setKeepAlive(true);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            while (!disconnect) {
                if (!messageQueue.isEmpty()) {
                    out.writeUTF(messageQueue.removeFirst());
                }
            }

            out.flush();
            socket.close();
            disconnect = false;

            for (onNetworkEventListener onNetworkEventListener : onNetworkEventListeners)
                onNetworkEventListener.onDisconnect();
        } catch (SocketTimeoutException e) {
            for (onNetworkEventListener onNetworkEventListener : onNetworkEventListeners)
                onNetworkEventListener.onConnectTimeout();
        } catch (IOException e) {
            for (onNetworkEventListener onNetworkEventListener : onNetworkEventListeners)
                onNetworkEventListener.onError(e);
        }
    }

    public interface onNetworkEventListener {
        void onConnect();

        void onDisconnect();

        void onConnectTimeout();

        void onError(IOException e);
    }
}
