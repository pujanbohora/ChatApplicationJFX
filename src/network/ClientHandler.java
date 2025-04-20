package network;

import model.ChatManager;
import model.Message;
import model.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Handles communication with a client.
 * This class manages the input/output streams for a connected client.
 */
public class ClientHandler {
    private final User user;
    private final Socket socket;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    private final ChatManager chatManager;
    private boolean running;

    /**
     * Constructor with user, socket, streams, and chat manager
     *
     * @param user         the user associated with this client
     * @param socket       the client socket
     * @param inputStream  the input stream for receiving messages
     * @param outputStream the output stream for sending messages
     * @param chatManager  the chat manager
     */
    public ClientHandler(User user, Socket socket, ObjectInputStream inputStream,
                         ObjectOutputStream outputStream, ChatManager chatManager) {
        this.user = user;
        this.socket = socket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.chatManager = chatManager;
    }

    /**
     * Starts handling messages for this client
     */
    public void start() {
        running = true;

        // Start a thread to receive messages
        new Thread(this::receiveMessages).start();

        // Send a welcome message
        try {
            sendMessage(new Message(
                    new User("System", "0.0.0.0"),
                    "Welcome to the chat, " + user.getUsername() + "!",
                    Message.MessageType.SYSTEM
            ));
        } catch (IOException e) {
            System.err.println("Error sending welcome message: " + e.getMessage());
        }
    }

    /**
     * Receives messages from the client
     */
    private void receiveMessages() {
        while (running) {
            try {
                // Read a message from the client
                Message message = (Message) inputStream.readObject();

                // Process the message
                chatManager.receiveMessage(message);

                // Broadcast the message to all clients
                chatManager.receiveMessage(message);

            } catch (IOException | ClassNotFoundException e) {
                if (running) {
                    System.err.println("Error receiving message from " + user.getUsername() + ": " + e.getMessage());
                    stop();
                }
                break;
            }
        }
    }

    /**
     * Sends a message to the client
     *
     * @param message the message to send
     * @throws IOException if an I/O error occurs
     */
    public void sendMessage(Message message) throws IOException {
        outputStream.writeObject(message);
        outputStream.flush();
    }

    /**
     * Stops handling messages for this client
     */
    public void stop() {
        running = false;

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }

        // Update user status and notify chat manager
        user.setOnline(false);
        chatManager.removeUser(user);

        System.out.println("Client handler for " + user.getUsername() + " stopped");
    }

    /**
     * Gets the user associated with this client
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Checks if the client handler is running
     *
     * @return true if the client handler is running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }
}
