package summary;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import registration.UserSession;
import summary.SmartJournal.SummaryData;

import java.util.Map;

public class SummaryController {

    @FXML private Label titleLabel;
    @FXML private VBox moodChartContainer;
    @FXML private VBox weatherChartContainer;
    @FXML private Label noDataLabel;

    @FXML
    public void initialize() {
        loadSummaryData();
    }

    private void loadSummaryData() {
        // Get current user email
        String userEmail = "guest@local"; // default
        if (UserSession.getInstance().getCurrentUser() != null) {
            userEmail = UserSession.getInstance().getCurrentUser().getEmail();
        }

        // Fetch summary data
        SummaryData summaryData = SmartJournal.getWeeklySummaryData(userEmail);

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

            // Populate mood chart
            populateChart(moodChartContainer, summaryData.getMoodCounts(), summaryData.getTotalEntries());

            // Populate weather chart
            populateChart(weatherChartContainer, summaryData.getWeatherCounts(), summaryData.getTotalEntries());
        }
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
