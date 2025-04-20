package model;

import chatbot.ChatBotIntegration;

/**
 * Represents a chatbot in the chat application.
 * This class integrates with a Python-based AI model for generating responses.
 */
public class ChatBot {
    private User botUser;
    private ChatBotIntegration chatBotIntegration;
    private boolean initialized;

    /**
     * Constructor with bot name
     *
     * @param name the name of the chatbot
     */
    public ChatBot(String name) {
        this.botUser = new User(name, "0.0.0.0", true, "bot_avatar.png");
        this.initialized = false;
    }

    /**
     * Initializes the chatbot
     * Sets up the Python process for AI response generation
     */
    public void initialize() {
        chatBotIntegration = new ChatBotIntegration(botUser);
        initialized = chatBotIntegration.initialize();

        if (initialized) {
            System.out.println("ChatBot initialized successfully");
        } else {
            System.err.println("Failed to initialize ChatBot");
        }
    }

    /**
     * Generates a response to the given input
     *
     * @param input the input message
     * @return the generated response
     */
    public String generateResponse(String input) {
        if (!initialized) {
            return "ChatBot not initialized";
        }

        Message inputMessage = new Message(new User("User", "0.0.0.0"), input);
        Message responseMessage = chatBotIntegration.generateResponse(inputMessage);
        return responseMessage.getContent();
    }

    /**
     * Gets the bot user
     *
     * @return the bot user
     */
    public User getBotUser() {
        return botUser;
    }

    /**
     * Shuts down the chatbot
     * Closes the Python process
     */
    public void shutdown() {
        if (chatBotIntegration != null) {
            chatBotIntegration.shutdown();
            initialized = false;
            System.out.println("ChatBot shutdown");
        }
    }
}
