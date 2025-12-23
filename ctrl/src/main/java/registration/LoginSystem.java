package registration;

import java.util.Scanner;

public class LoginSystem {
    static UserManager userManager = new UserManager();
    static Scanner sc = new Scanner(System.in);
    public static User currentUser = null;

    //Login Page
    public static User start() {
        while (true) {
            System.out.println("=== Smart Journaling Login System ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Select option: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    return login();
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

    //allow other feature to use current user
    public static User getCurrentUser() {
        return currentUser;
    }
    
    //user login
    public static User login() {
        System.out.print("Enter Email: ");
        String email = sc.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = sc.nextLine().trim();

        User user = userManager.login(email, password);
        if (user != null) {
            currentUser = user; // ✅ store logged-in user
            System.out.println("Login successful! Welcome, " + user.getDisplayName());
            welcome_user();
            userDashboard();
            return user;
        } else {
            System.out.println("Invalid credentials. Please try again.");
            return null;
        }
    }

    public static void welcome_user() {
        // Call the welcome module and pass the logged-in user's display name.
        // Only call when a user is logged in; don't use a default name.
        if (currentUser != null) {
            welcome.welcome.welcome_user(currentUser.getDisplayName());
        }
    }

    //registration
    public static void register() {
        // Get all user information first
        System.out.print("Enter Email: ");
        String email = sc.nextLine().trim();

        // ✅ Email cannot be empty and must contain ".com"
        if (email.isEmpty() || !email.contains(".com")) {
            System.out.println("Invalid email! Email must contain '.com' and cannot be empty.");
            return;
        }

        System.out.print("Enter Display Name: ");
        String name = sc.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Display name cannot be empty!");
            return;
        }

        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        if (password.isEmpty()) {
            System.out.println("Password cannot be empty!");
            return;
        }

        // Now check if email exists and try to register
        if (userManager.emailExists(email)) {
            System.out.println("Email already registered! Please try a different email.");
            return;
        }

        boolean success = userManager.register(email, name, password);
        if (success) {
            System.out.println("Registration successful!");
        } else {
            System.out.println("Registration failed!");
        }
    }

    public static void userDashboard() {
        while (true) {
            System.out.println("\n=== User Dashboard ===");
            System.out.println("1. Modify Account");
            System.out.println("2. Open Journal");
            System.out.println("3. Logout");
            System.out.print("Choose option: ");
            int opt = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (opt) {
                case 1 -> userSettings();
                case 2 -> {
                    // Launch the journal application (keeps user logged in)
                    boolean didLogout = journalpage.journalApp.runJournalApp();
                    if (didLogout) {
                        System.out.println("Logged out successfully!\n");
                        currentUser = null; // clear session
                        return; // exit back to main login menu
                    }
                }
                case 3 -> {
                    System.out.println("Logged out successfully!\n");
                    currentUser = null; // clear session
                    return; // exit the loop, go back to main menu
                }
                default -> System.out.println("Invalid option!");
            }
        }
    }
    //modify account
    public static void userSettings() {
        if (currentUser == null) {
        System.out.println("Error: No user logged in.");
        return;
        }

        String email = currentUser.getEmail();

        System.out.println("1. Edit Display Name");
        System.out.println("2. Edit Password");
        System.out.println("3. Delete Account");
        System.out.print("Select option: ");
        int opt = sc.nextInt();
        sc.nextLine(); // consume newline

        switch (opt) {
            case 1 -> {
                System.out.print("Enter new display name: ");
                String newName = sc.nextLine();

                if (newName.isEmpty()) {
                    System.out.println("Display name cannot be empty!");
                    return;
                }

                if (userManager.editUser(email, newName, null)){
                    // Don't call login() with the stored hashed password — login expects the plaintext password.
                    // Instead, fetch the updated User object directly from the manager.
                    currentUser = userManager.getUserByEmail(email);
                    if (currentUser != null) {
                        System.out.println("Display name updated!");
                        System.out.println("Your new display name is now: " + currentUser.getDisplayName());
                    } else {
                        System.out.println("Display name updated, but failed to refresh user data.");
                    }
                } // refresh user data
                else{
                    System.out.println("User not found.");
                }
            }
            case 2 -> {
                System.out.print("Enter new password: ");
                String newPass = sc.nextLine().trim();
                if (newPass.isEmpty()) {
                    System.out.println("Password cannot be empty!");
                    return;
                }
                if (userManager.editUser(email, null, newPass)) {
                    // Refresh the currentUser reference so session state matches stored data
                    currentUser = userManager.getUserByEmail(email);
                    System.out.println("Password updated!");
                } else {
                    System.out.println("User not found.");
                }
            }
            case 3 -> {
                System.out.print("Are you sure you want to delete your account? (y/n): ");
                String confirm = sc.nextLine();
                if (confirm.equalsIgnoreCase("y")) {
                    if (userManager.deleteUser(email)) {
                        System.out.println("Account deleted successfully!");
                        System.out.println("Returning to main menu...");
                        start(); // go back to login/register
                        currentUser = null;
                    } else {
                        System.out.println("User not found.");
                    }
                } else {
                    System.out.println("Cancelled account deletion.");
                }
            }
                    default -> System.out.println("Invalid choice.");
        }
    }
    public static void main(String[] args) {
            start(); // this launches the main menu
        }
}
