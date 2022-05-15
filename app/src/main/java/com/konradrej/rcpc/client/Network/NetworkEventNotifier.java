package com.konradrej.rcpc.client.Network;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles notifying NetworkEventListeners.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 2.0
 * @since 2.0
 */
public class NetworkEventNotifier {
    private final Set<INetworkEventListener> networkEventListeners = new HashSet<>();

    /**
     * Adds INetworkEventListener instance as a listener.
     *
     * @param networkEventListener INetworkEventListener instance to add
     * @since 2.0
     */
    public void addNetworkEventListener(INetworkEventListener networkEventListener) {
        networkEventListeners.add(networkEventListener);
    }

    /**
     * Removes INetworkEventListener instance from being a listener.
     *
     * @param networkEventListener INetworkEventListener instance to remove
     * @since 2.0
     */
    public void removeNetworkEventListener(INetworkEventListener networkEventListener) {
        networkEventListeners.remove(networkEventListener);
    }

    /**
     * Shorthand, see {@link #notifyNetworkEventListeners(NetworkEvent, Exception, JSONObject)} method.
     *
     * @param event network event
     * @since 2.0
     */
    public void notifyNetworkEventListeners(NetworkEvent event) {
        notifyNetworkEventListeners(event, null, null);
    }

    /**
     * Shorthand, see {@link #notifyNetworkEventListeners(NetworkEvent, Exception, JSONObject)} method.
     *
     * @param event network event
     * @param e exception to supply
     * @since 2.0
     */
    public void notifyNetworkEventListeners(NetworkEvent event, Exception e) {
        notifyNetworkEventListeners(event, e, null);
    }

    /**
     * Shorthand, see {@link #notifyNetworkEventListeners(NetworkEvent, Exception, JSONObject)} method.
     *
     * @param event network event
     * @param message message to supply
     * @since 2.0
     */
    public void notifyNetworkEventListeners(NetworkEvent event, JSONObject message) {
        notifyNetworkEventListeners(event, null, message);
    }

    /**
     * Method to notify all listeners.
     *
     * @param event network event
     * @param e exception to supply
     * @param message message to supply
     * @since 2.0
     */
    public void notifyNetworkEventListeners(NetworkEvent event, Exception e, JSONObject message) {
        for (INetworkEventListener networkEventListener : networkEventListeners) {
            switch (event) {
                case CONNECT:
                    networkEventListener.onConnect();
                    break;
                case DISCONNECT:
                    networkEventListener.onDisconnect();
                    break;
                case TIMEOUT:
                    networkEventListener.onTimeout();
                    break;
                case ERROR:
                    networkEventListener.onError(e);
                    break;
                case REFUSED:
                    networkEventListener.onRefused();
                    break;
                case SEND_MESSAGE:
                    networkEventListener.onSendMessage(message);
                    break;
                case RECEIVE_MESSAGE:
                    networkEventListener.onReceiveMessage(message);
                    break;
            }
        }
    }
}
