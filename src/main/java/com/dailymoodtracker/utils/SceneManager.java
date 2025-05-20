package com.dailymoodtracker.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class to manage scene transitions in the application.
 */
public class SceneManager {
    private static final Logger logger = LoggerFactory.getLogger(SceneManager.class);
    
    private static SceneManager instance;
    private Stage mainStage;
    private Map<String, Scene> sceneCache = new HashMap<>();
    
    private SceneManager() {
        // Private constructor for singleton
    }
    
    /**
     * Get the singleton instance of SceneManager.
     * @return SceneManager instance
     */
    public static synchronized SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }
    
    /**
     * Set the main application stage.
     * @param stage The main application stage
     */
    public void setMainStage(Stage stage) {
        this.mainStage = stage;
    }
    
    /**
     * Get the main application stage.
     * @return The main application stage
     */
    public Stage getMainStage() {
        return mainStage;
    }
    
    /**
     * Switch the main stage to a new scene.
     * @param fxmlPath The path to the FXML file defining the scene
     * @param title The title for the stage
     * @param useCache Whether to cache the scene for future use
     */
    public void switchScene(String fxmlPath, String title, boolean useCache) {
        try {
            Scene scene;
            if (useCache && sceneCache.containsKey(fxmlPath)) {
                // Use cached scene if available
                scene = sceneCache.get(fxmlPath);
                logger.debug("Using cached scene: {}", fxmlPath);
            } else {
                // Load new scene
                Parent root = loadFXML(fxmlPath);
                scene = new Scene(root);
                
                if (useCache) {
                    sceneCache.put(fxmlPath, scene);
                    logger.debug("Cached scene: {}", fxmlPath);
                }
            }
            
            mainStage.setTitle(title);
            mainStage.setScene(scene);
            
            logger.info("Switched to scene: {}", fxmlPath);
        } catch (IOException e) {
            logger.error("Error switching scene to {}: {}", fxmlPath, e.getMessage(), e);
            AlertHelper.showError("Navigation Error", 
                    "Failed to load the requested screen. Please try again.");
        }
    }
    
    /**
     * Open a new window as a modal dialog.
     * @param fxmlPath The path to the FXML file defining the dialog
     * @param title The title for the dialog window
     * @return The stage for the dialog
     */
    public Stage openDialog(String fxmlPath, String title) {
        try {
            Parent root = loadFXML(fxmlPath);
            Scene scene = new Scene(root);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.setScene(scene);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(mainStage);
            
            logger.info("Opened dialog: {}", fxmlPath);
            
            return dialogStage;
        } catch (IOException e) {
            logger.error("Error opening dialog {}: {}", fxmlPath, e.getMessage(), e);
            AlertHelper.showError("Dialog Error", 
                    "Failed to open the dialog. Please try again.");
            return null;
        }
    }
    
    /**
     * Load an FXML file and return the root node.
     * @param fxmlPath The path to the FXML file
     * @return The root node of the loaded FXML
     * @throws IOException If the FXML file cannot be loaded
     */
    private Parent loadFXML(String fxmlPath) throws IOException {
        URL resource = getClass().getClassLoader().getResource(fxmlPath);
        if (resource == null) {
            throw new IOException("Could not find FXML file: " + fxmlPath);
        }
        return FXMLLoader.load(resource);
    }
    
    /**
     * Clear the scene cache.
     */
    public void clearCache() {
        sceneCache.clear();
        logger.info("Scene cache cleared");
    }
} 