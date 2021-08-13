package com.konradrej.remotemouseclient;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;

public class ConnectionHandler implements Runnable {

    private final String DEBUG_TAG = "ConnectionHandler";

    private final Deque<String> messageQueue = new ArrayDeque<>();
    private boolean disconnect = false;


    public void disconnect() {
        disconnect = true;
    }

    public void sendMessage(String message) {
        messageQueue.add(message);
    }

    @Override
    public void run() {
        try (Socket socket = new Socket("192.168.1.65", 666);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            while (!disconnect) {
                if (!messageQueue.isEmpty()) {
                    out.writeUTF(messageQueue.removeFirst());
                }
            }

            out.flush();
        } catch (IOException e) {
            Log.e(DEBUG_TAG, "IO error occurred.\n\n" + e.getMessage());
        }
    }
}
