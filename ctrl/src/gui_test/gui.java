import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class gui extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create a button
        Button btn = new Button("Say Hello!");

        // Set an action for the button
        btn.setOnAction(event -> System.out.println("Hello, JavaFX!"));

        // Create a layout pane (StackPane in this case)
        StackPane root = new StackPane();
        root.getChildren().add(btn); // Add the button to the layout

        // Create a scene with the layout pane as its root
        Scene scene = new Scene(root, 300, 200); // Width and height

        // Set the scene on the primary stage
        primaryStage.setTitle("Hello World JavaFX"); // Window title
        primaryStage.setScene(scene);

        // Show the stage
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // Launch the JavaFX application
    }
}