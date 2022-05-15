package com.konradrej.rcpc.client.Network;

import android.util.Log;

import com.konradrej.rcpc.client.App;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.net.ssl.SSLSocket;

/**
 * RCPC communication class, takes a SSLSocket and NetworkEventListener to setup
 * communication with a RCPC server.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 2.0
 * @since 2.0
 */
public class ConnectionHandler {

    private static final String TAG = ConnectionHandler.class.getName();

    private final SSLSocket sslSocket;
    private final NetworkEventNotifier networkEventNotifier;
    private final Deque<JSONObject> messageDeque = new ArrayDeque<>();

    private boolean disconnect = false;
    private BufferedWriter out;
    private BufferedReader in;

    /**
     * Construct a new ConnectionHandler around SSLSocket and NetworkEventNotifier instances.
     *
     * @param sslSocket            SSLSocket to use
     * @param networkEventNotifier NetworkEventNotifier to use
     * @since 2.0
     */
    public ConnectionHandler(SSLSocket sslSocket, NetworkEventNotifier networkEventNotifier) {
        this.sslSocket = sslSocket;
        this.networkEventNotifier = networkEventNotifier;
    }

    /**
     * Run the communication handling.
     *
     * @since 2.0
     */
    public void run() {
        try (SSLSocket sslSocket = this.sslSocket) {
            setupIO(sslSocket);
            sendUuidMessage();

            runReader();
            runWriter();

            if (!sslSocket.isOutputShutdown()) {
                JSONObject message = new JSONObject();
                message.put("type", "INFO_USER_CLOSED_CONNECTION");

                writeMessage(message);
            }
        } catch (Exception e) {
            Log.e(TAG, "The server connection encountered an error.", e);
            networkEventNotifier.notifyNetworkEventListeners(NetworkEvent.ERROR, e);
        }

        networkEventNotifier.notifyNetworkEventListeners(NetworkEvent.DISCONNECT);
    }

    /**
     * Adds message to message queue.
     *
     * @param message message to send
     * @since 2.0
     */
    public synchronized void sendMessage(JSONObject message) {
        messageDeque.add(message);
    }

    /**
     * Tells ConnectionHandler to disconnect gracefully.
     *
     * @since 2.0
     */
    public synchronized void disconnect() {
        disconnect = true;
    }

    private void setupIO(SSLSocket sslSocket) throws IOException {
        out = new BufferedWriter((
                new OutputStreamWriter(sslSocket.getOutputStream(), StandardCharsets.UTF_8))
        );

        in = new BufferedReader(
                new InputStreamReader(sslSocket.getInputStream(), StandardCharsets.UTF_8)
        );
    }

    private void sendUuidMessage() throws Exception {
        try {
            JSONObject message = new JSONObject();

            message.put("type", "INFO_UUID");
            message.put("uuid", App.getDeviceUUID());

            writeMessage(message);
        } catch (JSONException | IOException e) {
            Log.e(TAG, "An error occurred while sending UUID message.", e);
            throw new Exception("Could not send UUID message. Error: " + e.getLocalizedMessage());
        }
    }

    private void writeMessage(JSONObject message) throws IOException {
        out.write(message.toString());
        out.newLine();
        out.flush();
    }

    private void runReader() {
        new Thread(() -> {
            try {
                while (!sslSocket.isInputShutdown() && !disconnect) {
                    JSONObject message = new JSONObject(in.readLine());
                    networkEventNotifier.notifyNetworkEventListeners(NetworkEvent.RECEIVE_MESSAGE, message);

                    switch (message.getString("type")) {
                        case "ACTION_GET_UUID":
                            sendUuidMessage();
                            break;
                        case "INFO_USER_ACCEPTED_CONNECTION":
                            networkEventNotifier.notifyNetworkEventListeners(NetworkEvent.CONNECT);
                            break;
                        case "INFO_USER_CLOSED_CONNECTION":
                            networkEventNotifier.notifyNetworkEventListeners(NetworkEvent.DISCONNECT);
                            break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "An error occurred while reading a message.", e);
            }

            disconnect();
        }).start();
    }

    private void runWriter() {
        try {
            while (!sslSocket.isOutputShutdown() && !disconnect) {
                if (!messageDeque.isEmpty()) {
                    writeMessage(messageDeque.removeFirst());
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "An IO error occurred while writing a message.", e);
        }

        disconnect();
    }
}
