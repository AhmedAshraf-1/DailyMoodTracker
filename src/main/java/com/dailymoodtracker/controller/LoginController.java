package com.dailymoodtracker.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import com.dailymoodtracker.model.User;
import com.dailymoodtracker.service.UserService;
import java.io.IOException;
import javafx.scene.layout.VBox;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

public class LoginController {
    @FXML private VBox mainContainer;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;
    @FXML private Label errorLabel;
    @FXML private Label titleLabel;
    
    private final UserService userService;
    
    public LoginController() {
        this.userService = UserService.getInstance();
    }
    
    @FXML
    public void initialize() {
        setupAnimations();
        setupInputValidation();
    }
    
    private void setupAnimations() {
        // Initial fade in animation for the form
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), mainContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Button hover animations
        loginButton.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), loginButton);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
            
            loginButton.setStyle(loginButton.getStyle() + 
                "-fx-background-color: #45a049;"); // Darker green on hover
        });

        loginButton.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), loginButton);
            scale.setToX(1);
            scale.setToY(1);
            scale.play();
            
            loginButton.setStyle(loginButton.getStyle() + 
                "-fx-background-color: #4CAF50;"); // Original green
        });

        // Text field focus animations
        setupTextFieldAnimation(usernameField);
        setupTextFieldAnimation(passwordField);
    }
    
    private void setupTextFieldAnimation(TextField field) {
        field.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                field.setStyle(field.getStyle() + 
                    "-fx-border-color: #4CAF50; -fx-border-width: 2px;");
            } else {
                field.setStyle(field.getStyle() + 
                    "-fx-border-color: #4D4D4D; -fx-border-width: 1px;");
            }
        });
    }
    
    private void setupInputValidation() {
        // Add input validation and real-time feedback
        usernameField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() < 3) {
                usernameField.setStyle(usernameField.getStyle() + 
                    "-fx-border-color: #FF5252;");
            } else {
                usernameField.setStyle(usernameField.getStyle() + 
                    "-fx-border-color: #4D4D4D;");
            }
        });

        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() < 6) {
                passwordField.setStyle(passwordField.getStyle() + 
                    "-fx-border-color: #FF5252;");
            } else {
                passwordField.setStyle(passwordField.getStyle() + 
                    "-fx-border-color: #4D4D4D;");
            }
        });
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }
        
        if (username.length() < 3) {
            showError("Username must be at least 3 characters");
            return;
        }
        
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }
        
        try {
            User user = userService.authenticateUser(username, password);
            if (user != null) {
                try {
                    MainController controller = new MainController(user);
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/fxml/MainView.fxml"));
                    loader.setController(controller);
                    Scene scene = new Scene(loader.load());
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    
                    // Create fade out transition
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), mainContainer);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);
                    fadeOut.setOnFinished(e -> {
                        stage.setScene(scene);
                        
                        // Fade in new scene
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), scene.getRoot());
                        fadeIn.setFromValue(0);
                        fadeIn.setToValue(1);
                        fadeIn.play();
                    });
                    fadeOut.play();
                    
                } catch (IOException e) {
                    showError("Error loading main view: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                showError("Invalid username or password");
                passwordField.clear();
            }
        } catch (Exception e) {
            showError("Error during login: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RegisterView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) registerLink.getScene().getWindow();
            
            // Create fade out transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), mainContainer);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                stage.setScene(scene);
                
                // Fade in new scene
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), scene.getRoot());
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fadeOut.play();
            
        } catch (IOException e) {
            showError("Error loading registration view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        
        // Shake animation for error
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(errorLabel.translateXProperty(), 0)),
            new KeyFrame(Duration.millis(100), new KeyValue(errorLabel.translateXProperty(), -10)),
            new KeyFrame(Duration.millis(200), new KeyValue(errorLabel.translateXProperty(), 10)),
            new KeyFrame(Duration.millis(300), new KeyValue(errorLabel.translateXProperty(), -10)),
            new KeyFrame(Duration.millis(400), new KeyValue(errorLabel.translateXProperty(), 0))
        );
        timeline.play();
        
        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), errorLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
    
    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
    }
} 