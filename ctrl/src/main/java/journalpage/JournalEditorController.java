package journalpage;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import registration.UserSession;
import mood.MoodAnalyzer;
import utils.WeatherBackgroundManager;

import java.time.LocalDate;

public class JournalEditorController {

    @FXML private DatePicker datePicker;
    @FXML private TextArea journalTextArea;
    @FXML private Label weatherLabel;
    @FXML private Label moodLabel;
    @FXML private Label statusLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private LocalDate selectedDate;

    @FXML
    public void initialize() {
        // Set default date to today
        selectedDate = LocalDate.now();
        datePicker.setValue(selectedDate);
        
        // Add listener for date changes
        datePicker.setOnAction(event -> {
            selectedDate = datePicker.getValue();
            loadJournalForDate(selectedDate);
        });
        
        // Load journal for today
        loadJournalForDate(selectedDate);
        
        // Update weather in background
        updateWeather();
    }

    private void loadJournalForDate(LocalDate date) {
        if (date == null) return;
        
        String entry = journalApp.readJournal(date);
        if (entry != null) {
            journalTextArea.setText(entry);
            statusLabel.setText("Loaded journal for " + date);
            analyzeMood();
        } else {
            journalTextArea.clear();
            statusLabel.setText("No journal found for " + date + ". Create a new entry.");
            moodLabel.setText("Mood: -");
        }
    }

    private void updateWeather() {
        new Thread(() -> {
            String weather = WeatherBackgroundManager.getCurrentWeather();
            Platform.runLater(() -> {
                weatherLabel.setText("Weather: " + weather);
            });
        }).start();
    }

    private void analyzeMood() {
        String entry = journalTextArea.getText();
        if (entry == null || entry.trim().isEmpty()) {
            moodLabel.setText("Mood: -");
            return;
        }
        
        new Thread(() -> {
            try {
                String moodResult = MoodAnalyzer.analyzeMood(entry);
                Platform.runLater(() -> {
                    moodLabel.setText("Mood: " + moodResult);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    moodLabel.setText("Mood: Analysis failed");
                });
            }
        }).start();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String entry = journalTextArea.getText();
        if (entry == null || entry.trim().isEmpty()) {
            statusLabel.setText("Cannot save empty journal entry!");
            return;
        }

        // Show saving status
        saveButton.setDisable(true);
        statusLabel.setText("Saving...");

        new Thread(() -> {
            try {
                // Get weather
                String weather = WeatherBackgroundManager.getCurrentWeather();
                
                // Analyze mood
                java.util.concurrent.atomic.AtomicReference<String> fullMoodRef = new java.util.concurrent.atomic.AtomicReference<>("Unknown");
                String chartMood = "Unknown";
                try {
                    String analyzed = MoodAnalyzer.analyzeMood(entry);
                    fullMoodRef.set(analyzed);
                    chartMood = extractMoodCategory(analyzed);
                } catch (Exception e) {
                    System.err.println("Mood analysis failed: " + e.getMessage());
                }
                
                // Save to database
                journalApp.saveJournal(selectedDate, entry, weather, chartMood);
                
                Platform.runLater(() -> {
                    statusLabel.setText("Journal saved successfully!");
                    moodLabel.setText("Mood: " + fullMoodRef.get());
                    weatherLabel.setText("Weather: " + weather);
                    saveButton.setDisable(false);
                    
                    // Close after 1 second
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                            Platform.runLater(() -> handleClose(event));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error saving journal: " + e.getMessage());
                    saveButton.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleAnalyzeMood(ActionEvent event) {
        analyzeMood();
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private String extractMoodCategory(String fullMood) {
        if (fullMood == null) {
            return "Unknown";
        }
        if (fullMood.contains("(")) {
            return fullMood.substring(0, fullMood.indexOf("(")).trim();
        }
        return fullMood;
    }
}
