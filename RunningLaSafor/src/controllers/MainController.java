package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

/**
 * FXML Controller class
 *
 * @author Pau
 */
public class MainController implements Initializable {

    @FXML
    private ToggleButton activitatsButton;
    @FXML
    private Label mousePosition;
    @FXML
    private SplitPane splitPane;
    @FXML
    private ListView<?> map_listview;
    @FXML
    private ScrollPane map_scrollpane;
    @FXML
    private ToggleGroup menu;
    @FXML
    private Circle avatarCircleMenu;
    @FXML
    private ToggleButton mapesButton;
    @FXML
    private MenuItem modificarPerfilButton;
    @FXML
    private MenuItem historialButton;
    @FXML
    private MenuItem logoutButton;
    @FXML
    private Button logoButton;
    @FXML
    private BorderPane contentPane;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setMenuAvatar("../resources/main/default_avatar.png");
        Node unlogged_dashboard = null;
        try {
            unlogged_dashboard = FXMLLoader.load(
                getClass().getResource("/views/DashboardUnlogged.fxml")
            );
        } catch (IOException ex) {
            System.getLogger(MainController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        
        contentPane.setCenter(unlogged_dashboard);
        
    }    


    @FXML
    private void listClicked(MouseEvent event) {
    }

    @FXML
    private void showPosition(MouseEvent event) {
    }
    
    private void setMenuAvatar(String image_path) {
        Image avatarImage = new Image(getClass().getResourceAsStream(image_path));
        avatarCircleMenu.setFill(new ImagePattern(avatarImage));
    }

    
}
