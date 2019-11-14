package Aufgabe1.Events;

import Aufgabe1.SocketClient;

/**
 * Event Handler for the OnConnect Event of a {@link SocketClient} or {@link Aufgabe1.SocketServer}
 */
public interface OnConnectEventHandler {
    void OnConnect(SocketClient client);
}
