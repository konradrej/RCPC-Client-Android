package com.konradrej.rcpc.client.Network;

/**
 * Possible network events.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 2.0
 * @since 2.0
 */
public enum NetworkEvent {
    CONNECT,
    DISCONNECT,
    TIMEOUT,
    ERROR,
    REFUSED,
    SEND_MESSAGE,
    RECEIVE_MESSAGE
}
