package com.konradrej.rcpc.client.Network;

import org.json.JSONObject;

/**
 * Interface for defining a NetworkEventListener.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 2.0
 * @since 2.0
 */
public interface INetworkEventListener {
    void onConnect();

    void onDisconnect();

    void onTimeout();

    void onError(Exception e);

    void onRefused();

    void onSendMessage(JSONObject message);

    void onReceiveMessage(JSONObject message);
}