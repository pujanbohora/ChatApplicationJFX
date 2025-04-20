package persistence;

import model.Message;
import model.User;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles file operations for chat history persistence.
 * This class provides methods to save and load chat messages.
 */
public class ChatHistoryManager {
    private static final String HISTORY_DIRECTORY = "data";
    private static final String HISTORY_FILE = "chat_history.txt";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructor that ensures the history directory exists
     */
    public ChatHistoryManager() {
        // Create history directory if it doesn't exist
        File historyDir = new File(HISTORY_DIRECTORY);
        if (!historyDir.exists()) {
            historyDir.mkdirs();
        }
    }

    /**
     * Saves a list of messages to the history file
     *
     * @param messages the list of messages to save
     * @throws IOException if an I/O error occurs
     */
    public void saveMessages(List<Message> messages) throws IOException {
        File historyFile = new File(HISTORY_DIRECTORY, HISTORY_FILE);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyFile))) {
            for (Message message : messages) {
                // Format: username|timestamp|type|content
                writer.write(formatMessageForStorage(message));
                writer.newLine();
            }
        }
    }

    /**
     * Formats a message for storage in the history file
     *
     * @param message the message to format
     * @return the formatted message string
     */
    private String formatMessageForStorage(Message message) {
        return message.getSender().getUsername() + "|" +
                message.getTimestamp().format(DATE_TIME_FORMATTER) + "|" +
                message.getType() + "|" +
                message.getContent();
    }

    /**
     * Loads messages from the history file
     *
     * @return the list of loaded messages
     * @throws IOException if an I/O error occurs
     */
    public List<Message> loadMessages() throws IOException {
        List<Message> messages = new ArrayList<>();
        File historyFile = new File(HISTORY_DIRECTORY, HISTORY_FILE);

        if (!historyFile.exists()) {
            return messages;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    Message message = parseMessageFromStorage(line);
                    if (message != null) {
                        messages.add(message);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing message: " + line + " - " + e.getMessage());
                }
            }
        }

        return messages;
    }

    /**
     * Parses a message from a storage string
     *
     * @param line the line to parse
     * @return the parsed message, or null if parsing failed
     */
    private Message parseMessageFromStorage(String line) {
        String[] parts = line.split("\\|", 4);
        if (parts.length < 4) {
            return null;
        }

        try {
            String username = parts[0];
            LocalDateTime timestamp = LocalDateTime.parse(parts[1], DATE_TIME_FORMATTER);
            Message.MessageType type = Message.MessageType.valueOf(parts[2]);
            String content = parts[3];

            User sender = new User(username, "0.0.0.0"); // IP address not stored in history

            // Create a message with the parsed timestamp
            return new Message(sender, content, type) {
                @Override
                public LocalDateTime getTimestamp() {
                    return timestamp;
                }
            };
        } catch (DateTimeParseException | IllegalArgumentException e) {
            System.err.println("Error parsing message components: " + e.getMessage());
            return null;
        }
    }

    /**
     * Appends a single message to the history file
     *
     * @param message the message to append
     * @throws IOException if an I/O error occurs
     */
    public void appendMessage(Message message) throws IOException {
        File historyFile = new File(HISTORY_DIRECTORY, HISTORY_FILE);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyFile, true))) {
            writer.write(formatMessageForStorage(message));
            writer.newLine();
        }
    }

    /**
     * Clears the chat history
     *
     * @throws IOException if an I/O error occurs
     */
    public void clearHistory() throws IOException {
        File historyFile = new File(HISTORY_DIRECTORY, HISTORY_FILE);

        if (historyFile.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyFile))) {
                // Just open and close the file to clear it
            }
        }
    }

    /**
     * Gets the path to the history file
     *
     * @return the path to the history file
     */
    public String getHistoryFilePath() {
        return new File(HISTORY_DIRECTORY, HISTORY_FILE).getAbsolutePath();
    }
}
