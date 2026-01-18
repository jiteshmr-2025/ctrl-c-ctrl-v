package registration;
/**
 *
 * @author chee
 */
import java.io.*;
import java.security.*;
import java.util.*;

public class UserManager {
    private final String filePath = "UserData.txt";
    private final ArrayList<User> users = new ArrayList<>();

    public UserManager() {
        loadUsers();
    }

    private String hashPassword(String password, String salt) {
    try {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String combined = password + salt; // combine password + salt
        byte[] hashedBytes = md.digest(combined.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));//covert to hex
        }
        return sb.toString();
    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("Error hashing password: " + e.getMessage());
    }
}


    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[8]; // 8 bytes = 64 bits of salt
        random.nextBytes(saltBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : saltBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


    private void loadUsers() {
        File file = new File(filePath);
        try {
            // Create the file if it doesn't exist
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                file.createNewFile();
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String email, displayName, password;
                while ((email = br.readLine()) != null) {
                    displayName = br.readLine();
                    password = br.readLine();
                    String salt = br.readLine();
                    users.add(new User(email, displayName, password, salt));
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

   private void saveAllUsers() {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            bw.write(u.getEmail());
            bw.newLine();
            bw.write(u.getDisplayName());
            bw.newLine();
            bw.write(u.getPassword());
            bw.newLine();
            bw.write(u.getSalt());

            // Prevent adding an extra blank line after the last user
            if (i < users.size() - 1) {
                bw.newLine();
            }
        }
    } catch (IOException e) {
        System.out.println("Error saving users: " + e.getMessage());
    }
}


    public boolean emailExists(String email) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    public boolean register(String email, String displayName, String password) {
        if (emailExists(email)) return false;
        String salt = generateSalt();
        String hashed = hashPassword(password, salt);
        users.add(new User(email, displayName, hashed, salt));
        saveAllUsers();
        return true;
    }

    public User login(String email, String password) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                String hashedInput = hashPassword(password, u.getSalt());
                if (u.getPassword().equals(hashedInput)) {
                    return u;
                }
            }
        }
        return null; // login failed
    }

    public boolean editUser(String email, String newDisplayName, String newPassword) {
    for (int i = 0; i < users.size(); i++) {
        User u = users.get(i);
        if (u.getEmail().equalsIgnoreCase(email)) {

            // Use existing values unless user updates them
            String updatedName = (newDisplayName != null && !newDisplayName.isEmpty())
                    ? newDisplayName
                    : u.getDisplayName();

            String updatedPass = u.getPassword();
            String updatedSalt = u.getSalt();

            // if new password is provided, hash and replace
            if (newPassword != null && !newPassword.isEmpty()) {
                updatedSalt = generateSalt();
                updatedPass = hashPassword(newPassword, updatedSalt); // ✅ hash the new password before saving
            } else {
                updatedPass = u.getPassword(); // keep old hashed password
            }

            // Update the user object
            users.set(i, new User(email, updatedName, updatedPass, updatedSalt));
            saveAllUsers(); // ✅ rewrite file with updated info
            return true;
        }
    }
    return false; // user not found
}


    public boolean deleteUser(String email) {
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User u = iterator.next();
            if (u.getEmail().equalsIgnoreCase(email)) {
                iterator.remove();
                saveAllUsers(); // rewrite after deletion
                return true;
            }
        }
        return false;
    }

    // Return a user by email (or null if not found)
    public User getUserByEmail(String email) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }

}
