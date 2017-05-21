package wickedmonkstudio.exchangerates;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("view/MainLayout.fxml"));
        primaryStage.setTitle("Exchange rates");
        primaryStage.setScene(new Scene(root, 480, 720));
        primaryStage.show();

        primaryStage.setOnCloseRequest(e->{
            Platform.exit();
            System.exit(1);
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
