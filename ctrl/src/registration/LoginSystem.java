package registration;

import java.io.*;
import java.util.Scanner;
import java.util.UUID; // Used to generate unique tokens

public class LoginSystem {
    static UserManager userManager = new UserManager();
    static Scanner sc = new Scanner(System.in);
    public static User currentUser = null;
    
    // File where we store the "Remember Me" token locally
    private static final String REMEMBER_FILE = "remember.token";

    public static void start() {
        // --- AUTO-LOGIN CHECK ---
        // Before showing the menu, check if we remember the user
        tryAutoLogin(); 
        
        while (true) {
            System.out.println("\n=== Smart Journaling Login System ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Select option: ");
            
            // Handle non-integer input gracefully
            int choice = -1;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1:
                    login(); // Modified to handle "Remember Me"
                    break;
                case 2:
                    register();
                    break;
                case 3:
                    System.out.println("Goodbye!");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
    
    // --- NEW: Auto-Login Feature ---
    public static void tryAutoLogin() {
        File file = new File(REMEMBER_FILE);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String token = br.readLine();
                if (token != null && !token.isEmpty()) {
                    System.out.println("Checking for saved login...");
                    User user = userManager.loginByToken(token);
                    
                    if (user != null) {
                        currentUser = user;
                        System.out.println(">> Auto-login successful! Welcome back, " + user.getDisplayName());
                        welcome_user();
                        userDashboard(); // Go straight to dashboard
                    }
                }
            } catch (IOException e) {
                System.out.println("Could not read saved login.");
            }
        }
    }

    public static User login() {
        System.out.print("Enter Email: ");
        String email = sc.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = sc.nextLine().trim();

        User user = userManager.login(email, password);
        if (user != null) {
            currentUser = user;
            System.out.println("Login successful! Welcome, " + user.getDisplayName());
            
            // --- NEW: Ask to Remember User ---
            System.out.print("Remember me on this device? (y/n): ");
            String ans = sc.nextLine();
            if (ans.equalsIgnoreCase("y")) {
                saveRememberToken(user.getEmail());
            }
            
            welcome_user();
            userDashboard();
            return user;
        } else {
            System.out.println("Invalid credentials. Please try again.");
            return null;
        }
    }
    
    // --- NEW: Save Token Helper ---
    public static void saveRememberToken(String email) {
        String token = UUID.randomUUID().toString(); // Generate a random unique ID
        
        // 1. Save to Database
        userManager.setRememberToken(email, token);
        
        // 2. Save to Local File
        try (FileWriter fw = new FileWriter(REMEMBER_FILE)) {
            fw.write(token);
            System.out.println(">> Device will remember you next time!");
        } catch (IOException e) {
            System.out.println("Failed to save remember token.");
        }
    }

    public static void userDashboard() {
        while (true) {
            if (currentUser == null) return;

            System.out.println("\n=== User Dashboard (" + currentUser.getDisplayName() + ") ===");
            System.out.println("1. Modify Account");
            System.out.println("2. Open Journal");
            System.out.println("3. Logout (Forget Me)");
            System.out.println("4. Exit Application (Remember Me)"); // <--- NEW OPTION
            System.out.print("Choose option: ");
            
            int opt = -1;
            try {
                opt = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) { continue; }

            switch (opt) {
                case 1:
                    userSettings();
                    break;
                case 2:
                    boolean didLogout = journalpage.journalApp.runJournalApp();
                    if (didLogout) {
                        performLogout();
                        return;
                    }
                    break;
                case 3:
                    // This DELETES the token (Standard security behavior)
                    performLogout();
                    return;
                case 4:
                    // This KEEPS the token and closes the app
                    System.out.println("Exiting... See you soon!");
                    System.exit(0); 
                    break;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }
    
    // --- NEW: Logout Helper ---
    public static void performLogout() {
        System.out.println("Logged out successfully!");
        
        // 1. Remove from Database
        if (currentUser != null) {
            userManager.removeRememberToken(currentUser.getEmail());
        }
        
        // 2. Delete Local File
        File file = new File(REMEMBER_FILE);
        if (file.exists()) {
            file.delete();
        }
        
        currentUser = null;
    }

    // ... (Keep register, userSettings, welcome_user, etc. exactly as they were) ...
    
    public static void register() {
        // (Copy your existing register code here)
        // ...
        // To save space in this answer, I am omitting the register/settings code 
        // because they don't change. Just make sure you keep them!
        
        // If you need me to paste the full register code again, let me know!
        
        // --- PASTE YOUR EXISTING REGISTER METHOD HERE ---
        System.out.print("Enter Email: ");
        String email = sc.nextLine().trim();
        if (email.isEmpty() || !email.contains(".com")) {
            System.out.println("Invalid email!"); return;
        }
        System.out.print("Enter Display Name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) return;
        System.out.print("Enter Password: ");
        String password = sc.nextLine();
        if (userManager.emailExists(email)) {
            System.out.println("Email already registered!"); return;
        }
        if (userManager.register(email, name, password)) {
            System.out.println("Registration successful!");
        } else {
            System.out.println("Registration failed!");
        }
    }
    
    public static void userSettings() {
       // (Copy your existing userSettings code here)
       // ...
       // --- PASTE YOUR EXISTING USERSETTINGS METHOD HERE ---
        if (currentUser == null) return;
        String email = currentUser.getEmail();
        System.out.println("1. Edit Display Name");
        System.out.println("2. Edit Password");
        System.out.println("3. Delete Account");
        System.out.println("4. Back to dashboard");
        System.out.print("Select option: ");
        int opt = sc.nextInt(); sc.nextLine();
        switch (opt) {
            case 1 -> {
                System.out.print("Enter new display name: ");
                String newName = sc.nextLine();
                if (userManager.editUser(email, newName, null)){
                    currentUser = userManager.getUserByEmail(email);
                    System.out.println("Display name updated!");
                }
            }
            case 2 -> {
                System.out.print("Enter new password: ");
                String newPass = sc.nextLine().trim();
                if (userManager.editUser(email, null, newPass)) {
                    currentUser = userManager.getUserByEmail(email);
                    System.out.println("Password updated!");
                }
            }
            case 3 -> {
                System.out.print("Delete account? (y/n): ");
                if (sc.nextLine().equalsIgnoreCase("y")) {
                    if (userManager.deleteUser(email)) {
                        System.out.println("Account deleted!");
                        performLogout(); // Use new logout method
                    }
                }
            }
            case 4 -> {
                return;
            }

            default -> {System.out.println("Invalid choice.");}
        }
    }

    public static void welcome_user() {
        if (currentUser != null) {
            try {
                welcome.welcome.welcome_user(currentUser.getDisplayName());
            } catch (Exception e) {
                // Ignore if welcome package is missing
            }
        }
    }
    
    public static User getCurrentUser() {
        return currentUser;
    }

    public static void main(String[] args) {
        start();
    }
}