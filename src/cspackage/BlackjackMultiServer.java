package cspackage;

import java.io.IOException;
import java.net.ServerSocket;

public class BlackjackMultiServer {		//taken from Lab 6, name is only thing changed
    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: java IMMultiServer <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        boolean listening = true;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server is running on port " + portNumber);

            while (listening) {
                new BlackjackMSThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }
}
