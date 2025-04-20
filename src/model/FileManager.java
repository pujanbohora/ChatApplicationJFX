package model;

import persistence.ChatHistoryManager;
import persistence.UserProfileManager;

import java.io.IOException;
import java.util.List;

/**
 * Manages file operations for the chat application.
 * This class handles saving and loading message history and user profiles
 * by delegating to specialized persistence managers.
 */
public class FileManager {
    private ChatManager chatManager;
    private ChatHistoryManager chatHistoryManager;
    private UserProfileManager userProfileManager;

    /**
     * Constructor with chat manager
     *
     * @param chatManager the chat manager
     */
    public FileManager(ChatManager chatManager) {
        this.chatManager = chatManager;
        this.chatHistoryManager = new ChatHistoryManager();
        this.userProfileManager = new UserProfileManager();
    }

    /**
     * Saves message history to file
     *
     * @param messages the list of messages to save
     * @throws IOException if an I/O error occurs
     */
    public void saveMessageHistory(List<Message> messages) throws IOException {
        chatHistoryManager.saveMessages(messages);
    }

    /**
     * Loads message history from file
     *
     * @return the list of loaded messages
     * @throws IOException if an I/O error occurs
     */
    public List<Message> loadMessageHistory() throws IOException {
        return chatHistoryManager.loadMessages();
    }

    /**
     * Appends a single message to the history file
     *
     * @param message the message to append
     * @throws IOException if an I/O error occurs
     */
    public void appendMessage(Message message) throws IOException {
        chatHistoryManager.appendMessage(message);
    }

    /**
     * Clears the chat history
     *
     * @throws IOException if an I/O error occurs
     */
    public void clearHistory() throws IOException {
        chatHistoryManager.clearHistory();
    }

    /**
     * Saves user profile to file
     *
     * @param user the user to save
     * @throws IOException if an I/O error occurs
     */
    public void saveUserProfile(User user) throws IOException {
        userProfileManager.saveUserProfile(user);
    }

    /**
     * Loads user profile from file
     *
     * @param username the username of the profile to load
     * @return the loaded user, or null if not found
     * @throws IOException if an I/O error occurs
     */
    public User loadUserProfile(String username) throws IOException {
        return userProfileManager.loadUserProfile(username);
    }

    /**
     * Gets a list of all saved user profiles
     *
     * @return a list of usernames
     */
    public List<String> getSavedUsernames() {
        return userProfileManager.getSavedUsernames();
    }

    /**
     * Deletes a user profile
     *
     * @param username the username of the profile to delete
     * @return true if the profile was deleted, false otherwise
     */
    public boolean deleteUserProfile(String username) {
        return userProfileManager.deleteUserProfile(username);
    }

    /**
     * Gets the path to the history file
     *
     * @return the path to the history file
     */
    public String getHistoryFilePath() {
        return chatHistoryManager.getHistoryFilePath();
    }
}
