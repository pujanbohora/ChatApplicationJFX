package model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages network communication for the chat application.
 * This class handles server and client functionality for local network communication.
 */
public class NetworkManager {
    private ServerSocket serverSocket;
    private Map<User, Socket> clientSockets;
    private boolean isServer;
    private String serverAddress;
    private int port;
    private ChatManager chatManager;
    private ExecutorService threadPool;
    private boolean running;

    /**
     * Constructor with chat manager and port
     *
     * @param chatManager the chat manager
     * @param port the port to use for communication
     */
    public NetworkManager(ChatManager chatManager, int port) {
        this.chatManager = chatManager;
        this.port = port;
        this.clientSockets = new HashMap<>();
        this.isServer = false;
        this.serverAddress = "127.0.0.1"; // Default to localhost
        this.threadPool = Executors.newCachedThreadPool();
        this.running = false;
    }

    /**
     * Starts the server
     * Listens for incoming connections and handles them
     */
    public void startServer() {
        if (running) return;

        try {
            serverSocket = new ServerSocket(port);
            isServer = true;
            running = true;

            // Start a thread to accept client connections
            threadPool.execute(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        handleClientConnection(clientSocket);
                    } catch (IOException e) {
                        if (running) {
                            System.err.println("Error accepting client connection: " + e.getMessage());
                        }
                    }
                }
            });

            System.out.println("Server started on port " + port);
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles a client connection
     *
     * @param clientSocket the client socket
     */
    private void handleClientConnection(Socket clientSocket) {
        threadPool.execute(() -> {
            try {
                ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());

                // First message should be the user information
                User user = (User) inputStream.readObject();
                user.setIpAddress(clientSocket.getInetAddress().getHostAddress());
                user.setOnline(true);

                // Add user to active users and client sockets
                chatManager.addUser(user);
                clientSockets.put(user, clientSocket);

                // Handle incoming messages from this client
                while (running) {
                    try {
                        Message message = (Message) inputStream.readObject();
                        chatManager.receiveMessage(message);
                    } catch (Exception e) {
                        System.err.println("Error receiving message: " + e.getMessage());
                        break;
                    }
                }

                // Remove user when connection is closed
                user.setOnline(false);
                chatManager.removeUser(user);
                clientSockets.remove(user);
                clientSocket.close();

            } catch (Exception e) {
                System.err.println("Error handling client connection: " + e.getMessage());
            }
        });
    }

    /**
     * Connects to a server
     *
     * @param serverAddress the server address to connect to
     */
    public void connectToServer(String serverAddress) {
        if (running) return;

        try {
            this.serverAddress = serverAddress;
            Socket socket = new Socket(serverAddress, port);
            running = true;

            // Send current user information to server
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(chatManager.getCurrentUser());

            // Start a thread to receive messages from server
            threadPool.execute(() -> {
                try {
                    ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

                    while (running) {
                        try {
                            Message message = (Message) inputStream.readObject();
                            chatManager.receiveMessage(message);
                        } catch (Exception e) {
                            System.err.println("Error receiving message from server: " + e.getMessage());
                            break;
                        }
                    }

                    socket.close();
                    running = false;

                } catch (Exception e) {
                    System.err.println("Error receiving messages from server: " + e.getMessage());
                }
            });

            System.out.println("Connected to server at " + serverAddress + ":" + port);
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Broadcasts a message to all connected clients
     *
     * @param message the message to broadcast
     */
    public void broadcastMessage(Message message) {
        for (Map.Entry<User, Socket> entry : clientSockets.entrySet()) {
            try {
                Socket socket = entry.getValue();
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject(message);
            } catch (IOException e) {
                System.err.println("Error broadcasting message to " + entry.getKey().getUsername() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Sends a direct message to a specific recipient
     *
     * @param message the message to send
     * @param recipient the recipient of the message
     */
    public void sendDirectMessage(Message message, User recipient) {
        Socket socket = clientSockets.get(recipient);
        if (socket != null) {
            try {
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject(message);
            } catch (IOException e) {
                System.err.println("Error sending direct message to " + recipient.getUsername() + ": " + e.getMessage());
            }
        } else {
            System.err.println("Recipient " + recipient.getUsername() + " not found or not connected");
        }
    }

    /**
     * Disconnects from the network
     * Closes all connections and stops the server if running
     */
    public void disconnectFromNetwork() {
        running = false;

        // Close all client sockets
        for (Socket socket : clientSockets.values()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
        clientSockets.clear();

        // Close server socket if running as server
        if (isServer && serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }

        isServer = false;
        System.out.println("Disconnected from network");
    }

    /**
     * Checks if this instance is running as a server
     *
     * @return true if running as server, false otherwise
     */
    public boolean isServer() {
        return isServer;
    }

    /**
     * Gets the server address
     *
     * @return the server address
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * Gets the port
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }
}
