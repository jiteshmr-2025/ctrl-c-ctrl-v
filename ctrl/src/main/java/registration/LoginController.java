package registration;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import landingpage.LandingPageController; // Make sure this import matches your package
import java.io.IOException;
import java.util.Objects;
import javafx.scene.layout.StackPane;
import utils.WeatherBackgroundManager;

public class LoginController {

    @FXML
    private StackPane rootPane;
    @FXML
    private MediaView weatherView;
    @FXML
    private Label weatherLabel;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private final UserManager userManager = new UserManager();
    private MediaPlayer mediaPlayer; // Keep reference to prevent garbage collection

    @FXML
    public void initialize() {
        // 1. Set default loading state
        weatherLabel.setText("Loading weather...");

        // 2. Run the Network/File logic in a BACKGROUND Thread
        new Thread(() -> {

            // A. Heavy lifting (Network call) happens here, off the UI thread
            String weather = WeatherBackgroundManager.getCurrentWeather();
            String videoFile = WeatherBackgroundManager.getVideoFileForWeather(weather);

            // B. Update the UI on the JavaFX Application Thread
            javafx.application.Platform.runLater(() -> {
                weatherLabel.setText("Current Weather: " + weather);
                playVideo(videoFile); // Play video only after we know which one

                // Resizing logic
                if (rootPane != null && weatherView != null) {
                    weatherView.fitWidthProperty().bind(rootPane.widthProperty());
                    weatherView.fitHeightProperty().bind(rootPane.heightProperty());
                    weatherView.setPreserveRatio(false);
                }
            });

        }).start();
    }

    // --- VIDEO HELPER ---
    private void playVideo(String fileName) {
        try {
            // Load video from resources/videos folder
            String path = Objects.requireNonNull(getClass().getResource("/assets/" + fileName)).toExternalForm();

            // Cleanup old player if exists
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
            }

            mediaPlayer = new MediaPlayer(new Media(path));
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop forever
            mediaPlayer.setMute(true); // Mute sound
            mediaPlayer.setAutoPlay(true);

            if (weatherView != null) {
                weatherView.setMediaPlayer(mediaPlayer);
            }

        } catch (Exception e) {
            System.out.println("Error loading video: " + fileName);
            e.printStackTrace();
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
