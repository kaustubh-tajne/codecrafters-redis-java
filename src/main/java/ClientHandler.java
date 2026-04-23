import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
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
