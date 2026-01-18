package summary;
/**
 *
 * @author ekitstrap
 * @author mingdao
 */
import java.io.IOException;
import javafx.application.Platform; // Added for runLater
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode; // Added for KeyCode
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import registration.UserSession;
import summary.SummaryPage.SummaryData;
import landingpage.LandingPageController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class SummaryController {

    @FXML private Label dateRangeLabel;
    
    // Left Pane (Timeline)
    @FXML private VBox timelineContainer;
    
    // Right Pane (Stats)
    @FXML private Label dominantMoodLabel;
    @FXML private Label dominantMoodSubtext;
    @FXML private Label weatherSummaryLabel;
    @FXML private Label aiQuoteLabel;

    @FXML
    public void initialize() {
        try {
            // Get current user email
            String userEmail = "guest@local"; 
            if (UserSession.getInstance().getCurrentUser() != null) {
                userEmail = UserSession.getInstance().getCurrentUser().getEmail();
            }
            
            // Set Date Range Text
            LocalDate today = LocalDate.now();
            LocalDate weekAgo = today.minusDays(6);
            DateTimeFormatter rangeFmt = DateTimeFormatter.ofPattern("MMM dd");
            dateRangeLabel.setText(weekAgo.format(rangeFmt) + " - " + today.format(rangeFmt));

            // Load Data
            loadSummaryData(userEmail);

            // ---------------------------------------------------------
            // NEW: Escape Key Handler
            // ---------------------------------------------------------
            Platform.runLater(() -> {
                // We can use any node to get the scene, timelineContainer is a safe bet
                Scene scene = timelineContainer.getScene();
                if (scene != null) {
                    scene.setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ESCAPE) {
                            returnToLandingPage();
                        }
                    });
                    // Ensure the scene has focus so it catches the key event
                    timelineContainer.requestFocus();
                }
            });

        } catch (Exception e) {
            System.err.println("Error initializing summary: " + e.getMessage());
        }
    }

    // --- NAVIGATION HELPER (NEW) ---
    private void returnToLandingPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LandingPage.fxml"));
            Parent root = loader.load();
            
            // Get stage from any UI element
            Stage stage = (Stage) timelineContainer.getScene().getWindow();
            
            LandingPageController controller = loader.getController();
            if (UserSession.getInstance().getCurrentUser() != null) {
                controller.setUserName(UserSession.getInstance().getCurrentUser().getDisplayName());
            }
            
            stage.setScene(new Scene(root));
            stage.setFullScreenExitHint("");
            stage.setFullScreen(true);
            
        } catch (IOException e) {
            System.err.println("Error loading Landing Page.");
        }
    }

    private void loadSummaryData(String userEmail) {
        // 1. Fetch Aggregates for Right Panel
        SummaryData summaryData = SummaryPage.getWeeklySummaryData(userEmail);
        updateInsightsPanel(summaryData);

        // 2. Fetch Raw Entries for Left Panel
        List<String[]> entries = SummaryPage.getWeeklyJournalEntries(userEmail);
        populateTimeline(entries);
    }

    private void updateInsightsPanel(SummaryData data) {
        if (data.getTotalEntries() == 0) {
            dominantMoodLabel.setText("No Data");
            dominantMoodSubtext.setText("Start journaling today!");
            weatherSummaryLabel.setText("--");
            aiQuoteLabel.setText("\"Your story begins with the first entry.\"");
            return;
        }

        // --- Calculate Dominant Mood ---
        String topMood = "Neutral";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : data.getMoodCounts().entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                topMood = entry.getKey();
            }
        }
        int moodPercentage = (int) ((maxCount * 100.0) / data.getTotalEntries());
        dominantMoodLabel.setText(topMood);
        dominantMoodSubtext.setText(moodPercentage + "% of the week");

        // --- Calculate Top Weather ---
        String topWeather = "Unknown";
        int maxWeatherCount = 0;
        for (Map.Entry<String, Integer> entry : data.getWeatherCounts().entrySet()) {
            if (entry.getValue() > maxWeatherCount) {
                maxWeatherCount = entry.getValue();
                topWeather = entry.getKey();
            }
        }
        weatherSummaryLabel.setText(maxWeatherCount + " days of " + topWeather);
        
        // --- Set Quote ---
        setContextualQuote(topMood);
    }

    private void setContextualQuote(String mood) {
        if (mood.equalsIgnoreCase("Positive") || mood.equalsIgnoreCase("Happy")) {
            aiQuoteLabel.setText("\"A fantastic week! Keep riding this wave of positivity.\"");
        } else if (mood.equalsIgnoreCase("Negative") || mood.equalsIgnoreCase("Terrible")) {
            aiQuoteLabel.setText("\"A tough week, but you made it through. Storms don't last forever.\"");
        } else {
            aiQuoteLabel.setText("\"A balanced week. Calm and steady progression.\"");
        }
    }

    private void populateTimeline(List<String[]> entries) {
        timelineContainer.getChildren().clear();

        DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dayNameFmt = DateTimeFormatter.ofPattern("EEE"); 
        DateTimeFormatter dayNumFmt = DateTimeFormatter.ofPattern("dd");  

        for (String[] entry : entries) {
            try {
                // Parse Data: [0]=date, [3]=weather, [4]=mood
                LocalDate date = LocalDate.parse(entry[0], dbFormatter);
                String weather = entry[3];
                String mood = entry[4];

                // Create Row Container
                HBox row = new HBox();
                row.getStyleClass().add("ledger-row");
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                // --- Date Column ---
                Label dayName = new Label(date.format(dayNameFmt).toUpperCase());
                dayName.getStyleClass().add("date-column");
                dayName.setPrefWidth(50);

                Label dayNum = new Label(date.format(dayNumFmt));
                dayNum.getStyleClass().add("date-number");
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                // --- Data Column ---
                HBox dataGroup = new HBox(20); 
                dataGroup.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label moodLabel = new Label(getMoodIcon(mood) + " " + mood);
                moodLabel.getStyleClass().add("entry-text-mood");

                Label weatherLabel = new Label(getWeatherIcon(weather) + " " + weather);
                weatherLabel.getStyleClass().add("entry-text-weather");

                dataGroup.getChildren().addAll(moodLabel, weatherLabel);

                row.getChildren().addAll(dayName, dayNum, spacer, dataGroup);
                timelineContainer.getChildren().add(row);

            } catch (Exception e) {
                System.err.println("Skipping invalid entry: " + e.getMessage());
            }
        }
    }

    private String getMoodIcon(String mood) {
        if (mood == null) return "😐";
        String m = mood.toLowerCase();
        if (m.contains("pos") || m.contains("happy") || m.contains("good")) return "🙂";
        if (m.contains("neg") || m.contains("sad") || m.contains("terrible")) return "🙁";
        return "😐";
    }

    private String getWeatherIcon(String weather) {
        if (weather == null) return "❓";
        String w = weather.toLowerCase();
        if (w.contains("storm") || w.contains("thunder")) return "⛈️";
        if (w.contains("rain") || w.contains("drizzle")) return "🌧️";
        if (w.contains("cloud")) return "☁️";
        if (w.contains("sun") || w.contains("clear")) return "☀️";
        if (w.contains("snow")) return "❄️";
        return "🌡️";
    }

    @FXML
    private void handleClose(ActionEvent event) {
        // Now uses the helper method
        returnToLandingPage();
    }
}