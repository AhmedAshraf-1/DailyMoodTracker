package com.dailymoodtracker.controller;

import com.dailymoodtracker.utils.SceneManager;
import com.dailymoodtracker.utils.AlertHelper;
import com.dailymoodtracker.model.User;
import com.dailymoodtracker.service.SentimentServiceFactory;
import com.dailymoodtracker.service.SentimentServiceFactory.ServiceType;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for the dashboard view.
 */
public class DashboardController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;
    @FXML private Label welcomeEmoji;
    @FXML private Label averageMoodEmoji;
    @FXML private Label averageMoodValue;
    @FXML private Label moodTrendLabel;
    @FXML private Label entryCountValue;
    @FXML private Label streakValue;
    @FXML private LineChart<String, Number> moodChart;
    @FXML private PieChart moodDistributionChart;
    @FXML private VBox activityImpactContainer;
    @FXML private VBox recentEntriesContainer;
    @FXML private VBox suggestionsContainer;
    @FXML private Button preferencesButton;
    @FXML private Button aiSettingsButton;
    @FXML private Button chatbotButton;
    
    private User currentUser;
    private SceneManager sceneManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sceneManager = SceneManager.getInstance();
        
        // Set current date
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d");
        dateLabel.setText("Today is " + today.format(formatter));
        
        // For demo purposes
        welcomeEmoji.setText(getRandomWelcomeEmoji());
        
        // Setup UI interactions
        setupButtonHandlers();
    }
    
    /**
     * Initialize user data.
     */
    public void initData(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome back, " + user.getUsername() + "!");
        
        // Load user-specific data
        loadUserData();
    }
    
    /**
     * Setup button click handlers.
     */
    private void setupButtonHandlers() {
        if (preferencesButton != null) {
            preferencesButton.setOnAction(event -> showPreferences());
        }
        
        if (aiSettingsButton != null) {
            aiSettingsButton.setOnAction(event -> showAISettings());
        }
        
        if (chatbotButton != null) {
            chatbotButton.setOnAction(event -> openChatbot());
        }
    }
    
    /**
     * Load user-specific data for dashboard.
     */
    private void loadUserData() {
        // This would be replaced with actual data loading in production
        logger.info("Loading dashboard data for user: {}", currentUser != null ? currentUser.getUsername() : "null");
        
        // Demo values
        averageMoodEmoji.setText("😊");
        averageMoodValue.setText("4.2");
        moodTrendLabel.setText("↗ Trending upward");
        entryCountValue.setText("28");
        streakValue.setText("7");
        
        // Load charts, activities, entries, and suggestions with real data
    }
    
    /**
     * Open journal view.
     */
    @FXML
    private void openJournal() {
        logger.info("Opening journal");
        AlertHelper.showInformation("Coming Soon", "The journal feature will be available in a future update.");
    }
    
    /**
     * Open chatbot view.
     */
    @FXML
    private void openChatbot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChatbotView.fxml"));
            Parent root = loader.load();
            
            // Get the controller and initialize with user
            ChatbotController controller = loader.getController();
            // controller.initData(currentUser);
            
            Stage stage = (Stage) chatbotButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            
            logger.info("Opened chatbot view");
        } catch (IOException e) {
            logger.error("Error opening chatbot view", e);
            AlertHelper.showError("Navigation Error", "Failed to open chatbot view: " + e.getMessage());
        }
    }
    
    /**
     * Open reminders dialog.
     */
    @FXML
    private void openRemindersDialog() {
        logger.info("Opening reminders dialog");
        AlertHelper.showInformation("Coming Soon", "The reminders feature will be available in a future update.");
    }
    
    /**
     * Show mood history.
     */
    @FXML
    private void showHistory() {
        logger.info("Showing mood history");
        AlertHelper.showInformation("Coming Soon", "The mood history feature will be available in a future update.");
    }
    
    /**
     * Show achievements.
     */
    @FXML
    private void showAchievements() {
        logger.info("Showing achievements");
        AlertHelper.showInformation("Coming Soon", "The achievements feature will be available in a future update.");
    }
    
    /**
     * Show user preferences.
     */
    @FXML
    private void showPreferences() {
        logger.info("Showing preferences");
        AlertHelper.showInformation("Coming Soon", "The user preferences feature will be available in a future update.");
    }
    
    /**
     * Show AI settings dialog.
     */
    private void showAISettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));
            Parent root = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("AI Settings");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(aiSettingsButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            
            // Display current AI service
            ServiceType currentService = SentimentServiceFactory.getServiceType();
            logger.info("Current AI service: {}", currentService);
            
            dialogStage.showAndWait();
            
            logger.info("AI settings dialog closed");
        } catch (IOException e) {
            logger.error("Error showing AI settings dialog", e);
            AlertHelper.showError("Dialog Error", "Failed to open AI settings: " + e.getMessage());
        }
    }
    
    /**
     * Log out the current user.
     */
    @FXML
    private void logout() {
        logger.info("User logout");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            
            logger.info("Returned to login screen");
        } catch (IOException e) {
            logger.error("Error returning to login screen", e);
            AlertHelper.showError("Navigation Error", "Failed to return to login screen: " + e.getMessage());
        }
    }
    
    /**
     * Get a random welcome emoji.
     */
    private String getRandomWelcomeEmoji() {
        String[] emojis = {"👋", "😊", "🌟", "🎉", "✨", "🌈", "☀️", "🌻"};
        int index = (int) (Math.random() * emojis.length);
        return emojis[index];
    }
} 