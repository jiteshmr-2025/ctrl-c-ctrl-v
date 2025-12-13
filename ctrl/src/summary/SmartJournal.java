package summary;



import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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

        // B. Define "Today" (In your real app, use LocalDate.now())
        // For this assignment mock data, we force "Today" to be 2025-10-11
        LocalDate today = LocalDate.parse("2025-10-11"); 

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

    // 2. Helper Method: Read File & Find Entry
    public static String[] getEntryForDate(String email, String date) {
        File file = new File("JournalData.txt");
        if (!file.exists()) return null;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split("\\|"); 
                // Check if User AND Date match
                if (parts.length >= 5 && parts[0].equals(date) && parts[1].equals(email)) {
                    return parts;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
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
        // Test with the user from our mock file
        displayWeeklySummary("s100201@student.fop");

        
    }
}