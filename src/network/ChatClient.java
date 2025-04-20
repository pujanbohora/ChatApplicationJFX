package network;

import model.ChatManager;
import model.Message;
import model.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Client implementation for the chat application.
 * This class handles connection to the server and message sending/receiving.
 */
public class ChatClient {
    private final String serverAddress;
    private final int port;
    private final ChatManager chatManager;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private boolean connected;
    private Thread receiveThread;

    /**
     * Constructor with server address, port, and chat manager
     *
     * @param serverAddress the server address to connect to
     * @param port          the server port
     * @param chatManager   the chat manager
     */
    public ChatClient(String serverAddress, int port, ChatManager chatManager) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.chatManager = chatManager;
        this.connected = false;
    }

    /**
     * Connects to the server
     *
     * @throws IOException if an I/O error occurs
     */
    public void connect() throws IOException {
        if (connected) return;

        try {
            // Connect to the server
            socket = new Socket(serverAddress, port);

            // Set up input/output streams
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            // Send user information to the server
            outputStream.writeObject(chatManager.getCurrentUser());
            outputStream.flush();

            connected = true;

            // Start receiving messages
            receiveThread = new Thread(this::receiveMessages);
            receiveThread.start();

            System.out.println("Connected to server at " + serverAddress + ":" + port);

        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Receives messages from the server
     */
    private void receiveMessages() {
        while (connected) {
            try {
                // Read a message from the server
                Message message = (Message) inputStream.readObject();

                // Process the message
                chatManager.receiveMessage(message);

            } catch (IOException | ClassNotFoundException e) {
                if (connected) {
                    System.err.println("Error receiving message from server: " + e.getMessage());
                    disconnect();
                }
                break;
            }
        }
    }

    /**
     * Sends a message to the server
     *
     * @param message the message to send
     * @throws IOException if an I/O error occurs
     */
    public void sendMessage(Message message) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to server");
        }

        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            System.err.println("Error sending message to server: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Disconnects from the server
     */
    public void disconnect() {
        if (!connected) return;

        connected = false;

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }

        System.out.println("Disconnected from server");
    }

    /**
     * Checks if the client is connected to the server
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Discovers servers on the local network
     * This is a simple implementation that tries to connect to common local IP addresses
     *
     * @param port the port to check
     * @return an array of available server addresses
     */
    public static String[] discoverServers(int port) {
        java.util.List<String> servers = new java.util.ArrayList<>();

        try {
            // Get local IP address
            String localIP = InetAddress.getLocalHost().getHostAddress();
            String subnet = localIP.substring(0, localIP.lastIndexOf('.') + 1);

            System.out.println("Scanning subnet: " + subnet + "0-255");

            // Scan the local subnet (limited range for performance)
            for (int i = 1; i <= 255; i++) {
                final String host = subnet + i;

                // Skip own IP
                if (host.equals(localIP)) continue;

                // Try to connect to each IP
                new Thread(() -> {
                    try {
                        Socket socket = new Socket();
                        socket.connect(new java.net.InetSocketAddress(host, port), 100); // 100ms timeout
                        socket.close();

                        synchronized (servers) {
                            servers.add(host);
                            System.out.println("Found server at: " + host);
                        }
                    } catch (IOException ignored) {
                        // Connection failed, server not available at this address
                    }
                }).start();
            }

            // Wait for all threads to complete (with timeout)
            Thread.sleep(2000); // 2 seconds should be enough for local network

        } catch (UnknownHostException | InterruptedException e) {
            System.err.println("Error discovering servers: " + e.getMessage());
        }

        return servers.toArray(new String[0]);
    }
}
