package chatbot;

import model.ChatBot;
import model.Message;
import model.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ChatBot integration manager that handles the communication between
 * the Java application and the Python-based chatbot.
 */
public class ChatBotIntegration {
    private Process pythonProcess;
    private BufferedWriter processInput;
    private BufferedReader processOutput;
    private boolean initialized;
    private final User botUser;

    /**
     * Constructor with bot user
     *
     * @param botUser the user representing the chatbot
     */
    public ChatBotIntegration(User botUser) {
        this.botUser = botUser;
        this.initialized = false;
    }

    /**
     * Initializes the chatbot integration
     *
     * @return true if initialization was successful, false otherwise
     */
    public boolean initialize() {
        try {
            // Ensure the Python script exists
            ensurePythonScript();

            // Set up the process builder to run the Python script
            ProcessBuilder processBuilder = new ProcessBuilder("python3",
                    "src/chatbot/chatbot.py");
            processBuilder.redirectErrorStream(true);

            // Start the process
            pythonProcess = processBuilder.start();

            // Set up input/output streams
            processInput = new BufferedWriter(
                    new OutputStreamWriter(pythonProcess.getOutputStream()));
            processOutput = new BufferedReader(
                    new InputStreamReader(pythonProcess.getInputStream()));

            // Read the initialization message
            String initMessage = processOutput.readLine();
            System.out.println("ChatBot initialization: " + initMessage);

            initialized = true;
            return true;
        } catch (IOException e) {
            System.err.println("Error initializing ChatBot: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Ensures the Python script exists in the correct location
     *
     * @throws IOException if an I/O error occurs
     */
    private void ensurePythonScript() throws IOException {
        Path scriptPath = Paths.get("src/chatbot/chatbot.py");
        if (!Files.exists(scriptPath)) {
            // Create directories if they don't exist
            Files.createDirectories(scriptPath.getParent());

            // Copy the script from the resources
            String scriptContent = Files.readString(Paths.get("src/chatbot/chatbot.py"));
            Files.writeString(scriptPath, scriptContent);
        }
    }

    /**
     * Generates a response to the given message
     *
     * @param message the message to respond to
     * @return the response message
     */
    public Message generateResponse(Message message) {
        if (!initialized) {
            return new Message(botUser, "ChatBot not initialized", Message.MessageType.SYSTEM);
        }

        try {
            // Send input to the Python process
            processInput.write(message.getContent());
            processInput.newLine();
            processInput.flush();

            // Read the response
            String response = processOutput.readLine();
            if (response == null) {
                return new Message(botUser, "No response from ChatBot", Message.MessageType.SYSTEM);
            }

            return new Message(botUser, response);
        } catch (IOException e) {
            System.err.println("Error generating response: " + e.getMessage());
            return new Message(botUser, "Error generating response: " + e.getMessage(),
                    Message.MessageType.SYSTEM);
        }
    }

    /**
     * Checks if the chatbot is initialized
     *
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Shuts down the chatbot integration
     */
    public void shutdown() {
        if (pythonProcess != null) {
            try {
                if (processInput != null) {
                    processInput.close();
                }
                if (processOutput != null) {
                    processOutput.close();
                }
                pythonProcess.destroy();
                System.out.println("ChatBot shutdown");
            } catch (IOException e) {
                System.err.println("Error shutting down ChatBot: " + e.getMessage());
            }
        }
        initialized = false;
    }
}
