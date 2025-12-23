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
import landingpage.LandingPageController; // Make sure this import matches your package
import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private final UserManager userManager = new UserManager();

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both email and password.");
            errorLabel.setVisible(true);
            return;
        }

        // 1. Check credentials
        User user = userManager.login(email, password);

        if (user != null) {
            // 2. SAVE TO SESSION (This replaces LoginSystem.currentUser = user)
            UserSession.getInstance().saveSession(user);

            System.out.println("Login Successful: " + user.getDisplayName());
            goToLandingPage(event, user.getDisplayName());

            // 3. Go to Main App
            goToLandingPage(event, user.getDisplayName());
        } else {
            errorLabel.setText("Invalid email or password.");
            errorLabel.setVisible(true);
        }
    }

    private void goToLandingPage(ActionEvent event, String username) {
        try {
            // Point to your LandingPage FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LandingPage.fxml"));
            Parent root = loader.load();

            // Pass data to Landing Page
            LandingPageController controller = loader.getController();
            controller.setUserName(username);

            // Switch Scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Error loading application.");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void switchToRegister(ActionEvent event) {
        try {
            // Point to your Register FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registration/Register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
