package registration;

import java.util.Scanner;

public class LoginSystem {
    static UserManager userManager = new UserManager();
    static Scanner sc = new Scanner(System.in);
    private static User currentUser = null;

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
    private static User login() {
        System.out.print("Enter Email: ");
        String email = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        User user = userManager.login(email, password);
        if (user != null) {
            currentUser = user; // ✅ store logged-in user
            System.out.println("Login successful! Welcome, " + user.getDisplayName());
            userDashboard();
            return user;
        } else {
            System.out.println("Invalid credentials. Please try again.");
            return null;
        }
    }

    //registration
    private static void register() {
        System.out.print("Enter Email: ");
        String email = sc.nextLine();

        // ✅ Email cannot be empty and must contain ".com"
        if (email.isEmpty() || !email.contains(".com")) {
            System.out.println("Invalid email! Email must contain '.com' and cannot be empty.");
            return;
        }

        if (userManager.emailExists(email)) {
            System.out.println("Email already registered!");
            return;
        }

        System.out.print("Enter Display Name: ");
        String name = sc.nextLine();

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

        boolean success = userManager.register(email, name, password);
        if (success) System.out.println("Registration successful!");
        else System.out.println("Registration failed!");
    }

    private static void userDashboard() {
    while (true) {
        System.out.println("\n=== User Dashboard ===");
        System.out.println("1. Modify Account");
        System.out.println("2. Logout");
        System.out.print("Choose option: ");
        int opt = sc.nextInt();
        sc.nextLine(); // consume newline

        switch (opt) {
            case 1 -> userSettings(); // pass the logged-in user
            case 2 -> {
                System.out.println("Logged out successfully!\n");
                currentUser = null; // ✅ clear session
                return; // exit the loop, go back to main menu
            }
            default -> System.out.println("Invalid option!");
        }
    }
}
    //modify account
    private static void userSettings() {
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
                    currentUser = userManager.login(email, currentUser.getPassword()); // refresh first
                    System.out.println("Display name updated!");
                    System.out.println("Your new display name is now: " + currentUser.getDisplayName());
                    currentUser = userManager.login(email, currentUser.getPassword());
                } // refresh user data
                else{
                    System.out.println("User not found.");
                }
            }
            case 2 -> {
                System.out.print("Enter new password: ");
                String newPass = sc.nextLine();
                if (newPass.isEmpty()) {
                    System.out.println("Password cannot be empty!");
                    return;
                }
                if (userManager.editUser(email, null, newPass))
                    System.out.println("Password updated!");
                else
                    System.out.println("User not found.");
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

}
