package Aufgabe1;

import Aufgabe1.Events.OnCloseEventHandler;
import Aufgabe1.Events.OnConnectEventHandler;
import Aufgabe1.Events.OnMessageEventHandler;

import java.io.IOException;
import java.util.List;


class Server
{
    private FolderManager folderManager;

    void Start(int port)
    {
        System.out.println("Starting Server!");

        try{
            String folderPath = "data";
            folderManager = new FolderManager(folderPath,true);
        }
        catch(IOException ex)
        {
            System.out.println("Cannot open Server Directory!");
            return;
        }

        System.out.println("Folder Manager Started!");

        SocketServer server = new SocketServer(port);

        server.SetOnCloseEventHandler(new ServerOnCloseEventHandler());
        server.SetOnConnectEventHandler(new ServerOnConnectEventHandler());
        server.SetOnMessageEventHandler(new ServerOnMessageEventHandler());

        if(!server.Listen())
        {
            System.err.println("Could not start Server!");
            return;
        }

        System.out.println("Server started, listening for connection!");
    }

    private static class ServerOnCloseEventHandler implements OnCloseEventHandler
    {
        @Override
        public void OnClose(SocketClient client) {
            System.out.println("Connection closed!");
        }
    }

    private static class ServerOnConnectEventHandler implements OnConnectEventHandler
    {
        @Override
        public void OnConnect(SocketClient client) {
            System.out.println("Connection opened!");
        }
    }

    private class ServerOnMessageEventHandler implements OnMessageEventHandler
    {
        @Override
        public void OnMessage(SocketClient client, String message) {
            if(handleRequest(client,message))
            {
                client.Close();
            }
        }
    }

    private boolean handleRequest(SocketClient client, String request)
    {
        System.out.println("Incoming Request:");
        System.out.println(request);

        String[] arguments = request.split(" ");

        if (arguments.length == 0) {
            handleInvalidRequest(client);
            return false;
        }
        switch (arguments[0]) {
            case "LIST":
                handleListRequest(client);
                return false;

            case "GET":
                handleGetRequest(client,arguments);
                return false;

            case "QUIT":
                handleQuitRequest();
                return true;

            default:
                handleInvalidRequest(client);
                return false;
        }
    }

    private void handleInvalidRequest(SocketClient client)
    {
        System.err.println("Invalid Request received!");
        client.Send("Invalid Request!");
    }

    private void handleServerError(SocketClient client)
    {
        client.Send("Internal Server Error!");
    }

    private void handleListRequest(SocketClient client)
    {
        System.out.println("LIST Request received!");
        List<String> files;
        try {
            files = folderManager.listFilesInFolder();
        }
        catch(IOException ex)
        {
            System.err.println("Error Listing Files!");
            handleServerError(client);
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("Files:\r\n");
        for (String filename : files) {
            message.append(filename).append("\r\n");
        }
        client.Send(message.toString());
    }

    private void handleGetRequest(SocketClient client, String[] arguments)
    {
        System.out.println("GET Request received!");
        if (arguments.length == 1) {
            handleInvalidRequest(client);
            return;
        }

        List<String> fileContent;
        try {
             fileContent = folderManager.getFileContent(arguments[1]);
        }
        catch (IOException ex)
        {
            System.out.println("Error reading File!");
            handleServerError(client);
            return;
        }
            StringBuilder message = new StringBuilder();
            message.append("File:\r\n");
            for (String line : fileContent) {
                message.append(line).append("\r\n");
            }
            client.Send(message.toString());
    }

    private void handleQuitRequest()
    {
        System.out.println("QUIT Request received!");
    }
}
