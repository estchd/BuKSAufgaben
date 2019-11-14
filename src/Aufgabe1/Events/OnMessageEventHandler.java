package Aufgabe1.Events;

import Aufgabe1.SocketClient;

/**
 * Event Handler for the OnMessage Event of a {@link Aufgabe1.SocketServer} or {@link SocketClient}
 */
public interface OnMessageEventHandler {
    void OnMessage(SocketClient client,String message);
}
