package com.dailymoodtracker.controller;

import com.dailymoodtracker.model.Goal;
import com.dailymoodtracker.model.User;
import com.dailymoodtracker.model.UserPreferences;
import com.dailymoodtracker.service.PreferencesService;
import com.dailymoodtracker.service.SentimentServiceFactory;
import com.dailymoodtracker.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Controller for the preferences view that allows users to customize themes, colors,
 * mood scales, and activity categories.
 */
public class PreferencesController {
    private static final Logger logger = LoggerFactory.getLogger(PreferencesController.class);
    
    // API URL preference key
    private static final String API_URL_PREF_KEY = "sentiment.api.url";
    private static final String DEFAULT_API_URL = "http://localhost:5000/analyze";
    private static final String PREFERENCES_FILE = System.getProperty("user.home") + File.separator + 
                                                 ".dailymoodtracker" + File.separator + "apiprefs.properties";
    
    @FXML private BorderPane mainContainer;
    @FXML private ComboBox<String> themeComboBox;
    @FXML private ComboBox<String> accentColorComboBox;
    @FXML private ComboBox<String> moodScaleComboBox;
    @FXML private HBox themePreview;
    @FXML private HBox moodScalePreview;
    @FXML private ListView<String> categoriesListView;
    @FXML private ListView<String> activitiesListView;
    @FXML private TextField newItemTextField;
    @FXML private ListView<String> goalsListView;
    @FXML private TextField newGoalTextField;
    @FXML private TextField apiKeyTextField;
    @FXML private Button testApiButton;
    @FXML private Label apiStatusLabel;
    
    private User currentUser;
    private UserPreferences userPreferences;
    private PreferencesService preferencesService;
    private String selectedCategory;
    private UserPreferences.Theme selectedTheme;
    private UserPreferences.AccentColor selectedAccentColor;
    private UserPreferences.MoodScale selectedMoodScale;
    private Properties systemPreferences;
    
    private ObservableList<String> categoriesObservable;
    private ObservableList<String> activitiesObservable;
    private ObservableList<String> goalsObservable;
    
    /**
     * Initialize the controller with a user.
     */
    public void initData(User user) {
        this.currentUser = user;
        preferencesService = PreferencesService.getInstance();
        userPreferences = preferencesService.getUserPreferences(user);
        systemPreferences = new Properties();
        
        // Load properties file if it exists
        try {
            File prefsFile = new File(PREFERENCES_FILE);
            if (prefsFile.exists()) {
                try (FileInputStream fis = new FileInputStream(prefsFile)) {
                    systemPreferences.load(fis);
                }
            } else {
                // Ensure directory exists
                prefsFile.getParentFile().mkdirs();
            }
        } catch (IOException e) {
            logger.error("Error loading preferences file", e);
        }
        
        if (userPreferences == null) {
            logger.error("Failed to load user preferences");
            showError("Error", "Failed to load user preferences", 
                     "Unable to load your preferences. Default settings will be used.");
            return;
        }
        
        // Initialize observables
        categoriesObservable = FXCollections.observableArrayList(userPreferences.getAllCategories());
        activitiesObservable = FXCollections.observableArrayList();
        
        // Convert goals to string representation
        List<String> goalStrings = new ArrayList<>();
        for (Goal goal : userPreferences.getPersonalGoals()) {
            goalStrings.add(goal.getDescription());
        }
        goalsObservable = FXCollections.observableArrayList(goalStrings);
        
        // Set initial values
        selectedTheme = userPreferences.getTheme();
        selectedAccentColor = userPreferences.getAccentColor();
        selectedMoodScale = userPreferences.getMoodScale();
        
        // Load UI with data
        initializeUI();
    }
    
    /**
     * Initialize the UI components with the user's preferences.
     */
    private void initializeUI() {
        // Set up theme combo box
        themeComboBox.getSelectionModel().select(selectedTheme.getName());
        
        // Set up accent color combo box
        accentColorComboBox.getSelectionModel().select(selectedAccentColor.name().substring(0, 1) + 
                                                      selectedAccentColor.name().substring(1).toLowerCase());
        
        // Set up mood scale combo box
        String moodScaleText = selectedMoodScale.getPoints() + "-point";
        moodScaleComboBox.getSelectionModel().select(moodScaleText);
        
        // Set up categories and activities lists
        categoriesListView.setItems(categoriesObservable);
        activitiesListView.setItems(activitiesObservable);
        
        // Set up goals list
        goalsListView.setItems(goalsObservable);
        
        // Set up API URL field
        String apiUrl = systemPreferences.getProperty(API_URL_PREF_KEY, DEFAULT_API_URL);
        apiKeyTextField.setText(apiUrl);
        
        // Update theme preview
        updateThemePreview();
        
        // Update mood scale preview
        updateMoodScalePreview();
    }
    
    /**
     * Update the theme preview with the selected theme and accent color.
     */
    private void updateThemePreview() {
        themePreview.getChildren().clear();
        
        // Background panel
        Rectangle background = new Rectangle(0, 0, 400, 120);
        background.setFill(Color.web(selectedTheme.getBackgroundColor()));
        
        // Card panel
        Rectangle card = new Rectangle(20, 20, 360, 80);
        card.setFill(Color.web(selectedTheme.getCardColor()));
        card.setArcWidth(10);
        card.setArcHeight(10);
        
        // Text label
        Label textLabel = new Label("Sample Text");
        textLabel.setTextFill(Color.web(selectedTheme.getTextColor()));
        textLabel.setLayoutX(40);
        textLabel.setLayoutY(40);
        
        // Accent color circle
        Circle accentCircle = new Circle(320, 60, 20);
        accentCircle.setFill(Color.web(selectedAccentColor.getHexColor()));
        
        // Button with accent color
        Button sampleButton = new Button("Button");
        sampleButton.setStyle(
            "-fx-background-color: " + selectedAccentColor.getHexColor() + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 5px;"
        );
        sampleButton.setLayoutX(100);
        sampleButton.setLayoutY(40);
        
        // Input field
        TextField sampleInput = new TextField("Input");
        sampleInput.setStyle(
            "-fx-background-color: " + selectedTheme.getInputColor() + ";" +
            "-fx-text-fill: " + selectedTheme.getTextColor() + ";" +
            "-fx-background-radius: 5px;"
        );
        sampleInput.setPrefWidth(100);
        sampleInput.setLayoutX(180);
        sampleInput.setLayoutY(40);
        
        // Add all elements to preview
        themePreview.getChildren().addAll(background, card, textLabel, accentCircle, sampleButton, sampleInput);
    }
    
    /**
     * Update the mood scale preview with the selected mood scale.
     */
    private void updateMoodScalePreview() {
        moodScalePreview.getChildren().clear();
        
        int points = selectedMoodScale.getPoints();
        
        // Create mood buttons
        for (int i = 1; i <= points; i++) {
            Button moodButton = new Button();
            moodButton.getStyleClass().add("mood-scale-button");
            
            // Add appropriate color class based on position in scale
            if (points == 3) {
                if (i == 1) moodButton.getStyleClass().add("mood-scale-button-1");
                else if (i == 2) moodButton.getStyleClass().add("mood-scale-button-3");
                else moodButton.getStyleClass().add("mood-scale-button-5");
            } else if (points == 5) {
                moodButton.getStyleClass().add("mood-scale-button-" + i);
            } else {
                // For 10-point scale
                if (i <= 2) moodButton.getStyleClass().add("mood-scale-button-1");
                else if (i <= 4) moodButton.getStyleClass().add("mood-scale-button-2");
                else if (i <= 6) moodButton.getStyleClass().add("mood-scale-button-3");
                else if (i <= 8) moodButton.getStyleClass().add("mood-scale-button-4");
                else moodButton.getStyleClass().add("mood-scale-button-5");
            }
            
            // Make middle button selected for preview
            if (i == points / 2 + 1) {
                moodButton.getStyleClass().add("selected");
            }
            
            moodScalePreview.getChildren().add(moodButton);
        }
    }
    
    /**
     * Handle theme change event.
     */
    @FXML
    private void onThemeChanged() {
        String themeName = themeComboBox.getSelectionModel().getSelectedItem();
        
        // Find matching theme
        for (UserPreferences.Theme theme : UserPreferences.Theme.values()) {
            if (theme.getName().equals(themeName)) {
                selectedTheme = theme;
                updateThemePreview();
                break;
            }
        }
    }
    
    /**
     * Handle accent color change event.
     */
    @FXML
    private void onAccentColorChanged() {
        String colorName = accentColorComboBox.getSelectionModel().getSelectedItem().toUpperCase();
        
        // Find matching accent color
        for (UserPreferences.AccentColor accentColor : UserPreferences.AccentColor.values()) {
            if (accentColor.name().equals(colorName)) {
                selectedAccentColor = accentColor;
                updateThemePreview();
                break;
            }
        }
    }
    
    /**
     * Handle mood scale change event.
     */
    @FXML
    private void onMoodScaleChanged() {
        String scaleText = moodScaleComboBox.getSelectionModel().getSelectedItem();
        int points = Integer.parseInt(scaleText.split("-")[0]);
        
        // Find matching mood scale
        for (UserPreferences.MoodScale moodScale : UserPreferences.MoodScale.values()) {
            if (moodScale.getPoints() == points) {
                selectedMoodScale = moodScale;
                updateMoodScalePreview();
                break;
            }
        }
    }
    
    /**
     * Handle category selection event.
     */
    @FXML
    private void onCategorySelected() {
        String category = categoriesListView.getSelectionModel().getSelectedItem();
        if (category != null) {
            selectedCategory = category;
            
            // Update activities list
            List<String> activities = userPreferences.getActivitiesForCategory(category);
            activitiesObservable.setAll(activities);
        }
    }
    
    /**
     * Add a new category.
     */
    @FXML
    private void onAddCategory() {
        String newCategory = newItemTextField.getText().trim();
        
        if (newCategory.isEmpty()) {
            showError("Invalid Input", "Empty Category Name", 
                     "Please enter a name for the new category.");
            return;
        }
        
        if (categoriesObservable.contains(newCategory)) {
            showError("Duplicate Category", "Category Already Exists", 
                     "A category with this name already exists.");
            return;
        }
        
        // Add to model and update UI
        userPreferences.addCategory(newCategory);
        categoriesObservable.add(newCategory);
        newItemTextField.clear();
        
        // Select the new category
        categoriesListView.getSelectionModel().select(newCategory);
        selectedCategory = newCategory;
    }
    
    /**
     * Add a new activity to the selected category.
     */
    @FXML
    private void onAddActivity() {
        if (selectedCategory == null) {
            showError("No Category Selected", "Please Select a Category", 
                     "Please select a category to add the activity to.");
            return;
        }
        
        String newActivity = newItemTextField.getText().trim();
        
        if (newActivity.isEmpty()) {
            showError("Invalid Input", "Empty Activity Name", 
                     "Please enter a name for the new activity.");
            return;
        }
        
        if (activitiesObservable.contains(newActivity)) {
            showError("Duplicate Activity", "Activity Already Exists", 
                     "An activity with this name already exists in this category.");
            return;
        }
        
        // Add to model and update UI
        userPreferences.addActivityToCategory(selectedCategory, newActivity);
        activitiesObservable.add(newActivity);
        newItemTextField.clear();
    }
    
    /**
     * Remove selected category or activity.
     */
    @FXML
    private void onRemoveItem() {
        // Check if a category is selected
        String selectedCategoryItem = categoriesListView.getSelectionModel().getSelectedItem();
        
        // Check if an activity is selected
        String selectedActivity = activitiesListView.getSelectionModel().getSelectedItem();
        
        if (selectedCategoryItem != null) {
            // Remove category
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirm Deletion");
            confirmDialog.setHeaderText("Delete Category");
            confirmDialog.setContentText("Are you sure you want to delete the category '" + 
                                        selectedCategoryItem + "' and all its activities?");
            
            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                userPreferences.removeCategory(selectedCategoryItem);
                categoriesObservable.remove(selectedCategoryItem);
                activitiesObservable.clear();
                selectedCategory = null;
            }
        } else if (selectedActivity != null && selectedCategory != null) {
            // Remove activity
            userPreferences.removeActivityFromCategory(selectedCategory, selectedActivity);
            activitiesObservable.remove(selectedActivity);
        } else {
            showError("No Selection", "No Item Selected", 
                     "Please select a category or activity to remove.");
        }
    }
    
    /**
     * Add a new personal goal.
     */
    @FXML
    private void onAddGoal() {
        String goalText = newGoalTextField.getText().trim();
        
        if (goalText.isEmpty()) {
            showError("Invalid Input", "Empty Goal", 
                     "Please enter a description for your goal.");
            return;
        }
        
        if (goalsObservable.contains(goalText)) {
            showError("Duplicate Goal", "Goal Already Exists", 
                     "A goal with this description already exists.");
            return;
        }
        
        // Create new goal with default end date (1 month from now)
        LocalDate creationDate = LocalDate.now();
        LocalDate targetDate = LocalDate.now().plusMonths(1);
        Goal newGoal = new Goal(goalText, creationDate, null, false);
        
        // Add to model and update UI
        userPreferences.addPersonalGoal(newGoal);
        goalsObservable.add(goalText);
        newGoalTextField.clear();
    }
    
    /**
     * Remove selected goal.
     */
    @FXML
    private void onRemoveGoal() {
        String selectedGoal = goalsListView.getSelectionModel().getSelectedItem();
        
        if (selectedGoal == null) {
            showError("No Selection", "No Goal Selected", 
                     "Please select a goal to remove.");
            return;
        }
        
        // Find and remove the goal
        for (Goal goal : new ArrayList<>(userPreferences.getPersonalGoals())) {
            if (goal.getDescription().equals(selectedGoal)) {
                userPreferences.removePersonalGoal(goal);
                goalsObservable.remove(selectedGoal);
                break;
            }
        }
    }
    
    /**
     * Test API button click handler.
     */
    @FXML
    private void testApiCredentials() {
        String apiUrl = apiKeyTextField.getText().trim();
        
        if (apiUrl.isEmpty()) {
            apiStatusLabel.setText("API URL cannot be empty");
            apiStatusLabel.setTextFill(Color.RED);
            return;
        }
        
        apiStatusLabel.setText("Testing API connection...");
        apiStatusLabel.setTextFill(Color.GRAY);
        testApiButton.setDisable(true);
        
        // Run in background thread
        new Thread(() -> {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                int status = connection.getResponseCode();
                
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    if (status >= 200 && status < 300) {
                        apiStatusLabel.setText("API connection successful!");
                        apiStatusLabel.setTextFill(Color.GREEN);
                    } else {
                        apiStatusLabel.setText("API test failed. Status code: " + status);
                        apiStatusLabel.setTextFill(Color.RED);
                    }
                    testApiButton.setDisable(false);
                });
            } catch (Exception e) {
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    apiStatusLabel.setText("Error: " + e.getMessage());
                    apiStatusLabel.setTextFill(Color.RED);
                    testApiButton.setDisable(false);
                });
            }
        }).start();
    }
    
    /**
     * Save button click handler.
     */
    @FXML
    private void onSave() {
        // Save API URL preference
        String apiUrl = apiKeyTextField.getText().trim();
        systemPreferences.setProperty(API_URL_PREF_KEY, apiUrl);
        
        // Save properties to file
        try (FileOutputStream fos = new FileOutputStream(PREFERENCES_FILE)) {
            systemPreferences.store(fos, "Daily Mood Tracker API Settings");
        } catch (IOException e) {
            logger.error("Error saving preferences file", e);
        }
        
        // Reload sentiment service with new URL
        SentimentServiceFactory.reloadService();
        
        // Update user preferences
        userPreferences.setTheme(selectedTheme);
        userPreferences.setAccentColor(selectedAccentColor);
        userPreferences.setMoodScale(selectedMoodScale);
        
        // Save personal goals - creating updated goals
        List<Goal> updatedGoals = new ArrayList<>();
        for (String goalText : goalsObservable) {
            Goal goal = new Goal(goalText, LocalDate.now(), null, false);
            updatedGoals.add(goal);
        }
        userPreferences.getPersonalGoals().clear();
        userPreferences.getPersonalGoals().addAll(updatedGoals);
        
        // Save to database
        boolean success = preferencesService.saveUserPreferences(userPreferences);
        
        if (success) {
            // Navigate back to main view
            goToMainView();
        } else {
            showError("Error", "Failed to save preferences", 
                     "An error occurred while saving your preferences. Please try again.");
        }
    }
    
    /**
     * Cancel changes and return to main view.
     */
    @FXML
    private void onCancel() {
        goToMainView();
    }
    
    /**
     * Navigate back to the main view.
     */
    private void goToMainView() {
        try {
            // Create main controller with current user
            MainController controller = new MainController(currentUser);
            
            // Load FXML with controller
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/MainView.fxml"));
            loader.setController(controller);
            
            // Create scene and stage
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            stage.setScene(scene);
            
            // Apply theme to new scene
            preferencesService.applyTheme(scene, userPreferences);
            
        } catch (IOException e) {
            logger.error("Error loading main view", e);
            showError("Navigation Error", "Failed to Load Main View", 
                     "There was an error returning to the main view: " + e.getMessage());
        }
    }
    
    /**
     * Show error alert dialog.
     */
    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 