package controllers;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;

public class MapaController implements Initializable {

    @FXML private SplitPane splitPanePrincipal;
    @FXML private VBox panelFormulario;
    @FXML private Button btnToggleForm;

    @FXML private TextField txtRegionName;
    @FXML private TextField txtLatMin;
    @FXML private TextField txtLatMax;
    @FXML private TextField txtLonMin;
    @FXML private TextField txtLonMax;
    @FXML private Button btnBrowse;
    @FXML private Button btnClearImage;

    @FXML private Button btnAddMap;
    @FXML private TableView<MapRegion> mapsTableView;
    @FXML private TableColumn<MapRegion, String> regionCol;
    @FXML private TableColumn<MapRegion, Double> latMinCol;
    @FXML private TableColumn<MapRegion, Double> latMaxCol;
    @FXML private TableColumn<MapRegion, Double> lonMinCol;
    @FXML private TableColumn<MapRegion, Double> lonMaxCol;
    @FXML private TableColumn<MapRegion, Void> deleteColumn;

    private SportActivityApp app;
    private File imagenSeleccionada;
    private boolean formularioVisible = false;
    private ObservableList<MapRegion> mapas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        app = SportActivityApp.getInstance();
        
        mapas = FXCollections.observableArrayList(app.getMapRegions());
        
        //IA per al SplitPane
        if (splitPanePrincipal != null) {
            splitPanePrincipal.setDividerPositions(1.0);
        }
        
        regionCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        latMinCol.setCellValueFactory(new PropertyValueFactory<>("latMin"));
        latMaxCol.setCellValueFactory(new PropertyValueFactory<>("latMax"));
        lonMinCol.setCellValueFactory(new PropertyValueFactory<>("lonMin"));
        lonMaxCol.setCellValueFactory(new PropertyValueFactory<>("lonMax"));
        
        configurarColumnaBorrar();
        
        mapsTableView.setItems(mapas);
    }    
    
    @FXML
    private void handleToggleForm(ActionEvent event) {
        if (formularioVisible) {
            splitPanePrincipal.setDividerPositions(1.0);
            btnToggleForm.setText("+ Afegir Mapa");
            formularioVisible = false;
        } else {
            splitPanePrincipal.setDividerPositions(0.65);
            btnToggleForm.setText("× Tancar");
            formularioVisible = true;
        }
    }

    @FXML
    private void handleSelectImage(ActionEvent event) {
        //IA per al fileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imatge del Mapa");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imatges JPG", "*.jpg", "*.jpeg")
        );
        
        Stage stage = (Stage) btnBrowse.getScene().getWindow();
        imagenSeleccionada = fileChooser.showOpenDialog(stage);
        
        if (imagenSeleccionada != null) {
            btnBrowse.setText("✓ " + imagenSeleccionada.getName());
            btnBrowse.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
            btnClearImage.setVisible(true);
            btnClearImage.setManaged(true);
        }
    }

    @FXML
    private void handleClearImage(ActionEvent event) {
        imagenSeleccionada = null;
        btnBrowse.setText("Seleccionar imatge...");
        btnBrowse.setStyle("-fx-background-color: #2200CC75; -fx-text-fill: white; -fx-font-weight: bold;");
        btnClearImage.setVisible(false);
        btnClearImage.setManaged(false);
    }

    @FXML
    private void handleSaveMap(ActionEvent event) {
        if (txtRegionName.getText().trim().isEmpty() || imagenSeleccionada == null ||
            txtLatMin.getText().trim().isEmpty() || txtLatMax.getText().trim().isEmpty() ||
            txtLonMin.getText().trim().isEmpty() || txtLonMax.getText().trim().isEmpty()) {
            
            mostrarAlerta(AlertType.WARNING, "Camps Incomplets", "Per favor, emplena tots els camps i selecciona una imatge.");
            return;
        }

        try {
            String nombreRegion = txtRegionName.getText().trim();
            double latMin = Double.parseDouble(txtLatMin.getText().trim());
            double latMax = Double.parseDouble(txtLatMax.getText().trim());
            double lonMin = Double.parseDouble(txtLonMin.getText().trim());
            double lonMax = Double.parseDouble(txtLonMax.getText().trim());
            
            MapRegion nuevaRegion = app.addMapRegion(
                nombreRegion, 
                imagenSeleccionada, 
                latMin, latMax, lonMin, lonMax
            );
            
            if (nuevaRegion != null) {
                mapas.add(nuevaRegion);
                limpiarFormulario();
            } else {
                mostrarAlerta(AlertType.ERROR, "Error de Sistema", "No s'ha pogut crear la regió del mapa.");
            }
            
        } catch (NumberFormatException e) {
            mostrarAlerta(AlertType.ERROR, "Error de Format", "Les coordenades de bounding box han de ser números decimals vàlids.");
        }
    }
    
    private void limpiarFormulario() {
        txtRegionName.clear();
        txtLatMin.clear();
        txtLatMax.clear();
        txtLonMin.clear();
        txtLonMax.clear();
        imagenSeleccionada = null;
        
        btnBrowse.setText("Seleccionar imatge...");
        btnBrowse.setStyle("-fx-background-color: #2200CC75; -fx-text-fill: white; -fx-font-weight: bold;");
        btnClearImage.setVisible(false);
        btnClearImage.setManaged(false);
    }

    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    
    private void configurarColumnaBorrar() {
        //IA per a la columna del boto de borrar
        Callback<TableColumn<MapRegion, Void>, TableCell<MapRegion, Void>> cellFactory = 
        new Callback<TableColumn<MapRegion, Void>, TableCell<MapRegion, Void>>() {
            @Override
            public TableCell<MapRegion, Void> call(final TableColumn<MapRegion, Void> param) {
                final TableCell<MapRegion, Void> cell = new TableCell<MapRegion, Void>() {
                    
                    private final Button btnBorrar = new Button();

                    {
                        Image img = new Image(getClass().getResourceAsStream("/imatges/papelera.png"));
                        ImageView view = new ImageView(img);
                        
                        view.setFitWidth(20);
                        view.setFitHeight(20);
                        view.setPreserveRatio(true);
                        
                        btnBorrar.setGraphic(view);
                        btnBorrar.getStyleClass().add("boto-paperera");                           
                        
                        btnBorrar.setOnAction(event -> {
                            MapRegion mapa = getTableView().getItems().get(getIndex());
    
                            Alert confirmacio = new Alert(AlertType.CONFIRMATION);
                            confirmacio.setTitle("Confirmar esborrat");
                            confirmacio.setHeaderText("Vas a eliminar el mapa: " + mapa.getName());
                            confirmacio.setContentText("Estàs segur? Aquesta acció no es pot desfer.");
    
                            Optional resultat = confirmacio.showAndWait();
    
                            if (resultat.isPresent() && resultat.get() == javafx.scene.control.ButtonType.OK) {
                            boolean esborrat = app.removeMapRegion(mapa);
        
                            if (esborrat) {
                                mapas.remove(mapa);
                            } else {
                                Alert alerta = new Alert(AlertType.ERROR);
                                alerta.setTitle("Error en esborrar");
                                alerta.setHeaderText("No es pot eliminar el mapa: " + mapa.getName());
                                alerta.setContentText("És un mapa per defecte o està sent usat per alguna activitat");
                                alerta.showAndWait();
                            }
                        }
                    });
                        
                        setAlignment(javafx.geometry.Pos.CENTER);
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnBorrar);
                        }
                    }
                };
                return cell;
            }
        };

        deleteColumn.setCellFactory(cellFactory);
    }
}