package summary;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import registration.UserSession;
import summary.SummaryPage.SummaryData;

import java.util.Map;

public class SummaryController {

    @FXML private Label titleLabel;
    @FXML private VBox moodChartContainer;
    @FXML private VBox weatherChartContainer;
    @FXML private Label noDataLabel;

    @FXML
    public void initialize() {
        try {
            loadSummaryData();
        } catch (Exception e) {
            System.err.println("Error loading summary data: " + e.getMessage());
            e.printStackTrace();
            // Show error message to user
            noDataLabel.setText("Error loading summary: " + e.getMessage());
            noDataLabel.setVisible(true);
            moodChartContainer.setVisible(false);
            weatherChartContainer.setVisible(false);
        }
    }

    private void loadSummaryData() {
        try {
            // Get current user email
            String userEmail = "guest@local"; // default
            if (UserSession.getInstance().getCurrentUser() != null) {
                userEmail = UserSession.getInstance().getCurrentUser().getEmail();
            }

            System.out.println("Loading summary for user: " + userEmail);

            // Fetch summary data
            SummaryData summaryData = SummaryPage.getWeeklySummaryData(userEmail);

            System.out.println("Summary data loaded. Total entries: " + summaryData.getTotalEntries());

            if (summaryData.getTotalEntries() == 0) {
                // Show "no data" message
                noDataLabel.setVisible(true);
                moodChartContainer.setVisible(false);
                weatherChartContainer.setVisible(false);
            } else {
                // Hide "no data" message and show charts
                noDataLabel.setVisible(false);
                moodChartContainer.setVisible(true);
                weatherChartContainer.setVisible(true);

                // Populate mood chart (only Positive and Negative)
                populateMoodChart(moodChartContainer, summaryData.getMoodCounts(), summaryData.getTotalEntries());

                // Populate weather chart
                populateChart(weatherChartContainer, summaryData.getWeatherCounts(), summaryData.getTotalEntries());
            }
        } catch (Exception e) {
            System.err.println("Error in loadSummaryData: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void populateMoodChart(VBox container, Map<String, Integer> data, int total) {
        // Clear existing content (except the first child which is the title)
        if (container.getChildren().size() > 1) {
            container.getChildren().remove(1, container.getChildren().size());
        }

        // Count only Negative and Positive (anything not Negative is Positive)
        int negativeCount = 0;
        int positiveCount = 0;
        
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            String label = entry.getKey();
            int count = entry.getValue();
            
            if ("Negative".equalsIgnoreCase(label)) {
                negativeCount += count;
            } else {
                // Everything else (Positive, Unknown, errors, etc.) counts as Positive
                positiveCount += count;
            }
        }

        // Display Negative
        double negPercentage = (negativeCount * 100.0) / total;
        int negBarLength = (int) (negPercentage / 5);
        StringBuilder negBar = new StringBuilder();
        for (int k = 0; k < negBarLength; k++) negBar.append("█");
        Label negLabel = new Label(String.format("%-15s | %-15s | %.1f%%", "Negative", negBar.toString(), negPercentage));
        negLabel.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 14px; -fx-text-fill: white;");
        container.getChildren().add(negLabel);

        // Display Positive
        double posPercentage = (positiveCount * 100.0) / total;
        int posBarLength = (int) (posPercentage / 5);
        StringBuilder posBar = new StringBuilder();
        for (int k = 0; k < posBarLength; k++) posBar.append("█");
        Label posLabel = new Label(String.format("%-15s | %-15s | %.1f%%", "Positive", posBar.toString(), posPercentage));
        posLabel.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 14px; -fx-text-fill: white;");
        container.getChildren().add(posLabel);
    }

    private void populateChart(VBox container, Map<String, Integer> data, int total) {
        // Clear existing content (except the first child which is the title)
        if (container.getChildren().size() > 1) {
            container.getChildren().remove(1, container.getChildren().size());
        }

        // Add chart bars
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            String label = entry.getKey();
            int count = entry.getValue();
            double percentage = (count * 100.0) / total;

            // Create bar representation
            int barLength = (int) (percentage / 5);
            StringBuilder bar = new StringBuilder();
            for (int k = 0; k < barLength; k++) {
                bar.append("█");
            }

            // Create label for this entry
            Label chartLabel = new Label(String.format("%-15s | %-15s | %.1f%%", label, bar.toString(), percentage));
            chartLabel.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 14px; -fx-text-fill: white;");
            
            container.getChildren().add(chartLabel);
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        // Close the summary window
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}