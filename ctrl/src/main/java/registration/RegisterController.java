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
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import utils.WeatherBackgroundManager; // Ensure this import is correct

import java.io.IOException;
import java.util.Objects;

public class RegisterController {

    // --- UI Elements ---
    @FXML private StackPane rootPane;       // Required for resizing video
    @FXML private MediaView weatherView;    // The background video
    @FXML private Label weatherLabel;       // Shows "Current Weather: Rain"
    
    @FXML private TextField regEmailField;
    @FXML private TextField regNameField;
    @FXML private PasswordField regPasswordField;
    @FXML private Label errorLabel;

    private final UserManager userManager = new UserManager();
    private MediaPlayer mediaPlayer; // Keep reference to prevent garbage collection

    // --- INITIALIZATION (Runs when screen loads) ---
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

    // --- REGISTRATION LOGIC (Existing) ---
    @FXML
    private void handleRegister(ActionEvent event) {
        String email = regEmailField.getText().trim();
        String name = regNameField.getText().trim();
        String password = regPasswordField.getText();

        // 1. Validation Logic
        if (email.isEmpty() || !email.contains(".com")) {
            showError("Invalid email. Must contain '.com'.");
            return;
        }
        if (name.isEmpty()) {
            showError("Display name cannot be empty.");
            return;
        }
        if (password.isEmpty()) {
            showError("Password cannot be empty.");
            return;
        }

        // 2. Check if email exists
        if (userManager.emailExists(email)) {
            showError("Email already registered.");
            return;
        }

        // 3. Register User
        boolean success = userManager.register(email, name, password);

        if (success) {
            System.out.println("Registration Successful for: " + name);
            switchToLogin(event);
        } else {
            showError("Registration failed. Please try again.");
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
            stage.setFullScreen(true); // Keep it full screen

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}