package registration;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;
import java.security.*;
import utils.MongoDBConnection;

public class UserManager {
    private final MongoCollection<Document> userCollection;

    public UserManager() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        this.userCollection = db.getCollection("users");
    }

    // --- Keep your existing helper methods ---
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String combined = password + salt;
            byte[] hashedBytes = md.digest(combined.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) { sb.append(String.format("%02x", b)); }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password: " + e.getMessage());
        }
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[8];
        random.nextBytes(saltBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : saltBytes) { sb.append(String.format("%02x", b)); }
        return sb.toString();
    }
    // ------------------------------------------

    public boolean emailExists(String email) {
        return userCollection.find(eq("email", email)).first() != null;
    }

    public boolean register(String email, String displayName, String password) {
        if (emailExists(email)) return false;

        String salt = generateSalt();
        String hashed = hashPassword(password, salt);

        Document newUser = new Document("email", email)
                .append("displayName", displayName)
                .append("password", hashed)
                .append("salt", salt);

        try {
            userCollection.insertOne(newUser);
            return true;
        } catch (Exception e) {
            System.out.println("Registration Error: " + e.getMessage());
            return false;
        }
    }

    public User login(String email, String password) {
        Document doc = userCollection.find(eq("email", email)).first();
        
        if (doc != null) {
            String storedSalt = doc.getString("salt");
            String storedHash = doc.getString("password");
            
            if (hashPassword(password, storedSalt).equals(storedHash)) {
                return new User(
                    doc.getString("email"),
                    doc.getString("displayName"),
                    storedHash,
                    storedSalt
                );
            }
        }
        return null;
    }

    public User getUserByEmail(String email) {
        Document doc = userCollection.find(eq("email", email)).first();
        if (doc != null) {
             return new User(doc.getString("email"), doc.getString("displayName"), doc.getString("password"), doc.getString("salt"));
        }
        return null;
    }
    // Inside UserManager class...

    public boolean editUser(String email, String newDisplayName, String newPassword) {
        // Find the user first
        Document user = userCollection.find(eq("email", email)).first();
        if (user == null) return false;

        // Prepare updates
        Document updates = new Document();
        if (newDisplayName != null && !newDisplayName.isEmpty()) {
            updates.append("displayName", newDisplayName);
        }
        if (newPassword != null && !newPassword.isEmpty()) {
            String newSalt = generateSalt(); // You need to regenerate salt if password changes
            String newHash = hashPassword(newPassword, newSalt);
            updates.append("password", newHash).append("salt", newSalt);
        }

        // Apply updates if there are any
        if (!updates.isEmpty()) {
            userCollection.updateOne(eq("email", email), new Document("$set", updates));
            return true;
        }
        return false;
    }

    public boolean deleteUser(String email) {
        // Delete the user document
        return userCollection.deleteOne(eq("email", email)).getDeletedCount() > 0;
    }

    // ... existing code ...

    // 1. Save a "Remember Me" token for a user
    public void setRememberToken(String email, String token) {
        // Update the user's document with a new "rememberToken" field
        userCollection.updateOne(eq("email", email), new Document("$set", new Document("rememberToken", token)));
    }

    // 2. Find a user by their "Remember Me" token (for Auto-Login)
    public User loginByToken(String token) {
        Document doc = userCollection.find(eq("rememberToken", token)).first();
        if (doc != null) {
            // Token matches! Return the user.
            return new User(
                doc.getString("email"), 
                doc.getString("displayName"), 
                doc.getString("password"), 
                doc.getString("salt")
            );
        }
        return null; // Invalid token
    }

    // 3. Remove the token (for Logout)
    public void removeRememberToken(String email) {
        userCollection.updateOne(eq("email", email), new Document("$unset", new Document("rememberToken", "")));
    }
}