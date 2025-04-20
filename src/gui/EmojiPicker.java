package gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * A custom dialog for displaying emoji selection.
 * This class provides a simple emoji picker for the chat application.
 */
public class EmojiPicker extends Stage {
    private final TextField targetTextField;

    /**
     * Constructor with target text field
     *
     * @param targetTextField the text field to insert emojis into
     */
    public EmojiPicker(TextField targetTextField) {
        this.targetTextField = targetTextField;

        // Configure stage
        setTitle("Select Emoji");
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setWidth(300);
        setHeight(400);

        // Create main container
        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(15));
        mainContainer.setStyle("-fx-background-color: white;");

        // Create emoji grid
        GridPane emojiGrid = new GridPane();
        emojiGrid.setHgap(10);
        emojiGrid.setVgap(10);
        emojiGrid.setPadding(new Insets(5));

        // Common emojis
        String[] emojis = {
                "😊", "😂", "😍", "😎", "😢", "😡", "👍", "👎",
                "❤️", "🎉", "🔥", "💯", "🙏", "👋", "🤔", "😴",
                "🥳", "😷", "🤣", "😭", "😘", "🤗", "👀", "💪",
                "👏", "🙌", "👌", "✌️", "🤞", "🤙", "👻", "🎂",
                "🍕", "🍔", "🍦", "🍷", "☕", "🍺", "🎮", "🎵",
                "🚗", "✈️", "🏠", "💻", "📱", "⏰", "💰", "🎁"
        };

        int col = 0;
        int row = 0;

        for (String emoji : emojis) {
            Button emojiButton = new Button(emoji);
            emojiButton.setStyle("-fx-font-size: 20px; -fx-background-color: transparent;");
            emojiButton.setPrefSize(50, 50);

            emojiButton.setOnAction(e -> {
                insertEmoji(emoji);
                close();
            });

            // Add hover effect
            emojiButton.setOnMouseEntered(e ->
                    emojiButton.setStyle("-fx-font-size: 20px; -fx-background-color: #F0F0F0; -fx-background-radius: 5;"));
            emojiButton.setOnMouseExited(e ->
                    emojiButton.setStyle("-fx-font-size: 20px; -fx-background-color: transparent;"));

            emojiGrid.add(emojiButton, col, row);

            col++;
            if (col > 5) {
                col = 0;
                row++;
            }
        }

        // Create scroll pane for emoji grid
        ScrollPane scrollPane = new ScrollPane(emojiGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-background: white;");

        // Add to main container
        mainContainer.getChildren().add(scrollPane);

        // Create scene
        Scene scene = new Scene(mainContainer);
        setScene(scene);
    }

    /**
     * Inserts an emoji into the target text field
     *
     * @param emoji the emoji to insert
     */
    private void insertEmoji(String emoji) {
        int caretPosition = targetTextField.getCaretPosition();
        String currentText = targetTextField.getText();

        String newText = currentText.substring(0, caretPosition) + emoji + currentText.substring(caretPosition);
        targetTextField.setText(newText);
        targetTextField.positionCaret(caretPosition + emoji.length());
        targetTextField.requestFocus();
    }
}
