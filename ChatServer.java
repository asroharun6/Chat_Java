import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final int PORT = 1234; // Port to listen on
    private static final String IP_ADDRESS = "192.168.70.191"; // Server IP address
    private static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        InetAddress bindAddr = InetAddress.getByName(IP_ADDRESS);
        try (ServerSocket serverSocket = new ServerSocket(PORT, 50, bindAddr)) {
            System.out.println("Server listening on IP " + IP_ADDRESS + " and port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        }
    }

    // Broadcast a message to all clients except the sender
    public static void broadcastMessage(String message, String senderName) {
        for (ClientHandler client : clients.values()) {
            if (!client.getUserName().equals(senderName)) {
                client.sendMessage(message);
            }
        }
    }

    // ClientHandler class
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
                broadcastMessage("User " + userName + " has joined the chat.", userName);

                String input;
                while ((input = in.readLine()) != null) {
                    broadcastMessage(userName + ": " + input, userName);
                }
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            } finally {
                try {
                    clients.remove(userName);
                    socket.close();
                    broadcastMessage(userName + " has left the chat.", userName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
