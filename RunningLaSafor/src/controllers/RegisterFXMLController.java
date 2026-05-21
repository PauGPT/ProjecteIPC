/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

import application.App;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

/**
 * FXML Controller class
 *
 * @author ultraMEga
 */
public class RegisterFXMLController implements Initializable {

    private File selectedAvatarFile = null;

    private SportActivityApp app = SportActivityApp.getInstance();

    private boolean errorUser = true;
    private boolean errorEmail = true;
    private boolean errorPassword = true;
    private boolean errorBirthDate = true;

    private boolean passVisible = false;
    private final Image openEye = new Image(getClass().getResourceAsStream("/resources/login/eye.png"));
    private final Image closeEye = new Image(getClass().getResourceAsStream("/resources/login/hidden.png"));

    @FXML
    private Circle avatarCircle;
    @FXML
    private Label avatarPlaceholder;
    @FXML
    private Button btnRemoveAvatar;
    @FXML
    private TextField userField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField passTextField;
    @FXML
    private PasswordField passField;
    @FXML
    private ImageView imgEye;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private Label errorRegister;
    @FXML
    private Button registerButton;
    @FXML
    private StackPane userContainer;
    @FXML
    private StackPane emailContainer;
    @FXML
    private StackPane passwordContainer;
    @FXML
    private StackPane birthContainer;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        passTextField.textProperty().bindBidirectional(passField.textProperty());

        userField.focusedProperty().addListener((value, oldValue, newValue) -> {
            if (!newValue) {

                if (userField.getText().isEmpty()) {
                    errorUser = true;
                    mostrarErrorEnCampo(userContainer, "El nom d'usuari no pot estar buit.");
                } else if (!User.checkNickName(userField.getText())) {
                    errorUser = true;
                    mostrarErrorEnCampo(userContainer, "Entre 6 i 15 caràcters, només lletres, dígits, guió o subguion.");

                } else if (app.nickNameExists(userField.getText())) {
                    errorUser = true;
                    mostrarErrorEnCampo(userContainer, "L'usuari ja existeix");

                } else {
                    errorUser = false;
                    ocultarErrorEnCampo(userContainer);
                }
            }
        });

        emailField.focusedProperty().addListener((value, oldValue, newValue) -> {
            if (!newValue) {

                if (emailField.getText().isEmpty()) {
                    errorEmail = true;
                    mostrarErrorEnCampo(emailContainer, "El mail no pot estar buit.");
                } else if (!User.checkEmail(emailField.getText())) {
                    errorEmail = true;
                    mostrarErrorEnCampo(emailContainer, "El format ha de ser usuari@domini");

                } else if (false) {
                    errorEmail = true;
                    mostrarErrorEnCampo(emailContainer, "El mail ja existeix");

                } else {
                    errorEmail = false;
                    ocultarErrorEnCampo(emailContainer);
                }
            }
        });

        passField.focusedProperty().addListener((value, oldValue, newValue) -> {
            if (!newValue) {
                if (passField.getText().isEmpty()) {
                    errorPassword = true;
                    mostrarErrorEnCampo(passwordContainer, "La contrasenya no pot estar buida");
                } else if (!User.checkPassword(passField.getText())) {
                    errorPassword = true;
                    mostrarErrorEnCampo(passwordContainer, "La contrasenya ha de tenir entre 8 i 20 caràcters, amb almenys una majúscula, una minúscula, un dígit i un símbol");
                } else {
                    errorPassword = false;
                    ocultarErrorEnCampo(passwordContainer);
                }
            }
        });

        passTextField.focusedProperty().addListener((value, oldValue, newValue) -> {
            if (!newValue) {
                String password = !passTextField.getText().isEmpty() ? passTextField.getText() : passField.getText();
                password = password.trim();

                if (password.isEmpty()) {
                    errorPassword = true;
                    mostrarErrorEnCampo(passwordContainer, "La contrasenya no pot estar buida");
                } else if (!User.checkPassword(password)) {
                    errorPassword = true;
                    mostrarErrorEnCampo(passwordContainer, "La contrasenya ha de tenir entre 8 i 20 caràcters, amb almenys una majúscula, una minúscula, un dígit i un símbol");
                } else {
                    errorPassword = false;
                    ocultarErrorEnCampo(passwordContainer);
                }
            }
        });

        birthDatePicker.focusedProperty().addListener((value, oldValue, newValue) -> {
            if (!newValue) {
                if (birthDatePicker.getValue() == null) {
                    errorBirthDate = true;
                    mostrarErrorEnCampo(birthContainer, "La data de naixement no pot estar buida");
                } else if (!User.isOlderThan(birthDatePicker.getValue(), 18)) {
                    errorBirthDate = true;
                    mostrarErrorEnCampo(birthContainer, "Has de tindre més de 18 anys per a registrar-te");
                } else {
                    errorBirthDate = false;
                    ocultarErrorEnCampo(birthContainer);
                }
            }
        });

        //Quitar el foco automático inicial de los campos de texto
        Platform.runLater(() -> {
            if (userField.getScene() != null) {
                userField.getScene().getRoot().requestFocus();
            }
        });

    }

    @FXML
    private void goToLogin(ActionEvent event) {

        App.getMainController().loadView("/views/Login.fxml");
    }

    @FXML
    private void changeAvatar(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar foto de perfil");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(avatarCircle.getScene().getWindow());

        if (file != null) {
            selectedAvatarFile = file;

            Image img = new Image(file.toURI().toString());
            avatarCircle.setFill(new ImagePattern(img));

            avatarPlaceholder.setVisible(false);
            btnRemoveAvatar.setVisible(true);
            btnRemoveAvatar.setManaged(true);

        }
    }

    @FXML
    private void resetAvatar(ActionEvent event) {

        selectedAvatarFile = null;
        avatarCircle.setFill(Color.web("#fff4f4"));

        avatarPlaceholder.setVisible(true);
        btnRemoveAvatar.setVisible(false);
        btnRemoveAvatar.setManaged(false);
    }

    private void mostrarErrorEnCampo(StackPane contenedorCampo, String mensaje) {
        VBox contenedorPadre = (VBox) contenedorCampo.getParent();
        int indiceCampo = contenedorPadre.getChildren().indexOf(contenedorCampo);

        String idError = contenedorCampo.getId() + "_error";
        Label errorLabel = (Label) contenedorPadre.lookup("#" + idError);

        if (errorLabel == null) {
            errorLabel = new Label(mensaje);
            errorLabel.setId(idError);
            errorLabel.setPrefWidth(300.0);
            errorLabel.setMinHeight(40.0);
            errorLabel.setPrefHeight(Region.USE_COMPUTED_SIZE);
            errorLabel.setMaxHeight(Region.USE_COMPUTED_SIZE);
            errorLabel.setWrapText(true);
            errorLabel.getStyleClass().add("errorLabel");
            

            VBox.setMargin(errorLabel, new javafx.geometry.Insets(5, 0, 5, 0));
            errorLabel.setPadding(new javafx.geometry.Insets(0, 0, 0, 5));

            try {
                ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/resources/login/exclamation.png")));
                icon.setFitWidth(20.0);
                icon.setFitHeight(20.0);
                icon.setPreserveRatio(true);
                errorLabel.setGraphic(icon);
            } catch (Exception e) {
                System.out.println("No se pudo cargar el icono de error: " + e.getMessage());
            }

            contenedorPadre.getChildren().add(indiceCampo + 1, errorLabel);
        } else {
            errorLabel.setText(mensaje);
        }
    }

    private void ocultarErrorEnCampo(StackPane contenedorCampo) {
        VBox contenedorPadre = (VBox) contenedorCampo.getParent();
        String idError = contenedorCampo.getId() + "_error";

        Label errorLabel = (Label) contenedorPadre.lookup("#" + idError);
        if (errorLabel != null) {
            contenedorPadre.getChildren().remove(errorLabel);
        }
    }

    @FXML
    private void registrarUsuario(ActionEvent event) {

        if (!errorUser && !errorEmail && !errorEmail && !errorBirthDate) {

            errorRegister.setVisible(false);
            errorRegister.setManaged(false);

            String nick = userField.getText();
            String email = emailField.getText();
            String pass = passField.getText();
            LocalDate birthDate = birthDatePicker.getValue();

            if (selectedAvatarFile != null) {
                String avatarPath = selectedAvatarFile.getAbsolutePath();
                app.registerUser(nick, email, pass, birthDate, avatarPath);

            } else {
                app.registerUser(nick, email, pass, birthDate, (Image) null);

            }

        } else {
            errorRegister.setVisible(true);
            errorRegister.setManaged(true);

        }

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
