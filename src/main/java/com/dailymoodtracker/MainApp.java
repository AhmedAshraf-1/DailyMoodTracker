package com.dailymoodtracker;

import com.dailymoodtracker.controller.LoginController;
import com.dailymoodtracker.controller.MainController;
import com.dailymoodtracker.model.User;
import com.dailymoodtracker.service.UserService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Show login screen - user data will be retained in the database
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Scene scene = new Scene(loader.load());
            
            primaryStage.setTitle("Daily Mood Tracker");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            
            // Create a fallback UI if FXML loading fails
            VBox fallbackRoot = new VBox(20);
            fallbackRoot.setStyle("-fx-padding: 20; -fx-background-color: #1a1a2e;");
            
            Label titleLabel = new Label("Daily Mood Tracker");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
            
            Label errorLabel = new Label("Error loading UI: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 14px;");
            
            fallbackRoot.getChildren().addAll(titleLabel, errorLabel);
            
            Scene fallbackScene = new Scene(fallbackRoot, 600, 400);
            primaryStage.setTitle("Daily Mood Tracker - Error");
            primaryStage.setScene(fallbackScene);
            primaryStage.show();
            
            // Show a more detailed error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Application Error");
            alert.setHeaderText("Failed to load the application UI");
            alert.setContentText("Please make sure all resources are available.\nError: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 