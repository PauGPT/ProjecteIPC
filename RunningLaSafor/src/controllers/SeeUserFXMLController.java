/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

/**
 * FXML Controller class
 *
 * @author ultraMEga
 */
public class SeeUserFXMLController implements Initializable {

    private File selectedAvatarFile = null;
    private SportActivityApp app = SportActivityApp.getInstance();
    User user;

    private boolean passVisible = false;
    private final Image openEye = new Image(getClass().getResourceAsStream("/resources/login/eye.png"));
    private final Image closeEye = new Image(getClass().getResourceAsStream("/resources/login/hidden.png"));
    @FXML
    private Circle avatarCircle;
    @FXML
    private StackPane userContainer;
    @FXML
    private TextField userField;
    @FXML
    private StackPane emailContainer;
    @FXML
    private TextField emailField;
    @FXML
    private StackPane passwordContainer;
    @FXML
    private TextField passTextField;
    @FXML
    private PasswordField passField;
    @FXML
    private ImageView imgEye;
    @FXML
    private StackPane birthContainer;
    @FXML
    private TextField birthField;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        app.login("juan_23", "juan_23!");
        user = app.getCurrentUser();

        if (user.getAvatarPath() != null) {
            selectedAvatarFile = new File(user.getAvatarPath());
            Image image = new Image(selectedAvatarFile.toURI().toString());
            avatarCircle.setFill(new ImagePattern(image));
        } else {
            Image image = new Image(getClass().getResourceAsStream("/resources/main/default_avatar.png"));
            avatarCircle.setFill(new ImagePattern(image));
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaFormateada = user.getBirthDate().format(formatter);
        birthField.setText(fechaFormateada);

        birthField.setText(fechaFormateada);
        emailField.setText(user.getEmail());
        userField.setText(user.getNickName());

        passField.setText(user.getPassword());
        passTextField.setText(user.getPassword());

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
}
