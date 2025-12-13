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

public class journalApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Connect to the "journals" collection in MongoDB
    private static MongoCollection<Document> getJournalCollection() {
        return MongoDBConnection.getDatabase().getCollection("journals");
    }

    public static boolean runJournalApp() {
        // Safety Check: Ensure a user is logged in before accessing journals
        if (LoginSystem.getCurrentUser() == null) {
            System.out.println("Error: No user logged in.");
            return false;
        }

        while (true) {
            System.out.println("\n=== Journal Dates ===");

            List<LocalDate> dates = getJournalDates();
            int index = 1;

            for (LocalDate date : dates) {
                if (date.equals(LocalDate.now()))
                    System.out.println(index + ". " + date + " (Today)");
                else
                    System.out.println(index + ". " + date);
                index++;
            }

            System.out.println(index + ". View/Create journal for a custom date");
            System.out.println((index + 1) + ". Back to Dashboard");
            System.out.print("\nSelect an option: ");
            
            int choice = getIntInput();

            if (choice == index + 1) {
                return false; // Return to Dashboard
            }

            LocalDate selectedDate;
            if (choice == index) {
                selectedDate = getCustomDate();
            } else if (choice > 0 && choice < index) {
                selectedDate = dates.get(choice - 1);
            } else {
                System.out.println("Invalid choice.");
                continue;
            }

            handleDateSelection(selectedDate);
        }
    }

    public static void handleDateSelection(LocalDate date) {
        // Step 1: Check Database for an entry
        String entry = readJournal(date);

        if (entry == null) {
            System.out.println("No journal entry found for " + date);
            System.out.println("Would you like to create one? (y/n)");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                createJournal(date);
            }
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

    public static void createJournal(LocalDate date) {
        System.out.println("Enter your journal entry for " + date + ":");
        String entry = scanner.nextLine();

        saveJournal(date, entry);
        System.out.println("Journal saved to Database!");
        analyzeMood(entry);
    }

    public static void editJournal(LocalDate date) {
        System.out.println("Enter new text for " + date + ":");
        String newEntry = scanner.nextLine();

        saveJournal(date, newEntry);
        System.out.println("Journal updated in Database!");
        analyzeMood(newEntry);
    }

    public static void viewJournal(LocalDate date) {
        String entry = readJournal(date);
        if (entry != null) {
            System.out.println("\n=== Entry for " + date + " ===");
            System.out.println(entry);
            analyzeMood(entry);
        } else {
            System.out.println("No entry found.");
        }
        System.out.println("\n(Press Enter to go back)");
        scanner.nextLine();
    }

    // --- MongoDB Operations ---

    public static void saveJournal(LocalDate date, String entry) {
        String email = LoginSystem.getCurrentUser().getEmail();
        String dateStr = date.toString();

        // Query to find if this user already has a journal for this date
        Document query = new Document("email", email).append("date", dateStr);
        
        // The data we want to save
        Document doc = new Document("email", email)
                .append("date", dateStr)
                .append("entry", entry);

        // Check if it exists
        if (getJournalCollection().find(query).first() != null) {
            // Update existing
            getJournalCollection().updateOne(query, Updates.set("entry", entry));
        } else {
            // Insert new
            getJournalCollection().insertOne(doc);
        }
    }

    public static String readJournal(LocalDate date) {
        String email = LoginSystem.getCurrentUser().getEmail();
        String dateStr = date.toString();

        // Search for: Email + Date
        Document query = new Document("email", email).append("date", dateStr);
        Document doc = getJournalCollection().find(query).first();

        if (doc != null) {
            return doc.getString("entry");
        }
        return null; // Not found
    }

    // --- Helper Methods ---

    public static void analyzeMood(String text) {
        try {
            // Assumes MoodAnalyzer.analyzeMood returns a String
            System.out.println("Detected mood: " + MoodAnalyzer.analyzeMood(text));
        } catch (Exception e) {
            System.out.println("Mood analysis skipped.");
        }
    }

    public static List<LocalDate> getJournalDates() {
        List<LocalDate> dates = new ArrayList<>();
        // Show today + past 3 days
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