import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import registration.UserManager;
import registration.UserSession;
import landingpage.LandingPageController; // Import this!

public class Ctrl extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // 1. REMOVE WINDOW BORDERS (Minimize/Close buttons)
        // This must be done before the stage is shown!
        stage.initStyle(StageStyle.UNDECORATED);
        
        // 1. Try to restore previous session
        UserManager userManager = new UserManager();
        boolean isLoggedIn = UserSession.getInstance().restoreSession(userManager);

        if (isLoggedIn) {
            // A. SESSION FOUND -> Go straight to Landing Page
            System.out.println("Auto-login successful!");
            openLandingPage(stage);
        } else {
            // B. NO SESSION -> Show Login Screen
            openLoginPage(stage);
        }
    }

    private void openLoginPage(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/registration/Login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        
        // 3. FORCE FULLSCREEN
        stage.setFullScreen(true);
        stage.setFullScreenExitHint(""); // Removes the "Press ESC to exit" text
        
        stage.show();
    }

    private void openLandingPage(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/LandingPage.fxml")); // Correct path?
        Parent root = loader.load();

        // Pass the restored username to the controller
        LandingPageController controller = loader.getController();
        String name = UserSession.getInstance().getCurrentUser().getDisplayName();
        controller.setUserName(name);

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
