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

class ClientHandler implements Runnable {
  private final Socket clientSocket;

  public ClientHandler(Socket socket) {
    this.clientSocket = socket;
  }

  @Override
  public void run() {

    try (InputStream input = clientSocket.getInputStream();
         OutputStream output = clientSocket.getOutputStream();
    ) {
      while (true) {
        String[] tokens = parseRESP(input);
        if (tokens.length == 0) {
          break;
        }
        String response = handleCommand(tokens);
        output.write(response.getBytes());
        output.flush();
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

  private String[] parseRESP(InputStream input) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    String line = reader.readLine();
    if (line == null || line.isEmpty() || line.charAt(0) != '*') {
      return new String[0];
    }
    int arrayLength = Integer.parseInt(line.substring(1));
    String[] tokens = new String[arrayLength];
    for (int i = 0; i < arrayLength; i++) {
      String bulkLine = reader.readLine();
      if (bulkLine == null || bulkLine.charAt(0) != '$') {
        return new String[0];
      }
      int bulkLength = Integer.parseInt(bulkLine.substring(1));
      StringBuilder sb = new StringBuilder();
      for (int j = 0; j < bulkLength; j++) {
        sb.append((char) reader.read());
      }
      reader.readLine(); // Consume the trailing \r\n
      tokens[i] = sb.toString();
    }
    return tokens;
  }

  private String handleCommand(String[] tokens) {
    if (tokens.length == 0) return "";

    String command = tokens[0].toUpperCase();

    return switch (command) {
      case "PING" -> "+PONG\r\n";
      case "ECHO" -> {
        if (tokens.length < 2) {
          yield "-ERR wrong number of arguments for 'echo' command\r\n";
        }
        String message = tokens[1];
        yield "$" + message.length() + "\r\n" + message + "\r\n";
      }
      default -> "-ERR unknown command '" + tokens[0] + "'\r\n";
    };

  }


}