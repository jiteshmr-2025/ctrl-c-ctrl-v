package landingpage;
/**
 *
 * @author mingdao
 * @author ekitstrap
 */
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import registration.UserSession;
import utils.GlobalVideoManager; // Import shared manager
import utils.WeatherBackgroundManager;
import welcome.welcome;

public class LandingPageController {

    @FXML private StackPane rootPane;
    
    // NEW: Container for the shared video (Replaces MediaView)
    @FXML private StackPane videoContainer; 
    
    @FXML private Label welcomeMessageLabel;
    @FXML private Label greetingLabel;       // Displays the GREETING ("Good Morning,")
    @FXML private Label dateTimeLabel;
    @FXML private Label instructionLabel; 

    private String currentUserName = "User";

    @FXML
    public void initialize() {
        startDynamicClock();

        // 1. Setup ESC key handler
        Platform.runLater(() -> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            if (stage != null) {
                setupEscapeKeyHandler(stage);
                rootPane.requestFocus();
            }
        });

        if (instructionLabel != null) {
            instructionLabel.setText("Press ENTER to Start  •  Press ESC to Exit");
        }

        // 2. Setup Enter/Esc Keys
        rootPane.setOnKeyPressed((var event) -> {
            switch (event.getCode()) {
                case ENTER -> handleNewEntry();
                case ESCAPE -> {
                    Platform.exit();
                    System.exit(0);
                }
            }
        });

        // 3. Load Weather Video (The Lag Fix)
        new Thread(() -> {
            LocalDate today = LocalDate.now();
            String weather = WeatherBackgroundManager.getWeatherForDate(today);
            String videoFile = WeatherBackgroundManager.getVideoFileForWeather(weather);

            Platform.runLater(() -> {
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
            // Clear old children, add shared video
            videoContainer.getChildren().clear();
            videoContainer.getChildren().add(sharedView);

            // Bind Size (Responsive Background)
            sharedView.fitWidthProperty().bind(rootPane.widthProperty());
            sharedView.fitHeightProperty().bind(rootPane.heightProperty());
            sharedView.setPreserveRatio(false);
            
            // Send to back so it sits behind the UI
            sharedView.toBack();
        }
    }

    public void setUserName(String name) {
        this.currentUserName = name;
        if (welcomeMessageLabel != null) {
            welcomeMessageLabel.setText(name.toUpperCase() + ".");
        }
        updateGreeting();
    }

    private void setupEscapeKeyHandler(Stage stage) {
        stage.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                Platform.exit();
                System.exit(0);
            }
        });
    }
    
    private void updateGreeting() {
        if (greetingLabel != null && welcomeMessageLabel != null) {
            // Our function, welcome _user returns "Good Morning, Name."
            // But your UI splits this into two labels: "Good Morning," and "ALEX."
            // We strip the name out so we can keep the stylish two-line design.
            String fullGreeting = welcome.welcome_user(""); // Get just "Good Morning, ."
            
            // Clean up the string to remove the trailing " ."
            String prefixOnly = fullGreeting.replace(" .", " "); 
            
            greetingLabel.setText(prefixOnly); // Sets "Good Morning,"
            welcomeMessageLabel.setText(currentUserName.toUpperCase() + "."); // Sets "ALEX."
        }
    }

    private void startDynamicClock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd  |  h:mm a");

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("GMT+8"));
            if (dateTimeLabel != null) {
                dateTimeLabel.setText(now.format(formatter).toUpperCase());
            }
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    @FXML
    protected void handleNewEntry() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/journalpage/JournalEditor.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene newScene = new Scene(root);
            stage.setScene(newScene);

            stage.setFullScreenExitHint("");
            stage.setFullScreen(true);

        } catch (IOException e) {
        }
    }

    @FXML
    protected void handleViewSummary() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/summary/Summary.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene newScene = new Scene(root);
            stage.setScene(newScene);

            stage.setFullScreenExitHint("");
            stage.setFullScreen(true);

        } catch (IOException e) {
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        UserSession.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registration/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreenExitHint("");
            stage.setFullScreen(true);
        } catch (IOException e) {
        }
    }
}