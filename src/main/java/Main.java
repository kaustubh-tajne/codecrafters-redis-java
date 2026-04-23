import java.io.*;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

//      Uncomment the code below to pass the first stage
        int port = 6379;
        try (ServerSocket serverSocket = new ServerSocket(port);) {

            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);

            while (true) {
                // Wait for connection from client.
              Socket clientSocket = serverSocket.accept();

              new Thread(
                      new ClientHandler(clientSocket)
              ).start();
            }
        } catch (IOException e) {
          System.out.println("IOException3: " + e.getMessage());
        }
  }

}

