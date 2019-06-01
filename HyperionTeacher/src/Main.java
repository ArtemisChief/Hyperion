import LocalServer.Component.LocalServer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("Teacher/TeacherView.fxml"));
        primaryStage.setTitle("Hyperion - Teacher");
        primaryStage.setScene(new Scene(root, 590, 390));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> LocalServer.Close());
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
