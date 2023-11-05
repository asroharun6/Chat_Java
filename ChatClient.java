import java.io.*;
import java.net.*;

public class ChatClient {
    private Socket socket;
    private BufferedReader in; // Input stream from server
    private PrintWriter out; // Output stream to server
    private BufferedReader consoleInput; // Input stream from console

    public ChatClient(String serverAddress, int serverPort) throws IOException {
        socket = new Socket(serverAddress, serverPort);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        consoleInput = new BufferedReader(new InputStreamReader(System.in));
    }

    public void start() {
        try {
            System.out.println(in.readLine()); // Read the prompt for name
            String userName = consoleInput.readLine();
            out.println(userName); // Send the user name to the server

            // Thread to read messages from the server
            Thread listenerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Connection to server lost.");
                }
            });
            listenerThread.start();

            // Read messages from the console and send them to the server
            String consoleMessage;
            while (!(consoleMessage = consoleInput.readLine()).equalsIgnoreCase("quit")) {
                out.println(consoleMessage);
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            closeEverything();
        }
    }

    private void closeEverything() {
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
            if (consoleInput != null) consoleInput.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java ChatClient <server ip> <port number>");
            System.exit(1);
        }
        String serverAddress = args[0];
        int serverPort = Integer.parseInt(args[1]);

        try {
            ChatClient client = new ChatClient(serverAddress, serverPort);
            client.start();
        } catch (IOException ex) {
            System.err.println("Error connecting to the server: " + ex.getMessage());
        }
    }
}
