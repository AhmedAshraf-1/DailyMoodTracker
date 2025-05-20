package com.dailymoodtracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import com.dailymoodtracker.service.ReminderService;
import com.dailymoodtracker.model.Reminder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.effect.ColorAdjust;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;

public class ReminderDialogController {
    @FXML private VBox mainContainer;
    @FXML private TableView<Reminder> reminderTable;
    @FXML private TableColumn<Reminder, LocalTime> timeColumn;
    @FXML private TableColumn<Reminder, String> messageColumn;
    @FXML private TableColumn<Reminder, String> frequencyColumn;
    @FXML private TableColumn<Reminder, Boolean> enabledColumn;
    @FXML private TableColumn<Reminder, Void> actionsColumn;
    @FXML private ComboBox<String> hourComboBox;
    @FXML private ComboBox<String> minuteComboBox;
    @FXML private ComboBox<String> frequencyComboBox;
    @FXML private TextArea messageArea;
    @FXML private Button addButton;
    @FXML private Button closeButton;
    @FXML private Label reminderIconLabel;
    @FXML private VBox headerPane;
    @FXML private ScrollPane contentScrollPane;

    private final ReminderService reminderService;
    private final int userId;

    public ReminderDialogController(ReminderService reminderService, int userId) {
        this.reminderService = reminderService;
        this.userId = userId;
    }

    @FXML
    private void initialize() {
        setupTableColumns();
        setupComboBoxes();
        loadReminders();
        setupAnimations();
        setupCloseButton();
        styleTableView();
        
        // Ensure the JavaFX application thread is used
        Platform.runLater(() -> {
            // Set minimum size for the dialog stage
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            if (stage != null) {
                stage.setMinWidth(700);
                stage.setMinHeight(600);
            }
        });
    }
    
    private void setupAnimations() {
        // Add pulsing glow animation to the clock icon
        if (reminderIconLabel != null) {
            Glow glow = (Glow) reminderIconLabel.getEffect();
            if (glow != null) {
                Timeline pulseTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.6)),
                    new KeyFrame(Duration.millis(1500), new KeyValue(glow.levelProperty(), 0.3)),
                    new KeyFrame(Duration.millis(3000), new KeyValue(glow.levelProperty(), 0.6))
                );
                pulseTimeline.setCycleCount(Timeline.INDEFINITE);
                pulseTimeline.play();
            }
            
            // Add subtle rotation animation to the clock icon
            RotateTransition rotateTransition = new RotateTransition(Duration.seconds(8), reminderIconLabel);
            rotateTransition.setByAngle(360);
            rotateTransition.setCycleCount(Timeline.INDEFINITE);
            rotateTransition.setInterpolator(Interpolator.LINEAR);
            rotateTransition.play();
        }
        
        // Style the header with gradient background
        if (headerPane != null) {
            headerPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #2d2d3a, #1a1a24);");
        }
    }
    
    private void setupCloseButton() {
        if (closeButton != null) {
            // Add hover effects
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
            
            // Close the dialog on button click
            closeButton.setOnAction(e -> {
                Stage stage = (Stage) closeButton.getScene().getWindow();
                stage.close();
            });
        }
    }

    private void styleTableView() {
        // Style the table
        reminderTable.getStyleClass().add("reminder-table");
        
        // Style the scroll pane
        if (contentScrollPane != null) {
            contentScrollPane.getStyleClass().add("achievement-scroll");
        }
        
        // Handle the rows - add hover effect to table rows
        reminderTable.setRowFactory(tv -> {
            TableRow<Reminder> row = new TableRow<>();
            
            // Set up hover effect
            row.setStyle("-fx-background-color: linear-gradient(to bottom right, #2d2d3a, #1a1a24); " + 
                         "-fx-background-radius: 8px;");
            
            row.setOnMouseEntered(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: linear-gradient(to bottom right, #33334d, #1f1f2e); " + 
                                 "-fx-background-radius: 8px;");
                }
            });
            
            row.setOnMouseExited(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: linear-gradient(to bottom right, #2d2d3a, #1a1a24); " + 
                                 "-fx-background-radius: 8px;");
                }
            });
            
            return row;
        });
        
        // Style table cells
        reminderTable.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05);" +
                              "-fx-background-radius: 15;" +
                              "-fx-border-radius: 15;" +
                              "-fx-border-width: 1;" +
                              "-fx-border-color: rgba(93, 95, 239, 0.2);");
    }

    private void setupComboBoxes() {
        // Hours (00-23)
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }
        hourComboBox.setItems(hours);
        
        // Minutes (00-59)
        ObservableList<String> minutes = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i++) {
            minutes.add(String.format("%02d", i));
        }
        minuteComboBox.setItems(minutes);
        
        // Frequency options
        ObservableList<String> frequencies = FXCollections.observableArrayList(
            "Daily", "Weekdays", "Weekends", "Weekly", "Monthly"
        );
        frequencyComboBox.setItems(frequencies);
        
        // Button hover effects
        addButton.setOnMouseEntered(e -> 
            addButton.setStyle("-fx-background-color: #5d80ffff; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-padding: 10 25; " +
                            "-fx-background-radius: 8;")
        );
        
        addButton.setOnMouseExited(e -> 
            addButton.setStyle("-fx-background-color: #4e71feff; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-padding: 10 25; " +
                            "-fx-background-radius: 8;")
        );
    }

    private void setupTableColumns() {
        // Format time
        timeColumn.setCellValueFactory(cellData -> {
            LocalTime time = cellData.getValue().getTime();
            return new javafx.beans.property.SimpleObjectProperty<>(time);
        });
        
        timeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalTime time, boolean empty) {
                super.updateItem(time, empty);
                if (empty || time == null) {
                    setText(null);
                } else {
                    setText(time.format(DateTimeFormatter.ofPattern("HH:mm")));
                    setStyle("-fx-text-fill: white;");
                }
            }
        });
        
        // Message column
        messageColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMessage()));
            
        messageColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                } else {
                    setText(message);
                    setStyle("-fx-text-fill: white;");
                }
            }
        });

        // Frequency column
        frequencyColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFrequency()));
            
        frequencyColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String frequency, boolean empty) {
                super.updateItem(frequency, empty);
                if (empty || frequency == null) {
                    setText(null);
                } else {
                    setText(frequency);
                    setStyle("-fx-text-fill: white;");
                }
            }
        });

        // Enabled column with toggle
        enabledColumn.setCellValueFactory(cellData -> 
            new SimpleBooleanProperty(cellData.getValue().isEnabled()));
            
        enabledColumn.setCellFactory(column -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();
            
            {
                checkBox.setStyle("-fx-text-fill: white;");
                checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        Reminder reminder = (Reminder) getTableRow().getItem();
                        reminder.setEnabled(newVal);
                        reminderService.updateReminder(reminder);
                    }
                });
            }
            
            @Override
            protected void updateItem(Boolean enabled, boolean empty) {
                super.updateItem(enabled, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(enabled);
                    setGraphic(checkBox);
                }
            }
        });

        // Actions column with styled buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = createActionButton("Edit", "linear-gradient(to bottom, #4e71fe, #3a5bc0)");
            private final Button deleteButton = createActionButton("Delete", "linear-gradient(to bottom, #ff4d4d, #c53030)");
            private final HBox container = new HBox(5, editButton, deleteButton);
            
            {
                container.setAlignment(Pos.CENTER);
                
                editButton.setOnAction(event -> {
                    Reminder reminder = getTableView().getItems().get(getIndex());
                    editReminder(reminder);
                });
                
                deleteButton.setOnAction(event -> {
                    Reminder reminder = getTableView().getItems().get(getIndex());
                    deleteReminderWithAnimation(reminder);
                });
                
                // Add hover effects
                setupButtonHoverEffects(editButton, "linear-gradient(to bottom, #5d80ff, #4a6ad0)", "linear-gradient(to bottom, #4e71fe, #3a5bc0)");
                setupButtonHoverEffects(deleteButton, "linear-gradient(to bottom, #ff6b6b, #d54040)", "linear-gradient(to bottom, #ff4d4d, #c53030)");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private Button createActionButton(String text, String backgroundColor) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + backgroundColor + "; " +
                      "-fx-text-fill: white; " +
                      "-fx-font-size: 12px; " +
                      "-fx-padding: 5 10; " +
                      "-fx-background-radius: 5;");
        return button;
    }
    
    private void setupButtonHoverEffects(Button button, String hoverColor, String normalColor) {
        button.setOnMouseEntered(e -> 
            button.setStyle("-fx-background-color: " + hoverColor + "; " +
                          "-fx-text-fill: white; " +
                          "-fx-font-size: 12px; " +
                          "-fx-padding: 5 10; " +
                          "-fx-background-radius: 5;")
        );
        
        button.setOnMouseExited(e -> 
            button.setStyle("-fx-background-color: " + normalColor + "; " +
                          "-fx-text-fill: white; " +
                          "-fx-font-size: 12px; " +
                          "-fx-padding: 5 10; " +
                          "-fx-background-radius: 5;")
        );
    }

    private void deleteReminderWithAnimation(Reminder reminder) {
        // Find the row node containing the reminder
        Node row = null;
        for (Node node : reminderTable.lookupAll(".table-row-cell")) {
            if (node instanceof TableRow) {
                TableRow<?> tableRow = (TableRow<?>) node;
                if (tableRow.getItem() == reminder) {
                    row = node;
                    break;
                }
            }
        }

        if (row != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(500), row);
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setOnFinished(e -> {
                reminderService.deleteReminder(reminder);
                loadReminders();
            });
            fade.play();
        } else {
            reminderService.deleteReminder(reminder);
            loadReminders();
        }
    }

    private void loadReminders() {
        ObservableList<Reminder> reminders = FXCollections.observableArrayList(
            reminderService.getUserReminders(userId)
        );
        reminderTable.setItems(reminders);
    }

    @FXML
    private void addReminder() {
        if (!validateInput()) {
            showError("Invalid Input", "Please fill in all fields");
            return;
        }

        LocalTime time = LocalTime.of(
            Integer.parseInt(hourComboBox.getValue()),
            Integer.parseInt(minuteComboBox.getValue())
        );

        String message = messageArea.getText();
        String frequency = frequencyComboBox.getValue();

        try {
            // Create a new reminder with a unique ID
            Reminder reminder = new Reminder(
                generateReminderId(),
                userId,
                time,
                message,
                true, // enabled by default
                frequency,
                false // not read initially
            );

            reminderService.createReminder(reminder);
            
            // Add animation for added item
            animateNewReminderAdded();
            
            loadReminders();
            clearInputs();
        } catch (Exception e) {
            showError("Error", "Failed to create reminder: " + e.getMessage());
        }
    }
    
    private void animateNewReminderAdded() {
        // Flash the icon to indicate success
        if (reminderIconLabel != null) {
            // Create a color adjust effect for changing the hue to green temporarily
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setHue(0.5); // Green-ish hue
            colorAdjust.setSaturation(0.5);
            colorAdjust.setBrightness(0.1);
            
            // Store the original effect
            javafx.scene.effect.Effect originalEffect = reminderIconLabel.getEffect();
            
            // Apply the color adjust and then revert
            Timeline colorTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(reminderIconLabel.effectProperty(), colorAdjust)),
                new KeyFrame(Duration.millis(800), new KeyValue(reminderIconLabel.effectProperty(), originalEffect))
            );
            colorTimeline.play();
        }
    }

    private int generateReminderId() {
        // Simple ID generation - you might want to use a more sophisticated method
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    private void editReminder(Reminder reminder) {
        // Populate the form with the reminder details
        hourComboBox.setValue(String.format("%02d", reminder.getTime().getHour()));
        minuteComboBox.setValue(String.format("%02d", reminder.getTime().getMinute()));
        frequencyComboBox.setValue(reminder.getFrequency());
        messageArea.setText(reminder.getMessage());
        
        // Change the add button to an update button
        addButton.setText("Update Reminder");
        addButton.setOnAction(e -> {
            updateReminder(reminder);
            // Reset the button after update
            addButton.setText("Add Reminder");
            addButton.setOnAction(this::addReminderFromEvent);
        });
    }
    
    // Helper method to handle the event for addReminder
    private void addReminderFromEvent(ActionEvent event) {
        addReminder();
    }

    private void updateReminder(Reminder reminder) {
        if (!validateInput()) {
            showError("Invalid Input", "Please fill in all fields");
            return;
        }
        
        LocalTime time = LocalTime.of(
            Integer.parseInt(hourComboBox.getValue()),
            Integer.parseInt(minuteComboBox.getValue())
        );
        
        String message = messageArea.getText();
        String frequency = frequencyComboBox.getValue();
        
        try {
            reminder.setTime(time);
            reminder.setMessage(message);
            reminder.setFrequency(frequency);
            
            reminderService.updateReminder(reminder);
            loadReminders();
            clearInputs();
        } catch (Exception e) {
            showError("Error", "Failed to update reminder: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        return hourComboBox.getValue() != null &&
               minuteComboBox.getValue() != null &&
               frequencyComboBox.getValue() != null &&
               !messageArea.getText().trim().isEmpty();
    }

    private void clearInputs() {
        hourComboBox.setValue(null);
        minuteComboBox.setValue(null);
        frequencyComboBox.setValue(null);
        messageArea.clear();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #2d2d3a, #1a1a24);" +
                          "-fx-border-color: rgba(93, 95, 239, 0.3);" +
                          "-fx-border-width: 1px;" +
                          "-fx-text-fill: white;");
        
        // Add stylesheet
        Scene scene = dialogPane.getScene();
        if (scene != null) {
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        }
        
        alert.showAndWait();
    }
} 