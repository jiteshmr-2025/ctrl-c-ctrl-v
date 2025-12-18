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

        // A. Setup Counters
        Map<String, Integer> moodCounts = new HashMap<>();
        Map<String, Integer> weatherCounts = new HashMap<>();
        int totalEntries = 0;

        // B. Define "Today"
        LocalDate today = LocalDate.now();

        // C. Loop through the past 7 days (6 days ago -> Today)
        for (int i = 6; i >= 0; i--) {
            LocalDate targetDate = today.minusDays(i);
            String dateString = targetDate.toString();

            // D. Extract data for this specific date
            String[] entry = getEntryForDate(currentUserEmail, dateString);

            if (entry != null) {
                totalEntries++;
                String weather = entry[3]; // Weather is at index 3
                String mood = entry[4];    // Mood is at index 4

                // Add to counters
                moodCounts.put(mood, moodCounts.getOrDefault(mood, 0) + 1);
                weatherCounts.put(weather, weatherCounts.getOrDefault(weather, 0) + 1);
            }
        }

        // E. Display the Charts
        if (totalEntries == 0) {
            System.out.println("No journal entries found for this week.");
        } else {
            System.out.println("\n[ MOOD CHART ]");
            printTextChart(moodCounts, totalEntries);

            System.out.println("\n[ WEATHER CHART ]");
            printTextChart(weatherCounts, totalEntries);
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
        String title = doc.getString("title");
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
    
    // Main for testing
    public static void main(String[] args) {
        // Example: run summary for current user email (replace with an actual email in your DB)
        displayWeeklySummary("s100201@student.fop");
    }
}