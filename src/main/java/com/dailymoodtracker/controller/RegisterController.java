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

public class RegisterController {
    @FXML private VBox mainContainer;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Hyperlink loginLink;
    @FXML private Label errorLabel;
    @FXML private Label titleLabel;
    
    private final UserService userService;
    
    public RegisterController() {
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
        registerButton.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), registerButton);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
            
            registerButton.setStyle(registerButton.getStyle() + 
                "-fx-background-color: #45a049;"); // Darker green on hover
        });

        registerButton.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), registerButton);
            scale.setToX(1);
            scale.setToY(1);
            scale.play();
            
            registerButton.setStyle(registerButton.getStyle() + 
                "-fx-background-color: #4CAF50;"); // Original green
        });

        // Text field focus animations
        setupTextFieldAnimation(usernameField);
        setupTextFieldAnimation(emailField);
        setupTextFieldAnimation(passwordField);
        setupTextFieldAnimation(confirmPasswordField);
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
        // Username validation
        usernameField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() < 3) {
                usernameField.setStyle(usernameField.getStyle() + 
                    "-fx-border-color: #FF5252;");
            } else {
                usernameField.setStyle(usernameField.getStyle() + 
                    "-fx-border-color: #4D4D4D;");
            }
        });

        // Email validation
        emailField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                emailField.setStyle(emailField.getStyle() + 
                    "-fx-border-color: #FF5252;");
            } else {
                emailField.setStyle(emailField.getStyle() + 
                    "-fx-border-color: #4D4D4D;");
            }
        });

        // Password validation
        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() < 6) {
                passwordField.setStyle(passwordField.getStyle() + 
                    "-fx-border-color: #FF5252;");
            } else {
                passwordField.setStyle(passwordField.getStyle() + 
                    "-fx-border-color: #4D4D4D;");
            }
            validatePasswordMatch();
        });

        // Confirm password validation
        confirmPasswordField.textProperty().addListener((obs, oldText, newText) -> {
            validatePasswordMatch();
        });
    }
    
    private void validatePasswordMatch() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (!confirmPassword.isEmpty() && !password.equals(confirmPassword)) {
            confirmPasswordField.setStyle(confirmPasswordField.getStyle() + 
                "-fx-border-color: #FF5252;");
        } else {
            confirmPasswordField.setStyle(confirmPasswordField.getStyle() + 
                "-fx-border-color: #4D4D4D;");
        }
    }
    
    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validate all fields
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }
        
        if (username.length() < 3) {
            showError("Username must be at least 3 characters");
            return;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Please enter a valid email address");
            return;
        }
        
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }
        
        try {
            User newUser = userService.registerUser(username, email, password);
            if (newUser != null) {
                showSuccess("Registration successful! Please log in.");
                // Wait a moment before transitioning to login
                PauseTransition pause = new PauseTransition(Duration.seconds(1));
                pause.setOnFinished(e -> handleLogin());
                pause.play();
            } else {
                showError("Registration failed. Please try again.");
            }
        } catch (Exception e) {
            showError("Error during registration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) loginLink.getScene().getWindow();
            
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
            showError("Error loading login view");
        }
    }
    
    private void showError(String message) {
        errorLabel.setStyle("-fx-text-fill: #FF5252;");
        showMessage(message);
    }
    
    private void showSuccess(String message) {
        errorLabel.setStyle("-fx-text-fill: #4CAF50;");
        showMessage(message);
    }
    
    private void showMessage(String message) {
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
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
} 