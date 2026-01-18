package registration;
/**
 *
 * @author chee
 */
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UserSession {
    private static UserSession instance;
    private User currentUser;
    
    // We will save a tiny text file called "session.token"
    // Using a hidden folder or user home directory is best practice, 
    // but for now, we'll keep it simple in the project root.
    private static final String SESSION_FILE = "session.token";

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) instance = new UserSession();
        return instance;
    }

    public User getCurrentUser() { return currentUser; }

    // --- NEW: Save the User's Email to a file ---
    public void saveSession(User user) {
        this.currentUser = user;
        try (PrintWriter out = new PrintWriter(new FileWriter(SESSION_FILE))) {
            // We only save the email. Never save plain-text passwords!
            out.println(user.getEmail());
            System.out.println("Session saved for: " + user.getEmail());
        } catch (IOException e) {
            System.out.println("Failed to save session: " + e.getMessage());
        }
    }

    // --- NEW: Try to load the user from the file ---
    public boolean restoreSession(UserManager userManager) {
        File file = new File(SESSION_FILE);
        if (!file.exists()) return false;

        try {
            String email = new String(Files.readAllBytes(Paths.get(SESSION_FILE))).trim();
            
            // We need to look up the full User object using the email
            User user = userManager.getUserByEmail(email);
            
            if (user != null) {
                this.currentUser = user;
                System.out.println("Session restored for: " + user.getDisplayName());
                return true;
            }
        } catch (IOException e) {
            System.out.println("Failed to restore session.");
        }
        return false;
    }

    // --- NEW: Delete file on Logout ---
    public void logout() {
        this.currentUser = null;
        File file = new File(SESSION_FILE);
        if (file.exists()) {
            file.delete(); // Delete the token so next launch requires login
        }
    }
}