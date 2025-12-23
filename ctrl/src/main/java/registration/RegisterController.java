package registration;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class RegisterController {

    @FXML private TextField regEmailField;
    @FXML private TextField regNameField;
    @FXML private PasswordField regPasswordField;
    @FXML private Label errorLabel;

    private final UserManager userManager = new UserManager();

    @FXML
    private void handleRegister(ActionEvent event) {
        String email = regEmailField.getText().trim();
        String name = regNameField.getText().trim();
        String password = regPasswordField.getText();

        // 1. Validation Logic (Mirrors your Console App)
        if (email.isEmpty() || !email.contains(".com")) {
            showError("Invalid email. Must contain '.com'.");
            return;
        }
        if (name.isEmpty()) {
            showError("Display name cannot be empty.");
            return;
        }
        if (password.isEmpty()) {
            showError("Password cannot be empty.");
            return;
        }

        // 2. Check if email exists
        if (userManager.emailExists(email)) {
            showError("Email already registered.");
            return;
        }

        // 3. Register User
        boolean success = userManager.register(email, name, password);

        if (success) {
            System.out.println("Registration Successful for: " + name);
            // Go back to login so they can sign in with new credentials
            switchToLogin(event); 
        } else {
            showError("Registration failed. Please try again.");
        }
    }

    @FXML
    private void switchToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login/Login.fxml")); 
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}