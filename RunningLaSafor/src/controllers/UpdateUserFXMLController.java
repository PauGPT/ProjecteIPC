/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

import application.App;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import javafx.stage.Stage;
import javafx.util.StringConverter;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class UpdateUserFXMLController implements Initializable {

    private File selectedAvatarFile = null;

    private SportActivityApp app = SportActivityApp.getInstance();
    User user;

    private boolean errorEmail = false;
    private boolean errorPassword = false;
    private boolean errorBirthDate = false;

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
    private StackPane emailContainer;
    @FXML
    private StackPane passwordContainer;
    @FXML
    private StackPane birthContainer;
    @FXML
    private Label userLabel;
    @FXML
    private VBox passwordRequirementsBox;
    @FXML
    private Label reqLength;
    @FXML
    private Label reqUppercase;
    @FXML
    private Label reqLowercase;
    @FXML
    private Label reqDigit;
    @FXML
    private Label reqSymbol;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        passTextField.textProperty().bindBidirectional(passField.textProperty());
        setupPasswordRequirements();

        user = app.getCurrentUser();

        if (user.getAvatarPath() != null) {
            selectedAvatarFile = new File(user.getAvatarPath());
            Image image = new Image(selectedAvatarFile.toURI().toString());
            avatarCircle.setFill(new ImagePattern(image));
            avatarPlaceholder.setVisible(false);
            btnRemoveAvatar.setVisible(true);
            btnRemoveAvatar.setManaged(true);
        }

        birthDatePicker.setValue(user.getBirthDate());
        emailField.setText(user.getEmail());
        userLabel.setText("Modifica les teues dades, " + user.getNickName());

        passField.setText(user.getPassword());
        passTextField.setText(user.getPassword());

        emailField.focusedProperty().addListener((value, oldValue, newValue) -> {
            if (!newValue) {
                if (emailField.getText().isEmpty()) {
                    errorEmail = true;
                    mostrarErrorEnCampo(emailContainer, "El mail no pot estar buit.");
                } else if (!User.checkEmail(emailField.getText())) {
                    errorEmail = true;
                    mostrarErrorEnCampo(emailContainer, "El format ha de ser usuari@domini");
                } else {
                    errorEmail = false;
                    ocultarErrorEnCampo(emailContainer);
                }
            }
        });

        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (errorEmail) {
                if (!newValue.isEmpty() && User.checkEmail(newValue)) {
                    errorEmail = false;
                    ocultarErrorEnCampo(emailContainer);
                }
            }
        });

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        birthDatePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return formatter.format(date);
                }
                return "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.trim().isEmpty()) {
                    try {
                        return LocalDate.parse(string, formatter);
                    } catch (Exception e) {
                        return null;
                    }
                }
                return null;
            }
        });

        TextField dateTextField = birthDatePicker.getEditor();

        dateTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() < oldValue.length()) {
                return;
            }

            if (newValue.length() > 10) {
                dateTextField.setText(oldValue);
                return;
            }

            String cleaned = newValue.replaceAll("[^0-9/]", "");
            String digits = cleaned.replaceAll("/", "");
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < digits.length(); i++) {
                sb.append(digits.charAt(i));
                if (i == 1 || i == 3) {
                    sb.append("/");
                }
            }

            if (!newValue.equals(sb.toString())) {
                dateTextField.setText(sb.toString());
                dateTextField.positionCaret(sb.length());
            }

            if (errorBirthDate && sb.length() == 10) {
                try {
                    LocalDate date = LocalDate.parse(sb.toString(), formatter);
                    if (User.isOlderThan(date, 12)) {
                        errorBirthDate = false;
                        ocultarErrorEnCampo(birthContainer);
                    }
                } catch (Exception e) {
                }
            }
        });

        birthDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (errorBirthDate && newVal != null && User.isOlderThan(newVal, 12)) {
                errorBirthDate = false;
                ocultarErrorEnCampo(birthContainer);
            }
        });

        birthDatePicker.focusedProperty().addListener((value, oldValue, newValue) -> {
            if (!newValue) {
                try {
                    String text = birthDatePicker.getEditor().getText();
                    if (text != null && !text.isEmpty()) {
                        birthDatePicker.setValue(birthDatePicker.getConverter().fromString(text));
                    }
                } catch (Exception e) {
                    birthDatePicker.setValue(null);
                }

                if (birthDatePicker.getValue() == null) {
                    errorBirthDate = true;
                    mostrarErrorEnCampo(birthContainer, "La data de naixement no pot estar buida");
                } else if (!User.isOlderThan(birthDatePicker.getValue(), 12)) {
                    errorBirthDate = true;
                    mostrarErrorEnCampo(birthContainer, "Has de tindre més de 12 anys per a registrar-te");
                } else {
                    errorBirthDate = false;
                    ocultarErrorEnCampo(birthContainer);
                }
            }
        });

        Platform.runLater(() -> {
            if (emailField.getScene() != null) {
                emailField.getScene().getRoot().requestFocus();
            }
        });
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
    private void actualizarUsuario(ActionEvent event) {
        if (!errorEmail && !errorPassword && !errorBirthDate) {

            errorRegister.setVisible(false);
            errorRegister.setManaged(false);

            String email = emailField.getText();
            String pass = passField.getText();
            LocalDate birthDate = birthDatePicker.getValue();

            if (selectedAvatarFile != null) {
                String avatarPath = selectedAvatarFile.getAbsolutePath();
                app.updateCurrentUser(email, pass, birthDate, avatarPath);
            } else {
                app.updateCurrentUser(email, pass, birthDate, (Image) null);
            }

            App.getMainController().updateMenuAvatar();

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Actualització de dades");
            alert.setHeaderText(null);
            alert.setContentText("Les teues dades s'han actualitzat correctament");
            
            java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
            
            if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                Stage stage = (Stage) registerButton.getScene().getWindow();

                App.getMainController().updateMenuAvatar();
                App.getMainController().updateToolbarState();
                App.getMainController().deselectMenuButtons();
                App.getMainController().loadView(App.Vista.DASHBOARD_LOGGED);

                if (stage != null) {
                    stage.setMaximized(true);
                }
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

    private void setupPasswordRequirements() {
        passField.focusedProperty().addListener((obs, oldVal, newValue) -> handlePasswordFocus(newValue));
        passTextField.focusedProperty().addListener((obs, oldVal, newValue) -> handlePasswordFocus(newValue));

        passField.textProperty().addListener((obs, oldVal, newValue) -> {
            if (passField.isFocused()) {
                updateRequirements(newValue);
            }
            if (errorPassword && !newValue.trim().isEmpty() && User.checkPassword(newValue.trim())) {
                errorPassword = false;
                ocultarErrorEnCampo(passwordContainer);
            }
        });

        passTextField.textProperty().addListener((obs, oldVal, newValue) -> {
            if (passTextField.isFocused()) {
                updateRequirements(newValue);
            }
            if (errorPassword && !newValue.trim().isEmpty() && User.checkPassword(newValue.trim())) {
                errorPassword = false;
                ocultarErrorEnCampo(passwordContainer);
            }
        });

        passField.focusedProperty().addListener((value, oldValue, newValue) -> {
            if (!newValue) {
                validatePasswordOnBlur();
            }
        });

        passTextField.focusedProperty().addListener((value, oldValue, newValue) -> {
            if (!newValue) {
                validatePasswordOnBlur();
            }
        });
    }

    private void handlePasswordFocus(boolean hasFocus) {
        if (hasFocus || passField.isFocused() || passTextField.isFocused()) {
            passwordRequirementsBox.setVisible(true);
            passwordRequirementsBox.setManaged(true);
            String currentPassword = passField.isFocused() ? passField.getText() : passTextField.getText();
            updateRequirements(currentPassword);
        } else {
            passwordRequirementsBox.setVisible(false);
            passwordRequirementsBox.setManaged(false);
        }
    }

    private void updateRequirements(String password) {
        if (password == null) {
            password = "";
        }

        boolean isLengthValid = password.length() >= 8 && password.length() <= 20;
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[^a-zA-Z0-9].*");

        toggleCriterion(reqLength, "8-20 Caràcters", isLengthValid);
        toggleCriterion(reqUppercase, "Almenys una majúscula", hasUppercase);
        toggleCriterion(reqLowercase, "Almenys una minúscula", hasLowercase);
        toggleCriterion(reqDigit, "Almenys un dígit", hasDigit);
        toggleCriterion(reqSymbol, "Almenys un símbol", hasSymbol);
    }

    private void toggleCriterion(Label label, String text, boolean isValid) {
        label.getStyleClass().removeAll("req-valid", "req-invalid");
        if (isValid) {
            label.setText("✔ " + text);
            label.getStyleClass().add("req-valid");
        } else {
            label.setText("✘ " + text);
            label.getStyleClass().add("req-invalid");
        }
    }

    private void validatePasswordOnBlur() {
        if (passField.isFocused() || passTextField.isFocused()) {
            return;
        }

        String password = passTextField.isVisible() ? passTextField.getText() : passField.getText();
        if (password == null) {
            password = "";
        }
        password = password.trim();

        if (password.isEmpty()) {
            errorPassword = true;
            mostrarErrorEnCampo(passwordContainer, "La contrasenya no pot estar buida");
        } else if (!User.checkPassword(password)) {
            errorPassword = true;
            mostrarErrorEnCampo(passwordContainer, "La contrasenya no cumpleix els requisits");
        } else {
            errorPassword = false;
            ocultarErrorEnCampo(passwordContainer);
        }
    }

}