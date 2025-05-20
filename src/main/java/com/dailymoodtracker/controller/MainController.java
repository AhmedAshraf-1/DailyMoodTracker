package com.dailymoodtracker.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.Node;
import javafx.scene.Parent;
import com.dailymoodtracker.model.MoodEntry;
import com.dailymoodtracker.model.MoodType;
import com.dailymoodtracker.model.User;
import com.dailymoodtracker.model.Achievement;
import com.dailymoodtracker.model.UserPreferences;
import com.dailymoodtracker.service.MoodService;
import com.dailymoodtracker.service.QuoteService;
import com.dailymoodtracker.service.ReminderService;
import com.dailymoodtracker.service.AchievementService;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Collections;
import com.dailymoodtracker.service.UserService;
import com.dailymoodtracker.service.PreferencesService;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;

public class MainController {
    @FXML private TextArea notesArea;
    @FXML private CheckBox activityExercise, activityWork, activitySocial, activityHobby;
    @FXML private Label quoteLabel;
    @FXML private GridPane moodGrid;
    @FXML private Button historyButton;
    @FXML private Button saveButton;
    @FXML private Label selectedMoodLabel;
    @FXML private BorderPane mainContainer;
    @FXML private FlowPane activitiesContainer;
    @FXML private VBox goalsContainer;
    @FXML private Button achievementsButton;
    @FXML private Button preferencesButton;

    private MoodType selectedMood = null;
    private final MoodService moodService;
    private final QuoteService quoteService;
    private final ReminderService reminderService;
    private final AchievementService achievementService;
    private User user;
    private final UserService userService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private PreferencesService preferencesService;
    private UserPreferences userPreferences;

    public MainController(User user) {
        this.user = user;
        this.userService = UserService.getInstance();
        this.moodService = new MoodService();
        this.quoteService = new QuoteService();
        this.reminderService = new ReminderService();
        this.achievementService = new AchievementService();
    }

    @FXML
    public void initialize() {
        if (user == null) {
            showError("No user logged in!");
            return;
        }
        setupUI();
        loadUserData();
        
        // Initialize preferences service
        preferencesService = PreferencesService.getInstance();
        userPreferences = preferencesService.getUserPreferences(user);
        
        // Apply user's theme preferences
        if (userPreferences != null) {
            preferencesService.applyTheme(mainContainer.getScene(), userPreferences);
        }
    }

    private void setupUI() {
        setupMoodButtons();
        setupStyles();
        loadMotivationalQuote();
        
        // Add entrance animations
        animateUIElements();
    }

    /**
     * Adds staggered entrance animations to major UI elements
     */
    private void animateUIElements() {
        // Get main sections from the scene
        Node[] elements = {
            quoteLabel,
            moodGrid.getParent(), // The VBox containing the mood section
            notesArea.getParent(), // The VBox containing the notes section
            activitiesContainer.getParent(), // The VBox containing activities
            goalsContainer.getParent(), // The VBox containing goals
            saveButton
        };

        // Apply staggered fade-in and slight zoom animations
        for (int i = 0; i < elements.length; i++) {
            Node element = elements[i];
            element.setOpacity(0); // Start invisible
            element.setScaleX(0.95);
            element.setScaleY(0.95);
            
            // Fade and scale in with staggered delay
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), element);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.millis(100 * i)); // Staggered timing
            
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), element);
            scaleIn.setFromX(0.95);
            scaleIn.setFromY(0.95);
            scaleIn.setToX(1);
            scaleIn.setToY(1);
            scaleIn.setDelay(Duration.millis(100 * i));
            
            fadeIn.play();
            scaleIn.play();
        }
        
        // Add subtle animation to the quote
        Timeline pulseTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(quoteLabel.scaleXProperty(), 1)),
            new KeyFrame(Duration.ZERO, new KeyValue(quoteLabel.scaleYProperty(), 1)),
            new KeyFrame(Duration.seconds(10), new KeyValue(quoteLabel.scaleXProperty(), 1.02)),
            new KeyFrame(Duration.seconds(10), new KeyValue(quoteLabel.scaleYProperty(), 1.02)),
            new KeyFrame(Duration.seconds(20), new KeyValue(quoteLabel.scaleXProperty(), 1))
        );
        pulseTimeline.setCycleCount(Timeline.INDEFINITE);
        pulseTimeline.setAutoReverse(true);
        pulseTimeline.play();
    }

    private void loadUserData() {
        updateUserInfo();
        checkAchievements();
    }

    private void setupMoodButtons() {
        moodGrid.setHgap(10);
        moodGrid.setVgap(10);
        moodGrid.setPadding(new Insets(10));

        // Clear any existing buttons
        moodGrid.getChildren().clear();

        for (MoodType mood : MoodType.values()) {
            Button button = new Button(mood.getEmoji());
            button.setId("moodButton" + mood.getLevel());
            button.getStyleClass().add("mood-button");
            button.getStyleClass().add("mood-button-" + mood.getLevel());
            
            // Add click animations and effects
            setupMoodButtonInteractions(button, mood);
            
            moodGrid.add(button, mood.getLevel() - 1, 0);
        }
    }
    
    /**
     * Adds interactive animations and effects to mood buttons
     */
    private void setupMoodButtonInteractions(Button button, MoodType mood) {
        button.setOnAction(event -> {
            // Reset all buttons first
            for (Node node : moodGrid.getChildren()) {
                if (node instanceof Button) {
                    node.getStyleClass().remove("selected");
                }
            }
            
            // Select current button with animation
            button.getStyleClass().add("selected");
            
            // Create selection animation
            ScaleTransition pulse = new ScaleTransition(Duration.millis(200), button);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.1);
            pulse.setToY(1.1);
            pulse.setCycleCount(2);
            pulse.setAutoReverse(true);
            
            // Create glow effect
            javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.6);
            button.setEffect(glow);
            
            Timeline fadeGlow = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.6)),
                new KeyFrame(Duration.millis(700), new KeyValue(glow.levelProperty(), 0.0))
            );
            
            // Play animations
            pulse.play();
            fadeGlow.play();
            
            // Update mood selection
            selectedMood = mood;
            selectedMoodLabel.setText("Selected mood: " + mood.getDescription());
            
            // Create a fade transition for the label
            FadeTransition labelFade = new FadeTransition(Duration.millis(300), selectedMoodLabel);
            labelFade.setFromValue(0.7);
            labelFade.setToValue(1.0);
            labelFade.play();
            
            // Log the mood
            logMood(event);
        });
        
        // Create hover effects
        button.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), button);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);
            scaleUp.play();
        });
        
        button.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), button);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
        });
    }

    private void setupStyles() {
        // Style the notes area
        notesArea.setStyle("-fx-background-color: #2A2A2A; -fx-text-fill: white; " +
                          "-fx-border-color: #404040; -fx-border-radius: 5px;");
        notesArea.setPromptText("How are you feeling today? (Optional)");
        
        // Style the activity checkboxes
        String checkboxStyle = "-fx-text-fill: white; -fx-font-size: 14px;";
        activityExercise.setStyle(checkboxStyle);
        activityWork.setStyle(checkboxStyle);
        activitySocial.setStyle(checkboxStyle);
        activityHobby.setStyle(checkboxStyle);
        
        // Style the save button
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                          "-fx-font-size: 14px; -fx-padding: 10px; -fx-background-radius: 5px;");
        saveButton.setOnMouseEntered(e -> saveButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; " +
                                                             "-fx-font-size: 14px; -fx-padding: 10px; -fx-background-radius: 5px;"));
        saveButton.setOnMouseExited(e -> saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                                                            "-fx-font-size: 14px; -fx-padding: 10px; -fx-background-radius: 5px;"));
        
        // Style the history button
        historyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                             "-fx-font-size: 14px; -fx-padding: 10px; -fx-background-radius: 5px;");
        historyButton.setOnMouseEntered(e -> historyButton.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; " +
                                                                   "-fx-font-size: 14px; -fx-padding: 10px; -fx-background-radius: 5px;"));
        historyButton.setOnMouseExited(e -> historyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                                                                  "-fx-font-size: 14px; -fx-padding: 10px; -fx-background-radius: 5px;"));
        
        // Style the quote label
        quoteLabel.setStyle("-fx-font-size: 16px; -fx-font-style: italic; -fx-text-fill: #BBBBBB;");
        quoteLabel.setWrapText(true);
    }

    @FXML
    private void showHistory(ActionEvent event) {
        List<MoodEntry> entries = moodService.getAllEntries();
        if (entries.isEmpty()) {
            showAlert("No History", "You haven't logged any moods yet!");
            return;
        }

        VBox historyBox = new VBox(15);
        historyBox.setPadding(new Insets(20));
        historyBox.setStyle("-fx-background-color: transparent;");

        for (MoodEntry entry : entries) {
            VBox entryBox = createHistoryEntryBox(entry);
            historyBox.getChildren().add(entryBox);
        }

        showHistoryDialog(historyBox);
    }

    private VBox createHistoryEntryBox(MoodEntry entry) {
        VBox entryBox = new VBox(10);
        entryBox.setPadding(new Insets(15));
        entryBox.getStyleClass().add("mood-entry-card");

        // Header with date and mood
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Mood emoji with background
        Label moodLabel = new Label(entry.getMoodEmoji());
        moodLabel.setStyle("-fx-font-size: 36px; -fx-min-width: 50px;");

        VBox detailsBox = new VBox(5);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(detailsBox, javafx.scene.layout.Priority.ALWAYS);

        // Date and mood description
        Label dateLabel = new Label(entry.getTimestamp().format(dateFormatter));
        dateLabel.getStyleClass().add("entry-date");

        Label moodDescLabel = new Label(entry.getMoodDescription());
        moodDescLabel.getStyleClass().add("entry-mood");

        detailsBox.getChildren().addAll(dateLabel, moodDescLabel);
        header.getChildren().addAll(moodLabel, detailsBox);
        entryBox.getChildren().add(header);

        // Notes section
        if (!entry.getNotes().isEmpty()) {
            Label notesLabel = new Label(entry.getNotes());
            notesLabel.getStyleClass().add("entry-notes");
            notesLabel.setWrapText(true);
            entryBox.getChildren().add(notesLabel);
        }

        // Activities section
        if (!entry.getActivities().isEmpty()) {
            Label activitiesHeader = new Label("Activities");
            activitiesHeader.getStyleClass().add("section-title");
            activitiesHeader.setStyle("-fx-font-size: 14px; -fx-padding: 5 0 0 0;");
            
            FlowPane activitiesPane = new FlowPane(10, 10);
            activitiesPane.setPrefWrapLength(500);

            for (String activity : entry.getActivities()) {
                Label activityLabel = new Label(activity);
                activityLabel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.07); -fx-padding: 5px 10px; " +
                                     "-fx-background-radius: 5px; -fx-text-fill: white;");
                activitiesPane.getChildren().add(activityLabel);
            }

            entryBox.getChildren().addAll(activitiesHeader, activitiesPane);
        }

        return entryBox;
    }

    private void showHistoryDialog(VBox content) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Mood History");
        
        // Create a header
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(25, 25, 15, 25));
        header.getStyleClass().add("history-header");
        
        Label titleLabel = new Label("Your Mood History");
        titleLabel.getStyleClass().add("history-title");
        
        Label subtitleLabel = new Label("Track your emotional journey over time");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e0e0e0;");
        
        header.getChildren().addAll(titleLabel, subtitleLabel);

        // Create main content area with scrolling
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("content-area");

        // Combine header and content
        VBox mainContainer = new VBox(header, scrollPane);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a1a2e, #16213e);");

        dialog.getDialogPane().setContent(mainContainer);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(700);
        dialog.getDialogPane().setPrefHeight(600);
        
        // Style the dialog
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a1a2e, #16213e);");
        dialogPane.getStylesheets().add("/styles/main.css");
        
        // Style the close button
        Button closeButton = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
        closeButton.getStyleClass().addAll("save-button");

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setMinWidth(600);
        stage.setMinHeight(500);

        dialog.showAndWait();
    }

    @FXML
    private void logMood(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        int moodLevel = Integer.parseInt(clickedButton.getId().replace("moodButton", ""));
        selectedMood = MoodType.fromLevel(moodLevel);
        
        // Get the color based on mood level
        String textColor;
        switch (moodLevel) {
            case 1: textColor = "#FF4D4D"; break; // Very Sad - Red
            case 2: textColor = "#FFA07A"; break; // Sad - Light Coral
            case 3: textColor = "#FFD700"; break; // Neutral - Gold
            case 4: textColor = "#98FB98"; break; // Happy - Pale Green
            case 5: textColor = "#32CD32"; break; // Very Happy - Lime Green
            default: textColor = "#FFFFFF"; break;
        }
        
        selectedMoodLabel.setText(selectedMood.getEmoji() + " " + selectedMood.getDescription());
        selectedMoodLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 16px; -fx-font-weight: bold;", textColor));
    }

    @FXML
    private void saveEntry(ActionEvent event) {
        if (selectedMood == null) {
            showAlert("Warning", "Please select a mood before saving!");
            return;
        }

        // Save to database
        List<String> activities = getSelectedActivities();
        MoodEntry entry = new MoodEntry(LocalDateTime.now(), selectedMood.getLevel(), 
                                      notesArea.getText(), activities);
        moodService.saveEntry(entry);
        
        // Create success animation
        playSuccessAnimation();
        
        // Clear form after animation completes
        Timeline clearFormTimeline = new Timeline(new KeyFrame(Duration.millis(1000), e -> clearForm()));
        clearFormTimeline.play();
    }
    
    /**
     * Plays a success animation after saving an entry
     */
    private void playSuccessAnimation() {
        // Create success overlay
        VBox overlay = new VBox();
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 20;");
        overlay.setPrefSize(300, 200);
        overlay.setMaxSize(300, 200);
        overlay.setOpacity(0);
        
        // Success checkmark
        Label checkmark = new Label("✓");
        checkmark.setStyle("-fx-text-fill: -fx-success; -fx-font-size: 64px; -fx-font-weight: bold;");
        
        // Success message
        Label message = new Label("Entry Saved!");
        message.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        overlay.getChildren().addAll(checkmark, message);
        
        // Add to main container temporarily
        StackPane overlayContainer = new StackPane();
        StackPane.setAlignment(overlay, Pos.CENTER);
        overlayContainer.getChildren().add(overlay);
        
        // Position it centered on screen
        Scene scene = mainContainer.getScene();
        overlayContainer.setPrefSize(scene.getWidth(), scene.getHeight());
        overlayContainer.setMouseTransparent(true);  // Allow clicks to pass through
        
        // Add to root
        BorderPane root = (BorderPane) scene.getRoot();
        root.getChildren().add(overlayContainer);
        BorderPane.setAlignment(overlayContainer, Pos.CENTER);
        
        // Create animations
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), overlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), overlay);
        scaleUp.setFromX(0.5);
        scaleUp.setFromY(0.5);
        scaleUp.setToX(1);
        scaleUp.setToY(1);
        
        ScaleTransition bounce = new ScaleTransition(Duration.millis(150), checkmark);
        bounce.setFromX(1.0);
        bounce.setFromY(1.0);
        bounce.setToX(1.2);
        bounce.setToY(1.2);
        bounce.setCycleCount(2);
        bounce.setAutoReverse(true);
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), overlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.millis(1000));
        
        // Remove overlay when done
        fadeOut.setOnFinished(e -> root.getChildren().remove(overlayContainer));
        
        // Play the animations in sequence
        fadeIn.play();
        scaleUp.play();
        
        // Play bounce after fade-in
        fadeIn.setOnFinished(e -> bounce.play());
        
        // Play fade-out after bounce
        bounce.setOnFinished(e -> fadeOut.play());
    }

    private List<String> getSelectedActivities() {
        List<String> activities = new ArrayList<>();
        
        // Default activities
        if (activityExercise.isSelected()) activities.add("Exercise");
        if (activityWork.isSelected()) activities.add("Work");
        if (activitySocial.isSelected()) activities.add("Social");
        if (activityHobby.isSelected()) activities.add("Hobby");
        
        // Custom activities
        for (Node node : activitiesContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox activityItem = (HBox) node;
                for (Node child : activityItem.getChildren()) {
                    if (child instanceof CheckBox) {
                        CheckBox checkbox = (CheckBox) child;
                        if (checkbox.isSelected()) {
                            activities.add(checkbox.getText());
                        }
                    }
                }
            }
        }
        
        return activities;
    }

    private void clearForm() {
        notesArea.clear();
        activityExercise.setSelected(false);
        activityWork.setSelected(false);
        activitySocial.setSelected(false);
        activityHobby.setSelected(false);
        selectedMood = null;
        selectedMoodLabel.setText("");
        selectedMoodLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;"); // Reset label style
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadMotivationalQuote() {
        String[] quotes = {
            "Every day is a new beginning.",
            "Your attitude determines your direction.",
            "Make today amazing!",
            "Small progress is still progress.",
            "You've got this!"
        };
        quoteLabel.setText(quotes[(int) (Math.random() * quotes.length)]);
    }

    @FXML
    private void openRemindersDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReminderDialog.fxml"));
            loader.setControllerFactory(param -> new ReminderDialogController(reminderService, user.getId()));
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Manage Reminders");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(mainContainer.getScene().getWindow());
            
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            
            // Set dialog style and behavior
            dialogStage.setScene(scene);
            dialogStage.setMinWidth(700);
            dialogStage.setMinHeight(600);
            
            // Add entry animation
            dialogStage.setOnShown(event -> {
                Node root = scene.getRoot();
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            
            dialogStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error opening reminders dialog", e.getMessage());
        }
    }

    @FXML
    private void addCustomActivity() {
        showInputDialog("Add Custom Activity", "Enter Activity Name", "Activity name").ifPresent(activityName -> {
            if (!activityName.trim().isEmpty()) {
                HBox activityItem = createItemWithCheckbox(activityName, "activity-checkbox");
                activitiesContainer.getChildren().add(activityItem);
            }
        });
    }

    @FXML
    private void showGoalDialog() {
        showInputDialog("Add Daily Goal", "Enter Your Goal", "Enter your goal").ifPresent(goal -> {
            if (!goal.trim().isEmpty()) {
                HBox goalItem = createItemWithCheckbox(goal, "goal-item");
                goalsContainer.getChildren().add(goalItem);
            }
        });
    }

    private HBox createItemWithCheckbox(String text, String styleClass) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add(styleClass);
        item.setPadding(new Insets(5, 10, 5, 10));
        item.setStyle("-fx-background-color: #2A2A2A; -fx-background-radius: 5px;");

        CheckBox checkbox = new CheckBox(text.trim());
        checkbox.getStyleClass().add(styleClass);
        checkbox.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Button deleteButton = new Button("×");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #FF4D4D; " +
            "-fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0 5 0 5;"
        );
        deleteButton.setOnMouseEntered(e -> deleteButton.setStyle(
            "-fx-background-color: #FF4D4D; -fx-text-fill: white; " +
            "-fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0 5 0 5;"
        ));
        deleteButton.setOnMouseExited(e -> deleteButton.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #FF4D4D; " +
            "-fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0 5 0 5;"
        ));
        deleteButton.setOnAction(e -> ((Pane)item.getParent()).getChildren().remove(item));

        // Make the HBox take full width
        item.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(checkbox, javafx.scene.layout.Priority.ALWAYS);

        item.getChildren().addAll(checkbox, deleteButton);
        return item;
    }

    private Optional<String> showInputDialog(String title, String header, String prompt) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField inputField = new TextField();
        inputField.setPromptText(prompt);
        grid.add(new Label(title + ":"), 0, 0);
        grid.add(inputField, 1, 0);

        Node confirmButton = dialog.getDialogPane().lookupButton(addButton);
        confirmButton.setDisable(true);
        inputField.textProperty().addListener((observable, oldValue, newValue) -> 
            confirmButton.setDisable(newValue.trim().isEmpty()));

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> 
            dialogButton == addButton ? inputField.getText() : null);

        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        return dialog.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #2d2d2d;");
        
        // Style the buttons
        dialogPane.getButtonTypes().stream()
            .map(buttonType -> (Button) dialogPane.lookupButton(buttonType))
            .forEach(button -> button.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 8px 20px; -fx-background-radius: 4px;"
            ));
        
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #2d2d2d;");
        
        // Style the buttons
        dialogPane.getButtonTypes().stream()
            .map(buttonType -> (Button) dialogPane.lookupButton(buttonType))
            .forEach(button -> button.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 8px 20px; -fx-background-radius: 4px;"
            ));
        
        alert.showAndWait();
    }

    private void updateUserInfo() {
        if (user != null && quoteLabel != null) {
            String welcomeMessage = "Welcome back, " + user.getUsername() + "!\n";
            quoteLabel.setText(welcomeMessage + quoteLabel.getText());
        }
    }

    private void checkAchievements() {
        if (user != null) {
            // Check existing achievements
            achievementService.checkMoodStreak(user);
            achievementService.checkGoalCompletion(user);
            achievementService.checkActivityVariety(user);
            
            // Check new achievements
            checkMoodConsistency();
            checkTimeOfDayVariety();
            checkNotesLength();
            checkWeeklyProgress();
        }
    }

    private void checkMoodConsistency() {
        List<MoodEntry> recentMoods = moodService.getRecentEntries(user, 7);
        if (recentMoods.size() >= 7) {
            boolean isConsistent = true;
            int firstMoodLevel = recentMoods.get(0).getMoodLevel();
            
            for (MoodEntry entry : recentMoods) {
                if (Math.abs(entry.getMoodLevel() - firstMoodLevel) > 1) {
                    isConsistent = false;
                    break;
                }
            }
            
            if (isConsistent) {
                Achievement achievement = new Achievement(
                    "Mood Stability",
                    "Maintained consistent mood levels for a week",
                    LocalDateTime.now()
                );
                if (achievementService.addAchievement(user, achievement)) {
                    showAchievementNotification(achievement);
                }
            }
        }
    }

    private void checkTimeOfDayVariety() {
        List<MoodEntry> entries = moodService.getAllEntries();
        Set<Integer> uniqueHours = entries.stream()
            .map(entry -> entry.getTimestamp().getHour())
            .collect(Collectors.toSet());
            
        if (uniqueHours.size() >= 4) {
            Achievement achievement = new Achievement(
                "Time Explorer",
                "Logged moods at 4 different times of day",
                LocalDateTime.now()
            );
            if (achievementService.addAchievement(user, achievement)) {
                showAchievementNotification(achievement);
            }
        }
    }

    private void checkNotesLength() {
        List<MoodEntry> entries = moodService.getAllEntries();
        boolean hasDetailedNotes = entries.stream()
            .anyMatch(entry -> entry.getNotes().length() >= 100);
            
        if (hasDetailedNotes) {
            Achievement achievement = new Achievement(
                "Detailed Observer",
                "Wrote a detailed mood entry with 100+ characters",
                LocalDateTime.now()
            );
            if (achievementService.addAchievement(user, achievement)) {
                showAchievementNotification(achievement);
            }
        }
    }

    private void checkWeeklyProgress() {
        List<MoodEntry> weekEntries = moodService.getRecentEntries(user, 7);
        double averageMood = weekEntries.stream()
            .mapToInt(MoodEntry::getMoodLevel)
            .average()
            .orElse(0);
            
        if (averageMood >= 4.0 && weekEntries.size() >= 5) {
            Achievement achievement = new Achievement(
                "Weekly Wellness",
                "Maintained positive moods for a week with regular tracking",
                LocalDateTime.now()
            );
            if (achievementService.addAchievement(user, achievement)) {
                showAchievementNotification(achievement);
            }
        }
    }

    @FXML
    private void showAchievements() {
        if (user == null) return;

        VBox achievementsBox = new VBox(15);
        achievementsBox.setPadding(new Insets(10));
        achievementsBox.setStyle("-fx-background-color: transparent;");
        achievementsBox.setAlignment(Pos.TOP_CENTER);

        List<Achievement> achievements = achievementService.getUnlockedAchievements(user);
        
        if (achievements.isEmpty()) {
            // Create an empty state view with the same style
            VBox emptyStateBox = new VBox(15);
            emptyStateBox.setAlignment(Pos.CENTER);
            emptyStateBox.setPadding(new Insets(50, 30, 50, 30));
            emptyStateBox.setStyle("-fx-background-color: linear-gradient(to bottom right, #2d2d3a30, #1a1a2430); " + 
                                 "-fx-background-radius: 15px; " +
                                 "-fx-border-width: 1px; " +
                                 "-fx-border-color: rgba(93, 95, 239, 0.2); " +
                                 "-fx-border-radius: 15px;");
            
            // Star icon
            Label starIcon = new Label("⭐");
            starIcon.setStyle("-fx-font-size: 64px; -fx-text-fill: #5D5FEF;");
            
            Label emptyTitle = new Label("No Achievements Yet");
            emptyTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
            
            Label emptyDesc = new Label("Track your moods and complete activities to unlock achievements!");
            emptyDesc.setStyle("-fx-font-size: 16px; -fx-text-fill: #a1a1aa; -fx-text-alignment: center;");
            emptyDesc.setWrapText(true);
            emptyDesc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            
            Button testButton = new Button("Test Achievements");
            testButton.setPrefWidth(200);
            testButton.setStyle("-fx-background-color: #4e71feff; -fx-text-fill: white; " +
                             "-fx-font-size: 14px; -fx-font-weight: bold; " +
                             "-fx-padding: 10px 25px; -fx-background-radius: 8px;");
            testButton.setOnAction(e -> testAchievements());
            
            emptyStateBox.getChildren().addAll(starIcon, emptyTitle, emptyDesc, new Separator(), testButton);
            achievementsBox.getChildren().add(emptyStateBox);
        } else {
            // Add all unlocked achievements
            for (Achievement achievement : achievements) {
                HBox achievementItem = createAchievementItem(achievement);
                achievementsBox.getChildren().add(achievementItem);
            }
        }

        showAchievementsDialog(achievementsBox);
    }

    private HBox createAchievementItem(Achievement achievement) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(20));
        // Apply the same gradient background as the popup
        item.setStyle("-fx-background-color: linear-gradient(to bottom right, #2d2d3a, #1a1a24); " + 
                     "-fx-background-radius: 15px; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 2); " + 
                     "-fx-border-width: 1px; " +
                     "-fx-border-color: rgba(93, 95, 239, 0.3); " +
                     "-fx-border-radius: 15px;");

        // Achievement icon with glow effect
        Label iconLabel = new Label(getAchievementIcon(achievement.getName()));
        iconLabel.setStyle("-fx-font-size: 36px; -fx-min-width: 50px;");
        javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.5);
        iconLabel.setEffect(glow);

        // Achievement details in a VBox
        VBox detailsBox = new VBox(8);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(detailsBox, javafx.scene.layout.Priority.ALWAYS);

        // Achievement name with better styling
        Label nameLabel = new Label(achievement.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white; " +
                          "-fx-effect: dropshadow(gaussian, rgba(93, 95, 239, 0.8), 2, 0, 0, 0);");

        // Achievement description
        Label descLabel = new Label(achievement.getDescription());
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #a1a1aa;");
        descLabel.setWrapText(true);

        // Unlocked date with trophy icon
        Label dateLabel = new Label("🏆 Unlocked: " + achievement.getDateAchieved().format(dateFormatter));
        dateLabel.setStyle("-fx-text-fill: #8a8a8a; -fx-font-size: 12px; -fx-font-style: italic;");

        detailsBox.getChildren().addAll(nameLabel, descLabel, dateLabel);
        item.getChildren().addAll(iconLabel, detailsBox);

        // Add hover effect with glow animation
        item.setOnMouseEntered(e -> {
            item.setStyle("-fx-background-color: linear-gradient(to bottom right, #33334d, #1f1f2e); " + 
                         "-fx-background-radius: 15px; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 12, 0, 0, 2); " + 
                         "-fx-border-width: 1px; " +
                         "-fx-border-color: rgba(93, 95, 239, 0.5); " +
                         "-fx-border-radius: 15px;");
            
            // Enhance glow on hover
            Timeline enhanceGlow = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.5)),
                new KeyFrame(Duration.millis(300), new KeyValue(glow.levelProperty(), 0.8))
            );
            enhanceGlow.play();
        });
        
        item.setOnMouseExited(e -> {
            item.setStyle("-fx-background-color: linear-gradient(to bottom right, #2d2d3a, #1a1a24); " + 
                         "-fx-background-radius: 15px; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 2); " + 
                         "-fx-border-width: 1px; " +
                         "-fx-border-color: rgba(93, 95, 239, 0.3); " +
                         "-fx-border-radius: 15px;");
            
            // Reduce glow on exit
            Timeline reduceGlow = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.8)),
                new KeyFrame(Duration.millis(300), new KeyValue(glow.levelProperty(), 0.5))
            );
            reduceGlow.play();
        });

        return item;
    }

    private String getAchievementIcon(String achievementName) {
        return switch (achievementName) {
            case "Mood Stability" -> "🎯";
            case "Time Explorer" -> "⏰";
            case "Detailed Observer" -> "📝";
            case "Weekly Wellness" -> "🌟";
            case "Goal Achiever" -> "🎯";
            case "Activity Master" -> "🏃";
            default -> "🏆";
        };
    }

    private void showAchievementsDialog(VBox content) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Achievements");
        
        // Create a header with same gradient style as popup
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 20, 20, 20));
        header.setStyle("-fx-background-color: linear-gradient(to bottom right, #2d2d3a, #1a1a24);");
        
        // Add trophy icon with glow
        Label trophyLabel = new Label("🏆");
        trophyLabel.setStyle("-fx-font-size: 48px;");
        javafx.scene.effect.Glow headerGlow = new javafx.scene.effect.Glow(0.6);
        trophyLabel.setEffect(headerGlow);
        
        // Pulsing animation for the trophy
        Timeline pulseTrophy = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(headerGlow.levelProperty(), 0.6)),
            new KeyFrame(Duration.millis(1500), new KeyValue(headerGlow.levelProperty(), 0.4)),
            new KeyFrame(Duration.millis(3000), new KeyValue(headerGlow.levelProperty(), 0.6))
        );
        pulseTrophy.setCycleCount(Timeline.INDEFINITE);
        pulseTrophy.play();
        
        // Title with gradient text similar to popup
        Label titleLabel = new Label("Your Achievements");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; " +
                          "-fx-text-fill: linear-gradient(to right, #5D5FEF, #17C3B2); " +
                          "-fx-effect: dropshadow(gaussian, rgba(93, 95, 239, 0.4), 3, 0, 0, 0);");
        
        Label subtitleLabel = new Label("Keep tracking your moods to unlock more achievements!");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #a1a1aa;");
        
        header.getChildren().addAll(trophyLabel, titleLabel, subtitleLabel);

        // Create main content area with scrolling
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        // Style the scroll bar
        scrollPane.getStyleClass().add("achievement-scroll");
        
        // Style the content
        content.setStyle("-fx-background-color: transparent;");
        content.setSpacing(15);
        content.setPadding(new Insets(10, 15, 10, 15));

        // Combine header and content in a container with matching background
        VBox mainContainer = new VBox(header, scrollPane);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a1a24, #121218);");

        dialog.getDialogPane().setContent(mainContainer);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(600);
        
        // Style the dialog
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a1a24, #121218);");
        
        // Create custom close button styled like the "AWESOME!" button in the popup
        Button closeButton = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
        closeButton.setText("Close");
        closeButton.setPrefWidth(150);
        closeButton.setStyle("-fx-background-color: #4e71feff; -fx-text-fill: white; " +
                           "-fx-font-size: 14px; -fx-font-weight: bold; " +
                           "-fx-padding: 10px 25px; -fx-background-radius: 8px;");
        
        // Add hover effect
        closeButton.setOnMouseEntered(e -> 
            closeButton.setStyle("-fx-background-color: #5d80ffff; -fx-text-fill: white; " +
                               "-fx-font-size: 14px; -fx-font-weight: bold; " +
                               "-fx-padding: 10px 25px; -fx-background-radius: 8px;")
        );
        
        closeButton.setOnMouseExited(e -> 
            closeButton.setStyle("-fx-background-color: #4e71feff; -fx-text-fill: white; " +
                               "-fx-font-size: 14px; -fx-font-weight: bold; " +
                               "-fx-padding: 10px 25px; -fx-background-radius: 8px;")
        );

        // Set stage properties
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setMinWidth(550);
        stage.setMinHeight(600);

        dialog.showAndWait();
    }

    private void showAchievementNotification(Achievement achievement) {
        // Create a custom dialog stage
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.NONE); // Non-modal
        dialogStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        dialogStage.setResizable(false);
        
        // Create achievement container
        VBox achievementBox = new VBox(10);
        achievementBox.setAlignment(Pos.CENTER);
        achievementBox.setPadding(new Insets(30));
        achievementBox.setMaxWidth(400);
        achievementBox.setMaxHeight(300);
        achievementBox.setStyle("-fx-background-color: linear-gradient(to bottom right, #2d2d3a, #1a1a24); " + 
                              "-fx-background-radius: 15px; " +
                              "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0, 0, 2); " + 
                              "-fx-border-width: 1px; " +
                              "-fx-border-color: rgba(93, 95, 239, 0.3); " +
                              "-fx-border-radius: 15px;");
        
        // Trophy icon with glow effect
        Label trophyLabel = new Label(getAchievementIcon(achievement.getName()));
        trophyLabel.setStyle("-fx-font-size: 64px;");
        javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.8);
        trophyLabel.setEffect(glow);
        
        // Achievement unlocked banner
        Label unlockedLabel = new Label("ACHIEVEMENT UNLOCKED!");
        unlockedLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; " +
                              "-fx-text-fill: linear-gradient(to right, #5D5FEF, #17C3B2); " +
                              "-fx-padding: 0 0 5 0;");
        
        // Achievement title with outline effect
        Label nameLabel = new Label(achievement.getName());
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white; " +
                          "-fx-effect: dropshadow(gaussian, rgba(93, 95, 239, 0.8), 3, 0, 0, 0);");
        
        // Achievement description
        Label descLabel = new Label(achievement.getDescription());
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #a1a1aa;");
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        // Button to close the dialog
        Button closeButton = new Button("AWESOME!");
        closeButton.getStyleClass().add("save-button");
        closeButton.setPrefWidth(150);
        closeButton.setOnAction(e -> dialogStage.close());
        
        // Add all components to the VBox
        Region spacer = new Region();
        spacer.setMinHeight(10);
        
        achievementBox.getChildren().addAll(
            trophyLabel, 
            unlockedLabel,
            nameLabel, 
            new Separator(), 
            descLabel,
            spacer,
            closeButton
        );
        
        // Create scene with transparent background
        Scene scene = new Scene(achievementBox);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        
        // Set the scene on the dialog stage
        dialogStage.setScene(scene);
        
        // Position the dialog in the top-right corner of the main window
        Stage mainStage = (Stage) mainContainer.getScene().getWindow();
        dialogStage.setX(mainStage.getX() + mainStage.getWidth() - 420);
        dialogStage.setY(mainStage.getY() + 50);
        
        // Show the dialog with animations
        dialogStage.show();
        
        // Create entrance animations
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), achievementBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(500), achievementBox);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        
        // Pulse animation for trophy
        Timeline pulseTrophy = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.8)),
            new KeyFrame(Duration.millis(1000), new KeyValue(glow.levelProperty(), 0.4)),
            new KeyFrame(Duration.millis(2000), new KeyValue(glow.levelProperty(), 0.8))
        );
        pulseTrophy.setCycleCount(5);
        
        // Play animations
        fadeIn.play();
        scaleIn.play();
        pulseTrophy.play();
        
        // Close automatically after 10 seconds if user doesn't interact
        Timeline autoClose = new Timeline(new KeyFrame(Duration.seconds(10), e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), achievementBox);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> dialogStage.close());
            fadeOut.play();
        }));
        autoClose.play();
        
        // Cancel auto-close if user interacts with the dialog
        closeButton.setOnMouseEntered(e -> autoClose.stop());
    }

    @FXML
    private void testAchievements() {
        if (user == null) return;

        // Test 1: Mood Stability (7 days of consistent moods)
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 7; i++) {
            MoodEntry entry = new MoodEntry(
                now.minusDays(i),
                4, // Happy mood
                "Test mood entry for stability",
                Arrays.asList("Reading", "Exercise")
            );
            moodService.saveEntry(entry);
        }
        checkMoodConsistency();

        // Test 2: Time Explorer (different times of day)
        MoodEntry morningEntry = new MoodEntry(
            now.withHour(9),
            3,
            "Morning entry",
            Collections.singletonList("Breakfast")
        );
        MoodEntry afternoonEntry = new MoodEntry(
            now.withHour(14),
            4,
            "Afternoon entry",
            Collections.singletonList("Work")
        );
        MoodEntry eveningEntry = new MoodEntry(
            now.withHour(19),
            5,
            "Evening entry",
            Collections.singletonList("Reading")
        );
        MoodEntry nightEntry = new MoodEntry(
            now.withHour(23),
            3,
            "Night entry",
            Collections.singletonList("Relaxing")
        );
        
        moodService.saveEntry(morningEntry);
        moodService.saveEntry(afternoonEntry);
        moodService.saveEntry(eveningEntry);
        moodService.saveEntry(nightEntry);
        checkTimeOfDayVariety();

        // Test 3: Detailed Observer (long note)
        MoodEntry detailedEntry = new MoodEntry(
            now,
            5,
            "This is a very detailed mood entry that contains more than 100 characters. " +
            "I'm feeling great today because I accomplished many tasks and had quality time with family and friends.",
            Arrays.asList("Family", "Work", "Exercise")
        );
        moodService.saveEntry(detailedEntry);
        checkNotesLength();

        // Test 4: Weekly Progress (positive moods for a week)
        for (int i = 0; i < 5; i++) {
            MoodEntry entry = new MoodEntry(
                now.minusDays(i),
                5, // Very Happy mood
                "Feeling great!",
                Collections.singletonList("Productive Day")
            );
            moodService.saveEntry(entry);
        }
        checkWeeklyProgress();

        // Show success message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Test Achievements");
        alert.setHeaderText("Achievement Tests Completed");
        alert.setContentText("Test achievements have been created. You can view them in the Achievements section.");
        alert.showAndWait();

        // Show the achievements dialog after adding test data
        showAchievements();
    }

    /**
     * Opens the preferences view where the user can customize the app.
     */
    @FXML
    private void showPreferences() {
        try {
            // Create preferences controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PreferencesView.fxml"));
            Parent root = loader.load();
            
            // Initialize controller with user data
            PreferencesController preferencesController = loader.getController();
            preferencesController.initData(user);
            
            // Create and show new scene
            Scene scene = new Scene(root);
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            stage.setScene(scene);
            
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not open preferences");
            alert.setContentText("An error occurred while trying to open preferences: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Gets the preferences service.
     */
    public PreferencesService getPreferencesService() {
        return preferencesService;
    }

    /**
     * Gets the current user's preferences.
     */
    public UserPreferences getUserPreferences() {
        return userPreferences;
    }

    /**
     * Open the AI chatbot view.
     */
    @FXML
    private void openChatbot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChatbotView.fxml"));
            Parent chatbotView = loader.load();
            
            // Get controller and set user ID
            ChatbotController controller = loader.getController();
            controller.setCurrentUserId(getCurrentUserId());
            
            Stage chatbotStage = new Stage();
            chatbotStage.setTitle("Mood Assistant - AI Chatbot");
            chatbotStage.setScene(new Scene(chatbotView));
            chatbotStage.setMinWidth(800);
            chatbotStage.setMinHeight(600);
            chatbotStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Chatbot Error", "Could not open chatbot: " + e.getMessage());
        }
    }
    
    /**
     * Get the current user ID.
     * 
     * @return the current user ID
     */
    private int getCurrentUserId() {
        // In a real application, you would get this from a session or authentication service
        return 1;
    }
    
    /**
     * Logs the user out and returns to the login screen.
     */
    @FXML
    private void logout() {
        try {
            // Create fade-out animation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), mainContainer);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            
            fadeOut.setOnFinished(e -> {
                try {
                    // Clear user data
                    user = null;
                    
                    // Load the login view
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
                    Scene scene = new Scene(loader.load());
                    Stage stage = (Stage) mainContainer.getScene().getWindow();
                    
                    // Apply the scene to the stage
                    stage.setScene(scene);
                    
                    // Create fade-in animation for the login screen
                    Parent root = scene.getRoot();
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    fadeIn.play();
                    
                } catch (IOException ex) {
                    showError("Error", "Could not load login view: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
            
            // Start the fade-out animation
            fadeOut.play();
            
        } catch (Exception ex) {
            showError("Error", "An error occurred during logout: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
} 