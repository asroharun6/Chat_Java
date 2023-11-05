import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final int PORT = 1234; // Port to listen on
    private static final String IP_ADDRESS = "192.168.70.191"; // Server IP address
    private static ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static List<Message> chatHistory = Collections.synchronizedList(new ArrayList<>());
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws IOException {
        InetAddress bindAddr = InetAddress.getByName(IP_ADDRESS);
        try (ServerSocket serverSocket = new ServerSocket(PORT, 50, bindAddr)) {
            System.out.println("Server listening on IP " + IP_ADDRESS + " and port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                new Thread(handler).start();
            }
        }
    }

    private static void broadcastMessage(String message, String senderName) {
        String timestamp = dateFormatter.format(new Date());
        String formattedMessage = String.format("%s %s: %s", timestamp, senderName, message);
        chatHistory.add(new Message(senderName, message, timestamp)); // Save to chat history

        for (ClientHandler client : clients.values()) {
            if (!client.getUserName().equals(senderName)) {
                client.sendMessage(formattedMessage);
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String userName;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public String getUserName() {
            return userName;
        }

        @Override
        public void run() {
            try {
                out.println("Enter your name: ");
                userName = in.readLine();
                clients.put(userName, this);
                broadcastMessage("has joined the chat", userName);

                String input;
                while ((input = in.readLine()) != null) {
                    if ("quit".equalsIgnoreCase(input)) {
                        break;
                    }
                    broadcastMessage(input, userName);
                }
            } catch (IOException e) {
                System.out.println(userName + " Error: " + e.getMessage());
            } finally {
                if (userName != null) {
                    clients.remove(userName);
                    broadcastMessage("has left the chat", userName);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class Message {
        String user;
        String message;
        String timestamp;

        public Message(String user, String message, String timestamp) {
            this.user = user;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
}
