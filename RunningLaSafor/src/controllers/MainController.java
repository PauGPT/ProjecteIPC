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
import javafx.scene.control.Alert;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
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
    private MenuItem visualitzarPerfilButton;
    @FXML
    private MenuItem modificarPerfilButton;
    @FXML
    private MenuItem historialButton;
    @FXML
    private MenuItem logoutButton;
    @FXML
    private Button logoButton;
    @FXML
    private Button helpButton;
    @FXML
    private BorderPane contentPane;
    @FXML
    private HBox toolbar;

    private boolean isProgrammaticDeselection = false;

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

        if (menu != null) {
            menu.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
                if (newVal == null && !isProgrammaticDeselection) {
                    menu.selectToggle(oldVal);
                }
            });
        }

        // Registrar tecla d'ajuda F1 a nivell d'escena quan estiga disponible
        toolbar.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.F1) {
                        // Només obri l'ajuda si l'usuari està identificat i la barra està activa
                        if (SportActivityApp.getInstance().getCurrentUser() != null) {
                            showHelpDialog();
                            event.consume();
                        }
                    }
                });
            }
        });
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

    @FXML
    private void handleActivitats(ActionEvent event) {
        loadView("/views/Activitats.fxml");
    }

    @FXML
    private void handleMapes(ActionEvent event) {
        loadView("/views/Mapa.fxml");
    }

    @FXML
    private void handleVisualitzarPerfil(ActionEvent event) {
        deselectMenuButtons();
        loadView("/views/seeUserFXML.fxml");
    }

    @FXML
    private void handleModificarPerfil(ActionEvent event) {
        deselectMenuButtons();
        loadView("/views/updateUserFXML.fxml");
    }

    @FXML
    private void handleHistorial(ActionEvent event) {
        deselectMenuButtons();
        loadView("/views/historialDeSesiones.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        deselectMenuButtons();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Tancar sessió");
        alert.setHeaderText(null);
        alert.setContentText("Estàs segur que vols tancar la sessió?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                SportActivityApp.getInstance().logout();
                updateMenuAvatar();
                updateToolbarState();
                loadView("/views/DashboardUnlogged.fxml");
            }
        });
    }

    @FXML
    private void handleLogo(ActionEvent event) {
        deselectMenuButtons();
        if (SportActivityApp.getInstance().getCurrentUser() != null) {
            loadView("/views/DashboardLogged.fxml");
        } else {
            loadView("/views/DashboardUnlogged.fxml");
        }
    }

    @FXML
    private void handleHelp(ActionEvent event) {
        showHelpDialog();
    }

    private void showHelpDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ajuda");
        alert.setHeaderText("Vols pujar la teua primera activitat?");
        alert.setContentText("Comprova en la secció \"Mapes\" que hi ha un mapa per a la teua activitat o puja'n un.\nDesprés afegeix el fitxer .gpx a la secció \"Activitats\".\nRecorda que fent click al botó del logo pots veure l'acumulat del mes.");
        if (toolbar != null && toolbar.getScene() != null) {
            alert.initOwner(toolbar.getScene().getWindow());
        }
        alert.showAndWait();
    }

    public void deselectMenuButtons() {
        if (menu != null) {
            isProgrammaticDeselection = true;
            menu.selectToggle(null);
            isProgrammaticDeselection = false;
        }
    }

    public void selectActivitats() {
        if (activitatsButton != null) {
            activitatsButton.setSelected(true);
        }
    }
}
