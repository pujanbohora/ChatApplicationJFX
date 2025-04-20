package model;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

/**
 * Manages chat sessions and message history.
 * This class is responsible for handling messages, users, and chat history.
 */
public class ChatManager {
    private List<User> activeUsers;
    private List<Message> messageHistory;
    private User currentUser;
    private NetworkManager networkManager;
    private FileManager fileManager;

    /**
     * Constructor with current user
     *
     * @param currentUser the current user of the application
     */
    public ChatManager(User currentUser) {
        this.currentUser = currentUser;
        this.activeUsers = new ArrayList<>();
        this.messageHistory = new ArrayList<>();
        this.activeUsers.add(currentUser);
    }

    // In ChatManager.java, add an interface for listeners
    public interface ChatManagerListener {
        void onUserAdded(User user);
        void onUserRemoved(User user);
        void onMessageReceived(Message message);
    }

    // Add a list of listeners
    private List<ChatManagerListener> listeners = new ArrayList<>();

    // Add methods to register/unregister listeners
    public void addListener(ChatManagerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ChatManagerListener listener) {
        listeners.remove(listener);
    }

    // Modify the addUser method to notify listeners
    public void addUser(User user) {
        if (!activeUsers.contains(user)) {
            activeUsers.add(user);
            // Notify listeners
            for (ChatManagerListener listener : listeners) {
                listener.onUserAdded(user);
            }
        }
    }

    // Modify the removeUser method to notify listeners
    public void removeUser(User user) {
        activeUsers.remove(user);
        // Notify listeners
        for (ChatManagerListener listener : listeners) {
            listener.onUserRemoved(user);
        }
    }


    /**
     * Initializes the chat manager
     * Sets up network manager and file manager
     */
    public void initialize() {
        try {
            // Initialize network manager with default port 8888
            this.networkManager = new NetworkManager(this, 8888);

            // Initialize file manager
            this.fileManager = new FileManager(this);

            // Load message history from file
            loadHistory();

            // Add system message indicating initialization
            Message systemMessage = new Message(
                    new User("System", "0.0.0.0"),
                    "Chat initialized for " + currentUser.getUsername(),
                    Message.MessageType.SYSTEM
            );
            messageHistory.add(systemMessage);

        } catch (Exception e) {
            System.err.println("Error initializing ChatManager: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends a message to a recipient
     *
     * @param content the content of the message
     * @param recipient the recipient of the message
     */
    public void sendMessage(String content, User recipient) {
        Message message = new Message(currentUser, content);
        messageHistory.add(message);

        // If recipient is not null, send direct message
        if (recipient != null) {
            try {
                networkManager.sendDirectMessage(message, recipient);
            } catch (Exception e) {
                System.err.println("Error sending direct message: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Otherwise broadcast to all users
            try {
                networkManager.broadcastMessage(message);
            } catch (Exception e) {
                System.err.println("Error broadcasting message: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Save message history after each message
        saveHistory();
    }

    /**
     * Receives a message from the network
     *
     * @param message the received message
     */
    public void receiveMessage(Message message) {
        messageHistory.add(message);
        saveHistory();

        // Notify listeners
        for (ChatManagerListener listener : listeners) {
            listener.onMessageReceived(message);
        }
    }


    /**
     * Adds a user to the active users list
     *
     * @param user the user to add
     */
//    public void addUser(User user) {
//        System.out.println("NEW USER::::::");
//        if (!activeUsers.contains(user)) {
//            System.out.println(user);
//            activeUsers.add(user);
//
//            System.out.println("USER ADDED");
//            System.out.println(activeUsers);
//
//            // Add system message indicating user joined
//            Message systemMessage = new Message(
//                    new User("System", "0.0.0.0"),
//                    user.getUsername() + " has joined the chat",
//                    Message.MessageType.SYSTEM
//            );
//            messageHistory.add(systemMessage);
//            saveHistory();
//        }
//    }
//
//    /**
//     * Removes a user from the active users list
//     *
//     * @param user the user to remove
//     */
//    public void removeUser(User user) {
//        if (activeUsers.contains(user)) {
//            activeUsers.remove(user);
//
//            // Add system message indicating user left
//            Message systemMessage = new Message(
//                    new User("System", "0.0.0.0"),
//                    user.getUsername() + " has left the chat",
//                    Message.MessageType.SYSTEM
//            );
//            messageHistory.add(systemMessage);
//            saveHistory();
//        }
//    }

    /**
     * Gets the list of active users
     *
     * @return the list of active users
     */
    public List<User> getActiveUsers() {
        return activeUsers;
    }

    /**
     * Gets the message history
     *
     * @return the message history
     */
    public List<Message> getMessageHistory() {
        return messageHistory;
    }

    /**
     * Gets the current user
     *
     * @return the current user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets the current user
     *
     * @param user the user to set as current
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Loads message history from file
     */
    public void loadHistory() {
        try {
            List<Message> loadedMessages = fileManager.loadMessageHistory();
            if (loadedMessages != null) {
                messageHistory.addAll(loadedMessages);
            }
        } catch (IOException e) {
            System.err.println("Error loading message history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Saves message history to file
     */
    public void saveHistory() {
        try {
            fileManager.saveMessageHistory(messageHistory);
        } catch (IOException e) {
            System.err.println("Error saving message history: " + e.getMessage());
            e.printStackTrace();
        }
    }
}



