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
import utils.GlobalVideoManager; // Import the new manager
import utils.WeatherBackgroundManager;
import java.io.IOException;

public class LoginController {

    @FXML
    private StackPane rootPane;

    // CHANGE 1: Use a Container (StackPane) instead of MediaView directly in FXML
    @FXML
    private StackPane videoContainer;

    @FXML
    private Label weatherLabel;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private final UserManager userManager = new UserManager();

    @FXML
    public void initialize() {
        weatherLabel.setText("Loading weather...");

        // 1. Fetch Weather & Video (Run in background to keep UI snappy)
        new Thread(() -> {
            String weather = WeatherBackgroundManager.getCurrentWeather();
            String videoFile = WeatherBackgroundManager.getVideoFileForWeather(weather);

            // 2. Update Video Manager (It won't reload if the video is the same!)
            Platform.runLater(() -> {
                weatherLabel.setText("Current Weather: " + weather);

                // Initialize the manager
                GlobalVideoManager.updateWeatherVideo(videoFile);

                // Attach the shared view to THIS screen
                attachVideoToBackground();
            });
        }).start();
    }

    private void attachVideoToBackground() {
        var sharedView = GlobalVideoManager.getSharedMediaView();

        if (sharedView != null && videoContainer != null) {
            // Add the view to our container
            videoContainer.getChildren().clear();
            videoContainer.getChildren().add(sharedView);

            // Bind Size (Make it responsive)
            sharedView.fitWidthProperty().bind(rootPane.widthProperty());
            sharedView.fitHeightProperty().bind(rootPane.heightProperty());
            sharedView.setPreserveRatio(false);

            // Send to back so it's behind the glass card
            sharedView.toBack();
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Basic Validation
        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both email and password.");
            errorLabel.setVisible(true);
            return;
        }

        // 1. Check credentials
        User user = userManager.login(email, password);

        if (user != null) {
            // 2. Save Session
            UserSession.getInstance().saveSession(user);
            System.out.println("Login Successful: " + user.getDisplayName());

            // 3. Get the Stage (Window) from the event button
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 4. Navigate to Landing Page (Pass Stage + Name)
            goToLandingPage(stage, user.getDisplayName());

        } else {
            errorLabel.setText("Invalid email or password.");
            errorLabel.setVisible(true);
        }
    }

    private void goToLandingPage(Stage stage, String userName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LandingPage.fxml"));
            Parent root = loader.load();

            // Pass the username to the new controller
            landingpage.LandingPageController controller = loader.getController();
            controller.setUserName(userName);

            // Set the scene
            stage.setScene(new Scene(root));

            // Force Full Screen (Fixes the "tiny window" bug)
            stage.setFullScreenExitHint("");
            stage.setFullScreen(true);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading LandingPage.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void switchToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registration/Register.fxml"));
            Parent root = loader.load();

            // Get current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);

            // --- THE CRITICAL FIX ---
            // JavaFX tries to exit fullscreen on scene change. We force it back.
            stage.setFullScreen(true);
            // ------------------------

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
