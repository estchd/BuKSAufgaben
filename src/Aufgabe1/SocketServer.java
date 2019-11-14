package Aufgabe1;

import Aufgabe1.Events.OnCloseEventHandler;
import Aufgabe1.Events.OnConnectEventHandler;
import Aufgabe1.Events.OnMessageEventHandler;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * A Socket Server used to listen to and accept connections
 */
public class SocketServer {

    private boolean listening = false;

    private int port;
    private int readInterval = 10;

    private ServerSocket server;
    private List<SocketClient> clients;

    private OnConnectEventHandler onConnectEventHandler;
    private OnCloseEventHandler onCloseEventHandler;
    private OnMessageEventHandler onMessageEventHandler;

    /**
     * Checks, if the {@link SocketServer} is currently listening for incoming connections
     * @return a boolean indicating whether the {@link SocketServer} is currently listening for incoming connections
     */
    public boolean IsListening()
    {
        return listening;
    }

    /**
     * Gets the Interval at which the {@link SocketServer}'s connections are polled for new messages
     * @return the interval
     */
    public int GetReadInterval()
    {
        return readInterval;
    }

    /**
     * Sets the Interval at which the {@link SocketServer}'s connections are polled for new messages
     * @param interval the new interval
     */
    public void SetReadInterval(int interval)
    {
        this.readInterval = interval;
        //Update the Interval for all connections
        for (SocketClient client : clients) {
            client.SetReadInterval(interval);
        }
    }

    /**
     * Sets the Event Handler for the {@link SocketServer}'s Connect Event
     * The Connect Event gets invoked, when the {@link SocketServer} accepts a new connection
     * @param eventHandler the Event Handler
     */
    public void SetOnConnectEventHandler(OnConnectEventHandler eventHandler)
    {
        onConnectEventHandler = eventHandler;
    }

    /**
     * Sets the Event Handler for the {@link SocketServer}'s Close Event
     * The Close Event gets invoked, when one of the {@link SocketServer}'s connections gets closed
     * @param eventHandler the Event Handler
     */
    public void SetOnCloseEventHandler(OnCloseEventHandler eventHandler)
    {
        onCloseEventHandler = eventHandler;
    }

    /**
     * Sets the Event Handler for the {@link SocketServer}'s Message Event
     * The Message Event gets invoked, when one of the {@link SocketServer}'s connections recieves a message
     * @param eventHandler the Event Handler
     */
    public void SetOnMessageEventHandler(OnMessageEventHandler eventHandler)
    {
        onMessageEventHandler = eventHandler;
    }

    /**
     * Creates a new {@link SocketServer} with the given port number
     * @param port the port number
     */
    public SocketServer(int port)
    {
        this.port = port;
        clients = new ArrayList<>();
    }

    /**
     * Sets the {@link SocketServer} to start listening to and accept incoming connections
     * @return a boolean indicating whether or not the server started listening for connections successfully
     */
    public boolean Listen()
    {
        try
        {
            server = new ServerSocket(port);
        }
        catch(Exception ex)
        {
            return false;
        }

        //We are now listening for connections
        listening = true;
        new Thread(() -> {
            while(!listening)
            {
                try
                {
                    //If there is an incomming connection accept it and add it to the list
                    SocketClient client = new SocketClient(server.accept(), true);
                    clients.add(client);
                    //Fire the OnConnect Event
                    if(onConnectEventHandler != null) onConnectEventHandler.OnConnect(client);

                    //Bind the OnClose and OnMessage Events of the Client to fire our OnClose and OnMessage Events
                    if(onCloseEventHandler != null)
                    {
                        client.SetOnCloseEventHandler(closedClient -> onCloseEventHandler.OnClose(closedClient));
                    }
                    if(onMessageEventHandler != null)
                    {
                        client.SetOnMessageEventHandler((messagedClient, message) -> onMessageEventHandler.OnMessage(messagedClient,message));
                    }
                }
                catch(Exception ignored){}
            }
        }).start();
        return true;
    }

    /**
     * Closes all of the {@link SocketServer}'s open connections and Stops the {@link SocketServer} from listening for incoming connections
     */
    public void Close()
    {
        listening = false;
        //Close all open connections and clear the connection list
        for (SocketClient client : clients) {
            client.Close();
        }
        clients.clear();

        //Close the server itself after all connections are closed
        try{
            server.close();
        }
        catch(Exception ignored){}
        server = null;
    }
}
