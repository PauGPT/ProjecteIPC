/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import upv.ipc.sportlib.Session;
import upv.ipc.sportlib.SportActivityApp;

/**
 * FXML Controller class
 *
 * @author JOAN
 */
public class historialDeSesiones implements Initializable {

    @FXML private TableView<Session> sesionesTableView;
    @FXML private TableColumn<Session, String> fechaInicioCol;
    @FXML private TableColumn<Session, String> fechaFinCol;
    @FXML private TableColumn<Session, String> duracionCol;
    @FXML private TableColumn<Session, Integer> importadasCol;
    @FXML private TableColumn<Session, Integer> vistasCol;
    @FXML private TableColumn<Session, Integer> anotacionesCol;

    @FXML private Label lblTotalTiempo;
    @FXML private Label lblTotalImportadas;
    @FXML private Label lblTotalVistas;
    @FXML private Label lblTotalAnotaciones;
    
    private ObservableList<Session> sesiones = null;
    private SportActivityApp app;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        app = SportActivityApp.getInstance();
        
        if (app.getCurrentUser() != null) {
            sesiones = FXCollections.observableArrayList(app.getSessionsByUser(app.getCurrentUser()));
        } else {
            sesiones = FXCollections.observableArrayList();
        }
        sesionesTableView.setItems(sesiones);
        
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        
        fechaInicioCol.setCellValueFactory(celda -> {
            if (celda.getValue().getStartTime() != null) {
                return new SimpleStringProperty(celda.getValue().getStartTime().format(formatador));
            }
            return new SimpleStringProperty("-");
        });
        
        fechaFinCol.setCellValueFactory(celda -> {
            if (celda.getValue().getEndTime() != null) {
                return new SimpleStringProperty(celda.getValue().getEndTime().format(formatador));
            }
            return new SimpleStringProperty("-");
        });
        
        duracionCol.setCellValueFactory(celda -> {
            if (celda.getValue().getDuration() != null) {
                long minuts = celda.getValue().getDuration().toMinutes();
                return new SimpleStringProperty(minuts + " min");
            }
            return new SimpleStringProperty("0 min");
        });

        importadasCol.setCellValueFactory(new PropertyValueFactory<>("importedActivities"));
        vistasCol.setCellValueFactory(new PropertyValueFactory<>("viewedActivities"));
        anotacionesCol.setCellValueFactory(new PropertyValueFactory<>("annotationsCreated"));
        
        actualizarTotals();
    }    

    private void actualizarTotals() {
        long tempsTotalMinuts = 0;
        int totalImportadas = 0;
        int totalVistas = 0;
        int totalAnotaciones = 0;
        
        for (Session s : sesiones) {
            if (s.getDuration() != null) {
                tempsTotalMinuts += s.getDuration().toMinutes();
            }
            totalImportadas += s.getImportedActivities();
            totalVistas += s.getViewedActivities();
            totalAnotaciones += s.getAnnotationsCreated();
        }
        
        lblTotalTiempo.setText(tempsTotalMinuts + " min");
        lblTotalImportadas.setText(String.valueOf(totalImportadas));
        lblTotalVistas.setText(String.valueOf(totalVistas));
        lblTotalAnotaciones.setText(String.valueOf(totalAnotaciones));
    }
}