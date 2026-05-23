package application;

import controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import upv.ipc.sportlib.SportActivityApp;

public class App extends Application {

    private static MainController mainController;

    public static MainController getMainController() {
        return mainController;
    }

    public static void setMainController(MainController controller) {
        mainController = controller;
    }
    
    public enum Vista {
        LOGIN("/views/Login.fxml"),
        DASHBOARD_UNLOGGED("/views/DashboardUnlogged.fxml"),
        REGISTER("/views/register.fxml"),
        DASHBOARD_LOGGED("/views/DashboardLogged.fxml");

        private final String ruta;
        Vista(String ruta) { this.ruta = ruta; }
        public String getRuta() { return this.ruta; }
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Main.fxml"));
        Parent root = loader.load();
        
        mainController = loader.getController();
        
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/logo_circular.png")));
        Scene scene = new Scene(root);
        stage.setTitle("Running La Safor");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
