package registration;

import java.io.*;
import java.util.Scanner;

public class LoginSystem {
    static UserManager userManager = new UserManager();
    static Scanner sc = new Scanner(System.in);
    public static User currentUser = null;
    
    // File where we store the "Remember Me" session token locally
    private static final String SESSION_FILE = "session.token";

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
    
    // --- Auto-Login Feature using file-based session ---
    public static void tryAutoLogin() {
        File file = new File(SESSION_FILE);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String email = br.readLine();
                if (email != null && !email.isEmpty()) {
                    System.out.println("Checking for saved login...");
                    User user = userManager.getUserByEmail(email.trim());
                    
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
            
            // --- Ask to Remember User ---
            System.out.print("Remember me on this device? (y/n): ");
            String ans = sc.nextLine();
            if (ans.equalsIgnoreCase("y")) {
                saveSession(user.getEmail());
            }
            
            welcome_user();
            userDashboard();
            return user;
        } else {
            System.out.println("Invalid credentials. Please try again.");
            return null;
        }
    }
    
    // --- Save Session Helper (file-based) ---
    public static void saveSession(String email) {
        try (FileWriter fw = new FileWriter(SESSION_FILE)) {
            fw.write(email);
            System.out.println(">> Device will remember you next time!");
        } catch (IOException e) {
            System.out.println("Failed to save session.");
        }
    }

    public static void userDashboard() {
        while (true) {
            if (currentUser == null) return;

            System.out.println("\n=== User Dashboard (" + currentUser.getDisplayName() + ") ===");
            System.out.println("1. Modify Account");
            System.out.println("2. Open Journal");
            System.out.println("3. Logout (Forget Me)");
            System.out.println("4. Exit Application (Remember Me)");
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
                    // This DELETES the session (Standard security behavior)
                    performLogout();
                    return;
                case 4:
                    // This KEEPS the session and closes the app
                    System.out.println("Exiting... See you soon!");
                    System.exit(0); 
                    break;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }
    
    // --- Logout Helper (file-based) ---
    public static void performLogout() {
        System.out.println("Logged out successfully!");
        
        // Delete Local Session File
        File file = new File(SESSION_FILE);
        if (file.exists()) {
            file.delete();
        }
        
        currentUser = null;
    }

    public static void register() {
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
                        performLogout();
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