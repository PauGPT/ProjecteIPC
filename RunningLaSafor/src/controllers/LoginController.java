/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

import application.App;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import upv.ipc.sportlib.SportActivityApp;

/**
 * FXML Controller class
 *
 * @author ultraMEga
 */
public class LoginController implements Initializable {

    private final Image openEye = new Image(getClass().getResourceAsStream("/resources/login/eye.png"));
    private final Image closeEye = new Image(getClass().getResourceAsStream("/resources/login/hidden.png"));
    private SportActivityApp app = SportActivityApp.getInstance();
    private boolean passVisible = false;

    @FXML
    private TextField passTextField;
    @FXML
    private PasswordField passField;
    @FXML
    private ImageView imgEye;
    @FXML
    private TextField userField;
    @FXML
    private Label errorLogin;
    @FXML
    private Button loginButton;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        passTextField.textProperty().bindBidirectional(passField.textProperty());
    }

    @FXML
    private void togglePasswordVisibility(ActionEvent event) {

        if (passVisible) {
            passTextField.setVisible(false);
            passTextField.setDisable(true);

            passField.setVisible(true);
            passField.setDisable(false);

            imgEye.setImage(openEye);

            passField.requestFocus();
            passField.selectEnd();
        } else {
            passField.setVisible(false);
            passField.setDisable(true);

            passTextField.setVisible(true);
            passTextField.setDisable(false);

            imgEye.setImage(closeEye);

            passTextField.requestFocus();
            passTextField.selectEnd();
        }

        passVisible = !passVisible;
    }

    @FXML
    private void login(ActionEvent event) {

        boolean islogged = app.login(userField.getText(), passField.getText());
        
        userField.setText("");
        passField.setText("");
        passTextField.setText("");

        if (islogged) {
            
            VBox.setMargin(loginButton, new Insets(30,0,0,0));

            errorLogin.setManaged(false);
            errorLogin.setVisible(false);

                Stage stage = (Stage) loginButton.getScene().getWindow();

                App.getMainController().updateMenuAvatar();
                App.getMainController().updateToolbarState();
                App.getMainController().deselectMenuButtons();
                App.getMainController().loadView(App.Vista.DASHBOARD_LOGGED);

                if (stage != null) {
                    stage.setMaximized(true);
                }
        } else {

            errorLogin.setManaged(true);
            errorLogin.setVisible(true);
            VBox.setMargin(loginButton, new Insets(0,0,0,0));

        }

    }

    @FXML
    private void goToRegister(ActionEvent event) {
        App.getMainController().loadView("/views/register.fxml");
    }
}
