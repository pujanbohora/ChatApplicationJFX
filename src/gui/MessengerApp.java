package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import model.*;
import network.*;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Main application class for the chat application.
 * This class implements a Messenger-style JavaFX GUI and integrates all components.
 */
public class MessengerApp extends Application implements ChatManager.ChatManagerListener {

    // Model components
    private ChatManager chatManager;
    private NetworkManager networkManager;
    private FileManager fileManager;
    private ChatBot chatBot;

    // UI components
    private Stage primaryStage;
    private Scene mainScene;
    private ListView<User> userListView;
    private VBox chatMessagesContainer;
    private ScrollPane chatScrollPane;
    private TextField messageInput;
    private Button sendButton;
    private Label statusLabel;
    private User selectedUser;

    // Network components
    private ChatServer server;
    private ChatClient client;
    private ServerDiscovery serverDiscovery;

    // Constants for styling
    private static final String PRIMARY_COLOR = "#0084FF";
    private static final String SECONDARY_COLOR = "#F0F0F0";
    private static final String BACKGROUND_COLOR = "#FFFFFF";
    private static final String TEXT_COLOR = "#000000";
    private static final String ONLINE_COLOR = "#4CAF50";
    private static final String OFFLINE_COLOR = "#9E9E9E";

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Initialize model components
        initializeModel();

        // Register this class as listener (AFTER initializeModel)
        chatManager.addListener(this);

        // Initialize UI
        initializeUI();

        // Set up event handlers
        setupEventHandlers();

        // Show the main window
        primaryStage.setTitle("Java Messenger - " + chatManager.getCurrentUser().getUsername());
        primaryStage.setScene(mainScene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        // Set up network connection
        setupNetworkConnection();
    }


//    public void start(Stage primaryStage) {
//        this.primaryStage = primaryStage;
//
//        // Initialize model components
//        initializeModel();
//
//        // Register as listener
//        chatManager.addListener(this);
//
//        // Initialize model components
//        initializeModel();
//
//        // Initialize UI
//        initializeUI();
//
//        // Set up event handlers
//        setupEventHandlers();
//
//        // Show the main window
//        primaryStage.setTitle("Java Messenger - " + chatManager.getCurrentUser().getUsername());
//        primaryStage.setScene(mainScene);
//        primaryStage.setMinWidth(900);
//        primaryStage.setMinHeight(600);
//        primaryStage.show();
//
//        // Set up network connection
//        setupNetworkConnection();
//    }

    /**
     * Initializes the model components
     */
    private void initializeModel() {
        try {
            // Create current user
            String username = System.getProperty("user.name", "User");
            String hostname = InetAddress.getLocalHost().getHostName();
            User currentUser = new User(username + "@" + hostname, InetAddress.getLocalHost().getHostAddress());

            // Initialize chat manager
            chatManager = new ChatManager(currentUser);
            chatManager.initialize();

            // Initialize chatbot
            chatBot = new ChatBot("ChatBot");
            chatBot.initialize();

            // Add chatbot to active users
            chatManager.addUser(chatBot.getBotUser());


            // Add listener to update UI
            chatManager.addListener(new ChatManager.ChatManagerListener() {
                @Override
                public void onUserAdded(User user) {
                    Platform.runLater(() -> updateUserList());
                }

                @Override
                public void onUserRemoved(User user) {
                    Platform.runLater(() -> updateUserList());
                }

                @Override
                public void onMessageReceived(Message message) {
                    Platform.runLater(() -> updateChatArea());
                }
            });



            // Create directory for avatars if it doesn't exist
            File avatarsDir = new File("avatars");
            if (!avatarsDir.exists()) {
                avatarsDir.mkdir();
            }

        } catch (Exception e) {
            showError("Error initializing application", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initializes the UI components
     */
    private void initializeUI() {
        // Create the root layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");

        // Create the left panel (user list)
        VBox leftPanel = createLeftPanel();
        root.setLeft(leftPanel);

        // Create the center panel (chat area)
        VBox centerPanel = createCenterPanel();
        root.setCenter(centerPanel);

        // Create the status bar
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E0E0E0; -fx-border-width: 1 0 0 0;");

        statusLabel = new Label("Not connected");
        statusLabel.setStyle("-fx-text-fill: " + OFFLINE_COLOR + ";");

        statusBar.getChildren().add(statusLabel);
        root.setBottom(statusBar);

        // Create the scene
        mainScene = new Scene(root, 900, 600);

        // Add CSS styling
        createCssFile();
        mainScene.getStylesheets().add(getClass().getResource("/css/messenger-style.css").toExternalForm());
    }

    /**
     * Creates the left panel with the user list
     *
     * @return the left panel
     */
    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(0));
        leftPanel.setPrefWidth(250);
        leftPanel.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E0E0E0; -fx-border-width: 0 1 0 0;");

        // Create header
        HBox header = new HBox(10);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + PRIMARY_COLOR + ";");

        Label titleLabel = new Label("Contacts");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        header.getChildren().add(titleLabel);

        // Create search box
        TextField searchField = new TextField();
        searchField.setPromptText("Search contacts...");
        searchField.setPadding(new Insets(8));
        searchField.setStyle("-fx-background-radius: 20; -fx-border-radius: 20;");
        VBox.setMargin(searchField, new Insets(0, 10, 10, 10));

        // Create the user list with custom cell factory
        userListView = new ListView<>();
        userListView.setPrefHeight(Integer.MAX_VALUE);
        userListView.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");

        userListView.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox container = new HBox(10);
                    container.setAlignment(Pos.CENTER_LEFT);
                    container.setPadding(new Insets(5, 10, 5, 10));

                    // Create avatar
                    StackPane avatarContainer = new StackPane();
                    ImageView avatarView = new ImageView(new Image(getClass().getResourceAsStream("/images/default_avatar.png")));
                    avatarView.setFitHeight(40);
                    avatarView.setFitWidth(40);

                    // Make avatar circular
                    Circle clip = new Circle(20, 20, 20);
                    avatarView.setClip(clip);

                    // Add online/offline indicator
                    Circle statusIndicator = new Circle(5);
                    statusIndicator.setFill(user.isOnline() ? Color.web(ONLINE_COLOR) : Color.web(OFFLINE_COLOR));
                    StackPane.setAlignment(statusIndicator, Pos.BOTTOM_RIGHT);
                    StackPane.setMargin(statusIndicator, new Insets(0, 0, 0, 0));

                    avatarContainer.getChildren().addAll(avatarView, statusIndicator);

                    // Create user info
                    VBox userInfo = new VBox(2);
                    Label nameLabel = new Label(user.getUsername());
                    nameLabel.setStyle("-fx-font-weight: bold;");

                    Label statusLabel = new Label(user.isOnline() ? "Online" : "Offline");
                    statusLabel.setStyle("-fx-text-fill: " + (user.isOnline() ? ONLINE_COLOR : OFFLINE_COLOR) + "; -fx-font-size: 11px;");

                    userInfo.getChildren().addAll(nameLabel, statusLabel);

                    container.getChildren().addAll(avatarContainer, userInfo);

                    setGraphic(container);
                    setText(null);

                    // Add hover effect
                    container.setStyle("-fx-background-color: transparent;");
                    setOnMouseEntered(e -> container.setStyle("-fx-background-color: #F0F0F0; -fx-background-radius: 5;"));
                    setOnMouseExited(e -> container.setStyle("-fx-background-color: transparent;"));
                }
            }
        });

        // Create connection controls
        HBox connectionControls = new HBox(10);
        connectionControls.setPadding(new Insets(10));

        Button hostButton = new Button("Host Chat");
        hostButton.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold;");
        hostButton.setPrefWidth(115);

        Button joinButton = new Button("Join Chat");
        joinButton.setStyle("-fx-background-color: " + SECONDARY_COLOR + "; -fx-text-fill: " + TEXT_COLOR + ";");
        joinButton.setPrefWidth(115);

        Button refreshButton = new Button("Refresh 🔄");
        refreshButton.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: black;");
        refreshButton.setPrefWidth(240);
//        refreshButton.setOnAction(e -> {
//            User staticTestUser = new User("TestUser123", "192.168.1.250");
//            chatManager.addUser(staticTestUser);
//            updateUserList();
//        });
        refreshButton.setOnAction(e -> updateUserList());

        Button disconnectButton = new Button("Disconnect 🔌");
        disconnectButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        disconnectButton.setPrefWidth(240);

        disconnectButton.setOnAction(e -> {
            shutdown(); // Safely shut down server/client/chatbot
            Platform.exit(); // Closes the JavaFX app
        });


        connectionControls.getChildren().addAll(hostButton, joinButton);

        // Add components to the panel
        leftPanel.getChildren().addAll(header, searchField, userListView, connectionControls);
        VBox.setVgrow(userListView, Priority.ALWAYS);
        VBox.setMargin(refreshButton, new Insets(0, 10, 0, 10));
        leftPanel.getChildren().add(refreshButton);

        VBox.setMargin(disconnectButton, new Insets(0, 10, 10, 10));
        leftPanel.getChildren().add(disconnectButton);

        // Set up event handlers for connection buttons
        hostButton.setOnAction(e -> hostChat());
        joinButton.setOnAction(e -> joinChat());

        return leftPanel;
    }

    /**
     * Creates the center panel with the chat area and message input
     *
     * @return the center panel
     */
    private VBox createCenterPanel() {
        VBox centerPanel = new VBox(0);
        centerPanel.setPadding(new Insets(0));

        // Create chat header
        HBox chatHeader = new HBox(10);
        chatHeader.setPadding(new Insets(15));
        chatHeader.setAlignment(Pos.CENTER_LEFT);
        chatHeader.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");

        ImageView selectedUserAvatar = new ImageView(new Image(getClass().getResourceAsStream("/images/default_avatar.png")));
        selectedUserAvatar.setFitHeight(30);
        selectedUserAvatar.setFitWidth(30);

        // Make avatar circular
        Circle clip = new Circle(15, 15, 15);
        selectedUserAvatar.setClip(clip);

        Label selectedUserLabel = new Label("Select a contact");
        selectedUserLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        chatHeader.getChildren().addAll(selectedUserAvatar, selectedUserLabel);

        // Create chat messages area
        chatMessagesContainer = new VBox(10);
        chatMessagesContainer.setPadding(new Insets(15));
        chatMessagesContainer.setAlignment(Pos.TOP_LEFT);

        chatScrollPane = new ScrollPane(chatMessagesContainer);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScrollPane.setStyle("-fx-background-color: white; -fx-background: white;");
        chatScrollPane.setPrefHeight(Integer.MAX_VALUE);

        // Create message input area
        HBox inputArea = new HBox(10);
        inputArea.setPadding(new Insets(10, 15, 10, 15));
        inputArea.setAlignment(Pos.CENTER);
        inputArea.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E0E0E0; -fx-border-width: 1 0 0 0;");

        Button emojiButton = new Button("😊");
        emojiButton.setStyle("-fx-font-size: 16px; -fx-background-color: transparent;");

        messageInput = new TextField();
        messageInput.setPromptText("Type a message...");
        messageInput.setPadding(new Insets(10));
        messageInput.setStyle("-fx-background-radius: 20; -fx-border-radius: 20;");

        sendButton = new Button("Send");
        sendButton.setDefaultButton(true);
        sendButton.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold;");

        inputArea.getChildren().addAll(emojiButton, messageInput, sendButton);
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        // Add components to the panel
        centerPanel.getChildren().addAll(chatHeader, chatScrollPane, inputArea);
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);

        return centerPanel;
    }

    /**
     * Creates a CSS file for styling the application
     */
    private void createCssFile() {
        try {
            // Create directory for CSS
            File cssDir = new File("src/css");
            if (!cssDir.exists()) {
                cssDir.mkdirs();
            }

            // Create CSS file
            String css = "/* Messenger-style CSS */\n\n" +
                    ".root {\n" +
                    "    -fx-font-family: 'Segoe UI', Arial, sans-serif;\n" +
                    "}\n\n" +
                    ".button {\n" +
                    "    -fx-background-radius: 5;\n" +
                    "    -fx-border-radius: 5;\n" +
                    "    -fx-padding: 8 15 8 15;\n" +
                    "    -fx-cursor: hand;\n" +
                    "}\n\n" +
                    ".text-field {\n" +
                    "    -fx-background-radius: 20;\n" +
                    "    -fx-border-radius: 20;\n" +
                    "    -fx-padding: 8 15 8 15;\n" +
                    "}\n\n" +
                    ".list-view {\n" +
                    "    -fx-background-color: transparent;\n" +
                    "    -fx-border-width: 0;\n" +
                    "}\n\n" +
                    ".list-cell {\n" +
                    "    -fx-background-color: transparent;\n" +
                    "    -fx-padding: 5 0 5 0;\n" +
                    "}\n\n" +
                    ".list-cell:selected {\n" +
                    "    -fx-background-color: #E4E6EB;\n" +
                    "    -fx-background-radius: 5;\n" +
                    "}\n\n" +
                    ".scroll-pane {\n" +
                    "    -fx-background-color: white;\n" +
                    "    -fx-background: white;\n" +
                    "}\n\n" +
                    ".scroll-pane > .viewport {\n" +
                    "    -fx-background-color: white;\n" +
                    "}\n\n" +
                    ".message-bubble {\n" +
                    "    -fx-background-radius: 18;\n" +
                    "    -fx-padding: 10 15 10 15;\n" +
                    "}\n\n" +
                    ".sent-message {\n" +
                    "    -fx-background-color: " + PRIMARY_COLOR + ";\n" +
                    "    -fx-text-fill: white;\n" +
                    "}\n\n" +
                    ".received-message {\n" +
                    "    -fx-background-color: " + SECONDARY_COLOR + ";\n" +
                    "    -fx-text-fill: black;\n" +
                    "}\n\n" +
                    ".message-timestamp {\n" +
                    "    -fx-font-size: 10px;\n" +
                    "    -fx-text-fill: #999999;\n" +
                    "}\n";

            File cssFile = new File("src/css/messenger-style.css");
            java.nio.file.Files.write(cssFile.toPath(), css.getBytes());

            // Create images directory and add default avatar
            File imagesDir = new File("src/images");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }

            // Note: In a real application, you would include an actual avatar image file
            // For this example, we'll create a simple placeholder
            String avatarSvg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\" viewBox=\"0 0 100 100\">" +
                    "<circle cx=\"50\" cy=\"50\" r=\"50\" fill=\"#E1E1E1\"/>" +
                    "<circle cx=\"50\" cy=\"40\" r=\"15\" fill=\"#A0A0A0\"/>" +
                    "<path d=\"M25,85 C25,65 75,65 75,85\" fill=\"#A0A0A0\"/>" +
                    "</svg>";

            File avatarFile = new File("src/images/default_avatar.svg");
            java.nio.file.Files.write(avatarFile.toPath(), avatarSvg.getBytes());

        } catch (IOException e) {
            System.err.println("Error creating CSS file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sets up event handlers for UI components
     */
    private void setupEventHandlers() {
        // User selection
        userListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> handleUserSelection(newValue));

        // Send message
        sendButton.setOnAction(e -> handleSendMessage());
        messageInput.setOnAction(e -> handleSendMessage());

        // Window close
        primaryStage.setOnCloseRequest(e -> {
            shutdown();
        });
    }

    /**
     * Sets up the network connection
     */
    private void setupNetworkConnection() {
        // Show connection dialog
        showConnectionDialog();
    }

    /**
     * Shows a dialog to choose between hosting and joining a chat
     */
    private void showConnectionDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Chat Connection");
        alert.setHeaderText("Choose Connection Type");
        alert.setContentText("Would you like to host a new chat or join an existing one?");

        ButtonType hostButton = new ButtonType("Host Chat");
        ButtonType joinButton = new ButtonType("Join Chat");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(hostButton, joinButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == hostButton) {
                hostChat();
            } else if (result.get() == joinButton) {
                joinChat();
            }
        }
    }

    /**
     * Hosts a new chat server
     */
    private void hostChat() {
        try {
            // Default port
            int port = 8888;

            // Create and start the server
            server = new ChatServer(port, chatManager);
            server.start();

            // Start server discovery service
            serverDiscovery = new ServerDiscovery(port);
            serverDiscovery.startAdvertising();

            // Update status
            statusLabel.setText("Hosting chat on " + InetAddress.getLocalHost().getHostAddress() + ":" + port);
            statusLabel.setStyle("-fx-text-fill: " + ONLINE_COLOR + ";");

            // Update UI
            updateUserList();

        } catch (IOException e) {
            showError("Error hosting chat", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Joins an existing chat
     */
    private void joinChat() {
        try {
            // Discover servers
            java.util.List<ServerDiscovery.ServerInfo> servers = ServerDiscovery.discoverServers(2000);

            if (servers.isEmpty()) {
                // No servers found, ask for manual input
                TextInputDialog dialog = new TextInputDialog("localhost:8888");
                dialog.setTitle("Join Chat");
                dialog.setHeaderText("No servers found automatically");
                dialog.setContentText("Please enter server address and port (e.g., 192.168.1.100:8888):");

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    String[] parts = result.get().split(":");
                    if (parts.length == 2) {
                        String address = parts[0];
                        int port = Integer.parseInt(parts[1]);

                        connectToServer(address, port);
                    } else {
                        showError("Invalid input", "Please use format: address:port");
                    }
                }
            } else {
                // Servers found, show selection dialog
                ChoiceDialog<ServerDiscovery.ServerInfo> dialog = new ChoiceDialog<>(servers.get(0), servers);
                dialog.setTitle("Join Chat");
                dialog.setHeaderText("Select a server to join");
                dialog.setContentText("Available servers:");

                Optional<ServerDiscovery.ServerInfo> result = dialog.showAndWait();
                if (result.isPresent()) {
                    ServerDiscovery.ServerInfo serverInfo = result.get();
                    connectToServer(serverInfo.getAddress(), serverInfo.getPort());
                }
            }

        } catch (IOException e) {
            showError("Error joining chat", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Connects to a specific server
     *
     * @param address the server address
     * @param port    the server port
     */
    private void connectToServer(String address, int port) {
//        try {
//            // Create and connect the client
//            client = new ChatClient(address, port, chatManager);
//            client.connect();
//
//            // Update status
//            statusLabel.setText("Connected to " + address + ":" + port);
//            statusLabel.setStyle("-fx-text-fill: " + ONLINE_COLOR + ";");
//
//            // Update UI
//            updateUserList();
//
//        } catch (IOException e) {
//            showError("Error connecting to server", e.getMessage());
//            e.printStackTrace();
//        }

        try {
            // Create and connect the client
            client = new ChatClient(address, port, chatManager);
            client.connect();

            // Update status
            statusLabel.setText("Connected to " + address + ":" + port);

            // Update UI
            updateUserList();

        } catch (BindException e) {
            // Specific handling for address already in use
            showError("Connection Error",
                    "The network port is already in use. Please try a different port or restart the application.");
        } catch (IOException e) {
            showError("Error connecting to server", e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * Handles user selection from the list
     *
     * @param user the selected user
     */
    private void handleUserSelection(User user) {
        if (user == null) return;

        selectedUser = user;

        // Update chat header
        HBox chatHeader = (HBox) ((VBox) chatScrollPane.getParent()).getChildren().get(0);
        Label selectedUserLabel = (Label) chatHeader.getChildren().get(1);
        selectedUserLabel.setText(user.getUsername());

        // Update chat area
        updateChatArea();
    }

    /**
     * Handles sending a message
     */
    private void handleSendMessage() {
        String content = messageInput.getText().trim();
        if (content.isEmpty() || selectedUser == null) return;

        try {
            if (selectedUser.equals(chatBot.getBotUser())) {
                // Send message to chatbot
                Message userMessage = new Message(chatManager.getCurrentUser(), content);
                chatManager.receiveMessage(userMessage);

                // Get response from chatbot
                String response = chatBot.generateResponse(content);
                Message botMessage = new Message(chatBot.getBotUser(), response);
                chatManager.receiveMessage(botMessage);

            } else {
                // Send message to selected user or broadcast
                chatManager.sendMessage(content, selectedUser);
            }

            // Clear input field
            messageInput.clear();

            // Update chat area
            updateChatArea();

        } catch (Exception e) {
            showError("Error sending message", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates the user list
     */
    private void updateUserList() {
        userListView.getItems().clear();
        userListView.getItems().addAll(chatManager.getActiveUsers());

        System.out.println("all users:::");
        System.out.println(userListView.getUserData());


        // Select the first user if none selected
        if (selectedUser == null && !userListView.getItems().isEmpty()) {
            userListView.getSelectionModel().select(0);
            selectedUser = userListView.getItems().get(0);

            // Update chat header
            HBox chatHeader = (HBox) ((VBox) chatScrollPane.getParent()).getChildren().get(0);
            Label selectedUserLabel = (Label) chatHeader.getChildren().get(1);
            selectedUserLabel.setText(selectedUser.getUsername());
        }
    }


    /**
     * Updates the chat area with messages
     */
    private void updateChatArea() {
        chatMessagesContainer.getChildren().clear();

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        User currentUser = chatManager.getCurrentUser();

        for (Message message : chatManager.getMessageHistory()) {
            // Show all messages or only those to/from selected user
            if (selectedUser == null ||
                    message.getSender().equals(selectedUser) ||
                    message.getSender().equals(currentUser)) {

                // Create message bubble
                HBox messageRow = new HBox(10);
                messageRow.setPadding(new Insets(5, 15, 5, 15));

                Label messageContent = new Label(message.getContent());
                messageContent.setWrapText(true);
                messageContent.setMaxWidth(400);

                Label timestamp = new Label(message.getTimestamp().format(timeFormatter));
                timestamp.getStyleClass().add("message-timestamp");

                VBox messageBox = new VBox(5);
                messageBox.getChildren().addAll(messageContent, timestamp);

                if (message.getSender().equals(currentUser)) {
                    // Sent message (right-aligned, blue)
                    messageRow.setAlignment(Pos.CENTER_RIGHT);
                    messageBox.getStyleClass().addAll("message-bubble", "sent-message");
                    messageRow.getChildren().add(messageBox);
                } else {
                    // Received message (left-aligned, gray)
                    messageRow.setAlignment(Pos.CENTER_LEFT);
                    messageBox.getStyleClass().addAll("message-bubble", "received-message");

                    // Add avatar for received messages
                    StackPane avatarContainer = new StackPane();
                    ImageView avatarView = new ImageView(new Image(getClass().getResourceAsStream("/images/default_avatar.png")));
                    avatarView.setFitHeight(30);
                    avatarView.setFitWidth(30);

                    // Make avatar circular
                    Circle clip = new Circle(15, 15, 15);
                    avatarView.setClip(clip);

                    avatarContainer.getChildren().add(avatarView);

                    messageRow.getChildren().addAll(avatarContainer, messageBox);
                }

                chatMessagesContainer.getChildren().add(messageRow);
            }
        }

        // Scroll to bottom
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }

    /**
     * Shows an error dialog
     *
     * @param title   the dialog title
     * @param message the error message
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shuts down the application
     */
    private void shutdown() {
        // Stop network components
        if (server != null) {
            server.stop();
        }

        if (client != null) {
            client.disconnect();
        }

        if (serverDiscovery != null) {
            serverDiscovery.stopAdvertising();
        }

        // Shutdown chatbot
        if (chatBot != null) {
            chatBot.shutdown();
        }

        // Save chat history
        chatManager.saveHistory();
    }

    /**
     * Main method
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void onUserAdded(User user) {
        Platform.runLater(this::updateUserList);
    }

    @Override
    public void onUserRemoved(User user) {
        Platform.runLater(this::updateUserList);
    }

    @Override
    public void onMessageReceived(Message message) {
        Platform.runLater(this::updateChatArea);
    }

}
