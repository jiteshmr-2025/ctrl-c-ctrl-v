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

    private static MongoCollection<Document> getJournalCollection() {
        return MongoDBConnection.getDatabase().getCollection("journals");
    }

    private static String getCurrentUserEmail() {
        if (UserSession.getInstance().getCurrentUser() != null) {
            return UserSession.getInstance().getCurrentUser().getEmail();
        }
        return "guest@local";
    }

    // --- GUI SUPPORT METHODS (NEW) ---
    /**
     * Gets the FULL document (Entry, Weather, Mood) for the GUI. Returns null
     * if no entry exists.
     * @param date
     * @return Journal Collection of the first query
     */
    public static Document getJournalDocument(LocalDate date) {
        String email = getCurrentUserEmail();
        String dateStr = date.toString();
        Document query = new Document("email", email).append("date", dateStr);
        return getJournalCollection().find(query).first();
    }

    // --------------------------------
    // --- EXISTING CLI METHODS ------
    public static void saveJournal(LocalDate date, String entry, String weather, String mood) {
        String email = getCurrentUserEmail();
        String dateStr = date.toString();

        Document filter = new Document("email", email).append("date", dateStr);

        Document doc = new Document("email", email)
                .append("date", dateStr)
                .append("entry", entry)
                .append("weather", weather)
                .append("mood", mood);

        getJournalCollection().replaceOne(
                filter,
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    public static String readJournal(LocalDate date) {
        Document doc = getJournalDocument(date); // Reuse the new method
        if (doc != null) {
            return doc.getString("entry");
        }
        return null;
    }
    
    public static String extractMoodCategory(String fullMood) {
        if (fullMood == null) {
            return "Unknown";
        }
        if (fullMood.contains("(")) {
            return fullMood.substring(0, fullMood.indexOf("(")).trim();
        }
        return fullMood;
    }

    // Fetch all journal entries for the timeline
    public static List<Document> getAllUserJournals() {
        String email = getCurrentUserEmail();
        // Find all documents for this user, Sorted by Date (Descending / Newest First)
        return getJournalCollection()
                .find(new Document("email", email))
                .sort(new Document("date", -1))
                .into(new ArrayList<>());
    }
}
