package journalpage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import org.bson.Document; // Import BSON Document
import registration.UserSession;
import mood.MoodAnalyzer;
import utils.WeatherBackgroundManager;
import landingpage.LandingPageController;

public class JournalEditorController {

    @FXML
    private DatePicker datePicker;
    @FXML
    private TextArea journalTextArea;
    @FXML
    private Label weatherLabel;
    @FXML
    private Label moodLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Button saveButton;
    @FXML
    private ListView<TimelineEntry> timelineListView;

    private LocalDate selectedDate;
    private String currentWeather = "Unknown";

    private static class TimelineEntry {

        LocalDate date;
        String preview;

        public TimelineEntry(LocalDate date, String preview) {
            this.date = date;
            this.preview = preview;
        }

        @Override
        public String toString() {
            return date.toString();
        }
    }

    @FXML
    public void initialize() {
        selectedDate = LocalDate.now();
        datePicker.setValue(selectedDate);

        // Handle Date Picking
        datePicker.setOnAction(event -> {
            selectedDate = datePicker.getValue();
            loadJournalForDate(selectedDate);
            selectDateInTimeline(selectedDate);
        });

        setupTimelineCellFactory();
        refreshTimeline();

        // Load initial data
        loadJournalForDate(selectedDate);

        // Handle Timeline Clicks
        timelineListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.date.equals(selectedDate)) {
                datePicker.setValue(newVal.date);
            }
        });
    }

    // --- CORE LOGIC FIXES ---
    private void loadJournalForDate(LocalDate date) {
        if (date == null) {
            return;
        }

        // FIX: Use journalApp to get the document directly
        Document doc = journalApp.getJournalDocument(date);

        if (doc != null) {
            // Existing Entry
            String entryText = doc.getString("entry");
            String weather = doc.getString("weather");
            String mood = doc.getString("mood");

            journalTextArea.setText(entryText);

            currentWeather = (weather != null) ? weather : "Unknown";
            weatherLabel.setText("Weather: " + currentWeather);
            moodLabel.setText("Mood: " + (mood != null ? mood : "-"));
            statusLabel.setText("Loaded entry for " + date);
        } else {
            // New Entry (Clean Slate)
            journalTextArea.clear();
            moodLabel.setText("Mood: -");
            statusLabel.setText("New entry for " + date);

            // Fetch live weather only if it's Today
            if (date.equals(LocalDate.now())) {
                fetchWeatherForDate(date);
            } else {
                weatherLabel.setText("Weather: Unknown (Past)");
            }
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String entryText = journalTextArea.getText();

        // Prevent saving empty text which might look like a "delete"
        if (entryText == null || entryText.trim().isEmpty()) {
            statusLabel.setText("Cannot save empty journal entry!");
            return;
        }

        saveButton.setDisable(true);
        statusLabel.setText("Saving...");

        new Thread(() -> {
            try {
                // Analyze Mood
                String analyzedMood = "Unknown";
                try {
                    analyzedMood = journalApp.extractMoodCategory(MoodAnalyzer.analyzeMood(entryText));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                final String finalMood = analyzedMood;

                // FIX: Call journalApp.saveJournal directly
                journalApp.saveJournal(selectedDate, entryText, currentWeather, finalMood);

                // Simulation sleep (optional, makes UI feel responsive)
                Thread.sleep(300);

                Platform.runLater(() -> {
                    statusLabel.setText("Saved!");
                    moodLabel.setText("Mood: " + finalMood);
                    saveButton.setDisable(false);

                    // Update the sidebar immediately
                    updateTimelineList(selectedDate, entryText);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    saveButton.setDisable(false);
                    e.printStackTrace();
                });
            }
        }).start();
    }

    // --- HELPER METHODS ---
    @FXML
    private void handleNewEntry(ActionEvent event) {
        LocalDate today = LocalDate.now();

        // Check if today exists in timeline
        TimelineEntry todayEntry = null;
        for (TimelineEntry entry : timelineListView.getItems()) {
            if (entry.date.equals(today)) {
                todayEntry = entry;
                break;
            }
        }

        if (todayEntry != null) {
            timelineListView.getSelectionModel().select(todayEntry);
            datePicker.setValue(today);
        } else {
            timelineListView.getSelectionModel().clearSelection();
            datePicker.setValue(today);
            journalTextArea.clear();
            journalTextArea.setPromptText("Start writing your new entry for today...");
            moodLabel.setText("Mood: -");
            statusLabel.setText("New Draft");
            fetchWeatherForDate(today);
        }
    }

    private void fetchWeatherForDate(LocalDate date) {
        weatherLabel.setText("Weather: Loading...");
        new Thread(() -> {
            String weather = WeatherBackgroundManager.getWeatherForDate(date);
            currentWeather = weather;
            Platform.runLater(() -> weatherLabel.setText("Weather: " + weather));
        }).start();
    }

    // ... (Keep updateTimelineList, setupTimelineCellFactory, handleAnalyzeMood, handleClose as they were) ...
    private void updateTimelineList(LocalDate date, String text) {
        String preview = text.replace("\n", " ").trim();
        if (preview.length() > 30) {
            preview = preview.substring(0, 30) + "...";
        }

        TimelineEntry existingEntry = null;
        for (TimelineEntry item : timelineListView.getItems()) {
            if (item.date.equals(date)) {
                existingEntry = item;
                break;
            }
        }

        if (existingEntry != null) {
            existingEntry.preview = preview;
            timelineListView.refresh();
        } else {
            TimelineEntry newEntry = new TimelineEntry(date, preview);
            timelineListView.getItems().add(0, newEntry);
            timelineListView.getItems().sort((a, b) -> b.date.compareTo(a.date));
            timelineListView.getSelectionModel().select(newEntry);
        }
    }

    private void setupTimelineCellFactory() {
        timelineListView.setCellFactory(param -> new ListCell<TimelineEntry>() {
            @Override
            protected void updateItem(TimelineEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox container = new VBox(4);
                    container.setAlignment(Pos.CENTER_LEFT);

                    // Date Label
                    Label dateLbl = new Label(item.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                    dateLbl.getStyleClass().add("timeline-date-label"); // Must match CSS

                    // Preview Label
                    Label previewLbl = new Label(item.preview);
                    previewLbl.getStyleClass().add("timeline-preview-label"); // Must match CSS

                    container.getChildren().addAll(dateLbl, previewLbl);
                    setGraphic(container);
                }
            }
        });
    }

    private void refreshTimeline() {
        // 1. Clear current list
        timelineListView.getItems().clear();

        // 2. Fetch real data from MongoDB
        List<Document> allEntries = journalApp.getAllUserJournals();
        List<TimelineEntry> uiEntries = new ArrayList<>();

        // 3. Convert Documents to Timeline Entries
        for (Document doc : allEntries) {
            try {
                String dateStr = doc.getString("date");
                String fullText = doc.getString("entry");

                if (dateStr != null && fullText != null) {
                    LocalDate date = LocalDate.parse(dateStr); // Assumes yyyy-MM-dd format

                    // Create a short preview
                    String preview = fullText.replace("\n", " ").trim();
                    if (preview.length() > 30) {
                        preview = preview.substring(0, 30) + "...";
                    }

                    uiEntries.add(new TimelineEntry(date, preview));
                }
            } catch (Exception e) {
                System.err.println("Skipping invalid entry: " + e.getMessage());
            }
        }

        // 4. Update the UI
        timelineListView.getItems().setAll(uiEntries);
    }

    private void selectDateInTimeline(LocalDate date) {
        for (TimelineEntry entry : timelineListView.getItems()) {
            if (entry.date.equals(date)) {
                timelineListView.getSelectionModel().select(entry);
                return;
            }
        }
        timelineListView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAnalyzeMood(ActionEvent event) {
        String entry = journalTextArea.getText();
        if (entry == null || entry.trim().isEmpty()) {
            return;
        }
        new Thread(() -> {
            try {
                String moodResult = MoodAnalyzer.analyzeMood(entry);
                Platform.runLater(() -> moodLabel.setText("Mood: " + moodResult));
            } catch (Exception e) {
                Platform.runLater(() -> moodLabel.setText("Mood: Error"));
            }
        }).start();
    }

    @FXML
    private void handleClose(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LandingPage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            LandingPageController controller = loader.getController();
            if (UserSession.getInstance().getCurrentUser() != null) {
                controller.setUserName(UserSession.getInstance().getCurrentUser().getDisplayName());
            }
            stage.setScene(new Scene(root));
            stage.setFullScreenExitHint("");
            stage.setFullScreen(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
