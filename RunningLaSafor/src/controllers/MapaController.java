package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;

public class MapaController implements Initializable {

    @FXML
    private Button btnAddMap;
    @FXML
    private TableView<MapRegion> mapsTableView;
    @FXML
    private TableColumn<MapRegion, String> regionCol;
    @FXML
    private TableColumn<MapRegion, Double> latMinCol;
    @FXML
    private TableColumn<MapRegion, Double> latMaxCol;
    @FXML
    private TableColumn<MapRegion, Double> lonMinCol;
    @FXML
    private TableColumn<MapRegion, Double> lonMaxCol;
    @FXML
    private TableColumn<MapRegion, Void> deleteColumn;

    private SportActivityApp app;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        app = SportActivityApp.getInstance();
        
        regionCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        latMinCol.setCellValueFactory(new PropertyValueFactory<>("latMin"));
        latMaxCol.setCellValueFactory(new PropertyValueFactory<>("latMax"));
        lonMinCol.setCellValueFactory(new PropertyValueFactory<>("lonMin"));
        lonMaxCol.setCellValueFactory(new PropertyValueFactory<>("lonMax"));
        
        configurarColumnaBorrar();
        mapsTableView.getItems().addAll(app.getMapRegions());
    }    
    
    private void configurarColumnaBorrar() {
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
                            boolean esborrat = app.removeMapRegion(mapa);
                            
                            if (esborrat) {
                                mapsTableView.getItems().remove(mapa);
                            } else {
                                Alert alerta = new Alert(AlertType.ERROR);
                            alerta.setTitle("Error en esborrar");
                            alerta.setHeaderText("No es pot eliminar el mapa: " + mapa.getName());
                            alerta.setContentText("És un mapa per defecte o està sent usat per alguna activitat");
                            alerta.showAndWait();
                            }
                        });
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