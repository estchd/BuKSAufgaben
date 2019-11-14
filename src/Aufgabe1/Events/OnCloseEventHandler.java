package Aufgabe1.Events;

import Aufgabe1.SocketClient;

/**
 * Event Handler for the OnClose Event of a {@link Aufgabe1.SocketServer} or {@link SocketClient}
 */
public interface OnCloseEventHandler {
    void OnClose(SocketClient client);
}
