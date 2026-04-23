import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private static final ConcurrentHashMap<String, CacheEntry> storage = new ConcurrentHashMap<>();
    private static final Logger log = Logger.getLogger(ClientHandler.class.getName());

    static class CacheEntry {
        String value;
        List<String> list;
        long expiryTime;

        CacheEntry(String value, long expiryTime) {
            this.value = value;
            this.list = null;
            this.expiryTime = expiryTime;
        }

        CacheEntry(List<String> list, long expiryTime) {
            this.value = null;
            this.list = list;
            this.expiryTime = expiryTime;
        }

        boolean isExpired() {
            return expiryTime != -1 && System.currentTimeMillis() > expiryTime;
        }

        boolean isList() {
            return list != null;
        }
    }

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
                    log.log(Level.SEVERE, "Wrong number of arguments for 'echo' command");
                    yield "-ERR wrong number of arguments for 'echo' command\r\n";
                }
                String message = tokens[1];
                yield "$" + message.length() + "\r\n" + message + "\r\n";
            }
            case "SET" -> {
                if (tokens.length < 3) {
                    log.log(Level.SEVERE, "Wrong number of arguments for 'SET' command");
                    yield "-ERR wrong number of arguments for 'set' command\r\n";
                } else if (tokens.length >= 5) {
                    String key = tokens[1];
                    String value = tokens[2];
                    String timeFormat = tokens[3];
                    long time = Long.parseLong(tokens[4]);
                    if (timeFormat.equalsIgnoreCase("PX")) {
                        long expiryTime = System.currentTimeMillis() + time;
                        storage.put(key, new CacheEntry(value, expiryTime));
                    } else if (timeFormat.equalsIgnoreCase("EX")) {
                        long expiryTime = System.currentTimeMillis() + (time * 1000);
                        storage.put(key, new CacheEntry(value, expiryTime));
                    } else {
                        log.log(Level.SEVERE, "Unknown time format for 'set' command");
                        yield "-ERR unknown time format for 'set' command\r\n";
                    }
                } else {
                    String key = tokens[1];
                    String value = tokens[2];
                    storage.put(key, new CacheEntry(value, -1));
                }
                yield "+OK\r\n";
            }
            case "GET" -> {
                if (tokens.length < 2) {
                    log.log(Level.SEVERE, "Wrong number of arguments for 'GET' command");
                    yield "-ERR wrong number of arguments for 'get' command\r\n";
                }
                String key = tokens[1];
                CacheEntry cacheEntryObj = storage.get(key);
                if (cacheEntryObj == null || cacheEntryObj.isExpired()) {
                    storage.remove(key);
                    yield "$-1\r\n";
                }
                else {
                    String value = cacheEntryObj.value;
                    yield "$" + value.length() + "\r\n" + value + "\r\n";
                }
            }
            case "RPUSH" -> {
                if (tokens.length < 3) {
                    log.log(Level.SEVERE, "Wrong number of arguments for 'RPUSH' command");
                    yield "-ERR wrong number of arguments for 'rpush' command\r\n";
                }
                String key = tokens[1];
                String value = tokens[2];
                List<String> list;
                CacheEntry cacheEntryObj = storage.get(key);
                if (cacheEntryObj == null || cacheEntryObj.isExpired()) {
                    list = new ArrayList<>();
                    list.add(value);
                    storage.put(key, new CacheEntry(list, -1));
                    yield ":1\r\n";
                } else if (cacheEntryObj.isList()) {
                    list = cacheEntryObj.list;
                    list.add(value);
                    yield ":" + list.size() + "\r\n";
                }
                else {
                    log.log(Level.SEVERE, "Wrong type of value for 'RPUSH' command");
                    yield "-ERR wrong type of value for 'rpush' command\r\n";
                }
            }
            default -> "-ERR unknown command '" + tokens[0] + "'\r\n";
        };

    }


}
