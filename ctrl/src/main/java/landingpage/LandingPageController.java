package landingpage;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;

// --- IMPORTS FOR DYNAMIC TEXT ---
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
// ------------------------------------

import java.io.IOException;
import javafx.scene.control.Button;
import weather.API_Get;
import static weather.API_Get.translateForecast;
import static welcome.welcome.welcome_user;

import org.json.JSONArray;
import org.json.JSONObject;

public class LandingPageController {

    // --- FXML References ---
    @FXML
    private StackPane rootPane;
    @FXML
    private ImageView backgroundImage;
    @FXML
    private Pane backgroundOverlay;
    @FXML
    private Label welcomeMessageLabel;
    @FXML
    private Label dateTimeLabel;

    // --- State Variables ---
    private String currentUserName = "Guest"; // Default until LoginController sets it

    // --- Constants ---
    private static final Image SUNNY_IMAGE = new Image("https://images.unsplash.com/photo-1470770841072-f978cf4d019e?fit=crop&w=1920&q=80");
    private static final Image NO_RAIN_IMAGE = new Image("https://images.unsplash.com/photo-1506744038136-46273834b3fb?fit=crop&w=1920&q=80");
    private static final Image PARTLY_CLOUDY_IMAGE = new Image("https://images.unsplash.com/photo-1517685352821-92088833b3bu?fit=crop&w=1920&q=80");
    private static final Image CLOUDY_IMAGE = new Image("https://images.unsplash.com/photo-1499951360447-b19be8fe80f5?fit=crop&w=1920&q=80");
    private static final Image DRIZZLE_IMAGE = new Image("https://images.unsplash.com/photo-1493130942363-63310e9c426f?fit=crop&w=1920&q=80");
    private static final Image RAIN_IMAGE = new Image("https://images.unsplash.com/photo-1515694346937-94d85e41e622?fit=crop&w=1920&q=80");
    private static final Image HEAVY_RAIN_IMAGE = new Image("https://images.unsplash.com/photo-1428592953211-077101b2ddd2?fit=crop&w=1920&q=80");
    private static final Image THUNDERSTORM_IMAGE = new Image("https://images.unsplash.com/photo-1472145246862-b24cf25c4a36?fit=crop&w=1920&q=80");

    @FXML
    public void initialize() {
        // 1. Responsive Bindings for Fullscreen
        backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());
        backgroundOverlay.prefWidthProperty().bind(rootPane.widthProperty());
        backgroundOverlay.prefHeightProperty().bind(rootPane.heightProperty());

        // 2. Fetch Weather & Set Background (Static, runs once)
        String currentWeather = weather_value();
        updateBackgroundForWeather(currentWeather);

        // 3. Start the Dynamic Clock (Runs forever)
        startDynamicClock();
    }

    // --- NEW: Method called by LoginController ---
    public void setUserName(String name) {
        this.currentUserName = name;
        // Update label immediately so user sees it instantly on load
        welcomeMessageLabel.setText(welcome_user(this.currentUserName));
    }

    // --- The Heartbeat ---
    private void startDynamicClock() {
        // Logic specific to your location
        ZoneId zone = ZoneId.of("GMT+8");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, h:mm:ss a");

        // The Timeline loops indefinitely every 1 second
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), event -> {

            // Get current time
            LocalDateTime now = LocalDateTime.now(zone);

            // A. Update the Time Label
            dateTimeLabel.setText(now.format(formatter).toUpperCase());

            // B. Update the Welcome Message
            // Uses 'this.currentUserName' so it stays correct after login
            welcomeMessageLabel.setText(welcome_user(this.currentUserName));
        }));

        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    // --- Existing Weather Logic (Unchanged) ---
    public static String weather_value() {
        API_Get api = new API_Get();
        try {
            String getUrl = "https://api.data.gov.my/weather/forecast/?contains=WP%20Kuala%20Lumpur@location__location_name&sort=date&limit=1";
            String getResponse = api.get(getUrl);
            System.setProperty("file.encoding", "UTF-8");

            JSONArray jsonArray = new JSONArray(getResponse);
            JSONObject firstItem = jsonArray.getJSONObject(0);

            String summary = firstItem.getString("summary_forecast");
            String englishSummary = translateForecast(summary);

            System.out.println("Weather Fetched: " + englishSummary);

            return englishSummary;
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    private void updateBackgroundForWeather(String weather) {
        if (weather == null) {
            weather = "Unknown";
        }

        switch (weather) {
            case "Sunny":
                backgroundImage.setImage(SUNNY_IMAGE);
                backgroundOverlay.setOpacity(0.35);
                break;
            case "No rain":
                backgroundImage.setImage(NO_RAIN_IMAGE);
                backgroundOverlay.setOpacity(0.3);
                break;
            case "Partly cloudy":
                backgroundImage.setImage(PARTLY_CLOUDY_IMAGE);
                backgroundOverlay.setOpacity(0.35);
                break;
            case "Cloudy":
                backgroundImage.setImage(CLOUDY_IMAGE);
                backgroundOverlay.setOpacity(0.4);
                break;
            case "Drizzle":
                backgroundImage.setImage(DRIZZLE_IMAGE);
                backgroundOverlay.setOpacity(0.4);
                break;
            case "Rain":
                backgroundImage.setImage(RAIN_IMAGE);
                backgroundOverlay.setOpacity(0.35);
                break;
            case "Heavy rain":
                backgroundImage.setImage(HEAVY_RAIN_IMAGE);
                backgroundOverlay.setOpacity(0.45);
                break;
            case "Thunderstorms":
                backgroundImage.setImage(THUNDERSTORM_IMAGE);
                backgroundOverlay.setOpacity(0.5);
                break;
            default:
                backgroundImage.setImage(CLOUDY_IMAGE);
                backgroundOverlay.setOpacity(0.4);
                break;
        }
    }

    @FXML
    protected void handleNewEntry() {
        try {
            System.out.println("Opening Journal Editor...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/journalpage/JournalEditor.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("New Journal Entry");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            System.out.println("Error loading Journal View: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        // 1. Clear session
        registration.UserSession.getInstance().logout();

        // 2. Go back to Login Screen
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/registration/Login.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setFullScreen(false);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
