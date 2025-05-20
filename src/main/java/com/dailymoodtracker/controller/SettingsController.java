package com.dailymoodtracker.controller;

import com.dailymoodtracker.service.SentimentServiceFactory;
import com.dailymoodtracker.service.SentimentServiceFactory.ServiceType;
import com.dailymoodtracker.service.PythonSentimentService;
import com.dailymoodtracker.service.HttpSentimentService;
import com.dailymoodtracker.utils.AlertHelper;
import com.dailymoodtracker.utils.SceneManager;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the settings panel.
 */
public class SettingsController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    @FXML private VBox rootVBox;
    @FXML private RadioButton rbPython;
    @FXML private RadioButton rbHttp;
    @FXML private RadioButton rbDummy;
    @FXML private Label lblPythonStatus;
    @FXML private Label lblHttpStatus;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private ToggleGroup serviceToggleGroup;
    private SceneManager sceneManager;
    private boolean changesUnsaved = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sceneManager = SceneManager.getInstance();
        setupToggleGroup();
        refreshServiceStatus();
    }

    /**
     * Set up radio buttons toggle group for service selection
     */
    private void setupToggleGroup() {
        serviceToggleGroup = new ToggleGroup();
        rbPython.setToggleGroup(serviceToggleGroup);
        rbHttp.setToggleGroup(serviceToggleGroup);
        rbDummy.setToggleGroup(serviceToggleGroup);

        // Set the initial selection based on current service
        ServiceType currentServiceType = SentimentServiceFactory.getServiceType();
        switch (currentServiceType) {
            case PYTHON:
                rbPython.setSelected(true);
                break;
            case HTTP:
                rbHttp.setSelected(true);
                break;
            case DUMMY:
                rbDummy.setSelected(true);
                break;
        }

        // Listen for changes
        serviceToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            changesUnsaved = true;
        });
    }

    /**
     * Refresh the status of service configurations
     */
    private void refreshServiceStatus() {
        // Check Python service availability
        boolean pythonAvailable = false;
        try {
            PythonSentimentService pythonService = PythonSentimentService.getInstance();
            pythonAvailable = pythonService.isServiceAvailable();
            lblPythonStatus.setText(pythonAvailable ? "Status: Available" : "Status: Not available");
            lblPythonStatus.setStyle(pythonAvailable ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
        } catch (Exception e) {
            logger.error("Error checking Python service: {}", e.getMessage());
            lblPythonStatus.setText("Status: Error checking service");
            lblPythonStatus.setStyle("-fx-text-fill: red;");
        }
        
        // Check HTTP service availability
        boolean httpAvailable = false;
        try {
            HttpSentimentService httpService = HttpSentimentService.getInstance();
            httpAvailable = httpService.isServiceAvailable();
            lblHttpStatus.setText(httpAvailable ? "Status: Available" : "Status: Not available");
            lblHttpStatus.setStyle(httpAvailable ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
        } catch (Exception e) {
            logger.error("Error checking HTTP service: {}", e.getMessage());
            lblHttpStatus.setText("Status: Error checking service");
            lblHttpStatus.setStyle("-fx-text-fill: red;");
        }
        
        // Enable/disable options based on configuration
        rbPython.setDisable(!pythonAvailable);
        rbHttp.setDisable(!httpAvailable);
        
        // If the selected service is not available, switch to an available one
        if ((rbPython.isSelected() && !pythonAvailable) ||
            (rbHttp.isSelected() && !httpAvailable)) {
            
            if (httpAvailable) {
                rbHttp.setSelected(true);
            } else if (pythonAvailable) {
                rbPython.setSelected(true);
            } else {
                rbDummy.setSelected(true);
            }
        }
    }

    /**
     * Save button action
     */
    @FXML
    private void handleSave() {
        try {
            ServiceType selectedServiceType = getSelectedServiceType();
            
            // Apply the selected service
            SentimentServiceFactory.setServiceType(selectedServiceType);
            logger.info("Sentiment service changed to: {}", selectedServiceType);
            
            // Show confirmation
            AlertHelper.showInformation("Settings Saved", 
                    "Sentiment Analysis service has been set to: " + selectedServiceType);
            
            changesUnsaved = false;
            closeWindow();
        } catch (Exception e) {
            logger.error("Error saving settings: {}", e.getMessage(), e);
            AlertHelper.showError("Error", "Failed to save settings: " + e.getMessage());
        }
    }

    /**
     * Cancel button action
     */
    @FXML
    private void handleCancel() {
        if (changesUnsaved) {
            boolean confirm = AlertHelper.showConfirmation("Unsaved Changes", 
                    "You have unsaved changes. Are you sure you want to exit?");
            if (!confirm) {
                return;
            }
        }
        closeWindow();
    }

    /**
     * Get the selected service type
     */
    private ServiceType getSelectedServiceType() {
        if (rbPython.isSelected()) {
            return ServiceType.PYTHON;
        } else if (rbHttp.isSelected()) {
            return ServiceType.HTTP;
        } else {
            return ServiceType.DUMMY;
        }
    }

    /**
     * Close the settings window
     */
    private void closeWindow() {
        Stage stage = (Stage) rootVBox.getScene().getWindow();
        stage.close();
    }
} 