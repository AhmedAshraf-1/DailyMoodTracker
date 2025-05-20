package com.dailymoodtracker.service;

import com.dailymoodtracker.model.Goal;
import com.dailymoodtracker.model.User;
import com.dailymoodtracker.model.UserPreferences;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing user preferences including themes, colors, and custom activities.
 */
public class PreferencesService {
    private static final Logger logger = LoggerFactory.getLogger(PreferencesService.class);
    private static final String PREFERENCES_DIR = System.getProperty("user.home") + File.separator + 
                                                  ".dailymoodtracker" + File.separator + "preferences";
    
    private Map<Integer, UserPreferences> userPreferencesCache;
    private static PreferencesService instance;
    
    // Get singleton instance
    public static PreferencesService getInstance() {
        if (instance == null) {
            instance = new PreferencesService();
        }
        return instance;
    }
    
    // Private constructor
    private PreferencesService() {
        userPreferencesCache = new HashMap<>();
        initializePreferencesDirectory();
    }
    
    // Initialize the preferences directory if it doesn't exist
    private void initializePreferencesDirectory() {
        Path preferencesPath = Paths.get(PREFERENCES_DIR);
        if (!Files.exists(preferencesPath)) {
            try {
                Files.createDirectories(preferencesPath);
                logger.info("Created preferences directory at: {}", PREFERENCES_DIR);
            } catch (IOException e) {
                logger.error("Failed to create preferences directory", e);
                showError("Error", "Failed to create preferences directory", e.getMessage());
            }
        }
    }
    
    // Get user preferences, creating default if not exists
    public UserPreferences getUserPreferences(User user) {
        if (user == null) {
            logger.error("Attempted to get preferences for null user");
            return null;
        }
        
        int userId = user.getId();
        
        // Return from cache if available
        if (userPreferencesCache.containsKey(userId)) {
            return userPreferencesCache.get(userId);
        }
        
        // Try to load from file
        UserPreferences preferences = loadUserPreferences(userId);
        
        // If not found, create default
        if (preferences == null) {
            preferences = new UserPreferences(userId);
            saveUserPreferences(preferences);
        }
        
        // Add to cache and return
        userPreferencesCache.put(userId, preferences);
        return preferences;
    }
    
    // Load user preferences from file
    private UserPreferences loadUserPreferences(int userId) {
        String filePath = PREFERENCES_DIR + File.separator + "user_" + userId + ".prefs";
        File prefsFile = new File(filePath);
        
        if (!prefsFile.exists()) {
            logger.info("No preferences file found for user ID: {}", userId);
            return null;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(prefsFile))) {
            UserPreferences prefs = (UserPreferences) ois.readObject();
            logger.info("Loaded preferences for user ID: {}", userId);
            return prefs;
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error loading preferences for user ID: {}", userId, e);
            showError("Error", "Failed to load user preferences", e.getMessage());
            return null;
        }
    }
    
    // Save user preferences to file
    public boolean saveUserPreferences(UserPreferences preferences) {
        if (preferences == null) {
            logger.error("Attempted to save null preferences");
            return false;
        }
        
        int userId = preferences.getUserId();
        String filePath = PREFERENCES_DIR + File.separator + "user_" + userId + ".prefs";
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(preferences);
            logger.info("Saved preferences for user ID: {}", userId);
            
            // Update cache
            userPreferencesCache.put(userId, preferences);
            return true;
        } catch (IOException e) {
            logger.error("Error saving preferences for user ID: {}", userId, e);
            showError("Error", "Failed to save user preferences", e.getMessage());
            return false;
        }
    }
    
    // Apply theme to scene based on user preferences
    public void applyTheme(Scene scene, UserPreferences preferences) {
        if (scene == null || preferences == null) {
            logger.error("Cannot apply theme: scene or preferences is null");
            return;
        }
        
        // Get theme styles
        String themeStyles = preferences.getThemeStyles();
        
        // Apply to scene
        scene.getRoot().setStyle(themeStyles);
        logger.info("Applied theme to scene for user ID: {}", preferences.getUserId());
    }
    
    // Apply theme to a specific node
    public void applyThemeToNode(Node node, UserPreferences preferences) {
        if (node == null || preferences == null) {
            logger.error("Cannot apply theme: node or preferences is null");
            return;
        }
        
        // Get theme styles
        String themeStyles = preferences.getThemeStyles();
        
        // Apply to node
        node.setStyle(node.getStyle() + themeStyles);
        logger.info("Applied theme to node for user ID: {}", preferences.getUserId());
    }
    
    // Update theme for user
    public void updateTheme(User user, UserPreferences.Theme theme) {
        UserPreferences prefs = getUserPreferences(user);
        if (prefs != null) {
            prefs.setTheme(theme);
            saveUserPreferences(prefs);
            logger.info("Updated theme to {} for user ID: {}", theme.getName(), user.getId());
        }
    }
    
    // Update accent color for user
    public void updateAccentColor(User user, UserPreferences.AccentColor accentColor) {
        UserPreferences prefs = getUserPreferences(user);
        if (prefs != null) {
            prefs.setAccentColor(accentColor);
            saveUserPreferences(prefs);
            logger.info("Updated accent color to {} for user ID: {}", accentColor.name(), user.getId());
        }
    }
    
    // Update mood scale for user
    public void updateMoodScale(User user, UserPreferences.MoodScale moodScale) {
        UserPreferences prefs = getUserPreferences(user);
        if (prefs != null) {
            prefs.setMoodScale(moodScale);
            saveUserPreferences(prefs);
            logger.info("Updated mood scale to {}-point for user ID: {}", moodScale.getPoints(), user.getId());
        }
    }
    
    // Add a personal goal for user
    public void addPersonalGoal(User user, Goal goal) {
        UserPreferences prefs = getUserPreferences(user);
        if (prefs != null) {
            prefs.addPersonalGoal(goal);
            saveUserPreferences(prefs);
            logger.info("Added personal goal for user ID: {}", user.getId());
        }
    }
    
    // Add a custom activity category
    public void addActivityCategory(User user, String categoryName) {
        UserPreferences prefs = getUserPreferences(user);
        if (prefs != null) {
            prefs.addCategory(categoryName);
            saveUserPreferences(prefs);
            logger.info("Added activity category '{}' for user ID: {}", categoryName, user.getId());
        }
    }
    
    // Add a custom activity to a category
    public void addActivity(User user, String categoryName, String activity) {
        UserPreferences prefs = getUserPreferences(user);
        if (prefs != null) {
            prefs.addActivityToCategory(categoryName, activity);
            saveUserPreferences(prefs);
            logger.info("Added activity '{}' to category '{}' for user ID: {}", 
                        activity, categoryName, user.getId());
        }
    }
    
    // Show error alert
    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.showAndWait();
    }
} 