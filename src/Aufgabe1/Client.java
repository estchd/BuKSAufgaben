package Aufgabe1;

import Aufgabe1.Events.OnCloseEventHandler;
import Aufgabe1.Events.OnConnectEventHandler;
import Aufgabe1.Events.OnMessageEventHandler;

import java.util.Scanner;

class Client
{
    void Start(String ip, int port)
    {
        System.out.println("Starting Client!");
        SocketClient client = new SocketClient(ip,port);

        ClientOnCloseEventHandler onCloseEventHandler = new ClientOnCloseEventHandler();
        ClientOnConnectEventHandler onConnectEventHandler = new ClientOnConnectEventHandler();
        ClientOnMessageEventHandler onMessageEventHandler = new ClientOnMessageEventHandler();

        client.SetOnCloseEventHandler(onCloseEventHandler);
        client.SetOnConnectEventHandler(onConnectEventHandler);
        client.SetOnMessageEventHandler(onMessageEventHandler);

        if(!client.Connect())
        {
            System.err.println("Could not connect to the Server!");
        }
    }

    private static class ClientOnCloseEventHandler implements OnCloseEventHandler
    {
        @Override
        public void OnClose(SocketClient client) {
            System.out.println("Client Connection Closed!");
        }
    }

    private class ClientOnConnectEventHandler implements OnConnectEventHandler
    {
        @Override
        public void OnConnect(SocketClient client) {
            System.out.println("Client Connected!");
            String request = requestCommand();
            if(!client.Send(request))
            {
                System.err.println("Could not send Command!");
                client.Close();
            }
        }
    }

    private class ClientOnMessageEventHandler implements OnMessageEventHandler
    {
        @Override
        public void OnMessage(SocketClient client, String message) {
            System.out.println("Message Recieved:");
            System.out.println(message);
            String response = requestCommand();
            if(!client.Send(response))
            {
                System.err.println("Could not send Command!");
                client.Close();
            }
        }
    }

    private String requestCommand()
    {
        System.out.println("Please input Command:");
        Scanner input = new Scanner(System.in);
        return input.nextLine();
    }
}


