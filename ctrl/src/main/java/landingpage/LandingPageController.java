package landingpage;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import registration.UserSession;
import utils.WeatherBackgroundManager; 

public class LandingPageController {

    @FXML private StackPane rootPane;
    @FXML private MediaView weatherView;
    @FXML private Label welcomeMessageLabel; // This will now hold JUST the name
    @FXML private Label dateTimeLabel;

    private MediaPlayer mediaPlayer;
    private String currentUserName = "User"; 

    @FXML
    public void initialize() {
        startDynamicClock();
        
        // Load weather video in background
        new Thread(() -> {
            String weather = WeatherBackgroundManager.getCurrentWeather();
            String videoFile = WeatherBackgroundManager.getVideoFileForWeather(weather);
            
            Platform.runLater(() -> {
                playVideo(videoFile);
                setupResizing();
            });
        }).start();
    }

    public void setUserName(String name) {
        this.currentUserName = name;
        // FIX: Display ONLY the name in uppercase (The "Good Morning" is static in FXML)
        if (welcomeMessageLabel != null) {
            welcomeMessageLabel.setText(name.toUpperCase() + "."); 
        }
    }

    private void playVideo(String fileName) {
        try {
            // FIX: Robust resource loading
            URL mediaUrl = getClass().getResource("/assets/" + fileName);
            if (mediaUrl == null) {
                System.out.println("VIDEO NOT FOUND: " + fileName);
                return; 
            }

            if (mediaPlayer != null) mediaPlayer.dispose();

            mediaPlayer = new MediaPlayer(new Media(mediaUrl.toExternalForm()));
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setMute(true); // Mute audio
            mediaPlayer.setAutoPlay(true);
            
            weatherView.setMediaPlayer(mediaPlayer);
            
        } catch (Exception e) {
            System.out.println("Error loading video: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupResizing() {
        if (rootPane != null && weatherView != null) {
            // Force video to fill the screen
            weatherView.fitWidthProperty().bind(rootPane.widthProperty());
            weatherView.fitHeightProperty().bind(rootPane.heightProperty());
            weatherView.setPreserveRatio(false); // Stretch to fill
        }
    }

    private void startDynamicClock() {
        // Updated Format: "WEDNESDAY, DECEMBER 24 | 2:09 AM"
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
    
    // --- Navigation Methods (Keep existing) ---
    @FXML
    protected void handleNewEntry() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/journalpage/JournalEditor.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // 1. Clear Session
        UserSession.getInstance().logout();

        try {
            // 2. Load Login View
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registration/Login.fxml"));
            Parent root = loader.load();

            // 3. Get Current Stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 4. Set Scene
            stage.setScene(new Scene(root));

            // 5. FORCE FULL SCREEN (This fixes the "tiny window" issue)
            stage.setFullScreenExitHint(""); // Optional: Hide the "Press ESC" message
            stage.setFullScreen(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}