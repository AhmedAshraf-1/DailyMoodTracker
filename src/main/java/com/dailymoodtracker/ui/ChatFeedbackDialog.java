package com.dailymoodtracker.ui;

import com.dailymoodtracker.model.ChatFeedback;
import com.dailymoodtracker.model.ChatMessage;
import com.dailymoodtracker.service.ChatTrainingService;
import com.dailymoodtracker.utils.AlertHelper;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Dialog for collecting user feedback about chatbot responses.
 * This feedback is used to improve future responses.
 */
public class ChatFeedbackDialog {
    
    private final Stage dialogStage;
    private final ChatMessage userMessage;
    private final ChatMessage botMessage;
    private final ChatTrainingService trainingService;
    
    /**
     * Create a new feedback dialog.
     * 
     * @param owner The owner window
     * @param userMessage The user's message
     * @param botMessage The bot's response message
     */
    public ChatFeedbackDialog(Window owner, ChatMessage userMessage, ChatMessage botMessage) {
        this.dialogStage = new Stage(StageStyle.UTILITY);
        this.userMessage = userMessage;
        this.botMessage = botMessage;
        this.trainingService = ChatTrainingService.getInstance();
        
        // Configure the dialog
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.setTitle("Response Feedback");
        dialogStage.setMinWidth(400);
        dialogStage.setMinHeight(300);
        
        // Set up the UI
        VBox layout = createLayout();
        Scene scene = new Scene(layout);
        dialogStage.setScene(scene);
    }
    
    /**
     * Show the dialog and wait for user input.
     */
    public void showAndWait() {
        dialogStage.showAndWait();
    }
    
    /**
     * Create the dialog layout.
     */
    private VBox createLayout() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        
        // Heading
        Label titleLabel = new Label("Help Improve the Chatbot");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Message display
        VBox messageBox = new VBox(5);
        
        Label userMessageLabel = new Label("Your message:");
        userMessageLabel.setStyle("-fx-font-weight: bold;");
        
        Label userMessageText = new Label(userMessage.getContent());
        userMessageText.setWrapText(true);
        userMessageText.setStyle("-fx-background-color: #e2f0ff; -fx-padding: 10; -fx-background-radius: 5;");
        
        Label botResponseLabel = new Label("Assistant's response:");
        botResponseLabel.setStyle("-fx-font-weight: bold;");
        
        Label botMessageText = new Label(botMessage.getContent());
        botMessageText.setWrapText(true);
        botMessageText.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-background-radius: 5;");
        
        messageBox.getChildren().addAll(userMessageLabel, userMessageText, botResponseLabel, botMessageText);
        
        // Feedback type selection
        Label feedbackTypeLabel = new Label("How was this response?");
        
        ComboBox<String> feedbackTypeCombo = new ComboBox<>();
        feedbackTypeCombo.getItems().addAll(
            "Helpful and appropriate",
            "Not helpful",
            "Not understanding my message",
            "Too generic",
            "Other (please specify)"
        );
        feedbackTypeCombo.setPromptText("Select feedback type");
        feedbackTypeCombo.setPrefWidth(Double.MAX_VALUE);
        
        // Comments area
        Label commentsLabel = new Label("Additional comments (optional):");
        
        TextArea commentsArea = new TextArea();
        commentsArea.setPromptText("Enter any additional feedback or suggestions here...");
        commentsArea.setPrefRowCount(4);
        VBox.setVgrow(commentsArea, Priority.ALWAYS);
        
        // Buttons
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> dialogStage.close());
        
        Button submitButton = new Button("Submit Feedback");
        submitButton.setDefaultButton(true);
        submitButton.setOnAction(e -> {
            if (feedbackTypeCombo.getValue() == null) {
                AlertHelper.showWarning("Required Field", "Please select a feedback type.");
                return;
            }
            
            submitFeedback(feedbackTypeCombo.getValue(), commentsArea.getText());
            dialogStage.close();
        });
        
        HBox buttonBox = new HBox(10, cancelButton, submitButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        // Add all components to the layout
        layout.getChildren().addAll(
            titleLabel,
            messageBox,
            feedbackTypeLabel,
            feedbackTypeCombo,
            commentsLabel,
            commentsArea,
            buttonBox
        );
        
        return layout;
    }
    
    /**
     * Submit the feedback to the training service.
     */
    private void submitFeedback(String feedbackType, String comments) {
        try {
            // Convert feedback type to enum
            ChatFeedback.FeedbackType type;
            if (feedbackType.startsWith("Helpful")) {
                type = ChatFeedback.FeedbackType.POSITIVE;
            } else if (feedbackType.startsWith("Not") || feedbackType.startsWith("Too")) {
                type = ChatFeedback.FeedbackType.NEGATIVE;
            } else {
                type = ChatFeedback.FeedbackType.SUGGESTION;
            }
            
            // Create feedback object
            ChatFeedback feedback = new ChatFeedback(
                userMessage.getContent(),
                botMessage.getContent(),
                type,
                comments,
                1 // Default user ID
            );
            
            // Add sentiment info if available
            if (userMessage.getSentiment() != null) {
                feedback.setSentimentType(userMessage.getSentiment().getDominantSentiment());
                feedback.setSpecificEmotion(userMessage.getSentiment().getSpecificEmotion());
            }
            
            // Submit to training service
            trainingService.addFeedback(feedback);
            
            // Show confirmation
            AlertHelper.showInformation("Feedback Submitted", 
                "Thank you for your feedback! It will help improve future responses.");
            
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to submit feedback: " + e.getMessage());
        }
    }
} 