package journalpage;

import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import mood.MoodAnalyzer;
import registration.UserSession;
import utils.MongoDBConnection;
import utils.WeatherBackgroundManager;

public class journalApp {

    public static final Scanner scanner = new Scanner(System.in);
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Gets the MongoDB collection for journals.
     */
    private static MongoCollection<Document> getJournalCollection() {
        return MongoDBConnection.getDatabase().getCollection("journals");
    }

    /**
     * Gets the current user's email from the session.
     * Returns a default email if no user is logged in.
     */
    private static String getCurrentUserEmail() {
        if (UserSession.getInstance().getCurrentUser() != null) {
            return UserSession.getInstance().getCurrentUser().getEmail();
        }
        // Fallback for standalone testing
        return "guest@local";
    }

    /**
     * Extracts the mood category from the full mood result.
     * Removes the percentage in parentheses (e.g., "Positive (100%)" -> "Positive").
     * 
     * @param fullMood the full mood result from the analyzer
     * @return the cleaned mood category
     */
    private static String extractMoodCategory(String fullMood) {
        if (fullMood == null) {
            return "Unknown";
        }
        if (fullMood.contains("(")) {
            return fullMood.substring(0, fullMood.indexOf("(")).trim();
        }
        return fullMood;
    }

    public static void main(String[] args) {
        runJournalApp();
    }

    public static boolean runJournalApp() {
        while (true) {
            System.out.println("\n=== Journal Dates ===");

            // Display last few days + today
            List<LocalDate> dates = getJournalDates();
            LocalDate today = LocalDate.now();
            int index = 1;

            for (LocalDate date : dates) {
                if (date.equals(today))
                    System.out.println(index + ". " + date + " (Today)");
                else
                    System.out.println(index + ". " + date);
                index++;
            }

            
            System.out.println(index + ". View/Create journal for a custom date"); // Custom date option
            System.out.println((index+1)+". Back to Dashboard");
            System.out.print("\nSelect a date to view/edit journal ("+(index+1)+ "to go back): ");
            int choice = getIntInput();

            if (choice < 1 || choice > index+1) {
                System.out.println("Invalid choice. Try again.");
                continue;
            }

            if (choice == (index+1)) {
                // return to caller (dashboard)
                return false;
            }

            LocalDate selectedDate;
            if (choice == index) {
                selectedDate = getCustomDate();
            } else {
                selectedDate = dates.get(choice - 1);
            }

            handleDateSelection(selectedDate);
        }
    }

    public static LocalDate getCustomDate() {
        while (true) {
            System.out.print("Enter date (yyyy-MM-dd): ");
            String input = scanner.nextLine();
            try {
                return LocalDate.parse(input, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please try again.");
            }
        }
    }

    public static void handleDateSelection(LocalDate date) {
        String entry = readJournal(date);

        if (entry == null) {
            System.out.println("No journal entry found for " + date);
            System.out.println("Would you like to create one? (y/n)");
            String choice = scanner.nextLine();
            if (choice.equalsIgnoreCase("y")) {
                createJournal(date);
            }
        } else {
            System.out.println("Journal found for " + date + ". What would you like to do?");
            System.out.println("1. View Journal");
            System.out.println("2. Edit Journal");
            System.out.println("3. Back to Dates");
            System.out.print("> ");
            int option = getIntInput();

            switch (option) {
                case 1 -> viewJournal(date);
                case 2 -> editJournal(date);
                default -> { return; }
            }
        }
    }

    public static void createJournal(LocalDate date) {
        System.out.println("Enter your journal entry for " + date + ":");
        System.out.print("> ");
        String entry = scanner.nextLine();

        // Fetch Weather
        System.out.print("Fetching weather info... ");
        String weather = WeatherBackgroundManager.getCurrentWeather();
        System.out.println("[" + weather + "]");

        // Analyze Mood
        System.out.print("Analyzing mood... ");
        String fullMood = "Unknown";
        String chartMood = "Unknown";
        
        try {
            fullMood = MoodAnalyzer.analyzeMood(entry);
            System.out.println("[" + fullMood + "]");
            chartMood = extractMoodCategory(fullMood);
        } catch (Exception e) {
            System.out.println("Mood analysis failed: " + e.getMessage());
        }

        // Save to MongoDB
        saveJournal(date, entry, weather, chartMood);
        System.out.println("Journal saved to Database!");

        System.out.println("Would you like to:");
        System.out.println("1. View Journal");
        System.out.println("2. Edit Journal");
        System.out.println("3. Back to Dates");
        System.out.print("> ");
        int choice = getIntInput();

        switch (choice) {
            case 1 -> viewJournal(date);
            case 2 -> editJournal(date);
            default -> { return; }
        }
    }

    public static void viewJournal(LocalDate date) {
        String entry = readJournal(date);
        if (entry == null) {
            System.out.println("No journal found for this date.");
            return;
        }

        System.out.println("\n=== Journal Entry for " + date + " ===");
        System.out.println(entry);
        
        // Analyze mood for the viewed journal
        try {
            String moodResult = MoodAnalyzer.analyzeMood(entry);
            System.out.println("\nDetected mood: " + moodResult);
        } catch (Exception e) {
            System.out.println("Mood analysis failed: " + e.getMessage());
        }
        System.out.println("\nPress Enter to go back.");
        scanner.nextLine();
    }

    public static void editJournal(LocalDate date) {
        System.out.println("Edit your journal entry for " + date + ":");
        System.out.print("> ");
        String newEntry = scanner.nextLine();

        // Re-analyze mood for new text
        String fullMood = "Unknown";
        String chartMood = "Unknown";
        try {
            fullMood = MoodAnalyzer.analyzeMood(newEntry);
            chartMood = extractMoodCategory(fullMood);
        } catch (Exception e) {
            System.out.println("Mood analysis failed: " + e.getMessage());
        }
        
        // Fetch current weather
        String weather = WeatherBackgroundManager.getCurrentWeather();

        saveJournal(date, newEntry, weather, chartMood);
        System.out.println("Journal updated! New Mood: " + fullMood);
    }

    public static List<LocalDate> getJournalDates() {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Display last 3 days + today
        for (int i = 3; i >= 0; i--) {
            dates.add(today.minusDays(i));
        }
        return dates;
    }

    /**
     * Saves a journal entry to MongoDB using upsert.
     * Creates a new entry or updates existing entry for the same user and date.
     */
    public static void saveJournal(LocalDate date, String entry, String weather, String mood) {
        String email = getCurrentUserEmail();
        String dateStr = date.toString();

        // Create filter for the query
        Document filter = new Document("email", email).append("date", dateStr);
        
        // Create document with all journal data
        Document doc = new Document("email", email)
                .append("date", dateStr)
                .append("entry", entry)
                .append("weather", weather)
                .append("mood", mood);

        // Use replaceOne with upsert option to insert or update in a single operation
        getJournalCollection().replaceOne(
            filter, 
            doc, 
            new ReplaceOptions().upsert(true)
        );
    }

    /**
     * Reads a journal entry from MongoDB for the current user and given date.
     * Returns null if no entry exists.
     */
    public static String readJournal(LocalDate date) {
        String email = getCurrentUserEmail();
        String dateStr = date.toString();
        Document query = new Document("email", email).append("date", dateStr);
        Document doc = getJournalCollection().find(query).first();
        if (doc != null) {
            return doc.getString("entry");
        }
        return null;
    }

    public static int getIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
