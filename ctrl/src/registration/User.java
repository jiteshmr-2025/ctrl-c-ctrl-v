package registration;

public class User {
    
    private final String email;
    private final String displayName;
    private final String password;
    private String salt;


    public User(String email, String displayName, String password, String salt) {
        this.email = email;
        this.displayName = displayName;
        this.password = password;
        this.salt = salt;
    }

    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getPassword() { return password; }
    public String getSalt() { return salt; }

    public void setSalt(String salt) { this.salt = salt; }
    public String toFileString() {
        return email + "\n" + displayName + "\n" + password + "\n";
    }
}
