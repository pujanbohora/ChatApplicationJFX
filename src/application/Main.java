package application;

import gui.MessengerApp;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main application launcher class.
 * This class serves as the entry point for the chat application.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Create and start the messenger application
            MessengerApp messengerApp = new MessengerApp();
            messengerApp.start(primaryStage);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Main method - entry point of the application
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
