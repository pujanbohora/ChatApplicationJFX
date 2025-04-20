package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a message in the chat application.
 * This class encapsulates message content, sender information, and metadata.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private User sender;
    private String content;
    private LocalDateTime timestamp;
    private MessageType type;

    /**
     * Enum for message types
     */
    public enum MessageType {
        CHAT,       // Regular chat message
        SYSTEM,     // System notification
        FILE,       // File transfer
        COMMAND     // Command message
    }

    /**
     * Constructor with sender and content
     *
     * @param sender the message sender
     * @param content the message content
     */
    public Message(User sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.type = MessageType.CHAT;
    }

    /**
     * Constructor with sender, content, and type
     *
     * @param sender the message sender
     * @param content the message content
     * @param type the message type
     */
    public Message(User sender, String content, MessageType type) {
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.type = type;
    }

    /**
     * Gets the message sender
     *
     * @return the sender
     */
    public User getSender() {
        return sender;
    }

    /**
     * Gets the message content
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the message timestamp
     *
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the message type
     *
     * @return the type
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Formats the message for display
     *
     * @return the formatted message
     */
    public String formatForDisplay() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String timeStr = timestamp.format(formatter);

        switch (type) {
            case SYSTEM:
                return "[" + timeStr + "] SYSTEM: " + content;
            case FILE:
                return "[" + timeStr + "] " + sender.getUsername() + " shared a file: " + content;
            case COMMAND:
                return "[" + timeStr + "] COMMAND: " + content;
            case CHAT:
            default:
                return "[" + timeStr + "] " + sender.getUsername() + ": " + content;
        }
    }

    /**
     * Returns a string representation of this message
     *
     * @return a string representation of this message
     */
    @Override
    public String toString() {
        return sender.getUsername() + "," +
                timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "," +
                type + "," +
                content;
    }
}
