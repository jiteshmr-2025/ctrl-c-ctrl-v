package registration;
/**
 *
 * @author ekitstrap
 * @author zayn
 */
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

public class LoginController {

    @FXML private StackPane rootPane;
    @FXML private StackPane videoContainer;
    @FXML private Label weatherLabel;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserManager userManager = new UserManager();

    @FXML
    public void initialize() {
        weatherLabel.setText("Loading weather...");

        // 1. Fetch Weather & Video
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
                    // ENTER: Trigger Login
                    else if (event.getCode() == KeyCode.ENTER) {
                        handleLogin(null); // Pass null because we refactored handleLogin to not need the event source
                    }
                });
                
                // Optional: Request focus on email field by default
                emailField.requestFocus();
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
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both email and password.");
            errorLabel.setVisible(true);
            return;
        }

        User user = userManager.login(email, password);

        if (user != null) {
            UserSession.getInstance().saveSession(user);
            System.out.println("Login Successful: " + user.getDisplayName());

            // FIX: Get Stage from a specific node (emailField) instead of the event source.
            // This ensures it works even if 'event' is null (triggered by Enter key).
            Stage stage = (Stage) emailField.getScene().getWindow();

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

            landingpage.LandingPageController controller = loader.getController();
            controller.setUserName(userName);

            stage.setScene(new Scene(root));
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
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setFullScreen(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}