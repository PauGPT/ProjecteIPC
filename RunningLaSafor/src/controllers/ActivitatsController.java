package controllers;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Scale;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.MapProjection;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.TrackPoint;

/**
 * FXML Controller class for Activitats
 */
public class ActivitatsController implements Initializable {

    @FXML
    private SplitPane splitPane;
    @FXML
    private ListView<Activity> activitiesListView;
    @FXML
    private Button btnImportActivity;
    @FXML
    private Button btnDeleteActivity;

    // Detail view FXML elements
    @FXML
    private Label activityNameLabel;
    @FXML
    private Label lblDistance;
    @FXML
    private Label lblDuration;
    @FXML
    private Label lblPace;
    @FXML
    private Label lblElevationGain;
    @FXML
    private Label lblElevationLoss;
    @FXML
    private ScrollPane mapScrollPane;
    @FXML
    private Pane mapPane;
    @FXML
    private ImageView mapImageView;
    @FXML
    private AreaChart<Number, Number> elevationChart;
    @FXML
    private NumberAxis xAxisChart;
    @FXML
    private NumberAxis yAxisChart;

    private SportActivityApp app;
    private final Scale mapScale = new Scale(1.0, 1.0, 0.0, 0.0);
    private java.util.List<TrackPoint> currentTrackPoints;
    private double[] currentAccumulatedDistances;
    private MapProjection currentProjection;
    private Circle highlightMarker;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        app = SportActivityApp.getInstance();
        
        // Define Custom CellFactory to show activity name
        activitiesListView.setCellFactory(listView -> new ListCell<Activity>() {
            @Override
            protected void updateItem(Activity activity, boolean empty) {
                super.updateItem(activity, empty);
                if (empty || activity == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(activity.getName());
                }
            }
        });

        // Set listener for selection changes
        activitiesListView.getSelectionModel().selectedItemProperty().addListener((obsVal, oldActivity, newActivity) -> {
            if (newActivity != null) {
                showActivityDetails(newActivity);
            } else {
                clearActivityDetails();
            }
        });

        // Populate the ListView with the current user's activities if they are logged in
        refreshActivities();
        
        // Disable symbols/dots and animations on the elevation chart to avoid JavaFX bugs
        elevationChart.setCreateSymbols(false);
        elevationChart.setAnimated(false);

        // Add zoom support to the map
        mapPane.getTransforms().add(mapScale);
        
        mapPane.setOnScroll(event -> {
            if (event.isControlDown()) {
                double zoomFactor = 1.05;
                if (event.getDeltaY() < 0) {
                    zoomFactor = 2.0 - zoomFactor;
                }
                double newScale = mapScale.getX() * zoomFactor;
                if (newScale >= 1.0 && newScale <= 5.0) {
                    mapScale.setX(newScale);
                    mapScale.setY(newScale);
                }
                event.consume();
            }
        });
        
        mapPane.setOnZoom(event -> {
            double zoomFactor = event.getZoomFactor();
            double newScale = mapScale.getX() * zoomFactor;
            if (newScale >= 1.0 && newScale <= 5.0) {
                mapScale.setX(newScale);
                mapScale.setY(newScale);
            }
            event.consume();
        });

        // Highlight matching point on map when hovering over elevation chart
        elevationChart.setOnMouseMoved(event -> {
            if (currentTrackPoints == null || currentTrackPoints.isEmpty() || currentAccumulatedDistances == null) {
                return;
            }
            javafx.geometry.Point2D localPoint = xAxisChart.sceneToLocal(event.getSceneX(), event.getSceneY());
            double xValue = xAxisChart.getValueForDisplay(localPoint.getX()).doubleValue();
            
            if (xValue < 0.0 || xValue > currentAccumulatedDistances[currentAccumulatedDistances.length - 1]) {
                if (highlightMarker != null) {
                    highlightMarker.setVisible(false);
                }
                return;
            }
            
            // Find closest trackpoint by binary search or simple loop
            int closestIndex = -1;
            double minDiff = Double.MAX_VALUE;
            for (int i = 0; i < currentAccumulatedDistances.length; i++) {
                double diff = Math.abs(currentAccumulatedDistances[i] - xValue);
                if (diff < minDiff) {
                    minDiff = diff;
                    closestIndex = i;
                }
            }
            
            if (closestIndex != -1 && currentProjection != null && highlightMarker != null) {
                TrackPoint closestPoint = currentTrackPoints.get(closestIndex);
                javafx.geometry.Point2D p = currentProjection.project(closestPoint);
                highlightMarker.setCenterX(p.getX());
                highlightMarker.setCenterY(p.getY());
                highlightMarker.setVisible(true);
            }
        });
        
        elevationChart.setOnMouseExited(event -> {
            if (highlightMarker != null) {
                highlightMarker.setVisible(false);
            }
        });
    }    

    private void refreshActivities() {
        if (app.getCurrentUser() != null) {
            activitiesListView.getItems().setAll(app.getActivitiesByUser(app.getCurrentUser()));
        }
    }

    private void clearActivityDetails() {
        activityNameLabel.setText("Selecciona una activitat");
        lblDistance.setText("0.0 km");
        lblDuration.setText("00:00:00");
        lblPace.setText("0:00 /km");
        lblElevationGain.setText("+0 m");
        lblElevationLoss.setText("-0 m");
        
        mapImageView.setImage(null);
        mapPane.getChildren().removeIf(node -> node != mapImageView);
        elevationChart.getData().clear();
        xAxisChart.setAutoRanging(true);
        mapScale.setX(1.0);
        mapScale.setY(1.0);
        btnDeleteActivity.setDisable(true);
    }

    private void showActivityDetails(Activity activity) {
        btnDeleteActivity.setDisable(false);
        mapScale.setX(1.0);
        mapScale.setY(1.0);
        // 1. Title/Name
        activityNameLabel.setText(activity.getName());

        // 2. Statistics
        // Distance in km
        double distanceKm = activity.getTotalDistance() / 1000.0;
        lblDistance.setText(String.format(java.util.Locale.getDefault(), "%.2f km", distanceKm));

        // Duration formatted as hh:mm:ss
        long totalSeconds = activity.getDuration().getSeconds();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        lblDuration.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

        // Average Pace formatted as mm:ss /km
        double paceRaw = activity.getAveragePace();
        int paceMinutes = (int) paceRaw;
        int paceSeconds = (int) Math.round((paceRaw - paceMinutes) * 60);
        if (paceSeconds == 60) {
            paceMinutes++;
            paceSeconds = 0;
        }
        lblPace.setText(String.format("%d:%02d /km", paceMinutes, paceSeconds));

        // Elevation Gain and Loss
        lblElevationGain.setText(String.format(java.util.Locale.getDefault(), "+%.0f m", activity.getElevationGain()));
        lblElevationLoss.setText(String.format(java.util.Locale.getDefault(), "-%.0f m", activity.getElevationLoss()));

        // 3. Map and Route Track Drawing
        MapRegion region = activity.getSuggestedMap();
        if (region == null) {
            region = app.findMapForActivity(activity);
        }
        if (region == null && !app.getMapRegions().isEmpty()) {
            region = app.getMapRegions().get(0);
        }

        if (region != null && region.getImagePath() != null) {
            File imgFile = new File(region.getImagePath());
            if (imgFile.exists()) {
                Image mapImg = new Image(imgFile.toURI().toString());
                mapImageView.setImage(mapImg);
                mapPane.setPrefSize(mapImg.getWidth(), mapImg.getHeight());
                
                drawRouteOnMap(activity, region, mapImg.getWidth(), mapImg.getHeight());
            } else {
                mapImageView.setImage(null);
                mapPane.getChildren().removeIf(node -> node != mapImageView);
            }
        } else {
            mapImageView.setImage(null);
            mapPane.getChildren().removeIf(node -> node != mapImageView);
        }

        // 4. Elevation Profile Chart
        drawElevationChart(activity);
    }

    private void drawRouteOnMap(Activity activity, MapRegion region, double width, double height) {
        // Clear all previous children from mapPane except mapImageView itself
        mapPane.getChildren().removeIf(node -> node != mapImageView);

        java.util.List<TrackPoint> points = activity.getTrackPoints();
        if (points == null || points.size() < 2) {
            return;
        }

        currentProjection = new MapProjection(region, width, height);

        // Find min and max speeds
        int numPoints = points.size();
        double minSpeed = Double.MAX_VALUE;
        double maxSpeed = -Double.MAX_VALUE;
        double[] speeds = new double[numPoints - 1];

        for (int i = 0; i < numPoints - 1; i++) {
            TrackPoint current = points.get(i);
            TrackPoint next = points.get(i + 1);
            double speed = current.speedTo(next);
            
            if (Double.isNaN(speed) || Double.isInfinite(speed)) {
                speed = 0.0;
            }
            speeds[i] = speed;
            if (speed < minSpeed) {
                minSpeed = speed;
            }
            if (speed > maxSpeed) {
                maxSpeed = speed;
            }
        }

        double speedRange = maxSpeed - minSpeed;

        java.util.List<javafx.scene.Node> segments = new java.util.ArrayList<>();

        // Draw track segments colored by speed
        for (int i = 0; i < numPoints - 1; i++) {
            TrackPoint current = points.get(i);
            TrackPoint next = points.get(i + 1);
            
            javafx.geometry.Point2D p1 = currentProjection.project(current);
            javafx.geometry.Point2D p2 = currentProjection.project(next);
            
            double factor = 0.5;
            if (speedRange > 0.0001) {
                factor = (speeds[i] - minSpeed) / speedRange;
            }
            factor = Math.max(0.0, Math.min(1.0, factor));
            
            // Classify color into 3 discrete zones: Green (slow), Orange (medium), Red (fast)
            Color color;
            if (factor < 1.0 / 3.0) {
                color = Color.color(0.18, 0.8, 0.44); // Green
            } else if (factor < 2.0 / 3.0) {
                color = Color.color(0.95, 0.6, 0.07); // Orange
            } else {
                color = Color.color(0.9, 0.22, 0.13); // Red
            }
            
            Line segment = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            segment.setStroke(color);
            segment.setStrokeWidth(4.0); // A bit thicker for better visibility of colors
            segment.setStrokeLineCap(StrokeLineCap.ROUND);
            
            segments.add(segment);
        }

        // Add all segments to pane in a single batch for performance
        mapPane.getChildren().addAll(segments);

        // Draw start (green) and end (red) points
        TrackPoint startPt = points.get(0);
        TrackPoint endPt = points.get(numPoints - 1);

        javafx.geometry.Point2D startProj = currentProjection.project(startPt);
        javafx.geometry.Point2D endProj = currentProjection.project(endPt);

        Circle startMarker = new Circle(startProj.getX(), startProj.getY(), 6, Color.GREEN);
        startMarker.setStroke(Color.WHITE);
        startMarker.setStrokeWidth(1.5);

        Circle endMarker = new Circle(endProj.getX(), endProj.getY(), 6, Color.RED);
        endMarker.setStroke(Color.WHITE);
        endMarker.setStrokeWidth(1.5);

        mapPane.getChildren().addAll(startMarker, endMarker);

        // Initialize and add the highlight marker (hidden by default)
        if (highlightMarker == null) {
            highlightMarker = new Circle(0, 0, 8, Color.GOLD);
            highlightMarker.setStroke(Color.BLACK);
            highlightMarker.setStrokeWidth(2.0);
        }
        highlightMarker.setVisible(false);
        mapPane.getChildren().add(highlightMarker);
    }

    private void drawElevationChart(Activity activity) {
        elevationChart.getData().clear();

        java.util.List<TrackPoint> points = activity.getTrackPoints();
        if (points == null || points.isEmpty()) {
            currentTrackPoints = null;
            currentAccumulatedDistances = null;
            xAxisChart.setAutoRanging(true);
            return;
        }

        currentTrackPoints = points;
        currentAccumulatedDistances = new double[points.size()];
        double accum = 0.0;
        TrackPoint prev = null;
        for (int i = 0; i < points.size(); i++) {
            TrackPoint tp = points.get(i);
            if (prev != null) {
                accum += prev.distanceTo(tp) / 1000.0;
            }
            currentAccumulatedDistances[i] = accum;
            prev = tp;
        }

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Altitud");

        int totalPoints = points.size();
        // Downsample points to ~200 for performance and smooth UI rendering
        int step = Math.max(1, totalPoints / 200);

        for (int i = 0; i < totalPoints; i++) {
            TrackPoint tp = points.get(i);
            double dist = currentAccumulatedDistances[i];
            if (i % step == 0 || i == totalPoints - 1) {
                series.getData().add(new XYChart.Data<>(dist, tp.getElevation()));
            }
        }

        double totalDistance = currentAccumulatedDistances[totalPoints - 1];

        // Set manual bounds on X-axis to match the exact total distance
        xAxisChart.setAutoRanging(false);
        xAxisChart.setLowerBound(0.0);
        xAxisChart.setUpperBound(totalDistance);
        
        // Calculate dynamic tick unit for better readability
        double tickUnit = 1.0;
        if (totalDistance > 0.0) {
            if (totalDistance <= 1.0) {
                tickUnit = 0.2;
            } else if (totalDistance <= 5.0) {
                tickUnit = 1.0;
            } else if (totalDistance <= 15.0) {
                tickUnit = 2.0;
            } else if (totalDistance <= 50.0) {
                tickUnit = 5.0;
            } else if (totalDistance <= 100.0) {
                tickUnit = 10.0;
            } else {
                tickUnit = Math.round(totalDistance / 10.0);
                if (tickUnit == 0.0) tickUnit = 1.0;
            }
        }
        xAxisChart.setTickUnit(tickUnit);

        elevationChart.getData().add(series);
    }

    private void updateActivitySuggestedMap(long activityId, long mapRegionId) {
        String url = "jdbc:sqlite:sportactivity.db";
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(url);
             java.sql.PreparedStatement stmt = conn.prepareStatement("UPDATE activities SET suggested_map_id = ? WHERE id = ?")) {
            stmt.setLong(1, mapRegionId);
            stmt.setLong(2, activityId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleImportActivity(ActionEvent event) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Importar nova activitat");
        dialog.setHeaderText("Introdueix les dades per a importar l'activitat");
        
        dialog.initOwner(btnImportActivity.getScene().getWindow());
        
        ButtonType importButtonType = new ButtonType("Importar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(importButtonType, ButtonType.CANCEL);
        
        TextField txtName = new TextField();
        txtName.setPromptText("Ex: Cursa de Muntanya");
        txtName.setPrefWidth(250);
        
        Label lblFile = new Label("Cap fitxer seleccionat");
        lblFile.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        
        Button btnBrowse = new Button("Seleccionar GPX...");
        btnBrowse.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-cursor: hand;");
        
        ComboBox<MapRegion> comboMaps = new ComboBox<>();
        comboMaps.setPrefWidth(250);
        comboMaps.setCellFactory(lv -> new ListCell<MapRegion>() {
            @Override
            protected void updateItem(MapRegion region, boolean empty) {
                super.updateItem(region, empty);
                if (empty || region == null) {
                    setText(null);
                } else {
                    setText(region.getName());
                }
            }
        });
        comboMaps.setButtonCell(new ListCell<MapRegion>() {
            @Override
            protected void updateItem(MapRegion region, boolean empty) {
                super.updateItem(region, empty);
                if (empty || region == null) {
                    setText(null);
                } else {
                    setText(region.getName());
                }
            }
        });
        
        comboMaps.getItems().setAll(app.getMapRegions());
        if (!comboMaps.getItems().isEmpty()) {
            comboMaps.getSelectionModel().select(0);
        }
        
        File[] selectedFile = new File[1];
        
        btnBrowse.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar Fitxer GPX");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fitxers GPX (*.gpx)", "*.gpx")
            );
            File file = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                selectedFile[0] = file;
                lblFile.setText(file.getName());
                lblFile.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                
                if (txtName.getText().trim().isEmpty()) {
                    String baseName = file.getName();
                    if (baseName.lastIndexOf(".") > 0) {
                        baseName = baseName.substring(0, baseName.lastIndexOf("."));
                    }
                    txtName.setText(baseName);
                }
            }
        });
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 10, 20));
        
        grid.add(new Label("Nom de l'activitat:"), 0, 0);
        grid.add(txtName, 1, 0);
        
        grid.add(new Label("Fitxer GPX:"), 0, 1);
        HBox fileBox = new HBox(10, btnBrowse, lblFile);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(fileBox, 1, 1);
        
        grid.add(new Label("Mapa a utilitzar:"), 0, 2);
        grid.add(comboMaps, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        Node importButton = dialog.getDialogPane().lookupButton(importButtonType);
        importButton.setDisable(true);
        
        lblFile.textProperty().addListener((observable, oldValue, newValue) -> {
            importButton.setDisable(newValue.equals("Cap fitxer seleccionat"));
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == importButtonType) {
                if (selectedFile[0] != null) {
                    try {
                        Activity newActivity = app.importActivity(selectedFile[0]);
                        if (newActivity != null) {
                            String customName = txtName.getText().trim();
                            if (!customName.isEmpty()) {
                                app.renameActivity(newActivity, customName);
                                newActivity.setName(customName);
                            }
                            
                            MapRegion selectedMap = comboMaps.getValue();
                            if (selectedMap != null) {
                                updateActivitySuggestedMap(newActivity.getId(), selectedMap.getId());
                            }
                            return true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return false;
        });
        
        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            refreshActivities();
            if (!activitiesListView.getItems().isEmpty()) {
                Activity lastAdded = activitiesListView.getItems().stream()
                    .max((a1, a2) -> Long.compare(a1.getId(), a2.getId()))
                    .orElse(null);
                if (lastAdded != null) {
                    activitiesListView.getSelectionModel().select(lastAdded);
                }
            }
        }
    }

    @FXML
    private void handleDeleteActivity(ActionEvent event) {
        Activity selected = activitiesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Suprimir activitat");
            alert.setHeaderText(null);
            alert.setContentText("Estàs segur que vols eliminar permanentment l'activitat '" + selected.getName() + "'?");
            alert.initOwner(btnDeleteActivity.getScene().getWindow());
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    boolean removed = app.removeActivity(selected);
                    if (removed) {
                        javafx.application.Platform.runLater(() -> {
                            activitiesListView.getSelectionModel().clearSelection();
                            refreshActivities();
                            clearActivityDetails();
                        });
                    } else {
                        Alert errAlert = new Alert(Alert.AlertType.ERROR);
                        errAlert.setTitle("Error");
                        errAlert.setHeaderText("No s'ha pogut eliminar l'activitat");
                        errAlert.setContentText("Hi ha hagut un error en eliminar l'activitat de la base de dades.");
                        errAlert.showAndWait();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert errAlert = new Alert(Alert.AlertType.ERROR);
                    errAlert.setTitle("Error");
                    errAlert.setHeaderText("S'ha produït una excepció");
                    errAlert.setContentText("Error en eliminar l'activitat: " + e.getMessage());
                    errAlert.showAndWait();
                }
            }
        }
    }
}

