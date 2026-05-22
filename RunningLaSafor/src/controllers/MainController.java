package controllers;

import application.App;
import java.io.File;
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
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

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
    @FXML
    private HBox toolbar;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Posar este controlador com a MainController
        App.setMainController(this);
        //Posar el unlogged dashboard com a center
        loadView("/views/DashboardUnlogged.fxml");
        //Cavniar el avatar per el default o el de l'usuari actual
        updateMenuAvatar();
        //Actualitzar l'estat de la barra d'eines (toolbar)
        updateToolbarState();
    }    


    @FXML
    private void listClicked(MouseEvent event) {
    }

    @FXML
    private void showPosition(MouseEvent event) {
    }
    
    public void updateMenuAvatar() {
        User user = SportActivityApp.getInstance().getCurrentUser();
        if (user != null && user.getAvatarPath() != null && !user.getAvatarPath().trim().isEmpty()) {
            try {
                File file = new File(user.getAvatarPath());
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    avatarCircleMenu.setFill(new ImagePattern(image));
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            Image avatarImage = new Image(getClass().getResourceAsStream("../resources/main/default_avatar.png"));
            avatarCircleMenu.setFill(new ImagePattern(avatarImage));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateToolbarState() {
        boolean loggedIn = SportActivityApp.getInstance().getCurrentUser() != null;
        if (toolbar != null) {
            toolbar.setDisable(!loggedIn);
        }
    }

    public void loadView(String fxml) {
        
        try {
            Node view = FXMLLoader.load(
                getClass().getResource(fxml)
            );
            contentPane.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public void loadView(App.Vista vista) {
        
        try {
            Node view = FXMLLoader.load(
                getClass().getResource(vista.getRuta())
            );
            contentPane.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
}
