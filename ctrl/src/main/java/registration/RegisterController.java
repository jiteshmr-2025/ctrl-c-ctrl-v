package registration;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode; // Import KeyCode
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import utils.GlobalVideoManager;
import utils.WeatherBackgroundManager;
import java.io.IOException;

public class RegisterController {

    @FXML private StackPane rootPane;
    @FXML private StackPane videoContainer;
    @FXML private Label weatherLabel;
    @FXML private TextField regNameField;
    @FXML private TextField regEmailField;
    @FXML private PasswordField regPasswordField;
    @FXML private Label errorLabel;

    private final UserManager userManager = new UserManager();

    @FXML
    public void initialize() {
        weatherLabel.setText("Loading weather...");

        // 1. Run in background to avoid UI freeze
        new Thread(() -> {
            String weather = WeatherBackgroundManager.getCurrentWeather();
            String videoFile = WeatherBackgroundManager.getVideoFileForWeather(weather);

            Platform.runLater(() -> {
                weatherLabel.setText("Current Weather: " + weather);
                GlobalVideoManager.updateWeatherVideo(videoFile);
                attachVideoToBackground();
            });
        }).start();

        // 2. Add Key Listeners (Escape & Enter)
        Platform.runLater(() -> {
            Scene scene = rootPane.getScene();
            if (scene != null) {
                scene.setOnKeyPressed(event -> {
                    // ESCAPE: Close the application
                    if (event.getCode() == KeyCode.ESCAPE) {
                        Platform.exit();
                        System.exit(0);
                    }
                    // ENTER: Trigger Registration
                    else if (event.getCode() == KeyCode.ENTER) {
                        handleRegister(null); // Pass null as we refactored the method
                    }
                });
                
                // Optional: Focus the Name field first
                regNameField.requestFocus();
            }
        });
    }

    private void attachVideoToBackground() {
        var sharedView = GlobalVideoManager.getSharedMediaView();

        if (sharedView != null && videoContainer != null) {
            videoContainer.getChildren().clear();
            videoContainer.getChildren().add(sharedView);
            sharedView.fitWidthProperty().bind(rootPane.widthProperty());
            sharedView.fitHeightProperty().bind(rootPane.heightProperty());
            sharedView.setPreserveRatio(false);
            sharedView.toBack();
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String name = regNameField.getText().trim();
        String email = regEmailField.getText().trim();
        String password = regPasswordField.getText().trim();

        // Basic Validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("All fields are required.");
            errorLabel.setVisible(true);
            return;
        }

        // 1. Attempt Registration
        boolean success = userManager.register(email, name, password);

        if (success) {
            System.out.println("Registration Successful: " + name);
            // 2. Redirect to Login
            switchToLogin(event);
        } else {
            errorLabel.setText("Email already exists.");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void switchToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registration/Login.fxml"));
            Parent root = loader.load();

            // FIX: Get stage from a UI element (regNameField) so it works with Enter key
            Stage stage = (Stage) regNameField.getScene().getWindow();
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setFullScreen(true);

        } catch (IOException e) {
            System.err.println("Error loading Login.fxml");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}