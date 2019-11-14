package Aufgabe1;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException
    {
        switch(args[0])
        {
            case "-server":
                Server server = new Server();
                server.Start(6666);
                break;

            case "-client":
                Client client = new Client();
                client.Start("localhost",6666);
        }
    }
}
