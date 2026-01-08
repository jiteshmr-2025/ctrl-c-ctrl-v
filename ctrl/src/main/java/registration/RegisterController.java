package registration;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import utils.GlobalVideoManager; // Import the shared manager
import utils.WeatherBackgroundManager;
import java.io.IOException;

public class RegisterController {

    @FXML
    private StackPane rootPane;

    // NEW: Container for the shared video
    @FXML
    private StackPane videoContainer;

    @FXML
    private Label weatherLabel;
    @FXML
    private TextField regNameField;
    @FXML
    private TextField regEmailField;
    @FXML
    private PasswordField regPasswordField;
    @FXML
    private Label errorLabel;

    private final UserManager userManager = new UserManager();

    @FXML
    public void initialize() {
        weatherLabel.setText("Loading weather...");

        // 1. Run in background to avoid UI freeze
        new Thread(() -> {
            String weather = WeatherBackgroundManager.getCurrentWeather();
            String videoFile = WeatherBackgroundManager.getVideoFileForWeather(weather);

            // 2. Update UI on JavaFX Thread
            Platform.runLater(() -> {
                weatherLabel.setText("Current Weather: " + weather);

                // Initialize/Update the shared video manager
                GlobalVideoManager.updateWeatherVideo(videoFile);

                // Attach the shared view to THIS screen
                attachVideoToBackground();
            });
        }).start();
    }

    private void attachVideoToBackground() {
        var sharedView = GlobalVideoManager.getSharedMediaView();

        if (sharedView != null && videoContainer != null) {
            // Clear any existing children and add the shared view
            videoContainer.getChildren().clear();
            videoContainer.getChildren().add(sharedView);

            // Bind Size (Responsive Background)
            sharedView.fitWidthProperty().bind(rootPane.widthProperty());
            sharedView.fitHeightProperty().bind(rootPane.heightProperty());
            sharedView.setPreserveRatio(false);

            // Send to back so it sits behind the vignette
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

            // 2. Auto-Login (Optional) or Redirect to Login
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

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);

            // Force Full Screen to prevent window resizing glitch
            stage.setFullScreen(true);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Login.fxml");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
