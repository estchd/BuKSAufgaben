package Aufgabe1;

import Aufgabe1.Events.OnCloseEventHandler;
import Aufgabe1.Events.OnConnectEventHandler;
import Aufgabe1.Events.OnMessageEventHandler;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A Socket Client used to send and receive Messages
 * The Socket Client supports Messages that are preceded by an int indicating the messages length in bytes
 */
public class SocketClient {
    private boolean connected = false;

    private String host;
    private int port;
    private int readInterval = 10;

    private Socket socket;

    private DataInputStream dataInput;
    private DataOutputStream dataOutput;

    private OnCloseEventHandler onCloseEventHandler;
    private OnConnectEventHandler onConnectEventHandler;
    private OnMessageEventHandler onMessageEventHandler;

    private ReentrantLock closeLock = new ReentrantLock();

    /**
     * Gets the current connection state of the {@link SocketClient}
     * @return a boolean indicating whether or not the {@link SocketClient} is currently connected
     */
    public boolean IsConnected()
    {
        return connected;
    }

    /**
     * Gets the Interval at which the {@link SocketClient} polls for new messages
     * @return the interval
     */
    public int GetReadInterval()
    {
        return readInterval;
    }

    /**
     * Sets the Interval at which the {@link SocketClient} polls for new messages
     * @param interval the new interval
     */
    public void SetReadInterval(int interval)
    {
        this.readInterval = interval;
    }

    /**
     * Sets the {@link SocketClient}'s Event Handler for the Close Event
     * The Close Event gets invoked, when the {@link SocketClient}'s connection gets closed
     * @param eventHandler the Event Handler
     */
    public void SetOnCloseEventHandler(OnCloseEventHandler eventHandler)
    {
        onCloseEventHandler = eventHandler;
    }

    /**
     * Sets the {@link SocketClient}'s Event Handler for the Connect Event
     * The Connect Event gets invoked, when the {@link SocketClient} gets connected
     * @param eventHandler
     */
    public void SetOnConnectEventHandler(OnConnectEventHandler eventHandler)
    {
        onConnectEventHandler = eventHandler;
    }

    /**
     * Sets the {@link SocketClient}'s Event Handler for the Message Event
     * The Message Event gets invoked, when the {@link SocketClient} receives a message
     * @param eventHandler
     */
    public void SetOnMessageEventHandler(OnMessageEventHandler eventHandler)
    {
        onMessageEventHandler = eventHandler;
    }

    /**
     * Creates a new {@link SocketClient} with the given host address and port number
     * @param host the host address
     * @param port the port number
     */
    public SocketClient(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    /**
     * Creates a new {@link SocketClient} with the given Socket
     * @param connectedSocket the Socket
     * @param connected whether or not the given Socket is already connected or not
     */
    public SocketClient(Socket connectedSocket, boolean connected)
    {
        this.socket = connectedSocket;
        //Start the internal Thread and setup Streams if the socket is already connected
        if(connected)
        {
            run();
        }
    }

    /**
     * Connects the {@link SocketClient}
     * Returns false, if the {@link SocketClient is already connected}
     * @return a boolean indicating whether or not the connection was successful
     */
    public boolean Connect()
    {
        if(this.socket != null || connected) return false;
        try
        {
            this.socket = new Socket(host, port);
        }
        catch(Exception ex)
        {
            return false;
        }
        return run();
    }

    /**
     * Closes the {@link SocketClient}'s connection
     */
    public void Close()
    {
        //Fire the OnClose event if and only if this is the first call to Close in this Connection
        //(Close might be called multiple times if Close gets called by a thread other than the internal one)
        closeLock.lock();
        if(connected){
            connected = false;
            if(onCloseEventHandler != null) onCloseEventHandler.OnClose(this);
        }
        closeLock.unlock();

        //Close Input and Output Streams in a way that every open Stream gets Closed
        try{
            dataInput.close();
        }
        catch(Exception ignored){}
        try
        {
            dataOutput.close();
        }
        catch(Exception ignored){}

        //Finally Close the Socket and discard it
        try {
            socket.close();
        }
        catch (Exception ignored){}
        socket = null;
    }

    /**
     * Sends a message over the {@link SocketClient}'s connection
     * @param message the message
     * @return a boolean indicating whether or not the message was successfully sent
     */
    public boolean Send(String message)
    {
        if(socket == null || !connected) return false;
        try
        {
            //Transmit the message length first and then the message itself
            byte[] messageData = message.getBytes();
            dataOutput.writeInt(messageData.length);
            dataOutput.write(messageData);
        }
        catch (Exception ex)
        {
            this.Close();
            if(onCloseEventHandler != null) onCloseEventHandler.OnClose(this);
            return false;
        }
        return true;
    }

    /**
     * Starts the SocketClients internal Thread and sets up the Input and Output Streams
     * @return a boolean indicating whether or not the internal Streams where set up and the internal Thread was started successfully
     */
    private boolean run()
    {
        //Setup the Input and Output Streams
        try
        {
            this.dataInput = new DataInputStream(socket.getInputStream());
            this.dataOutput = new DataOutputStream(socket.getOutputStream());
        }
        catch(Exception ex)
        {
            this.Close();
            return false;
        }
        //We are now completely Connected
        connected = true;

        //We need this to access this from the internal thread
        final SocketClient client = this;
        new Thread(() -> {
            while(connected)
            {
                try
                {
                    Thread.sleep(readInterval);

                    //Receive the message length first and then the message itself
                    int messageLength = dataInput.readInt();
                    byte[] messageData = new byte[messageLength];
                    dataInput.readFully(messageData,0,messageLength);

                    //Fire the OnMessage Event
                    if(onMessageEventHandler != null) onMessageEventHandler.OnMessage(client,new String(messageData));
                }
                catch(Exception ex)
                {
                    Close();
                }
            }
        }).start();
        //As we completed the Connection Setup, fire the OnConnect Event
        if(onConnectEventHandler != null) onConnectEventHandler.OnConnect(this);
        return true;
    }
}
