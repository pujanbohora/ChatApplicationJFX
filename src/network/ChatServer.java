package network;

import model.ChatManager;
import model.Message;
import model.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server implementation for the chat application.
 * This class handles incoming connections and message routing.
 */
public class ChatServer {
    private final int port;
    private final ChatManager chatManager;
    private ServerSocket serverSocket;
    private boolean running;
    private final ExecutorService threadPool;

    /**
     * Constructor with port and chat manager
     *
     * @param port the port to listen on
     * @param chatManager the chat manager
     */
    public ChatServer(int port, ChatManager chatManager) {
        this.port = port;
        this.chatManager = chatManager;
        this.threadPool = Executors.newCachedThreadPool();
    }

    /**
     * Starts the server
     *
     * @throws IOException if an I/O error occurs
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;

        System.out.println("Server started on " + InetAddress.getLocalHost().getHostAddress() + ":" + port);

        // Add a system message to the chat
        chatManager.receiveMessage(new Message(
                new User("System", "0.0.0.0"),
                "Server started on " + InetAddress.getLocalHost().getHostAddress() + ":" + port,
                Message.MessageType.SYSTEM
        ));

        // Start accepting client connections
        threadPool.execute(this::acceptClients);
    }

    /**
     * Accepts client connections
     */
    private void acceptClients() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Handles a client connection
     *
     * @param clientSocket the client socket
     */
    private void handleClient(Socket clientSocket) {
        threadPool.execute(() -> {
            try {
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                System.out.println("New client connected: " + clientAddress);

                // Set up input/output streams
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                // Read user information
                model.User user = (model.User) in.readObject();
                user.setIpAddress(clientAddress);
                user.setOnline(true);

                // Add user to chat manager
                chatManager.addUser(user);

                // Create a client handler
                ClientHandler handler = new ClientHandler(user, clientSocket, in, out, chatManager);

                // Start handling messages
                handler.start();

            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error handling client: " + e.getMessage());
            }
        });
    }

    /**
     * Stops the server
     */
    public void stop() {
        running = false;
        threadPool.shutdown();

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }

        System.out.println("Server stopped");
    }

    /**
     * Checks if the server is running
     *
     * @return true if the server is running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }
}
