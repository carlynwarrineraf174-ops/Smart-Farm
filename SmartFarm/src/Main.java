import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.FarmView;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        FarmView view = new FarmView(primaryStage);
        Scene scene = new Scene(view.getRoot(), 1300, 750);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setTitle("🌾 智慧农场");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        Platform.runLater(view::showStartDialog);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
