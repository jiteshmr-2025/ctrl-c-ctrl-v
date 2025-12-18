package journalpage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import registration.LoginSystem;
import utils.MongoDBConnection;
import mood.MoodAnalyzer;
import summary.SmartJournal;
import weather.API_Get; // IMPORT THIS

public class journalApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static MongoCollection<Document> getJournalCollection() {
        return MongoDBConnection.getDatabase().getCollection("journals");
    }

    public static boolean runJournalApp() {
        // (Keep this method exactly as it is)
        if (LoginSystem.getCurrentUser() == null) {
            System.out.println("Error: No user logged in.");
            return false;
        }
        while (true) {
            System.out.println("\n=== Journal Dates ===");
            List<LocalDate> dates = getJournalDates();
            int index = 1;
            for (LocalDate date : dates) {
                if (date.equals(LocalDate.now())) System.out.println(index + ". " + date + " (Today)");
                else System.out.println(index + ". " + date);
                index++;
            }
            System.out.println(index + ". View/Create journal for a custom date");
            System.out.println((index + 1) + ". Weekly Summary");
            System.out.println((index + 2) + ". Back to Dashboard");
            System.out.print("\nSelect an option: ");
            int choice = getIntInput();

            if (choice == index + 2) return false;
            LocalDate selectedDate;
            if (choice == index) selectedDate = getCustomDate();
            else if (choice == index + 1) {
                SmartJournal.displayWeeklySummary(LoginSystem.getCurrentUser().getEmail());
                continue;
            } else if (choice > 0 && choice < index) selectedDate = dates.get(choice - 1);
            else { System.out.println("Invalid choice."); continue; }
            handleDateSelection(selectedDate);
        }
    }

    public static void handleDateSelection(LocalDate date) {
        String entry = readJournal(date);
        if (entry == null) {
            System.out.println("No journal entry found for " + date);
            System.out.println("Would you like to create one? (y/n)");
            if (scanner.nextLine().equalsIgnoreCase("y")) createJournal(date);
        } else {
            System.out.println("Journal found for " + date + ".");
            System.out.println("1. View Journal");
            System.out.println("2. Edit Journal");
            System.out.println("3. Back");
            System.out.print("> ");
            int option = getIntInput();
            if (option == 1) viewJournal(date);
            else if (option == 2) editJournal(date);
        }
    }

    // --- UPDATED CREATE METHOD ---
    public static void createJournal(LocalDate date) {
        System.out.println("Enter your journal entry for " + date + ":");
        String entry = scanner.nextLine();

        // 1. Fetch Weather
        System.out.print("Fetching weather info... ");
        String weather = API_Get.getCurrentWeather();
        System.out.println("[" + weather + "]");

        // 2. Analyze Mood
        System.out.print("Analyzing mood... ");
        String fullMood = "Unknown";
        String chartMood = "Unknown";
        
        try {
            fullMood = MoodAnalyzer.analyzeMood(entry); // Returns "Positive (100%)"
            System.out.println("[" + fullMood + "]");   // Display to user immediately
            
            // Clean mood for the database chart (remove the percentage)
            if (fullMood.contains("(")) {
                chartMood = fullMood.substring(0, fullMood.indexOf("(")).trim();
            } else {
                chartMood = fullMood;
            }
        } catch (Exception e) {
            System.out.println("Mood analysis failed.");
        }

        // 3. Save ALL data (Entry + Weather + Mood)
        saveJournal(date, entry, weather, chartMood);
        System.out.println("Journal saved to Database!");
    }

    // --- UPDATED EDIT METHOD ---
    public static void editJournal(LocalDate date) {
        System.out.println("Enter new text for " + date + ":");
        String newEntry = scanner.nextLine();

        // Re-analyze mood for new text
        String fullMood = MoodAnalyzer.analyzeMood(newEntry);
        String chartMood = fullMood;
        if (fullMood.contains("(")) {
            chartMood = fullMood.substring(0, fullMood.indexOf("(")).trim();
        }
        
        // Preserve existing weather if possible, or fetch new
        // For simplicity here, we will just fetch current weather or default
        String weather = API_Get.getCurrentWeather();

        saveJournal(date, newEntry, weather, chartMood);
        System.out.println("Journal updated! New Mood: " + fullMood);
    }

    // --- UPDATED SAVE METHOD ---
    public static void saveJournal(LocalDate date, String entry, String weather, String mood) {
        String email = LoginSystem.getCurrentUser().getEmail();
        String dateStr = date.toString();

        Document query = new Document("email", email).append("date", dateStr);
        
        // Include weather and mood in the document
        Document doc = new Document("email", email)
                .append("date", dateStr)
                .append("entry", entry)
                .append("weather", weather)
                .append("mood", mood);

        if (getJournalCollection().find(query).first() != null) {
            // Update existing
            getJournalCollection().updateOne(query, Updates.combine(
                Updates.set("entry", entry),
                Updates.set("weather", weather),
                Updates.set("mood", mood)
            ));
        } else {
            // Insert new
            getJournalCollection().insertOne(doc);
        }
    }

    public static void viewJournal(LocalDate date) {
        // We can just read the text, or read the full document if we want to show saved weather
        String entry = readJournal(date);
        if (entry != null) {
            System.out.println("\n=== Entry for " + date + " ===");
            System.out.println(entry);
            
            // Optional: Re-analyze or fetch from DB. 
            // Since we just want to view, re-analyzing is fine for display
            analyzeMood(entry); 
        } else {
            System.out.println("No entry found.");
        }
        System.out.println("\n(Press Enter to go back)");
        scanner.nextLine();
    }

    public static String readJournal(LocalDate date) {
        String email = LoginSystem.getCurrentUser().getEmail();
        String dateStr = date.toString();
        Document query = new Document("email", email).append("date", dateStr);
        Document doc = getJournalCollection().find(query).first();
        if (doc != null) {
            return doc.getString("entry");
        }
        return null;
    }

    public static void analyzeMood(String text) {
        try {
            System.out.println("Detected mood: " + MoodAnalyzer.analyzeMood(text));
        } catch (Exception e) {
            System.out.println("Mood analysis skipped.");
        }
    }

    public static List<LocalDate> getJournalDates() {
        List<LocalDate> dates = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            dates.add(LocalDate.now().minusDays(i));
        }
        return dates;
    }

    public static LocalDate getCustomDate() {
        while (true) {
            System.out.print("Enter date (yyyy-MM-dd): ");
            try {
                return LocalDate.parse(scanner.nextLine(), DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid format. Use yyyy-MM-dd");
            }
        }
    }

    public static int getIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}