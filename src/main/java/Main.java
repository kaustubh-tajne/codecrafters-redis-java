import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

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

              new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
          System.out.println("IOException3: " + e.getMessage());
        }
  }

}

class ClientHandler implements Runnable {
  private final Socket clientSocket;

  public ClientHandler(Socket socket) {
    this.clientSocket = socket;
  }

  @Override
  public void run() {
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      String line;
      while ((line = in.readLine()) != null){
        System.out.println(line);
        if (line.trim().equalsIgnoreCase("PING")){
          clientSocket.getOutputStream().write("+PONG\r\n".getBytes());
        }
      }
    } catch (IOException e) {
      System.out.println("IOException1: " + e.getMessage());
    } finally {
      try {
        if (clientSocket != null) {
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println("IOException2: " + e.getMessage());
      }
    }
  }



}