package summary;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import utils.MongoDBConnection;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class SmartJournal {

    // 1. The Main Method to Show the Summary
    public static void displayWeeklySummary(String currentUserEmail) {
        System.out.println("\n==========================================");
        System.out.println("      WEEKLY SUMMARY (Past 7 Days)      ");
        System.out.println("==========================================");

        // Get summary data
        SummaryData summaryData = getWeeklySummaryData(currentUserEmail);

        // Display the Charts
        if (summaryData.getTotalEntries() == 0) {
            System.out.println("No journal entries found for this week.");
        } else {
            System.out.println("\n[ MOOD CHART ]");
            printTextChart(summaryData.getMoodCounts(), summaryData.getTotalEntries());

            System.out.println("\n[ WEATHER CHART ]");
            printTextChart(summaryData.getWeatherCounts(), summaryData.getTotalEntries());
        }
        System.out.println("==========================================\n");
    }

    // 2. Helper Method: Fetch a journal entry for a date from MongoDB
    public static String[] getEntryForDate(String email, String date) {
        MongoCollection<Document> coll = MongoDBConnection.getDatabase().getCollection("journals");
        Document doc = coll.find(Filters.and(
                Filters.eq("email", email),
                Filters.eq("date", date)
        )).first();

        if (doc == null) return null;

        String foundDate = doc.getString("date");
        String foundEmail = doc.getString("email");
        String title = doc.getString("entry");
        String weather = doc.getString("weather");
        String mood = doc.getString("mood");

        // Provide robust defaults if fields are missing
        if (title == null) title = "";
        if (weather == null || weather.isBlank()) weather = "Unknown";
        if (mood == null || mood.isBlank()) mood = "Unknown";

        // Keep the same positions used by existing code: [0]=date, [1]=email, [2]=title, [3]=weather, [4]=mood
        return new String[] { foundDate, foundEmail, title, weather, mood };
    }

    // 3. Helper Method: Draw the "Text Pie Chart"
    public static void printTextChart(Map<String, Integer> data, int total) {
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            String label = entry.getKey();
            int count = entry.getValue();
            double percentage = (count * 100.0) / total;

            // Draw Bar: 1 block '█' = 5%
            int barLength = (int) (percentage / 5);
            StringBuilder bar = new StringBuilder();
            for (int k = 0; k < barLength; k++) bar.append("█");

            // Print: Label | Bar | Percentage
            System.out.printf("%-15s | %-15s | %.1f%%%n", label, bar.toString(), percentage);
        }
    }
    
    // Helper method to get summary data for GUI
    public static SummaryData getWeeklySummaryData(String currentUserEmail) {
        Map<String, Integer> moodCounts = new HashMap<>();
        Map<String, Integer> weatherCounts = new HashMap<>();
        int totalEntries = 0;

        LocalDate today = LocalDate.now();

        // Loop through the past 7 days (6 days ago -> Today)
        for (int i = 6; i >= 0; i--) {
            LocalDate targetDate = today.minusDays(i);
            String dateString = targetDate.toString();

            String[] entry = getEntryForDate(currentUserEmail, dateString);

            if (entry != null) {
                totalEntries++;
                String weather = entry[3];
                String mood = entry[4];

                moodCounts.put(mood, moodCounts.getOrDefault(mood, 0) + 1);
                weatherCounts.put(weather, weatherCounts.getOrDefault(weather, 0) + 1);
            }
        }

        return new SummaryData(moodCounts, weatherCounts, totalEntries);
    }
    
    // Data class to hold summary information
    public static class SummaryData {
        private final Map<String, Integer> moodCounts;
        private final Map<String, Integer> weatherCounts;
        private final int totalEntries;
        
        public SummaryData(Map<String, Integer> moodCounts, Map<String, Integer> weatherCounts, int totalEntries) {
            this.moodCounts = moodCounts;
            this.weatherCounts = weatherCounts;
            this.totalEntries = totalEntries;
        }
        
        public Map<String, Integer> getMoodCounts() {
            return moodCounts;
        }
        
        public Map<String, Integer> getWeatherCounts() {
            return weatherCounts;
        }
        
        public int getTotalEntries() {
            return totalEntries;
        }
    }
}
