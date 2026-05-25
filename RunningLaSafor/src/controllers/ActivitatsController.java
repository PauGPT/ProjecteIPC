package controllers;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
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
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.MapProjection;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.TrackPoint;
import upv.ipc.sportlib.Annotation;
import upv.ipc.sportlib.AnnotationType;
import upv.ipc.sportlib.GeoPoint;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.text.Text;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.Cursor;


public class ActivitatsController implements Initializable {

    @FXML
    private SplitPane splitPane;
    @FXML
    private ListView<Activity> activitiesListView;
    @FXML
    private Button btnImportActivity;
    @FXML
    private Button btnDeleteActivity;

    
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
    private Circle chartHighlightMarker;
    private Line chartHighlightLine;
    @FXML
    private VBox detailVBox;

    //Variable auxiliars per a les anotacions
    private boolean waitingForSecondPoint = false;
    private AnnotationType pendingType;
    private GeoPoint pendingFirstPoint;
    private ContextMenu activeContextMenu;
    private Node previewShape;



    @Override
    public void initialize(URL url, ResourceBundle rb) {
        app = SportActivityApp.getInstance();
        
        //CellFactory per a la llista d'activitats
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

        //Listener per a la selecció d'activitats
        activitiesListView.getSelectionModel().selectedItemProperty().addListener((obs, oV, nV) -> {
            if (nV != null) {
                incrementViewedActivities();
                showActivityDetails(nV);
            } else {
                clearActivityDetails();
            }
        });

        
        refreshActivities();
        
        //Llevar boletes del perfil de desnivell
        elevationChart.setCreateSymbols(false);
        elevationChart.setAnimated(false);

        
        // Codi generat per IA =================================================================================
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
                if (chartHighlightMarker != null) {
                    chartHighlightMarker.setVisible(false);
                }
                if (chartHighlightLine != null) {
                    chartHighlightLine.setVisible(false);
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

                // Show circle and line on the chart
                if (chartHighlightMarker == null) {
                    chartHighlightMarker = new Circle(0, 0, 5, Color.web("#10b981")); // Small green circle
                    chartHighlightMarker.setStroke(Color.BLACK);
                    chartHighlightMarker.setStrokeWidth(1.5);
                    chartHighlightMarker.setMouseTransparent(true);
                }

                if (chartHighlightLine == null) {
                    chartHighlightLine = new Line(0, 0, 0, 0);
                    chartHighlightLine.setStroke(Color.BLACK); // Solid black guide line
                    chartHighlightLine.setStrokeWidth(1.0);
                    chartHighlightLine.setMouseTransparent(true);
                }

                // Add to the plot area parent group or pane if not already present
                Node plotBackground = elevationChart.lookup(".chart-plot-background");
                if (plotBackground != null) {
                    Parent plotParent = plotBackground.getParent();
                    boolean added = false;
                    if (plotParent instanceof javafx.scene.Group) {
                        javafx.scene.Group plotGroup = (javafx.scene.Group) plotParent;
                        if (!plotGroup.getChildren().contains(chartHighlightLine)) {
                            plotGroup.getChildren().add(chartHighlightLine);
                        }
                        if (!plotGroup.getChildren().contains(chartHighlightMarker)) {
                            plotGroup.getChildren().add(chartHighlightMarker);
                        }
                        added = true;
                    } else if (plotParent instanceof Pane) {
                        Pane plotPane = (Pane) plotParent;
                        if (!plotPane.getChildren().contains(chartHighlightLine)) {
                            plotPane.getChildren().add(chartHighlightLine);
                        }
                        if (!plotPane.getChildren().contains(chartHighlightMarker)) {
                            plotPane.getChildren().add(chartHighlightMarker);
                        }
                        added = true;
                    }
                    
                    if (added) {
                        double distVal = currentAccumulatedDistances[closestIndex];
                        double elevVal = closestPoint.getElevation();
                        
                        double displayX = xAxisChart.getDisplayPosition(distVal);
                        double displayY = yAxisChart.getDisplayPosition(elevVal);
                        
                        // Convert axis-relative coordinates to plotParent-relative coordinates
                        double sceneX = xAxisChart.localToScene(displayX, 0).getX();
                        double plotX = plotParent.sceneToLocal(sceneX, 0).getX();
                        
                        double sceneY = yAxisChart.localToScene(0, displayY).getY();
                        double plotY = plotParent.sceneToLocal(0, sceneY).getY();
                        
                        chartHighlightMarker.setCenterX(plotX);
                        chartHighlightMarker.setCenterY(plotY);
                        chartHighlightMarker.setVisible(true);
                        
                        chartHighlightLine.setStartX(plotX);
                        chartHighlightLine.setStartY(plotBackground.getLayoutY());
                        chartHighlightLine.setEndX(plotX);
                        chartHighlightLine.setEndY(plotBackground.getLayoutY() + plotBackground.getLayoutBounds().getHeight());
                        chartHighlightLine.setVisible(true);

                        // Ensure proper Z-ordering (marker on top of line)
                        chartHighlightLine.toFront();
                        chartHighlightMarker.toFront();
                    }
                }
            }
        });
        
        elevationChart.setOnMouseExited(event -> {
            if (highlightMarker != null) {
                highlightMarker.setVisible(false);
            }
            if (chartHighlightMarker != null) {
                chartHighlightMarker.setVisible(false);
            }
            if (chartHighlightLine != null) {
                chartHighlightLine.setVisible(false);
            }
        });

        // Click handler on mapPane for creating annotations
        mapPane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                if (waitingForSecondPoint) {
                    waitingForSecondPoint = false;
                    mapPane.setCursor(Cursor.DEFAULT);
                    cleanupPreviewShape();
                    return;
                }
                Activity selected = activitiesListView.getSelectionModel().getSelectedItem();
                if (selected == null || currentProjection == null) {
                    return;
                }
                showMapContextMenu(event.getX(), event.getY(), event.getScreenX(), event.getScreenY());
                event.consume();
            } else if (event.getButton() == MouseButton.PRIMARY) {
                if (activeContextMenu != null && activeContextMenu.isShowing()) {
                    activeContextMenu.hide();
                }
                if (waitingForSecondPoint) {
                    handleSecondPointClicked(event.getX(), event.getY());
                    event.consume();
                }
            }
        });

        // Mouse moved handler on mapPane for rendering live line/circle previews
        mapPane.setOnMouseMoved(event -> {
            if (waitingForSecondPoint && pendingFirstPoint != null && currentProjection != null) {
                double mouseX = event.getX();
                double mouseY = event.getY();
                
                javafx.geometry.Point2D p1 = currentProjection.project(pendingFirstPoint);
                
                cleanupPreviewShape();
                
                if (pendingType == AnnotationType.LINE) {
                    Line tempLine = new Line(p1.getX(), p1.getY(), mouseX, mouseY);
                    tempLine.setStroke(Color.web("#E74C3C"));
                    tempLine.setStrokeWidth(2.0);
                    tempLine.getStrokeDashArray().addAll(5.0, 5.0);
                    tempLine.setMouseTransparent(true);
                    previewShape = tempLine;
                    mapPane.getChildren().add(previewShape);
                } else if (pendingType == AnnotationType.CIRCLE) {
                    double radius = Math.hypot(mouseX - p1.getX(), mouseY - p1.getY());
                    Circle tempCircle = new Circle(p1.getX(), p1.getY(), radius, Color.TRANSPARENT);
                    tempCircle.setStroke(Color.web("#E74C3C"));
                    tempCircle.setStrokeWidth(2.0);
                    tempCircle.getStrokeDashArray().addAll(5.0, 5.0);
                    tempCircle.setMouseTransparent(true);
                    previewShape = tempCircle;
                    mapPane.getChildren().add(previewShape);
                }
            }
        });

        // Inicialització de la vista sense cap activitat seleccionada
        clearActivityDetails();
    }    

    private void incrementViewedActivities() {
        try {
            SportActivityApp appInstance = SportActivityApp.getInstance();
            java.lang.reflect.Field field = appInstance.getClass().getDeclaredField("viewedActivitiesCount");
            field.setAccessible(true);
            int currentCount = (int) field.get(appInstance);
            field.set(appInstance, currentCount + 1);
        } catch (Exception e) {
            System.err.println("Error incrementing viewed activities counter: " + e.getMessage());
        }
    }

    private void refreshActivities() {
        if (app.getCurrentUser() != null) {
            activitiesListView.getItems().setAll(app.getActivitiesByUser(app.getCurrentUser()));
        }
    }

    private void clearActivityDetails() {
        activityNameLabel.setText("Selecciona una activitat");
        /*lblDistance.setText("0.0 km");
        lblDuration.setText("00:00:00");
        lblPace.setText("0:00 /km");
        lblElevationGain.setText("+0 m");
        lblElevationLoss.setText("-0 m");
        */
        
        showItems(false);
        
        mapImageView.setImage(null);
        mapPane.getChildren().removeIf(node -> node != mapImageView);
        elevationChart.getData().clear();
        xAxisChart.setAutoRanging(true);
        if (chartHighlightMarker != null) {
            chartHighlightMarker.setVisible(false);
        }
        if (chartHighlightLine != null) {
            chartHighlightLine.setVisible(false);
        }
        
        mapScale.setX(1.0);
        mapScale.setY(1.0);
        btnDeleteActivity.setDisable(true);
        waitingForSecondPoint = false;
        mapPane.setCursor(Cursor.DEFAULT);
        cleanupPreviewShape();
        if (activeContextMenu != null && activeContextMenu.isShowing()) {
            activeContextMenu.hide();
        }
    }
    
    // Modifica la visibilitat dels elements del detailVBox excepte la etiqueta amb el nom/títol
    private void showItems(boolean show) {
        detailVBox.setVisible(true);
        for (int i = 1; i < detailVBox.getChildren().size(); i++) {
            Node child = detailVBox.getChildren().get(i);
            child.setVisible(show);
            child.setManaged(show);
        }
    }

    private void showActivityDetails(Activity activity) {
        if (activeContextMenu != null && activeContextMenu.isShowing()) {
            activeContextMenu.hide();
        }
        btnDeleteActivity.setDisable(false);
        showItems(true);
        mapScale.setX(1.0);
        mapScale.setY(1.0);
        //Nom de l'activitat
        activityNameLabel.setText(activity.getName());

        //Estadístiques de l'activitat
        //Distancia en km
        double distanceKm = activity.getTotalDistance() / 1000.0;
        lblDistance.setText(String.format(java.util.Locale.getDefault(), "%.2f km", distanceKm));

        //Duració en hh:mm:ss
        long totalSeconds = activity.getDuration().getSeconds();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        lblDuration.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

        //Imprimir velocitat
        double paceRaw = activity.getAveragePace();
        int paceMinutes = (int) paceRaw;
        int paceSeconds = (int) Math.round((paceRaw - paceMinutes) * 60);
        if (paceSeconds == 60) {
            paceMinutes++;
            paceSeconds = 0;
        }
        lblPace.setText(String.format("%d:%02d /km", paceMinutes, paceSeconds));

        //Imprimir desnivell
        lblElevationGain.setText(String.format(java.util.Locale.getDefault(), "+%.0f m", activity.getElevationGain()));
        lblElevationLoss.setText(String.format(java.util.Locale.getDefault(), "-%.0f m", activity.getElevationLoss()));

        // Posar el mapa i la ruta sobre ell
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

        //Dibuixar perfil de desnivell
        drawElevationChart(activity);
    }

    private void drawRouteOnMap(Activity activity, MapRegion region, double width, double height) {
        //Borrar tots els punts excepte el imageview
        mapPane.getChildren().removeIf(node -> node != mapImageView);
        
        //Llista amb tots els punts del .gpx
        List<TrackPoint> points = activity.getTrackPoints();
        
        if (points == null || points.size() < 2) return; //ruta amb 0/1 punts

        currentProjection = new MapProjection(region, width, height);
        
        //Forma anterior de buscar velocitat màxima i mínima
        
        int numPoints = points.size();
//        double minSpeed = Double.MAX_VALUE;
//        double maxSpeed = -Double.MAX_VALUE;
        double[] speeds = new double[numPoints - 1];
        List<Double> validSpeeds = new ArrayList<>();
        
        for (int i = 0; i < numPoints - 1; i++) {
            TrackPoint current = points.get(i);
            TrackPoint next = points.get(i + 1);
            double speed = current.speedTo(next);
            
            if (Double.isNaN(speed) || Double.isInfinite(speed)) {
                speed = 0.0;
            }
            
            /*
            if (speed < minSpeed) {
                minSpeed = speed;
            }
            if (speed > maxSpeed) {
                maxSpeed = speed;
            }
            */
            
            speeds[i] = speed;
            validSpeeds.add(speed);
            
        }
        
        Collections.sort(validSpeeds);
        
        //Calcular percentils 5 i 95 per a ignorar valors anòmals de velocitat
        int p5index = (int) Math.round((validSpeeds.size() * 0.05));
        int p95index = (int) Math.round((validSpeeds.size() * 0.95));
        //Per si de cas
        if (p95index >= validSpeeds.size()) p95index = validSpeeds.size() - 1;
        
        
        double minSpeed = validSpeeds.get(p5index);
        double maxSpeed = validSpeeds.get(p95index);

        double speedRange = maxSpeed - minSpeed;

        List<Node> segments = new java.util.ArrayList<>();

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
            
            //Canviar el color segons la velocitat
            
            Color colorLent = Color.color(0.18, 0.8, 0.44);    // Verd
            Color colorMig = Color.color(0.95, 0.75, 0.07);    // Taronja
            Color colorRapid = Color.color(0.9, 0.22, 0.13);   // Roig
            
            Color color;
            if (factor < 0.5) {
                color = colorLent.interpolate(colorMig, factor * 2.0);
            } else {
                color = colorMig.interpolate(colorRapid, (factor - 0.5) * 2.0);
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
            highlightMarker = new Circle(0, 0, 8, Color.web("#10b981"));
            highlightMarker.setStroke(Color.BLACK);
            highlightMarker.setStrokeWidth(2.0);
        }
        highlightMarker.setVisible(false);
        mapPane.getChildren().add(highlightMarker);
        
        drawAnnotations(activity);

        // Enfocar el mapa al punt d'inici de la ruta
        javafx.application.Platform.runLater(() -> {
            double viewportWidth = mapScrollPane.getViewportBounds().getWidth();
            double viewportHeight = mapScrollPane.getViewportBounds().getHeight();
            
            if (viewportWidth <= 0) viewportWidth = mapScrollPane.getWidth();
            if (viewportHeight <= 0) viewportHeight = mapScrollPane.getHeight();
            if (viewportWidth <= 0) viewportWidth = 500;
            if (viewportHeight <= 0) viewportHeight = 350;
            
            double hMax = width - viewportWidth;
            double vMax = height - viewportHeight;
            
            if (hMax > 0) {
                double hVal = (startProj.getX() - (viewportWidth / 2.0)) / hMax;
                mapScrollPane.setHvalue(Math.max(0.0, Math.min(1.0, hVal)));
            } else {
                mapScrollPane.setHvalue(0.5);
            }
            
            if (vMax > 0) {
                double vVal = (startProj.getY() - (viewportHeight / 2.0)) / vMax;
                mapScrollPane.setVvalue(Math.max(0.0, Math.min(1.0, vVal)));
            } else {
                mapScrollPane.setVvalue(0.5);
            }
        });
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
        btnBrowse.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6px; -fx-font-weight: bold;");
        btnBrowse.setOnMouseEntered(e -> btnBrowse.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6px; -fx-font-weight: bold;"));
        btnBrowse.setOnMouseExited(e -> btnBrowse.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6px; -fx-font-weight: bold;"));
        
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
                    Alert errAlert = new Alert(Alert.AlertType.ERROR);
                    errAlert.setTitle("Error");
                    errAlert.setHeaderText("S'ha produït una excepció");
                    errAlert.setContentText("Error en eliminar l'activitat: " + e.getMessage());
                    errAlert.showAndWait();
                }
            }
        }
    }

    private void showMapContextMenu(double clickX, double clickY, double screenX, double screenY) {
        if (activeContextMenu != null && activeContextMenu.isShowing()) {
            activeContextMenu.hide();
        }
        
        ContextMenu contextMenu = new ContextMenu();
        activeContextMenu = contextMenu;
        
        MenuItem itemPoint = new MenuItem("Afegir Punt");
        MenuItem itemText = new MenuItem("Afegir Text");
        MenuItem itemLine = new MenuItem("Afegir Línia");
        MenuItem itemCircle = new MenuItem("Afegir Cercle");
        
        GeoPoint gp1 = currentProjection.unproject(clickX, clickY);
        
        itemPoint.setOnAction(e -> showAnnotationDialogAndCreate(gp1, null, AnnotationType.POINT));
        itemText.setOnAction(e -> showAnnotationDialogAndCreate(gp1, null, AnnotationType.TEXT));
        
        itemLine.setOnAction(e -> {
            pendingFirstPoint = gp1;
            pendingType = AnnotationType.LINE;
            waitingForSecondPoint = true;
            mapPane.setCursor(Cursor.CROSSHAIR);
        });
        
        itemCircle.setOnAction(e -> {
            pendingFirstPoint = gp1;
            pendingType = AnnotationType.CIRCLE;
            waitingForSecondPoint = true;
            mapPane.setCursor(Cursor.CROSSHAIR);
        });
        
        contextMenu.getItems().addAll(itemPoint, itemText, itemLine, itemCircle);
        contextMenu.show(mapPane, screenX, screenY);
    }

    private void handleSecondPointClicked(double clickX, double clickY) {
        waitingForSecondPoint = false;
        mapPane.setCursor(Cursor.DEFAULT);
        cleanupPreviewShape();
        
        if (currentProjection == null || pendingFirstPoint == null || pendingType == null) {
            return;
        }
        
        GeoPoint gp2 = currentProjection.unproject(clickX, clickY);
        showAnnotationDialogAndCreate(pendingFirstPoint, gp2, pendingType);
        
        pendingFirstPoint = null;
        pendingType = null;
    }

    private void cleanupPreviewShape() {
        if (previewShape != null) {
            mapPane.getChildren().remove(previewShape);
            previewShape = null;
        }
    }

    private void showAnnotationDialogAndCreate(GeoPoint gp1, GeoPoint gp2, AnnotationType type) {
        Activity selected = activitiesListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        Dialog<Annotation> dialog = new Dialog<>();
        dialog.setTitle("Nova anotació");
        dialog.setHeaderText("Introdueix els detalls de l'anotació");
        dialog.initOwner(mapPane.getScene().getWindow());
        
        ButtonType createButtonType = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        TextField txtText = new TextField();
        txtText.setPromptText("Text de l'anotació");
        txtText.setPrefWidth(200);
        
        ColorPicker colorPicker = new ColorPicker(Color.web("#E74C3C"));
        
        Slider sliderWidth = new Slider(1.0, 10.0, 2.0);
        sliderWidth.setShowTickMarks(true);
        sliderWidth.setShowTickLabels(true);
        sliderWidth.setMajorTickUnit(2.0);
        sliderWidth.setMinorTickCount(1);
        sliderWidth.setSnapToTicks(true);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        int row = 0;
        if (type == AnnotationType.TEXT) {
            grid.add(new Label("Text:"), 0, row);
            grid.add(txtText, 1, row);
            row++;
        }
        
        grid.add(new Label("Color:"), 0, row);
        grid.add(colorPicker, 1, row);
        row++;
        
        if (type != AnnotationType.TEXT) {
            grid.add(new Label("Grossor del traç (px):"), 0, row);
            grid.add(sliderWidth, 1, row);
            row++;
        }
        
        dialog.getDialogPane().setContent(grid);
        
        if (type == AnnotationType.TEXT) {
            javafx.application.Platform.runLater(txtText::requestFocus);
        }
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String text = (type == AnnotationType.TEXT) ? txtText.getText().trim() : "";
                Color c = colorPicker.getValue();
                String colorHex = String.format("#%02X%02X%02X",
                    (int) (c.getRed() * 255),
                    (int) (c.getGreen() * 255),
                    (int) (c.getBlue() * 255)
                );
                double strokeWidth = sliderWidth.getValue();
                
                List<GeoPoint> points;
                if (gp2 == null) {
                    points = List.of(gp1);
                } else {
                    points = List.of(gp1, gp2);
                }
                
                return new Annotation(type, text, colorHex, strokeWidth, points);
            }
            return null;
        });
        
        Optional<Annotation> result = dialog.showAndWait();
        result.ifPresent(ann -> {
            Annotation saved = app.addAnnotation(selected, ann);
            if (saved != null) {
                showActivityDetails(selected);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("No s'ha pogut guardar l'anotació");
                alert.setContentText("Hi ha hagut un error en guardar l'anotació a la base de dades.");
                alert.showAndWait();
            }
        });
    }

    private void drawAnnotations(Activity activity) {
        if (activity == null || currentProjection == null) return;
        List<Annotation> annotations = activity.getAnnotations();
        if (annotations == null) return;
        
        for (Annotation ann : annotations) {
            drawSingleAnnotation(ann);
        }
    }

    private void drawSingleAnnotation(Annotation ann) {
        Color color = Color.RED;
        try {
            color = Color.web(ann.getColor());
        } catch (Exception e) {
            // fallback
        }
        double strokeWidth = ann.getStrokeWidth();
        if (strokeWidth <= 0) strokeWidth = 2.0;
        
        List<GeoPoint> geoPoints = ann.getGeoPoints();
        if (geoPoints == null || geoPoints.isEmpty()) return;
        
        switch (ann.getType()) {
            case POINT: {
                GeoPoint gp = geoPoints.get(0);
                javafx.geometry.Point2D p = currentProjection.project(gp);
                
                Circle circle = new Circle(p.getX(), p.getY(), 6.0, color);
                circle.setStroke(Color.WHITE);
                circle.setStrokeWidth(1.0);
                
                mapPane.getChildren().add(circle);
                
                addDeleteContextMenu(circle, ann);
                break;
            }
            case TEXT: {
                GeoPoint gp = geoPoints.get(0);
                javafx.geometry.Point2D p = currentProjection.project(gp);
                
                String text = ann.getText();
                if (text == null || text.trim().isEmpty()) {
                    text = "Text";
                }
                Text txt = new Text(text);
                txt.setFill(color);
                txt.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                DropShadow ds = new DropShadow();
                ds.setRadius(3.0);
                ds.setColor(Color.WHITE);
                txt.setEffect(ds);
                
                txt.setX(p.getX());
                txt.setY(p.getY());
                mapPane.getChildren().add(txt);
                
                addDeleteContextMenu(txt, ann);
                break;
            }
            case LINE: {
                if (geoPoints.size() < 2) return;
                javafx.geometry.Point2D p1 = currentProjection.project(geoPoints.get(0));
                javafx.geometry.Point2D p2 = currentProjection.project(geoPoints.get(1));
                
                Line line = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                line.setStroke(color);
                line.setStrokeWidth(strokeWidth);
                line.setStrokeLineCap(StrokeLineCap.ROUND);
                mapPane.getChildren().add(line);
                
                addDeleteContextMenu(line, ann);
                break;
            }
            case CIRCLE: {
                if (geoPoints.size() < 2) return;
                javafx.geometry.Point2D center = currentProjection.project(geoPoints.get(0));
                javafx.geometry.Point2D edge = currentProjection.project(geoPoints.get(1));
                
                double radius = Math.hypot(edge.getX() - center.getX(), edge.getY() - center.getY());
                Circle circle = new Circle(center.getX(), center.getY(), radius, Color.TRANSPARENT);
                circle.setStroke(color);
                circle.setStrokeWidth(strokeWidth);
                mapPane.getChildren().add(circle);
                
                addDeleteContextMenu(circle, ann);
                break;
            }
        }
    }

    private void addDeleteContextMenu(Node node, Annotation ann) {
        node.setOnContextMenuRequested(event -> {
            if (activeContextMenu != null && activeContextMenu.isShowing()) {
                activeContextMenu.hide();
            }
            ContextMenu contextMenu = new ContextMenu();
            activeContextMenu = contextMenu;
            MenuItem deleteItem = new MenuItem("Eliminar anotació");
            deleteItem.setOnAction(e -> {
                boolean removed = app.removeAnnotation(ann);
                if (removed) {
                    Activity selected = activitiesListView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        showActivityDetails(selected);
                    }
                }
            });
            contextMenu.getItems().add(deleteItem);
            contextMenu.show(node, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }
}

