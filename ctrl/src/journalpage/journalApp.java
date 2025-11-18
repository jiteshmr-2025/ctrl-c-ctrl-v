package journalpage;

import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.*;
import mood.MoodAnalyzer;

public class journalApp {

    // Save journals in a `journals` folder inside the `ctrl/src/journalpage` package
    public static final String JOURNAL_FOLDER = System.getProperty("user.dir") + File.separator + "ctrl" + File.separator + "src" + File.separator + "journalpage" + File.separator + "journals";
    public static final Scanner scanner = new Scanner(System.in);
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        new File(JOURNAL_FOLDER).mkdirs(); // Ensure Desktop/journal folder exists
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
            System.out.print("\nSelect a date to view/edit journal (0 to go back): ");
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
        String filePath = JOURNAL_FOLDER + "/" + date + ".txt";
        File journalFile = new File(filePath);

        if (!journalFile.exists()) {
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

        saveJournal(date, entry);
        System.out.println("Journal saved successfully!");

        // Analyze mood for the newly created journal
        try {
            String moodResult = MoodAnalyzer.analyzeMood(entry);
            System.out.println("Detected mood: " + moodResult);
        } catch (Exception e) {
            System.out.println("Mood analysis failed: " + e.getMessage());
        }

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

        saveJournal(date, newEntry);
        System.out.println("Journal updated successfully!");
        // Analyze mood for the updated journal
        try {
            String moodResult = MoodAnalyzer.analyzeMood(newEntry);
            System.out.println("Detected mood: " + moodResult);
        } catch (Exception e) {
            System.out.println("Mood analysis failed: " + e.getMessage());
        }
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

    public static void saveJournal(LocalDate date, String entry) {
        try (FileWriter writer = new FileWriter(JOURNAL_FOLDER + "/" + date + ".txt")) {
            writer.write(entry);
        } catch (IOException e) {
            System.out.println("Error saving journal: " + e.getMessage());
        }
    }

    public static String readJournal(LocalDate date) {
        StringBuilder sb = new StringBuilder();
        File file = new File(JOURNAL_FOLDER + "/" + date + ".txt");

        if (!file.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line).append("\n");
        } catch (IOException e) {
            System.out.println("Error reading journal: " + e.getMessage());
        }
        return sb.toString().trim();
    }

    public static int getIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
