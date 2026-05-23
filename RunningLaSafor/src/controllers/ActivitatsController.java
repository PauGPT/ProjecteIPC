package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

/**
 * FXML Controller class for Activitats
 */
public class ActivitatsController implements Initializable {

    @FXML
    private SplitPane splitPane;
    @FXML
    private ListView<Activity> activitiesListView;
    @FXML
    private AnchorPane detailPane;

    private SportActivityApp app;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        app = SportActivityApp.getInstance();
        
        // Populate the ListView with the current user's activities if they are logged in
        if (app.getCurrentUser() != null) {
            activitiesListView.getItems().setAll(app.getActivitiesByUser(app.getCurrentUser()));
        }
    }    
}
