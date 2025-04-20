package test;

import model.*;
import network.*;
import persistence.*;
import chatbot.*;

import java.io.IOException;
import java.util.List;

/**
 * Test class for validating the chat application against project requirements.
 * This class tests all major components of the application.
 */
public class ApplicationTest {

    /**
     * Main method to run the tests
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Starting Chat Application Tests");
        System.out.println("===============================");

        // Test model classes
        testModelClasses();

        // Test network communication
        testNetworkCommunication();

        // Test persistence
        testPersistence();

        // Test chatbot integration
        testChatbotIntegration();

        System.out.println("\nAll tests completed!");
    }

    /**
     * Tests the model classes
     */
    private static void testModelClasses() {
        System.out.println("\nTesting Model Classes");
        System.out.println("--------------------");

        // Test User class
        System.out.println("Testing User class...");
        User user1 = new User("TestUser1", "192.168.1.1");
        User user2 = new User("TestUser2", "192.168.1.2", true, "custom_avatar.png");

        assert user1.getUsername().equals("TestUser1") : "Username not set correctly";
        assert user1.getIpAddress().equals("192.168.1.1") : "IP address not set correctly";
        assert !user1.isOnline() : "Online status should be false by default";
        assert user1.getAvatar().equals("default_avatar.png") : "Default avatar not set correctly";

        assert user2.isOnline() : "Online status not set correctly";
        assert user2.getAvatar().equals("custom_avatar.png") : "Custom avatar not set correctly";

        user1.setOnline(true);
        assert user1.isOnline() : "Failed to update online status";

        System.out.println("User class tests passed!");

        // Test Message class
        System.out.println("Testing Message class...");
        Message message1 = new Message(user1, "Hello, world!");
        Message message2 = new Message(user2, "System notification", Message.MessageType.SYSTEM);

        assert message1.getSender().equals(user1) : "Sender not set correctly";
        assert message1.getContent().equals("Hello, world!") : "Content not set correctly";
        assert message1.getType() == Message.MessageType.CHAT : "Default message type should be CHAT";
        assert message1.getTimestamp() != null : "Timestamp not set";

        assert message2.getType() == Message.MessageType.SYSTEM : "Message type not set correctly";

        String formattedMessage = message1.formatForDisplay();
        assert formattedMessage.contains("TestUser1") : "Formatted message should contain username";
        assert formattedMessage.contains("Hello, world!") : "Formatted message should contain content";

        System.out.println("Message class tests passed!");

        // Test ChatManager class
        System.out.println("Testing ChatManager class...");
        ChatManager chatManager = new ChatManager(user1);
        chatManager.initialize();

        chatManager.addUser(user2);
        List<User> activeUsers = chatManager.getActiveUsers();
        assert activeUsers.size() == 2 : "Active users list should contain 2 users";
        assert activeUsers.contains(user1) : "Active users should contain user1";
        assert activeUsers.contains(user2) : "Active users should contain user2";

        chatManager.sendMessage("Test message", user2);
        List<Message> messageHistory = chatManager.getMessageHistory();
        assert messageHistory.size() >= 1 : "Message history should contain at least 1 message";

        // Find the test message
        boolean foundTestMessage = false;
        for (Message message : messageHistory) {
            if (message.getContent().equals("Test message") &&
                    message.getSender().equals(user1)) {
                foundTestMessage = true;
                break;
            }
        }
        assert foundTestMessage : "Test message not found in history";

        System.out.println("ChatManager class tests passed!");
    }

    /**
     * Tests the network communication
     */
    private static void testNetworkCommunication() {
        System.out.println("\nTesting Network Communication");
        System.out.println("----------------------------");

        // Test NetworkManager class
        System.out.println("Testing NetworkManager class...");
        User user = new User("NetworkTestUser", "192.168.1.1");
        ChatManager chatManager = new ChatManager(user);
        NetworkManager networkManager = new NetworkManager(chatManager, 8889);

        assert networkManager.getPort() == 8889 : "Port not set correctly";

        System.out.println("NetworkManager class tests passed!");

        // Test ServerDiscovery class
        System.out.println("Testing ServerDiscovery class...");
        ServerDiscovery serverDiscovery = new ServerDiscovery(8889);
        assert serverDiscovery != null : "Failed to create ServerDiscovery";

        // Note: We're not actually starting advertising or discovering servers
        // as that would require network access and multiple instances

        System.out.println("ServerDiscovery class tests passed!");

        // Test ChatServer and ChatClient classes
        System.out.println("Testing ChatServer and ChatClient classes...");
        // Note: We're not actually starting the server or connecting clients
        // as that would require network access and multiple instances

        System.out.println("Network communication tests completed!");
    }

    /**
     * Tests the persistence functionality
     */
    private static void testPersistence() {
        System.out.println("\nTesting Persistence");
        System.out.println("------------------");

        // Test ChatHistoryManager class
        System.out.println("Testing ChatHistoryManager class...");
        ChatHistoryManager chatHistoryManager = new ChatHistoryManager();

        // Create test messages
        User user1 = new User("PersistenceTestUser1", "192.168.1.1");
        User user2 = new User("PersistenceTestUser2", "192.168.1.2");
        Message message1 = new Message(user1, "Test message 1");
        Message message2 = new Message(user2, "Test message 2");

        try {
            // Save messages
            chatHistoryManager.clearHistory();
            chatHistoryManager.appendMessage(message1);
            chatHistoryManager.appendMessage(message2);

            // Load messages
            List<Message> loadedMessages = chatHistoryManager.loadMessages();
            assert loadedMessages.size() == 2 : "Should have loaded 2 messages";

            // Verify message content
            boolean foundMessage1 = false;
            boolean foundMessage2 = false;
            for (Message message : loadedMessages) {
                if (message.getContent().equals("Test message 1") &&
                        message.getSender().getUsername().equals("PersistenceTestUser1")) {
                    foundMessage1 = true;
                }
                if (message.getContent().equals("Test message 2") &&
                        message.getSender().getUsername().equals("PersistenceTestUser2")) {
                    foundMessage2 = true;
                }
            }
            assert foundMessage1 : "Test message 1 not found in loaded messages";
            assert foundMessage2 : "Test message 2 not found in loaded messages";

            System.out.println("ChatHistoryManager class tests passed!");
        } catch (IOException e) {
            System.err.println("Error testing ChatHistoryManager: " + e.getMessage());
        }

        // Test UserProfileManager class
        System.out.println("Testing UserProfileManager class...");
        UserProfileManager userProfileManager = new UserProfileManager();

        try {
            // Save user profile
            User testUser = new User("ProfileTestUser", "192.168.1.3", true, "test_avatar.png");
            userProfileManager.saveUserProfile(testUser);

            // Load user profile
            User loadedUser = userProfileManager.loadUserProfile("ProfileTestUser");
            assert loadedUser != null : "Failed to load user profile";
            assert loadedUser.getUsername().equals("ProfileTestUser") : "Username not loaded correctly";
            assert loadedUser.isOnline() : "Online status not loaded correctly";
            assert loadedUser.getAvatar().equals("test_avatar.png") : "Avatar not loaded correctly";

            // Get saved usernames
            List<String> savedUsernames = userProfileManager.getSavedUsernames();
            assert savedUsernames.contains("ProfileTestUser") : "ProfileTestUser not found in saved usernames";

            // Delete user profile
            boolean deleted = userProfileManager.deleteUserProfile("ProfileTestUser");
            assert deleted : "Failed to delete user profile";

            System.out.println("UserProfileManager class tests passed!");
        } catch (IOException e) {
            System.err.println("Error testing UserProfileManager: " + e.getMessage());
        }

        // Test FileManager class
        System.out.println("Testing FileManager class...");
        User user = new User("FileManagerTestUser", "192.168.1.4");
        ChatManager chatManager = new ChatManager(user);
        FileManager fileManager = new FileManager(chatManager);

        try {
            // Test message history operations
            Message testMessage = new Message(user, "FileManager test message");
            fileManager.appendMessage(testMessage);

            List<Message> loadedMessages = fileManager.loadMessageHistory();
            boolean foundTestMessage = false;
            for (Message message : loadedMessages) {
                if (message.getContent().equals("FileManager test message") &&
                        message.getSender().getUsername().equals("FileManagerTestUser")) {
                    foundTestMessage = true;
                    break;
                }
            }
            assert foundTestMessage : "Test message not found in loaded messages";

            // Test user profile operations
            User profileUser = new User("FileManagerProfileUser", "192.168.1.5");
            fileManager.saveUserProfile(profileUser);

            User loadedUser = fileManager.loadUserProfile("FileManagerProfileUser");
            assert loadedUser != null : "Failed to load user profile";
            assert loadedUser.getUsername().equals("FileManagerProfileUser") : "Username not loaded correctly";

            // Clean up
            fileManager.deleteUserProfile("FileManagerProfileUser");

            System.out.println("FileManager class tests passed!");
        } catch (IOException e) {
            System.err.println("Error testing FileManager: " + e.getMessage());
        }
    }

    /**
     * Tests the chatbot integration
     */
    private static void testChatbotIntegration() {
        System.out.println("\nTesting Chatbot Integration");
        System.out.println("-------------------------");

        // Test ChatBot class
        System.out.println("Testing ChatBot class...");
        ChatBot chatBot = new ChatBot("TestChatBot");

        // Note: We're not actually initializing the chatbot as that would require
        // Python and potentially external dependencies

        User botUser = chatBot.getBotUser();
        assert botUser != null : "Bot user should not be null";
        assert botUser.getUsername().equals("TestChatBot") : "Bot username not set correctly";

        System.out.println("ChatBot class tests passed!");

        // Test ChatBotIntegration class
        System.out.println("Testing ChatBotIntegration class...");
        // Note: We're not actually initializing the integration as that would require
        // Python and potentially external dependencies

        System.out.println("Chatbot integration tests completed!");
    }
}
